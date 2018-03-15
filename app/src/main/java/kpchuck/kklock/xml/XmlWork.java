package kpchuck.kklock.xml;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import kpchuck.kklock.utils.FileHelper;
import kpchuck.kklock.utils.PrefUtils;
import static kpchuck.kklock.constants.PrefConstants.*;
import static kpchuck.kklock.constants.XmlConstants.*;

/**
 * Created by karol on 31/12/17.
 */

public class XmlWork {

    private String srcFolder;
    private boolean removeClock;
    private boolean makeDynamic;
    private PrefUtils prefUtils;
    private FileHelper fileHelper;
    private XmlUtils utils;
    private File romzip = new File(Environment.getExternalStorageDirectory() + "/K-Klock/tempF/Rom.zip");
    private File baseFolders = new File(romzip, "assets/overlays/com.android.systemui");
    private String statusbar = "status_bar.xml";
    private String systemicons = "system_icons.xml";
    private boolean hasAttrs = false;

    public XmlWork(Context context, String srcFolder, boolean removeClock, boolean makeDynamic) throws Exception{

        this.srcFolder = Environment.getExternalStorageDirectory() + "/K-Klock/" + srcFolder;
        this.removeClock = removeClock;
        this.makeDynamic = makeDynamic;
        this.prefUtils = new PrefUtils(context);
        this.fileHelper = new FileHelper();
        this.utils = new XmlUtils();

        // Start Modding
        makeFolders();
        this.hasAttrs = utils.moveAttrsIfPresent(this.srcFolder);
        modController();

    }


