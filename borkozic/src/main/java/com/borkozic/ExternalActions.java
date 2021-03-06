/*
 * Androzic - android navigation client that uses OziExplorer maps (ozf2, ozfx3).
 * Copyright (C) 2010-2012  Andrey Novikov <http://andreynovikov.info/>
 *
 * This file is part of Androzic application.
 *
 * Androzic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Androzic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Androzic.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.borkozic;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.borkozic.data.Route;
import com.borkozic.data.Waypoint;
import com.borkozic.navigation.NavigationService;
import com.borkozic.overlay.RouteOverlay;

/**
 * Executes intents from external applications.
 * 
 * @author Andrey Novikov
 */
public class ExternalActions extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		String action = intent.getAction();
		Log.e("ANDROZIC","New intent: "+action);
		
		Borkozic application = (Borkozic) getApplication();
		Intent activity = new Intent(this, MapActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		if (action.equals("com.borkozic.PLOT_ROUTE"))
		{
            double[] wptLat = intent.getExtras().getDoubleArray("targetLat");
            double[] wptLon = intent.getExtras().getDoubleArray("targetLon");
            String[] wptNames = intent.getExtras().getStringArray("targetName");
            if (wptLat != null && wptLon != null && wptLat.length == wptLon.length)
            {
            	Route route = new Route("External route", "", true);
            	for (int i=0; i < wptLat.length; i++)
            	{
            		String name = wptNames != null ? wptNames[i] : "RWPT"+i;
            		route.addWaypoint(name, wptLat[i], wptLon[i]);
            	}
            	int rt = application.addRoute(route);
    			RouteOverlay newRoute = new RouteOverlay(this, route);
    			// FIXME no overlay at this point
    			application.routeOverlays.add(newRoute);
    			startService(new Intent(this, NavigationService.class).setAction(NavigationService.NAVIGATE_ROUTE).putExtra(NavigationService.EXTRA_ROUTE_INDEX, rt));
            }
            else
            {
				Toast.makeText(getBaseContext(), "Bad route data", Toast.LENGTH_LONG).show();
            }
		}
		else if (action.equals("com.google.android.radar.SHOW_RADAR"))
		{
	        double lat = intent.getFloatExtra("latitude", 0);
	        double lon = intent.getFloatExtra("longitude", 0);
	        Waypoint waypoint = new Waypoint("", "", lat, lon);
    		waypoint.date = Calendar.getInstance().getTime();
			int wpt = application.addWaypoint(waypoint);
			waypoint.name = "WPT" + wpt;
			Intent i = new Intent(getApplicationContext(), NavigationService.class).setAction(NavigationService.NAVIGATE_MAPOBJECT);
			i.putExtra(NavigationService.EXTRA_NAME, waypoint.name);
			i.putExtra(NavigationService.EXTRA_LATITUDE, waypoint.latitude);
			i.putExtra(NavigationService.EXTRA_LONGITUDE, waypoint.longitude);
			i.putExtra(NavigationService.EXTRA_PROXIMITY, waypoint.proximity);
			startService(i);
		}
		else if ("geo".equals(intent.getScheme()))
		{
			Uri uri = intent.getData();
			String data = uri.getSchemeSpecificPart();
			
			// geo:latitude,longitude
			// geo:latitude,longitude?z=zoom
			if (data.contains("?"))
				data = data.substring(0, data.indexOf("?") - 1);
			try
			{
				String[] ll = data.split(",");
				double lat = Double.parseDouble(ll[0]);
				double lon = Double.parseDouble(ll[1]);
				activity.putExtra("lat", lat);
				activity.putExtra("lon", lon);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
        startActivity(activity);
		finish();
	}
}
