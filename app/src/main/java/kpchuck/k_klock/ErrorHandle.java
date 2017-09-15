package kpchuck.k_klock;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Karol Przestrzelski on 01/09/2017.
 */

public class ErrorHandle {

    String prefFile = "prefFileName";
    String asyncError = "AsyncError";

    public void resetAsyncError(Context context){
        SharedPreferences myPref = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPref.edit();
        editor.putBoolean(asyncError, true);
    }

    public boolean getAsyncError(Context context){
        SharedPreferences myPref = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        boolean noerror = myPref.getBoolean(asyncError, true);
        return noerror;
    }

    public void handleAsyncError(Context context, Exception f, boolean forOtherRoms){
        SharedPreferences myPref = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPref.edit();
        editor.putBoolean(asyncError, false);

        String rootfolder = Environment.getExternalStorageDirectory() + "/K-Klock/";

        OtherRomsHandler handler = new OtherRomsHandler(context);
        File errorFolder = handler.newFolder(rootfolder + "errorLogs");

        if(forOtherRoms) {
            String xmlFolder = rootfolder + "userInput/";

            File sysui = new File(rootfolder + "SystemUI.apk");
            File statusxml = new File(xmlFolder + "status_bar.xml");
            File keyguardxml = new File(xmlFolder + "keyguard_status_bar.xml");

            try{
                FileUtils.copyFileToDirectory(sysui, errorFolder);
                FileUtils.copyFileToDirectory(statusxml, errorFolder);
                FileUtils.copyFileToDirectory(keyguardxml, errorFolder);
            }catch (IOException e){
                Log.e("klock", e.getMessage());
            }
        }
        File emessage = deleteifexists(errorFolder + "/errorMessage.txt");
        File estack = deleteifexists(errorFolder + "/errorStacktrace.txt");
        try {

            PrintWriter printWriter = new PrintWriter(emessage);
            printWriter.println(f.getMessage());
            printWriter.close();

            PrintWriter printWriter1 = new PrintWriter(estack);
            f.printStackTrace(printWriter1);
            printWriter1.close();

        }catch (FileNotFoundException k){
            Log.e("klock", k.getMessage());
        }

        ZipUtil.pack(errorFolder, deleteifexists(rootfolder + "errorLogs.zip"));
    }

    public File deleteifexists(String path){
        File file = new File(path);
        if(file.exists())
            file.delete();
        return file;

    }
}
