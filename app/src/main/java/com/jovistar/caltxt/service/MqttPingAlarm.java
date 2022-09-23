package com.jovistar.caltxt.service;

/*
 * PING must occur at specific interval of time when network is available,
 * preferably before KEEP_ALIVE time. When device is in DOZE state,
 * ping will suspend (as network access suspends during doze)
 */
// 02-FEB-17, using Paho Mqtt Service, so no need to ping, taken care by service
// automatically
/*
public class MqttPingAlarm extends BroadcastReceiver {
	private static final String TAG = "MqttPingAlarm";

	@Override
	public void onReceive(Context context, Intent arg1) {
		CaltxtToast.acquirePartialWakeLock(context);

		Log.d(TAG, "MqttPingAlarm::onReceive");

		RebootService.getConnection(context.getApplicationContext()).publishKeepAlive();

		//schedule next ping
		schedule(context.getApplicationContext());

		CaltxtToast.releasePartialWakeLock();//added 29-DEC-16
	}

	public static void schedule(Context context) {

//		if(WifiScanAlarm.isJobScheduled(context,Constants.JOB_ID_MQTT_KEEPALIVE))
//			cancel(context);

		if (Build.VERSION.SDK_INT >= 23) {

			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, MqttPingAlarm.class);

			PendingIntent pi = PendingIntent.getBroadcast(context,
					Constants.JOB_ID_MQTT_KEEPALIVE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

			am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 
					Calendar.getInstance().getTimeInMillis()+(Constants.mqtt_defaultKeepAlive/2), pi);
//		} else if (Build.VERSION.SDK_INT >= 21) {
//
//			ComponentName serviceComponent = new ComponentName(CaltxtApp.getCustomAppContext(), MqttPingJob.class);
//	        JobInfo.Builder builder = new JobInfo.Builder(Constants.JOB_ID_MQTT_KEEPALIVE, serviceComponent);
//	        builder.setMinimumLatency(Constants.mqtt_defaultKeepAlive/2); // wait at least
//	        builder.setOverrideDeadline(Constants.mqtt_defaultKeepAlive); // maximum delay
	         network not required, otherwise non-network related functions will also be paused)
//	        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
//	        builder.setRequiresDeviceIdle(false); // device should be idle
//	        builder.setRequiresCharging(false); // we don't care if the device is charging or not
//	        JobScheduler jobScheduler = (JobScheduler) CaltxtApp.getCustomAppContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
//
//			jobScheduler.schedule(builder.build());
		} else {

			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, MqttPingAlarm.class);

			PendingIntent pi = PendingIntent.getBroadcast(context,
					Constants.JOB_ID_MQTT_KEEPALIVE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

			am.set(AlarmManager.RTC_WAKEUP, 
					Calendar.getInstance().getTimeInMillis()+(Constants.mqtt_defaultKeepAlive/2), pi);
		}

		Log.v(TAG, TAG+"::SetAlarm complete");

	}

	public static void cancel(Context context) {
//		if (Build.VERSION.SDK_INT >= 21) {
//	        JobScheduler jobScheduler = (JobScheduler) CaltxtApp.getCustomAppContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
//	        jobScheduler.cancel(Constants.JOB_ID_MQTT_KEEPALIVE);
//		} else {
			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, RulesAlarm.class);
			PendingIntent pi = PendingIntent.getBroadcast(context,
					Constants.JOB_ID_MQTT_KEEPALIVE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	
			am.cancel(pi);
//		}

		Log.v(TAG, "AlarmReceiver::cancel complete for JOB_ID_MQTT_KEEPALIVE");
	}
}
*/