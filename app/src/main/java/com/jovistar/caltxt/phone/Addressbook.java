package com.jovistar.caltxt.phone;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.activity.IFTTT;
import com.jovistar.caltxt.activity.SignupProfile;
import com.jovistar.caltxt.activity.CaltxtStatus;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.app.Caltxt;
import com.jovistar.caltxt.bo.XQrp;
import com.jovistar.caltxt.network.data.Connection;
import com.jovistar.caltxt.notification.Notify;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.caltxt.service.RebootService;
import com.jovistar.caltxt.service.WifiScanReceiver;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

//import com.jovistar.commons.facade.ModelFacade;

public class Addressbook extends Book {

    private static final String TAG = "Addressbook";

    public static int PLACE_INDEX_IN_STATUS = 3;
    private Context mContext = Caltxt.getCustomAppContext();
    static Addressbook adb;
    static String mMyCountryCode = null;

    private static XMob myProfile = null;
//    private ArrayList<CaltxtStatus> mStatusArray = new ArrayList<CaltxtStatus>();
    private HashMap<String, Integer> mStatusResourceMap = new HashMap<String, Integer>();
    //	static ArrayList<CaltxtStatus> mStatusGrid = new ArrayList<CaltxtStatus>();
//	private final String[] FROM_COLUMNS = { Build.VERSION.SDK_INT >= 11 ? "display_name"/* FOR Contacts.DISPLAY_NAME_PRIMARY */
//			: Contacts.DISPLAY_NAME };
    private final String[] FROM_COLUMNS = {Contacts.DISPLAY_NAME, Contacts._ID, Contacts.CONTENT_TYPE};

//	SparseIntArray statusIconResourceMap = new SparseIntArray();
//	SparseIntArray statusOverlayIconResourceMap = new SparseIntArray();

    private Addressbook(Context context) {
        Log.d(TAG, " Addressbook constructure");
        mContext = context;
/*		statusIconResourceMap.append(XMob.STATUS_DND, R.drawable.dnd);
        statusIconResourceMap.append(XMob.STATUS_AVAILABLE, R.drawable.available);
		statusIconResourceMap.append(XMob.STATUS_AWAY, R.drawable.away);
		statusIconResourceMap.append(XMob.STATUS_BUSY, R.drawable.busy);
		statusIconResourceMap.append(XMob.STATUS_OFFLINE, R.drawable.offline);
		statusIconResourceMap.append(XMob.STATUS_UNREGISTERED, R.drawable.ic_call_white_24dp);

		statusOverlayIconResourceMap.append(XMob.STATUS_DND, R.drawable.dnd_dot);
		statusOverlayIconResourceMap.append(XMob.STATUS_AVAILABLE, R.drawable.available_dot);
		statusOverlayIconResourceMap.append(XMob.STATUS_AWAY, R.drawable.away_dot);
		statusOverlayIconResourceMap.append(XMob.STATUS_BUSY, R.drawable.busy_dot);
//		statusOverlayIconResourceMap.append(XMob.STATUS_OFFLINE, R.drawable.offline_dot);//uncomment to show grey dot
		statusOverlayIconResourceMap.append(XMob.STATUS_UNREGISTERED, R.drawable.blank_dot);
*/
        myProfile = getMyProfile();
//		Persistence.getInstance(Globals.getCustomAppContext()).clearXMOB();
//		if(mStatusArray.isEmpty() || mStatusResourceMap.isEmpty()) {
//		}

        String[] statusnames = mContext.getResources().getStringArray(R.array.preference_status_grid_titles);
        TypedArray imgs = mContext.getResources().obtainTypedArray(R.array.grid_status_icons);

        for (int i = 0; i < statusnames.length; i++) {
            mStatusResourceMap.put(statusnames[i], imgs.getResourceId(i, -1));
        }

        imgs.recycle();
        // 08-FEB-2017, never load into memory, always query from database
        // load when UI is being shown, that is in SplashScreen
//	        load();//Load in RebootService. Addressbook object should be loaded always since its function uses the contacts inside the array
    }

    public static Addressbook getInstance(Context context) {
        if (adb == null)
            return adb = new Addressbook(context);
        return adb;
    }

    @Override
    public XMob get(Object key) {
        String uname = XMob.toFQMN((String) key, Addressbook.getMyCountryCode());
        XMob o = (XMob) super.get(uname);
        if (o == null) {
            o = Persistence.getInstance(mContext).getXMOBByUsername(uname);
        }
        return o;
    }

    public void setMyCountryCode(String cc) {
        mMyCountryCode = cc;
    }

    public static String getMyCountryCode() {
        if (mMyCountryCode == null) {
            SharedPreferences settings = PreferenceManager
                    .getDefaultSharedPreferences(Caltxt.getCustomAppContext());
            mMyCountryCode = settings.getString(Caltxt.getCustomAppContext()
                    .getString(R.string.profile_key_mobile_country_code), "");
            Log.d(TAG, "getMyCountryCode, " + mMyCountryCode);
            if (mMyCountryCode.length() > 0) {
                mMyCountryCode = mMyCountryCode.split("\\+")[1];
                mMyCountryCode = mMyCountryCode.substring(0, mMyCountryCode.length()-1);
            }
        }
        return mMyCountryCode;
    }

    public List<CaltxtStatus> getStatusList() {
        // 10JUN17, list changed to first five
        ArrayList<CaltxtStatus> mStatusArray = new ArrayList<CaltxtStatus>();

        String[] statusnames = mContext.getResources().getStringArray(R.array.preference_status_grid_titles);

        for (int i = 0; i < 5; i++) {
            mStatusArray.add(new CaltxtStatus(statusnames[i], statusnames[i], getStatusResourceIDByName(statusnames[i])));
        }

        return mStatusArray;
    }

    private ArrayList<CaltxtStatus> getPlaceListStandard() {
        // 10JUN17, list starts from fifth item

        ArrayList<CaltxtStatus> mStatusArray = new ArrayList<CaltxtStatus>();

        String[] statusnames = mContext.getResources().getStringArray(R.array.preference_status_grid_titles);

        for (int i = 5; i < statusnames.length; i++) {
            // commented below add line, 02-2018, fetch places from Persistence.getAllPlaces
            mStatusArray.add(new CaltxtStatus(statusnames[i], statusnames[i], getStatusResourceIDByName(statusnames[i])));
        }

        return mStatusArray;
    }

    public ArrayList<CaltxtStatus> getPlaceList() {
//        Persistence.getInstance(mContext).clearPlaces();
        ArrayList<CaltxtStatus> al = getPlaceListStandard();

        ArrayList<String> list = Persistence.getInstance(mContext).getAllPlaces();
        Log.d(TAG, " user places size: "+list.size());
        // add user created places also
        for (int i = 0; i < list.size(); i++) {
            // commented below add line, 02-2018, fetch places from Persistence.getAllPlaces
            Log.d(TAG, " user place: "+list.get(i));
            al.add(new CaltxtStatus(list.get(i), list.get(i), getStatusResourceIDByName(list.get(i))));
        }

        al.add(new CaltxtStatus("Add", "Add", R.drawable.ic_add_white_24dp));
        mStatusResourceMap.put("Add", R.drawable.ic_add_white_24dp);

        return al;
    }

