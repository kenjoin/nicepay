package com.nicepay.nicepay.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.nicepay.nicepay.MainActivity;
import com.nicepay.nicepay.R;
import com.nicepay.nicepay.client.PayBackcall;
import com.nicepay.nicepay.client.PayManager;
import com.nicepay.nicepay.client.entry.PayInfo;
import com.nicepay.nicepay.utils.Constant;

import java.lang.ref.WeakReference;


public class MyNotificationListenerService extends NotificationListenerService {


    public static void actionStart(Context ctx) {
//System.out.println("---- Notification service started!");
        Intent i = new Intent(ctx, MyNotificationListenerService.class);
        i.setAction("ACTION_START");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startService(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        actionStart(getBaseContext());
//        acquireWakeLock();
//        toggleNotificationListenerService();
    }


    //重新开启MyNotificationListenerService
    private void toggleNotificationListenerService() {

        if(NotificationManagerCompat.getEnabledListenerPackages(getBaseContext()).contains(getBaseContext().getPackageName())) {
            ComponentName thisComponent = new ComponentName(this,  MyNotificationListenerService.class);
            PackageManager pm = getPackageManager();
            pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            Toast.makeText(getBaseContext(), "执行", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getBaseContext(), "不执行", Toast.LENGTH_SHORT).show();
        }
    }


    public static MainActivity a;


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Notification notifcation = sbn.getNotification();

        if (notifcation == null) {
            return;
        }

        Bundle extras = notifcation.extras;

        if (extras != null) {
            // 获取包名
            String pkg = sbn.getPackageName();
            // 获取通知标题
            String title = extras.getString(Notification.EXTRA_TITLE, "");
            // 获取通知内容
            String content = extras.getString(Notification.EXTRA_TEXT, "");

            Log.i("info", String.format("收到通知，包名：%s，标题：%s，内容：%s", pkg, title, content));

            // 测试代码 start
            Message msg = new Message();
            msg.what = Constant.MsgWhat.n_2;
            msg.obj = "收到通知:" + content;
            a.button2.setText(msg.obj + "");
            MainActivity.mhandler.sendMessage(msg);
            MainActivity.showLog.setText(msg.obj.toString());
            Toast.makeText(a.getApplicationContext(), ">>>" + msg.obj.toString(), Toast.LENGTH_LONG);

            // TODO 这里的测试代码好像走不通。
            
            // 测试代码 end

            PayInfo payInfo = new PayInfo();
            payInfo.setTitle(title);
            payInfo.setContent(content);
            payInfo.setPackageName(pkg);

            PayManager.get().processOnReceive(new PayBackcall(payInfo) {

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

    }


}
