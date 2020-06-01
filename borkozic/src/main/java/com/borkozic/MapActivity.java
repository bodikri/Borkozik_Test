/*
Това е централното активити когато е заредена картата. От него се управляват и активират всички дейности.
 */

package com.borkozic;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
//import androidx.core.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.borkozic.area.AreaDetails;
import com.borkozic.area.AreaEdit;
import com.borkozic.area.AreaList;
import com.borkozic.area.AreaListActivity;
import com.borkozic.data.Area;
import com.borkozic.data.MapObject;
import com.borkozic.data.Route;
import com.borkozic.data.Track;
import com.borkozic.data.Waypoint;
import com.borkozic.data.WaypointSet;
import com.borkozic.location.ILocationListener;
import com.borkozic.location.ILocationService;
import com.borkozic.location.LocationService;
import com.borkozic.map.MapInformation;
import com.borkozic.navigation.NavigationService;
import com.borkozic.overlay.AccuracyOverlay;
import com.borkozic.overlay.AreaOverlay;
import com.borkozic.overlay.CurrentTrackOverlay;
import com.borkozic.overlay.DistanceOverlay;
import com.borkozic.overlay.MapObjectsOverlay;
import com.borkozic.overlay.NavigationOverlay;
import com.borkozic.overlay.RouteOverlay;
import com.borkozic.overlay.ScaleOverlay;
import com.borkozic.overlay.TrackOverlay;
import com.borkozic.overlay.WaypointsOverlay;
import com.borkozic.route.RouteDetails;
import com.borkozic.route.RouteEdit;
import com.borkozic.route.RouteList;
import com.borkozic.route.RouteListActivity;
import com.borkozic.route.RouteStart;
import com.borkozic.track.TrackExportDialog;
import com.borkozic.track.TrackListActivity;
import com.borkozic.util.Astro;
import com.borkozic.util.CoordinateParser;
import com.borkozic.util.Geo;
import com.borkozic.util.OziExplorerFiles;
import com.borkozic.util.StringFormatter;
import com.borkozic.waypoint.OnWaypointActionListener;
import com.borkozic.waypoint.WaypointFileList;
import com.borkozic.waypoint.WaypointInfo;
import com.borkozic.waypoint.WaypointListActivity;
import com.borkozic.waypoint.WaypointProject;
import com.borkozic.waypoint.WaypointProperties;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction3D;
import net.londatiga.android.QuickAction3D.OnActionItemClickListener;

import org.miscwidgets.interpolator.EasingType.Type;
import org.miscwidgets.interpolator.ExpoInterpolator;
import org.miscwidgets.widget.Panel;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class MapActivity extends AppCompatActivity implements View.OnClickListener, OnSharedPreferenceChangeListener, OnWaypointActionListener, SeekBar.OnSeekBarChangeListener, Panel.OnPanelListener
{
    private static final String TAG = "MapActivity";
    public static final String BTN_TITLE = "BtnTitle";
    private static final int RESULT_MANAGE_WAYPOINTS = 0x200;
    private static final int RESULT_LOAD_WAYPOINTS = 0x300;
    private static final int RESULT_SAVE_WAYPOINT = 0x400;
    private static final int RESULT_LOAD_MAP = 0x500;
    private static final int RESULT_MANAGE_TRACKS = 0x600;
    private static final int RESULT_MANAGE_ROUTES = 0x900;
    private static final int RESULT_EDIT_ROUTE = 0x110;
    private static final int RESULT_LOAD_MAP_ATPOSITION = 0x120;
    private static final int RESULT_SAVE_WAYPOINTS = 0x140;

    private static final int RESULT_MANAGE_AREAS = 0x150;
    private static final int RESULT_EDIT_AREA = 0x160;

    private static final int qaAddWaypointToRoute = 1;
    private static final int qaNavigateToWaypoint = 2;
    private static final int qaNavigateToMapObject = 2;

    private static final int qaAddWaypointToArea = 1;

    private static final int SCREEN_ORIENTATION_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    // main preferences
    protected String precisionFormat = "%.0f";
    protected double speedFactor;
    protected String speedAbbr;
    protected double elevationFactor;
    protected String elevationAbbr;
    protected double zeroElevation;
    private double lastElevation;
    protected int renderInterval;
    protected int magInterval;
    protected boolean autoDim;
    protected int dimInterval;
    protected int dimValue;
    protected int showDistance;
    protected boolean showAccuracy;
    protected boolean followOnLocation;
    protected int exitConfirmation;
    private boolean secondBack;
    private Toast backToast;

    private TextView coordinates;
    private TextView satInfo;
    private TextView accuracy;

    private TextView waypointName;
    private TextView waypointExtra;
    private TextView routeName;
    private TextView routeExtra;
    private TextView areaName;
    private TextView areaExtra;

    //private TextView distanceValue;
    //private TextView distanceUnit;
    //private TextView bearingValue;
    //private TextView bearingUnit;
    private TextView belowaboveValue;
    private TextView belowaboveUnit;
    private TextView belowaboveName;
    private TextView turnValue;

    private TextView speedValue;
    private TextView speedUnit;
    private TextView trackValue;
    private TextView trackUnit;
    private TextView elevationName;
    private TextView elevationValue;
    private TextView elevationUnit;
    private TextView xtkValue;
    private TextView xtkUnit;

    private TextView currentFile;
    private TextView mapZoom;

    protected SeekBar trackBar;
    protected TextView waitBar;
    protected MapView map;
    protected QuickAction3D wptQuickAction;
    protected QuickAction3D rteQuickAction;
    protected QuickAction3D mobQuickAction;
    private ViewGroup dimView;

    protected Borkozic application;

    protected ExecutorService executorThread = Executors.newSingleThreadExecutor();
    private FinishHandler finishHandler;

    private int waypointSelected = -1;
    private int routeSelected = -1;
    private int areaSelected = -1;
    private long mapObjectSelected = -1;

    private ILocationService locationService = null;
    public NavigationService navigationService = null;

    private Location lastKnownLocation;
    protected long lastRenderTime = 0;
    protected long lastDim = 0;
    protected long lastMagnetic = 0;
    private boolean lastGeoid = true;

    private boolean animationSet;
    private boolean isFullscreen;
    private boolean keepScreenOn;
    private String[] panelActions;
    private List<String> activeActions;
    LightingColorFilter disable = new LightingColorFilter(0xFFFFFFFF, 0xFF555555);

    protected boolean ready = false;
    private boolean restarting = false;

    /* Called when the activity is first created. */
    @SuppressLint("ShowToast")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate()");

        ready = false;
        isFullscreen = false;

        backToast = Toast.makeText(this, R.string.backQuit, Toast.LENGTH_SHORT);
        finishHandler = new FinishHandler(this);

        application = (Borkozic) getApplication();

        // FIXME Should find a better place for this
        application.mapObjectsOverlay = new MapObjectsOverlay(this);

        // check if called after crash
        if (!application.mapsInited)
        {
            restarting = true;
            startActivity(new Intent(this, Splash.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK).putExtras(getIntent()));
            finish();
            return;
        }

        application.setMapActivity(this);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        setRequestedOrientation(Integer.parseInt(settings.getString(getString(R.string.pref_orientation), "-1")));
        //setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        settings.registerOnSharedPreferenceChangeListener(this);
        Resources resources = getResources();
        if (settings.getBoolean(getString(R.string.pref_hideactionbar), resources.getBoolean(R.bool.def_hideactionbar)))
        {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        panelActions = getResources().getStringArray(R.array.panel_action_values);

        setContentView(R.layout.act_main);
        coordinates = (TextView) findViewById(R.id.coordinates);
        satInfo = (TextView) findViewById(R.id.sats);
        accuracy = (TextView) findViewById(R.id.accuracy_satinfo);
        currentFile = (TextView) findViewById(R.id.currentfile);
        mapZoom = (TextView) findViewById(R.id.currentzoom);
        waypointName = (TextView) findViewById(R.id.waypointname);
        waypointExtra = (TextView) findViewById(R.id.waypointextra);
        routeName = (TextView) findViewById(R.id.routename);
        routeExtra = (TextView) findViewById(R.id.routeextra);
        areaName = (TextView) findViewById(R.id.areaname);
        areaExtra = (TextView) findViewById(R.id.areaextra);
        speedValue = (TextView) findViewById(R.id.speed);
        speedUnit = (TextView) findViewById(R.id.speedunit);
        trackValue = (TextView) findViewById(R.id.track);
        trackUnit = (TextView) findViewById(R.id.trackunit);
        elevationValue = (TextView) findViewById(R.id.elevation);
        elevationName = (TextView) findViewById(R.id.elevationname);
        elevationUnit = (TextView) findViewById(R.id.elevationunit);
        //distanceValue = (TextView) findViewById(R.id.distance);
        //distanceUnit = (TextView) findViewById(R.id.distanceunit);
        belowaboveValue =(TextView) findViewById(R.id.abovebelowGS);
        belowaboveUnit = (TextView) findViewById(R.id.abovebelowGSunit);
        belowaboveName =(TextView) findViewById(R.id.abovebelowGSname);
        xtkValue = (TextView) findViewById(R.id.xtk);
        xtkUnit = (TextView) findViewById(R.id.xtkunit);
        //bearingValue = (TextView) findViewById(R.id.bearing);
        //bearingUnit = (TextView) findViewById(R.id.bearingunit);
        turnValue = (TextView) findViewById(R.id.turn);
        trackBar = (SeekBar) findViewById(R.id.trackbar);
        waitBar = (TextView) findViewById(R.id.waitbar);
        map = (MapView) findViewById(R.id.mapview);

        // set button actions
        findViewById(R.id.zoomin).setOnClickListener(this);
        findViewById(R.id.zoomout).setOnClickListener(this);
        findViewById(R.id.nextmap).setOnClickListener(this);
        findViewById(R.id.prevmap).setOnClickListener(this);
        findViewById(R.id.maps).setOnClickListener(this);
        findViewById(R.id.waypoints).setOnClickListener(this);
        findViewById(R.id.info).setOnClickListener(this);
        findViewById(R.id.follow).setOnClickListener(this);
        findViewById(R.id.locate).setOnClickListener(this);
        findViewById(R.id.tracking).setOnClickListener(this);
        findViewById(R.id.expand).setOnClickListener(this);
        findViewById(R.id.finishedit).setOnClickListener(this);
        findViewById(R.id.addpoint).setOnClickListener(this);
        findViewById(R.id.insertpoint).setOnClickListener(this);
        findViewById(R.id.removepoint).setOnClickListener(this);
        findViewById(R.id.orderpoints).setOnClickListener(this);
        findViewById(R.id.finishtrackedit).setOnClickListener(this);
        findViewById(R.id.cutafter).setOnClickListener(this);
        findViewById(R.id.cutbefore).setOnClickListener(this);

        findViewById(R.id.norm_button).setOnClickListener(this);
        findViewById(R.id.emer_button).setOnClickListener(this);
        findViewById(R.id.zero_button).setOnClickListener(this);
        findViewById(R.id.clear_button).setOnClickListener(this);


        Panel panel = (Panel) findViewById(R.id.panel);
        panel.setOnPanelListener(this);
        panel.setInterpolator(new ExpoInterpolator(Type.OUT));

        wptQuickAction = new QuickAction3D(this, QuickAction3D.VERTICAL);//ContextCompat.getDrawable(getActivity(), R.drawable.ic_action_add);
        wptQuickAction.addActionItem(new ActionItem(qaAddWaypointToRoute, getString(R.string.menu_addtoroute), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_add, null)));
        wptQuickAction.setOnActionItemClickListener(waypointActionItemClickListener);//resources.getDrawable(R.drawable.ic_action_add)));

        rteQuickAction = new QuickAction3D(this, QuickAction3D.VERTICAL);
        rteQuickAction.addActionItem(new ActionItem(qaNavigateToWaypoint, getString(R.string.menu_thisnavpoint), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_directions, null)));
        rteQuickAction.setOnActionItemClickListener(routeActionItemClickListener);//ic_action_directions

        mobQuickAction = new QuickAction3D(this, QuickAction3D.VERTICAL);
        mobQuickAction.addActionItem(new ActionItem(qaNavigateToMapObject, getString(R.string.menu_navigate), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_directions, null)));
        mobQuickAction.setOnActionItemClickListener(mapObjectActionItemClickListener);

        trackBar.setOnSeekBarChangeListener(this);