    public void addPlace(String plc) {
        if(plc==null || plc.length()==0)
            return;

        if(plc.length()>3 && plc.substring(0, 3).equalsIgnoreCase("at ")) {
            plc = plc.substring(3);
        }
        Persistence.getInstance(mContext).insertPlace("at "+plc);

//        List<CaltxtStatus> thingsToBeAdd = new ArrayList<CaltxtStatus>();
//        thingsToBeAdd.add(new CaltxtStatus(plc, plc, R.drawable.ic_place_white_24dp));
//        mStatusArray.addAll(thingsToBeAdd );
//        mStatusArray.add(new CaltxtStatus(plc, plc, R.drawable.ic_place_white_24dp));
    }

    public void removePlace(String plc) {
        Persistence.getInstance(mContext).deletePlace(plc);
    }

	/*replace the MORE icon with newly selected place icon
	public void setPlaceInStatusList(CaltxtStatus s) {
		mStatusArray.set(PLACE_INDEX_IN_STATUS, s);
	}*/

    public int getStatusResourceIDByName(String name) {
        Integer i = mStatusResourceMap.get(name);
        if (i == null) {
            return R.drawable.ic_place_white_24dp;
        } else {
            return i.intValue();
        }
    }

    public String getNameByStatusResourceID(int resid) {
        for (Entry<String, Integer> entry : mStatusResourceMap.entrySet()) {
            if (entry.getValue().equals(resid)) {
                return (entry.getKey());
            }
        }
        return "";
    }

    public void load() {
//		Context mContext = CaltxtApp.getCustomAppContext();
        //load only if book is empty (possibly first time initialized)
//    	if(getCount()==0) {
        //initialize local db with address book
        if (Persistence.getInstance(mContext).getCountXMOB() == 0) {
            //			syncAddressbookAndDB(new ArrayList<XMob>(readPhoneAddressBook()));
            syncWithPhoneAddressbook();
        } else {
            //fetch contacts from persistent database
            super.clear();// clear only memory
            load(Persistence.getInstance(mContext).restore("XMob"));
        }
//        }
    }

    /*
        public synchronized void syncWithPhoneAddressbook() {
            syncAddressbookAndDB(readPhoneAddressBook());
        }

        @Override
        public synchronized void clear() {
            Persistence.getInstance(Globals.getCustomAppContext()).clearXMOB();
            super.clear();
        }
    */
    public synchronized void prepend(XMob obj) {
        if (Persistence.getInstance(mContext).hasXMOBByUsername(obj.getUsername())) {
            // exists in database
            obj = Persistence.getInstance(mContext).getXMOBByUsername(obj.getUsername());
        } else {
            // does not exists in database
            long id = Persistence.getInstance(mContext).insert(obj);
            obj.setPersistenceId(id);
        }

        // check if exists
        XMob m = (XMob) super.get(obj.getUsername());
        if (m == null) {// does not exist in memory
            super.prepend(obj.getKey(), obj);
        }

        // SIM2 shows in Contact UI
//		if(obj.getNumber2().length()>0) {
        //add if SIM2 exits
//			super.prepend(obj.getNumber2(), obj);
//		}
    }

    public synchronized void remove(XMob obj) {
        Persistence.getInstance(mContext).deleteXMob(obj.getUsername());
        super.remove(obj.getKey(), obj);
        Log.i(TAG, "remove name," + obj.toString() + ", pid," + obj.getPersistenceId());
    }

    public synchronized void remove(String number) {
        XMob m = getContact(number);
        if (m == null)
            return;
        Persistence.getInstance(mContext).deleteXMob(m.getUsername());
        super.remove(m.getKey(), m);
        Log.i(TAG, "remove name," + m.toString() + ", pid," + m.getPersistenceId());
    }

    public synchronized void update(XMob newMob) {
        XMob oldMob = getContact((String) newMob.getKey());

        //whether coming from CCM or MQTT, these (below) will be latest, copy
        //when user update status, headline, or name, it is updated in CCM also
        oldMob.setStatus(newMob.getStatus());
        oldMob.setHeadline(newMob.getHeadline());
        oldMob.setName(newMob.getName());
        oldMob.setOccupation(newMob.getOccupation());
        // 27-NOV-16: DO NOT set the place here. Place is always set a trigger from Cell or Wifi change
        // 23-JAN-17: place is set in memory, not in database
        oldMob.setPlace(newMob.getPlace());
        if (newMob.getNumber2() != null && newMob.getNumber2().length() > 0) {
            oldMob.setNumber2(newMob.getNumber2());
        }

        if (newMob.getModified() > 0) {//MQTT broadcast, update cache
//			oldMob.setStatus(newMob.getStatus());
//			oldMob.setHeadline(newMob.getHeadline());
//			oldMob.setName(newMob.getName());
            oldMob.setModified(newMob.getModified());
        } else {//coming from CCM
//			if(oldMob.getModified()>0) {
            //skip, already have latest from device itself
            //13-Sep-15, THIS IF is commented because CCM also has latest status, headline, name of user
//			} else {
            //status, headline, name are also latest on CCM, copy them
//				oldMob.setStatus(newMob.getStatus());
//				oldMob.setHeadline(newMob.getHeadline());
//				oldMob.setName(newMob.getName());
//			}
        }
//		Context mContext = CaltxtApp.getCustomAppContext();
        Persistence.getInstance(mContext).update(oldMob);
        Log.i(TAG, "update  " + oldMob.toString());
    }

