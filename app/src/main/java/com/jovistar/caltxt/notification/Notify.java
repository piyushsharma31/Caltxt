/*
 * Licensed Materials - Property of IBM
 *
 * 5747-SM3
 *
 * (C) Copyright IBM Corp. 1999, 2012 All Rights Reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 *
 */
package com.jovistar.caltxt.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.snackbar.Snackbar;
import com.jovistar.caltxt.R;
import com.jovistar.caltxt.activity.CaltxtPager;
import com.jovistar.caltxt.activity.QuickResponseEdit;
import com.jovistar.caltxt.activity.CaltxtStatusPager;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.phone.Addressbook;

import java.util.Calendar;

/**
 * Provides static methods for creating and showing notifications to the user.
 */
public class Notify {

    private static final String TAG = "Notify";

    /**
     * Message ID Counter
     **/
    // private static int MessageID = 1;
//	private static int MSGID_CONN_ERROR = 7989;
    private static int MSGID_AUTO_RESPONSE_SET = 7990;
    private static int MSGID_ALERT_MISSED = 7991;
    private static int MSGID_ALERT = 7992;
    private static int MSGID_ALERT_CONNECTION = 7993;
    private static int MSGID_BLOCKED = 7994;
    private static int MSGID_AUTORESPONSE_SENT = 7995;
    private static int MSGID_DND = 7996;
    private static int MSGID_ALERT_MISSED_REPLY = 7991;
    private static int MSGID_ALERT_STATUS_CHANGE = 7997;
    private static int MSGID_ALERT_CELLLOC = 7998;
    private static int MSGID_ALERT_CELLINFO = 7999;
    private static int MSGID_TRIGGER_ACTION = 8000;

    public static int auto_responded_call_count = 0;
    public static int dnd_rejected_call_count = 0;
    public static int blocked_call_count = 0;
    public static int missed_call_count = 0;
    public static int missed_text_count = 0;
    public static int trigger_action_count = 0;
    public static int missed_call_response_count = 0;//response text to call

    /**
     * Displays a notification in the notification area of the UI
     *
     * @param context           Context from which to create the notification
     * @param messageString     The string to display to the user as a message
     * @param intent            The intent which will start the activity when the user clicks
     *                          the notification
     * @param notificationTitle The resource reference to the notification title
     *                          <p>
     *                          void notifcation231(Context context, String messageString,
     *                          Intent intent, int notificationTitle) {
     *                          <p>
     *                          //Get the notification manage which we will use to display the
     *                          notification String ns = Context.NOTIFICATION_SERVICE;
     *                          NotificationManager mNotificationManager =
     *                          (NotificationManager) context.getSystemService(ns);
     *                          <p>
     *                          Calendar.getInstance().getTime().toString();
     *                          <p>
     *                          long when = System.currentTimeMillis();
     *                          <p>
     *                          //get the notification title from the application's
     *                          strings.xml file CharSequence contentTitle =
     *                          context.getString(notificationTitle);
     *                          <p>
     *                          //the message that will be displayed as the ticker String
     *                          ticker = contentTitle + " " + messageString;
     *                          <p>
     *                          //build the pending intent that will start the appropriate
     *                          activity PendingIntent pendingIntent =
     *                          PendingIntent.getActivity(context, Constants.showHistory,
     *                          intent, 0);
     *                          <p>
     *                          //build the notification Builder notificationCompat = new
     *                          NotificationCompat.Builder(context);
     *                          notificationCompat.setAutoCancel(true)
     *                          .setContentTitle(contentTitle)
     *                          .setContentIntent(pendingIntent)
     *                          .setContentText(messageString) .setTicker(ticker)
     *                          .setWhen(when) .setSmallIcon(Constants.icon_notify_offline);
     *                          <p>
     *                          Notification notification = notificationCompat.build();
     *                          //display the notification
     *                          mNotificationManager.notify(MessageID, notification);
     *                          MessageID++;
     *                          <p>
     *                          }
     *                          <p>
     *                          static void notifcation_caltxt_cancel(Context context) { //Get
     *                          the notification manage which we will use to display the
     *                          notification // String ns = Context.NOTIFICATION_SERVICE;
     *                          NotificationManager mNotificationManager =
     *                          (NotificationManager)
     *                          context.getSystemService(Context.NOTIFICATION_SERVICE);
     *                          mNotificationManager.cancel(MSGID_CONN_ERROR); }
     */

