package kpchuck.k_klock.xml;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import kpchuck.k_klock.R;
import kpchuck.k_klock.editKeyguard;
import kpchuck.k_klock.editStatusBar;
import kpchuck.k_klock.utils.FileHelper;
import kpchuck.k_klock.utils.PrefUtils;

/**
 * Created by Karol Przestrzelski on 17/08/2017.
 */

public class OtherRomsHandler extends AsyncTask<Void,Void, Void>{

    private String slash = "/";

    private String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock";
    File topA = newFolder(rootFolder + slash + "tempF");
    File topF = new File(topA.getAbsolutePath() + "/Rom.zip");
    private String xmlFolder = rootFolder + slash + "userInput";
    private String tag = "klock";
    private boolean hasAttrs = false;

    private Context context;
    public OtherRomsHandler(Context context, boolean hasAttrs){
        this.context=context;
        this.hasAttrs = hasAttrs;

    }

    @Override
    protected Void doInBackground(Void... voids){
        try {
            makeMergerFolder();
            moveAttrsIfPresent();
            new editKeyguard().editKeyguard(context, hasAttrs);
            new editStatusBar().Execution(context, hasAttrs);
            writeType2Desc();

        }catch (Exception e){
            Log.e("klock", e.getMessage());
        }
        return null;
    }


    public void shortToast(String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void moveAttrsIfPresent(){
        File attrs = new File(xmlFolder + "/attrs.xml");
        if (attrs.exists()){
            this.hasAttrs = true;
            Log.d("klock", "Moving attrs.xml to rom.zip");
            FileHelper fileHelper = new FileHelper();
            File a = fileHelper.newFolder(topF + "/assets/overlays/com.android.systemui/res/values");
            try{
                FileUtils.copyFileToDirectory(attrs, a);
            }catch (IOException e){
                Log.e("klock", e.getMessage());
            }
        }
    }

    public Document fixUpForAttrs(Document document){
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

    public boolean checkForXmls(){
        boolean test = false;
        try {
            File xmlfolder = newFolder(xmlFolder);
            PrefUtils prefUtils = new PrefUtils(context);

            boolean statusbar = FileUtils.directoryContains(xmlfolder, new File(xmlFolder + slash + "status_bar.xml"));
            boolean keyguard = FileUtils.directoryContains(xmlfolder, new File(xmlFolder + slash + "keyguard_status_bar.xml"));
            boolean systemicons = FileUtils.directoryContains(xmlfolder, new File(xmlFolder + slash + "system_icons.xml"));

            if (!statusbar) shortToast(String.format(context.getString(R.string.xml_not_present_for_otherroms), "status_bar.xml"));
            if (!keyguard) shortToast(String.format(context.getString(R.string.xml_not_present_for_otherroms), "keyguard_status_bar.xml"));
            if (!systemicons) shortToast(String.format(context.getString(R.string.xml_not_present_for_otherroms), "system_icons.xml"));

            String romName = prefUtils.getString("selectedRom", "");
            if (romName.equals(context.getString(R.string.otherRomsBeta))){
                test = statusbar && keyguard && systemicons;
            }
            else {
                test = statusbar && keyguard;
            }

        }catch(IOException e){

        }
        return test;
    }

    public void makeMergerFolder(){
        File topA = newFolder(rootFolder + slash + "tempF");
        File topF = newFolder(topA.getAbsolutePath() + "/Rom.zip");
       // File mergerFolder = newFolder(rootFolder + slash + "temp2" + slash+ "merge");
     //   String f = mergerFolder.getAbsolutePath();

        newFolder(topF + slash + "assets");
        newFolder(topF + "/assets/overlays");
        File s = newFolder(topF + "/assets/overlays/com.android.systemui");
        String t = s.getAbsolutePath();
        String[] startFolder = {"res", "type2_Clock_on_Lockscreen_Right", "type2_Clock_on_Lockscreen_Left", "type2_No_Clock_on_Lockscreen_Center",
                "type2_No_Clock_on_Lockscreen_Right", "type2_No_Clock_on_Lockscreen_Left", "type2_Stock_Clock_Center", "type2_Stock_Clock_Left",
                "type2_Stock_Clock_Right"};
        for(String k :startFolder){
            newFolder(t + slash + k);
            newFolder(t + slash + k + "/layout") ;

        }

    }

    public void writeType2Desc(){
        File file = new File(topF + "/assets/overlays/com.android.systemui/type2");
        String data = "Clock Style (Clock on Lockscreen Center";
        try {
            FileUtils.writeStringToFile(file, data);
        }catch (IOException e){
            Log.e("klock", e.getMessage());
        }
    }

    public File newFolder(String filePath){
        File folder = new File(filePath);
        if (!folder.exists() || !folder.isDirectory())folder.mkdirs();
        return folder;
    }

    public Document replaceAt(Document doc){
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
            output = output.replace("@", "@*com.android.systemui:");
            doc = stringToDom(output);
            doc.normalizeDocument();

        }catch (Exception e){Log.e(tag, e.getMessage());
        }return doc;
    }

    public static Document stringToDom(String xmlSource)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
    }

}
