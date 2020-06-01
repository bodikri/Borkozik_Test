/*
 * Androzic - android navigation client that uses OziExplorer maps (ozf2, ozfx3).
 * Copyright (C) 2010-2012 Andrey Novikov <http://andreynovikov.info/>
 * 
 * This file is part of Androzic application.
 * 
 * Androzic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Androzic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Androzic. If not, see <http://www.gnu.org/licenses/>.
 */

package com.borkozic.waypoint;

import android.content.Intent;
import android.os.Bundle;
//import androidx.core.app.Fragment;
//import androidx.core.app.FragmentTransaction;
//import android.support.v7.app.ActionBarActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.borkozic.Borkozic;
import com.borkozic.R;
import com.borkozic.data.Waypoint;
import com.borkozic.navigation.NavigationService;
import com.borkozic.util.StringFormatter;
//todo - da направя всяка точка по маршрут да се добавя автоматично в някакъв RutesWaypointList,  за да може да се редактират тези точки
//понеже не е направено да се редактират точките вкарани в някакъв маршрут
public class WaypointListActivity extends AppCompatActivity implements OnWaypointActionListener
{
	private Borkozic application;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		application = Borkozic.getApplication();

		setContentView(R.layout.act_fragment);

		if (savedInstanceState == null)
		{
			Fragment fragment = Fragment.instantiate(this, WaypointList.class.getName());
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			fragmentTransaction.add(android.R.id.content, fragment, "WaypointList");
			fragmentTransaction.commit();
		}
	}

	@Override
	public void onWaypointView(Waypoint waypoint)
	{
		application.ensureVisible(waypoint);
		finish();
	}

	@Override
	public void onWaypointNavigate(Waypoint waypoint)
	{
		Intent intent = new Intent(application, NavigationService.class).setAction(NavigationService.NAVIGATE_MAPOBJECT);
		intent.putExtra(NavigationService.EXTRA_NAME, waypoint.name);
		intent.putExtra(NavigationService.EXTRA_LATITUDE, waypoint.latitude);
		intent.putExtra(NavigationService.EXTRA_LONGITUDE, waypoint.longitude);
		intent.putExtra(NavigationService.EXTRA_PROXIMITY, waypoint.proximity);
		application.startService(intent);
		finish();
	}

	@Override
	public void onWaypointEdit(Waypoint waypoint)
	{
		int index = application.getWaypointIndex(waypoint);
		startActivity(new Intent(application, WaypointProperties.class).putExtra("INDEX", index));
	}

	@Override
	public void onWaypointShare(Waypoint waypoint)
	{
		Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, R.string.currentloc);
		String coords = StringFormatter.coordinates(application.coordinateFormat, " ", waypoint.latitude, waypoint.longitude);
		i.putExtra(Intent.EXTRA_TEXT, waypoint.name + " @ " + coords);
		startActivity(Intent.createChooser(i, getString(R.string.menu_share)));
	}

	@Override
	public void onWaypointRemove(Waypoint waypoint)
	{
		application.removeWaypoint(waypoint);
	}

}
