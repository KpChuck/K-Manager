package kpchuck.kklock.utils;

import static android.content.Context.CLIPBOARD_SERVICE;
import static kpchuck.kklock.constants.PrefConstants.*;
import static androidx.core.content.FileProvider.getUriForFile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kpchuck.kklock.BuildConfig;
import kpchuck.kklock.R;

/**
 * Created by Karol Przestrzelski on 11/08/2017.
 */

public class FileHelper {

    private Context ctx;

    public FileHelper(Context context){
        this.ctx = context;
    }

    public File newFolder(File file, String file1){
        return newFolder(new File(file, file1));
    }

    public File newFolder(File file){
        return newFolder(file.getAbsolutePath());
    }

    public File newFolder(String path){
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {return  file;}
        else{
            file.mkdirs();
            return file;
        }
    }

    public void installApk(File file){

        PackageManager packageManager = ctx.getPackageManager();
       try {
           if (Build.VERSION.SDK_INT == 26){
               if (packageManager.canRequestPackageInstalls()) {
                   final Intent intent = new Intent(Intent.ACTION_VIEW)
                           .setDataAndType(getUriForFile(ctx,
                                   BuildConfig.APPLICATION_ID + ".fileprovider", file),
                                   "application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                   ctx.startActivity(intent);
               }}
           else {
                final Intent intent = new Intent(Intent.ACTION_VIEW)
                           .setDataAndType(getUriForFile(ctx,
                                   BuildConfig.APPLICATION_ID + ".fileprovider", file),
                                   "application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                   ctx.startActivity(intent);
           }
       }catch (Exception e){
        Log.e("klock", e.getMessage());
    }
    }

    public boolean isPackageInstalled(String... packagename) {
        PackageManager packageManager = ctx.getPackageManager();
        for (String p: packagename){
            try {
                packageManager.getPackageInfo(p, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return false;
    }

    public void openInstalledPackage(String... packages){
        Intent i;
        PackageManager manager = ctx.getPackageManager();
        for (String p: packages) {
            try {
                i = manager.getLaunchIntentForPackage(p);
                if (i == null)
                    throw new PackageManager.NameNotFoundException();
                i.addCategory(Intent.CATEGORY_LAUNCHER);
                ctx.startActivity(i);
                return;
            } catch (PackageManager.NameNotFoundException e) {

            }
        }
    }

    public ArrayList<String> deleteItemFromArray(String item, ArrayList<String> tempList){

        for(int i=0; i<tempList.size(); i++){
            String tempString = tempList.get(i);
            if(tempString.equals(item)) {
                tempList.remove(i);
                break;
            }
        }
        return tempList;

    }

    public FilenameFilter APK = new java.io.FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            if (name.lastIndexOf('.') > 0) {
                int lastIndex = name.lastIndexOf('.');
                String str = name.substring(lastIndex);
                return str.equals(".apk");
            }
            return false;
        }

    };

    public FilenameFilter ZIP = new java.io.FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            if(name.lastIndexOf('.')>0) {

                // get last index for '.' char
                int lastIndex = name.lastIndexOf('.');

                // get extension
                String str = name.substring(lastIndex);

                // match path name extension
                return str.equals(".zip");
            }

            return false;
        }
    };

    public FilenameFilter XML = new java.io.FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            if(name.lastIndexOf('.')>0) {

                // get last index for '.' char
                int lastIndex = name.lastIndexOf('.');
                // get extension
                String str = name.substring(lastIndex);


                //    String typeCheck = name.substring(0, 5);
                // match path name extension
                return str.equals(".xml");
            }

            return false;
        }
    };

    public FilenameFilter DIRECTORY = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return dir.isDirectory();
        }
    };


    public boolean checkQsFile(PrefUtils prefUtils){
        String path = prefUtils.getString("qsBgFilePath", "null");
        if (path == null) return  false;
        File file = new File(path);
        if (!file.exists()) return false;
        String ext = FilenameUtils.getExtension(file.getName());
        return !ext.equals("png");
    }

    public void copyFromAssets(String assetDir, String whichString, File outDir, Context context, boolean unzipExtracted) {
        String slash = "/";
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(assetDir);
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);

        }
        if (files != null) for (String filename : files) {
            if(filename.equals(whichString)){
                InputStream in = null;
                try {
                    in = assetManager.open(assetDir + slash + filename);
                    File outFile = new File(outDir, whichString);
                    if (unzipExtracted) ZipUtil.unpack(in, outFile);
                    else FileUtils.copyInputStreamToFile(in, outFile);

                } catch(IOException e) {
                    Log.e("tag", "Failed to copy asset file: " + filename, e);
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            Log.e("klock", e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public void copyFromAssets(Context context, String assetPath, File outDir) throws IOException{

        AssetManager assetManager = context.getAssets();
        FileUtils.copyInputStreamToFile(assetManager.open(assetPath), outDir);
    }

    public ArrayList<String[]> walk( String path ) {

        ArrayList<String[]> filePathList = new ArrayList<>();

        File root = new File( path );
        File[] list = root.listFiles();

        for ( File f : list ) {

            if ( f.isDirectory() ) {
                ArrayList<String[]> t = walk( f.getAbsolutePath() );
                for (String[] s: t) filePathList.add(s);
            }
            else {
                String fab = f.getAbsolutePath();
                String fpath = fab.substring(fab.indexOf("assets"), fab.indexOf(f.getName()));

                String[] myInfo = new String[]{
                        f.getName(),
                        fpath,
                        fab
                };
                filePathList.add(myInfo);
            }
        }
        return filePathList;
    }


    private boolean fileContains(File file, String filter){
        Pattern pattern = Pattern.compile(filter);
        Matcher matcher = pattern.matcher(file.getName());
        return matcher.find();
    }

    /*
    kpchuck.k_klock.Checks if the version code of the last checked github apk is the same as the version code
     */
    public boolean newVersion(Context context){
        PrefUtils prefUtils = new PrefUtils(context);
        String name = prefUtils.getString(LATEST_GITHUB_VERSION_NAME, "");
        if (name.equals("")) return false;
        String versionName = name.substring(11, name.lastIndexOf(".apk"));
        Log.d("klock", "Current version number is " + Integer.valueOf(versionName));

        return Integer.valueOf(versionName) > Integer.valueOf(BuildConfig.VERSION_NAME);

    }

    public void copyToClipBoard(Context context, String text) {

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(
                "K-Manager_text",
                text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, context.getString(R.string.saved_clipboard), Toast.LENGTH_SHORT).show();
    }

    public void renameFile(File file, String new_name) throws IOException{
        //String path = FilenameUtils.getFullPath(file.getAbsolutePath());
        FileUtils.copyFile(file, new File(file.getParentFile(), new_name));
        file.delete();
        //if (!file.renameTo(new File(path + new_name))){
        //    Log.d("klock", "Could not rename " + file.getAbsolutePath());
        //}
    }
}
