package com.jovistar.caltxt.app;

/**
 * Created by jovika on 1/5/2017.
 */

public class Config {
    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "com.jovistar.caltxt.firebase.registrationComplete";
    public static final String PUSH_NOTIFICATION = "com.jovistar.caltxt.firebase.pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";
}
