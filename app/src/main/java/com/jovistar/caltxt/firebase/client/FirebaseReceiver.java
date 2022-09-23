package com.jovistar.caltxt.firebase.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jovistar.caltxt.activity.CaltxtInputActivity;
import com.jovistar.caltxt.app.Config;
import com.jovistar.commons.bo.XMob;

/**
 * Created by jovika on 1/26/2017.
 */

public class FirebaseReceiver extends BroadcastReceiver {
    final static String TAG = "FirebaseReceiver";

    public static String CALL_MOBILE = "com.jovistar.caltxt.broadcast.call_mobile";

    @Override
    public void onReceive(Context context, Intent intent) {
        // checking for type intent filter
        if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
            // gcm successfully registered

            // now subscribe to `global` topic to receive app wide notifications
//            FirebaseMessaging.getInstance().subscribeToTopic(
//                    Constants.CALTXT_MESSAGE_TOPIC + Addressbook.getInstance(context).getMyProfile().getUsername());

            // show the token
            String token = intent.getStringExtra("token");
            Log.v(TAG, "Firebase Reg Id: " + token);

        } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
            // new push notification is received

            String message = intent.getStringExtra("message");

            Log.v(TAG, "Push notification: " + message);
        } else if (intent.getAction().equals(CALL_MOBILE)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            XMob mob = (XMob) intent.getSerializableExtra("IDTOBJECT");
            Log.v(TAG, "onReceive mob " + mob);
            //show caltxt input activity
            Intent caltxtInput = new Intent(context.getApplicationContext(), CaltxtInputActivity.class);
            caltxtInput.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            caltxtInput.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);//commented 10-DEC-16. why need?
            caltxtInput.putExtra("IDTOBJECT", mob);
//            caltxtInput.putExtra("VIEW", Constants.INPUT_VIEW);
            context.getApplicationContext().startActivity(caltxtInput);
        }
    }
}
