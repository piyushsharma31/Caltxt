package com.jovistar.caltxt.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jovistar.caltxt.activity.SignupProfile;

public class RebootReceiver extends BroadcastReceiver {
    final String TAG = "RebootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");

        //process only if user is registered for service
        if (!SignupProfile.isSIM1Verified(context.getApplicationContext())
                && !SignupProfile.isSIM2Verified(context.getApplicationContext())) {
            Log.e(TAG, "onReceive, SIM NOT VERIFIED");
            return;
        }

//		context.startService(new Intent(context.getApplicationContext(), MessageService.class));
//    	if ("android.intent.action.REBOOT".equals(intent.getAction())
//				|| "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())
//				|| "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())
//    			|| "com.jovistar.caltxt.message.RestartService".equals(intent.getAction())) {
        // start the service
        context.getApplicationContext().startService(new Intent(context.getApplicationContext(), RebootService.class).putExtra("caller", "RebootReceiver"));
//    			startWakefulService(context.getApplicationContext(),
//						new Intent(context.getApplicationContext(), RebootService.class).putExtra("caller", "RebootReceiver"));
//		}
    }
}
