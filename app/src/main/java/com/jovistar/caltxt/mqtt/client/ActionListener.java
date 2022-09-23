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
import android.util.Log;

import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.mqtt.client.ConnectionMqtt.ConnectionStatus;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.network.voice.CaltxtHandler;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

/**
 * This Class handles receiving information from the
 * {@link MqttAndroidClient} and updating the {@link ConnectionMqtt}
 * associated with the action
 */
public class ActionListener implements IMqttActionListener {
    private static final String TAG = "ActionListener";
    // receiver that wakes the Service up when it's time to ping the server
//    public static PingSender pingSender;

    /**
     * Actions that can be performed Asynchronously <strong>and</strong>
     * associated with a {@link ActionListener} object
     */
    public enum Action {
        /**
         * Connect Action
         **/
        CONNECT,
        /**
         * Disconnect Action
         **/
        DISCONNECT,
        /**
         * Subscribe Action
         **/
        SUBSCRIBE,
        /**
         * Publish Action
         **/
        PUBLISH
    }

    /**
     * The {@link Action} that is associated with this instance of
     * <code>ActionListener</code>
     **/
    private Action action;
    /**
     * The arguments passed to be used for formatting strings
     **/
    private String[] additionalArgs;
    /**
     * Handle of the {@link ConnectionMqtt} this action was being executed on
     **/
    private String clientHandle;
    /**
     * {@link Context} for performing various operations
     **/
    private Context context;
    private ConnectionMqtt connectionMqtt;

    /**
     * Creates a generic action listener for actions performed form any activity
     *
     * @param context        The application context
     * @param action         The action that is being performed
     * @param clientHandle   The handle for the client which the action is being performed
     *                       on
     * @param additionalArgs Used for as arguments for string formating
     */
    public ActionListener(Context context, Action action, ConnectionMqtt c,/*String clientHandle,*/
                          String... additionalArgs) {
        this.context = context;
        this.action = action;
//		this.clientHandle = clientHandle;
        this.clientHandle = c.handle();
        this.additionalArgs = additionalArgs;
        this.connectionMqtt = c;
    }

