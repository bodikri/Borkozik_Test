package com.borkozic.area;
/*
клас в който се задават глобалните настройки на конкретната Зона:Име, цвят контур, Цвят, полупрозрачност

 */
import android.app.Activity;
import android.os.Build;
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

    private static final int MIN_VALUE = 10;
    private Area area;

    private TextView name;
    private TextView textViewProcentage;
    //private TextView description;
    private SeekBar seekBarAreaTransparency;
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
        colorArea.setColor(area.fillColor, getResources().getColor(R.color.areacolor));

        ViewGroup width = (ViewGroup) findViewById(R.id.width_layout);
        width.setVisibility(View.GONE);

        Button save = (Button) findViewById(R.id.done_button);
        save.setOnClickListener(saveOnClickListener);

        Button cancel = (Button) findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { finish(); } });
        textViewProcentage = (TextView) findViewById(R.id.textView);

        seekBarAreaTransparency = (SeekBar) findViewById(R.id.seekBar);
        seekBarAreaTransparency.setMax( 200 );
        if (area.AreaTransperency < 0)
        {//todo - по това ще разбирам че за първи път се създава и е необходимопри натискане на бутона DONE - да отваря MapActivity в режим EDIT
            area.AreaTransperency = getResources().getInteger(R.integer.def_area_transparensy);//при първоначално зареждане по дефолт
        }
        //area.AreaTransperency = getResources().getInteger(R.integer.def_area_transparensy);//todo - да се постави във settings - default transparency
        seekBarAreaTransparency.setProgress(area.AreaTransperency);//todo -- не знам как да го направя както горните два атрибута на color
        textViewProcentage.setText(""+ area.AreaTransperency + "%");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBarAreaTransparency.setMin( 10 );
        }
        seekBarAreaTransparency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewProcentage.setText(""+ progress + "%");
                if (progress < MIN_VALUE) {
                    /* if seek bar value is lesser than min value then set min value to seek bar */
                    seekBar.setProgress(MIN_VALUE);
                }
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
                area.fillColor = colorArea.getColor();
                area.AreaTransperency = seekBarAreaTransparency.getProgress(); //percentToInt(seekBarAreaTransparency.getProgress());
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
/* -неуспешен опит да преобразувам процентите (0-255) в (0-100)
    private int percentToInt (int p)

    {
    int percent = Math.max(10, Math.min(100, p)); // bound percent from 0 to 100
    int intValue = Math.round( p / 100 * 255); // map percent to nearest integer (0 - 255)
        return intValue;
    }*/
}
