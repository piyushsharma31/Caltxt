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
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.activity.Settings;
import com.jovistar.caltxt.activity.SignupProfile;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.mqtt.client.ActionListener.Action;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.network.data.ConnectivityBroadcastReceiver;
import com.jovistar.caltxt.network.voice.TelephonyInfo;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.commons.bo.XMob;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents a {@link MqttAndroidClient} and the actions it has
 * performed
 */
public class ConnectionMqtt {
    private static final String TAG = "ConnectionMqtt";

    static ConnectionMqtt connectionMqtt;//singleton connectionMqtt object
    /**
     * ClientHandle for this ConnectionMqtt Object
     **/
    private String clientHandle = null;
    /**
     * The clientId of the client associated with this <code>ConnectionMqtt</code>
     * object
     **/
    private String clientId = null;
    /**
     * The host that the {@link MqttAndroidClient} represented by this
     * <code>ConnectionMqtt</code> is represented by
     **/
    private String host = null;
    /**
     * The port on the server this client is connecting to
     **/
    private int port = 0;
    /**
     * {@link ConnectionStatus} of the {@link MqttAndroidClient}
     * represented by this <code>ConnectionMqtt</code> object. Default value is
     * {@link ConnectionStatus#NONE}
     **/
    private ConnectionStatus status = ConnectionStatus.NONE;
    /**
     * The history of the {@link MqttAndroidClient} represented by this
     * <code>ConnectionMqtt</code> object
     **/
    private ArrayList<String> history = null;
    /**
     * The {@link MqttAndroidClient} instance this class represents
     **/
    private MqttAndroidClient client = null;

    /**
     * The {@link Context} of the application this object is part of
     **/
    private Context context = null;

    /**
     * The {@link MqttConnectOptions} that were used to connect this client
     **/
    private MqttConnectOptions conOpt;

    /**
     * True if this connection is secured using SSL
     **/
    private boolean tlsConnection = false;

    /**
     * Persistence id, used by {@link Persistence}
     **/
    private long persistenceId = -1;

    /** The list of this connection's subscriptions
     private final Map<String, Subscription> subscriptions = new HashMap<String, Subscription>();

     private final ArrayList<ReceivedMessage> messageHistory =  new ArrayList<ReceivedMessage>();

     private final ArrayList<IReceivedMessageListener> receivedMessageListeners = new ArrayList<IReceivedMessageListener>();
     **/
    /**
     * Connections status for a connection
     */
    public enum ConnectionStatus {

        /**
         * Client is Connecting
         **/
        CONNECTING,
        /**
         * Client is Connected
         **/
        CONNECTED,
        /**
         * Client is Disconnecting
         **/
        DISCONNECTING,
        /**
         * Client is Disconnected
         **/
        DISCONNECTED,
        /**
         * Client has encountered an Error
         **/
        ERROR,
        /**
         * Status is unknown
         **/
        NONE
    }

    public static synchronized ConnectionMqtt getConnection(Context context) {
        if (!SignupProfile.isSIM1Verified(context)
                && !SignupProfile.isSIM2Verified(context)) {
            return (connectionMqtt = null);
        }

        if (connectionMqtt == null
//				|| (connectionMqtt.isConnectedOrConnecting()==false
//				&& connectionMqtt.isConnected()==false
//				&& ConnectivityBroadcastReceiver.haveNetworkConnection(context)
                ) {
            connectionMqtt = ConnectionMqtt.createConnection(
                    "caltxt." + Addressbook.getMyProfile().getUsername(),
                    "caltxt." + Addressbook.getMyProfile().getUsername(),
                    context.getResources().getString(R.string.serverURIHint),
                    Constants.mqtt_defaultPort,
                    context.getApplicationContext(),
                    Constants.mqtt_defaultSsl);
            connectionMqtt.getClient().setCallback(new MqttCallbackHandler(context, connectionMqtt.handle()));
            connectionMqtt.getClient().setTraceEnabled(/*true 02-2018 commented*/false);
            connectionMqtt.changeConnectionStatus(ConnectionStatus.DISCONNECTED);
            initMqttConnection(context.getApplicationContext());

//        } else if (connectionMqtt.isConnectedOrConnecting() == false && ConnectivityBroadcastReceiver.haveNetworkConnection()) {

            // 15-dec-18, added because the reconnect was going in loop in this condition.
            // commented, 16-dec-18, why mqttService should handle reconnect
//            initMqttConnection(context.getApplicationContext());

        }

        return connectionMqtt;
    }

