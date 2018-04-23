package kpchuck.kklock.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.InterstitialAd;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import jadx.api.JadxDecompiler;
import jadx.core.utils.exceptions.JadxException;
import kellinwood.security.zipsigner.ZipSigner;
import kpchuck.kklock.R;
import kpchuck.kklock.xml.XmlCreation;
import kpchuck.kklock.xml.XmlWork;

import static kpchuck.kklock.constants.PrefConstants.*;

/**
 * Created by karol on 04/12/17.
 */

public class ApkBuilder extends AsyncTask<String, String, String>{

    String slash = "/";
    String rootFolder = Environment.getExternalStorageDirectory() + slash + "K-Klock";

    private Context context;
    private RelativeLayout relativeLayout;
    private TextView tv;
    private RelativeLayout defaultLayout;
    private boolean hasAllXmls = false;
    private FileHelper fileHelper;
    private PrefUtils prefUtils;
    private File mergerFolder;
    private File tempFolder;
    private String univ = "universal";

    public ApkBuilder(Context context, RelativeLayout relativeLayout, TextView textView, RelativeLayout defaultLayout, boolean xmls){
        this.fileHelper = new FileHelper();
        this.context = context;
        this.prefUtils = new PrefUtils(context);
        this.relativeLayout = relativeLayout;
        this.tv = textView;
        this.defaultLayout = defaultLayout;
        this.hasAllXmls= xmls;
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
    protected String doInBackground(String... apkVersion) {

        try {
            makeDirs();
            ExtractAssets();
            if (!hasAllXmls && isOtherRoms(prefUtils.getString(PREF_SELECTED_ROM, ""))) {
                publishProgress("Decompiling Apk...");

                File sysui = new File(Environment.getExternalStorageDirectory() + "/K-Klock/userInput/SystemUI.apk");
                SuUtils suUtils = new SuUtils();
                if (!sysui.exists()) {
                    suUtils.runSuCommand("cp /system/priv-app/$(ls /system/priv-app | grep SystemUI)/*.apk /sdcard/K-Klock/userInput/SystemUI.apk");
                }
                decompileSysUI(sysui);
            }
            publishProgress(context.getString(R.string.apkBuilderLoading));
            modTheRomZip();
            insertCustomXmls();
            dealWivQsBg();
            appendOptionsZip();// Takes long about 10s
            // Takes long about 5s
            publishProgress("Signing K-Klock...");
            createApkFromDir(new File(mergerFolder, "universalFiles.zip"), apkVersion[0]);

            publishProgress("Cleaning files...");

            cleanup();


            publishProgress("Installing K-Klock...");
            File apk = new File(rootFolder + slash + apkVersion[0]);
            SuUtils su = new SuUtils();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (!prefs.getBoolean("installSilently", true) || !su.hasRoot()) {
                FileHelper fh = new FileHelper();
                showSnackbar();
                fh.installApk(apk, context);
            } else {
                String install = "pm install -r /sdcard/K-Klock/" + apkVersion[0];
                String output = su.runSuCommand(install).toString();
                if (output.contains("Success")) showSnackbar();
            }

        } catch (JadxException e) {
            Log.e("klock", "Error decompiling SystemUI.apk " + e.getMessage());
            throw new RuntimeException("Jadx error I think: \n" + e);
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
            throw new RuntimeException("Error modding rom I think: \n" + e);

        }

        return apkVersion[0];
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
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        relativeLayout.setVisibility(View.GONE);
        tv.setText("");

    }

    private void decompileSysUI(File sysui) throws JadxException, IOException{

        JadxDecompiler jadx = new JadxDecompiler();

        File kManager = fileHelper.newFolder(Environment.getExternalStorageDirectory() + "/K-Manager/");

        if (ZipUtil.containsEntry(sysui, "classes.dex")){
            ZipUtil.removeEntry(sysui, "classes.dex");
        }

        File resOut = new File(kManager, "res_out");
        jadx.setSources(true);
        jadx.loadFile(sysui);
        jadx.setOutputDir(kManager);
        jadx.setOutputDirRes(resOut);
       // jadx.setWhichXmls(new String[]{"status_bar.xml", "system_icons.xml", "keyguard_status_bar.xml", "resources.arsc"});
        jadx.save();
        List<File> xmls = new ArrayList<>();
        xmls.add(new File(resOut, "res/layout/status_bar.xml"));
        xmls.add(new File(resOut, "res/layout/keyguard_status_bar.xml"));
        xmls.add(new File(resOut, "res/layout/system_icons.xml"));
        xmls.add(new File(resOut, "res/values/attrs.xml"));
        xmls.add(new File(resOut, "res/layout/quick_status_bar_expanded_header.xml"));
        File userInput = sysui.getParentFile();
        for (File f : xmls){
            FileUtils.copyFileToDirectory(f, userInput);
        }

    }

    private void showSnackbar() {
        Snackbar snackbar = Snackbar.make(defaultLayout, "Open K-Klock in Substratum", Snackbar.LENGTH_INDEFINITE)
                .setAction("Open", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final String SUBSTRATUM_PACKAGE_NAME = "projekt.substratum";
                        final String THEME_PACKAGE_NAME = "com.kpchuck.kklock";

                        Intent intentActivity = new Intent();
                        intentActivity = intentActivity.setClassName(SUBSTRATUM_PACKAGE_NAME, "projekt.substratum.activities.launch.ThemeLaunchActivity");

                        intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intentActivity.putExtra("package_name", THEME_PACKAGE_NAME);
                        intentActivity.setAction("projekt.substratum.THEME");
                        intentActivity.setPackage(THEME_PACKAGE_NAME);
                        intentActivity.putExtra("calling_package_name", THEME_PACKAGE_NAME);
                        intentActivity.putExtra("oms_check", true);
                        intentActivity.putExtra("theme_mode", (String) null);
                        intentActivity.putExtra("notification", false);
                        intentActivity.putExtra("hash_passthrough", true);
                        intentActivity.putExtra("certified", false);


                        context.startActivity(intentActivity);
                    }
                });

