package com.borkozic.area;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.borkozic.Borkozic;
import com.borkozic.R;
import com.borkozic.data.Area;
import com.borkozic.data.Waypoint;
import com.borkozic.waypoint.WaypointProperties;
import com.ericharlow.DragNDrop.DragListener;
import com.ericharlow.DragNDrop.DragNDropAdapter;
import com.ericharlow.DragNDrop.DragNDropListView;
import com.ericharlow.DragNDrop.DropListener;
import com.ericharlow.DragNDrop.RemoveListener;

import java.util.ArrayList;
import java.util.List;

public class AreaEdit extends ListActivity implements DropListener, View.OnClickListener, RemoveListener, DragListener {

    private Area area;
    private int index;

    private int backgroundColor = 0x00000000;
    private int defaultBackgroundColor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_area_edit);

        index = getIntent().getExtras().getInt("INDEX");

        Borkozic application = (Borkozic) getApplication();
        //todo
        area = application.getArea(index);
        setTitle(area.name);

        ListView listView = getListView();

        if (listView instanceof DragNDropListView)
        {
            ((DragNDropListView) listView).setDropListener(this);
            ((DragNDropListView) listView).setRemoveListener(this);
            ((DragNDropListView) listView).setDragListener(this);
        }

        findViewById(R.id.done_button).setOnClickListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        List<Waypoint> waypoints = area.getWaypoints();
        ArrayList<String> content = new ArrayList<String>(waypoints.size());
        for (int i = 0; i < waypoints.size(); i++)
        {
            content.add(waypoints.get(i).name);
        }
        setListAdapter(new DragNDropAdapter(this, new int[] { R.layout.dragitem }, new int[] { R.id.TextView01 }, content));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        startActivity(new Intent(this, WaypointProperties.class).putExtra("INDEX", position).putExtra("AREA", index+1));
    }

    @Override
    public void onClick(View v)
    {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onRemove(int which)
    {
        ListAdapter adapter = getListAdapter();
        if (adapter instanceof DragNDropAdapter)
        {
            ((DragNDropAdapter) adapter).onRemove(which);
            area.removeWaypoint(area.getWaypoint(which));
            getListView().invalidateViews();
        }
    }

    @Override
    public void onDrop(int from, int to)
    {
        ListAdapter adapter = getListAdapter();
        if (adapter instanceof DragNDropAdapter)
        {
            ((DragNDropAdapter) adapter).onDrop(from, to);
            Waypoint wpt = area.getWaypoint(from);
            area.removeWaypoint(wpt);
            area.addWaypoint(from < to ? to - 1 : to, wpt);
            getListView().invalidateViews();
        }
    }

    @Override
    public void onDrag(int x, int y, ListView listView)
    {
    }

    @Override
    public void onStartDrag(View itemView)
    {
        itemView.setVisibility(View.INVISIBLE);
        defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
        itemView.setBackgroundColor(backgroundColor);
    }

    @Override
    public void onStopDrag(View itemView)
    {
        itemView.setVisibility(View.VISIBLE);
        itemView.setBackgroundColor(defaultBackgroundColor);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        area = null;
    }

}
