package com.jovistar.caltxt.phone;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

import java.util.ArrayList;

public class Logbook extends Book {
    private static final String TAG = "Logbook";
    private Context mContext;
    static Logbook lgb;

    private Logbook(Context context) {

        this.mContext = context;
        // load in memory only when showing UI (in SplashScreen)
//		load();
    }

    public static Logbook get(Context context) {
        if (lgb == null)
            return lgb = new Logbook(context);
        return lgb;
    }

    @Override
    public XCtx get(Object key) {
        XCtx o = (XCtx) super.get(key);
        if (o == null) {
            o = (XCtx) Persistence.getInstance(mContext).get((Long) key, "XCtx");
        }
        return o;
    }

    @Override
    public synchronized void clear() {
        Persistence.getInstance(mContext).clearXCTX();
        super.clear();
    }

    public synchronized void load() {
        //load only if book is empty (possibly first time initialized)
//		if(getCount()==0) {
//		if(getCount() < Persistence.getInstance(mContext).getCountXCTX()) {
        super.clear();// clear only from memory
        ArrayList<IDTObject> l = Persistence.getInstance(mContext).restore("XCtx");
        if (l.size() > 0)
            super.load(l);
//		}
    }

    public synchronized long prepend(IDTObject obj) {
        if (obj.getPersistenceId() > 0) {
            update((XCtx) obj);
            return obj.getPersistenceId();
        }
        long id = Persistence.getInstance(mContext).insert(obj);
        obj.setPersistenceId(id);
        super.prepend(id, obj);
        Log.i(TAG, "prepend " + obj.toString());
        return id;
    }

    public synchronized void remove(XCtx obj) {
        Log.i(TAG, "remove persistence id " + obj.getPersistenceId());
        Persistence.getInstance(mContext).deleteXCTX(obj.getPersistenceId());
        super.remove(obj.getPersistenceId(), obj);
    }

    public XCtx update(XCtx ctx) {
        XCtx cx = get(ctx.getPersistenceId());
        // get from db
        //commented 12-FEB-17, Persistence.get was not working
//		XCtx cx = (XCtx) Persistence.getInstance(mContext).get(ctx.getPersistenceId(), "XCtx");
        cx.setAck(ctx.getAck());
        cx.setNameCallee(ctx.getNameCallee());
        cx.setNameCaller(ctx.getNameCaller());
        cx.setCallState(ctx.getCallState());
        cx.setCallPriority(ctx.getCallPriority());
        cx.setCaltxt(ctx.getCaltxt());
        cx.setCity(ctx.getCity());
        cx.setRecvToD(ctx.getRecvToD());
        cx.setEndToD(ctx.getEndToD());
        cx.setOccupation(ctx.getOccupation());
        cx.setStartToD(ctx.getStartToD());
        cx.setCallOptions(ctx.getCallOptions());
        cx.setUsername2Caller(ctx.getUsername2Caller());
        cx.setUsernameCaller(ctx.getUsernameCaller());
        cx.setNumberCallee(ctx.getNumberCallee());
        cx.setAckEtc(ctx.getAckEtc());
        cx.setCaltxtEtc(ctx.getCaltxtEtc());

        Persistence.getInstance(mContext).update(cx);
//		Persistence.getInstance(mContext).update(ctx);
        Log.i(TAG, "update " + cx.toString());
        return cx;
    }

