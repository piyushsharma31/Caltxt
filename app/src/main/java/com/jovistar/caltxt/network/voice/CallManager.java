package com.jovistar.caltxt.network.voice;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telecom.TelecomManager;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.android.internal.telephony.ITelephony;
import com.jovistar.caltxt.R;
import com.jovistar.caltxt.activity.Settings;
import com.jovistar.caltxt.activity.SplashScreen;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.commons.bo.XMob;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jovika on 2/1/2017.
 */

public class CallManager {
    private static final String TAG = "CallManager";

    public static String stateStr = TelephonyManager.EXTRA_STATE_IDLE;
    public static String lastStateStr = TelephonyManager.EXTRA_STATE_IDLE;
    public static String lastLastStateStr = TelephonyManager.EXTRA_STATE_IDLE;
    public static int PHONE_LISTENER_FLAGS = PhoneStateListener.LISTEN_CALL_STATE;
//    public HashMap<String, XPlc> cellids = new HashMap<String, XPlc>();

    //    private static ArrayList<Call> outgoingCalls = new ArrayList<Call>();
//    private static ArrayList<Call> incomingCalls = new ArrayList<Call>();
    private static HashMap<String, Long> outgoingCalls = new HashMap<String, Long>();
    private static HashMap<String, Long> incomingCalls = new HashMap<String, Long>();

    static ITelephony telephonyService = null;
    private static CallManager instance;

    private CallManager() {
        Log.d(TAG, "CallManager object constructed");
    }

    public HashMap getIncomingCallsInProgress() {
        return incomingCalls;
    }

    public HashMap getOutgoingCallsInProgress() {
        return outgoingCalls;
    }

    public long pushCall(int bound, String number) {
        number = XMob.toFQMN(number, Addressbook.getMyCountryCode());
        Log.d(TAG, "pushCall " + number);
        long timeinms = 0;
        if (bound == Constants.CALL_TYPE_INBOUND) {
            timeinms = Calendar.getInstance().getTimeInMillis();
            incomingCalls.put(number, timeinms);
        } else if (bound == Constants.CALL_TYPE_OUTBOUND) {
            timeinms = Calendar.getInstance().getTimeInMillis();
            outgoingCalls.put(number, timeinms);
        }

        return timeinms;
    }

    public long peekCall(int bound, String number) {
        number = XMob.toFQMN(number, Addressbook.getMyCountryCode());
        Long timeinms = null;
        Log.d(TAG, "peekCall " + number);
        if (bound == Constants.CALL_TYPE_INBOUND) {
            timeinms = incomingCalls.get(number);
        } else if (bound == Constants.CALL_TYPE_OUTBOUND) {
            timeinms = outgoingCalls.get(number);
        }
        return (timeinms==null?0:timeinms);
    }

    public long popCall(int bound, String number) {
        number = XMob.toFQMN(number, Addressbook.getMyCountryCode());
        Long timeinms = null;
        Log.d(TAG, "popCall " + number);
        if (bound == Constants.CALL_TYPE_INBOUND) {
            timeinms = incomingCalls.remove(number);
        } else if (bound == Constants.CALL_TYPE_OUTBOUND) {
            timeinms = outgoingCalls.remove(number);
        }
        return (timeinms==null?0:timeinms);
    }

    public static CallManager getInstance() {
        if (instance == null)
            instance = new CallManager();
        return instance;
    }

    public boolean isIncomingCallInProgress(String number) {
        number = XMob.toFQMN(number, Addressbook.getMyCountryCode());
        return incomingCalls.containsKey(number);
//        Iterator<Call> it = incomingCalls.iterator();
//
//        Call object = null;
//        while (it.hasNext()) {
//            object = (Call) it.next();
//            if (object.number.equals(number))
//                return true;
//        }

//        return false;
    }

    public synchronized boolean isOutgoingCallInProgress(String number) {
        number = XMob.toFQMN(number, Addressbook.getMyCountryCode());
        return outgoingCalls.containsKey(number);
/*        Iterator<Call> it = outgoingCalls.iterator();

        Log.d(TAG, "CallReceiver:isOutgoingCallInProgress number " + number);
        Call object = null;
        while (it.hasNext()) {
            object = (Call) it.next();
            Log.d(TAG, "CallReceiver:isOutgoingCallInProgress " + object.number);
//			if(object.number.equals(number))
            if (object.number.endsWith(number))
                return true;
        }

        return false;*/
    }

