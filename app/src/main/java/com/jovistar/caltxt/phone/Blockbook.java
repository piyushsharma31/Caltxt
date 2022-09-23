package com.jovistar.caltxt.phone;

import android.content.Context;
import android.util.Log;

import com.jovistar.caltxt.bo.XCtx;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

import java.util.ArrayList;

public class Blockbook extends Book {
    private static final String TAG = "Blockbook";

    static Blockbook _bb = null;

    Context context;

    public static Blockbook getInstance(Context context) {
        if (_bb == null) {
            _bb = new Blockbook(context);
        }
        return _bb;
    }

    Blockbook(Context context) {
        this.context = context;
        ArrayList<IDTObject> bb = Persistence.getInstance(context).getAllBlockedNumbers();
        load(bb);
    }

    @Override
    public IDTObject get(Object key) {
        String uname = XMob.toFQMN((String) key, Addressbook.getMyCountryCode());
        return super.get(uname);
    }

    public boolean isBlocked(String uname) {
        if(get(uname)!=null) {
            return true;
        } else {

            // recurse Blockbook.get with value string having one less digit from the right. E.g. 911402381901, 91140238190, 9114023819
            // 911402381, 91140238, 9114023 until we find a match (which is a series XMob)
            String tmpusername = uname;
            for(int i=0; i< uname.length(); i++) {
                tmpusername = tmpusername.substring(0, tmpusername.length()-1);
//                Log.d(TAG, "isBlocked tmpusername "+tmpusername);

                if(get(tmpusername) != null) {
                    return true;
                }
            }
        }

        return false;
    }

    //add to block list
    public void add(String username, String name) {
        String uname = XMob.toFQMN(username, Addressbook.getMyCountryCode());
        XMob m = (XMob) this.get(uname);
        if (m != null)
            return;//already added

        //add in db
        long i = Persistence.getInstance(context).insertXBLO(uname, name);
        if (i != -1) {
            XMob mob = Addressbook.getInstance(context).getRegistered(uname);
            if (mob == null) {
                mob = new XMob();
                mob.setNumber(username/*, CaltxtApp.getMyCountryCode()*/);
                mob.setUsername(XMob.toFQMN(mob.getNumber(), Addressbook.getInstance(context).getMyCountryCode()));
                mob.setName(name);
                mob.setBlocked();
            } else {
                mob.setBlocked();
                Addressbook.getInstance(context).update(mob);
            }
            super.prepend(mob.getKey(), mob);
        }
    }

    //remove from block list
    public void remove(String username) {
        XMob mob = (XMob) this.get(username);
        if (mob != null) {
            super.remove(mob.getKey(), mob);
            //remove from db
            Persistence.getInstance(context).deleteXBLO(username);
        }

        //remove from address book
        XMob m = Addressbook.getInstance(context).get(username);
        if (m != null) {
            m.setUnblocked();
            Addressbook.getInstance(context).update(m);
        }
    }

    public void clear() {
        //clear from address book
        Addressbook.getInstance(context).clearBlockedStatus();
        //clear from db
        Persistence.getInstance(context).clearXBLO();
        //clear from class
        super.clear();
    }
}
