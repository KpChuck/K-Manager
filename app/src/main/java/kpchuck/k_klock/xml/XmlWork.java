package kpchuck.k_klock.xml;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import kpchuck.k_klock.utils.FileHelper;
import kpchuck.k_klock.utils.PrefUtils;
import static kpchuck.k_klock.constants.PrefConstants.*;
import static kpchuck.k_klock.constants.XmlConstants.*;

/**
 * Created by karol on 31/12/17.
 */

public class XmlWork {

    private Context context;
    private String srcFolder;
    private boolean removeClock;
    private boolean makeDynamic;
    private PrefUtils prefUtils;
    private FileHelper fileHelper;
    private XmlUtils utils;
    private File romzip = new File(Environment.getExternalStorageDirectory() + "/K-Klock/tempF/Rom.zip");
    private File baseFolders = new File(romzip, "assets/overlays/com.android.systemui");
    private String keyguardx = "keyguard_status_bar.xml";
    private String statusbar = "status_bar.xml";
    private String systemicons = "system_icons.xml";

    public XmlWork(Context context, String srcFolder, boolean removeClock, boolean makeDynamic){

        this.context = context;
        this.srcFolder = Environment.getExternalStorageDirectory() + "/K-Klock/" + srcFolder;
        this.removeClock = removeClock;
        this.makeDynamic = makeDynamic;
        this.prefUtils = new PrefUtils(context);
        this.fileHelper = new FileHelper();
        this.utils = new XmlUtils();

        // Start Modding
        makeFolders();
        utils.moveAttrsIfPresent(srcFolder);
        modController();

    }


