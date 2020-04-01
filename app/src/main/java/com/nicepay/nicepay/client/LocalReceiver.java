package com.nicepay.nicepay.client;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.nicepay.nicepay.MainActivity;
import com.nicepay.nicepay.client.entry.PayInfo;
import com.nicepay.nicepay.conf.GlobalSettings;
import com.nicepay.nicepay.utils.Constant;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocalReceiver extends BroadcastReceiver {

    private final static OkHttpClient client = new OkHttpClient();

    @Override
    public void onReceive(Context context, Intent intent) {

        // 后期使用MQTT

        String msg = intent.getExtras().getString("msg");

        Log.e("ddd","收到本地广播" + msg);
        Toast.makeText(context,"收到本地广播" + msg, Toast.LENGTH_SHORT).show();

        PayInfo payInfo = null;
        try{
            payInfo = JSONObject.parseObject(msg, PayInfo.class);
        }catch (Exception e) {
            Toast.makeText(context,"解析异常：" + msg, Toast.LENGTH_SHORT).show();
            return;
        }

        String title = payInfo.getTitle(), content = payInfo.getContent(), pkg = payInfo.getPackageName();

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("payload", JSONObject.toJSON(payInfo));

        final int[] retryNum = {3};
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSONObject.toJSONString(payload));

        final Request request = new Request.Builder()
                .url(GlobalSettings.POST_URL)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);

        PayInfo finalPayInfo = payInfo;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                try {
                    if(--retryNum[0] > 0) {
                        call.execute();
                        Log.d("warn", "推送失败，重试" + retryNum[0] + "次, content: " + content);
                    } else {
                        Log.d("warn", "推送失败，重试无效...");
                        Message msg = new Message();
                        msg.what = Constant.MsgWhat.n_1;
                        msg.obj = e.getMessage();
                    }
                } catch (IOException ex) {
                    Log.e("exception", e.getMessage());
                }

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Message msg = new Message();
                msg.what = Constant.MsgWhat.n_2;
//                msg.obj = response.body().string();
                msg.obj = "推送成功：" + title + "=>" + finalPayInfo.getMoney();

//                MainActivity.mhandler.sendMessage(msg);
//                Log.d("info", "===============================推送成功===============================");
            }
        });
    }

}
