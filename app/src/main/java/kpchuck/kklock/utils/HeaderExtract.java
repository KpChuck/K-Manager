package kpchuck.kklock.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jadx.api.JadxDecompiler;
import jadx.core.utils.exceptions.JadxException;
import kpchuck.kklock.BuildConfig;
import kpchuck.kklock.R;
import static kpchuck.kklock.constants.PrefConstants.*;

/**
 * Created by karol on 19/03/18.
 */

public class HeaderExtract extends AsyncTask<Void, String, Void> {

    String slash = "/";
    String rootFolder = Environment.getExternalStorageDirectory() + slash + "K-Klock";

    private Context context;
    private RelativeLayout relativeLayout;
    private TextView tv;
    private RelativeLayout defaultLayout;
    private FileHelper fileHelper;
    PrefUtils prefUtils ;

    public HeaderExtract(Context context, RelativeLayout relativeLayout, TextView textView, RelativeLayout defaultLayout){
        this.fileHelper = new FileHelper();
        this.context = context;
        this.relativeLayout = relativeLayout;
        this.tv = textView;
        this.defaultLayout = defaultLayout;
        this.prefUtils = new PrefUtils(context);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        relativeLayout.setVisibility(View.VISIBLE);
        tv.setText("Extracting, please wait...");
    }

    @Override
    protected Void doInBackground(Void... apkVersion) {

        try {

            publishProgress("Decompiling Apk...");

            File sysui = new File(Environment.getExternalStorageDirectory() + "/K-Klock/userInput/SystemUI.apk");
            SuUtils suUtils = new SuUtils();
            suUtils.runSuCommand("cp /system/priv-app/$(ls /system/priv-app | grep SystemUI)/*.apk /sdcard/K-Klock/userInput/SystemUI.apk");

            decompileSysUI(sysui);

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
        ZipAndSend();

    }

    private void decompileSysUI(File sysui) throws JadxException, IOException{

        JadxDecompiler jadx = new JadxDecompiler();

        File kManager = fileHelper.newFolder(Environment.getExternalStorageDirectory() + "/K-Manager/");
        File dest = fileHelper.newFolder(kManager, "libs");
        fileHelper.copyFromAssets("android", "android.zip", dest, context, true);
        new File(dest, "android.zip").renameTo(new File(dest, "android"));

        if (ZipUtil.containsEntry(sysui, "classes.dex")){
            ZipUtil.removeEntry(sysui, "classes.dex");
        }

        File resOut = new File(kManager, "res_out");
        jadx.setSources(true);
        jadx.loadFile(sysui);
        jadx.setOutputDir(kManager);
        jadx.setOutputDirRes(resOut);
        jadx.save();
        List<File> xmls = new ArrayList<>();
        xmls.add(new File(resOut, "res/layout/quick_status_bar_expanded_header.xml"));

        for (File f : xmls){
            FileUtils.copyFileToDirectory(f, kManager);
        }

    }

    public void ZipAndSend(){
        File root = new File(Environment.getExternalStorageDirectory() + "/K-Manager/");
        File zip = new File(root,  prefUtils.getString(PREF_SELECTED_ROM, "") + ".zip");
        if (zip.exists()) zip.delete();
        // Zip the xmls

        List<ZipEntrySource> zipEntrySources = new ArrayList<>();

        String name = "quick_status_bar_expanded_header.xml";
        zipEntrySources.add(new FileSource("/" + name, new File(root, name)));

        ZipEntrySource[] addedEntries = new ZipEntrySource[zipEntrySources.size()];
        for (int i = 0; i< zipEntrySources.size(); i++){
            addedEntries[i] = zipEntrySources.get(i);
        }
        ZipUtil.pack(addedEntries, zip);
        // Send the zip file
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        String message = String.format("Hi, here is the xml for %s", prefUtils.getString(PREF_SELECTED_ROM, ""));
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"przestrzelski.com@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name) );
        i.putExtra(Intent.EXTRA_TEXT, (message));
        i.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", zip));
        try {
            context.startActivity(Intent.createChooser(i,
                    "Send through..."));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context,
                    "Error sending zip, try again later",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }
}