//Да се заредят planeLogo and size
        //onSharedPreferenceChanged(settings, getString(R.string.pref_exit));
        map.planeLogo = (application.planePath).substring(36);
        map.setMovingCursorSize(settings.getInt("planelogosize", resources.getInteger(R.integer.def_planelogosize)));
        map.initialize(application);

        dimView = new RelativeLayout(this);
        //Зарежда навигация към точка ако има стартиран такъв (MapObject-Wpt)
        String navWpt = settings.getString(getString(R.string.nav_wpt), "");
        if (!"".equals(navWpt) && savedInstanceState == null)
        {
            Intent intent = new Intent(getApplicationContext(), NavigationService.class).setAction(NavigationService.NAVIGATE_MAPOBJECT);
            intent.putExtra(NavigationService.EXTRA_NAME, navWpt);
            intent.putExtra(NavigationService.EXTRA_LATITUDE, (double) settings.getFloat(getString(R.string.nav_wpt_lat), 0));
            intent.putExtra(NavigationService.EXTRA_LONGITUDE, (double) settings.getFloat(getString(R.string.nav_wpt_lon), 0));
            intent.putExtra(NavigationService.EXTRA_PROXIMITY, settings.getInt(getString(R.string.nav_wpt_prx), 0));
            startService(intent);
        }
        //По същият пример трябва да зарежда навигация към зона( към точка в центъра на зоната)
        /*
         String navАреа = settings.getString(getString(R.string.nav_ареа), "");
        if (!"".equals(navАреа) && savedInstanceState == null)
        {
            Intent intent = new Intent(getApplicationContext(), NavigationService.class).setAction(NavigationService.NAVIGATE_MAPOBJECT);
            intent.putExtra(NavigationService.EXTRA_NAME, navАреа);
            intent.putExtra(NavigationService.EXTRA_LATITUDE, (double) settings.getFloat(getString(R.string.nav_ареа_lat), 0));
            intent.putExtra(NavigationService.EXTRA_LONGITUDE, (double) settings.getFloat(getString(R.string.nav_ареа_lon), 0));
            intent.putExtra(NavigationService.EXTRA_PROXIMITY, settings.getInt(getString(R.string.nav_ареа_prx), 0));
            startService(intent);
        }
        */
        //Зарежда навигация по Маршрут ако има стартиран такъв
        String navRoute = settings.getString(getString(R.string.nav_route), "");
        if (!"".equals(navRoute) && settings.getBoolean(getString(R.string.pref_navigation_loadlast), getResources().getBoolean(R.bool.def_navigation_loadlast)) && savedInstanceState == null)
        {
            int ndir = settings.getInt(getString(R.string.nav_route_dir), 0);
            int nwpt = settings.getInt(getString(R.string.nav_route_wpt), -1);
            try
            {
                int rt = -1;
                Route route = application.getRouteByFile(navRoute);
                if (route != null)
                {
                    route.show = true;
                    rt = application.getRouteIndex(route);
                }
                else
                {
                    File rtf = new File(navRoute);
                    // FIXME It's bad - it can be not a first route in a file
                    route = OziExplorerFiles.loadRoutesFromFile(rtf, application.charset).get(0);
                    rt = application.addRoute(route);
                }
                RouteOverlay newRoute = new RouteOverlay(this, route);
                application.routeOverlays.add(newRoute);
                startService(new Intent(this, NavigationService.class).setAction(NavigationService.NAVIGATE_ROUTE).putExtra(NavigationService.EXTRA_ROUTE_INDEX, rt).putExtra(NavigationService.EXTRA_ROUTE_DIRECTION, ndir).putExtra(NavigationService.EXTRA_ROUTE_START, nwpt));
            }
            catch (Exception e)
            {
                Log.e(TAG, "Failed to start navigation", e);
            }
        }

        // set activity preferences
        onSharedPreferenceChanged(settings, getString(R.string.pref_exit));
        onSharedPreferenceChanged(settings, getString(R.string.pref_unitprecision));
        // set map preferences
        onSharedPreferenceChanged(settings, getString(R.string.pref_mapadjacent));
        onSharedPreferenceChanged(settings, getString(R.string.pref_mapcropborder));
        onSharedPreferenceChanged(settings, getString(R.string.pref_mapdrawborder));
        onSharedPreferenceChanged(settings, getString(R.string.pref_cursorcolor));
        onSharedPreferenceChanged(settings, getString(R.string.pref_grid_mapshow));
        onSharedPreferenceChanged(settings, getString(R.string.pref_grid_usershow));
        onSharedPreferenceChanged(settings, getString(R.string.pref_grid_preference));
        onSharedPreferenceChanged(settings, getString(R.string.pref_panelactions));
        onSharedPreferenceChanged(settings, getString(R.string.pref_maprotation));
        if (getIntent().getExtras() != null)
            onNewIntent(getIntent());

        ready = true;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.e(TAG, "onStart()");
        ((ViewGroup) getWindow().getDecorView()).addView(dimView);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "onNewIntent()");
        if (intent.hasExtra("launch")) {
            Serializable object = intent.getExtras().getSerializable("launch");
            if (Class.class.isInstance(object)) {
                Intent launch = new Intent(this, (Class<?>) object);
                launch.putExtras(intent);
                launch.removeExtra("launch");
                startActivity(launch);
            }
        } else if (intent.hasExtra("lat") && intent.hasExtra("lon")) {
            Borkozic application = (Borkozic) getApplication();
            application.ensureVisible(intent.getExtras().getDouble("lat"), intent.getExtras().getDouble("lon"));
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e(TAG, "onResume()");

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        Resources resources = getResources();

        // update some preferences
        try {
            String def_plane = (application.planePath).substring(36);
            //Let we see what kind procedures are available
            getSupportActionBar().setTitle(resources.getString(R.string.app_name) + "-" + def_plane);

        }catch (Exception e)
        {
            Toast.makeText(MapActivity.this,"Неточен път към папката за процедури!", Toast.LENGTH_LONG).show();
        }
        int speedIdx = Integer.parseInt(settings.getString(getString(R.string.pref_unitspeed), "0"));
        speedFactor = Double.parseDouble(resources.getStringArray(R.array.speed_factors)[speedIdx]);//множител който преобразува от м/с в км/ч или мили(както е избрано в  настройките)
        speedAbbr = resources.getStringArray(R.array.speed_abbrs)[speedIdx];
        speedUnit.setText(speedAbbr);
        int distanceIdx = Integer.parseInt(settings.getString(getString(R.string.pref_unitdistance), "0"));
        int elevationIdx = Integer.parseInt(settings.getString(getString(R.string.pref_unitelevation), "0"));
        elevationFactor = Double.parseDouble(resources.getStringArray(R.array.elevation_factors)[elevationIdx]);//множител който преобразува височината от метри в фити
        elevationAbbr = resources.getStringArray(R.array.elevation_abbrs)[elevationIdx];
        elevationUnit.setText(elevationAbbr);
        StringFormatter.distanceFactor = Double.parseDouble(resources.getStringArray(R.array.distance_factors)[distanceIdx]);
        StringFormatter.distanceAbbr = resources.getStringArray(R.array.distance_abbrs)[distanceIdx];
        StringFormatter.distanceShortFactor = Double.parseDouble(resources.getStringArray(R.array.distance_factors_short)[distanceIdx]);
        StringFormatter.distanceShortAbbr = resources.getStringArray(R.array.distance_abbrs_short)[distanceIdx];
        StringFormatter.elevationFactor = Double.parseDouble(resources.getStringArray(R.array.elevation_factors)[elevationIdx]);

        application.angleType = Integer.parseInt(settings.getString(getString(R.string.pref_unitangle), "0"));
        trackUnit.setText((application.angleType == 0 ? "deg" : getString(R.string.degmag)));
        //bearingUnit.setText((application.angleType == 0 ? "deg" : getString(R.string.degmag)));
        application.coordinateFormat = Integer.parseInt(settings.getString(getString(R.string.pref_unitcoordinate), "0"));
        application.sunriseType = Integer.parseInt(settings.getString(getString(R.string.pref_unitsunrise), "0"));

        renderInterval = settings.getInt(getString(R.string.pref_maprenderinterval), resources.getInteger(R.integer.def_maprenderinterval)) * 100;
        followOnLocation = settings.getBoolean(getString(R.string.pref_mapfollowonloc), resources.getBoolean(R.bool.def_mapfollowonloc));
        magInterval = resources.getInteger(R.integer.def_maginterval) * 1000;
        showDistance = Integer.parseInt(settings.getString(getString(R.string.pref_showdistance_int), getString(R.string.def_showdistance)));
        showAccuracy = settings.getBoolean(getString(R.string.pref_showaccuracy), true);
        autoDim = settings.getBoolean(getString(R.string.pref_mapdim), resources.getBoolean(R.bool.def_mapdim));
        dimInterval = settings.getInt(getString(R.string.pref_mapdiminterval), resources.getInteger(R.integer.def_mapdiminterval)) * 1000;
        dimValue = settings.getInt(getString(R.string.pref_mapdimvalue), resources.getInteger(R.integer.def_mapdimvalue));

        map.setHideOnDrag(settings.getBoolean(getString(R.string.pref_maphideondrag), resources.getBoolean(R.bool.def_maphideondrag)));
        map.setStrictUnfollow(!settings.getBoolean(getString(R.string.pref_unfollowontap), resources.getBoolean(R.bool.def_unfollowontap)));
        map.setLookAhead(settings.getInt(getString(R.string.pref_lookahead), resources.getInteger(R.integer.def_lookahead)));
        map.setTrackUp(settings.getString(getString(R.string.pref_maprotation), resources.getString(R.string.def_maprotation)));
        map.setBestMapEnabled(settings.getBoolean(getString(R.string.pref_mapbest), resources.getBoolean(R.bool.def_mapbest)));
        map.setBestMapInterval(settings.getInt(getString(R.string.pref_mapbestinterval), resources.getInteger(R.integer.def_mapbestinterval)) * 1000);
        map.setCursorVector(Integer.parseInt(settings.getString(getString(R.string.pref_cursorvector), getString(R.string.def_cursorvector))), settings.getInt(getString(R.string.pref_cursorvectormlpr), resources.getInteger(R.integer.def_cursorvectormlpr)));
        map.setProximity(Integer.parseInt(settings.getString(getString(R.string.pref_navigation_proximity), getString(R.string.def_navigation_proximity))));// Задава близост при достигане на която навигацията автоматично превключва на следваща точка

        // prepare views
        customizeLayout(settings);
        findViewById(R.id.editroute).setVisibility(application.editingRoute != null || application.editingArea != null ? View.VISIBLE : View.GONE);//появяа се само при режим на редакция на маршрут/зона

        if (application.editingTrack != null)
        {
            startEditTrack(application.editingTrack);
        }
        updateGPSStatus();
        updateNavigationStatus();
        // prepare overlays
        updateOverlays(settings, false);

        if (settings.getBoolean(getString(R.string.ui_drawer_open), false))
        {
            Panel panel = (Panel) findViewById(R.id.panel);
            panel.setOpen(true, false);
        }

        onSharedPreferenceChanged(settings, getString(R.string.pref_wakelock));
        map.setKeepScreenOn(keepScreenOn);

        // TODO move into application
        if (lastKnownLocation != null)
        {
            if (lastKnownLocation.getProvider().equals(LocationManager.GPS_PROVIDER))
            {
                updateMovingInfo(lastKnownLocation, true);
                updateNavigationInfo();
                dimScreen(lastKnownLocation);
            }
            else if (lastKnownLocation.getProvider().equals(LocationManager.NETWORK_PROVIDER))
            {
                dimScreen(lastKnownLocation);
            }
        }

        bindService(new Intent(this, LocationService.class), locationConnection, BIND_AUTO_CREATE);
        bindService(new Intent(this, NavigationService.class), navigationConnection, BIND_AUTO_CREATE);

        registerReceiver(broadcastReceiver, new IntentFilter(NavigationService.BROADCAST_NAVIGATION_STATUS));
        registerReceiver(broadcastReceiver, new IntentFilter(NavigationService.BROADCAST_NAVIGATION_STATE));
        registerReceiver(broadcastReceiver, new IntentFilter(LocationService.BROADCAST_LOCATING_STATUS));
        registerReceiver(broadcastReceiver, new IntentFilter(LocationService.BROADCAST_TRACKING_STATUS));
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));

        if (application.hasEnsureVisible())
        {
            setFollowing(false);
            followOnLocation = false;
            double[] loc = application.getEnsureVisible();
            application.setMapCenter(loc[0], loc[1], true, false);
            application.clearEnsureVisible();
        }
        else
        {
            application.updateLocationMaps(true, map.isBestMapEnabled());
        }
        updateMapViewArea();
        map.resume();
        map.updateMapInfo();
        map.update();
        map.requestFocus();
        Log.e(TAG, "After map.requestFocus()");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.e(TAG, "onPause()");

        unregisterReceiver(broadcastReceiver);
        map.pause();

        // save active route or Wpt
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(getString(R.string.nav_route), "");
        editor.putString(getString(R.string.nav_wpt), "");
        if (navigationService != null)
        {
            if (navigationService.isNavigatingViaRoute())
            {
                Route route = navigationService.navRoute;
                if (route.filepath != null)
                {
                    editor.putString(getString(R.string.nav_route), route.filepath);
                    editor.putInt(getString(R.string.nav_route_idx), application.getRouteIndex(navigationService.navRoute));
                    editor.putInt(getString(R.string.nav_route_dir), navigationService.navDirection);
                    editor.putInt(getString(R.string.nav_route_wpt), navigationService.navCurrentRoutePoint);
                }
            }
            else if (navigationService.isNavigating())
            {
                MapObject wpt = navigationService.navWaypoint;
                editor.putString(getString(R.string.nav_wpt), wpt.name);
                editor.putInt(getString(R.string.nav_wpt_prx), wpt.proximity);
                editor.putFloat(getString(R.string.nav_wpt_lat), (float) wpt.latitude);
                editor.putFloat(getString(R.string.nav_wpt_lon), (float) wpt.longitude);
            }
        }
        editor.apply();

        if (navigationService != null)
        {
            unbindService(navigationConnection);
            navigationService = null;
        }
        if (locationService != null)
        {
            locationService.unregisterLocationCallback(locationListener);
            locationService = null;
        }
        unbindService(locationConnection);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Log.e(TAG, "onStop()");
        ((ViewGroup) getWindow().getDecorView()).removeView(dimView);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.e(TAG, "onDestroy()");
        ready = false;

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

        if (isFinishing() && !restarting)
        {
            // clear all overlays from map
            updateOverlays(null, true);
            application.waypointsOverlay = null;
            application.navigationOverlay = null;
            application.distanceOverlay = null;

            application.clear();
        }

        restarting = false;

        application = null;

        map = null;

        coordinates = null;
        satInfo = null;
        currentFile = null;
        mapZoom = null;
        waypointName = null;
        waypointExtra = null;
        routeName = null;
        routeExtra = null;
        speedValue = null;
        speedUnit = null;
        trackValue = null;
        elevationValue = null;
        elevationUnit = null;
        elevationName=null;
        //distanceValue = null;
        //distanceUnit = null;
        xtkValue = null;
        xtkUnit = null;
        belowaboveName=null;
        belowaboveUnit=null;
        belowaboveValue=null;
        //bearingValue = null;
        turnValue = null;
        trackBar = null;
    }
