package kpchuck.k_klock;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.content.res.AssetManager;
import java.io.IOException;
import java.io.File;
import android.net.Uri;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.stephentuso.welcome.WelcomeHelper;

import org.apache.commons.io.FileUtils;

import java.util.Arrays;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gr.escsoft.michaelprimez.searchablespinner.SearchableSpinner;
import gr.escsoft.michaelprimez.searchablespinner.interfaces.IStatusListener;
import gr.escsoft.michaelprimez.searchablespinner.interfaces.OnItemSelectedListener;
import kpchuck.k_klock.Activities.InformationWebViewActivity;
import kpchuck.k_klock.Activities.MyWelcomeActivity;
import kpchuck.k_klock.Adapters.ColorAdapter;
import kpchuck.k_klock.Adapters.FormatAdapter;
import kpchuck.k_klock.Adapters.SimpleListAdapter;
import kpchuck.k_klock.Fragments.InputAlertDialogFragment;
import kpchuck.k_klock.Fragments.TextAlertDialogFragment;
import kpchuck.k_klock.Interfaces.BtnClickListener;
import kpchuck.k_klock.Interfaces.DialogClickListener;
import kpchuck.k_klock.Services.CheckforUpdatesService;
import kpchuck.k_klock.Utils.ApkBuilder;
import kpchuck.k_klock.Utils.FileHelper;
import kpchuck.k_klock.Utils.PrefUtils;
import static kpchuck.k_klock.Constants.PrefConstants.*;


public class MainActivity extends AppCompatActivity {

    // Bind Switches and things to do with switches
    @BindView (R.id.networkSignalIndicatorSwitch) Switch indicatorSwitch;
    @BindView (R.id.noQsTilesTv) Switch qsSwitch;
    @BindView (R.id.roundedRecents) Switch recentsSwitch;
    @BindView (R.id.moveNetworkLeft) Switch moveLeftSwitch;
    @BindView (R.id.hideStatusbar) Switch hideStatusbar;
    @BindView (R.id.colorIcons) Switch iconColors;
    @BindView (R.id.qsBg) Switch qsBg;
    @BindView (R.id.minitMod) Switch minit;
    @BindView (R.id.qsTitle) Switch qstitle;
    @BindView (R.id.ampm) Switch ampm;
    @BindView (R.id.iconColorCardView) CardView iconView;

    // Bind layouts, spinner
    @BindView (R.id.loadingId) RelativeLayout loadingLayout;
    @BindView (R.id.loadingTextView) TextView loadingTextView;
    @BindView (R.id.romSelectionSpinner) SearchableSpinner searchableSpinner;
    @BindView (R.id.defaultLayout) ScrollView scrollView;



    // Call Strings, Arraylists and Classes for later use
    ArrayList<String> roms = new ArrayList<>();
    ArrayList<String> colorsTitles = new ArrayList<>();
    ArrayList<String> formatsTitles = new ArrayList<>();
    ArrayList<String> colorsValues = new ArrayList<>();
    ArrayList<String> formatsValues = new ArrayList<>();
    String slash = "/";
    PrefUtils prefUtils;
    private Context context;
    FileHelper fileHelper;
    String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock";
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
    WelcomeHelper welcomeScreen;
    Drawer drawer;
    @BindString (R.string.otherRomsBeta) String betaString;

    PrimaryDrawerItem updateNotif;
    DrawerBuilder builder;
    private boolean spinnerOpen = false;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        welcomeScreen.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostResume() {
        if(prefUtils.getBoolTrue("joinTelegram")) promptTelegram();


        super.onPostResume();
    }

