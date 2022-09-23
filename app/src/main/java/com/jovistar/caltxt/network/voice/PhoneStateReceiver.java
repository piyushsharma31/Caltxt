package com.jovistar.caltxt.network.voice;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.bo.XPlc;
import com.jovistar.caltxt.bo.XQrp;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Blockbook;
import com.jovistar.commons.bo.XMob;

import java.util.Calendar;
import java.util.List;

/**
 * Created by jovika on 2/1/2017.
 */

public class PhoneStateReceiver extends PhoneStateListener {
    private static final String TAG = "PhoneStateReceiver";

    static PhoneStateReceiver instance;
    private Context mContext;
    static int currState = 0;
    static int lastState = 0;
    static int lastLastState = 0;

    private PhoneStateReceiver(Context context) {
        mContext = context;
    }

    public static PhoneStateReceiver getInstance(Context context) {
        if (instance == null) {
            instance = new PhoneStateReceiver(context);
        }
        return instance;
    }

    @Override
    public void onCallStateChanged(int state, String phoneNumber) {

        lastLastState = lastState;
        lastState = currState;
        currState = state;
        Log.d(TAG, "onCallStateChanged currState " + currState+ ", lastState "+lastState+", lastLastState "+lastLastState);

        if (state == TelephonyManager.CALL_STATE_RINGING) {
            Log.d(TAG, "onCallStateChanged CALL_STATE_RINGING " + phoneNumber);
            XQrp autoResponse = Persistence.getInstance(mContext).getQuickAutoResponse();

//            XMob mob = (XMob) Blockbook.getInstance(mContext).get(XMob.toFQMN(incomingNumber, Addressbook.getInstance(mContext).getMyCountryCode()));
            boolean isBlocked = Blockbook.getInstance(mContext).isBlocked(XMob.toFQMN(phoneNumber, Addressbook.getInstance(mContext).getMyCountryCode()));
            if (isBlocked) {
                Log.d(TAG, "onCallStateChanged blocked? " + isBlocked);
            }

            if (Addressbook.getInstance(mContext).getMyProfile().isDND()) {
                Log.d(TAG, "onCallStateChanged isDND? " + Addressbook.getInstance(mContext).getMyProfile().isDND());
                Log.d(TAG, "onCallStateChanged headline " + Addressbook.getInstance(mContext).getMyProfile().getHeadline());
            }
            if (autoResponse != null) {
                Log.d(TAG, "onCallStateChanged autoResponse? " + (autoResponse != null));
            }
            //disconnnect call if this caller is blocked or Caltxt set to auto response
            if (isBlocked ||
                    (autoResponse != null &&
                            autoResponse.getAutoResponseEndTime() > Calendar.getInstance().getTimeInMillis())
                    || Addressbook.getInstance(mContext).getMyProfile().isDND()) {

//					vibrateModeRinger();
//					silentRinger();//SILENT DOES NOT WORK
                //wait for 3 seconds so that caltxt toast comes up before call is disconnected
                /*CountDownTimer timer = new CountDownTimer(2000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {*/
                CallManager.getInstance().endCall(mContext);
//                        Globals.playRingtone();
/*                    }
                };
                timer.start();*/
            }
        } else if (state == TelephonyManager.CALL_STATE_IDLE) {
            Log.d(TAG, "onCallStateChanged CALL_STATE_IDLE " + phoneNumber);
        } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
            Log.d(TAG, "onCallStateChanged CALL_STATE_OFFHOOK " + phoneNumber);
            long timeinms = CallManager.getInstance().peekCall(Constants.CALL_TYPE_OUTBOUND, phoneNumber);

            if(timeinms==0 && lastState==TelephonyManager.CALL_STATE_IDLE) {
                // 24112019, CallHandler::ACTION_NEW_OUTGOING_CALL did not pushCall (permission commented in maniefest file
                // so ACTION_NEW_OUTGOING_CALL will not be invoked, therefore pushCall here
                CallManager.getInstance().pushCall(Constants.CALL_TYPE_OUTBOUND, phoneNumber);
            }
        }
    }

    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfo) {
//        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (cellInfo != null) {
//				Notify.notify_caltxt_alert_cellinfo(TAG, cellInfo.size()+", "+cellInfo, "", "");
//            Log.d(TAG, "onCellInfoChanged");

            String uid = "";
            //, newLocationString = "";
//          String currentLocationString = Addressbook.getInstance(mContext).getMyProfile().getPlace();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                for (CellInfo m : cellInfo) {
                    if (m instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) m;
                        CellIdentityLte cIdentity = cellInfoLte.getCellIdentity();

                        uid = CallManager.getInstance().getCellId(mContext, cIdentity);
                        Log.d(TAG, "CellInfoLte " + uid);

                    } else if (m instanceof CellInfoGsm) {
                        CellInfoGsm u = (CellInfoGsm) m;
                        CellIdentityGsm cIdentity = u.getCellIdentity();

                        uid = CallManager.getInstance().getCellId(mContext, cIdentity);
                        Log.d(TAG, "CellInfoGsm " + uid);
                    } else if (m instanceof CellInfoWcdma) {
                        CellInfoWcdma u = (CellInfoWcdma) m;
                        CellIdentityWcdma cIdentity = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            cIdentity = u.getCellIdentity();
                            uid = CallManager.getInstance().getCellId(mContext, cIdentity);
                        }
                        Log.d(TAG, "CellInfoWcdma " + uid);
                    } else if (m instanceof CellInfoCdma) {
                        CellInfoCdma u = (CellInfoCdma) m;
                        CellIdentityCdma cIdentity = u.getCellIdentity();
                        uid = CallManager.getInstance().getCellId(mContext, cIdentity);
                        Log.d(TAG, "CellInfoCdma " + uid);
                    }

                    // commented 06-JUL-17, do not process cell id change based place change
                    // chances are that a temporary and frequent change of cell id might trigger
                    // place changes