    public synchronized boolean isAnyCallInProgress() {
        Log.d(TAG, "isAnyCallInProgress outgoingCalls.size() "+outgoingCalls.size()+", stateStr "+stateStr);
        return ((outgoingCalls.size() > 0) || stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK));
    }

    public void endCall(Context context) {
        try {
            if (telephonyService == null) {
                callControllerInit(context);
            }
            telephonyService.endCall();
            Log.d(TAG, "CallReceiver endCall");
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void normalModeRinger(Context context) {

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
/*        //For Normal mode
//        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI);
            am.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI);
            am.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI);
            am.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI);
            am.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI);
            am.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI);
//        } else {
//            am.setStreamMute(AudioManager.STREAM_MUSIC, false);
//            am.setStreamMute(AudioManager.STREAM_ALARM, false);
//            am.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
//            am.setStreamMute(AudioManager.STREAM_RING, false);
//            am.setStreamMute(AudioManager.STREAM_SYSTEM, false);
//            am.setStreamMute(AudioManager.STREAM_VOICE_CALL, false);
//        }*/
    }

    public static void vibrateModeRinger(Context context) {

        AudioManager amanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        //For Vibrate mode
        amanager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//		amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
//        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
//        amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
//        amanager.setStreamMute(AudioManager.STREAM_RING, true);
//        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        Log.d(TAG, "CallReceiver:vibrateModeRinger");
    }

    public static void silentRinger(Context context) {
        /*try {
			if(telephonyService==null) {
				callControllerInit();
			}
			telephonyService.silenceRinger();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        AudioManager am;
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
        am.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
        am.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
        am.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
        am.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
        am.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_MUTE, 0);

        //For Silent mode
//        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        Log.d(TAG, "CallReceiver:silentRinger");
    }

    public static void answerCall(Context context) {
        try {
            if (telephonyService == null) {
                callControllerInit(context);
            }
            telephonyService.answerRingingCall();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void callControllerInit(Context context) {
        return;
        //commented 25SEP2022 -- internal API cannot call
/*        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                //telecomManager.endCall();
            }

        }else{
            //Ask for permission here

        }
        Log.d(TAG, "callControllerInit START");
        // Java Reflections
        Class c = null;
        try {
            c = Class.forName(telephonyManager.getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Method m = null;
        try {
            m = c.getDeclaredMethod("getITelephony");
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        m.setAccessible(true);
        if (telephonyService == null) {
            try {
                telephonyService = (ITelephony) m.invoke(telephonyManager);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "callControllerInit END");*/
    }

    public void listen(Context context) {

        return;
        //commented 25SEP2022 -- internal API
/*        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//		if (is_blocked) {
        PhoneStateReceiver callBlockListener = PhoneStateReceiver.getInstance(context);
        telephonyManager.listen(callBlockListener, PhoneStateListener.LISTEN_NONE);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        Settings.detect_places = settings.getString(
                context.getString(R.string.preference_key_places_remember),
                context.getResources().getString(R.string.preference_value_places_remember_default));

        if (Settings.detect_places.equals(Settings.PLACES_AUTO_DETECT)) {
            Log.d("CallReceiver", "onPreferenceChange LISTEN_CELL_LOCATION");
            PHONE_LISTENER_FLAGS = PhoneStateListener.LISTEN_CALL_STATE
                    | PhoneStateListener.LISTEN_CELL_LOCATION
                    | PhoneStateListener.LISTEN_CELL_INFO;
        } else {
            Log.d("CallReceiver", "onPreferenceChange LISTEN_CALL_STATE");
            PHONE_LISTENER_FLAGS = PhoneStateListener.LISTEN_CALL_STATE;
        }

        try {
            telephonyManager.listen(callBlockListener, PHONE_LISTENER_FLAGS);
        } catch (SecurityException e) {
            PHONE_LISTENER_FLAGS = PhoneStateListener.LISTEN_CALL_STATE;
            telephonyManager.listen(callBlockListener, PHONE_LISTENER_FLAGS);
        }*/
        //			telephonyManager.listen(callBlockListener, PhoneStateListener.LISTEN_CELL_LOCATION);
//		} else {
//			telephonyManager.listen(callBlockListener, PhoneStateListener.LISTEN_NONE);
//		}
    }

    public String getCellId(Context context, GsmCellLocation loc) {
        String cid = null;
        if (loc.getLac() < 0) {
            return null;
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //commented 25SEP2022 -- internal API
        // String networkOperator = telephonyManager.getNetworkOperator();
        if (loc.getLac() == -1 || loc.getCid() == -1) {

        } else {
            //commented 25SEP2022 -- internal API
            //cid = telephonyManager.getNetworkOperator() + "." + (loc.getLac() & 0xffff) + "." + (loc.getCid() & 0xffff);
        }

        Log.d(TAG, "getCellId GsmCellLocation " + cid);
        return cid;
    }

    public String getCellId(Context context, CdmaCellLocation loc) {
        String cid = null;
        if (loc.getNetworkId() < 0) {
            return cid;
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (loc.getNetworkId() == -1 || loc.getBaseStationId() == -1) {

        } else {
            //commented 25SEP2022 -- internal API
//            cid = telephonyManager.getNetworkOperator()/*loc.getSystemId()*/
//                    + "." + (loc.getNetworkId()) + "." + (loc.getBaseStationId());
        }
        Log.d(TAG, "getCellId CdmaCellLocation " + cid);
        return cid;
    }

    public String getCellId(Context context, NeighboringCellInfo cInfo) {
        String cid = null;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = telephonyManager.getNetworkOperator();
        int mcc = Integer.parseInt(networkOperator.substring(0, 3));
        int mnc = Integer.parseInt(networkOperator.substring(3));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (cInfo.getNetworkType() == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
                return null;
            } else if (cInfo.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS
                    || cInfo.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE) {

                if (cInfo.getLac() != NeighboringCellInfo.UNKNOWN_CID
                        && cInfo.getCid() != NeighboringCellInfo.UNKNOWN_CID) {
                    cid = (String.valueOf(mcc) + mnc) + "."
                            + (cInfo.getLac() & 0xffff) + "."
                            + (cInfo.getCid() & 0xffff);
                } else {
                    Log.d(TAG, "getCellId invalid " + cInfo);
                }

            } else /*if(cInfo.getNetworkType()==TelephonyManager.NETWORK_TYPE_HSDPA
                    ||  cInfo.getNetworkType()==TelephonyManager.NETWORK_TYPE_UMTS
                    || cInfo.getNetworkType()==TelephonyManager.NETWORK_TYPE_HSUPA
                    || cInfo.getNetworkType()==TelephonyManager.NETWORK_TYPE_HSPA)*/ {
                if (cInfo.getPsc() != NeighboringCellInfo.UNKNOWN_CID) {
                    cid = (String.valueOf(mcc) + mnc) + "."
                            + (cInfo.getPsc() & 0x1ff);
                } else {
                    Log.d(TAG, "getCellId invalid " + cInfo);
                }
            }
        }
        Log.d(TAG, "getCellId NeighboringCellInfo " + cid);
        return cid;
    }

    public String getCellId(Context context, CellIdentityLte cIdentity) {
        String cid = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            if(cIdentity.getMnc()<0) {
//                return null;
//            }

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (cIdentity.getTac() == Integer.MAX_VALUE
                    || cIdentity.getCi() == Integer.MAX_VALUE
                    || cIdentity.getMcc() == Integer.MAX_VALUE
                    || cIdentity.getMnc() == Integer.MAX_VALUE
//                    || cIdentity.getPci()==Integer.MAX_VALUE
            ) {

                Log.d(TAG, "getCellId invalid " + cIdentity);
            } else {
                cid = (String.valueOf(cIdentity.getMcc()) + cIdentity.getMnc()) + "."
                        + (cIdentity.getTac() & 0xffff) + "."
                        + (cIdentity.getCi() & 0xffff);// + "."
//                        + (cIdentity.getPci());
            }
        }
        Log.d(TAG, "getCellId CellIdentityLte " + cid);
        return cid;
    }

    public String getCellId(Context context, CellIdentityGsm cIdentity) {
        String cid = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            if(cIdentity.getMnc()<0) {
//                return null;
//            }

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (cIdentity.getLac() == Integer.MAX_VALUE
                    || cIdentity.getMcc() == Integer.MAX_VALUE
                    || cIdentity.getMnc() == Integer.MAX_VALUE
                    || cIdentity.getCid() == Integer.MAX_VALUE
                    || cIdentity.getCid() == -1) {

                Log.d(TAG, "getCellId invalid " + cIdentity);
            } else {
                cid = (String.valueOf(cIdentity.getMcc()) + cIdentity.getMnc()) + "."
                        + (cIdentity.getLac() & 0xffff) + "."
                        + (cIdentity.getCid() & 0xffff);
            }
        }
        Log.d(TAG, "getCellId CellIdentityGsm " + cid);
        return cid;
    }

    public String getCellId(Context context, CellIdentityCdma cIdentity) {
        String cid = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            if(cIdentity.getNetworkId()<0) {
//                return null;
//            }

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (cIdentity.getNetworkId() == Integer.MAX_VALUE
                    || cIdentity.getBasestationId() == Integer.MAX_VALUE) {

                Log.d(TAG, "getCellId invalid " + cIdentity);
            } else {
                cid = telephonyManager.getNetworkOperator()/*cIdentity.getSystemId()*/ + "."
                        + (cIdentity.getNetworkId() & 0xffff) + "."
                        + (cIdentity.getBasestationId() & 0xffff);
            }
        }
        Log.d(TAG, "getCellId CellIdentityCdma " + cid);
        return cid;
    }

    public String getCellId(Context context, CellIdentityWcdma cIdentity) {
        String cid = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            if(cIdentity.getLac()<0) {
//                return null;
//            }

//            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (cIdentity.getLac() == Integer.MAX_VALUE
                    || cIdentity.getMcc() == Integer.MAX_VALUE
                    || cIdentity.getMcc() == Integer.MAX_VALUE
                    || cIdentity.getCid() == Integer.MAX_VALUE
                    || cIdentity.getCid() == -1) {

                Log.d(TAG, "getCellId invalid " + cIdentity);
            } else {
                cid = (String.valueOf(cIdentity.getMcc()) + cIdentity.getMnc()) + "."
                        + (cIdentity.getLac() & 0xffff) + "."
                        + (cIdentity.getCid() & 0xffff/*added mask 19-DEC-2017. It was missed supposedly*/);
            }
        }
        Log.d(TAG, "getCellId CellIdentityWCdma " + cid);
        return cid;
    }

    // get all cells including primary and neighbouring cells at this location
    public HashMap<String, Integer> getAllCells(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        HashMap<String, Integer> result = new HashMap<>();

        // add primary & neighbouring cells. Try getAllCellInfo
        List<CellInfo> cellsInfo = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                //commented 25SEP2022 -- internal API
                //cellsInfo = telephonyManager.getAllCellInfo();
            }
        } catch (SecurityException e) {
            // program will crash most likely if permission is not explicitly granted by user
        }
        String cellid = null;
        int strengthDbm = 1;

        // add all cells to result
        if (cellsInfo != null) {

            for (int i = 0; i < cellsInfo.size(); i++) {
                CellInfo ci = cellsInfo.get(i);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (ci instanceof CellInfoLte) {
                        CellInfoLte loc = (CellInfoLte) ci;
                        cellid = getCellId(context, loc.getCellIdentity());
                        strengthDbm = loc.getCellSignalStrength().getDbm();
                    } else if (ci instanceof CellInfoCdma) {
                        CellInfoCdma loc = (CellInfoCdma) ci;
                        cellid = getCellId(context, loc.getCellIdentity());
                        strengthDbm = loc.getCellSignalStrength().getDbm();
                    } else if (ci instanceof CellInfoGsm) {
                        CellInfoGsm loc = (CellInfoGsm) ci;
                        cellid = getCellId(context, loc.getCellIdentity());
                        strengthDbm = loc.getCellSignalStrength().getDbm();
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        if (ci instanceof CellInfoWcdma) {
                            CellInfoWcdma loc = (CellInfoWcdma) ci;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                cellid = getCellId(context, loc.getCellIdentity());
                                strengthDbm = loc.getCellSignalStrength().getDbm();
                            }
                        }
                    }
                }

                if (cellid != null) {
                    result.put(cellid, strengthDbm);
                }
            }
        } /*else {
            // if getAllCellInfo returns null, call getNeighbouringCellInfo

            // add primary cell to result
            result.put(getUniqueCellLocationAPI1(context), strengthDbm);

            // add neighbouring cells to result
            List<NeighboringCellInfo> ncellsInfo = telephonyManager.getNeighboringCellInfo();
            Log.d(TAG, "ncellsInfo " + ncellsInfo);
            if (ncellsInfo != null) {
                for (int i = 0; i < ncellsInfo.size(); i++) {
                    NeighboringCellInfo ci = ncellsInfo.get(i);
                    if (ci.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS
                            || ci.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE) {

                        // For GSM, it is in "asu" ranging from 0 to 31 (dBm = -113 + 2*asu)
                        // 0 means "-113 dBm or less" and 31 means "-51 dBm or greater"
                        int dBm = -113 + (2 * ci.getRssi());
                        result.put(getCellId(context, ci), dBm);
                    } else
//                    if(ci.getNetworkType()==TelephonyManager.NETWORK_TYPE_UMTS
//                            || ci.getNetworkType()==TelephonyManager.NETWORK_TYPE_HSDPA
//                            || ci.getNetworkType()==TelephonyManager.NETWORK_TYPE_HSUPA
//                            || ci.getNetworkType()==TelephonyManager.NETWORK_TYPE_HSPA)
                             {
                        // For UMTS, it is the Level index of CPICH RSCP defined in TS 25.125

                        result.put(getCellId(context, ci), ci.getRssi());
                    }
                }
            }
        }*/

        Log.d(TAG, "getAllCells result " + result);
        return result;
    }

    /*
        public String getUniqueCellLocation(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return getUniqueCellLocationAPI17(context);
            } else {
                return getUniqueCellLocationAPI1(context);
            }
        }
    */

