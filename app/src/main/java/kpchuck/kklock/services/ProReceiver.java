package kpchuck.kklock.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import kpchuck.kklock.utils.PrefUtils;

public class ProReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("klock", "Received answer from pro app");
        if (intent.getAction() != null && intent.getAction().equals("kpchuck.k_klock.pro.check")){
            boolean perm = intent.getBooleanExtra("perm", false);
            new PrefUtils(context).putBool("hellothere", perm);
            Intent i = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
            i.putExtra("perm", perm);
            Log.d("klock", "Sending broadcast to main activity: " + perm);
            LocalBroadcastManager.getInstance(context).sendBroadcast(i);

        }
    }
}
