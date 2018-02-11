package com.example.android.testdemo.deamon;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yxb on 2018/2/9.
 * 守护管理类，用来管理所有的服务
 */

public class DaemonManager {

    public static final int DEFAULT_WAKE_UP_INTERVAL = 10 * 1000;//默认唤醒时间
    private static final int MINIMAL_WAKE_UP_INTERVAL = 3 * 1000;//最小唤醒时间

    public static Context hostContext;

    private static final Map<Class<? extends Service>, ServiceConnection> BIND_STATE_MAP = new HashMap<>();

    private static boolean inited;
    private static int mInterval = DEFAULT_WAKE_UP_INTERVAL;

    public static void init(Context context, int interval){
        hostContext = context;
        mInterval = interval;
        inited = true;
    }

    /**
     * 启动需要被守护的进程service
     */
    public static void startDeamonService(){
        startServiceMayBind(AliveService.class);
    }

    /**
     * 启动守护进程service
     */
    public static void startWatchService(){
        startServiceMayBind(WatchService.class);
    }

    /**
     * 绑定的目的：可以当内存不足被回收时回调onServiceDisconnected，在这里再拉起进程
     * @param serviceClass
     */
    public static void startServiceMayBind(@NonNull final Class<? extends Service> serviceClass) {
        if (!inited) throw new RuntimeException("please init first!");
        final Intent i = new Intent(hostContext, serviceClass);
        startServiceSafely(i);
        ServiceConnection connection = BIND_STATE_MAP.get(serviceClass);
        if (connection == null) hostContext.bindService(i, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                BIND_STATE_MAP.put(serviceClass, this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                BIND_STATE_MAP.remove(serviceClass);
                startServiceSafely(i);
                hostContext.bindService(i, this, Context.BIND_AUTO_CREATE);
            }

            @Override
            public void onBindingDied(ComponentName name) {
                onServiceDisconnected(name);
            }
        }, Context.BIND_AUTO_CREATE);
    }

    static void startServiceSafely(Intent i) {
        if (!inited) throw new RuntimeException("please init first!");
        try { hostContext.startService(i); } catch (Exception ignored) {}
    }

    static int getWakeUpInterval() {
        return Math.max(mInterval, MINIMAL_WAKE_UP_INTERVAL);
    }
}
