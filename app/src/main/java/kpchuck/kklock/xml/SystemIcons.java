package kpchuck.kklock.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;

import kpchuck.kklock.utils.PrefUtils;
import static kpchuck.kklock.constants.PrefConstants.*;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_WIDTH;
import static kpchuck.kklock.constants.XmlConstants.X_WEIGHT;
import static kpchuck.kklock.constants.XmlConstants.X_WRAP_CONTENT;

public class SystemIcons extends XmlBase{

    private boolean batteryIconGone = false;
    private Element battery;

    public SystemIcons(XmlUtils utils, PrefUtils prefUtils, File inFile) throws Exception{
        super(utils, prefUtils, inFile);
    }

    @Override
    public void createWorkCopy() throws Exception {
        super.createWorkCopy();
        setupSystem();
    }

    private void hideBatteryIcon(){
        if(batteryIconGone || !prefUtils.getBool(PREF_HIDE_BATTERY_ICON)) return;

        battery = utils.findElementById(workingCopy, "@*com.android.systemui:id/battery");
        battery.removeAttribute("android:layout_width");
        battery.removeAttribute("android:layout_height");
        battery.setAttribute("android:layout_width", "0.0dip");
        battery.setAttribute("android:layout_height", "0.0dip");
        batteryIconGone = true;
    }

    private void modForMinit(){
        if (!prefUtils.getBool(PREF_MINIT)) return;
        hideBatteryIcon();

        Element minitmod = workingCopy.createElement("com.android.systemui.statusbar.policy.MinitBattery");
        minitmod.setAttribute("android:layout_width", "wrap_content");
        minitmod.setAttribute("android:layout_height", "wrap_content");
        minitmod.setAttribute("android:layout_marginEnd", "7.0dip");

        utils.insertBefore(minitmod, battery);
    }

    private void hideStatusIcons(){
        if(prefUtils.getInt(PREF_MOVE_LEFT) == 1) return;

        NodeList list = getDocumentElement().getElementsByTagName("include");
        Element includeElement = (Element) list.item(0);
        if (includeElement != null)
            utils.changeAttribute(includeElement, X_LAYOUT_WIDTH, "0dip");
        else {
            includeElement = utils.findElementById(workingCopy, "@*com.android.systemui:id/statusIcons");
            if (includeElement.hasAttribute(X_WEIGHT))
                includeElement.removeAttribute(X_WEIGHT);
            if (getDocumentElement().getAttribute(X_LAYOUT_WIDTH).equals("match_parent"))
                utils.changeAttribute(getDocumentElement(), X_LAYOUT_WIDTH, X_WRAP_CONTENT);
        }
    }

    private void setupSystem(){
        hideStatusIcons();
        hideBatteryIcon();
        modForMinit();

    }


}