    @OnClick(R.id.fab)
    public void startBuilding() {

        if (prefUtils.getBool("gsBgPref") && !fileHelper.checkQsFile(prefUtils)) return;

        String romName = prefUtils.getString("selectedRom", getString(R.string.chooseRom));
        if(romName.equals(getResources().getString(R.string.chooseRom))){
            shortToast(getResources().getString(R.string.selectRomToast));

        }
        else if(romName.equals(betaString)) {
            OtherRomsHandler handler = new OtherRomsHandler(getApplicationContext());

            if (handler.checkForXmls()) {
                handler.execute();
                buildingProcess();
            }
            else if (!handler.checkForXmls()){
                Intent i = new Intent(getApplicationContext(), InformationWebViewActivity.class);
                i.putExtra("value", 4);
                startActivity(i);
            }
        }

        else if(!romName.equals("") || !romName.equals(betaString)) {
            buildingProcess();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.context = getApplicationContext();
        this.fileHelper = new FileHelper();
        this.prefUtils = new PrefUtils(getApplicationContext());
        LocalBroadcastManager.getInstance(this).registerReceiver(BReceiver, new IntentFilter("message"));

        // Ask for Install Unknown Apps Permissions
        if(Build.VERSION.SDK_INT >= 26 && !getPackageManager().canRequestPackageInstalls()){
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
                    shortToast("You will have to install K-Klock manually from the K-Klock folder without this permission granted");

                }
            };
            alertDialogFragment.Instantiate("Install Apps Permissions Required", getString(R.string.request_install_perms),
                    "Grant", "Deny", clickReactor);
            alertDialogFragment.show(getSupportFragmentManager(), "missiles");

        }

        // Check for updates
        // use this to start and trigger a service
        Intent i = new Intent(context, CheckforUpdatesService.class);
        // potentially add data to the intent
        i.putExtra("action", 1);
        context.startService(i);

        fileHelper.newFolder(rootFolder);
        fileHelper.newFolder(rootFolder + "/userInput");

