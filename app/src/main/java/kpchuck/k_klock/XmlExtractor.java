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
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.stericson.RootTools.RootTools;
import jadx.api.JadxDecompiler;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by Karol Przestrzelski on 29/08/2017.
 */

public class XmlExtractor {

    Context context;
    String destFolder = Environment.getExternalStorageDirectory() + "/K-Klock/apk";
    String dir;
    String apk = destFolder + "/SystemUI.apk";
    RelativeLayout rv;
    TextView tv;

    public XmlExtractor(Context context, RelativeLayout relativeLayout, TextView tv){
        this.context=context;
        this.rv=relativeLayout;
        this.tv=tv;
    }


    public void throwInTheDex(){
        new Dex(apk, destFolder, context, rv, tv).execute();
    }


    private File newFolder(String folder) {
        File file = new File(folder);
        if(!file.exists() || !file.isDirectory()) file.mkdirs();
        return file;
    }



}

class Dex extends AsyncTask<Void, Void, Void>{

    String apk;
    String destFolder;
    Context context;
    RelativeLayout rv;
    TextView tv;
    String dir;

    ErrorHandle err = new ErrorHandle();

    public Dex(String apk, String destFolder, Context context, RelativeLayout relativeLayout, TextView tv){
        this.apk=apk;
        this.destFolder=destFolder;
        this.context=context;
        this.rv=relativeLayout;
        this.tv=tv;
    }

    public void pullApk(){
        try {
            if (new File(apk).exists())FileUtils.forceDelete(new File(apk));
            Process p = Runtime.getRuntime().exec("su");
        }catch (IOException e){
            Log.e("klock", e.getMessage());
            err.handleAsyncError(context, e, true);
        }
        newFolder(destFolder);

        dir = "/system/priv-app/SystemUI/SystemUI.apk";
        if(RootTools.exists(dir)){
            RootTools.copyFile(dir, destFolder + "/SystemUI.apk", false, false);
        }else{
            dir = "/system/priv-app/OPSystemUI/OPSystemUI.apk";
            if (RootTools.exists(dir)) RootTools.copyFile(dir, destFolder + "/SystemUI.apk", false, false);

        }
    }

    protected void onProgressUpdate(Integer...progress){

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        rv.setVisibility(View.VISIBLE);
        // Do something like display a progress bar
    }
    public Void doInBackground(Void...voids){
        String root = Environment.getExternalStorageDirectory() + "/K-Klock/";
        newFolder(root);
        newFolder(root + "apk");
        newFolder(root + "userInput");

        if (err.getAsyncError(context))cleanupFolders();
        if (err.getAsyncError(context))pullApk();

        if (err.getAsyncError(context))throwDexIntoAssets();
        if (err.getAsyncError(context))extract();
        if (err.getAsyncError(context))prepareXmls();

        return null;
    }


    public void cleanupFolders(){
        try{
            File apkFolder = new File(destFolder);
            File userInput = new File(Environment.getExternalStorageDirectory() + "/K-Klock/userInput");

            if (apkFolder.exists() && apkFolder.isDirectory())FileUtils.deleteDirectory(apkFolder);
            if (userInput.exists() && userInput.isDirectory())FileUtils.deleteDirectory(userInput);

        }catch (IOException e){
            Log.e("klock", e.getMessage());
            err.handleAsyncError(context, e, true);

        }
    }