    private void modController() throws Exception{

        File[] folders = baseFolders.listFiles(fileHelper.DIRECTORY);


        // Start with keyguard_status_bar.xml
        Document keyguard = getDocument(new File(srcFolder + "/keyguard_status_bar.xml"));
        keyguard = utils.replaceAt(keyguard);

        if ((prefUtils.getBool(PREF_CARRIER_TEXT) && !prefUtils.getBool(PREF_CARRIER_EVERYWHERE))) {
            keyguard = addCustomTextToLockscreen(keyguard, hideCarrierText(keyguard));
        }
        else if (prefUtils.getBool(PREF_MOVE_LEFT)) {
            hideCarrierText(keyguard);
        }
        if (prefUtils.getBool(PREF_BLACKOUT_LOCKSCREEN)){
            Element rootElement = keyguard.getDocumentElement();
            rootElement = utils.changeAttribute(rootElement, "android:background", "#ff000000");
        }
        keyguard = utils.fixUpForAttrs(keyguard, hasAttrs);

        Element carrierText = utils.findElementById(keyguard.getDocumentElement(),
                "@*com.android.systemui:id/keyguard_carrier_text");
        carrierText = utils.changeAttribute(carrierText, "android:textColor", "#ffffffff");
        carrierText = utils.changeAttribute(carrierText, "android:textAppearance", "?android:textAppearanceSmall");

        String[] unmodPlaces = {"type2_No_Clock_on_Lockscreen_Right", "type2_Dynamic_Clock_Right", "type2_Stock_Clock_Right",
                    "type2_Clock_on_Lockscreen_Right", "type2_Clock_on_Lockscreen_Left", "type2_Clock_on_Lockscreen_Center"};
        // Write unmodified keyguard
        for (String s: unmodPlaces){
            if (new File(baseFolders, s).exists())
                writeDocToFile(keyguard, new File(baseFolders, s + "/layout/keyguard_status_bar.xml"));
        }
        if (!leaveResBlank())
            writeDocToFile(keyguard, new File(baseFolders, "res/layout/keyguard_status_bar.xml"));

        // Write modified keyguard
        Element superContainer = utils.findElementInDoc(keyguard, "LinearLayout",
                "@*com.android.systemui:id/system_icons_super_container");
        superContainer = utils.changeAttribute(superContainer, X_LAYOUT_WIDTH, "0dip");
        superContainer.setAttribute("android:visibility", "gone");

        String[] modPlaces = {"type2_No_Clock_on_Lockscreen_Center", "type2_Dynamic_Clock_Center", "type2_Stock_Clock_Center",
                    "type2_No_Clock_on_Lockscreen_Left", "type2_Stock_Clock_Left", "type2_Dynamic_Clock_Left"};
        for (String s: modPlaces){
            if (new File(baseFolders, s).exists())
                writeDocToFile(keyguard, new File(baseFolders, s + "/layout/keyguard_status_bar.xml"));
        }

        // Continue with System_icons
        if (prefUtils.getBool(PREF_MOVE_LEFT) || prefUtils.getBool(PREF_MINIT) || prefUtils.getBool(PREF_MOVE_NOTIFICATIONS_RIGHT)){
            Document sysicons = getDocument(new File(srcFolder + "/" + systemicons));
            sysicons = utils.replaceAt(sysicons);
            Element rootElement = sysicons.getDocumentElement();
            sysicons = modForMinit(sysicons, rootElement);

            if(prefUtils.getBool(PREF_MOVE_LEFT)) {
                NodeList list = rootElement.getElementsByTagName("include");
                Element includeElement = (Element) list.item(0);

                Element battery = utils.findElementInDoc(sysicons, "com.android.systemui.BatteryMeterView",
                        "@*com.android.systemui:id/battery");
                Attr margin = includeElement.getAttributeNode("android:layout_marginStart");
                battery.setAttribute(margin.getName(), margin.getValue());

                rootElement.removeChild(includeElement);
            }

            if (prefUtils.getBool(PREF_MOVE_NOTIFICATIONS_RIGHT)){
                Element notificationArea = sysicons.createElement("com.android.systemui.statusbar.AlphaOptimizedFrameLayout");
                notificationArea.setAttribute("android:orientation", "horizontal");
                notificationArea.setAttribute(X_ID, "@*com.android.systemui:id/notification_icon_area");
                notificationArea.setAttribute(X_LAYOUT_WIDTH, "0.0dip");
                notificationArea.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
                notificationArea.setAttribute(X_WEIGHT, "1.0");

                rootElement.insertBefore(notificationArea, utils.getFirstChildElement(rootElement));
            }

            fileHelper.newFolder(baseFolders, "res/");
            File lay = fileHelper.newFolder(baseFolders, "/res/layout");
            writeDocToFile(sysicons, new File(lay, systemicons));
        }

        // Finish with status_bar
        Document status = setupStatusBar();
        // Clock on Lockscreens first


        // Find elements needed
        Element rootElement = status.getDocumentElement();


        Element systemIconArea = utils.findElementInDoc(status,
                "com.android.keyguard.AlphaOptimizedLinearLayout",
                "@*com.android.systemui:id/system_icon_area");
        Element stockClock = utils.findElementById(systemIconArea,
                "@*com.android.systemui:id/clock");
        Element statusBarContents = utils.findElementInDoc(status,
                "LinearLayout",
                "@*com.android.systemui:id/status_bar_contents");
        //Insert Right Clock First
        Element customClock = createClock(status, false, "start|center", X_WRAP_CONTENT);
        systemIconArea.insertBefore(customClock, stockClock);

        if (removeClock && Build.VERSION.SDK_INT > 25 && prefUtils.getBool(PREF_CLOCK_HIDEABLE)) systemIconArea.removeChild(stockClock);
        writeDocToFile(status, new File(baseFolders, "type2_Clock_on_Lockscreen_Right/layout/" + statusbar));

        // Now Left Clock
        systemIconArea.removeChild(customClock);
        customClock = createClock(status, false, "left|center", X_WRAP_CONTENT);
        statusBarContents.insertBefore(customClock, utils.getFirstChildElement(statusBarContents));
        writeDocToFile(status, new File(baseFolders, "type2_Clock_on_Lockscreen_Left/layout/" + statusbar));

        // Center Clock
        statusBarContents.removeChild(customClock);
        systemIconArea = utils.changeAttribute(systemIconArea, X_LAYOUT_WIDTH, "0dip");
        systemIconArea.setAttribute(X_WEIGHT, "1");

        ArrayList<Element> systemIconElements = utils.getChildElements(systemIconArea);
        for (Element e : systemIconElements){
            if (e.getAttribute(X_LAYOUT_WIDTH).equals(X_FILL_PARENT) || e.getAttribute(X_LAYOUT_WIDTH).equals("match_parent")){
                utils.changeAttribute(e, X_LAYOUT_WIDTH, X_WRAP_CONTENT);
            }
        }

        customClock = createClock(status, false, "center", X_WRAP_CONTENT);
        Element view = createViewElement(status);

        statusBarContents.insertBefore(customClock, systemIconArea);
        systemIconArea.insertBefore(view, utils.getFirstChildElement(systemIconArea));

        status = packToRightOf(status, statusBarContents, "TextClock", null);


        if (leaveResBlank()) writeDocToFile(status, new File(baseFolders, "type2_Clock_on_Lockscreen_Center/layout/" + statusbar));
        else writeDocToFile(status, new File(baseFolders, "res/layout/" + statusbar));

        // All other clocks
        status = setupStatusBar();
        // Find elements needed
        systemIconArea = utils.findElementInDoc(status,
                "com.android.keyguard.AlphaOptimizedLinearLayout",
                "@*com.android.systemui:id/system_icon_area");
        stockClock = utils.findElementById(systemIconArea,
                "@*com.android.systemui:id/clock");
        statusBarContents = utils.findElementInDoc(status,
                "LinearLayout",
                "@*com.android.systemui:id/status_bar_contents");
        rootElement = status.getDocumentElement();

        // Right clocks
        customClock = createClock(status, false, "start|center", X_WRAP_CONTENT);
        Element hideE = createLLTop(status, X_FILL_PARENT, "center");

        systemIconArea.insertBefore(hideE, stockClock);
        if (removeClock && Build.VERSION.SDK_INT > 25 && prefUtils.getBool(PREF_CLOCK_HIDEABLE)) systemIconArea.removeChild(stockClock);

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
        systemIconArea = utils.changeAttribute(systemIconArea, X_LAYOUT_WIDTH, "0dip");
        systemIconArea.setAttribute(X_WEIGHT, "1");
        systemIconElements = utils.getChildElements(systemIconArea);
        for (Element e : systemIconElements){
            if (e.getAttribute(X_LAYOUT_WIDTH).equals(X_FILL_PARENT) || e.getAttribute(X_LAYOUT_WIDTH).equals("match_parent")){
                utils.changeAttribute(e, X_LAYOUT_WIDTH, X_WRAP_CONTENT);
            }
        }

        customClock = createClock(status, false, "center", X_WRAP_CONTENT);
        hideE = createLLTop(status, X_WRAP_CONTENT, "center");
        view = createViewElement(status);

        statusBarContents.insertBefore(hideE, systemIconArea);

        systemIconArea.insertBefore(view, utils.getFirstChildElement(systemIconArea));

        hideE.appendChild(customClock);
        status = packToRightOf(status, statusBarContents, "LinearLayout", "@*com.android.systemui:id/system_icon_area");

        writeDocToFile(status, new File(baseFolders, "type2_No_Clock_on_Lockscreen_Center/layout/" + statusbar));
        if (makeDynamic){
            customClock = utils.changeAttribute(customClock, X_ID, "@*com.android.systemui:id/clock");
            customClock.setAttribute(X_ID, "");
            writeDocToFile(status, new File(baseFolders, "type2_Dynamic_Clock_Center/layout/" + statusbar));
        }
        hideE.removeChild(customClock);
        customClock = createClock(status, true, "center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        writeDocToFile(status, new File(baseFolders, "type2_Stock_Clock_Center/layout/" + statusbar));

        utils.writeType2Desc(leaveResBlank() ? "Clock Style (Default NONE)" : "Clock Style (Default Clock on Lockscreen Center",
                baseFolders.getAbsolutePath() + "/type2");

    }

    private Document packToRightOf(Document doc, Element parentElement, String tagName, String idName){
        ArrayList<Element> elements = utils.getChildElements(parentElement);
        ArrayList<Element> rightElements = utils.getRightElementsTo(parentElement, tagName, idName);
        Element linearLayout = doc.createElement("LinearLayout");
        linearLayout.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
        linearLayout.setAttribute(X_LAYOUT_WIDTH, "0dp");
        linearLayout.setAttribute(X_WEIGHT, "1.0");

        parentElement.insertBefore(linearLayout, rightElements.get(0));
       // Collections.reverse(rightElements);
        for (Element element : rightElements){
            parentElement.removeChild(element);
            linearLayout.appendChild(element);
        }
        return doc;
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
        Element stockClock = utils.findElementInDoc(doc,
                "com.android.systemui.statusbar.policy.Clock",
                "@*com.android.systemui:id/clock");
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

        if (Build.VERSION.SDK_INT > 26 && prefUtils.getBool(PREF_CLOCK_HIDEABLE)) textClock.setAttribute(X_ID, "@*com.android.systemui:id/clock");

        textClock.setAttribute("android:layout_width", width);
        textClock.setAttribute("android:gravity", gravity);
        setPadding(textClock);


        return textClock;
    }

    private void setPadding(Element textClock) {
        // Set the same padding that stock clock has
        String[] pads = {"android:paddingStart", "android:paddingEnd"};
        for (String s : pads){
            utils.changeAttribute(textClock, s, "1dp");
        }
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
        status = utils.fixUpForAttrs(status, hasAttrs);

        Element statusBarContents = utils.findElementInDoc(status,
                "LinearLayout",
                "@*com.android.systemui:id/status_bar_contents");

        if (prefUtils.getBool(PREF_MOVE_LEFT)){
            status = moveLeft(status);
        }
        if (prefUtils.getBool(PREF_CARRIER_TEXT)){
            if (prefUtils.getBool(PREF_CARRIER_EVERYWHERE)){
                status = addCustomTextEverywhere(status);
            }
            if (prefUtils.getBool(PREF_CARRIER_HIDE_NOTIFICATIONS) && !prefUtils.getBool(PREF_MOVE_NOTIFICATIONS_RIGHT)){
                status = hideNotifications(status);
            }
        }
        if (prefUtils.getBool(PREF_MOVE_NOTIFICATIONS_RIGHT) ){

            Element notification = utils.findElementInDoc(status, "com.android.systemui.statusbar.AlphaOptimizedFrameLayout",
                    "@*com.android.systemui:id/notification_icon_area");
            statusBarContents.removeChild(notification);
            ArrayList<Element> list = utils.getRightElementsTo(statusBarContents, "com.android.keyguard.AlphaOptimizedLinearLayout",
                    "@*com.android.systemui:id/system_icon_area");
            if (list.size() != 0) {
                if (!utils.isPushyOutElement(list.get(list.size() - 1))) {
                    Element view = createViewElement(status);
                    statusBarContents.insertBefore(view, utils.lastElement(statusBarContents));
                }
            }
        }

        if (removeClock){
            Element stockClock = utils.findElementInDoc(status,
                    "com.android.systemui.statusbar.policy.Clock",
                    "@*com.android.systemui:id/clock");
            utils.changeAttribute(stockClock, X_LAYOUT_WIDTH, "0dip");
        }
        if (prefUtils.getBool(PREF_CHANGE_STATBAR_COLOR)){
            statusBarContents = utils.changeAttribute(statusBarContents, "android:background",
                    prefUtils.getString(PREF_STATBAR_COLOR, ""));
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
        Element carrierTextElement = utils.findElementById(doc.getDocumentElement(),
                "@*com.android.systemui:id/keyguard_carrier_text");

        // Hide the carrier text
        if (carrierTextElement == null){
            Log.w("klock", "Can't hide carrier text - null element");

        }
        utils.changeAttribute(carrierTextElement, X_LAYOUT_WIDTH, "0dip");

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

    private void writeDocToFile(Document doc, File dest) throws Exception{

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(new FileOutputStream(dest));
        transformer.transform(source, result);

    }

}
