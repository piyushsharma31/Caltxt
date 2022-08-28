package com.jovistar.caltxt.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jovistar.caltxt.R;

public class Settings extends AppCompatActivity {
    public static String PLACES_AUTO_DETECT = "1";
    public static String PLACES_DETECT_NEVER = "0";
    public static String detect_places = PLACES_AUTO_DETECT;
    private static int delivery_by_sms = -1;//not set
    public static boolean iam_discoverable_by_anyone = true;
    private static final String TAG = "Settings";

    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    public static boolean isSMSEnabled(Context context) {
        if (delivery_by_sms == -1) {

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            String delivery_type = settings.getString(
                    context.getString(R.string.preference_key_caltxt_delivery_sms),
                    context.getString(R.string.preference_value_caltxt_delivery_data)/*default*/);

            Settings.delivery_by_sms = delivery_type.equals(
                    context.getString(R.string.preference_value_caltxt_delivery_data)) ?
                    0 : 1;
        }

        return delivery_by_sms == 1;
    }

    public static void setSMSEnabled(boolean enable, Context context) {
        if (enable) {
            SignupProfile.setPreference(context,
                    context.getString(R.string.preference_key_caltxt_delivery_sms),
                    context.getString(R.string.preference_value_caltxt_delivery_sms));
        } else {
            SignupProfile.setPreference(context,
                    context.getString(R.string.preference_key_caltxt_delivery_sms),
                    context.getString(R.string.preference_value_caltxt_delivery_data));
        }
        delivery_by_sms = enable ? 1 : 0;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");

        Log.d(TAG, "onCreate");
    }

