package com.borkozic.area;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.borkozic.Borkozic;
import com.borkozic.R;
import com.borkozic.data.Area;
import com.borkozic.ui.ColorButton;

public class AreaProperties extends Activity {

    private Area area;

    private TextView name;
    private TextView textViewProcentage;
    //private TextView areaTransparencyText;
    //private TextView description;
    private SeekBar seekBar;
    private CheckBox show;
    private ColorButton colorLine;
    private ColorButton colorArea;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // FIXME Should have its own layout - done
        setContentView(R.layout.act_area_properties);

        int index = getIntent().getExtras().getInt("index");

        Borkozic application = (Borkozic) getApplication();
        area = application.getArea(index);// тук трябва да зарежда зоната

        name = (TextView) findViewById(R.id.name_text);
        name.setText(area.name);
        //todo да се добави да може да се въвежда долна и горна граница на зоната


		/*
		description = (TextView) findViewById(R.id.description_text);
		description.setText(track.description);
		*/
        show = (CheckBox) findViewById(R.id.show_check);
        show.setChecked(area.show);

        colorLine = (ColorButton) findViewById(R.id.colorLine_button);
        colorLine.setColor(area.lineColor, getResources().getColor(R.color.arealinecolor));

        colorArea = (ColorButton) findViewById(R.id.colorArea_button);
        colorArea.setColor(area.areaColor, getResources().getColor(R.color.areacolor));

        ViewGroup width = (ViewGroup) findViewById(R.id.width_layout);
        width.setVisibility(View.GONE);

        Button save = (Button) findViewById(R.id.done_button);
        save.setOnClickListener(saveOnClickListener);

        Button cancel = (Button) findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { finish(); } });
        textViewProcentage = (TextView) findViewById(R.id.textView);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewProcentage.setText(""+ progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    private View.OnClickListener saveOnClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            try
            {
                area.name = name.getText().toString();
                //route.description = description.getText().toString();
                area.show = show.isChecked();
                area.lineColor = colorLine.getColor();
                area.areaColor = colorArea.getColor();
                area.AreaTransperency = seekBar.getProgress();
                //todo долна и горна граница на зоната
                setResult(RESULT_OK);
                finish();
            }
            catch (Exception e)
            {
                Toast.makeText(getBaseContext(), "Error saving Area", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        area = null;
        name = null;
        show = null;
        //todo горна и долна граница на зоната
        colorLine = null;
        colorArea = null;
    }

}
