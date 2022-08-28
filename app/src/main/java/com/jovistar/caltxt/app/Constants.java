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
package com.jovistar.caltxt.app;

import com.jovistar.caltxt.mqtt.client.ConnectionMqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;


/**
 * This Class provides constants used for returning results from an activity
 */
public class Constants {

    /**
     * Admin topic for Caltxt where WILL is sent
     **/
//	  public static String CALTXT_ADMIN_ADS_TOPIC = "com.jovistar/caltxt/adm/ads/";//Ads
//	  public static String CALTXT_ADMIN_ALERTS_TOPIC = "com.jovistar/caltxt/adm/alerts/";//Administrative messages
    /*voice mail, missed calls (when receive was offline)*/
//	public static String CALTXT_VOICEMAIL_TOPIC = "com.jovistar/caltxt/helo/";
	/*actual subscription topic is caltxt/msg/fqmn*/
    public static String IMAGE_FILE_EXTN = ".jpg";
    public static final String CALTXT_MESSAGE_TOPIC = "com.jovistar/caltxt/msg/";
    public static final String CALTXT_WILL_TOPIC = "com.jovistar/caltxt/wll/";
    /*Default values **/
    public static final int MAX_SEARCH_RESULT_SIZE = 10;
    public static final int MAX_HISTORY_SIZE = 10;
    public static final int CALL_TYPE_INBOUND = 1, CALL_TYPE_OUTBOUND = 2;
    /**
     * Default QOS value
     * QoS0, At most once: The message is delivered at most once, or it may not be delivered at all. Its delivery
     * across the network is not acknowledged. The message is not stored. The message could be lost if the client
     * is disconnected, or if the server fails. QoS0 is the fastest mode of transfer. It is sometimes called
     * "fire and forget".
     * QoS1, At least once: The message is always delivered at least once. It might be delivered multiple times if
     * there is a failure before an acknowledgment is received by the sender. The message must be stored locally at
     * the sender, until the sender receives confirmation that the message has been published by the receiver.
     * The message is stored in case the message must be sent again.
     * QoS2, Exactly once: The message is always delivered exactly once. The message must be stored locally at the
     * sender, until the sender receives confirmation that the message has been published by the receiver. The message
     * is stored in case the message must be sent again. QoS2 is the safest, but slowest mode of transfer. A more
     * sophisticated handshaking and acknowledgment sequence is used than for QoS1 to ensure no duplication of
     * messages occurs.
     */
    public static final int wifiscan_defaultInterval = 15 * 60 * 1000;//1200000;//at what rate contacts are pinged
    public static final int mqtt_defaultPingInterval = 20 * 60 * 1000;//1200000;//at what rate contacts are pinged
    public static final int mqtt_defaultQosDiscovery = 0;
    public static final int mqtt_defaultQosMessage = 2;
	  /*
	   * Two wildcards are available, + or #.
	   * + can be used as a wildcard for a single level of hierarchy.
	   *     sensors/+/temperature/+
	   *     following subscriptions will match:
	   *     a/b/c/d
	   *     +/b/c/d
	   *     a/+/c/d
	   *     a/+/+/d
	   *     +/+/+/+
	   * # can be used as a wildcard for all remaining levels of hierarchy
	   * 	With a topic of "a/b/c/d" following subscription will match
	   * 	a/b/c/d
	   * 	a/#
	   * 	a/b/#
	   * 	a/b/c/#
	   * 	+/b/c/#
	   */
    /**
     * Default timeout
     */
    public static final int firebase_defaultPingTTL = 15;//15 seconds
    public static final int firebase_defaultMessageTTL = 24 * 60 * 60;//24 hours
    public static final int mqtt_defaultConnectionTimeOut = 5;//in seconds
    /**
     * Default keep alive value
     */
    public static final int mqtt_defaultConnectionKeepAlive = 30 * 60 * 1000;//240;//in seconds. changed 24-DEC-16

    public static final int firebase_defaultPingInterval = 30 * 1000;//240;//in seconds. changed 24-DEC-16
    /**
     * Default SSL enabled flag
     */
    public static final boolean mqtt_defaultSsl = false;
    /**
     * Default message retained flag
     */
    public static final boolean mqtt_defaultRetained = false;
    /**
     * Default last will message
     */
    public static final MqttMessage mqtt_defaultLastWill = null;
    /**
     * Default port
     */
    public static final int mqtt_defaultPort = 24816;
    /**
     * Show History Request Code
     **/
    public static final int showHistory = 3;
    public static final String FRAGMENT_BLOCKED = "Blocked";
    public static final String FRAGMENT_SEARCH = "Discover";
    public static final String FRAGMENT_CONTACTS = "Contacts";
    public static final String FRAGMENT_CALLS = "Calls";
    public static final String FRAGMENT_STATUS = "Status";

