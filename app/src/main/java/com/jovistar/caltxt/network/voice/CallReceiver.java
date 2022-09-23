package com.jovistar.caltxt.network.voice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Date;

public abstract class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";

    // The receiver will be recreated whenever android feels like it. We need a
    // static variable to remember data between instantiations
    // initialize statics in Caltxt.java so that it remain until lifetime of app
//	static boolean INITIALIZED = false;//whether OnReceive called atleast once

    @Override
    public void onReceive(Context context, Intent intent) {
    }

    // Derived classes should override these to respond to specific events of
    // interest
    protected void onIncomingCallRinging(Context ctx, String number, Date start) {
    }

    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    protected void onMissedCall(Context ctx, String number, Date start) {
    }
}
