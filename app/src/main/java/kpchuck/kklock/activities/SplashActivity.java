package kpchuck.kklock.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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

    }

    private BroadcastReceiver BReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {

            Intent i = new Intent(context, MainActivity.class);
            context.startActivity(i);
            finish();

        }
    };
}
