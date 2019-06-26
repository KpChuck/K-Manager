package kpchuck.kklock.xml;


import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.FileHelper;
import kpchuck.kklock.utils.PrefUtils;

import static kpchuck.kklock.constants.PrefConstants.PREF_CARRIER_CUSTOM_TEXT;
import static kpchuck.kklock.constants.PrefConstants.PREF_CARRIER_HIDE_NOTIFICATIONS;
import static kpchuck.kklock.constants.PrefConstants.PREF_CHANGE_STATBAR_COLOR;
import static kpchuck.kklock.constants.PrefConstants.PREF_CLOCK_HIDEABLE;
import static kpchuck.kklock.constants.PrefConstants.PREF_CUSTOM_ICON;
import static kpchuck.kklock.constants.PrefConstants.PREF_CUSTOM_ICON_FILE;
import static kpchuck.kklock.constants.XmlConstants.X_FILL_PARENT;
import static kpchuck.kklock.constants.XmlConstants.X_GRAVITY;
import static kpchuck.kklock.constants.XmlConstants.X_ID;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_HEIGHT;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_WIDTH;
import static kpchuck.kklock.constants.XmlConstants.X_WEIGHT;
import static kpchuck.kklock.constants.XmlConstants.X_WRAP_CONTENT;

public class StatusBar extends XmlBase {

    private Element left, right, center;
    private String idStart = "@*com.android.systemui:id/";

    public StatusBar(XmlUtils utils, PrefUtils prefUtils, File document, Context context) throws Exception{
        super(utils, prefUtils, document, context);
    }

    public void createWorkCopy(int clockPosition) throws Exception{
        createWorkCopy();
        if (clockPosition == XmlUtils.CENTER || isNetworkIconCenter() || isCustomIconCenter()){
            setupWithCenter();
        } else {
            setupLeftRight();
        }
        setup();
        if (prefUtils.getBool(R.string.key_hide_statusbar_icons_lockscreen)){
            Element container = createSystemAreaElement();
            container.setAttribute(X_LAYOUT_WIDTH, X_FILL_PARENT);
            moveElementsIntoElement(container, utils.getChildElements(getDocumentElement()));
            getDocumentElement().appendChild(container);
        }
    }

    public void insertLeft(Element element){
        if (!utils.getChildElements(left).isEmpty())
            utils.insertBefore(element, utils.getFirstChildElement(left));
        else
            left.appendChild(element);
    }

