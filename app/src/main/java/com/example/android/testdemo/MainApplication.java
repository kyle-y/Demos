package com.example.android.testdemo;

import android.app.Application;

import com.example.android.testdemo.deamon.DaemonManager;

/**
 * Created by yxb on 2018/2/9.
 */

public class MainApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        DaemonManager.init(this, 5_000);
    }
}