//От тук започва същинската част на кода какво да прави машинката при нормална работа
    //Прави връзка с датчиците за местоположение и обновява информацията при стартирана навигация
    private ServiceConnection navigationConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            navigationService = ((NavigationService.LocalBinder) service).getService();
            runOnUiThread(new Runnable() {
                public void run()
                {
                    if (!ready)
                        return;
                    updateNavigationStatus();
                    updateNavigationInfo();
                }
            });
            Log.d(TAG, "Navigation service connected");
        }

        public void onServiceDisconnected(ComponentName className)
        {
            navigationService = null;
            Log.d(TAG, "Navigation service disconnected");
        }
    };
    //Какво да прави при обновяване на информацията за местоположението
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            Log.e(TAG, "Broadcast: " + action);
            if (action.equals(NavigationService.BROADCAST_NAVIGATION_STATE))
            {
                final int state = intent.getExtras().getInt("state");
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        if (!ready)
                            return;
                        if (state == NavigationService.STATE_REACHED)
                        {
                            Toast.makeText(getApplicationContext(), R.string.arrived, Toast.LENGTH_LONG).show();
                        }
                        updateNavigationStatus();
                    }
                });
            }
            else if (action.equals(NavigationService.BROADCAST_NAVIGATION_STATUS))
            {
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        if (!ready)
                            return;
                        updateNavigationInfo();
                    }
                });
            }
            else if (action.equals(LocationService.BROADCAST_TRACKING_STATUS))
            {
                updateMapButtons();
            }
            else if (action.equals(LocationService.BROADCAST_LOCATING_STATUS))
            {
                updateMapButtons();
                if (locationService != null && !locationService.isLocating())
                    map.clearLocation();
            }
            // In fact this is not needed on modern devices through activity is always
            // paused when the screen is turned off. But we will keep it, may be there
            // exist some devices (ROMs) that do not pause activities.
            else if (action.equals(Intent.ACTION_SCREEN_OFF))
            {
                map.pause();
            }
            else if (action.equals(Intent.ACTION_SCREEN_ON))
            {
                map.resume();
            }
        }
    };

    private ServiceConnection locationConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder)
        {
            locationService = (ILocationService) binder;
            locationService.registerLocationCallback(locationListener);
            Log.d(TAG, "Location service connected");
        }

        public void onServiceDisconnected(ComponentName className)
        {
            locationService = null;
            Log.d(TAG, "Location service disconnected");
        }
    };

    private ILocationListener locationListener = new ILocationListener() {
        @Override
        public void onGpsStatusChanged(String provider, final int status, final int fsats, final int tsats)
        {
            if (LocationManager.GPS_PROVIDER.equals(provider))//при обновяване на информация получена от GPS
            {
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        if (!ready)
                            return;
                        switch (status)
                        {
                            case LocationService.GPS_OK:
                                if (!map.isFixed())
                                {
                                    satInfo.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gpsworking));
                                    map.setMoving(true);
                                    map.setFixed(true);
                                    updateGPSStatus();
                                }
                                satInfo.setText(String.valueOf(fsats) + "/" + String.valueOf(tsats));
                                break;
                            case LocationService.GPS_OFF:
                                satInfo.setText(R.string.sat_stop);
                                satInfo.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gpsdisabled));
                                map.setMoving(false);
                                map.setFixed(false);
                                updateGPSStatus();
                                break;
                            case LocationService.GPS_SEARCHING:
                                if (map.isFixed())
                                {
                                    satInfo.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gpsenabled));
                                    map.setFixed(false);
                                }
                                satInfo.setText(String.valueOf(fsats) + "/" + String.valueOf(tsats));
                                break;
                        }
                    }
                });
            }
        }

        @Override
        public void onLocationChanged(final Location location, final boolean continous, final boolean geoid, final float smoothspeed, final float avgspeed)
        {
            if (!ready)
                return;

            //Log.d(TAG, "Location arrived");

            final long lastLocationMillis = location.getTime();

            boolean magnetic = false;
            if (application.angleType == 1 && lastLocationMillis - lastMagnetic >= magInterval)
            {
                magnetic = true;
                lastMagnetic = lastLocationMillis;
            }

            // update map
            if (lastLocationMillis - lastRenderTime >= renderInterval)
            {
                lastRenderTime = lastLocationMillis;

                application.setLocation(location, magnetic);
                map.setLocation(location);
                final boolean enableFollowing = followOnLocation && lastKnownLocation == null;

                lastKnownLocation = location;

                if (application.accuracyOverlay != null && location.hasAccuracy())
                {
                    application.accuracyOverlay.setAccuracy(location.getAccuracy());
                }

                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        if (!LocationManager.GPS_PROVIDER.equals(location.getProvider()) && map.isMoving())
                        {
                            map.setMoving(false);
                            updateGPSStatus();
                        }
                        // Mock provider hack
                        if (!map.isFixed() && continous && LocationManager.GPS_PROVIDER.equals(location.getProvider()))
                        {
                            satInfo.setText(R.string.sat_start);
                            satInfo.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gpsworking));//gpsworking
                            map.setMoving(true);
                            map.setFixed(true);
                            updateGPSStatus();
                        }
                        accuracy.setText(location.hasAccuracy() ? ("Accuracy: " +StringFormatter.distanceH(location.getAccuracy(), "%.1f", 1000)) : "N/A");
                        updateMovingInfo(location, geoid);//обновява информацията за скоростта, височината и курса

                        if (enableFollowing)
                            setFollowing(true);
                        else
                            map.update();

                        // auto dim
                        if (autoDim && dimInterval > 0 && lastLocationMillis - lastDim >= dimInterval)
                        {
                            dimScreen(location);
                            lastDim = lastLocationMillis;
                        }
                    }
                });
            }
        }

        @Override
        public void onProviderChanged(String provider)
        {
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            if (LocationManager.GPS_PROVIDER.equals(provider))
            {
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        if (!ready)
                            return;
                        satInfo.setText(R.string.sat_stop);
                        satInfo.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gpsdisabled));//gpsdisabled
                        map.setMoving(false);
                        map.setFixed(false);
                        updateGPSStatus();
                    }
                });
            }
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            if (LocationManager.GPS_PROVIDER.equals(provider))
            {
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        if (!ready)
                            return;
                        if (!map.isFixed())
                        {
                            satInfo.setText(R.string.sat_start);
                            //colorGlidePath = ContextCompat.getColor(getApplicationContext(), R.color.aboveGlidePath)
                            //getResources().getColor(R.color.gpsenabled)
                            satInfo.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gpsenabled));
                        }
                    }
                });
            }
        }
    };

    private void updateMapViewArea()
    {
        final ViewTreeObserver vto = map.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout()
            {
                Rect area = new Rect();
                map.getLocalVisibleRect(area);
                View v = findViewById(R.id.topbar);
                if (v != null)
                    area.top = v.getBottom();
                v = findViewById(R.id.bottombar);
                if (v != null)
                    area.bottom = v.getTop();
                v = findViewById(R.id.rightbar);
                if (v != null)
                    area.right = v.getLeft();
                if (!area.isEmpty())
                    map.updateViewArea(area);
                if (vto.isAlive())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        vto.removeOnGlobalLayoutListener(this);
                    } else {
                        //noinspection deprecation
                        vto.removeGlobalOnLayoutListener(this);
                    }
                //listener.onGlobalLayout();
                /*{
                    vto.removeGlobalOnLayoutListener(this);
                }
                else
                {
                    final ViewTreeObserver vto1 = map.getViewTreeObserver();
                    vto1.removeGlobalOnLayoutListener(this);
                }*/
            }
        });
    }

    public void updateMap()
    {
        if (map != null)
            map.postInvalidate();
    }

    private final void updateMapButtons()
    {
        ViewGroup container = (ViewGroup) findViewById(R.id.button_container);

        for (String action : panelActions)
        {
            int id = getResources().getIdentifier(action, "id", getPackageName());
            ImageButton aib = (ImageButton) container.findViewById(id);
            if (aib != null)
            {
                if (activeActions.contains(action))
                {
                    aib.setVisibility(View.VISIBLE);
                    switch (id)
                    {
                        case R.id.follow://ContextCompat.getDrawable(getActivity(), R.drawable.name)||ResourcesCompat.getDrawable(getResources(), R.drawable.name, null);
                           // aib.setImageDrawable(getResources().getDrawable(map.isFollowing() ? R.drawable.cursor_drag_arrow : R.drawable.target));
                            aib.setImageDrawable(ResourcesCompat.getDrawable(getResources(),(map.isFollowing() ? R.drawable.cursor_drag_arrow : R.drawable.target),null));
                            break;
                        case R.id.locate:
                            boolean isLocating = locationService != null && locationService.isLocating();
                            aib.setImageDrawable(ResourcesCompat.getDrawable(getResources(),(isLocating ? R.drawable.pin_map_no : R.drawable.pin_map),null));
                            break;
                        case R.id.tracking:
                            boolean isTracking = locationService != null && locationService.isTracking();
                            aib.setImageDrawable(ResourcesCompat.getDrawable(getResources(),(isTracking ? R.drawable.doc_delete : R.drawable.doc_edit),null));
                            break;
                    }
                }
                else
                {
                    aib.setVisibility(View.GONE);
                }
            }
        }
    }

    protected final void updateCoordinates(final double latlon[])
    {
        // TODO strange situation, needs investigation
        if (application != null)
        {
            final String pos = StringFormatter.coordinates(application.coordinateFormat, " ", latlon[0], latlon[1]);
            this.runOnUiThread(new Runnable() {

                @Override
                public void run()
                {
                    coordinates.setText(pos);
                }
            });
        }
    }

    protected final void updateFileInfo()
    {
        final String title = application.getMapTitle();
        this.runOnUiThread(new Runnable() {

            @Override
            public void run()
            {
                if (title != null)
                {
                    currentFile.setText(title);
                }
                else
                {
                    currentFile.setText("-no map-");
                }

                updateZoomInfo();
            }
        });
    }

    protected final void updateZoomInfo()
    {
        double zoom = application.getZoom() * 100;

        if (zoom == 0.0)
        {
            mapZoom.setText("---%");
        }
        else
        {
            int rz = (int) Math.floor(zoom);
            String zoomStr = zoom - rz != 0.0 ? String.format("%.1f", zoom) : String.valueOf(rz);
            mapZoom.setText(zoomStr + "%");
        }

        ImageButton zoomin = (ImageButton) findViewById(R.id.zoomin);
        ImageButton zoomout = (ImageButton) findViewById(R.id.zoomout);
        zoomin.setEnabled(application.getNextZoom() != 0.0);
        zoomout.setEnabled(application.getPrevZoom() != 0.0);

        LightingColorFilter disable = new LightingColorFilter(0xFFFFFFFF, 0xFF444444);

        zoomin.setColorFilter(zoomin.isEnabled() ? null : disable);
        zoomout.setColorFilter(zoomout.isEnabled() ? null : disable);
    }

    protected void updateGPSStatus()
    {
        int v = map.isMoving() && application.editingRoute == null && application.editingTrack == null ? View.VISIBLE : View.GONE;
        View view = findViewById(R.id.movinginfo);
        if (view.getVisibility() != v)
        {
            view.setVisibility(v);
            updateMapViewArea();
        }
    }

    protected void updateNavigationStatus()//Changed  for Flaying usage-3D
    {
        boolean isNavigating = navigationService != null && navigationService.isNavigating();
        boolean isNavigatingViaRoute = isNavigating && navigationService.isNavigatingViaRoute();

        // waypoint panel
        findViewById(R.id.waypointinfo).setVisibility(isNavigating ? View.VISIBLE : View.GONE);
        // route panel
        findViewById(R.id.routeinfo).setVisibility(isNavigatingViaRoute ? View.VISIBLE : View.GONE);
        // distance
        //distanceValue.setVisibility(isNavigating ? View.VISIBLE : View.GONE);
        //findViewById(R.id.distancelt).setVisibility(isNavigating ? View.VISIBLE : View.GONE);

        // bearing
        //bearingValue.setVisibility(isNavigating ? View.VISIBLE : View.GONE);
        //findViewById(R.id.bearinglt).setVisibility(isNavigating ? View.VISIBLE : View.GONE);
        // turn
        turnValue.setVisibility(isNavigating ? View.VISIBLE : View.GONE);
        findViewById(R.id.turnlt).setVisibility(isNavigating ? View.VISIBLE : View.GONE);
        // xtk
        xtkValue.setVisibility(isNavigatingViaRoute ? View.VISIBLE : View.GONE);
        findViewById(R.id.xtklt).setVisibility(isNavigatingViaRoute ? View.VISIBLE : View.GONE);
        // abovebelowGS
        try {
            belowaboveValue.setVisibility(isNavigatingViaRoute ? View.VISIBLE : View.GONE);
            findViewById(R.id.abovebelowGSlt).setVisibility(isNavigatingViaRoute ? View.VISIBLE : View.GONE);
        }catch (Exception e){}
        // we hide elevation in Navigating mode and show Above/Below glide path


        if (isNavigatingViaRoute)
        {
            routeName.setText("› " + navigationService.navRoute.name);
        }
        if (isNavigating)
        {
            waypointName.setText("» " + navigationService.navWaypoint.name);
            if (application.navigationOverlay == null)
            {
                application.navigationOverlay = new NavigationOverlay(this);
                application.navigationOverlay.onMapChanged();
            }
        }
        else if (application.navigationOverlay != null)
        {
            application.navigationOverlay.onBeforeDestroy();
            application.navigationOverlay = null;
        }

        updateMapViewArea();
        map.update();
    }

    protected void updateNavigationInfo()
    {
        if (navigationService == null || !navigationService.isNavigating())
            return;

        double distance = navigationService.navDistance;
        double bearing = navigationService.navBearing;
        long turn = navigationService.navTurn;
        double vmg = navigationService.avvmg * speedFactor;
        int ete = navigationService.navETE;

        String[] dist = StringFormatter.distanceC(distance, precisionFormat);
        String extra = dist[0] + " "+ dist[1] + " | " + String.format(precisionFormat, vmg) + " " + speedAbbr + " | " + StringFormatter.timeHSec(ete);

        String trnsym = "";
        if (turn > 0)
        {
            trnsym = "R";
        }
        else if (turn < 0)
        {
            trnsym = "L";
            turn = -turn;
        }

    //	bearing = application.fixDeclination(bearing);	//distanceValue.setText(dist[0]);	//distanceUnit.setText(dist[1]);	//bearingValue.setText(String.valueOf(Math.round(bearing)));
        turnValue.setText(String.valueOf(Math.round(turn)) + trnsym);
        waypointExtra.setText(extra);

        if (navigationService.isNavigatingViaRoute())
        {
            boolean hasNext = navigationService.hasNextRouteWaypoint();
            if (distance < navigationService.navProximity * 3 && !animationSet)
            {
                AnimationSet animation = new AnimationSet(true);
                animation.addAnimation(new AlphaAnimation(1.0f, 0.3f));
                animation.addAnimation(new AlphaAnimation(0.3f, 1.0f));
                animation.setDuration(500);
                animation.setRepeatCount(10);
                findViewById(R.id.waypointinfo).startAnimation(animation);
                if (!hasNext)
                {
                    findViewById(R.id.routeinfo).startAnimation(animation);
                }
                animationSet = true;
            }
            else if (animationSet)
            {
                findViewById(R.id.waypointinfo).setAnimation(null);
                if (!hasNext)
                {
                    findViewById(R.id.routeinfo).setAnimation(null);
                }
                animationSet = false;
            }

            if (navigationService.navXTK == Double.NEGATIVE_INFINITY)
            {
                xtkValue.setText("--");
                xtkUnit.setText("--");
            }
            else
            {
                String xtksym = navigationService.navXTK == 0 ? "" : navigationService.navXTK > 0 ? "R" : "L";
                String[] xtks = StringFormatter.distanceC(Math.abs(navigationService.navXTK));
                xtkValue.setText(xtks[0] + xtksym);
                xtkUnit.setText(xtks[1]);
            }

            double navDistance = navigationService.navRouteDistanceLeft();
            int eta = navigationService.navRouteETE(navDistance);
            if (eta < Integer.MAX_VALUE)
                eta += navigationService.navETE;
            extra = StringFormatter.distanceH(navDistance + distance, 1000) + " | " + StringFormatter.timeHSec(eta);
            routeExtra.setText(extra);
        }
    }
