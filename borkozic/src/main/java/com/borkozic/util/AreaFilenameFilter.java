package com.borkozic.util;

import java.io.File;
import java.io.FilenameFilter;

public class AreaFilenameFilter implements FilenameFilter {


    @Override
    public boolean accept(final File dir, final String filename)
    {
        String lc = filename.toLowerCase();
        return lc.endsWith(".art2") ;
    }
}