    /** Connect Request Code */
//  static final int mqtt_connect = 0;
    /** Advanced Connect Request Code  **/
//  static final int mqtt_advancedConnect = 1;
    /**
     * Last will Request Code
     **/
//  static final int mqtt_lastWill = 2;
    public static final String FRAGMENT_PLACES = "Place";
    /**
     * Property name for the history field in {@link ConnectionMqtt} object for use with {@link java.beans.PropertyChangeEvent}
     **/
    public static final String historyProperty = "history";
    public static final String callIncomingProperty = "callIncomingProperty";
    public static final String callIncomingAnsweredProperty = "callIncomingAnsweredProperty";
    public static final String callIncomingEndProperty = "callIncomingEndProperty";
    public static final String callIncomingMissedProperty = "callIncomingMissedProperty";
    public static final String callOutgoingStartProperty = "callOutgoingStartProperty";

    /* Bundle Keys */
    public static final String callOutgoingEndProperty = "callOutgoingEndProperty";
    public static final String messageUpdateProperty = "messageUpdate";
    public static final String messageArrivedProperty = "messageArrived";
    public static final String messageErrorProperty = "messageErrorProperty";
    public static final String mqttDeliveredProperty = "mqttDeliveredProperty";
    public static final String mqttPublishedProperty = "mqttPublishedProperty";
    public static final String mqttPublishingProperty = "mqttPublishingProperty";
    public static final String smsDeliveredProperty = "smsDeliveredProperty";
    public static final String smsPublishedProperty = "smsPublishedProperty";
    public static final String smsPublishingProperty = "smsPublishingProperty";
    public static final String firebaseDeliveredProperty = "firebaseDeliveredProperty";
    public static final String firebasePublishedProperty = "firebasePublishedProperty";
    public static final String firebasePublishingProperty = "firebasePublishingProperty";
    public static final String connectErrorProperty = "connectError";
    public static final String contactNameUpdatedProperty = "contactNameUpdatedProperty";
    public static final String contactNameAddProperty = "contactNameAddProperty";
    public static final String contactBlockedProperty = "contactBlockedProperty";
    public static final String contactDeleteProperty = "contactDeleteProperty";
    public static final String contactUnblockedProperty = "contactUnblockedProperty";
    public static final String usersSearchResultProperty = "usersSearchResultProperty";
    public static final String caltxtFAQProperty = "caltxtFAQProperty";
    public static final String usersMatchResultProperty = "usersMatchResultProperty";
    public static final String firebaseAddressbookSyncCompleteProperty = "firebaseAddressbookSyncCompleteProperty";

    /* Firebase realtime database references */
    public static final String caltxt_users_firebase = "caltxt_users";
    public static final String caltxt_faq_firebase = "caltxt_faq";
    public static final String caltxt_admin_firebase = "caltxt_admin";
    /* Property names */
    public static final String myStatusChangeProperty = "myStatusChangeProperty";
    public static final String logDeletedProperty = "logDeletedProperty";
    public static final String contactPhotoChangeProperty = "contactPhotoChangeProperty";
    /**
     * Property name for the connection status field in {@link ConnectionMqtt} object for use with {@link java.beans.PropertyChangeEvent}
     **/
    public static final String ConnectionStatusProperty = "connectionStatus";
    /**
     * Empty String for comparisons
     **/
    public static final String empty = "";
    /**
     * Location Flag Bundle Key
     **/
    static final String location = "location";
    /**
     * Picture Flag Bundle Key
     **/
    static final String picture = "picture";
    /**
     * Location Flag Bundle Key
     **/
    static final String name = "name";
    /**
     * Application TAG for logs where class name is not used
     */
    static final String TAG = "MQTT Android";
    /**
     * Server Bundle Key
     **/
    static final String server = "server";
    /**
     * Port Bundle Key
     **/
    static final String port = "port";
    /**
     * ClientID Bundle Key
     **/
    static final String clientId = "clientId";
    /**
     * Topic Bundle Key
     **/
    static final String topic = "topic";
    /**
     * History Bundle Key
     **/
    static final String history = "history";
    /**
     * Message Bundle Key
     **/
    static final String message = "message";
    /**
     * Retained Flag Bundle Key
     **/
    static final String retained = "retained";
    /**
     * QOS Value Bundle Key
     **/
    static final String qos = "qos";
    /**
     * User name Bundle Key
     **/
    static final String username = "username";
    /**
     * Password Bundle Key
     **/
    static final String password = "password";
    /**
     * Keep Alive value Bundle Key
     **/
    static final String keepalive = "keepalive";
    /**
     * Timeout Bundle Key
     **/
    static final String timeout = "timeout";
    /**
     * SSL Enabled Flag Bundle Key
     **/
    static final String ssl = "ssl";
    /**
     * Connections Bundle Key
     **/
    static final String connections = "connections";
    /**
     * Clean Session Flag Bundle Key
     **/
    static final String cleanSession = "cleanSession";
    /**
     * Action Bundle Key
     **/
    static final String action = "action";
    /**
     * Space String Literal
     **/
    static final String space = " ";

