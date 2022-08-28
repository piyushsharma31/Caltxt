package com.jovistar.commons.bo;

import java.io.Serializable;
import java.util.HashMap;
//import com.ae.lib.images.Icons;

public class XUsr implements IDTObject, Serializable {

    public String id;//header
    public String pwd;//subject
    public String creatdt;//body
    public String modifdt;//footer
    public String cmnt;

    public XUsr() {
    	id=pwd=creatdt=modifdt=cmnt="";
    }

    public String getIcon() {
        return (id);
        //Image img = CCMIDlet.instance.images.getSprite(id);
        //return img==null?CCMIDlet.instance.images.getSprite("contactmale"):img;
    }

    public String getCName() {
        return "XUsr";
    }
/*
    public void print() {
        CCMIDlet.instance.debug("obj = " + this);
        CCMIDlet.instance.debug("id = " + id);
        CCMIDlet.instance.debug("pwd = " + pwd);
        CCMIDlet.instance.debug("creatdt = " + creatdt);
        CCMIDlet.instance.debug("modifdt = " + modifdt);
        CCMIDlet.instance.debug("cmnt = " + cmnt);
    }

    public HashMap extractIcons() {
        Icons.put("id", "contactmale");
        Icons.put("pwd", "file");
        Icons.put("creatdt", "calendar");
        Icons.put("modifdt", "calendar");
        return Icons.put("cmnt", "notes");
    }
*/
    public HashMap extractFields() {

        HashMap ht = new HashMap();
        if (id != null) {
            ht.put("id", id);
        }
        if (pwd != null) {
            ht.put("pwd", pwd);
        }
        if (creatdt != null) {
            ht.put("creatdt", creatdt);
        }
        if (modifdt != null) {
            ht.put("modifdt", modifdt);
        }
        if (cmnt != null) {
            ht.put("cmnt", cmnt);
        }
        return ht;
    }

    public String toString() {
        return id+", "+pwd+", "+creatdt+", "+modifdt+", "+cmnt;
    }

	@Override
	public String searchString() {
		// TODO Auto-generated method stub
		return null;
	}

    public String getHeader() {
    	return id;
    }

    public String getFooter() {
    	return modifdt;
    }

	@Override
	public String getSubject() {
		return pwd;
	}

	@Override
	public String getBody() {
		return creatdt;
	}

	@Override
	public void setSubject(String s) {
		pwd = s;
	}

	@Override
	public void setBody(String s) {
		creatdt = s;
	}

	@Override
	public void setHeader(String s) {
		id = s;
	}

	@Override
	public void setFooter(String s) {
		modifdt = s;
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
        id = (String) ht.get("id");
        pwd = (String) ht.get("pwd");
        creatdt = (String) ht.get("creatdt");
        modifdt = (String) ht.get("modifdt");
        cmnt = (String) ht.get("cmnt");
        //print();
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
