package kpchuck.kklock.utils;

import android.content.Context;
import android.os.Environment;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;

import kpchuck.kklock.R;
import kpchuck.kklock.xml.XmlBase;
import kpchuck.kklock.xml.XmlUtils;
import kpchuck.kklock.xml.XmlWork;

import static kpchuck.kklock.constants.PrefConstants.PREF_QS_BG;
import static kpchuck.kklock.constants.PrefConstants.PREF_QS_BG_FILE;
import static kpchuck.kklock.constants.PrefConstants.PREF_QS_HEADER;
import static kpchuck.kklock.constants.PrefConstants.PREF_QS_HEADER_FILE;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_HEIGHT;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_WIDTH;

/**
 * Created by karol on 20/09/17.
 */

public class QsBgUtil {

    private PrefUtils prefUtils;
    private File tempFolder = new File(Environment.getExternalStorageDirectory() + "/K-Klock/tempF");
    private File dir;
    private FileHelper fileHelper;
    private String header_png = "abc_list_selector_holo_dark";
    private XmlUtils xmlUtils;


    public QsBgUtil(Context context, XmlUtils xmlUtils) throws Exception{
        this.fileHelper = new FileHelper();
        this.xmlUtils = xmlUtils;
        this.prefUtils = new PrefUtils(context);
        if (prefUtils.getBool(PREF_QS_BG) || prefUtils.getBool(PREF_QS_HEADER)){
            buildDirs();
            File attention = new File(dir.getAbsolutePath() + "/assets/overlays/com.android.systemui.headers/attention");

            if (prefUtils.getBool(PREF_QS_BG))
                moveImage(PREF_QS_BG_FILE, "qs_background_primary");
                xmlUtils.writeType2Desc(context.getString(R.string.qs_images_attention), attention.getAbsolutePath());

            if (prefUtils.getBool(PREF_QS_HEADER)) {
                if (modQsHeader()){
                    moveImage(PREF_QS_HEADER_FILE, header_png);
                    xmlUtils.writeType2Desc(context.getString(R.string.qs_images_attention), attention.getAbsolutePath());
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

        fileHelper.newFolder(t + "/res/" + XmlWork.layout);
    }

    private void moveImage(String file_pref, String newName) throws IOException{
        File destFolder = new File(dir.getAbsolutePath() + "/assets/overlays/com.android.systemui.headers/res/drawable");

        String filePath = prefUtils.getString(file_pref, "null");
        FileUtils.copyFileToDirectory(new File(filePath), destFolder);
        File qsFile = new File(destFolder, filePath.substring(filePath.lastIndexOf("/")));
        qsFile.renameTo(new File(destFolder, newName + getExtension(filePath)));

    }

    private boolean modQsHeader() throws Exception{

        final String inputFolder = "userInput";
        File destFolder = new File(dir,  "/assets/overlays/com.android.systemui.headers/res/" + XmlWork.layout);

        File qsHeader = new File(Environment.getExternalStorageDirectory() + "/K-Klock/" + inputFolder +  "/quick_status_bar_expanded_header.xml");
        if (!qsHeader.exists()) return false;

        XmlBase base = new XmlBase(xmlUtils, prefUtils, qsHeader, null);
        xmlUtils.replaceStuffInXml(base.workingCopy, new String[]{"?attr/wallpaperTextColorSecondary"}, new String[]{"#ffffffff"});
        xmlUtils.replaceStuffInXml(base.workingCopy, new String[]{"@style/Widget.Material.Button.Borderless"},
                new String[]{"@android:style/Widget.Material.Button.Borderless"});

        Element rootElement = base.getDocumentElement();

        xmlUtils.changeAttribute(rootElement, "android:layout_gravity", "@*com.android.systemui:integer/notification_panel_layout_gravity");

        boolean alternate_qs_header = prefUtils.getBool("alternate_qs_header");

        if (alternate_qs_header) {
            Element image = base.workingCopy.createElement("View");
            image.setAttribute(X_LAYOUT_WIDTH, "match_parent");
            image.setAttribute(X_LAYOUT_HEIGHT, "124dip");
            image.setAttribute("android:background", "@*com.android.systemui:drawable/" + header_png);
            if (!prefUtils.getBool("opaque_qs_header") )image.setAttribute("android:alpha", "0.8");
            image.setAttribute("android:paddingStart", "0dip");
            image.setAttribute("android:paddingEnd", "0dip");
            image.setAttribute("android:layout_alignParentRight", "true");
            image.setAttribute("android:layout_alignParentTop", "true");

            rootElement.insertBefore(image, xmlUtils.getFirstChildElement(rootElement));

        } else {
            if (!prefUtils.getBool("opaque_qs_header"))
                xmlUtils.changeAttribute(rootElement, "android:alpha", "0.8");
            xmlUtils.changeAttribute(rootElement, X_LAYOUT_HEIGHT, "124dip");
            xmlUtils.changeAttribute(rootElement, "android:background", "@*com.android.systemui:drawable/" + header_png);
        }
        base.writeDocument(new File(destFolder, "quick_status_bar_expanded_header.xml"));

        return true;
    }

}
