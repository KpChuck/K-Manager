package kpchuck.kklock.xml;

import android.content.Context;
import android.os.Build;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.FileHelper;
import kpchuck.kklock.utils.PrefUtils;

import static kpchuck.kklock.constants.PrefConstants.DEV_HIDE_CLOCK;
import static kpchuck.kklock.constants.PrefConstants.PREF_CARRIER_EVERYWHERE;
import static kpchuck.kklock.constants.PrefConstants.PREF_CARRIER_HIDE_NOTIFICATIONS;
import static kpchuck.kklock.constants.PrefConstants.PREF_CARRIER_TEXT;
import static kpchuck.kklock.constants.PrefConstants.PREF_CHANGE_STATBAR_COLOR;
import static kpchuck.kklock.constants.PrefConstants.PREF_CLOCK_HIDEABLE;
import static kpchuck.kklock.constants.PrefConstants.PREF_CUSTOM_ICON;
import static kpchuck.kklock.constants.PrefConstants.PREF_CUSTOM_ICON_FILE;
import static kpchuck.kklock.constants.PrefConstants.PREF_MOVE_LEFT;
import static kpchuck.kklock.constants.PrefConstants.PREF_MOVE_NOTIFICATIONS_RIGHT;
import static kpchuck.kklock.constants.PrefConstants.PREF_STATBAR_COLOR;
import static kpchuck.kklock.constants.XmlConstants.X_FILL_PARENT;
import static kpchuck.kklock.constants.XmlConstants.X_GRAVITY;
import static kpchuck.kklock.constants.XmlConstants.X_ID;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_HEIGHT;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_WIDTH;
import static kpchuck.kklock.constants.XmlConstants.X_WEIGHT;
import static kpchuck.kklock.constants.XmlConstants.X_WRAP_CONTENT;

public class StatusBar extends XmlBase{

    private Document status;

    private Element statusBarContents;
    private Element systemIconArea;
    private boolean isCentered = false;
    private Element leftPushyElement;

    private boolean debug = false;
    private int counter = 0;
    private String[] colors = new String[]{
            "#ff00ff00", // green
            "#ffff0000", // red
            "#ff0000ff", // blue
            "#ff00ffff", // cyan
            "#ffff00ff", // magenta/pink
            "#ffffff00", // yellow
            "#ffffffff", // white
            "#ff444444", // black
            "#ffbbbbbb"  // grey
    };

