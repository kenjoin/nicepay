package com.nicepay.nicepay.client;

import android.os.Message;

import com.nicepay.nicepay.MainActivity;
import com.nicepay.nicepay.client.entry.PayInfo;

public class ServiceMsgHanlder extends Thread {

    private String title;
    private String content;
    private String pkg;

    public ServiceMsgHanlder(String title, String content, String pkg) {
        this.title = title;
        this.content = content;
        this.pkg = pkg;
    }

    @Override
    public void run() {
/*
        PayInfo payInfo = new PayInfo();
        payInfo.setTitle(title);
        payInfo.setContent(content);
        payInfo.setPackageName(pkg);

        payInfo.setTitle("微信支付");
        payInfo.setContent("微信支付，内容：微信支付收款0.09元(朋友到店)====>" + content);
        payInfo.setPackageName("com.tencent.mm");

        PayManager.get().processOnReceive(new PayBackcall(payInfo) {

            @Override
            protected void success(Message msg) {
                MainActivity.mhandler.sendMessage(msg);
            }

            @Override
            protected void failure(Message msg) {
                MainActivity.mhandler.sendMessage(msg);
            }
        });*/

    }
}
