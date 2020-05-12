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

package com.borkozic.waypoint;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.borkozic.Borkozic;
import com.borkozic.R;
import com.borkozic.data.Waypoint;
import com.borkozic.util.Geo;
import com.borkozic.util.StringFormatter;

public class WaypointProject extends Activity
{
	List<Waypoint> waypoints = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.act_waypoint_project);

		Borkozic application = (Borkozic) getApplication();
		waypoints = application.getWaypoints();
		
		((TextView) findViewById(R.id.name_text)).setText("WPT"+waypoints.size());

		Collections.sort(waypoints, new Comparator<Waypoint>()
        {
            @Override
            public int compare(Waypoint o1, Waypoint o2)
            {
           		return (o1.name.compareToIgnoreCase(o2.name));
            }
        });

		String[] items = new String[waypoints.size()+1];
		items[0] = getString(R.string.currentloc);
		int i = 1;
		for (Waypoint wpt : waypoints)
		{
			items[i] =  wpt.name;
			i++;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		((Spinner) findViewById(R.id.source_spinner)).setAdapter(adapter);
		
		items = new String[2];
		items[0] = StringFormatter.distanceAbbr;
		items[1] = StringFormatter.distanceShortAbbr;
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		((Spinner) findViewById(R.id.distance_spinner)).setAdapter(adapter);

		items = getResources().getStringArray(R.array.angle_units);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		((Spinner) findViewById(R.id.bearing_spinner)).setAdapter(adapter);
		((Spinner) findViewById(R.id.bearing_spinner)).setSelection(application.angleType);

	    ((Button) findViewById(R.id.done_button)).setOnClickListener(doneOnClickListener);
	    ((Button) findViewById(R.id.cancel_button)).setOnClickListener(new OnClickListener() { public void onClick(View v) { finish(); } });
    }
		
	private OnClickListener doneOnClickListener = new OnClickListener()
	{
        public void onClick(View v)
        {
        	try
        	{
        		Borkozic application = (Borkozic) getApplication();
        		Waypoint waypoint = new Waypoint();
        		waypoint.name = ((TextView) findViewById(R.id.name_text)).getText().toString();
				//waypoint.altitude = ((TextView) findViewById(R.id.name_text)).getText().toString();
        		double distance = Integer.parseInt(((TextView) findViewById(R.id.distance_text)).getText().toString());
        		double bearing = Integer.parseInt(((TextView) findViewById(R.id.bearing_text)).getText().toString());
        		int src = ((Spinner) findViewById(R.id.source_spinner)).getSelectedItemPosition();
        		int df = ((Spinner) findViewById(R.id.distance_spinner)).getSelectedItemPosition();
        		int bf = ((Spinner) findViewById(R.id.bearing_spinner)).getSelectedItemPosition();
        		double[] loc;
        		if (src > 0)
        		{
        			 loc = new double[2];
        			 loc[0] = waypoints.get(src-1).latitude;
        			 loc[1] = waypoints.get(src-1).longitude;
        		}
        		else
        		{
    				loc = application.getLocation();
        		}

        		if (df == 0)
        		{
        			distance = distance / StringFormatter.distanceFactor * 1000;
        		}
        		else
        		{
        			distance = distance / StringFormatter.distanceShortFactor;
        		}
        		if (bf == 1)
        		{
        			GeomagneticField mag = new GeomagneticField((float) loc[0], (float) loc[1], 0.0f, System.currentTimeMillis());
        			bearing -= mag.getDeclination();
        		}
        		double[] prj = Geo.projection(loc[0], loc[1], distance, bearing);
        		waypoint.latitude = prj[0];
        		waypoint.longitude = prj[1];
        		waypoint.date = Calendar.getInstance().getTime();
        		application.addWaypoint(waypoint);
    			setResult(RESULT_OK, new Intent().putExtra("index", application.getWaypointIndex(waypoint)));
        		finish();
        	}
        	catch (Exception e)
        	{
    			Toast.makeText(getBaseContext(), "Invalid input", Toast.LENGTH_LONG).show();
    			e.printStackTrace();
        	}
        }
    };

    @Override
	protected void onDestroy()
	{
		super.onDestroy();
		waypoints = null;
	}
}
