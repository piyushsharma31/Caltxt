package com.jovistar.commons.bo;

//import javax.microedition.lcdui.Image;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Color;

public class XMsg implements IDTObject, Serializable {

    public short unred;
    public String frm;
    public String sub;
    public String bdy;//piggyback total count of conversations, conversations details
    public String time;
    public ArrayList<String> to;//array of string objects
    public int id;//inboxitem id (XMsg.id)
    public int cid;//its child id

    public XMsg() {
        to = new ArrayList<String>();
    }

    public String getCName() {
        return "XMsg";
    }

    public String getIcon() {
/*        if(unred==1)
            return ("messagenew");
        else
            return ("messageopen");*/
        return frm;
    }
/*
    public void print() {
        CCMIDlet.instance.debug("unred = " + unred);
        CCMIDlet.instance.debug("frm = " + frm);
        CCMIDlet.instance.debug("sub = " + sub);
        CCMIDlet.instance.debug("bdy = " + bdy);
        CCMIDlet.instance.debug("time = " + time);
        CCMIDlet.instance.debug("to = " + to);
        CCMIDlet.instance.debug("id = " + id);
        CCMIDlet.instance.debug("cid = " + cid);
    }
*/

    public HashMap extractFields() {

        HashMap ht = new HashMap();
        ht.put("unred", Integer.toString(unred));
        if (frm != null) {
            ht.put("frm", frm);
        }
        if (sub != null) {
            ht.put("sub", sub);
        }
        if (bdy != null) {
            ht.put("bdy", bdy);
        }
        if (time != null) {
            ht.put("time", time);
        }
        if (to != null) {
            ht.put("to", to);
        }
        ht.put("cid", Integer.toString(cid));
        ht.put("id", Integer.toString(id));

        return ht;
    }

    public void populateFields(HashMap ht) {

        unred = Short.parseShort((String) ht.get("unred"));
        frm = (String) ht.get("frm");
        sub = (String) ht.get("sub");
        bdy = (String) ht.get("bdy");
        time = (String) ht.get("time");
        try {
            to.clear();
            to = null;
            to = (ArrayList) ht.get("to");
        } catch (ClassCastException cce) {
            to = new ArrayList();
            to.add((String) ht.get("to"));
        }

        cid = Integer.parseInt((String) ht.get("cid"));
        id = Integer.parseInt((String) ht.get("id"));
        //print();
    }

    public String getHeader() {
    	return frm+": "+sub;
    }

    public String getFooter() {
//    	return "";
    	return time;
    }
/*
    public String toString() {
        if(Constants.wrapon)
            return toStringWrapOn();
        else
            return toStringWrapOff();
    }

    public String toStringWrapOff() {
        return sub;
//        return CCMIDlet.instance.strings.getText((sub==null?
  //          "no subject":(sub.length()==0?"no subject":CCMIDlet.instance.strings.getText(sub)))
    //            , frm);
    }

    public String toStringWrapOn() {
        //return CCMIDlet.instance.strings.getText((CCMIDlet.instance.dateUtil.sqltimewithoutyear(time)), frm)
        return frm+Constants.getInstance().NEWLINE
                +sub;
                //+(sub==null?"no subject":(sub.length()==0?"no subject":CCMIDlet.instance.strings.getText(sub)));
    }*/

	@Override
	public String searchString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSubject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBody() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSubject(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBody(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getPersistenceId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPersistenceId(long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHeader(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFooter(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getKey() {
		return id;
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

	@Override
	public int getSubjectIconResource() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSubjectBackground() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSubjectFontColor() {
		return Color.BLACK;
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
		return 0;
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
		return 0;
	}

	@Override
	public int getIconResource() {
		// TODO Auto-generated method stub
		return 0;
	}
}
