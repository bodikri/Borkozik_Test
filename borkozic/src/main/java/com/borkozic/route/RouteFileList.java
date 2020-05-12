/*
 * Androzic - android navigation client that uses OziExplorer maps (ozf2, ozfx3).
 * Copyright (C) 2010-2012  Andrey Novikov <http://andreynovikov.info/>
 *
 * This file is part of Androzic application.
 *
 * Androzic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Androzic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Androzic.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.borkozic.route;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.Intent;

import com.borkozic.Borkozic;
import com.borkozic.data.Route;
import com.borkozic.ui.FileListActivity;
import com.borkozic.util.GpxFiles;
import com.borkozic.util.KmlFiles;
import com.borkozic.util.OziExplorerFiles;
import com.borkozic.util.RouteFilenameFilter;

public class RouteFileList extends FileListActivity
{
	@Override
	protected FilenameFilter getFilenameFilter()
	{
		return new RouteFilenameFilter();
	}

	@Override
	protected String getPath()
	{
		Borkozic application = (Borkozic) RouteFileList.this.getApplication();
		return application.dataPath;
	}

	@Override
	protected void loadFile(File file)
	{
		Borkozic application = (Borkozic) getApplication();
	    List<Route> routes = null;
		try
		{
			String lc = file.getName().toLowerCase();
			if (lc.endsWith(".rt2") || lc.endsWith(".rte"))
			{
				routes = OziExplorerFiles.loadRoutesFromFile(file, application.charset);
			}
			else if (lc.endsWith(".kml"))
			{
				routes = KmlFiles.loadRoutesFromFile(file);
			}
			else if (lc.endsWith(".gpx"))
			{
				routes = GpxFiles.loadRoutesFromFile(file);
			}
			if (routes.size() > 0)
			{
				int[] index = new int[routes.size()];
				int i = 0;
				for (Route route: routes)
				{
					index[i] = application.addRoute(route);
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
