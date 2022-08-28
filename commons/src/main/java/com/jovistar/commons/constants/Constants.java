
package com.jovistar.commons.constants;
//import javax.microedition.lcdui.Command;
//import javax.microedition.lcdui.Font;

public class Constants {

	public static boolean wrapon = true;
    private static Constants instance;

    public static final String IMG_FOLDER = "photos";
    public static final String VDO_FOLDER = "videos";
    public static final String DOC_FOLDER = "docs";
    public static final String AUD_FOLDER = "audios";
    public int ERROR = 0;
    public final String[] videofilter = {".3gp", ".mpg", ".mp4"};
    public final String[] imagefilter = {".bmp", ".gif", ".png", ".jpg", ".jpeg"};
    public final String[] documentfilter = {".pdf", ".doc", ".txt", ".csv"};
    public final String[] audiofilter = {".3gpp", ".mp3", ".wav", ".mid", ".amr", ".aac"};
//    public Font newitemfont;//init in StartupJob

    public final char NEWLINE = '\n';
    public final char SPACE_REPLACEMENT = '_';
    public final String FIELD_SEPARATOR = "^";
    public final String OFFERCODESTR = ":ofc:";
    public final String MAX_INBOX_ITEM_ID_CTT = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
    public final String INDIVIDUAL_USER_COMMUNITY = "society|individual";
    public final String BACK_DIR = "..";
    public final short DIRECTORY = 1;
    public final short FILE = 2;

    public final int CONNECTION_TIMEOUT_SEC=15;//seconds
    public final int MAX_INBOX_ITEM_ID=99999999;
    public final int MAXSZ_TOLIST = 2000;
    public final int MAXSZ_LOC = 512;
    public final int MAXSZ_REFRESHRATE = 2;
    public final int MAXSZ_USRNAME = 24;
    public final int MAXSZ_LASTNAME=24;
    public final int MAXSZ_FIRSTNAME=24;
    public final int MAXSZ_MOBILE=10;//considering only india mobile numbers without code
    public final int MAXSZ_TELEPHONE=8;//considering only india telephone numbers without code
    public final int MAXSZ_EMAIL=48;
    public final int MAXSZ_SUBJECT=200;
    public final int MAXSZ_MSGBODY=100;
    public final int MAXSZ_PASSWD=16;
    public final int MAXSZ_COMMENT=32;
    public final int MAXSZ_NOTE=200;
    public final int MAXSZ_STATUS=32;
    public final int MAXSZ_ABOUT=100;
    public final int MAXSZ_COLLEGE=48;
    public final int MAXSZ_SCHOOL=48;
    public final int MAXSZ_TITLE=32;
    public final int MAXSZ_CITY=48;
    public final int MAXSZ_ACTIVATIONCODE=10;
    public final int MAXSZ_REGION=48;
    public final int MAXSZ_STREET=48;

    public final String STR_CONTACTS="contacts";
    public final String STR_CAL="calendar";
    public final String STR_TODO="todo";

