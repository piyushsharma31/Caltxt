package com.jovistar.commons.bo;

import java.io.Serializable;
import java.util.HashMap;

public class XAdCat implements IDTObject, Serializable {

	public String cat;
    public int sz;

    public String getHeader() {
    	return cat;
    }

    public String getFooter() {
    	return "No. of items "+sz;
    }

    protected XAdCat() {
    }

    public String getCName() {
        return "XAdCat";
    }

    public String getIcon() {
        return cat;
    }

    public HashMap extractFields() {

        HashMap ht = new HashMap();
        ht.put("sz", Integer.toString(sz));
        if (cat != null) {
            ht.put("cat", cat);
        }

        return ht;
    }

    public String toString() {
		return (sz)+"^"+(cat);
    }

	public void setCat(String c) {
		cat = c;
	}

	@Override
	public String searchString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSubject() {
		// TODO Auto-generated method stub
		return cat;
	}

	@Override
	public String getBody() {
		// TODO Auto-generated method stub
		return cat;
	}

	@Override
	public void setSubject(String s) {
		// TODO Auto-generated method stub
		cat = s;
	}

	@Override
	public void setBody(String s) {
		// TODO Auto-generated method stub
		cat = s;
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
        sz = Integer.parseInt((String) table.get("sz"));
        cat = (String) table.get("cat");
	}

	@Override
	public void setHeader(String s) {
		// TODO Auto-generated method stub
		cat = s;
	}

	@Override
	public void setFooter(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getKey() {
		return cat;
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
