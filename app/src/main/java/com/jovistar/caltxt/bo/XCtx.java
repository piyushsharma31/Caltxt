package com.jovistar.caltxt.bo;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.Log;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.app.Constants;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.caltxt.phone.Blockbook;
import com.jovistar.commons.bo.IDTObject;
import com.jovistar.commons.bo.XMob;

import java.io.Serializable;
import java.util.HashMap;

public class XCtx implements IDTObject, Serializable {

    private static final String TAG = "XCtx";

    private static final long serialVersionUID = 1L;
    // objtyp^name_caller^number_caller^occupation_caller^context_caller^city_caller^name_callee^number_callee^persistenceId^number2_caller
    // 1^Ravi^8860090854^Service Engineer^Refrigerator^Gurgaon^Piyush^9953693002^123

    protected long pid = -1;//persistence id, one who initiated context call
    protected long rpid = -1;//remote persistence id

    public static final short IN_CALL_MISSED = 1;//RINGING->IDLE
    public static final short IN_CALL = 2;//RINGING->OFFHOOK
    public static final short OUT_CALL = 3;//OFFHOOK
    public static final short IN_CALL_BLOCKED = 4;//caller blocked
    public static final short OUT_CALL_DIALED_BUSY = 5;//OFFHOOK->IDLE
    public static final short IN_MESSAGE_REPLY = 6;//ONLY MESSAGE
    public static final short OUT_MESSAGE = 7;//ONLY MESSAGE
    public static final short IN_MESSAGE_RECEIVED = 8;
    public static final short IN_CALL_REPLY = 9;//INCOMING CALL ACK
    public static final short IN_CALL_REPLY_RECEIVED = 10;
    public static final short OUT_MESSAGE_ADMIN = 11;//administrative messages
    public static final short OUT_MESSAGE_AD = 12;//Ad messages
    public static final short IN_CALL_REJECT_AUTOREPLY = 13;//rejected & auto response sent
    public static final short IN_CALL_REJECT_DND = 14;//rejected due to DND mode
    public static final short OUT_MESSAGE_TRIGGER = 15;//alert messages

    String name_caller;// caller name
    String uname_caller;// caller number (topic); set this var with original called number; convert to username to query Addressbook
    String uname2_caller;// caller 2nd number (dual sim); set this var with standby number
    String occupation_caller;// caller occupation
    String context_caller;// caltxt (call context)
    String name_callee;
    String number_callee;// original number called; convert to username to query Addressbook
    String ack_msg;// any return message for the call
    String context_caller_etc;// any additional message with Caltxt, can contain URL to content
    String ack_msg_etc;// any additional message with Ack, can contain URL to content

    String city_caller;// caller city
    long time_of_day_recv;// time of call received / made / time of message sent by caller - ALWAYS SET BY INITIATOR
    long time_of_day_start;// time of call start (answered, off hook) to calculate call duration
    long time_of_day_end;// time of call end (hang up)
    /*
     *  path to profile picture (hold it now! may have to build another business object (BO) for this
     *  as caltxt message should be as small as possible. picture can be sent async during idle time or on demand
     */
//	String iconurl;
    /*
	 *  0-missed,1-received,2-dialed
	 */
    short callState;
    short callOptions = PRIORITY_NORMAL;//default NORMAL priority and not SENT

    /*
     * 0000 0000 0000 0000 (mask, 2 byte short)
     * 0001 0000 0000 0000 (IS CALTXT CALL)
     * 0000 0000 0000 0001 (priority NORMAL)
     * 0000 0000 0000 0010 (priority URGENT)
     * 0000 0000 0000 0100 (priority EMERGENCY)
     * 0000 0000 0000 1000 (SENT)
     * 0000 0000 0001 0000 (DELIVERED)
     * 0000 0000 0001 1000 (MASK_DELIVERY)
     * 0000 0000 0000 0111 (MASK_PRIORITY)
     */
    public static final short SENT = 0x0008;
    public static final short DELIVERED = 0x0010;
    public static final short PRIORITY_NORMAL = 0x0001;
    public static final short PRIORITY_URGENT = 0x0002;
    public static final short PRIORITY_EMERGENCY = 0x0004;
    public static final short IS_CALTXT_CALL = 0x1000;
    public static final short MASK_PRIORITY = 0x1ff8;
//	public static final short MASK_DELIVERY=0x0018;
//	public static final short MASK_IS_CALTXT_CALL=0x0018;

