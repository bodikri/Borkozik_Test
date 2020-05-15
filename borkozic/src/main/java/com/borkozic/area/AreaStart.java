package com.borkozic.area;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.borkozic.Borkozic;
import com.borkozic.R;
import com.borkozic.data.Area;
import com.borkozic.data.Waypoint;
import com.borkozic.navigation.NavigationService;

public class AreaStart extends Activity {
    private Area area;
    private RadioButton forward;
    private RadioButton reverse;

    private int index;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_area_start);

        index = getIntent().getExtras().getInt("index");

        Borkozic application = (Borkozic) getApplication();
        area = application.getArea(index);

        if (area.length() < 2)
        {
            Toast.makeText(getBaseContext(), R.string.err_shortarea, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        this.setTitle(area.name);

        Waypoint start = area.getWaypoint(0);
        Waypoint end = area.getWaypoint(area.length()-1);

        forward = (RadioButton) findViewById(R.id.forward);
        forward.setText(start.name + " to "+end.name);
        reverse = (RadioButton) findViewById(R.id.reverse);
        reverse.setText(end.name + " to "+start.name);

        forward.setChecked(true);

        Button navigate = (Button) findViewById(R.id.navigate_button);
        navigate.setOnClickListener(navigateOnClickListener);
    }

    private View.OnClickListener navigateOnClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            area.show = true;
            int dir = forward.isChecked() ? NavigationService.DIRECTION_FORWARD : NavigationService.DIRECTION_REVERSE;
            startService(new Intent(getApplicationContext(), NavigationService.class).setAction(NavigationService.NAVIGATE_AREA).putExtra("index", index).putExtra("direction", dir));
            setResult(RESULT_OK);
            finish();
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        area = null;
        forward = null;
        reverse = null;
    }
}
