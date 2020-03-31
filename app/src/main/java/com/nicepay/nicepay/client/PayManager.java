package com.nicepay.nicepay.client;

import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.nicepay.nicepay.client.entry.PayInfo;
import com.nicepay.nicepay.conf.GlobalSettings;
import com.nicepay.nicepay.utils.Constant;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PayManager {

    // 线程池
    private final ExecutorService threadPool = new ThreadPoolExecutor(1, 100, 10000, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(5), new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread th = new Thread(r, "threadPool" + r.hashCode());
            return th;
        }
    }, new ThreadPoolExecutor.CallerRunsPolicy());

    private static PayManager payManager = new PayManager();

    private PayManager(){}

    public static PayManager get(){
        return payManager;
    }

    public void processOnReceive(final PayBackcall payBackcall) {

        PayInfo payInfo = payBackcall.getPayInfo();

        String title = payInfo.getTitle(), content = payInfo.getContent(), pkg = payInfo.getPackageName();

        // 检验消息合法性
        if(!checkMsgValid(title, content, pkg)) {
            // 非法消息
            Message msg = new Message();
            msg.what = Constant.MsgWhat.n_0;
            msg.obj = "非法消息" + pkg;
            payBackcall.failure(msg);
            return;
        }

        // 获取金额
        payInfo.setMoney(parseMoney(content));

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("payload", JSONObject.toJSON(payInfo));

        final int[] retryNum = {3};
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSONObject.toJSONString(payload));

        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(GlobalSettings.POST_URL)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);
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
                        payBackcall.failure(msg);
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
                msg.obj = "推送成功：" + title + "=>" + payInfo.getMoney();
                payBackcall.success(msg);
            }
        });

    }




    /**
     * 解析内容字符串，提取金额
     *
     * @param content
     * @return
     */
    private static String parseMoney(String content) {
        Pattern pattern = Pattern.compile("收款(([1-9]\\d*)|0)(\\.(\\d){0,2})?元");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String tmp = matcher.group();
            Pattern patternnum = Pattern.compile("(([1-9]\\d*)|0)(\\.(\\d){0,2})?");
            Matcher matchernum = patternnum.matcher(tmp);
            if (matchernum.find())
                return matchernum.group();
        }
        return null;
    }

    /**
     * 验证消息的合法性，防止非官方消息被处理
     *
     * @param title
     * @param content
     * @param pkg
     * @return
     */
    private static boolean checkMsgValid(String title, String content, String pkg) {
        if ("com.eg.android.AlipayGphone".equals(pkg)) {
            // 支付宝的消息格式，标题：支付宝通知，内容：支付宝成功收款1.00元。
            return "支付宝通知".equals(title);
        } else if ("com.tencent.mm".equals(pkg)) {
            // 微信支付的消息格式
            // 1条：标题：微信支付，内容：微信支付收款0.01元(朋友到店)
            // 多条：标题：微信支付，内容：[4条]微信支付: 微信支付收款1.01元(朋友到店)
            Pattern pattern = Pattern.compile("^((\\[\\+?\\d+条])?微信支付:|微信支付收款)");
            Matcher matcher = pattern.matcher(content);
            return "微信支付".equals(title);// && matcher.find();
        }
        return false;
    }

}
