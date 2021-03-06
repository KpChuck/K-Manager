package kpchuck.kklock.xml;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Environment;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import kpchuck.kklock.utils.FileHelper;

import static kpchuck.kklock.constants.XmlConstants.X_GRAVITY;
import static kpchuck.kklock.constants.XmlConstants.X_ID;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_HEIGHT;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_WIDTH;
import static kpchuck.kklock.constants.XmlConstants.X_WEIGHT;

/**
 * Created by karol on 26/12/17.
 */

public class XmlUtils {

    public File romzip = new File(Environment.getExternalStorageDirectory() + "/K-Klock/tempF/Rom.zip");
    public File baseFolders = new File(romzip, "assets/overlays/com.android.systemui");
    private boolean hasRightInserted = false;
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int CENTER = 2;
    public static final int NONE = 3;
    public static final int USE_SYSTEM = 4;

    public boolean isHasRightInserted() {
        return hasRightInserted;
    }

    public void setHasRightInserted(boolean hasRightInserted) {
        this.hasRightInserted = hasRightInserted;
    }

    public boolean hasStatusIconContainer(Context context)  {
        String container = "com.android.systemui.statusbar.phone.StatusIconContainer";
        try {
            String packageName = "com.android.systemui";
            Resources res = context.getPackageManager().getResourcesForApplication(packageName);
            int id = res.getIdentifier("system_icons", "layout", packageName);
            XmlResourceParser x = res.getLayout(id);
            Document xml = getDocument(x, context);
            String stringXml = domToString(xml);
            return stringXml.contains(container);
        }catch (PackageManager.NameNotFoundException e){
            return false;
        } catch (Exception f){
            return false;
        }
    }

    public boolean hasResource(Context context, String type, String name){
        try {
            String packageName = "com.android.systemui";
            Resources res = context.getPackageManager().getResourcesForApplication(packageName);
            int id = res.getIdentifier(name, type, packageName);
            return id != 0;
        }catch (PackageManager.NameNotFoundException e){
            return false;
        }
    }

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

