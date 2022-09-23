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
package com.jovistar.caltxt.mqtt.client;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.mqtt.client.ConnectionMqtt.ConnectionStatus;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.network.voice.CaltxtHandler;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

/**
 * Handles call backs from the MQTT Client
 */
public class MqttCallbackHandler implements MqttCallbackExtended {

    private static final String TAG = "MqttCallbackHandler";
    Context context;
    /**
     * Client handle to reference the connection that this handler is attached to
     **/
    private final String clientHandle;

    /**
     * Creates an <code>MqttCallbackHandler</code> object
     *
     * @param context      The application's context
     * @param clientHandle The handle to a {@link ConnectionMqtt} object
     */
    public MqttCallbackHandler(Context context, String clientHandle) {
        this.context = context;
        this.clientHandle = clientHandle;
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

//		Log.v(TAG, "MqttCallbackHandler::connectComplete()");
        ConnectionMqtt c = ConnectionMqtt.getConnection(context);

//        if (c.handle() != clientHandle) {
        if (!c.handle().equals(clientHandle)) {
            Log.e(TAG, "c.handle(): " + c.handle() + ", not equal to clientHandle " + clientHandle);
            return;
        }

        c.changeConnectionStatus(ConnectionMqtt.ConnectionStatus.CONNECTED);

        if (reconnect) {
            // reconnected, subscribe again
//			Log.d(TAG,"Reconnected to : " + serverURI);
            // Re-subscribe
            CaltxtHandler.get(context).subscribeTopics();
        } else {
//			Log.d(TAG,"Connected to: " + serverURI);
        }

        //send not-delivered messages
        CaltxtHandler.get(context).flushUndeliveredMessages();

//		c.try2Connect_success();
//		ConnectionMqtt.CONNECT_THREAD_RUNNING = false;

//		Log.i(TAG, "subscribed:"+Constants.CALTXT_ADMIN_ALERTS_TOPIC + Addressbook.getMyProfile().getUsername());
//		RebootService.BroadcastStatus();//- commented 5/APR/16 - integrated in Adapter::getView()
        /*if (pingSender == null)
        {
            pingSender = new PingSender();
            context.registerReceiver(pingSender, new IntentFilter(PingSender.MQTT_PING_ACTION));
        }
		pingSender.scheduleNextPing(context);*/
//		MqttPingAlarm.schedule(context);//in-built in new MessageService (gradle)
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
     */
    @Override
    public void connectionLost(Throwable cause) {
//		CaltxtToast.acquirePartialWakeLock(context);

        //		if (cause != null) {
        ConnectionMqtt c = ConnectionMqtt.getConnection(context);
//			c.addAction("ConnectionMqtt Lost");
//			c.disconnect();
        c.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

        //added 11-JAN-2018 to reset connection object to null
        //this will reinitialize connection object in RebootService.getConnection() call
        //commented 02-2018, let the MqttConnection be not created every now and then
        //what is the benefit?
//        RebootService.setDisconnected();

        // format string to use a notification text
//			Object[] args = new Object[2];
//			args[0] = c.getId();
//			args[1] = c.getHostName();

        // build intent
//			Intent intent = new Intent(Intent.ACTION_MAIN);
//			intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
//			Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
//			intent.setClassName("com.android.phone","com.android.phone.Settings");
/*			Intent intent = new Intent();
			intent.setClassName(context, "com.jovistar.caltxt.activity.");
			intent.putExtra("handle", clientHandle);
			String message = context.getString(R.string.check_network, args);*/

//		if(ConnectivityBroadcastReceiver.haveNetworkConnection(context)) {
//			c.reconnect();
//		}
//		}
/*			if (ActionListener.pingSender != null) {
	            CaltxtApp.getCustomAppContext().unregisterReceiver(ActionListener.pingSender);
	            ActionListener.pingSender = null;
	        }*/
//		CaltxtToast.releasePartialWakeLock();
        Log.e(TAG, "MqttCallbackHandler::connectionLost cause " + cause);
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String,
     * org.eclipse.paho.client.mqttv3.MqttMessage)
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
//		CaltxtToast.acquirePartialWakeLock(context);
//		Log.v(TAG, "MqttCallbackHandler::messageArrived, qos, "+message.getQos()+", isRetained, "+message.isRetained());

        String messageString = new String(message.getPayload());

		/* decode Base64 message */
        byte[] tmp2 = Base64.decode(messageString.substring(2), Base64.DEFAULT);
        try {
            messageString = new String(tmp2, StandardCharsets.UTF_8);

            CaltxtHandler.get(context).processIncomingCaltxt(topic, messageString);
            // receiving this message will have kept the connection alive for us, so
            // we take advantage of this to postpone the next scheduled ping/keepAlive
//        ActionListener.pingSender.scheduleNextPing(CaltxtApp.getCustomAppContext());
//		MqttPingAlarm.schedule(context);//in-built in MessageService (gradle) 30-JAN-17

        } finally {
//			CaltxtToast.releasePartialWakeLock();
        }
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
//		CaltxtToast.acquirePartialWakeLock(context);

//		Log.e(TAG, "MqttCallbackHandler::deliveryComplete "+token);
        try {
            // decode Base64 message
            byte[] tmp2 = Base64.decode(token.getMessage().toString().substring(2), Base64.DEFAULT);
            String message_string = null;
            message_string = new String(tmp2, StandardCharsets.UTF_8);

            CaltxtHandler.get(context).updateDelivered(0, message_string);
            Connection.get().addAction(Constants.mqttDeliveredProperty, null, message_string);
        } catch (MqttException e) {
            Log.e(TAG, "MqttCallbackHandler::deliveryComplete " + e.getLocalizedMessage());
        } finally {
//			CaltxtToast.releasePartialWakeLock();
        }
    }
}
