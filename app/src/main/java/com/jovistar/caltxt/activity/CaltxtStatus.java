package com.jovistar.caltxt.activity;

public class CaltxtStatus {

    private String name;
    private String code;
    //    private Drawable resourceDrawable;
    private int resourceID;

    public CaltxtStatus(String name, String code, /*Drawable flag, */int resID) {
        this.name = name;
        this.code = code;
//        this.resourceDrawable = flag;
        this.resourceID = resID;
    }

    public String getStatusName() {
        return name;
    }

    //    public Drawable getResourceDrawable() {
//        return resourceDrawable;
//    }
    public String getStatusCode() {
        return code;
    }

    public void setStatusName(String name) {
        this.name = name;
    }

    public int getResourceID() {
        return resourceID;
    }
}
