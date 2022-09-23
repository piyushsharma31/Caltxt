package com.jovistar.caltxt.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class ScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenReceiver";

    static ScreenReceiver iam;
    public static boolean wasScreenOn = true;
    long idleStartTime = 0, idleEndTime = 0;

    Context context;

    private ScreenReceiver(Context context) {
        this.context = context;
    }

    public static ScreenReceiver getInstance(Context context) {
        if (iam == null) {
            iam = new ScreenReceiver(context);
        }
        return iam;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive ON " + idleEndTime);
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // do whatever you need to do here
            wasScreenOn = false;
            idleStartTime = Calendar.getInstance().getTimeInMillis();
            Log.v(TAG, "onReceive OFF " + idleStartTime);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // and do whatever you need to do here
            wasScreenOn = true;
            idleEndTime = Calendar.getInstance().getTimeInMillis();
            Log.v(TAG, "onReceive ON " + idleEndTime);
            Toast.makeText(context, "Screen ON after " + getIdleTime(), Toast.LENGTH_LONG).show();
        }
    }

    public String getIdleTime() {
        if (idleEndTime < idleStartTime)
            return "0 minutes";

        return DateUtils.getRelativeDateTimeString(context, idleEndTime - idleStartTime,
                DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME).toString();
    }
}
