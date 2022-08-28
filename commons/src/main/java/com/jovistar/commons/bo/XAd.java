package com.jovistar.commons.bo;

import java.io.Serializable;
import java.util.HashMap;

import android.graphics.Color;
import android.util.Log;

public class XAd implements IDTObject, Serializable {

    public int id;
    public String txt;
    public String url;
    public String owner;
    public String creatdt;
    public String modifdt;
    public String grpurl;
    public String vdourl;
    public String fltr;//filter
    public int refrsrt;//seconds OR count of items in a feed title
    public String cat;//category
    public String titl;

    public String getHeader() {
        /*String st = null;
        StringBuffer sb = new StringBuffer();
        if(txt==null) {
            sb.append(titl);
        } else {
            sb.append("updated "+modifdt);
        }
        st = sb.toString();
        sb = null;
        return st;*/
    	return titl;
    }

    public String getFooter() {
        return "";
    }

    public XAd() {
    }

    public String getCName() {
        return "XAd";
    }

    public String getIcon() {
		Log.d("XAd", "getIcon:"+cat);
        return (cat);
    }

    /*public void print() {
    CCMIDlet.instance.debug("id = " + id);
    CCMIDlet.instance.debug("txt = " + txt);
    CCMIDlet.instance.debug("url = " + url);
    CCMIDlet.instance.debug("owner = " + owner);
    CCMIDlet.instance.debug("creatdt = " + creatdt);
    CCMIDlet.instance.debug("modifdt = " + modifdt);
    CCMIDlet.instance.debug("grpurl = " + grpurl);
    CCMIDlet.instance.debug("vdourl = " + vdourl);
    CCMIDlet.instance.debug("titl = " + titl);
    CCMIDlet.instance.debug("filter = " + fltr);
    CCMIDlet.instance.debug("category = " + cat);
    CCMIDlet.instance.debug("refrsrt = " + refrsrt);
    }
    public HashMap extractIcons() {
    }
*/
    public HashMap extractFields() {

        HashMap ht = new HashMap();
        ht.put("id", Integer.toString(id));
        if (txt != null) {
            ht.put("txt", txt);
        }
        if (url != null) {
            ht.put("url", url);
        }
        if (owner != null) {
            ht.put("owner", owner);
        }
        if (creatdt != null) {
            ht.put("creatdt", creatdt);
        }
        if (modifdt != null) {
            ht.put("modifdt", modifdt);
        }
        if (grpurl != null) {
            ht.put("grpurl", grpurl);
        }
        if (vdourl != null) {
            ht.put("vdourl", vdourl);
        }
        if (titl != null) {
            ht.put("titl", titl);
        }
        if (fltr != null) {
            ht.put("fltr", titl);
        }
        if (cat != null) {
            ht.put("cat", titl);
        }
        ht.put("refrsrt", Integer.toString(refrsrt));

        return ht;
    }

    public String toString() {
        String st = null;
        StringBuffer sb = new StringBuffer();
        if(txt==null) {
            sb.append(titl);
        } else {
            sb.append("updated ").append(modifdt);
            sb.append("\n").append(titl);
        }
        st = sb.toString();
        sb = null;
        return st;
    }

	@Override
	public String searchString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSubject() {
		return txt;
	}

	@Override
	public String getBody() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public void setSubject(String s) {
		txt = s;
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
        id = Integer.parseInt((String) ht.get("id"));
        txt = (String) ht.get("txt");
        url = (String) ht.get("url");
        owner = (String) ht.get("owner");
        creatdt = (String) ht.get("creatdt");
        modifdt = (String) ht.get("modifdt");
        grpurl = (String) ht.get("grpurl");
        vdourl = (String) ht.get("vdourl");
        titl = (String) ht.get("titl");
        fltr = (String) ht.get("fltr");
        cat = (String) ht.get("cat");
        refrsrt = Integer.parseInt((String) ht.get("refrsrt"));
	}

	@Override
	public void setHeader(String s) {
		// TODO Auto-generated method stub
		titl = s;
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
	public int getSubjectIconResource() {
		// TODO Auto-generated method stub
		return 0;
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
		// TODO Auto-generated method stub
		return Color.BLACK;
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
