package kpchuck.k_klock.utils;

import android.content.Context;
import android.util.Log;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import kpchuck.k_klock.R;
import kpchuck.k_klock.xml.XmlUtils;

/**
 * Created by karol on 20/09/17.
 */

public class QsBgUtil {

    Context context;
    PrefUtils prefUtils;
    File tempFolder;
    File dir;
    FileHelper fileHelper;

    String slash = "/";


    public QsBgUtil(Context context, File tempFolder){
        this.context=context;
        this.tempFolder = tempFolder;
        this.fileHelper = new FileHelper();
        this.prefUtils = new PrefUtils(context);
        if (prefUtils.getBool("qsBgPref")){
            buildDirs();
            buildFilePath();
            addTyepText();
        }

    }

    private void buildDirs(){
        File myDir = fileHelper.newFolder(new File(tempFolder, "backgrounds.zip"));
        this.dir = myDir;
        fileHelper.newFolder(new File(myDir, "assets"));
        fileHelper.newFolder(myDir.getAbsolutePath() + "/assets/overlays");
        String t = fileHelper.newFolder(myDir.getAbsolutePath() + "assets/overlays/com.android.systemui.headers").getAbsolutePath();
        fileHelper.newFolder(t + "/res");
        fileHelper.newFolder(t + "/res/drawable");
    }

    private void addTyepText(){
        String type2 = context.getString(R.string.qs_background_type2);
        File destFile = new File(dir.getAbsolutePath() + "/assets/overlays/com.android.systemui.headers/type2");
        new XmlUtils().writeType2Desc(type2, destFile.getAbsolutePath());


    }

    private void buildFilePath(){
        File destFolder = new File(dir.getAbsolutePath() + "/assets/overlays/com.android.systemui.headers/res/drawable");

        String filePath = prefUtils.getString("qsBgFilePath", "null");
        try{
            FileUtils.copyFileToDirectory(new File(filePath), destFolder);
            String[] files = destFolder.list();
            File qsFile = new File(destFolder, files[0]);
            qsFile.renameTo(new File(destFolder, "qs_background_primary.png"));

        }catch (IOException e){
            Log.e("klock", e.getMessage());
        }


    }

}
