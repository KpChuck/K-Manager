package kpchuck.k_klock.Services;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import kpchuck.k_klock.BuildConfig;
import kpchuck.k_klock.MainActivity;
import kpchuck.k_klock.R;
import kpchuck.k_klock.Utils.PrefUtils;
import kpchuck.k_klock.Utils.FileHelper;
import static kpchuck.k_klock.Constants.PrefConstants.*;

/**
 * Created by karol on 05/12/17.
 */

public class CheckforUpdatesService extends Service {

    private static String url = "https://github.com/KpChuck/K-Manager/releases";
 //   String url = "https://github.com/ungeeked/Ozone/releases";
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

    private void sendBroadcast (){
        Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

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

            request.setTitle("Downloading" + " " + name);
            request.setDescription("Enjoy");

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


        /**
         * Attachment download complete receiver.
         * <p/>
         * 1. Receiver gets called once attachment download completed.
         * 2. Open the downloaded file.
         */
        BroadcastReceiver attachmentDownloadCompleteReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);

                    DownloadManager manager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
                    try {
                        manager.openDownloadedFile(downloadId);
                    }catch (FileNotFoundException e){
                        log(e);
                    }
                    File apk = new File(DOWNLOAD_PATH + name);
                    if (!apk.exists()) Log.e("klock", "Apk not found");
                    else {
                        fileHelper.installApk(apk, ctx);
                    }

                }
            }
        };

    }



    private class CheckGithub extends AsyncTask<Void, Void, Void>{

        Context context;
        String downloadUrl;
        PrefUtils prefUtils;
        String name;

        @Override
        protected Void doInBackground(Void... voids) {

            context = getApplicationContext();
            prefUtils = new PrefUtils(context);

            try {
                Document doc = Jsoup.connect(url).get();
                Element newsetVersion = doc.selectFirst(".release-downloads a strong");
                if (newsetVersion == null){
                    Log.d("klock", "Newset version is null");
                    name = "";
                }
                else {

                    Log.d("klock", newsetVersion.ownText());

                    name = newsetVersion.ownText();

                    downloadUrl = "http://github.com/" + doc.selectFirst(".release-downloads a").attr("href");

                    prefUtils.putString(LATEST_GITHUB_VERSION_NAME, name);
                    prefUtils.putString(LATEST_GITHUB_VERSION_URL, downloadUrl);
                    sendBroadcast();
                }



            }catch (IOException e){
                log(e);
            }


            return null;
        }
    }
}
