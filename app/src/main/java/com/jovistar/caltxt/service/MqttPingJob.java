package com.jovistar.caltxt.service;
/*
import java.util.Calendar;

import com.jovistar.caltxt.ui.CaltxtApp;
import com.jovistar.caltxt.ui.CaltxtToast;
import com.jovistar.caltxt.ui.Constants;
import com.jovistar.commons.util.Logr;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class MqttPingJob extends JobService {
	private static final String TAG = "MqttPingJob";

	public MqttPingJob() {
		super();
	}

	@Override
	public boolean onStartJob(JobParameters arg0) {

		CaltxtToast.acquirePartialWakeLock();//added 29-DEC-16
		RebootService.getConnection().publishKeepAlive();

		schedule();
		CaltxtToast.releasePartialWakeLock();//added 29-DEC-16

		return false;
	}

	@Override
	public boolean onStopJob(JobParameters arg0) {

		return false;
	}

	public static void cancel() {
        JobScheduler jobScheduler = (JobScheduler) CaltxtApp.getCustomAppContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(Constants.JOB_ID_MQTT_KEEPALIVE);
	}

	public static void schedule() {

		if(WifiScanAlarm.isJobScheduled(Constants.JOB_ID_MQTT_KEEPALIVE)) {
			cancel();
		}

		ComponentName serviceComponent = new ComponentName(CaltxtApp.getCustomAppContext(), MqttPingJob.class);
        JobInfo.Builder builder = new JobInfo.Builder(Constants.JOB_ID_MQTT_KEEPALIVE, serviceComponent);
        builder.setMinimumLatency(Constants.mqtt_defaultKeepAlive/2); // wait at least
        builder.setOverrideDeadline(Constants.mqtt_defaultKeepAlive); // maximum delay
        // network not required, otherwise non-network related functions will also be paused)
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setRequiresDeviceIdle(false); // device should be idle
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) CaltxtApp.getCustomAppContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);

		jobScheduler.schedule(builder.build());

		Log.v(TAG, TAG+"::scheduleJob complete");

	}
}*/
