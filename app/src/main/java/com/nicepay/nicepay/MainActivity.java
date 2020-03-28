package com.nicepay.nicepay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nicepay.nicepay.conf.GlobalSettings;
import com.nicepay.nicepay.service.MyNotificationListenerService;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static TextView showLog;
    private Button button;
    private Button button2;
    private EditText editText;

    public static mHandler mhandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("info", "===============================run ok...===============================");
        mhandler = new mHandler(this);

        showLog = findViewById(R.id.showLog);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        editText = findViewById(R.id.editText);

        String string = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (!string.contains(MyNotificationListenerService.class.getName())) {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                Log.d("d", "===================================="+text);
                GlobalSettings.POST_URL = text;
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testOkHttp();
            }
        });

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mhandler != null){
            mhandler.removeCallbacksAndMessages(null);
        }
    }

    private void testOkHttp() {

        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(GlobalSettings.POST_URL)
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

                    String msg1 = response.body().string();
                    Log.d("log", "=====================发送成功" + msg1);
                    Log.d("log", "=====================发送成功" + msg1);


                    // TODO 这里如何修改showLog控件显示的字符
//                    showLog.setText("发送成功" + msg);

                    Message msg = new Message();
                    msg.what = 01;
                    msg.obj = msg1;
                    mhandler.sendMessage(msg);

                    // TODO 这里能弹Toast不？
//                    Toast.makeText(getApplicationContext(), "发送成功" + msg1, Toast.LENGTH_LONG).show();
                }
            });

            showLog.setText("执行成功");
        }catch (Exception e) {
            showLog.setText("执行异常" + e.getMessage());
        }
    }


    //Handler静态内部类 防止内存泄漏
    public static class mHandler extends Handler {
        WeakReference<MainActivity> weakReference;
        public mHandler(MainActivity mainActivity){
            weakReference = new WeakReference<MainActivity>(mainActivity);
        }
        @Override
        public void handleMessage(Message msg){
            MainActivity mainActivity = weakReference.get();
            if(mainActivity != null){
                mainActivity.showLog.setText(String.valueOf(msg.obj));
                Toast.makeText(mainActivity.getApplicationContext(), "发送失败" + String.valueOf(msg.obj), Toast.LENGTH_LONG).show();
            }
        }

    }
}