    // this method must be private and be called from within this class only
    // otherwise may result in inconsistent state of mqtt connectionMqtt
    private synchronized static void initMqttConnection(Context context) {

        Log.v(TAG, "initMqttConnection, ENTRY");

//		boolean doConnect = true;
//		String host = context.getResources().getString(R.string.serverURIHint);
/*		if(connectionMqtt==null) {
			// 19-JUN-16 changed client handle to username, from IMEI
			connectionMqtt = ConnectionMqtt.createConnection(Addressbook.getInstance(context).getMyProfile().getUsername(),
					Addressbook.getInstance(context).getMyProfile().getUsername(),
					context.getResources().getString(R.string.serverURIHint),
					Constants.mqtt_defaultPort,
					context,
					Constants.mqtt_defaultSsl);
*/
        MqttConnectOptions conOpt = new MqttConnectOptions();
//			conOpt.setAutomaticReconnect(true);
        conOpt.setCleanSession(false);//Durable Subscriptions (cleanSession = false);broker store messages for him
        conOpt.setConnectionTimeout(Constants.mqtt_defaultConnectionTimeOut);
        conOpt.setKeepAliveInterval(Constants.mqtt_defaultConnectionKeepAlive);

        XMob will = new XMob();
        will.init(Addressbook.getInstance(context).getMyProfile().toString());
        will.setHeadline(XMob.STRING_STATUS_OFFLINE);
        will.setStatusOffline();

        String tmp = will.toString().substring(0, 2)
                + Base64.encodeToString(will.toString().getBytes(), Base64.DEFAULT);

        conOpt.setWill(Constants.CALTXT_WILL_TOPIC + Addressbook.getInstance(context).getMyProfile().getUsername(),
                tmp.getBytes(),
                Constants.mqtt_defaultQosDiscovery, false/*retained*/);
        if (/*TelephonyInfo.getInstance(context)*/SignupProfile.isDualSIM() && Addressbook.getInstance(context).getMyProfile().getNumber2().length() > 0) {
            conOpt.setWill(Constants.CALTXT_WILL_TOPIC +
                            XMob.toFQMN(Addressbook.getInstance(context).getMyProfile().getNumber2(), Addressbook.getInstance(context).getMyCountryCode()),
                    tmp.getBytes(),
                    Constants.mqtt_defaultQosDiscovery, false/*retained*/);
        }
        connectionMqtt.addConnectionOptions(conOpt);

        String[] actionArgs = new String[1];
        actionArgs[0] = Addressbook.getMyProfile().getUsername();

        final ActionListener action_listner = new ActionListener(context,
                ActionListener.Action.CONNECT, connectionMqtt, actionArgs);

        try {
            connectionMqtt.changeConnectionStatus(ConnectionStatus.CONNECTING);
            connectionMqtt.getClient().connect(conOpt, null, action_listner);
        } catch (MqttException e) {
            Log.e(TAG, "initMqttConnection exception " + e.getLocalizedMessage());
        }

        Log.v(TAG, "initMqttConnection, EXIT");
    }

    /**
     * Creates a connection from persisted information in the database store,
     * attempting to create a {@link MqttAndroidClient} and the client
     * handle.
     *
     * @param clientId      The id of the client
     * @param host          the server which the client is connecting to
     * @param port          the port on the server which the client will attempt to
     *                      connect to
     * @param context       the application context
     * @param tlsConnection true if the connection is secured by SSL
     * @return a new instance of <code>ConnectionMqtt</code>
     */
    public static ConnectionMqtt createConnection(String clientHandle, String clientId, String host, int port, Context context, boolean tlsConnection) {

        String uri;
        if (tlsConnection) {
            uri = "ssl://" + host + ":" + port;
        } else {
            uri = "tcp://" + host + ":" + port;
        }

        MqttAndroidClient client = new MqttAndroidClient(context, uri, clientId);
        return new ConnectionMqtt(clientHandle, clientId, host, port, context, client, tlsConnection);
    }

