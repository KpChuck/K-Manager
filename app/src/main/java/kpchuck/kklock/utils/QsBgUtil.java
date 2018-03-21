package kpchuck.kklock.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;


import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import kpchuck.kklock.R;
import kpchuck.kklock.xml.XmlUtils;
import static kpchuck.kklock.constants.PrefConstants.*;

/**
 * Created by karol on 20/09/17.
 */

public class QsBgUtil {

    Context context;
    PrefUtils prefUtils;
    File tempFolder;
    File dir;
    FileHelper fileHelper;
    String inputFolder;


    public QsBgUtil(Context context, File tempFolder, String inputFolder) throws Exception{
        this.context=context;
        this.inputFolder = inputFolder;
        this.tempFolder = tempFolder;
        this.fileHelper = new FileHelper();
        this.prefUtils = new PrefUtils(context);
        if (prefUtils.getBool(PREF_QS_BG) || prefUtils.getBool(PREF_QS_HEADER)){
            buildDirs();
            if (prefUtils.getBool(PREF_QS_BG))
                 moveImage(PREF_QS_BG_FILE, "qs_background_primary.png");
            if (prefUtils.getBool(PREF_QS_HEADER)){
                if (modQsHeader())
                    moveImage(PREF_QS_HEADER_FILE, "arrow_down.png");
            }
        }

    }

    private void buildDirs(){
        File myDir = fileHelper.newFolder(new File(tempFolder, "backgrounds.zip"));
        this.dir = myDir;
        fileHelper.newFolder(new File(myDir, "assets"));
        fileHelper.newFolder(myDir.getAbsolutePath() + "/assets/overlays");
        String t = fileHelper.newFolder(myDir.getAbsolutePath() + "/assets/overlays/com.android.systemui.headers").getAbsolutePath();
        fileHelper.newFolder(t + "/res");
        fileHelper.newFolder(t + "/res/drawable-anydpi");
        fileHelper.newFolder(t + "/res/layout");
    }

    private void moveImage(String file_pref, String newName) throws IOException{
        File destFolder = new File(dir.getAbsolutePath() + "/assets/overlays/com.android.systemui.headers/res/drawable-anydpi");

        String filePath = prefUtils.getString(file_pref, "null");
        FileUtils.copyFileToDirectory(new File(filePath), destFolder);
        File qsFile = new File(destFolder, filePath.substring(filePath.lastIndexOf("/")));
        qsFile.renameTo(new File(destFolder, newName));

    }

    private boolean modQsHeader() throws Exception{

        File destFolder = new File(dir,  "/assets/overlays/com.android.systemui.headers/res/layout");

        File qsHeader = new File(Environment.getExternalStorageDirectory() + "/K-Klock/" + inputFolder +  "/quick_status_bar_expanded_header.xml");
        if (!qsHeader.exists()) return false;
        XmlUtils xmlUtils = new XmlUtils();

        Document xml = xmlUtils.getDocument(qsHeader);
        xml = xmlUtils.replaceAt(xml);
        Element rootElement = xml.getDocumentElement();

        String[] attrs = {"xmlns:prvandroid", "xmlns:systemui", "xmlns:aapt"};
        for (String s: attrs){
            if (rootElement.hasAttribute(s))
                rootElement.removeAttribute(s);
        }

        rootElement.setAttribute("xmlns:systemui", "http://schemas.android.com/apk/res/com.android.systemui");
        rootElement.setAttribute("android:alpha", "0.8");
        rootElement.setAttribute("android:background", "@*com.android.systemui:drawable/arrow_down");

        xmlUtils.writeDocToFile(xml, new File(destFolder, "quick_status_bar_expanded_header.xml"));


        return true;
    }

}
