package com.jovistar.caltxt.bo;

//import com.jovistar.caltxt.service.LocationReceiver;
/*
public class XLoc implements IDTObject, Serializable {
	private static final long serialVersionUID = 12L;

	private String name; // name of the location
    private double latitude; // latitude (-90 to +90)
    private double longitude; // longitude (-180 to +180)
    private double altitude;
    private String address; // address of the location
    private float accuracy;// in meters
    private long timestamp;// time when this location is updated
    private float bearing;// in degrees
    private float speed;// in meters/second over ground
    private String provider;// name of the provider that generated this fix
	long pid;//persistence id
//	Context context;

//	public XLoc (Context context) {
//		this.context = context;
//	}

	public Location getLocationFromThisObject() {
		Location loc = new Location(LocationReceiver.defaultProvider);
		loc.setAccuracy(accuracy);
		loc.setAltitude(altitude);
		loc.setBearing(bearing);
		loc.setLatitude(latitude);
		loc.setLongitude(longitude);;
		loc.setSpeed(speed);
		loc.setProvider(provider);
		loc.setTime(timestamp);

		return loc;
	}

	public String getName() {
    	return name;
    }

    public void setName(String nam) {
    	name = nam;
    }

    public double getLatitude() {
    	return latitude;
    }

    public void setAltitude(double l) {
    	altitude = l;
    }

    public double getAltitude() {
    	return altitude;
    }

    public void setLatitude(double l) {
    	latitude = l;
    }

    public float getAccuracy() {
    	return accuracy;
    }

    public void setAccuracy(float a) {
    	accuracy = a;
    }

    public float getSpeed() {
    	return speed;
    }

    public void setSpeed(float s) {
    	speed = s;
    }

    public float getBearing() {
    	return bearing;
    }

    public void setBearing(float s) {
    	bearing = s;
    }

    public String getProvider() {
    	return provider;
    }

    public void setProvider(String p) {
    	provider = p;
    }

    public long getTimestamp() {
    	return timestamp;
    }

    public void setTimestamp(long l) {
    	timestamp = l;
    }

    public double getLongitude() {
    	return longitude;
    }

    public void setLogitude(double l) {
    	longitude = l;
    }

    public String getAddress() {
    	return address;
    }

    public void setAddress(String a) {
    	address = a;
    }

    @Override
	public String searchString() {
		return null;
	}

	@Override
	public long getPersistenceId() {
		return pid;
	}

	@Override
	public void setPersistenceId(long id) {
		pid = id;
	}

	@Override
	public String getHeader() {
		return name;
	}

	@Override
	public void setHeader(String s) {
	}

	@Override
	public int getHeaderIconResource() {
		return 0;
	}

	@Override
	public int getHeaderBackground() {
		return 0;
	}

	@Override
	public int getHeaderFontColor() {
		return 0;
	}

	@Override
	public String getSubject() {
		return latitude+", "+longitude;
	}

	@Override
	public void setSubject(String s) {
	}

	@Override
	public int getSubjectIconResource() {
		return R.drawable.ic_network_cell_black_24dp;
	}

	@Override
	public int getSubjectBackground() {
		return 0;
	}

	@Override
	public int getSubjectFontColor() {
		return 0;
	}

	@Override
	public String getBody() {
		return null;
	}

	@Override
	public void setBody(String s) {
		
	}

	@Override
	public int getBodyIconResource() {
		return 0;
	}

	@Override
	public int getBodyBackground() {
		return 0;
	}

	@Override
	public int getBodyFontColor() {
		return 0;
	}

	@Override
	public String getFooter() {
		return "";
	}

	public String getFooter(Context context) {
		return DateUtils.getRelativeDateTimeString(context, timestamp,
				DateUtils.MINUTE_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME).toString();
	}

	@Override
	public void setFooter(String s) {
	}

	@Override
	public int getFooterIconResource() {
		return 0;
	}

	@Override
	public int getFooterBackground() {
		return 0;
	}

	@Override
	public int getFooterFontColor() {
		return 0;
	}

	@Override
	public void populateFields(HashMap<String, Object> table) {
	}

	@Override
	public HashMap<String, Object> extractFields() {
		return null;
	}

	@Override
	public String getIcon() {
		return null;
	}

	@Override
	public int getIconResource() {
		return 0;
	}

	@Override
	public String getCName() {
		return "XLoc";
	}

	@Override
	public Object getKey() {
		return null;
	}

}
*/