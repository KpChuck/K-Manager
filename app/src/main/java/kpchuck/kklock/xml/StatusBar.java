package kpchuck.kklock.xml;

import android.os.Build;
import android.util.Log;
import android.view.View;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kpchuck.kklock.constants.PrefConstants;
import kpchuck.kklock.utils.FileHelper;
import kpchuck.kklock.utils.PrefUtils;

import static kpchuck.kklock.constants.PrefConstants.*;
import static kpchuck.kklock.constants.XmlConstants.*;

public class StatusBar extends XmlBase{

    private Document status;

    private Element statusBarContents;
    private Element systemIconArea;
    private boolean isCentered = false;
    private Element leftPushyElement;

    public StatusBar(XmlUtils utils, PrefUtils prefUtils, File document) throws Exception{
        super(utils, prefUtils, document);
    }

    @Override
    public void createWorkCopy() throws Exception {
        status = utils.cloneDocument(document);
        workingCopy = status;
        isCentered = false;
        setupStatusBar();
    }

    private void setupStatusBar() throws Exception{

        statusBarContents = utils.findElementById(status, "@*com.android.systemui:id/status_bar_contents");
        systemIconArea = utils.findElementById(status, "@*com.android.systemui:id/system_icon_area");

        fixForLg(status, true);

        addCustomIcon();

        if (prefUtils.getBool(PREF_CARRIER_EVERYWHERE) ){
            if (!prefUtils.getBool(PREF_CARRIER_TEXT)) addCustomTextEverywhere();
            if (prefUtils.getBool(PREF_CARRIER_HIDE_NOTIFICATIONS) && !prefUtils.getBool(PREF_MOVE_NOTIFICATIONS_RIGHT))
                hideNotifications();
        }

        moveNotificationsRight();
        moveNetworkLeft();

        if (prefUtils.getBool(DEV_HIDE_CLOCK))
            hideStockClock();

        if (prefUtils.getBool(PREF_CHANGE_STATBAR_COLOR)){
            String bg = "#" + Integer.toHexString(prefUtils.getInt(PREF_STATBAR_COLOR));
            statusBarContents = utils.changeAttribute(statusBarContents, "android:background",bg);
        }

        if (prefUtils.getInt(PREF_MOVE_LEFT) < 2){
            insertAtRoot(createHideyLayout(X_FILL_PARENT, "center"));
        }
    }


    public void insertLeft(Element element){
        Element insertBeforeElement = utils.getFirstChildElement(statusBarContents);
        utils.insertBefore(element, insertBeforeElement);
    }

    public void insertRight(Element element){
        systemIconArea.appendChild(element);
    }

    public void insertCenter(Element element){
        if (!isCentered) center();
        utils.insertBefore(element, systemIconArea);

    }

    public void insertInnerRight(Element element){
        if (utils.isPushyOutElement(element) && isCentered && leftPushyElement != null){
            systemIconArea.insertBefore(element, leftPushyElement);
            systemIconArea.removeChild(leftPushyElement);
            leftPushyElement = null;
        }
        else {
            utils.insertBefore(element, utils.getFirstChildElement(systemIconArea));
        }
    }

    public Element createClock(boolean stock, String gravity, String width){

        Element textClock;

        if (stock) {
            textClock = status.createElement("com.android.systemui.statusbar.policy.Clock");
            textClock.setAttribute(X_ID, "@*com.android.systemui:id/clock");
            int style = prefUtils.getInt("clockAmStyle");
            textClock.setAttribute("systemui:amPmStyle", String.valueOf(style));
        }
        else {
            textClock = status.createElement("TextClock");
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
            utils.changeAttribute(textClock, s, "2.5dp");
        }
    }

    public Element createHideyLayout(String width, String gravity){

        Element hideyLayout = status.createElement("LinearLayout");
        hideyLayout.setAttribute("android:layout_width", width);
        hideyLayout.setAttribute("android:layout_height", "fill_parent");
        hideyLayout.setAttribute("android:gravity", gravity);
        hideyLayout.setAttribute("android:orientation", "horizontal");
        hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");

        return hideyLayout;
    }

