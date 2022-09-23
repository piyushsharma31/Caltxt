package com.jovistar.caltxt.firebase.client;

/**
 * Created by jovika on 1/5/2017.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jovistar.caltxt.R;
import com.jovistar.caltxt.activity.SignupProfile;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.app.Config;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.commons.bo.XMob;
import com.jovistar.commons.bo.XUsr;

public class FirebaseIDService /*extends FirebaseInstanceIdService*/ {
    private static final String TAG = "FirebaseIDService";
/*
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Saving reg id to shared preferences
        storeRegIdInPref(refreshedToken);

        // sending gcm token to server
        sendGCMTokensToServer(getApplicationContext());

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
*/
    public static void storeRegIdInPref(Context context, String token) {
        SharedPreferences pref = context.getSharedPreferences(Config.SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("regId", token);
        editor.apply();
    }

    private static String getRegistrationID(Context context) {
        SharedPreferences pref = context.getSharedPreferences(Config.SHARED_PREF, 0);
        return pref.getString("regId", null);
    }

    public static void sendGCMTokensToServer(Context context) {
        String refreshedToken = getRegistrationID(context);

        if (Addressbook.getInstance(context).getMyProfile() != null && refreshedToken != null) {
            sendGCMTokenToServer(refreshedToken,
                    Addressbook.getInstance(context).getMyProfile().getUsername());
            // also map SIM2 number to same token; for cases when client want to send text to SIM2
            if (SignupProfile.isSIM2Verified(context)) {
                sendGCMTokenToServer(refreshedToken,
                        XMob.toFQMN(Addressbook.getInstance(context).getMyProfile().getNumber2(),
                                Addressbook.getInstance(context).getMyCountryCode()));
            }
        }
    }

    private static void sendGCMTokenToServer(String token, final String mobId) {

        if (mobId == null || mobId.length() <= 0) {
            return;
        }
        // store the key(mobile number), value(token) pair in real time DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.caltxt_users_firebase);
        ref.child(mobId).setValue(token);

        // User data change listener
        ref.child(mobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String token = dataSnapshot.getValue(String.class);

                // Check for null
                if (token == null) {
                    Log.e(TAG, "User data is null! for " + mobId);
                    return;
                }

                Log.d(TAG, "User data is changed! setPreference" + token);
                // set preference to indicate registration completion
                XUsr user = new XUsr();
                user.id = Addressbook.getInstance(Caltxt.getCustomAppContext()).getMyProfile().getUsername();
                user.pwd = Addressbook.getInstance(Caltxt.getCustomAppContext()).getIMEI();
                user.cmnt = Addressbook.getInstance(Caltxt.getCustomAppContext()).getIMEI();

                String u1 = SignupProfile.getPreference(Caltxt.getCustomAppContext(), Caltxt.getCustomAppContext().getString(R.string.profile_key_mobile));
                String u2 = SignupProfile.getPreference(Caltxt.getCustomAppContext(), Caltxt.getCustomAppContext().getString(R.string.profile_key_mobile2));

                Log.v(TAG, "sendGCMTokenToServer, user " + user.id);
                Log.v(TAG, "sendGCMTokenToServer, u1 " + u1 + ", " + u2);

                if (user.id.endsWith(u1)) {
                    SignupProfile.setPreference(Caltxt.getCustomAppContext(), Caltxt.getCustomAppContext().getString(R.string.profile_key_number_verification_status), SignupProfile.REGISTERED_PHONE_NUMBER_VERIFIED);
                } else if (user.id.endsWith(u2)) {
                    SignupProfile.setPreference(Caltxt.getCustomAppContext(), Caltxt.getCustomAppContext().getString(R.string.profile_key_number2_verification_status), SignupProfile.REGISTERED_PHONE_NUMBER_VERIFIED);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read user " + error.toException());
            }
        });

    }
}
