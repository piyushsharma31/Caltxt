package com.jovistar.caltxt.firebase.client;

/**
 * Created by jovika on 1/5/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jovistar.caltxt.R;
import com.jovistar.caltxt.activity.Settings;
import com.jovistar.caltxt.activity.SignupProfile;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.app.Config;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.network.data.ConnectivityBroadcastReceiver;
import com.jovistar.caltxt.network.voice.CaltxtHandler;
import com.jovistar.caltxt.notification.NotificationUtils;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.service.WifiScanReceiver;
import com.jovistar.commons.bo.XMob;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jovistar.caltxt.app.Constants.wifiscan_defaultInterval;
import static com.jovistar.caltxt.firebase.client.FirebaseIDService.sendGCMTokensToServer;
import static com.jovistar.caltxt.firebase.client.FirebaseIDService.storeRegIdInPref;

public class ConnectionFirebase extends FirebaseMessagingService {

    private static final String TAG = "ConnectionFirebase";

    static boolean CONNECT_STATUS = false;
    private NotificationUtils notificationUtils;

    public static final String FCM_PROJECT_SENDER_ID = "138704848634";
    public static final String FCM_SERVER_CONNECTION = "@gcm.googleapis.com";
    public static final String BACKEND_ACTION_REGISTER = "com.jovistar.caltxt.firebaseappserverxmpp.REGISTER";
    public static final String BACKEND_ACTION_MESSAGE = "com.jovistar.caltxt.firebaseappserverxmpp.MESSAGE";
    public static final String BACKEND_ACTION_MESSAGE_RECEIPT = "com.jovistar.caltxt.firebaseappserverxmpp.MESSAGE.RECEIPT";
    public static final String BACKEND_ACTION_MESSAGE_ACK = "com.jovistar.caltxt.firebaseappserverxmpp.MESSAGE.ACK";
    public static final String BACKEND_ACTION_ECHO = "com.jovistar.caltxt.firebaseappserverxmpp.ECHO";
    public static final Random RANDOM = new Random();
    //    private static final String recipient = "dlcHsKiaBgs:APA91bFDxIIxorwW5A4JlzwYeXOkVtpnURP5iakv5335IGP1Vy7VKOKmqTeFy0EhIm4WgLWnz1miM12SqD7lVpGidYMaRd2B33VDCFPCyIi3YZyPasdGcHsh0Mge1xCx6GcSYTRGE290";

    ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN",s);
        // Saving reg id to shared preferences
        storeRegIdInPref(getApplicationContext(), s);

        // sending gcm token to server
        sendGCMTokensToServer(getApplicationContext());

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", s);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.v(TAG, "onDeletedMessages");
    }

    @Override
    public void onMessageSent(String msgId) {
        super.onMessageSent(msgId);
        Log.v(TAG, "onMessageSent: msgId " + msgId);
        String pid = msgId.substring(msgId.lastIndexOf("-") + 1);
        XCtx ctx = (XCtx) Persistence.getInstance(Caltxt.getCustomAppContext()).get(Long.parseLong(pid), "XCtx");
        if (ctx != null) {
            CaltxtHandler.get(Caltxt.getCustomAppContext()).updateSent(0, ctx.toString());
            Connection.get().addAction(Constants.firebasePublishedProperty, ctx.toString(), ctx.toString());
        } else {
            // perhaps a ping message
        }
    }

    @Override
    public void onSendError(String msgId, Exception exception) {
        super.onSendError(msgId, exception);
        Log.v(TAG, "onSendError: msgId " + msgId);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage == null)
            return;

//        CaltxtToast.acquirePartialWakeLock(Caltxt.getCustomAppContext());

        // process message
//        Intent resultIntent = new Intent(Caltxt.getCustomAppContext(), CaltxtPager.class);
//        resultIntent.putExtra("message", remoteMessage.getMessageId());
//        showNotificationMessage(Caltxt.getCustomAppContext(), "Caltxt", remoteMessage.getMessageId(),
//                Long.toString(Calendar.getInstance().getTimeInMillis()), resultIntent);

        Log.v(TAG, "onMessageReceived: msgId " + remoteMessage.getMessageId() +
                ", type " + remoteMessage.getMessageType());
//        Toast.makeText(Caltxt.getCustomAppContext(), "MessageArrived: ", Toast.LENGTH_LONG).show();


        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.v(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.v(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData());
                handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }

//        CaltxtToast.releasePartialWakeLock();
        // initialize the RebootService, inturn MQTT service (MQTTService is apparently sleeping)
        // commented, getConnection should always initialize the connection
//        if(RebootService.getConnection(Caltxt.getCustomAppContext()).isConnectedOrConnecting()==false) {
//            startService(new Intent(Caltxt.getCustomAppContext(), RebootService.class).putExtra("caller", "RebootReceiver"));
//        }

        long last_wifi_scan = SignupProfile.getPreferenceLong(getApplicationContext(), getResources().getString(R.string.preference_key_last_wifi_scan));
        long current_millis = Calendar.getInstance().getTimeInMillis();
        if(current_millis - last_wifi_scan > wifiscan_defaultInterval) {
            Log.d(TAG, "onMessageReceived: last_wifi_scan " + last_wifi_scan);
            Log.d(TAG, "onMessageReceived: current_time " + current_millis);
            Log.d(TAG, "onMessageReceived: current_time - last_wifi_scan " + (current_millis-last_wifi_scan));
            final WifiScanReceiver wifiSR = new WifiScanReceiver();
            wifiSR.startWifiAndCellScan(getApplicationContext());
        }
    }

    private void handleNotification(String message) {
        Log.v(TAG, "handleNotification: " + message);
        if (!NotificationUtils.isAppIsInBackground(Caltxt.getCustomAppContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(Caltxt.getCustomAppContext());
            notificationUtils.playNotificationSound();
        } else {
            // If the app is in background, firebase itself handles the notification
        }
    }

    private void handleCaltxt(String action, long pid, String from, String ctxstr) {

        Log.v(TAG, "handleCaltxt from: " + from + ", pid: " + pid + ", ctx: " + ctxstr);

        if (action != null && action.equals(BACKEND_ACTION_MESSAGE_RECEIPT)) {

            XCtx ctx = (XCtx) Persistence.getInstance(Caltxt.getCustomAppContext()).get(pid, "XCtx");
//            ctx.setDelivered();
            CaltxtHandler.get(Caltxt.getCustomAppContext()).updateDelivered(0, ctx.toString());

            Connection.get().addAction(Constants.firebaseDeliveredProperty, null, ctx.toString());

            Log.v(TAG, "handleCaltxt : " + BACKEND_ACTION_MESSAGE_RECEIPT + ", ctx " + ctx);

        } else if (action != null && action.equals(BACKEND_ACTION_MESSAGE_ACK)) {
            Log.v(TAG, "handleCaltxt : " + BACKEND_ACTION_MESSAGE_ACK);
        } else if (action != null && action.equals(BACKEND_ACTION_MESSAGE)) {
            CaltxtHandler.get(Caltxt.getCustomAppContext()).processIncomingCaltxt(from, ctxstr);
            Log.v(TAG, "handleCaltxt : processIncomingCaltxt " + ctxstr);
        } else {
            Log.v(TAG, "handleCaltxt : UNKNOWN ACTION " + action);
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.v(TAG, "handleDataMessage : " + json.toString());

        try {
//            JSONObject data = json.getJSONObject("data");

            String caltxt = json.getString("message");
            String sender = json.getString("sender");
            String action = json.getString("action");
            String timestamp = json.getString("timestamp");

            long pid = 0;//valid only for receipt message

            if (action != null && action.equals(BACKEND_ACTION_MESSAGE_RECEIPT)) {
                String msgId = json.getString("message_id");
                Log.v(TAG, "handleDataMessage msgId: " + msgId);
                pid = Long.parseLong(msgId.substring(msgId.lastIndexOf("-") + 1));
            } else if (action != null && action.equals(BACKEND_ACTION_MESSAGE)) {
                /* decode Base64 message */
                byte[] tmp2 = Base64.decode(caltxt.substring(2), Base64.DEFAULT);
                caltxt = new String(tmp2, StandardCharsets.UTF_8);
            }

            handleCaltxt(action, pid, sender, caltxt);