    public void center(){
        String system_icon_area_id = "@*com.android.systemui:id/system_icon_area";
        ArrayList<Element> rightElements = utils.getRightElementsTo(statusBarContents, "", system_icon_area_id);
        ArrayList<Element> leftElements = utils.getLeftElementsTo(statusBarContents, "", system_icon_area_id);
        packRightOf(rightElements);
        packLeftOf(leftElements);
        isCentered = true;
    }

    private void hideStockClock() {
        List<Element> clock_elements = new ArrayList<>();
        for (String s : new String[]{"clock", "center_clock", "left_clock"}){
            clock_elements.addAll(utils.findElementsById(status, "@*com.android.systemui:id/"+s));
        }
        for (Element e: clock_elements) {
            for (String attr : new String[]{"android:paddingEnd", "android:paddingStart", "android:padding", X_LAYOUT_WIDTH})
                utils.changeAttribute(e, attr, "0dip");
            if (Build.VERSION.SDK_INT > 25 && prefUtils.getBool(PREF_CLOCK_HIDEABLE))
                e.getParentNode().removeChild(e);
        }
    }

    private void hideNotifications(){
        Element notificationArea = utils.findElementById(status, "@*com.android.systemui:id/notification_icon_area");
        Element hideNotificationLayout = status.createElement("LinearLayout");

        hideNotificationLayout.setAttribute(X_LAYOUT_WIDTH, "0dip");
        hideNotificationLayout.setAttribute(X_LAYOUT_HEIGHT, "0dip");
        hideNotificationLayout.setAttribute("android:layout_weight", "1.0");
        notificationArea.getParentNode().insertBefore(hideNotificationLayout, notificationArea);
        notificationArea.getParentNode().removeChild(notificationArea);
        hideNotificationLayout.appendChild(notificationArea);
    }

    private void addCustomIcon() throws IOException {
        if (prefUtils.getBool(PREF_CUSTOM_ICON) && !prefUtils.getString(PREF_CUSTOM_ICON_FILE, "").equals("")){
            FileUtils.copyFile(new File(prefUtils.getString(PREF_CUSTOM_ICON_FILE, "")),
                    new File(new FileHelper().newFolder(utils.baseFolders, "res/drawable"), "abc_list_selector_holo_light.png"));

            Element image = status.createElement("ImageView");
            image.setAttribute(X_LAYOUT_WIDTH, "@*com.android.systemui:dimen/status_bar_icon_size");
            image.setAttribute(X_LAYOUT_HEIGHT, "@*com.android.systemui:dimen/status_bar_icon_size");
            image.setAttribute("android:src", "@*com.android.systemui:drawable/abc_list_selector_holo_light");
            // image.setAttribute("android:background", "@*com.android.systemui:drawable/abc_list_selector_holo_light");
            image.setAttribute("android:padding", "2.0dip");
            image.setAttribute("android:scaleType", "centerInside");

           insertLeft(image);
        }
    }

    private void addCustomTextEverywhere() throws Exception{

        Element customTextElement = status.createElement("TextView");
        createCustomTextElement(customTextElement);

        // Insert TextView
        insertLeft(customTextElement);
    }

    private void moveNetworkLeft(){
        int position = prefUtils.getInt(PREF_MOVE_LEFT);
        if (position == 2) return; // Already on right

        Element toInclude = status.createElement("include");
        toInclude.setAttribute("android:layout_width", "wrap_content");
        toInclude.setAttribute("android:layout_height", "fill_parent");
        toInclude.setAttribute("android:layout_marginStart", "2.5dip");
        toInclude.setAttribute("layout", "@*com.android.systemui:layout/signal_cluster_view");
        toInclude.setAttribute("android:gravity", "center_vertical");

        if (position == 0)
            insertLeft(toInclude);
        else
            insertCenter(toInclude);
    }

