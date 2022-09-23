package com.jovistar.caltxt.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Logbook;
import com.jovistar.caltxt.service.RebootService;

import java.util.HashMap;
import java.util.Map;

public class SplashScreen extends AppCompatActivity {

    private static final String TAG = "SplashScreen";
    //	Context mContext = this;
    InitApp mInitAppTask = null;
    SplashScreen activity = this;
//	boolean receiver_registered = false;
//	public static int screenWidth = 0, screenHeight = 0;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(RebootService.RESULT);
                if (resultCode == RESULT_OK) {
                    //start main activity
                    Intent it = new Intent(getApplicationContext(), CaltxtPager.class);
//					it.putExtra("list", Logbook.get().getList());
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(it);
//					Notify.notify_caltxt_cancel_all();
                    finish();
                } else {
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        ProgressBar pb = findViewById(R.id.progress_bar);
        pb.setVisibility(View.VISIBLE);

        ((TextView)findViewById(R.id.textsplash2)).setText(About.getVersionInfo());
//		setScreenResolution();

        registerReceiver(receiver, new IntentFilter(RebootService.REBOOT_SERVICE_COMPLETE_NOTIFICATION));
//		receiver_registered = true;

        if (SignupProfile.isNewUser(SplashScreen.this)) {//App installed for first time, initiate Sign up
            //start sign up activity
            Intent it = new Intent(getBaseContext(), TOS.class);
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            /*28-DEC-16 commented: flag FLAG_ACTIVITY_NO_HISTORY destroys the activity when another activity comes
			 * in front. With this flag onRequestPermissionsResult was not getting called in
			 * TOS since the activity was getting destroyed after requestPermission call*/
//			it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(it);
            finish();
        } else if (SignupProfile.isSIMCardChanged(SplashScreen.this)) {
            Log.i(TAG, "SIM CHANGED ");
            //SIM changed, open sign up Activity
            Intent i = new Intent(getBaseContext(), SignupProfile.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } else {
            // commented 14112019, do not need addressbook write permission. all edits/add/delete contacts is not allowed Caltxt
            if (!((Caltxt) getApplication()).isPermissionGranted(SplashScreen.this, "android.permission.READ_CONTACTS")) {
                if (PackageManager.PERMISSION_GRANTED !=
                        Caltxt.checkPermission(SplashScreen.this, "android.permission.READ_CONTACTS",
                                Caltxt.CALTXT_PERMISSIONS_REQUEST_READ_CONTACTS,
                                "Caltxt need permission to read your contacts to build contact list. It will never upload your contacts" +
                                        " to remote server")) {
                    return;
                }
            }

            if(!((Caltxt) getApplication()).isPermissionGranted(SplashScreen.this, "android.permission.ACCESS_COARSE_LOCATION")) {
                if (PackageManager.PERMISSION_GRANTED
                        != Caltxt.checkPermission(SplashScreen.this, "android.permission.ACCESS_COARSE_LOCATION",
                        Caltxt.CALTXT_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION,
                        "Caltxt needs permission to use nearby WiFi hotspots to tag your status")) {

                    Log.i(TAG, "onCreate : Declined CALTXT_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION");
                    return;
                }
            }

            Log.i(TAG, "onCreate : InitApp");
            mInitAppTask = new InitApp();
            mInitAppTask.execute((Void) null);
            Addressbook.getInstance(getApplicationContext()).getMyCountryCode();// initialized country code
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
//		if(receiver_registered) {
        unregisterReceiver(receiver);
//			receiver_registered = false;
//		}
        super.onDestroy();
    }

    class InitApp extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
//			RebootServiceJob.schedule();
            Log.i(TAG, "InitApp : doInBackground");
			/* start service directly here, otherwise scheduler takes time to initiate*/
            // commented 24-JUL-17, start service in Caltxt.java, dont have to start every time UI is opened
            startService(new Intent(SplashScreen.this, RebootService.class).putExtra("caller", "RebootReceiver"));
            if (Logbook.get(getBaseContext()).getList().size() != Persistence.getInstance(getBaseContext()).getCountXCTX()) {
                Logbook.get(getBaseContext()).load();//loads log book
            }

//			Addressbook.getInstance(getBaseContext()).load();//loads address book

//			Intent broadcastIntent = new Intent("com.jovistar.caltxt.message.RestartService");
//			sendBroadcast(broadcastIntent);

            //and schedule restart in next 5 minutes (this will trigger restart every 5 minutes)
            //commented 28-DEC-16. No need since background service running issue resolved (it seems!)
	        /*if (Build.VERSION.SDK_INT >= 21) {
				RebootServiceJob.schedule(0);
			} else {
				RebootServiceAlarmReceiver.schedule(0);
			}*/
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
//		        try {
                //wait for service to initialize
//		        	synchronized (semaphore) {
//		        		semaphore.wait();
//		        	}
                //start main activity
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}

            } else {
            }
            mInitAppTask = null;
            Log.i(TAG, "InitApp : onPostExecute "+success);
        }

        @Override
        protected void onCancelled() {
            mInitAppTask = null;
        }
    }

    /*30APR17, moved to SignupProfile
        private void setScreenResolution() {
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
        }
    */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        Log.v(TAG, TAG + "::onRequestPermissionsResult");
        Map<String, Integer> perms = new HashMap<String, Integer>();
        switch (requestCode) {
            case Caltxt.CALTXT_PERMISSIONS_REQUEST_READ_CONTACTS:
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
	            		|| perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    mInitAppTask = new InitApp();
                    mInitAppTask.execute((Void) null);

                } else {
                    // Permission Denied
                    finish();
                }
                break;
        }
    }
}
