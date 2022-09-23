package com.jovistar.caltxt.network.voice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.jovistar.caltxt.activity.CaltxtPager;
import com.jovistar.caltxt.activity.Settings;
import com.jovistar.caltxt.activity.CaltxtToast;
import com.jovistar.caltxt.activity.SignupProfile;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.firebase.client.ConnectionFirebase;
import com.jovistar.caltxt.firebase.storage.DownloadService;
import com.jovistar.caltxt.mqtt.client.ConnectionMqtt;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.notification.NotificationUtils;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Logbook;
import com.jovistar.caltxt.phone.Searchbook;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XAd;
import com.jovistar.commons.bo.XMob;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

//import com.jovistar.caltxt.network.sms.SMSBroadcastReceiver;

public class CaltxtHandler /*implements PropertyChangeListener */ {
    private static final String TAG = "CaltxtHandler";

    static CaltxtHandler i;
    static boolean flushing = false;

    //	private ArrayList<String> caltxt_history;
    private String CALTXT_HISTORY_CURSOR = "INDEX";
    //temporaryStatusHolder should be as many as incoming calls --REWORK LOGIC FOR STATUS CHANGE ON IN/OUT CALL--
//	private static String temporaryStatusHolder = XMob.STRING_STATUS_AVAILABLE;
    private static HashMap<String, Long> inboundQ = new HashMap<String, Long>();// in bound calls
    private static HashMap<String, Long> outboundQ = new HashMap<String, Long>();// out bound calls
    //	Context mContext;
//	private long CALTXT_TIMEOUT = 15000;
//	public static long mRemotePID = 0;//to keep sender persistence id
//	public static long mLocalPID = 0;//to keep local persistence id (for msg delivery confirmation for ACK)
    Context context = null;

    private CaltxtHandler(Context context) {
        this.context = context;
//		loadCaltxtHistory();
    }

    public static CaltxtHandler get(Context context) {
        if (i == null)
            i = new CaltxtHandler(context);
        return i;
    }

    public void subscribeTopics() {
        ConnectionMqtt c = ConnectionMqtt.getConnection(context);
        if (c.isConnected() == false)
            return;
        //un-subscribe any topic subscribed earlier
        c.unsubscribe(Constants.CALTXT_MESSAGE_TOPIC + "#");
        c.unsubscribe(Constants.CALTXT_WILL_TOPIC + "#");

        //21-NOV-16: commented below IF; myProfile must set username based on verified numbers in Addressbook class
//		if(SignupProfile.isSIM1Verified(context)==true) {
        c.subscribe(Constants.CALTXT_MESSAGE_TOPIC + Addressbook.getInstance(context).getMyProfile().getUsername());
//		Log.v(TAG, "subscribed:"+Constants.CALTXT_MESSAGE_TOPIC + Addressbook.getInstance(context).getMyProfile().getUsername());
//		}

        //21-NOV-16: commented below IF; myProfile must set username based on verified numbers in Addressbook class
//		if(SignupProfile.isNumber2Verified(context)==true) {
        if (/*TelephonyInfo.getInstance(context)*/SignupProfile.isDualSIM() && Addressbook.getInstance(context).getMyProfile().getNumber2().length() > 0) {
            c.subscribe(Constants.CALTXT_MESSAGE_TOPIC + XMob.toFQMN(Addressbook.getInstance(context).getMyProfile().getNumber2(),
                    Addressbook.getInstance(context).getMyCountryCode()));
//			Log.v(TAG, "subscribed:"+Constants.CALTXT_MESSAGE_TOPIC + Addressbook.getInstance(context).getMyProfile().getNumber2());
        }
//			c.subscribe(Constants.CALTXT_MESSAGE_TOPIC + Addressbook.getMyProfile().getUsername());//for caltxt messages
        //		c.subscribe(Constants.CALTXT_VOICEMAIL_TOPIC + Addressbook.getMyProfile().getUsername());//for voice mail
        //		c.subscribe(Constants.CALTXT_ADMIN_ALERTS_TOPIC + Addressbook.getMyProfile().getUsername());//for admin messages
//		}
    }

    public XCtx peekCaltxt(int bound, String number) {
        number = XMob.toFQMN(number, Addressbook.getMyCountryCode());
        XCtx ctx = null;
        Long id = 0L;
        if (bound == Constants.CALL_TYPE_INBOUND)
            id = inboundQ.get(number);
        else
            id = outboundQ.get(number);
        if (id != null)
            ctx = (XCtx) Persistence.getInstance(context).get(id.longValue(), "XCtx");
        return ctx;
    }

    public synchronized XCtx popCaltxt(int bound, String number) {
        number = XMob.toFQMN(number, Addressbook.getMyCountryCode());
        XCtx ctx = null;
        Long id = 0L;
        if (bound == Constants.CALL_TYPE_INBOUND)
            id = inboundQ.get(number);
        else
            id = outboundQ.get(number);
        if (id != null)
            ctx = (XCtx) Persistence.getInstance(context).get(id.longValue(), "XCtx");
        if (bound == Constants.CALL_TYPE_INBOUND)
            inboundQ.remove(number);
        else
            outboundQ.remove(number);
        return ctx;
    }

    public synchronized long pushCaltxt(int bound, String number, XCtx ctx) {
        number = XMob.toFQMN(number, Addressbook.getMyCountryCode());
        if (bound == Constants.CALL_TYPE_INBOUND && inboundQ.get(number) != null) {
            /*entry for same number exists already
			* for existing caltxt, no corresponding call received yet, but one more caltxt arrived
			* previous call might have been cancelled by caller or didnt reach this callee
			* therefore log it as missed call
			*/
            XCtx c = popCaltxt(bound, number);//(XCtx) Logbook.get().get(inboundQ.get(number));
            c.setCallState(XCtx.IN_CALL_MISSED);
            Logbook.get(context).update(c);
//			Log.d(TAG, "pushCaltxt, missed call "+c.toString());
//				return inboundQ.get(number);
        } else if ((bound == Constants.CALL_TYPE_OUTBOUND && outboundQ.get(number) != null)) {
            //should never come here!! but put the same logic as above anyway
            XCtx c = popCaltxt(bound, number);//(XCtx) Logbook.get().get(inboundQ.get(number));
            c.setCallState(XCtx.OUT_CALL_DIALED_BUSY);
            Logbook.get(context).update(c);
//				return outboundQ.get(number);
        }

        long id = Logbook.get(context).prepend(ctx);
        if (bound == Constants.CALL_TYPE_INBOUND)
            inboundQ.put(number, id);
        else
            outboundQ.put(number, id);
        return id;
    }