    public Element getLastChildElement(Node parent){
        Element myElement = null;
        NodeList childs = parent.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                myElement = (Element) child;
            }
        }
        return myElement;
    }


    @Nullable
    public Element findElementById(Document document, String idName){
        return findElementById(document.getDocumentElement(), idName);
    }

    @Nullable
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

    @Nullable
    public List<Element> findElementsById(Document document, String idName){
        return findElementsById(document.getDocumentElement(), idName, new ArrayList<Element>());
    }

    public List<Element> findElementsById(Element parentElement, String idName, List<Element> elements){
        //if (parentElement.getAttribute(X_ID).equals(idName)) elements.add(parentElement);


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

    public ArrayList<Element> findElementsByTag(Document document, String tagName){
        return findElementsByTag(document.getDocumentElement(), tagName, new ArrayList<>());
    }

    public ArrayList<Element> findElementsByTag(Element parent, String tagName, ArrayList<Element> elements){
        if (parent.getTagName().equals(tagName))
            elements.add(parent);
        NodeList es = parent.getElementsByTagName(tagName);
        for (int i=0; i<es.getLength(); i++)
            elements.add((Element) es.item(i));

        return elements;
    }

    public Element findElementByTag(Document document, String tagName){
        return findElementByTag(document.getDocumentElement(), tagName);
    }

    private Element findElementByTag(Element parentElement, String tagName){
        if (parentElement.getTagName().equals(tagName)) return parentElement;

        NodeList elements = parentElement.getElementsByTagName(tagName);
        return (Element) elements.item(0);
    }

    @Nullable
    public Element findElementByTagAttr(Element parentElement, String tagName, String attrName, String attrValue){
        if (isTheTagElement(parentElement, tagName, attrName, attrValue))
            return parentElement;

        NodeList elements = parentElement.getElementsByTagName(tagName);
        for (int i = 0; i<elements.getLength(); i++){
            Element element = (Element) elements.item(i);
            if (isTheTagElement(element, tagName, attrName, attrValue))
                return element;
        }
        return null;
    }

    public Element createViewElement(Document doc){
        Element view = doc.createElement("View");
        view.setAttribute("android:visibility", "invisible");
        view.setAttribute(X_LAYOUT_WIDTH, "0.0dip");
        view.setAttribute(X_LAYOUT_HEIGHT, "fill_parent");
        view.setAttribute(X_WEIGHT, "1.0");

        return view;
    }

    private boolean isTheTagElement(Element element, String elementTag, String attrName, String attrValue){
        return element.getTagName().equals(elementTag) && element.getAttribute(attrName).equals(attrValue);
    }

    private boolean isTheElement(Element element, String layoutTag, String idName){
        return isTheElement(element, layoutTag, idName, X_ID);
    }

    private boolean isTheElement(Element element, String layoutTag, String idName, String attributeName){
        if (element.hasAttribute("android:tag") && !element.getTagName().equals(layoutTag)) return false;
        if (!element.hasAttribute(attributeName)) return false;

        Attr attr = element.getAttributeNode(attributeName);
        return attr != null && attr.getValue().equals(idName);
    }

    public void removeElement(Element element){
        if (element.getParentNode() != null)
            element.getParentNode().removeChild(element);
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

    public Document replaceAt(Document doc) throws Exception{
        findAbnormallyLongGravity(doc.getDocumentElement());
        doc = replaceStuffInXml(doc,
                new String[]{"@+", "@", "@*com.android.systemui:android"},
                new String[]{"@", "@*com.android.systemui:", "@*android"});
        return doc;
    }

    public Document replaceStuffInXml(Document doc, String[] old, String[] news) throws Exception{
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String output = writer.getBuffer().toString();
        for (int i=0; i<old.length; i++)
            output = output.replace(old[i], news[i]);
        doc = stringToDom(output);
        doc.normalizeDocument();
        return doc;
    }

    public String domToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String output = writer.getBuffer().toString();
        return output;
    }

    public static Document stringToDom(String xmlSource)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
    }

    public void writeType2Desc(String desc, String path){
        File file = new File(path);
        try {
            FileUtils.writeStringToFile(file, desc, "utf-8", false);
        }catch (IOException e){
            Log.e("klock", e.getMessage());
        }
    }

    private String pathToMerger = Environment.getExternalStorageDirectory() + "/K-Klock/tempF";
    private boolean hasAttrs = false;

    public void moveAttrsIfPresent(String xmlFolder) throws IOException{
        File attrs = new File(xmlFolder + "/attrs.xml");
        if (attrs.exists()){
            hasAttrs = true;
            File a = new File(pathToMerger + "/Rom.zip/assets/overlays/com.android.systemui/res/values");
            a.mkdirs();

            FileUtils.copyFileToDirectory(attrs, a);
        }
    }

    public void fixUpForAttrs(Document document){
        if (hasAttrs){
            Element rootElement = document.getDocumentElement();
            changeAttribute(rootElement, "xmlns:systemui", "http://schemas.android.com/apk/res-auto");
            changeAttribute(rootElement, "xmlns:tools", "http://schemas.android.com/tools");
            changeAttribute(rootElement, "xmlns:app", "http://schemas.android.com/apk/res-auto");
        }
    }

    public boolean isWeightedElement(Element element){
        return isPushyOutElement(element);
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
        boolean passedElement  = false;

        for (Element element : elements){
            if (idName == null){
                if (element.getTagName().equals(tagName))
                    break;
            }
            else if (isTheElement(element, tagName, idName))
                passedElement = true;
            if (passedElement)
                rightElements.add(element);
        }

        return rightElements;
    }

    public void insertBefore(Element toInsert, Element insertBefore){
        insertBefore.getParentNode().insertBefore(toInsert, insertBefore);
    }

    public ArrayList<Element> getLeftElementsTo(Element parentElement, Element divider){
        String tagName = null;
        String idName = null;
        if (divider.hasAttribute(X_ID))
            idName = divider.getAttribute(X_ID);
        else
            tagName = divider.getTagName();
        return getLeftElementsTo(parentElement, tagName, idName);
    }

    public ArrayList<Element> getLeftElementsTo(Element parentElement, String tagName, String idName){
        ArrayList<Element> elements = getChildElements(parentElement);
        ArrayList<Element> leftElements = new ArrayList<>();

        for (Element element : elements){
            if (idName == null){
                if (element.getTagName().equals(tagName))
                    break;
            }
            else if (isTheElement(element, tagName, idName))
                break;
            leftElements.add(element);
        }

        return leftElements;
    }

    public Document getDocument(File file) {
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(file);
        } catch (Exception e) {
            Log.e("klock", "Error getting document for file " + file.getName() + "\n" + e.getMessage());
        }

        return doc;
    }

    public String wrapInFont(String string){
        return "<font>" + string + "</font>";
    }

    public void writeResource(File file, String resourceType, String name, String value) throws Exception{
        writeResources(file, resourceType, new String[]{name}, new String[]{value});
    }

    public void writeResources(File file, String resourceType, String[] name, String[] values, String... arrayVals) throws Exception{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
        Document doc = documentBuilder.newDocument();
        Element root = doc.createElement("resources");
        doc.appendChild(root);
        if (resourceType.equals("string-array") || resourceType.equals("style")){
            Element array = doc.createElement(resourceType);
            array.setAttribute("name", arrayVals[0]);
            if (arrayVals.length > 1)
                array.setAttribute("parent", arrayVals[1]);
            root.appendChild(array);
            resourceType = "item";
            root = array;
        }
        for (int i=0; i<values.length; i++){
            Element newElement = doc.createElement(resourceType);
            newElement.setAttribute("name", name[i]);
            try {
                NodeList nodeList = stringToDom(values[i]).getChildNodes();
                for (int j = 0; j < nodeList.getLength(); j++)
                    newElement.appendChild(doc.importNode(nodeList.item(j), true));
            } catch (Exception e){
                newElement.setTextContent(values[i]);
            }
            root.appendChild(newElement);
        }
        writeDocToFile(doc, file);
    }

    public Document cloneDocument(Document doc) throws Exception{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
        Document cloneDoc = documentBuilder.newDocument();
        cloneDoc.appendChild(
                cloneDoc.importNode(doc.getDocumentElement(), true)
        );
        return cloneDoc;
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
            FileHelper fileHelper = new FileHelper(context);
            File[] files = baseFolder.listFiles();
            for (File file : files) {
                if (filenames.contains(file.getName())) {
                    int index = filenames.indexOf(file.getName());
                    fileHelper.renameFile(file, translated_filenames.get(index));
                } else if (file.getName().equals("type1a") && id_1a != 0) {
                    FileUtils.write(file, context.getString(id_1a), "utf-8", false);
                } else if (file.getName().equals("type1b") && id_1b != 0) {
                    FileUtils.write(file, context.getString(id_1b), "utf-8", false);
                } else if (file.getName().equals("type1c") && id_1c != 0) {
                    FileUtils.write(file, context.getString(id_1c), "utf-8", false);
                } else if (file.getName().equals("type2") && id_2 != 0) {
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

    private HashMap<Integer, String> getConstants(Class o) {
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