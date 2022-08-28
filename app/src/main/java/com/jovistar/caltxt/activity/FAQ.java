package com.jovistar.caltxt.activity;

import android.os.Bundle;
import android.view.View;

import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.firebase.client.DatabaseFirebase;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XAd;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class FAQ extends DTOListUI implements PropertyChangeListener {
    private static final String TAG = "FAQ";

    FAQ activity;
    ArrayList<IDTObject> faqs = new ArrayList<IDTObject>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        // attempt to restore from persistence
//		getSupportActionBar().setHomeButtonEnabled(true);
//		getSupportActionBar().setLogo(R.drawable.ic_action_help);
        getSupportActionBar().setTitle("FAQ");

        Connection.get().registerChangeListener(this);

        log_adapter = new DTOListAdapter(this, faqs);
        listview.setAdapter(log_adapter);
        log_adapter.notifyDataSetChanged();

        //		new GetFAQ().execute();
        DatabaseFirebase.getCaltxtFAQ();

        //show progress
//		View progressStatusView = findViewById(R.id.progress_status);
//		TextView textView = (TextView) findViewById(R.id.progress_status_textview);
//		SignupProfile.showProgress(true, activity.getDTOView(), progressStatusView, textView, "");
        mProgress.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
//		DTOListUI.isInForeground=true;
    }

    @Override
    protected void onDestroy() {
//		DTOListUI.isInForeground=false;
        Connection.get().removeChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        final String propertyName = event.getPropertyName();
        final String oldValue = event.getOldValue() == null ? "" : (String) event.getOldValue();
        final String newValue = event.getNewValue() == null ? "" : (String) event.getNewValue();
//					Log.d(TAG, "propertyChange "+propertyName+", "+newValue);

        if (propertyName.equals(Constants.caltxtFAQProperty)) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgress.setVisibility(View.GONE);
                    XAd ad = new XAd();
                    ad.setHeader(oldValue);
                    ad.setSubject(newValue);
                    faqs.add(ad);
                    log_adapter.notifyDataSetChanged();
                }
            });
        }
    }
}
