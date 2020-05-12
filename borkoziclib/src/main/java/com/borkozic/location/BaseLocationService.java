package com.borkozic.location;

import android.app.Service;

public abstract class BaseLocationService extends Service
{
	public static final String BORKOZIC_LOCATION_SERVICE = "com.borkozic.location";
	/**
	 * Broadcast sent when service status changes
	 */
	public static final String BROADCAST_LOCATING_STATUS = "com.borkozic.locatingStatusChanged";
	/**
	 * GPS status code
	 */
	public static final int GPS_OFF = 1;
	/**
	 * GPS status code
	 */
	public static final int GPS_SEARCHING = 2;
	/**
	 * GPS status code
	 */
	public static final int GPS_OK = 3;
}
