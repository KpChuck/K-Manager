package kpchuck.kklock.xml;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kpchuck.kklock.R;
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
    private boolean newIconStyle = false;
    private Element newIconElement = null;

    public XmlWork(Context context, String srcFolder, boolean removeClock, boolean makeDynamic) throws Exception{

        this.srcFolder = Environment.getExternalStorageDirectory() + "/K-Klock/" + srcFolder;
        this.removeClock = removeClock;
        this.makeDynamic = makeDynamic;
        this.prefUtils = new PrefUtils(context);
        this.fileHelper = new FileHelper();
        this.utils = new XmlUtils();

        // Start Modding
        makeFolders(context);
        translate(context);
        this.hasAttrs = utils.moveAttrsIfPresent(this.srcFolder);
        modController(context);

    }

    private void modController(Context context) throws Exception{

        File[] folders = baseFolders.listFiles(fileHelper.DIRECTORY);


        // Start with keyguard_status_bar.xml
        Document keyguard = utils.getDocument(new File(srcFolder + "/keyguard_status_bar.xml"));
        keyguard = utils.replaceAt(keyguard);

        if ((prefUtils.getBool(PREF_CARRIER_TEXT) && prefUtils.getBool(PREF_CARRIER_EVERYWHERE))) {
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
        if (carrierText != null) {
            carrierText = utils.changeAttribute(carrierText, "android:textColor", "#ffffffff");
            carrierText = utils.changeAttribute(carrierText, "android:textAppearance", "?android:textAppearanceSmall");
        }

        String[] unmodPlaces = {utils.getType2(context, R.string.right_no_clock), utils.getType2(context, R.string.right_dynamic),
                utils.getType2(context, R.string.right_stock), context.getString(R.string.right_clock),
                utils.getType2(context, R.string.left_clock), utils.getType2(context, R.string.center_clock)};

        // Write unmodified keyguard
        if (prefUtils.getBool(PREF_MOVE_LEFT)) {
            for (String s : unmodPlaces) {
                if (new File(baseFolders, s).exists())
                    utils.writeDocToFile(keyguard, new File(baseFolders, s + "/layout/keyguard_status_bar.xml"));
            }
            if (!leaveResBlank())
                utils.writeDocToFile(keyguard, new File(baseFolders, "res/layout/keyguard_status_bar.xml"));
        }

        // Write modified keyguard
        Element superContainer = utils.findElementById(keyguard.getDocumentElement(),
                "@*com.android.systemui:id/system_icons_super_container");
        if (superContainer == null && Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1)
            superContainer = utils.findElementById(keyguard, "@*com.android.systemui:id/status_icon_area");

        superContainer = utils.changeAttribute(superContainer, X_LAYOUT_WIDTH, "0dip");
        superContainer.setAttribute("android:visibility", "gone");
        keyguard = fixForLg(keyguard, false);


        String[] modPlaces = {utils.getType2(context, R.string.center_no_clock), utils.getType2(context, R.string.center_dynamic),
                utils.getType2(context, R.string.center_stock), utils.getType2(context, R.string.left_no_clock),
                utils.getType2(context, R.string.left_stock), utils.getType2(context, R.string.left_dynamic)};

        if (prefUtils.getBool(PREF_MOVE_LEFT)){
            modPlaces = ArrayUtils.concat(modPlaces, unmodPlaces);
            if (!leaveResBlank())
                utils.writeDocToFile(keyguard, new File(baseFolders, "res/layout/keyguard_status_bar.xml"));
        }
        for (String s: modPlaces){
            if (new File(baseFolders, s).exists())
                utils.writeDocToFile(keyguard, new File(baseFolders, s + "/layout/keyguard_status_bar.xml"));
        }

        // Continue with System_icons
        if (prefUtils.getBool(PREF_MOVE_LEFT) || prefUtils.getBool(PREF_MINIT)){
            Document sysicons = utils.getDocument(new File(srcFolder + "/" + systemicons));
            sysicons = utils.replaceAt(sysicons);
            Element rootElement = sysicons.getDocumentElement();
            sysicons = modForMinit(sysicons, rootElement);

            if(prefUtils.getBool(PREF_MOVE_LEFT)) {
                NodeList list = rootElement.getElementsByTagName("include");
                Element includeElement = (Element) list.item(0);
                if (includeElement != null)
                    utils.changeAttribute(includeElement, X_LAYOUT_WIDTH, "0dip");
                else {
                    newIconStyle = true;
                    includeElement = utils.findElementById(sysicons, "@*com.android.systemui:id/statusIcons");
                    newIconElement = includeElement;
                    utils.changeAttribute(includeElement, X_WEIGHT, "0");
                }
            }

            fileHelper.newFolder(baseFolders, "res/");
            File lay = fileHelper.newFolder(baseFolders, "/res/layout");
            utils.writeDocToFile(sysicons, new File(lay, systemicons));
        }

        // Finish with status_bar
        Document status = setupStatusBar();
        // Clock on Lockscreens first

        Element systemIconArea = utils.findElementById(status,
                "@*com.android.systemui:id/system_icon_area");
        Element stockClock = utils.findElementById(systemIconArea,
                "@*com.android.systemui:id/clock");
        Element statusBarContents = utils.findElementById(status,
                "@*com.android.systemui:id/status_bar_contents");

        if (prefUtils.getBool(PREF_MOVE_LEFT)){
            status.getDocumentElement().insertBefore(
                    createLLTop(status, X_FILL_PARENT, "center"),
                    utils.getFirstChildElement(utils.getFirstChildElement(status.getDocumentElement()))
            );
        }
        //Insert Right Clock First
        Element customClock = createClock(status, false, "start|center", X_WRAP_CONTENT);
        systemIconArea.insertBefore(customClock, stockClock);

        if (removeClock && Build.VERSION.SDK_INT > 25 && prefUtils.getBool(PREF_CLOCK_HIDEABLE)) status.removeChild(stockClock);
        utils.writeDocToFile(status, new File(baseFolders, utils.getType2(context, R.string.right_clock)+"/layout/" + statusbar));

        // Now Left Clock
        systemIconArea.removeChild(customClock);
        customClock = createClock(status, false, "left|center", X_WRAP_CONTENT);
        statusBarContents.insertBefore(customClock, utils.getFirstChildElement(statusBarContents));
        utils.writeDocToFile(status, new File(baseFolders, utils.getType2(context, R.string.left_clock)+"/layout/" + statusbar));

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
        statusBarContents.insertBefore(customClock, systemIconArea);

        if (!utils.isPushyOutElement(utils.getFirstChildElement(systemIconArea))) {
            Element view = createViewElement(status);
            systemIconArea.insertBefore(view, utils.getFirstChildElement(systemIconArea));
        }

        status = packToRightOf(status, statusBarContents, "TextClock", null);


        if (leaveResBlank()) utils.writeDocToFile(status, new File(baseFolders, utils.getType2(context, R.string.center_clock)+"/layout/" + statusbar));
        else utils.writeDocToFile(status, new File(baseFolders, "res/layout/" + statusbar));

        // All other clocks
        status = setupStatusBar();
        // Find elements needed
        systemIconArea = utils.findElementById(status,
                "@*com.android.systemui:id/system_icon_area");
        stockClock = utils.findElementById(systemIconArea,
                "@*com.android.systemui:id/clock");
        statusBarContents = utils.findElementById(status,
                "@*com.android.systemui:id/status_bar_contents");

        // Right clocks
        if (prefUtils.getBool(PREF_MOVE_LEFT)){
            status.getDocumentElement().insertBefore(
                    createLLTop(status, X_FILL_PARENT, "center"),
                    utils.getFirstChildElement(utils.getFirstChildElement(status.getDocumentElement()))
            );
        }
        customClock = createClock(status, false, "start|center", X_WRAP_CONTENT);
        Element hideE = createLLTop(status, X_FILL_PARENT, "center");

        systemIconArea.insertBefore(hideE, stockClock);
        if (removeClock && Build.VERSION.SDK_INT > 25 && prefUtils.getBool(PREF_CLOCK_HIDEABLE)) systemIconArea.removeChild(stockClock);

        hideE.appendChild(customClock);
        utils.writeDocToFile(status, new File(baseFolders,utils.getType2(context, R.string.right_no_clock)+"/layout/" + statusbar));
        if (makeDynamic) {
            customClock.setAttribute(X_ID, "@*com.android.systemui:id/clock");
            utils.writeDocToFile(status, new File(baseFolders, utils.getType2(context, R.string.right_dynamic)+"/layout/" + statusbar));
        }

        hideE.removeChild(customClock);
        customClock = createClock(status, true, "start|center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        utils.writeDocToFile(status, new File(baseFolders, utils.getType2(context, R.string.right_stock)+"/layout/" + statusbar));

        // Left Clocks
        systemIconArea.removeChild(hideE);
        hideE = createLLTop(status, X_WRAP_CONTENT, "left");
        customClock = createClock(status, false, "left|center", X_WRAP_CONTENT);

        statusBarContents.insertBefore(hideE, utils.getFirstChildElement(statusBarContents));
        hideE.appendChild(customClock);
        utils.writeDocToFile(status, new File(baseFolders, utils.getType2(context, R.string.left_no_clock)+"/layout/" + statusbar));
        if (makeDynamic){
            customClock.setAttribute(X_ID, "@*com.android.systemui:id/clock");
            utils.writeDocToFile(status, new File(baseFolders, utils.getType2(context, R.string.left_dynamic)+"/layout/" + statusbar));
        }
        hideE.removeChild(customClock);
        customClock = createClock(status, true, "left|center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        utils.writeDocToFile(status, new File(baseFolders, utils.getType2(context, R.string.left_stock)+"/layout/" + statusbar));

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

        statusBarContents.insertBefore(hideE, systemIconArea);

        if (!utils.isPushyOutElement(utils.getFirstChildElement(systemIconArea))) {

            Element view = createViewElement(status);
            systemIconArea.insertBefore(view, utils.getFirstChildElement(systemIconArea));
        }

        hideE.appendChild(customClock);
        status = packToRightOf(status, statusBarContents, "LinearLayout", "@*com.android.systemui:id/system_icon_area");

        utils.writeDocToFile(status, new File(baseFolders, utils.getType2(context, R.string.center_no_clock)+"/layout/" + statusbar));
        if (makeDynamic){
            customClock = utils.changeAttribute(customClock, X_ID, "@*com.android.systemui:id/clock");
            customClock.setAttribute(X_ID, "");
            utils.writeDocToFile(status, new File(baseFolders, utils.getType2(context, R.string.center_dynamic)+"/layout/" + statusbar));
        }
        hideE.removeChild(customClock);
        customClock = createClock(status, true, "center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        utils.writeDocToFile(status, new File(baseFolders, utils.getType2(context, R.string.center_stock)+"/layout/" + statusbar));

        utils.writeType2Desc(leaveResBlank() ? context.getString(R.string.sysui_type2_none) : context.getString(R.string.sysui_type2_center),
                baseFolders.getAbsolutePath() + "/type2");

    }

    private Document packToRightOf(Document doc, Element parentElement, String tagName, String idName){
        ArrayList<Element> rightElements = utils.getRightElementsTo(parentElement, tagName, idName);

        Element linearLayout = doc.createElement("LinearLayout");
        linearLayout.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
        linearLayout.setAttribute(X_LAYOUT_WIDTH, "0dp");
        linearLayout.setAttribute(X_WEIGHT, "1.0");

        parentElement.insertBefore(linearLayout, utils.getFirstChildElement(parentElement));
        //parentElement.insertBefore(linearLayout, rightElements.get(0));

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

        Element textClock;

        if (stock) {
            textClock = doc.createElement("com.android.systemui.statusbar.policy.Clock");
           // textClock.setAttribute(X_ID, "@*com.android.systemui:id/clock");
        }
        else {
            textClock = doc.createElement("TextClock");
            textClock.setAttribute("android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
            textClock.setAttribute("android:format24Hour", "@*com.android.systemui:string/keyguard_widget_24_hours_format");

            if (Build.VERSION.SDK_INT > 26 && prefUtils.getBool(PREF_CLOCK_HIDEABLE))
                textClock.setAttribute(X_ID, "@*com.android.systemui:id/clock");
        }

        textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
        textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
        textClock.setAttribute("android:layout_height", "fill_parent");
        textClock.setAttribute("android:singleLine", "true");
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

    private Document setupStatusBar() throws Exception{
        Document status = utils.getDocument(new File(srcFolder + "/" + statusbar));
        status = utils.replaceAt(status);
        status = utils.fixUpForAttrs(status, hasAttrs);

        status = fixForLg(status, true);

        Element statusBarContents = utils.findElementById(status,
                "@*com.android.systemui:id/status_bar_contents");
        Element systemIconArea = utils.findElementById(status, "@*com.android.systemui:id/system_icon_area");

        if (prefUtils.getBool(PREF_MOVE_LEFT)){
            status = moveLeft(status);
        }
        if (prefUtils.getBool(PREF_CARRIER_EVERYWHERE) ){
            if (!prefUtils.getBool(PREF_CARRIER_TEXT)){
                status = addCustomTextEverywhere(status);
            }
            if (prefUtils.getBool(PREF_CARRIER_HIDE_NOTIFICATIONS) && !prefUtils.getBool(PREF_MOVE_NOTIFICATIONS_RIGHT)){
                status = hideNotifications(status);
            }
        }
        if (prefUtils.getBool(PREF_MOVE_NOTIFICATIONS_RIGHT) ){

            Element notification = utils.findElementById(status,
                    "@*com.android.systemui:id/notification_icon_area");
            notification.getParentNode().removeChild(notification);

            Element notificationArea = status.createElement("com.android.systemui.statusbar.AlphaOptimizedFrameLayout");
            notificationArea.setAttribute("android:orientation", "horizontal");
            notificationArea.setAttribute(X_ID, "@*com.android.systemui:id/notification_icon_area");
            notificationArea.setAttribute(X_LAYOUT_WIDTH, "0.0dip");
            notificationArea.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
            notificationArea.setAttribute(X_WEIGHT, "1.0");
            notificationArea.setAttribute("android:layoutDirection", "rtl");

            //notificationHolder.appendChild(notificationArea);
            systemIconArea.insertBefore(notificationArea, utils.getFirstChildElement(systemIconArea));

            utils.changeAttribute(systemIconArea, X_LAYOUT_WIDTH, "0dip");
            utils.changeAttribute(systemIconArea, X_WEIGHT, "1.0");

        }

        if (removeClock){
            List<Element> clock_elements = new ArrayList<>();
            for (String s : new String[]{"clock", "center_clock", "left_clock"}){
                clock_elements.addAll(utils.findElementsById(status, "@*com.android.systemui:id/"+s));
            }
            for (Element e: clock_elements)
                utils.changeAttribute(e, X_LAYOUT_WIDTH, "0dip");
        }
        if (prefUtils.getBool(PREF_CHANGE_STATBAR_COLOR)){
            String bg = prefUtils.getString(PREF_STATBAR_COLOR, "");
            if (bg.equals("")) bg = "#00ffffff";
            statusBarContents = utils.changeAttribute(statusBarContents, "android:background",bg);
        }
        status = addCustomIcon(status);
        return status;
    }

    private Document addCustomIcon(Document document) throws IOException{
        if (prefUtils.getBool(PREF_CUSTOM_ICON) && !prefUtils.getString(PREF_CUSTOM_ICON_FILE, "").equals("")){
            FileUtils.copyFile(new File(prefUtils.getString(PREF_CUSTOM_ICON_FILE, "")),
                    new File(fileHelper.newFolder(baseFolders, "res/drawable"), "abc_list_selector_holo_light.png"));

            Element image = document.createElement("ImageView");
            image.setAttribute(X_LAYOUT_WIDTH, "@*com.android.systemui:dimen/status_bar_icon_size");
            image.setAttribute(X_LAYOUT_HEIGHT, "@*com.android.systemui:dimen/status_bar_icon_size");
            image.setAttribute("android:src", "@*com.android.systemui:drawable/abc_list_selector_holo_light");
           // image.setAttribute("android:background", "@*com.android.systemui:drawable/abc_list_selector_holo_light");
            image.setAttribute("android:padding", "2.0dip");
            image.setAttribute("android:scaleType", "centerInside");

            Element status_bar_contents = utils.findElementById(document, "@*com.android.systemui:id/status_bar_contents");
            status_bar_contents.insertBefore(image, utils.getFirstChildElement(status_bar_contents));

        }
        return document;
    }

    private Document hideNotifications(Document doc){
        Element notificationArea = utils.findElementById(doc, "@*com.android.systemui:id/notification_icon_area");
        Element hideNotificationLayout = doc.createElement("LinearLayout");

        hideNotificationLayout.setAttribute(X_LAYOUT_WIDTH, "0dip");
        hideNotificationLayout.setAttribute(X_LAYOUT_HEIGHT, "0dip");
        hideNotificationLayout.setAttribute("android:layout_weight", "1.0");
        notificationArea.getParentNode().insertBefore(hideNotificationLayout, notificationArea);
        notificationArea.getParentNode().removeChild(notificationArea);
        hideNotificationLayout.appendChild(notificationArea);

        return doc;
    }

    private Document addCustomTextEverywhere(Document doc) throws Exception{
        Element statusBarContents = utils.findElementById(doc, "@*com.android.systemui:id/status_bar_contents");

        Element customTextElement = doc.createElement("TextView");
        customTextElement = createCustomTextElement(customTextElement);

        // Insert TextView
        Element insertBeforeElement = utils.getFirstChildElement(statusBarContents);
        statusBarContents.insertBefore(customTextElement, insertBeforeElement);

        // Write string to strings.xml
        File stringsF = new File(baseFolders, "res/values/");
        stringsF.mkdirs();
        new XmlCreation().createStringDoc(new File(stringsF, "strings.xml"), "legacy_vpn_name",
                prefUtils.getString(PREF_CARRIER_CUSTOM_TEXT, ""));

        return doc;
    }

    private Document moveLeft(Document doc){
        Element layout = utils.findElementById(doc, "@*com.android.systemui:id/status_bar_contents");
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
            Element battery = utils.findElementById(doc, "@*com.android.systemui:id/battery");
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
        if (carrierTextElement == null){
            carrierTextElement = getCarrierTextLike(doc);
        }

        carrierTextElement.getParentNode().insertBefore(customTextElement, carrierTextElement);
        return doc;
    }

    private Element hideCarrierText(Document doc){
        Element carrierTextElement = utils.findElementById(doc,
                "@*com.android.systemui:id/keyguard_carrier_text");

        // Hide the carrier text
        if (carrierTextElement == null){
            Log.w("klock", "Can't hide carrier text - null element");
            carrierTextElement = getCarrierTextLike(doc);

        }

        utils.changeAttribute(carrierTextElement, X_LAYOUT_WIDTH, "0dip");

        return carrierTextElement;
    }

    private Element getCarrierTextLike(Document doc){
        return utils.findElementLikeId(doc, "@*com.android.systemui:id/keyguard_carrier_text");
    }

    private Element createCustomTextElement(Element customTextElement){
        customTextElement.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
        customTextElement.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
        customTextElement.setAttribute(X_GRAVITY, X_GRAVITY_CENTER_VERTICAL);
        customTextElement.setAttribute("android:singleLine", "true");
        customTextElement.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
        customTextElement.setAttribute("android:text", "@*com.android.systemui:string/legacy_vpn_name");
       // customTextElement.setAttribute("android:text", prefUtils.getString(PREF_CARRIER_CUSTOM_TEXT, ""));

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

    private Document fixForLg(Document doc, boolean isStatusBar){

        Element two = utils.findElementByTag(doc, "com.lge.systemui.widget.StatusIconAnimatorView");
        Log.d("klock", "Is fix needed " + (two == null));
        if (two == null) return doc;

        if (isStatusBar){
            Element system_icon_area = utils.findElementById(doc, "@*com.android.systemui:id/system_icon_area");
            Element status_bar_contents = utils.findElementById(doc, "@*com.android.systemui:id/status_bar_contents");
            Element one = utils.findElementByTag(doc, "com.lge.systemui.widget.StatusIconsLinearLayout");

            status_bar_contents.removeChild(one);
            status_bar_contents.removeChild(two);

            system_icon_area.insertBefore(two, utils.getFirstChildElement(system_icon_area));
            system_icon_area.insertBefore(one, two);
        }
        else {
            Element one = utils.findElementByTag(doc, "com.lge.systemui.widget.StatusIconsLinearLayout");
            for (Element s: new Element[]{one, two}){
                utils.changeAttribute(s, X_LAYOUT_WIDTH, "0dip");
                utils.changeAttribute(s, "android:visibility", "gone");
            }
        }
        return doc;
    }

    private void makeFolders(Context context) {

        fileHelper.newFolder(romzip);
        fileHelper.newFolder(romzip, "assets");
        fileHelper.newFolder(romzip, "/assets/overlays");
        File s = fileHelper.newFolder(romzip, "/assets/overlays/com.android.systemui");

        ArrayList<String> startFolder = new ArrayList<>();
        startFolder.add("res");
        startFolder.add(utils.getType2(context, R.string.right_clock));
        startFolder.add(utils.getType2(context, R.string.left_clock));
        startFolder.add(utils.getType2(context, R.string.center_no_clock));
        startFolder.add(utils.getType2(context, R.string.right_no_clock));
        startFolder.add(utils.getType2(context, R.string.left_no_clock));
        startFolder.add(utils.getType2(context, R.string.center_stock));
        startFolder.add(utils.getType2(context, R.string.left_stock));
        startFolder.add(utils.getType2(context, R.string.right_stock));
        if (makeDynamic) {
            startFolder.add(utils.getType2(context, R.string.right_dynamic));
            startFolder.add(utils.getType2(context, R.string.left_dynamic));
            startFolder.add(utils.getType2(context, R.string.center_dynamic));
        }
        if (leaveResBlank()) {
            startFolder.add(utils.getType2(context, R.string.center_clock));startFolder.remove("res");
        }

        for (String k : startFolder) {
            fileHelper.newFolder(s, k);
            fileHelper.newFolder(s, k + "/layout");

        }
    }

    private boolean leaveResBlank(){
        return ((prefUtils.getString(PREF_SELECTED_ROM, "").contains("OxygenOS") && prefUtils.getString(PREF_SELECTED_ROM, "").contains("Nougat")));

    }

    private void translate(Context context){
        ArrayList<String> filenames = utils.substratize(utils.getEngArray(context, R.array.included_colors_title), "type1a", ".xml");
        filenames.addAll(utils.substratize(utils.getEngArray(context, R.array.included_formats_title), "type1b", ".xml"));
        filenames.addAll(utils.substratize(utils.getEngArray(context, R.array.font_names), "type1c", ".xml"));
        ArrayList<String> translated_filenames = utils.substratize(utils.getArray(context, R.array.included_colors_title), "type1a", ".xml");
        translated_filenames.addAll(utils.substratize(utils.getArray(context, R.array.included_formats_title), "type1b", ".xml"));
        translated_filenames.addAll(utils.substratize(utils.getArray(context, R.array.font_names), "type1c", ".xml"));

        utils.translate(context, baseFolders, filenames, translated_filenames, R.string.sysui_type1a, R.string.sysui_type1b, R.string.sysui_type1c, 0);
    }
}
