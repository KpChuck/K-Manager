package kpchuck.k_klock.utils;

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
import android.widget.ScrollView;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import kellinwood.security.zipsigner.ZipSigner;
import kpchuck.k_klock.XmlModding;
import kpchuck.k_klock.R;
import kpchuck.k_klock.xml.XmlCreation;
import kpchuck.k_klock.xml.XmlWork;

import static kpchuck.k_klock.constants.PrefConstants.*;

/**
 * Created by karol on 04/12/17.
 */

public class ApkBuilder extends AsyncTask<String, String, String>{

    String slash = "/";
    String rootFolder = Environment.getExternalStorageDirectory() + slash + "K-Klock";

    private Context context;
    private RelativeLayout relativeLayout;
    private TextView tv;
    private ScrollView scrollView;
    private FileHelper fileHelper;
    private PrefUtils prefUtils;
    private File mergerFolder;
    private File tempFolder;
    private String univ = "universal";

    public ApkBuilder(Context context, RelativeLayout relativeLayout, TextView textView, ScrollView scrollView){
        this.fileHelper = new FileHelper();
        this.context = context;
        this.prefUtils = new PrefUtils(context);
        this.relativeLayout = relativeLayout;
        this.tv = textView;
        this.scrollView = scrollView;
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

        makeDirs();
        ExtractAssets();
        modTheRomZip();
        insertCustomXmls();
        dealWivQsBg();
        appendOptionsZip();// Takes long about 10s
        // Takes long about 5s
        try {
            createApkFromDir(new File(mergerFolder, "universalFiles.zip"), apkVersion[0]);
        }catch (Exception e){
            Log.e("klock", e.getMessage());
        }
        cleanup();

        return apkVersion[0];
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        String apkVersion = result;
        File apk = new File(rootFolder + slash + apkVersion);
        SuUtils su = new SuUtils();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("installSilently", true) || !su.hasRoot()) {
            FileHelper fh = new FileHelper();
            showSnackbar();
            fh.installApk(apk, context);
        }else {
            String install = "pm install -r /sdcard/K-Klock/" + apkVersion;
            String output = su.runCommand(install).toString();
            if (output.contains("Success")) showSnackbar();
        }
        relativeLayout.setVisibility(View.GONE);
        tv.setText("");

    }

    private void showSnackbar() {
        try {
            Snackbar snackbar = Snackbar.make(scrollView, "Open K-Klock in Substratum", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Open", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final String SUBSTRATUM_PACKAGE_NAME = "projekt.substratum";
                            final String THEME_PACKAGE_NAME = "com.kpchuck.kklock";

                            Intent intentActivity = new Intent();
                            intentActivity = intentActivity.setClassName(SUBSTRATUM_PACKAGE_NAME, "projekt.substratum.activities.launch.ThemeLaunchActivity");
                          /*  intent.putExtra("package_name", THEME_PACKAGE_NAME);
                            intent.setAction("projekt.substratum.THEME");
                            intent.setPackage(THEME_PACKAGE_NAME);
                            intent.putExtra("calling_package_name", THEME_PACKAGE_NAME);
                            intent.putExtra("oms_check", false);
                            intent.putExtra("theme_mode", (String) null);
                            intent.putExtra("notification", false);
                            intent.putExtra("hash_passthrough", true);
                            intent.putExtra("certified", false);*/


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
        }catch (Exception e){
            Log.e("klock", e.getMessage());
        }
    }

    public void makeDirs (){
        this.mergerFolder = new File(rootFolder + "/merger");
        this.tempFolder = new File(rootFolder + "/tempF");

        fileHelper.newFolder(mergerFolder.getAbsolutePath());
        fileHelper.newFolder(tempFolder.getAbsolutePath());

    }

    public void ExtractAssets (){
        File customInput = new File(rootFolder + "/customInput");
        String romName = prefUtils.getString(PREF_SELECTED_ROM, context.getString(R.string.chooseRom));
        // Copy files used for every apk first
        fileHelper.copyFromAssets(univ, "universalFiles.zip", mergerFolder, context, false);
        // Copy optional files into the tempfolder
        if (prefUtils.getBool(PREF_RECENTS)) fileHelper.copyFromAssets(univ, "recents.zip", tempFolder, context, true);
        if(prefUtils.getBool(PREF_QS)) fileHelper.copyFromAssets(univ, "qsTiles.zip".trim(), tempFolder, context, true);
        if(prefUtils.getBool(PREF_ICON)) fileHelper.copyFromAssets(univ, "colorIcons.zip".trim(), tempFolder, context, true);
        if (prefUtils.getBool(PREF_LOCKSCREEN_STATUSBAR_SIZE)) fileHelper.copyFromAssets(univ, "hideStatusbar.zip".trim(), tempFolder, context, true);
        if (prefUtils.getBool(PREF_QS_BG) && !fileHelper.getOos(romName).equals("OxygenOS"))
            fileHelper.copyFromAssets(univ, "qsBgs.zip", tempFolder, context, true);
        if (prefUtils.getBool(PREF_QS_LABEL)) fileHelper.copyFromAssets(univ, "qsTitle.zip", tempFolder, context, true);
        if (prefUtils.getBool(PREF_AM)) fileHelper.copyFromAssets(univ, "ampm.zip", tempFolder, context, true);
        if (prefUtils.getBool(PREF_STATUSBAR_CLOCK_SIZE)) fileHelper.copyFromAssets(univ, "clockSize.zip", tempFolder, context, true);
        if(prefUtils.getBool(PREF_INDICATORS) && romName.equals("OxygenOS Nougat"))
            fileHelper.copyFromAssets(univ, "indicatorsN.zip".trim(), tempFolder, context, true);
        if(prefUtils.getBool(PREF_INDICATORS) && romName.equals("OxygenOS Oreo"))
            fileHelper.copyFromAssets(univ, "indicatorsO.zip".trim(), tempFolder, context, true);
        // Copy the rom specific file if a rom was selected
        if (!romName.equals(context.getString(R.string.otherRomsBeta))) {
            fileHelper.copyFromAssets("romSpecific", romName + ".zip".trim(), customInput, context, true);
            File zip = new File(customInput, romName + ".zip");
            for (File x : zip.listFiles()){
                try{
                    FileUtils.copyFileToDirectory(x, customInput);
                }catch (IOException e){
                    Log.e("klock", e.getMessage());
                }
            }
            try {
                FileUtils.deleteDirectory(zip);
            }catch (IOException e){Log.e("klock", e.getMessage());}
        }
    }

    private void modTheRomZip(){
        String rom = prefUtils.getString(PREF_SELECTED_ROM, "");

        new XmlWork(context,
                isOtherRoms(rom) ? "userInput" : "customInput",
                hideClock(rom),
                shouldBeDynamic(rom));
    }

    private boolean shouldBeDynamic(String rom){
        SharedPreferences defPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean inWhitelist = Arrays.asList(context.getResources().getStringArray(R.array.dynamic_clocks)).contains(rom);
        boolean enabledInSettings = defPref.getBoolean(DEV_MAKE_DYNAMIC, false) && isOtherRoms(rom);
        return inWhitelist || enabledInSettings;
    }

    private boolean hideClock(String rom){
        SharedPreferences defPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean inWhitelist = Arrays.asList(context.getResources().getStringArray(R.array.hide_clock)).contains(rom);
        boolean enabledInSettings = defPref.getBoolean(DEV_HIDE_CLOCK, false) && isOtherRoms(rom);
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

    public void insertCustomXmls(){

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
            try {
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
            }catch(IOException e){
                Log.e("klock", e.getMessage());
            }
        }
    }

    public void dealWivQsBg(){
        new QsBgUtil(context, tempFolder);
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


    public void createApkFromDir(File universalZip, String apkVersion) throws Exception {

        File signedApk = new File(rootFolder + slash + apkVersion);
        ZipSigner zipSigner = new ZipSigner();
        zipSigner.setKeymode("auto-testkey");
        zipSigner.signZip(universalZip.getAbsolutePath(), signedApk.getAbsolutePath());
        Log.d("ZipUtils", "signedZip() : file present -> " + signedApk.exists() + " at " + signedApk.getAbsolutePath());
    }

    public void cleanup (){
        try{
            FileUtils.deleteDirectory(tempFolder);
            FileUtils.deleteDirectory(mergerFolder);
            FileUtils.deleteDirectory(new File(rootFolder + "/customInput"));
        }catch (IOException e){
            Log.e("klock", e.getMessage());
        }
    }



}
