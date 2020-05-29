package com.borkozic.area;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import com.borkozic.Borkozic;
import com.borkozic.R;
import com.borkozic.data.Area;
import com.borkozic.util.StringFormatter;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AreaList extends ListFragment {
    public static final int MODE_MANAGE = 1;
    public static final int MODE_START = 2;

    private static final int qaAreaDetails = 1;
    private static final int qaAreaNavigate = 2;
    private static final int qaAreaProperties = 3;
    private static final int qaAreaEdit = 4;
    private static final int qaAreaSave = 5;
    private static final int qaAreaRemove = 6;

    private OnAreaActionListener areaActionsCallback;

    protected ExecutorService threadPool = Executors.newFixedThreadPool(2);
    final Handler handler = new Handler();

    private AreaListAdapter adapter;
    private QuickAction quickAction;
    private int selectedKey;
    private Drawable selectedBackground;

    private int mode;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.list_with_empty_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        TextView emptyView = (TextView) getListView().getEmptyView();
        if (emptyView != null)
            emptyView.setText(R.string.msg_empty_area_list);

        Activity activity = getActivity();

        mode = activity.getIntent().getExtras().getInt("MODE");

        if (mode == MODE_START)
            activity.setTitle(getString(R.string.selectarea_name));

        adapter = new AreaListAdapter(activity);
        setListAdapter(adapter);

        Resources resources = getResources();
        quickAction = new QuickAction(activity);
        quickAction.addActionItem(new ActionItem(qaAreaDetails, getString(R.string.menu_details), resources.getDrawable(R.drawable.ic_action_list)));
        quickAction.addActionItem(new ActionItem(qaAreaNavigate, getString(R.string.menu_navigate), resources.getDrawable(R.drawable.ic_action_directions)));
        quickAction.addActionItem(new ActionItem(qaAreaProperties, getString(R.string.menu_properties), resources.getDrawable(R.drawable.ic_action_edit)));
        quickAction.addActionItem(new ActionItem(qaAreaEdit, getString(R.string.menu_edit), resources.getDrawable(R.drawable.ic_action_track)));
        quickAction.addActionItem(new ActionItem(qaAreaSave, getString(R.string.menu_save), resources.getDrawable(R.drawable.ic_action_save)));
        quickAction.addActionItem(new ActionItem(qaAreaRemove, getString(R.string.menu_remove), resources.getDrawable(R.drawable.ic_action_cancel)));

        quickAction.setOnActionItemClickListener(areaActionItemClickListener);
        quickAction.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss()
            {
                View v = getListView().findViewWithTag("selected");
                if (v != null)
                {
                    v.setBackground(selectedBackground);
                    v.setTag(null);
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        //super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try
        {
            areaActionsCallback = (OnAreaActionListener) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString() + " must implement OnAreaActionListener");
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        if (mode == MODE_MANAGE)
        {
            inflater.inflate(R.menu.menu_area_list, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menuNewArea:
                Borkozic application = Borkozic.getApplication();
                // todo - да направя да може клиента да въвежда центъра на зоната, по два начина-чрез координати или чрез посочване на картата
                Area area = new Area("New Area", "",null, true,10,1000);
                application.addArea(area);
                areaActionsCallback.onAreaEdit(area);
                return true;
            case R.id.menuLoadArea:
                getActivity().startActivityForResult(new Intent(getActivity(), AreaFileList.class), AreaListActivity.RESULT_LOAD_AREA);
                return true;
        }
        return false;
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id)
    {
        switch (mode)
        {
            case MODE_MANAGE:
                v.setTag("selected");
                selectedKey = position;
                selectedBackground = v.getBackground();
                int l = v.getPaddingLeft();
                int t = v.getPaddingTop();
                int r = v.getPaddingRight();
                int b = v.getPaddingBottom();
                v.setBackgroundResource(R.drawable.list_selector_background_focus);
                v.setPadding(l, t, r, b);
                quickAction.show(v);
                break;
            case MODE_START:
                Borkozic application = Borkozic.getApplication();
                Area area = application.getArea(position);
                areaActionsCallback.onAreaNavigate(area);
                break;
        }
    }

    private QuickAction.OnActionItemClickListener areaActionItemClickListener = new QuickAction.OnActionItemClickListener() {
        @Override
        public void onItemClick(QuickAction source, int pos, int actionId)
        {
            Borkozic application = Borkozic.getApplication();
            Area area = application.getArea(selectedKey);

            switch (actionId)
            {
                case qaAreaDetails:
                    areaActionsCallback.onAreaDetails(area);
                    break;
                case qaAreaNavigate:
                    areaActionsCallback.onAreaNavigate(area);
                    break;
                case qaAreaProperties:
                    areaActionsCallback.onAreaEdit(area);
                    break;
                case qaAreaEdit:
                    areaActionsCallback.onAreaEditPath(area); //todo - има нужа да проверя за какво е това
                    break;
                case qaAreaSave:
                    areaActionsCallback.onAreaSave(area);
                    break;
                case qaAreaRemove:
                    application.removeArea(area);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public class AreaListAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;
        private int mItemLayout;
        private float mDensity;
        private Path mLinePath;
        private Paint mFillPaint;
        private Paint mLinePaint;
        private Paint mBorderPaint;
        private int mPointWidth;
        private int mAreaWidth;
        private Borkozic application;

        public AreaListAdapter(Context context)
        {
            mItemLayout = R.layout.area_list_item;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mDensity = context.getResources().getDisplayMetrics().density;

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

            mLinePath = new Path();
            mLinePath.setLastPoint(12 * mDensity, 5 * mDensity);
            mLinePath.lineTo(24 * mDensity, 12 * mDensity);
            mLinePath.lineTo(15 * mDensity, 24 * mDensity);
            mLinePath.lineTo(28 * mDensity, 35 * mDensity);

            mPointWidth = settings.getInt(context.getString(R.string.pref_waypoint_width), context.getResources().getInteger(R.integer.def_waypoint_width));
            mAreaWidth = settings.getInt(context.getString(R.string.pref_area_linewidth), context.getResources().getInteger(R.integer.def_area_linewidth));
            mFillPaint = new Paint();
            mFillPaint.setAntiAlias(false);
            mFillPaint.setStrokeWidth(1);
            mFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mFillPaint.setColor(context.getResources().getColor(R.color.areawaypoint));
            mLinePaint = new Paint();
            mLinePaint.setAntiAlias(true);
            mLinePaint.setStrokeWidth(mAreaWidth * mDensity);
            mLinePaint.setStyle(Paint.Style.STROKE);
            mLinePaint.setColor(context.getResources().getColor(R.color.arealinecolor));//todo - има нужда от уточняване кой и какъв е този цвят
            mBorderPaint = new Paint();
            mBorderPaint.setAntiAlias(true);
            mBorderPaint.setStrokeWidth(1);
            mBorderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mBorderPaint.setColor(context.getResources().getColor(R.color.areacolor));//todo - има нужда от уточняване кой и какъв е този цвят

            application = Borkozic.getApplication();
        }

        @Override
        public Area getItem(int position)
        {
            return application.getArea(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public int getCount()
        {
            return application.getAreas().size();
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
            }
            Area area = getItem(position);
            TextView text = (TextView) v.findViewById(R.id.name);
            text.setText(area.name);
            String distance = StringFormatter.distanceH(area.distance);
            text = (TextView) v.findViewById(R.id.distance);
            text.setText(distance);
            text = (TextView) v.findViewById(R.id.filename);
            if (area.filepath != null)
            {
                String filepath = area.filepath.startsWith(application.dataPath) ? area.filepath.substring(application.dataPath.length() + 1, area.filepath.length()) : area.filepath;
                text.setText(filepath);
            }
            else
            {
                text.setText("");
            }
            ImageView icon = (ImageView) v.findViewById(R.id.icon);
            Bitmap bm = Bitmap.createBitmap((int) (40 * mDensity), (int) (40 * mDensity), Bitmap.Config.ARGB_8888);
            bm.eraseColor(Color.TRANSPARENT);
            Canvas bc = new Canvas(bm);
            mLinePaint.setColor(area.lineColor);
            mBorderPaint.setColor(area.fillColor);
            bc.drawPath(mLinePath, mLinePaint);
            int half = Math.round(mPointWidth / 4);
            bc.drawCircle(12 * mDensity, 5 * mDensity, half, mFillPaint);
            bc.drawCircle(12 * mDensity, 5 * mDensity, half, mBorderPaint);
            bc.drawCircle(24 * mDensity, 12 * mDensity, half, mFillPaint);
            bc.drawCircle(24 * mDensity, 12 * mDensity, half, mBorderPaint);
            bc.drawCircle(15 * mDensity, 24 * mDensity, half, mFillPaint);
            bc.drawCircle(15 * mDensity, 24 * mDensity, half, mBorderPaint);
            bc.drawCircle(28 * mDensity, 35 * mDensity, half, mFillPaint);
            bc.drawCircle(28 * mDensity, 35 * mDensity, half, mBorderPaint);
            icon.setImageBitmap(bm);

            return v;
        }

        @Override
        public boolean hasStableIds()
        {
            return true;
        }
    }
}