    public void insertRight(Element element){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1 && utils.getChildElements(right).size() == 1){
            Element include = utils.getChildElements(right).get(0);
            if (include.getAttribute("layout").equals("@*com.android.systemui:layout/system_icons")){
                include.setAttribute(X_LAYOUT_WIDTH, "0dip");
                include.setAttribute(X_WEIGHT, "1.0");
                include.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
            }
        }
        right.appendChild(element);
    }

    public void insertAfterRight(Element element){
        right.getParentNode().appendChild(element);
    }

    public void insertCenter(Element element){
        center.appendChild(element);
    }

    private void setup() throws Exception{
        hideClock();
        addCustomIcon();
        addCustomTextEverywhere();
        hideNotifications();
        moveNotificationAndNetwork();
        if (prefUtils.getBool(PREF_CHANGE_STATBAR_COLOR)){
            Element statusBarContents = utils.findElementById(workingCopy, idStart + "status_bar_contents");
            String bg = "#" + Integer.toHexString(prefUtils.getInt(R.string.key_new_statusbar_color));
            if (bg.equals("#0")) bg = "#ff000000";
            statusBarContents.setAttribute("android:background", bg);
        }

    }

    private void setupLeftRight(){
        Element statusBarContents = utils.findElementById(workingCopy, idStart + "status_bar_contents");

        left = utils.findElementById(workingCopy, idStart + "status_bar_left_side");

        if (left == null) {
            // Gotta throw everything into a linear layout now, to left of system_icon_area
            Element divider = utils.findElementById(workingCopy, idStart + "system_icon_area");
            left = createLinearContainer("left");
            moveElementsIntoElement(left, utils.getLeftElementsTo(statusBarContents, divider));
            utils.insertBefore(left, divider);
        }
        hideOneHighlightHintViewOOS();

        right = utils.findElementById(workingCopy, idStart + "system_icon_area");
        right.setAttribute("android:gravity", "right");
        right.setAttribute("android:layout_gravity", "right");
    }

    private void setupWithCenter(){
        setupLeftRight();
        if (!(left.getAttribute(X_ID).equals(idStart + "status_bar_left_side") && utils.isWeightedElement((Element) left.getParentNode())))
            makeWeightedElement(left);
        if (prefUtils.getBool(R.string.key_oos_is_bad)) {
            left.removeAttribute(X_WEIGHT);
            left.setAttribute(X_LAYOUT_WIDTH, X_FILL_PARENT);
        }
        makeWeightedElement(right);
        center = createLinearContainer("center");
        unweightElement(center);
        utils.insertBefore(center, right);
    }


    private void hideClock(){
        hideElements(new String[]{"clock", "center_clock", "left_clock", "clock_container", "right_clock_container", "left_clock_container"});
    }

    private void addCustomIcon() throws IOException {
        if (!prefUtils.getBool(PREF_CUSTOM_ICON) && prefUtils.getString(PREF_CUSTOM_ICON_FILE, "").equals(""))
            return;
        FileUtils.copyFile(new File(prefUtils.getString(PREF_CUSTOM_ICON_FILE, "")),
                new File(new FileHelper().newFolder(utils.baseFolders, "res/drawable"), "abc_list_selector_holo_light.png"));

        Element image = workingCopy.createElement("ImageView");
        image.setAttribute(X_LAYOUT_WIDTH, "@*com.android.systemui:dimen/status_bar_icon_size");
        image.setAttribute(X_LAYOUT_HEIGHT, "@*com.android.systemui:dimen/status_bar_icon_size");
        image.setAttribute("android:src", "@*com.android.systemui:drawable/abc_list_selector_holo_light");
        image.setAttribute("android:padding", "3.0dip");
        image.setAttribute("android:scaleType", "centerInside");

        insertForPosition(R.string.key_custom_icon_position, image);
    }

    private void addCustomTextEverywhere() throws Exception{
        if (!prefUtils.getBool(R.string.key_carrier_text_enable) || prefUtils.getBool(R.string.key_custom_text_lockscreen))
            return;

        Element customTextElement = workingCopy.createElement("TextView");
        createCustomTextElement(customTextElement);
        // Insert TextView
        insertLeft(customTextElement);
    }

    private void hideNotifications(){
        if (!prefUtils.getBool(PREF_CARRIER_HIDE_NOTIFICATIONS))
            return;
        hideElement("notification_icon_area");
    }

    private void moveNotificationsToRight(){
        Element networkElement = createNetworkIconsElement();
        Element includeElement = utils.findElementByTagAttr(right, "include", "layout", "@*com.android.systemui:layout/system_icons");
        Element notificationElement = createNotificationsElement();
        notificationElement.setAttribute("android:layoutDirection", "rtl");
        // Insert notifications
        utils.insertBefore(notificationElement, includeElement);
        if (utils.isWeightedElement(networkElement)){
            utils.insertBefore(networkElement, includeElement);
            if (prefUtils.getInt(R.string.key_move_network) != XmlUtils.RIGHT)
                networkElement.removeAttribute(X_WEIGHT);
            combineWeightedTogether(notificationElement, networkElement, false, "statusIcons");
        }
    }

    private void moveNetworkLeft(){
        Element notificationArea = utils.findElementById(workingCopy, idStart + "notification_icon_area");
        Element networkElement = createNetworkIconsElement();

        if (utils.isWeightedElement(networkElement)){
            utils.insertBefore(networkElement, notificationArea);
            combineWeightedTogether(networkElement, notificationArea, true, "statusIcons");
        } else {
            insertLeft(networkElement);
        }
    }

    private void moveNotificationAndNetwork(){
        int notificationPosition = prefUtils.getBool(R.string.key_hide_notifications) ? XmlUtils.LEFT : prefUtils.getInt(R.string
                .key_move_notifications);
        int networkPosition = prefUtils.getInt(R.string.key_move_network);
        Element notificationArea = utils.findElementById(workingCopy, idStart + "notification_icon_area");
        Element networkElement = createNetworkIconsElement();

        if (notificationPosition == networkPosition){
            if (networkPosition == XmlUtils.LEFT){
                moveNetworkLeft();
            } else if (networkPosition == XmlUtils.RIGHT){
                moveNotificationsToRight();
            }
        } else {
            // Else if they're not on same side
            // Do nothing if on left
            if (notificationPosition == XmlUtils.RIGHT) {
                moveNotificationsToRight();
            }
            if (networkPosition == XmlUtils.LEFT){
                moveNetworkLeft();
            } else if (isNetworkIconCenter()){
                insertCenter(networkElement);
            }
        }
        if (notificationPosition == XmlUtils.RIGHT)
            utils.removeElement(notificationArea);

    }

    public void removeClock(){
        for (Element clock : utils.findElementsById(workingCopy, idStart + "clock")) {
            utils.removeElement(clock);
        }
    }

    public Element createClock(boolean stock, boolean wrapInSystemIconArea, boolean addIdStockClock){

        Element textClock;

        if (stock) {
            textClock = workingCopy.createElement("com.android.systemui.statusbar.policy.Clock");
            if (addIdStockClock)
                textClock.setAttribute(X_ID, "@*com.android.systemui:id/clock");
        }
        else {
            textClock = workingCopy.createElement("TextClock");
            textClock.setAttribute("android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
            textClock.setAttribute("android:format24Hour", "@*com.android.systemui:string/keyguard_widget_24_hours_format");
        }

        textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
        textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
        textClock.setAttribute("android:layout_height", "fill_parent");
        textClock.setAttribute("android:singleLine", "true");
        textClock.setAttribute("android:layout_width", X_WRAP_CONTENT);
        textClock.setAttribute("android:gravity", "center");
        textClock.setAttribute("android:paddingStart", "3dp");
        textClock.setAttribute("android:paddingEnd", "3dp");
        String font = prefUtils.getString(R.string.key_clock_font, "roboto-regular").toLowerCase();
        textClock.setAttribute("android:fontFamily", font);

        if (wrapInSystemIconArea){
            Element area = createSystemAreaElement();
            area.appendChild(textClock);
            return area;
        }
        return textClock;
    }

    public Element createSystemAreaElement(){

        boolean oos = prefUtils.getBool(R.string.key_oos_is_bad);
        String tagName = oos ? utils.findElementById(workingCopy, idStart + "system_icon_area").getTagName() : "LinearLayout";
        Element hideyLayout = workingCopy.createElement(tagName);
        hideyLayout.setAttribute("android:layout_width", X_WRAP_CONTENT);
        hideyLayout.setAttribute("android:layout_height", "fill_parent");
        hideyLayout.setAttribute("android:gravity", "center");
        hideyLayout.setAttribute("android:orientation", "horizontal");
        hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");

        return hideyLayout;
    }

    private Element createLinearContainer(String gravity){
        Element container = workingCopy.createElement("LinearLayout");
        container.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
        container.setAttribute(X_LAYOUT_WIDTH, "0dp");
        container.setAttribute(X_WEIGHT, "1.0");
        container.setAttribute("android:tag",  gravity + "_elements");
        container.setAttribute("android:gravity", gravity);
        container.setAttribute("android:layout_gravity", gravity);
        return container;
    }

    private Element createNotificationsElement(){
        Element notificationArea = workingCopy.createElement("com.android.systemui.statusbar.AlphaOptimizedFrameLayout");
        notificationArea.setAttribute("android:orientation", "horizontal");
        notificationArea.setAttribute(X_ID, "@*com.android.systemui:id/notification_icon_area");
        notificationArea.setAttribute(X_LAYOUT_WIDTH, "0.0dip");
        notificationArea.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
        notificationArea.setAttribute(X_WEIGHT, "1.0");
        notificationArea.setAttribute("android:gravity", "right");
        notificationArea.setAttribute("android:layout_gravity", "right");
        return notificationArea;
    }

    private Element createNetworkIconsElement(){
        Element networkElement;

        if (!utils.hasStatusIconContainer(context)) {
            networkElement = workingCopy.createElement("include");
            networkElement.setAttribute("android:layout_width", "wrap_content");
            networkElement.setAttribute("android:layout_height", "fill_parent");
            networkElement.setAttribute("android:paddingEnd", "3dip");
            networkElement.setAttribute("layout", "@*com.android.systemui:layout/signal_cluster_view");
            networkElement.setAttribute("android:gravity", "left");

        } else {
            networkElement = workingCopy.createElement("com.android.systemui.statusbar.phone.StatusIconContainer");
            networkElement.setAttribute("android:id", "@*com.android.systemui:id/statusIcons");
            networkElement.setAttribute(X_LAYOUT_WIDTH, "0dp");
            networkElement.setAttribute(X_WEIGHT, "1.0");
            networkElement.setAttribute(X_LAYOUT_HEIGHT, "match_parent");
            networkElement.setAttribute("android:paddingEnd", "@*com.android.systemui:dimen/signal_cluster_battery_padding");
            networkElement.setAttribute(X_GRAVITY, "left");
            networkElement.setAttribute("android:layout_gravity", "left");
            networkElement.setAttribute("android:orientation", "horizontal");
            networkElement.setAttribute("android:paddingEnd", "3dp");
        }
        return networkElement;
    }

    private void hideOneHighlightHintViewOOS(){
        ArrayList<Element> includes = utils.findElementsByTag(workingCopy, "include");
        for (Element e: includes){
            if (utils.isWeightedElement(e) && e.hasAttribute("layout") && e.getAttribute("layout").equals(idStart + "highlight_hint_view")){
                e.removeAttribute(X_WEIGHT);
            }
        }
    }

    /*
    Puts two elements together in a frame layout
    First one is on left, Second on right
    @param spaceOnRight if true, element on right gets room to expand, if false element on left
    @param idOfNotSpace passes id of non expanding element
     */
    private void combineWeightedTogether(Element first, Element second, boolean spaceOnRight, String idOfNotSpace){
        Element frame = workingCopy.createElement("RelativeLayout");
        frame.setAttribute(X_LAYOUT_WIDTH, "0dip");
        frame.setAttribute(X_WEIGHT, "1");
        frame.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
        utils.insertBefore(frame, first);
        // Change up elements a bit
        first.setAttribute("android:layout_alignParentStart", "true");
        second.setAttribute("android:layout_alignParentEnd", "true");
        if (spaceOnRight)
            second.setAttribute("android:layout_toRightOf", idStart + idOfNotSpace);
        else
            first.setAttribute("android:layout_toLeftOf", idStart + idOfNotSpace);

        for (Element e : new Element[]{first, second}){
            e.setAttribute(X_LAYOUT_WIDTH, X_WRAP_CONTENT);
            utils.removeElement(e);
            e.removeAttribute("android:layout_gravity");
            frame.appendChild(e);
        }
    }

    private void insertForPosition(int positionKey, Element element){
        int position = prefUtils.getInt(positionKey);
        if (position == XmlUtils.LEFT)
            insertLeft(element);
        else if (position == XmlUtils.RIGHT)
            insertRight(element);
        else if (position == XmlUtils.CENTER)
            insertCenter(element);
    }

    private void makeWeightedElement(Element element){
        Element temp = element;
        while (!temp.hasAttribute(X_ID) || !temp.getAttribute(X_ID).equals(idStart + "status_bar_contents")){
            temp.setAttribute(X_WEIGHT, "1");
            temp.setAttribute(X_LAYOUT_WIDTH, "0dp");
            temp = (Element) temp.getParentNode();
        }
    }

    private void unweightElement(Element element){
        element.removeAttribute(X_WEIGHT);
        element.setAttribute(X_LAYOUT_WIDTH, X_WRAP_CONTENT);
    }

    private void moveElementsIntoElement(Element newParent, ArrayList<Element> elements){
        elements.forEach(element -> {
            utils.removeElement(element);
            newParent.appendChild(element);
        });
    }

    private void hideElements(String[] ids){
        for (String id: ids)
            hideElement(id);
    }

    private void hideElement(String id){
        Element e = utils.findElementById(workingCopy, idStart + id);
        for (String attr : new String[]
                {"android:paddingEnd", "android:paddingStart", "android:padding", X_LAYOUT_WIDTH}) {
            if (e != null)
                e.setAttribute(attr, "0dp");
        }
        if (e != null)
            e.removeAttribute(X_WEIGHT);
    }

    private boolean isCustomIconCenter(){
        return prefUtils.getBool(R.string.key_custom_icon) && prefUtils.getInt(R.string.key_custom_icon_position) == XmlUtils.CENTER && !prefUtils.getString(PREF_CUSTOM_ICON_FILE, "").equals("");
    }

    private boolean isNetworkIconCenter(){
        return prefUtils.getInt(R.string.key_move_network) == XmlUtils.CENTER && utils.hasResource(context, "layout", "signal_cluster_view");
    }

}