/*
            String title = "Caltxt message";
//            String message = json.getString("message");
//            boolean isBackground = json.getBoolean("is_background");
            String imageUrl = "";
//            String action = json.getString("action");
//            JSONObject payload = json.getJSONObject("payload");

//            Log.v(TAG, "title: " + title);
//            Log.v(TAG, "message: " + message);
//            Log.v(TAG, "isBackground: " + isBackground);
//            Log.v(TAG, "payload: " + payload.toString());
//            Log.v(TAG, "imageUrl: " + imageUrl);
//            Log.v(TAG, "timestamp: " + timestamp);
//            Log.v(TAG, "action: " + action);

            if (!NotificationUtils.isAppIsInBackground(Caltxt.getCustomAppContext())) {
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", caltxt);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                // play notification sound
                NotificationUtils notificationUtils = new NotificationUtils(Caltxt.getCustomAppContext());
                notificationUtils.playNotificationSound();
            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(Caltxt.getCustomAppContext(), CaltxtPager.class);
                resultIntent.putExtra("message", caltxt);

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(Caltxt.getCustomAppContext(), title, caltxt, timestamp, resultIntent);
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(Caltxt.getCustomAppContext(), title, caltxt, timestamp, resultIntent, imageUrl);
                }
            }*/
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

    /*
        public static void sendNPUpstreamMessage(String sender, String recipient, String msg, String msgId) {

            // Normal priority. This is the default priority for data messages
            // High priority. This is the default priority for notification messages
            // message id must be unique for every sender id. To make unique, append
            // username to persistence id, "919953693002-891"
            // To send a message to other device through the XMPP Server, you should add the
            // receiverId and change the action name to BACKEND_ACTION_MESSAGE in the data
        }
    */
    private static void sendUpstreamMessage(String sender, String recipient, String msg, String msgId, String priority, int ttl/*sec*/, boolean receipt) {
        String tmp = new String(msg.getBytes()).substring(0, 2)
                + Base64.encodeToString(msg.getBytes(), Base64.DEFAULT);
        Log.i(TAG, "sendUpstreamMessage sender:" + sender + " recipient:" + recipient + " message " + msg+ " msgId " + msgId+ " priority " + priority+ " ttl " + ttl+ " receipt " + receipt);

        // Normal priority. This is the default priority for data messages
        // High priority. This is the default priority for notification messages
        // message id must be unique for every sender id. To make unique, append
        // username to persistence id, "919953693002-891"
        // To send a message to other device through the XMPP Server, you should add the
        // receiverId and change the action name to BACKEND_ACTION_MESSAGE in the data
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(FCM_PROJECT_SENDER_ID + FCM_SERVER_CONNECTION)
                .setMessageId(msgId/*Integer.toString(RANDOM.nextInt())*/)
                .setTtl(/*Constants.firebase_defaultMessageTTL*/ttl)// 24 hours
                .addData("message", tmp)
                .addData("sender", sender)
                .addData("recipient", recipient)
                .addData("action", BACKEND_ACTION_MESSAGE)
                .addData("priority", priority/*"high" or "normal"*/)
                .addData("timestamp", Long.toString(Calendar.getInstance().getTimeInMillis()))
                .addData("receipt", receipt ? "yes" : "no")
//                .addData("receipt", priority.equals("high")?"yes":"no")
                .build());
        // To send a message to other device through the XMPP Server, you should add the
        // receiverId and change the action name to BACKEND_ACTION_MESSAGE in the data
    }

    private static void publish(final String sender, final String recipient, final String message, final String msgId, final int qos, final int ttl, final boolean receipt) {
        if (message == null || message.length() == 0 || recipient == null || recipient.length() == 0)
            return;

        Log.i(TAG, "publish, sender:" + sender + " recipient:" + recipient + " message " + message);
        // store the key(mobile number), value(token) pair in real time DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.caltxt_users_firebase);

        // User data change listener
        ref.child(recipient)./*addValueEventListener*/addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String token = dataSnapshot.getValue(String.class);

                // Check for null
                if (token == null) {
                    Log.e(TAG, "User data is null! for " + recipient);
                    return;
                }

                Log.d(TAG, "User data is changed! " + token);
                if (qos == Constants.mqtt_defaultQosMessage) {
//                    Log.i(TAG, "publish high, sender:" + sender + " recipient:" + recipient + " message " + message);

                    sendUpstreamMessage(sender, token, message, msgId, "high", ttl, receipt);
//					RebootService.getConnection(context).addAction(Constants.firebasePublishedProperty, message, message);
                } else {
//                    Log.i(TAG, "publish normal, sender:" + sender + " recipient:" + recipient + " message " + message);

                    sendUpstreamMessage(sender, token, message, msgId, "normal", ttl, receipt);
//					RebootService.getConnection(context).addAction(Constants.firebasePublishedProperty, message, message);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user " + error.toException());
            }
        });

        Connection.get().addAction(Constants.firebasePublishingProperty, recipient, message);
    }

    public static void publishXCtx(String to, String message, long pid) {
        Log.i(TAG, "publishXCtx, to:" + to + ", message:" + message + ", msgId pid:" + pid);
        publish(Addressbook.getMyProfile().getUsername(),
                to,
//				to.substring(to.lastIndexOf("/")+1),
                message,
                Addressbook.getMyProfile().getUsername() + "-" + pid,
                Constants.mqtt_defaultQosMessage,
                Constants.firebase_defaultMessageTTL/*24 hours*/,
                true);

//		RebootService.getConnection(context).addAction(Constants.firebasePublishedProperty, to, message);
    }

    private void ackPing(String from/*idenfies SIM from which ping should be sent*/, String contact) {
        if (Addressbook.getInstance(Caltxt.getCustomAppContext()).isContact(contact) || Settings.iam_discoverable_by_anyone) {
            XMob frm = Addressbook.getMyProfile();
            String ackString = frm.toStringForACK();

            if (from.endsWith(frm.getUsername())) {
                //nothing to do
            } else if (from.endsWith(frm.getNumber2())) {
                String u = frm.getUsername();
                frm.setUsername(XMob.toFQMN(frm.getNumber2(), Addressbook.getMyCountryCode()));
                frm.setNumber2(u);
                ackString = frm.toStringForACK();
            }

            publish(Addressbook.getMyProfile().getUsername(),
                    contact,
                    ackString,
                    Addressbook.getMyProfile().getUsername() + "-" + ConnectionFirebase.RANDOM.nextInt(),
                    Constants.mqtt_defaultQosMessage,
                    Constants.firebase_defaultPingTTL/*ttl in seconds*/,
                    false);
        }
    }

    private void pingContact(String from, String contact) {
        Log.i(TAG, "pingContact, contact:" + contact + ", from:" + from + ", username:" + Addressbook.getMyProfile().getUsername() +
                ", number2:" + Addressbook.getMyProfile().getNumber2());
        //DO NOT PING SELF
        // commented 22OCT2017, Firebase connection test based on self ping
//        if(Addressbook.getMyProfile().getUsername().endsWith(contact)
//                || Addressbook.getMyProfile().getNumber2().endsWith(contact))
//            return;
        if (Addressbook.getInstance(Caltxt.getCustomAppContext()).isContact(contact) || Settings.iam_discoverable_by_anyone) {
            Log.i(TAG, "pingContact, iam_discoverable_by_anyone:" + Settings.iam_discoverable_by_anyone);
            XMob frm = Addressbook.getMyProfile();
            Log.i(TAG, "pingContact, frm.toString():" + frm.toString());
            if (from.endsWith(frm.getUsername())) {
                //nothing to do
            } else if (from.endsWith(frm.getNumber2())) {
                String u = frm.getUsername();
                frm.setUsername(XMob.toFQMN(frm.getNumber2(), Addressbook.getMyCountryCode()));
                frm.setNumber2(u);
            }

            publish(Addressbook.getMyProfile().getUsername(),
                    contact,
                    frm.toString(),
                    Addressbook.getMyProfile().getUsername() + "-" + ConnectionFirebase.RANDOM.nextInt(),
                    Constants.mqtt_defaultQosMessage,
                    Constants.firebase_defaultPingTTL/*ttl in seconds*/,
                    false);

            //mark if as offline (pingACK will confirm online status)
            XMob mob = Addressbook.getInstance(Caltxt.getCustomAppContext()).getRegistered(contact);
            if (mob != null && mob.isRegistered()/*mob.getStatus()!=XMob.STATUS_UNREGISTERED*/)//registered Caltxt user
                mob.setStatusOffline();
//				mob.setStatus(XMob.STATUS_OFFLINE);
        }
    }

    class ContactPinger implements Runnable {
        String contactToPing;
        String from;
        boolean isAck;

        ContactPinger(String from, String contactToPing, boolean isAck) {
            this.contactToPing = contactToPing;
            this.from = from;
            this.isAck = isAck;
        }

        @Override
        public void run() {

            if (isAck)
                ackPing(from, contactToPing);
            else
                pingContact(from, contactToPing);
        }
    }

    public void submitPingContact(String from, String contact) {
//		if (statusCache.containsKey(contact)==false
//				|| (Calendar.getInstance().getTimeInMillis() - statusCache.get(contact).longValue()) > Constants.mqtt_defaultPingInterval) {
//        statusCache.put(contact, Calendar.getInstance().getTimeInMillis());
        executorService.submit(new ContactPinger(from, contact, false));
//		}
        Log.i(TAG, "submitPingContact, contact:" + contact);
    }

    public void submitAckPingContact(String from, String contact) {
//		if (statusCache.containsKey(contact)==false
//				|| (Calendar.getInstance().getTimeInMillis() - statusCache.get(contact).longValue()) > Constants.mqtt_defaultPingInterval) {
//        statusCache.put(contact, Calendar.getInstance().getTimeInMillis());
        executorService.submit(new ContactPinger(from, contact, true));
//		}
        Log.i(TAG, "submitAckPingContact, contact:" + contact);
    }

    public void submitForcedPingContact(String from, String contact) {
        String ffrom = XMob.toFQMN(from, Addressbook.getMyCountryCode());
        String ccontact = XMob.toFQMN(contact, Addressbook.getMyCountryCode());
//        statusCache.put(ccontact, Calendar.getInstance().getTimeInMillis());
        executorService.submit(new ContactPinger(ffrom, ccontact, false));
//        pingContact(from, ccontact);
        Log.i(TAG, "submitForcedPingContact, contact:" + ccontact);
    }

    public static void setConnected() {
        CONNECT_STATUS = true;
    }

    public static void resetConnected() {
        CONNECT_STATUS = false;
    }

    public static boolean isConnected() {
        if (ConnectivityBroadcastReceiver.haveNetworkConnection() && !CONNECT_STATUS) {
            Connection.get().submitPingSelfFirebase();
        }
        return (CONNECT_STATUS);
    }
}