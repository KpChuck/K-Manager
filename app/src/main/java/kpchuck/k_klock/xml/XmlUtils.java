package kpchuck.k_klock.xml;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import kpchuck.k_klock.utils.FileHelper;

import static kpchuck.k_klock.constants.XmlConstants.*;

/**
 * Created by karol on 26/12/17.
 */

public class XmlUtils {

    public Element getFirstChildElement(Node parent) {
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

    public Element findElementInDoc(Document doc, String layoutTag, String idName){

        Element myElement = null;

        Element rootElement = doc.getDocumentElement();
        if (isTheElement(rootElement, layoutTag, idName)) return rootElement;

        myElement = getElementById(rootElement, layoutTag, idName);
        if (myElement != null) return myElement;

        return null;
    }

    public Element findElementById(Document doc, String idName){
        Element parentElement = doc.getDocumentElement();
        if (parentElement.getAttribute(X_ID).equals(idName)) return parentElement;


        NodeList list = parentElement.getChildNodes();
        Element layout = null;
        for (int i=0; i<list.getLength(); i++){
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                layout = (Element) list.item(i);
                Attr attr = layout.getAttributeNode(X_ID);
                if (attr.getValue().equals(idName)) break;
                else layout = null;
            }
        }
        if (layout == null){
            Log.e("klock", "Could not find element with id " + idName);
            return null;
        }
        return layout;
    }


    public boolean isTheElement(Element element, String layoutTag, String idName){
        return isTheElement(element, layoutTag, idName, X_ID);
    }

    private boolean isTheElement(Element element, String layoutTag, String idName, String attributeName){
        if (!element.getTagName().equals(layoutTag)) return false;
        if (!element.hasAttribute(attributeName)) return false;

        Attr attr = element.getAttributeNode(attributeName);
        if (attr.getValue().equals(idName))return true;
        else return false;
    }

    public Element changeAttribute(Element element, String attribute, String value){
        element.removeAttribute(attribute);
        element.setAttribute(attribute, value);
        return element;
    }

    public ArrayList<Element> getChildElements(Element element){

        ArrayList<Element> elements = new ArrayList<>();

        NodeList childs = element.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                elements.add(childElement);
            }
        }

        return elements;

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

    private void findAbnormallyLongGravity(Element element){

        Element e = null;
        NodeList childs = element.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                e = (Element) child;
                findAbnormallyLongGravity(e);
                if (!e.hasAttribute(X_GRAVITY)) continue;
                String gravity = e.getAttribute(X_GRAVITY);
                if ((gravity.length() > 15))
                    e = changeAttribute(e, X_GRAVITY, "start|center");
            }
        }
    }

    public Document replaceAt(Document doc){
        doc = replaceStuffInXml(doc, "@+", "@");
        findAbnormallyLongGravity(doc.getDocumentElement());
        return replaceStuffInXml(doc, "@", "@*com.android.systemui:");
    }

    public Document replaceStuffInXml(Document doc, String old, String news){
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            output = output.replace(old, news);
            doc = stringToDom(output);
            doc.normalizeDocument();


        }catch (Exception e){Log.e("klock", e.getMessage());

        }
        return doc;
    }

    public static Document stringToDom(String xmlSource)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
    }

    public void writeType2Desc(String desc, String path){
        File file = new File(path);
      //  String data = "Clock Style (Clock on Lockscreen Center";
        try {
            FileUtils.writeStringToFile(file, desc, "utf-8", false);
        }catch (IOException e){
            Log.e("klock", e.getMessage());
        }
    }

    String pathToMerger = Environment.getExternalStorageDirectory() + "/K-Klock/tempF";

    public boolean moveAttrsIfPresent(String xmlFolder){
        File attrs = new File(xmlFolder + "/attrs.xml");
        if (attrs.exists()){
            Log.d("klock", "Moving attrs.xml to rom.zip");
            FileHelper fileHelper = new FileHelper();
            fileHelper.newFolder(pathToMerger + "/Rom.zip/assets/overlays/com.android.systemui/res/");
            fileHelper.newFolder(pathToMerger + "/Rom.zip/assets/overlays/com.android.systemui/res/values");
            File a = fileHelper.newFolder(pathToMerger + "/Rom.zip/assets/overlays/com.android.systemui/res/values");

            try{
                FileUtils.copyFileToDirectory(attrs, a);
            }catch (IOException e){
                Log.e("klock", e.getMessage());
            }
            return true;
        }
        return false;
    }

    public Document fixUpForAttrs(Document document, boolean hasAttrs){
        if (hasAttrs){
            Log.d("klock", "trying to fix up res-auto");
            Element rootElement = document.getDocumentElement();
            try{
                rootElement.removeAttribute("xmlns:systemui");
                rootElement.setAttribute("xmlns:systemui", "http://schemas.android.com/apk/res-auto");
            }
            catch (Exception e){
                Log.d("klock", "Couldn't set res-auto attribute for xmlns:systemui");
            }
        }
        return document;
    }

    public Element lastElement(Element parentElement){
        ArrayList<Element> elements = getChildElements(parentElement);
        return elements.get(elements.size() -1 );
    }

    public boolean isPushyOutElement(Element element){
        if (element.hasAttribute(X_WEIGHT) && element.hasAttribute(X_LAYOUT_WIDTH)) {
            String weight = element.getAttribute(X_WEIGHT).substring(0, 1);
            String width = element.getAttribute(X_LAYOUT_WIDTH).substring(0, 1);
            return (weight.equals("1") && width.equals("0"));
        }else {
            return false;
        }
    }


    public ArrayList<Element> getRightElementsTo(Element parentElement, String tagName, String idName){
        ArrayList<Element> elements = getChildElements(parentElement);
        ArrayList<Element> rightElements = new ArrayList<>();

        for (Element element : elements){
            if (idName == null){
                if (element.getTagName().equals(tagName)) break;
            }
            else if (isTheElement(element, tagName, idName)) break;
            rightElements.add(element);
        }

        return rightElements;
    }
}
