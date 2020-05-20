package com.borkozic.data;

import com.borkozic.util.Geo;

import java.util.ArrayList;
import java.util.List;

public class Area {

    public String name;
    public String description;
    public boolean show;
    public int wptColor = -1;
    public int lineColor = -1;
    public int lineWidth;
    public int areaColor = -1;
    public int AreaTransperency = -1;

    public double bottomArea=10;
    public double topArea=1000;
    public Waypoint AreaCenter;
    public double distance;
    public String filepath = null;
    public boolean removed = false;
    public boolean editing = false;

    private Waypoint firstWaypoint;
    private Waypoint lastWaypoint;


    private final List<Waypoint> waypoints = new ArrayList<Waypoint>(0);

    public Area()
    {
        this("", "", null,false,10,1000);
    }
    public Area(String name, String description, Waypoint AreaCenter, boolean show, double bottomArea, double topArea)
    {
        this.name = name;
        this.description = description;
        this.AreaCenter = AreaCenter;
        this.show = show;
        this.bottomArea = bottomArea;
        this.topArea = topArea;
    }

    public List<Waypoint> getWaypoints()
    {
        return waypoints;
    }

    public void addWaypoint(Waypoint waypoint)
    {
        if (lastWaypoint != null)
        {
            distance += Geo.distance(lastWaypoint.latitude, lastWaypoint.longitude, waypoint.latitude, waypoint.longitude, lastWaypoint.altitude, waypoint.altitude);
        }
        lastWaypoint = waypoint;
        waypoints.add(lastWaypoint);
    }

    public void addWaypoint(int pos, Waypoint waypoint)
    {
        waypoints.add(pos, waypoint);
        lastWaypoint = waypoints.get(waypoints.size()-1);
        distance = distanceBetween(0, waypoints.size()-1);
    }
    public Waypoint addWaypoint(String name, double lat, double lon)
    {
        Waypoint waypoint = new Waypoint(name, "", lat, lon, 0.0);//Todo fixme - must find elevation at current point on the map
        addWaypoint(waypoint);
        return waypoint;
    }
    public Waypoint addWaypoint(String name, double lat, double lon, double alt)
    {
        Waypoint waypoint = new Waypoint(name, "", lat, lon, alt);
        addWaypoint(waypoint);
        return waypoint;
    }
    public Waypoint addAreaCenter(String name, double lat, double lon, double alt)
    {
        Waypoint waypoint = new Waypoint(name, "", lat, lon, alt);
        AreaCenter = waypoint;
        return this.AreaCenter;
    }
    private void insertWaypoint(Waypoint waypoint)
    {
        if (waypoints.size() < 3) //Todo fixme - При 1 или 2 точки не може да се направи затворен контур, за това само се добавят към списъка
        {
            addWaypoint(waypoint);
            return;
        }
		//Todo fixme - При 3 и повече точки следва да се извършва проверка дали 3 последователни точки лежат на една права,
		//Да се извърши изчисляване на центъра на зоната и да се запише AreaCenter.
        int after = waypoints.size() - 1;
        double xtk = Double.MAX_VALUE;
        synchronized (waypoints)
        {
            for (int i = 0; i < waypoints.size()-1; i++)
            {
                double distance = Geo.distance(waypoint.latitude, waypoint.longitude, waypoints.get(i+1).latitude, waypoints.get(i+1).longitude, waypoint.altitude, waypoints.get(i+1).altitude);
                double bearing1 = Geo.bearing(waypoint.latitude, waypoint.longitude, waypoints.get(i+1).latitude, waypoints.get(i+1).longitude);
                double dtk1 = Geo.bearing(waypoints.get(i).latitude, waypoints.get(i).longitude, waypoints.get(i+1).latitude, waypoints.get(i+1).longitude);
                double cxtk1 = Math.abs(Geo.xtk(distance, dtk1, bearing1));
                double bearing2 = Geo.bearing(waypoint.latitude, waypoint.longitude, waypoints.get(i).latitude, waypoints.get(i).longitude);
                double dtk2 = Geo.bearing(waypoints.get(i+1).latitude, waypoints.get(i+1).longitude, waypoints.get(i).latitude, waypoints.get(i).longitude);
                double cxtk2 = Math.abs(Geo.xtk(distance, dtk2, bearing2));

                if (cxtk2 != Double.POSITIVE_INFINITY && cxtk1 < xtk)
                {
                    xtk = cxtk1;
                    after = i;
                }
            }
        }
        waypoints.add(after+1, waypoint);
        lastWaypoint = waypoints.get(waypoints.size()-1);
        distance = distanceBetween(0, waypoints.size()-1);
    }

    public Waypoint insertWaypoint(String name, double lat, double lon)
    {
        Waypoint waypoint = new Waypoint(name, "", lat, lon, 0.0);
        insertWaypoint(waypoint);
        return waypoint;
    }
    public Waypoint insertWaypoint(String name, double lat, double lon, double alt)
    {
        Waypoint waypoint = new Waypoint(name, "", lat, lon, alt);
        insertWaypoint(waypoint);
        return waypoint;
    }

    public void insertWaypoint(int after, Waypoint waypoint)
    {
        waypoints.add(after+1, waypoint);
        lastWaypoint = waypoints.get(waypoints.size()-1);
        distance = distanceBetween(0, waypoints.size()-1);
    }
    public Waypoint insertWaypoint(int after, String name, double lat, double lon)
    {
        Waypoint waypoint = new Waypoint(name, "", lat, lon);
        insertWaypoint(after, waypoint);
        return waypoint;
    }
    public Waypoint insertWaypoint(int after, String name, double lat, double lon, double alt)
    {
        Waypoint waypoint = new Waypoint(name, "", lat, lon, alt);
        insertWaypoint(after, waypoint);
        return waypoint;
    }

    public void removeWaypoint(Waypoint waypoint)
    {
        waypoints.remove(waypoint);
        if (waypoints.size() > 0)
        {
            lastWaypoint = waypoints.get(waypoints.size()-1);
            distance = distanceBetween(0, waypoints.size()-1);
        }
    }

    public Waypoint getWaypoint(int index) throws IndexOutOfBoundsException
    {
        return waypoints.get(index);
    }

    public int length()
    {
        return waypoints.size();
    }

    public void clear()
    {
        synchronized (waypoints)
        {
            waypoints.clear();
        }
        lastWaypoint = null;
        distance = 0;
    }

    public double distanceBetween(int first, int last)
    {
        double dist = 0.0;
        synchronized (waypoints)
        {
            for (int i = first; i < last; i++)
            {
                dist += Geo.distance(waypoints.get(i).latitude, waypoints.get(i).longitude, waypoints.get(i+1).latitude, waypoints.get(i+1).longitude, waypoints.get(i).altitude, waypoints.get(i+1).altitude);
            }
        }
        return dist;
    }

    public double course(int prev, int next)
    {
        synchronized (waypoints)
        {
            return Geo.bearing(waypoints.get(prev).latitude, waypoints.get(prev).longitude, waypoints.get(next).latitude, waypoints.get(next).longitude);
        }
    }

}
