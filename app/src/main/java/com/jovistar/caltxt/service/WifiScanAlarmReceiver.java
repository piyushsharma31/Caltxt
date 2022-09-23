package com.jovistar.caltxt.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.legacy.content.WakefulBroadcastReceiver;

import com.jovistar.caltxt.app.Constants;

import java.util.Calendar;

/*
 * Doze restrictions (deep doze:device is not charging, the screen is off, and the 
 * device is completely stationary). 'completely stationary' requirement is not 
 * mandatory to into doze mode in Nougat
 * Lite doze: both the screen is off and the device is not charging:
 * 1. The system does not perform Wi-Fi scans
 * 2. The system does not allow JobScheduler to run
 * 3. The system ignores wake locks
 * 4. Network access is suspended
 * 
 * When a device is on battery power, and the screen has been off for a certain time, 
 * the device enters Doze and applies the first subset of restrictions: It shuts off 
 * app network access, and defers jobs and syncs.
 * If the device is stationary for a certain time after entering Doze, the system 
 * applies the rest of the Doze restrictions to PowerManager.WakeLock, AlarmManager 
 * alarms, GPS, and Wi-Fi scans
 */
public class WifiScanAlarmReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "WifiScanAlarmReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        // check if device has moved, scan WiFi
        if (MotionReceiver.getInstance().hasMoved(context)) {
            Log.d(TAG, "onReceive started");
            // hasMoved will always return true if no Motion sensor available on the device
            // start cell id scan to discover this place
            final WifiScanReceiver wifiSR = new WifiScanReceiver();
            wifiSR.startWifiAndCellScan(context.getApplicationContext());
        }

        // register motion receiver again, in case receiver was killed
//        MotionReceiver.getInstance().registerMotionReceiver(context.getApplicationContext());
    }

    public static void schedule(Context context) {

        if (isAlarmSet(context, Constants.JOB_ID_WIFI_NETWORK_SCAN))
            return;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(am==null) {
            return;
        }

        Intent intent = new Intent(context, WifiScanAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context,
                Constants.JOB_ID_WIFI_NETWORK_SCAN, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar cal = Calendar.getInstance();
        // start 30 seconds after
        cal.add(Calendar.SECOND, 30);

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);

        Log.d(TAG, "AlarmManager::schedule");
    }

    public static void _cancel(Context context) {

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WifiScanAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context,
                Constants.JOB_ID_WIFI_NETWORK_SCAN/*unique id for PI*/, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.cancel(pi);

        Log.d(TAG, "WifiScan::cancel");
    }

    public static boolean isAlarmSet(Context context, int id) {
        boolean alarmUp = (PendingIntent.getBroadcast(context, id,
                new Intent(context, WifiScanAlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        if(alarmUp) {
            Log.d(TAG, "isAlarmSet " + alarmUp);
        }
        return alarmUp;

    }
}
