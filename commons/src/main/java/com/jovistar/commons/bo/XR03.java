package com.jovistar.commons.bo;

//import javax.microedition.lcdui.Image;
import java.io.Serializable;
import java.util.HashMap;

public class XR03 implements IDTObject, Serializable {

    public int id;
    public short ver=1504;//version=year month (2015. 04)
    public String sesid;
    public short svc;
    public short op;
    public String unm;
    public IDTObject param;

    public String getHeader() {
    	return unm;
    }

    public String toString() {
    	return id+":"+sesid+":"+svc+":"+op+":"+unm;
    }

    public String getFooter() {
    	return sesid;
    }

    public XR03() {
    }

    public String getIcon() {
        return ("info");
        //return CCMIDlet.instance.images.getSprite("info");
    }
/*
    public HashMap<String, String> extractIcons() {
        Icons.put("id", "info");
        Icons.put("sesid", "info");
        Icons.put("svc", "info");
        Icons.put("op", "info");
        Icons.put("unm", "contactmale");
        return Icons.put("param", "info");
    }
*/
    public HashMap<String, Object> extractFields() {
        HashMap<String, Object> ht = new HashMap<String, Object>();
        ht.put("id", Integer.toString(id));
        ht.put("ver", Integer.toString(ver));
        if (sesid != null) {
            ht.put("sesid", sesid);
        }
        //if (svc != null) {
            ht.put("svc", Integer.toString(svc));
        //}
        //if (op != null) {
            ht.put("op", Integer.toString(op));
        //}
        if (unm != null) {
            ht.put("unm", unm);
        }
        if (param != null) {
            ht.put("param", param);
        }
        return ht;
    }

    public void populateFields(HashMap<String, Object> ht) {
        id = Integer.parseInt((String) ht.get("id"));
        ver = Short.parseShort((String) ht.get("ver"));
        sesid = (String) ht.get("sesid");
        //svc = (String) ht.get("svc");
        //op = (String) ht.get("op");
        svc = Short.parseShort((String) ht.get("svc"));
        op = Short.parseShort((String) ht.get("op"));
        unm = (String) ht.get("unm");
        param = (IDTObject) ht.get("param");
    }

    public String getCName() {
        return "XR03";
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
		// TODO Auto-generated method stub
		return 0;
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
