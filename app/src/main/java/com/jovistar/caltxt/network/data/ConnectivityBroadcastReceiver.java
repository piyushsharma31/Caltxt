package com.jovistar.caltxt.network.data;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.legacy.content.WakefulBroadcastReceiver;

import com.jovistar.caltxt.activity.SignupProfile;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.firebase.client.ConnectionFirebase;

import java.util.List;

public class ConnectivityBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "ConnectivityBR";

    static ConnectivityBroadcastReceiver br;

    static ConnectivityBroadcastReceiver getInstance() {
        if (br == null) {
            br = new ConnectivityBroadcastReceiver();
        }
        return br;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //process only if user is registered for service
        if (!SignupProfile.isSIM1Verified(context.getApplicationContext())
                && !SignupProfile.isSIM2Verified(context.getApplicationContext())) {
            //no SIM verified yet!
            Log.e(TAG, "no SIM verified yet!");
            return;
        }

        String action = intent.getAction();

		/*
         * Apps targeting Android 7.0 (API level 24) and higher do not receive CONNECTIVITY_ACTION broadcasts
		 * if they declare their broadcast receiver in the manifest. Apps will still receive CONNECTIVITY_ACTION 
		 * broadcasts if they register their BroadcastReceiver with Context.registerReceiver() and that context 
		 * is still valid
		 */
        if (ConnectivityManager.CONNECTIVITY_ACTION.equalsIgnoreCase(action)) {
            if (haveNetworkConnection()) {
//				Log.v(TAG, "Data connection available, action " + action);
                // commented, not required, as MqttService will receive network state and reconnect!
//				startWakefulService(context.getApplicationContext(), new Intent(context.getApplicationContext(), RebootService.class).putExtra("caller", "RebootReceiver"));
//                Connection.get().submitPingSelfFirebase();
            } else {
//				Log.v(TAG, "Data connection NOT available, action " + action);
                //disconnected, monitor network status change
                //enable in ConnectionMqtt.changeStatus
//				ConnectivityBroadcastReceiver.enableDataConnectivityListener(context.getApplicationContext());
                ConnectionFirebase.resetConnected();
            }
        }
    }

    private static ScanResult getWiFiAccessPointStrongest(List<ScanResult> results) {
        ScanResult r = null;
        int size = results.size();
        for (int i = 0; i < size; i++) {
            if (r == null) {
                //first result
                r = results.get(i);
            } else {
                if (r.level < results.get(i).level) {
                    //another result with stronger signal strength
                    r = results.get(i);
                }
            }
        }
        return r;
    }

    /*
        private static synchronized void wifiScanResultCallback(Context context, List<ScanResult> results, String place) {
            int size = results.size();
            Log.d(TAG, "wifi scan result..size, "+size);
            XPlc plc = null;
            ScanResult whereIam=null;

            if(place==null) {
                //discovering tagged place
                for(int i=0; i<size; i++) {
                    plc = Persistence.getInstance(context).getStatusForCellId(
                            results.get(i).BSSID+","+results.get(i).SSID);
                    if(plc!=null) {

                        if(whereIam==null) {
                            //first result
                            whereIam = results.get(i);
                        } else {
                            if(whereIam.level < results.get(i).level) {
                                //another result with stronger signal strength
                                whereIam = results.get(i);
                            }
                        }
                    }
                }

                boolean notify = false;
                String newLocationString = "";
                //this is the place where you are
                if(whereIam!=null) {
                    //moved to known place
                    plc = Persistence.getInstance(context).getStatusForCellId(
                        whereIam.BSSID+","+whereIam.SSID);
                    newLocationString = plc.getStatus();
    //				Addressbook.getMyProfile().setPlace(newLocationString);
                    notify = true;
                } else {
                    //moved to unknown place, notify
                    String currentLocationString = Addressbook.getInstance(context).getMyProfile().getPlace();
                    if(currentLocationString.length()>0)
                        notify = true;
                }

                Addressbook.getInstance(context).changeMyStatus(Addressbook.getInstance(context).getMyProfile().getHeadline(),
                        newLocationString);
    //			if(notify) {
    //				Notify.notify_caltxt_status_change(
    //						newLocationString.length()==0
    //						? Addressbook.getMyProfile().getHeadline()
    //								: Addressbook.getMyProfile().getHeadline()+", "+newLocationString,
    //						"", "", Addressbook.get().getContactStatusIconResource(Addressbook.getMyProfile()));
    //			}
            } else {
                whereIam = null;
                //tagging place
                for(int i=0; i<size; i++) {
                    //tag all SSID to place
                    Persistence.getInstance(context).insertXPLC(
                            place, results.get(i).BSSID+","+results.get(i).SSID);

                    if(whereIam==null) {
                        //first result
                        whereIam = results.get(i);
                    } else {
                        if(whereIam.level < results.get(i).level) {
                            //another result with stronger signal strength
                            whereIam = results.get(i);
                        }
                    }
                }
                //tag place to strongest AP
    //			Persistence.getInstance(CaltxtApp.getCustomAppContext()).insertXPLC(
    //					place, whereIam.BSSID+","+whereIam.SSID);
            }
        }

        public static void scanWiFiAccessPoints(final Context context, final String place) {

            CaltxtToast.acquirePartialWakeLock(context);

            final WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            final BroadcastReceiver wifiListener = new BroadcastReceiver() {

                @Override
                public void onReceive(Context c, Intent intent) {
                   List<ScanResult> results = wifi.getScanResults();
                   wifiScanResultCallback(context, results, place);

                   context.getApplicationContext().unregisterReceiver(this);
    //               wl.release();
                   CaltxtToast.releasePartialWakeLock();
                }
            };
            context.getApplicationContext().registerReceiver(wifiListener, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            boolean started = wifi.startScan();

            Log.d(TAG, "wifi scan started.. "+started);
        }

    public static String getWiFiSSID(Context context) {
        String ssid = "";
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null
                && activeNetwork.isConnected()
                && ConnectivityManager.TYPE_WIFI == activeNetwork.getType ()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo ();
            ssid  = info.getSSID();
        }

        return ssid;
    }
*/
    public static boolean haveNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) Caltxt.getCustomAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		/*
		* !!STOP STOP STOP!!
		* Do not change to isConnectedOrConnecting(). Let it be .isConnected() !!
		* isConnectedOrConnecting() will lead to Mqtt being initialized when
		* Network is not available. And that will lead to exception in MqttService.
		* If MqttService is initialized without Intenet connectivity, it will throw
		* exception during reconnect()
		* */
        if (activeNetwork != null) {
            Log.i(TAG, "haveNetworkConnection CONNECTED " + activeNetwork.isConnected());
        } else {
            Log.i(TAG, "haveNetworkConnection NOT CONNECTED");
        }
        return (activeNetwork != null
                && activeNetwork.isConnected/*OrConnecting*/());// to avoid condition of connecting forever
    }

    /*
        public static boolean haveNetworkConnection(Context context) {
            try {
                InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name

                if (ipAddr.equals("")) {
                    return false;
                } else {
                    return true;
                }

            } catch (Exception e) {
                return false;
            }
        }
    /*
        public static boolean haveNetworkConnection(Context context) {
            boolean haveConnectedWifi = false;
            boolean haveConnectedMobile = false;

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        haveConnectedWifi = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        haveConnectedMobile = true;
            }
            return haveConnectedWifi || haveConnectedMobile;
        }
    */
    public static void enableDataConnectivityListener() {

        if (br == null) {
            Caltxt.getCustomAppContext().getApplicationContext().
                    registerReceiver(getInstance(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            Log.v(TAG, "action:data listener enabled!!");
        }
//		ComponentName receiver = new ComponentName(context, ConnectivityBroadcastReceiver.class);
//		PackageManager pm = context.getPackageManager();
//		pm.setComponentEnabledSetting(receiver,
//		        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//		        PackageManager.DONT_KILL_APP);

    }

    public static void disableDataConnectivityListener(Context context) {
        if (br != null) {
            context.getApplicationContext().
                    unregisterReceiver(br);
            br = null;
            Log.v(TAG, "action:data listener disabled");
        }
//		ComponentName receiver = new ComponentName(context, ConnectivityBroadcastReceiver.class);
//		PackageManager pm = context.getPackageManager();
//		pm.setComponentEnabledSetting(receiver,
//		        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//		        PackageManager.DONT_KILL_APP);

    }
/*
	public static void enableSMSListener(Context context) {

		ComponentName receiver = new ComponentName(context, SMSBroadcastReceiver.class);
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);

		Log.v(TAG, "action:SMS listener enabled!!");
	}*/

}
