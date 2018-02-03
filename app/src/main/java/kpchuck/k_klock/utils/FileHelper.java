package kpchuck.k_klock.utils;

import static android.content.Context.CLIPBOARD_SERVICE;
import static kpchuck.k_klock.constants.PrefConstants.*;
import static android.support.v4.content.FileProvider.getUriForFile;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kpchuck.k_klock.BuildConfig;

/**
 * Created by Karol Przestrzelski on 11/08/2017.
 */

public class FileHelper {

    private Context ctx;

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

    public void installApk(File file, Context context){
        this.ctx=context;

        PackageManager packageManager = context.getPackageManager();
       try {
           if (Build.VERSION.SDK_INT == 26){
               if (packageManager.canRequestPackageInstalls()) {
                   final Intent intent = new Intent(Intent.ACTION_VIEW)
                           .setDataAndType(getUriForFile(ctx,
                                   ctx.getPackageName() + ".fileprovider", file),
                                   "application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                   ctx.startActivity(intent);
               }}
           else {
                final Intent intent = new Intent(Intent.ACTION_VIEW)
                           .setDataAndType(getUriForFile(ctx,
                                   ctx.getPackageName() + ".fileprovider", file),
                                   "application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                   ctx.startActivity(intent);
           }
       }catch (Exception e){
        Log.e("klock", e.getMessage());
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
    public void deleteItems(String title, String value, String titleArrayKey, String valueArrayKey, Context context){
        PrefUtils prefUtils = new PrefUtils(context);
        title = title.replace(" ", "_")+".xml";

        ArrayList<String> titles = prefUtils.loadArray(titleArrayKey);
        ArrayList<String> values = prefUtils.loadArray(valueArrayKey);

        titles = deleteItemFromArray(title, titles);
        values = deleteItemFromArray(value, values);

        prefUtils.saveArray(titles, titleArrayKey);
        prefUtils.saveArray(values, valueArrayKey);


    }

    public FilenameFilter APK = new java.io.FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            if (name.lastIndexOf('.') > 0) {
                int lastIndex = name.lastIndexOf('.');
                String str = name.substring(lastIndex);
                if (str.equals(".apk")) {
                    return true;
                }
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
                if(str.equals(".zip")) {
                    return true;
                }
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
                if(str.equals(".xml")) {
                    return true;
                }
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

    public String getOos(String oos){
        if (oos.length() < 8) return "thisIsNotOxygenOS";
        oos = oos.substring(0, 8);
        return oos;
    }

    public int decreaseToLowest(String[] testStringArray){
        int kk;

        if (testStringArray == null || testStringArray.length == 0) return 1;
        Arrays.sort(testStringArray);
        List<String> list = Arrays.asList(testStringArray);
        Collections.reverse(list);

        ArrayList<String> klockArray = new ArrayList<>();
        for (String s: list) if (s.substring(0, 7).equals("K-Klock")) klockArray.add(s);

        if(klockArray.size() != 0) {
            ArrayList<Integer> listOfVersions = new ArrayList<>();

            for(String s : klockArray){
                    String toInt = s.substring(s.indexOf("v") + 1, s.lastIndexOf("."));
                    int bleh = Integer.parseInt(toInt);
                    listOfVersions.add(bleh);

            }
            Integer[] intArray = listOfVersions.toArray(new Integer[listOfVersions.size()]);
            Arrays.sort(intArray);
            List<Integer> li = Arrays.asList(intArray);
            Collections.reverse(li);
            intArray = (Integer[]) li.toArray();
            kk = intArray[0]+1;
        }else{ kk = 1; }
        return kk;
    }

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

    /*
    Checks if the version code of the last checked github apk is the same as the version code
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
                "K-Manager_text", // What should I set for this "label"?
                text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Saved to clip board", Toast.LENGTH_SHORT).show();
    }
}
