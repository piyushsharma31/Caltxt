package com.jovistar.caltxt.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.jovistar.caltxt.R;
import com.jovistar.caltxt.activity.SignupProfile;
import com.jovistar.caltxt.mqtt.client.ConnectionMqtt;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.network.voice.CallManager;
import com.jovistar.caltxt.network.voice.PhoneStateReceiver;
import com.jovistar.caltxt.network.voice.TelephonyInfo;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.service.MotionReceiver;
import com.jovistar.caltxt.service.RebootService;
import com.jovistar.commons.bo.XMob;

/**
 * Created by jovika on 1/5/2017.
 */

public class Caltxt extends Application {
    private static final String TAG = "CaltxtApp";
    public static boolean WAIT_FOR_IDLE_STATE_AND_START = false;
    public static XMob mobile_to_call;

    public static final int CALTXT_PERMISSIONS_REQUEST_WRITE_CONTACTS = 1;
    public static final int CALTXT_PERMISSIONS_REQUEST_READ_PHONE_STATE = 2;
    public static final int CALTXT_PERMISSIONS_REQUEST_SEND_SMS = 3;
    public static final int CALTXT_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 4;
    public static final int CALTXT_PERMISSIONS_REQUEST_ACCESS_INTERNET = 5;
    public static final int CALTXT_PERMISSIONS_REQUEST_CALL = 6;
//    public static final int CALTXT_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 7;
    public static final int CALTXT_PERMISSIONS_REQUEST_READ_CONTACTS = 8;
    public static final int CALTXT_PERMISSIONS_ANSWER_PHONE_CALLS = 9;

    private static Context mContext = null;

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        FirebaseApp.initializeApp(this);

//        Log.setLevel(Log.VERBOSE);
        Log.d(TAG, " ===onCreate=== "/* +mMyMob.getIcon() */);
        TelephonyInfo.printTelephonyManagerMethodNamesForThisDevice(mContext);
        MotionReceiver.getInstance().registerMotionReceiver(getApplicationContext());

        // 28APR17: moved here from CallHandler to initialize at App init, rather than at phone state change
        if (SignupProfile.isNumberVerifiedUserAdded(mContext)) {//user registered successfully
            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            PhoneStateReceiver callBlockListener = PhoneStateReceiver.getInstance(getApplicationContext());
            telephonyManager.listen(callBlockListener, PhoneStateListener.LISTEN_NONE);
            CallManager callManager = CallManager.getInstance();
            CallManager.callControllerInit(getApplicationContext());
            callManager.listen(getApplicationContext());
//            Persistence.getInstance(getApplicationContext()).getAllXPLC();
        }

        // attempt Mqtt connection here since App is restarted.
        // Mqtt is attempted restart in following classes:
        // 1) SplashScreen when user opens app
        // 2) ConnectivityBroadcastReceiver, when network connection is restored. This receiver is started in changeMyStatus
        // 3) ConnectionFirebase, when firebase message is received
        // 4) RebootReceiver, when device is rebooted
        // 5) WifiScanAlarm, every 15 minutes when Wifi scan alarm goes off
        if (ConnectionMqtt.getConnection(getApplicationContext()) != null
                && /*RebootService.getConnection(getApplicationContext())*/Connection.get().isConnected() == false) {
            Log.d(TAG, ConnectionMqtt.getConnection(getApplicationContext()).toString());
        }

        // added 24-JUL-17, startService initialize at start of App
        // rather than everytime UI is opened
        //commented 23SEP2022. this will trigger READ_CONTACTS, therefore moving
        //it to Activity so that permission can be asked if not already
        //startService(new Intent(getApplicationContext(), RebootService.class).putExtra("caller", "RebootReceiver"));

        int temporaryStatusHolder = -1;
        try {
            temporaryStatusHolder = Integer.parseInt(SignupProfile.getPreference(getApplicationContext(),
                    getString(R.string.preference_key_temporary_status)));
        } catch (NumberFormatException e) {

        }
        Log.d(TAG, "temporaryStatusHolder " + temporaryStatusHolder);
        if (temporaryStatusHolder >= 0 && !CallManager.getInstance().isAnyCallInProgress()) {
            Addressbook.getInstance(this).getMyProfile().setStatus(temporaryStatusHolder);
            SignupProfile.setPreference(getApplicationContext(),
                    getApplicationContext().getString(R.string.preference_key_temporary_status),
                    "-1");
        }

        String temporaryHeadlineHolder = SignupProfile.getPreference(getApplicationContext(),
                getApplicationContext().getString(R.string.preference_key_temporary_headline));
        Log.d(TAG, "temporaryHeadlineHolder " + temporaryHeadlineHolder);
        if (temporaryHeadlineHolder.trim().length() > 0 && !CallManager.getInstance().isAnyCallInProgress()) {
            // change back only status
            // place remains same
            Addressbook.getInstance(getApplicationContext()).changeMyStatus(temporaryHeadlineHolder,
                    Addressbook.getInstance(getApplicationContext()).getMyProfile().getPlace());
            SignupProfile.setPreference(getApplicationContext(),
                    getApplicationContext().getString(R.string.preference_key_temporary_headline),
                    "");
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    public static boolean isPermissionGranted(final Activity activity, final String permission) {
        int ret = ContextCompat.checkSelfPermission(activity,
                permission/*Manifest.permission.READ_CONTACTS*/);

        return ret == PackageManager.PERMISSION_GRANTED;
    }

    public static int checkPermission(final Activity activity, final String permission, final int callbackArg, String rationale) {
        int ret = ContextCompat.checkSelfPermission(activity, permission);

        if (ret != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {

                // Show an explanation to the user *asynchronously* -- don't
                // block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                showMessageOKCancel(activity, rationale,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.w(TAG, TAG + "::onClick which " + which);
                                if (which == -2) {
                                    activity.finish();
                                } else {
                                    ActivityCompat.requestPermissions(activity,
                                            new String[]{permission},
                                            callbackArg);
                                }
                            }
                        });
                Log.w(TAG, TAG + "::shouldShowRequestPermissionRationale");
                return ret;

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(activity,
                        new String[]{permission},
                        callbackArg);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        return ret;
    }

    public static void showMessageOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(R.string.ok, okListener)
                .setNegativeButton(R.string.cancel, okListener)
                .create()
                .show();
    }

    public static Context getCustomAppContext() {
        return mContext;
    }

    public boolean hasTelephony() {
        //commented 18SEP2022
        /*TelephonyManager tm = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getDeviceId() != null
                && mContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_TELEPHONY)) {
            return hasSim();
            // THIS PHONE HAS SMS FUNCTIONALITY
        } else {
            // NO SMS HERE :(
            return false;
        }*/
        return true;
    }

    public boolean hasSim() {
        TelephonyManager tm = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);

        return tm.getSimState() == TelephonyManager.SIM_STATE_READY;
    }
}