    /**
     * Changes the connection status of the client
     * <p>
     * The connection status of this connection
     */
    public void changeConnectionStatus(ConnectionStatus newStatus) {
        boolean statusChanged = false;
        if (status != newStatus && (newStatus.equals(ConnectionStatus.CONNECTED)
                || newStatus.equals(ConnectionStatus.DISCONNECTED))) {
            statusChanged = true;
        }

        status = newStatus;

        if (newStatus.equals(ConnectionStatus.CONNECTED)) {

            //already connected, no need to monitor network status change
            ConnectivityBroadcastReceiver.disableDataConnectivityListener(context);

        } else if (newStatus.equals(ConnectionStatus.CONNECTING) /*||
                newStatus.equals(ConnectionStatus.DISCONNECTING)*/) {

        } else {//ERROR, NONE, DISCONNECTED, DISCONNECTING

            Addressbook.getInstance(context).updateStatusAllOffline();
            // 02-FEB-17, using MqttService as gradle package (paho service)
            // reconnect is tried by MqttService itself on network state change
            // so need to reconnect here!
//			MqttPingAlarm.cancel(context);
            //has network connection
            if (ConnectivityBroadcastReceiver.haveNetworkConnection()) {
                //try connecting again in 1 minute
				/*new CountDownTimer(Constants.CALTXT_RECONNECT_WAIT, Constants.CALTXT_RECONNECT_WAIT) {
					public void onTick(long millisUntilFinished) {
					}
					public void onFinish() {
						reconnect();
					}
				}.start();*/

            } else {
                //disconnected, monitor network status change
                ConnectivityBroadcastReceiver.enableDataConnectivityListener();
            }
        }

        if (statusChanged) {

            Connection.get().addAction(Constants.ConnectionStatusProperty, null, null);

        }
    }

    public void updateConnection(String clientId, String host, int port, boolean tlsConnection) {
        String uri;
        if (tlsConnection) {
            uri = "ssl://" + host + ":" + port;
        } else {
            uri = "tcp://" + host + ":" + port;
        }

        this.clientId = clientId;
        this.host = host;
        this.port = port;
        this.tlsConnection = tlsConnection;
        this.client = new MqttAndroidClient(context, uri, clientId);

    }

	/*
	 * public static ConnectionMqtt get() { return connection; }
	 */

    /**
     * Creates a connection object with the server information and the client
     * hand which is the reference used to pass the client around activities
     *
     * @param clientHandle  The handle to this <code>ConnectionMqtt</code> object
     * @param clientId      The Id of the client
     * @param host          The server which the client is connecting to
     * @param port          The port on the server which the client will attempt to connect to
     * @param context       The application context
     * @param client        The MqttAndroidClient which communicates with the service for this connection
     * @param tlsConnection true if the connection is secured by SSL
     */
    private ConnectionMqtt(String clientHandle, String clientId, String host,
                           int port, Context context, MqttAndroidClient client, boolean tlsConnection) {
        //generate the client handle from its hash code
        this.clientHandle = clientHandle;
        this.clientId = clientId;
        this.host = host;
        this.port = port;
        this.context = context;
        this.client = client;
        this.tlsConnection = tlsConnection;
        history = new ArrayList<String>();
        String sb = "Client: " +
                clientId +
                " created";

    }

