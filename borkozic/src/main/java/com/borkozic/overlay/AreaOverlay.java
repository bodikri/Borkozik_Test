package com.borkozic.overlay;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.MotionEvent;

import androidx.core.content.ContextCompat;

import com.borkozic.Borkozic;
import com.borkozic.MapActivity;
import com.borkozic.MapView;
import com.borkozic.R;
import com.borkozic.data.Area;
import com.borkozic.data.Waypoint;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class AreaOverlay extends MapOverlay{
    Paint linePaint;
    Paint borderPaint;
    Paint fillPaint;
    Paint textPaint;
    Paint textFillPaint;
    Area area;
    Map<Waypoint, Bitmap> bitmaps;

    int pointWidth = 10;
    int areaWidth = 2;
    boolean showNames;

    public AreaOverlay(final Activity mapActivity)
    {
        super(mapActivity);

        area = new Area();
        bitmaps = new WeakHashMap<Waypoint, Bitmap>();

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(areaWidth);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(ContextCompat.getColor(context, R.color.arealinecolor));//linePaint.setColor(context.getResources().getColor(R.color.arealine));
        fillPaint = new Paint();
        fillPaint.setAntiAlias(false);
        fillPaint.setStrokeWidth(1);
        fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        fillPaint.setColor(ContextCompat.getColor(context, R.color.areawaypoint));
        borderPaint = new Paint();
        borderPaint.setAntiAlias(false);
        borderPaint.setStrokeWidth(1);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(ContextCompat.getColor(context, R.color.arealinecolor));
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStrokeWidth(2);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(pointWidth * 1.5f);
        textPaint.setTypeface(Typeface.SANS_SERIF);
        textPaint.setColor(ContextCompat.getColor(context, R.color.areawaypointtext));
        textFillPaint = new Paint();
        textFillPaint.setAntiAlias(false);
        textFillPaint.setStrokeWidth(1);
        textFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textFillPaint.setColor(ContextCompat.getColor(context, R.color.arealinecolor));

        onPreferencesChanged(PreferenceManager.getDefaultSharedPreferences(context));

        enabled = true;
    }

    public AreaOverlay(final Activity mapActivity, final Area aArea)
    {
        this(mapActivity);

        area = aArea;
        if (area.lineColor == -1)
            area.lineColor = linePaint.getColor();
        onAreaPropertiesChanged();
    }

    private void initAreaColors()
    {
        linePaint.setColor(area.lineColor);
        linePaint.setAlpha(0xAA);
        borderPaint.setColor(area.lineColor);
        textFillPaint.setColor(area.lineColor);
        textFillPaint.setAlpha(0x88);
        double Y = getLuminance(area.lineColor);
        if (Y <= .5)
            textPaint.setColor(Color.WHITE);
        else
            textPaint.setColor(Color.BLACK);
    }

    private double adjustValue(int cc)
    {
        double val = cc;
        val = val / 255;
        if (val <= 0.03928)
            val = val / 12.92;
        else
            val = Math.pow(((val + 0.055) / 1.055), 2.4);
        return val;
    }

    private double getLuminance(int rgb)
    {
        // http://www.w3.org/TR/WCAG20/relative-luminance.xml
        int R = (rgb & 0x00FF0000) >>> 16;
        int G = (rgb & 0x0000FF00) >>> 8;
        int B = (rgb & 0x000000FF);
        return 0.2126 * adjustValue(R) + 0.7152 * adjustValue(G) + 0.0722 * adjustValue(B);
    }

    public void onAreaPropertiesChanged()
    {
        if (linePaint.getColor() != area.lineColor)
        {
            initAreaColors();
        }
        if (area.editing)
        {
            linePaint.setPathEffect(new DashPathEffect(new float[] { 5, 2 }, 0));
            linePaint.setStrokeWidth(areaWidth * 3);
        }
        else
        {
            linePaint.setPathEffect(null);
            linePaint.setStrokeWidth(areaWidth);
        }
        bitmaps.clear();
    }

    public void onBeforeDestroy()
    {
        super.onBeforeDestroy();
        bitmaps.clear();
    }

    public Area getArea()
    {
        return area;
    }

    @Override
    public boolean onSingleTap(MotionEvent e, Rect mapTap, MapView mapView)
    {
        if (! area.show)
            return false;

        Borkozic application = (Borkozic) context.getApplication();

        List<Waypoint> waypoints = area.getWaypoints();
        synchronized (waypoints)
        {
            for (int i = waypoints.size() - 1; i >= 0; i--)
            {
                Waypoint wpt = waypoints.get(i);
                int[] pointXY = application.getXYbyLatLon(wpt.latitude, wpt.longitude);
                if (mapTap.contains(pointXY[0], pointXY[1]) && context instanceof MapActivity)
                {
                    return ((MapActivity) context).areaWaypointTapped(application.getAreaIndex(area), i, (int) e.getX(), (int) e.getY());
                }
            }
        }
        return false;
    }

    @Override
    protected void onDraw(final Canvas c, final MapView mapView, int centerX, int centerY)
    {
        if (!area.show)
            return;

        Borkozic application = (Borkozic) context.getApplication();

        final int[] cxy = mapView.mapCenterXY;

        final Path path = new Path();
        int i = 0;
        int lastX = 0, lastY = 0;
        int firstX = 0, firstY = 0;
        List<Waypoint> waypoints = area.getWaypoints();
        synchronized (waypoints)
        {
            for (Waypoint wpt : waypoints)
            {
                int[] xy = application.getXYbyLatLon(wpt.latitude, wpt.longitude);

                if (i == 0)
                {
                    path.setLastPoint(xy[0] - cxy[0], xy[1] - cxy[1]);
                    lastX = xy[0];
                    lastY = xy[1];
                    firstX = xy[0];
                    firstY = xy[1];
                }
                else
                {
                    if (Math.abs(lastX - xy[0]) > 2 || Math.abs(lastY - xy[1]) > 2)
                    {
                        path.lineTo(xy[0] - cxy[0], xy[1] - cxy[1]);
                        lastX = xy[0];
                        lastY = xy[1];
                    }
                }
                i++;
            }
            if (Math.abs(lastX - firstX) > 2 || Math.abs(lastY - firstY) > 2)
            {
                path.lineTo(firstX - cxy[0], firstY - cxy[1]);
            }
        }
        c.drawPath(path, linePaint);
    }

    @Override
    protected void onDrawFinished(final Canvas c, final MapView mapView, int centerX, int centerY)
    {
        if (!area.show)
            return;

        Borkozic application = (Borkozic) context.getApplication();

        final int[] cxy = mapView.mapCenterXY;

        final int half = Math.round(pointWidth / 2);

        List<Waypoint> waypoints = area.getWaypoints();

        synchronized (waypoints)
        {
            for (Waypoint wpt : waypoints)
            {
                Bitmap bitmap = bitmaps.get(wpt);
                if (bitmap == null)
                {
                    int width = pointWidth;
                    int height = pointWidth + 2;

                    if (showNames)
                    {
                        Rect bounds = new Rect();
                        textPaint.getTextBounds(wpt.name, 0, wpt.name.length(), bounds);
                        bounds.inset(-2, -4);
                        width += 5 + bounds.width();
                        if (height < bounds.height())
                            height = bounds.height();
                    }

                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas bc = new Canvas(bitmap);

                    bc.translate(half, half);
                    if (showNames)
                        bc.translate(0, 2);
                    bc.drawCircle(0, 0, half, fillPaint);
                    bc.drawCircle(0, 0, half, borderPaint);

                    if (showNames)
                    {
                        Rect rect = new Rect();
                        textPaint.getTextBounds(wpt.name, 0, wpt.name.length(), rect);
                        rect.inset(-2, -4);
                        rect.offset(+half + 5, +half - 3);
                        bc.drawRect(rect, textFillPaint);
                        bc.drawText(wpt.name, +half + 6, +half, textPaint);
                    }
                    bitmaps.put(wpt, bitmap);
                }
                int[] xy = application.getXYbyLatLon(wpt.latitude, wpt.longitude);
                c.drawBitmap(bitmap, xy[0] - half - cxy[0], xy[1] - half - cxy[1], null);
            }
        }
    }

    @Override
    public void onPreferencesChanged(SharedPreferences settings)
    {
        areaWidth = settings.getInt(context.getString(R.string.pref_area_linewidth), context.getResources().getInteger(R.integer.def_area_linewidth));
        pointWidth = settings.getInt(context.getString(R.string.pref_area_pointwidth), context.getResources().getInteger(R.integer.def_area_pointwidth));
        showNames = settings.getBoolean(context.getString(R.string.pref_area_showname), true);

        if (!area.editing)
        {
            linePaint.setStrokeWidth(areaWidth);
        }
        textPaint.setTextSize(pointWidth * 1.5f);
        bitmaps.clear();
    }
}
