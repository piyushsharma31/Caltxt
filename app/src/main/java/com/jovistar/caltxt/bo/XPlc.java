package com.jovistar.caltxt.bo;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;

import com.jovistar.caltxt.R;
import com.jovistar.commons.bo.IDTObject;

import java.io.Serializable;
import java.util.HashMap;

public class XPlc implements IDTObject, Serializable {
    private static final long serialVersionUID = 10L;

    public static short NETWORK_TYPE_WIFI = 2;
    public static short NETWORK_TYPE_CELL = 1;

    String cellid;// "40411.129.16571", "09:89:f5:90:11:91,egglpant"
    String status;// Home, Work, Gym
    long time;
    long pid;
    int type;// cell site, wifi site, hybrid etc

    //	Context context;
//	public void XPlc(Context context) {
//		this.context = context;
//	}
    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setCellId(String cid) {
        cellid = cid;
    }

    public String getCellId() {
        return cellid;
    }

    public void setStatus(String sts) {
        status = sts;
    }

    public String getStatus() {
        return status;
    }

    public void setTime(long tm) {
        time = tm;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String searchString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getPersistenceId() {
        // TODO Auto-generated method stub
        return pid;
    }

    @Override
    public void setPersistenceId(long id) {
        // TODO Auto-generated method stub
        pid = id;
    }

    @Override
    public String getHeader() {

        return status;
    }

    @Override
    public void setHeader(String s) {
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
        return (Color.BLACK);
    }

    @Override
    public String getSubject() {
//		try{
//			Float.parseFloat(cellid);
        return cellid;
//		}catch(NumberFormatException nfe){
//			return (cellid.substring(cellid.indexOf(",")+1));
//		}
    }

    @Override
    public void setSubject(String s) {
    }

    @Override
    public int getSubjectIconResource() {
        if (type == NETWORK_TYPE_WIFI)
            return R.drawable.ic_network_wifi_black_24dp;
        else
            return R.drawable.ic_network_cell_black_24dp;
//		try{
//			Float.parseFloat(cellid);
//			return R.drawable.ic_network_cell_black_24dp;
//		}catch(NumberFormatException nfe){
//			return R.drawable.ic_network_wifi_black_24dp;
//		}
    }

    public boolean isWiFi() {
        return (type == NETWORK_TYPE_WIFI);
//		try{
//			Float.parseFloat(cellid);
//			return false;
//		}catch(NumberFormatException nfe){
//			return true;
//		}
    }

    @Override
    public int getSubjectBackground() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getSubjectFontColor() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getBody() {
        return "";
    }

    public String getBody(Context context) {
        return DateUtils.getRelativeDateTimeString(context, time,
                DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME).toString();
    }

    @Override
    public String toString() {
        return cellid + ", " + status + ", " + time + ", " + pid + ", " + type;
    }

    @Override
    public void setBody(String s) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getBodyIconResource() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getBodyBackground() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getBodyFontColor() {
        // TODO Auto-generated method stub
        return (Color.BLACK);
    }

    @Override
    public String getFooter() {
        return "";
    }

    public String getFooter(Context context) {
        return DateUtils.getRelativeDateTimeString(context, time,
                DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME).toString();
    }

    @Override
    public void setFooter(String s) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getFooterIconResource() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getFooterBackground() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getFooterFontColor() {
        // TODO Auto-generated method stub
        return (Color.BLACK);
    }

    @Override
    public void populateFields(HashMap<String, Object> table) {
        // TODO Auto-generated method stub

    }

    @Override
    public HashMap<String, Object> extractFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIcon() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getIconResource() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getCName() {
        return "XPlc";
    }

    @Override
    public Object getKey() {
        // TODO Auto-generated method stub
        return null;
    }

}
