/*
Изчертава зоната като слой. Моята добавка се състои в това да я запълни с цвят.
Това го осъществявам по следния начин. Изполвам функцията path на класа canvas.
Чрез отместване на вътре с по дебела четка запълвам зоната. Трудността в задачата се състои да определя втрепните точки.
За целта разглеждам 3 съседни точки.M1(x1,y1);M2(x2,y2);M2(x3,y3) През 2 точки минава права. Намирам ъгъла който първата правата сключва с оста Ох.
Използва се Декартовото уравнение на права y=kx+n, като коефициента n - отместването на правата от центъра не е важно, защото търсим ъгъл.
k - ъглов коефициент (не е ъгъла който сключва правата с оста Ох) k=tg(alpha)=dx/dy, където dx=x2-x1,dy=y2-y1.
ъгълът alpha=atan2(k)=atan2(dx,dy). Съответно по горните формули намирам ъглите на 2-те прави минаващи през точка M2.
Нека тези ъгли означим с alpha и beta. Нека с theta означим ъгълът който е заключен между правите. Той винаги е по-малък от 180.
Трябва да намеря координатите на четвърта точка намираща се на права която е ъглополовяща на двете прави. Т.е. тя минава през М2.
theta се намира по следната формула tg(theta)=(k2-k1)/(1-k1k2), следователно theta=atan2(k2-k1,1-k1k2), където k1 и k2 са ъгловите коефициенти на двете прави.
Ъгълът който новата ъглополовяща сключва с оста Ох е: (alpha+theta/2). Как ще намерим коордиинатите на новата точка, която е отместена на вътре по ъглополовящата.
Т.е. трябва ни отместването dx,dy от M2. Нека да го означим New_dx, New_dy. В зависимост от това колко на вътре искаме да бъде отместването изчисляваме отсечката
на отместването, която ще означим със c. Т.е. c=a/sin(theta/2). Ако (alpha+theta/2) е 0, 90 или 180 то отместването ще бъде дължината на отсечката (New_dx=0, New_dy=c).
В останалите случаи New_dx, New_dy се изчисляват използвайки питагоровата теорема(c^=New_dx^+New_dy^) и връзката New_k=New_dx/New_dy.
 */
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
import com.borkozic.util.Geometric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class AreaOverlay extends MapOverlay{
    private Paint linePaint;
    private Paint linePaint2;
    private Paint borderPaint;
    private Paint fillPaint;
    private Paint textPaint;
    private Paint textFillPaint;
    private Waypoint wpt0;
    private Waypoint wpt1;
    private Waypoint wpt2;
    private Waypoint CalcWPT;
    Area area;
    Map<Waypoint, Bitmap> bitmaps;

    private int pointWidth = 10;
    private int areaWidth = 2;
    private int areaInWidth = 10;
    private boolean showNames;

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

        linePaint2 = new Paint();
        linePaint2.setAntiAlias(false);
        linePaint2.setStrokeWidth(areaInWidth);
        linePaint2.setStyle(Paint.Style.STROKE);
        linePaint2.setColor(ContextCompat.getColor(context, R.color.areacolor));//linePaint.setColor(context.getResources().getColor(R.color.arealine));

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
        //int[] arr_xy = new int[2];
        ArrayList<arr_xy> arrL_xy= new ArrayList<>();
        ArrayList<Integer> arr_X = new ArrayList<>();
        ArrayList<Integer> arr_Y = new ArrayList<>();
        final Path path = new Path();
        final Path path2 = new Path();
        int i = 0;

        int lastX = 0, lastY = 0;
        int firstX = 0, firstY = 0;
        int secondX = 0, secondY = 0;
        int thirdX = 0, thirdY = 0;
        int endX = 0, endY = 0;
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
                    //Geometric.newPointF(xy,xy,xy,4) ;
                    arr_xy firstM = new arr_xy(firstX,firstY);
                    arrL_xy.add(i,firstM);
                    arr_X.add(i,firstX);
                    arr_Y.add(i,firstY);
                }
                else
                {
                    if (Math.abs(lastX - xy[0]) > 2 || Math.abs(lastY - xy[1]) > 2)
                    {
                        path.lineTo(xy[0] - cxy[0], xy[1] - cxy[1]);
                        lastX = xy[0];
                        lastY = xy[1];
                        arr_xy nextM = new arr_xy(lastX,lastY);
                        arrL_xy.add(i,nextM);
                        arr_X.add(i,lastX);
                        arr_Y.add(i,lastY);
                    }
                }
                i++;
            }
            if (Math.abs(lastX - firstX) > 2 || Math.abs(lastY - firstY) > 2)
            {
                path.lineTo(firstX - cxy[0], firstY - cxy[1]);// затваря фигурата
            }
            int[] xy1 = new int[2];
            xy1[0] = arr_X.get(0); xy1[1] = arr_Y.get(0);
            int[] xy2 = new int[2];
            xy2[0] = arr_X.get(1); xy2[1] = arr_Y.get(1);
            int[] xy3 = new int[2];
            xy3[0] = arr_X.get(2); xy3[1] = arr_Y.get(2);
            int[] new_xy0 = new int[2]; new_xy0[0] = arr_X.get(0)-10; new_xy0[1] = arr_Y.get(0)-10;//Geometric.newPointF(xy1,xy2,xy3,4);
            path2.setLastPoint(new_xy0[0] - cxy[0], new_xy0[1] - cxy[1]);
            xy1[0] = arr_X.get(1); xy1[1] = arr_Y.get(1);
            xy2[0] = arr_X.get(2); xy2[1] = arr_Y.get(2);
            xy2[0] = arr_X.get(3); xy2[1] = arr_Y.get(3);
            new_xy0[0] = arr_X.get(1)-10; new_xy0[1] = arr_Y.get(1)-10;
            //new_xy0 = Geometric.newPointF(xy1,xy2,xy3,4);
            path2.lineTo(new_xy0[0] - cxy[0], new_xy0[1] - cxy[1]);
            xy1[0] = arr_X.get(2); xy1[1] = arr_Y.get(2);
            xy2[0] = arr_X.get(3); xy2[1] = arr_Y.get(3);
            xy2[0] = arr_X.get(0); xy2[1] = arr_Y.get(0);
            new_xy0[0] = arr_X.get(2)-10; new_xy0[1] = arr_Y.get(2)-10;
            //new_xy0 = Geometric.newPointF(xy1,xy2,xy3,4);
            path2.lineTo(new_xy0[0] - cxy[0], new_xy0[1] - cxy[1]);
            new_xy0[0] = arr_X.get(3)-10; new_xy0[1] = arr_Y.get(3)-10;
            //new_xy0 = Geometric.newPointF(xy1,xy2,xy3,4);
            path2.lineTo(new_xy0[0] - cxy[0], new_xy0[1] - cxy[1]);
            new_xy0[0] = arr_X.get(0)-10; new_xy0[1] = arr_Y.get(0)-10;
            //new_xy0 = Geometric.newPointF(xy1,xy2,xy3,4);
            path2.lineTo(new_xy0[0] - cxy[0], new_xy0[1] - cxy[1]);
        }




        c.drawPath(path, linePaint);
        c.drawPath(path2, linePaint2);
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