    private void modController(){

        File[] folders = baseFolders.listFiles(fileHelper.DIRECTORY);

        // Start with keyguard_status_bar.xml
        Document keyguard = getDocument(new File(srcFolder + "/keyguard_status_bar.xml"));
        keyguard = utils.replaceAt(keyguard);

        if (prefUtils.getBool(PREF_CARRIER_TEXT) && !prefUtils.getBool(PREF_CARRIER_EVERYWHERE)) {
            keyguard = addCustomTextToLockscreen(keyguard, hideCarrierText(keyguard));
        }
        keyguard = utils.fixUpForAttrs(keyguard);

        String[] modPlaces = {"type2_No_Clock_on_Lockscreen_Right", "type2_Dynamic_Clock_Right", "type2_Stock_Clock_Right"};
        // Write unmodified keyguard
        if (!leaveResBlank()) writeDocToFile(keyguard, new File(baseFolders, "res/layout/" + keyguardx));
        else {
            for (File file : folders){
                if (!Arrays.asList(modPlaces).contains(file.getName()) && !file.getName().equals("res"))
                    writeDocToFile(keyguard, new File(file, "layout/" + keyguardx));
            }
        }
        // Write modified keyguard
        for (String s: modPlaces){
            writeDocToFile(keyguard, new File(baseFolders, s + "/layout/keyguard_status_bar.xml"));
        }

        // Continue with System_icons
        if (prefUtils.getBool(PREF_MOVE_LEFT) || prefUtils.getBool(PREF_MINIT)){
            Document sysicons = getDocument(new File(srcFolder + "/" + systemicons));
            sysicons = utils.replaceAt(sysicons);
            Element rootElement = sysicons.getDocumentElement();
            sysicons = modForMinit(sysicons, rootElement);

            if(prefUtils.getBool(PREF_MOVE_LEFT)) {
                NodeList list = rootElement.getElementsByTagName("include");
                Element includeElement = (Element) list.item(0);
                rootElement.removeChild(includeElement);
            }

            fileHelper.newFolder(baseFolders, "res/");
            File lay = fileHelper.newFolder(baseFolders, "/res/layout");
            writeDocToFile(sysicons, lay);
        }

        // Finish with status_bar
        Document status = setupStatusBar();
        // Clock on Lockscreens first


        // Find elements needed
        Element rootElement = status.getDocumentElement();
        rootElement.insertBefore(createLLTop(status, X_FILL_PARENT, "center"), utils.getFirstChildElement(rootElement));

        Element stockClock = utils.findElementInDoc(status,
                "com.android.systemui.statusbar.policy.clock",
                "@*com.android.systemui:id/clock");
        Element systemIconArea = utils.findElementInDoc(status,
                "com.android.keyguard.AlphaOptimizedLinearLayout",
                "@*com.android.systemui:id/system_icon_area");
        Element statusBarContents = utils.findElementInDoc(status,
                "LinearLayout",
                "@*com.android.systemui:id/status_bar_contents");
        //Insert Right Clock First
        Element customClock = createClock(status, false, "start|center", X_WRAP_CONTENT);
        systemIconArea.insertBefore(customClock, stockClock);
        writeDocToFile(status, new File(baseFolders, "type2_Clock_on_Lockscreen_Right/layout/" + statusbar));

        // Now Left Clock
        systemIconArea.removeChild(customClock);
        customClock = createClock(status, false, "left|center", X_WRAP_CONTENT);
        statusBarContents.insertBefore(customClock, utils.getFirstChildElement(statusBarContents));
        writeDocToFile(status, new File(baseFolders, "type2_Clock_on_Lockscreen_Left/layout/" + statusbar));

        // Center Clock
        statusBarContents.removeChild(customClock);
        if (makeInlineCenter()){
            systemIconArea = utils.changeAttribute(systemIconArea, X_LAYOUT_WIDTH, "0dip");
            systemIconArea.setAttribute(X_WEIGHT, "1");

            customClock = createClock(status, false, "center", X_WRAP_CONTENT);
            Element view = createViewElement(status);

            statusBarContents.insertBefore(customClock, systemIconArea);
            systemIconArea.insertBefore(view, utils.getFirstChildElement(systemIconArea));
        }

        else {
            customClock = createClock(status, false, "center", X_FILL_PARENT);
            rootElement.insertBefore(customClock, utils.getFirstChildElement(rootElement));
        }
        if (leaveResBlank()) writeDocToFile(status, new File(baseFolders, "type2_Clock_on_Lockscreen_Center/layout/" + statusbar));
        else writeDocToFile(status, new File(baseFolders, "res/layout/" + statusbar));

        // All other clocks
        status = setupStatusBar();
        // Find elements needed
        stockClock = utils.findElementInDoc(status,
                "com.android.systemui.statusbar.policy.clock",
                "@*com.android.systemui:id/clock");
        systemIconArea = utils.findElementInDoc(status,
                "com.android.keyguard.AlphaOptimizedLinearLayout",
                "@*com.android.systemui:id/system_icon_area");
        statusBarContents = utils.findElementInDoc(status,
                "LinearLayout",
                "@*com.android.systemui:id/status_bar_contents");
        rootElement = status.getDocumentElement();

        // Right clocks
        customClock = createClock(status, false, "start|center", X_WRAP_CONTENT);
        Element hideE = createLLTop(status, X_FILL_PARENT, "center");

        systemIconArea.insertBefore(hideE, stockClock);
        hideE.appendChild(customClock);
        writeDocToFile(status, new File(baseFolders,"type2_No_Clock_on_Lockscreen_Right/layout/" + statusbar));
        if (makeDynamic) {
            customClock.setAttribute(X_ID, "@*com.android.systemui:id/clock");
            writeDocToFile(status, new File(baseFolders, "type2_Dynamic_Clock_Right/layout/" + statusbar));
        }
        hideE.removeChild(customClock);
        customClock = createClock(status, true, "start|center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        writeDocToFile(status, new File(baseFolders, "type2_Stock_Clock_Right/layout/" + statusbar));

        // Left Clocks
        systemIconArea.removeChild(hideE);
        hideE = createLLTop(status, X_WRAP_CONTENT, "left");
        customClock = createClock(status, false, "left|center", X_WRAP_CONTENT);
        statusBarContents.insertBefore(hideE, utils.getFirstChildElement(statusBarContents));
        hideE.appendChild(customClock);
        writeDocToFile(status, new File(baseFolders, "type2_No_Clock_on_Lockscreen_Left/layout/" + statusbar));
        if (makeDynamic){
            customClock.setAttribute(X_ID, "@*com.android.systemui:id/clock");
            writeDocToFile(status, new File(baseFolders, "type2_Dynamic_Clock_Left/layout/" + statusbar));
        }
        hideE.removeChild(customClock);
        customClock = createClock(status, true, "left|center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        writeDocToFile(status, new File(baseFolders, "type2_Stock_Clock_Left/layout/" + statusbar));

        // Center Clocks
        statusBarContents.removeChild(hideE);
        if (makeInlineCenter()){
            systemIconArea = utils.changeAttribute(systemIconArea, X_LAYOUT_WIDTH, "0dip");
            systemIconArea.setAttribute(X_WEIGHT, "1");

            customClock = createClock(status, false, "center", X_WRAP_CONTENT);
            hideE = createLLTop(status, X_WRAP_CONTENT, "center");
            Element view = createViewElement(status);

            statusBarContents.insertBefore(hideE, systemIconArea);
            systemIconArea.insertBefore(view, utils.getFirstChildElement(systemIconArea));

            hideE.appendChild(customClock);
            writeDocToFile(status, new File(baseFolders, "type2_No_Clock_on_Lockscreen_Center/layout/" + statusbar));
            if (makeDynamic){
                customClock.setAttribute(X_ID, "@*com.android.systemui:id/clock");
                writeDocToFile(status, new File(baseFolders, "type2_Dynamic_Clock_Center/layout/" + statusbar));
            }
            hideE.removeChild(customClock);
            customClock = createClock(status, true, "center", X_WRAP_CONTENT);
            hideE.appendChild(customClock);
            writeDocToFile(status, new File(baseFolders, "type2_Stock_Clock_Center/layout/" + statusbar));
        }

        else {
            hideE = createLLTop(status, X_FILL_PARENT, "center");
            customClock = createClock(status, false, "center", X_FILL_PARENT);
            rootElement.insertBefore(hideE, utils.getFirstChildElement(rootElement));

            hideE.appendChild(customClock);
            writeDocToFile(status, new File(baseFolders, "type2_No_Clock_on_Lockscreen_Center/layout/" + statusbar));
            if (makeDynamic){
                customClock.setAttribute(X_ID, "@*com.android.systemui:id/clock");
                writeDocToFile(status, new File(baseFolders, "type2_Dynamic_Clock_Center/layout/" + statusbar));
            }
            hideE.removeChild(customClock);
            customClock = createClock(status, true, "center", X_FILL_PARENT);
            hideE.appendChild(customClock);
            writeDocToFile(status, new File(baseFolders, "type2_Stock_Clock_Center/layout/" + statusbar));
        }



        utils.writeType2Desc(leaveResBlank() ? "Clock Style (Default NONE)" : "Clock Style (Default Clock on Lockscreen Center", baseFolders.getAbsolutePath());

    }

    private Element createViewElement(Document doc){
        Element view = doc.createElement("View");
        view.setAttribute("android:visibility", "invisible");
        view.setAttribute(X_LAYOUT_WIDTH, "0.0dip");
        view.setAttribute(X_LAYOUT_HEIGHT, "fill_parent");
        view.setAttribute(X_WEIGHT, "1.0");

        return view;
    }

    private Element createClock(Document doc, boolean stock, String gravity, String width){

        Element textClock = null;
        if (stock) {
            textClock = doc.createElement("com.android.systemui.statusbar.policy.Clock");
            textClock.setAttribute(X_ID, "@*com.android.systemui:id/clock");
        }
        else {
            textClock = doc.createElement("TextClock");
        }
        textClock.setAttribute("android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
        textClock.setAttribute("android:format24Hour", "@*com.android.systemui:string/keyguard_widget_24_hours_format");
        textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
        textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
        textClock.setAttribute("android:layout_height", "fill_parent");
        textClock.setAttribute("android:singleLine", "true");

        textClock.setAttribute("android:layout_width", width);
        textClock.setAttribute("android:gravity", gravity);

        return textClock;
    }

    private Element createLLTop(Document doc, String width, String gravity){

        Element hideyLayout = doc.createElement("LinearLayout");
        hideyLayout.setAttribute("android:layout_width", width);
        hideyLayout.setAttribute("android:layout_height", "fill_parent");
        hideyLayout.setAttribute("android:gravity", gravity);
        hideyLayout.setAttribute("android:orientation", "horizontal");
        hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");

        return hideyLayout;
    }

    private Document setupStatusBar() {
        Document status = getDocument(new File(srcFolder + "/" + statusbar));
        status = utils.replaceAt(status);
        status = utils.fixUpForAttrs(status);

        if (prefUtils.getBool(PREF_MOVE_LEFT)){
            status = moveLeft(status);
        }
        if (prefUtils.getBool(PREF_CARRIER_TEXT)){
            if (prefUtils.getBool(PREF_CARRIER_EVERYWHERE)){
                status = addCustomTextEverywhere(status);
            }
            if (prefUtils.getBool(PREF_CARRIER_HIDE_NOTIFICATIONS)){
                status = hideNotifications(status);
            }
        }
        if (removeClock){
            Element stockClock = utils.findElementInDoc(status,
                    "com.android.systemui.statusbar.policy.Clock",
                    "@*com.android.systemui:id/clock");
            stockClock = utils.changeAttribute(stockClock, X_LAYOUT_WIDTH, "0dip");
        }
        return status;
    }

    private Document hideNotifications(Document doc){
        Element statusBarContents = utils.findElementInDoc(doc, "LinearLayout", "@*com.android.systemui:id/status_bar_contents");
        Element notificationArea = utils.findElementInDoc(doc, "com.android.systemui.statusbar.AlphaOptimizedFrameLayout", "@*com.android.systemui:id/notification_icon_area");

        Element hideNotificationLayout = doc.createElement("LinearLayout");
        hideNotificationLayout.setAttribute(X_LAYOUT_WIDTH, "0dip");
        hideNotificationLayout.setAttribute(X_LAYOUT_HEIGHT, "0dip");
        hideNotificationLayout.setAttribute("android:layout_weight", "1.0");
        statusBarContents.insertBefore(hideNotificationLayout, notificationArea);
        statusBarContents.removeChild(notificationArea);
        hideNotificationLayout.appendChild(notificationArea);

        return doc;
    }

    private Document addCustomTextEverywhere(Document doc){
        Element statusBarContents = utils.findElementInDoc(doc, "LinearLayout", "@*com.android.systemui:id/status_bar_contents");

        Element customTextElement = doc.createElement("TextView");
        customTextElement = createCustomTextElement(customTextElement);

        // Insert TextView
        Element insertBeforeElement = utils.getFirstChildElement(statusBarContents);

        statusBarContents.insertBefore(customTextElement, insertBeforeElement);
        return doc;
    }

    private Document moveLeft(Document doc){
        Element layout = utils.findElementInDoc(doc, "LinearLayout", "@*com.android.systemui:id/status_bar_contents");
        // Might have to complicate this if netwrok icons on extreme left aren't good
        Element insertBeforeElement = utils.getFirstChildElement(layout);

        Element toInclude = doc.createElement("include");
        toInclude.setAttribute("android:layout_width", "wrap_content");
        toInclude.setAttribute("android:layout_height", "fill_parent");
        toInclude.setAttribute("android:layout_marginStart", "2.5dip");
        toInclude.setAttribute("layout", "@*com.android.systemui:layout/signal_cluster_view");
        toInclude.setAttribute("android:gravity", "center_vertical");

        layout.insertBefore(toInclude, insertBeforeElement);
        return doc;
    }

    private Document modForMinit(Document doc, Element rootElement){
        if (prefUtils.getBool(PREF_MINIT)){
            Element battery = utils.findElementInDoc(doc, "com.android.systemui.BatteryMeterView", "@*com.android.systemui:id/battery");
            battery.removeAttribute("android:layout_width");
            battery.removeAttribute("android:layout_height");
            battery.setAttribute("android:layout_width", "0.0dip");
            battery.setAttribute("android:layout_height", "0.0dip");

            Element minitmod = doc.createElement("com.android.systemui.statusbar.policy.MinitBattery");
            minitmod.setAttribute("android:layout_width", "wrap_content");
            minitmod.setAttribute("android:layout_height", "wrap_content");
            minitmod.setAttribute("android:layout_marginEnd", "7.0dip");

            rootElement.insertBefore(minitmod, battery);

        }
        return doc;
    }

    private Document addCustomTextToLockscreen(Document doc, Element carrierTextElement){
        Element rootElement = doc.getDocumentElement();
        Element customTextElement = doc.createElement("TextView");
        // Create custom textView
        customTextElement = createCustomTextElement(customTextElement);

        //Insert TextView
        rootElement.insertBefore(customTextElement, carrierTextElement);
        return doc;
    }

    private Element hideCarrierText(Document doc){
        Element rootElement = doc.getDocumentElement();
        Element carrierTextElement = utils.getElementById(rootElement, "com.android.keyguard.CarrierText",
                "@*com.android.systemui:id/keyguard_carrier_text");

        // Hide the carrier text
        carrierTextElement.removeAttribute(X_LAYOUT_WIDTH);
        carrierTextElement.setAttribute(X_LAYOUT_WIDTH, "0dp");

        return carrierTextElement;
    }

    private Element createCustomTextElement(Element customTextElement){
        customTextElement.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
        customTextElement.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
        customTextElement.setAttribute(X_GRAVITY, X_GRAVITY_CENTER_VERTICAL);
        customTextElement.setAttribute("android:singleLine", "true");
        customTextElement.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
        customTextElement.setAttribute("android:text", prefUtils.getString(PREF_CARRIER_CUSTOM_TEXT, ""));

        if (prefUtils.getBool(PREF_CARRIER_EVERYWHERE)) {
            customTextElement.setAttribute(X_LAYOUT_WIDTH, X_WRAP_CONTENT);
        }
        else {
            customTextElement.setAttribute("android:layout_toStartOf", "@*com.android.systemui:id/system_icons_super_container");
            customTextElement.setAttribute("android:layout_marginStart", "@*com.android.systemui:dimen/keyguard_carrier_text_margin");
            customTextElement.setAttribute(X_LAYOUT_WIDTH, X_FILL_PARENT);
        }

        return customTextElement;
    }

    private void makeFolders() {

        fileHelper.newFolder(romzip);
        fileHelper.newFolder(romzip, "assets");
        fileHelper.newFolder(romzip, "/assets/overlays");
        File s = fileHelper.newFolder(romzip, "/assets/overlays/com.android.systemui");
        String t = s.getAbsolutePath();

        ArrayList<String> startFolder = new ArrayList<>();
        startFolder.add("res");
        startFolder.add("type2_Clock_on_Lockscreen_Right");
        startFolder.add("type2_Clock_on_Lockscreen_Left");
        startFolder.add("type2_No_Clock_on_Lockscreen_Center");
        startFolder.add("type2_No_Clock_on_Lockscreen_Right");
        startFolder.add("type2_No_Clock_on_Lockscreen_Left");
        startFolder.add("type2_Stock_Clock_Center");
        startFolder.add("type2_Stock_Clock_Left");
        startFolder.add("type2_Stock_Clock_Right");
        if (makeDynamic) {
            startFolder.add("type2_Dynamic_Clock_Right");
            startFolder.add("type2_Dynamic_Clock_Left");
            startFolder.add("type2_Dynamic_Clock_Center");
        }
        if (leaveResBlank()) {
            startFolder.add("type2_Clock_on_Lockscreen_Center");startFolder.remove("res");
        }
        String slash = "/";

        for (String k : startFolder) {
            fileHelper.newFolder(t + slash + k);
            fileHelper.newFolder(t + slash + k + "/layout");

        }
    }

    private boolean leaveResBlank(){
        return ((prefUtils.getString(PREF_SELECTED_ROM, "").contains("OxygenOS") && prefUtils.getString(PREF_SELECTED_ROM, "").contains("Nougat")));

    }

    private boolean makeInlineCenter(){
        return (!(prefUtils.getBool(PREF_MOVE_LEFT) || prefUtils.getBool(PREF_CARRIER_EVERYWHERE)));
    }

    private Document getDocument(File file){
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(file);
        }catch (Exception e){
            Log.e("klock", "Error getting document for file " + file.getName() + "\n" + e.getMessage());
        }

        return doc;
    }

    private void writeDocToFile(Document doc, File[] dest){
        for (File file : dest){
            writeDocToFile(doc, file);
        }
    }

    private void writeDocToFile(Document doc, File dest){
        try{
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new FileOutputStream(dest));
            transformer.transform(source, result);
        }
        catch (Exception e){
            Log.e("klock", "Error writing document to file "+ dest.getName() + "\n" + e.getMessage());
        }
    }

}