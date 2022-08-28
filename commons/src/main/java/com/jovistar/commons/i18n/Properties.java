package com.jovistar.commons.i18n;

import com.jovistar.commons.constants.Constants;

////import java.util.Hashtable;
//import javax.microedition.lcdui.Display;
//import javax.microedition.lcdui.Displayable;

public class Properties /*extends Hashtable */{

	private static Properties instance;
	
	public static Properties getInstance(){
		if(instance==null)
			instance=new Properties();
		return instance;
	}
//    public final int icon_list_x = 52;//all icon in all type of list view willbe same size
//    public final int icon_list_y = 52;
    //public final int icon_listwrap_x = 24;
    //public final int icon_listwrap_y = 24;

    /*properties are not stored in .properties file*/
    /*instead made inline in java class, to OBFUSCATE*/
    /*Constant Strings are not obfuscated anyway*/
    public String rskey_username = "unm";
    public String rskey_password = "psd";
    public String rskey_enablesound = "snd";//enable sound
    public String rskey_listsize = "lstsz";//item list max size
    public String rskey_refreshrate = "rfsh";//item refresh rate
    public String rskey_font = "font";//application font
    public String rskey_wrap = "wrap";//application font

    public String label_estd = "established since";
    public String label_bday = "birthday";
    public String label_gender = "gender";
    public String label_male = "male";
    public String label_female = "female";
    public String label_anniv = "anniv";
    public String label_marital = "relationship status";
    public String label_comunity = "community:";
    public String label_school = "school";
    public String label_title = "title";
    public String label_college = "college";
    public String label_about = "about";
    public String label_category = "category";
    public String label_scategory = "sub category";
    public String label_notspecified = "not specified";
    public String label_married = "married";
    public String label_single = "single";
    public String label_business = "business";
    public String label_individual = "individual";
    public String ad_text = "";
    public String info = "information";
    public String info_smscheck_pass = "password sent. please check your registered email";
    public String info_smscheck_actcode = "activation code sent. please check your registered email";
    public String info_processing = "processing";
    public String info_wait = "please wait";
    public String info_norecords = "no match found";
    public String info_encodingunsupportedphone = "unsupported encoding";
    public String info_pimnotsupported = "PIM not supported";
    //public String info_success = "done";
    public String err = "error";
    public String err_restorefail = "restore failed";
    public String err_backupoperfail = "backup op failed";
    public String err_imguploadfail = "image post failed";
    public String err_network = "not connected";
    public String err_importphone = "error importing";
    public String err_opencontactphone = "error opening local contacts";
    public String err_openeventphone = "error opening local events";
    public String err_writeeventphone = "error writing events";
    public String err_writecontactphone = "error writing contacts";
    public String err_connection = "connection error";
    public String err_requestparse = "error creating request";
    public String err_recordstoreerr = "recordstore error";
    public String err_imagecreate = "error creating image";
    public String confirm = "confirmation";
    public String ad_url = "http://www.alerteyes.com";
    public String about_left = "Connect";
    public String about_right = "Central";
    public String title_headlines = "headlines";
    public String title_backupui = "phone backup";
    public String title_activationui = "enter activation code";
    public String title_filebrowser = "file browser";
    public String title_register3 = "sign up 3/3";
    public String title_register2 = "sign up 2/3";
    public String title_register1 = "sign up 1/3";
    public String title_forgotpassui = "forgot password?";
    public String title_locui = "my location";
    public String title_locuimtg = "set location";
    public String title_profileui = "profile";
    public String title_lhui = "location history";
    public String title_placesui = "places";
    public String title_addplaceui = "add a new place";
    public String title_abui = "contacts";
    public String title_contactsui = "contacts";
    public String title_selcontacts = "select recipients";
    public String title_contactui = "contact detail";
//    public String title_msgsui = "conversations";

