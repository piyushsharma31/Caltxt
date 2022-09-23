package com.jovistar.caltxt.bo;

import com.jovistar.commons.bo.IDTObject;

import java.io.Serializable;
import java.util.HashMap;

public class XQrp implements IDTObject, Serializable {

    private static final long serialVersionUID = 8L;

    String qrp;
    long auto_response_end_time;//0==not set as auto response; if >0, specifies time until auto response is active
    int rowId;//db row id

    public XQrp(String s, long i, int id) {
        qrp = s;
        auto_response_end_time = i;
        rowId = id;
    }

    public void setAutoResponseEndTime(long val) {
        auto_response_end_time = val;
    }

    public void setQuickResponseValue(String val) {
        qrp = val;
    }

    public long getAutoResponseEndTime() {
        return auto_response_end_time;
    }

    public int getRowId() {
        return rowId;
    }

    public String getQuickResponseValue() {
        return qrp;
    }

    public String toString() {
        return qrp;
    }

    @Override
    public String searchString() {
        return qrp;
    }

    @Override
    public long getPersistenceId() {
        return rowId;
    }

    @Override
    public void setPersistenceId(long id) {
        rowId = (int) id;
    }

    @Override
    public String getHeader() {
        return qrp;
    }

    @Override
    public void setHeader(String s) {
        qrp = s;
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
    public String getSubject() {
        return null;
    }

    @Override
    public void setSubject(String s) {
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
        return 0;
    }

    @Override
    public String getBody() {
        return null;
    }

    @Override
    public void setBody(String s) {
    }

    @Override
    public int getBodyIconResource() {
        return 0;
    }

    @Override
    public int getBodyBackground() {
        return 0;
    }

    @Override
    public int getBodyFontColor() {
        return 0;
    }

    @Override
    public String getFooter() {
        return null;
    }

    @Override
    public void setFooter(String s) {
    }

    @Override
    public int getFooterIconResource() {
        return 0;
    }

    @Override
    public int getFooterBackground() {
        return 0;
    }

    @Override
    public int getFooterFontColor() {
        return 0;
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
        return "XQrp";
    }

    @Override
    public Object getKey() {
        return getRowId();
    }

    @Override
    public int getIconResource() {
        // TODO Auto-generated method stub
        return 0;
    }
}