    public XCtx() {
        name_caller = ack_msg = name_callee = uname_caller = uname2_caller = number_callee
                = context_caller = occupation_caller = city_caller = context_caller_etc = ack_msg_etc = "";
		/*time_of_day_start=*/
        time_of_day_recv = System.currentTimeMillis();
    }

    public void setNotSent() {
        callOptions = (short) (callOptions & 0xfff7);
    }

    public static boolean isXCtx(String message) {
        return message.startsWith(String.valueOf(serialVersionUID) + '^');
    }

    public void setRecvToD(long tod) {
        time_of_day_recv = tod;
    }

    public void setStartToD(long tod) {
        time_of_day_start = tod;
//		Log.v(TAG, time_of_day_start+", "+time_of_day_start);
    }

    public void setEndToD(long tod) {
        time_of_day_end = tod;
    }

    /*
        public void touch() {
            time_of_day_start = time_of_day_end = time_of_day_recv = System.currentTimeMillis();
    //		time_of_day_end = System.currentTimeMillis();
            context_caller = "";
        }*/
    public void setCallPriority(short pri) {
        callOptions = (short) (pri | (callOptions & MASK_PRIORITY));
    }

    public short getCallPriority() {
        return (short) (callOptions & (PRIORITY_NORMAL | PRIORITY_URGENT | PRIORITY_EMERGENCY));
    }

    public short getCallOptions() {
        return callOptions;
    }

    public void setCallOptions(short options) {
        callOptions = options;
    }

    public void setCallState(short state) {
        callState = state;
    }

    public boolean isSent() {
        return (callOptions & SENT) == SENT;
    }

    public void setSent() {
        callOptions = (short) (SENT | (callOptions));
    }

    public boolean isCaltxtCall() {
        return (callOptions & IS_CALTXT_CALL) == IS_CALTXT_CALL;
    }

    public void setCaltxtCall() {
        callOptions = (short) (IS_CALTXT_CALL | callOptions);
    }

    public boolean isDelivered() {
        return (callOptions & DELIVERED) == DELIVERED;
    }

    public void setDelivered() {
        callOptions = (short) (DELIVERED | callOptions);
    }

    /*
    public void setDuration(short du) {
        duration = du;
    }

    public short getDuration() {
        return time_of_day_end-time_of_day_start;
    }
*/
    public long getRecvToD() {
        return time_of_day_recv;
    }

    public long getStartToD() {
        return time_of_day_start;
    }

    public long getEndToD() {
        return time_of_day_end;
    }

    public void setCaltxt(String caltxt) {
        this.context_caller = caltxt;
    }

    // initialize this object from MQTT payload string
    public void init(String str) {
        String[] tokens = str.split("\\^", -1);
//		Log.v(TAG, str+", "+tokens.length);
        int i = 0;

        String objtype = tokens[i++];//should be equal to serialVersionUID
        if (serialVersionUID != Long.parseLong(objtype))
            Log.e(TAG, str + ", NOT A XCTX OBJECT TYPE");
        name_caller = tokens[i++];
        uname_caller = tokens[i++];
        occupation_caller = tokens[i++];
        context_caller = tokens[i++];
        city_caller = tokens[i++];
        name_callee = tokens[i++];
        number_callee = tokens[i++];
        ack_msg = tokens[i++];
        try {
            pid = Long.parseLong(tokens[i++]);
            rpid = Long.parseLong(tokens[i++]);
        } catch (NumberFormatException e) {

        }
/*		if(getNumberCallee().endsWith(Addressbook.getMyProfile().getNumber()))
			callState = IN_CALL_RECEIVED;
		else
			callState = OUT_CALL_DIALED;*/
        callOptions = Short.parseShort(tokens[i++]);
        callState = Short.parseShort(tokens[i++]);
        time_of_day_recv = Long.parseLong(tokens[i++]);
        uname2_caller = tokens[i++];
        try {
            context_caller_etc = tokens[i++];
        } catch (IndexOutOfBoundsException e) {
            // ignore, older version dont have etc1
        }
        try {
            ack_msg_etc = tokens[i++];
        } catch (IndexOutOfBoundsException e) {
            // ignore, older version dont have etc1
        }
    }

