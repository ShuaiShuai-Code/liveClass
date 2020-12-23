package com.pili.pldroid.playerdemo;

import android.app.Application;

import cn.jpush.im.android.api.JMessageClient;

public class NimApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JMessageClient.setDebugMode(true);
        JMessageClient.init(this);
    }
}