/*    private String getUniqueCellLocationAPI1(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String uid = "";
        int cid, lac = 0;

        if (PackageManager.PERMISSION_GRANTED
                != Caltxt.checkPermission((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION,
                Caltxt.CALTXT_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION,
                "Caltxt needs permission to use nearby WiFi hotspots to tag your status")) {

            return uid;
        }

        CellLocation cloc = telephonyManager.getCellLocation();

        if (cloc instanceof GsmCellLocation) {
            GsmCellLocation loc = (GsmCellLocation) cloc;
            uid = getCellId(context, loc);
        } else if (cloc instanceof CdmaCellLocation) {
            CdmaCellLocation loc = (CdmaCellLocation) cloc;
            if (loc.getNetworkId() < 0) {
                return null;
            }
            uid = getCellId(context, loc);
        }

        Log.d(TAG, "getUniqueCellLocationAPI1 UID " + uid);
        return uid;
    }
*/
/*    private String getUniqueCellLocationAPI17(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String uid = "";
        ArrayList<String> uids = new ArrayList<>();
        int cid, lac = 0;
        List<CellInfo> cellsInfo = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            cellsInfo = telephonyManager.getAllCellInfo();

            if(cellsInfo==null) {
                // old phone return null getAllCellInfo, so call getUniqueCellLocationAPI1 instead
                return getUniqueCellLocationAPI1(context);
//                return uid;
            }

            for(int i=0; i<cellsInfo.size(); i++) {
                CellInfo ci = cellsInfo.get(i);
                if (ci instanceof CellInfoLte) {
                    CellInfoLte loc = (CellInfoLte) ci;
                    uid = getCellId(context, loc.getCellIdentity());
                } else if (ci instanceof CellInfoCdma) {
                    CellInfoCdma loc = (CellInfoCdma) ci;
                    uid = getCellId(context, loc.getCellIdentity());
                } else if (ci instanceof CellInfoGsm) {
                    CellInfoGsm loc = (CellInfoGsm) ci;
                    uid = getCellId(context, loc.getCellIdentity());
                } else if (ci instanceof CellInfoWcdma) {
                    CellInfoWcdma loc = (CellInfoWcdma) ci;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        uid = getCellId(context, loc.getCellIdentity());
                    }
                }

                if(ci.isRegistered()) {
                    Log.d(TAG, "getUniqueCellLocationAPI17 registered UID "+uid);
                    break;
                } else {
                    Log.d(TAG, "getUniqueCellLocationAPI17 UID "+uid);
                }
            }
        }

        Log.d(TAG, "getUniqueCellLocationAPI17 UID "+uid);
        return uid;
    }*/
}
