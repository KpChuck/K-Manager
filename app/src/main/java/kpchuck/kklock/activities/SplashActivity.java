package kpchuck.kklock.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import kpchuck.kklock.Checks;
import kpchuck.kklock.MainActivity;
import kpchuck.kklock.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme_Dark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        LocalBroadcastManager.getInstance(this).registerReceiver(BReceiver, new IntentFilter("splash"));
        new Checks().checkPro(this);
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (Exception e){}
            Intent i = new Intent("splash");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
        }).start();

    }

    private BroadcastReceiver BReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(i);
            finish();

        }
    };
}
