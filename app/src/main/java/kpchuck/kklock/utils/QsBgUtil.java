package kpchuck.kklock.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import kpchuck.kklock.R;
import kpchuck.kklock.xml.XmlUtils;

/**
 * Created by karol on 20/09/17.
 */

public class QsBgUtil {

    Context context;
    PrefUtils prefUtils;
    File tempFolder;
    File dir;
    FileHelper fileHelper;


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

    public void changeAlpha(String png) throws FileNotFoundException{
        Bitmap originalBitmap = BitmapFactory.decodeFile(png);

        // lets create a new empty bitmap
        Bitmap newBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
          // create a canvas where we can draw on
        Canvas canvas = new Canvas(newBitmap);
          // create a paint instance with alpha
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(42);
         // now lets draw using alphaPaint instance
        canvas.drawBitmap(originalBitmap, 0, 0, alphaPaint);

          // now lets store the bitmap to a file - the canvas has drawn on the newBitmap, so we can just store that one
          // please add stream handling with try/catch blocks
        FileOutputStream fos = new FileOutputStream(new File(png));
        newBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

    }

}