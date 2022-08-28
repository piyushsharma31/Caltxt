package com.jovistar.commons.bo;

//import javax.microedition.lcdui.Image;
import java.io.Serializable;
import java.util.HashMap;

public class XReqSts implements IDTObject, Serializable {

    public int cd;
    public String cdstr;

    public String getHeader() {
    	return toString();
    }

    public String getFooter() {
    	return cdstr;
    }

    public XReqSts() {
    }

    public String getIcon() {
        return ("info");
    }

    public String getCName() {
        return "XReqSts";
    }
/*
    public HashMap extractIcons() {
        Icons.put("cd", "info");
        return Icons.put("cdstr", "info");
    }
*/
    public HashMap extractFields() {
        HashMap ht = new HashMap();
        ht.put("cd", Integer.toString(cd));
        if (cdstr != null) {
            ht.put("cdstr", cdstr);
        }
        return ht;
    }

    public String toString() {
        return "request status";
    }

    public XReqSts copy(){
    	XReqSts status = new XReqSts();
    	status.cd = cd;
    	status.cdstr = cdstr;
    	return status;
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
        cd = Integer.parseInt((String) table.get("cd"));
        cdstr = (String) table.get("cdstr");
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
		return cd;
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
