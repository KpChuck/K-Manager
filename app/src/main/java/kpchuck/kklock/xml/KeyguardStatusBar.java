package kpchuck.kklock.xml;

import android.os.Build;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.List;

import kpchuck.kklock.utils.PrefUtils;

import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_WIDTH;
import static kpchuck.kklock.constants.PrefConstants.*;

public class KeyguardStatusBar extends XmlBase {

    private Element carrierTextElement;

    public KeyguardStatusBar(XmlUtils utils, PrefUtils prefUtils, File document) throws Exception{
        super(utils, prefUtils, document);
    }

    @Override
    public void createWorkCopy() throws Exception {
        super.createWorkCopy();
        setupKeyguard();
    }

    public void writeDocuments(List<String> folders) throws Exception{
        for (String s : folders) {
            if (new File(utils.baseFolders, s).exists())
                writeDocument(new File(utils.baseFolders, s + "/layout/keyguard_status_bar.xml"));
        }
    }

    public void addCustomTextToLockscreen() throws Exception{
        Element customTextElement = workingCopy.createElement("TextView");
        // Create custom textView
        createCustomTextElement(customTextElement);

        utils.insertBefore(customTextElement, carrierTextElement);
    }

    public void hideCarrierText(){

        utils.changeAttribute(carrierTextElement, X_LAYOUT_WIDTH, "0dip");
    }

    public void setBackground(String color){
        utils.changeAttribute(getDocumentElement(), "android:background", color);
    }

    public void hideStatusIcons(){
        Element superContainer = utils.findElementById(getDocumentElement(),
                "@*com.android.systemui:id/system_icons_super_container");
        if (superContainer == null && Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1)
            superContainer = utils.findElementById(workingCopy, "@*com.android.systemui:id/status_icon_area");

        utils.changeAttribute(superContainer, X_LAYOUT_WIDTH, "0dip");
        superContainer.setAttribute("android:visibility", "gone");
        fixForLg(workingCopy, false);
    }

    private void getCarrierTextLike(){
        Element carrierTextElement = utils.findElementById(workingCopy, "@*com.android.systemui:id/keyguard_carrier_text");
        if (carrierTextElement == null)
            carrierTextElement = utils.findElementLikeId(workingCopy, "@*com.android.systemui:id/keyguard_carrier_text");
        this.carrierTextElement=carrierTextElement;
    }

    private void setupKeyguard() throws Exception{
        getCarrierTextLike();
        utils.changeAttribute(carrierTextElement, "android:textColor", "#ffffffff");
        utils.changeAttribute(carrierTextElement, "android:textAppearance", "?android:textAppearanceSmall");
        getCarrierTextLike();

        if ((prefUtils.getBool(PREF_CARRIER_TEXT) && prefUtils.getBool(PREF_CARRIER_EVERYWHERE))) {
            addCustomTextToLockscreen();
            hideCarrierText();
        }
        else if (prefUtils.getInt(PREF_MOVE_LEFT) == 0) {
            hideCarrierText();
        }
        if (prefUtils.getBool(PREF_BLACKOUT_LOCKSCREEN)){
            setBackground("#ff000000");
        }

    }
}