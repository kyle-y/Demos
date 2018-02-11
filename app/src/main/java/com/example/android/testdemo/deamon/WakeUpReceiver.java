package com.example.android.testdemo.deamon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by yxb on 2018/2/9.
 * 当接受到某一类广播时，拉起进程
 */

public class WakeUpReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("aaa", "开始守护。。");
        DaemonManager.startDeamonService();
    }
}
