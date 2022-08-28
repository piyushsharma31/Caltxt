package com.jovistar.commons.facade;

import android.util.Log;

import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XRes;
import com.jovistar.commons.exception.CCMException;
import com.jovistar.commons.threads.JobRunner;
import com.jovistar.commons.threads.ServiceRequestJob;
import com.jovistar.commons.ui.IDisplayObject;

public class ModelFacade {

	private static ModelFacade instance;
    public static String loginuser, thisusersessionid;

    public void setUname(String unm) {
    	loginuser = unm;
    }

    public void setSession(String ssid) {
		thisusersessionid = ssid;
    }

    public static ModelFacade getInstance(){
		if(instance==null){
			instance=new ModelFacade();
//			ServiceRequestJob.init();
		}
		return instance;
	}

    public void setServiceHostPort(String host, int port) {
    	ServiceRequestJob.init(host, port);
    }

    //private CCMIDlet midlet;
/*    XCtt thisusercontact;
    XLoc thisusercurrentlocation;
    XPref thisuserpreferences;
    Vector thisuserlocationhistory;
    String thisusersessionid;
    XAdb thisuseraddressbook;*/
    /*************************************************************************
     ***OPERATION CODES based on operation to be done on object (service) ****
     *************************************************************************/
    short seed = 1;
    public short OP_ADD;// = "delete";
    public short OP_DELETE;// = "delete";
    public short OP_SET;// = "set";
    public short OP_GET;// = "get";
    public short OP_ISEXIST;// = "isexist";
    public short OP_GETNEXT;// = "getnext";
    public short OP_GETPREV;// = "getprev";
    public short OP_GETALL;// = "getall";
    public short OP_SEND;// = "send";
    public short OP_LOGIN;// = "login";
    public short OP_LOGOUT;// = "logout";
    public short OP_SEARCH;// = "search";
    public short OP_SEARCHNEXT;// = "searchnext";
    public short OP_SEARCHPREV;// = "searchprev";
    public short OP_GETITEMSBYDAYS;// = "getitembydays";//get messages for last day
    public short OP_BACKUP;//= "backup";
    public short OP_RESTORE;// = "restore";
    public short OP_SUMMARY;// = "summary";
    public short OP_GETACKCOUNT;// = "get ack count";
    public short OP_ACK;// = "acknowledge inbox item";
    public short OP_BROWSE;// = "browse share files";
    public short OP_NEW;//new share directory
    /*********************************************************
     **** SERVICE CODES based on object to be acted upon ***** 
     *********************************************************/
    public short SVC_INBOX;// = "inbox";
    public short SVC_INBOXITEM;// = "inboxitem";
    public short SVC_MEETING;// = "meeting";
    public short SVC_MESSAGE;// = "message";
    public short SVC_EVENT;// = "event";
    public short SVC_PHONETODO;// = "phonetodo";
    public short SVC_CONTACT;// = "contact";
    public short SVC_PROFILE;// = "profile";
    public short SVC_PREFERENCES;// = "preferences";
    public short SVC_ADDRESSBOOK;// = 6;
    public short SVC_AD;// = "ad";
    public short SVC_USER;// = "user";
    public short SVC_PHONECALENDAR;// = "phonecalendar";
    public short SVC_PHONECONTACT;// = "phonecontact";
    public short SVC_LOCATIONCURR;// = "location";
    public short SVC_LOCHISTORY;// = "locationh";
    public short SVC_COUNTRYLIST;// = "countrylist";
    public short SVC_CITYLIST;// = "citylist";
    public short SVC_IMAGE;// = "image";
    public short SVC_THUMBIMAGE;// = "thumbimage";
    public short SVC_USERIMAGE;// = "userimage";
    public short SVC_PASSWORD;// = "pass";
    public short SVC_CODE;// = "code";
    public short SVC_MOBILE;// = "mobile";
    public short SVC_PLACECAT;
    public short SVC_PLACESUBCAT;
    public short SVC_JAR;
    public short SVC_MOBSITE;
    public short SVC_LOCATIONBYCELL;
    public short SVC_MODIFYDATE;
    public short SVC_STATELIST;
    public short SVC_SHARE;
    public short SVC_CCMSMS;//send sms with link m.connectcentral.in
    public short SVC_ADS;//ads by category
    public short SVC_ADCAT;//ad categories
    public short SVC_ADTITLE;//ad title (feed title)
    public short SVC_FEEDBYURLTITL;//ad by url
    public short SVC_FEEDTITL;//feed by url
    public short SVC_ADDRESSBOOK_PLACES;//PLACES list
    public short SVC_FEEDBACK;//feedback
    public short SVC_MESSAGEDETAIL;// = "conversation trail";
    public short SVC_CALENDAR;// = "user calendar";
    public short SVC_PLACE;// = "a place";
    public short SVC_EMAIL;
    public short SVC_CALTXT_USER;
    public short SVC_FEEDBACK_CALTXT;

