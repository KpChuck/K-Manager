package kpchuck.k_klock.Utils;

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
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import eu.chainfire.libsuperuser.Shell;
import kellinwood.security.zipsigner.ZipSigner;
import kpchuck.k_klock.MoveNetworkIconsLeft;
import kpchuck.k_klock.R;

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



    @Override
    protected String doInBackground(String... apkVersion) {

        makeDirs();
        ExtractAssets();
        insertCustomXmls();
        dealWivQsBg();
        appendOptionsZip();
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("installSilently", true) || !Shell.SU.available()) {
            FileHelper fh = new FileHelper();
            showSnackbar();
            fh.installApk(apk, context);
        }else {
            String install = "pm install -r /sdcard/K-Klock/" + apkVersion;
            String output = Shell.SU.run(install).toString();
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

                            Intent intent = new Intent();
                            intent = intent.setClassName(SUBSTRATUM_PACKAGE_NAME, "projekt.substratum.activities.launch.ThemeLaunchActivity");
                            intent.putExtra("package_name", THEME_PACKAGE_NAME);
                            intent.setAction("projekt.substratum.THEME");
                            intent.setPackage(THEME_PACKAGE_NAME);
                            intent.putExtra("calling_package_name", THEME_PACKAGE_NAME);
                            intent.putExtra("oms_check", false);
                            intent.putExtra("theme_mode", (String) null);
                            intent.putExtra("notification", false);
                            intent.putExtra("hash_passthrough", true);
                            intent.putExtra("certified", false);
                            context.startActivity(intent);
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
        String romName = prefUtils.getString("selectedRom", context.getString(R.string.chooseRom));
        // Copy files used for every apk first
        fileHelper.copyFromAssets(univ, "universalFiles.zip", mergerFolder, context);
        // Copy optional files into the tempfolder
        if(prefUtils.getBool("qsPref")) fileHelper.copyFromAssets(univ, "qsTiles.zip".trim(), tempFolder, context);
        if(prefUtils.getBool("iconPref")) fileHelper.copyFromAssets(univ, "colorIcons.zip".trim(), tempFolder, context);
        if(prefUtils.getBool("recentsPref")) fileHelper.copyFromAssets(univ, "recents.zip".trim(), tempFolder, context);
        if (prefUtils.getBool("hideStatusbarPref")) fileHelper.copyFromAssets(univ, "hideStatusbar.zip".trim(), tempFolder, context);
        if (prefUtils.getBool("qsBgPref") && !fileHelper.getOos(romName).equals("OxygenOS")) fileHelper.copyFromAssets(univ, "qsBgs.zip", tempFolder, context);
        if (prefUtils.getBool("qsTitlePref")) fileHelper.copyFromAssets(univ, "qsTitle.zip", tempFolder, context);
        if (prefUtils.getBool("amPref")) fileHelper.copyFromAssets(univ, "ampm.zip", tempFolder, context);
        if(prefUtils.getBool("indicatorPref") && romName.equals("OxygenOS Nougat")) fileHelper.copyFromAssets(univ, "indicatorsN.zip".trim(), tempFolder, context);
        if(prefUtils.getBool("indicatorPref") && romName.equals("OxygenOS Oreo")) fileHelper.copyFromAssets(univ, "indicatorsO.zip".trim(), tempFolder, context);
        // Copy the rom specific file if a rom was selected
        if (!romName.equals(context.getString(R.string.otherRomsBeta)) || !romName.equals(""))
            fileHelper.copyFromAssets("romSpecific", romName+".zip".trim(), tempFolder, context);
    }

    public void insertCustomXmls(){

        boolean themeIcons = prefUtils.getBool("iconPref");

        File dir = fileHelper.newFolder(new File(tempFolder, "xmls.zip"));
        String[] xmlArray = dir.list(fileHelper.XML);

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
        ZipUtil.unexplode(dir);
    }

    public void dealWivQsBg(){
        new QsBgUtil(context, tempFolder);
    }

    public void appendOptionsZip (){

        File[] zipFiles = tempFolder.listFiles(fileHelper.ZIP);
        for (File z: zipFiles) {
            ZipUtil.explode(z);
            if (z.toString().contains(prefUtils.getString("selectedRom", context.getString(R.string.chooseRom))))
                new MoveNetworkIconsLeft(context, z);
            String filePath = z.getAbsolutePath();
            appendToUniversal(filePath);
        }
    }



    public void appendToUniversal(String filePath){
        ArrayList<String[]> filePaths = fileHelper.walk(filePath);
        ArrayList<File> paths = new ArrayList<>();
        ArrayList<String> fileNames = new ArrayList<>();

        for (String[] aFile: filePaths) {
            String fileName = (aFile[0]);
            String path = (aFile[1]);

            paths.add(new File(filePath + slash + path + fileName));
            fileNames.add(path + fileName);
        }
        ZipUtil.addOrReplaceEntries(
                new File(mergerFolder, "universalFiles.zip"),
                FileSource.pair(paths.toArray(new File[paths.size()]), fileNames.toArray(new String[fileNames.size()])));
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
        }catch (IOException e){
            Log.e("klock", e.getMessage());
        }
    }



}
