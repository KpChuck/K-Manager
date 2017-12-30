package kpchuck.k_klock;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Karol Przestrzelski on 15/08/2017.
 */

public class editKeyguard{


    String slash = "/";

    String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock";
    String xmlFolder = rootFolder + slash + "userInput/";
    String tag = "klock";

    String rootApk = rootFolder + slash + "tempF" + "/Rom.zip" + "/assets/overlays/com.android.systemui/";

    Context context;
    private boolean hasAttrs;
    OtherRomsHandler handler;


    public void editKeyguard(Context context, boolean hasAttrs){
        this.context=context;
        this.hasAttrs = hasAttrs;
        handler = new OtherRomsHandler(context, hasAttrs);
        moveOriginal();
        moveEdited();
    }


    public void moveOriginal(){try{
        File original = new File(xmlFolder + "keyguard_status_bar.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document doc = documentBuilder.parse(original);
        doc = handler.replaceAt(doc);
        doc = handler.fixUpForAttrs(doc);


        File dest = new File(rootApk + "type2_No_Clock_on_Lockscreen_Right"+ slash + "layout");
        File dest2 = new File(rootApk + "type2_Stock_Clock_Right" + slash + "layout");
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        for (File f: new File[]{dest, dest2}) {
            StreamResult result = new StreamResult(f);

            transformer.transform(source, result);
        }

        }
    catch (Exception e){}
    }

    public void moveEdited(){
            editing(rootApk + "res/layout");

    }

    public File newFolder(String filePath){
        File folder = new File(filePath);
        if (!folder.exists() || !folder.isDirectory())folder.mkdirs();
        return folder;
    }



    public void editing(String destFolder) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document doc = documentBuilder.parse(new File(xmlFolder  + "keyguard_status_bar.xml"));
            doc.normalizeDocument();
            doc = handler.replaceAt(doc);
            doc = handler.fixUpForAttrs(doc);



            Element docEl = doc.getDocumentElement();
            Node childNode = docEl.getFirstChild();


            while (childNode.getNextSibling() != null) {
                childNode = childNode.getNextSibling();
                if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals("LinearLayout")) {
                    Element linearLayout = (Element) childNode;

                    NamedNodeMap attributes = linearLayout.getAttributes();

                    //Add visibility attribute
                    if (attributes.getNamedItem("android:visibility") == null) {
                        linearLayout.setAttribute("android:visibility", "gone");
                    } else {
                        linearLayout.removeAttribute("android:visibility");
                        linearLayout.setAttribute("android:visibility", "gone");
                    }
                    //Add layout width attribute
                    if (attributes.getNamedItem("android:layout_width") == null) {
                        linearLayout.setAttribute("android:layout_width", "0dp");
                    } else {
                        linearLayout.removeAttribute("android:layout_width");
                        linearLayout.setAttribute("android:layout_width", "0dp");
                    }
                }
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            newFolder(destFolder);
            StreamResult result = new StreamResult(new File(destFolder + slash + "keyguard_status_bar.xml"));
            transformer.transform(source, result);

        } catch (Exception e) {

        }
    }




}
