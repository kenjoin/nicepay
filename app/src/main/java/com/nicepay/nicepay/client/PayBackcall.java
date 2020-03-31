package com.nicepay.nicepay.client;

import android.os.Message;

import com.nicepay.nicepay.client.entry.PayInfo;

public abstract class PayBackcall {

    private PayInfo payInfo;

    public PayBackcall(PayInfo payInfo) {
        this.payInfo = payInfo;
    }

    protected abstract void success(Message msg);

    protected abstract void failure(Message msg);

    public PayInfo getPayInfo() {
        return payInfo;
    }

    public void setPayInfo(PayInfo payInfo) {
        this.payInfo = payInfo;
    }
}
