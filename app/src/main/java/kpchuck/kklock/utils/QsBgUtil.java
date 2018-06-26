package kpchuck.kklock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
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
import static kpchuck.kklock.constants.XmlConstants.*;

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
    String header_png = "abc_list_selector_holo_dark";


    public QsBgUtil(Context context, File tempFolder, String inputFolder) throws Exception{
        this.context=context;
        this.inputFolder = inputFolder;
        this.tempFolder = tempFolder;
        this.fileHelper = new FileHelper();
        this.prefUtils = new PrefUtils(context);
        if (prefUtils.getBool(PREF_QS_BG) || prefUtils.getBool(PREF_QS_HEADER)){
            buildDirs();
            File attention = new File(dir.getAbsolutePath() + "/assets/overlays/com.android.systemui.headers/attention");

            if (prefUtils.getBool(PREF_QS_BG))
                moveImage(PREF_QS_BG_FILE, "qs_background_primary");
                new XmlUtils().writeType2Desc(context.getString(R.string.qs_images_attention), attention.getAbsolutePath());

            if (prefUtils.getBool(PREF_QS_HEADER)) {
                if (modQsHeader()){
                    moveImage(PREF_QS_HEADER_FILE, header_png);
                    new XmlUtils().writeType2Desc(context.getString(R.string.qs_images_attention), attention.getAbsolutePath());
                 }
            }

        }

    }

    private String getExtension(String file){
        return file.substring(file.lastIndexOf("."), file.length());
    }

    private void buildDirs(){
        File myDir = fileHelper.newFolder(new File(tempFolder, "backgrounds.zip"));
        this.dir = myDir;
        fileHelper.newFolder(new File(myDir, "assets"));
        fileHelper.newFolder(myDir.getAbsolutePath() + "/assets/overlays");
        String t = fileHelper.newFolder(myDir.getAbsolutePath() + "/assets/overlays/com.android.systemui.headers").getAbsolutePath();
        fileHelper.newFolder(t + "/res");
        fileHelper.newFolder(t + "/res/drawable");
        fileHelper.newFolder(t + "/res/values");

        fileHelper.newFolder(t + "/res/layout");
    }

    private void moveImage(String file_pref, String newName) throws IOException{
        File destFolder = new File(dir.getAbsolutePath() + "/assets/overlays/com.android.systemui.headers/res/drawable");

        String filePath = prefUtils.getString(file_pref, "null");
        FileUtils.copyFileToDirectory(new File(filePath), destFolder);
        File qsFile = new File(destFolder, filePath.substring(filePath.lastIndexOf("/")));
        qsFile.renameTo(new File(destFolder, newName + getExtension(filePath)));

    }

    private boolean modQsHeader() throws Exception{

        File destFolder = new File(dir,  "/assets/overlays/com.android.systemui.headers/res/layout");

        File qsHeader = new File(Environment.getExternalStorageDirectory() + "/K-Klock/" + inputFolder +  "/quick_status_bar_expanded_header.xml");
        if (!qsHeader.exists()) return false;
        XmlUtils xmlUtils = new XmlUtils();

        Document xml = xmlUtils.getDocument(qsHeader);
        xml = xmlUtils.replaceAt(xml);
        xml = xmlUtils.replaceStuffInXml(xml, "?attr/wallpaperTextColorSecondary", "#ffffffff");

        Element rootElement = xml.getDocumentElement();

        String[] attrs = {"xmlns:prvandroid", "xmlns:systemui", "xmlns:aapt"};
        for (String s: attrs){
            if (rootElement.hasAttribute(s))
                rootElement.removeAttribute(s);
        }

        rootElement.setAttribute("xmlns:systemui", "http://schemas.android.com/apk/res/com.android.systemui");

        boolean alternate_qs_header = prefUtils.getBool("alternate_qs_header");

        if (alternate_qs_header) {
            Element image = xml.createElement("View");
            image.setAttribute(X_LAYOUT_WIDTH, "match_parent");
            image.setAttribute(X_LAYOUT_HEIGHT, "124dip");
            image.setAttribute("android:background", "@*com.android.systemui:drawable/" + header_png);
            image.setAttribute("android:alpha", "0.8");
            image.setAttribute("android:paddingStart", "0dip");
            image.setAttribute("android:paddingEnd", "0dip");
            image.setAttribute("android:layout_alignParentRight", "true");
            image.setAttribute("android:layout_alignParentTop", "true");

            rootElement.insertBefore(image, xmlUtils.getFirstChildElement(rootElement));

        } else {

            rootElement = xmlUtils.changeAttribute(rootElement, "android:alpha", "0.8");
            rootElement = xmlUtils.changeAttribute(rootElement, X_LAYOUT_HEIGHT, "124dip");
            rootElement = xmlUtils.changeAttribute(rootElement, "android:background", "@*com.android.systemui:drawable/" + header_png);
        }


        xmlUtils.writeDocToFile(xml, new File(destFolder, "quick_status_bar_expanded_header.xml"));


        return true;
    }

}
