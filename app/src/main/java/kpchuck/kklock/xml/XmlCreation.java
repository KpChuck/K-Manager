package kpchuck.kklock.xml;

/**
 * Created by Karol Przestrzelski on 07/08/2017.
 */
import android.content.Context;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlCreation {

    Context context;
    public void putContext(Context context){
        this.context=context;
    }

    public void createTypeA(String xmlFilename, String colorCode){
        try {

           String start = "<?xml version=\"1.0\" encoding=\"utf-8\"?><resources><color name=\"status_bar_clock_color\">";
            String end = "</color></resources>";
            String fullColorXml = start + colorCode + end;

            XmlUtils utils = new XmlUtils();
            Document doc = utils.stringToDom(fullColorXml);

            String slash = "/";
            String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock" + slash + "type1a_"+xmlFilename;
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rootFolder));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

        }  catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createTypeB(String xmlFilename, String formatCode){
        try {

   String start = "<?xml version=\"1.0\" encoding=\"utf-8\"?><resources><string name=\"keyguard_widget_12_hours_format\"> ";
            String middle = " </string><string name=\"keyguard_widget_24_hours_format\"> ";
            String end = " </string></resources>";
            String fullXmlString = start + formatCode + middle + formatCode + end;

            XmlUtils utils = new XmlUtils();
            Document doc = utils.stringToDom(fullXmlString);

            String slash = "/";
            String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock" + slash + "type1b_"+xmlFilename;
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rootFolder));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);


        } catch (Exception pce) {
            pce.getMessage();
        }

    }

    public void createIcons (String filename, String color){
        String start = "<?xml version=\"1.0\" encoding=\"utf-8\"?><resources>\n<color name=\"dark_mode_icon_color_single_tone\">";
        String end = "</color>\n<color name=\"light_mode_icon_color_single_tone\">@*com.android.systemui:color/status_bar_clock_color</color>\n<color name=\"light_mode_icon_color_dual_tone_fill\">@*com.android.systemui:color/status_bar_clock_color</color></resources>";
        String middle = "</color>\n<color name=\"dark_mode_icon_color_dual_tone_fill\">";

        String fullXml = start + color + middle + color + end;

        try{
            XmlUtils utils = new XmlUtils();
            Document doc = utils.stringToDom(fullXml);

            String slash = "/";
            String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock" + slash + "type1c_"+filename;
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rootFolder));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);


        } catch (Exception pce) {
            pce.getMessage();

        }

    }

    public void createStringDoc(File dest, String name, String value)throws Exception{
        XmlUtils utils = new XmlUtils();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();
        Element docElement = doc.createElement("resources");
        doc.appendChild(docElement);
        Element string = doc.createElement("string");
        string.setAttribute("name", name);
        string.setTextContent(value);
        utils.writeDocToFile(doc, dest);
    }

}
