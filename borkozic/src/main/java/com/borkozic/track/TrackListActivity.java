/*
 * Androzic - android navigation client that uses OziExplorer maps (ozf2, ozfx3).
 * Copyright (C) 2010-2014 Andrey Novikov <http://andreynovikov.info/>
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

package com.borkozic.track;

import android.app.Activity;
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
import com.borkozic.data.Track;
import com.borkozic.overlay.TrackOverlay;

public class TrackListActivity extends AppCompatActivity implements OnTrackActionListener
{
	static final int RESULT_LOAD_TRACK = 1;

	private Borkozic application;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		application = Borkozic.getApplication();

		setContentView(R.layout.act_fragment);

		if (savedInstanceState == null)
		{
			Fragment fragment = Fragment.instantiate(this, TrackList.class.getName());
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			fragmentTransaction.add(android.R.id.content, fragment, "TrackList");
			fragmentTransaction.commit();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode)
		{
			case RESULT_LOAD_TRACK:
				if (resultCode == Activity.RESULT_OK)
				{
					final Borkozic application = Borkozic.getApplication();
					int[] indexes = data.getExtras().getIntArray("index");
					for (int index : indexes)
					{
						TrackOverlay newTrack = new TrackOverlay(this, application.getTrack(index));
						application.fileTrackOverlays.add(newTrack);
					}
				}
				break;
		}
	}

	@Override
	public void onTrackEdit(Track track)
	{
		startActivity(new Intent(this, TrackProperties.class).putExtra("INDEX", application.getTrackIndex(track)));
	}

	@Override
	public void onTrackEditPath(Track track)
	{
		setResult(RESULT_OK, new Intent().putExtra("index", application.getTrackIndex(track)));
		finish();
	}

	@Override
	public void onTrackToRoute(Track track)
	{
		startActivity(new Intent(this, TrackToRoute.class).putExtra("INDEX", application.getTrackIndex(track)));
		finish();
	}

	@Override
	public void onTrackSave(Track track)
	{
		startActivity(new Intent(this, TrackSave.class).putExtra("INDEX", application.getTrackIndex(track)));
	}
}