    public StatusBar(XmlUtils utils, PrefUtils prefUtils, File document, Context context) throws Exception{
        super(utils, prefUtils, document, context);
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

        if (debug){
            setDebug((statusBarContents));
            setDebug(systemIconArea);
        }

        fixForLg(status, true);

        addCustomIcon();

        if (prefUtils.getBool(getString(R.string.key_hide_carrier_text)))
            hideCarrierText();

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

    public void insertCenter(Element element) throws Exception{
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

    private void hideCarrierText(){
        String[] ids = new String[]{"zte_barlabels", "zte_barlabel", "bar_sim_one", "bar_sim_two"};
        Element carrierText;
        int c = 0;
        for (String id: ids) {
            carrierText = utils.findElementById(status, "@*com.android.systemui:id/" + id);
            if (carrierText == null)
                continue;
            utils.changeAttribute(carrierText, X_LAYOUT_WIDTH, "0dp");
            utils.changeAttribute(carrierText, X_WEIGHT, "0");
            utils.changeAttribute(carrierText, "android:addStatesFromChildren", "false");
            utils.changeAttribute(carrierText, "android:clipChildren", "true");
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
        if (debug)
            setDebug(textClock);

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

    public void center() throws Exception{
        String system_icon_area_id = "@*com.android.systemui:id/system_icon_area";
        ArrayList<Element> rightElements = utils.getRightElementsTo(statusBarContents, "", system_icon_area_id);
        ArrayList<Element> leftElements = utils.getLeftElementsTo(statusBarContents, "", system_icon_area_id);
        boolean rightElementsIsEmpty = rightElements.isEmpty();
        packRightOf(rightElements);
        packLeftOf(leftElements, rightElementsIsEmpty);
        isCentered = true;
    }

    private void hideStockClock() {
        List<Element> clock_elements = new ArrayList<>();
        for (String s : new String[]{"clock", "center_clock", "left_clock", "clock_container"}){
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
            if (debug)
                setDebug(image);

           insertLeft(image);
        }
    }

    private void addCustomTextEverywhere() throws Exception{

        Element customTextElement = status.createElement("TextView");
        createCustomTextElement(customTextElement);
        if (debug)
            setDebug(customTextElement);

        // Insert TextView
        insertLeft(customTextElement);
    }

    private void moveNetworkLeft() throws Exception{
        int position = prefUtils.getInt(PREF_MOVE_LEFT);
        if (position == 1) return; // Already on right

        Element toInclude;
        if (utils.hasResource(context, "layout", "signal_cluster_view")) {
            toInclude = status.createElement("include");
            toInclude.setAttribute("android:layout_width", "wrap_content");
            toInclude.setAttribute("android:layout_height", "fill_parent");
            toInclude.setAttribute("android:layout_marginStart", "2.5dip");
            toInclude.setAttribute("layout", "@*com.android.systemui:layout/signal_cluster_view");
            toInclude.setAttribute("android:gravity", "center_vertical");
            if (debug)
                setDebug(toInclude);
        } else {
            if (position == 2)
                return;
            toInclude = status.createElement("com.android.systemui.statusbar.phone.StatusIconContainer");
            toInclude.setAttribute("android:id", "@*com.android.systemui:id/statusIcons");
            toInclude.setAttribute(X_LAYOUT_WIDTH, "0dp");
            toInclude.setAttribute(X_WEIGHT, "1.0");
            toInclude.setAttribute(X_LAYOUT_HEIGHT, "match_parent");
            toInclude.setAttribute("android:paddingEnd", "@*com.android.systemui:dimen/signal_cluster_battery_padding");
            toInclude.setAttribute(X_GRAVITY, "center_vertical");
            toInclude.setAttribute("android:layoutDirection", "rtl");
            toInclude.setAttribute("android:orientation", "horizontal");
            if (debug)
                setDebug(toInclude);

            if (prefUtils.getInt(PREF_MOVE_NOTIFICATIONS_RIGHT) == position){
                Element notification = utils.findElementById(status,
                        "@*com.android.systemui:id/notification_icon_area");
                if (debug)
                    setDebug(notification);
                Element relativeLayout = status.createElement("RelativeLayout");
                if (debug)
                    setDebug(relativeLayout);
                relativeLayout.setAttribute(X_LAYOUT_WIDTH, "0dp");
                relativeLayout.setAttribute(X_WEIGHT, "1.0");
                relativeLayout.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
                utils.changeAttribute(toInclude, X_LAYOUT_WIDTH, X_WRAP_CONTENT);
                utils.changeAttribute(notification, X_LAYOUT_WIDTH, X_WRAP_CONTENT);
                notification.getParentNode().insertBefore(relativeLayout, notification);
                toInclude.removeAttribute("android:layoutDirection");
                toInclude.setAttribute("android:layout_alignParentStart", "true");
                notification.setAttribute("android:layout_toRightOf", "@*com.android.systemui:id/statusIcons");
                notification.setAttribute("android:layout_alignParentEnd", "true");
                relativeLayout.appendChild(toInclude);
                relativeLayout.appendChild(notification);
            }
            else {
                Element rightMostElement;
                rightMostElement = utils.findElementById(status, "@*com.android.systemui:id/status_bar_left_side");
                if (rightMostElement != null) {
                    rightMostElement.appendChild(toInclude);
                    return;
                }

                // Either before cutout space or system_icon_area
                rightMostElement = utils.findElementById(status, "@*com.android.systemui:id/cutout_space_view");
                if (rightMostElement == null){
                    rightMostElement = utils.findElementById(status, "@*com.android.systemui:id/system_icon_area");
                }

                rightMostElement.getParentNode().insertBefore(toInclude, rightMostElement);
            }
            return;
        }

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
        notificationArea.setAttribute("android:layoutDirection", "rtl");
        if (debug)
            setDebug(notificationArea);

        utils.changeAttribute(systemIconArea, X_WEIGHT, "1.0");
        utils.changeAttribute(systemIconArea, X_LAYOUT_WIDTH, "0dp");

        insertInnerRight(notificationArea);

    }

    private Element createViewElement(){
        Element view = status.createElement("View");
        view.setAttribute("android:visibility", "invisible");
        view.setAttribute(X_LAYOUT_WIDTH, "0.0dip");
        view.setAttribute(X_LAYOUT_HEIGHT, "fill_parent");
        view.setAttribute(X_WEIGHT, "1.0");

        return view;
    }

    private void packRightOf(ArrayList<Element> rightElements) throws Exception{

        // Workaround for Miui
        if (rightElements.size() == 0){
            List<Element> parents = utils.findElementsById(status, "@*com.android.systemui:id/system_icon_area");
            Element parent = (Element) parents.get(parents.size()-1).getParentNode();
            if (utils.isPushyOutElement(parent))
                rightElements.add(parent);
        }

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

        // Gotta check if first element and all its first children are pushy elements
        Element firstCheck = rightElements.get(0);
        boolean hasPushyElement = utils.isPushyOutElement(firstCheck);
        boolean insertInside = false;
        while (firstCheck.hasChildNodes()){
            Element check = utils.getFirstChildElement(firstCheck);
            if (check == null) break;
            firstCheck = check;
            hasPushyElement = utils.isPushyOutElement(firstCheck);
            insertInside = true;
        }
        // If there aren't right elements insert
        // or if the first one hasnt got layout_width and weight
        // And it isn't moving notifications right
        // Create layout with android:layout_width
        if (rightElements.size() == 0 || (
                !hasPushyElement
                        && prefUtils.getInt(PREF_MOVE_NOTIFICATIONS_RIGHT) != 1)){
            leftPushyElement = createViewElement();
            if (insertInside)
                utils.insertBefore(leftPushyElement, firstCheck);
            else
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

    private void packLeftOf(ArrayList<Element> leftElements, boolean rightElementsEmpty){

        // Workaround for Miui
        if (rightElementsEmpty){
            while (leftElements.size() != 1)
                leftElements.remove(leftElements.size()-1);
        }

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

        // Gotta check if first element and all its first children are pushy elements
        Element firstCheck = leftElements.get(leftElements.size()-1);
        boolean hasPushyElement = utils.isPushyOutElement(firstCheck);
        while (firstCheck.hasChildNodes()){
            firstCheck = utils.getLastChildElement(firstCheck);
            hasPushyElement = utils.isPushyOutElement(firstCheck);
        }
        // If no elements insert
        // OR if the last element doesnt have a weight and notifications or the status icons with a weight arent present
        if (leftElements.size() == 0 ||
                (!hasPushyElement)
                    && (prefUtils.getInt(PREF_MOVE_NOTIFICATIONS_RIGHT) != 0
                        || (prefUtils.getInt(PREF_MOVE_LEFT) != 0
                                && !utils.hasResource(context, "layout", "signal_cluster_view"))))
            linearLayout.appendChild(createViewElement());

        statusBarContents=linearLayout;
    }

    private boolean isViewElement(Element testElement){
        Element viewElement = createViewElement();
        if (!viewElement.getTagName().equals(testElement.getTagName()))
            return false;
        NamedNodeMap map = viewElement.getAttributes();
        for (int i=0; i<map.getLength(); i++){
            Node node = map.item(i);
            if (node.getNodeType() == Node.ATTRIBUTE_NODE){
                Attr attr = (Attr) node;
                if (!testElement.hasAttribute(attr.getName()))
                    return false;
                Attr testAttr = testElement.getAttributeNode(attr.getName());
                if (!testAttr.getValue().equals(attr.getValue()))
                    return false;
            }
        }
        return true;
    }

    private void setDebug(Element element){
        utils.changeAttribute(element, "android:background", colors[counter]);
        counter++;
        if (counter == colors.length){
            counter = 0;
        }
    }
}
