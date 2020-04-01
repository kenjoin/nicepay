package com.nicepay.nicepay.service;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSONObject;
import com.nicepay.nicepay.client.LocalReceiver;
import com.nicepay.nicepay.client.entry.PayInfo;


public class MyNotificationListenerService extends NotificationListenerService {


    /**
     * 用于判断是否屏幕是亮着的
     */
    private boolean isScreenOn;

    /**
     * 获取PowerManager.WakeLock对象
     */
    private PowerManager.WakeLock wakeLock;
    /**
     * KeyguardManager.KeyguardLock对象
     */
    private KeyguardManager.KeyguardLock keyguardLock;

    private LocalBroadcastManager localBroadcastManager;
    private LocalReceiver localReceiver;
    private IntentFilter intentFilter;

    @Override
    public void onCreate() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.nyl.orderlybroadcast.AnotherBroadcastReceiver");
        localReceiver = new LocalReceiver();
        //注册本地接收器
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //取消绑定
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        // 如果熄屏状态，则亮屏再解锁
//        if (!isScreenOn()) {
//            wakeUpScreen();
//        }

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

            Handler handlerThree = new Handler(Looper.getMainLooper());
            handlerThree.post(new Runnable(){
                public void run(){
                    Toast.makeText(getApplicationContext() ,"show:" + title + "\n" + content + "\n" + pkg ,Toast.LENGTH_LONG).show();
                }
            });

            PayInfo payInfo = new PayInfo();
            payInfo.setTitle(title);
            payInfo.setContent(content);
            payInfo.setPackageName(pkg);

            Intent intent = new Intent("com.nyl.orderlybroadcast.AnotherBroadcastReceiver");
            intent.putExtra("msg", JSONObject.toJSONString(payInfo));
            //发送本地广播
            localBroadcastManager.sendBroadcast(intent);
        }

    }


    /**
     * 判断是否处于亮屏状态
     *
     * @return true-亮屏，false-暗屏
     */
    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        isScreenOn = pm.isScreenOn();
        Log.e("isScreenOn", isScreenOn + "");
        return isScreenOn;
    }

    /**
     * 解锁屏幕
     */
    @SuppressLint("InvalidWakeLockTag")
    private void wakeUpScreen() {

        //获取电源管理器对象
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //后面的参数|表示同时传入两个值，最后的是调试用的Tag
        wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "bright");

        //点亮屏幕
        wakeLock.acquire();

        //得到键盘锁管理器
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        keyguardLock = km.newKeyguardLock("unlock");

        //解锁
        keyguardLock.disableKeyguard();
    }


}