    /* Useful constants*/
    public static int PHOTO_SIZE_STANDARD = 96;
    //	public static int INPUT_VIEW = 1;
    public static int TOAST_ACTION_SEND_VIEW = 1;// send caltxt & present a view
    public static int TOAST_ACTION_VIEW = 2;// present a view
    //	public static int LIST_IMG_HEIGHT = 192;//list image size requested from server;list view image size fixed 64
//	public static int PROFILE_IMG_HEIGHT = 192;//size required for profile image;profile view image size fixed 192
    public static int CALTXT_RECONNECT_WAIT = 5000;
    public static int CALTXT_INPUT_TIMEOUT = 60000;
    public static int CALTXT_MSG_LAG = 60000;//delay allowed for caltxt to arrive after call connects
    public static int CALTXT_RPY_LAG = 90000;//delay allowed for caltxt REPLY to arrive after call disconnects
    //	public static int CALTXT_TOAST_TIMEOUT = 30000;
    public static short CONTACT_DISCOVERY_DEFAULT = 0;//0:public, 1:private
    public static short CALTXT_WIFI_SCAN_INTERVAL = 15 * 60;//in seconds
    public static short CALTXT_WIFI_SCAN_INTERVAL_MAX = 20 * 60;//in seconds

    public static short JOB_ID_AUTO_RESPONSE = 1;//service job id for auto response alarm
    public static short JOB_ID_WIFI_NETWORK_SCAN = 2;//service job id for wifi scanning
    public static short JOB_ID_REBOOT_SERVICE_INIT = 3;//service job id for auto response alarm
    public static short JOB_ID_MQTT_KEEPALIVE = 4;//service job id for pinging mqtt server (keep alive)
    //this is temporary number (self) used to initialize mMyMob
//	public static String MY_PROFILE_NUMBER = "1111111111";
	/*
	public static int icon_actionbar_available = R.drawable.ic_available_white_24dp;//_actionbar;
	public static int icon_actionbar_busy = R.drawable.ic_busy_white_24dp;//_actionbar;
	public static int icon_actionbar_dnd = R.drawable.ic_donotdisturb_white_24dp;//_actionbar;
	public static int icon_actionbar_offline = R.drawable.ic_busy_white_24dp;//_actionbar;
	public static int icon_actionbar_away = R.drawable.ic_away_white_24dp;//_actionbar;
	public static int icon_actionbar_auto = R.drawable.ic_auto_answer_mode_outline_white_24dp;//_actionbar;

	public static int icon_notify_available = R.drawable.available;//_notify;
	public static int icon_notify_busy = R.drawable.busy;//_notify;
	public static int icon_notify_dnd = R.drawable.dnd;//_notify;
	public static int icon_notify_offline = R.drawable.offline;//_notify;
	public static int icon_notify_away = R.drawable.away;//_notify;
	public static int icon_notify_missed = R.drawable.missed;//missed;

	public static int icon_listview_available = icon_actionbar_available;
	public static int icon_listview_busy = icon_actionbar_busy;
	public static int icon_listview_dnd = icon_actionbar_dnd;
	public static int icon_listview_offline = icon_actionbar_offline;
	public static int icon_listview_away = icon_actionbar_away;
*//*
	public static int icon_status_available = R.drawable.caltxt_status_available;//_trans;
	public static int icon_status_busy = R.drawable.caltxt_status_busy;//_trans;
	public static int icon_status_dnd = R.drawable.caltxt_status_dnd;//_trans;
	public static int icon_status_offline = R.drawable.caltxt_status_offline;//_trans;
	public static int icon_status_away = R.drawable.caltxt_status_away;//_trans;

	public static int icon_notify_available = icon_status_available;//_trans;
	public static int icon_notify_busy = icon_status_busy;//_trans;
	public static int icon_notify_dnd = icon_status_dnd;//_trans;
	public static int icon_notify_offline = icon_status_offline;//_trans;
	public static int icon_notify_away = icon_status_away;
*/
//	public static int icon_msg_sent = R.drawable.ic_message_sent;//_trans;
//	public static int icon_msg_sent_not = R.drawable.ic_message_sent_not;
//	public static int icon_msg_received = R.drawable.ic_message_received;//_trans;
//	public static int icon_msg_received_not = R.drawable.ic_message_received_not;
//	public static int icon_social_person = R.drawable.ic_person_social;
}
