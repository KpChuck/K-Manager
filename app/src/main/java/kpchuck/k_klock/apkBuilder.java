package kpchuck.k_klock; /**
 * Created by Karol Przestrzelski on 08/08/2017.
 */
import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import kellinwood.security.zipsigner.ZipSigner;
import kpchuck.k_klock.Utils.FileHelper;
import kpchuck.k_klock.Utils.PrefUtils;
import kpchuck.k_klock.Utils.QsBgUtil;

public class apkBuilder  extends android.os.AsyncTask<String, String, String> {

    String slash = "/";
    String rootFolder = Environment.getExternalStorageDirectory() + slash + "K-Klock";



    java.io.FilenameFilter fileNameFilter = new java.io.FilenameFilter() {

@Override
public boolean accept(File dir, String name) {
        if(name.lastIndexOf('.')>0) {

            // get last index for '.' char
            int lastIndex = name.lastIndexOf('.');

            // get extension
            String str = name.substring(lastIndex);

            // match path name extension
                if(str.equals(".zip")) {
                     return true;
                }
            }

            return false;
        }
        };
    private Context context;
    private RelativeLayout relativeLayout;
    private TextView tv;
    private ScrollView frameLayout;

    public apkBuilder(Context context, RelativeLayout relativeLayout, TextView tv, ScrollView frameLayout){
        this.context=context;
        this.relativeLayout=relativeLayout;
        this.tv=tv;
        this.frameLayout=frameLayout;

    }

    public void shortToast(String message){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void longToast(String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        relativeLayout.setVisibility(View.VISIBLE);
        tv.setText(R.string.apkBuilderLoading);

            // Do something like display a progress bar
    }

        public String doInBackground(String... apkVersion) {

                File temp2 = new File(rootFolder + slash + "temp2");
                if (!temp2.exists()) temp2.mkdirs();
                File mergerFolder = new File(rootFolder + slash + "temp2" + slash + "merge");
                mergerFolder.mkdirs();

                File ha = new File(rootFolder + slash + "temp" + slash);
                String[] tempFolder = ha.list(fileNameFilter);
                for (String s : tempFolder) {
                    File explosion = new File(rootFolder + slash + "temp" + slash + s);
                    String cool = new String(rootFolder + slash + "temp" + slash + s);


                    ZipUtil.explode(explosion);

                    String[] exploosionArray = explosion.list();


                    for (String f : exploosionArray) {

                        String path = cool + slash + f;
                        try {
                            File pathFile = new File(path);
                            if (pathFile.isDirectory()) {
                                FileUtils.copyDirectoryToDirectory(pathFile, mergerFolder);
                            } else {
                                FileUtils.copyFileToDirectory(pathFile, mergerFolder);
                            }
                        } catch (Exception e) {
                        }
                    }
                }


                findAndCopyXml();
                new QsBgUtil(context);
                File xmlFolder = new File(rootFolder + slash + "temp3" + slash + "assets");
                try {
                    FileUtils.copyDirectoryToDirectory(xmlFolder, mergerFolder);

                } catch (IOException e) {
                }

                new MoveNetworkIconsLeft(context);


                try {
                    createApkFromDir(mergerFolder, new File(rootFolder + slash + "test"), apkVersion[0]);
                    File tempFile = new File(rootFolder + slash + "temp");
                    FileUtils.deleteDirectory(tempFile);
                    File temp2File = new File(rootFolder + slash + "temp2");
                    FileUtils.deleteDirectory(temp2File);
                    File testKey = new File(rootFolder + slash + "test");
                    testKey.delete();
                    File temp3 = new File(rootFolder + slash + "temp3");
                    if (temp3.exists()) FileUtils.deleteDirectory(temp3);


                } catch (Exception e) {




            }return apkVersion[0];
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);

                // Do things like update the progress bar
                }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
                super.onPostExecute(result);

                String apkVersion = result;
           File apk = new File(rootFolder + slash + apkVersion);
            FileHelper fh = new FileHelper();
            fh.installApk(apk, context);



            relativeLayout.setVisibility(View.GONE);
            tv.setText("");

            try {
                Snackbar snackbar = Snackbar.make(frameLayout, "Open K-Klock in Substratum", Snackbar.LENGTH_INDEFINITE)
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
                shortToast(e.getMessage());
            }



            // Do things like hide the progress bar or change a TextView
                }


        FilenameFilter fileNameFilterXml = new java.io.FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
            if(name.lastIndexOf('.')>0) {

                    // get last index for '.' char
                    int lastIndex = name.lastIndexOf('.');
                    // get extension
                    String str = name.substring(lastIndex);


                //    String typeCheck = name.substring(0, 5);
                    // match path name extension
                    if(str.equals(".xml")) {
                            return true;
                    }
            }

            return false;
                }
        };

        public void findAndCopyXml(){
            File dir = new File(rootFolder);
            String[] xmlArray = dir.list(fileNameFilterXml);
            makeFolder(rootFolder + slash + "temp3");
            makeFolder(rootFolder + slash + "temp3" + slash + "assets");
            makeFolder(rootFolder + slash + "temp3" + slash + "assets" + slash + "overlays");
            makeFolder(rootFolder + slash + "temp3" + slash + "assets" + slash + "overlays" + slash + "com.android.systemui");
            File dest_folder = new File(rootFolder + slash + "temp3" + slash + "assets" + slash + "overlays" + slash + "com.android.systemui");

            //Make icon theming folder
            PrefUtils prefUtils = new PrefUtils(context);
            boolean themeIcons = prefUtils.getBool("iconPref");
            if (themeIcons){
                makeFolder(rootFolder + "/temp3/assets/overlays/com.android.systemui.statusbars");
            }



            for(String s : xmlArray){
                File xml = new File(rootFolder + slash + s);
                try {
                    if (themeIcons){
                        File icon_dest = new File(rootFolder + "/temp3/assets/overlays/com.android.systemui.statusbars/");
                        if (s.substring(0, 6).equals("type1c")) {
                            FileUtils.copyFileToDirectory(xml, icon_dest);
                            xml.delete();
                        }
                    }
                    if (xml.exists()) {
                        FileUtils.copyFileToDirectory(xml, dest_folder);
                        xml.delete();
                    }
                }catch(IOException e){}

                }

        }

        public void makeFolder(String file){
            File stuff = new File(file);
            if(!stuff.exists()){
                stuff.mkdirs();
            }


        }


        public static void createApkFromDir(File dir, File destFile, String apkVersion) throws Exception {

                ZipUtil.pack(dir, destFile);
                File signedApk = new File(destFile.getParent(), apkVersion);
                ZipSigner zipSigner = new ZipSigner();
                zipSigner.setKeymode("auto-testkey");
                zipSigner.signZip(destFile.getAbsolutePath(), signedApk.getAbsolutePath());
                Log.d("ZipUtils", "signedZip() : file present -> " + signedApk.exists() + " at " + signedApk.getAbsolutePath());


                }


        }