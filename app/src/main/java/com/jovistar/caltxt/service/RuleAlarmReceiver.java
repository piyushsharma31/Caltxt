package com.jovistar.caltxt.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jovistar.caltxt.activity.IFTTT;
import com.jovistar.caltxt.activity.QuickResponseEdit;
import com.jovistar.caltxt.bo.XQrp;
import com.jovistar.caltxt.bo.XRul;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;

import java.util.Calendar;

/*
 * THIS CLASS MUST SCHEDULE ALARMS WHICH TRIGGER AT EXACT TIME EVEN
 * WHEN PHONE IS IDLE OR ASLEEP. AND ARE NOT IMPACTED BY NEW FEATURE 
 * INTRODUCED IN LATER VERSION (>21) LIKE DOZE.
 * Doze restrictions -- 
 * 1. If you need to set alarms that fire while in Doze, use 
 * setAndAllowWhileIdle() or setExactAndAllowWhileIdle()
 * 2. Alarms set with setAlarmClock() continue to 
 * fire normally the system exits Doze shortly before those alarms
 * fire. 
 */
public class RuleAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "RuleAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

//		CaltxtToast.acquirePartialWakeLock(context.getApplicationContext());

        long pid = intent.getLongExtra("PID_RUL", -1);
        if (pid == -1) {
            pid = intent.getLongExtra("PID_QRP", -1);
            //do not process, unknown
            if (pid > 0) {
                XQrp qrp = Persistence.getInstance(context.getApplicationContext()).getQuickAutoResponse();
                if (qrp.getPersistenceId() == pid) {
                    Log.w(TAG, TAG + "::onReceive QRP " + qrp.toString());
                    //reset profile status for auto response
                    QuickResponseEdit.resetAutoResponse(context.getApplicationContext());
                    QuickResponseEdit.promptExtendAutoResponse(context.getApplicationContext());
                } else {
                    Log.w(TAG, TAG + "::OnReceive QRP do not match!!! " + qrp.getPersistenceId()
                            + ", & " + pid);
                }
            }
        } else {
            //execute the rule
            XRul rul = Persistence.getInstance(context.getApplicationContext()).getXRul(pid);
            Log.w(TAG, TAG + "::onReceive RUL " + rul.toString());
            IFTTT.triggerAction(context.getApplicationContext(), rul);
        }

//		CaltxtToast.releasePartialWakeLock();
    }

    public static void SetAlarm(Context context, XRul rule) {
        if (rule.getActionWhen() <= Calendar.getInstance().getTimeInMillis()
                || rule.getPersistenceId() < 0) {
            Log.w(TAG, TAG + "::SetAlarm SKIPPED (time past) for rule " + rule.toString());
            return;
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RuleAlarmReceiver.class);
        intent.putExtra("PID_RUL", rule.getPersistenceId());
        PendingIntent pi = PendingIntent.getBroadcast(context,
                (int) rule.getPersistenceId()/*unique id for PI*/, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT >= 23) {
//			am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, rule.getActionWhen(), pi);
            // Neither setAndAllowWhileIdle() nor setExactAndAllowWhileIdle() can fire alarms more than once per 9 minutes, per app
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, rule.getActionWhen(), pi);
//		} else if (Build.VERSION.SDK_INT >= 21) {
//			AlarmManager.AlarmClockInfo aci = new AlarmManager.AlarmClockInfo(rule.getActionWhen(), pi);
//			am.setAlarmClock(aci, pi);
        } else if (Build.VERSION.SDK_INT >= 19) {
            am.setExact(AlarmManager.RTC_WAKEUP, rule.getActionWhen(), pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, rule.getActionWhen(), pi);
        }

        Log.v(TAG, TAG + "::SetAlarm complete for rule " + rule.toString());
    }

    public static void cancel(Context context, long pid) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RuleAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context,
                (int) pid/*unique id for PI*/, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.cancel(pi);

        Log.v(TAG, TAG + "::cancelAlarm complete for rule pid " + pid);
    }

    public static void SetAutoResponseAlarm(Context context, long pid, long atTime, View actionView) {
        if (atTime <= Calendar.getInstance().getTimeInMillis()) {
            Log.w(TAG, TAG + "::SetAlarm SKIPPED (time past) for Quick resp pid " + pid);
            return;
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(am==null) {
            Notify.toast(actionView, context, "Alarm was not set!", Toast.LENGTH_LONG);
            return;
        }

        Intent intent = new Intent(context, RuleAlarmReceiver.class);
        intent.putExtra("PID_QRP", pid);
        PendingIntent pi = PendingIntent.getBroadcast(context,
                (int) pid/*unique id for PI*/, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT >= 23) {
//			am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atTime, pi);
            // Neither setAndAllowWhileIdle() nor setExactAndAllowWhileIdle() can fire alarms more than once per 9 minutes, per app
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atTime, pi);
//		} else if (Build.VERSION.SDK_INT >= 21) {
//			AlarmManager.AlarmClockInfo aci = new AlarmManager.AlarmClockInfo(atTime, pi);
//			am.setAlarmClock(aci, pi);
        } else if (Build.VERSION.SDK_INT >= 19) {
            am.setExact(AlarmManager.RTC_WAKEUP, atTime, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, atTime, pi);
        }

        Log.v(TAG, TAG + "::SetAlarm complete for rule pid " + pid);
    }
/*
    public static void cancelAutoResponseAlarm(long pid) {
//    	XQrp qrp = Persistence.getInstance(context).getQuickAutoResponse();

//    	if(qrp==null) {
//			Log.w(TAG, TAG+"::cancelAutoResponseAlarm nothing to cancel!!!");
//    		return;
//    	}

    	AlarmManager am = (AlarmManager) CaltxtApp.getCustomAppContext().getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(CaltxtApp.getCustomAppContext(), RuleAlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(CaltxtApp.getCustomAppContext(),
				(int) pid, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		am.cancel(pi);

		Log.v(TAG, TAG+"::cancelAlarm complete for AutoResponse "+pid);
	}
*/
}