    /**
     * Add an action to the history of the client
     *
     *            the history item to add
     *
     *            public void addAction(String action) {
     *
     *            Object[] args = new String[1]; SimpleDateFormat sdf = new
     *            SimpleDateFormat( context.getString(R.string.dateFormat));
     *            args[0] = sdf.format(new Date());
     *
     *            String timestamp = context.getString(R.string.timestamp,
     *            args); history.add(action + timestamp);
     *
     *            notifyListeners(new PropertyChangeEvent(this,
     *            Constants.historyProperty, null, action)); }
     */
    // added by Piyush (END)

    /**
     * Generate an array of Spanned items representing the history of this
     * connection.
     *
     * @return an array of history entries
     */
    public Spanned[] history() {

        int i = 0;
        Spanned[] array = new Spanned[history.size()];

        for (String s : history) {
            if (s != null) {
                array[i] = Html.fromHtml(s);
                i++;
            }
        }

        return array;
    }

    /**
     * Gets the client handle for this connection
     *
     * @return client Handle for this connection
     */
    public String handle() {
        return clientHandle;
    }

    /**
     * Determines if the client is connected
     *
     * @return is the client connected
     */
    public boolean isConnected() {
        try {
            return client.isConnected();
        } catch (Exception e) {
            return false;
        }
//		return status == ConnectionStatus.CONNECTED;
    }

    /**
     * Determines if the client is disconnected
     *
     * @return is the client connected
     */
    public boolean isDisconnected() {
        try {
            return !client.isConnected();
        } catch (Exception e) {
            return true;
        }
//		return status == ConnectionStatus.DISCONNECTED;
    }

    /**
     * A string representing the state of the client this connection object
     * represents
     *
     * @return A string representing the state of the client
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(clientId);
        sb.append("\n ");

        switch (status) {

            case CONNECTED:
                sb.append(context.getString(R.string.connectedto));
                break;
            case DISCONNECTED:
                sb.append(context.getString(R.string.disconnected));
                break;
            case NONE:
                sb.append(context.getString(R.string.no_status));
                break;
            case CONNECTING:
                sb.append(context.getString(R.string.connecting));
                break;
            case DISCONNECTING:
                sb.append(context.getString(R.string.disconnecting));
                break;
            case ERROR:
                sb.append(context.getString(R.string.connectionError));
        }
        sb.append(" ");
        sb.append(host);

        return sb.toString();
    }

    /**
     * Determines if a given handle refers to this client
     *
     * @param handle The handle to compare with this clients handle
     * @return true if the handles match
     */
    public boolean isHandle(String handle) {
        return clientHandle.equals(handle);
    }

    /**
     * Compares two connection objects for equality this only takes account of
     * the client handle
     *
     * @param o The object to compare to
     * @return true if the client handles match
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConnectionMqtt)) {
            return false;
        }

        ConnectionMqtt c = (ConnectionMqtt) o;

        return clientHandle.equals(c.clientHandle);
    }

    /**
     * Get the client Id for the client this object represents
     *
     * @return the client id for the client this object represents
     */
    public String getId() {
        return clientId;
    }

    /**
     * Get the host name of the server that this connection object is associated
     * with
     *
     * @return the host name of the server this connection object is associated
     * with
     */
    public String getHostName() {

        return host;
    }

    /**
     * Determines if the client is in a state of connecting or connected.
     *
     * @return if the client is connecting or connected
     */
    public boolean isConnectedOrConnecting() {
        return isConnected() || (status == ConnectionStatus.CONNECTING);
//		return (status == ConnectionStatus.CONNECTED)
//				|| (status == ConnectionStatus.CONNECTING);
    }

    /**
     * Client is currently not in an error state
     *
     * @return true if the client is in not an error state
     */
    public boolean noError() {
        return status != ConnectionStatus.ERROR;
    }

    /**
     * Gets the client which communicates with the android service.
     *
     * @return the client which communicates with the android service
     */
    public MqttAndroidClient getClient() {
        return client;
    }

    /**
     * Add the connectOptions used to connect the client to the server
     *
     * @param connectOptions the connectOptions used to connect to the server
     */
    public void addConnectionOptions(MqttConnectOptions connectOptions) {
        conOpt = connectOptions;
    }