        snackbar.show();

    }

    public void makeDirs (){
        this.mergerFolder = new File(rootFolder + "/merger");
        this.tempFolder = new File(rootFolder + "/tempF");

        fileHelper.newFolder(mergerFolder.getAbsolutePath());
        fileHelper.newFolder(tempFolder.getAbsolutePath());

    }

    public void ExtractAssets () throws IOException{
        File customInput = new File(rootFolder + "/customInput");
        String romName = prefUtils.getString(PREF_SELECTED_ROM, context.getString(R.string.chooseRom));
        // Copy files used for every apk first
        fileHelper.copyFromAssets(univ, "universalFiles.zip", mergerFolder, context, false);
        // Copy optional files into the tempfolder
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>(){{
            put(PREF_RECENTS, "recents.zip");
            put(PREF_QS, "qsTiles.zip");
            put(PREF_ICON, "colorIcons.zip");
            put(PREF_LOCKSCREEN_STATUSBAR_SIZE, "hideStatusbar.zip");
            put(PREF_QS_LABEL, "qsTitle.zip");
            put(PREF_AM, "ampm.zip");
            put(PREF_STATUSBAR_CLOCK_SIZE, "clockSize.zip");
            put(PREF_LOCK_CLOCK, "lock_clock.zip");
            put(PREF_HEADS_UP, "timeout.zip");

        }};
        for (String key: hashMap.keySet()){
            checkPrefAndCopy(key, hashMap.get(key));
        }

        if(prefUtils.getBool(PREF_INDICATORS) && romName.equals("OxygenOS Nougat"))
            fileHelper.copyFromAssets(univ, "indicatorsN.zip".trim(), tempFolder, context, true);
        if(prefUtils.getBool(PREF_INDICATORS) && romName.startsWith("OxygenOS Oreo"))
            fileHelper.copyFromAssets(univ, "indicatorsO.zip".trim(), tempFolder, context, true);

        // Copy the rom specific file if a rom was selected
        if (!romName.equals(context.getString(R.string.otherRomsBeta))) {
            fileHelper.copyFromAssets("romSpecific", romName + ".zip".trim(), customInput, context, true);
            File zip = new File(customInput, romName + ".zip");
            for (File x : zip.listFiles()){
                FileUtils.copyFileToDirectory(x, customInput);
            }
            FileUtils.deleteDirectory(zip);
        }
    }

    private void checkPrefAndCopy(String key, String zipName){
        if(prefUtils.getBool(key))
            fileHelper.copyFromAssets(univ, zipName, tempFolder, context, true);
    }

    private void modTheRomZip() throws Exception{
        String rom = prefUtils.getString(PREF_SELECTED_ROM, "");

        new XmlWork(context,
                isOtherRoms(rom) ? "userInput" : "customInput",
                hideClock(rom),
                shouldBeDynamic(rom));
    }

    private boolean shouldBeDynamic(String rom){
        boolean inWhitelist = Arrays.asList(context.getResources().getStringArray(R.array.dynamic_clocks)).contains(rom);
        boolean enabledInSettings = prefUtils.getBool(DEV_MAKE_DYNAMIC) && isOtherRoms(rom);
        return inWhitelist || enabledInSettings;
    }

    private boolean hideClock(String rom){
        boolean inWhitelist = Arrays.asList(context.getResources().getStringArray(R.array.hide_clock)).contains(rom);
        boolean enabledInSettings = prefUtils.getBool(DEV_HIDE_CLOCK) && isOtherRoms(rom);
        return inWhitelist || enabledInSettings;
    }

    private boolean isOtherRoms(String rom){
        return rom.equals(context.getString(R.string.otherRomsBeta));
    }

    public void xmlBuilder(){

        ArrayList<String> colorsTitles = prefUtils.loadArray(COLOR_TITLES);
        ArrayList<String> colorsValues = prefUtils.loadArray(COLOR_VALUES);
        ArrayList<String> formatsTitles = prefUtils.loadArray(FORMAT_TITLES);
        ArrayList<String> formatsValues = prefUtils.loadArray(FORMAT_VALUES);

        int colorLimit = colorsTitles.size();
        for (int i = 0; i < colorLimit; i++) {
            String colorTitle = colorsTitles.get(i);
            String colorValue = colorsValues.get(i);
            XmlCreation xmlCreator = new XmlCreation();
            xmlCreator.putContext(context);
            xmlCreator.createTypeA(colorTitle, colorValue);
        }

        int formatLimit = formatsTitles.size();
        for (int i = 0; i < formatLimit; i++) {
            String formatTitle = formatsTitles.get(i);
            String formatValue = formatsValues.get(i);
            XmlCreation xmlCreator = new XmlCreation();
            xmlCreator.putContext(context);
            xmlCreator.createTypeB(formatTitle, formatValue);
        }

        if (prefUtils.getBool(PREF_ICON)){
            ArrayList<String> titles = prefUtils.loadArray(ICON_TITLES);
            ArrayList<String> values = prefUtils.loadArray(ICON_VALUES);
            for (int i = 0; i < titles.size(); i ++){
                String title = titles.get(i);
                String value = values.get(i);
                XmlCreation xmlcreation = new XmlCreation();
                xmlcreation.putContext(context);
                xmlcreation.createIcons(title, value);
            }
        }
    }

    public void insertCustomXmls() throws IOException{

        xmlBuilder();

        boolean themeIcons = prefUtils.getBool(PREF_ICON);

        File dir = fileHelper.newFolder(new File(tempFolder, "xmls.zip"));
        File root = new File(rootFolder);
        String[] xmlArray = root.list(fileHelper.XML);

        fileHelper.newFolder(new File(dir, "assets"));
        fileHelper.newFolder(dir.getAbsolutePath() + slash + "assets" + slash + "overlays");

        File abDest = fileHelper.newFolder(dir.getAbsolutePath() + slash + "assets" + slash + "overlays" + slash + "com.android.systemui");

        for(String s : xmlArray){
            File xml = new File(rootFolder + slash + s);

            if (themeIcons){
                File cDest = fileHelper.newFolder(dir.getAbsolutePath() + slash + "assets" + slash + "overlays" + slash + "com.android.systemui.statusbars");

                if (s.substring(0, 6).equals("type1c")) {
                    FileUtils.copyFileToDirectory(xml, cDest);
                    xml.delete();
                }
            }
            if (xml.exists()) {
                FileUtils.copyFileToDirectory(xml, abDest);
                xml.delete();
            }
        }
        }

    public void dealWivQsBg() throws Exception{
        String rom = prefUtils.getString(PREF_SELECTED_ROM, "");
        new QsBgUtil(context, tempFolder, isOtherRoms(rom) ? "userInput" : "customInput");
    }

    public void appendOptionsZip (){

        File[] zipFiles = tempFolder.listFiles(fileHelper.ZIP);

        ArrayList<String> fileNames = new ArrayList<>();
        ArrayList<File> pathsToFile = new ArrayList<>();

        for (File z: zipFiles) {
            String filePath = z.getAbsolutePath();
            ArrayList<String[]> filePaths = fileHelper.walk(filePath);

            for (String[] aFile: filePaths) {
                String fileName = (aFile[0]); //Filename
                String path = (aFile[1]); //Path from zip ie. assets/overlays/com.android.systemui/
                String absFilePath = aFile[2]; //Absolute path to file

                pathsToFile.add(new File(absFilePath));
                fileNames.add(path + fileName);
            }
        }
        ZipUtil.addOrReplaceEntries(
                new File(mergerFolder, "universalFiles.zip"),
                FileSource.pair(pathsToFile.toArray(new File[pathsToFile.size()]), fileNames.toArray(new String[fileNames.size()])));
    }

    public void createApkFromDir(File universalZip, String apkVersion) throws IOException, ClassNotFoundException, IllegalAccessException,
            InstantiationException, GeneralSecurityException{

        File signedApk = new File(rootFolder + slash + apkVersion);
        ZipSigner zipSigner = new ZipSigner();
        zipSigner.setKeymode("auto-testkey");
        zipSigner.signZip(universalZip.getAbsolutePath(), signedApk.getAbsolutePath());
        Log.d("ZipUtils", "signedZip() : file present -> " + signedApk.exists() + " at " + signedApk.getAbsolutePath());
    }

    public void cleanup () throws IOException{

        List<File> files = Arrays.asList(tempFolder, mergerFolder, new File(rootFolder + "/customInput"));
        for (File f: files){
            if (f.exists()) FileUtils.deleteDirectory(f);
        }

    }
}
