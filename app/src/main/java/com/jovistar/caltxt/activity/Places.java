package com.jovistar.caltxt.activity;

import android.os.Bundle;
import android.util.Log;

import com.jovistar.caltxt.bo.XPlc;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.commons.bo.IDTObject;

import java.util.ArrayList;

public class Places extends DTOListUI {
    private static final String TAG = "Places";

    Places activity;
    ArrayList<IDTObject> places = new ArrayList<IDTObject>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;
        // attempt to restore from persistence
//		getSupportActionBar().setHomeButtonEnabled(true);
//		getSupportActionBar().setLogo(R.drawable.ic_action_help);
        getSupportActionBar().setTitle("Tagged places");

        ArrayList<XPlc> array = Persistence.getInstance(getApplicationContext()).getAllXPLC();
//		Object[] col = CallManager.getInstance().cellids.values().toArray();
        for (int i = 0; i < array.size(); i++) {
            places.add(i, array.get(i));
        }
        Log.d(TAG, "onCreate, places size " + places.size());
        log_adapter = new DTOListAdapter(this, places);
        listview.setAdapter(log_adapter);
        log_adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
//		DTOListUI.isInForeground=true;
    }

    @Override
    protected void onDestroy() {
//		DTOListUI.isInForeground=false;
        super.onDestroy();
    }

}