    public int INBOX_ITEM_TYPE_MSG = 1;
    public int INBOX_ITEM_TYPE_MTG = 2;
    public int INBOX_ITEM_TYPE_EVT = 3;
    public int INBOX_ITEM_TYPE_CTC = 4;

    public ModelFacade() {

        //midlet = mlet;

        OP_ADD = seed++;
        OP_DELETE = seed++;
        OP_SET = seed++;
        OP_GET = seed++;
        OP_GETNEXT = seed++;
        OP_GETPREV = seed++;
        OP_GETALL = seed++;
        OP_SEND = seed++;
        OP_LOGIN = seed++;
        OP_LOGOUT = seed++;//10
        OP_SEARCH = seed++;
        OP_SEARCHNEXT = seed++;
        OP_SEARCHPREV = seed++;
        OP_BACKUP = seed++;
        OP_RESTORE = seed++;
        OP_SUMMARY = seed++;
        OP_GETACKCOUNT = seed++;
        OP_ACK = seed++;//18
        OP_BROWSE = seed++;//browse share files
        OP_NEW = seed++;//new share directory
        /*********************************************************
         **** SERVICE CODES based on object to be acted upon ***** 
         *********************************************************/
        SVC_INBOX = seed++;//21
        SVC_INBOXITEM = seed++;
        SVC_MEETING = seed++;
        SVC_MESSAGE = seed++;
        SVC_EVENT = seed++;
        SVC_PHONETODO = seed++;
        SVC_CONTACT = seed++;
        SVC_PROFILE = seed++;
        SVC_PREFERENCES = seed++;
        SVC_ADDRESSBOOK = seed++;//30
        SVC_AD = seed++;//31
        SVC_USER = seed++;//32
        SVC_PHONECALENDAR = seed++;
        SVC_PHONECONTACT = seed++;
        SVC_LOCATIONCURR = seed++;
        SVC_LOCHISTORY = seed++;
        SVC_COUNTRYLIST = seed++;//37
        SVC_CITYLIST = seed++;
        SVC_IMAGE = seed++;
        SVC_THUMBIMAGE = seed++;
        SVC_USERIMAGE = seed++;
        SVC_PASSWORD = seed++;
        SVC_CODE = seed++;
        SVC_MOBILE = seed++;
        SVC_PLACECAT = seed++;//45
        SVC_PLACESUBCAT = seed++;
        SVC_JAR = seed++;//47
        SVC_MOBSITE = seed++;
        SVC_LOCATIONBYCELL = seed++;
        SVC_MODIFYDATE = seed++;
        SVC_STATELIST = seed++;//51
        SVC_SHARE = seed++;//52
        SVC_CCMSMS = seed++;//53
        SVC_ADS = seed++;//54
        SVC_ADCAT = seed++;//55
        SVC_ADTITLE = seed++;//56
        SVC_FEEDBYURLTITL = seed++;//57
        SVC_FEEDTITL = seed++;//58
        SVC_ADDRESSBOOK_PLACES = seed++;//59
        SVC_FEEDBACK = seed++;//60
        SVC_MESSAGEDETAIL = seed++;//61
        SVC_CALENDAR = seed++;//62
        SVC_PLACE = seed++;//63
        SVC_EMAIL = seed++;//64
        OP_ISEXIST = seed++;//65
        SVC_CALTXT_USER = seed++;//66
        SVC_FEEDBACK_CALTXT = seed++;

        //THIS MUST BE LAST TO ASSIGN NUMBER
        OP_GETITEMSBYDAYS = seed++;//get item for last x day. this must be highest number
    }

    public /*synchronized*/ void fxAsyncServiceRequest(short servicename,
            short op, IDTObject param, IDisplayObject display) /*20-01-2011:commented throws CCMException */ {
        /*        if(CCMIDlet.instance.serviceJob.busy) {
        CCMIDlet.instance.displayFactory.getProgressUI().showInfo("busy. please wait");
        return;
        }*/
        //CCMIDlet.instance.serviceJob.serviceRequest(servicename, op, param, display);
        ServiceRequestJob p = new ServiceRequestJob(servicename, op, param, display);
//        Log.d("ServiceRequestJob", "created");
        try{
            JobRunner.getInstance().run(p);
//            Log.d("JobRunner", "started");
        }catch(CCMException e){
            e = null;
            p = null;
            return;
        }
//        display.setTitle(Properties.getInstance().title_reqesting);
        Log.d("fxServiceRequest","svc:" + servicename + ", op:" + op);
    }

    public XRes fxServiceRequest(short servicename, short op, IDTObject param) {
//        Log.d("fxServiceRequest", "start");
        ServiceRequestJob p = new ServiceRequestJob(servicename, op, param, null);
//        Log.d("fxServiceRequest","end");
        return p.SyncExecute();
    }

    public String getThisUsername() {
        return loginuser;
    }

    public String getThisUserSessionId() {
        return thisusersessionid;
    }
}
