package kpchuck.kklock.xml;

import android.content.Context;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.PrefUtils;

import static kpchuck.kklock.constants.PrefConstants.PREF_CARRIER_CUSTOM_TEXT;
import static kpchuck.kklock.constants.PrefConstants.PREF_CARRIER_EVERYWHERE;
import static kpchuck.kklock.constants.PrefConstants.PREF_HTML_CUSTOM_TEXT;
import static kpchuck.kklock.constants.XmlConstants.X_FILL_PARENT;
import static kpchuck.kklock.constants.XmlConstants.X_GRAVITY;
import static kpchuck.kklock.constants.XmlConstants.X_GRAVITY_CENTER_VERTICAL;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_HEIGHT;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_WIDTH;
import static kpchuck.kklock.constants.XmlConstants.X_WEIGHT;
import static kpchuck.kklock.constants.XmlConstants.X_WRAP_CONTENT;

public class XmlBase{

    public XmlUtils utils;
    public PrefUtils prefUtils;
    public final Document document;
    public Document workingCopy;
    public Context context;
    public String fontStyle = "";
    public String fontType = "";
    public String idStart = "@*com.android.systemui:id/";


    public XmlBase(XmlUtils utils, PrefUtils prefUtils, File inFile, Context context) throws Exception{
        this.utils = utils;
        this.prefUtils = prefUtils;
        this.context = context;

        Document document = utils.getDocument(inFile);
        document = utils.replaceAt(document);
        utils.fixUpForAttrs(document);
        this.document=document;

        initFonts();
        createWorkCopy();
    }

    public void writeDocument(File outFile) throws Exception{
        utils.writeDocToFile(workingCopy, outFile);
    }

    private void initFonts(){
        String[] fonts = prefUtils.getString(R.string.key_clock_font, "roboto-regular ").toLowerCase().split(" ", 2);
        if (fonts.length < 2)
            fontStyle = "normal";
        else
            fontStyle = fonts[1].replace(" ", "|");
        fontType = fonts[0].toLowerCase();
    }

    public String getString(int id){
        return context.getString(id);
    }

    public void createWorkCopy() throws Exception{
        this.workingCopy = utils.cloneDocument(document);
    }

    public Element getDocumentElement(){
        return workingCopy.getDocumentElement();
    }

    public void insertAtRoot(Element element){
        utils.insertBefore(element, utils.getFirstChildElement(getDocumentElement()));
    }

    public void createCustomTextElement(Element customTextElement) throws Exception{
        customTextElement.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
        customTextElement.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
        customTextElement.setAttribute(X_GRAVITY, X_GRAVITY_CENTER_VERTICAL);
        customTextElement.setAttribute("android:singleLine", "true");
        customTextElement.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
        customTextElement.setAttribute("android:textStyle", fontStyle);
        customTextElement.setAttribute("android:fontFamily", fontType);

        //String hijack_name = "legacy_vpn_name";
        String hijack_name = "qs_paging";
        customTextElement.setAttribute("android:text", "@*com.android.systemui:string/" + hijack_name);
        File stringsF = new File(utils.baseFolders, "res/values/");
        stringsF.mkdirs();
        Log.d("klock", prefUtils.getString(PREF_CARRIER_CUSTOM_TEXT, ""));
        new XmlCreation().createStringDoc(new File(stringsF, "strings.xml"), hijack_name,
                prefUtils.getString(PREF_CARRIER_CUSTOM_TEXT, ""));

        if (prefUtils.getBool(PREF_CARRIER_EVERYWHERE)) {
            customTextElement.setAttribute(X_LAYOUT_WIDTH, X_WRAP_CONTENT);
            customTextElement.setAttribute("android:paddingEnd", "3dp");
        }
        else {
            customTextElement.setAttribute("android:layout_toStartOf", "@*com.android.systemui:id/system_icons_super_container");
            customTextElement.setAttribute("android:layout_marginStart", "@*com.android.systemui:dimen/keyguard_carrier_text_margin");
            customTextElement.setAttribute(X_LAYOUT_WIDTH, X_FILL_PARENT);
        }
    }

    public void fixForLg(Document doc, boolean isStatusBar){

        Element two = utils.findElementByTag(doc, "com.lge.systemui.widget.StatusIconAnimatorView");
        Element one = utils.findElementByTag(doc, "com.lge.systemui.widget.StatusIconsLinearLayout");
        if (two == null && one == null) return;

        if (isStatusBar){
            Element system_icon_area = utils.findElementById(doc, "@*com.android.systemui:id/system_icon_area");
            for (Element s: new Element[]{two, one}) {
                if (s == null) continue;
                s.getParentNode().removeChild(s);
                system_icon_area.insertBefore(s, utils.getFirstChildElement(system_icon_area));
            }
        }
        else {
            one = utils.findElementByTag(doc, "com.lge.systemui.widget.StatusIconsLinearLayout");
            for (Element s: new Element[]{one, two}){
                if (s == null) continue;
                utils.changeAttribute(s, X_LAYOUT_WIDTH, "0dip");
                utils.changeAttribute(s, "android:visibility", "gone");
            }
        }
    }

    void hideClock(){
        if (prefUtils.getInt(R.string.key_clock_position) != XmlUtils.USE_SYSTEM)
            hideElements(new String[]{"clock", "center_clock", "left_clock", "clock_container",
                "right_clock_container", "left_clock_container", "notch_clock_stub", "keyguard_clock"});
    }

    private void hideElements(String[] ids){
        for (String id: ids)
            hideElement(id);
    }

    void hideElement(String id) {
        Element e = utils.findElementById(workingCopy, idStart + id);
        if (e == null) return;
        for (String attr : new String[]
                {"android:paddingEnd", "android:paddingStart", "android:padding", X_LAYOUT_WIDTH}) {
            e.setAttribute(attr, "0dp");
        }
        e.removeAttribute(X_WEIGHT);
        e.removeAttribute("android:layout");
    }

}