    public static void notify_caltxt_cancel_all(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        Log.i(TAG, "notify_caltxt_cancel_all , resetAutoResponse");
    }

    public static void notify_caltxt_status_change(Context context, String title, String msg, String info, int resid) {
        String ticker = "Caltxt status " + title;

        if (Addressbook.getInstance(context).getMyProfile().isAutoResponding() || Addressbook.getInstance(context).getMyProfile().isDND()) {
            notify_caltxt_cancel(context, MSGID_ALERT_STATUS_CHANGE);
            return;
        }

        notifcation_caltxt(context,
                "Caltxt status",
                msg.length() == 0 ? title : msg, "Tap to change",
                new Intent(context, CaltxtStatusPager.class),
                ticker, resid, Calendar.getInstance().getTimeInMillis(),
                Notification.FLAG_AUTO_CANCEL,
                MSGID_ALERT_STATUS_CHANGE);
    }

    /*
        public static void notify_caltxt_alert_cellinfo(Context context,String title, String msg, String info, String uri) {
            String ticker = "Caltxt alert: " + title;
            notifcation_caltxt(context,title,
                    msg.length() == 0 ? title : msg, info, new Intent(Intent.ACTION_VIEW,
                            Uri.parse(uri)), ticker, R.drawable.notification,
                    System.currentTimeMillis(), Notification.FLAG_AUTO_CANCEL,
                    MSGID_ALERT_CELLINFO);
        }

        public static void notify_caltxt_alert_cellloc(Context context,String title, String msg, String info, String uri) {
            String ticker = "Caltxt alert: " + title;
            notifcation_caltxt(context,title,
                    msg.length() == 0 ? title : msg, info, new Intent(Intent.ACTION_VIEW,
                            Uri.parse(uri)), ticker, R.drawable.notification,
                    System.currentTimeMillis(), Notification.FLAG_AUTO_CANCEL,
                    MSGID_ALERT_CELLLOC);
        }
    */
    public static void notify_caltxt_alert(Context context, String title, String msg, String info, String uri) {
        String ticker = "Caltxt alert: " + title;
        notifcation_caltxt(context, title,
                msg.length() == 0 ? title : msg, info, new Intent(Intent.ACTION_VIEW,
                        Uri.parse(uri)), ticker, R.drawable.ic_message_white_24dp,
                System.currentTimeMillis(), Notification.FLAG_AUTO_CANCEL,
                MSGID_ALERT);
    }

    public static void notify_caltxt_trigger(Context context, String title, String msg, String info, String uri) {
        String ticker = "Caltxt alert: " + title;
        notifcation_caltxt(context, title,
                msg.length() == 0 ? title : msg, info, new Intent(Intent.ACTION_VIEW,
                        Uri.parse(uri)), ticker, R.drawable.ic_auto_answer_received_outline_white_24dp,
                System.currentTimeMillis(), Notification.FLAG_AUTO_CANCEL,
                MSGID_ALERT);
    }