    public ArrayList<String> getCaltxtHistory() {
        return loadCaltxtHistory();
    }

    private ArrayList<String> loadCaltxtHistory() {
        ArrayList<String> caltxt_history = new ArrayList<String>();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        // SharedPreferences sp = this.getSharedPreferences(CALTXT_HISTORY, 0);
//		if (caltxt_history == null) {
//			caltxt_history = new ArrayList<String>();
//		}

//		caltxt_history.clear();
//		int size = sp.getInt(CALTXT_HISTORY_CURSOR, 0);
        String tmp;
        for (int i = 0; i < Constants.MAX_HISTORY_SIZE; i++) {
            tmp = sp.getString("list_" + i, "");
            if (tmp.trim().length() > 0) {
//				caltxt_history.add(tmp);
                caltxt_history.add(0, tmp);
//				Log.d(TAG, "loadCaltxtHistory list_ "+i+" "+tmp);
            }
        }
//		Log.d(TAG, "loadCaltxtHistory list_ "+" LISTED "+caltxt_history.size());

        return caltxt_history;
    }

    public synchronized int findIndexOfCaltxtHistory(String txt) {
        int index = -1;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//		int size = sp.getInt(CALTXT_HISTORY_CURSOR, 0);
        String tmp;
        for (int i = 0; i < Constants.MAX_HISTORY_SIZE; i++) {
//		while(true) {
            tmp = sp.getString("list_" + i, "");
            if (tmp.equals(txt)) {
                index = i;
                break;
            }
        }

//		Log.d(TAG, "findIndexOfCaltxtHistory index "+index +", txt "+txt);
        return index;
    }

    public synchronized void removeFromCaltxtHistory(String txt) {
        int i = findIndexOfCaltxtHistory(txt);
        if (i < 0)
            return;

//		caltxt_history.remove(i);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        // SharedPreferences sp = this.getSharedPreferences(CALTXT_HISTORY, 0);
        SharedPreferences.Editor spe = sp.edit();
//		spe.remove(txt);
//		spe.putInt(CALTXT_HISTORY_CURSOR, caltxt_history.size());
        spe.putString("list_" + i, "");//put blank string for deleted txt
        spe.apply();
//		loadCaltxtHistory();
    }

    public synchronized void addToCaltxtHistory(String txt) {

        ArrayList<String> caltxt_history = loadCaltxtHistory();
        if (txt == null || caltxt_history.contains(txt) || txt.length() == 0) {
            return;
        }

//		int sz = caltxt_history.size();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        // SharedPreferences sp = this.getSharedPreferences(CALTXT_HISTORY, 0);
        SharedPreferences.Editor spe = sp.edit();
        int size = sp.getInt(CALTXT_HISTORY_CURSOR, 0);

        if (size >= Constants.MAX_HISTORY_SIZE) {
            spe.putString("list_0", txt);
            spe.putInt(CALTXT_HISTORY_CURSOR, 0 + 1);
//			caltxt_history.remove(0);
        } else {
            spe.putString("list_" + size, txt);
            spe.putInt(CALTXT_HISTORY_CURSOR, size + 1);
//			spe.putInt(CALTXT_HISTORY_CURSOR, sz);
        }

        spe.apply();
//		Log.e(TAG, "addToCaltxtHistory size, "+size);
    }

