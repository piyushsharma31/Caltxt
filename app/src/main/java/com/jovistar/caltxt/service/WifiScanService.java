package com.jovistar.caltxt.service;

/**
 * Created by jovika on 1/27/2017.
 */
/*
public class WifiScanService extends IntentService {
    private static final String TAG = "WifiScanService";

    public WifiScanService() {
        super("WifiScanService");
    }
//    static boolean WIFI_ENABLED_BY_WIFISCANSERVICE = false;
//    private static boolean WIFI_SCANNED_SCHEDULED = false;

    @Override
    protected void onHandleIntent(final Intent intent) {

        Log.d(TAG, "onHandleIntent");

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock partialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PARTIAL LOCK");
        partialWakeLock.acquire();

        // start cell id scan to discover this place
//        startCellScan();

        final String place = intent.getStringExtra("place");
        final WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

//        final BroadcastReceiver wifiListener = new BroadcastReceiver() {

//            @Override
//            public void onReceive(Context context, Intent intent) {
//                List<ScanResult> results = wifi.getScanResults();
//                wifiScanResultCallback(WifiScanService.this, results, place);
//                getApplicationContext().unregisterReceiver(this);
//                MotionReceiver.getInstance().resetMoved();
//                Log.d(TAG, "wifi scan completed");
//                partialWakeLock.release();
//                WifiScanAlarmReceiver.completeWakefulIntent(intent);
//            }
//        };

//        getApplicationContext().registerReceiver(wifiListener, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        WifiScanReceiver wifiSR = new WifiScanReceiver();
        wifiSR.setAction(place);
        getApplicationContext().registerReceiver(wifiSR, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        boolean started = wifi.startScan();

        if(!started) {
            getApplicationContext().unregisterReceiver(wifiSR);
            MotionReceiver.getInstance().resetMoved();
            Log.d(TAG, "wifi scan completed");
            partialWakeLock.release();
            WifiScanAlarmReceiver.completeWakefulIntent(intent);
        }

        Log.d(TAG, "wifi scan started.. "+started);
    }

    private static synchronized void wifiScanResultCallback(Context context, List<ScanResult> results, String place) {
        int size = 0;
        if(results!=null) {
            size = results.size();
        }

        Log.d(TAG, "wifi scan result..size, "+size);
        XPlc plc = null;
        ScanResult whereIam=null;

        if(place==null) {
            //discovering tagged place
            for (int i = 0; i < size; i++) {
                plc = Persistence.getInstance(context).getPlaceForCellId(results.get(i).BSSID + "," + results.get(i).SSID);
                if (plc != null) {

                    if (whereIam == null) {
                        //first result
                        whereIam = results.get(i);
                    } else {
                        if (whereIam.level < results.get(i).level) {
                            //another result with stronger signal strength
                            whereIam = results.get(i);
                        }
                    }
                }
            }

            Log.d(TAG, "wifi scan result..whereIam, "+whereIam);
            boolean notify = false;
            String newLocationString = "";
            //this is the place where you are
            if (whereIam != null) {
                // wifi found, moved to known place?
                plc = Persistence.getInstance(context).getPlaceForCellId(whereIam.BSSID + "," + whereIam.SSID);
                notify = true;

                // WiFi found! now find if still in same cell network
                int cell_cnt = Persistence.getInstance(context).getCellIdCountForStatus(plc.getStatus());
                String cellid = CallManager.getInstance().getUniqueCellLocation(context);
                String netid = cellid.substring(0, cellid.lastIndexOf("."));
                int net_cnt = Persistence.getInstance(context).getCellNetworkCountForStatus(plc.getStatus(), netid);
                if (cell_cnt > 0 || net_cnt > 0) {
                    // wifi found, one or more cell/network found; moved to a known place
                    newLocationString = plc.getStatus();
                    Log.d(TAG, "wifiScanResultCallback, WiFi FOUND, " + plc.getStatus()
                            + (cell_cnt > 0 ? (", Cell FOUND " + cellid) : (", Net FOUND " + netid)));
                } else {
                    // wifi found, cell/network not found
                    // moved to unknown place (wifi hotspot is portable? movable, not fixed)
                    newLocationString = "";
                    Log.d(TAG, "wifiScanResultCallback, WiFi FOUND, " + plc.getStatus() + ", Cell/Net NOT FOUND " + netid);
                }
            } else {
                // wifi not found, see if cell id found
                String cellid = CallManager.getInstance().getUniqueCellLocation(context);
                plc = Persistence.getInstance(context).getPlaceForCellId(cellid);

                if(plc!=null) {
                    // wifi not found, cell id found, see if wifi id exist for this place
                    int net_cnt = Persistence.getInstance(context).getWiFiCellIdCountForStatus(plc.getStatus());
                    // do not set status based on cellid as there exist a wifi id also; let it me discovered
                    // via wifi scan
                    if(net_cnt>0) {
                        return;
                    }

                    // set the place based on cell id, perhaps for this place there are no wifi ids
                    newLocationString = plc.getStatus();
                } else {
                    // wifi not found, cell id not found, see if cell network id found
                    // place is not recognized, mark unknown. place is recognized by cell id or wifi id
                    // absence of both mean place not recognized, even if network id found
                    newLocationString = "";

                    String netid = cellid.substring(0, cellid.lastIndexOf("."));
                    plc = Persistence.getInstance(context).getPlaceForNetworkId(netid);

                    if (plc != null) {
                        // wifi id not found, cell id not found, cell network found
                        Log.d(TAG, "wifiScanResultCallback, WiFi NOT FOUND, network place found " + plc.getStatus());
//                        String currentLocationString = Addressbook.getInstance(context).getMyProfile().getPlace();

//                        if (currentLocationString.equals(plc.getStatus())) {
//                            Log.d(TAG, "wifiScanResultCallback, WiFi NOT FOUND, network place found moving outside " + plc.getStatus());
                            // do not change the current place. Moving outside of a place
//                            newLocationString = plc.getStatus();
//                        } else {
                            // moved outside into another place, but don't recognize place by cell network, let wifi recognize it!
//                            newLocationString = "";
//                        }
                    } else {
                        // wifi not found, cell network also not found; moved to unknown place
                        newLocationString = "";
                    }
                }
// phone might have moved away from last place (getPlace). So getStatusForNetwork for current network is better
//approach than finding network/cell for current place. therefore commented
                // moved to unknown place, notify
//                String currentLocationString = Addressbook.getInstance(context).getMyProfile().getPlace();
//                if (currentLocationString.length() > 0)
//                    notify = true;
//
//                int net_cnt = Persistence.getInstance(context).getCellNetworkCountForStatus(currentLocationString, netid);
//                if (net_cnt > 0) {
//                     at a known place area (Home area, Work area ..)
//                    newLocationString = currentLocationString;
//                    Log.d(TAG, "wifiScanResultCallback, WiFi NOT FOUND, " + currentLocationString + ", Net FOUND " + netid);
//                } else {
//                     at a new unknown place
//                    newLocationString = "";
//                    Log.d(TAG, "wifiScanResultCallback, WiFi NOT FOUND, " + currentLocationString + ", Net NOT FOUND " + netid);
//                }
            }

            Addressbook.getInstance(context).changeMyStatus(Addressbook.getInstance(context).getMyProfile().getHeadline(),
                    newLocationString);
            // moved addAction in changeMyStatus
//            if (notify) {
//                RebootService.getConnection(context).addAction(Constants.myStatusChangeProperty, null, newLocationString);
//            }
//			if(notify) {
//				Notify.notify_caltxt_status_change(
//						newLocationString.length()==0
//						? Addressbook.getMyProfile().getHeadline()
//								: Addressbook.getMyProfile().getHeadline()+", "+newLocationString,
//						"", "", Addressbook.get().getContactStatusIconResource(Addressbook.getMyProfile()));
//			}
        } else if(place!=null && place.equals("remove")) {
            // remove SSID tagged to any place; forget command
            for(int i=0; i<size; i++) {
                // remove SSID(s) only for this place
                // commented deleteAllXPLCForStatus 24JUN17,
                // there may be overlap of SSID b/w two or more places, remove that overlap
//                plc = Persistence.getInstance(context).getStatusForCellId(results.get(i).BSSID+","+results.get(i).SSID);
//                if(plc!=null) {
//                    Persistence.getInstance(context).deleteAllXPLCForStatus(plc.getStatus());
//                }

                //remove SSID if exist
                Persistence.getInstance(context).deleteXPLC(results.get(i).BSSID+","+results.get(i).SSID);
//                CallManager.getInstance().cellids.remove(results.get(i).BSSID+","+results.get(i).SSID);
                Log.d(TAG, "remove SSID "
                        +(plc==null?(results.get(i).BSSID+","+results.get(i).SSID):plc.toString()));
            }

            // refresh the cell id list
            Persistence.getInstance(context).getAllXPLC();
        } else {
            whereIam = null;
            //tagging place
            for(int i=0; i<size; i++) {
                //tag all SSID to place
                Persistence.getInstance(context).insertXPLC(
                        place, results.get(i).BSSID+","+results.get(i).SSID, XPlc.NETWORK_TYPE_WIFI);

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
}*/
