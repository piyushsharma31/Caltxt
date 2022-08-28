package com.jovistar.commons.bo;

import java.io.Serializable;
import java.util.HashMap;

import android.graphics.Color;
import android.util.Log;

//import com.ae.caltxt.images.Icons;

/*
 * Phone book 
 */
public class XMob implements IDTObject, Serializable {
	private static final String TAG = "XMob";

	public static final long serialVersionUID = 3L;
	public static String STRING_STATUS_DND = "Do not disturb";
	public static String STRING_STATUS_AVAILABLE = "Available";
	public static String STRING_STATUS_AWAY = "Away";
	public static String STRING_STATUS_BUSY = "Busy";
	public static String STRING_STATUS_OFFLINE = "Offline";
//	public static String STRING_STATUS_BUSY_ONCALL = "Busy, on call";
	public static String STRING_STATUS_DEFAULT = STRING_STATUS_AVAILABLE;
	public static String IMAGE_FILE_EXTN = ".jpg";

	public static String STRING_STATUS_MEETING = "In meeting";
	public static String STRING_STATUS_DINING = "Dining";

	private long persistenceId=-1;
	long modified;//TOD when ONLINE

	/* 
	*	"status" specifies icon to use
	*	icon resource(>2) - this icon res used when mqtt connected
	*	unregistered(2) - default value
	*	available(1) - update from mqtt message from Caltxt
	*	offline(0) - update from ccm server
	*/
	public static short STATUS_OFFLINE = 0x0000;
	public static short STATUS_AVAILABLE = 0x0001;
	public static short STATUS_UNREGISTERED = 0x0002;//DO NOT CHANGE VALUE ASSIGNED
	public static short STATUS_AWAY = 0x0004;
	public static short STATUS_BUSY = 0x0008;
	public static short STATUS_DND = 0x0010;
	public static short STATUS_BLOCKED = 0x0100;
	public static short AUTO_RESPONSE = 0x1000;
//	int AUTO_RESPONSE_RESET = 0xefff;
//	int MASK_DND = 0x00f0;
	int MASK_BLOCKED = 0x0f00;
	int MASK_STATUS = 0x00ff;
	int MASK_AUTO = 0xf000;

	/*
	 * 0000 0000 0000 0000 (status OFFLINE)
	 * 0000 0000 0000 0001 (status AVAILABLE)
	 * 0000 0000 0000 0010 (status UNREGISTERED)
	 * 0000 0000 0000 0100 (status AWAY)
	 * 0000 0000 0000 1000 (status BUSY)
	 * 0000 0000 0001 0000 (status DND)
	 * 
	 * 0000 0001 0000 0000 (BLOCKED)
	 *
	 * 0001 0000 0000 0000 (Automatic RESPONSE MODE SET)
	 */
	private int status=STATUS_UNREGISTERED;//default value; 'status' can have any of above values ONLY

	private String name="";//header
	private String uname="";
	private String number="";//subject
	private String number2="";//sim 2 number
	private String headline = "";//(24-May-15) STRING_STATUS_DEFAULT;//body
	private String occupation="";// caller occupation, body
	private String place="";// caller city, footer
//	private String imei;

	public XMob() {
		modified = 0;
	}

	public boolean has(String fqdn) {
		if(fqdn==null || fqdn.length()==0)
			return false;

        return fqdn.equals(uname) || fqdn.equals(number2);
    }

	public XMob(String name, String number, String countrycode) {
		this.name = name;
		this.number = number;
		this.number2 = "";
		this.headline = "";//24-May//STRING_STATUS_DEFAULT;
/*
		//construct user name (fully qualified mobile number) from number
		while(uname.startsWith("0") && uname.length()>0)
			uname = uname.substring(1);
		uname = uname.replaceAll("[-+]", "");
		if(uname.length()==10)
			uname = countrycode + uname;*/
		this.uname = toFQMN(number, countrycode);
//		Log.v("XMob:XMob ", toString());
	}

	public boolean isRegistered() {
		return !((status & MASK_STATUS)==STATUS_UNREGISTERED);
	}

	public boolean isAvailable() {
		return (status & MASK_STATUS)==STATUS_AVAILABLE;
	}

	public boolean isBusy() {
		return (status & MASK_STATUS)==STATUS_BUSY;
	}

	public boolean isAway() {
		return (status & MASK_STATUS)==STATUS_AWAY;
	}

	public boolean isOffline() {
		return (status & MASK_STATUS)==STATUS_OFFLINE;
	}

	public boolean isDND() {
		return (status & MASK_STATUS)==STATUS_DND;
	}

	public boolean isAutoResponding() {
		return (status & MASK_AUTO)==AUTO_RESPONSE;
	}

	public boolean isBlocked() {
		return (status & MASK_BLOCKED)==STATUS_BLOCKED;
	}

	public void setStatusUnregistered() {
		headline = "";
		status = (status & 0xff00) | STATUS_UNREGISTERED;
	}

	public void setStatusAvailable() {
		status = (status & 0xff00) | STATUS_AVAILABLE;
	}