    private void moveNotificationsRight(){
        int position = prefUtils.getInt(PREF_MOVE_NOTIFICATIONS_RIGHT);
        if (position == 0) return; // Already on right

        Element notification = utils.findElementById(status,
                "@*com.android.systemui:id/notification_icon_area");
        notification.getParentNode().removeChild(notification);

        Element notificationArea = status.createElement("com.android.systemui.statusbar.AlphaOptimizedFrameLayout");
        notificationArea.setAttribute("android:orientation", "horizontal");
        notificationArea.setAttribute(X_ID, "@*com.android.systemui:id/notification_icon_area");
        notificationArea.setAttribute(X_LAYOUT_WIDTH, "0.0dip");
        notificationArea.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
        notificationArea.setAttribute(X_WEIGHT, "1.0");
        if (position == 2)
            notificationArea.setAttribute("android:layoutDirection", "rtl");

        if (position == 2) {
            insertInnerRight(notificationArea);
            utils.changeAttribute(systemIconArea, X_LAYOUT_WIDTH, "0dip");
            utils.changeAttribute(systemIconArea, X_WEIGHT, "1.0");
        }
        else {

            notificationArea = status.createElement("LinearLayout");
            notificationArea.setAttribute("android:orientation", "horizontal");
            notificationArea.setAttribute(X_ID, "@*com.android.systemui:id/notification_icon_area");
            notificationArea.setAttribute(X_LAYOUT_WIDTH, "0.0dip");
            notificationArea.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
            notificationArea.setAttribute(X_WEIGHT, "1.0");
            notificationArea.setAttribute("android:weightSum", "100");
            notificationArea.setAttribute("android:background", "#ffff0000");

            Element v = createViewElement();
            notificationArea.appendChild(v);

            insertCenter(notificationArea);
            utils.changeAttribute(systemIconArea, "android:background", "#ff00ff00");
            utils.changeAttribute(statusBarContents, "android:background", "#ff0000ff");
        }

    }

    private Element createViewElement(){
        Element view = status.createElement("View");
        view.setAttribute("android:visibility", "invisible");
        view.setAttribute(X_LAYOUT_WIDTH, "0.0dip");
        view.setAttribute(X_LAYOUT_HEIGHT, "fill_parent");
        view.setAttribute(X_WEIGHT, "1.0");

        return view;
    }

    private void packRightOf(ArrayList<Element> rightElements){

        Element linearLayout = status.createElement("LinearLayout");
        linearLayout.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
        linearLayout.setAttribute(X_LAYOUT_WIDTH, "0dp");
        linearLayout.setAttribute(X_WEIGHT, "1.0");
        linearLayout.setAttribute("android:tag", "right_elements");

        // If there are right elements, insert Layout before the first one
        // Else just add it onto the end
        if (rightElements.size() > 0)
            statusBarContents.insertBefore(linearLayout, rightElements.get(0));
        else
            statusBarContents.appendChild(linearLayout);

        // If there are right elements or the first one hasnt got layout_width
        // Create layout with android:layout_width
        if (rightElements.size() == 0 || !utils.isPushyOutElement(rightElements.get(0))) {
            leftPushyElement = createViewElement();
            linearLayout.appendChild(leftPushyElement);
        }

        // Remove all right elements and append them to linearLayout
        for (Element element : rightElements){
            statusBarContents.removeChild(element);
            linearLayout.appendChild(element);

        }
        // Make the new systemIconArea the right element container
        systemIconArea=linearLayout;
    }

    private void packLeftOf(ArrayList<Element> leftElements){

        Element linearLayout = status.createElement("LinearLayout");
        linearLayout.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
        linearLayout.setAttribute(X_LAYOUT_WIDTH, "0dp");
        linearLayout.setAttribute(X_WEIGHT, "1.0");
        linearLayout.setAttribute("android:tag", "left_elements");

        statusBarContents.insertBefore(linearLayout, utils.getFirstChildElement(statusBarContents));

        for (Element element : leftElements){
            statusBarContents.removeChild(element);
            linearLayout.appendChild(element);
        }
        if (leftElements.size() == 0 || !utils.isPushyOutElement(leftElements.get(leftElements.size()-1)))
            linearLayout.appendChild(createViewElement());

        statusBarContents=linearLayout;
    }
}
