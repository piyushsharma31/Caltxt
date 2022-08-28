package com.jovistar.commons.bo;

import java.util.HashMap;

public interface IDTObject {
	String delimiter = "^";
    String searchString();

    long getPersistenceId();
    void setPersistenceId(long id);

    String getHeader();
    void setHeader(String s);
    int getHeaderIconResource();
    int getHeaderBackground();
    int getHeaderFontColor();

    String getSubject();
    void setSubject(String s);
    int getSubjectIconResource();
    int getSubjectBackground();
    int getSubjectFontColor();

    String getBody();
    void setBody(String s);
    int getBodyIconResource();
    int getBodyBackground();
    int getBodyFontColor();

    String getFooter();
    void setFooter(String s);
    int getFooterIconResource();
    int getFooterBackground();
    int getFooterFontColor();

    void populateFields(HashMap<String, Object> table);
    HashMap<String, Object> extractFields();
//    HashMap<String,String> extractIcons();
    String getIcon();
    int getIconResource();
    String getCName();
    String toString();
    Object getKey();
}
