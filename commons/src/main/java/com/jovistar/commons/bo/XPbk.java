package com.jovistar.commons.bo;

//import javax.microedition.lcdui.Image;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Color;

public class XPbk implements IDTObject, Serializable {

    public String unm;
    public ArrayList<XMob> myPhStus;

    public String getHeader() {
    	return unm;
    }

    public String getFooter() {
    	return myPhStus.toString();
    }

    public XPbk() {
        myPhStus = new ArrayList<XMob>();
    }

    public String getCName() {
        return "XPbk";
    }

    public String getIcon() {
        return ("contatmale");
        //return CCMIDlet.instance.images.getSprite("contatmale");
    }

    public HashMap<String,Object> extractFields() {

        HashMap<String,Object> ht = new HashMap<String,Object>();
        if (unm != null) {
            ht.put("unm", unm);
        }
        if (myPhStus != null) {
            ht.put("myPhStus", myPhStus);
        }
        return ht;
    }

    public String toString() {
        return unm;
    }

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
	public void populateFields(HashMap<String, Object> ht) {
        unm = (String) ht.get("unm");
        try {
            myPhStus.clear();//removeAllElements();
            myPhStus = null;
            myPhStus = (ArrayList<XMob>) ht.get("myPhStus");
        } catch (ClassCastException cce) {
            myPhStus = new ArrayList<XMob>();
//            CCMIDlet.instance.debug("populateFields:CCast1:");
            myPhStus.add((XMob) ht.get("myPhStus"));
//            CCMIDlet.instance.debug("populateFields:CCast2:");
        }
        //print();
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
		return unm;
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
		// TODO Auto-generated method stub
		return 0;
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
