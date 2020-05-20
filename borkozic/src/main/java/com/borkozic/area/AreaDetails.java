package com.borkozic.area;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.borkozic.Borkozic;
import com.borkozic.R;
import com.borkozic.data.Area;
import com.borkozic.data.Route;
import com.borkozic.data.Waypoint;
import com.borkozic.navigation.NavigationService;
import com.borkozic.route.RouteDetails;
import com.borkozic.util.StringFormatter;
import com.borkozic.waypoint.WaypointProperties;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

public class AreaDetails extends ListActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "RouteDetails";

    private static final int RESULT_START_ROUTE = 1;

    private static final int qaWaypointVisible = 1;
    private static final int qaWaypointNavigate = 2;
    private static final int qaWaypointProperties = 3;

    private NavigationService navigationService;
    private AreaDetails.WaypointListAdapter adapter;
    private QuickAction quickAction;

    private Area area;
    private boolean navigation;
    private int selectedPosition;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        int index = getIntent().getExtras().getInt("index");
        navigation = getIntent().getExtras().getBoolean("nav");

        Borkozic application = (Borkozic) getApplication();
        area = application.getArea(index);

        setTitle(navigation ? "› " + area.name : area.name);

        adapter = new AreaDetails.WaypointListAdapter(this, area);
        setListAdapter(adapter);

        Resources resources = getResources();
        quickAction = new QuickAction(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            quickAction.addActionItem(new ActionItem(qaWaypointVisible, getString(R.string.menu_view), resources.getDrawable(R.drawable.ic_action_show, null)));
        }else {
            quickAction.addActionItem(new ActionItem(qaWaypointVisible, getString(R.string.menu_view), resources.getDrawable(R.drawable.ic_action_show)));
        }

        if (navigation)
        {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                quickAction.addActionItem(new ActionItem(qaWaypointVisible, getString(R.string.menu_navigate), resources.getDrawable(R.drawable.ic_action_show, null)));
            }else {
                quickAction.addActionItem(new ActionItem(qaWaypointVisible, getString(R.string.menu_navigate), resources.getDrawable(R.drawable.ic_action_show)));
            }

        }
        else
        {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                quickAction.addActionItem(new ActionItem(qaWaypointVisible, getString(R.string.menu_edit), resources.getDrawable(R.drawable.ic_action_show, null)));
            }else {
                quickAction.addActionItem(new ActionItem(qaWaypointVisible, getString(R.string.menu_edit), resources.getDrawable(R.drawable.ic_action_show)));
            }
        }
        quickAction.setOnActionItemClickListener(actionItemClickListener);

        getListView().setOnItemClickListener(this);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        if (navigation)
        {
            bindService(new Intent(this, NavigationService.class), navigationConnection, BIND_AUTO_CREATE);
            boolean lock = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_wakelock), getResources().getBoolean(R.bool.def_wakelock));
            if (lock)
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (navigation)
        {
            unregisterReceiver(navigationReceiver);
            unbindService(navigationConnection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        if (! navigation)
        {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.routedetails_menu, menu);
        }
        return true;
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        selectedPosition = position;
        quickAction.show(view);
    }

    private QuickAction.OnActionItemClickListener actionItemClickListener = new QuickAction.OnActionItemClickListener(){
        @Override
        public void onItemClick(QuickAction source, int pos, int actionId)
        {
            Borkozic application = Borkozic.getApplication();
            switch (actionId)
            {
                case qaWaypointVisible:
                    area.show = true;
                    application.ensureVisible(area.getWaypoint(selectedPosition));
                    setResult(RESULT_OK);
                    finish();
                    break;
                case qaWaypointNavigate:
                    if (navigationService != null)
                    {
                        if (navigationService.navDirection == NavigationService.DIRECTION_REVERSE)
                            selectedPosition = area.length() - selectedPosition - 1;
                        navigationService.setRouteWaypoint(selectedPosition);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case qaWaypointProperties:
                    int index = application.getAreaIndex(area);
                    startActivity(new Intent(AreaDetails.this, WaypointProperties.class).putExtra("INDEX", selectedPosition).putExtra("ROUTE", index + 1));
                    break;
            }
        }
    };

    private ServiceConnection navigationConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            navigationService = ((NavigationService.LocalBinder) service).getService();
            registerReceiver(navigationReceiver, new IntentFilter(NavigationService.BROADCAST_NAVIGATION_STATUS));
            registerReceiver(navigationReceiver, new IntentFilter(NavigationService.BROADCAST_NAVIGATION_STATE));
            Log.d(TAG, "Navigation broadcast receiver registered");
            runOnUiThread(new Runnable() {
                public void run()
                {
                    adapter.notifyDataSetChanged();
                }
            });
        }

        public void onServiceDisconnected(ComponentName className)
        {
            unregisterReceiver(navigationReceiver);
            navigationService = null;
        }
    };

    private BroadcastReceiver navigationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.e(TAG, "Broadcast: " + intent.getAction());
            if (intent.getAction().equals(NavigationService.BROADCAST_NAVIGATION_STATE))
            {
                final int state = intent.getExtras().getInt("state");
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        if (state == NavigationService.STATE_REACHED)
                        {
                            Toast.makeText(getApplicationContext(), R.string.arrived, Toast.LENGTH_LONG).show();
                            navigation = false;
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            }
            if (intent.getAction().equals(NavigationService.BROADCAST_NAVIGATION_STATUS))
            {
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    };
    // Shows route waypoint list, distance, course, total distance НО би трябвало да го направя да показва:
    //точките на зоните и най-отдолу центъра на зоната и съответно дистанция и курс към нея
    public class WaypointListAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;
        private int mItemLayout;
        private Area mArea;

        public WaypointListAdapter(Context context, Area area)
        {
            mItemLayout = R.layout.area_waypoint_list;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mArea = area;
        }

        public Waypoint getItem(int position)
        {
            if (navigation && navigationService != null && navigationService.navDirection  == NavigationService.DIRECTION_REVERSE)
                position = mArea.length() - position - 1;
            return mArea.getWaypoint(position);
        }

        @Override
        public long getItemId(int position)
        {
            if (navigation && navigationService != null && navigationService.navDirection  == NavigationService.DIRECTION_REVERSE)
                position = mArea.length() - position - 1;
            return position;
        }

        @Override
        public int getCount()
        {
            return mArea.length();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v;
            if (convertView == null)
            {
                v = mInflater.inflate(mItemLayout, parent, false);
            }
            else
            {
                v = convertView;
                v = mInflater.inflate(mItemLayout, parent, false);
            }
            Waypoint wpt = (Waypoint) getItem(position);
            TextView text = (TextView) v.findViewById(R.id.name);
            TextView txtAlt =(TextView) v.findViewById(R.id.altitude);
            if (text != null)
            {
                text.setText(wpt.name);
                String[] dist = StringFormatter.distanceC(wpt.altitude, 10000);
                String Alt = dist[0] + dist[1];
                txtAlt.setText(Alt);

            }
            if (navigation && navigationService != null && navigationService.isNavigatingViaRoute())
            {
                int progress = position - navigationService.navRouteCurrentIndex();
                if (position > 0)
                {
                    double dist = progress == 0 ? navigationService.navDistance : mArea.distanceBetween(position - 1, position);
                    String distance = StringFormatter.distanceH(dist);
                    text =  v.findViewById(R.id.distance);
                    if (text != null)
                    {
                        text.setText(distance);
//						if (progress == 0)
//							text.setTextAppearance(RouteDetails.this, resid);
                    }
                    double crs;
                    if (progress == 0)
                        crs = navigationService.navBearing;
                    else if (navigationService.navDirection == NavigationService.DIRECTION_FORWARD)
                        crs = mArea.course(position - 1, position);
                    else
                        crs = mArea.course(position, position - 1);
                    String course = StringFormatter.bearingH(crs);
                    text = (TextView) v.findViewById(R.id.course);
                    if (text != null)
                    {
                        text.setText(course);
                    }
                }
                if (progress >= 0)
                {
                    double dist = navigationService.navDistance;
                    if (progress > 0)
                        dist += navigationService.navRouteDistanceLeftTo(position);
                    String distance = StringFormatter.distanceH(dist);
                    text = (TextView) v.findViewById(R.id.total_distance);
                    if (text != null)
                    {
                        text.setText(distance);
                    }
                    int ete = progress == 0 ? navigationService.navETE : navigationService.navRouteWaypointETE(position);
                    String s = StringFormatter.timeR(ete);
                    text = (TextView) v.findViewById(R.id.ete);
                    if (text != null)
                    {
                        text.setText(s);
                    }
                    int eta = navigationService.navETE;
                    if (progress > 0 && eta < Integer.MAX_VALUE)
                    {
                        int t = navigationService.navRouteETETo(position);
                        if (t < Integer.MAX_VALUE)
                            eta += t;
                    }
                    s = StringFormatter.timeR(eta);
                    text = (TextView) v.findViewById(R.id.eta);
                    if (text != null)
                    {
                        text.setText(s);
                    }

                    if (progress == 0)
                    {
                        text = (TextView) v.findViewById(R.id.name);
                        text.setText("» " + text.getText());
                    }
                }
                else
                {
                    text = (TextView) v.findViewById(R.id.name);
                    text.setTextColor(text.getTextColors().withAlpha(128));
                    text = (TextView) v.findViewById(R.id.distance);
                    text.setTextColor(text.getTextColors().withAlpha(128));
                    text = (TextView) v.findViewById(R.id.course);
                    text.setTextColor(text.getTextColors().withAlpha(128));
                }
            }
            else
            {
                if (position > 0)
                {
                    double dist = mArea.distanceBetween(position - 1, position);
                    String distance = StringFormatter.distanceH(dist);
                    text = (TextView) v.findViewById(R.id.distance);
                    if (text != null)
                    {
                        text.setText(distance);
                    }
                    double crs = mArea.course(position - 1, position);
                    String course = StringFormatter.bearingH(crs);
                    text = (TextView) v.findViewById(R.id.course);
                    if (text != null)
                    {
                        text.setText(course);
                    }
                }
                double dist = position > 0 ? mArea.distanceBetween(0, position) : 0.;
                String distance = StringFormatter.distanceH(dist);
                text = (TextView) v.findViewById(R.id.total_distance);
                if (text != null)
                {
                    text.setText(distance);
                }
            }

            return v;
        }

        @Override
        public boolean hasStableIds()
        {
            return true;
        }
    }


}
