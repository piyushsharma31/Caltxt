package com.jovistar.caltxt.service;
/*
import java.util.Calendar;
import java.util.HashMap;

import com.jovistar.caltxt.activity.CaltxtPager;
import com.jovistar.caltxt.bo.XLoc;
import com.jovistar.caltxt.notification.NotificationUtils;
import com.jovistar.caltxt.persistence.Persistence;
import com.jovistar.commons.util.Logr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class LocationReceiver implements LocationListener {
	private static final String TAG = "LocationReceiver";

	LocationManager locationManager;
	String providerCoarse;
	boolean network_enabled = false;
	public static String defaultProvider = LocationManager.NETWORK_PROVIDER;
	public static HashMap<String, XLoc> nameLocations = new HashMap<String, XLoc>();

	private static LocationReceiver instance;
	Context context;

	public static LocationReceiver getInstance(Context context) {
		if(instance==null)
			instance = new LocationReceiver(context);
		return instance;
	}

	private LocationReceiver(Context context) {
		this.context = context;
	}

	public void registerMotionReceiver() {

		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		// criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// String providerFine = locationManager.getBestProvider(criteria, true);

		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		providerCoarse = locationManager.getBestProvider(criteria, true);
		network_enabled = locationManager.isProviderEnabled(defaultProvider);
		Log.d(TAG, " LocationReceiver::providerCoarse "+providerCoarse );
		Log.d(TAG, " LocationReceiver::network_enabled "+network_enabled);

		// if (providerFine != null) {
		// manager.requestLocationUpdates(providerFine, 300000, 100, this);
		// }

		Persistence.getInstance(context).getAllXLoc();

		Log.d(TAG, " LocationReceiver::getAllXLoc "+nameLocations.size());

		int ret = ContextCompat.checkSelfPermission(context,
				Manifest.permission.ACCESS_COARSE_LOCATION);

		if (ret != PackageManager.PERMISSION_GRANTED) {
			return ;
		} else {

			// Register the listener with the Location Manager to receive location
			// updates
			if (providerCoarse != null && network_enabled) {
				locationManager.requestLocationUpdates(providerCoarse,
				300000, // minutes
				100, // meters
				this);
				Log.d(TAG, " LocationReceiver::requestLocationUpdates providerCoarse");
			} else {
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
				Log.d(TAG, " LocationReceiver::requestLocationUpdates NETWORK_PROVIDER");
			}
		}
	}

	Location getOldLocation() {
		// Returns last known location, this is the fastest way to get a
		// location fix.
		int ret = ContextCompat.checkSelfPermission(context,
				Manifest.permission.ACCESS_COARSE_LOCATION);

		if (ret != PackageManager.PERMISSION_GRANTED) {
			return null;
		}
		return locationManager.getLastKnownLocation(defaultProvider);
	}

//	 Make use of location after deciding if it is better than previous one.
//	 @param location
//	 Newly acquired location.
	void tagThisLocation(Location location, String name) {
		XLoc loc = Persistence.getInstance(context).getXLoc(name);
		boolean newLocation = true;

		if (loc==null || isBetterLocation(loc.getLocationFromThisObject(), location)) {
			// If location is better
			if(loc==null) {
				loc = new XLoc();
				loc.setName(name);
			} else {
				newLocation = false;
			}
			loc.setLatitude(location.getLatitude());
			loc.setLogitude(location.getLongitude());
			loc.setTimestamp(location.getTime());
			loc.setAccuracy(location.getAccuracy());
			loc.setBearing(location.getBearing());
			loc.setSpeed(location.getSpeed());
			loc.setProvider(location.getProvider());
			if(newLocation) {
				Persistence.getInstance(context).insert(loc);
			} else {
				Persistence.getInstance(context).update(loc);
			}
		}

	}

//	 Time difference threshold set for one minute.
	static final int TIME_DIFFERENCE_THRESHOLD = 1 * 60 * 1000;

//	 * Decide if new location is better than older by following some basic
//	 * criteria. This algorithm can be as simple or complicated as your needs
//	 * dictate it. Try experimenting and get your best location strategy
//	 * algorithm.
//	 *
//	 * @param oldLocation
//	 *            Old location used for comparison.
//	 * @param newLocation
//	 *            Newly acquired location compared to old one.
//	 * @return If new location is more accurate and suits your criteria more
//	 *         than the old one.

	boolean isBetterLocation(Location oldLocation, Location newLocation) {
		// If there is no old location, of course the new location is better.
		if (oldLocation == null) {
			return true;
		}

		// Check if new location is newer in time.
		boolean isNewer = newLocation.getTime() > oldLocation.getTime();

		// Check if new location more accurate. Accuracy is radius in meters, so
		// less is better.
		boolean isMoreAccurate = newLocation.getAccuracy() < oldLocation
				.getAccuracy();
		if (isMoreAccurate && isNewer) {
			// More accurate and newer is always better.
			return true;
		} else if (isMoreAccurate && !isNewer) {
			// More accurate but not newer can lead to bad fix because of user
			// movement.
			// Let us set a threshold for the maximum tolerance of time
			// difference.
			long timeDifference = newLocation.getTime() - oldLocation.getTime();

			// If time difference is not greater then allowed threshold we
			// accept it.
			if (timeDifference > -TIME_DIFFERENCE_THRESHOLD) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, provider + " LocationReceiver::onProviderDisabled");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, provider + " LocationReceiver::onProviderEnabled");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, provider + " LocationReceiver::onStatusChanged status "+ status);
	}

	@Override
	public void onLocationChanged(Location location) {
		// Do work with new location. Implementation of this method will be
		// covered later.

//		float distance = location.distanceBetween(location.getLatitude(), location.getLongitude(), 
//				endLatitude, endLongitude, results);
		tagThisLocation(location, "at Home");

		MotionReceiver.DEVICE_MOVED_SIGNIFICANTLY = true;

		// play notification sound
		NotificationUtils notificationUtils = new NotificationUtils(context.getApplicationContext());
		notificationUtils.playNotificationSound();
		Intent resultIntent = new Intent(context, CaltxtPager.class);
		notificationUtils.showNotificationMessage("Significant motion detected",
				location.toString(), Long.toString(Calendar.getInstance().getTimeInMillis()), resultIntent);

		context.startService(new Intent(context, WifiScanService.class));

		Log.d(TAG, " LocationReceiver::onLocationChanged location "+ location);
	}

}
*/