package com.example.android.testdemo.deamon;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.util.Log;

/**
 * Created by yxb on 2018/2/9.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService{
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e("aaa", "唤醒");
        DaemonManager.startDeamonService();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
