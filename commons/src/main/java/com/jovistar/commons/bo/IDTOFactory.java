package com.jovistar.commons.bo;

//import com.ae.ccm.CCMIDlet;

public class IDTOFactory {

//    CCMIDlet midlet;
    protected static IDTOFactory instance;

    //singleton objects
    XR03 req;
    XReqSts reqSts;
    XRes res;
    XUsr usr;
    XMob mob;
    XAdCat cat;
    XAd ad;

    protected IDTOFactory() {}

//    public IDTOFactory(CCMIDlet mlet) {
//        midlet=mlet;
//    }

    public static IDTOFactory getInstance() {
    	if(instance==null)
    		instance=new IDTOFactory();
    	return instance;
    }

    public IDTObject getInstance(String classname, String username) {

        //ObjectFactory of = ObjectFactory.getInstance();
        if (classname.equals("XUsr")) {
            return getUser(username);
        } else if (classname.equals("XReq")) {
            return getXRequest();
        } else if (classname.equals("XRes")) {
            return getNewXResponse();
        } else if (classname.equals("XReqSts")) {
            return getNewRequestStatus();
        } else if (classname.equals("XPbk")) {
            return getNewXPbk();
        } else if (classname.equals("XMsg")) {
            return getNewXMsg();
        } else if (classname.equals("XMob")) {
            return getNewXMob();
        } else if (classname.equals("XAdCat")) {
            return getNewXAdCat();
        } else if (classname.equals("XAd")) {
            return getNewXAd();
        }
        return null;
    }

    public XR03 getXRequest() {
        if(req==null)
            req = new XR03();
        return req;
    }

    public XPbk getNewXPbk() {
        return new XPbk();
    }

    public XMsg getNewXMsg() {
        return new XMsg();
    }

    public XAdCat getNewXAdCat() {
        return new XAdCat();
    }

    public XAd getNewXAd() {
        return new XAd();
    }

    public XMob getNewXMob() {
        return new XMob();
    }

    public XRes getNewXResponse() {
//        if(res==null)
            res = new XRes();
        return res;
    }

    public XReqSts getNewRequestStatus() {
//        if(reqSts==null)
            reqSts = new XReqSts();
        return reqSts;
    }

    public XUsr getUser(String urnm) {
        if(usr==null)
            usr = new XUsr();
        if(urnm != null)
            if(urnm.length()>0)
                usr.id=(urnm);
        return usr;
    }
}
