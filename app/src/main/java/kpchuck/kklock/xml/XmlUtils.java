package kpchuck.kklock.xml;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.sax2.Driver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.FileHelper;

import static kpchuck.kklock.constants.XmlConstants.*;

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


    public Element findElementById(Document document, String idName){
        return findElementById(document.getDocumentElement(), idName);
    }

    public Element findElementById(Element parentElement, String idName){
        if (parentElement.getAttribute(X_ID).equals(idName)) return parentElement;


        NodeList list = parentElement.getChildNodes();
        Element layout = null;
        for (int i=0; i<list.getLength(); i++){
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                layout = (Element) list.item(i);
                Attr attr = layout.getAttributeNode(X_ID);
                if (attr != null && attr.getValue().equals(idName)) break;
                if (layout.getChildNodes() != null || layout.getChildNodes().getLength() != 0){
                    layout = findElementById(layout, idName);
                    if (layout != null) break;
                }
                layout = null;
            }
        }

        return layout;
    }

    public List<Element> findElementsById(Document document, String idName){
        return findElementsById(document.getDocumentElement(), idName, new ArrayList<Element>());
    }

    public List<Element> findElementsById(Element parentElement, String idName, List<Element> elements){
        if (parentElement.getAttribute(X_ID).equals(idName)) elements.add(parentElement);


        NodeList list = parentElement.getChildNodes();
        Element layout;
        for (int i=0; i<list.getLength(); i++){
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                layout = (Element) list.item(i);
                Attr attr = layout.getAttributeNode(X_ID);
                if (attr != null && attr.getValue().equals(idName)) elements.add(layout);
                if (layout.getChildNodes() != null || layout.getChildNodes().getLength() != 0){
                    elements = findElementsById(layout, idName, elements);
                }
            }
        }

        return elements;
    }

    public Element findElementLikeId(Document document, String idName){
        return findElementLikeId(document.getDocumentElement(), idName);
    }

    public Element findElementLikeId(Element parentElement, String idName){
        if (parentElement.getAttribute(X_ID).equals(idName)) return parentElement;

        NodeList list = parentElement.getChildNodes();
        Element layout = null;
        for (int i=0; i<list.getLength(); i++){
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                layout = (Element) list.item(i);
                Attr attr = layout.getAttributeNode(X_ID);
                if (attr != null && attr.getValue().contains(idName)) break;
                if (layout.getChildNodes() != null || layout.getChildNodes().getLength() != 0){
                    layout = findElementById(layout, idName);
                    if (layout != null) break;
                }
                layout = null;
            }
        }

        return layout;
    }

    public Element findElementByTag(Document document, String tagName){
        return findElementByTag(document.getDocumentElement(), tagName);
    }

    private Element findElementByTag(Element parentElement, String tagName){
        if (parentElement.getTagName().equals(tagName)) return parentElement;

        NodeList elements = parentElement.getElementsByTagName(tagName);
        return (Element) elements.item(0);
    }


    private boolean isTheElement(Element element, String layoutTag, String idName){
        return isTheElement(element, layoutTag, idName, X_ID);
    }

    private boolean isTheElement(Element element, String layoutTag, String idName, String attributeName){
        if (!element.getTagName().equals(layoutTag)) return false;
        if (!element.hasAttribute(attributeName)) return false;

        Attr attr = element.getAttributeNode(attributeName);
        if (attr != null && attr.getValue().equals(idName))return true;
        else return false;
    }

    public Element changeAttribute(Element element, String attribute, String value){
        if (element.hasAttribute(attribute))
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

    public String elementString(Element element) throws TransformerException{
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(element);
        StreamResult result = new StreamResult(new StringWriter());



        transformer.transform(source, result);

        String k = result.getWriter().toString();
        k=k.substring("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length());
        return k;
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
            if (attr != null && attr.getValue().equals(idName)) break;
            else layout = null;
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
        doc = replaceStuffInXml(doc, "@", "@*com.android.systemui:");
        return replaceStuffInXml(doc, "@*com.android.systemui:android", "@*android");
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

    private String pathToMerger = Environment.getExternalStorageDirectory() + "/K-Klock/tempF";

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

    public Document getDocument(File file){
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(file);
        }catch (Exception e){
            Log.e("klock", "Error getting document for file " + file.getName() + "\n" + e.getMessage());
        }

        return doc;
    }

    public void writeDocToFile(Document doc, File dest) throws Exception{

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(new FileOutputStream(dest));
        transformer.transform(source, result);

    }

    public ArrayList<String> getEngArray(Context context, int id) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale("en"));
        return new ArrayList<>(Arrays.asList(context.createConfigurationContext(configuration).getResources().getStringArray(id)));
    }

    public ArrayList<String> getArray(Context context, int id) {
        return new ArrayList<>(Arrays.asList(context.getResources().getStringArray(id)));
    }

    public void translate(Context context, File baseFolder, ArrayList<String> filenames, ArrayList<String> translated_filenames,
                           int id_1a, int id_1b, int id_1c, int id_2){
        try {
            FileHelper fileHelper = new FileHelper();
            File[] files = baseFolder.listFiles();
            for (File file : files) {
                if (filenames.contains(file.getName())) {
                    int index = filenames.indexOf(file.getName());
                    fileHelper.renameFile(file, translated_filenames.get(index));
                } else if (file.getName().equals("type1a")) {
                    FileUtils.write(file, context.getString(id_1a), "utf-8", false);
                } else if (file.getName().equals("type1b")) {
                    FileUtils.write(file, context.getString(id_1b), "utf-8", false);
                } else if (file.getName().equals("type1c")) {
                    FileUtils.write(file, context.getString(id_1c), "utf-8", false);
                } else if (file.getName().equals("type2")) {
                    FileUtils.write(file, context.getString(id_2), "utf-8", false);
                }
            }
        }catch (IOException e){
            Log.e("klock", "Translation failed. "+ e.getMessage());
        }


    }

    public ArrayList<String> substratize(ArrayList<String> arrayList, String start, String end){
        ArrayList<String> newArrayList = new ArrayList<>();
        for (int i=0; i<arrayList.size(); i++){
            String item = arrayList.get(i);
            item = start + " " + item + end;
            item = item.replace(" ", "_");
            newArrayList.add(item);
        }
        return newArrayList;
    }

    public String getType2(Context context, int id){
        String string = context.getString(id);
        String item = "type2 " + string;
        return item.replace(" ", "_");
    }

    public Document getDocument(XmlResourceParser resourceParser, Context context) throws Exception{

        DocumentBuilderFactory d = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = d.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Resources resources = context.getPackageManager().getResourcesForApplication("com.android.systemui");

        int eventType = resourceParser.getEventType();

        Element beforeElement = null;
        Element currentElement = null;
        document.appendChild(document.createTextNode("\n"));

        while (eventType != XmlPullParser.END_DOCUMENT){
            if (eventType == XmlPullParser.START_TAG){

                currentElement = document.createElement(resourceParser.getName());
                if (beforeElement == null){
                    document.appendChild(currentElement);
                    currentElement.appendChild(document.createTextNode("\n"));
                    currentElement = getAttributes(context, resourceParser, resources, currentElement);

                    beforeElement = currentElement;
                }
                else{
                    beforeElement.appendChild(currentElement);
                    beforeElement.appendChild(document.createTextNode("\n"));
                    currentElement = getAttributes(context, resourceParser, resources, currentElement);
                }
            }
            else if (eventType == XmlPullParser.END_TAG){
                try {
                    beforeElement = (Element) currentElement.getParentNode();
                }catch (NullPointerException e){
                    beforeElement = null;
                }
            }
            eventType = resourceParser.next();
        }

        return document;
    }

    private Element getAttributes(Context context, XmlResourceParser resourceParser, Resources resources, Element currentElement) throws Exception{

        int count = resourceParser.getAttributeCount();
        for (int i=0; i<count; i++){
            String name = "android:" + resourceParser.getAttributeName(i);
            String value = resourceParser.getAttributeValue(i);
            String h = resourceParser.getAttributeNamespace(i);


            if (value.startsWith("@")){
                value = getAttribute(resources, value);
            }
            else if (value.startsWith("?")){
                value = getReference(context, value);
            }
            else if (name.endsWith("gravity")){
                value = parseGravity(value);
            }
            else if (name.contains("width") || name.contains("height")){
                value = getWidth(value);
            }
            currentElement.setAttribute(name, value);

        }
        return currentElement;
    }

    private String getAttribute(Resources res, String value){
        int v = Integer.valueOf(value.substring(1));
        value = res.getResourceEntryName(v);
        String s = res.getResourceTypeName(v);
        return "@"+s+"/"+value;
    }

    private String getReference(Context context, String value) throws PackageManager.NameNotFoundException{
        int v = Integer.valueOf(value.substring(1));
        Resources android = context.getPackageManager().getResourcesForApplication("android");
        Resources sysui = context.getPackageManager().getResourcesForApplication("com.android.systemui");
        String r;
        try{
            r = "android:"+android.getResourceEntryName(v);
        }catch (Exception e){
            r = sysui.getResourceEntryName(v);
        }
        return "?"+r;
    }

    private HashMap<Integer, String> getConstants(Class o) throws Exception{
        HashMap<Integer, String> constants = new HashMap<>();
        Field[] fields = o.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()) && f.getName().equals(f.getName().toUpperCase())) {
                try {
                    Integer i = (int) f.get(null);
                    constants.put(i, f.getName());
                }catch (Exception e){}
            }
        }
        return constants;
    }

    private String getWidth(String value) {
        try {
            switch (Integer.valueOf(value)) {
                case LinearLayout.LayoutParams.MATCH_PARENT:
                    return "MATCH_PARENT";
                case LinearLayout.LayoutParams.WRAP_CONTENT:
                    return "WRAP_CONTENT";
            }
        }catch (Exception e){}
        return value;
    }

    private String parseGravity(String value) throws Exception{

        int v = Integer.decode(value);
        HashMap<Integer, String> modifiers = getConstants(Gravity.class);
        List<String> p = new ArrayList<>();
        if (v < 1000)
            p = partition(v);
        else {
            List<Integer> big = new ArrayList<>();
            for (Integer i : modifiers.keySet()){
                if (i > 1000) big.add(i);
            }
            for (Integer i: big){
                if (v > i) {
                    List<String> q = partition(v - i);
                    for (String s : q){
                        p.add(s + " " + String.valueOf(i));
                    }
                }
            }

        }
        List<List<Integer>> left = new ArrayList<>();
        for (String line: p){
            line = line.substring(1);
            List<String> number = Arrays.asList(line.split(" "));
            if (number.size() < modifiers.size()){
                List<Integer> numbers = new ArrayList<>();
                for (String l : number){
                    try{
                        numbers.add(Integer.valueOf(l));
                    }catch (Exception e){}
                }

                if (new HashSet<Integer>(numbers).size() == numbers.size() && modifiers.keySet().containsAll(numbers)){
                    left.add(numbers);
                }
            }
        }
        if (left.size() > 0) {
            int index =0;
            int largest =left.get(0).size();
            for (int i=0; i<left.size();i++){
                List<Integer> u = left.get(i);
                if (u.size() < largest){
                    largest = u.size();
                    index = i;
                }
            }
            StringBuilder result = new StringBuilder();
            for (Integer i: left.get(index)){
                result.append(modifiers.get(i));
                result.append("|");
            }
            result.reverse().delete(0, 1).reverse();
            return result.toString();
        }
        return "";
    }

    private List<String> partition(int n) {
        return partition(n, n, "", new ArrayList<String>());
    }
    private List<String> partition(int n, int max, String prefix, List<String> strings) {
        if (n == 0) {
            strings.add(prefix);
            return strings;
        }

        for (int i = Math.min(max, n); i >= 1; i--) {
            strings = partition(n-i, i, prefix + " " + i, strings);
        }
        return strings;
    }
}