    @Override
    public String getHeader() {
        String header = "";
//		if(Addressbook.getMyProfile().getUsername().equals(uname_caller)
//				|| XMob.toFQMN(Addressbook.getMyProfile().getNumber2(), CaltxtApp.getMyCountryCode()).equals(uname_caller)) {
        if (Addressbook.isItMe(uname_caller)) {
            header = getNameCallee();
//				+(occupation_caller.trim().length()!=0?", "+occupation_caller:"")
//				+(city_caller.trim().length()!=0?", "+city_caller:"");
        } else {
            header = getNameCaller();
//				+(occupation_caller.trim().length()!=0?", "+occupation_caller:"")
//				+(city_caller.trim().length()!=0?", "+city_caller:"");
        }

        try {
            Float.parseFloat(header);
            return /*"+"+*/header;
        } catch (NumberFormatException nfe) {
            return header;
        }
    }

    public void setCallerSelf(XMob myProfile, String countryCode) {
        name_caller = myProfile.getName();
        uname_caller = myProfile.getUsername();
        uname2_caller = XMob.toFQMN(myProfile.getNumber2(), countryCode);
        occupation_caller = myProfile.getOccupation();
//		city_caller = Addressbook.getMyProfile().getCity();
//		iconurl = Addressbook.getMyProfile().getIcon();
        callState = OUT_CALL;
    }

    public String getUsernameCaller() {
        return uname_caller;
    }

    public String getUsername2Caller() {
        return uname2_caller;
    }

    public String getNumberCallee() {
        return number_callee;
    }

    //set the calling number as usernamecaller and standby number as username2caller
    public void setUsernameCaller(String uname) {
		/*if(uname2_caller.length()>0) {
			if(uname.equals(uname2_caller)) {
				String temp = uname_caller;
				uname_caller = uname;//XMob.toFQMN(num, Globals.getMyCountryCode());
				uname2_caller = temp;
			} else {
				uname_caller = uname;//XMob.toFQMN(num, Globals.getMyCountryCode());
			}
		} else {
			uname_caller = uname;//XMob.toFQMN(num, Globals.getMyCountryCode());
		}*/
        uname_caller = uname;//XMob.toFQMN(num, Globals.getMyCountryCode());
    }

    public void setUsername2Caller(String uname) {
        uname2_caller = uname;//XMob.toFQMN(num, Globals.getMyCountryCode());
    }

    public void setNumberCallee(String uname) {
        number_callee = uname;//XMob.toFQMN(num, Globals.getMyCountryCode());
    }

    public String getAck() {
        return ack_msg;
//		return ack_msg.length()==0?"(no ack)":ack_msg;
    }

    public void setAck(String ck) {
        ack_msg = ck;
    }

    public String getNameCaller() {//always return from addressbook (assumed its always updated)
		/*if(name_caller.length()==0)
			return number_caller;
		else
			return name_caller;*/
//		return Addressbook.get().getContactName(uname_caller);
//		String name = Addressbook.get().getName(uname_caller);
//		if(name==null)
        return name_caller;
//		else
//			return name;
//		return name_caller;//add 12-Mar-2016
    }

