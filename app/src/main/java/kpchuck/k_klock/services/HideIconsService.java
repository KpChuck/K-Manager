package kpchuck.k_klock.services;

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
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import kpchuck.k_klock.MainActivity;
import kpchuck.k_klock.R;
import kpchuck.k_klock.utils.FileHelper;
import kpchuck.k_klock.utils.PrefUtils;
import kpchuck.k_klock.utils.StatusBarIconsUtils;

import static kpchuck.k_klock.constants.PrefConstants.LATEST_GITHUB_VERSION_NAME;

public class HideIconsService extends Service {
    public HideIconsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("klock", "starting service");
        if (Build.VERSION.SDK_INT > 25) startInForeground();
        IntentFilter screenStateFilter = new IntentFilter();
        IntentFilter screenUnlockedFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenUnlockedFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenOffReceiver, screenStateFilter);
        registerReceiver(unlockedReceiver, screenUnlockedFilter);

        return super.onStartCommand(intent, flags, startId);
    }

    private void startInForeground() {
        String NOTIFICATION_CHANNEL_ID = "K-Manager Hide Icon Service";
        String NOTIFICATION_CHANNEL_NAME = "Hide Statusbar Icons";
        String NOTIFICATION_CHANNEL_DESC = "K-Manager's notification channel to keep hide icons service running in the background";
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon_24dp)
                .setContentTitle("K-Manager")
                .setContentText("Hide icons service is running in the background")
                .setContentIntent(pendingIntent);

        Notification notification=builder.build();

        if(Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(101, notification);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(screenOffReceiver);
        unregisterReceiver(unlockedReceiver);
        super.onDestroy();
    }

    BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            StatusBarIconsUtils utils = new StatusBarIconsUtils();
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                utils.hideIcons(context);
                Log.i("Check","Screen went ON");
            }
        }
    };

    BroadcastReceiver unlockedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            StatusBarIconsUtils utils = new StatusBarIconsUtils();
            if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                utils.restoreIcons(context);
                Log.i("Check","Screen unlocked");
            }
        }
    };
}
