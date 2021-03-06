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

/*
 * Contributor: Gutorov Dmitry <dolfwolkov at gmail dot com>
 */

package com.borkozic.waypoint;

import java.io.File;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
//import androidx.core.app.DialogFragment;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.borkozic.Borkozic;
import com.borkozic.R;
import com.borkozic.data.Waypoint;
import com.borkozic.util.Geo;
import com.borkozic.util.StringFormatter;

public class WaypointInfo extends DialogFragment implements OnClickListener
{
	private Waypoint waypoint;
	private Drawable icon;
	private OnWaypointActionListener waypointActionsCallback;

	public WaypointInfo()
	{
		setRetainInstance(true);
	}
	
	public void setWaypoint(Waypoint waypoint)
	{
		this.waypoint = waypoint;
		icon = null;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.act_waypoint_info, container);
	    ((ImageButton) view.findViewById(R.id.navigate_button)).setOnClickListener(this);
	    ((ImageButton) view.findViewById(R.id.edit_button)).setOnClickListener(this);
	    ((ImageButton) view.findViewById(R.id.share_button)).setOnClickListener(this);
	    ((ImageButton) view.findViewById(R.id.remove_button)).setOnClickListener(this);
	    return view;
    }

	@Override
	public void onAttach(@NonNull Context context)
	{
		super.onAttach(context);
		Activity act = null;
		if (context instanceof Activity){
			act=(Activity) context;
		}
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try
		{
			waypointActionsCallback = (OnWaypointActionListener) act;
		}
		catch (ClassCastException e)
		{
			throw new ClassCastException(act.toString() + " must implement OnWaypointActionListener");
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Bundle args = getArguments();
		if (args != null)
		{
			double lat = args.getDouble("lat");
	        double lon = args.getDouble("lon");
			double elev = args.getDouble("elev"); //Добавих го, но за сега не го използвам
	        updateWaypointInfo(lat, lon, elev);
		}
	}

	@Override
	public void onDestroyView()
	{
		if (getDialog() != null && getRetainInstance())
			getDialog().setDismissMessage(null);
		super.onDestroyView();
	}

	@Override
    public void onClick(View v)
    {
		switch (v.getId())
		{
			case R.id.navigate_button:
				waypointActionsCallback.onWaypointNavigate(waypoint);
				break;
			case R.id.edit_button:
				waypointActionsCallback.onWaypointEdit(waypoint);
				break;
			case R.id.share_button:
				waypointActionsCallback.onWaypointShare(waypoint);
				break;
			case R.id.remove_button:
				waypointActionsCallback.onWaypointRemove(waypoint);
				break;
		}
		dismiss();
    }
	
	@SuppressLint("NewApi")
	private void updateWaypointInfo(double lat, double lon, double elev)
	{
        
		Borkozic application = Borkozic.getApplication();
		Activity activity = getActivity();
		Dialog dialog = getDialog();
		View view = getView();
		
		if (waypoint.drawImage)
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
			Bitmap b = BitmapFactory.decodeFile(application.iconPath + File.separator + waypoint.image, options);
			if (b != null)
			{
				b.setDensity(Bitmap.DENSITY_NONE);
				icon = new BitmapDrawable(getResources(), b);
			}
		}

		assert view != null;
		WebView description = (WebView) view.findViewById(R.id.description);
		
		if ("".equals(waypoint.description))
		{
			description.setVisibility(View.GONE);
		}
		else
		{
			String descriptionHtml;
			try
			{
				TypedValue tv = new TypedValue();
				assert activity != null;
				Theme theme = activity.getTheme();
				//Resources resources = getResources();
				theme.resolveAttribute(android.R.attr.textColorSecondary, tv, true);
				int secondaryColor = ContextCompat.getColor(Objects.requireNonNull(getContext()), tv.resourceId);

				String css = String.format("<style type=\"text/css\">html,body{margin:0;background:transparent} *{color:#%06X}</style>\n", (secondaryColor & 0x00FFFFFF));
				descriptionHtml = css + waypoint.description;
				description.setWebViewClient(new WebViewClient()
				{
				    @Override
				    public void onPageFinished(WebView view, String url)
				    {
				    	view.setBackgroundColor(Color.TRANSPARENT);
				        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				        	view.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
				    }
				});
				description.setBackgroundColor(Color.TRANSPARENT);
		        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		        	description.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
			}
			catch (Resources.NotFoundException e)
			{
				description.setBackgroundColor(Color.LTGRAY);
				descriptionHtml = waypoint.description;
			}
			
			WebSettings settings = description.getSettings();
			settings.setDefaultTextEncodingName("utf-8");
			settings.setAllowFileAccess(true);
			Uri baseUrl = Uri.fromFile(new File(application.dataPath));
			description.loadDataWithBaseURL(baseUrl.toString() + "/", descriptionHtml, "text/html", "utf-8", null);
		}

		String coords = StringFormatter.coordinates(application.coordinateFormat, " ", waypoint.latitude, waypoint.longitude);
		((TextView) view.findViewById(R.id.coordinates)).setText(coords);
		
		if (waypoint.altitude != Integer.MIN_VALUE)
		{
			//String altitude = String.format(Locale.getDefault(), "%d %s", waypoint.altitude, getResources().getStringArray(R.array.distance_abbrs_short)[2]);
			String altitude = StringFormatter.elevationC(waypoint.altitude);
					((TextView) view.findViewById(R.id.altitude)).setText(altitude);
		}
		
		double dist = Geo.distance(lat, lon, waypoint.latitude, waypoint.longitude);
		double bearing = Geo.bearing(lat, lon, waypoint.latitude, waypoint.longitude);
		bearing = application.fixDeclination(bearing);
		String distance = StringFormatter.distanceH(dist)+" "+StringFormatter.bearingH(bearing);
		((TextView) view.findViewById(R.id.distance)).setText(distance);

		if (waypoint.date != null)
			((TextView) view.findViewById(R.id.date)).setText(DateFormat.getDateFormat(activity).format(waypoint.date)+" "+DateFormat.getTimeFormat(activity).format(waypoint.date));
		else
			((TextView) view.findViewById(R.id.date)).setVisibility(View.GONE);
		assert dialog != null;
		if (icon != null) {
			dialog.setFeatureDrawable(Window.FEATURE_LEFT_ICON, icon);
		}
		else {
			dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_map);
		}
		
		dialog.setTitle(waypoint.name);
	}
}