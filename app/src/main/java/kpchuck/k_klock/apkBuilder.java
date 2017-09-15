package kpchuck.k_klock; /**
 * Created by Karol Przestrzelski on 08/08/2017.
 */
import org.apache.commons.io.FileUtils;


import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.github.ybq.android.spinkit.SpinKitView;
import java.io.File;
import android.content.Intent;
import android.net.Uri;

public class apkBuilder  extends android.os.AsyncTask<String, String, String> {

                String prefFile = "prefFileName";
                String slash = "/";
                String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock";



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
        private android.content.Context context;
    private RelativeLayout relativeLayout;
    private TextView tv;
    ErrorHandle err;

    public apkBuilder(android.content.Context context, RelativeLayout relativeLayout, TextView tv){
        this.context=context;
        this.relativeLayout=relativeLayout;
        this.tv=tv;
        ErrorHandle errorHandle = new ErrorHandle();
        this.err=errorHandle;

    }

        public void shortToast(String message){
                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show();
                }

    public void longToast(String message){
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show();
    }


        @Override
        protected void onPreExecute() {
                super.onPreExecute();
            relativeLayout.setVisibility(View.VISIBLE);
            tv.setText(R.string.apkBuilderLoading);

                // Do something like display a progress bar
                }

        public String doInBackground(String... apkVersion) {

            if (err.getAsyncError(context)) {
                File temp2 = new File(rootFolder + slash + "temp2");
                if (!temp2.exists()) temp2.mkdirs();
                File mergerFolder = new File(rootFolder + slash + "temp2" + slash + "merge");
                mergerFolder.mkdirs();

                File ha = new File(rootFolder + slash + "temp" + slash);
                String[] tempFolder = ha.list(fileNameFilter);
                for (String s : tempFolder) {
                    File explosion = new File(rootFolder + slash + "temp" + slash + s);
                    String cool = new String(rootFolder + slash + "temp" + slash + s);

                    org.zeroturnaround.zip.ZipUtil.explode(explosion);

                    String[] exploosionArray = explosion.list();


                    for (String f : exploosionArray) {

                        String path = cool + slash + f;
                        try {
                            File pathFile = new File(path);
                            if (pathFile.isDirectory()) {
                                org.apache.commons.io.FileUtils.copyDirectoryToDirectory(pathFile, mergerFolder);
                            } else {
                                org.apache.commons.io.FileUtils.copyFileToDirectory(pathFile, mergerFolder);
                            }
                        } catch (Exception e) {
                        }
                    }
                }


                findAndCopyXml();
                File xmlFolder = new File(rootFolder + slash + "temp3" + slash + "assets");
                try {
                    org.apache.commons.io.FileUtils.copyDirectoryToDirectory(xmlFolder, mergerFolder);
                } catch (java.io.IOException e) {
                }

                SharedPreferences myPref = context.getSharedPreferences(prefFile, Context.MODE_PRIVATE);
                if (myPref.getBoolean("moveLeftPref", false)) new MoveNetworkIconsLeft(context);


                try {
                    createApkFromDir(mergerFolder, new File(rootFolder + slash + "test"), apkVersion[0]);
                    File tempFile = new File(rootFolder + slash + "temp");
                    org.apache.commons.io.FileUtils.deleteDirectory(tempFile);
                    File temp2File = new File(rootFolder + slash + "temp2");
                    org.apache.commons.io.FileUtils.deleteDirectory(temp2File);
                    File testKey = new File(rootFolder + slash + "test");
                    testKey.delete();
                    File temp3 = new File(rootFolder + slash + "temp3");
                    if (temp3.exists()) FileUtils.deleteDirectory(temp3);


                } catch (Exception e) {

                }


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
            fh.FileHelper(context, apk);



            relativeLayout.setVisibility(View.GONE);
            tv.setText("");


            // Do things like hide the progress bar or change a TextView
                }


        java.io.FilenameFilter fileNameFilterXml = new java.io.FilenameFilter() {

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



            for(String s : xmlArray){
                File xml = new File(rootFolder + slash + s);
                try {
                    org.apache.commons.io.FileUtils.copyFileToDirectory(xml, dest_folder);
                    xml.delete();
                }catch(java.io.IOException e){}

                }

        }

        public void makeFolder(String file){
            File stuff = new File(file);
            if(!stuff.exists()){
                stuff.mkdirs();
            }


        }


        public static void createApkFromDir(File dir, File destFile, String apkVersion) throws Exception {

                org.zeroturnaround.zip.ZipUtil.pack(dir, destFile);
                File signedApk = new File(destFile.getParent(), apkVersion);
                kellinwood.security.zipsigner.ZipSigner zipSigner = new kellinwood.security.zipsigner.ZipSigner();
                zipSigner.setKeymode("auto-testkey");
                zipSigner.signZip(destFile.getAbsolutePath(), signedApk.getAbsolutePath());
                android.util.Log.d("ZipUtils", "signedZip() : file present -> " + signedApk.exists() + " at " + signedApk.getAbsolutePath());


                }


        }