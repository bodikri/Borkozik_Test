package com.borkozic.area;

import android.content.Intent;

import com.borkozic.Borkozic;
import com.borkozic.data.Area;
import com.borkozic.ui.FileListActivity;
import com.borkozic.util.AreaFilenameFilter;
import com.borkozic.util.GpxFiles;
import com.borkozic.util.KmlFiles;
import com.borkozic.util.OziExplorerFiles;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public class AreaFileList extends FileListActivity {

    @Override
    protected FilenameFilter getFilenameFilter()
    {
        return new AreaFilenameFilter();
    }

    @Override
    protected String getPath()
    {
        Borkozic application = (Borkozic) AreaFileList.this.getApplication();
        return application.dataPath;
    }

    @Override
    protected void loadFile(File file)
    {
        Borkozic application = (Borkozic) getApplication();
        List<Area> areas = null;
        try
        {
            String lc = file.getName().toLowerCase();
            if (lc.endsWith(".rt2") || lc.endsWith(".rte"))
            {
                areas = OziExplorerFiles.loadAreasFromFile(file, application.charset);
            }
            else if (lc.endsWith(".kml"))
            {
                areas = KmlFiles.loadAreasFromFile(file);
            }
            else if (lc.endsWith(".gpx"))
            {
                areas = GpxFiles.loadAreasFromFile(file);
            }
            if (areas.size() > 0)
            {
                int[] index = new int[areas.size()];
                int i = 0;
                for (Area area: areas)
                {
                    index[i] = application.addArea(area);
                    i++;
                }
                setResult(RESULT_OK, new Intent().putExtra("index", index));
            }
            else
            {
                setResult(RESULT_CANCELED, new Intent());
            }
            finish();
        }
        catch (IllegalArgumentException e)
        {
            runOnUiThread(wrongFormat);
        }
        catch (SAXException e)
        {
            runOnUiThread(wrongFormat);
            e.printStackTrace();
        }
        catch (IOException e)
        {
            runOnUiThread(readError);
            e.printStackTrace();
        }
        catch (ParserConfigurationException e)
        {
            runOnUiThread(readError);
            e.printStackTrace();
        }
    }
}