    /**
     * Get the connectOptions used to connect this client to the server
     *
     * @return The connectOptions used to connect the client to the server
     */
    public MqttConnectOptions getConnectionOptions() {
        return conOpt;
    }

    /**
     * Gets the port that this connection connects to.
     *
     * @return port that this connection connects to
     */
    public int getPort() {
        return port;
    }

    /**
     * Determines if the connection is secured using SSL, returning a C style
     * integer value
     *
     * @return 1 if SSL secured 0 if plain text
     */
    public int isSSL() {
        return tlsConnection ? 1 : 0;
    }

    /**
     * Assign a persistence ID to this object
     *
     * @param id the persistence id to assign
     */
    public void assignPersistenceId(long id) {
        persistenceId = id;
    }

    /**
     * Returns the persistence ID assigned to this object
     *
     * @return the persistence ID assigned to this object
     */
    public long persistenceId() {
        return persistenceId;
    }

    /**
     * Reconnect the selected client
     * *
     public void reconnect() {
     if(isConnectedOrConnecting())
     return;

     changeConnectionStatus(ConnectionStatus.CONNECTING);

     //		try {
     //			Thread.sleep(3000);//just wait for 5 seconds in case someone doing successive reconnect
     //		} catch (InterruptedException e) {
     //		}

     try {
     getClient().connect(
     conOpt,
     null,
     new ActionListener(context, Action.CONNECT, clientHandle,
     (String[]) null));
     //		} catch (MqttSecurityException e) {
     // connection.addAction("Client failed to connect");
     } catch (MqttException e) {
     Log.e(TAG,
     "Failed to reconnect the client with the handle "
     + clientHandle);
     }
     }
     */
    /**
     * Disconnect the client
     */
    public void disconnect() {

		/*11-11-14: commented to not stop any disconnect request
		// if the client is not connected, process the disconnect
		if (!isConnected()) {
			return;
		}*/
        changeConnectionStatus(ConnectionStatus.DISCONNECTING);

        try {
            getClient().disconnect(
                    null,
                    new ActionListener(context, Action.DISCONNECT,
                            this, (String[]) null));

            // Intent svc = new Intent(activitySettings, MqttService.class);
        } catch (MqttException e) {
            Log.e(TAG,
                    "Failed to disconnect the client with the handle "
                            + clientHandle);
            // connection.addAction("Client failed to disconnect");
        }
    }

    /**
     * Subscribe to a topic that the user has specified
     */
    public void subscribe(String topic) {
//		int qos = Constants.defaultQos;

		/*11-11-14: commented to not stop any publish request; the message will get through once re-connected
		if (!isConnected()) {
			// notify the user, not connected; and try connecting
			return;
		}*/

        String[] topics = new String[1];
        topics[0] = topic;
        try {
            // Connections.getInstance(this).getConnection(clientHandle).getClient()
            getClient().subscribe(
                    topics[0],
                    Constants.mqtt_defaultQosMessage,
                    null,
                    new ActionListener(context, Action.SUBSCRIBE, this,
                            topics));
//		} catch (MqttSecurityException e) {
        } catch (MqttException e) {
            Log.e(TAG, "Failed to subscribe to"
                    + topics[0] + " the client with the handle " + clientHandle);
        }
    }

    /**
     * Unsubscribe to a topic that the user has specified
     */
    public void unsubscribe(String topic) {
//		int qos = Constants.defaultQos;

		/*11-11-14: commented to not stop any publish request; the message will get through once re-connected
		if (!isConnected()) {
			// notify the user, not connected; and try connecting
			return;
		}*/

        String[] topics = new String[1];
        topics[0] = topic;
        try {
            // Connections.getInstance(this).getConnection(clientHandle).getClient()
            getClient().unsubscribe(topics[0]);
//		} catch (MqttSecurityException e) {
        } catch (MqttException e) {
            Log.e(TAG,
                    "Failed to unsubscribe to" + topics[0]
                            + " the client with the handle " + clientHandle);
        }
    }

    ExecutorService executorService = Executors.newFixedThreadPool(5);

