package com.jovistar.caltxt.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.activity.CaltxtPager;
import com.jovistar.caltxt.activity.IFTTT;
import com.jovistar.caltxt.activity.Settings;
import com.jovistar.caltxt.activity.SignupProfile;
import com.jovistar.caltxt.bo.XQrp;
import com.jovistar.caltxt.firebase.client.DatabaseFirebase;
import com.jovistar.caltxt.firebase.client.FirebaseIDService;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;

import java.util.Calendar;

//import com.jovistar.commons.facade.ModelFacade;

public class RebootService extends IntentService {

    private static final String TAG = "RebootService";

    Intent receiver_intent;

    public static final String REBOOT_SERVICE_COMPLETE_NOTIFICATION = "com.jovistar.caltxt.service.RebootService.receiver";
    public static final String RESULT = "result";

    public static boolean is_status_dirty = false;
    private static boolean is_running = false;

    Context mContext;

    public RebootService() {
        super("Caltxt Reboot Service");
    }

    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent ENTRY");

        receiver_intent = intent;
        mContext = getApplicationContext();
        String intentType = intent.getExtras().getString("caller");

        if (intentType == null || is_running) {
            return;
        }

        is_running = true;

        if (intentType.equals("RebootReceiver")) {

            if (SignupProfile.isNewUser(mContext)) {
                is_running = false;
                Log.e(TAG, "isNewUser!");
                return;
            }

            mandatoryInitializationRebootRestartKillStart(mContext);

            if (SignupProfile.isNewUserAndNumberVerified(mContext)) {
                Log.v(TAG, "onHandleIntent isNewUserAndNumberVerified");

                // register token
                FirebaseIDService.sendGCMTokensToServer(getApplicationContext());

                notifyComplete();//notify complete immediately

            } else if (SignupProfile.isNumberVerifiedUserAdded(mContext)) {//user registered successfully
                Log.v(TAG, "onHandleIntent isNumberVerifiedUserAdded");

                CaltxtPager.updateNotifications(mContext);//show notification if DND or Auto response is set

                notifyComplete();//notify complete immediately
            }
        }

        Log.v(TAG, "onHandleIntent EXIT");
        is_running = false;

    }

    public synchronized void mandatoryInitializationRebootRestartKillStart(Context context) {

        if (Addressbook.getInstance(mContext).getSIMSerial() != null && Addressbook.getInstance(mContext).getOldSIMSerial().length() > 0 && !Addressbook.getInstance(mContext).getOldSIMSerial().equals(Addressbook.getInstance(mContext).getSIMSerial())) {
            //SIM changed. Ask user to re-register!
            SignupProfile.setPreference(mContext, mContext.getString(R.string.profile_key_number_verification_status), SignupProfile.REGISTERED_PHONE_NUMBER_SIM_CHANGED);
            Log.i(TAG, "SIM CHANGED " + Addressbook.getInstance(mContext).getSIMSerial() + ":" + Addressbook.getInstance(mContext).getSIMSerial());
        }

        IFTTT.listenForTimedActions(mContext.getApplicationContext());

        WifiScanAlarmReceiver.schedule(mContext.getApplicationContext());

        //init db with default quick response values - initialize once FIRST TIME
        if (Persistence.getInstance(mContext).getCountXQRP() == 0) {
            String[] values = getResources().getStringArray(R.array.call_reject_acknowledgement);
            //insert new values in database
            for (int i = 0; i < values.length; i++) {
                Persistence.getInstance(mContext).insertXQRP(values[i]);
            }
        }

        XQrp autoResponse = Persistence.getInstance(mContext).getQuickAutoResponse();
        if (autoResponse != null && autoResponse.getAutoResponseEndTime() <= Calendar.getInstance().getTimeInMillis()) {
            //if auto response expired, reset all auto responses
            Persistence.getInstance(mContext).resetAutoResponse();
            //reset profile status for auto response
            Addressbook.getInstance(mContext).getMyProfile().resetStatusAutoResponding();
            Log.v(TAG, "mandatoryInitializationRebootRestartKillStart , resetAutoResponse");
        }

        if (Persistence.getInstance(context).getCountXMOB() == 0
                || Addressbook.getInstance(context).getList().size() != Persistence.getInstance(context).getCountXMOB()) {
            Addressbook.getInstance(mContext).load();//loads address book
            DatabaseFirebase.checkAndSyncAddressbook();
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);

        //initialize discover_by_anyone flag
        String discovery_type = settings.getString( mContext.getString(R.string.preference_key_contact_discovery), mContext.getString(R.string.preference_value_contact_discovery_anyone));
        Settings.iam_discoverable_by_anyone = discovery_type.equals(mContext.getString(R.string.preference_value_contact_discovery_anyone));
        Log.v(TAG, "mandatoryInitializationRebootRestartKillStart, iam_discoverable_by_anyone:" + Settings.iam_discoverable_by_anyone);

        Settings.detect_places = settings.getString(mContext.getString(R.string.preference_key_places_remember), mContext.getResources().getString(R.string.preference_value_places_remember_default));
        Log.v(TAG, "mandatoryInitializationRebootRestartKillStart, remember_places:" + Settings.detect_places);
    }

    private void notifyComplete() {

        Intent intent = new Intent(REBOOT_SERVICE_COMPLETE_NOTIFICATION);
        intent.putExtra("result", Activity.RESULT_OK);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);

    }
}