    /* This is made static to avoid instantiating the Addressbook object (thus the addressbook loading)
     * Otherwise, during sign up, the addressbook will load, freezing sign up screen for some time
     */
    public static XMob getMyProfile() {
//		Context mContext = CaltxtApp.getCustomAppContext();

        if (myProfile != null && /*added 12112019 to make sure username is not empty*/myProfile.getUsername().length() != 0
                && SignupProfile.isNumberVerifiedUserAdded(Caltxt.getCustomAppContext())) {
            /**
             ** reset Auto response status if expired
             **/
            XQrp qrp = Persistence.getInstance(Caltxt.getCustomAppContext()).getQuickAutoResponse();
            if (qrp != null) {
                if (qrp.getAutoResponseEndTime() <= Calendar.getInstance().getTimeInMillis()) {
                    //if auto response expired, reset all auto responses
                    Persistence.getInstance(Caltxt.getCustomAppContext()).resetAutoResponse();
                    myProfile.resetStatusAutoResponding();
                    Log.i(TAG, "getMyProfile , resetAutoResponse");
                } else {
                    myProfile.setStatusAutoResponding();
                }
            } else {
                myProfile.resetStatusAutoResponding();
            }
//            Log.i(TAG, "getMyProfile !=null," + myProfile);
            return myProfile;
        }

        // Restore preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Caltxt.getCustomAppContext());

        String name = settings.getString(Caltxt.getCustomAppContext().getString(R.string.profile_key_fullname),
                Caltxt.getCustomAppContext().getString(R.string.profile_value_name_default));
        String number = settings.getString(Caltxt.getCustomAppContext().getString(R.string.profile_key_mobile),
                Caltxt.getCustomAppContext().getString(R.string.profile_value_mobile_default));
        String number2 = settings.getString(Caltxt.getCustomAppContext().getString(R.string.profile_key_mobile2),
                Caltxt.getCustomAppContext().getString(R.string.profile_value_mobile_default));
//		mMyCountryCode = settings.getString(mContext.getString(R.string.profile_key_mobile_country_code), "");
//		if(mMyCountryCode.length()>0)
//			mMyCountryCode = mMyCountryCode.split(",")[1];
/*
		if (myProfile == null) {
			myProfile = new XMob(name, number, CaltxtApp.getMyCountryCode());
		} else {
			myProfile.setName(name);
			myProfile.setNumber(number, CaltxtApp.getMyCountryCode());
			myProfile.setUsername(XMob.toFQMN(myProfile.getNumber(), CaltxtApp.getMyCountryCode()));
		}

		if (myProfile == null) {
			myProfile = new XMob(name, number, CaltxtApp.getMyCountryCode());
		}
*/
        if (SignupProfile.isSIM1Verified(Caltxt.getCustomAppContext())) {
            if (myProfile == null) {
                myProfile = new XMob(name, number, getMyCountryCode());
            } else {
                myProfile.setUsername(XMob.toFQMN(number, getMyCountryCode()));
                myProfile.setNumber(number);
//				myProfile.setNumber(XMob.toFQMN(number, getMyCountryCode()));
                myProfile.setName(name);
            }
            //DO NOT set unverified number in profile. So commented below
//			myProfile.setNumber2(XMob.toFQMN(number2, CaltxtApp.getMyCountryCode()));
        }

        if (SignupProfile.isSIM2Verified(Caltxt.getCustomAppContext())) {
            if (myProfile == null) {
                myProfile = new XMob(name, number2, getMyCountryCode());
            } else {//number is set as username, so this is verified SIM2, add it
                // commented 10-JUL-17, no way to convert to fqdn at receiver side (if his country is different from senders')
//				myProfile.setNumber2(number2);
                myProfile.setNumber2(XMob.toFQMN(number2, getMyCountryCode()));
                myProfile.setName(name);
            }
        }

        if (myProfile == null) {
            myProfile = new XMob("", "", getMyCountryCode());
        } else {
            //sign up not complete yet
//            ModelFacade.getInstance().setUname(myProfile.getUsername());
            myProfile.setHeadline(settings.getString(
                    Caltxt.getCustomAppContext().getString(R.string.profile_key_status_headline), XMob.STRING_STATUS_DEFAULT));
            myProfile.setStatus(settings.getInt(
                    Caltxt.getCustomAppContext().getString(R.string.profile_key_status), XMob.STATUS_AVAILABLE));
            myProfile.setPlace(settings.getString(
                    Caltxt.getCustomAppContext().getString(R.string.profile_key_location), ""));
//	        Addressbook.get().setMyStatusFromIconResource(settings.getInt(
//					Caltxt.getCustomAppContext().getString(R.string.profile_key_status_icon), R.drawable.ic_available_white_24dp));

            myProfile.setOccupation(settings.getString(Caltxt.getCustomAppContext().getString(R.string.profile_key_occupation), ""));
        }

//		mMyMob.setIcon(settings.getString(getString(R.string.profile_key_pic_url),//NO NEED, getIcon returns correct name always
//				getString(R.string.profile_value_picurl_default)));
//		mMyMob.setCity(Settings.CURRENT_LOCATION);

//		caltxtlogui.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
				/* reset the Action Bar title and Icon */
//				getSupportActionBar().setTitle(/*Html.fromHtml("<b>" + */mMyMob.getName()/* + "</b>")*/);
//				getSupportActionBar().setSubtitle(/*Html.fromHtml("<small>" + */mMyMob.getHeadline()/* + "</small>")*/);
//				if (mMyMob.getHeadline().equals(XMob.STRING_STATUS_DND)) {
//					getSupportActionBar().setLogo(Constants.icon_dnd);
//				} else if (mMyMob.getHeadline().equals(Constants.STRING_STATUS_AVAILABLE)) {
//					getSupportActionBar().setLogo(Constants.icon_available);
//				} else if (mMyMob.getHeadline().equals("Away from phone")) {
//					getSupportActionBar().setLogo(Constants.icon_away);
//				} else if (mMyMob.getHeadline().equals("Busy")) {
//					getSupportActionBar().setLogo(Constants.icon_busy);
//				} else {
//					getSupportActionBar().setLogo(Constants.icon_available);
//				}
//			}
//		});
        Log.i(TAG, "getMyProfile ," + myProfile);

        return myProfile;
    }

    public static boolean isItMe(String uname) {
        if (uname == null || myProfile == null || uname.length() == 0)
            return false;

        uname = XMob.toFQMN(uname, getMyCountryCode());
//Log.d(TAG, "isItMe uname "+uname+", profile "+myProfile);
        return uname.equals(myProfile.getUsername())
                || uname.equals(XMob.toFQMN(myProfile.getNumber2(), mMyCountryCode));
    }

    // get from address book or search book; get contact if its registered
    public XMob getRegistered(String uname) {
        XMob mob = getContact(uname);
        if (mob == null) {
            mob = (XMob) Searchbook.get(mContext).get(uname);
        }
        return mob;
    }

    // get only registered contact present in address book
    public XMob getContact(String uname) {
        //19-DEC-16, get straight from the database. data structure may not be up to date
        //commented 19-DEC-16, because contact state is rewritten (to offline) when fetched
        //from database
        String uuname = XMob.toFQMN(uname, Addressbook.getMyCountryCode());
//		Log.i(TAG, "getContact, get(uuname) "+get(uuname));
        if (get(uuname) == null) {// not loaded in memory (not showing in UI)
            return Persistence.getInstance(mContext).getXMOBByUsername(uuname);
        } else {
            return get(uuname);
        }
    }

    /*
        public int getContactStatus(String uname) {
            XMob tt = (XMob) get(uname);
            if(tt==null)
                return 2;
            else
                return tt.getStatus();
        }
    */
    public String getHeadline(String uname) {
        String uuname = XMob.toFQMN(uname, getMyCountryCode());
        XMob tt = getContact(uuname);
        if (tt == null) {
            tt = (XMob) Searchbook.get(mContext).get(uuname);
            if (tt == null) {
                return "";
            }
        }
        return tt.getHeadline();
    }

    // does contact exist in addressbook
    public boolean isContact(String uname) {
        XMob tt = getContact(uname);
        return tt != null;
    }

    public boolean isConnected(String uname) {
        XMob mob = getContact(uname);
        if (mob != null && Connection.get().isConnected()) {
            if (mob.isDND())
                return true;
            else if (mob.isAutoResponding())
                return true;
            else if (mob.isAvailable())
                return true;
            else if (mob.isAway())
                return true;
            else if (mob.isBusy())
                return true;
            else if (mob.isOffline())
                return false;
            else
                return false;
        } else {
            return false;
        }
    }

    // is contact registered on caltxt
    public boolean isRegistered(String uname) {
        String uuname = XMob.toFQMN(uname, getMyCountryCode());

        if (isCaltxtContact(uuname) == false) {
            XMob tt = (XMob) Searchbook.get(mContext).get(uuname);
            return tt != null;
        }
        return true;
    }