    //	private Map<String, Long> statusCache = Collections
//			.synchronizedMap(new WeakHashMap<String, Long>());
/*
	// Task for the queue
	private class ContactToPing {
		public String username;
		public ImageView imageView;

		public ContactToPing(String u, ImageView i) {
			username = u;
			imageView = i;
		}
	}
*/
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

            //display photo
//			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
//			Activity a = (Activity) contactToPing.imageView.getContext();
//			a.runOnUiThread(bd);
//			}
        }
    }

    public void submitPingContact(String from, String contact) {
//		if (statusCache.containsKey(contact)==false
//				|| (Calendar.getInstance().getTimeInMillis() - statusCache.get(contact).longValue()) > Constants.mqtt_defaultPingInterval) {
//			statusCache.put(contact, Calendar.getInstance().getTimeInMillis());
        executorService.submit(new ContactPinger(from, contact, false));
//		}
        Log.i(TAG, "submitPingContact, contact:" + contact);
    }

    public void submitAckPingContact(String from, String contact) {
//		if (statusCache.containsKey(contact)==false
//				|| (Calendar.getInstance().getTimeInMillis() - statusCache.get(contact).longValue()) > Constants.mqtt_defaultPingInterval) {
//			statusCache.put(contact, Calendar.getInstance().getTimeInMillis());
        executorService.submit(new ContactPinger(from, contact, true));
//		}
        Log.i(TAG, "submitAckPingContact, contact:" + contact);
    }

    public void submitForcedPingContact(String from, String contact) {
        String ffrom = XMob.toFQMN(from, Addressbook.getMyCountryCode());
        String ccontact = XMob.toFQMN(contact, Addressbook.getMyCountryCode());
//		statusCache.put(ccontact, Calendar.getInstance().getTimeInMillis());
        executorService.submit(new ContactPinger(ffrom, ccontact, false));
        Log.i(TAG, "submitForcedPingContact, contact:" + ccontact);
    }

    private void pingContact(String from, String contact) {
        //DO NOT PING SELF
        if (Addressbook.getInstance(context).getMyProfile().getUsername().endsWith(contact)
                || Addressbook.getInstance(context).getMyProfile().getNumber2().endsWith(contact))
            return;
//		Log.i(TAG, "pingContact, contact:"+contact+", discoverable?"+Settings.iam_discoverable_by_anyone);
        if (Addressbook.getInstance(context).isContact(contact) || Settings.iam_discoverable_by_anyone) {
//			Log.e(TAG, "pingContact, contact:"+contact);
            XMob frm = Addressbook.getInstance(context).getMyProfile();
//			Log.i(TAG, "pingContact, frm.toString():"+frm.toString());
            if (from.endsWith(frm.getUsername())) {
                //nothing to do
            } else if (from.endsWith(frm.getNumber2())) {
                String u = frm.getUsername();
                frm.setUsername(XMob.toFQMN(frm.getNumber2(), Addressbook.getInstance(context).getMyCountryCode()));
                frm.setNumber2(u);
            }

            publish(Constants.CALTXT_MESSAGE_TOPIC + contact,
                    frm.toString(),
                    Constants.mqtt_defaultQosDiscovery);

            //mark if as offline (pingACK will confirm online status)
            XMob mob = Addressbook.getInstance(context).getRegistered(contact);
            if (mob != null && mob.isRegistered()/*mob.getStatus()!=XMob.STATUS_UNREGISTERED*/)//registered Caltxt user
                mob.setStatusOffline();
//				mob.setStatus(XMob.STATUS_OFFLINE);
        }
    }

    private void ackPing(String from/*idenfies SIM from which ping should be sent*/, String contact) {
        if (Addressbook.getInstance(context).getMyProfile().getUsername().endsWith(contact)
                || Addressbook.getInstance(context).getMyProfile().getNumber2().endsWith(contact))
            return;
//		Log.i(TAG, "ackPing, contact:"+contact+", discoverable?"+Settings.iam_discoverable_by_anyone);

        if (Addressbook.getInstance(context).isContact(contact) || Settings.iam_discoverable_by_anyone) {
            XMob frm = Addressbook.getInstance(context).getMyProfile();
            String ackString = frm.toStringForACK();

            if (from.endsWith(frm.getUsername())) {
                //nothing to do
            } else if (from.endsWith(frm.getNumber2())) {
                String u = frm.getUsername();
                frm.setUsername(XMob.toFQMN(frm.getNumber2(), Addressbook.getInstance(context).getMyCountryCode()));
                frm.setNumber2(u);
                ackString = frm.toStringForACK();
            }

            publish(Constants.CALTXT_MESSAGE_TOPIC + contact,
                    ackString,
                    Constants.mqtt_defaultQosDiscovery);
        }
    }

    public void publishXCtx(String to, String message, long pid) {
        Log.i(TAG, "publishXCtx, to:" + to + ", message:" + message);
//		if(Addressbook.getInstance(context).isRegisteredAndConnected(to)) {
        publish(Constants.CALTXT_MESSAGE_TOPIC + to, message, Constants.mqtt_defaultQosMessage);
//		}

//		RebootService.getConnection(context).addAction(Constants.mqttPublishedProperty, message, pid);
    }

    /*
    public void publishAlert(String topic, String message) {
        publish(Constants.CALTXT_ADMIN_ALERTS_TOPIC+topic, message, Constants.mqtt_defaultQosMessage);
    }
*/
    public void publishKeepAlive() {

        final String MQTT_KEEP_ALIVE_TOPIC = "/users/" + Addressbook.getInstance(context).getMyProfile().getUsername() + "/keepalive"; // Topic
//	    final byte[] MQTT_KEEP_ALIVE_MESSAGE = { 0 };
        publish(MQTT_KEEP_ALIVE_TOPIC, "000"/*so that our msg is 2 char long atleast*/,
                Constants.mqtt_defaultQosDiscovery);

    }

    private void publish(String topic, String message, int qos) {
        Log.i(TAG, "publish, topic:" + topic + " message " + message);
        if (message == null || message.length() == 0 || topic == null || topic.length() == 0)
            return;
//		int qos = Constants.defaultQos;
//		boolean retained = false;
/*		while(topic.startsWith("0") && topic.length()>0)
			topic = topic.substring(1);
		topic.replaceAll("[-+]", "");
*/
        String[] args = new String[2];
        args[0] = message;
        args[1] = topic;

		/*11-11-14: commented to not stop any publish request; the message will get through once re-connected
		if (!isConnected()) {
			// notify the user, not connected; and try connecting
			return;
		}*/

        String tmp = new String(message.getBytes()).substring(0, 2)
                + Base64.encodeToString(message.getBytes(), Base64.DEFAULT);

        // Connections.getInstance(this).getConnection(clientHandle).getClient()
        if (getClient() != null) {
            try {
                getClient().publish(
                        args[1],
                        tmp.getBytes(),
						/*Constants.defaultQos*/qos,
                        Constants.mqtt_defaultRetained /*retained? send empty msg to clear topic with retained msgs*/,
                        null,
                        new ActionListener(context, Action.PUBLISH,
                                this, args));
            } catch (MqttSecurityException e) {
                Log.e(TAG,
                        "Failed to publish a messged from the client with the handle "
                                + clientHandle + ", " + e.getLocalizedMessage());
            } catch (MqttPersistenceException e) {
                Log.e(TAG,
                        "Failed to publish a messged from the client with the handle "
                                + clientHandle + ", " + e.getLocalizedMessage());
            } catch (MqttException e) {
                Log.e(TAG,
                        "Failed to publish a messged from the client with the handle "
                                + clientHandle + ", " + e.getLocalizedMessage());
            } catch (Exception e) {
                Log.e(TAG,
                        "Failed to publish a messged from the client with the handle "
                                + clientHandle + ", " + e.getLocalizedMessage());
            } finally {
            }
        }

        Connection.get().addAction(Constants.mqttPublishingProperty, topic, message);
    }

}