//                    processCellIdToDiscoverPlace(uid);
                }
            }
        }
    }

    @Override
    public void onCellLocationChanged(CellLocation location) {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String uid = "";
        //, newLocationString = "",
        //      currentLocationString = Addressbook.getInstance(mContext).getMyProfile().getPlace();

        if (location instanceof GsmCellLocation) {
            GsmCellLocation gsmLocation = (GsmCellLocation) location;
            if (gsmLocation.getLac() <= 0) {
                // network info not available
                return;
            }
            // 14JUN17, use getUniqueCellLocation to get cell id again. it seems cell got through
            // location object here is different from what getUniqueCellLocation returns
            // so use getUniqueCellLocation here as well as in WifiScanService
//            uid = CallManager.getInstance().getUniqueCellLocation(mContext);
//            Log.d(TAG, "onCellLocationChanged GsmCellLocation getUniqueCellLocation " + uid);
            uid = CallManager.getInstance().getCellId(mContext, gsmLocation);
//            Log.d(TAG, "onCellLocationChanged GsmCellLocation getCellId " + uid);

        } else if (location instanceof CdmaCellLocation) {
            CdmaCellLocation cdmaLocation = (CdmaCellLocation) location;
            if (cdmaLocation.getNetworkId() <= 0) {
                // network info not available
                return;
            }

            // 14JUN17, use getUniqueCellLocation to get cell id again. it seems cell got through
            // location object here is different from what getUniqueCellLocation returns
            // so use getUniqueCellLocation here as well as in WifiScanService
//            uid = CallManager.getInstance().getUniqueCellLocation(mContext);
            uid = CallManager.getInstance().getCellId(mContext, cdmaLocation);
            Log.d(TAG, "onCellLocationChanged CdmaCellLocation " + uid
                    + ", base stn id " + cdmaLocation.getBaseStationId()
                    + ", net id " + cdmaLocation.getNetworkId()
                    + ", sys id " + cdmaLocation.getSystemId());

        } else {
            Log.d(TAG, "onCellLocationChanged CdmaCellLocation unknown location type " + location.toString());
        }

        // commented 06-JUL-17, do not process cell id change based place change
        // chances are that a temporary and frequent change of cell id might trigger
        // place changes
//        processCellIdToDiscoverPlace(uid);
    }

    void processCellIdToDiscoverPlace(String cellid) {
        String newLocationString = Addressbook.getInstance(mContext).getMyProfile().getPlace();
        if (cellid == null || cellid.length() == 0 || cellid.contains(".") == false) {
            return;
        }

        // see if in known location using Cell ID
        XPlc plc_cell = Persistence.getInstance(mContext).getPlaceForCellId(cellid);
        XPlc plc_net = null;
        int wifiCellIdCount = 0;

        if (plc_cell != null) {
            // found a matching place for this cell, see if wifi cell also found
            wifiCellIdCount = Persistence.getInstance(mContext).getWiFiCellIdCountForStatus(plc_cell.getStatus());
            if (wifiCellIdCount == 0) {
                // no wifi cell id for this place, even then update plc (place). some places may not have wifi
                newLocationString = plc_cell.getStatus();
            } else {
                // wifi cell id found for this location, let it be discovered in WiFi scan
                newLocationString = plc_cell.getStatus();
//                return;
            }
        } else {
            // no place found for this cell
            // see if place found for this cell network (using LAC)
            plc_net = Persistence.getInstance(mContext).getPlaceForNetworkId(cellid.substring(0, cellid.lastIndexOf(".")));

            if (plc_net == null) {
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

        Log.d(TAG, "processCellIdToDiscoverPlace plc_net " + plc_net
                + ", plc_cell " + plc_cell
                + ", cellid " + cellid
                + ", wifiCellIdCount " + wifiCellIdCount);

        // commented 05-JUL-17, onCellLocationChanged is called too often, let the place discovery be @15min (WifiScanService)
//        Addressbook.getInstance(mContext).changeMyStatus(
//                Addressbook.getInstance(mContext).getMyProfile().getHeadline(), newLocationString);

        final WifiManager wifi = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= 18) {
            if (false == wifi.isWifiEnabled() && false == wifi.isScanAlwaysAvailable()) {
                // change place only when WiFi scanning is disabled by user
                // commented 19-FEB-17: location change was triggered on TWG10 when call is received
                // due to cell change and WiFi scan not available
                // uncommented, 05-JUL-17, check if any call in progress, if not then only changeStatus
                // generally observed: mobile phone switch cell id when making/receiving call
                if (!CallManager.getInstance().isAnyCallInProgress()) {
                    Addressbook.getInstance(mContext).changeMyStatus(
                            Addressbook.getInstance(mContext).getMyProfile().getHeadline(), newLocationString);
                }
            }
        } else {
            // wifi scan is available in VERSION < 18
            // ignore cell info change. WiFi scan will detect location change
        }

        Log.d(TAG, "isAnyCallInProgress " + CallManager.getInstance().isAnyCallInProgress());
        // 28APR17, added for debugging
        // commented, 17MAY17
//            Intent resultIntent = new Intent(mContext, CaltxtPager.class);
//            NotificationUtils notificationUtils = new NotificationUtils(mContext);
//            notificationUtils.showNotificationMessage("GsmCellLocation changed", cellid, Long.toString(Calendar.getInstance().getTimeInMillis()), resultIntent);
//            Notify.playNotification(mContext);
    }
}
