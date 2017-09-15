package kpchuck.k_klock;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
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
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by Karol Przestrzelski on 04/09/2017.
 */

public class MoveNetworkIconsLeft {

    private Context context;

    String rootFolder = Environment.getExternalStorageDirectory() + "/K-Klock";
    String slash = "/";
    File mergerFolder = new File(rootFolder + slash + "temp2" + slash + "merge");
    String rootApkPath = mergerFolder.getAbsolutePath() + "/assets/overlays/com.android.systemui";
    String systemicons = "system_icons.xml";
    String statusbar = "status_bar.xml";
    String prefFile = "prefFileName";
    String romName;


    public MoveNetworkIconsLeft(Context context){
        this.context=context;
        getPref();
        if (!romName.equals(context.getString(R.string.otherRomsBeta)))copySystemIconsAssets("systemicons", romName);
        else copyFromUserInput();

        editSystemIcons();
        editStatusBar();

    }

    private void copyFromUserInput(){
        try{
            String userInputPath = rootFolder + "/userInput/";
            File file = new File(userInputPath + systemicons);
            FileUtils.copyFileToDirectory(file, new File(rootApkPath + "/res/layout"));

        }catch (IOException e){
            Log.e("klock", e.getMessage());
        }
    }

    private void editStatusBar(){
        File rootFile = new File(rootApkPath);
        File[] files = rootFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for(File s: files){
            File file = new File(s.getAbsolutePath() + slash + "layout/" +  statusbar);
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(file);

                Element resources = doc.getDocumentElement();
                NodeList list = resources.getElementsByTagName("LinearLayout");
                Element layout = null;
                for (int i=0;i<list.getLength();i++){
                    layout = (Element) list.item(i);
                    Attr attr = layout.getAttributeNode("android:id");
                    if (attr.getValue().equals("@*com.android.systemui:id/status_bar_contents")) break;
                    else layout = null;
                }


               Node childNode = layout.getFirstChild();
                Element testElement = null;
                boolean hasLL = false;
                boolean tobreak = false;
                while( !tobreak) {
                    childNode = childNode.getNextSibling();
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element childElement = (Element) childNode;
                        if (childElement.getTagName().equals("LinearLayout") && childElement.hasChildNodes()){
                            hasLL = true;
                            testElement = childElement;
                        }
                        else {hasLL=false;}
                        tobreak = true;
                    }
                }

                if (!hasLL) {
                    NodeList findtv = layout.getElementsByTagName("TextView");
                    testElement = (Element) findtv.item(0);
                }


                Element toInclude = doc.createElement("include");
                toInclude.setAttribute("android:layout_width", "wrap_content");
                toInclude.setAttribute("android:layout_height", "fill_parent");
                toInclude.setAttribute("android:layout_marginStart", "@*com.android.systemui:dimen/signal_cluster_margin_start");
                toInclude.setAttribute("layout", "@*com.android.systemui:layout/signal_cluster_view");
                toInclude.setAttribute("android:gravity", "center_vertical");

                if (hasLL)testElement.appendChild(toInclude);
                if(!hasLL)layout.insertBefore(toInclude, testElement);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);

                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
            }catch (Exception e){
                Log.e("klock", e.getMessage());

            }

        }

    }

    private void editSystemIcons(){
        File sysicons = new File(rootApkPath + "/res/layout/" + systemicons);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(sysicons);


            Element rootElement = doc.getDocumentElement();
            NodeList list = rootElement.getElementsByTagName("include");
            Element includeElement = (Element) list.item(0);

            rootElement.removeChild(includeElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(sysicons);
            transformer.transform(source, result);

            replaceStuffInXml(sysicons.getAbsolutePath(), "@", "@*com.android.systemui:");
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

            StreamResult result = new StreamResult(file);
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
        SharedPreferences myPref = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        String rom = myPref.getString("selectedRom", context.getString(R.string.chooseRom));
        this.romName=rom;
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
                    File outFile = new File(rootApkPath + "/res/layout/" + systemicons);
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

    private void copyFile(java.io.InputStream in, java.io.OutputStream out) throws java.io.IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