	public void setStatusBusy() {
		status = (status & 0xfff00) | STATUS_BUSY;
	}

	public void setStatusAway() {
		status = (status & 0xff00) | STATUS_AWAY;
	}

	public void setStatusOffline() {
//		status = (status & 0x0f00/*reset the automatic response status also*/) | STATUS_OFFLINE;
		status = (status & 0xff00) | STATUS_OFFLINE;
	}
/*
	public void resetStatusDND() {
		status = (status & 0xff0f);
	}
*/
	public void setStatusDND() {
		status = (status & 0xff00) | STATUS_DND;
	}

	/* status received from remote user 
	 * update only online, auto response part
	 * keep intact any local status : blocked
	 * */
	public void setStatusOnline(int s) {
		status = (status ) | (s & MASK_STATUS) | (s & AUTO_RESPONSE);
	}

	public void setStatusAutoResponding() {
		status = (status & 0x0fff) | AUTO_RESPONSE;
	}

	public void resetStatusAutoResponding() {
		status = (status & 0x0fff);
	}

	public void setBlocked() {
		status = (status & 0xf0ff) | STATUS_BLOCKED;
	}

	public void setUnblocked() {
		status = status & 0xf0ff;
	}

	public void setModified(long mod) {
		modified = mod;
	}

	public long getModified() {
		return modified;
	}

	/* 
	 * toFQMN - Covert a mobile number to
	 * Fully Qualified Mobile Number
	 * with prefixed country code
	 */
	public static String toFQMN(String number, String countrycode) {

		String fqmn = number;
		while(fqmn.startsWith("0") && fqmn.length()>0)
			fqmn = fqmn.substring(1);
		fqmn = fqmn.replaceAll( "[^\\d]", "" );//removes all non-numeric char
		if(fqmn.length()==10)
			fqmn = countrycode + fqmn;
		return fqmn;
	}

	public void init(String str) {
		Log.v("XMob", "init "+str);
		String[] tokens = str.split("\\^", -1);
		int i = 0;

		String objtype = tokens[i++];//should be equal to serialVersionUID
		if(serialVersionUID!=Long.parseLong(objtype))
			Log.e("XMob", str+", NOT A XMOB OBJECT TYPE");
		name= tokens[i++];
		number = uname = tokens[i++];
		headline = tokens[i++].trim();
		if(headline.length()==0)
			headline = STRING_STATUS_DEFAULT;
		try{
			status = Integer.parseInt(tokens[i++]);
		}catch(NumberFormatException e){
		}
/*		if(headline.trim().toLowerCase().equals("offline"))
			status = 0;
		else
			status = 1;*/
		occupation = tokens[i++];// uncommented 10-JUL-17, dont know why was commented
		place = tokens[i++];// uncommented 10-JUL-17, dont know why was commented
//		uname = tokens[i++];
//		status = Short.parseShort(tokens[i++]);//ONLY REQUIRED IN CCW commands; not in MQTT
//		persistenceId = Long.parseLong(tokens[i++]);//NOT REQUIRED IN MQTT
		number2 = tokens[i++];//sim 2. // uncommented 10-JUL-17, dont know why was commented
	}

//	public CharSequence[] getNumbers() {
		/*		int sz=0;
		if(number_1.length()>0)
			sz++;
		if(number_2.length()>0)
			sz++;
		if(number_3.length()>0)
			sz++;*/
//		CharSequence sb[] = new CharSequence[1];
//		if(number.length()>0)
//			sb[0]=(number);
/*		if(number_2.length()>0)
			sb[1]=(number_2);
		if(number_3.length()>0)
			sb[2]=(number_3);*/
//		return sb;
//	}
/*
	public String getNumber_1() {
		return (number_1);
	}
	public String getNumber_2() {
		return (number_2);
	}
	public String getNumber_3() {
		return (number_3);
	}
	public void setNumber_1(String s) {
		number_1 = s;
	}
	public void setNumber_2(String s) {
		number_2 = s;
	}
	public void setNumber_3(String s) {
		number_3 = s;
	}
*/
	public String getNumber() {
		if(number==null || number.trim().length()==0) {
			return "+" + uname;
		}
		return (number);
	}

//	public void setNumber(String s, String countrycode) {//commented 20-NOV-16. WHY country code here
	public void setNumber(String s) {
		number = s;
//		uname = toFQMN(number, countrycode);
	}

	public String getNumber2() {
		if(number2==null || number2.trim().length()==0) {
			return "";
		}
		return (number2);
	}

	public void setNumber2(String s) {
		number2 = s;
	}

	public String getUsername() {
		return uname;
	}

	public void setUsername(String cllr) {
		uname = cllr;
	}

	public String getHeadline() {
		return headline;
	}

	public void setHeadline(String cllr) {
		headline = cllr;
	}

	public String getName() {
		if(name==null || name.trim().length()==0) {
			return getNumber();
		}
		return name;
	}