    public String getNameCallee() {//always return from addressbook (assumed its always updated)
		/*if(name_callee.length()==0)
			return number_callee;
		else
			return name_callee;*/
//		return Addressbook.get().getContactName(number_callee);
//		String name = Addressbook.getInstance(context).getName(number_callee);
//		if(name==null)
        return name_callee;
//		else
//			return name;
//		return name_callee;//add 12-Mar-2016
    }

    public String setNameCaller(String cllr) {
        return name_caller = cllr;
    }

    public String setNameCallee(String cllr) {
        return name_callee = cllr;
    }

    public String getOccupation() {
        return occupation_caller;
    }

    public void setOccupation(String ocptn) {
        occupation_caller = ocptn;
    }

    public String getRecvToDString(Context context) {
		/*		long now = System.currentTimeMillis();
		if(DateUtils.isToday(time_of_day_start)) {
			if(now-time_of_day_start<3600000) {
				return  DateUtils.getRelativeTimeSpanString(time_of_day_start, now, DateUtils.MINUTE_IN_MILLIS).toString();
			} else {
				return DateUtils.formatDateTime(context, time_of_day_start, DateUtils.FORMAT_SHOW_TIME);
			}
		}
		return DateUtils.formatDateTime(context, time_of_day_start, DateUtils.FORMAT_ABBREV_ALL
				&DateUtils.FORMAT_SHOW_TIME
				&DateUtils.FORMAT_SHOW_DATE);*/
        return DateUtils.getRelativeDateTimeString(context, time_of_day_recv,
                DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME).toString();
    }

    public String getCity() {
        return city_caller;
    }

    public void setCity(String cty) {
        city_caller = cty;
    }

    public String getCaltxt() {
        return context_caller;
    }

    public String getCName() {
        return "XCtx";
    }

    @Override
    public String getIcon() {
//		if(Addressbook.getMyProfile().getUsername().equals(uname_caller)
//				|| XMob.toFQMN(Addressbook.getMyProfile().getNumber2(), CaltxtApp.getMyCountryCode()).equals(uname_caller)
//				/*callState==OUT_CALL_DIALED || callState==OUT_MESSAGE || callState==OUT_CALL_DIALED_BUSY*/)
        String ucallee = XMob.toFQMN(number_callee, Addressbook.getMyCountryCode());
        String ucaller = XMob.toFQMN(uname_caller, Addressbook.getMyCountryCode());
        if (Addressbook.isItMe(ucaller))
            return ucallee + Constants.IMAGE_FILE_EXTN;
        else
            return ucaller + Constants.IMAGE_FILE_EXTN;
    }

    public void setIcon(String path) {
//		return iconurl = path;//DO NOTHING, since GetIcon always return correct icon name
    }

    public short getCallState() {
        return callState;
    }

    @Override
    public String searchString() {
//		if(Addressbook.getMyProfile().getUsername().equals(uname_caller)
//				|| XMob.toFQMN(Addressbook.getMyProfile().getNumber2(), CaltxtApp.getMyCountryCode()).equals(uname_caller)
//				/*callState==IN_CALL_MISSED || callState==IN_CALL_RECEIVED*/)
        if (Addressbook.isItMe(uname_caller))
            return (getNameCallee() + context_caller + getNumberCallee() + ack_msg);
        else
            return (getNameCaller() + context_caller + getUsernameCaller() + ack_msg);
    }
/*
	@Override
	public String toString() {
		return serialVersionUID
				+"Caltxt call recieved from\t"+(getNameCaller())
				+"("+(uname_caller)
				+")\t"+(occupation_caller)
				+", subject\t"+(context_caller)
				+"from\t"+(city_caller)
				+", to\t"+(getNameCallee())
				+"("+(number_callee)
				+")"+(ack_msg)
				+", id\t"+(pid)
				+", rid\t"+(rpid)
				+", option\t"+(callOptions)
				+", state\t"+(callState)
				+", at\t"+(time_of_day_recv);
	}
*/

