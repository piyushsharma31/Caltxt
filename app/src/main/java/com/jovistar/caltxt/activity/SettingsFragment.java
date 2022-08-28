package com.jovistar.caltxt.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.mqtt.client.ConnectionMqtt;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

//		bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_contact_discovery)));
//		bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_caltxt_align)));
//		bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_caltxt_timeout)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_caltxt_delivery_sms)));
//		bindPreferenceSummaryToValue(findPreference(getString(R.string.preference_key_places_remember)));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                //25112019, DUE TO GOOGLE POLICY CHANGE FOR SMS this preference is disabled
                preference.setEnabled(false);
            } else if (preference instanceof CheckBoxPreference) {
                CheckBoxPreference cbPreference = (CheckBoxPreference) preference;
                if (!cbPreference.isChecked()) {
                    ConnectionMqtt.getConnection(preference.getContext());//.checkNetworkAndAttemptReconnect();
                } else {
                    ConnectionMqtt.getConnection(preference.getContext()).disconnect();
                }
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if (false == preference instanceof CheckBoxPreference)
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                    preference,
                    PreferenceManager.getDefaultSharedPreferences(
                            preference.getContext()).getString(preference.getKey(),
                            ""));
    }

}
