package kpchuck.k_klock.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by karol on 20/09/17.
 */

public class QsBgUtil {

    Context context;
    private String prefFile = "prefFileName";
    SharedPreferences myPref;
    SharedPreferences.Editor editor;
    String slash = "/";


    public QsBgUtil(Context context){
        this.context=context;
        myPref = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        if (checkPrefs()){
            buildFilePath();
           // addTyepText();


        }

    }

    private void addTyepText(){
        String type2 = "Custom Qs Background";
        File mergerFolder = new File(Environment.getExternalStorageDirectory() + "/K-Klock" + slash + "temp2" + slash + "merge");
        File destFile = new File(mergerFolder.getAbsolutePath() + "/assets/overlays/com.android.systemui.headers/type2");
       /* try {
            destFile.createNewFile();
            FileUtils.writeStringToFile(destFile, type2, true);
        }catch (IOException e){}*/

    }

    private void buildFilePath(){
        File mergerFolder = new File(Environment.getExternalStorageDirectory() + "/K-Klock" + slash + "temp2" + slash + "merge");
        File destFolder = new File(mergerFolder.getAbsolutePath() + "/assets/overlays/com.android.systemui.headers/res/drawable");

        String filePath = myPref.getString("qsBgFilePath", "null");
        try{
            FileUtils.copyFileToDirectory(new File(filePath), destFolder);
            String[] files = destFolder.list();
            File qsFile = new File(destFolder, files[0]);
            qsFile.renameTo(new File(destFolder, "qs_background_primary.png"));

        }catch (IOException e){
            Log.e("klock", e.getMessage());
        }


    }

    private boolean checkPrefs(){
        return myPref.getBoolean("qsBgPref", false);
    }


}
