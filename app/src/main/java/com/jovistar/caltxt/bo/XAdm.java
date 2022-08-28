package com.jovistar.caltxt.bo;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.Log;

import com.jovistar.caltxt.R;
import com.jovistar.caltxt.phone.Addressbook;
import com.jovistar.commons.bo.IDTObject;

import java.util.Calendar;
import java.util.HashMap;

public class XAdm implements IDTObject {
     /* OBJ TYPE(9)^CALLER NAME(Admin)^CALLER NUMBER(000000000000)^CALLEE NAME^CALLEE NUMBER^STATUS */

    private static final String TAG = "XAdm";

    public static final long serialVersionUID = 9L;

    String name_caller, number_caller, name_callee, number_callee, title, msg, url;
    long time_of_day_recv;

//	Context context;
//	public XAdm (Context context) {
//		this.context = context;
//	}

    public void init(String str) {
        String[] tokens = str.split("\\^", -1);
        int i = 0;
        time_of_day_recv = Calendar.getInstance().getTimeInMillis();

        String objtype = tokens[i++];//should be equal to serialVersionUID
        if (serialVersionUID != Long.parseLong(objtype)) {
            Log.e(TAG, str + ", NOT A XADM OBJECT TYPE");
            return;
        }
        name_caller = tokens[i++];
        number_caller = tokens[i++];
        name_callee = tokens[i++];
        number_callee = tokens[i++];
        msg = tokens[i++];
        title = tokens[i++];
        url = tokens[i++];
    }

    public String getMsg() {
        return msg;
    }

    public String getNameCaller() {
        return name_caller;
    }

    public String getNumberCaller() {
        return number_caller;
    }

    public String getNameCallee() {
        return name_callee;
    }

    public String getNumberCallee() {
        return number_callee;
    }

    public void setMsg(String m) {
        msg = m;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String m) {
        url = m;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String m) {
        title = m;
    }

    public void setNameCaller(String n) {
        name_caller = n;
    }

    public void setNumberCaller(String n) {
        number_caller = n;
    }

    public void setNameCallee(String n) {
        name_callee = n;
    }

    public void setNumberCallee(String n) {
        number_callee = n;
    }

    @Override
    public String toString() {
        return (
                serialVersionUID)
                + delimiter + (getNameCaller())
                + delimiter + (getNumberCaller())
                + delimiter + (getNameCallee())
                + delimiter + (getNumberCallee())
                + delimiter + (getMsg())
                + delimiter + (getTitle())
                + delimiter + (getUrl());
    }

    @Override
    public String searchString() {
        return getNameCaller() + getMsg() + getTitle();
    }

    @Override
    public String getSubject() {
        return Long.toString(time_of_day_recv);
    }

    public String getSubject(Context context) {
        return (getRecvToDString(context));
    }

    public String getRecvToDString(Context context) {
        return DateUtils.getRelativeDateTimeString(context, time_of_day_recv,
                DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME).toString();
    }

    @Override
    public String getBody() {
        return title;
    }

    @Override
    public void setSubject(String s) {
    }

    @Override
    public void setBody(String s) {
    }

    @Override
    public long getPersistenceId() {
        return 0;
    }

    @Override
    public void setPersistenceId(long id) {
    }

    @Override
    public void populateFields(HashMap<String, Object> table) {
    }

    @Override
    public HashMap<String, Object> extractFields() {
        return null;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getCName() {
        return TAG;
    }

    @Override
    public String getHeader() {
        return getNameCaller();
    }

    @Override
    public void setHeader(String s) {
        name_caller = s;
    }

    @Override
    public String getFooter() {
        return getMsg();
    }

    @Override
    public void setFooter(String s) {
        msg = s;
    }

    @Override
    public Object getKey() {
        return number_caller;
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
        return 0;
    }

    @Override
    public int getSubjectIconResource() {
//		if(number_caller.equals(Addressbook.getMyProfile().getUsername())) {
//		if(Addressbook.getInstance(context).isItMe(number_caller)) {
//			if(dark){
//				return (R.drawable.ic_message_out_white_24dp);
//			} else {
//				return (R.drawable.ic_message_out_grey_24dp);
//			}
//		} else {
        return (R.drawable.ic_message_white_24dp);
//		}
//		return (R.drawable.time);
    }

    public int getSubjectIconResource(Context context) {
//		if(number_caller.equals(Addressbook.getMyProfile().getUsername())) {
        if (Addressbook.getInstance(context).isItMe(number_caller)) {
//			if(dark){
//				return (R.drawable.ic_message_out_white_24dp);
//			} else {
            return (R.drawable.ic_message_out_grey_24dp);
//			}
        } else {
            return (R.drawable.ic_message_white_24dp);
        }
//		return (R.drawable.time);
    }

    @Override
    public int getSubjectBackground() {
        return 0;
    }

    @Override
    public int getSubjectFontColor() {
        return 0;
    }

    @Override
    public int getBodyIconResource() {
        return 0;
    }

    public int getBodyBackground() {
        return (android.R.drawable.screen_background_light_transparent);
    }

    @Override
    public int getBodyFontColor() {
        return (Color.BLACK);
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
        return (Color.BLACK);
    }

    @Override
    public int getIconResource() {
        // TODO Auto-generated method stub
        return 0;
    }
}