//
    protected void updateMovingInfo(final Location location, final boolean geoid)
    {
        double eOrb = 0.0; //eOrb - elevationOrGladePath
        double needtobe = 0.0; //eOrb - elevationOrGladePath
        double s = location.getSpeed() * speedFactor;
        double track = application.fixDeclination(location.getBearing());
        speedValue.setText(String.format(precisionFormat, s));
        trackValue.setText(String.valueOf(Math.round(track)));
        boolean isNavigating = navigationService != null && navigationService.isNavigating();
        boolean isNavigatingViaRoute = isNavigating && navigationService.isNavigatingViaRoute();
        int colorGlidePath;
        if (isNavigatingViaRoute)
        {// Calculate AboveBelow Glide Path

            eOrb = Geo.belowAboveGlidePath(location.getLatitude(), location.getLongitude(), navigationService.navWaypoint.latitude, navigationService.navWaypoint.longitude, location.getAltitude(), navigationService.navWaypoint.altitude, navigationService.SlopeAngle);
            eOrb = (eOrb-zeroElevation)*elevationFactor;//понеже работим със относителна височина - трябва превишението по схемата да е спрямо летището
            belowaboveValue.setText(String.valueOf(Math.round(eOrb)));

            if (eOrb<-5) // Below Glide Path
            {colorGlidePath = ContextCompat.getColor(getApplicationContext(), R.color.belowGlidePath);//5m belowGlidePath - colored in red
                belowaboveName.setText("BELOW");
                belowaboveName.setTextColor(colorGlidePath);
                belowaboveValue.setTextColor(colorGlidePath);
                belowaboveUnit.setTextColor(colorGlidePath);
            }else if (eOrb>20){ // Above Glide Path
                colorGlidePath = ContextCompat.getColor(getApplicationContext(), R.color.aboveGlidePath);//5m belowGlidePath - colored in blue
                belowaboveName.setText("above");
                belowaboveName.setTextColor(colorGlidePath);
                belowaboveValue.setTextColor(colorGlidePath);
                belowaboveUnit.setTextColor(colorGlidePath);
            }else { // On Glide Path
                colorGlidePath = ContextCompat.getColor(getApplicationContext(), R.color.onGlidePath);//5m belowGlidePath - colored in white
                belowaboveName.setText("OnGlidePath");
                belowaboveName.setTextColor(colorGlidePath);
                belowaboveValue.setTextColor(colorGlidePath);
                belowaboveUnit.setTextColor(colorGlidePath);
            }

            //Log.i(TAG, "UpdateMouvingInfo: AboveBelowGlidePath= " + eOrb);
        } else {
            //colorGlidePath = ContextCompat.getColor(getApplicationContext(), R.color.aboveGlidePath);//5m belowGlidePath - colored in white
            }
        lastElevation =location.getAltitude();
        double elev = (lastElevation-zeroElevation) * elevationFactor;
        elevationValue.setText(String.valueOf(Math.round(elev)));
        // TODO set separate color
        if (geoid != lastGeoid)
        {
            int color = geoid ? 0xffffffff : ContextCompat.getColor(getApplicationContext(), R.color.gpsenabled);
            elevationValue.setTextColor(color);
            elevationUnit.setTextColor(color);
            ((TextView) findViewById(R.id.elevationname)).setTextColor(color);
            lastGeoid = geoid;
        }
    }

    private final void customizeLayout(final SharedPreferences settings)
    {
        boolean slVisible = settings.getBoolean(getString(R.string.pref_showsatinfo), true);
        boolean mlVisible = settings.getBoolean(getString(R.string.pref_showmapinfo), true);

        findViewById(R.id.satinfo).setVisibility(slVisible ? View.VISIBLE : View.GONE);
        findViewById(R.id.mapinfo).setVisibility(mlVisible ? View.VISIBLE : View.GONE);

        updateMapViewArea();
    }

    private final void updateOverlays(final SharedPreferences settings, final boolean justRemove)
    {
        boolean ctEnabled = false;
        boolean wptEnabled = false;
        boolean navEnabled = false;
        boolean distEnabled = false;
        boolean accEnabled = false;
        boolean moEnabled = false;
        boolean scaleEnabled = false;

        if (!justRemove)
        {
            ctEnabled = settings.getBoolean(getString(R.string.pref_showcurrenttrack), true);
            wptEnabled = settings.getBoolean(getString(R.string.pref_showwaypoints), true);
            distEnabled = showDistance > 0;
            accEnabled = showAccuracy;
            navEnabled = navigationService != null && navigationService.isNavigating();
            moEnabled = true;
            scaleEnabled = true;
        }
        if (ctEnabled && application.currentTrackOverlay == null)
        {
            application.currentTrackOverlay = new CurrentTrackOverlay(this);
        }
        else if (!ctEnabled && application.currentTrackOverlay != null)
        {
            application.currentTrackOverlay.onBeforeDestroy();
            application.currentTrackOverlay = null;
        }
        if (application.waypointsOverlay == null)
        {
            application.waypointsOverlay = new WaypointsOverlay(this);
            application.waypointsOverlay.setWaypoints(application.getWaypoints());
        }
        application.waypointsOverlay.setEnabled(wptEnabled);
        if (navEnabled && application.navigationOverlay == null)
        {
            application.navigationOverlay = new NavigationOverlay(this);
        }
        else if (!navEnabled && application.navigationOverlay != null)
        {
            application.navigationOverlay.onBeforeDestroy();
            application.navigationOverlay = null;
        }
        if (distEnabled && application.distanceOverlay == null)
        {
            application.distanceOverlay = new DistanceOverlay(this);
        }
        else if (!distEnabled && application.distanceOverlay != null)
        {
            application.distanceOverlay.onBeforeDestroy();
            application.distanceOverlay = null;
        }
        if (!moEnabled && application.mapObjectsOverlay != null)
        {
            application.mapObjectsOverlay.onBeforeDestroy();
            application.mapObjectsOverlay = null;
        }
        if (scaleEnabled && application.scaleOverlay == null)
        {
            application.scaleOverlay = new ScaleOverlay(this);
        }
        else if (!scaleEnabled && application.scaleOverlay != null)
        {
            application.scaleOverlay.onBeforeDestroy();
            application.scaleOverlay = null;
        }
        if (accEnabled && application.accuracyOverlay == null)
        {
            application.accuracyOverlay = new AccuracyOverlay(this);
            application.accuracyOverlay.setAccuracy(application.getLocationAsLocation().getAccuracy());
        }
        else if (!accEnabled && application.accuracyOverlay != null)
        {
            application.accuracyOverlay.onBeforeDestroy();
            application.accuracyOverlay = null;
        }

        if (justRemove)
        {
            for (TrackOverlay to : application.fileTrackOverlays)
            {
                to.onBeforeDestroy();
            }
            application.fileTrackOverlays.clear();
            for (RouteOverlay ro : application.routeOverlays)
            {
                ro.onBeforeDestroy();
            }
            application.routeOverlays.clear();
            for (AreaOverlay ao : application.areaOverlays)
            {
                ao.onBeforeDestroy();
            }
            application.areaOverlays.clear();
            if (application.waypointsOverlay != null)
            {
                application.waypointsOverlay.onBeforeDestroy();
            }
        }
        else
        {
            for (TrackOverlay to : application.fileTrackOverlays)
            {
                to.onPreferencesChanged(settings);
            }
            for (RouteOverlay ro : application.routeOverlays)
            {
                ro.onPreferencesChanged(settings);
            }
            for (AreaOverlay ao : application.areaOverlays)
            {
                ao.onPreferencesChanged(settings);
            }
            if (application.waypointsOverlay != null)
            {
                application.waypointsOverlay.onPreferencesChanged(settings);
            }
            if (application.navigationOverlay != null)
            {
                application.navigationOverlay.onPreferencesChanged(settings);
            }
            if (application.mapObjectsOverlay != null)
            {
                application.mapObjectsOverlay.onPreferencesChanged(settings);
            }
            if (application.distanceOverlay != null)
            {
                application.distanceOverlay.onPreferencesChanged(settings);
            }
            if (application.accuracyOverlay != null)
            {
                application.accuracyOverlay.onPreferencesChanged(settings);
            }
            if (application.scaleOverlay != null)
            {
                application.scaleOverlay.onPreferencesChanged(settings);
            }
            if (application.currentTrackOverlay != null)
            {
                application.currentTrackOverlay.onPreferencesChanged(settings);
            }
        }
    }

    private void startEditTrack(Track track)
    {
        setFollowing(false);
        application.editingTrack = track;
        application.editingTrack.editing = true;
        int n = application.editingTrack.getPoints().size() - 1;
        int p = application.editingTrack.editingPos >= 0 ? application.editingTrack.editingPos : n;
        application.editingTrack.editingPos = p;
        trackBar.setMax(n);
        trackBar.setProgress(0);
        trackBar.setProgress(p);
        trackBar.setKeyProgressIncrement(1);
        onProgressChanged(trackBar, p, false);
        findViewById(R.id.edittrack).setVisibility(View.VISIBLE);
        findViewById(R.id.trackdetails).setVisibility(View.VISIBLE);
        updateGPSStatus();
        if (showDistance > 0)
            application.distanceOverlay.setEnabled(false);
        map.setFocusable(false);
        map.setFocusableInTouchMode(false);
        trackBar.requestFocus();
        updateMapViewArea();
    }

    private void startEditRoute(Route route)
    {
        setFollowing(false);
        application.editingRoute = route;
        application.editingRoute.editing = true;

        boolean newroute = true;
        for (Iterator<RouteOverlay> iter = application.routeOverlays.iterator(); iter.hasNext();)
        {
            RouteOverlay ro = iter.next();
            if (ro.getRoute().editing)
            {
                ro.onRoutePropertiesChanged();
                newroute = false;
            }
        }
        if (newroute)
        {
            RouteOverlay newRoute = new RouteOverlay(this, application.editingRoute);
            application.routeOverlays.add(newRoute);
        }
        findViewById(R.id.editroute).setVisibility(View.VISIBLE);
        //Log.d(TAG, "startEditRoute");
        updateGPSStatus();
        application.routeEditingWaypoints = new Stack<Waypoint>();
        if (showDistance > 0)
            application.distanceOverlay.setEnabled(false);
        updateMapViewArea();
    }

     private void startEditArea(Area area)
    {
        setFollowing(false);
        application.editingArea = area;
        application.editingArea.editing = true;

        boolean newarea = true;
        for (Iterator<AreaOverlay> iter = application.areaOverlays.iterator(); iter.hasNext();)
        {
            AreaOverlay аo = iter.next();
            if (аo.getArea().editing)
            {
                аo.onAreaPropertiesChanged();
                newarea = false;
            }
        }
        if (newarea)
        {
            AreaOverlay newArea = new AreaOverlay(this, application.editingArea);
            application.areaOverlays.add(newArea);
        }
        findViewById(R.id.editroute).setVisibility(View.VISIBLE);//използвам същият панел с бутони за едитване на маршрут
        //Log.d(TAG, "startEditArea");
        updateGPSStatus();
        application.areaEditingWaypoints = new Stack<Waypoint>();
        if (showDistance > 0)
            application.distanceOverlay.setEnabled(false);
        updateMapViewArea();
    }


    public void setFollowing(boolean follow)
    {
        if (application.editingRoute == null && application.editingTrack == null)
        {
            if (showDistance > 0 && application.distanceOverlay != null)
            {
                if (showDistance == 2 && !follow)
                {
                    application.distanceOverlay.setAncor(application.getLocation());
                    application.distanceOverlay.setEnabled(true);
                }
                else
                {
                    application.distanceOverlay.setEnabled(false);
                }
            }
            map.setFollowing(follow);
        }
    }

    public void zoomMap(final float factor)
    {
        waitBar.setVisibility(View.VISIBLE);
        waitBar.setText(R.string.msg_wait);
        executorThread.execute(new Runnable() {
            public void run()
            {
                synchronized (map)
                {
                    if (application.zoomBy(factor))
                    {
                        map.updateMapInfo();
                        map.update();
                    }
                }
                finishHandler.sendEmptyMessage(0);
            }
        });
    }

    protected void dimScreen(Location location)
    {
        int color = Color.TRANSPARENT;
        Calendar now = GregorianCalendar.getInstance(TimeZone.getDefault());
        if (autoDim && !Astro.isDaytime(application.getZenith(), location, now))
            color = dimValue << 57; // value * 2 and shifted to transparency octet
        dimView.setBackgroundColor(color);
    }

    public boolean waypointTapped(Waypoint waypoint, int x, int y)
    {
        try
        {
            if (application.editingRoute != null)
            {
                routeSelected = -1;
                waypointSelected = application.getWaypointIndex(waypoint);
                wptQuickAction.show(map, x, y);
                return true;
            }
            else
            {
                Location loc = application.getLocationAsLocation();
                FragmentManager fm = getSupportFragmentManager();
                WaypointInfo waypointInfo = (WaypointInfo) fm.findFragmentByTag("waypoint_info");
                if (waypointInfo == null)
                    waypointInfo = new WaypointInfo();
                waypointInfo.setWaypoint(waypoint);
                Bundle args = new Bundle();
                args.putDouble("lat", loc.getLatitude());
                args.putDouble("lon", loc.getLongitude());
                waypointInfo.setArguments(args);
                waypointInfo.show(fm, "waypoint_info");
                return true;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Performs action on a tapped route waypoint.
     *
     * @param route
     *            route index
     * @param index
     *            waypoint index inside route
     * @param x
     *            view X coordinate
     * @param y
     *            view Y coordinate
     * @return true if any action was performed
     */
    public boolean routeWaypointTapped(int route, int index, int x, int y)
    {
        if (application.editingRoute != null && application.editingRoute == application.getRoute(route))
        {
            startActivityForResult(new Intent(this, WaypointProperties.class).putExtra("INDEX", index).putExtra("ROUTE", route + 1), RESULT_EDIT_ROUTE);
            Log.e(TAG, "startActivityForResult_WaypointProperties:" + "putExtra_INDEX" + index);
            return true;
        }
        else if (navigationService != null && navigationService.navRoute == application.getRoute(route))
        {
            routeSelected = route;
            waypointSelected = index;
            rteQuickAction.show(map, x, y);
            Log.e(TAG, "rteQuickAction");
            return true;
        }
        else
        {
            startActivity(new Intent(this, RouteDetails.class).putExtra("index", route));
           //Log.e(TAG, "startActivity:" + "RouteDetails_putExtra_INDEX" + route);
            return true;
        }
    }
    /*
     * Performs action on a tapped area waypoint.
     *
     * @param area
     *            area index
     * @param index
     *            waypoint index inside area
     * @param x
     *            view X coordinate
     * @param y
     *            view Y coordinate
     * @return true if any action was performed
     */
    public boolean areaWaypointTapped(int area, int index, int x, int y)
    {
        if (application.editingArea != null && application.editingArea == application.getArea(area))
        {
            startActivityForResult(new Intent(this, WaypointProperties.class).putExtra("INDEX", index).putExtra("AREA", area + 1), RESULT_EDIT_AREA);
            return true;
        }
        else if (navigationService != null && navigationService.navArea == application.getArea(area))
        {
            areaSelected = area;
            waypointSelected = index;
            rteQuickAction.show(map, x, y);
            return true;
        }
        else
        {
            startActivity(new Intent(this, AreaDetails.class).putExtra("INDEX", area));
            return true;
        }
    }

    public boolean mapObjectTapped(long id, int x, int y)
    {
        mapObjectSelected = id;
        mobQuickAction.show(map, x, y);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // add plugins
        SubMenu views = menu.findItem(R.id.menuView).getSubMenu();
        Map<String, Pair<Drawable, Intent>> plugins = application.getPluginsViews();
        for (String plugin : plugins.keySet())
        {
            MenuItem item = views.add(plugin);
            item.setIntent(plugins.get(plugin).second);
            if (plugins.get(plugin).first != null)
                item.setIcon(plugins.get(plugin).first);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu)
    {
        if (application.editingRoute != null || application.editingTrack != null)
            return false;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        boolean wpt = application.hasWaypoints();
        boolean rts = application.hasRoutes();
        boolean ars = application.hasAreas();
        boolean nvw = navigationService != null && navigationService.isNavigating();
        boolean nvr = navigationService != null && navigationService.isNavigatingViaRoute();
        boolean nva = navigationService != null && navigationService.isNavigatingViaArea();
        boolean cbm = ((clipboard.hasPrimaryClip()) && (clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)));
//следва код който дефинира поведението на трите точки(меню)
        menu.findItem(R.id.menuManageWaypoints).setEnabled(wpt);
        menu.findItem(R.id.menuExportCurrentTrack).setEnabled(application.currentTrackOverlay != null);
        menu.findItem(R.id.menuClearCurrentTrack).setEnabled(application.currentTrackOverlay != null);
        //menu.findItem(R.id.menuManageAreas).setVisible(!nva);
        menu.findItem(R.id.menuManageRoutes).setVisible(!nvr);//при стартиране на навигация по маршриут изчезва полето
        menu.findItem(R.id.menuStartNavigation).setVisible(!nvr);//полето се премахва от възможния избор за менюто и се замества със две полета меню за следваща и предишна точка по маршрут при активиран маршрут
        menu.findItem(R.id.menuStartNavigation).setEnabled(rts);//ако има налични маршрути полето е активно
        menu.findItem(R.id.menuStartArea).setEnabled(ars);//ако има налични зони полето става активоно - за сега няма вързана активност
        menu.findItem(R.id.menuNavigationDetails).setVisible(nvr);//полето се показва ако има стартирана навигация по маршриут
        menu.findItem(R.id.menuAreaDetails).setVisible(nva);//полето се показва само ако има стартирано авигация към зона
        menu.findItem(R.id.menuNextNavPoint).setVisible(nvr);//полето се показва ако има стартирана навигация по маршриут
        menu.findItem(R.id.menuPrevNavPoint).setVisible(nvr);//полето се показва ако има стартирана навигация по маршриут
        menu.findItem(R.id.menuNextNavPoint).setEnabled(navigationService != null && navigationService.hasNextRouteWaypoint());//полето е активно при наличие на следваща точка по маршрута
        menu.findItem(R.id.menuPrevNavPoint).setEnabled(navigationService != null && navigationService.hasPrevRouteWaypoint());//полето е активно при наличие на предишна точка по маршрута
        menu.findItem(R.id.menuStopNavigation).setEnabled(nvw);//полето се показва ако има стартирана навигация към точка
        menu.findItem(R.id.menuStopNavigationArea).setEnabled(nva);// полето става неактивно
        menu.findItem(R.id.menuSetAnchor).setVisible(showDistance > 0 && !map.isFollowing());
        menu.findItem(R.id.menuPasteLocation).setEnabled(cbm);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {//при натискане на някой от бутоните на меню (трите точки) лентата
        switch (item.getItemId())
        {
            case R.id.menuSearch:
                onSearchRequested();
                return true;
            case R.id.menuAddWaypoint:
            {
                double[] loc = application.getMapCenter();
                Waypoint waypoint = new Waypoint("", "", loc[0], loc[1]);//TODO ask user for wpt altitude, or find altitude on surface
                waypoint.date = Calendar.getInstance().getTime();
                int wpt = application.addWaypoint(waypoint);
                waypoint.name = "WPT" + wpt;
                application.saveDefaultWaypoints();
                map.update();
                return true;
            }
            case R.id.menuNewWaypoint:
                startActivityForResult(new Intent(this, WaypointProperties.class).putExtra("INDEX", -1), RESULT_SAVE_WAYPOINT);
                return true;
            case R.id.menuProjectWaypoint:
                startActivityForResult(new Intent(this, WaypointProject.class), RESULT_SAVE_WAYPOINT);
                return true;
            case R.id.menuManageWaypoints:
                startActivityForResult(new Intent(this, WaypointListActivity.class), RESULT_MANAGE_WAYPOINTS);
                return true;
            case R.id.menuLoadWaypoints:
                startActivityForResult(new Intent(this, WaypointFileList.class), RESULT_LOAD_WAYPOINTS);
                return true;
            case R.id.menuManageTracks:
                startActivityForResult(new Intent(this, TrackListActivity.class), RESULT_MANAGE_TRACKS);
                return true;
            case R.id.menuExportCurrentTrack:
                FragmentManager fm = getSupportFragmentManager();
                TrackExportDialog trackExportDialog = new TrackExportDialog(locationService);
                trackExportDialog.show(fm, "track_export");
                return true;
            case R.id.menuExpandCurrentTrack:
                new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.warning).setMessage(R.string.msg_expandcurrenttrack).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (application.currentTrackOverlay != null)
                        {
                            Track track = locationService.getTrack();
                            track.show = true;
                            application.currentTrackOverlay.setTrack(track);
                        }
                    }
                }).setNegativeButton(R.string.no, null).show();
                return true;
            case R.id.menuClearCurrentTrack:
                new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.warning).setMessage(R.string.msg_clearcurrenttrack).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (application.currentTrackOverlay != null)
                            application.currentTrackOverlay.clear();
                        locationService.clearTrack();
                    }
                }).setNegativeButton(R.string.no, null).show();
                return true;
            case R.id.menuManageRoutes:
                //Log.e(TAG, "on_menuManageRoutes");
                startActivityForResult(new Intent(this, RouteListActivity.class).putExtra("MODE", RouteList.MODE_MANAGE), RESULT_MANAGE_ROUTES);
                return true;
            case R.id.menuManageAreas://todo - да се уточни и направи да действа
                //Log.e(TAG, "on_menuManageAreas");
                startActivityForResult(new Intent(this, AreaListActivity.class).putExtra("MODE", AreaList.MODE_MANAGE), RESULT_MANAGE_AREAS);
                return true;
            case R.id.menuStartArea://todo - да се уточни и направи да действа
                //Log.e(TAG, "on_menuManageAreas");
                startActivity(new Intent(this, AreaListActivity.class).putExtra("MODE", AreaList.MODE_START));
                return true;
            case R.id.menuStartNavigation:
                if (application.getRoutes().size() > 1)
                {
                    startActivity(new Intent(this, RouteListActivity.class).putExtra("MODE", RouteList.MODE_START));
                }
                else
                {
                    startActivity(new Intent(this, RouteStart.class).putExtra("INDEX", 0));
                }
                return true;
            case R.id.menuNavigationDetails:
                startActivity(new Intent(this, RouteDetails.class).putExtra("index", application.getRouteIndex(navigationService.navRoute)).putExtra("nav", true));
                return true;
            case R.id.menuNextNavPoint:
                navigationService.nextRouteWaypoint();
                return true;
            case R.id.menuPrevNavPoint:
                navigationService.prevRouteWaypoint();
                return true;
            case R.id.menuStopNavigation:
            {
                navigationService.stopNavigation();
                return true;
            }
            case R.id.menuHSI:
                startActivity(new Intent(this, HSIActivity.class));
                return true;
            case R.id.menuInformation:
                startActivity(new Intent(this, Information.class));
                return true;
            case R.id.menuMapInfo:
                startActivity(new Intent(this, MapInformation.class));
                return true;
            case R.id.menuCursorMaps:
                startActivityForResult(new Intent(this, MapList.class).putExtra("pos", true), RESULT_LOAD_MAP_ATPOSITION);
                return true;
            case R.id.menuAllMaps:
                startActivityForResult(new Intent(this, MapList.class), RESULT_LOAD_MAP);
                return true;
            case R.id.menuShare:
            {
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, R.string.currentloc);
                double[] sloc = application.getMapCenter();
                String spos = StringFormatter.coordinates(application.coordinateFormat, " ", sloc[0], sloc[1]);
                i.putExtra(Intent.EXTRA_TEXT, spos);
                startActivity(Intent.createChooser(i, getString(R.string.menu_share)));
                return true;
            }
            /*case R.id.menuSharnigLocation://опит да вкарам SharingLocation тук - но неуспешно за сега
            {
                Intent Sl = new Intent(this, SharingLocation.class);
                startActivity(Sl);
            }*/
            case R.id.menuViewElsewhere:
            {
                double[] sloc = application.getMapCenter();
                String geoUri = "geo:" + Double.toString(sloc[0]) + "," + Double.toString(sloc[1]);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(geoUri));
                startActivity(intent);
                return true;
            }
            case R.id.menuCopyLocation:
            {// Gets a handle to the clipboard service.
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                double[] cloc = application.getMapCenter();
                String cpos = StringFormatter.coordinates(application.coordinateFormat, " ", cloc[0], cloc[1]);
                // Creates a new text clip to put on the clipboard
                ClipData clip = ClipData.newPlainText("simple text",cpos);
                clipboard.setPrimaryClip(clip);
               // mPasteItem.setEnabled(true);
                return true;
            }
            case R.id.menuPasteLocation:
            {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                /* If the clipboard doesn't contain data, disable the paste menu item.
                // If it does contain data, decide if you can handle the data.
                if (!(clipboard.hasPrimaryClip())) {
                   //Клипборда е празен
                } else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                    // This disables the paste menu item, since the clipboard has data but it is not plain text
                   // mPasteItem.setEnabled(false);
                    Toast.makeText(getApplicationContext(), R.string.wrongClipboard, Toast.LENGTH_LONG).show();
                } else
                    { // This enables the paste menu item, since the clipboard contains plain text.*/
                    ClipData.Item itemClip = clipboard.getPrimaryClip().getItemAt(0);
                    String q = itemClip.getText().toString();
                        try
                        {
                            double c[] = CoordinateParser.parse(q);
                            if (!Double.isNaN(c[0]) && !Double.isNaN(c[1]))
                            {
                                boolean mapChanged = application.setMapCenter(c[0], c[1], true, false);
                                if (mapChanged)
                                    map.updateMapInfo();
                                map.update();
                                map.setFollowing(false);
                            }
                        }
                        catch (IllegalArgumentException e)
                        {
                        };
                return true;
            }
            case R.id.menuSetAnchor:
                if (showDistance > 0)
                {
                    application.distanceOverlay.setAncor(application.getMapCenter());
                    application.distanceOverlay.setEnabled(true);
                }
                return true;
            case R.id.menuPreferences:
                {
                startActivity(new Intent(this, PreferencesHC.class));
                return true;
                }
        }
        return false;
    }

    private OnActionItemClickListener waypointActionItemClickListener = new OnActionItemClickListener() {
        @Override
        public void onItemClick(QuickAction3D source, int pos, int actionId)
        {
            Waypoint wpt = application.getWaypoint(waypointSelected);

            switch (actionId)
            {
                case qaAddWaypointToRoute:
                    application.routeEditingWaypoints.push(application.editingRoute.addWaypoint(wpt.name, wpt.latitude, wpt.longitude, wpt.altitude));
                    map.invalidate();
                    break;
            }
            waypointSelected = -1;
        }
    };

    private OnActionItemClickListener routeActionItemClickListener = new OnActionItemClickListener() {
        @Override
        public void onItemClick(QuickAction3D source, int pos, int actionId)
        {
            switch (actionId)
            {
                case qaNavigateToWaypoint:
                    navigationService.setRouteWaypoint(waypointSelected);
                    break;
            }
            waypointSelected = -1;
        }
    };

    private OnActionItemClickListener mapObjectActionItemClickListener = new OnActionItemClickListener() {
        @Override
        public void onItemClick(QuickAction3D source, int pos, int actionId)
        {
            switch (actionId)
            {
                case qaNavigateToMapObject:
                    navigationService.navigateTo(application.getMapObject(mapObjectSelected));
                    break;
            }
            mapObjectSelected = -1;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case RESULT_MANAGE_WAYPOINTS:
            {
                application.waypointsOverlay.clearBitmapCache();
                application.saveWaypoints();
                break;
            }
            case RESULT_LOAD_WAYPOINTS:
            {
                if (resultCode == RESULT_OK)
                {
                    Bundle extras = data.getExtras();
                    int count = extras.getInt("count");
                    if (count > 0)
                    {
                        application.waypointsOverlay.clearBitmapCache();
                    }
                }
                break;
            }
            case RESULT_SAVE_WAYPOINT:
            {
                if (resultCode == RESULT_OK)
                {
                    application.waypointsOverlay.clearBitmapCache();
                    application.saveWaypoints();
                    if (data != null && data.hasExtra("index")
                            && PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_waypoint_visible), getResources().getBoolean(R.bool.def_waypoint_visible)))
                        application.ensureVisible(application.getWaypoint(data.getIntExtra("index", -1)));
                }
                break;
            }
            case RESULT_SAVE_WAYPOINTS:
                if (resultCode == RESULT_OK)
                {
                    application.saveDefaultWaypoints();
                }
                break;
            case RESULT_MANAGE_TRACKS:
                for (Iterator<TrackOverlay> iter = application.fileTrackOverlays.iterator(); iter.hasNext();)
                {
                    TrackOverlay to = iter.next();
                    to.onTrackPropertiesChanged();
                    if (to.getTrack().removed)
                    {
                        to.onBeforeDestroy();
                        iter.remove();
                    }
                }
                if (resultCode == RESULT_OK)
                {
                    Bundle extras = data.getExtras();
                    int index = extras.getInt("index");
                    startEditTrack(application.getTrack(index));
                }
                break;
            case RESULT_MANAGE_ROUTES:
            {
                for (Iterator<RouteOverlay> iter = application.routeOverlays.iterator(); iter.hasNext();)
                {
                    RouteOverlay ro = iter.next();
                    ro.onRoutePropertiesChanged();
                    if (ro.getRoute().removed)
                    {
                        ro.onBeforeDestroy();
                        iter.remove();
                    }
                }
                if (resultCode == RESULT_OK)
                {
                    Bundle extras = data.getExtras();
                    int index = extras.getInt("index");
                    int dir = extras.getInt("dir");
                    if (dir != 0)
                        startService(new Intent(this, NavigationService.class).setAction(NavigationService.NAVIGATE_ROUTE).putExtra(NavigationService.EXTRA_ROUTE_INDEX, index).putExtra(NavigationService.EXTRA_ROUTE_DIRECTION, dir));
                    else
                        startEditRoute(application.getRoute(index));
                }
                break;
            }
            case RESULT_MANAGE_AREAS:
            {
                for (Iterator<AreaOverlay> iter = application.areaOverlays.iterator(); iter.hasNext();)
                {
                    AreaOverlay ro = iter.next();
                    ro.onAreaPropertiesChanged();
                    if (ro.getArea().removed)
                    {
                        ro.onBeforeDestroy();
                        iter.remove();
                    }
                }
                if (resultCode == RESULT_OK)
                {
                    Bundle extras = data.getExtras();
                    int index = extras.getInt("index");
                    int dir = extras.getInt("dir");
                    if (dir != 0)
                        startService(new Intent(this, NavigationService.class).setAction(NavigationService.NAVIGATE_AREA).putExtra(NavigationService.EXTRA_AREA_INDEX, index));
                    else
                        startEditArea(application.getArea(index));
                }
                break;
            }
            case RESULT_EDIT_ROUTE:
                for (Iterator<RouteOverlay> iter = application.routeOverlays.iterator(); iter.hasNext();)
                {
                    RouteOverlay ro = iter.next();
                    if (ro.getRoute().editing)
                        ro.onRoutePropertiesChanged();
                }
                break;
            case RESULT_EDIT_AREA:
                for (Iterator<AreaOverlay> iter = application.areaOverlays.iterator(); iter.hasNext();)
                {
                    AreaOverlay ao = iter.next();
                    if (ao.getArea().editing)
                        ao.onAreaPropertiesChanged();
                }
                break;
            case RESULT_LOAD_MAP:
                if (resultCode == RESULT_OK)
                {
                    Bundle extras = data.getExtras();
                    final int id = extras.getInt("id");
                    synchronized (map)
                    {
                        application.loadMap(id);
                        map.suspendBestMap();
                        setFollowing(false);
                        map.updateMapInfo();
                        map.update();
                    }
                }
                break;
            case RESULT_LOAD_MAP_ATPOSITION:
                if (resultCode == RESULT_OK)
                {
                    Bundle extras = data.getExtras();
                    final int id = extras.getInt("id");
                    if (application.selectMap(id))
                    {
                        map.suspendBestMap();
                        map.updateMapInfo();
                        map.update();
                    }
                    else
                    {
                        map.update();
                    }
                }
                break;
        }
    }

    final Handler backHandler = new Handler();

    @Override
    public void onBackPressed()
    {
        switch (exitConfirmation)
        {
            case 0:
                // wait for second back
                if (secondBack)
                {
                    backToast.cancel();
                    MapActivity.this.finish();
                }
                else
                {
                    secondBack = true;
                    backToast.show();
                    backHandler.postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            secondBack = false;
                        }
                    }, 2000);
                }
                return;
            case 1:
                // Ask the user if they want to quit
                new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.quitQuestion).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // TODO change context everywhere?
                        stopService(new Intent(MapActivity.this, NavigationService.class));
                        MapActivity.this.finish();
                    }
                }).setNegativeButton(R.string.no, null).show();
                return;
            default:
                super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.zoomin:
                if (application.getNextZoom() == 0.0)
                    break;
                waitBar.setVisibility(View.VISIBLE);
                waitBar.setText(R.string.msg_wait);
                executorThread.execute(new Runnable() {
                    public void run()
                    {
                        synchronized (map)
                        {
                            if (application.zoomIn())
                            {
                                map.updateMapInfo();
                                map.update();
                            }
                        }
                        finishHandler.sendEmptyMessage(0);
                    }
                });
                break;
            case R.id.zoomout:
                if (application.getPrevZoom() == 0.0)
                    break;
                waitBar.setVisibility(View.VISIBLE);
                waitBar.setText(R.string.msg_wait);
                executorThread.execute(new Runnable() {
                    public void run()
                    {
                        synchronized (map)
                        {
                            if (application.zoomOut())
                            {
                                map.updateMapInfo();
                                map.update();
                            }
                        }
                        finishHandler.sendEmptyMessage(0);
                    }
                });
                break;
            case R.id.nextmap:
                waitBar.setVisibility(View.VISIBLE);
                waitBar.setText(R.string.msg_wait);
                executorThread.execute(new Runnable() {
                    public void run()
                    {
                        synchronized (map)
                        {
                            if (application.prevMap())
                            {
                                map.suspendBestMap();
                                map.updateMapInfo();
                                map.update();
                            }
                        }
                        finishHandler.sendEmptyMessage(0);
                    }
                });
                break;
            case R.id.prevmap:
                waitBar.setVisibility(View.VISIBLE);
                waitBar.setText(R.string.msg_wait);
                executorThread.execute(new Runnable() {
                    public void run()
                    {
                        synchronized (map)
                        {
                            if (application.nextMap())
                            {
                                map.suspendBestMap();
                                map.updateMapInfo();
                                map.update();
                            }
                        }
                        finishHandler.sendEmptyMessage(0);
                    }
                });
                break;
            case R.id.maps:
                startActivityForResult(new Intent(this, MapList.class).putExtra("pos", true), RESULT_LOAD_MAP_ATPOSITION);
                break;
            case R.id.waypoints:
                startActivityForResult(new Intent(this, WaypointListActivity.class), RESULT_MANAGE_WAYPOINTS);
                break;
            case R.id.info:
                //Log.d(TAG, "onStartINFO");
                startActivity(new Intent(this, Information.class));
                break;
            case R.id.emer_button:
                Button be = (Button) findViewById(R.id.emer_button);
                //Log.d(TAG, "onEmerButtonPress");
                Intent IntentBtnEmer = new Intent(this, BtnsProceduresSet.class);
                IntentBtnEmer.putExtra(BTN_TITLE, be.getText());// Send btn Emer
                startActivity(IntentBtnEmer);
                break;
            case R.id.norm_button:
                Button bn = (Button) findViewById(R.id.norm_button);
                //Log.d(TAG, "onNormButtonPress");
                Intent IntentBtnNorm = new Intent(this, BtnsProceduresSet.class);
                IntentBtnNorm.putExtra(BTN_TITLE, bn.getText());// Send btn Norm
                startActivity(IntentBtnNorm);
                break;
            case R.id.zero_button:
                zeroElevation = lastElevation;
                application.setZeroLevelDouble(lastElevation);
                //application.bearingSet+=5;
                //Log.d(TAG, "Bearing="+application.bearingSet);
                break;
            case R.id.clear_button:
                zeroElevation = 0.0;
                //application.bearingSet-=5;
                //Log.d(TAG, "Bearing="+application.bearingSet);
                application.setZeroLevelDouble(0.0);
                break;
            case R.id.follow:
                setFollowing(!map.isFollowing());
                break;
            case R.id.locate:
            {
                boolean isLocating = locationService != null && locationService.isLocating();
                application.enableLocating(!isLocating);
                Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putBoolean(getString(R.string.lc_locate), !isLocating);
                editor.apply();
                break;
            }
            case R.id.tracking:
            {
                boolean isTracking = locationService != null && locationService.isTracking();
                application.enableTracking(!isTracking);
                Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putBoolean(getString(R.string.lc_track), !isTracking);
                editor.apply();
                break;
            }
            case R.id.expand:
                ImageButton expand = (ImageButton) findViewById(R.id.expand);
                if (isFullscreen)
                {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    expand.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.expand, null));
                }
                else
                {//ContextCompat.getDrawable(getActivity(), R.drawable.name)||ResourcesCompat.getDrawable(getResources(), R.drawable.name, null);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    expand.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.collapse, null));
                }
                isFullscreen = !isFullscreen;
                break;
            case R.id.cutbefore:
                application.editingTrack.cutBefore(trackBar.getProgress());
                int nb = application.editingTrack.getPoints().size() - 1;
                trackBar.setMax(nb);
                trackBar.setProgress(0);
                break;
            case R.id.cutafter:
                application.editingTrack.cutAfter(trackBar.getProgress());
                int na = application.editingTrack.getPoints().size() - 1;
                trackBar.setMax(na);
                trackBar.setProgress(0);
                trackBar.setProgress(na);
                break;
            case R.id.addpoint: //От бутоните за редакция на маршрут - добавя следваща точка към маршрута
                if (application.editingArea != null)
                {
                    double[] aloc = application.getMapCenter();
                    application.areaEditingWaypoints.push(application.editingArea.addWaypoint("AWPT" + application.editingArea.length(), aloc[0], aloc[1]));
                }else {
                    double[] aloc = application.getMapCenter();
                    application.routeEditingWaypoints.push(application.editingRoute.addWaypoint("RWPT" + application.editingRoute.length(), aloc[0], aloc[1]));
                }
                break;
            case R.id.insertpoint: // добавя междинна точка към маршрута/зона
                if (application.editingArea != null)
                {
                    double[] iloc = application.getMapCenter();
                    application.areaEditingWaypoints.push(application.editingArea.insertWaypoint("AWPT" + application.editingArea.length(), iloc[0], iloc[1]));
                }else {
                    double[] iloc = application.getMapCenter();
                    application.routeEditingWaypoints.push(application.editingRoute.insertWaypoint("RWPT" + application.editingRoute.length(), iloc[0], iloc[1]));
                }
                break;
            case R.id.removepoint: // премахва точка от маршрута/зона
                if (application.editingArea != null)
                {
                    if (!application.areaEditingWaypoints.empty()) {
                        application.editingArea.removeWaypoint(application.areaEditingWaypoints.pop());
                    }
                }else {
                    if (!application.routeEditingWaypoints.empty()) {
                        application.editingRoute.removeWaypoint(application.routeEditingWaypoints.pop());
                    }
                }
                break;
            case R.id.orderpoints://показва детайли за реда на точките по маршрута/зона
                if (application.editingArea != null)
                {
                    startActivityForResult(new Intent(this, AreaEdit.class).putExtra("INDEX", application.getAreaIndex(application.editingArea)), RESULT_EDIT_AREA);
                }else {
                    startActivityForResult(new Intent(this, RouteEdit.class).putExtra("INDEX", application.getRouteIndex(application.editingRoute)), RESULT_EDIT_ROUTE);
                }
                break;
            case R.id.finishedit://завършва редакция на маршрут/зона
                if (application.editingArea != null)
                {
                    if ("New area".equals(application.editingArea.name)) {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
                        application.editingArea.name = formatter.format(new Date());
                    }
                    application.editingArea.editing = false;
                    for (Iterator<AreaOverlay> iter = application.areaOverlays.iterator(); iter.hasNext(); ) {
                        AreaOverlay ro = iter.next();
                        ro.onAreaPropertiesChanged();
                    }
                    application.editingArea = null;
                    application.areaEditingWaypoints = null;
                    findViewById(R.id.editroute).setVisibility(View.GONE);//лентата с която се редактира маршрута/зоната изчезва - използва същата лената и а маршрута
                    updateGPSStatus();
                    if (showDistance == 2) {
                        application.distanceOverlay.setEnabled(true);
                    }
                    updateMapViewArea();
                    map.requestFocus();
                }else {
                    if ("New route".equals(application.editingRoute.name)) {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
                        application.editingRoute.name = formatter.format(new Date());
                    }
                    application.editingRoute.editing = false;
                    for (Iterator<RouteOverlay> iter = application.routeOverlays.iterator(); iter.hasNext(); ) {
                        RouteOverlay ro = iter.next();
                        ro.onRoutePropertiesChanged();
                    }
                    application.editingRoute = null;
                    application.routeEditingWaypoints = null;
                    findViewById(R.id.editroute).setVisibility(View.GONE);//лентата с която се редактира маршрута изчезва
                    updateGPSStatus();
                    if (showDistance == 2) {
                        application.distanceOverlay.setEnabled(true);
                    }
                    updateMapViewArea();
                    map.requestFocus();
                }
                break;
            case R.id.finishtrackedit:
                application.editingTrack.editing = false;
                application.editingTrack.editingPos = -1;
                application.editingTrack = null;
                findViewById(R.id.edittrack).setVisibility(View.GONE);
                findViewById(R.id.trackdetails).setVisibility(View.GONE);
                updateGPSStatus();
                if (showDistance == 2)
                {
                    application.distanceOverlay.setEnabled(true);
                }
                map.setFocusable(true);
                map.setFocusableInTouchMode(true);
                map.requestFocus();
                break;
        }
    }

    @Override
    public void onWaypointView(Waypoint waypoint)
    {
        application.ensureVisible(waypoint);
    }

    @Override
    public void onWaypointNavigate(final Waypoint waypoint)
    {
        Intent intent = new Intent(getApplicationContext(), NavigationService.class).setAction(NavigationService.NAVIGATE_MAPOBJECT);
        intent.putExtra(NavigationService.EXTRA_NAME, waypoint.name);
        intent.putExtra(NavigationService.EXTRA_LATITUDE, waypoint.latitude);
        intent.putExtra(NavigationService.EXTRA_LONGITUDE, waypoint.longitude);
        intent.putExtra(NavigationService.EXTRA_PROXIMITY, waypoint.proximity);
        startService(intent);
    }

    @Override
    public void onWaypointEdit(final Waypoint waypoint)
    {
        int index = application.getWaypointIndex(waypoint);
        startActivityForResult(new Intent(this, WaypointProperties.class).putExtra("INDEX", index), RESULT_SAVE_WAYPOINT);
    }

    @Override
    public void onWaypointShare(final Waypoint waypoint)
    {
        Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, R.string.currentloc);
        String coords = StringFormatter.coordinates(application.coordinateFormat, " ", waypoint.latitude, waypoint.longitude);
        i.putExtra(Intent.EXTRA_TEXT, waypoint.name + " @ " + coords);
        startActivity(Intent.createChooser(i, getString(R.string.menu_share)));
    }

    @Override
    public void onWaypointRemove(final Waypoint waypoint)
    {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.removeWaypointQuestion)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                WaypointSet wptset = waypoint.set;
                application.removeWaypoint(waypoint);
                application.saveWaypoints(wptset);
                map.invalidate();
            }

        }).setNegativeButton(R.string.no, null).show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        switch (seekBar.getId())
        {
            case R.id.trackbar:
                if (fromUser)
                {
                    application.editingTrack.editingPos = progress;
                }
                Track.TrackPoint tp = application.editingTrack.getPoint(progress);
                double ele = tp.elevation * elevationFactor;
                ((TextView) findViewById(R.id.tp_number)).setText("#" + (progress + 1));
                // FIXME Need UTM support here
                ((TextView) findViewById(R.id.tp_latitude)).setText(StringFormatter.coordinate(application.coordinateFormat, tp.latitude));
                ((TextView) findViewById(R.id.tp_longitude)).setText(StringFormatter.coordinate(application.coordinateFormat, tp.longitude));
                ((TextView) findViewById(R.id.tp_elevation)).setText(String.valueOf(Math.round(ele)) + " " + elevationAbbr);
                ((TextView) findViewById(R.id.tp_time)).setText(SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(new Date(tp.time)));
                boolean mapChanged = application.setMapCenter(tp.latitude, tp.longitude, false, false);
                if (mapChanged)
                    map.updateMapInfo();
                map.update();
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        Log.e(TAG, "onRestoreInstanceState()");
        lastKnownLocation = savedInstanceState.getParcelable("lastKnownLocation");
        lastRenderTime = savedInstanceState.getLong("lastRenderTime");
        lastMagnetic = savedInstanceState.getLong("lastMagnetic");
        lastDim = savedInstanceState.getLong("lastDim");
        lastGeoid = savedInstanceState.getBoolean("lastGeoid");

        waypointSelected = savedInstanceState.getInt("waypointSelected");
        routeSelected = savedInstanceState.getInt("routeSelected");
        areaSelected = savedInstanceState.getInt("areaSelected");
        mapObjectSelected = savedInstanceState.getLong("mapObjectSelected");

        /*
         double[] distAncor = savedInstanceState.getDoubleArray("distAncor");
         if (distAncor != null)
          {
          application.distanceOverlay = new DistanceOverlay(this);
          application.distanceOverlay.setAncor(distAncor);
          }
         */
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "onSaveInstanceState()");
        outState.putParcelable("lastKnownLocation", lastKnownLocation);
        outState.putLong("lastRenderTime", lastRenderTime);
        outState.putLong("lastMagnetic", lastMagnetic);
        outState.putLong("lastDim", lastDim);
        outState.putBoolean("lastGeoid", lastGeoid);

        outState.putInt("waypointSelected", waypointSelected);
        outState.putInt("routeSelected", routeSelected);
        outState.putInt("areaSelected", areaSelected);
        outState.putLong("mapObjectSelected", mapObjectSelected);

        if (application.distanceOverlay != null)
        {
            outState.putDoubleArray("distAncor", application.distanceOverlay.getAncor());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Resources resources = getResources();
        // application preferences
        if (getString(R.string.pref_folder_data).equals(key))
        {
            application.setDataPath(Borkozic.PATH_DATA, sharedPreferences.getString(key, resources.getString(R.string.def_folder_data)));
        }
        else if (getString(R.string.pref_folder_sas).equals(key))
        {
            application.setDataPath(Borkozic.PATH_SAS, sharedPreferences.getString(key, resources.getString(R.string.def_folder_sas)));
        }
        else if (getString(R.string.pref_folder_icon).equals(key))
        {
            application.setDataPath(Borkozic.PATH_ICONS, sharedPreferences.getString(key, resources.getString(R.string.def_folder_icon)));
        }
        else if (getString(R.string.pref_folder_plane).equals(key))
        {
            application.setDataPath(Borkozic.PATH_PLANES, sharedPreferences.getString(key, resources.getString(R.string.def_folder_plane)));
            map.planeLogo = (application.planePath).substring(36);
            map.setMovingCursorSize(sharedPreferences.getInt("planelogosize", resources.getInteger(R.integer.def_planelogosize)));
        }
        //Advaced preferences
        else if (getString(R.string.pref_planelogosize).equals(key))
        {
            map.setMovingCursorSize(sharedPreferences.getInt(key, resources.getInteger(R.integer.def_planelogosize)));
        }

        else if (getString(R.string.pref_orientation).equals(key))
        {
            setRequestedOrientation(Integer.parseInt(sharedPreferences.getString(key, "-1")));
        }

        else if (getString(R.string.pref_grid_mapshow).equals(key))
        {
            application.mapGrid = sharedPreferences.getBoolean(key, false);
            application.initGrids();
        }
        else if (getString(R.string.pref_grid_usershow).equals(key))
        {
            application.userGrid = sharedPreferences.getBoolean(key, false);
            application.initGrids();
        }
        else if (getString(R.string.pref_grid_preference).equals(key))
        {
            application.gridPrefer = Integer.parseInt(sharedPreferences.getString(key, "0"));
            application.initGrids();
        }
        else if (getString(R.string.pref_grid_userscale).equals(key) || getString(R.string.pref_grid_userunit).equals(key) || getString(R.string.pref_grid_usermpp).equals(key))
        {
            application.initGrids();
        }
        else if (getString(R.string.pref_useonlinemap).equals(key) && sharedPreferences.getBoolean(key, false))
        {
            application.setOnlineMap(sharedPreferences.getString(getString(R.string.pref_onlinemap), resources.getString(R.string.def_onlinemap)));
        }
        else if (getString(R.string.pref_onlinemap).equals(key) || getString(R.string.pref_onlinemapscale).equals(key))
        {
            application.setOnlineMap(sharedPreferences.getString(getString(R.string.pref_onlinemap), resources.getString(R.string.def_onlinemap)));
        }
        else if (getString(R.string.pref_mapadjacent).equals(key))
        {
            application.adjacentMaps = sharedPreferences.getBoolean(key, resources.getBoolean(R.bool.def_mapadjacent));
        }
        else if (getString(R.string.pref_mapcropborder).equals(key))
        {
            application.cropMapBorder = sharedPreferences.getBoolean(key, resources.getBoolean(R.bool.def_mapcropborder));
        }
        else if (getString(R.string.pref_mapdrawborder).equals(key))
        {
            application.drawMapBorder = sharedPreferences.getBoolean(key, resources.getBoolean(R.bool.def_mapdrawborder));
        }
        // activity preferences
        else if (getString(R.string.pref_wakelock).equals(key))
        {
            keepScreenOn = sharedPreferences.getBoolean(key, resources.getBoolean(R.bool.def_wakelock));
            android.view.Window wnd = getWindow();
            if (wnd != null)
            {
                if (keepScreenOn)
                    wnd.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                else
                    wnd.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
        else if (getString(R.string.pref_exit).equals(key))
        {
            exitConfirmation = Integer.parseInt(sharedPreferences.getString(key, "0"));
            secondBack = false;
        }
        else if (getString(R.string.pref_unitprecision).equals(key))
        {
            boolean precision = sharedPreferences.getBoolean(key, resources.getBoolean(R.bool.def_unitprecision));
            precisionFormat = precision ? "%.1f" : "%.0f";
        }
        // map preferences
        else if (getString(R.string.pref_cursorcolor).equals(key))
        {
            map.setCursorColor(sharedPreferences.getInt(key, ContextCompat.getColor(getApplicationContext(), R.color.cursor)));
        }
        else if (getString(R.string.pref_panelactions).equals(key))
        {
            String pa = sharedPreferences.getString(key, resources.getString(R.string.def_panelactions));
            activeActions = Arrays.asList(pa.split(","));
        }
    }

    @Override
    public void onPanelClosed(Panel panel)
    {
        // save panel state
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(getString(R.string.ui_drawer_open), false);
        editor.apply();
    }

    @Override
    public void onPanelOpened(Panel panel)
    {
        updateMapButtons();
        // save panel state
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(getString(R.string.ui_drawer_open), true);
        editor.apply();
    }



    @SuppressLint("HandlerLeak")
    private class FinishHandler extends Handler
    {
        private final WeakReference<MapActivity> target;

        FinishHandler(MapActivity activity)
        {
            this.target = new WeakReference<MapActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            MapActivity mapActivity = target.get();
            if (mapActivity != null)
            {
                mapActivity.waitBar.setVisibility(View.INVISIBLE);
                mapActivity.waitBar.setText("");
            }
        }
    }
}
