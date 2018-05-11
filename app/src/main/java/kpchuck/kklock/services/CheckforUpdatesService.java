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

import kpchuck.kklock.R;
import kpchuck.kklock.utils.PrefUtils;
import kpchuck.kklock.utils.FileHelper;
import static kpchuck.kklock.constants.PrefConstants.*;

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
                Document doc = Jsoup.connect(url).get();
                Element newsetVersion = doc.selectFirst(".my-4 a strong");
                Element changelog = doc.selectFirst(".markdown-body p");
                String c = changelog.wholeText();
                prefUtils.remove(CHANGELOG_ARRAY);
                prefUtils.putString(CHANGELOG_ARRAY, c);

                if (newsetVersion == null){
                    Log.d("klock", "Newset version is null");
                    name = "";
                }
                else {
                    Log.d("klock", newsetVersion.ownText());

                    name = newsetVersion.ownText();
                    downloadUrl = "http://github.com/" + doc.selectFirst(".my-4 a").attr("href");

                    prefUtils.putString(LATEST_GITHUB_VERSION_NAME, name);
                    prefUtils.putString(LATEST_GITHUB_VERSION_URL, downloadUrl);
                    sendBroadcast(1);

                    if (prefUtils.getBoolTrue("sync_rom_files")) {
                        syncRomFiles();
                    }
                }



            }catch (IOException e){
                log(e);
            }


            return null;
        }

        private void syncRomFiles() throws IOException{
            Document document = Jsoup.connect("http://github.com/KpChuck/K-Manager/tree/master/app/src/main/assets/romSpecific").get();
            Elements rom_files = document.select("div.repository-content div.file-wrap table tbody tr.js-navigation-item");
            rom_files.remove(0); // Remove the up navigation element
            SharedPreferences sharedPreferences = context.getSharedPreferences("rom_files", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            List<String> download_urls = new ArrayList<>();
            for (Element element: rom_files){
                Element keyElement = element.selectFirst("td.content a");
                String key = keyElement.attr("title");
                String lastEdited = element.selectFirst("td.age time-ago").attr("datetime");
                if (!lastEdited.equals(sharedPreferences.getString(key, ""))){
                    editor.putString(key, lastEdited);
                    String download_url = "http://github.com" + element.selectFirst("td.content a").attr("href");
                    String romName = download_url.substring(download_url.lastIndexOf('/')+1, download_url.lastIndexOf("."))
                            .replace("%20", " ");
                    download_urls.add(romName);
                    download_urls.add(download_url);
                }
            }
            editor.apply();
            if (download_urls.size() > 1){
                shownotification();
                for (int i=0; i<download_urls.size(); i+=2){
                    float prog = i * 100f / download_urls.size();
                    updateNotification((int)prog, download_urls.get(i));
                    downloadFile(getDownloadUrl(download_urls.get(i+1)));
                }
                finishNotification();
                sendBroadcast(2);
            }

        }

        NotificationManager notificationManager;
        NotificationCompat.Builder notificationBuilder;
        Notification notification;
        Integer notificationID = 100;
        String NOTIFICATION_CHANNEL_ID = "Sync Rom Files";
        String NOTIFICATION_CHANNEL_NAME = "Sync Rom Files";
        String NOTIFICATION_CHANNEL_DESC = "Syncs rom files";

        private void shownotification(){

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            //Set notification information:
            notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);
            notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.notification_icon_24dp)
                    .setContentTitle("Syncing Rom Files")
                    .setContentText("")
                    .setProgress(100, 0, false);

            //Send the notification:
            notification = notificationBuilder.build();

            if(Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(NOTIFICATION_CHANNEL_DESC);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(notificationID, notification);
        }

        private void updateNotification(int progress, String desc){
            //Update notification information:
            notificationBuilder.setProgress(100, progress, false);
            notificationBuilder.setContentText(desc);

            //Send the notification:
            notification = notificationBuilder.build();
            if(Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(NOTIFICATION_CHANNEL_DESC);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(notificationID, notification);
        }

        private void finishNotification(){
            notificationBuilder.setOngoing(false)
                    .setContentText("Finished syncing")
                    .setProgress(0, 0, false);

            //Send the notification:
            notification = notificationBuilder.build();
            if(Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(NOTIFICATION_CHANNEL_DESC);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(notificationID, notification);
        }

        private void downloadFile(String url) {
            try {
                String filename = url.substring(url.lastIndexOf('/')+1).replace("%20", " ");
                File file = new File(Environment.getExternalStorageDirectory() + "/K-Manager/romSpecific/" );
                file.mkdirs();
                File download_file = new File(file, filename);
                if (download_file.exists()) download_file.delete();

                URL website = new URL(url);
                FileUtils.copyURLToFile(website, download_file);
            }catch (Exception e){
                Log.e("klock", e.getMessage());
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        private String getDownloadUrl(String url) throws IOException{
            Log.d("klock", url);
            Document doc = Jsoup.connect(url).get();
            Element view_raw = doc.selectFirst("#raw-url");
            return "https://github.com" + view_raw.attr("href");
        }

    }

}