    @Override
    public String toString() {
        return (
//				(IN_MESSAGE==callState || OUT_MESSAGE==callState)?5:
                serialVersionUID)
                + delimiter + (getNameCaller())
                + delimiter + (XMob.toFQMN(uname_caller, Addressbook.getMyCountryCode()))
//				+delimiter+(uname_caller)
                + delimiter + (occupation_caller)
                + delimiter + (context_caller)
                + delimiter + (city_caller)
                + delimiter + (getNameCallee())
                + delimiter + (XMob.toFQMN(number_callee, Addressbook.getMyCountryCode()))
//				+delimiter+(number_callee)
                + delimiter + (ack_msg)
                + delimiter + (pid)
                + delimiter + (rpid)
                + delimiter + (callOptions)
                + delimiter + (callState)
                + delimiter + (time_of_day_recv)
                + delimiter + (uname2_caller)
                + delimiter + (context_caller_etc)
                + delimiter + (ack_msg_etc);
    }

    /*public String toStringX() {
        return toString().concat(delimiter).concat(Long.toString(pid))
        .concat(delimiter).concat(Long.toString(time_of_day_recv))
        .concat(delimiter).concat(Long.toString(time_of_day_end-time_of_day_start))
//		.concat(delimiter).concat(iconurl)
        .concat(delimiter).concat(Short.toString(callState));
    }*/
/*
    public HashMap<String, String> extractFields() {

        HashMap<String, String> ht = new HashMap<String, String>();
        ht.put("name_caller", name_caller);
        ht.put("name_callee", name_callee);
        ht.put("number_caller", number_caller);
        ht.put("number_callee", number_callee);
        ht.put("context_caller", context_caller);
        ht.put("occupation_caller", occupation_caller);
        ht.put("city_caller", city_caller);
        ht.put("iconurl", iconurl);
        ht.put("time_of_day", String.valueOf(time_of_day));
        ht.put("callState", String.valueOf(callState));
        ht.put("duration", String.valueOf(duration));

        return ht;
    }

    public void populateFields(HashMap<String, String> ht) {

    	name_caller=ht.get("name_caller");
    	name_callee=ht.get("name_callee");
    	number_caller=ht.get("number_caller");
    	number_callee=ht.get("number_callee");
    	context_caller=ht.get("context_caller");
    	occupation_caller=ht.get("occupation_caller");
    	city_caller=ht.get("city_caller");
    	iconurl=ht.get("iconurl");
    	time_of_day=Long.valueOf((String)ht.get("time_of_day"));
    	callState=Short.valueOf(ht.get("callstate"));
    	duration=Short.valueOf(ht.get("duration"));
        //print();
    }
*/
    public long getPersistenceId() {
        return pid;
    }

    public void setPersistenceId(long id) {
        pid = id;
    }

    public long getRemotePersistenceId() {
        return rpid;
    }

    public void setRemotePersistenceId(long id) {
        rpid = id;
    }

    @Override
    public String getSubject() {
        return "";
    }

    public String getSubject(Context context) {
        return (getRecvToDString(context)
                + (getDurationString().length() == 0 ? "" : ", " + getDurationString()));
        //return getDurationString();
    }

