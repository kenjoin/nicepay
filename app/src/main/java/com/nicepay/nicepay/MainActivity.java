package com.nicepay.nicepay;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView showLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("info", "===============================run ok...===============================");

        showLog = findViewById(R.id.showLog);

        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url("http://www.yikouhui.com/setShield.shtml?enable=false")
                .get()//默认就是GET请求，可以不写
                .build();

        Call call = client.newCall(request);

        try {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
//                Toast.makeText(getApplicationContext(), "发送失败" + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("log", "发送失败" + e.getMessage());
//                    showLog.setText("发送失败" + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String msg = response.body().string();
                    Log.d("log", "=====================发送成功" + msg);
                    Log.d("log", "=====================发送成功" + msg);


                    // TODO 这里如何修改showLog控件显示的字符
//                    showLog.setText("发送成功" + msg);

                    // TODO 这里能弹Toast不？
//                    Toast.makeText(getApplicationContext(), "发送成功" + msg, Toast.LENGTH_LONG).show();
                }
            });

            showLog.setText("执行成功");
        }catch (Exception e) {
            showLog.setText("执行异常" + e.getMessage());
        }

    }
}
