package kpchuck.k_klock.services;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

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
        IntentFilter screenStateFilter = new IntentFilter();
        IntentFilter screenUnlockedFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenUnlockedFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenOffReceiver, screenStateFilter);
        registerReceiver(unlockedReceiver, screenUnlockedFilter);

        return Service.START_NOT_STICKY;
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