    /**
     * The action associated with this listener has been successful.
     *
     * @param asyncActionToken This argument is not used
     */
    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        switch (action) {
            case CONNECT:
                connect();
                break;
            case DISCONNECT:
                disconnect();
                break;
            case SUBSCRIBE:
                subscribe();
                break;
            case PUBLISH:
                publish();
                break;
        }
    }

    /**
     * A publish action has been successfully completed, update connectionMqtt
     * object associated with the client this action belongs to, then notify the
     * user of success
     */
    private void publish() {

//		Log.e(TAG, "publish , "+additionalArgs[0]);
        // ConnectionMqtt c =
        // Connections.getInstance(context).getConnection(clientHandle);
//		ConnectionMqtt c = RebootService.getConnection(context);
        if (!connectionMqtt.handle().equals(clientHandle)) {
            return;
        }

        CaltxtHandler.get(context).updateSent(0, additionalArgs[0]);

//		String actionTaken = context.getString(R.string.toast_pub_success, additionalArgs[0], additionalArgs[1]);
//		c.addAction(actionTaken);
        Connection.get().addAction(Constants.mqttPublishedProperty, additionalArgs[1]/*topic*/, additionalArgs[0]/*message*/);
    }

    /**
     * A subscribe action has been successfully completed, update the connectionMqtt
     * object associated with the client this action belongs to and then notify
     * the user of success
     */
    private void subscribe() {
        // ConnectionMqtt c =
        // Connections.getInstance(context).getConnection(clientHandle);
//		ConnectionMqtt c = RebootService.getConnection(context);
        if (!connectionMqtt.handle().equals(clientHandle)) {
            return;
        }

//		String actionTaken = context.getString(R.string.toast_sub_success,
//				(Object[]) additionalArgs);
//		c.addAction(actionTaken);
    }

    /**
     * A disconnection action has been successfully completed, update the
     * connectionMqtt object associated with the client this action belongs to and
     * then notify the user of success.
     */
    private void disconnect() {
        // ConnectionMqtt c =
        // Connections.getInstance(context).getConnection(clientHandle);
//		ConnectionMqtt c = RebootService.getConnection(context);

        //added 11-JAN-2018 to reset connection object to null
        //this will reinitialize connection object in RebootService.getConnection() call
        //commented 02-2018, let the MqttConnection be not created every now and then
        //what is the benefit?
//        RebootService.setDisconnected();

        if (!connectionMqtt.handle().equals(clientHandle)) {
            return;
        }
        connectionMqtt.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

//		String actionTaken = context.getString(R.string.toast_disconnected);
        /*if (pingSender != null)
        {
            context.unregisterReceiver(pingSender);
            pingSender = null;
        }*/
    }

    /**
     * A connectionMqtt action has been successfully completed, update the
     * connectionMqtt object associated with the client this action belongs to and
     * then notify the user of success.
     */
    private void connect() {

        // ConnectionMqtt c =
        // Connections.getInstance(context).getConnection(clientHandle);
//		ConnectionMqtt c = RebootService.getConnection(context);
        if (!connectionMqtt.handle().equals(clientHandle)) {
            return;
        }
        Log.v(TAG, "ActionListener::connect()");
        connectionMqtt.changeConnectionStatus(ConnectionMqtt.ConnectionStatus.CONNECTED);
//		commented 25-FEB-17, opening this will disturb connectionMqtt state
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        connectionMqtt.getClient().setBufferOpts(disconnectedBufferOptions);

        CaltxtHandler.get(context).subscribeTopics();

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
        //send not-delivered messages
//		CaltxtHandler.get(context).flushUndeliveredMessages();
    }

    /**
     * The action associated with the object was a failure
     *
     * @param token     This argument is not used
     * @param exception The exception which indicates why the action failed
     */
    @Override
    public void onFailure(IMqttToken token, Throwable exception) {
        switch (action) {
            case CONNECT:
                connect(exception);
                break;
            case DISCONNECT:
                disconnect(exception);
                break;
            case SUBSCRIBE:
                subscribe(exception);
                break;
            case PUBLISH:
                publish(exception);
                break;
        }
    }

    /**
     * A publish action was unsuccessful, notify user and update client history
     *
     * @param exception This argument is not used
     */
    private void publish(Throwable exception) {
        Log.e(TAG, "ActionListener::publish() exception " + exception);
        // ConnectionMqtt c =
        // Connections.getInstance(context).getConnection(clientHandle);
//		ConnectionMqtt c = RebootService.getConnection(context);
        if (!connectionMqtt.handle().equals(clientHandle)) {
            return;
        }

        Connection.get().addAction(Constants.messageErrorProperty, additionalArgs[1], additionalArgs[0]);
//		String action = context.getString(R.string.toast_pub_failed,
//				additionalArgs[1], additionalArgs[0]);
//		c.addAction(action);
    }

    /**
     * A subscribe action was unsuccessful, notify user and update client
     * history
     *
     * @param exception This argument is not used
     */
    private void subscribe(Throwable exception) {
        Log.e(TAG, "ActionListener::subscribe() exception " + exception);
        // ConnectionMqtt c =
        // Connections.getInstance(context).getConnection(clientHandle);
//		ConnectionMqtt c = RebootService.getConnection(context);
        if (!connectionMqtt.handle().equals(clientHandle)) {
            return;
        }

//		String action = context.getString(R.string.toast_sub_failed,
//				(Object[]) additionalArgs);
//		c.addAction(action);
    }

    /**
     * A disconnect action was unsuccessful, notify user and update client
     * history
     *
     * @param exception This argument is not used
     */
    private void disconnect(Throwable exception) {
        Log.e(TAG, "ActionListener::disconnect() exception " + exception);
        // ConnectionMqtt c =
        // Connections.getInstance(context).getConnection(clientHandle);
//		ConnectionMqtt c = RebootService.getConnection(context);
        if (!connectionMqtt.handle().equals(clientHandle)) {
            return;
        }

        connectionMqtt.changeConnectionStatus(ConnectionStatus.ERROR);
        //added 11-JAN-2018 to reset connection object to null
        //this will reinitialize connection object in RebootService.getConnection() call
        //commented 02-2018, let the MqttConnection be not created every now and then
        //what is the benefit?
//        RebootService.setDisconnected();

//		c.addAction(Constants.connectErrorProperty, null, null);
    }

    /**
     * A connect action was unsuccessful, notify the user and update client
     * history
     *
     * @param exception This argument is not used
     */
    private void connect(Throwable exception) {
        Log.e(TAG, "ActionListener::connect() exception " + exception);
        // ConnectionMqtt c =
        // Connections.getInstance(context).getConnection(clientHandle);
//		ConnectionMqtt c = RebootService.getConnection(context);

        //added 11-JAN-2018 to reset connection object to null
        //this will reinitialize connection object in RebootService.getConnection() call
        //commented 02-2018, let the MqttConnection be not created every now and then
        //what is the benefit?
//        RebootService.setDisconnected();

        // connect exception also comes when Mqtt is already connected
        if (!connectionMqtt.isConnected()) {
            connectionMqtt.changeConnectionStatus(ConnectionStatus.ERROR);

            if (!connectionMqtt.handle().equals(clientHandle)) {
                return;
            }

//		c.addAction(Constants.connectErrorProperty, null, null);
        }

//		c.addAction("Client failed to connect");

//		ConnectionMqtt.CONNECT_THREAD_RUNNING = false;
        // recycle the ConnectionMqtt object, otherwise setAutomaticReconnect(true) will not work!
        //commented 30-MAR-17, exception, Attempt to invoke virtual method
        // 'void org.eclipse.paho.android.service.MqttService.unsubscribe(java.lang.String,
        // java.lang.String, java.lang.String, java.lang.String)' on a null object reference
//        RebootService.setConnection(null);
    }
}
