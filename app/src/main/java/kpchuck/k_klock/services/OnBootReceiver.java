package kpchuck.k_klock.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kpchuck.k_klock.utils.PrefUtils;
import static kpchuck.k_klock.constants.PrefConstants.PREF_HIDE_ICONS_ON_LOCKSCREEN;

public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("klock", "On Boot receiver started");
        if (action.equals("android.intent.action.BOOT_COMPLETED") || action.equals("android.intent.action.QUICKBOOT_POWERON")) {
            PrefUtils prefUtils = new PrefUtils(context);
            if (prefUtils.getBool(PREF_HIDE_ICONS_ON_LOCKSCREEN)) {
                context.startService(new Intent(context, HideIconsService.class));
            }
        }
    }
}
