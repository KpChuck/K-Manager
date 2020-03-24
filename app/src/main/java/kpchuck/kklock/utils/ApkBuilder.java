package kpchuck.kklock.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.android.apksig.ApkSigner;
import com.android.apksig.apk.ApkFormatException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import brut.androlib.AndrolibException;
import kpchuck.kklock.Checks;
import kpchuck.kklock.R;
import kpchuck.kklock.xml.XmlCreation;
import kpchuck.kklock.xml.XmlUtils;
import kpchuck.kklock.xml.XmlWork;

import static kpchuck.kklock.constants.PrefConstants.*;

/**
 * Created by karol on 04/12/17.
 */

public class ApkBuilder extends AsyncTask<Void, String, Void>{

    String rootFolder = Environment.getExternalStorageDirectory() + "/K-Klock";

    private Context context;
    private RelativeLayout relativeLayout;
    private TextView tv;
    private RelativeLayout defaultLayout;
    private FileHelper fileHelper;
    private PrefUtils prefUtils;
    private File mergerFolder;
    private File tempFolder;
    private String univ = "universal";
    private static String apkVersion = "K-Klock.apk";

    public ApkBuilder(Context context, RelativeLayout relativeLayout, TextView textView, RelativeLayout defaultLayout){
        this.fileHelper = new FileHelper(context);
        this.context = context;
        this.prefUtils = new PrefUtils(context);
        this.relativeLayout = relativeLayout;
        this.tv = textView;
        this.defaultLayout = defaultLayout;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        relativeLayout.setVisibility(View.VISIBLE);
        tv.setText(R.string.apkBuilderLoading);
    }


    public long time(){
        return System.currentTimeMillis();
    }


    @Override
    protected Void doInBackground(Void... voids) {

        doStuff();

        try {
            makeDirs();
            publishProgress(context.getString(R.string.decompiling));
            decompileSysUI();
            publishProgress(context.getString(R.string.apkBuilderLoading));
            //TODO Fix translating signing
            //translateAll();
            ExtractAssets();
            modTheRomZip();
            appendOptionsZip();// Takes long about 10s
            // Takes long about 5s
            publishProgress(context.getString(R.string.signing));
            createApkFromDir(new File(mergerFolder, "universalFiles.zip"));

            publishProgress(context.getString(R.string.cleaning_files));

            cleanup();


            publishProgress(context.getString(R.string.installing_apk));
            File apk = new File(rootFolder, apkVersion);
            SuUtils su = new SuUtils();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (!prefs.getBoolean("installSilently", true) || !su.hasRoot()) {
                fileHelper.installApk(apk);
            } else {
                String install = String.format("cp %s/K-Klock/K-Klock.apk /data/local/tmp/ && pm install -r /data/local/tmp/K-Klock.apk", Environment.getExternalStorageDirectory().getPath());
                String output = su.runSuCommand(install).toString();
                if (!output.contains("Success"))
                    fileHelper.installApk(apk);
            }
            showSnackbar();

        } catch (AndrolibException e) {
            Log.e("klock", "Error decompiling SystemUI.apk " + e.getMessage());
            throw new RuntimeException("Apktool error:", e);
        }  catch (Exception e){
            Log.e("klock", "Error: ", e);
            File errormsg = new File(Environment.getExternalStorageDirectory() + "/K-Klock/error.txt");
            try{
                if (errormsg.exists())errormsg.delete();
                FileUtils.writeStringToFile(errormsg, e.getMessage() + "\n", "utf-8", true);
                FileUtils.writeStringToFile(errormsg, e.getCause() + "\n", "utf-8", true);

                for (StackTraceElement s : e.getStackTrace()){
                    FileUtils.writeStringToFile(errormsg, s.toString() + "\n" , "utf-8", true);

                }
            } catch (IOException q){
                 Log.e("klock", q.getMessage());
            }
            StackTraceElement[] message = e.getStackTrace();
            StringBuilder builder = new StringBuilder();
            for (StackTraceElement stackTraceElement : message) {
                builder.append(stackTraceElement);
                builder.append("\n");
            }

            throw new RuntimeException("Error modding rom I think: \n" + builder.toString() + e);

        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        tv.setText(values[0]);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        relativeLayout.setVisibility(View.GONE);
        tv.setText("");
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        relativeLayout.setVisibility(View.GONE);
        tv.setText("");

    }

    private void decompileSysUI() throws Exception{

        File inputDir = new File(rootFolder, "inputFiles");
        if (!prefUtils.getBool(R.string.key_decompile_everytime)){
            // Check if all xmls are present first though
            String[] xmlNames = {"status_bar.xml", "keyguard_status_bar.xml", "system_icons.xml", "quick_status_bar_expanded_header.xml"};
            String[] presentXml = inputDir.list(fileHelper.XML);
            if (Arrays.asList(presentXml).containsAll(Arrays.asList(xmlNames)))
                return;
        }
        if (inputDir.exists() && inputDir.listFiles() != null) {
            for (File file : inputDir.listFiles())
                file.delete();
        }

        File sysui = new File(inputDir, "SystemUI.apk");
        if (!sysui.exists()) {
            PackageInfo m = context.getPackageManager().getPackageInfo("com.android.systemui", 0);
            String src = m.applicationInfo.sourceDir;
            FileUtils.copyFile(new File(src), sysui);
        }

        ApkDecompiler decompiler = new ApkDecompiler();
        decompiler.decompile(context.getPackageManager(), sysui);
    }

    private void showSnackbar() {
        Snackbar snackbar = Snackbar.make(defaultLayout, context.getString(R.string.open_in_substratum), Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.open, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final String SUBSTRATUM_PACKAGE_NAME = "projekt.substratum";
                        final String SUBSTRATUM_LITE_PACKAGE_NAME =  "projekt.substratum.lite";
                        fileHelper.openInstalledPackage(SUBSTRATUM_PACKAGE_NAME, SUBSTRATUM_LITE_PACKAGE_NAME);
                    }
                });