    public int getSubjectIconResource() {
        if (getCallState() == XCtx.OUT_CALL) {//outgoing context call
            return (R.drawable.outcall);
        } else if (getCallState() == XCtx.IN_CALL_MISSED) {//incoming call missed
            return (R.drawable.missedcall);
        } else if (getCallState() == XCtx.IN_CALL_REJECT_AUTOREPLY
                || getCallState() == XCtx.IN_CALL_REJECT_DND) {//incoming call rejected (DND or Auto pilot)
            return (R.drawable.rejectcall);
        } else if (getCallState() == XCtx.IN_CALL_BLOCKED) {//incoming call blocked (auto response or DND or blocked)
//			if(dark)
//				return (R.drawable.ic_block_white_24dp);
//			else
            return (R.drawable.ic_block_black_24dp);
        } else if (getCallState() == XCtx.IN_CALL) {//incoming context call
            return (R.drawable.incall);
        } else if (getCallState() == XCtx.IN_CALL_REPLY) {//incoming call acknowledged with message
            return (R.drawable.rejectcall);
        } else if (getCallState() == XCtx.IN_CALL_REPLY_RECEIVED) {//outgoing call acknowledged with message
            return (R.drawable.outcallbusy);
        } else if (getCallState() == XCtx.IN_MESSAGE_REPLY
                || getCallState() == XCtx.IN_MESSAGE_RECEIVED
                || getCallState() == XCtx.OUT_MESSAGE
                || getCallState() == XCtx.OUT_MESSAGE_TRIGGER
                || getCallState() == XCtx.OUT_MESSAGE_ADMIN
                || getCallState() == XCtx.OUT_MESSAGE_AD) {
//			if(uname_caller.equals(Addressbook.getMyProfile().getUsername())) {
			/*30APR17, commented, why show message ICON
			if(Addressbook.isItMe(uname_caller)) {
				if(dark)
					return (R.drawable.ic_message_out_white_24dp);
				else
					return (R.drawable.ic_message_out_grey_24dp);
			} else {
				if(dark)
					return (R.drawable.ic_message_white_24dp);
				else
					return (R.drawable.ic_message_grey_24dp);
			}*/
            return 0;
        } else {
//			if(uname_caller.equals(Addressbook.getMyProfile().getUsername())) {
            if (Addressbook.isItMe(uname_caller)) {
                return (R.drawable.outcall);
            } else {
                return (R.drawable.incall);
            }
//			return (R.drawable.ic_action_star_0);
        }
    }

    private String getDurationString() {
        return "";
		/* since duration is calculted from time OFFHOOK, so
		 * dont show duration until find solution to this 
		 * problem
		 
		if(time_of_day_start==0)
			return "";
		long milliseconds = time_of_day_end-time_of_day_start;
		int seconds = (int) (milliseconds / 1000) % 60 ;
		int minutes = (int) ((milliseconds / (1000*60)) % 60);
		int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		return (hours>0?Integer.toString(hours)+" hours ":"")
				+(minutes>0?Integer.toString(minutes)+" min ":"")
				+(seconds>0?Integer.toString(seconds)+" sec":"");*/
    }

    public String getBodyPicURL() {
        if (Addressbook.isItMe(uname_caller)) {
            return getAckEtc();
        } else {
            if (getCallState() == XCtx.OUT_MESSAGE_TRIGGER) {
                return getCaltxtEtc();
            } else if (getCallState() == XCtx.OUT_MESSAGE_ADMIN) {
                return getCaltxtEtc();
            } else {
                return getCaltxtEtc();
            }
        }
    }

    @Override
    public String getBody() {
//		if (!(Addressbook.getMyProfile().getUsername().equals(uname_caller)
//				|| XMob.toFQMN(Addressbook.getMyProfile().getNumber2(), CaltxtApp.getMyCountryCode()).equals(uname_caller))) {
        if (Addressbook.isItMe(uname_caller)) {
            return getAck();
        } else {
            if (getCallState() == XCtx.OUT_MESSAGE_TRIGGER) {
                return "<i>" + getCaltxt() + "</i>";
            } else if (getCallState() == XCtx.OUT_MESSAGE_ADMIN) {
                return "<b>" + getOccupation() + "</b>, " + getCaltxt();
            } else {
                return getCaltxt();
            }
        }
    }

    @Override
    public void setSubject(String s) {
    }

    @Override
    public void setBody(String s) {
    }

    @Override
    public void populateFields(HashMap<String, Object> table) {
    }

    @Override
    public HashMap<String, Object> extractFields() {
        return null;
    }

    @Override
    public void setHeader(String s) {
    }

