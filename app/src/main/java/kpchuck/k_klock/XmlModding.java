package kpchuck.k_klock;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import kpchuck.k_klock.Constants.PrefConstants;
import kpchuck.k_klock.Utils.FileHelper;
import kpchuck.k_klock.Utils.PrefUtils;
import static kpchuck.k_klock.Constants.PrefConstants.*;
import static kpchuck.k_klock.Constants.XmlConstants.*;

/**
 * Created by Karol Przestrzelski on 04/09/2017.
 */

public class XmlModding {

    private Context context;

    String rootFolder = Environment.getExternalStorageDirectory() + "/K-Klock";
    String slash = "/";
   // File mergerFolder = new File(rootFolder + slash + "temp2" + slash + "merge");
    //String rootApkPath = mergerFolder.getAbsolutePath() + "/assets/overlays/com.android.systemui";
    String systemicons = "system_icons.xml";
    String statusbar = "status_bar.xml";
    String romName;
    boolean toMoveLeft;
    boolean toMinit;
    boolean customCarrierText;
    boolean showCarrierTextEverywhere;
    File romFolder;
    String layoutPath;


    public XmlModding(Context context, File romFolder){
        this.context=context;
        this.romFolder=romFolder;
        this.layoutPath = romFolder.getAbsolutePath() + "/assets/overlays/com.android.systemui";
        getPref();
        if (toMoveLeft || toMinit) {
            if (!romName.equals(context.getString(R.string.otherRomsBeta)))
                copySystemIconsAssets("systemicons", romName);
            else copyFromUserInput();

            editSystemIcons();
        }
        if (toMoveLeft || showCarrierTextEverywhere) editStatusBar();
        if (customCarrierText) addCustomCarrierTextToLockscreen();


    }

    private Element createCustomTextElement(Element customTextElement){
        PrefUtils prefUtils = new PrefUtils(context);
        customTextElement.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
        customTextElement.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
        customTextElement.setAttribute(X_GRAVITY, X_GRAVITY_CENTER_VERTICAL);
        customTextElement.setAttribute("android:singleLine", "true");
        customTextElement.setAttribute(X_LAYOUT_HEIGHT, X_FILL_PARENT);
        customTextElement.setAttribute("android:text", prefUtils.getString(PREF_CARRIER_CUSTOM_TEXT, ""));

        if (showCarrierTextEverywhere) {
            customTextElement.setAttribute(X_LAYOUT_WIDTH, X_WRAP_CONTENT);
        }
        else {
            customTextElement.setAttribute("android:layout_toStartOf", "@*com.android.systemui:id/system_icons_super_container");
            customTextElement.setAttribute("android:layout_marginStart", "@*com.android.systemui:dimen/keyguard_carrier_text_margin");
            customTextElement.setAttribute(X_LAYOUT_WIDTH, X_FILL_PARENT);
        }

        return customTextElement;
    }

