package com.borkozic;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.webkit.WebView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Procedures_Text extends Activity {
    protected String btnName;
    protected Borkozic application;
// TODO move this function onto a proper place
    public static String readInputStreamAsString(InputStream in)
            throws IOException {

        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while(result != -1) {
            byte b = (byte)result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }
    //WebView myBrowser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act__procedures__txt);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        application = (Borkozic) getApplication();
        //getActionBar().setDisplayHomeAsUpEnabled(true);

      /*
      Получавам информация кой xml файл да се зареди и да се покаже
       */
               // get our html content
        //String htmlAsString = getString(R.string.StopEngine);      // used by WebView
        WebView webView = (WebView) findViewById(R.id.webView);
        //webView.loadDataWithBaseURL(null, htmlAsString, "text/html", "utf-8", null);

        btnName = getIntent().getStringExtra(BtnsProceduresSet.BTNS_TITLE); // Recieve btn Number for creating name fo xml file that need to be shown
        String xmlName = btnName + ".txt";
        //android.util.Log.d(xmlName, "onCreate: ");
        //android.util.Log.d(htmlAsString, "Data");
        try {
           // InputStream stream = getAssets().open(xmlName);
            InputStream stream = new FileInputStream(new File(application.planePath, xmlName));
            String htmlString = readInputStreamAsString(stream);

            //android.util.Log.d(htmlString, "OnCreate");
            webView.loadDataWithBaseURL(null, htmlString, "text/html", "utf-8", null);

        } catch (Exception e)
        {
//TODO Show usser what is wrong
        }

    }


}