    private void processXCtx(XCtx ctx) {
		/* swap persistence id (remote and local). persistence id is always be local to sending
		 * mobile client. therefore, set persistence id to remote persistence id before local
		 * processing
		 */
        long tmp = ctx.getPersistenceId();
        ctx.setPersistenceId(ctx.getRemotePersistenceId());
        ctx.setRemotePersistenceId(tmp);

        String caller = null;
        if (Addressbook.getInstance(context).isItMe(ctx.getUsernameCaller())) {
            caller = XMob.toFQMN(ctx.getNumberCallee(), Addressbook.getMyCountryCode());
        } else {
            caller = ctx.getUsernameCaller();
        }

        // ignore duplicate message
        ArrayList<IDTObject> ctxs = Persistence.getInstance(context).getXCTXByRemotePID(tmp, caller);
        if (!ctxs.isEmpty()) {
            XCtx cc = (XCtx) ctxs.get(0);
            if (Addressbook.getInstance(context).isItMe(cc.getUsernameCaller())) {
                // caller is me
                if (ctx.getAck().equals(cc.getAck())) {
                    // ack already received! duplicate message
//					Log.d(TAG, "ack already received! duplicate message cc "+cc);
                    Intent resultIntent = new Intent(context, CaltxtPager.class);
                    resultIntent.putExtra("message", cc.getPersistenceId());
                    NotificationUtils notificationUtils = new NotificationUtils(context);
//					notificationUtils.showNotificationMessage("Duplicate ack", cc.toString(), Long.toString(Calendar.getInstance().getTimeInMillis()), resultIntent);
                    return;
                }
            } else {
                // caller is not me, caltxt already received
                if (ctx.getCaltxt().equals(cc.getCaltxt())) {
//					Log.d(TAG, "Caller is not me, caltxt already received cc " + cc);
                    Intent resultIntent = new Intent(context, CaltxtPager.class);
                    resultIntent.putExtra("message", cc.getPersistenceId());
                    NotificationUtils notificationUtils = new NotificationUtils(context);
//					notificationUtils.showNotificationMessage("Duplicate caltxt", cc.toString(), Long.toString(Calendar.getInstance().getTimeInMillis()), resultIntent);
                    return;
                }
            }
        }

        if (ctx.getCaltxtEtc().length() > 0 && ctx.getCaltxtEtc().startsWith("http")) {
            // Kick off DownloadService to download the file
            Intent intent = new Intent(context.getApplicationContext(), DownloadService.class)
                    .putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, ctx.getCaltxtEtc())
                    .setAction(DownloadService.ACTION_DOWNLOAD);
            context.getApplicationContext().startService(intent);
//			Log.d(TAG, "getCaltxtEtc download start "+ctx.getCaltxtEtc());
        } else if (ctx.getAckEtc().length() > 0 && ctx.getAckEtc().startsWith("http")) {
            // Kick off DownloadService to download the file
            Intent intent = new Intent(context.getApplicationContext(), DownloadService.class)
                    .putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, ctx.getAckEtc())
                    .setAction(DownloadService.ACTION_DOWNLOAD);
            context.getApplicationContext().startService(intent);
//			Log.d(TAG, "getAckEtc download start "+ctx.getAckEtc());
        }

        if (ctx.getCallState() == XCtx.OUT_CALL) {//INCOMING CONTEXT CALL
            processIncomingCaltxtCall(ctx);
        } else if (ctx.getCallState() == XCtx.IN_CALL_REPLY ||//CALL ACK
                ctx.getCallState() == XCtx.IN_MESSAGE_REPLY
                || ctx.getCallState() == XCtx.IN_CALL_REJECT_AUTOREPLY) {//MESSAGE REPLY
//			Log.i(TAG, "processIncomingCaltxt IN_CALL_REPLY");
            processIncomingReply(ctx);
        } else if (ctx.getCallState() == XCtx.OUT_MESSAGE) {//MESSAGE COMING
            processIncomingCaltxtMessage(ctx);
        } else if (ctx.getCallState() == XCtx.OUT_MESSAGE_ADMIN//ADMIN MESSAGE
                || ctx.getCallState() == XCtx.OUT_MESSAGE_TRIGGER) {//TRIGGER MESSAGE
            processIncomingAdminMessage(ctx);
        } else {
            Log.e(TAG, "processIncomingCaltxt " + ctx.toString());
        }
    }

    public synchronized void processIncomingCaltxt(XCtx ctx) {
        processXCtx(ctx);
    }

    public synchronized void processIncomingCaltxt(String topic, String msg) {
        ConnectionMqtt c = ConnectionMqtt.getConnection(context);
//		if(c.isConnected()==false) {
//
//		}
//		IDTObject iobj=null;

        Log.d(TAG, "processIncomingCaltxt, topic, " + topic + ", msg, " + msg);

        if (XCtx.isXCtx(msg)) {//XCtx
//			Log.d(TAG, "processIncomingCaltxt XCtx, "+msg);
            IDTObject iobj = new XCtx();
            XCtx ctx = (XCtx) iobj;
            ctx.init(msg);

            processXCtx(ctx);
/*			long tmp = ctx.getPersistenceId();
			ctx.setPersistenceId(ctx.getRemotePersistenceId());
			ctx.setRemotePersistenceId(tmp);

			if(ctx.getCallState()==XCtx.OUT_CALL) {//INCOMING CONTEXT CALL
				processIncomingCaltxtCall(ctx);
			} else if(ctx.getCallState()==XCtx.IN_CALL_REPLY ||//CALL ACK
					ctx.getCallState()==XCtx.IN_MESSAGE_REPLY
					|| ctx.getCallState()==XCtx.IN_CALL_REJECT_AUTOREPLY) {//MESSAGE REPLY
				Log.i(TAG, "processIncomingCaltxt IN_CALL_REPLY");
				processIncomingReply(ctx);
			} else if(ctx.getCallState()==XCtx.OUT_MESSAGE) {//MESSAGE COMING
				processIncomingCaltxtMessage(ctx);
			} else if(ctx.getCallState()==XCtx.OUT_MESSAGE_ADMIN) {//ADMIN MESSAGE
				processIncomingAdminMessage(ctx);
			} else {
				Log.e(TAG, "processIncomingCaltxt "+ctx.toString());
			}
*/
            // if new number, discover
            if (!Addressbook.getInstance(context).isCaltxtContact(ctx.getUsernameCaller())) {//does not have contact
                Connection.get().submitPingContact(Addressbook.getInstance(context).getMyProfile().getUsername(),
                        ctx.getUsernameCaller());

                if (!Addressbook.getInstance(context).isRegistered(ctx.getUsernameCaller())) {//does not have in Search list also
                    XMob mob = new XMob();
                    mob.setUsername(XMob.toFQMN(ctx.getUsernameCaller(), Addressbook.getMyCountryCode()));
                    mob.setName(ctx.getNameCaller());
                    mob.setStatusOffline();
                    Searchbook.get(context).prepend(mob);
                }
            }
        } else if (msg.startsWith(String.valueOf(XMob.serialVersionUID))) {//3^JOHN MILLER^918860090854^AVAILABLE^1234567(icon status)

            XMob status = new XMob();//(XMob) iobj;
            status.init(msg);
            status.setModified(Calendar.getInstance().getTimeInMillis());

            if (Addressbook.isItMe(status.getUsername())) {
                // self connection test!
                ConnectionFirebase.setConnected();
            } else {

                if (status.isOffline()) {
                    // offline (received WILL)
                    status.setStatusOffline();

                    // un-subscribe to his WILL
                    c.unsubscribe(Constants.CALTXT_WILL_TOPIC + status.getNumber());
                } else {
                    status.setStatusOnline(status.getStatus());

                    if (Settings.iam_discoverable_by_anyone) {
                        // send back YOUR ACK status to sender, if discovery is public
                        Connection.get().submitAckPingContact(topic.substring(topic.lastIndexOf("/") + 1), status.getNumber());

                        // ping sim2 also, if exists
                        // commented 26-JUL-17, why ping again the same phone, sync sim1, sim2 both together in address book
					/*if(status.getNumber2()!=null && status.getNumber2().length()>0) {
						c.submitPingContact(Addressbook.getInstance(context).getMyProfile().getUsername(),
								XMob.toFQMN(status.getNumber2(), Addressbook.getMyCountryCode()));
					}*/

                    }

                    subscribeWillAndUpdateAddressbook(msg);
                }
            }

//			RebootService.getConnection().flushUndeliveredMessages();

        } else if (msg.startsWith(String.valueOf(XMob.serialVersionUID + 1))) {//4^JOHN MILLER^918860090854^AVAILABLE ---- (4) is ACK for (3)
            // ack of PING arrived, subscribe will and update address book
            subscribeWillAndUpdateAddressbook(msg);
        } else {
            Log.e(TAG, "processIncomingCaltxt, UNKNOWN message received!");
        }

        Connection.get().addAction(Constants.messageArrivedProperty, topic, msg);
    }

    private void subscribeWillAndUpdateAddressbook(String payload) {

        ConnectionMqtt c = ConnectionMqtt.getConnection(context);

        XMob status = new XMob();
        status.init(payload);
        status.setModified(Calendar.getInstance().getTimeInMillis());
        status.setStatusOnline(status.getStatus());//online
        status.setModified(Calendar.getInstance().getTimeInMillis());

        ArrayList<XMob> v = new ArrayList<XMob>();
        v.add(status);

        // contact exists in address book?
        if (Addressbook.getInstance(context).isContact(status.getNumber())) {
            // subscribe to his WILL, update address book
            c.subscribe(Constants.CALTXT_WILL_TOPIC + status.getNumber());
            Addressbook.getInstance(context).syncAddressbookAndDB(v);
        } else {
            // not a contact, put in search list
            if (Addressbook.getInstance(context).isRegistered(status.getUsername())) {
                // does have in Search list, update it!
                Searchbook.get(context).update(v);
            } else {
                // don't have in Search list, add it!
                Searchbook.get(context).add(v);
            }
        }
    }

    private void processIncomingReply(XCtx rpy) {//call or message reply
        if (rpy.getCallState() == XCtx.IN_CALL_REPLY
                || rpy.getCallState() == XCtx.IN_CALL_REJECT_AUTOREPLY) {
            rpy.setCallState(XCtx.IN_CALL_REPLY_RECEIVED);
        } else {
            rpy.setCallState(XCtx.IN_MESSAGE_REPLY);
        }

        if (rpy.getPersistenceId() <= 0) {
            Log.i(TAG, "processIncomingReply getPersistenceId zero");
            rpy = Logbook.get(context).merge(rpy, Constants.CALTXT_MSG_LAG);
        } else {
            rpy = Logbook.get(context).update(rpy);
        }

        if (rpy.getRecvToD() + Constants.CALTXT_RPY_LAG > System.currentTimeMillis()) {
            Intent it = new Intent(context, CaltxtToast.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            it.putExtra("IDTOBJECT", rpy);
            it.putExtra("ACTION", Constants.TOAST_ACTION_VIEW);
            context.startActivity(it);
//			ActivityCaltxt.showCaltxt(rpy, true);
        } else {
            Notify.notify_caltxt_missed_reply(context, rpy.getNameCallee(), rpy.getAck(), "", rpy.getRecvToD());
        }

        Notify.playNotification(context);
    }

    private void processIncomingCaltxtMessage(XCtx ctx) {
        ctx.setCallState(XCtx.IN_MESSAGE_RECEIVED);
        Log.i(TAG, "processIncomingCaltxtMessage " + ctx.toString());

        Logbook.get(context).prepend(ctx);
        if (ctx.getRecvToD() + Constants.CALTXT_MSG_LAG > System.currentTimeMillis()) {
            Intent it = new Intent(context, CaltxtToast.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            it.putExtra("IDTOBJECT", ctx);
            it.putExtra("ACTION", Constants.TOAST_ACTION_VIEW);
            context.startActivity(it);
//			ActivityCaltxt.showCaltxt(ctx, true);
        } else {
            Notify.notify_caltxt_missed_text(context, ctx.getNameCaller(),
                    ctx.getCaltxt(), "", ctx.getRecvToD());
        }

        Notify.playNotification(context);
    }

    private void processIncomingAdMessage(XAd ad) {
        Log.i(TAG, "processIncomingAdMessage " + ad.toString());

        Logbook.get(context).prepend(ad);
    }

    private void processIncomingAdminMessage(XCtx adm) {
        Log.i(TAG, "processIncomingAdminMessage " + adm.toString());
        if (adm.getCallState() == XCtx.OUT_MESSAGE_TRIGGER) {
            Notify.notify_caltxt_trigger(context, adm.getNameCaller(), adm.getCaltxt(), "", adm.getCity());
        } else {
            Notify.notify_caltxt_alert(context, adm.getNameCaller(), adm.getCaltxt(), "", adm.getCity());
        }
        Notify.playNotification(context);

        Logbook.get(context).prepend(adm);
    }

    private void processIncomingCaltxtCall(XCtx ctx) {
        Log.i(TAG, "processIncomingCaltxtCall " + ctx.toString());
        ctx.setCallState(XCtx.IN_CALL);

//		if(CallHandler.get().isIncomingCallInProgress(ctx.getNameCaller())) {
        if (CallManager.getInstance().isIncomingCallInProgress(ctx.getUsernameCaller())
                || CallManager.getInstance().isIncomingCallInProgress(ctx.getUsername2Caller())) {

            if (CallManager.getInstance().isIncomingCallInProgress(ctx.getUsernameCaller())) {
                //called from SIM1
                XCtx c = peekCaltxt(Constants.CALL_TYPE_INBOUND, ctx.getUsernameCaller());
                c.setCaltxt(ctx.getCaltxt());
                if (ctx.getUsername2Caller().length() > 0) {
                    c.setUsername2Caller(ctx.getUsername2Caller());
                }
                Logbook.get(context).update(c);

                // update PID for CaltxtToast to fetch it
                ctx.setPersistenceId(c.getPersistenceId());
                ctx.setRemotePersistenceId(c.getRemotePersistenceId());
//				pushCaltxt(Constants.CALL_TYPE_INBOUND, ctx.getUsernameCaller(), ctx);
            } else if (CallManager.getInstance().isIncomingCallInProgress(ctx.getUsername2Caller())) {
                //called from SIM2
                XCtx c = peekCaltxt(Constants.CALL_TYPE_INBOUND, ctx.getUsername2Caller());
                c.setCaltxt(ctx.getCaltxt());
                if (ctx.getUsername2Caller().length() > 0) {
                    c.setUsername2Caller(ctx.getUsername2Caller());
                }
                Logbook.get(context).update(c);

                // update PID for CaltxtToast to fetch it
                ctx.setPersistenceId(c.getPersistenceId());
                ctx.setRemotePersistenceId(c.getRemotePersistenceId());
//				pushCaltxt(Constants.CALL_TYPE_INBOUND, ctx.getUsername2Caller(), ctx);
            }

			/* call in progress from this number... */
//			ActivityCaltxt.showCaltxtAsToast(ctx);
            Intent it = new Intent(context, CaltxtToast.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            it.putExtra("IDTOBJECT", ctx);
            it.putExtra("ACTION", Constants.TOAST_ACTION_VIEW);
            context.startActivity(it);
//			ActivityCaltxt.showCaltxt(ctx, false);
            Log.i(TAG, "processIncomingCaltxtCall isIncomingCallInProgress " + ctx.toString());
        } else {
			/* no call in progress from this number...
			*  case-1: call already ended within last 10 secs or caller cancelled call
			*  case-2: call may be coming anytime. caltxt reached earlier than call
			*  */
            if (ctx.getRecvToD() + Constants.CALTXT_MSG_LAG < System.currentTimeMillis()) {
                Log.i(TAG, "processIncomingCaltxtCall past CALTXT_MSG_LAG getRecvToD " + ctx.getRecvToD()
                        + ", ToD " + System.currentTimeMillis());
				/* caltxt received is delayed by more than 10 secs. possible reasons..
				 * 1. caller might have cancelled the call soon after initiation. call will never come
				 * 2. callee was not reachable when call was made; caltxt was sent anyway. call will never come
				 * 3. call ended before caltxt arrived. call will never come
				 * assumption: it will not take more than 10 sec for call to reach callee after caller dialed number
				 */
                ctx.setCallState(XCtx.IN_CALL_MISSED);
                Logbook.get(context).merge(ctx, Constants.CALTXT_MSG_LAG);
            } else {
                Log.i(TAG, "processIncomingCaltxtCall call coming");
				/* caltxt received within last 10 seconds, call may be coming. add in the caltxt queue/log
				* if call does not come, this caltxt will be replaced by new caltxt from same number
				* when next caltxt arrives
				*/
                ctx.setCallState(XCtx.IN_CALL);
                pushCaltxt(Constants.CALL_TYPE_INBOUND, ctx.getUsernameCaller(), ctx);
                if (ctx.getUsername2Caller().length() > 0) {
                    //add SIM2 number also, who know calls is from this number or primary
                    pushCaltxt(Constants.CALL_TYPE_INBOUND, ctx.getUsername2Caller(), ctx);
                }
            }
        }
    }

    //MESSAGE to a contact
    public synchronized int initiateMessage(XMob mob, String message, String url, short priority) {
        if (Addressbook.isItMe(mob.getUsername())) {
            return 0;
        }

        Log.i(TAG, "initiateMessage, mob:" + mob + " message:" + message);
		/* prepare caltxt message */
        XCtx ctx = new XCtx();
        ctx.setCallerSelf(Addressbook.getInstance(context).getMyProfile(), Addressbook.getInstance(context).getMyCountryCode());// set myself as caller
        ctx.setNameCallee(mob.getName());
        ctx.setNumberCallee(mob.getNumber());
        ctx.setCallPriority(priority);
        ctx.setRecvToD(System.currentTimeMillis());//set call initiate time to NOW
        ctx.setStartToD(System.currentTimeMillis());//set call start time to NOW
        ctx.setCaltxt(message);
        ctx.setCallState(XCtx.OUT_MESSAGE);
        ctx.setCaltxtEtc(url);

//		addToCaltxtHistory(message);

        pushCaltxt(Constants.CALL_TYPE_OUTBOUND, mob.getUsername(), ctx);
        int ret = sendXCtx(mob.getUsername(), ctx);
        popCaltxt(Constants.CALL_TYPE_OUTBOUND, mob.getUsername());

        return ret;
    }

    //CALL contact with a context
    public synchronized void initiateContextCall(final XMob mob, String caltxt, String url, short priority) {

        if (!((Caltxt) context.getApplicationContext()).hasTelephony() || !((Caltxt) context.getApplicationContext()).hasSim()
            || mob.getUsername().length()==0 || mob.getUsername().equals(Addressbook.getInstance(context).getMyProfile().getUsername())) {
//			Notify.toast(context, context.getString(R.string.caltxt_call_telephony_not_registered), Toast.LENGTH_SHORT);
//			return 0;
            return;
        }

		/* prepare caltxt message */
        final XCtx ctx = new XCtx();
        ctx.setCallerSelf(Addressbook.getInstance(context).getMyProfile(), Addressbook.getInstance(context).getMyCountryCode());// set myself as caller
        ctx.setNameCallee(mob.getName());
        ctx.setNumberCallee(mob.getNumber());
        ctx.setCallPriority(priority);
        ctx.setRecvToD(System.currentTimeMillis());//set call recieve / initiate time to NOW (as initiated)
        ctx.setStartToD(System.currentTimeMillis());//set call start time to NOW
        ctx.setCaltxt(caltxt);
//		addToCaltxtHistory(caltxt);
        ctx.setCallState(XCtx.OUT_CALL);
        ctx.setCaltxtEtc(url);

        pushCaltxt(Constants.CALL_TYPE_OUTBOUND, mob.getUsername(), ctx);

        int ret = 0;
        // show Toast for registered or unregistered callees. Normal call is initiated in CaltxtToast only
//		if(mob.isRegistered()) {

			/* SMS is disabled for now 26/MAR/2016
			if(mob.isOffline() && Settings.delivery_by_sms) {
				sendXCtxBySMS(mob.getUsername(), ctx);
				ret = 1;
			} else {*/
//			if(RebootService.getConnection(context).isConnected()
//					|| Settings.isSMSEnabled(context)) {
        // wait for message to be sent and then call
        Intent it = new Intent(context, CaltxtToast.class);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        it.putExtra("IDTOBJECT", ctx);
        it.putExtra("ACTION", Constants.TOAST_ACTION_SEND_VIEW);
        context.startActivity(it);

//			} else {
        // offline, and sms not enabled. just call
//				initiateNormalCall(mob.getNumber(), context);
//			}
//			sendXCtx(mob.getUsername(), ctx);
//			}
        Log.i(TAG, "initiateContextCall, mob: " + mob);
		/* show Toast for registered or unregistered callees. Normal call is initiated in CaltxtToast only
		} else {
           Log.i(TAG, "initiateContextCall, not registered mob: "+mob);
			initiateNormalCall(mob.getNumber(), context);*/
//		}

//		return ret;
    }

    /*
     * number format types:
     *
     * 990-981-5610
     * +91-9909815610
     * 09909815610
     * 9909815610
     * 5678
     * *111*#
     * 18008993456
     * +1-512-952-9062
     */
    public void initiateNormalCall(String number, Context context) {
//		Log.i(TAG, "initiateNormalCall, number:"+number);
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        //commented 1-APR-2017, call original number as dialed by user
		/*if(number.length()>10 && !number.startsWith("+") && !number.startsWith("0")) {
			try {
				Float.valueOf(number);
				number = "+" + number;
			}catch(NumberFormatException e) {
				Log.e(TAG, "initiateNormalCall, NumberFormatException:"+e);
			}
		}*/
        callIntent.setData(Uri.parse("tel:" + number));
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        int ret = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);
        if (ret != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Log.i(TAG, "initiateNormalCall, tel:" + number);
        context.startActivity(callIntent);
    }

    //REPLY contact MESSAGE
    public synchronized int initiateMessageReply(XCtx rpy, String caltxt, String url) {
        if (rpy.getCallState() == XCtx.IN_MESSAGE_RECEIVED
                || rpy.getCallState() == XCtx.IN_MESSAGE_REPLY
                || rpy.getCallState() == XCtx.OUT_MESSAGE) {
            rpy.setCallState(XCtx.IN_MESSAGE_REPLY);
        } else {
            rpy.setCallState(XCtx.IN_CALL_REPLY);
        }

        rpy.setAck(caltxt);
        rpy.setAckEtc(url);
        rpy.setNameCallee(Addressbook.getInstance(context).getMyProfile().getName());
        Logbook.get(context).update(rpy);

        return sendXCtx(rpy.getUsernameCaller(), rpy);
    }

    /* send caltxt via SMS or MQTT based on user preference
     * 0 = message queued, not sent
     * 1 = message sent through SMS
     * 2 = message sent through Mqtt
     * 3 = message sent through Firebase
     * */
    public int sendXCtx(String to, XCtx ctx) {
        if (Addressbook.isItMe(to)) {
            Log.e(TAG, "sendXCtx, to self!!!!!!!:" + to + ", ctx:" + ctx);
            return 0;
        }

        to = XMob.toFQMN(to, Addressbook.getMyCountryCode());
        Log.i(TAG, "sendXCtx, to:" + to + ", ctx:" + ctx);

//		if(Addressbook.getInstance(context).isRegistered(to)) {//is a registered contact (need not be a contact)
        if (Addressbook.getInstance(context).isRegisteredAndConnected(to)) {
            Log.i(TAG, "sendXCtx, publishXCtx  MQTT");
            Connection.get().publishXCtx(to, ctx.toString(), ctx.getPersistenceId());
//			ctx.setSent();
            ctx.setCallOptions((short) 0);//reset
            ctx.setCallPriority(XCtx.PRIORITY_NORMAL);
            ctx.setCaltxtCall();
            Logbook.get(context).update(ctx);
            return 2;
        } else if (Addressbook.getInstance(context).isRegistered(to)) {
            // send empty message (missed call alert) by Internet only, NOT BY SMS
            // send caltxt via Firebase since Caltxt on the other side is'nt running or connected to Internet
            // and user does not want to send via SMS
            // send caltxt via SMS, for registered but not connected user, and for unregistered user
            // 04-Jan-19, commented due to Google play policy change
            /*if (Settings.isSMSEnabled(context) && ctx.getCaltxt().trim().length() > 0) {
                sendXCtxAsPlainTextBySMS(to, ctx);
                return 1;
            }*/

            Log.i(TAG, "sendXCtx, publishXCtx Firebase");
//			RebootService.getConnection(context).publishXCtxFirebase(to, ctx.toString(), ctx.getPersistenceId());
            ConnectionFirebase.publishXCtx(to, ctx.toString(), ctx.getPersistenceId());
//			ctx.setSent();
            ctx.setCallOptions((short) 0);//reset
            ctx.setCallPriority(XCtx.PRIORITY_NORMAL);
            ctx.setCaltxtCall();
            Logbook.get(context).update(ctx);
            return 3;
        } else {
            Log.i(TAG, "sendXCtx, publishXCtx not registered");
            // send caltxt via SMS, for registered but not connected user, and for unregistered user
            // moved to above if else block
//            if (Settings.isSMSEnabled(context) && ctx.getCaltxt().trim().length() > 0) {
//                sendXCtxAsPlainTextBySMS(to, ctx);
//                return 1;
//            }
        }

        return 0;
    }

    /* COMMENTED FOR NOW - 26/MAR/2016 */
    //to send caltxt to offline registered users
/*	private void sendXCtxBySMS(String to, XCtx ctx) {

		if(Settings.delivery_by_sms && Addressbook.get().isRegistered(to)) {
			ctx.setSent();
			Logbook.get().update(ctx);
			SMSBroadcastReceiver.sendSMSMessage(to, ctx.toString());

			Log.d(TAG, "sendXCtxBySMS "+ctx.toString());
		}
	}
*/
    //to send caltxt to non-registered users
/*    private void sendXCtxAsPlainTextBySMS(String to, XCtx ctx) {
        to = XMob.toFQMN(to, Addressbook.getMyCountryCode());

        if (Settings.isSMSEnabled(context)) {
			//to identify SMS delivery (updateDelivered)
            String text = ctx.getCaltxt();// +" pid:"+ctx.getPersistenceId();
            long pid = ctx.getPersistenceId();
            if (ctx.getCallState() == XCtx.IN_MESSAGE_REPLY || ctx.getCallState() == XCtx.IN_CALL_REPLY) {
                text = ctx.getAck();
            }
            ctx.setCallOptions((short) 0);//reset
            ctx.setCallPriority(XCtx.PRIORITY_NORMAL);
            ctx.setCaltxtCall();
//			ctx.setSent();
            Logbook.get(context).update(ctx);
            SMSBroadcastReceiver.sendSMSMessage(context, pid, to, text);

//			Log.d(TAG, "sendAsPlainTextBySMS "+text);
        }
    }
*/
    //ACK contact CALL with a message
    public synchronized void sendXCtxReply(XCtx ack, String rpy) {

        ack.setNameCallee(Addressbook.getInstance(context).getMyProfile().getName());

        if (Addressbook.getInstance(context).isRegistered(ack.getUsernameCaller())) {
            ack.setAck(rpy);
            Logbook.get(context).update(ack);

            sendXCtx(ack.getUsernameCaller(), ack);
        } else {
            //is NOT a Caltxt contact, send plain SMS to non Caltxt phone numbers
            //if remote phone is offline, and sms disabled can not send ACK
            // 04-Jan-19 commented SMS send/receive, Google play policy change (Use of SMS or Call Log permission groups)
            /*if (Addressbook.getInstance(context).isItMe(ack.getUsernameCaller())) {
                sendXCtxReplyAsPlainTextBySMS(XMob.toFQMN(ack.getNumberCallee(), Addressbook.getMyCountryCode()), ack,
                        rpy);
            } else {
                sendXCtxReplyAsPlainTextBySMS(ack.getUsernameCaller(), ack,
                        rpy);
            }*/
        }
    }

    /*
        private void sendXCtxReplyBySMS(String to, XCtx ctx, String rpy) {
            if(Settings.delivery_by_sms && Addressbook.get().isRegistered(to)) {
                ctx.setCallState(XCtx.IN_CALL_REPLY);
                ctx.setAck(rpy);
                ctx.setSent();
                Logbook.get().update(ctx);
                SMSBroadcastReceiver.sendSMSMessage(to, ctx.toString());

                Log.d(TAG, "sendReplyCtxBySMS "+ctx.toString());
            }
        }

    private void sendXCtxReplyAsPlainTextBySMS(String to, XCtx ctx, String rpy) {
        to = XMob.toFQMN(to, Addressbook.getMyCountryCode());
//		if(Settings.delivery_by_sms) {
        if (Settings.isSMSEnabled(context)) {
			//to identify SMS delivery (updateDelivered)
            String text = rpy;// +" pid:"+ctx.getPersistenceId();
            ctx.setNameCallee(Addressbook.getInstance(context).getMyProfile().getName());
            ctx.setAck(rpy);
            ctx.setCallOptions((short) 0);//reset
            ctx.setCallPriority(XCtx.PRIORITY_NORMAL);
            ctx.setCaltxtCall();
//			ctx.setSent();
            Logbook.get(context).update(ctx);
            SMSBroadcastReceiver.sendSMSMessage(context, ctx.getPersistenceId(), to, text);

//			Log.d(TAG, "sendReplyAsPlainTextBySMS "+text);
        }
    }*/

    //ALERT to all
    public synchronized void publishAlert(String to, String message, String title, String url) {
        if (message == null || message.length() == 0 || Addressbook.isItMe(to))
            return;

        to = XMob.toFQMN(to, Addressbook.getMyCountryCode());

		/* prepare caltxt message */
        final XCtx adm = new XCtx();
        adm.setCallerSelf(Addressbook.getInstance(context).getMyProfile(), Addressbook.getInstance(context).getMyCountryCode());
        adm.setNumberCallee(to);
        adm.setNameCallee(Addressbook.getInstance(context).getName(to));
        adm.setCaltxt(message);
        adm.setOccupation(title);
        adm.setCity(url);
        adm.setCallState(XCtx.OUT_MESSAGE_ADMIN);

//		addToCaltxtHistory(message);

        pushCaltxt(XCtx.OUT_MESSAGE_ADMIN, to, adm);
//		pushCaltxt(Constants.CALL_TYPE_OUTBOUND, "0000000000", ctx);
        sendXCtx(to, adm);
//		RebootService.getConnection().publishAlert(to, adm.toString());
//		popCaltxt(Constants.CALL_TYPE_OUTBOUND, "0000000000");
        popCaltxt(XCtx.OUT_MESSAGE_ADMIN, to);
    }

    public synchronized void publishTriggerAlert(String to, String message, String title, String url) {
        if (message == null || message.length() == 0)
            return;

        to = XMob.toFQMN(to, Addressbook.getMyCountryCode());

		/* prepare caltxt message */
        final XCtx adm = new XCtx();
        adm.setCallerSelf(Addressbook.getInstance(context).getMyProfile(), Addressbook.getInstance(context).getMyCountryCode());
        adm.setNameCallee(Addressbook.getInstance(context).getName(to));
        adm.setNumberCallee(to);
//		adm.setNameCallee("Me");
        adm.setCaltxt(message);
        adm.setOccupation(title);
        adm.setCity(url);
        adm.setCallState(XCtx.OUT_MESSAGE_TRIGGER);

//		addToCaltxtHistory(message);

        pushCaltxt(XCtx.OUT_MESSAGE_TRIGGER, to, adm);
//		pushCaltxt(Constants.CALL_TYPE_OUTBOUND, "0000000000", ctx);
        sendXCtx(to, adm);
//		RebootService.getConnection().publishAlert(to, adm.toString());
//		popCaltxt(Constants.CALL_TYPE_OUTBOUND, "0000000000");
        popCaltxt(XCtx.OUT_MESSAGE_TRIGGER, to);
    }

    public synchronized void updateDelivered(long pid/*0 for XCtx, valid for plain text*/, String message_string) {
        XCtx new_ctx = null;
        Log.d(TAG, "updateDelivered Deliverd pid " + pid + ", " + message_string);
        if (XCtx.isXCtx(message_string)) {
            XCtx ctx = new XCtx();
            ctx.init(message_string);
            // 08-MAR-17, get from the memory Log so that update is visible right there!
            // if memory Log does not have entry, get() method fetches from Persistence
            new_ctx = Logbook.get(context).get(ctx.getPersistenceId());
            if (new_ctx == null) {
                new_ctx = (XCtx) Persistence.getInstance(context).get(ctx.getPersistenceId(), "XCtx");
            }
        } else {//others, plain SMS
//			String tokens[] = message_string.split("pid:",-1);
//			int i = 0;
//			if(tokens.length>0) {
//				String text = tokens[i++];
//				Log.d(TAG, "updateDelivered, "+text);
//			}
//			if(tokens.length>1) {
//				String pid = tokens[i++];
            Log.d(TAG, "updateDelivered, " + pid);
            // 08-MAR-17, get from the memory Log so that update is visible right there!
            // if memory Log does not have entry, get() method fetches from Persistence
            new_ctx = Logbook.get(context).get(pid);
            if (new_ctx == null) {
                new_ctx = (XCtx) Persistence.getInstance(context).get(pid, "XCtx");
            }
        }

        if (new_ctx != null) {
            new_ctx.setDelivered();
//			Persistence.getInstance(context).update(new_ctx);
            Logbook.get(context).update(new_ctx);
            Log.d(TAG, "updateDelivered, " + new_ctx.toString());
        }
    }

    public synchronized void updateSent(long pid, String message_string) {
        XCtx new_ctx = null;
        Log.d(TAG, "updateSent pid " + pid + ", " + message_string);
        if (XCtx.isXCtx(message_string)) {
            XCtx ctx = new XCtx();
            ctx.init(message_string);
            new_ctx = Logbook.get(context).get(ctx.getPersistenceId());
            if (new_ctx == null) {
                new_ctx = (XCtx) Persistence.getInstance(context).get(ctx.getPersistenceId(), "XCtx");
            }
        } else {//others, plain SMS
//			String tokens[] = message_string.split("pid:",-1);
//			int i = 0;
//			if(tokens.length>0) {
//				String text = tokens[i++];
//				Log.d(TAG, "updateDelivered, "+text);
//			}
//			if(tokens.length>1) {
//				String pid = tokens[i++];
            Log.d(TAG, "updateSent, " + pid);
            new_ctx = Logbook.get(context).get(pid);
            if (new_ctx == null) {
                // if not loaded in memory, get from persistence
                new_ctx = (XCtx) Persistence.getInstance(context).get(pid, "XCtx");
            }
        }

        if (new_ctx != null) {
            new_ctx.setSent();
//			Persistence.getInstance(context).update(new_ctx);
            Logbook.get(context).update(new_ctx);
            Log.d(TAG, "updateSent, " + new_ctx.toString());
        }
    }
	/*
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		String new_msg = (String) event.getNewValue();
		String number = (String) event.getOldValue();

		Log.v(TAG, propertyName+ " propertyChange, old: "+number+ ", new: "+new_msg);

		if (propertyName.equals(Constants.messageErrorProperty) ||
				propertyName.equals(Constants.smsDeliveredProperty) ||
				propertyName.equals(Constants.messageDeliveredProperty)) {
			//only for Caltxt, not messages
			if(new_msg.startsWith(String.valueOf(XCtx.serialVersionUID++'^'))) {
				XCtx ctx = new XCtx();
				ctx.init(new_msg);
				number = ctx.getUsernameCallee();
			}
		}

		if (propertyName.equals(Constants.smsDeliveredProperty) ||
				propertyName.equals(Constants.messageDeliveredProperty)) {

//			RebootService.getConnection().removeChangeListener(this);
			final XCtx ctx = CaltxtHandler.get().peekCaltxt(Constants.CALL_TYPE_OUTBOUND, number);
			if(ctx!=null) {
				initiateNormalCall(number, context);
			}
//		} else if (propertyName.equals(Constants.messageErrorProperty)) {
//			Toast.makeText(Globals.getCustomAppContext(), "Message send failed", Toast.LENGTH_LONG).show();
//
//			RebootService.getConnection().removeChangeListener(this);
//			initiateNormalCall(number, context);
		}
	}*/

    public void flushUndeliveredMessages() {
        if (/*flushing*/true)//temporarily disable 16-JAN-17 - to test
            return;
        //send not-delivered messages
        new Thread(new Runnable() {
            @Override
            public void run() {
                flushing = true;

                try {
					/*
					 * sleep for 10 seconds before initiating flush, so that 
					 * any pending deliveryComplete notification is received 
					 * before sending message again
					 */
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ArrayList<IDTObject> a = Logbook.get(context).getList();
                for (int i = 0; i < a.size(); i++) {
                    XCtx ctx = (XCtx) a.get(i);
                    long sentAgo = Calendar.getInstance().getTimeInMillis() - ctx.getStartToD();
//					if(ctx.isCaltxtCall() && !ctx.isDelivered()/* && ctx.isSent()*/) {
                    if (ctx.isCaltxtCall() && (/*sentAgo < Constants.firebase_defaultMessageTTL
					/*!ctx.isDelivered() ||*/ !ctx.isSent())) {
                        Log.i(TAG, "flushUndeliveredMessages:" + ctx.toString());
//						if(ctx.getUsernameCaller().equals(Addressbook.getMyProfile().getUsername())) {
                        if (Addressbook.getInstance(context).isItMe(ctx.getUsernameCaller())) {
//							publishMessage(ctx.getUsernameCallee(), ctx.toString());
//							if(Addressbook.get().isRegistered(ctx.getUsernameCallee())) {
                            sendXCtx(XMob.toFQMN(ctx.getNumberCallee(), Addressbook.getMyCountryCode()), ctx);
//							} else {
//								ctx.setNotSent();//reset SENT flag (previous version default flag)
//								Logbook.get().update(ctx);
//							}
                        } else {
//							publishMessage(ctx.getUsernameCaller(), ctx.toString());
//							if(Addressbook.get().isRegistered(ctx.getUsernameCaller())) {
                            sendXCtx(ctx.getUsernameCaller(), ctx);
//							} else {
//								ctx.setNotSent();//reset SENT flag (previous version default flag)
//								Logbook.get().update(ctx);
//							}
                        }
                        Log.i(TAG, "published again:" + ctx.toString());
//						break;
                    }
                }
                flushing = false;
            }
        }).start();
    }
}