    /*
        public static void notify_caltxt_connected(Context context,String title, String msg, String info) {
            String ticker = "Caltxt service connected, " + title;
            notifcation_caltxt(context,"Caltxt connected",
                    msg.length() == 0 ? title : msg,
                            info,
                    new Intent(context, SplashScreen.class),
                    ticker, R.drawable.notification, System.currentTimeMillis(), Notification.FLAG_AUTO_CANCEL,
                     MSGID_ALERT_CONNECTION);
        }

        public static void notify_caltxt_disconnected(Context context,String reason, String info) {
            if (ConnectivityBroadcastReceiver.haveNetworkConnection(context)) {
                String ticker = "Caltxt service down due to " + reason;
                notifcation_caltxt(context,
                        "Caltxt service down", context
                                .getString(R.string.service_not_available),
                                info,
                        new Intent(context,
                                SplashScreen.class),
                        ticker, R.drawable.notification,
                        System.currentTimeMillis(), Notification.FLAG_AUTO_CANCEL,
                        MSGID_ALERT_CONNECTION);
            } else {
                String ticker = "Caltxt service disconnected due to " + reason;
                notifcation_caltxt(context,
                        "Caltxt service disconnected",
                        context.getString(
                                R.string.check_network),
                                info,
                        // new
                        // Intent(android.provider.Settings.ACTION_WIFI_SETTINGS),
                        new Intent(
                                android.provider.Settings.ACTION_WIRELESS_SETTINGS),
                        ticker, R.drawable.notification, System
                                .currentTimeMillis(), Notification.FLAG_AUTO_CANCEL,
                                MSGID_ALERT_CONNECTION);
            }
        }
    */
    private static void notify_caltxt_cancel(Context context, int msgid) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(msgid/*MSGID_ALERT_MISSED*/);
    }

    public static void notify_caltxt_missed_call_alert_cancel(Context context) {
        notify_caltxt_cancel(context, MSGID_ALERT_MISSED);
    }

    public static void notify_caltxt_missed_call(Context context, String from, String subject, String info, long when) {

        missed_call_count++;

        String ticker = "Missed call from " + from;
        notifcation_caltxt(context,
                missed_call_count == 1 ? "Missed call" : "Missed calls",
                missed_call_count == 1 ? from : missed_call_count + " missed calls",
                missed_call_count == 1 ? subject : "",
                new Intent(context, CaltxtPager.class).putExtra("fragment", Constants.FRAGMENT_CALLS),
                ticker, R.drawable.ic_missed_caltxt_outline_white_24dp, when, Notification.FLAG_AUTO_CANCEL,
                MSGID_ALERT_MISSED);
    }

    public static void notify_caltxt_missed_reply(Context context, String from, String subject, String info, long when) {

        missed_call_response_count++;

        String ticker = "Missed response from " + from;
        notifcation_caltxt(context,
                missed_call_response_count == 1 ? "Missed response" : "Missed responses",
                missed_call_response_count == 1 ? from : missed_call_response_count + " missed responses",
                missed_call_response_count == 1 ? subject : "",
                new Intent(context, CaltxtPager.class).putExtra("fragment", Constants.FRAGMENT_CALLS),
                ticker, R.drawable.ic_missed_caltxt_outline_white_24dp, when, Notification.FLAG_AUTO_CANCEL,
                MSGID_ALERT_MISSED_REPLY);
    }

    public static void notify_caltxt_call_blocked(Context context, String from, String subject, String info, long when) {

        blocked_call_count++;

        String ticker = "Blocked call from " + from;
        notifcation_caltxt(context,
                blocked_call_count == 1 ? "Blocked call" : "Blocked calls",
                blocked_call_count == 1 ? from : blocked_call_count + " blocked calls",
                blocked_call_count == 1 ? subject : "",
                new Intent(context, CaltxtPager.class).putExtra("fragment", Constants.FRAGMENT_BLOCKED),
                ticker, R.drawable.ic_blocked_caltxt_outline_white_24dp, when, Notification.FLAG_AUTO_CANCEL,
                MSGID_BLOCKED);
    }

    public static void notify_caltxt_call_dnd(Context context, String from, String subject, String info, long when) {

        dnd_rejected_call_count++;
        subject = dnd_rejected_call_count + (dnd_rejected_call_count == 1 ? " new call" : " new calls");

        String ticker = "Rejected call from " + from;
        notifcation_caltxt(context,
                "Do not disturb mode enabled",
                subject,
                dnd_rejected_call_count == 1 ? subject : "",
                new Intent(context, CaltxtPager.class).putExtra("fragment", Constants.FRAGMENT_CALLS),
                ticker, R.drawable.ic_donotdisturb_white_24dp, when, Notification.FLAG_ONGOING_EVENT,
                MSGID_DND);
    }

    public static void notify_caltxt_call_dnd_enabled(Context context, long when) {

        String ticker = "Do not disturb mode enabled";
        notifcation_caltxt(context,
                "Caltxt status",
                "Do not disturb",
                "Tap to change",
                new Intent(context, CaltxtStatusPager.class).putExtra("fragment", Constants.FRAGMENT_STATUS),
//				new Intent(context, CaltxtPager.class).putExtra("fragment", Constants.FRAGMENT_CALLS),
                ticker, R.drawable.ic_donotdisturb_white_24dp, when, Notification.FLAG_ONGOING_EVENT,
                MSGID_DND);
    }

    public static void notify_caltxt_dnd_cancel(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(MSGID_DND);
        Log.i(TAG, "notify_caltxt_dnd_cancel , resetDND");
    }

    public static void notify_caltxt_autoresponse_enabled(Context context, String subject, String info) {
        Drawable d1 = context.getResources().getDrawable(R.drawable.ic_auto_answer_sent_outline_white_24dp);
        Drawable wrappedDrawable = DrawableCompat.wrap(d1);
        wrappedDrawable = wrappedDrawable.mutate();
        DrawableCompat.setTint(wrappedDrawable, context.getResources().getColor(R.color.green));
        d1.invalidateSelf();
        auto_responded_call_count = 0;//reset auto responded calls count
        String ticker = "Automatic response enabled";
        notifcation_caltxt(context,
                ticker,
                subject,
                "expires " + info,
                new Intent(context, QuickResponseEdit.class),
                ticker, R.drawable.ic_auto_answer_sent_outline_white_24dp, Calendar.getInstance().getTimeInMillis(),
                Notification.FLAG_ONGOING_EVENT,
                MSGID_AUTO_RESPONSE_SET);
    }

    /*
        public static void notify_caltxt_autoresponse_set_connected(String subject, String info) {
            Drawable d1 = context.getResources().getDrawable(R.drawable.ic_auto_answer_sent_outline_white_24dp);
            Drawable wrappedDrawable = DrawableCompat.wrap(d1);
            wrappedDrawable = wrappedDrawable.mutate();
            DrawableCompat.setTint(wrappedDrawable, CaltxtApp.getCustomAppContext().getResources().getColor(R.color.green));
            d1.invalidateSelf();
            auto_responded_call_count = 0;//reset auto responded calls count
            String ticker = "Auto response enabled";
            notifcation_caltxt(
                    ticker,
                    subject,
                    "expires "+info,
                    new Intent(CaltxtApp.getCustomAppContext(), QuickResponseEdit.class),
                    ticker, R.drawable.ic_auto_answer_sent_outline_white_24dp, Calendar.getInstance().getTimeInMillis(),
                    Notification.FLAG_ONGOING_EVENT,
                    MSGID_AUTO_RESPONSE_SET);
        }
    */
    public static void notify_caltxt_autoresponse_cancel(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(MSGID_AUTO_RESPONSE_SET);
        Log.i(TAG, "notify_caltxt_autoresponse_cancel , resetAutoResponse");
    }

    public static void notify_caltxt_autoresponse_sent(Context context, String to, String subject, String info,
                                                       long when) {

        String ticker = "Automatic response sent to " + to;
        auto_responded_call_count++;
        subject = auto_responded_call_count + (auto_responded_call_count == 1 ? " new call" : " new calls");

        notifcation_caltxt(context,
                "Automatic response enabled",
                subject,
                "Automatic response sent",
                new Intent(context, CaltxtPager.class).putExtra("fragment", Constants.FRAGMENT_CALLS),
                ticker, R.drawable.ic_auto_answer_sent_outline_white_24dp, when, Notification.FLAG_ONGOING_EVENT,
                /*MSGID_AUTORESPONSE_SENT*/MSGID_AUTO_RESPONSE_SET);
    }

    public static void notify_caltxt_trigger_text_sent(Context context, String to, String subject, String info,
                                                       long when) {

        trigger_action_count++;
//		subject = trigger_action_count + (trigger_action_count==1?" new action completed":" new actions completed");
        subject = "Sent text to " + to + " (" + subject + ")";
        if (trigger_action_count > 1) {
            subject = subject + " ...";
        }
        String ticker = (trigger_action_count == 1 ? "Trigger action complete" : (trigger_action_count + " trigger actions complete"));
//		String ticker = "Trigger sent text to " + to;

        notifcation_caltxt(context,
                ticker,
                subject,
                "",
                new Intent(context, CaltxtPager.class).putExtra("fragment", Constants.FRAGMENT_CALLS),
                ticker, R.drawable.ic_auto_answer_sent_outline_white_24dp, when, Notification.FLAG_AUTO_CANCEL,
                MSGID_TRIGGER_ACTION);
    }

    public static void notify_caltxt_missed_text(Context context, String from, String subject, String info, long when) {

        missed_text_count++;

        String ticker = "Missed text from " + from;
        notifcation_caltxt(context,
                missed_text_count == 1 ? "Missed text" : "Missed texts",
                missed_text_count == 1 ? from : missed_text_count + " missed texts",
                missed_text_count == 1 ? subject : "",
                new Intent(context, CaltxtPager.class).putExtra("fragment", Constants.FRAGMENT_CALLS),
                ticker, R.drawable.ic_missed_caltxt_outline_white_24dp, when, Notification.FLAG_AUTO_CANCEL,
                MSGID_ALERT_MISSED);
    }

    /*
     * private static void notify_data_enabled_caltxt_disconnected(Context
     * context) { notifcation_caltxt(context,
     * context.getString(R.string.service_not_available), new Intent(context,
     * SplashScreen.class),
     * context.getString(R.string.service_connect_failure),
     * Constants.icon_notify_offline); }
     *
     * private static void notify_data_disabled(Context context) {
     * notifcation_caltxt(context, context.getString(R.string.check_network), //
     * new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), new
     * Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS),
     * context.getString(R.string.service_connect_failure),
     * Constants.icon_notify_offline); }
     */
    private static void notifcation_caltxt(Context context, String title,
                                           String message, String info, Intent intent, String ticker, int icon_resource,
                                           long when, int flag, int msgid) {

//		Context context = auto_responded_call_count;
        // Get the notification manage which we will use to display the
        // notification
        // String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Calendar.getInstance().getTime().toString();

        // long when = System.currentTimeMillis();

        // get the notification title from the application's strings.xml file
        // CharSequence contentTitle = context.getString(notificationTitle);

        // the message that will be displayed as the ticker
        // String ticker = notificationTitle + ". " + notificationMessage;

        // build the pending intent that will start the appropriate activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                Constants.showHistory, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // build the notification
        Builder notificationCompat = new NotificationCompat.Builder(context);
        notificationCompat.setContentTitle(title)
                .setContentIntent(pendingIntent).setContentText(message)
                .setContentInfo(info)
                .setTicker(ticker).setWhen(when).setSmallIcon(icon_resource);

        Notification notification = notificationCompat.build();
        // notification.flags = Notification.FLAG_ONGOING_EVENT;
//		notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.flags = flag;

        // display the notification
        mNotificationManager.notify(msgid, notification);
    }

    /**
     * Display a toast notification to the user
     *
     * @param context  Context from which to create a notification
     * @param text     The text the toast should display
     * @param duration The amount of time for the toast to appear to the user
     */
    public static void toast(final View view, final Context context, CharSequence text, int duration) {

        if (duration == Toast.LENGTH_LONG)
            duration = Snackbar.LENGTH_LONG;
        else if (duration == Toast.LENGTH_SHORT)
            duration = Snackbar.LENGTH_SHORT;

        Snackbar.make(view, text, duration)/*.setAction("Close", new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
//	            Toast.makeText(context, "Snackbar Action", Toast.LENGTH_LONG).show();
	        }
    	})*/.show();
//		Toast toast = Toast.makeText(context, text, duration);
//		toast.show();
    }


    // static Ringtone ringToneAlarm;
    static Ringtone ringTone;
    static Ringtone ringToneNotification;

    public static void playNotification(Context context) {
        try {
            if (ringToneNotification == null) {
                Uri notification = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                ringToneNotification = RingtoneManager.getRingtone(context,
                        notification);
            }
            ringToneNotification.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPlayNotification() {
        try {
            if (ringToneNotification != null) {
                ringToneNotification.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playRingtone(Context context) {
        try {
            if (ringTone == null) {
                Uri notification = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                ringTone = RingtoneManager.getRingtone(context, notification);
            }
            ringTone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPlayRingtone() {
        try {
            if (ringTone != null)
                ringTone.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static MediaPlayer player = null;

    public static void playAlarm(Context context) {
        stopPlayAlarm();
        try {
            // if(ringToneAlarm==null) {
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_ALARM);
            // ringToneAlarm = RingtoneManager.getRingtone(mContext,
            // notification);
            // if(player==null)
            player = MediaPlayer.create(context, notification);
            // else
            // player.setDataSource(mContext, notification);
            player.setLooping(true);
            player.start();
            // }
            // ringToneAlarm.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopPlayAlarm() {
        try {
            // if(ringToneAlarm!=null) {
            // Uri notification =
            // RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (player != null) {
                player.setLooping(false);
                player.stop();
            }
            // ringToneAlarm.stop();
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