    public void prepareXmls(){
        try{
            File xmlfolder = newFolder(Environment.getExternalStorageDirectory() + "/K-Klock/userInput");
            FileUtils.copyFileToDirectory(new File(destFolder + "/DecompiledApk/res/layout/status_bar.xml"), xmlfolder);
            FileUtils.copyFileToDirectory(new File(destFolder + "/DecompiledApk/res/layout/keyguard_status_bar.xml"), xmlfolder);

            String statusbar = xmlfolder + "/status_bar.xml";
            String keyguard = xmlfolder + "/keyguard_status_bar.xml";

            //Keyguard Changes
            replaceStuffInXml(keyguard,
                    "<com.android.systemui.statusbar.phone.KeyguardStatusBarView " + "xmlns:android=\"http://schemas.android.com/apk/res/com.android.systemui\"",
            "<com.android.systemui.statusbar.phone.KeyguardStatusBarView" +
                    "    xmlns:android=\"http://schemas.android.com/apk/res/android\"" +
                    "xmlns:systemui=\"http://schemas.android.com/apk/res/com.android.systemui\"");
            replaceStuffInXml(keyguard, "UNKNOWN_DATA_0x7f100170", "@dimen/status_bar_header_height_keyguard");
            replaceStuffInXml(keyguard, "UNKNOWN_DATA_0x7f1001d1", "@dimen/multi_user_switch_width_keyguard");
            replaceStuffInXml(keyguard, "UNKNOWN_DATA_0x7f1001d3", "@dimen/multi_user_avatar_keyguard_size");
            replaceStuffInXml(keyguard, "UNKNOWN_DATA_0x7f10016e", "@dimen/status_bar_header_height");
            replaceStuffInXml(keyguard, "UNKNOWN_DATA_0x7f100288", "@dimen/status_bar_height");
            replaceStuffInXml(keyguard, "?unknown_attr_ref: 1010042", "?android:attr/textAppearanceSmall");
            replaceStuffInXml(keyguard, "UNKNOWN_DATA_0x7f0c017a", "@dimen/status_bar_header_height_keyguard");
            replaceStuffInXml(keyguard, "UNKNOWN_DATA_0x7f0c01db", "@dimen/multi_user_switch_width_keyguard");
            replaceStuffInXml(keyguard, "UNKNOWN_DATA_0x7f0c01dd", "@dimen/multi_user_avatar_keyguard_size");
            replaceStuffInXml(keyguard, "UNKNOWN_DATA_0x7f0c0178", "@dimen/status_bar_header_height");
            replaceStuffInXml(keyguard, "UNKNOWN_DATA_0x7f0c0292", "@dimen/status_bar_height");

            replaceStuffInXml(keyguard, "systemui:", "android:");

            //StatusBar Changes
            replaceStuffInXml(statusbar,
                    "<com.android.systemui.statusbar.phone.PhoneStatusBarView " + "xmlns:android=\"http://schemas.android.com/apk/res/com.android.systemui\"",
                    "<com.android.systemui.statusbar.phone.PhoneStatusBarView" +
                            "    xmlns:android=\"http://schemas.android.com/apk/res/android\"" +
                            "xmlns:systemui=\"http://schemas.android.com/apk/res/com.android.systemui\"");
            replaceStuffInXml(statusbar, "UNKNOWN_DATA_0x7f10014d", "@dimen/status_bar_icon_size");
            replaceStuffInXml(statusbar, "UNKNOWN_DATA_0x1", "0dip");
            replaceStuffInXml(statusbar, "@r$layout/drawer_item", "@layout/emergency_cryptkeeper_text");
            replaceStuffInXml(statusbar, "UNKNOWN_DATA_0x7f0c0157", "@dimen/status_bar_icon_size");
            replaceStuffInXml(statusbar, "@r$id/slot_id_name", "@id/clock");
            replaceStuffInXml(statusbar, "@r$layout/drawer_category", "@layout/emergency_cryptkeeper_text");


            replaceStuffInXml(statusbar, "systemui:", "android:");

        }catch (IOException e){
            Log.e("klock", e.getMessage());
            err.handleAsyncError(context, e, true);

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
            err.handleAsyncError(context, e, true);

        }
    }

    public static Document stringToDom(String xmlSource)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
    }


    public void throwDexIntoAssets(){
        copyDexFromAssets();

        ZipUtil.unpack(new File(apk), newFolder(destFolder + "/SystemUI"));
        try {
            FileUtils.copyFileToDirectory(new File(destFolder + "/classes.dex"), new File(destFolder + "/SystemUI"));
            FileUtils.forceDelete(new File(apk));

            ZipUtil.pack(new File(destFolder + "/SystemUI"), new File(apk));
        }catch (IOException e){
            Log.e("klock", e.getMessage());
            err.handleAsyncError(context, e, true);

        }
    }
    public void extract(){
        try {
            File resDir = new File(destFolder + "/DecompiledApk");

            JadxDecompiler jadx = new JadxDecompiler();
            jadx.setOutputDir(resDir);
            jadx.loadFile(new File(apk));
            jadx.saveResources();
        }catch (Exception e){
            Log.e("klock", e.getMessage());
            err.handleAsyncError(context, e, true);

        }
    }

    private File newFolder(String folder) {
        File file = new File(folder);
        if(!file.exists() && !file.isDirectory()) file.mkdirs();
        return file;
    }

    private void copyDexFromAssets() {
        AssetManager assetManager = context.getAssets();

        java.io.InputStream in = null;
        java.io.OutputStream out = null;
        try {
            in = assetManager.open("classes.dex");
            out = new java.io.FileOutputStream(destFolder +"/classes.dex");
            copyFile(in, out);
        } catch(IOException e) {
            android.util.Log.e("klock", "Failed to copy asset file: " + destFolder + e.getMessage());
            err.handleAsyncError(context, e, true);

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

    private void copyFile(java.io.InputStream in, java.io.OutputStream out) throws java.io.IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

}