    /*
        @Override
        protected void onPostCreate(Bundle savedInstanceState) {
            super.onPostCreate(savedInstanceState);

            setupSimplePreferencesScreen();
        }
    */
    @Override
    public void onPause() {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String delivery_type = settings.getString(
                getApplicationContext().getString(R.string.preference_key_caltxt_delivery_sms),
                getApplicationContext().getString(R.string.preference_value_caltxt_delivery_data));
        delivery_by_sms = delivery_type.equals(
                getApplicationContext().getString(R.string.preference_value_caltxt_delivery_data)) ?
                0 : 1;

        String discovery_type = settings.getString(
                getApplicationContext().getString(R.string.preference_key_contact_discovery),
                getApplicationContext().getString(R.string.preference_value_contact_discovery_anyone));
        iam_discoverable_by_anyone = discovery_type.
                equals(getApplicationContext().getString(R.string.preference_value_contact_discovery_anyone));

        Log.d(TAG, "onPause, iam_discoverable_by_anyone: " + iam_discoverable_by_anyone);
        super.onPause();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     * private void setupSimplePreferencesScreen() {
     * //		if (!isSimplePreferences(this)) {
     * //			return;
     * //		}
     * <p>
     * addPreferencesFromResource(R.xml.pref_data_sync);
     * <p>
     * //		bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_contact_discovery)));
     * bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_caltxt_align)));
     * bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_caltxt_timeout)));
     * bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_caltxt_delivery_sms)));
     * }
     */

    @Override
    public void onResume() {
        super.onResume();
        /*Preference auto_response_preference = findPreference(getString(R.string.preference_key_autoresponse));
		XQrp auto_response = Persistence.getInstance(this).getQuickAutoResponse();

		if(auto_response==null) {
			String actionTaken = Globals.getCustomAppContext().getString(R.string.preference_summary_autoresponse_disabled);
//			String [] additionalArgs = {getResources().getString(R.string.preference_value_autoresponse_default_none)};
//			String actionTaken = Globals.getCustomAppContext().getString(R.string.preference_summary_autoresponse,
//					(Object[]) additionalArgs);
			if(auto_response_preference!=null)
				auto_response_preference.setSummary(actionTaken);
		} else {
			//Automatic response duration expired
			if(auto_response.getAutoResponseEndTime()<=Calendar.getInstance().getTimeInMillis()) {
		    	//reset all auto responses
		    	Persistence.getInstance(this).resetAutoResponse();

				String actionTaken = Globals.getCustomAppContext().getString(R.string.preference_summary_autoresponse_disabled);
//				String [] additionalArgs = {getResources().getString(R.string.preference_value_autoresponse_default_none)};
//				String actionTaken = Globals.getCustomAppContext().getString(R.string.preference_summary_autoresponse,
//						(Object[]) additionalArgs);
				auto_response_preference.setSummary(actionTaken);
			} else {
				String [] additionalArgs = {auto_response.getQuickResponseValue()};
				String actionTaken = Globals.getCustomAppContext().getString(R.string.preference_summary_autoresponse_enabled,
				(Object[]) additionalArgs);
				auto_response_preference.setSummary(Html.fromHtml("<font color=\"blue\">"+actionTaken+"</font>"));
			}
		}*/
    }

    /** {@inheritDoc}
     @Override public boolean onIsMultiPane() {
     return isXLargeTablet(this) && !isSimplePreferences(this);
     }
     */
    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     private static boolean isXLargeTablet(Context context) {
     return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
     }
     */

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     private static boolean isSimplePreferences(Context context) {
     return ALWAYS_SIMPLE_PREFS
     || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
     || !isXLargeTablet(context);
     }
     */

    /** {@inheritDoc}
     @Override
     @TargetApi(Build.VERSION_CODES.HONEYCOMB) public void onBuildHeaders(List<Header> target) {
     if (!isSimplePreferences(this)) {
     loadHeadersFromResource(R.xml.pref_headers, target);
     }
     }
     */
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
    @Override public boolean onPreferenceChange(Preference preference, Object value) {
    String stringValue = value.toString();

    if (preference instanceof ListPreference) {
    // For list preferences, look up the correct display value in
    // the preference's 'entries' list.
    ListPreference listPreference = (ListPreference) preference;
    int index = listPreference.findIndexOfValue(stringValue);

    // Set the summary to reflect the new value.
    preference.setSummary(index >= 0 ? listPreference.getEntries()[index]	: null);
    } else if (preference instanceof CheckBoxPreference) {
    CheckBoxPreference cbPreference = (CheckBoxPreference) preference;
    if(!cbPreference.isChecked()) {
    RebootService.getConnection();//.checkNetworkAndAttemptReconnect();
    } else {
    RebootService.getConnection().disconnect();
    }
    } else {
    // For all other preferences, set the summary to the value's
    // simple string representation.
    preference.setSummary(stringValue);
    }
    return true;
    }
    };
     */

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
    private static void bindPreferenceSummaryToValue(Preference preference) {
    // Set the listener to watch for value changes.
    preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

    // Trigger the listener immediately with the preference's
    // current value.
    if(false==preference instanceof CheckBoxPreference)
    sBindPreferenceSummaryToValueListener.onPreferenceChange(
    preference,
    PreferenceManager.getDefaultSharedPreferences(
    preference.getContext()).getString(preference.getKey(),
    ""));
    }
     */

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     @TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class GeneralPreferenceFragment extends PreferenceFragment {
     @Override public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     addPreferencesFromResource(R.xml.pref_general);

     // Bind the summaries of EditText/List/Dialog/Ringtone preferences
     // to their values. When their values change, their summaries are
     // updated to reflect the new value, per the Android Design
     // guidelines.
     bindPreferenceSummaryToValue(findPreference(getString(R.string.profile_key_fullname)));
     bindPreferenceSummaryToValue(findPreference(getString(R.string.profile_key_status_headline)));
     bindPreferenceSummaryToValue(findPreference(getString(R.string.profile_key_mobile)));
     }
     }
     */

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.

     @TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class NotificationPreferenceFragment extends
     PreferenceFragment {
     @Override public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     addPreferencesFromResource(R.xml.preference_notification);

     // Bind the summaries of EditText/List/Dialog/Ringtone preferences
     // to their values. When their values change, their summaries are
     // updated to reflect the new value, per the Android Design
     // guidelines.
     bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
     }
     }
     */
    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     @TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class DataSyncPreferenceFragment extends PreferenceFragment {
     @Override public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     addPreferencesFromResource(R.xml.pref_data_sync);

     //			bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_contact_discovery)));
     bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_caltxt_align)));
     bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_caltxt_timeout)));
     bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_caltxt_delivery_sms)));
     }
     }
     */
}
