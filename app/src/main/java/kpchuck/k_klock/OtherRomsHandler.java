package kpchuck.k_klock;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zeroturnaround.zip.ZipUtil;


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

import kpchuck.k_klock.Utils.PrefUtils;

/**
 * Created by Karol Przestrzelski on 17/08/2017.
 */

public class OtherRomsHandler extends AsyncTask<Void,Void, Void>{

    private String slash = "/";

    private String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock";
    File topA = newFolder(rootFolder + slash + "tempF");
    File topF = newFolder(topA.getAbsolutePath() + "/Rom.zip");
    private String xmlFolder = rootFolder + slash + "userInput";
    private String tag = "klock";

    Context context;
    public OtherRomsHandler(Context context){
        this.context=context;

    }

    @Override
    protected Void doInBackground(Void... voids){
        try {
            makeMergerFolder();
            new editKeyguard().editKeyguard(context);
            new editStatusBar().Execution(context);
            writeType2Desc();

        }catch (Exception e){
            Log.e("klock", e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }


    public void shortToast(String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public boolean checkForXmls(){
        boolean test = false;
        try {
            File xmlfolder = newFolder(xmlFolder);
            PrefUtils prefUtils = new PrefUtils(context);

            boolean statusbar = FileUtils.directoryContains(xmlfolder, new File(xmlFolder + slash + "status_bar.xml"));
            boolean keyguard = FileUtils.directoryContains(xmlfolder, new File(xmlFolder + slash + "keyguard_status_bar.xml"));
            boolean systemicons = FileUtils.directoryContains(xmlfolder, new File(xmlFolder + slash + "system_icons.xml"));

            if (!statusbar) shortToast("Sorry, status_bar.xml not in /sdcard/K-Klock/userInput.");
            if (!keyguard) shortToast("Sorry, keyguard_status_bar.xml not in /sdcard/K-Klock/userInput.");
            if (!systemicons) shortToast("Sorry, system_icons.xml not in /sdcard/K-Klock/userInput.");

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
