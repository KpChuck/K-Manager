package kpchuck.kklock.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import kpchuck.kklock.utils.PrefUtils;
import static kpchuck.kklock.constants.PrefConstants.PREF_HIDE_ICONS_ON_LOCKSCREEN;

public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("klock", "On Boot receiver started");
        if (action.equals("android.intent.action.BOOT_COMPLETED") || action.equals("android.intent.action.QUICKBOOT_POWERON")) {
            PrefUtils prefUtils = new PrefUtils(context);
            if (prefUtils.getBool(PREF_HIDE_ICONS_ON_LOCKSCREEN)) {
                Intent i = new Intent(context, HideIconsService.class);
                if (Build.VERSION.SDK_INT > 25) context.startForegroundService(i);
                else context.startService(i);
            }
        }
    }
}