    private void addCarrierTextToStatusBar(File dir){
        File statusbar = new File(dir.getAbsolutePath() + "/layout/status_bar.xml");
        if (!statusbar.exists()) return;
        PrefUtils prefUtils = new PrefUtils(context);

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(statusbar);

            Element rootElement = doc.getDocumentElement();
            Element statusBarContents = getElementById(rootElement, "LinearLayout", "@*com.android.systemui:id/status_bar_contents");
            Element notificationArea = getElementById(statusBarContents, "com.android.systemui.statusbar.AlphaOptimizedFrameLayout", "@*com.android.systemui:id/notification_icon_area");
            Element customTextElement = doc.createElement("TextView");

            if (prefUtils.getBool(PREF_CARRIER_HIDE_NOTIFICATIONS)) {
                // Hide the notification Icons
                Element hideNotificationLayout = doc.createElement("LinearLayout");
                hideNotificationLayout.setAttribute(X_LAYOUT_WIDTH, "0dip");
                hideNotificationLayout.setAttribute(X_LAYOUT_HEIGHT, "0dip");
                hideNotificationLayout.setAttribute("android:layout_weight", "1.0");
                statusBarContents.insertBefore(hideNotificationLayout, notificationArea);
                statusBarContents.removeChild(notificationArea);
                hideNotificationLayout.appendChild(notificationArea);
            }

            if (prefUtils.getBool(PREF_CARRIER_EVERYWHERE)) {
                customTextElement = createCustomTextElement(customTextElement);

                // Insert TextView
                Element insertBeforeElement = getFirstChildElement(statusBarContents);

                statusBarContents.insertBefore(customTextElement, insertBeforeElement);
            }

            // Write to file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new FileOutputStream(statusbar));
            transformer.transform(source, result);


        }catch (Exception e){
            Log.e("klock", e.getMessage());
        }
    }

    private void addCustomCarrierTextToLockscreen(){
        File rootRom = new File(layoutPath);
        FileHelper fileHelper = new FileHelper();
        PrefUtils prefUtils = new PrefUtils(context);
        for (File dir : rootRom.listFiles(fileHelper.DIRECTORY)){
            addCarrierTextToStatusBar(dir);
            File keyguard = new File(dir.getAbsolutePath() + "/layout/keyguard_status_bar.xml");
            if (keyguard.exists()) {
                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(keyguard);

                    Element rootElement = doc.getDocumentElement();
                    Element carrierTextElement = getElementById(rootElement, "com.android.keyguard.CarrierText",
                            "@*com.android.systemui:id/keyguard_carrier_text");
                    Element customTextElement = doc.createElement("TextView");


                    // Hide the carrier text
                    carrierTextElement.removeAttribute(X_LAYOUT_WIDTH);
                    carrierTextElement.setAttribute(X_LAYOUT_WIDTH, "0dp");


                    if (!prefUtils.getBool(PREF_CARRIER_EVERYWHERE)) {

                        // Create custom textView
                        customTextElement = createCustomTextElement(customTextElement);

                        //Insert TextView
                        rootElement.insertBefore(customTextElement, carrierTextElement);
                    }

                    // Write to file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(doc);

                    StreamResult result = new StreamResult(new FileOutputStream(keyguard));
                    transformer.transform(source, result);


                } catch (Exception e) {

                    Log.e("klock", e.getMessage());
                }

            }
        }
    }

    private void copyFromUserInput(){
        try{
            String userInputPath = rootFolder + "/userInput/";
            File file = new File(userInputPath + systemicons);
            FileUtils.copyFileToDirectory(file, new File(layoutPath + "/res/layout"));

        }catch (IOException e){
            Log.e("klock", e.getMessage());
        }
    }

    private void editStatusBar(){
        File rootFile = new File(layoutPath);
        File[] files = rootFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for(File s: files){
            if (!new File(s.getAbsolutePath() + slash + "layout").exists()) continue;
            File file = new File(s.getAbsolutePath() + slash + "layout/" +  statusbar);
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(file);

                Element resources = doc.getDocumentElement();
                NodeList list = resources.getElementsByTagName("LinearLayout");
                Element layout = null;
                for (int i=0;i<list.getLength();i++){
                    Element layouttemp = (Element) list.item(i);
                    Attr attr = layouttemp.getAttributeNode("android:id");
                    if (attr.getValue().equals("@*com.android.systemui:id/status_bar_contents")) {
                        Log.d("klock", "found statusbar contentd layout");
                        layout = layouttemp;
                    }

                    //  else Log.d("klock", "statusbarcontents layout was null");
                }

                Element insertBeforeElement;

                Node firstNode = layout.getFirstChild();
                while (firstNode.getNodeType() != Node.ELEMENT_NODE) firstNode = firstNode.getNextSibling();

                Element firstElement = (Element) firstNode;
                String firstTag = firstElement.getTagName();

                if (firstTag.equals("LinearLayout") || firstTag.equals("TextClock")){
                    Node nextElement = firstElement.getNextSibling();
                    while (nextElement.getNodeType() != Node.ELEMENT_NODE){
                        nextElement = nextElement.getNextSibling();
                    }
                    insertBeforeElement = (Element) nextElement;
                }
                else {
                    insertBeforeElement = firstElement;
                }


                // Remove center clock and Add Other StatusBarClock
                String xmlpath = file.getAbsolutePath();

                if (xmlpath.contains("Center") || xmlpath.contains("res")) {
                    String TAG = "klock";
                    Log.d(TAG, xmlpath);
                    Element rootElement = doc.getDocumentElement();

                    Element statusbarContents = getElementById(rootElement, "LinearLayout", "@*com.android.systemui:id/status_bar_contents");

                    // Delete Remains of previous
                    Element realSysArea = getElementById(statusbarContents, "com.android.keyguard.AlphaOptimizedLinearLayout", "@*com.android.systemui:id/system_icon_area");
                    realSysArea.removeAttribute("android:layout_weight");
                    realSysArea.removeAttribute("android:layout_width");
                    realSysArea.setAttribute("android:layout_width", "wrap_content");

                    Element viewElement = getElementByAttribute(realSysArea, "View", "android:layout_weight", "1.0");
                    realSysArea.removeChild(viewElement);


                    if (xmlpath.contains("Center")) {
                        Element layoutWithClock = getElementById(statusbarContents, "LinearLayout", "@*com.android.systemui:id/system_icon_area");
                        statusbarContents.removeChild(layoutWithClock);
                    } else if (xmlpath.contains("res")) {
                        Element clock = getElementByAttribute(statusbarContents, "TextClock", "android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
                        statusbarContents.removeChild(clock);
                        Element linearstart = getElementById(rootElement, "LinearLayout", "@*com.android.systemui:id/system_icon_area");
                        rootElement.removeChild(linearstart);
                    }


                    //Make the new status bar clock


                    Element hideyLayout = doc.createElement("LinearLayout");
                    hideyLayout.setAttribute("android:layout_width", "fill_parent");
                    hideyLayout.setAttribute("android:layout_height", "fill_parent");
                    hideyLayout.setAttribute("android:gravity", "center");
                    hideyLayout.setAttribute("android:orientation", "horizontal");
                    hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");

                    //Creating textclock
                    Element textClock = doc.createElement("TextClock");
                    textClock.setAttribute("android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
                    textClock.setAttribute("android:format24Hour", "@*com.android.systemui:string/keyguard_widget_24_hours_format");
                    textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
                    textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
                    textClock.setAttribute("android:layout_width", "fill_parent");
                    textClock.setAttribute("android:layout_height", "fill_parent");
                    textClock.setAttribute("android:gravity", "center");
                    textClock.setAttribute("android:singleLine", "true");
                    if (xmlpath.contains("Dynamic") && xmlpath.contains("Center")) textClock.setAttribute("android:id", "@*com.android.systemui:id/clock");

                    rootElement.insertBefore(hideyLayout, getFirstChildElement(rootElement));

                    if (xmlpath.contains("Center") && xmlpath.contains("Stock")) {
                        //Creating textclock
                        Element stockClock = doc.createElement("com.android.systemui.statusbar.policy.Clock");
                        stockClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
                        stockClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
                        stockClock.setAttribute("android:layout_width", "fill_parent");
                        stockClock.setAttribute("android:layout_height", "fill_parent");
                        stockClock.setAttribute("android:gravity", "center");
                        stockClock.setAttribute("android:singleLine", "true");
                        stockClock.setAttribute("android:id", "@*com.android.systemui:id/clock");

                        rootElement.insertBefore(hideyLayout, getFirstChildElement(rootElement));
                        hideyLayout.appendChild(stockClock);
                    } else if (xmlpath.contains("Center")) {

                        hideyLayout.appendChild(textClock);

                    } else if (xmlpath.contains("res")) {
                        rootElement.insertBefore(textClock, hideyLayout);
                    }
                }

                if (toMoveLeft) {
                    Element toInclude = doc.createElement("include");
                    toInclude.setAttribute("android:layout_width", "wrap_content");
                    toInclude.setAttribute("android:layout_height", "fill_parent");
                    toInclude.setAttribute("android:layout_marginStart", "2.5dip");
                    toInclude.setAttribute("layout", "@*com.android.systemui:layout/signal_cluster_view");
                    toInclude.setAttribute("android:gravity", "center_vertical");

                    layout.insertBefore(toInclude, insertBeforeElement);
                }

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);

                StreamResult result = new StreamResult(new FileOutputStream(file));
                transformer.transform(source, result);
            }catch (Exception e){
                Log.e("klock", e.getMessage());

            }

        }

    }

    public static Element getFirstChildElement(Node parent) {
        Element myElement = null;
        NodeList childs = parent.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                myElement = (Element) child;
                break;
            }else {
                myElement = null;
            }
        }
        return myElement;
    }

    public Element getElementById (Element parentElement, String layoutTag, String idName){

        return getElementByAttribute(parentElement, layoutTag, "android:id", idName);

    }

    public Element getElementByAttribute (Element parentElement, String layoutTag, String attributeName, String idName){

        NodeList list = parentElement.getElementsByTagName(layoutTag);
        Element layout = null;
        for (int i=0; i<list.getLength(); i++){
            layout = (Element) list.item(i);
            Attr attr = layout.getAttributeNode(attributeName);
            if (attr.getValue().equals(idName)) break;
            else layout = null;
        }
        if (layout == null){
            Log.e("klock", "Layout is equal to null" + layoutTag);
            return null;
        }
        return layout;

    }

    private void editSystemIcons(){
        File sysicons = new File(layoutPath + "/res/layout/" + systemicons);
        sysicons.mkdirs();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(sysicons);
            replaceStuffInXml(sysicons.getAbsolutePath(), "@", "@*com.android.systemui:");

            if (toMinit){
                Element rootElement = doc.getDocumentElement();
                NodeList list = rootElement.getElementsByTagName("com.android.systemui.BatteryMeterView");
                Element battery = (Element) list.item(0);
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

            if(toMoveLeft) {
                Element rootElement = doc.getDocumentElement();
                NodeList list = rootElement.getElementsByTagName("include");
                Element includeElement = (Element) list.item(0);

                rootElement.removeChild(includeElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new FileOutputStream(sysicons));
            transformer.transform(source, result);

        }catch (Exception e){
            Log.e("klock", e.getMessage());

        }

    }

    public void replaceStuffInXml(String xml, String old, String news){
        try {
            File file = new File(xml);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
            output = output.replace(old, news);
            doc = stringToDom(output);
            doc.normalizeDocument();

            DOMSource source = new DOMSource(doc);

            if (file.exists()) FileUtils.forceDelete(file);

            StreamResult result = new StreamResult(new FileOutputStream(file));
            transformer.transform(source, result);

        }catch (Exception e){Log.e("klock", e.getMessage());

        }
    }

    public static Document stringToDom(String xmlSource)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
    }


    private void getPref(){
        PrefUtils prefUtils = new PrefUtils(context);
        String rom = prefUtils.getString("selectedRom", context.getString(R.string.chooseRom));
        this.romName=rom;
        this.toMoveLeft=prefUtils.getBool("moveLeftPref");
        this.toMinit=prefUtils.getBool("minitPref");
        this.customCarrierText = prefUtils.getBool(PREF_CARRIER_TEXT);
        this.showCarrierTextEverywhere = prefUtils.getBool(PREF_CARRIER_EVERYWHERE);
    }

    private void copySystemIconsAssets(String assetDir, String whichString) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(assetDir);
        } catch (IOException e) {
            android.util.Log.e("tag", "Failed to get asset file list.", e);

        }
        if (files != null) for (String filename : files) {
            if(filename.equals(whichString)){
                java.io.InputStream in = null;
                java.io.OutputStream out = null;
                try {
                    in = assetManager.open(assetDir + slash + filename + slash + systemicons);
                    FileHelper fileHelper = new FileHelper();
                    fileHelper.newFolder(layoutPath + "/res/layout");
                    File outFile = new File(layoutPath + "/res/layout/" + systemicons);
                    out = new java.io.FileOutputStream(outFile);
                    copyFile(in, out);
                } catch(IOException e) {
                    android.util.Log.e("tag", "Failed to copy asset file: " + filename, e);
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }}
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
