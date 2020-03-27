package com.nicepay.nicepay.service;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MyNotificationListenerService extends NotificationListenerService {

	private static final String POST_URL = "https://www.baidu.com";
//	private static final String POST_URL = "http://api.yikouhui.com/setNotice.shtml";

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

			
			processOnReceive(pkg, title, content);
		}

	}

	/**
	 * 消息来时处理
	 *
	 * @param pkg
	 * @param title
	 * @param content
	 */
	private void processOnReceive(String pkg, String title, String content) {

		OkHttpClient client = new OkHttpClient();
		final Request request = new Request.Builder()
				.url(POST_URL)
				.get()//默认就是GET请求，可以不写
				.build();

		Call call = client.newCall(request);
		call.enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				Toast.makeText(getApplicationContext(), "发送失败" + e.getMessage(), Toast.LENGTH_LONG).show();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				Toast.makeText(getApplicationContext(), "发送成功" + response.body(), Toast.LENGTH_LONG).show();
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
	 * @param gateway
	 * @return
	 */
	private static boolean checkMsgValid(String title, String content, String gateway) {
		if ("wxpay".equals(gateway)) {
			// 微信支付的消息格式
			// 1条：标题：微信支付，内容：微信支付收款0.01元(朋友到店)
			// 多条：标题：微信支付，内容：[4条]微信支付: 微信支付收款1.01元(朋友到店)
			Pattern pattern = Pattern.compile("^((\\[\\+?\\d+条])?微信支付:|微信支付收款)");
			Matcher matcher = pattern.matcher(content);
			return "微信支付".equals(title) && matcher.find();
		} else if ("alipay".equals(gateway)) {
			// 支付宝的消息格式，标题：支付宝通知，内容：支付宝成功收款1.00元。
			return "支付宝通知".equals(title);
		}
		return false;
	}

}