    final int COMMAND_PRIORITY = 1;
/*
    public final Command CMD_LOGIN = new Command("sign in", Command.OK, COMMAND_PRIORITY);
    public final Command CMD_EXIT = new Command("exit", Command.EXIT, COMMAND_PRIORITY+2);
    public final Command CMD_YES = new Command("yes", Command.OK, COMMAND_PRIORITY+1);
    public final Command CMD_NO = new Command("no", Command.CANCEL, COMMAND_PRIORITY);
    public final Command CMD_BACK = new Command("back", Command.BACK, COMMAND_PRIORITY);
    public final Command CMD_CANCEL = new Command("cancel", Command.CANCEL, COMMAND_PRIORITY);

    public final Command CMD_START = new Command("start", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_UPDATE = new Command("update", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_IAMHERE = new Command("iam here", Command.OK, COMMAND_PRIORITY + 1);
    //public final Command CMD_CREATE = new Command("create", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_OK = new Command("ok", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_VERIFYCODE = new Command("verify code", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_TERMSOFUSE = new Command("terms of use", Command.OK, COMMAND_PRIORITY + 1);
    //public final Command CMD_SAVE = new Command("save", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_SEND = new Command("send", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_FEEDBACK = new Command("send feedback", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_NEXT = new Command("next", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_PREV = new Command("previous", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_GETCDAGAIN = new Command("get activation code", Command.OK, COMMAND_PRIORITY + 1);
    //public final Command CMD_ADDATTENDEES = new Command("add attendees", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_EDITMSG = new Command("edit", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_SELECT = new Command("select", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_VIEW = new Command("view", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_DELETE = new Command("delete", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_ADD = new Command("add", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_ADDPLACE = new Command("add a new place", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_ACCEPT = new Command("accept", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_DECLINE = new Command("decline", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_COPY = new Command("copy", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_OPEN = new Command("open", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_REPLY = new Command("reply", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_FORWARD = new Command("forward", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_INVITE = new Command("invite", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_CREATEUSER = new Command("sign up", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_FORGOTPASS = new Command("forgot password?", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_BACKUPRESTORE = new Command("backup phone", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_SETTING = new Command("settings", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_ABOUT = new Command("about", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_MYSHARE = new Command("my share", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_MYPRFL = new Command("my profile", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_MYLOC = new Command("my location", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_MYNEWS = new Command("my news", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_CHGLOC = new Command("set location", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_ADDRECIPIENT = new Command("add recipients", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_SEARCH = new Command("search", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_FIND_CONTACTS = new Command("find people", Command.OK, COMMAND_PRIORITY + 1);
    //public final Command CMD_FIND_BIZ = new Command("find business", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_FIND_PLACE = new Command("find place", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_FIND_CALENDAR = new Command("find calendar", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_FIND_EVENT = new Command("find event", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_FULLSTORY = new Command("full story", Command.OK, COMMAND_PRIORITY + 1);

    public final Command CMD_FIND_DEALS = new Command("find deals", Command.OK, COMMAND_PRIORITY + 1);

    public final Command CMD_MARKALL = new Command("mark all", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_UNMARKALL = new Command("unmark all", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_REFRESH = new Command("refresh", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_LOOKUPCTY = new Command("lookup city", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_ALLVIEW = new Command("all view", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_TODAYVW = new Command("today view", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_NXTXVW = new Command("next x days", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_WEEKVW = new Command("week view", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_ALLVW = new Command("all view", Command.SCREEN, COMMAND_PRIORITY + 1);
    //public final Command CMD_MONTHVW = new Command("month view", Command.SCREEN, COMMAND_PRIORITY + 1);

    public final Command CMD_PROFILE = new Command("profile", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_NEW = new Command("new", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_NEWMSG = new Command("start conversation", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_NEWMTG = new Command("create meetup", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_NEWEVT = new Command("create event", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_IMPORT = new Command("import to phone", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_TRAIL = new Command("location", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_UPLOADIMAGE = new Command("upload my photo", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_SHARE = new Command("new share", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_PLAY = new Command("play", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_STOPPLAY = new Command("stop", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_FULLSCREEN = new Command("full screen", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_COMMENT = new Command("write comment", Command.OK, COMMAND_PRIORITY + 1);
    public final Command CMD_UNSHARE = new Command("unshare", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_NEWALBUM = new Command("new album", Command.SCREEN, COMMAND_PRIORITY + 1);
    public final Command CMD_BROWSESHARE = new Command("browse share", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_SHOWIMAGE = new Command("show photo", Command.ITEM, COMMAND_PRIORITY + 1);
    public final Command CMD_CONTACT = new Command("show contact details", Command.ITEM, COMMAND_PRIORITY + 1);
*/
    private Constants() {
    }

    public static Constants getInstance() {

        if (instance == null) {
            instance = new Constants();
        }
        return instance;
    }

}