	public String setName(String cllr) {
		return name = cllr;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int s) {
		status = s;
	}
	/*
	public String getIMEI() {
		return imei;
	}

	public String setIMEI(String im) {
		return imei = im;
	}
*/

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String ocptn) {
		occupation = ocptn;
	}

	public static boolean isXMob(String message) {
        return message.startsWith(String.valueOf(serialVersionUID) + '^')
                || message.startsWith(String.valueOf(serialVersionUID + 1) + '^');
    }

	public String getPlace() {
		return place;
	}

	public void setPlace(String cty) {
		place = cty;
	}

	public String getCName() {
		return "XMob";
	}

	public String getIcon() {
		return uname + IMAGE_FILE_EXTN;
	}

	public void setIcon(String path) {
//		return iconurl = path;//DO NOTHING, since GetIcon always return correct icon name
	}

	public String toString() {
		return (serialVersionUID)+delimiter+(name)+delimiter+(uname)
				+delimiter+(headline)
				+delimiter+(status)
				+delimiter+(occupation)
				+delimiter+(place)
				+delimiter+(getNumber2());
	}

	public String toStringForACK() {//this is used as ACK message to original XMob status msg
		return (serialVersionUID+1)+delimiter+(name)+delimiter+(uname)
				+delimiter+(headline)
				+delimiter+(status)
				+delimiter+(occupation)
				+delimiter+(place)
				+delimiter+(getNumber2());
	}

	@Override
	public String getHeader() {
		return name;
	}

	@Override
	public String getSubject() {
		/*if(status==STATUS_OFFLINE) {
			if(headline.equals(STRING_STATUS_DND) ||
				headline.equals(STRING_STATUS_AVAILABLE) ||
				headline.equals(STRING_STATUS_AWAY) ||
				headline.equals(STRING_STATUS_BUSY) ||
				headline.equals(STRING_STATUS_OFFLINE) ||
				headline.trim().length()==0)
			return STRING_STATUS_DEFAULT;
		} else if(status==STATUS_UNREGISTERED) {
			return "";
		}*/
//		return (headline.length() == 0 ? (getNumber()) : headline);
		return getNumber();
//		return headline;
//		return (number.toString());
/*		StringBuffer sb = new StringBuffer();
		if(number_1.length()>0)
			sb.append(number_1);
		if(number_2.length()>0)
			sb.append(", ").append(number_2);
		if(number_3.length()>0)
			sb.append(", ").append(number_3);
		return sb.toString(); */
	}

	@Override
	public void setSubject(String s) {
		headline = s;
	}

	@Override
	public String getBody() {
		return headline;
	}

	@Override
	public void setBody(String s) {
		headline = s;
	}

	public HashMap<String, Object> extractFields() {

        HashMap<String, Object> ht = new HashMap<String, Object>();
        if (name != null) {
            ht.put("name", name);
        }
        if (number != null) {
            ht.put("number", number);
        }
        if (uname != null) {
            ht.put("uname", uname);
        }
        if (headline != null) {
            ht.put("headline", headline);
        }
/*        if (occupation != null) {
            ht.put("occupation", occupation);
        }
        if (city != null) {
            ht.put("city", city);
        }*/
        ht.put("status", status);

        return ht;
    }

    public long getPersistenceId() {
    	return persistenceId;
    }

    public void setPersistenceId(long id) {
    	persistenceId = id;
    }

	public String searchString() {
		return (name+uname+headline);
	}

	public String toStringX() {
		return toString();
	}

	@Override
	public void populateFields(HashMap<String, Object> ht) {
        name = (String) ht.get("name");
        uname = (String) ht.get("uname");
        number = (String) ht.get("number");
        headline = (String) ht.get("headline");
//        occupation = (String) ht.get("occupation");
//        city = (String) ht.get("city");
        if(ht.get("status")!=null)
        	status = Short.parseShort(((String) ht.get("status")));
	}

	@Override
	public void setHeader(String s) {
		name = s;
	}

	@Override
	public String getFooter() {
		return "";
	}

	@Override
	public void setFooter(String s) {
	}

	@Override
	public Object getKey() {
		return getUsername();
	}

	@Override
	public int getHeaderIconResource() {
		return 0;
	}

	@Override
	public int getHeaderBackground() {
		return 0;
	}

	@Override
	public int getHeaderFontColor() {
		if(isBlocked())
			return (Color.RED);
		else
			return (Color.BLACK);
	}

	@Override
	public int getSubjectIconResource() {
		return 0;
	}

	@Override
	public int getSubjectBackground() {
		return 0;
	}

	@Override
	public int getSubjectFontColor() {
		return (Color.GRAY);
	}

	@Override
	public int getBodyIconResource() {
		return 0;
	}

	public int getBodyBackground() {
		return (0);
//		return (android.R.drawable.screen_background_light_transparent);
	}

	@Override
	public int getBodyFontColor() {
		return (Color.GRAY);
	}

	@Override
	public int getFooterIconResource() {
		return 0;
	}

	@Override
	public int getFooterBackground() {
		return (android.R.drawable.screen_background_light_transparent);
	}

	@Override
	public int getFooterFontColor() {
		return (Color.GRAY);
	}

	@Override
	public int getIconResource() {
		return 0;
	}
}