    public boolean isRegisteredAndConnected(String uname) {
        uname = XMob.toFQMN(uname, getMyCountryCode());

        XMob tt = getContact(uname);
        return isRegistered(uname) && /*!tt.isOffline()*/isConnected(uname);
    }

    public boolean isCaltxtContact(String uname) {
        XMob tt = getContact(uname);
        Log.i(TAG, "isCaltxtContact, tt " + tt);
        return tt != null && tt.isRegistered();

    }

    public boolean isCaltxtContactAndConnected(String uname) {
        return isCaltxtContact(uname) && isConnected(uname);
    }

    public ArrayList<XMob> getContacts() {
        ArrayList<XMob> al = new ArrayList<XMob>();
        Iterator<IDTObject> it = getList().iterator();

        XMob object = null;
        while (it.hasNext()) {
            object = (XMob) it.next();
            al.add(object);
        }
        return al;
    }

    public ArrayList<XMob> getUnregisteredContacts() {
        ArrayList<XMob> al = new ArrayList<XMob>();
        Iterator<IDTObject> it = getList().iterator();

        XMob object = null;
        while (it.hasNext()) {
            object = (XMob) it.next();
            if (/*object.getStatus()==2*/object.isRegistered() == false) {
                al.add(object);
            }
        }
        return al;
    }

    public void clearBlockedStatus() {
        Iterator<IDTObject> it = getList().iterator();

        XMob object = null;
        while (it.hasNext()) {
            object = (XMob) it.next();
            if (object.isBlocked()) {
                object.setUnblocked();
                update(object);
            }
        }
    }

    public int getCaltxtContactsCount() {
        Iterator<IDTObject> it = getList().iterator();
        int count = 0;

        XMob object = null;
        while (it.hasNext()) {
            object = (XMob) it.next();
            if (/*object.getStatus()!=2*/object.isRegistered()) {
                count++;
            }
        }
        return count;
    }

    public String getName(String uname) {
        String uuname = XMob.toFQMN(uname, getMyCountryCode());

        if (uuname.equals(myProfile.getUsername()))
            return myProfile.getName();

        XMob tt = getContact(uuname);
        if (tt == null) {
            tt = (XMob) Searchbook.get(mContext).get(uuname);
            if (tt == null) {
                return /*"+"+*/uname;
            }
        }
        return tt.getName();
    }

    public synchronized void updateStatusAllOffline() {
        Iterator<IDTObject> it = getList().iterator();
        XMob mob;
        while (it.hasNext()) {
            mob = (XMob) it.next();
//			if(mob.getStatus()!=XMob.STATUS_UNREGISTERED)//registered Caltxt user
//    			mob.setStatus(XMob.STATUS_OFFLINE);
            if (mob.isRegistered())//registered Caltxt user
                mob.setStatusOffline();
        }
    }

    public synchronized void updateStatusAllUnregistered() {
        Iterator<IDTObject> it = getList().iterator();
        XMob mob;
        while (it.hasNext()) {
            mob = (XMob) it.next();
            if (mob.isRegistered()) {//registered Caltxt user
                mob.setStatusUnregistered();
                update(mob);
//    			Log.i(TAG, "updateStatusAllUnregistered .."+mob.toString());
            }
        }
    }

    //insert or update DB with registered XMob; insert or update addressBook object
    public void syncAddressbookAndDB(ArrayList<XMob> mobs) {
        Log.i(TAG, "syncAddressbookAndDB ENTRY..");
        XMob oldMob = null;

        for (XMob newMob : mobs) {//while(newList.hasNext()) {
            if (newMob.getUsername().equals(myProfile.getUsername())) {
                continue;//skip myself
            }
            //sync DB
            oldMob = get(newMob.getUsername());
            if (oldMob == null) {
                prepend(newMob);
            } else {/* && oldMob.getUsername().equals(newMob.getUsername())*/
                if (oldMob.isRegistered() == false && newMob.isRegistered() == true) {
                    remove(oldMob);//remove old, prepend new mob so that they appear on top of contact list
                    prepend(newMob);
                } else {
                    update(newMob);
                }
            }

            Log.d(TAG, "syncAddressbookAndDB, " + newMob.toString());

            // SIM2
            if (newMob.getNumber2() == null || newMob.getNumber2().length() == 0) {
                continue;
            }

            oldMob = get(newMob.getNumber2());
            // swap sim2, sim1
            String number2 = newMob.getNumber2();
            newMob.setNumber2(newMob.getUsername());
            newMob.setUsername(number2);
            if (oldMob == null) {
                prepend(newMob);
            } else {
                if (oldMob.isRegistered() == false && newMob.isRegistered() == true) {
                    remove(oldMob);//remove old, prepend new mob so that they appear on top of contact list
                    prepend(newMob);
                } else {
                    update(newMob);
                }
            }

            Log.d(TAG, "syncAddressbookAndDB, sim2 " + newMob.toString());
        }
        Log.i(TAG, "syncAddressbookAndDB EXIT");
    }
/*
	public synchronized void syncContactsInAddressBook(Collection<XMob> mobs) {
		XMob m1, m;
		Iterator<XMob> it = mobs.iterator();

		while(it.hasNext()) {
			m1 = it.next();
			//sync address book (addressBook object)
			String un = m1.getUsername();
			m = addressBook.get(un);
			if(m!=null) {
				m.setStatus(m1.getStatus());//indicate as registered user
				m.setHeadline(m1.getHeadline());
				m.setName(m1.getName());
			} else {
				add(m1);
			}
		}
	}
*/

