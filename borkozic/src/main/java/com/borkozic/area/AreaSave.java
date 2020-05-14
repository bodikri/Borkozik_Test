package com.borkozic.area;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.borkozic.Borkozic;
import com.borkozic.R;
import com.borkozic.data.Area;
import com.borkozic.util.FileUtils;
import com.borkozic.util.OziExplorerFiles;

import java.io.File;

public class AreaSave extends Activity {
    private TextView filename;
    private Area area;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_save);

        filename = (TextView) findViewById(R.id.filename_text);

        int index = getIntent().getExtras().getInt("index");

        Borkozic application = (Borkozic) getApplication();
        area = application.getArea(index);

        if (area.filepath != null)
        {
            File file = new File(area.filepath);
            filename.setText(file.getName());
        }
        else
        {
            filename.setText(FileUtils.sanitizeFilename(area.name) + ".rt2");
        }

        Button save = (Button) findViewById(R.id.save_button);
        save.setOnClickListener(saveOnClickListener);

        Button cancel = (Button) findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { finish(); } });
    }

    private View.OnClickListener saveOnClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            String fname = filename.getText().toString();
            fname = fname.replace("../", "");
            fname = fname.replace("/", "");
            if ("".equals(fname))
                return;

            try
            {
                Borkozic application = (Borkozic) getApplication();
                File dir = new File(application.dataPath);
                if (! dir.exists())
                    dir.mkdirs();
                File file = new File(dir, fname);
                if (! file.exists())
                {
                    file.createNewFile();
                }
                if (file.canWrite())
                {
                    OziExplorerFiles.saveAreaToFile(file, application.charset, area);
                    area.filepath = file.getAbsolutePath();
                }
                finish();
            }
            catch (Exception e)
            {
                Toast.makeText(AreaSave.this, R.string.err_write, Toast.LENGTH_LONG).show();
                Log.e("BORKOZIC", e.toString(), e);
            }
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        area = null;
        filename = null;
    }
}
