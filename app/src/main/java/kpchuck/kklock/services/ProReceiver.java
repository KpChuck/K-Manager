package kpchuck.kklock.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import kpchuck.kklock.utils.PrefUtils;

public class ProReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null && intent.getAction().equals("kpchuck.k_klock.pro.check")){
            boolean perm = intent.getBooleanExtra("perm", false);
            new PrefUtils(context).putBool("hellothere", perm);
            Intent i = new Intent ("splash"); //put the same message as in the filter you used in the activity when registering the receiver
            i.putExtra("perm", perm);
            LocalBroadcastManager.getInstance(context).sendBroadcast(i);

        }
    }
}
