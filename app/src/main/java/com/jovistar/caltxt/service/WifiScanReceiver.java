package com.jovistar.caltxt.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.activity.SignupProfile;
import com.jovistar.caltxt.bo.XPlc;
import com.jovistar.caltxt.network.voice.CallManager;
import com.jovistar.caltxt.network.voice.PlaceCellStrength;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by jovika on 9/12/2017.
 */

public class WifiScanReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiScanReceiver";

    private String placeAction = null;
    private static boolean scanComplete = true;
    boolean scanWiFiComplete = false;
    boolean scanCellComplete = false;
    //    int count_known_cell_ap = 0;
//    String place_cell_tagged = "";
//    HashMap<String/*place*/, Integer/*signal strength*/> PlaceCellStrength = new HashMap<>();
//    HashMap<String/*place*/, Integer/*signal strength*/> placeWiFiStrength = new HashMap<>();
    ArrayList<PlaceCellStrength> placeCellStrength = new ArrayList<>();
    ArrayList<PlaceCellStrength> placeWiFiStrength = new ArrayList<>();

//    int count_known_cell_net = 0;
//    String place_net_tagged = "";

//    int count_known_wifi_ap = 0;
//    String place_wifi_tagged = "";

//    static boolean isLastScanComplete() {
//        return scanComplete;
//    }

    public void setAction(String action) {
        this.placeAction = action;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

//        final String place = intent.getStringExtra("place");
        final WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> results = wifi.getScanResults();
        wifiScanResultCallback(context, results, placeAction);

        scanWiFiComplete = true;
        context.unregisterReceiver(this);
        MotionReceiver.getInstance().resetMoved(context);

        // set the last wifi scan timestamp
        long current_millis = Calendar.getInstance().getTimeInMillis();
        long last_wifi_scan = SignupProfile.getPreferenceLong(context, context.getResources().getString(R.string.preference_key_last_wifi_scan));
        SignupProfile.setPreferenceLong(context, context.getResources().getString(R.string.preference_key_last_wifi_scan), current_millis);

        Log.d(TAG, "wifi scan completed (onReceive) @"+current_millis+", last_wifi_scan "+last_wifi_scan);
    }

    private synchronized void wifiScanResultCallback(Context context, List<ScanResult> wifiCells, String place) {
        int size = 0;
        if (wifiCells != null) {
            size = wifiCells.size();
        }

        Log.d(TAG, "wifi scan result..size, " + size + ", " + wifiCells);

        if (place == null) {
            ScanResult wifiCell = null;
            XPlc plc = null;
            String cellid = "";
            // discovering this place
            placeWiFiStrength.clear();
            for (int i = 0; i < size; i++) {
                wifiCell = wifiCells.get(i);
                cellid = wifiCell.BSSID + "," + wifiCell.SSID;
                plc = Persistence.getInstance(context).getPlaceForCellId(cellid);
                if (plc != null) {
//                    count_known_wifi_ap++;
//                    int count = placeWiFiCount.containsKey(plc.getStatus())?placeWiFiCount.get(plc.getStatus()):0;
//                    placeWiFiCount.put(plc.getStatus(), count+1);
                    placeWiFiStrength.add(
                            new PlaceCellStrength(
                                    plc.getStatus(),
                                    wifiCell.level,
//                                    WifiManager.calculateSignalLevel(wifiCell.level, 100),
                                    cellid));

                    /*if (wifiCell == null) {
                        //first result
                        wifiCell = wifiCells.get(i);
                    } else {
                        if (wifiCell.level < wifiCells.get(i).level) {
                            //another result with stronger signal strength
                            wifiCell = wifiCells.get(i);
                        }
                    }*/
                }
            }

//            Log.d(TAG, "wifi scan result.. "+placeWiFiStrength);
            //this is the place where you are
            /*if (wifiCell != null) {
                // wifi found, moved to known place?
                plc = Persistence.getInstance(context).getPlaceForCellId(cellid);
//                place_wifi_tagged = plc.getStatus();
            }*/
            Log.d(TAG, "wifi scan result..placeWiFiStrength, " + placeWiFiStrength);
        } else if (place != null && place.equals("remove")) {
            // remove this place from tagged places
            // remove SSID tagged to this place. commented to avoid deleting overlap places
//            String cellid = results.get(i).BSSID+","+results.get(i).SSID;
//            plc = Persistence.getInstance(context).getPlaceForCellId(cellid);
//            if(plc!=null) {
            Persistence.getInstance(context).deleteAllXPLCForStatus(Addressbook.getInstance(context).getMyProfile().getPlace());
//            }

            /* remove SSID tagged to this place (new discovery); forget command
            for(int i=0; i<size; i++) {
            cellid = wifiCells.get(i).BSSID+","+wifiCells.get(i).SSID;
                Persistence.getInstance(context).deleteXPLC(cellid);

                Log.d(TAG, "remove SSID "
                        +(plc==null?(cellid):plc.toString()));
            }*/

            // refresh the cell id list
            Persistence.getInstance(context).getAllXPLC();
        } else {
            String cellid = "";
            // tag this place (with all wifi cells) as "place"
            for (int i = 0; i < size; i++) {
                cellid = wifiCells.get(i).BSSID + "," + wifiCells.get(i).SSID;
                //tag all SSID to place
                Persistence.getInstance(context).insertXPLC(place, cellid, XPlc.NETWORK_TYPE_WIFI);
//                count_known_wifi_ap++;
                Log.d(TAG, "insertXPLC, wifi " + cellid);
            }
        }
    }

    public void startWifiAndCellScan(final Context context) {
        if (scanComplete == false) {
            Log.e(TAG, "scan already scheduled!! place " + placeAction);
            return;
        }

        placeWiFiStrength.clear();
        placeCellStrength.clear();
        scanCellComplete = false;
        scanWiFiComplete = false;
        scanComplete = false;

        startCellScan(context);
        // place discovery
//        if(placeAction==null) {
        // scan 2g/3g/4g cells first, then wifi cells
//            cells = startCellScan(context.getApplicationContext());
//        }

        final WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        context.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        final boolean scanStarted = wifi.startScan();

        if (!scanStarted) {
            context.unregisterReceiver(this);
            // resetMoved commented 02-2018, moved=false should be done when scan successfully
            // resulted in place change/or no change. moved value should be intact if scan is
            // not successful/or initiated, so that its taken into consideration as soon as next scan
            // is successful
//            MotionReceiver.getInstance().resetMoved();
//            scanComplete = true;
            Log.d(TAG, "startWifiScan did not start");
            return;
        } else {
            Log.d(TAG, "startWifiScan started!!");
        }

//        final WifiScanReceiver wifiSR = this;
        // check after 15 seconds if scan is complete, if not release resources
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                new CountDownTimer(15000, 15000) {
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        scanComplete = true;

                        if (scanWiFiComplete) {
                            Log.d(TAG, "wifi scan completed (startWifiScan)");
                        } else {
                            context.unregisterReceiver(WifiScanReceiver.this);
                            // resetMoved commented 02-2018, moved=false should be done when scan successfully
                            //MotionReceiver.getInstance().resetMoved();
                            Log.d(TAG, "wifi scan did not completed (startWifiScan)");
                        }

                        if (placeAction == null) {
                            Log.d(TAG, "wifi scan placeAction==null");
                            // discovering this place
                            if (!CallManager.getInstance().isAnyCallInProgress()) {
                                Log.d(TAG, "wifi scan isAnyCallInProgress NO!");
                                changeStatusForWiFiAndCellScan(context);
                            }
                        }
                    }
                }.start();
            }
        });
    }

    private void changeStatusForWiFiAndCellScan(final Context context) {
        String newPlaceWiFi = "";
        Integer strengthWifi = -1000;
        for (int i = 0; i < placeWiFiStrength.size(); i++) {
            String place = placeWiFiStrength.get(i).place;
            int placeStrength = placeWiFiStrength.get(i).strength;
            if (strengthWifi < placeStrength) {
                newPlaceWiFi = place;
                strengthWifi = placeStrength;
            }
        }
        Log.d(TAG, "changeStatusForWiFiAndCellScan newPlaceWiFi "+newPlaceWiFi+", strengthWifi "+strengthWifi);

        String newPlaceCell = "";
        // Signal strength is represented in -dBm format (0 to -100)
        Integer strengthCell = -1000;// assigned very small value to cover all possible cell discovered
        for (int i = 0; i < placeCellStrength.size(); i++) {
            String place = placeCellStrength.get(i).place;
            int placeStrength = placeCellStrength.get(i).strength;
            if (strengthCell < placeStrength) {
                newPlaceCell = place;
                strengthCell = placeStrength;
            }
        }
        Log.d(TAG, "changeStatusForWiFiAndCellScan newPlaceCell "+newPlaceCell+", strengthCell "+strengthCell);

        if (placeWiFiStrength.size() == 0 && placeCellStrength.size() == 0) {
            Log.d(TAG, "changeStatusForWiFiAndCellScan no cell, no wifi found");
            // no cell, no wifi found - cannot be true (generally)
            if(scanWiFiComplete) {
                // wifi scan successfully completed, therefore its true that we are at a place where there
                // is no connectivity. mark this place as unknown!!
                Addressbook.getInstance(context).changeMyStatus(Addressbook.getInstance(context).getMyProfile().getHeadline(),
                        "");
            } else {
                // sometimes device is in doze / sleep mode therefore network is put off by device
                // to save battery. so, do not change place / status
            }
        } else if (placeWiFiStrength.size() > 1) {
            Log.d(TAG, "changeStatusForWiFiAndCellScan at least 2 wifi found");
            // at least 2 wifi found, must be a place, not portable (moving) wifi hotspot, change place
            Addressbook.getInstance(context).changeMyStatus(Addressbook.getInstance(context).getMyProfile().getHeadline(),
                    newPlaceWiFi);

            /* tag neighbouring cells to this place
            Iterator<String> iterator = cells.iterator();
            while (iterator.hasNext()) {
                String cell = iterator.next();
                Persistence.getInstance(context).insertXPLC(newPlaceWiFi, cell, XPlc.NETWORK_TYPE_CELL);
                Log.d(TAG, "insertXPLC, neighbouring cell " + cell);
            }*/

        } else if (placeCellStrength.size() > 0) {
            Log.d(TAG, "changeStatusForWiFiAndCellScan at least 1 cell found");
            int wifiCellIdCount = Persistence.getInstance(context).getWiFiCellIdCountForStatus(newPlaceCell);
            if(wifiCellIdCount==0) {
                Log.d(TAG, "changeStatusForWiFiAndCellScan at least one known cell found");
                // at least one known cell found and this place does not map any WiFi hotspot
                Addressbook.getInstance(context).changeMyStatus(Addressbook.getInstance(context).getMyProfile().getHeadline(),
                        newPlaceCell);
            } else {
                // added 02-2018
                // if WiFi cell id count for this place is >0, then always set place when WiFi hotspot found
                // this helps in tagging places based on WiFi where both WiFi and GSM is found/mapped
                // for example, a pub might have a WiFi hotspot and GSM also is accessible from there; however the
                // place will be tagged to pub when WiFi hotspot is found, not just when GSM is found
                // 25112019, since we are not sure this place is newPlaceCell or newPlaceWiFi (not sufficient hotspots),
                // we will tag this as unknown
                Log.d(TAG, "changeStatusForWiFiAndCellScan newPlaceCell "+newPlaceCell+", getPlace "+Addressbook.getInstance(context).getMyProfile().getPlace());
                if(!newPlaceCell.equals(Addressbook.getInstance(context).getMyProfile().getPlace())) {
                    Log.d(TAG, "changeStatusForWiFiAndCellScan set place to unknown");
                    Addressbook.getInstance(context).changeMyStatus(Addressbook.getInstance(context).getMyProfile().getHeadline(),
                            "");
                }
            }
        } else {
            Log.d(TAG, "startWifiScan, ONE wifi found placeWiFiStrength " + placeWiFiStrength.size());
            Log.d(TAG, "startWifiScan, ONE cell found placeCellStrength " + placeCellStrength.size());
        }
    }

    private Set<String> startCellScan(Context context) {

        HashMap<String, Integer> cells = CallManager.getInstance().getAllCells(context);
        if(cells.size()==0) {
            scanCellComplete = false;
            return cells.keySet();
        }

        scanCellComplete = true;

        if (placeAction == null) {
            // discovering this place
            placeCellStrength.clear();
            Iterator<String> iterator = cells.keySet().iterator();
            while (iterator.hasNext()) {
                String cellid = iterator.next();
                int cellStrength = cells.get(cellid);
                XPlc plc = Persistence.getInstance(context).getPlaceForCellId(cellid);
                if (plc == null) {
                    // no place associated with cell. find place associated with cell network instead
                    // commented 20OCT17, cell network does not necessarily depicts location area
//                    cellid = cellid.substring(0, cellid.lastIndexOf("."));
//                    plc = Persistence.getInstance(context).getPlaceForNetworkId(cellid);
                    if (plc != null) {
//                        strength_known_cell_net++;
//                        place_net_tagged = plc.getStatus();

                        // commented, do we need to tag on cell network?
                        // add new cells/wifi if discovered at known place
//                        int count = PlaceCellStrength.containsKey(plc.getStatus())?PlaceCellStrength.get(plc.getStatus()):0;
                        placeCellStrength.add(new PlaceCellStrength(plc.getStatus(), cellStrength, cellid));
                    }
                } else {
//                    count_known_cell_ap++;
//                    place_cell_tagged = plc.getStatus();

//                    int count = placeCellCount.containsKey(plc.getStatus())?placeCellCount.get(plc.getStatus()):0;
                    placeCellStrength.add(new PlaceCellStrength(plc.getStatus(), cellStrength, cellid));
                }
            }

//            if(!CallManager.getInstance().isAnyCallInProgress()) {
//                Addressbook.getInstance(context).changeMyStatus(Addressbook.getInstance(context).getMyProfile().getHeadline(),
//                        place_cell_tagged);
//            }
            Log.d(TAG, "cell scan result..PlaceCellStrength, " + placeCellStrength);
        } else if (placeAction.equalsIgnoreCase("remove")) {
            // remove all cells tagged to this place
            Persistence.getInstance(context).deleteAllXPLCForStatus(Addressbook.getInstance(context).getMyProfile().getPlace());
            /* remove all cells at this place from tagged places
            for (int i = 0; i < cells.size(); i++) {
                String cellid = cells.get(i);
                Log.d(TAG, "Forget cell id " + cellid);
                Persistence.getInstance(context).deleteXPLC(cellid);
            }*/
        } else {
            // tag this place (with all wifi cells) as "place"
            Iterator<String> iterator = cells.keySet().iterator();
//            for (int i = 0; i < cells.size(); i++) {
            while (iterator.hasNext()) {
                String cellid = iterator.next();
                Persistence.getInstance(context).insertXPLC(placeAction, cellid, XPlc.NETWORK_TYPE_CELL);
//                count_known_cell_ap++;
                Log.d(TAG, "insertXPLC, cell " + cellid);
            }
        }

        return cells.keySet();
    }
    /*void startCellScan(Context context) {

        String cellid = CallManager.getInstance().getUniqueCellLocation(context);

        String newLocationString = Addressbook.getInstance(context).getMyProfile().getPlace();

        if(cellid==null || cellid.length()==0 || cellid.contains(".")==false) {
            return;
        }

        // see if in known location using Cell ID
        XPlc plc_cell = Persistence.getInstance(context).getPlaceForCellId(cellid);
        XPlc plc_net = null;
//        int wifiCellIdCount = 0;

        if (plc_cell != null) {
            // found a matching place for this cell, see if wifi cell also found
//            wifiCellIdCount = Persistence.getInstance(context).getWiFiCellIdCountForStatus(plc_cell.getStatus());
//            if (wifiCellIdCount == 0) {
            // no wifi cell id for this place, even then update plc (place). some places may not have wifi
//                newLocationString = plc_cell.getStatus();
//            } else {
            // wifi cell id found for this location, let it be discovered in WiFi scan
            newLocationString = plc_cell.getStatus();
//                return;
//            }
        } else {
            // no place found for this cell
            // see if place found for this cell network (using LAC)
            plc_net = Persistence.getInstance(context).getPlaceForNetworkId(cellid.substring(0, cellid.lastIndexOf(".")));

            if(plc_net==null) {
                // cell id or network id not found; moved to unknown place
                // sometimes phone switches to unknown cell network at an known place also
                // for example, during incoming / outgoing call phone may switch to 2g cell (different cell network)
                // and rest of the time it remains in 3g/4g cell (different cell network)
                newLocationString = "";
            } else {
                // network id found, do not set new place, let wifi discover
                // if wifi is disabled (isScanAlwaysAvailable==false or isWifiEnabled==false),
                // then changeStatus will be called to set this place as current
                newLocationString = plc_net.getStatus();
            }

            // initiate Wifi discovery, to know if we are at unknown place
            // commented 05-JUL-17, onCellLocationChanged is called too often, let the place discovery be @15min (WifiScanService)
//            mContext.startService(new Intent(mContext, WifiScanService.class));
        }

        Log.d(TAG, "startCellScan plc_net " +plc_net
                +", plc_cell "+plc_cell
                +", cellid "+cellid);
//                + ", wifiCellIdCount "+wifiCellIdCount);

        // commented 05-JUL-17, onCellLocationChanged is called too often, let the place discovery be @15min (WifiScanService)
//        Addressbook.getInstance(mContext).changeMyStatus(
//                Addressbook.getInstance(mContext).getMyProfile().getHeadline(), newLocationString);

//        final WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        if (Build.VERSION.SDK_INT >= 18) {
//            if (false == wifi.isWifiEnabled() && false == wifi.isScanAlwaysAvailable()) {
        // change place only when WiFi scanning is disabled by user
        // commented 19-FEB-17: location change was triggered on TWG10 when call is received
        // due to cell change and WiFi scan not available
        // uncommented, 05-JUL-17, check if any call in progress, if not then only changeStatus
        // generally observed: mobile phone switch cell id when making/receiving call
        if(!CallManager.getInstance().isAnyCallInProgress()) {
            Addressbook.getInstance(context).changeMyStatus(
                    Addressbook.getInstance(context).getMyProfile().getHeadline(), newLocationString);
        }
//            }
//        } else {
        // wifi scan is available in VERSION < 18
        // ignore cell info change. WiFi scan will detect location change
//        }

        Log.d(TAG, "startCellScan, isAnyCallInProgress "+CallManager.getInstance().isAnyCallInProgress());
        // 28APR17, added for debugging
        // commented, 17MAY17
//            Intent resultIntent = new Intent(mContext, CaltxtPager.class);
//            NotificationUtils notificationUtils = new NotificationUtils(mContext);
//            notificationUtils.showNotificationMessage("GsmCellLocation changed", cellid, Long.toString(Calendar.getInstance().getTimeInMillis()), resultIntent);
//            Notify.playNotification(mContext);
    }*/
}
