package kpchuck.kklock;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stephentuso.welcome.WelcomeHelper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cat.ereza.customactivityoncrash.config.CaocConfig;
import kotlin.io.FilesKt;
import kpchuck.kklock.activities.CustomCrashActivity;
import kpchuck.kklock.activities.MyWelcomeActivity;
import kpchuck.kklock.adapters.SwipeTabAdapter;
import kpchuck.kklock.dialogs.TextAlertDialogFragment;
import kpchuck.kklock.interfaces.DialogClickListener;
import kpchuck.kklock.utils.ApkBuilder;
import kpchuck.kklock.utils.FileHelper;
import kpchuck.kklock.utils.PrefUtils;

import static kpchuck.kklock.constants.PrefConstants.PREF_BLACK_THEME;


public class MainActivity extends AppCompatActivity{


    // Bind layouts, spinner
    @BindView (R.id.loadingId) RelativeLayout loadingLayout;
    @BindView (R.id.loadingTextView) TextView loadingTextView;
    @BindView (R.id.defaultLayout) RelativeLayout defaultLayout;
    SwipeTabAdapter tabAdapter;

    String slash = "/";
    PrefUtils prefUtils;
    private Context context;
    FileHelper fileHelper;
    String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock";
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
    WelcomeHelper welcomeScreen;

    private boolean hasAll = false;
    private boolean isPro = false;
    private boolean installed_from_playstore = true;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        welcomeScreen.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostResume() {
        if(prefUtils.getBoolTrue("joinTelegram")) promptTelegram();

        if (!fileHelper.isPackageInstalled("projekt.substratum", "projekt.substratum.lite")) {

            TextAlertDialogFragment textAlertDialogFragment = new TextAlertDialogFragment();
            DialogClickListener clickListener = new DialogClickListener() {
                @Override
                public void onPositiveBtnClick() {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=projekt.substratum&hl=en"));
                    startActivity(browserIntent);
                }

                @Override
                public void onCancelBtnClick() {

                }
            };
            textAlertDialogFragment.Instantiate(getString(R.string.subs_not_installed),
                    getString(R.string.subs_required),
                    getString(R.string.okay), getString(R.string.cancel), clickListener);
            textAlertDialogFragment.show(getSupportFragmentManager(), "");

        }



        super.onPostResume();
    }

    @OnClick(R.id.fab)
    public void startBuilding() {
        new ApkBuilder(context, loadingLayout, loadingTextView, defaultLayout).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.context = this;
        this.fileHelper = new FileHelper(context);
        this.prefUtils = new PrefUtils(context);

        setTheme(prefUtils.getBoolTrue(PREF_BLACK_THEME) ? R.style.AppTheme_Dark : R.style.AppTheme);
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup tabs
        List<Pair<String, Integer>> fragments = new ArrayList<>();
        fragments.add(new Pair<>("Clock", R.xml.clock));
        fragments.add(new Pair<>("StatusBar", R.xml.statusbar));
        fragments.add(new Pair<>("Misc", R.xml.qs));
        fragments.add(new Pair<>("Settings", R.xml.app_settings));

        tabAdapter = new SwipeTabAdapter(getFragmentManager(), fragments);
        // Set the adapter onto the view pager
        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(tabAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager, true);

        // Setup Custom Crash Activity
        CaocConfig.Builder.create()
                .trackActivities(true) //default: false
                .restartActivity(MainActivity.class) //default: null (your app's launch activity)
                .errorActivity(CustomCrashActivity.class)
                .apply();

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        // Ask for Permissions
        if(Build.VERSION.SDK_INT >= 26 && !getPackageManager().canRequestPackageInstalls()) {
            TextAlertDialogFragment alertDialogFragment = new TextAlertDialogFragment();
            DialogClickListener clickReactor = new DialogClickListener() {
                @Override
                public void onPositiveBtnClick() {
                    Intent k = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    k.setData(uri);
                    startActivity(k);
                }

                @Override
                public void onCancelBtnClick() {
                    shortToast(getString(R.string.install_permission_not_granted));

                }
            };
            alertDialogFragment.Instantiate(getString(R.string.install_permission_title), getString(R.string.request_install_perms),
                    getString(R.string.okay), getString(R.string.cancel), clickReactor);
            alertDialogFragment.show(getSupportFragmentManager(), "missiles");
        }

        fileHelper.newFolder(rootFolder);
        fileHelper.newFolder(rootFolder + "/inputFiles");

        welcomeScreen = new WelcomeHelper(this, MyWelcomeActivity.class);
        welcomeScreen.show(savedInstanceState);

        new CleanupFiles().execute();

        showOosWarning();
    }

    protected void onResume(){
        super.onResume();
        setTheme(prefUtils.getBoolTrue(PREF_BLACK_THEME) ? R.style.AppTheme_Dark : R.style.AppTheme);

    }

    public void shortToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void promptTelegram(){
        if (fileHelper.isPackageInstalled("org.telegram.messenger", "org.telegram.plus", "org.thunderdog.challegram")) {

            TextAlertDialogFragment fragment = new TextAlertDialogFragment();
            fragment.Instantiate("K-Klock Telegram", getString(R.string.joinTelegram), getString(R.string.okay),
                    getString(R.string.not_today), new DialogClickListener() {
                @Override
                public void onPositiveBtnClick() {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/kklock"));
                    context.startActivity(browserIntent);
                }
                @Override
                public void onCancelBtnClick() {
                }
            });
            fragment.show(getSupportFragmentManager(), "missiles");

        }
        prefUtils.putBool("joinTelegram", false);
    }

    public static boolean hasPermissions(android.content.Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (androidx.core.app.ActivityCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showOosWarning(){
        if (!prefUtils.getBool(R.string.key_do_not_show_oos_warning)) {
            DialogClickListener clickListener = new DialogClickListener() {
                @Override
                public void onPositiveBtnClick() {

                }

                @Override
                public void onCancelBtnClick() {
                    prefUtils.putBool(R.string.key_do_not_show_oos_warning, true);
                }
            };
            TextAlertDialogFragment dialogFragment = new TextAlertDialogFragment();
            dialogFragment.Instantiate(getString(R.string.oos_warning_title), getString(R.string.oos_warning_message),
                    getString(R.string.okay), "Do not show again", clickListener);
            dialogFragment.show(getSupportFragmentManager(), "ooswarningtag");
        }
    }
}



class CleanupFiles extends AsyncTask<Void, Void, Void>{


    private void cleanDir (File file){
        if (file.exists()){
            FilesKt.deleteRecursively(file);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String rootDir = Environment.getExternalStorageDirectory() + "/K-Klock/";
        String[] temps = new String[]{"temp", "temp2", "temp3", "tempF", "merger", "userInput"};
        for (String temp : temps) cleanDir(new File(rootDir + temp));
        File testKey = new File(rootDir + "test");
        if (testKey.exists()) testKey.delete();
        return null;
    }
}