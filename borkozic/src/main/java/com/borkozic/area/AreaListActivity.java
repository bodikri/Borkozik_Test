package com.borkozic.area;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.borkozic.Borkozic;
import com.borkozic.R;
import com.borkozic.area.OnAreaActionListener;
import com.borkozic.data.Area;

public class AreaListActivity extends AppCompatActivity implements OnAreaActionListener {
    static final int RESULT_START_AREA = 1;
    static final int RESULT_LOAD_AREA = 2;
    static final int RESULT_AREA_DETAILS = 3;

    private Borkozic application;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        application = Borkozic.getApplication();

        setContentView(R.layout.act_fragment);

        if (savedInstanceState == null)
        {
            Fragment fragment = Fragment.instantiate(this, AreaList.class.getName());
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(android.R.id.content, fragment, "AreaList");
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case RESULT_START_AREA:
                if (resultCode == RESULT_OK)
                    finish();
                break;
            case RESULT_LOAD_AREA:
                if (resultCode == RESULT_OK)
                {
                    final Borkozic application = Borkozic.getApplication();
                    int[] indexes = data.getExtras().getIntArray("index");
                    for (int index : indexes)
                    {
                        AreaOverlay newArea = new AreaOverlay(this, application.getArea(index));
                        application.areaOverlays.add(newArea);
                    }
                }
                break;
            case RESULT_AREA_DETAILS:
                if (resultCode == RESULT_OK)
                {
                    finish();
                }
        }
    }

    @Override
    public void onAreaDetails(Area area)
    {
        startActivityForResult(new Intent(this, AreaDetails.class).putExtra("index", application.getAreaIndex(area)), RESULT_AREA_DETAILS);
    }

    @Override
    public void onAreaNavigate(Area area)
    {
        startActivityForResult(new Intent(this, AreaStart.class).putExtra("index", application.getAreaIndex(area)), RESULT_START_AREA);
    }

    @Override
    public void onAreaEdit(Area area)
    {
        startActivity(new Intent(this, AreaProperties.class).putExtra("index", application.getAreaIndex(area)));
    }

    @Override
    public void onAreaEditPath(Area area)
    {
        area.show = true;
        setResult(RESULT_OK, new Intent().putExtra("index", application.getAreaIndex(area)));
        finish();
    }

    @Override
    public void onAreaSave(Area area)
    {
        startActivity(new Intent(this, AreaSave.class).putExtra("index", application.getAreaIndex(area)));
    }
}
