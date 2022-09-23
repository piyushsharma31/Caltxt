// 04-Jan-19, commented the SMS receiver class due to Google Play policy change (Use of SMS or Call Log permission groups)
/*package com.jovistar.caltxt.network.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.activity.SignupProfile;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.firebase.client.ConnectionFirebase;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.network.voice.CaltxtHandler;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Logbook;
import com.jovistar.commons.bo.XMob;

public class SMSBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSBroadcastReceiver";

    static SMSBroadcastReceiver instance;
    public static boolean sms_receiver_registered = false;

    public SMSBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //process only if user is registered for service
//		if(!SignupProfile.isNumberVerified(context)) {
//			return;
//		}

        Bundle bundle = intent.getExtras();
        if (bundle == null)
            return;
        String action = intent.getAction();

        if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            SmsMessage[] msgs = null;
            String msg_from = null, msg_body = null;
            if (bundle != null) {
                // ---retrieve the SMS message received---
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        msg_body = msgs[i].getMessageBody();
                    }

                    // notify the Sign-up to process registration
//						if (msg_from.endsWith(msg_body)) {
                    if (SignupProfile.smsContentSent.equals(msg_body)) {
                        abortBroadcast();// to not let Inbox receive this sms

                        synchronized (SignupProfile.semaphore) {
                            Log.i(TAG, "SMSBroadcastReceiver:onReceive SMS verification :" + msg_body + ", smsContentSent:" + SignupProfile.smsContentSent);
                            //store SIM slot for registered number
                            String simSlot = String.valueOf(bundle.getInt("simId", -1));
                            SignupProfile.setPreference(context.getApplicationContext(),
                                    context.getApplicationContext().getString(R.string.profile_sim_slot), simSlot);
                            //store SIM serial#
                            TelephonyManager mngr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            String IMSI = mngr.getSubscriberId();//IMSI - change with SIM
                            String simSerialNumber = mngr.getSimSerialNumber();//SIM# - change with SIM
                            SignupProfile.setPreference(context.getApplicationContext(),
                                    context.getApplicationContext().getString(R.string.profile_sim_serial_number), simSerialNumber);
                            Log.i(TAG, "SMSBroadcastReceiver:onReceive SMS verification done serial:" + simSerialNumber + ", slot:" + simSlot);

                            SignupProfile.semaphore.notifyAll();
                            SignupProfile.semaphore = msg_body;// successful registration
                            abortBroadcast();// to not let Inbox receive this sms
                        }
                    } else if (XCtx.isXCtx(msg_body)) {
                        abortBroadcast();// to not let Inbox receive this sms

                        CaltxtHandler.get(context.getApplicationContext()).processIncomingCaltxt(msg_from, msg_body);
                    } else if (msg_body.contains("via Caltxt")) {
                        abortBroadcast();// to not let Inbox receive this sms

                        Log.v(TAG, "onReceive via Caltxt body: " + msg_body);

                        XCtx ctx = new XCtx();
                        ctx.setNameCaller(msg_body.substring(0, msg_body.indexOf("via Caltxt") - 1));
                        ctx.setUsernameCaller(XMob.toFQMN(msg_from, Addressbook.getMyCountryCode()));
                        ctx.setNumberCallee(Addressbook.getMyProfile().getUsername());
                        ctx.setCaltxt(msg_body.substring(msg_body.indexOf(":") + 2, msg_body.lastIndexOf("(") - 1));

                        long pid = 0;
                        if (msg_body.contains("mid:")) {
                            pid = Long.parseLong(msg_body.substring(msg_body.lastIndexOf(":") + 1, msg_body.length() - 1));
                            ctx.setPersistenceId(pid);
                            ctx.setCallState(XCtx.OUT_MESSAGE);
                        } else if (msg_body.contains("cid:")) {
                            pid = Long.parseLong(msg_body.substring(msg_body.lastIndexOf(":") + 1, msg_body.length() - 1));
                            ctx.setPersistenceId(pid);
                            ctx.setCallState(XCtx.OUT_CALL);
                        } else if (msg_body.contains("mrid:")) {
                            // fetch the local pid sent by other side. remote pid is not sent! ok?
                            pid = Long.parseLong(msg_body.substring(msg_body.lastIndexOf(":") + 1, msg_body.length() - 1));
                            ctx = Logbook.get(context.getApplicationContext()).get(pid);
                            ctx.setAck(msg_body.substring(msg_body.indexOf(":") + 2, msg_body.lastIndexOf("(") - 1));
                            ctx.setCallState(XCtx.IN_MESSAGE_REPLY);
                        } else if (msg_body.contains("crid:")) {
                            // fetch the local pid sent by other side. remote pid is not sent! ok?
                            pid = Long.parseLong(msg_body.substring(msg_body.lastIndexOf(":") + 1, msg_body.length() - 1));
                            ctx = Logbook.get(context.getApplicationContext()).get(pid);
                            ctx.setAck(msg_body.substring(msg_body.indexOf(":") + 2, msg_body.lastIndexOf("(") - 1));
                            ctx.setCallState(XCtx.IN_CALL_REPLY);
                        }
                        Log.v(TAG, "onReceive via Caltxt ctx: " + ctx.toString());
                        CaltxtHandler.get(context.getApplicationContext()).processIncomingCaltxt(ctx);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "SMSBroadcastReceiver:onReceive, unknown exception");
                }
            }
        }
    }

    public static void sendSMSMessage(Context context, final long pid, final String number, final String message) {

        final class SMSDeliveredReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
//				CaltxtToast.acquirePartialWakeLock(context);
                Log.e(TAG, "SMSBroadcastReceiver:SMSDeliveredReceiver onReceive pid " + ", " + pid + ", " + message);
                CaltxtHandler.get(context.getApplicationContext()).updateDelivered(pid, message);
                //delivered
                Connection.get().addAction(Constants.smsDeliveredProperty, number, message);
                context.getApplicationContext().unregisterReceiver(this);
//				CaltxtToast.releasePartialWakeLock();
            }
        }

        final class SMSSentReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
//				CaltxtToast.acquirePartialWakeLock(context);
                String result = "";

                switch (getResultCode()) {

                    case Activity.RESULT_OK:
                        result = "Transmission successful";
                        CaltxtHandler.get(context).updateSent(pid, message);
                        Connection.get().addAction(Constants.smsPublishedProperty, number, message);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        result = "Transmission failed";

                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        result = "Radio off";

                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        result = "No PDU defined";

                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        result = "No service";

                        // SMS failed, send through firebase anyway!
                        if (Addressbook.getInstance(context.getApplicationContext()).isRegistered(number)) {//is a registered contact (need not be a contact)
//						RebootService.getConnection(context.getApplicationContext()).publishXCtxFirebase(number, message, pid);
                            ConnectionFirebase.publishXCtx(number, message, pid);
                        }
                        Connection.get().addAction(Constants.messageErrorProperty, number, message);
                }

                Log.e(TAG, "SMSBroadcastReceiver:SMSSentReceiver onReceive result " + result + ", " + message);
                //published, not delivered
//				Toast.makeText(Globals.getCustomAppContext(), number+", "+result, Toast.LENGTH_LONG).show();
                context.getApplicationContext().unregisterReceiver(this);
//				CaltxtToast.releasePartialWakeLock();
            }
        }

        if (message == null || message.length() == 0 || number == null
                || number.length() == 0)
            return;

        try {
            String SENT = "sent";
            String DELIVERED = "delivered";

            Intent sentIntent = new Intent(SENT);
            //Create Pending Intents
            PendingIntent sentPI = PendingIntent.getBroadcast(
                    context.getApplicationContext(), 0, sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Intent deliveryIntent = new Intent(DELIVERED);

            PendingIntent deliverPI = PendingIntent.getBroadcast(
                    context.getApplicationContext(), 0, deliveryIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
			// Register for SMS send action
            SMSSentReceiver sentRecvr = new SMSSentReceiver();
            context.getApplicationContext().registerReceiver(sentRecvr, new IntentFilter(SENT));
			// Register for Delivery event
            SMSDeliveredReceiver deliveredRecvr = new SMSDeliveredReceiver();
            context.getApplicationContext().registerReceiver(deliveredRecvr, new IntentFilter(DELIVERED));

			// Send SMS
            SmsManager smsManager = SmsManager.getDefault();
            String tmp = null;
            XCtx ctx = null;
			// encode Base64
            if (XCtx.isXCtx(message)) {
                tmp = message.substring(0, 2) + Base64.encodeToString(message.getBytes("UTF-8"), Base64.DEFAULT);
            } else {
                ctx = Logbook.get(context.getApplicationContext()).get(pid);
                String call_identifier = "";
                long rpid = pid;
                //plain SMS to non Caltxt phone number
                if (ctx.getCallState() == XCtx.OUT_MESSAGE) {
                    call_identifier = "mid";
                } else if (ctx.getCallState() == XCtx.OUT_CALL) {
                    call_identifier = "cid";
                } else if (ctx.getCallState() == XCtx.IN_CALL_REPLY) {
                    call_identifier = "crid";
                    // send remote pid for other side to fetch the XCtx record
                    rpid = ctx.getRemotePersistenceId();
                } else if (ctx.getCallState() == XCtx.IN_MESSAGE_REPLY) {
                    call_identifier = "mrid";
                    // send remote pid for other side to fetch the XCtx record
                    rpid = ctx.getRemotePersistenceId();
                }

                if (call_identifier.length() == 0) {
                    tmp = Addressbook.getMyProfile().getName()
                            + " via Caltxt : " + message;
                } else {
                    tmp = Addressbook.getMyProfile().getName()
                            + " via Caltxt : " + message
                            + " (" + call_identifier + ":" + rpid + ")";
                }
                Log.i(TAG, "SMSBroadcastReceiver:prepare - state " + ctx.getCallState() + ", " + ctx.toString());
            }
            smsManager.sendTextMessage("+" + number, null, tmp, sentPI, deliverPI);
            Connection.get().
                    addAction(Constants.smsPublishingProperty, number, (ctx == null ? message : ctx.toString()));
            Log.i(TAG, "SMSBroadcastReceiver:SMS Sent - " + tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
*/