    public String getFooterPicURL() {
        if (Addressbook.isItMe(uname_caller)) {
            if (getCallState() == XCtx.OUT_MESSAGE_TRIGGER) {
                return getCaltxtEtc();
            } else if (getCallState() == XCtx.OUT_MESSAGE_ADMIN) {
                return getCaltxtEtc();
            } else {
                return getCaltxtEtc();
            }
        } else {
            return getAckEtc();
        }
    }

    @Override
    public String getFooter() {
//		if (Addressbook.getMyProfile().getUsername().equals(uname_caller)
//				|| XMob.toFQMN(Addressbook.getMyProfile().getNumber2(), CaltxtApp.getMyCountryCode()).equals(uname_caller)) {
        if (Addressbook.isItMe(uname_caller)) {
            if (getCallState() == XCtx.OUT_MESSAGE_TRIGGER) {
                return "<i>" + getCaltxt() + "</i>";
            } else if (getCallState() == XCtx.OUT_MESSAGE_ADMIN) {
                return "<b>" + getOccupation() + "</b>, " + getCaltxt();
            } else {
                return getCaltxt();
            }
        } else {
            return getAck();
        }
    }

    @Override
    public void setFooter(String s) {
    }

    @Override
    public Object getKey() {
        return getPersistenceId();
    }

    @Override
    public int getHeaderIconResource() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHeaderBackground() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHeaderFontColor() {
        return Color.BLACK;
    }

    public int getHeaderFontColor(Context context) {
        boolean isblocked = false;
//		if(Addressbook.getMyProfile().getUsername().equals(uname_caller)
//				|| XMob.toFQMN(Addressbook.getMyProfile().getNumber2(), CaltxtApp.getMyCountryCode()).equals(uname_caller)) {
        if (Addressbook.isItMe(uname_caller)) {
            if (Blockbook.getInstance(context).get(number_callee) != null) {
                isblocked = true;
            }
        } else {
            if (Blockbook.getInstance(context).get(uname_caller) != null) {
                isblocked = true;
            }
        }
        if (isblocked)
            return (Color.RED);
        else {
//			return context.getResources().getColor(R.color.darkgreen);
            return (Color.BLACK);
        }
    }

    @Override
    public int getSubjectBackground() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getSubjectFontColor() {
        return (Color.GRAY);
    }

    @Override
    public int getBodyIconResource() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getBodyBackground() {
//		if(getCallPriority()==XCtx.PRIORITY_EMERGENCY) {
//			if(!getUsernameCaller().equals(Addressbook.getMyProfile().getUsername())) {
//			if(!Addressbook.isItMe(getUsernameCaller())) {
//				return (R.drawable.caltxtto_sos_selector);
//				return (R.drawable.calloutto_red_white_text);
//				holder.textViewBody.setTextColor(Color.WHITE);
//			} else {
//				holder.textViewBody.setTextColor(Color.BLACK);
//				return (R.drawable.caltxtto_selector);
//				return (R.drawable.calloutto_trans_white_black_text);
//			}
//		} else {
//			holder.textViewBody.setTextColor(Color.BLACK);
        if (getBody().isEmpty()) {
            if (getCallState() == XCtx.OUT_MESSAGE_TRIGGER) {
                if (Addressbook.isItMe(getUsernameCaller())) {
                    return (R.drawable.caltxtfrom_event_selector);
                } else {
                    return (R.drawable.caltxtto_event_selector);
                }
            } else {
                return 0;//R.drawable.call_selector;//25-DEC-18, commented to zero, to skip background on call entries
            }
        } else {
            if (getCallState() == XCtx.OUT_MESSAGE_TRIGGER) {
                if (Addressbook.isItMe(getUsernameCaller())) {
                    return (R.drawable.caltxtfrom_event_selector);
                } else {
                    return (R.drawable.caltxtto_event_selector);
                }
            } else {
                return (R.drawable.caltxtto_selector);
            }
        }
//			return (R.drawable.calloutto_trans_white_black_text);
//		}
    }