        snackbar.show();

    }

    public void makeDirs (){
        mergerFolder = new File(rootFolder + "/merger");
        tempFolder = new File(rootFolder + "/tempF");

        fileHelper.newFolder(mergerFolder.getAbsolutePath());
        fileHelper.newFolder(tempFolder.getAbsolutePath());

    }

    public void ExtractAssets () {
        // Copy files used for every apk first
        fileHelper.copyFromAssets(univ, "universalFiles.zip", mergerFolder, context, true);
        // Copy optional files into the tempfolder
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>(){{
            put(PREF_RECENTS, "recents.zip");

        }};
        for (String key: hashMap.keySet()){
            checkPrefAndCopy(key, hashMap.get(key));
        }
    }

    private void checkPrefAndCopy(String key, String zipName){
        if(prefUtils.getBool(key))
            fileHelper.copyFromAssets(univ, zipName, tempFolder, context, true);
    }

    private void modTheRomZip() throws Exception{
        new XmlWork(context);
    }

    public void appendOptionsZip () throws IOException{

        File[] zipFiles = tempFolder.listFiles(fileHelper.ZIP);
        File univF = new File(mergerFolder, "universalFiles.zip");

        for (File f : zipFiles){
            for (File file : f.listFiles()){
                if (file.isDirectory()) FileUtils.copyDirectoryToDirectory(file, univF);
                else FileUtils.copyFileToDirectory(file, univF);

            }
        }
        ZipUtil.unexplode(univF);
    }

    public void createApkFromDir(File universalZip) throws IOException, GeneralSecurityException, ApkFormatException{

        File signedApk = new File(rootFolder, apkVersion);

        File key = new File(Environment.getExternalStorageDirectory() + "/K-Manager/key");
        char[] keyPass = "overlay".toCharArray();

        if (!key.exists()) {
            Log.d("klock", "Loading keystore...");
            fileHelper.copyFromAssets(context, "key", key);
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(key), keyPass);
        PrivateKey privateKey = (PrivateKey) keyStore.getKey("key", keyPass);
        List<X509Certificate> certs = new ArrayList<>();
        certs.add((X509Certificate) keyStore.getCertificateChain("key")[0]);

        ApkSigner.SignerConfig signerConfig =
                new ApkSigner.SignerConfig.Builder("overlay", privateKey, certs).build();
        List<ApkSigner.SignerConfig> signerConfigs = new ArrayList<>();
        signerConfigs.add(signerConfig);
        new ApkSigner.Builder(signerConfigs)
                .setV1SigningEnabled(false)
                .setV2SigningEnabled(true)
                .setInputApk(new File(universalZip.getAbsolutePath()))
                .setOutputApk(new File(signedApk.getAbsolutePath()))
                .setMinSdkVersion(Build.VERSION.SDK_INT)
                .build()
                .sign();

    }

    public void cleanup () throws IOException{

        List<File> files = Arrays.asList(tempFolder, mergerFolder);
        for (File f: files){
            if (f.exists()) FileUtils.deleteDirectory(f);
        }

    }

    private void doStuff() {
        boolean b = new Checks().getSelfVerifiedPirateTools(context);
        if (b){
            String eplan = context.getString(R.string.lucky_patcher_message);
            String[] e = eplan.split(" ");
            try {
                for (String k : e) {
                    publishProgress(k);
                    Thread.sleep(750);
                }
            }catch (InterruptedException c){
                Log.d("klock", "hi");
            }
        }
    }

    private void translateAll(){
        XmlUtils xmlUtils = new XmlUtils();
        translate(xmlUtils, "ampm", ".statusbars", R.array.statusbars_type1b, 0, R.string.statusbars_type1b, 0, 0, "type1b");
        translate(xmlUtils, "clockSize", ".tiles", R.array.tiles_type1c, 0, 0, R.string.tiles_type1c, 0, "type1c");
        translate(xmlUtils, "colorIcons", ".statusbars", R.array.included_icons_title, 0, 0, R.string.statusbars_type1c, 0, "type1c");
        translate(xmlUtils, "hideStatusbar", ".statusbars", R.array.statusbars_type1a, R.string.statusbars_type1a, 0, 0, 0, "type1a");
        translate(xmlUtils, "indicatorsN", ".statusbars", R.array.statusbars_type2, 0, 0, 0, R.string.statusbars_type2, "type2");
        translate(xmlUtils, "indicators0", ".statusbars", R.array.statusbars_type2, 0, 0, 0, R.string.statusbars_type2, "type2");
        translate(xmlUtils, "lock_clock", ".headers", R.array.headers_type1b, 0, R.string.headers_type1b, 0, 0, "type1b");
        translate(xmlUtils, "qsTiles", ".tiles", R.array.tiles_type2, 0, 0, 0, R.string.tiles_type2, "type2");
        translate(xmlUtils, "qsTitle", ".tiles", R.array.tiles_type1a, R.string.tiles_type1a, 0, 0, 0, "type1a");
        translate(xmlUtils, "recents", ".tiles", R.array.tiles_type1b, 0, R.string.tiles_type1b, 0, 0, "type1b");
        translate(xmlUtils, "timeout", ".headers", R.array.headers_type1a, R.string.headers_type1a, 0, 0, 0, "type1a");

        translate(xmlUtils, mergerFolder, "universalFiles", "", R.array.included_colors_title, R.string.sysui_type1a, 0, 0, 0, "type1a");
        translate(xmlUtils, mergerFolder, "universalFiles", "", R.array.included_formats_title, 0, R.string.sysui_type1b, 0, 0, "type1b");
        translate(xmlUtils, mergerFolder, "universalFiles", "", R.array.font_names, 0, 0, R.string.sysui_type1c, 0, "type1c");


    }

    private void translate(XmlUtils xmlUtils, String zipName, String ending, int id_array, int id_1a, int id_1b, int id_1c, int id_2, String starter){
        translate(xmlUtils, tempFolder, zipName, ending, id_array, id_1a, id_1b, id_1c, id_2, starter);
    }

    private void translate(XmlUtils xmlUtils, File folderName, String zipName, String ending, int id_array, int id_1a, int id_1b, int id_1c, int id_2, String starter){
        File base = new File(folderName, zipName + ".zip/assets/overlays/com.android.systemui" + ending);
        if (!base.exists()) return;
        String end = "";
        if (id_2 == 0) end = ".xml";
        xmlUtils.translate(context, base, xmlUtils.substratize(xmlUtils.getEngArray(context, id_array), starter, end),
                xmlUtils.substratize(xmlUtils.getArray(context, id_array), starter, end), id_1a, id_1b, id_1c, id_2);
    }
}
