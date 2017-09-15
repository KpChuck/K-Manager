package kpchuck.k_klock;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.StringWriter;

/**
 * Created by Karol Przestrzelski on 15/08/2017.
 */

public class editStatusBar{


    Context context;
    OtherRomsHandler handler = new OtherRomsHandler(context);

    String slash = "/";

    String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock";
    String xmlFolder = rootFolder + slash + "userInput/";
    String tag = "klock";


    String rootApk = rootFolder + slash + "temp2" + slash + "merge" + "/assets/overlays/com.android.systemui/";

    public File newFolder(String filePath){
        File folder = new File(filePath);
        if (!folder.exists() || !folder.isDirectory())folder.mkdirs();
        return folder;
    }



    public void Execution(Context context){
        this.context=context;
        editCenterNotOnLockscreen();
        editCenterOnLockscreen();
        editLeftNotOnLockscreen();
        editLeftOnLockscreen();
        editRightNotOnLockscreen();
        editRightOnLockscreen();
    }

    public void editRightNotOnLockscreen(){

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
            doc = handler.replaceAt(doc);

            Element rootElement = doc.getDocumentElement();

            Element textClock = doc.createElement("TextClock");
            textClock.setAttribute("android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
            textClock.setAttribute("android:format24Hour", "@*com.android.systemui:string/keyguard_widget_24_hours_format");
            textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
            textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
            textClock.setAttribute("android:layout_width", "wrap_content");
            textClock.setAttribute("android:layout_height", "fill_parent");
            textClock.setAttribute("android:gravity", "center");
            textClock.setAttribute("android:singleLine", "true");
            textClock.setAttribute("android:layout_alignParentBottom", "true");




            Element hideyLayout = doc.createElement("LinearLayout");
            hideyLayout.setAttribute("android:layout_width", "fill_parent");
            hideyLayout.setAttribute("android:layout_height", "fill_parent");
            hideyLayout.setAttribute("android:gravity", "center");
            hideyLayout.setAttribute("android:orientation", "horizontal");
            hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");

            NodeList nodeList = rootElement.getElementsByTagName("LinearLayout");
            if(nodeList.getLength() ==1){
                Element secondRootElement = (Element) nodeList.item(0);
                NodeList anotherNodeList = secondRootElement.getElementsByTagName("com.android.keyguard.AlphaOptimizedLinearLayout");
                if(anotherNodeList.getLength()==1){
                    Element thirdElement = (Element) anotherNodeList.item(0);
                    NodeList stockClock = thirdElement.getElementsByTagName("com.android.systemui.statusbar.policy.Clock");
                    if(stockClock.getLength()==1){
                        Node firstChild = stockClock.item(0);
                        thirdElement.insertBefore(hideyLayout, firstChild);
                        hideyLayout.appendChild(textClock);

                    }
                }
            }


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(rootApk + "type2_No_Clock_on_Lockscreen_Right/layout/" + "status_bar.xml"));
            transformer.transform(source, result);
        }catch (Exception e){
            Log.e(tag, e.getMessage());

        }
    }


    public void editLeftNotOnLockscreen(){

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
            doc = handler.replaceAt(doc);

            Element rootElement = doc.getDocumentElement();

            Element textClock = doc.createElement("TextClock");
            textClock.setAttribute("android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
            textClock.setAttribute("android:format24Hour", "@*com.android.systemui:string/keyguard_widget_24_hours_format");
            textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
            textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
            textClock.setAttribute("android:layout_width", "wrap_content");
            textClock.setAttribute("android:layout_height", "fill_parent");
            textClock.setAttribute("android:gravity", "left|center");
            textClock.setAttribute("android:singleLine", "true");




            Element hideyLayout = doc.createElement("LinearLayout");
            hideyLayout.setAttribute("android:layout_width", "wrap_content");
            hideyLayout.setAttribute("android:layout_height", "fill_parent");
            hideyLayout.setAttribute("android:gravity", "left");
            hideyLayout.setAttribute("android:orientation", "horizontal");
            hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");

            NodeList startingElement = rootElement.getElementsByTagName("LinearLayout");
            if(startingElement.getLength()==1) {
                Element startElement = (Element) startingElement.item(0);
                Node firstChild = startElement.getFirstChild();
                startElement.insertBefore(hideyLayout, firstChild);
                hideyLayout.appendChild(textClock);
            }else Log.d("error", "two elemnts with linearLayout");


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(rootApk + "type2_No_Clock_on_Lockscreen_Left/layout/" + "status_bar.xml"));
            transformer.transform(source, result);
        }catch (Exception e){
            Log.d("hi",e.getMessage());
        }
    }

    public void editCenterNotOnLockscreen(){

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
            doc = handler.replaceAt(doc);


            Element rootElement = doc.getDocumentElement();

            Element textClock = doc.createElement("TextClock");
            textClock.setAttribute("android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
            textClock.setAttribute("android:format24Hour", "@*com.android.systemui:string/keyguard_widget_24_hours_format");
            textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
            textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
            textClock.setAttribute("android:layout_width", "fill_parent");
            textClock.setAttribute("android:layout_height", "fill_parent");
            textClock.setAttribute("android:gravity", "center");
            textClock.setAttribute("android:singleLine", "true");

            Node firstElement = rootElement.getFirstChild();

            Element hideyLayout = doc.createElement("LinearLayout");
            hideyLayout.setAttribute("android:layout_width", "fill_parent");
            hideyLayout.setAttribute("android:layout_height", "fill_parent");
            hideyLayout.setAttribute("android:gravity", "center");
            hideyLayout.setAttribute("android:orientation", "horizontal");
            hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");
            hideyLayout.appendChild(textClock);

            rootElement.insertBefore(hideyLayout, firstElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(rootApk + "type2_No_Clock_on_Lockscreen_Center/layout/" + "status_bar.xml"));
            transformer.transform(source, result);
        }catch (Exception e){
            Log.d("hi",e.getMessage());
        }
    }


    public void editRightOnLockscreen(){

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
            doc = handler.replaceAt(doc);


            Element rootElement = doc.getDocumentElement();

            Element textClock = doc.createElement("TextClock");
            textClock.setAttribute("android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
            textClock.setAttribute("android:format24Hour", "@*com.android.systemui:string/keyguard_widget_24_hours_format");
            textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
            textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
            textClock.setAttribute("android:layout_width", "wrap_content");
            textClock.setAttribute("android:layout_height", "fill_parent");
            textClock.setAttribute("android:gravity", "center");
            textClock.setAttribute("android:singleLine", "true");
            textClock.setAttribute("android:layout_alignParentBottom", "true");

            NodeList nodeList = rootElement.getElementsByTagName("LinearLayout");
            Node firstChild;
            if(nodeList.getLength() ==1){
                Element secondRootElement = (Element) nodeList.item(0);
                NodeList anotherNodeList = secondRootElement.getElementsByTagName("com.android.keyguard.AlphaOptimizedLinearLayout");
                if(anotherNodeList.getLength()==1){
                    Element thirdElement = (Element) anotherNodeList.item(0);
                    NodeList stockClock = thirdElement.getElementsByTagName("com.android.systemui.statusbar.policy.Clock");
                    if(stockClock.getLength()==1){
                        firstChild = stockClock.item(0);
                        thirdElement.insertBefore(textClock, firstChild);

                    }
                }
            }


            Element hideyLayout = doc.createElement("LinearLayout");
            hideyLayout.setAttribute("android:layout_width", "fill_parent");
            hideyLayout.setAttribute("android:layout_height", "fill_parent");
            hideyLayout.setAttribute("android:gravity", "center");
            hideyLayout.setAttribute("android:orientation", "horizontal");
            hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");

            Node firstElement = rootElement.getFirstChild();

            rootElement.insertBefore(hideyLayout, firstElement);


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(rootApk + "type2_Clock_on_Lockscreen_Right/layout/" + "status_bar.xml"));
            transformer.transform(source, result);
        }catch (Exception e){
            Log.e(tag, e.getMessage());
        }
    }


    public void editLeftOnLockscreen(){

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
            doc = handler.replaceAt(doc);

            Element rootElement = doc.getDocumentElement();

            Element textClock = doc.createElement("TextClock");
            textClock.setAttribute("android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
            textClock.setAttribute("android:format24Hour", "@*com.android.systemui:string/keyguard_widget_24_hours_format");
            textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
            textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
            textClock.setAttribute("android:layout_width", "wrap_content");
            textClock.setAttribute("android:layout_height", "fill_parent");
            textClock.setAttribute("android:gravity", "left|center");
            textClock.setAttribute("android:singleLine", "true");

            NodeList startingElement = rootElement.getElementsByTagName("LinearLayout");
            if(startingElement.getLength()==1) {
                Element startElement = (Element) startingElement.item(0);
                Node firstChild = startElement.getFirstChild();
                startElement.insertBefore(textClock, firstChild);
            }else Log.d("error", "two elemnts with linearLayout");


            Element hideyLayout = doc.createElement("LinearLayout");
            hideyLayout.setAttribute("android:layout_width", "fill_parent");
            hideyLayout.setAttribute("android:layout_height", "fill_parent");
            hideyLayout.setAttribute("android:gravity", "center");
            hideyLayout.setAttribute("android:orientation", "horizontal");
            hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");

            Node firstElement = rootElement.getFirstChild();
            rootElement.insertBefore(hideyLayout, firstElement);


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(rootApk + "type2_Clock_on_Lockscreen_Left/layout/" + "status_bar.xml"));
            transformer.transform(source, result);
        }catch (Exception e){
            Log.d("hi",e.getMessage());
        }
    }





    public void editCenterOnLockscreen(){

            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
                doc = handler.replaceAt(doc);

                Element rootElement = doc.getDocumentElement();

                Element textClock = doc.createElement("TextClock");
                textClock.setAttribute("android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
                textClock.setAttribute("android:format24Hour", "@*com.android.systemui:string/keyguard_widget_24_hours_format");
                textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
                textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
                textClock.setAttribute("android:layout_width", "fill_parent");
                textClock.setAttribute("android:layout_height", "fill_parent");
                textClock.setAttribute("android:gravity", "center");
                textClock.setAttribute("android:singleLine", "true");

                Node firstElement = rootElement.getFirstChild();
                rootElement.insertBefore(textClock, firstElement);

                Element hideyLayout = doc.createElement("LinearLayout");
                hideyLayout.setAttribute("android:layout_width", "fill_parent");
                hideyLayout.setAttribute("android:layout_height", "fill_parent");
                hideyLayout.setAttribute("android:gravity", "center");
                hideyLayout.setAttribute("android:orientation", "horizontal");
                hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");

                rootElement.insertBefore(hideyLayout, firstElement);


                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);

                StreamResult result = new StreamResult(new File(rootApk + "res/layout/" + "status_bar.xml"));
                transformer.transform(source, result);
            }catch (Exception e){
                Log.d("hi",e.getMessage());
            }
        }




    }
