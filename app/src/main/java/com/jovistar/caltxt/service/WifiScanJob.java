/*package com.jovistar.caltxt.service;

import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.network.data.ConnectivityBroadcastReceiver;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class WifiScanJob extends JobService {
    private static final String TAG = "WifiScanJob";

    public WifiScanJob() {
        super();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob");
        if (MotionReceiver.getInstance().hasMoved(getApplicationContext())) {
            Log.d(TAG, "onStartJob started");
            // hasMoved will always return true if no Motion sensor available on the device
            // start cell id scan to discover this place
            final WifiScanReceiver wifiSR = new WifiScanReceiver();
            wifiSR.startWifiAndCellScan(getApplicationContext().getApplicationContext());
            if(ConnectivityBroadcastReceiver.haveNetworkConnection()) {
                Connection.get().submitPingSelfFirebase();
            }
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob getJobId "+params.getJobId());

        return false;
    }

    public static int schedule(Context context) {

        if (isJobScheduled(context, Constants.JOB_ID_WIFI_NETWORK_SCAN)) {
//            cancel(context);
            return JobScheduler.RESULT_SUCCESS;
        }

         // job will not kick in (doze mode) if the device is battery powered, with
		 // screen off (even if moving - courtesy Nougat)
		 // Therefore Wifi scan job will not be initiated in those conditions.
        Log.d(TAG, "WifiScanJob::schedule");

        ComponentName serviceComponent = new ComponentName(context, WifiScanJob.class);
        JobInfo.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder = new JobInfo.Builder(Constants.JOB_ID_WIFI_NETWORK_SCAN, serviceComponent);
//            builder.setMinimumLatency(Constants.CALTXT_WIFI_SCAN_INTERVAL * 1000); // wait at least
//            builder.setOverrideDeadline(Constants.CALTXT_WIFI_SCAN_INTERVAL_MAX * 1000); // maximum delay
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // DO NOT require network
            builder.setRequiresDeviceIdle(false); // device need not be idle
//            builder.setRequiresCharging(false); // we don't care if the device is charging or not
	        builder.setPeriodic(Constants.CALTXT_WIFI_SCAN_INTERVAL * 1000);
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            return jobScheduler.schedule(builder.build());
        } else {
            return JobScheduler.RESULT_FAILURE;
        }
    }

    public static void cancel(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            jobScheduler.cancel(Constants.JOB_ID_WIFI_NETWORK_SCAN);
            Log.d(TAG, "WifiScan::cancel");
        }
    }

    static boolean isJobScheduled(Context context, int jid) {

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> ji = null;
        boolean jobScheduled = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ji = jobScheduler.getAllPendingJobs();

            if (ji == null) {
                jobScheduled = false;
//                return false;
            } else {

                for (int i = 0; i < ji.size(); i++) {
                    if (ji.get(i).getId() == jid) {
                        jobScheduled = true;
//                        return true;
                    }
                }
            }
        }

        if(jobScheduled) {
            Log.d(TAG, "isJobScheduled " + jobScheduled);
        }
        return jobScheduled;
    }

    public static void printAllScheduledJobs(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> ji = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ji = jobScheduler.getAllPendingJobs();

            if (ji == null) {
                return;
            }

            for (int i = 0; i < ji.size(); i++) {
                Log.v(TAG, "printAllScheduled pid " + ji.get(i).getId());
                if (ji.get(i).getId() == 0) {
                    jobScheduler.cancel(0);
                }
            }
        }
    }
}*/