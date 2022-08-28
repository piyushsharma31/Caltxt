package com.jovistar.caltxt.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.app.Constants;

public class CaltxtStatusPager extends AppCompatActivity {
    private static final String TAG = "CaltxtStatusPager";

    // private CaltxtStatusFragment mFragment;
    private CaltxtStatusFragmentAdapter mAdapter;
    private ViewPager mViewPager;
    // PagerSlidingTabStrip mTabs = null;
    private TabLayout tabLayout;
    static int lastOpenFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.status_pager);
        // Adding toolbar to the activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Initializing the tablayout
        tabLayout = findViewById(R.id.tabLayout);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        if (getIntent().getExtras() != null) {
            String intentType = getIntent().getExtras().getString("fragment");
            if (intentType != null) {
                if (intentType.equals(Constants.FRAGMENT_STATUS)) {
                    lastOpenFragment = 0;
                } else if (intentType.equals(Constants.FRAGMENT_PLACES)) {
                    lastOpenFragment = 1;
                }
            }
        }


        // Initializing viewPager
        mViewPager = findViewById(R.id.pager);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mAdapter = new CaltxtStatusFragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(lastOpenFragment);

        if (lastOpenFragment == 0)
            getSupportActionBar().setTitle(R.string.title_my_status);
        else
            getSupportActionBar().setTitle(R.string.title_my_location);

        Log.d(TAG, "onTabSelected setCurrentItem " + lastOpenFragment);

        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                lastOpenFragment = tab.getPosition();
                invalidateOptionsMenu();
                if (tab.getPosition() == 0)
                    getSupportActionBar().setTitle(R.string.title_my_status);
                else
                    getSupportActionBar().setTitle(R.string.title_my_location);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_rules_settings:
                Intent it = new Intent(CaltxtStatusPager.this, IFTTT.class);
                startActivity(it);
//				finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * * 13-SEP-16 : Commented functions onCreateOptionsMenu & onPrepareOptionsMenu to not let user change AUTO mode.
     * * By default AUTO mode is ON. Uncomment these functions to enable the contextual menu
     * * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.caltxt_status_menu, menu);

        MenuItem item = menu.findItem(R.id.action_rules_settings);

/*	    MenuItem item = menu.findItem(R.id.switchId);

		final SwitchCompat switchAB = (SwitchCompat) MenuItemCompat.getActionView(item);
		if(Settings.detect_places.equals(Settings.PLACES_AUTO_DETECT)) {
			switchAB.setChecked(true);
			switchAB.setText("Auto ON");
		} else {
			switchAB.setChecked(false);
			switchAB.setText("Auto OFF");
		}
		switchAB.setSwitchPadding(12);
		switchAB.setPadding(0, 0, 12, 0);

	    switchAB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
//					Toast.makeText(getApplication(), "Caltxt will remember and automatically tag place when you revisit", Toast.LENGTH_SHORT).show();
					Notify.toast(switchAB, getApplication(), "Caltxt will remember and automatically tag place when revisited", Toast.LENGTH_LONG);
					SignupProfile.setPreference(CaltxtStatusPager.this,
							getString(R.string.preference_key_places_remember), 
							Settings.PLACES_AUTO_DETECT);
					switchAB.setText("Auto ON");

				} else {
//					Notify.toast(switchAB, getApplication(), "Current place is manually tagged by user", Toast.LENGTH_LONG);
					SignupProfile.setPreference(CaltxtStatusPager.this,
							getString(R.string.preference_key_places_remember), 
							Settings.PLACES_DETECT_NEVER);
					switchAB.setText("Auto OFF");
				}

				Settings.detect_places = SignupProfile.getPreference(CaltxtStatusPager.this,
						CaltxtApp.getCustomAppContext().getString(R.string.preference_key_places_remember));
				CallHandler.RESET_CALL_LISTENER = true;
				CallHandler.get();
			}
		});*/
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        /***
         * REMOVE SWITCH
         * if(mViewPager.getCurrentItem()==0) {//status
         menu.findItem(R.id.switchId).setVisible(false);
         } else if(mViewPager.getCurrentItem()==1) {//places
         menu.findItem(R.id.switchId).setVisible(true);
         }*/

        return super.onPrepareOptionsMenu(menu);
    }
/*
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Caltxt.CALTXT_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
            case Caltxt.CALTXT_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Intent i = new Intent(getApplicationContext(), CaltxtStatusPager.class);
                    startActivity(i);
                } else {
                    // Permission Denied
                    finish();
//                    Toast.makeText(MainActivity.this, "WRITE_CONTACTS Denied", Toast.LENGTH_SHORT)
//                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }*/
}
