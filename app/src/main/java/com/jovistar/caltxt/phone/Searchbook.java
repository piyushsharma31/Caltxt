package com.jovistar.caltxt.phone;

import android.content.Context;
import android.util.Log;

import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

import java.util.ArrayList;

public class Searchbook extends Book {
    private static final String TAG = "Searchbook";
    private Context mContext;
    public static int DISCOVER_COUNT_AT_A_TIME = 25;
    public static boolean search_result_contain_self = false;

    static Searchbook _sb = null;

    public static Searchbook get(Context context) {
        if (_sb == null) {
            _sb = new Searchbook(context);
        }
        return _sb;
    }

    Searchbook(Context context) {
        mContext = context;
//		ArrayList<IDTObject> sb = Persistence.getInstance(Globals.getCustomAppContext()).getAllBlockedNumbers();
//		load(sb);
    }

    /*
        //add to search list
        public void add(String username, String name) {
            XMob mob = (XMob) get(username);
            if(mob!=null || username.equals(Addressbook.getMyProfile().getUsername())) {
                search_result_contain_self = true;
                return;//already added
            }

            mob = new XMob();
            mob.setNumber(username, Globals.getMyCountryCode());
            mob.setName(name);
            mob.setUsername(XMob.toFQMN(mob.getNumber(), CaltxtApp.getMyCountryCode()));

            super.prepend(mob.getKey(), mob);
        }
    */
    //remove from search list
    public void remove(String username) {
        XMob mob = (XMob) this.get(username);
        if (mob != null) {
            super.remove(mob.getKey(), mob);
        }
    }

    public void clear() {
        //clear from class
        super.clear();
    }

    public void add(ArrayList<XMob> mobs) {
        Log.i(TAG, "add ENTRY..");
        XMob oldMob = null;

        for (XMob newMob : mobs) {//while(newList.hasNext()) {
//			if(newMob.getUsername().equals(Addressbook.getMyProfile().getUsername())) {
            if (Addressbook.getInstance(mContext).isItMe(newMob.getUsername())) {
                search_result_contain_self = true;
                continue;//skip myself
            }
            //sync DB
            oldMob = (XMob) get(newMob.getUsername());
            if (oldMob == null) {
                prepend(newMob);
            } else {/* && oldMob.getUsername().equals(newMob.getUsername())*/
                update(newMob);
            }

            // SIM2
            if (newMob.getNumber2() == null || newMob.getNumber2().length() == 0) {
                continue;
            }

            oldMob = (XMob) get(newMob.getNumber2());
            // swap sim2, sim1
            String number2 = newMob.getNumber2();
            newMob.setNumber2(newMob.getUsername());
            newMob.setUsername(number2);
            if (oldMob == null) {
                prepend(newMob);
            } else {/* && oldMob.getUsername().equals(newMob.getUsername())*/
                update(newMob);
            }
        }
        Log.i(TAG, "syncAddressbookAndDB EXIT");
    }

    public void update(ArrayList<XMob> mobs) {
        Log.i(TAG, "syncSearchbookAndDB ENTRY..");
        XMob oldMob = null;

        for (XMob newMob : mobs) {//while(newList.hasNext()) {
//			if(newMob.getUsername().equals(Addressbook.getMyProfile().getUsername())) {
            if (Addressbook.getInstance(mContext).isItMe(newMob.getUsername())) {
                search_result_contain_self = true;
                continue;//skip myself
            }
            //sync DB
            oldMob = (XMob) get(newMob.getUsername());
            if (oldMob != null) {
                update(newMob);
            }

            // SIM2
            if (newMob.getNumber2() == null || newMob.getNumber2().length() == 0) {
                continue;
            }

            oldMob = (XMob) get(newMob.getNumber2());
            // swap sim2, sim1
            String number2 = newMob.getNumber2();
            newMob.setNumber2(newMob.getUsername());
            newMob.setUsername(number2);
            if (oldMob == null) {
                prepend(newMob);
            } else {/* && oldMob.getUsername().equals(newMob.getUsername())*/
                update(newMob);
            }
        }
        Log.i(TAG, "syncAddressbookAndDB EXIT");
    }

    public synchronized void prepend(XMob obj) {
        XMob mob = (XMob) get(obj.getUsername());
        if (mob != null || Addressbook.getInstance(mContext).isItMe(obj.getUsername())) {
//		if(mob!=null || obj.getUsername().equals(Addressbook.getMyProfile().getUsername())) {
            if (Addressbook.getInstance(mContext).isItMe(obj.getUsername()))
                search_result_contain_self = true;
            return;
        }
//		if(Persistence.getInstance(Globals.getCustomAppContext()).hasXMOBByUsername(obj.getUsername()))
//			return;
//		long id = Persistence.getInstance(Globals.getCustomAppContext()).insert(obj);
//		obj.setPersistenceId(id);
        super.prepend(obj.getKey(), obj);
        Log.i(TAG, "add " + obj.toString());
    }

    public synchronized void update(XMob newMob) {
        XMob oldMob = (XMob) get(newMob.getKey());

        //whether coming from CCM or MQTT, these (below) will be latest, copy
        //when user update status, headline, or name, it is updated in CCM also
        oldMob.setStatus(newMob.getStatus());
        oldMob.setHeadline(newMob.getHeadline());
        oldMob.setName(newMob.getName());

        if (newMob.getModified() > 0) {//MQTT broadcast, update cache
            oldMob.setModified(newMob.getModified());
        } else {//coming from CCM
        }
//		Persistence.getInstance(mContext).update(oldMob);
        Log.i(TAG, "update " + oldMob.toString());
    }

    @Override
    public IDTObject get(Object key) {
        String uname = XMob.toFQMN((String) key, Addressbook.getMyCountryCode());
        return super.get(uname);
    }
}