    ///Inbox item titles///
//    public String inbox_messages = "conversations";
//    public String inbox_meetings = "meetinvites";
//    public String inbox_events = "events";
//    public String inbox_contacts = "contacts";
//    public String inbox_places = "places";
//    public String inbox_feeds = "feeds";
//    public String inbox_search = "search";
//    public String inbox_myshare = "my share";
//    public String inbox_myprofile = "my profile";
//    public String inbox_myloc = "my location";
//    public String inbox_backup = "phone backup";
//    public String inbox_settings = "settings";
/*
//    public String title_searchfilterui = "search filter";
//    public String title_msgui = "message";
    public String title_searchui = "search";
    public String title_settingui = "settings";
    public String title_evtnewui = "new event";
    public String title_evtui = "event";
    public String title_evtsui = "events";
    public String title_calui = "meetinvites";
    public String title_evtsmui = "events month ahead";
    public String title_evtswui = "events week ahead";
    public String title_evtstui = "events today";
    public String title_loginui = "CCm - sign in";
    public String title_mainui = "";
    public String title_mtgnewui = "new meetup";
    public String title_msgnewui = "new conversation";
    public String title_mtgui = "meetup";
    public String title_mtgsui = "meetups";
    public String title_mtgui_today = "meetups today";
    public String title_mtgui_week = "meetups week ahead";
    public String title_mtgui_month = "meetups month ahead";
    public String msg_txt = "message text";*/
    public String info_searchui_reqsent = "request sent";
    public String search_contact = "search people";
    public String search_place = "search places";
    public String search_event = "search events";
    public String search_meeting = "search meetups";
    public String warn = "warning";
    public String warn_warncitylookup = "please lookup city first";
    public String warn_city = "please specify complete city";
    public String warn_cityselect = "please select city from list";
    public String warn_citylookuplen = "lookup name must be atleast 3 letter long";
    public String warn_citylookup = "lookup city does not match selection";
    public String warn_citylookupfirst = "please lookup city first";
    public String warn_jobrunning = "please wait, a job already running";
    public String warn_userexist = "user already registered";
    public String warn_mobexist = "mobile already registered";
    public String warn_emailexist = "email already registered";
    public String warn_category = "please specify category";
    public String warn_scategory = "please specify sub category";
    public String warn_recordstorenotopen = "recordstore not open";
    public String warn_invalidrecordid = "invalid record id";
    public String warn_uidnotsupportedphone = "required field (uid) is not supported by the address book";
    public String warn_namenotsupportedphone = "required name field is not supported by the address book";
    public String warn_setcurrlocation = "please set your current location";
    public String warn_uname_len = "username length must be less than 24 and more than 4 char long";
    public String warn_uname_spl = "username may only contain letters, numbers, underscores and periods";
    public String warn_estd = "please specify establish date";
    public String warn_uname = "please specify username";
    //public String warn_state = "please specify complete state";
    //public String warn_country = "please specify complete country";
    public String warn_newpasswd = "please specify new password";
    public String warn_passwd = "please specify password";
    public String warn_newpasswdmatch = "new passwords do not match";
    public String warn_termsofuse = "please accept terms of use";
    public String warn_oldpasswd = "old password incorrect";
    public String warn_fname = "please specify first name/title 1";
    public String warn_lname = "please specify last name/title 2";
    public String warn_pimsummary = "summary field is not supported";
    public String warn_mobile = "please specify valid mobile number";
    public String warn_phone = "please specify valid phone number";
    public String warn_gender = "please specify gender";
    public String warn_community = "please specify community";
    public String warn_married = "please specify relationship status";
    public String warn_title = "please specify title";
    public String warn_email = "please specify valid email";
    public String warn_birthday = "please specify birthday";
    public String warn_about = "please specify about yourself";
    public String warn_subject = "message is empty";
    public String warn_attendees = "please specify to list";
    public String warn_itemperview = "items per view must be greater than 0";
    public String warn_refrshrate = "refresh rate must be greater than 0";
    public String warn_loc = "please specify location";
    public String warn_set_loc = "please set your location first";
    public String warn_starttime = "please specify start time";
    public String warn_starttimepast = "start time in past";
    public String warn_endtimepast = "end time past start time";
    public String warn_endtime = "please specify end time";
    public String warn_recipient = "please specify recipient";
    public String warn_msgbody = "please specify message body";
    public String warn_code = "please enter the code";
    public String warn_filenotexist = "file does not exists";
    public String info_activationcodesent = "thank you for registering!";
    public String label_termsofuse = "go connectcentral.in for terms of use";
    public String label_acceptterms = "accept terms of use";
    public String conf_exit = "do you really want to exit?";
    public String info_nophoto = "no photo";
    public String label_donotmatter = "does not matter";
    public String title_connecting = "connecting";
    public String title_connected = "connected";
    public String title_reqesting = "requesting";
    public String title_sending = "sending";
    public String title_logingout = "loging out";
    public String title_receiving = "receiving";
    public String title_uploading = "uploading";
    public String title_restoring = "restoring";
    public String title_backingup = "backing up..";
    public String title_complete = "complete";
    //public String title_backingup = "processing backup";
    public String info_font_change = "done, please refresh CCm inbox";
//    public String sys_bg = "0xFFFFFF";
//    public String sys_cache = "12";
//    public String sys_ad_addisplaytime = "5000";
    public String sys_alerttimeout = "3000";
    public String sys_rsname = "ae.ccm";
//    public String sys_about_color_line = "777777";
//    public String sys_about_color_rect = "FFFFFF";
//    public String sys_about_color_roundrect = "CCCCCC";
//    public String sys_server = "10.237.89.187";
//    public String sys_server = "localhost";
//    public String sys_server = "10.0.2.2";//is localhost
//    public String sys_server = "connectcentral.in";
//    public String sys_server = "173.192.211.120";
//    public String sys_port = "8084";
    public static int sys_log = Constants.getInstance().ERROR;
//    public static int sys_log = CCMIDlet.DEBUG;
    //public String sys_dir_icon = "/";
    public static final String SEP_STR = System.getProperty("file.separator");
    public static final char SEP_CHAR = System.getProperty("file.separator").toCharArray()[0];
    //CCW server file separators
    public static final String CCW_SEP_STR = "/";
    public static final char CCW_SEP_CHAR = '/';
    /********BELOW PROPERTIES TO DEFINE JAR RELEASE TARGET PLATFORM********
     * For screen resolutions: 320 x 240, 176 x 144, 176 x 208, 352×416, 240 x 320
     * 416 x 352, 800 x 352, 416 x 352, 
     */
//    public final int icon_list_x = Display.getDisplay(CCMIDlet.instance).getBestImageWidth(Display.LIST_ELEMENT);//list item image x size
  //  public final int icon_list_y = Display.getDisplay(CCMIDlet.instance).getBestImageHeight(Display.LIST_ELEMENT);//list item image y size
    //public final int icon_listwrap_x = Display.getDisplay(CCMIDlet.instance).getBestImageWidth(Display.CHOICE_GROUP_ELEMENT);//list item image x size
    //public final int icon_listwrap_y = Display.getDisplay(CCMIDlet.instance).getBestImageHeight(Display.CHOICE_GROUP_ELEMENT);//list item image y size
    //public String sys_dir_icon = "/icons/24x16/";

