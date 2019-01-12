package kpchuck.kklock.xml;

import android.content.Context;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.PrefUtils;

import static kpchuck.kklock.constants.PrefConstants.PREF_HIDE_BATTERY_ICON;
import static kpchuck.kklock.constants.PrefConstants.PREF_MINIT;
import static kpchuck.kklock.constants.PrefConstants.PREF_MOVE_LEFT;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_WIDTH;
import static kpchuck.kklock.constants.XmlConstants.X_WEIGHT;
import static kpchuck.kklock.constants.XmlConstants.X_WRAP_CONTENT;

public class SystemIcons extends XmlBase{

    private boolean batteryIconGone = false;
    private Element battery;

    public SystemIcons(XmlUtils utils, PrefUtils prefUtils, File inFile, Context context) throws Exception{
        super(utils, prefUtils, inFile, context);
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
        if(prefUtils.getInt(PREF_MOVE_LEFT) == XmlUtils.RIGHT &&
                prefUtils.getInt(R.string.key_move_notifications) != XmlUtils.RIGHT) return;

        NodeList list = getDocumentElement().getElementsByTagName("include");
        Element includeElement = (Element) list.item(0);
        if (includeElement != null) {
            if (prefUtils.getInt(R.string.key_move_network) != XmlUtils.RIGHT)
                utils.changeAttribute(includeElement, X_LAYOUT_WIDTH, "0dip");
            return;
        }
        includeElement = utils.findElementById(workingCopy, "@*com.android.systemui:id/statusIcons");
        includeElement.removeAttribute(X_WEIGHT);
        if (prefUtils.getInt(R.string.key_move_notifications) == XmlUtils.RIGHT)
            getDocumentElement().setAttribute(X_LAYOUT_WIDTH, X_WRAP_CONTENT);
        else
            utils.insertBefore(utils.createViewElement(workingCopy), includeElement);

    }

    private void setupSystem(){
        hideStatusIcons();
        hideBatteryIcon();
        modForMinit();

    }


}
