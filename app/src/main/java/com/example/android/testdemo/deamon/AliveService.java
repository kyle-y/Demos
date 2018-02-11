package com.example.android.testdemo.deamon;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by yxb on 2018/2/9.
 * 主进程，作用（1，提高主进程优先级；2，被拉起保证主进程不挂掉）
 */

public class AliveService extends Service{

    private static final int NOTIFACITION_ID = 101;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("aaa", "onCreate");
        startForeground(NOTIFACITION_ID, new Notification());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            startService(new Intent(getApplicationContext(), InnerService.class));
        }
        DaemonManager.startWatchService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("aaa", "onStartCommand");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("aaa", "onBind");
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e("aaa", "onTaskRemoved");
        DaemonManager.startDeamonService();
        DaemonManager.startWatchService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("aaa", "onDestroy");
        DaemonManager.startDeamonService();
        DaemonManager.startWatchService();
    }

    public static class InnerService extends Service{

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(NOTIFACITION_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
    }
}