    @Override
    public int getBodyFontColor() {
		/*if(getCallPriority()==XCtx.PRIORITY_EMERGENCY) {
			if(!getUsernameCaller().equals(Addressbook.getMyProfile().getUsername())) {
				return (Color.WHITE);
			} else {
				return (Color.BLACK);
			}
		} else {*/
//			return (Color.GRAY);
//		}
        if (getBody().isEmpty()) {
            return (Color.DKGRAY);
        } else {
            Log.v(TAG, "getBodyFontColor "+getCallPriority());
            if (getCallPriority() == XCtx.PRIORITY_EMERGENCY) {
                if (!Addressbook.isItMe(getUsernameCaller())) {
                    return (Color.RED);
                }
            }
            return (Color.DKGRAY);
        }
    }

    public int getFooterIconResource() {
        if (isDelivered()) {
            return (R.drawable.ic_done_all_white_24dp);
        } else if (isSent()) {
            return (R.drawable.ic_done_white_24dp);
        } else {
            return R.drawable.ic_access_time_white_24dp;
        }
    }

    @Override
    public int getFooterBackground() {
//		if(getCallState() == XCtx.OUT_MESSAGE_TRIGGER) {
//			return (R.drawable.caltxtfrom_event_selector);
//		} else {
        return (R.drawable.caltxtfrom_selector);
//		}
/*		if (isDelivered()) {
//			if(getCallPriority()==XCtx.PRIORITY_EMERGENCY) {
//				if(getUsernameCaller().equals(Addressbook.getMyProfile().getUsername())) {
//				if(Addressbook.isItMe(getUsernameCaller())) {
//					return (R.drawable.caltxtfrom_sos_selector);
//					return (R.drawable.calloutfrom_red_white_text);
//					holder.textViewBody2.setTextColor(Color.WHITE);
//				} else {
//					return (R.drawable.caltxtfrom_selector);
//					return (R.drawable.calloutfrom_trans_lightgreen_black_text);
//					holder.textViewBody2.setTextColor(Color.BLACK);
//				}
//			} else {
				return (R.drawable.caltxtfrom_selector);
//				return (R.drawable.calloutfrom_trans_lightgreen_black_text);
//				holder.textViewBody2.setTextColor(Color.BLACK);
//			}
//			holder.imageViewBody2.setImageResource(Constants.icon_msg_sent);
		} else if(isSent()) {
			return (R.drawable.caltxtfrom_sent_selector);
		} else {
			return (R.drawable.caltxtfrom_undelivered_selector);
//			return (R.drawable.calloutfrom_transparent);
//			holder.imageViewBody2.setImageResource(Constants.icon_msg_sent_not);
		}*/
    }

    @Override
    public int getFooterFontColor() {
		/*if (isDelivered()) {
			if(getCallPriority()==XCtx.PRIORITY_EMERGENCY) {
				if(getUsernameCaller().equals(Addressbook.getMyProfile().getUsername())) {
					return (Color.WHITE);
				} else {
					return (Color.BLACK);
				}
			} else {
				return (Color.BLACK);
			}
		} else {*/
//			return (Color.GRAY);
//		}
        if (getFooter().isEmpty()) {
            return (Color.DKGRAY);
        } else {
            Log.v(TAG, "getFooterFontColor "+getCallPriority());
            if (getCallPriority() == XCtx.PRIORITY_EMERGENCY) {
                if (Addressbook.isItMe(getUsernameCaller())) {
                    return (Color.RED);
                }
            }
            return (Color.DKGRAY);
        }
    }

    @Override
    public int getIconResource() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setCaltxtEtc(String etc) {
        if (etc == null)
            return;
        context_caller_etc = etc;
    }

    public String getCaltxtEtc() {
        return context_caller_etc;
    }

    public void setAckEtc(String etc) {
        if (etc == null)
            return;
        ack_msg_etc = etc;
    }

    public String getAckEtc() {
        return ack_msg_etc;
    }
}