    public Properties() {
/*        public String estd = "estd";
        public String bday = "bday";
        public String gender = "sex";
        public String male = "male";
        public String female = "female";
        public String anniv = "anniv";
        public String marital = "marital";
        public String comunity = "community:";
        public String school = "school";
        public String title = "title";
        public String college = "college";
        public String abt = "about";
        public String category = "category";
        public String scategory = "subcategory";
        public String notspecified = "not specified";
        public String married = "married";
        public String single = "single";
        public String business = "business";
        public String individual = "individual";
        public String userexist = "user already registered";
        public String mobexist = "mobile already registered";
        public String ad = "Connect Central is the best!";
        public String info = "information";
        public String confirmation = "confirmation";
        public String err = "error";
        public String errorimagecreate = "error image creation";
        public String warn = "warning";
        public String smsalert = "please check email/sms for activation code!";
        public String processing = "processing";
        public String norecords = "no record found!";
        public String recordstorenotopen = "recordstore not open";
        public String invalidrecordid = "invalid record id";
        public String recordstoreerr = "recordstore error";
        public String uidnotsupportedphone = "required field (uid) is not supported by the address book";
        public String namenotsupportedphone = "required name field is not supported by the address book";
        public String encodingunsupportedphone = "unsupported encoding";
        public String pimnotsupported = "PIM not supported";
        public String errorimportphone = "error importing";
        public String erroropencontactphone = "error opening local contacts";
        public String erroropeneventphone = "error opening local events";
        public String errorwriteeventphone = "error writing events";
        public String errorwritecontactphone = "error writing contacts";
        public String errorconnection = "connection error";
        public String errorrequestparse = "error creating request";
        public String warnsetcurrlocation = "please set your current location";
        public String success = "done";
        public String ad.url = "http://www.alerteyes.com";
        public String networkerr = "not connected";
        public String copyright = "copyright © 2008 Alerteyes";
        public String about.left = "connect";
        public String about.right = "central";
        public String backupui.title = "phone backup";
        public String activationui.title = "enter activation code";
        public String filebrowser.title = "file browser";
        public String register3.title = "create user: step 3";
        public String register2.title = "create user: step 2";
        public String register1.title = "create user: step 1";
        public String forgotpassui.title = "forgot password?";
        public String locui.title = "set location";
        public String locui.titlemtg = "set location";
        public String profileui.title = "profile";
        public String lhui.title = "location history";
        public String abui.title = "contacts";
        public String contactsui.title = "contacts";
        public String contactui.title = "contact detail";
        public String msgsui.title = "messages";
        public String sfilterui.title = "search filter";
        public String msgui.title = "message";
        public String searchui.title = "search";
        public String searchui.reqsent = "request sent";
        public String settingui.title = "settings";
        public String evtnewui.title = "new event";
        public String evtui.title = "event";
        public String evtsui.title = "events";
        public String evtsmui.title = "events -month ahead";
        public String evtswui.title = "events -week ahead";
        public String evtstui.title = "events -today";
        public String loginui.title = "sign in";
        public String mainui.title = "CCm";
        public String mtgnewui.title = "new meetup";
        public String msgnewui.title = "new conversation";
        public String mtgui.title = "meetup";
        public String mtgsui.title = "meetups";
        public String mtgui.today = "meetups -today";
        public String mtgui.week = "meetups -week ahead";
        public String mtgui.month = "meetups -month ahead";
        public String msg.txt = "message text";
        public String filter.cont = "search users";
        public String filter.event = "search events";
        public String filter.mtg = "search meetups";
        public String warning.uname.len = "username length must be less than 24 and more than 4 char long";
        public String warning.uname.spl = "username must be alphanumeric, may contain comma and underscore";
        public String warning.estd = "please specify establish date";
        public String warning.state = "please specify state";
        public String warning.city = "please specify city";
        public String warning.country = "please specify country";
        public String warning.passwd = "please specify password";
        public String warning.termsofuse = "terms of use not accepted";
        public String warning.passwd1 = "old password do not match";
        public String warning.fname = "please specify first name/title 1";
        public String warning.lname = "please specify last name/title 2";
        public String warning.pimsummary = "summary field is not supported";
        public String warning.mobile = "please specify valid mobile number";
        public String warning.gender = "please specify gender";
        public String warning.community = "please specify community";
        public String warning.married = "please specify marital status";
        public String warning.title = "please specify title";
        public String warning.email = "please specify valid email";
        public String warning.birthday = "please specify birthdate";
        public String warning.abt = "please specify about yourself";
        public String warning.subject = "please specify subject";
        public String warning.attendees = "please specify attendees";
        public String warning.loc = "please specify location";
        public String warning.sttime = "please specify start time";
        public String warning.sttimepast = "start time in past";
        public String warning.edtimepast = "end time past start time";
        public String warning.edtime = "please specify end time";
        public String warning.recipient = "please specify recipient";
        public String warning.msgbody = "please specify message body";
        public String activationcodesent = "activation code sent!";
        public String termsofuse = "terms of use";
        public String acceptterms = "i accept, visited connectcentral.in for terms";
        public String warning.code = "please enter the code";
        public String exitconfirm = "do you really want to exit?";
        public String restorefail = "restore failed";
        public String backupoperfail = "backup op failed";
        public String nophoto = "no photo";
        public String imguploadfail = "image post failed";
        public String filenotexist = "file does not exists";
        public String donotmatter = "does'nt matter";
        public String connecting = "connecting";
        public String connected = "connected";
        public String sending = "sending";
        public String receiving = "receiving";
        public String restoring = "restoring";
        public String complete = "complete";
        public String status.backup = "backup";
        public String font.change = "font change will take effect after application restart";
        public String cache.sz = "5";
        public String ui.bg = "0xFFFFFF";
        public String sys.cache = "12";
        public String ad.addisplaytime = "5000";
        public String alerttimeout = "3000";
        public String prefs.rsname = "ae.ccm.preferences";
        public String about.color.line = "777777";
        public String about.color.rect = "FFFFFF";
        public String about.color.roundrect = "CCCCCC";
        public String server = "192.168.1.3";
        //public String server = "localhost";
        public String port = "8080";
        public String debug = "true";
        //public String dir.icon = "/";
        //public String sys.dir.icon = "/icons/24x16/";*/
        //below is always fetched from server
        //sys.ad.fetchinterval
    }
    //TODO: only strings
//    public Object put(Object arg0, Object arg1) {
    //      return super.put(arg0.toString(), arg1.toString());
    //}
/*
    public String get(String name) {
        String value = (String) super.get(name);
//        if (value == null) {
  //          return (parent == null) ? null : parent.get(name);
    //    } else {
            return value;
//        }
    }

    public String get(String name, String def) {
        String value = get(name);
        return (value == null) ? def : String.valueOf(value);
    }

    public boolean getBoolean(String name, boolean def) {
        String value = get(name);
        return (value == null) ? def : value.equalsIgnoreCase("true");
    }

    public int getInt(String name, int def) {
        String value = get(name);
        if (value != null) {
            if (value.startsWith("-")) {
                return -1 * Integer.parseInt(value.substring(1));
            } else {
                return Integer.parseInt(value);
            }
        }

        return def;
    }
*/
/*
    public int getInt(String name, int def, int r) {
        String value = get(name);
        if (value != null) {
            if (value.startsWith("-")) {
                return -1 * Integer.parseInt(value.substring(1), r);
            } else {
                return Integer.parseInt(value, r);
            }
        }

        return def;
    }*/
}
