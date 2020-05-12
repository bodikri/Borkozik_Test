package com.borkozic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class BtnsProceduresSet extends Activity {
    private static final String TAG = "BtnsProceduresSet";
    public static final String BTNS_TITLE = "ButonsTitle";
    protected String btnName;
    protected Borkozic application;
    // TODO move this function onto a proper place, remove from here and from Pro_Text.java


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
    /* Идея:
        изпращам информация чрез бутона кой xml файл трябва да зареди за да се покаже
        на стартираното активити.
         Всеки бутон трябва да съдържа тази информация.(как?)
         Цел първа е как да заредя бутоните от файл без да ги
         описвам програмно, като във този файл трябва да има за всеки бутон
         отпратка към съответния файл който трябва да зареди
         */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_btn_procedures);

        application = (Borkozic) getApplication();
        LinearLayout ll = (LinearLayout)findViewById(R.id.linearLayotSet);
        //Тук трябва да зареддя бутоните които прихващам от MapActiviti
        btnName = getIntent().getStringExtra(MapActivity.BTN_TITLE); // Recieve btn Number
        setTitle(btnName + " Procedures List");
        android.util.Log.d(TAG, "ReceiveBtn: " + btnName );
        String btnTxt = btnName + ".txt";

        //String area_buttons = getString(R.string.AreaButtons);
        String btnsString = "";
        try {
            //според натиснатия бутон(EMER или NORM) зарежда новите бутони
           // InputStream stream = getAssets().open(btnTxt);
            InputStream infilestream = new FileInputStream(new File(application.planePath, btnTxt));
            //btnsString = readInputStreamAsString(stream);
            btnsString = readInputStreamAsString(infilestream);
            //android.util.Log.d(htmlString, "OnCreate");
           // webView.loadDataWithBaseURL(null, htmlString, "text/html", "utf-8", null);
            //Toast.makeText(BtnsProceduresSet.this,application.dataPath, Toast.LENGTH_LONG).show();

        } catch (Exception e)
        {
       // Show usser what is wrong
      // toast - да показва пътя
            Toast.makeText(BtnsProceduresSet.this,"Папката за процедури е празна или пътя към нея е неточен!", Toast.LENGTH_LONG).show();
        }

        //android.util.Log.d(TAG, btnsString);
        //Следва код който да разделя на отделни бутони
        String splitbtns[] = btnsString.split(";");
       // String btns [] = LoadTXT();

        for (int i = 0; i < splitbtns.length; i++)
        {
            String splitbtnName[] = splitbtns[i].split(":");

            // add buttons
            try {
                Button b = new Button(this);
                String btn_txt = splitbtnName[0].replaceAll(System.getProperty("line.separator"), "");
                b.setText(btn_txt);
                b.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                final Integer btnID = Integer.parseInt(splitbtnName[1]);
                b.setId(btnID);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent BtnsIntent = new Intent(BtnsProceduresSet.this, Procedures_Text.class);
                        BtnsIntent.putExtra(BTNS_TITLE, btnID.toString());// Send btn Numer
                        startActivity(BtnsIntent);
                    }
                });
                ll.addView(b);
            }  catch (Exception e)
            {  // toast - да показва пътя
                Toast.makeText(BtnsProceduresSet.this,"Избраната папка е: " + application.planePath, Toast.LENGTH_LONG).show();
                finish();

            }





        }
    }
   /*
    public void btnClickHandler(View view) {

        Intent EmerTextIntent = new Intent(this, Procedures_Text.class);

        startActivity(EmerTextIntent);

    }
    */
}