    /* XCTX object should have local persistence id set when passed to this function */
    public XCtx merge(XCtx ctx, long lag) {
        XCtx cx = ctx;
        Log.i(TAG, "merge ctx " + ctx);
        long pid = Persistence.getInstance(mContext).mergeWithLatestXCTX(ctx, lag);
        Log.i(TAG, "merge pid " + pid + " ,ctx " + ctx);
        if (pid <= 0) {//no adjacent record found; this is a missed call
            ctx.setCallState(XCtx.IN_CALL_MISSED);
//			 if(ctx.getCaltxt().length()>0) {
            Notify.notify_caltxt_missed_call(mContext, ctx.getNameCaller(),
                    ctx.getCaltxt(), "", ctx.getRecvToD());
//			 }
            prepend(ctx);
        } else {
            //merge ctx with local ctx(pid)
//			XCtx cx = (XCtx) get(pid);
            cx = (XCtx) Persistence.getInstance(mContext).get(pid, "XCtx");
            if (cx.getAck().length() == 0) {
                cx.setAck(ctx.getAck());
            }
            if (cx.getCity().length() == 0) {
                cx.setCity(ctx.getCity());
            }
            if (cx.getOccupation().length() == 0) {
                cx.setOccupation(ctx.getOccupation());
            }
            if (cx.getCaltxt().length() == 0) {
                cx.setCaltxt(ctx.getCaltxt());
            }
            cx.setCallState(ctx.getCallState());
            update(cx);
        }
        return cx;
    }

    /*
        public void updateName(XCtx ctx) {
            if(ctx.getUsernameCallee().equals(Addressbook.getMyProfile().getUsername())) {
    //			if(ctx.getNameCaller().equals(ctx.getUsernameCaller())) {
                    ctx.setNameCaller(Addressbook.get().getContactName(ctx.getUsernameCaller()));
                    update(ctx);
    //			}
            } else if(ctx.getUsernameCaller().equals(Addressbook.getMyProfile().getUsername())) {
    //			if(ctx.getNameCallee().equals(ctx.getUsernameCallee())) {
                    ctx.setNameCallee(Addressbook.get().getContactName(ctx.getUsernameCallee()));
                    update(ctx);
    //			}
            }
    //		Persistence.getInstance(mContext).update(ctx);
        }
    */
    public void updateNames(ArrayList<XMob> m) {//sync CTX in DB for any name change in address book
        new UpdateNames().execute(m);
    }
/*
    //loads contacts into addressBook
	private synchronized ArrayList<IDTObject> loadLog() {
		//fetch log from persistent db
		ArrayList<IDTObject> ctxs = Persistence.getInstance(mContext).restore("XCtx");
		Iterator<IDTObject> iterator = ctxs.iterator();
		while(iterator.hasNext()) {
			XCtx ctx = (XCtx)iterator.next();
			if(mylog.get(ctx.getPersistenceId())==null) {
				logbook.put(ctx.getPersistenceId(), ctx);
			}
		}
		return ctxs;
	}*/

    class UpdateNames extends AsyncTask<ArrayList<XMob>, Void, Void> {
        @Override
        protected void onPreExecute() {//called before doInBackground on UI thread
        }

        @Override
        protected Void doInBackground(ArrayList<XMob>... mobs1) {
            ArrayList<XMob> mobs = mobs1[0];
            for (IDTObject c : getList()) {
//				Log.i(TAG, "updateNames xctx," +c + ", "+mobs.size());
                for (int i = 0; i < mobs.size(); i++) {
                    if (XMob.toFQMN(((XCtx) c).getNumberCallee(), Addressbook.getMyCountryCode()).equals(mobs.get(i).getUsername())) {
                        ((XCtx) c).setNameCallee(Addressbook.getInstance(mContext).getName(mobs.get(i).getUsername()));
                        Logbook.get(mContext).update(((XCtx) c));
                        Log.i(TAG, "updateNames mob," + mobs.get(i));
                    } else if (((XCtx) c).getUsernameCaller().equals(mobs.get(i).getUsername())) {
                        ((XCtx) c).setNameCaller(Addressbook.getInstance(mContext).getName(mobs.get(i).getUsername()));
                        Logbook.get(mContext).update(((XCtx) c));
                        Log.i(TAG, "updateNames mob," + mobs.get(i));
                    }
                }
            }
            return null;//invoked on the background thread immediately after onPreExecute()
        }

        @Override
        protected void onPostExecute(Void v) {//invoked on the UI thread after the background computation finishes
        }
    }
}
