package kpchuck.kklock.services;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import kpchuck.kklock.BuildConfig;
import kpchuck.kklock.R;
import kpchuck.kklock.utils.PrefUtils;
import kpchuck.kklock.utils.FileHelper;
import static kpchuck.kklock.constants.PrefConstants.*;

/**
 * Created by karol on 05/12/17.
 */

public class CheckforUpdatesService extends Service {

    String DOWNLOAD_PATH = Environment.getExternalStorageDirectory() + "/K-Manager/";
   // String name;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void log(Exception e){
        Log.e("klock", e.getMessage());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent.getIntExtra("action",1);
        if (action == 1) {
            new CheckGithub().execute();
        }
        else if (action == 2){
            new DownloadApk().execute();
        }

        return Service.START_NOT_STICKY;
    }

    private void sendBroadcast (int key){
        Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("key", key);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    BroadcastReceiver attachmentDownloadCompleteReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {

            PrefUtils prefUtils = new PrefUtils(ctx);
            String action = intent.getAction();
            String name = prefUtils.getString(LATEST_GITHUB_VERSION_NAME, "");
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                File apk = new File(DOWNLOAD_PATH + name);
                if (!apk.exists()) Log.e("klock", "Apk not found");
                else {
                    FileHelper fileHelper = new FileHelper();
                    fileHelper.installApk(apk, ctx);
                }

            }
        }
    };

    private class DownloadApk extends AsyncTask<Void, Void, Void>{

        FileHelper fileHelper;
        String downloadUrl;
        Context context;
        PrefUtils prefUtils;
        String name;

        public DownloadApk(){
            this.context = getApplicationContext();
            prefUtils = new PrefUtils(context);

        }

        @Override
        protected Void doInBackground(Void... voids) {

            fileHelper = new FileHelper();
            fileHelper.newFolder(DOWNLOAD_PATH);

            name = prefUtils.getString(LATEST_GITHUB_VERSION_NAME, null);
            downloadUrl = prefUtils.getString(LATEST_GITHUB_VERSION_URL, null);

            File apk = new File(DOWNLOAD_PATH + name);
            if (apk.exists()) apk.delete();

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            context.registerReceiver(attachmentDownloadCompleteReceive, new IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE));


            request.setTitle(getString(R.string.downloading) + " " + name);
            request.setDescription(getString(R.string.enjoy));

            request.setNotificationVisibility(DownloadManager.Request
                    .VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir("K-Manager",
                    name);

            DownloadManager manager = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }



    private class CheckGithub extends AsyncTask<Void, Void, Void>{

        Context context;
        String downloadUrl;
        PrefUtils prefUtils;
        String name;

        @Override
        protected void onPostExecute(Void aVoid) {
            stopSelf();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            context = getApplicationContext();
            prefUtils = new PrefUtils(context);

            try {
                Document doc = Jsoup.connect(getString(R.string.k_manager_git_link)).get();
                Element newsetVersion = doc.selectFirst(".my-4 a strong");
                Element changelog = doc.selectFirst(".markdown-body p");
                String c = changelog.wholeText();
                prefUtils.remove(CHANGELOG_ARRAY);
                prefUtils.putString(CHANGELOG_ARRAY, c);

                if (newsetVersion == null){
                    Log.d("klock", "Newest version is null");
                    name = "";
                }
                else {
                    Log.d("klock", newsetVersion.ownText());

                    name = newsetVersion.ownText();
                    downloadUrl = "http://github.com/" + doc.selectFirst(".my-4 a").attr("href");

                    prefUtils.putString(LATEST_GITHUB_VERSION_NAME, name);
                    prefUtils.putString(LATEST_GITHUB_VERSION_URL, downloadUrl);
                    sendBroadcast(1);

                }



            }catch (IOException e){
                log(e);
            }


            return null;
        }

    }

}
