package com.nicepay.nicepay;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.nicepay.nicepay.client.PayBackcall;
import com.nicepay.nicepay.client.PayManager;
import com.nicepay.nicepay.client.entry.PayInfo;
import com.nicepay.nicepay.conf.GlobalSettings;
import com.nicepay.nicepay.service.MyNotificationListenerService;
import com.nicepay.nicepay.utils.Constant;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public TextView showLog;
    private Button button;
    public Button button2;
    private Button button3;
    private EditText editText;

    PowerManager.WakeLock mWakeLock;// 电源锁

    public static mHandler mhandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        setRequestedOrientation(ActivityInfo .SCREEN_ORIENTATION_UNSPECIFIED);//竖屏
        Log.d("info", "===============================run ok...===============================");
        mhandler = new mHandler(this);

        showLog = findViewById(R.id.showLog);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        editText = findViewById(R.id.editText);

        String string = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (string == null || !string.contains(MyNotificationListenerService.class.getName())) {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }

        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                Log.d("d", "====================================" + text);
                GlobalSettings.POST_URL = text;
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                testOkHttp();
                test();
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNotificationListenerService();
            }
        });

    }


    /**
     * onCreate时,申请设备电源锁
     */
    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
        if (null == mWakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "myService");
            if (null != mWakeLock) {
                mWakeLock.acquire();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mhandler != null) {
            mhandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock != null) {
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    //重新开启MyNotificationListenerService
    private void toggleNotificationListenerService() {

        if(NotificationManagerCompat.getEnabledListenerPackages(getBaseContext()).contains(getBaseContext().getPackageName())) {
            ComponentName thisComponent = new ComponentName(this,  MyNotificationListenerService.class);
            PackageManager pm = getPackageManager();
            pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            Toast.makeText(getBaseContext(), "执行" + getBaseContext().getPackageName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getBaseContext(), "不执行" + getBaseContext().getPackageName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void test() {

        PayInfo payInfo = new PayInfo();
        payInfo.setTitle("微信支付");
        payInfo.setContent("微信支付，内容：微信支付收款0.01元(朋友到店)");
        payInfo.setPackageName("com.tencent.mm");

        PayManager.get().processOnReceive(new PayBackcall(payInfo){

            @Override
            protected void success(Message msg) {
                MainActivity.mhandler.sendMessage(msg);
            }

            @Override
            protected void failure(Message msg) {
                MainActivity.mhandler.sendMessage(msg);
            }
        });
    }

    private void testOkHttp() {

        showLog.setText("1.准备测试通讯");
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
                    Log.e("log", "发送失败" + e.getMessage());
//                    Toast.makeText(MainActivity.context, "测试通讯失败，" + e.getMessage(), Toast.LENGTH_LONG);
//                    showLog.setText(showLog.getText() + "\n" + "3.测试通讯失败 " + e.getMessage());
//                    Message msg = new Message();
//                    msg.what = Constant.MsgWhat.n_9001;
//                    msg.obj = "3.测试通讯失败 " + e.getMessage();
//                    mhandler.handleMessage(msg);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String msg1 = response.body().string();
                    Log.d("log", "=====================发送成功" + msg1);
//                    Toast.makeText(MainActivity.context, "测试通讯成功" + msg1, Toast.LENGTH_LONG);
//                    showLog.setText(showLog.getText() + "\n" + "3.测试通讯成功 " + msg1);
//                    Message msg = new Message();
//                    msg.what = Constant.MsgWhat.n_9001;
//                    msg.obj = "3.测试通讯成功 " + msg1;
//                    mhandler.handleMessage(msg);
                }
            });
            showLog.setText(showLog.getText() + "\n" + "2.测试操作正常");
        } catch (Exception e) {
            showLog.setText(showLog.getText() + "\n" + "2.测试操作异常 " + e.getMessage());
        }
    }


    //Handler静态内部类 防止内存泄漏
    public static class mHandler extends Handler {
        WeakReference<MainActivity> weakReference;

        public mHandler(MainActivity mainActivity) {
            weakReference = new WeakReference<MainActivity>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = weakReference.get();
            if (mainActivity != null) {
                if (msg.what != Constant.MsgWhat.n_9001) {
                    mainActivity.showLog.setText(String.valueOf(msg.obj));
                } else if (msg.what <= Constant.MsgWhat.n_2) {
                    mainActivity.showLog.setText(mainActivity.showLog.getText() + "\n" + String.valueOf(msg.obj));
                } else {
                    mainActivity.showLog.setText(mainActivity.showLog.getText() + "\n" + String.valueOf(msg.obj));
                }
                Toast.makeText(mainActivity.getApplicationContext(), String.valueOf(msg.obj), Toast.LENGTH_LONG).show();
            }
        }

    }
}
