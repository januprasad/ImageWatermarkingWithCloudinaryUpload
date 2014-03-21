package com.jenuine.task_a;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationTracker {
	private LocationManager locationManager;
	private Location loc;
	private Context context;

	public LocationTracker(Context context) {
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	public Location currentLocation() {
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		String provider = locationManager
				.getBestProvider(new Criteria(), false);

		Location location = locationManager.getLastKnownLocation(provider);

	/*	if (location == null) {
			System.out.println("requesting....");
			locationManager.requestLocationUpdates(provider, 0, 0, listener);
			return loc;
		} else {
			loc = location;
		}*/
		return location;
	}

	private LocationListener listener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onLocationChanged(Location location) {
			Log.e("App", "location update : " + location);
			loc = location;
			locationManager.removeUpdates(listener);
		}
	};
}
