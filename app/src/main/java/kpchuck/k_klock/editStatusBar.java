package kpchuck.k_klock;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
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

import kpchuck.k_klock.Utils.XmlUtils;

/**
 * Created by Karol Przestrzelski on 15/08/2017.
 */

public class editStatusBar{


    Context context;
    OtherRomsHandler handler = new OtherRomsHandler(context);
    XmlUtils xmlUtils = new XmlUtils();

    String slash = "/";

    String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock";
    String xmlFolder = rootFolder + slash + "userInput/";
    String tag = "klock";


    String rootApk = rootFolder + slash + "tempF" + "/Rom.zip" + "/assets/overlays/com.android.systemui/";

    public void Execution(Context context){
        this.context=context;
        editCenterNotOnLockscreen();
        editCenterOnLockscreen();
        editLeftNotOnLockscreen();
        editLeftOnLockscreen();
        editRightNotOnLockscreen();
        editRightOnLockscreen();
        editStockCenter();

        editStockRight();

        editStockLeft();

    }
    

    public void editRightNotOnLockscreen(){

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
            doc = handler.replaceAt(doc);
            handler.fixUpForAttrs(doc);

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
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
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
            handler.fixUpForAttrs(doc);

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
            Log.d("klock",e.getMessage());
        }
    }

    public void editCenterNotOnLockscreen(){

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
            doc = handler.replaceAt(doc);
            handler.fixUpForAttrs(doc);

            Element rootElement = doc.getDocumentElement();
            //Get linear layout where the clock will be inserted before
            NodeList list = rootElement.getElementsByTagName("LinearLayout");
            Element layout = null;
            for (int i=0;i<list.getLength();i++){
                layout = (Element) list.item(i);
                Attr attr = layout.getAttributeNode("android:id");
                if (attr.getValue().equals("@*com.android.systemui:id/status_bar_contents")) break;
                else layout = null;
            }
            if (layout == null)Log.e(tag, "LinearLayout is null");

            NodeList deeplist = layout.getElementsByTagName("com.android.keyguard.AlphaOptimizedLinearLayout");
            Element systemIconArea = null;
            for (int i=0;i<deeplist.getLength();i++){
                systemIconArea = (Element) deeplist.item(i);
                Attr attr = systemIconArea.getAttributeNode("android:id");
                if (attr.getValue().equals("@*com.android.systemui:id/system_icon_area")) break;
                else systemIconArea = null;
            }
            if (systemIconArea == null)Log.e(tag, "SystemIconArea is null");

            //Remove all attributes from system_icon_area
            NamedNodeMap attibutes = systemIconArea.getAttributes();
            for (int i = 0; i < attibutes.getLength(); i++){

                systemIconArea.removeAttribute(attibutes.item(i).toString());
            }
            //Create new system_icon_area
            systemIconArea.setAttribute("android:orientation", "horizontal");
            systemIconArea.setAttribute("android:id","@*com.android.systemui:id/system_icon_area");
            systemIconArea.setAttribute("android:layout_width", "0.0dip");
            systemIconArea.setAttribute("android:layout_weight", "1");
            systemIconArea.setAttribute("android:layout_height", "fill_parent");


            //Creating textclock
            Element textClock = doc.createElement("TextClock");
            textClock.setAttribute("android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
            textClock.setAttribute("android:format24Hour", "@*com.android.systemui:string/keyguard_widget_24_hours_format");
            textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
            textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
            textClock.setAttribute("android:layout_width", "wrap_content");
            textClock.setAttribute("android:layout_height", "fill_parent");
            textClock.setAttribute("android:gravity", "center");
            textClock.setAttribute("android:singleLine", "true");



            //Create the view layout
            Element view = doc.createElement("View");
            view.setAttribute("android:visibility", "invisible");
            view.setAttribute("android:layout_width", "0.0dip");
            view.setAttribute("android:layout_height", "fill_parent");
            view.setAttribute("android:layout_weight", "1.0");




            //Adding the Linear Layout to hide the clock
            Element hideyLayout = doc.createElement("LinearLayout");
            hideyLayout.setAttribute("android:layout_width", "wrap_content");
            hideyLayout.setAttribute("android:layout_height", "fill_parent");
            hideyLayout.setAttribute("android:gravity", "center");
            hideyLayout.setAttribute("android:orientation", "horizontal");
            hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");
            //Add text clock into linearlayout
            hideyLayout.appendChild(textClock);

            //Insert stuff into target element
            layout.insertBefore(hideyLayout, systemIconArea);
            systemIconArea.insertBefore(view, systemIconArea.getFirstChild());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(rootApk + "type2_No_Clock_on_Lockscreen_Center/layout/" + "status_bar.xml"));
            transformer.transform(source, result);
        }catch (Exception e){
            Log.e("klock",e.getMessage());
        }
    }


    public void editRightOnLockscreen(){

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
            doc = handler.replaceAt(doc);
            handler.fixUpForAttrs(doc);

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
            handler.fixUpForAttrs(doc);

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
            Log.d("klock",e.getMessage());
        }
    }





    public void editCenterOnLockscreen(){

            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
                doc = handler.replaceAt(doc);
                handler.fixUpForAttrs(doc);

                Element rootElement = doc.getDocumentElement();
                //Get linear layout where the clock will be inserted before
                NodeList list = rootElement.getElementsByTagName("LinearLayout");
                Element layout = null;
                for (int i=0;i<list.getLength();i++){
                    layout = (Element) list.item(i);
                    Attr attr = layout.getAttributeNode("android:id");
                    if (attr.getValue().equals("@*com.android.systemui:id/status_bar_contents")) break;
                    else layout = null;
                }

                NodeList deeplist = layout.getElementsByTagName("com.android.keyguard.AlphaOptimizedLinearLayout");
                Element systemIconArea = null;
                for (int i=0;i<deeplist.getLength();i++){
                    systemIconArea = (Element) deeplist.item(i);
                    Attr attr = systemIconArea.getAttributeNode("android:id");
                    if (attr.getValue().equals("@*com.android.systemui:id/system_icon_area")) break;
                    else systemIconArea = null;
                }
                //Remove all attributes from system_icon_area
                NamedNodeMap attibutes = systemIconArea.getAttributes();
                for (int i = 0; i < attibutes.getLength(); i++){
                    systemIconArea.removeAttribute(attibutes.item(i).toString());
                }
                //Create new system_icon_area
                systemIconArea.setAttribute("android:orientation", "horizontal");
                systemIconArea.setAttribute("android:id","@*com.android.systemui:id/system_icon_area");
                systemIconArea.setAttribute("android:layout_width", "0.0dip");
                systemIconArea.setAttribute("android:layout_weight", "1");
                systemIconArea.setAttribute("android:layout_height", "fill_parent");

                //Creating textclock
                Element textClock = doc.createElement("TextClock");
                textClock.setAttribute("android:format12Hour", "@*com.android.systemui:string/keyguard_widget_12_hours_format");
                textClock.setAttribute("android:format24Hour", "@*com.android.systemui:string/keyguard_widget_24_hours_format");
                textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
                textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
                textClock.setAttribute("android:layout_width", "wrap_content");
                textClock.setAttribute("android:layout_height", "fill_parent");
                textClock.setAttribute("android:gravity", "center");
                textClock.setAttribute("android:singleLine", "true");

                //Create the view layout
                Element view = doc.createElement("View");
                view.setAttribute("android:visibility", "invisible");
                view.setAttribute("android:layout_width", "0.0dip");
                view.setAttribute("android:layout_height", "fill_parent");
                view.setAttribute("android:layout_weight", "1.0");


                //Adding the Linear Layout to hide the clock
                Element hideyLayout = doc.createElement("LinearLayout");
                hideyLayout.setAttribute("android:layout_width", "wrap_content");
                hideyLayout.setAttribute("android:layout_height", "fill_parent");
                hideyLayout.setAttribute("android:gravity", "center");
                hideyLayout.setAttribute("android:orientation", "horizontal");
                hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");
                //Add LinearLayout to the start of rootelemnt
                rootElement.insertBefore(hideyLayout, rootElement.getFirstChild());

                //Insert stuff into target element
                layout.insertBefore(textClock, systemIconArea);
                systemIconArea.insertBefore(view, systemIconArea.getFirstChild());


                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);

                StreamResult result = new StreamResult(new File(rootApk + "res/layout/" + "status_bar.xml"));
                transformer.transform(source, result);
            }catch (Exception e){
                Log.d("klock",e.getMessage());
            }
        }

    public void editStockRight(){

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
            doc = handler.replaceAt(doc);
            handler.fixUpForAttrs(doc);

            Element rootElement = doc.getDocumentElement();

            Element textClock = doc.createElement("com.android.systemui.statusbar.policy.Clock");
            textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
            textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
            textClock.setAttribute("android:layout_width", "wrap_content");
            textClock.setAttribute("android:layout_height", "fill_parent");
            textClock.setAttribute("android:gravity", "start|center");
            textClock.setAttribute("android:singleLine", "true");
            textClock.setAttribute("android:id", "@*com.android.systemui:id/clock");
            textClock.setAttribute("android:paddingEnd", "@*com.android.systemui:dimen/status_bar_clock_end_padding");


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
                        thirdElement.removeChild(firstChild);
                    }
                }
            }


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(rootApk + "type2_Stock_Clock_Right/layout/" + "status_bar.xml"));
            transformer.transform(source, result);
        }catch (Exception e){
            Log.e(tag, e.getMessage());

        }
    }


    public void editStockLeft(){

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
            doc = handler.replaceAt(doc);
            handler.fixUpForAttrs(doc);

            Element rootElement = doc.getDocumentElement();

            //Get linear layout where the clock will be inserted before
            NodeList list = rootElement.getElementsByTagName("LinearLayout");
            Element layout = null;
            for (int i=0;i<list.getLength();i++){
                layout = (Element) list.item(i);
                Attr attr = layout.getAttributeNode("android:id");
                if (attr.getValue().equals("@*com.android.systemui:id/status_bar_contents")) break;
                else layout = null;
            }
            if (layout == null)Log.e(tag, "LinearLayout is null");

            //Remove Stock Clock
            NodeList clocklist = layout.getElementsByTagName("com.android.keyguard.AlphaOptimizedLinearLayout");
            Element clocklayout = null;
            for (int i=0;i<clocklist.getLength();i++){
                clocklayout = (Element) clocklist.item(i);
                Attr attr = clocklayout.getAttributeNode("android:id");
                if (attr.getValue().equals("@*com.android.systemui:id/system_icon_area")){
                    NodeList stocklist = layout.getElementsByTagName("com.android.systemui.statusbar.policy.Clock");
                    Element stocklayout = null;
                    for (int k=0;k<stocklist.getLength();k++) {
                        stocklayout = (Element) stocklist.item(k);
                        Attr stockattr = stocklayout.getAttributeNode("android:id");
                        if (stockattr.getValue().equals("@*com.android.systemui:id/clock")) {
                            clocklayout.removeChild(stocklayout);
                        }
                    }
                }
            }

            Element textClock = doc.createElement("com.android.systemui.statusbar.policy.Clock");
            textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
            textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
            textClock.setAttribute("android:layout_width", "wrap_content");
            textClock.setAttribute("android:layout_height", "fill_parent");
            textClock.setAttribute("android:id", "@*com.android.systemui:id/clock");
            textClock.setAttribute("android:gravity", "left|center");
            textClock.setAttribute("android:singleLine", "true");
            textClock.setAttribute("android:paddingStart", "@*com.android.systemui:dimen/status_bar_clock_end_padding");
            textClock.setAttribute("android:paddingEnd", "@*com.android.systemui:dimen/status_bar_clock_end_padding");

            Element hideyLayout = doc.createElement("LinearLayout");
            hideyLayout.setAttribute("android:layout_width", "wrap_content");
            hideyLayout.setAttribute("android:layout_height", "fill_parent");
            hideyLayout.setAttribute("android:gravity", "left");
            hideyLayout.setAttribute("android:orientation", "horizontal");
            hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");


            Node firstChild = layout.getFirstChild();
            layout.insertBefore(hideyLayout, firstChild);
            hideyLayout.appendChild(textClock);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(rootApk + "type2_Stock_Clock_Left/layout/" + "status_bar.xml"));
            transformer.transform(source, result);
        }catch (Exception e){
            Log.d("klock",e.getMessage());
        }
    }

    public void editStockCenter(){

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFolder + "status_bar.xml"));
            doc = handler.replaceAt(doc);
            handler.fixUpForAttrs(doc);


            Element rootElement = doc.getDocumentElement();
            //Get linear layout where the clock will be inserted before
            NodeList list = rootElement.getElementsByTagName("LinearLayout");
            Element layout = null;
            for (int i=0;i<list.getLength();i++){
                layout = (Element) list.item(i);
                Attr attr = layout.getAttributeNode("android:id");
                if (attr.getValue().equals("@*com.android.systemui:id/status_bar_contents")) break;
                else layout = null;
            }
            if (layout == null)Log.e(tag, "LinearLayout is null");

            //Remove Stock Clock
            NodeList clocklist = layout.getElementsByTagName("com.android.keyguard.AlphaOptimizedLinearLayout");
            Element clocklayout = null;
            for (int i=0;i<clocklist.getLength();i++){
                clocklayout = (Element) clocklist.item(i);
                Attr attr = clocklayout.getAttributeNode("android:id");
                if (attr.getValue().equals("@*com.android.systemui:id/system_icon_area")){
                    NodeList stocklist = layout.getElementsByTagName("com.android.systemui.statusbar.policy.Clock");
                    Element stocklayout = null;
                    for (int k=0;k<stocklist.getLength();k++) {
                        stocklayout = (Element) stocklist.item(k);
                        Attr stockattr = stocklayout.getAttributeNode("android:id");
                        if (stockattr.getValue().equals("@*com.android.systemui:id/clock")) {
                            clocklayout.removeChild(stocklayout);
                        }
                    }
                }
            }

            NodeList deeplist = layout.getElementsByTagName("com.android.keyguard.AlphaOptimizedLinearLayout");
            Element systemIconArea = null;
            for (int i=0;i<deeplist.getLength();i++){
                systemIconArea = (Element) deeplist.item(i);
                Attr attr = systemIconArea.getAttributeNode("android:id");
                if (attr.getValue().equals("@*com.android.systemui:id/system_icon_area")) break;
                else systemIconArea = null;
            }
            if (systemIconArea == null)Log.e(tag, "SystemIconArea is null");

            //Remove all attributes from system_icon_area
            NamedNodeMap attibutes = systemIconArea.getAttributes();
            for (int i = 0; i < attibutes.getLength(); i++){

                systemIconArea.removeAttribute(attibutes.item(i).toString());
            }
            //Create new system_icon_area
            systemIconArea.setAttribute("android:orientation", "horizontal");
            systemIconArea.setAttribute("android:id","@*com.android.systemui:id/system_icon_area");
            systemIconArea.setAttribute("android:layout_width", "0.0dip");
            systemIconArea.setAttribute("android:layout_weight", "1");
            systemIconArea.setAttribute("android:layout_height", "fill_parent");



            //Creating textclock
            Element textClock = doc.createElement("com.android.systemui.statusbar.policy.Clock");
            textClock.setAttribute("android:textAppearance", "@*com.android.systemui:style/TextAppearance.StatusBar.Clock");
            textClock.setAttribute("android:textColor", "@*com.android.systemui:color/status_bar_clock_color");
            textClock.setAttribute("android:layout_width", "wrap_content");
            textClock.setAttribute("android:layout_height", "fill_parent");
            textClock.setAttribute("android:gravity", "center");
            textClock.setAttribute("android:singleLine", "true");
            textClock.setAttribute("android:id", "@*com.android.systemui:id/clock");

            //Create the view layout
            Element view = doc.createElement("View");
            view.setAttribute("android:visibility", "invisible");
            view.setAttribute("android:layout_width", "0.0dip");
            view.setAttribute("android:layout_height", "fill_parent");
            view.setAttribute("android:layout_weight", "1.0");


            //Adding the Linear Layout to hide the clock
            Element hideyLayout = doc.createElement("LinearLayout");
            hideyLayout.setAttribute("android:layout_width", "wrap_content");
            hideyLayout.setAttribute("android:layout_height", "fill_parent");
            hideyLayout.setAttribute("android:gravity", "center");
            hideyLayout.setAttribute("android:orientation", "horizontal");
            hideyLayout.setAttribute("android:id", "@*com.android.systemui:id/system_icon_area");
            //Add text clock into linearlayout
            hideyLayout.appendChild(textClock);

            //Insert stuff into target element
            layout.insertBefore(hideyLayout, systemIconArea);
            systemIconArea.insertBefore(view, systemIconArea.getFirstChild());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(new File(rootApk + "type2_Stock_Clock_Center/layout/" + "status_bar.xml"));
            transformer.transform(source, result);
        }catch (Exception e){
            Log.e("klock",e.getMessage());
        }
    }




}
