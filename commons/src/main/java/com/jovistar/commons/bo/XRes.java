package com.jovistar.commons.bo;

//import javax.microedition.lcdui.Image;
import java.io.Serializable;
import java.util.HashMap;

public class XRes implements IDTObject, Serializable {

    public int id;
    public short ver=1;
    public String sesid;
    public short svc;
    public short op;
    public String umn;
    public Object rslt;//could be a ArrayList or IDTOObject
    public XReqSts status;

    public String getHeader() {
    	return umn;
    }

    public String getFooter() {
    	return sesid;
    }

    public XRes() {
    }

    public String getIcon() {
        return ("info");
        //return CCMIDlet.instance.images.getSprite("info");
    }

    public String getCName() {
        return "XRes";
    }
/*
    public void print() {
        CCMIDlet.instance.debug("id" + id);
        CCMIDlet.instance.debug("sesid = " + sesid);
        CCMIDlet.instance.debug("svc = " + svc);
        CCMIDlet.instance.debug("op = " + op);
        CCMIDlet.instance.debug("umn = " + umn);
        CCMIDlet.instance.debug("rslt = " + rslt);
        CCMIDlet.instance.debug("status = " + status);
    }

    public HashMap extractIcons() {
        Icons.put("id", "info");
        Icons.put("sesid", "info");
        Icons.put("svc", "info");
        Icons.put("op", "info");
        Icons.put("umn", "contactmale");
        Icons.put("rslt", "info");
        return Icons.put("status", "info");
    }
*/
    public HashMap extractFields() {
        HashMap ht = new HashMap();
        ht.put("id", Integer.toString(id));
        ver = Short.parseShort((String) ht.get("ver"));
        if (sesid != null) {
            ht.put("sesid", sesid);
        }
        //if (svc != null) {
            ht.put("svc", Integer.toString(svc));
        //}
        //if (op != null) {
            ht.put("op", Integer.toString(op));
        //}
        if (umn != null) {
            ht.put("umn", umn);
        }
        if (rslt != null) {
            ht.put("rslt", rslt);
        }
        if (status != null) {
            ht.put("status", status);
        }
        return ht;
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
	public void populateFields(HashMap<String, Object> table) {
		// TODO Auto-generated method stub
        id = Integer.parseInt((String) table.get("id"));
        sesid = (String) table.get("sesid");
        //svc = (String) table.get("svc");
        ver = Short.parseShort((String) table.get("ver"));
        svc = Short.parseShort((String) table.get("svc"));
        op = Short.parseShort((String) table.get("op"));
        umn = (String) table.get("umn");
        rslt = table.get("rslt");
        status = (XReqSts) table.get("status");
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