    public String getContactNameFromPhoneAddressbook(String number) {
        String name = null;

//		Context mContext = CaltxtApp.getCustomAppContext();

        final ContentResolver cr = mContext.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor cursor = cr.query(uri, new String[]{BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        try {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToNext();
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return name;
    }

    public String getContactNumberFromPhoneAddressbook(String name) {
        String number = null;

        final ContentResolver cr = mContext.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(name));
        Cursor cursor = cr.query(uri, new String[]{BaseColumns._ID,
                ContactsContract.PhoneLookup.NUMBER}, null, null, null);

        try {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToNext();
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return number;
    }

    public String getContactNameFromPhoneAddressbook(int contactID) {
        String value = null;

//		Context mContext = CaltxtApp.getCustomAppContext();

        final ContentResolver cr = mContext.getContentResolver();
//	    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
//	    Cursor cursor = cr.query(uri, new String[] {BaseColumns._ID,
//	            ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

        Cursor cursor = cr.query(Data.CONTENT_URI,
                new String[]{Phone.DISPLAY_NAME},
                Data.CONTACT_ID + "=?" + " AND "
                        + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
                new String[]{String.valueOf(contactID)}, null);

        try {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToNext();
                value = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return value;
    }

    public ArrayList<String> getContactNumberFromPhoneAddressbook(int contactID) {
        String value = null;
        ArrayList<String> values = new ArrayList<String>();

//		Context mContext = CaltxtApp.getCustomAppContext();

        final ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(Data.CONTENT_URI,
                new String[]{Phone.NUMBER},
                Data.CONTACT_ID + "=?" + " AND "
                        + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
                new String[]{String.valueOf(contactID)}, null);

        try {
            while (cursor != null && cursor.moveToNext()) {
//	        	cursor.moveToNext();
                value = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                values.add(value);
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return values;
    }

    public int getContactIDFromPhoneAddressbook(String number) {
        int ID = 0;

//		Context mContext = CaltxtApp.getCustomAppContext();

        final ContentResolver cr = mContext.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor cursor = cr.query(uri, new String[]{BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        try {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToNext();
                ID = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                String num = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                Log.i(TAG, "getContactIDFromPhoneAddressbook " + num);
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return ID;
    }

    public static ArrayList<String> getPhoneAddressbook() {
        ArrayList<String> ab = new ArrayList<>();

        Cursor phones = Caltxt.getCustomAppContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER},
                null, null, null);

        String number;
        int numbers_count = 0;
        Log.i(TAG, "getPhoneAddressbook entries.." + phones.getCount());

        if (phones != null)
            while (phones.moveToNext()) {
                number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if (number != null && number.trim().length() > 0) {
                    if (isItMe(number))
                        continue;

                    numbers_count++;
                    ab.add(number);
                }
            }
        phones.close();
        return ab;
    }

    //read phone address book and sync up Caltxt address book
    public void syncWithPhoneAddressbook() {

        super.clear();// clear only memory

        long start = Calendar.getInstance().getTimeInMillis();

//		Context mContext = CaltxtApp.getCustomAppContext();

        Cursor phones = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER},
                null, null, null);
        String name, number;
        int numbers_count = 0;
        Log.i(TAG, "syncWithPhoneAddressbook ENTRY.." + phones.getCount());
        if (phones != null)
            while (phones.moveToNext()) {
                name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if (number != null && number.trim().length() > 0) {
                    XMob tt = new XMob(name, number, getMyCountryCode());
//				if(tt.getUsername().equals(Addressbook.getMyProfile().getUsername()))
                    if (isItMe(tt.getUsername()))
                        continue;
//			  XMob m = Persistence.getInstance(mContext).getXMOBByUsername(tt.getUsername());
//				if(	m==null) {//new number in phone book
//			  XMob m = (XMob) get(tt.getUsername());
                    numbers_count++;
                    prepend(tt);
                    Log.i(TAG, "syncWithPhoneAddressbook prepend number.." + tt.getName() + tt.getNumber());
//				} else if(!m.getName().equals(tt.getName())) {//name change in phone book
//					update(tt);
//				}
                }
            }
        phones.close();
        Log.i(TAG, "readPhoneAddressBook EXIT, end size " +/*contactsPhoneNumbers.size()+*/", phone entries " + numbers_count
                + ":" + getCount() + " time taken " + (Calendar.getInstance().getTimeInMillis() - start) / 1000);
    }

    public void syncWithPhoneAddressbook123() {
        long start = Calendar.getInstance().getTimeInMillis();
        Log.i(TAG, "readPhoneAddressBook begin..");

        final ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID, ContactsContract.PhoneLookup.DISPLAY_NAME}
                , null, null, null);
        Cursor phones = null;
        String contactId, name, number;
        int numbers_count = 0;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                name = cursor.getString(cursor.getColumnIndex(FROM_COLUMNS[0]));
//				hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

//				if (hasPhone.equalsIgnoreCase("1")) {
                // create XCtt
					/*phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
										+ " = " + contactId, null, null);*/
                phones = cr.query(Data.CONTENT_URI,
                        new String[]{Phone.NUMBER/*, Data._ID, Phone.TYPE, Phone.LABEL*/},
                        Data.CONTACT_ID + "=?" + " AND "
                                + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
                        new String[]{String.valueOf(contactId)}, null);

                if (phones == null)
                    continue;
//					phones.moveToFirst();

//					Log.i(TAG, "readPhoneAddressBook phones count "+phones.getCount() + ", "+phones);
                while (phones.moveToNext()/*.isAfterLast() == false*/) {
//						int type = phones.getInt(phones.getColumnIndexOrThrow(Phone.TYPE));
//						String label = phones.getString(phones.getColumnIndexOrThrow(Phone.LABEL));
//						String label1 = phones.getString(phones.getColumnIndexOrThrow("data4"));
//						String label2 = phones.getString(phones.getColumnIndexOrThrow("data5"));
                    number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    if (number != null && number.trim().length() > 0) {
                        XMob tt = new XMob(name, number, getMyCountryCode());
//							if(tt.getUsername().equals(Addressbook.getMyProfile().getUsername()))
                        if (isItMe(tt.getUsername()))
                            continue;
                        XMob m = get(tt.getUsername());
                        if (m == null) {//new number in phone book
                            numbers_count++;
                            prepend(tt);
//								contactsPhoneNumbers.put(tt.getUsername(), tt);
//								Log.i(TAG, "readPhoneAddressBook add "+tt.toString());
//							} else if(!m.getName().equals(tt.getName())) {//name change in phone book
//								update(tt);
//								Log.i(TAG, "readPhoneAddressBook update "+tt.toString());
                        }
                    }
//						phones.moveToNext();
                }
                phones.close();
            }
//			}
            cursor.close();
        }
/*
		//contactsMap is updated; now UPDATE the status for phone book entries
		ArrayList<IDTObject> mobs = getPersistence().restore("XMob");
		Iterator<IDTObject> iterator = mobs.iterator();
		while(iterator.hasNext()) {
			XMob mob = (XMob)iterator.next();
			if(addressBook.get(mob.getUsername())==null) {
				add(mob);
			}
		}*/
        //now sync any names (non caltxt) changed in address book
//		Logbook.get().updateName();
        Log.i(TAG, "readPhoneAddressBook end size " +/*contactsPhoneNumbers.size()+*/", phone entries " + numbers_count
                + ":" + getCount() + " time taken " + (Calendar.getInstance().getTimeInMillis() - start) / 1000);
//		return contactsPhoneNumbers.values();
    }

    public Drawable getContactStatusOverlayIconResource(String uname) {
        int overlay_resource1 = R.drawable.blank_dot;
        int overlay_resource2 = R.drawable.blank_dot;

        XMob mob = getRegistered(uname);
//		Log.d(TAG, "getContactStatusOverlayIconResource, mob:"+mob);

        if (mob != null) {
            if (mob.isAvailable()) {
                overlay_resource1 = R.drawable.available_dot;
            } else if (mob.isAway()) {
                overlay_resource1 = R.drawable.away_dot;
            } else if (mob.isBusy()) {
                overlay_resource1 = R.drawable.busy_dot;
            }

            if (mob.isDND()) {
                overlay_resource1 = R.drawable.dnd_dot;
            }

            if (mob.isAutoResponding()) {
                overlay_resource2 = R.drawable.auto_reply_overlay;
                if (mob.isOffline()) {
                    overlay_resource1 = R.drawable.offline_dot;
                }
            }

            if (Blockbook.getInstance(mContext).get(mob.getUsername()) != null) {
                overlay_resource1 = R.drawable.blocked_dot;
            }
        }

        Resources r = mContext.getResources();
        Drawable[] layers = new Drawable[2];
        layers[0] = r.getDrawable(overlay_resource1);
        layers[1] = r.getDrawable(overlay_resource2);

        LayerDrawable layerDrawable = new LayerDrawable(layers);
        return layerDrawable;

//		return R.drawable.blank_dot;
/*		int status = getStatusOverlayIconResource(getContactStatus(uname));
		if(status==0) {//does not exist in map
			return R.drawable.blank_dot;
		} else {
			return status;
		}*/
    }

    /*
        public int getContactStatusIconResource(String uname) {
            int status = getStatusIconResource(getContactStatus(uname));

            if(status==0)//does not exist in map
                return Constants.icon_listview_offline;
            else
                return status;
        }

        public void setMyStatusFromIconResource(int iconresource) {
            if(iconresource==R.drawable.ic_available_white_24dp) {
                getMyProfile().setStatusAvailable();
            } else if(iconresource==R.drawable.ic_busy_white_24dp) {
                getMyProfile().setStatusBusy();
            } else if(iconresource==R.drawable.ic_away_white_24dp) {
                getMyProfile().setStatusAway();
            } else if(iconresource==R.drawable.ic_donotdisturb_white_24dp) {
                getMyProfile().setStatusDND();
    //		} else if(iconresource==R.drawable.ic_busy_white_24dp) {
    //			getMyProfile().setStatusOffline();
    //			Log.i(TAG, "setMyStatusFromIconResource icon_actionbar_offline");
            } else if(iconresource==R.drawable.ic_auto_answer_mode_outline_white_24dp) {
                getMyProfile().setStatusAutoResponding();
            } else {
                getMyProfile().setStatusOffline();
                Log.i(TAG, "setMyStatusFromIconResource iconresource "+iconresource);
            }
        }*/
/*
	private int getStatusIconResource(int status) {
		return statusIconResourceMap.get(status);
	}

	private int getStatusOverlayIconResource(int status) {
		return statusOverlayIconResourceMap.get(status);
	}

	public int getContactStatusIconResource(XMob mob) {

		int resid = 0;
//		Resources r = mContext.getResources();
//		Drawable[] layers = null;
//		layers = new Drawable[2];
		//		if(RebootService.getConnection().isConnected()) {
			if(mob.isAvailable()) {
//				layers[0] = r.getDrawable(R.drawable.circle_available);
//				layers[1] = r.getDrawable(R.drawable.ic_available_white_24dp);
				resid = R.drawable.ic_available_white_24dp;
			} else if(mob.isAway()) {
//				layers[0] = r.getDrawable(R.drawable.circle_away);
//				layers[1] = r.getDrawable(R.drawable.ic_away_white_24dp);
				resid = R.drawable.ic_away_white_24dp;
			} else if(mob.isBusy()) {
//				layers[0] = r.getDrawable(R.drawable.circle_busy);
//				layers[1] = r.getDrawable(R.drawable.ic_busy_white_24dp);
				resid = R.drawable.ic_busy_white_24dp;
			} else if(mob.isDND()) {
//				layers[0] = r.getDrawable(R.drawable.circle_dnd);
//				layers[1] = r.getDrawable(R.drawable.ic_donotdisturb_white_24dp);
				resid = R.drawable.ic_donotdisturb_white_24dp;
			} else if(mob.isOffline()) {
//				layers[0] = r.getDrawable(R.drawable.circle_grey);
//				layers[1] = r.getDrawable(R.drawable.ic_busy_white_24dp);
				resid = R.drawable.ic_busy_white_24dp;
			} else {//unregistered
//				layers[0] = r.getDrawable(R.drawable.circle_grey);
//				layers[1] = r.getDrawable(R.drawable.ic_busy_white_24dp);
				resid = getStatusResourceIDByName(mob.getHeadline());
			}

			int tresid = getStatusResourceIDByName(mob.getPlace());
			if(tresid>0) {
//				layers[1] = r.getDrawable(resid);
				resid = tresid;
//				layers[1] = CaltxtApp.getImageLoader().resize(r.getDrawable(resid), 16);
			}

			if(mob.isAutoResponding()) {
//				layers[1] = r.getDrawable(R.drawable.ic_auto_white_24dp);
				resid = R.drawable.ic_auto_white_24dp;
			}
//		}
//			LayerDrawable layerDrawable = new LayerDrawable(layers);
//			return layerDrawable;
			return resid;
	}
*/
    public String geMyStatusString() {

        if (getMyProfile().isDND())
            return XMob.STRING_STATUS_DND;
        if (getMyProfile().isAutoResponding())
            return "Automatic Response";

        if (getMyProfile().isAvailable())
            return XMob.STRING_STATUS_AVAILABLE;
        else if (getMyProfile().isAway())
            return XMob.STRING_STATUS_AWAY;
        else if (getMyProfile().isBusy())
            return XMob.STRING_STATUS_BUSY;
        else if (getMyProfile().isOffline())
            return XMob.STRING_STATUS_OFFLINE;
        else
            return "";
    }

    public int geStatusBackgroundResource(XMob mob) {

        if (mob != null && Connection.get().isConnected()) {
			/*if(mob.getHeadline().equals(XMob.STRING_STATUS_DND)) {
				return R.drawable.circle_dnd;
			} else if(mob.getHeadline().equals(XMob.STRING_STATUS_BUSY)) {
				return R.drawable.circle_busy;
			} else if(mob.getHeadline().equals(XMob.STRING_STATUS_AVAILABLE)) {
				return R.drawable.circle_available;
			} else if(mob.getHeadline().equals(XMob.STRING_STATUS_AWAY)) {
				return R.drawable.circle_away;
			} else {*/
            if (mob.isDND())
                return R.drawable.circle_dnd;
            else if (mob.isAutoResponding())
                return R.drawable.circle_busy;
            else if (mob.isAvailable())
                return R.drawable.circle_available;
            else if (mob.isAway())
                return R.drawable.circle_away;
            else if (mob.isBusy())
                return R.drawable.circle_busy;
            else if (mob.isOffline())
                return R.drawable.circle_grey;
            else
                return R.drawable.circle_grey;
//			}
        } else {
            return R.drawable.circle_grey;
        }
    }

    public int getMyStatusColor() {
        if (Connection.get().isConnected()) {

			/*if(getMyProfile().getHeadline().equals(XMob.STRING_STATUS_DND)) {
				return mContext.getResources().getColor(R.color.color_status_dnd);
			} else if(getMyProfile().getHeadline().equals(XMob.STRING_STATUS_BUSY)) {
				return mContext.getResources().getColor(R.color.color_status_busy);
			} else if(getMyProfile().getHeadline().equals(XMob.STRING_STATUS_AVAILABLE)) {
				return mContext.getResources().getColor(R.color.color_status_available);
			} else if(getMyProfile().getHeadline().equals(XMob.STRING_STATUS_AWAY)) {
				return mContext.getResources().getColor(R.color.color_status_away);
			} else {*/

            if (getMyProfile().isDND())
                return mContext.getResources().getColor(R.color.color_status_dnd);
            else if (getMyProfile().isAutoResponding())
                return mContext.getResources().getColor(R.color.color_status_busy);
            else if (getMyProfile().isAvailable())
                return mContext.getResources().getColor(R.color.color_status_available);
            else if (getMyProfile().isAway())
                return mContext.getResources().getColor(R.color.color_status_away);
            else if (getMyProfile().isBusy())
                return mContext.getResources().getColor(R.color.color_status_busy);
            else if (getMyProfile().isOffline()) {
                Log.d(TAG, "getMyStatusColor color_status_offline1");
                return mContext.getResources().getColor(R.color.color_status_offline);
            } else {
                return mContext.getResources().getColor(R.color.color_status_available);
            }
//			}
        }
        Log.d(TAG, "getMyStatusColor color_status_offline");
        return mContext.getResources().getColor(R.color.color_status_offline);
    }

    //20-JUN-16 show the actual status always
    public int getContactStatusIconResource(XMob mob) {

        if (mob == null)
            return R.drawable.ic_unknown_outline_white_24dp;

        if (mob.isAutoResponding()) {
            return R.drawable.ic_auto_answer_sent_outline_white_24dp;
        } else if (mob.isDND()) {
            return R.drawable.ic_donotdisturb_white_24dp;
        } else if (!mob.isRegistered()) {
            return R.drawable.ic_unknown_outline_white_24dp;
        } else {
            int residLocation = getStatusResourceIDByName(mob.getPlace());
            int residHeadline = getStatusResourceIDByName(mob.getHeadline());

            if (residLocation == 0) {
                return residHeadline;
            } else {
                if (mob.getHeadline().equals(XMob.STRING_STATUS_AVAILABLE)
                        || mob.getHeadline().equals(XMob.STRING_STATUS_BUSY)
                        || mob.getHeadline().equals(XMob.STRING_STATUS_AWAY)) {
                    return residLocation;
                } else {
                    return residHeadline;
                }
            }
        }
/*
//		if(RebootService.getConnection().isConnected()) {
			if(mob.isAvailable())
				return R.drawable.ic_available_white_24dp;
			else if(mob.isAway())
				return R.drawable.ic_away_white_24dp;
			else if(mob.isBusy())
				return R.drawable.ic_busy_white_24dp;
			else if(mob.isDND())
				return R.drawable.ic_donotdisturb_white_24dp;
			else if(mob.isOffline())
				return R.drawable.ic_busy_white_24dp;
			else {
				return getStatusResourceIDByName(mob.getPlace());
//				return R.drawable.ic_busy_white_24dp;
//				return Constants.icon_actionbar_available;
			}
//		}
*/
//		return R.drawable.ic_busy_white_24dp;
    }

    public void changeMyPlace(final String place) {
		/*
		 * SET the status ICON
		 */
    }

    public void changeMyStatus(final String new_headline, final String new_place) {

        // if sim registration pending, STOP (new user or sim changed)
        if(false== SignupProfile.isNumberVerifiedUserAdded(mContext))
            return;

        // 13JUN17, old place, headline should be picked from persistent storage rather
        // than memory (App might have got killed/died etc in middle)
        // 14JUN17, commented again (if App is killed in middle, it will again reinit from
        // persistent storage again (so latest place/status from persistent is picked)
        String old_headline = SignupProfile.getPreference(mContext, mContext.getString(R.string.profile_key_status_headline));
//		String old_headline = getMyProfile().getHeadline();
        // commented 09-01-2018, why set profile parameter, it will get set only when new param is different (as in code below)
//        getMyProfile().setHeadline(old_headline);// just in case headline in memory and persistence is not in sync

        String old_place = SignupProfile.getPreference(mContext, mContext.getString(R.string.profile_key_location));
//		String old_place = getMyProfile().getPlace();
        // commented 09-01-2018, why set profile parameter, it will get set only when new param is different (as in code below)
//        getMyProfile().setPlace(old_place);// just in case place in memory and persistence is not in sync

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        int old_status = settings.getInt(mContext.getString(R.string.profile_key_status), XMob.STATUS_AVAILABLE);
//		int old_status = getMyProfile().getStatus();
        // commented 09-01-2018, why set profile parameter, it will get set only when new param is different (as in code below)
//        getMyProfile().setStatus(old_status);// just in case status in memory and persistence is not in sync

        String new_place_override = new_place;

        Log.i(TAG, "changeMyStatus old_place " + old_place
                + ", old_headline " + old_headline);
        Log.i(TAG, "changeMyStatus new_place " + new_place
                + ", new_headline " + new_headline);

        if ((new_headline.equals(old_headline) && new_place_override.equals(old_place))
                || (new_headline.trim().length() == 0)) {
            return;
        }

		/*
		 * SET the status ICON
		 */
        int left_icon_resource = getStatusResourceIDByName(new_headline);//all icon including for DND
        if (new_place.startsWith("at ")) {
            left_icon_resource = getStatusResourceIDByName(new_place);
        }

        if (left_icon_resource == 0) {//must be auto response
            if (getMyProfile().isAutoResponding()) {
                left_icon_resource = R.drawable.ic_auto_answer_sent_outline_white_24dp;
            } else {
                // just in case new_headline comes blank
                left_icon_resource = R.drawable.ic_available_white_24dp;
            }
        }

        if (new_headline.equals(old_headline) == false && new_headline.length() > 0) {

			/*
			 * SET the STATUS
			 */
            if (new_headline.equals(XMob.STRING_STATUS_DND)) {
                getMyProfile().setStatusDND();
            } else if (new_headline.equals(XMob.STRING_STATUS_AVAILABLE)) {
                getMyProfile().setStatusAvailable();
            } else if (new_headline.equals(XMob.STRING_STATUS_AWAY)) {
                getMyProfile().setStatusAway();
            } else if (new_headline.equals(XMob.STRING_STATUS_BUSY)) {
                getMyProfile().setStatusBusy();
            } else if (getMyProfile().isAutoResponding()) {
                getMyProfile().setStatusBusy();
            } else if (new_headline.length() > 0) {//status string is not from preset statuses
                // set status to busy
                Log.i(TAG, "changeMyStatus ---" + new_headline + new_place);
                getMyProfile().setStatusBusy();
                if (new_headline.equals(XMob.STRING_STATUS_DINING)
                        || new_headline.equals(XMob.STRING_STATUS_MEETING)) {
                    //dining, meeting - headline remains as it is!
                } else {
                    //driving, in bus etc
                    //current headline is like driving, in bus etc.. change it to available
                    //since arrived at some location. because user can not be driving if at home, work etc
                    //***beware of location (cell id) is same for few kilometers***
                    new_place_override = "";
                }
            }

            getMyProfile().setHeadline(new_headline);
            SignupProfile.setPreference(mContext,
                    mContext.getString(R.string.profile_key_status_headline),
                    new_headline);

			/*
			 * SET status DIRTY
			 */
            RebootService.is_status_dirty = true;
        }

        //if place changed, trigger action, if any
        if (new_place_override.equals(old_place) == false /*&& new_place_override.length()>0 **new place can be unknown/blank value**/) {
            /***
             * set the PLACE
             */
            if (new_place.equals("Forget")) {
                new_place_override = "";
                left_icon_resource = getStatusResourceIDByName(old_headline);

                final WifiScanReceiver wifiSR = new WifiScanReceiver();
                wifiSR.setAction("remove");
                // forget current place also; remove cell id, wifi id from places list
                // forget this place cell id
//                wifiSR.startCellScan(mContext.getApplicationContext());
//				String uid = CallManager.getInstance().getUniqueCellLocation(mContext);
//				Log.d(TAG, "Forget cell id " + uid);
//				Persistence.getInstance(mContext).deleteXPLC(uid);
//				CallManager.getInstance().cellids.remove(uid);
                // forget this place wifi ssid(s)
                wifiSR.startWifiAndCellScan(mContext.getApplicationContext());
//				mContext.getApplicationContext().
//						startService(new Intent(mContext, WifiScanService.class).putExtra("place", "remove"));
//			} else if(new_place.startsWith("at ")) {
//				left_icon_resource = getStatusResourceIDByName(new_place);
            }

            //trigger any location based Rules if enabled
            if (new_place_override.length() == 0)//moved to unknown place from known place
                IFTTT.placeChangeCallback(mContext, old_place, 1);
            else if (old_place.length() == 0) {//moved to known place from unknown place
                IFTTT.placeChangeCallback(mContext, new_place_override, 0);
            } else {// moved from known place to another known place
                IFTTT.placeChangeCallback(mContext, new_place_override, 0);
                IFTTT.placeChangeCallback(mContext, old_place, 1);
            }

            getMyProfile().setPlace(new_place_override);
            Log.i(TAG, "changeMyStatus setPlace " + new_place_override);

            SignupProfile.setPreference(mContext,
                    mContext.getString(R.string.profile_key_location),
                    new_place_override);

			/*
			 * SET status DIRTY
			 */
            RebootService.is_status_dirty = true;
        }

        //here, headline could be for auto response or remaining status (driving etc)
        if (getMyProfile().isAutoResponding()) {
            //headline remains as it is!
        }

		/*
		 * STORE ICON in database
		 */
        SignupProfile.setPreference(mContext,
                mContext.getString(R.string.profile_key_status_icon),
                left_icon_resource);

        /*
         * STORE STATUS in database
         */

        if (getMyProfile().getStatus() != old_status) {
            SignupProfile.setPreference(mContext,
                    mContext.getString(R.string.profile_key_status),
                    getMyProfile().getStatus());
        }

		/*
		 * UPDATE status on SERVER also
		 */
//        ModelFacade.getInstance().fxAsyncServiceRequest(
//                ModelFacade.getInstance().SVC_CALTXT_USER,
//                ModelFacade.getInstance().OP_SET, getMyProfile(),
//                new CCWService(mContext));
//			RebootService.BroadcastStatus();//- commented 5/APR/16 - integrated in Adapter::getView()

//		updateActionBarTitle();
//		if(!status.equals("Forget")) {
        if (RebootService.is_status_dirty) {
            Connection.get().addAction(Constants.myStatusChangeProperty, null, new_place_override);

            // so that place icon is shown
            // commented 14SEP17, icon should have been assigned in code above
//            left_icon_resource = getStatusResourceIDByName(new_place_override);

            Notify.notify_caltxt_status_change(mContext,
                    new_place_override.length() == 0 ? new_headline : new_headline + ", " + new_place_override,
//					headline.equals(XMob.STATUS_OFFLINE) ? location : headline+", "+location, 
                    "", "", left_icon_resource);
        }

        Log.i(TAG, "changeMyStatus NEW " + getMyProfile());
    }

    /*
    private int getMyStatusIconResourceOverlay() {
        if(RebootService.getConnection().isConnected()) {
            if(getMyProfile().isAvailable())
                return R.drawable.available_dot;
            else if(getMyProfile().isAway())
                return R.drawable.away_dot;
            else if(getMyProfile().isBusy())
                return R.drawable.busy_dot;
            else if(getMyProfile().isDND())
                return R.drawable.dnd_dot;
            else if(getMyProfile().isAutoResponding())
                return R.drawable.auto_reply_overlay;
            else
                return R.drawable.available_dot;
        }
        return R.drawable.offline_dot;
    }

    public Drawable getMyStatusIconResourceCircle() {
        Drawable d1 = ((CaltxtApp)CaltxtApp.getCustomAppContext()).getImageLoader().getSquareDrawableFromFile(
                Addressbook.getMyProfile().getIcon(),
                Constants.PHOTO_SIZE_STANDARD, R.drawable.ic_person_white_24dp);

        XQrp qrp = Persistence.getInstance(mContext).getQuickAutoResponse();
        Resources r = mContext.getResources();
        Drawable[] layers = null;
        if(qrp == null) {
            layers = new Drawable[3];
            layers[0] = d1;
            layers[1] = r.getDrawable(getMyStatusIconResourceOverlay());
            layers[2] = r.getDrawable(R.drawable.frame_actionbar);
        } else {
            layers = new Drawable[3];
            layers[0] = d1;
            layers[1] = r.getDrawable(getMyStatusIconResourceOverlay());
            layers[2] = r.getDrawable(R.drawable.auto_reply_overlay);
        }
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        return layerDrawable;
    }

    public Drawable getMyStatusIconResourceSquare() {
        Drawable d1 = ((CaltxtApp)CaltxtApp.getCustomAppContext()).getImageLoader().getSquareDrawableFromFile(
                Addressbook.getMyProfile().getIcon(),
                Constants.PHOTO_SIZE_STANDARD, R.drawable.ic_person_white_24dp);

        XQrp qrp = Persistence.getInstance(mContext).getQuickAutoResponse();
        Resources r = mContext.getResources();
        Drawable[] layers = null;
        if(qrp == null) {
            layers = new Drawable[2];
            layers[0] = d1;
            layers[1] = r.getDrawable(getMyStatusIconResourceOverlay());
        } else {
            layers = new Drawable[3];
            layers[0] = d1;
            layers[1] = r.getDrawable(getMyStatusIconResourceOverlay());
            layers[2] = r.getDrawable(R.drawable.auto_reply_overlay);
        }
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        return layerDrawable;
    }

    public Drawable getMyStatusIconResource1() {
        int res = getMyStatusIconResource();

        XQrp qrp = Persistence.getInstance(mContext).getQuickAutoResponse();
        Resources r = mContext.getResources();
        Drawable[] layers = null;
        if(qrp == null) {
            layers = new Drawable[1];
            layers[0] = r.getDrawable(res);
        } else {
            layers = new Drawable[2];
            layers[0] = r.getDrawable(res);
            layers[1] = r.getDrawable(R.drawable.auto_reply_overlay);
        }
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        return layerDrawable;
    }*/
    public String getIMEI() {
        TelephonyManager mngr = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        return mngr.getDeviceId();// unique ID; IMEI for GSM or ESN for CDMA;
        // does not change with SIM
    }

    public String getIMSI() {
        TelephonyManager mngr = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        return mngr.getSubscriberId();// IMSI - change with SIM
    }

    public String getSIMSerial() {
        TelephonyManager mngr = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        return "0";
        //commented 20SEP2022 for permission
        //return mngr.getSimSerialNumber();// SIM# - change with SIM
    }

    public String getOldSIMSerial() {
        return SignupProfile.getPreference(mContext,
                mContext.getString(R.string.profile_sim_serial_number));
    }

}