        welcomeScreen = new WelcomeHelper(this, MyWelcomeActivity.class);
        welcomeScreen.show(savedInstanceState);

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        // Create the material drawer
        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.primary_dark)
                .withProfileImagesVisible(false)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(getString(R.string.app_name))
                                .withEmail(R.string.version))
                .withCurrentProfileHiddenInList(true)
                .build();

        builder = new DrawerBuilder();

        builder.withActivity(this);
        builder.withToolbar(toolbar);
        builder.withAccountHeader(header);

        updateNotif = new PrimaryDrawerItem().withIdentifier(99).withName(getString(R.string.updateItem))
                .withIcon(android.R.drawable.stat_sys_download)
                .withDescription(R.string.updateDesc);

        PrimaryDrawerItem telegramItem = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.telegramItem)
                .withIcon(android.R.drawable.sym_action_chat);

        PrimaryDrawerItem settingsItem = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.settings)
                .withIcon(android.R.drawable.ic_menu_preferences);

        ExpandableDrawerItem faqsItem = new ExpandableDrawerItem()
                .withName(R.string.faqsTitle)
                .withIcon(android.R.drawable.ic_menu_help)
                .withSelectable(false).withSubItems(
                        new SecondaryDrawerItem().withName(R.string.faqsItem)
                                .withLevel(2)
                                .withIdentifier(3),
                        new SecondaryDrawerItem().withName(R.string.otherromsItem)
                                .withLevel(2)
                                .withIdentifier(4),
                        new SecondaryDrawerItem().withName(R.string.qsbgItem)
                                .withLevel(2)
                                .withIdentifier(5),
                        new SecondaryDrawerItem().withName(R.string.leftnetworkItem)
                                .withLevel(2)
                                .withIdentifier(6));

        ExpandableDrawerItem linksItem = new ExpandableDrawerItem()
                .withName(R.string.usefullLinksTitle)
                .withIcon(R.drawable.link_image)
                .withSelectable(false).withSubItems(
                        new SecondaryDrawerItem().withName(R.string.rgbItem)
                            .withLevel(2)
                            .withIdentifier(7),
                        new SecondaryDrawerItem().withName(R.string.formatsItem)
                            .withLevel(2)
                            .withIdentifier(8)
                );

        PrimaryDrawerItem changelogItem = new PrimaryDrawerItem().withIdentifier(9).withName(R.string.changelogItem);

        if (fileHelper.newVersion(context)) builder.addDrawerItems(updateNotif);
        builder.addDrawerItems(telegramItem, settingsItem, faqsItem, linksItem, changelogItem);


        builder.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                if (drawerItem != null) {
                    switch ((int) drawerItem.getIdentifier()) {
                        case 1:
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/kklock"));
                            startActivity(browserIntent);
                            break;
                        case 2:
                            Intent i = new Intent(context, SettingsActivity.class);
                            startActivity(i);
                            break;
                        case 3:
                            Intent faq = new Intent(context, InformationWebViewActivity.class);
                            faq.putExtra("value", 1);
                            startActivity(faq);
                            break;
                        case 4:
                            Intent rom = new Intent(context, InformationWebViewActivity.class);
                            rom.putExtra("value", 4);
                            startActivity(rom);
                            break;
                        case 5:
                            Intent qsbg = new Intent(context, InformationWebViewActivity.class);
                            qsbg.putExtra("value", 2);
                            startActivity(qsbg);
                            break;
                        case 6:
                            Intent left = new Intent(context, InformationWebViewActivity.class);
                            left.putExtra("value", 3);
                            startActivity(left);
                            break;
                        case 7:
                            Intent rgb = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.rapidtables.com/web/color/RGB_Color.htm"));
                            startActivity(rgb);
                            break;
                        case 8:
                            Intent formatIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://forum.xda-developers.com/showthread.php?t=2713812"));
                            startActivity(formatIntent);
                            break;
                        case 9:
                            ArrayList<String> changelog = prefUtils.loadArray(CHANGELOG_ARRAY);
                            String changelist = "";
                            for (String c : changelog){
                                changelist += "\n\t" + c;
                            }
                            TextAlertDialogFragment dialogFragment = new TextAlertDialogFragment();
                            DialogClickListener dialogClickListener = new DialogClickListener() {
                                @Override
                                public void onPositiveBtnClick() {

                                }

                                @Override
                                public void onCancelBtnClick() {

                                }
                            };
                            dialogFragment.Instantiate("Changelog", changelist, "Thanks", "Okay", dialogClickListener);
                            dialogFragment.show(getSupportFragmentManager(), "klock");
                            break;
                        case 99:
                            Intent download= new Intent(context, CheckforUpdatesService.class);
                            // potentially add data to the intent
                            download.putExtra("action", 2);
                            context.startService(download);
                            break;
                    }
                }
                return false;
            }});

        drawer = builder.build();

        new CleanupFiles(loadingLayout, loadingTextView).execute();

        getArrayForRoms();



        // Initialize the spinner
        final SimpleListAdapter simpleListAdapter = new SimpleListAdapter(this, roms);
        searchableSpinner.setAdapter(simpleListAdapter);
        searchableSpinner.setSelectedItem(prefUtils.getString("selectedRom", getString(R.string.chooseRom)));
        searchableSpinner.setStatusListener(new IStatusListener() {
            @Override
            public void spinnerIsOpening() {
                spinnerOpen = true;
            }

            @Override
            public void spinnerIsClosing() {
                spinnerOpen = false;
            }
        });
        searchableSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position, long id) {
                String selectedItem = simpleListAdapter.getItem(position).toString();
                prefUtils.putString("selectedRom", selectedItem);
                if (fileHelper.getOos(selectedItem).equals("OxygenOS")){
                    qsBg.setVisibility(View.GONE);
                    indicatorSwitch.setVisibility(View.VISIBLE);
                }else {
                    indicatorSwitch.setVisibility(View.GONE);
                    qsBg.setVisibility(View.VISIBLE);
                }

            } // to close the onItemSelected

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void notifyOnUpdate(){
        Log.d("klock", "Sending notification...");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
        Intent intent = new Intent(context, CheckforUpdatesService.class);
        intent.putExtra("action", 2);

        PendingIntent pIntent = PendingIntent.getService(context, 0, intent, 0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Update notifcation");
            notificationChannel.enableLights(false);
            notificationChannel.setVibrationPattern(new long[]{500});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        String name = prefUtils.getString(LATEST_GITHUB_VERSION_NAME, null);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Update Available")
                //     .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Update " + name + " available")
                .setContentText("Tap here to download")
                .setContentIntent(pIntent)
                .setContentInfo("Update");

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());

    }

    private BroadcastReceiver  BReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean newVersion = fileHelper.newVersion(context);
            Log.d("klock", "Is update available boolean is " + newVersion);
            if (newVersion && drawer.getDrawerItem(99) == null){
                notifyOnUpdate();
                drawer.addItemAtPosition(updateNotif, 1);
            }

            else if (!newVersion){
                drawer.removeItem(99);
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!searchableSpinner.isInsideSearchEditText(ev)) {
            searchableSpinner.hideEdit();
        }
        return super.dispatchTouchEvent(ev);
    }

    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(BReceiver, new IntentFilter("message"));
    }

    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(BReceiver);
    }

    private void buildingProcess() {

        String[] check = new File(rootFolder).list(fileHelper.APK);
        int k = fileHelper.decreaseToLowest(check);
        String apkVersion = "K-Klock_v" + k + ".apk";

        new ApkBuilder(context, loadingLayout, loadingTextView, scrollView).execute(apkVersion, apkVersion, apkVersion);

    }

    public void ShowIncluded(final View view){
        RelativeLayout bgLayout = findViewById(R.id.listViewLayout);
        TextView oneTv = findViewById(R.id.firstTextView);
        TextView twoTv = findViewById(R.id.secondTextView);
        ListView oneLv = findViewById(R.id.firstListView);
        ListView twoLv = findViewById(R.id.secondListView);
        Button button = findViewById(R.id.addButton);


        final ArrayList<String> titles = prefUtils.loadArray( "iconsTitles");
        final ArrayList<String> values = prefUtils.loadArray("iconsValues");

        if(!titles.isEmpty()) {
            final ArrayList<String> finalTitles = new ArrayList<>();
            for (int i = 0; i<titles.size(); i++){
                String starting = titles.get(i);
                String middle = starting.replace('_',' ');
                String end = middle.substring(0, middle.lastIndexOf("."));
                finalTitles.add(end);
            }

            BtnClickListener editListener = new BtnClickListener() {
                @Override
                public void onBtnClick(int position) {
                    String name = finalTitles.get(position);
                    String value = values.get(position);

                    InputAlertDialogFragment dialogFragment = new InputAlertDialogFragment();
                    dialogFragment.Instantiate(getString(R.string.add_color_name_title), name,
                            value, true, "icons",
                            true, view);
                    dialogFragment.show(getSupportFragmentManager(), "klock");                }
            };

            BtnClickListener deleteListener = new BtnClickListener() {
                @Override
                public void onBtnClick(int position) {
                    String name = finalTitles.get(position);
                    String value = values.get(position);
                    deleteItems(titles, values, name, value, "iconsTitles", "iconsValues", "saveIcons");
                    ShowIncluded(view);
                }
            };

            ColorAdapter oneAdapter = new ColorAdapter(getApplicationContext(), finalTitles, values, false, editListener, deleteListener);
            oneLv.setAdapter(oneAdapter);
            oneLv.setVisibility(View.VISIBLE);


        }else{
            oneLv.setVisibility(View.GONE);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputAlertDialogFragment dialogFragment = new InputAlertDialogFragment();
                dialogFragment.Instantiate(getString(R.string.add_color_name_title), getString(R.string.add_color_name_hint),
                        getString(R.string.add_color_value_hint), false, "icons",
                        true, v);
                dialogFragment.show(getSupportFragmentManager(), "klock");

            }
        });

        ArrayList<String> one = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.included_icons_title)));
        ArrayList<String> two = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.included_icons_value)));

        ColorAdapter twoAdapter = new ColorAdapter(getApplicationContext(), one, two, true, null, null);
        twoLv.setAdapter(twoAdapter);
        twoTv.setText(getString(R.string.included_colors));
        oneTv.setText(getString(R.string.added_colors));
        bgLayout.setVisibility(View.VISIBLE);

    }

    public void showLayout (final RelativeLayout relativeLayout){
        relativeLayout.animate()
                .alpha(1.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        relativeLayout.setVisibility(View.VISIBLE);
                    }
                });
    }

    public void hideLayout (final RelativeLayout relativeLayout){
        relativeLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.view_hide));
        relativeLayout.animate()
                .alpha(0.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        relativeLayout.setVisibility(View.GONE);
                    }
                });

    }

    public void addCustomColors(View v){
        InputAlertDialogFragment dialogFragment = new InputAlertDialogFragment();
        dialogFragment.Instantiate(getString(R.string.add_color_name_title), getString(R.string.add_color_name_hint),
                getString(R.string.add_color_value_hint), false, "colors",
                false, v);
        dialogFragment.show(getSupportFragmentManager(), "klock");
    }

    public void addCustomFormats(View v){
        InputAlertDialogFragment fragment = new InputAlertDialogFragment();
        fragment.Instantiate(getString(R.string.add_format_name_title), getString(R.string.add_format_name_hint),
                getString(R.string.add_format_value_hint), false, "formats",
                false, v);
        fragment.show(getSupportFragmentManager(), "klock");
    }

    public void showIncludedColors(final View v){
        RelativeLayout bgLayout = (RelativeLayout) findViewById(R.id.listViewLayout);
        TextView oneTv = (TextView) findViewById(R.id.firstTextView);
        TextView twoTv = (TextView) findViewById(R.id.secondTextView);
        ListView oneLv = (ListView) findViewById(R.id.firstListView);
        ListView twoLv = (ListView) findViewById(R.id.secondListView);
        Button button = (Button) findViewById(R.id.addButton);


        colorsTitles = prefUtils.loadArray( "colorsTitles");
        colorsValues = prefUtils.loadArray("colorsValues");

        if(!colorsTitles.isEmpty()) {
            final ArrayList<String> finalColorsTitles = new ArrayList<>();
            for (int i = 0; i<colorsTitles.size(); i++){
                String starting = colorsTitles.get(i);
                String middle = starting.replace('_',' ');
                String end = middle.substring(0, middle.lastIndexOf("."));
                finalColorsTitles.add(end);
            }

            BtnClickListener editListener = new BtnClickListener() {
                @Override
                public void onBtnClick(int position) {
                    String name = finalColorsTitles.get(position);
                    String value = colorsValues.get(position);

                    InputAlertDialogFragment dialogFragment = new InputAlertDialogFragment();
                    dialogFragment.Instantiate(getString(R.string.add_color_name_title), name,
                            value, true, "colors",
                            true, v);
                    dialogFragment.show(getSupportFragmentManager(), "klock");                }
            };

            BtnClickListener deleteListener = new BtnClickListener() {
                @Override
                public void onBtnClick(int position) {
                    String name = finalColorsTitles.get(position);
                    String value = colorsValues.get(position);
                    deleteItems(colorsTitles, colorsValues, name, value, "colorsTitles", "colorsValues", "saveColors");
                    showIncludedColors(v);
                }
            };

            ColorAdapter oneAdapter = new ColorAdapter(getApplicationContext(), finalColorsTitles, colorsValues, false, editListener, deleteListener);
            oneLv.setAdapter(oneAdapter);
            oneLv.setVisibility(View.VISIBLE);

        }else{
            oneLv.setVisibility(View.GONE);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputAlertDialogFragment dialogFragment = new InputAlertDialogFragment();
                dialogFragment.Instantiate(getString(R.string.add_color_name_title), getString(R.string.add_color_name_hint),
                        getString(R.string.add_color_value_hint), false, "colors",
                        true, v);
                dialogFragment.show(getSupportFragmentManager(), "klock");

            }
        });

        ArrayList<String> one = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.included_colors_title)));
        ArrayList<String> two = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.included_colors_value)));

        ColorAdapter twoAdapter = new ColorAdapter(getApplicationContext(), one, two, true, null, null);
        twoLv.setAdapter(twoAdapter);
        twoTv.setText(getString(R.string.included_colors));
        oneTv.setText(getString(R.string.added_colors));
        showLayout(bgLayout);

    }

    public void hideList(View v){
        RelativeLayout rv = findViewById(R.id.listViewLayout);
        if(rv.getVisibility() == View.VISIBLE){
            hideLayout(rv);

        }
    }

    public void showIncludedFormats(final View v){
        RelativeLayout bgLayout = (RelativeLayout) findViewById(R.id.listViewLayout);
        TextView oneTv = (TextView) findViewById(R.id.firstTextView);
        TextView twoTv = (TextView) findViewById(R.id.secondTextView);
        ListView oneLv = (ListView) findViewById(R.id.firstListView);
        ListView twoLv = (ListView) findViewById(R.id.secondListView);
        Button button = (Button) findViewById(R.id.addButton);


        formatsTitles = prefUtils.loadArray("formatsTitles");
        formatsValues = prefUtils.loadArray("formatsValues");

        if(!formatsTitles.isEmpty()) {
            final ArrayList<String> finalFormatsTitles = new ArrayList<>();
            for (int i = 0; i<formatsTitles.size(); i++){
                String starting = formatsTitles.get(i);
                String middle = starting.replace('_',' ');
                String end = middle.substring(0, middle.lastIndexOf("."));
                finalFormatsTitles.add(end);

            }


            BtnClickListener editListener = new BtnClickListener() {
                @Override
                public void onBtnClick(int position) {
                    String name = finalFormatsTitles.get(position);
                    String value = formatsValues.get(position);

                    InputAlertDialogFragment fragment = new InputAlertDialogFragment();
                    fragment.Instantiate(getString(R.string.add_format_name_title), name,
                            value, true, "formats",
                            true, v);
                    fragment.show(getSupportFragmentManager(), "klock");
                }
            };

            BtnClickListener deleteListener = new BtnClickListener() {
                @Override
                public void onBtnClick(int position) {
                    String name = finalFormatsTitles.get(position);
                    String value = formatsValues.get(position);
                    deleteItems(formatsTitles, formatsValues, name, value, "formatsTitles", "formatsValues", "saveFormats");
                    showIncludedFormats(v);
                }
            };

            FormatAdapter oneAdapter = new FormatAdapter(getApplicationContext(), finalFormatsTitles,  false, editListener, deleteListener);
            oneLv.setAdapter(oneAdapter);
            oneLv.setVisibility(View.VISIBLE);
        }else{
            oneLv.setVisibility(View.GONE);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputAlertDialogFragment fragment = new InputAlertDialogFragment();
                fragment.Instantiate(getString(R.string.add_format_name_title), getString(R.string.add_format_name_hint),
                        getString(R.string.add_format_value_hint), false, "formats",
                        true, v);
                fragment.show(getSupportFragmentManager(), "klock");            }
        });

        ArrayList<String> one = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.included_formats_title)));

        FormatAdapter twoAdapter = new FormatAdapter(getApplicationContext(), one, true, null, null);
        twoLv.setAdapter(twoAdapter);
        twoTv.setText(getString(R.string.included_formats));
        oneTv.setText(getString(R.string.added_formats));
        showLayout(bgLayout);
    }

    public void deleteItems(ArrayList<String> titles, ArrayList<String> values, String title, String value, String titleArrayKey, String valueArrayKey, String toSaveBoolKey){
        title = title.replace(" ", "_")+".xml";
        FileHelper fileHelper = new FileHelper();

        titles = prefUtils.loadArray(titleArrayKey);
        values = prefUtils.loadArray(valueArrayKey);

        titles = fileHelper.deleteItemFromArray(title, titles);
        values = fileHelper.deleteItemFromArray(value, values);

        prefUtils.saveArray(titles, titleArrayKey);
        prefUtils.saveArray(values, valueArrayKey);


    }

    public void getArrayForRoms(){
        try {
            String[] temp = getAssets().list("romSpecific");
            Arrays.sort(temp);
            for(String s:temp){
                s = s.substring(0, s.lastIndexOf('.'));
                roms.add(s);
            };
            roms.add(betaString);
        }catch(Exception e){
            shortToast(e.getMessage());
        }

    }

    public void shortToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        RelativeLayout rv = findViewById(R.id.listViewLayout);
        if (rv.getVisibility() == View.VISIBLE){
            hideLayout(rv);
        }
        else if (spinnerOpen){
            searchableSpinner.hideEdit();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/KarolPrzes"));
            startActivity(browserIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void promptTelegram(){
        if (isPackageInstalled("org.telegram.messenger", getApplicationContext().getPackageManager()) ){

            TextAlertDialogFragment fragment = new TextAlertDialogFragment();
            fragment.Instantiate("K-Klock Telegram", getString(R.string.joinTelegram), "OK", "Not Today", new DialogClickListener() {
                @Override
                public void onPositiveBtnClick() {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/kklock"));
                    getApplicationContext().startActivity(browserIntent);
                }
                @Override
                public void onCancelBtnClick() {
                }
            });
            fragment.show(getSupportFragmentManager(), "missiles");

        }
        prefUtils.putBool("joinTelegram", false);
    }

    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean hasPermissions(android.content.Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (android.support.v4.app.ActivityCompat.checkSelfPermission(context, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }}

class CleanupFiles extends AsyncTask<Void, Void, Void>{

    private RelativeLayout relativeLayout;
    private TextView tv;

    public CleanupFiles(RelativeLayout relativeLayout, TextView tv){
        this.relativeLayout = relativeLayout;
        this.tv = tv;
    }

    private void cleanDir (File file){
        if (file.exists()){
            try{
                FileUtils.deleteDirectory(file);
            }catch (IOException e){
                Log.e("klock", e.getMessage());
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        relativeLayout.setVisibility(View.VISIBLE);
        tv.setText("Cleaning residual files...");
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        relativeLayout.setVisibility(View.GONE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String rootDir = Environment.getExternalStorageDirectory() + "/K-Klock/";
        String[] temps = new String[]{"temp", "temp2", "temp3", "tempF", "merger"};
        for (String temp : temps) cleanDir(new File(rootDir + temp));
        File testKey = new File(rootDir + "test");
        if (testKey.exists()) testKey.delete();
        return null;
    }
}