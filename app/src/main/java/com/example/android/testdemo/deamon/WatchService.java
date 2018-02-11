package com.example.android.testdemo.deamon;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by yxb on 2018/2/9.
 * :x进程，用来不断监听并拉起主进程
 */

public class WatchService extends Service{

    private static final int NOTIFACITION_ID = 101;
    private static final int JOB_ID = 1;
    private PendingIntent pendingIntent;
    private JobInfo jobInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFACITION_ID, new Notification());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            startService(new Intent(getApplicationContext(), InnerService.class));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startJob();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        startJob();
        return null;
    }

    private void startJob(){
        //Android 5.0+ 使用 JobScheduler，效果比 AlarmManager 好
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (jobInfo == null) {
                JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(DaemonManager.hostContext, JobSchedulerService.class));
                builder.setPeriodic(DaemonManager.getWakeUpInterval());
                //Android 7.0+ 增加了一项针对 JobScheduler 的新限制，最小间隔只能是下面设定的数字
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) builder.setPeriodic(JobInfo.getMinPeriodMillis(), JobInfo.getMinFlexMillis());
                builder.setPersisted(true);
                jobInfo = builder.build();
            }
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.schedule(jobInfo);
        } else {
            //Android 4.4- 使用 AlarmManager
            if (pendingIntent == null) {
                Intent i = new Intent(DaemonManager.hostContext, AliveService.class);
                pendingIntent = PendingIntent.getService(DaemonManager.hostContext, JOB_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + DaemonManager.getWakeUpInterval(),
                    DaemonManager.getWakeUpInterval(),
                    pendingIntent);
        }

        //守护 Service 组件的启用状态, 使其不被 MAT 等工具禁用
        getPackageManager().setComponentEnabledSetting(new ComponentName(getPackageName(), AliveService.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private void cancelJob(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler scheduler = (JobScheduler) DaemonManager.hostContext.getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancel(JOB_ID);
        } else {
            AlarmManager am = (AlarmManager) DaemonManager.hostContext.getSystemService(ALARM_SERVICE);
            if (pendingIntent != null) am.cancel(pendingIntent);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        cancelJob();
        DaemonManager.startDeamonService();
        DaemonManager.startWatchService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelJob();
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
