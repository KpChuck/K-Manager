package kpchuck.kklock;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.os.Bundle;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;

import java.io.IOException;
import java.io.File;
import android.net.Uri;
import android.content.Intent;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
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
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.sax2.Driver;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cat.ereza.customactivityoncrash.config.CaocConfig;
import gr.escsoft.michaelprimez.searchablespinner.SearchableSpinner;
import gr.escsoft.michaelprimez.searchablespinner.interfaces.IStatusListener;
import gr.escsoft.michaelprimez.searchablespinner.interfaces.OnItemSelectedListener;

import kpchuck.kklock.activities.InformationWebViewActivity;
import kpchuck.kklock.activities.MyWelcomeActivity;
import kpchuck.kklock.activities.SettingsActivity;
import kpchuck.kklock.adapters.ColorAdapter;
import kpchuck.kklock.adapters.FormatAdapter;
import kpchuck.kklock.adapters.SimpleListAdapter;
import kpchuck.kklock.adapters.SwipeTabAdapter;
import kpchuck.kklock.fragments.ClockFragment;
import kpchuck.kklock.fragments.IconsFragment;
import kpchuck.kklock.dialogs.InputAlertDialogFragment;
import kpchuck.kklock.dialogs.ListDialogFragment;
import kpchuck.kklock.fragments.MiscFragment;
import kpchuck.kklock.fragments.StatusBarFragment;
import kpchuck.kklock.dialogs.TextAlertDialogFragment;
import kpchuck.kklock.interfaces.BtnClickListener;
import kpchuck.kklock.interfaces.DialogClickListener;
import kpchuck.kklock.services.CheckforUpdatesService;
import kpchuck.kklock.utils.ApkBuilder;
import kpchuck.kklock.utils.FileHelper;
import kpchuck.kklock.utils.MessageEvent;
import kpchuck.kklock.utils.PrefUtils;
import kpchuck.kklock.xml.XmlUtils;

import static kpchuck.kklock.constants.PrefConstants.*;


public class MainActivity extends AppCompatActivity implements ColorPickerDialogListener{


    // Bind layouts, spinner
    @BindView (R.id.loadingId) RelativeLayout loadingLayout;
    @BindView (R.id.loadingTextView) TextView loadingTextView;
    @BindView (R.id.romSelectionSpinner) SearchableSpinner searchableSpinner;
    @BindView (R.id.defaultLayout) RelativeLayout defaultLayout;
    @BindView (R.id.spinnerLinearLayout) LinearLayout SpinnerLayout;
    @BindView(R.id.otherRomsQm) ImageButton questionMark;
    SwipeTabAdapter tabAdapter;
    @BindView(R.id.orSettings) Button orSettingsButton;
    // Fragment Stuff
    private ClockFragment clockFragment;
    private IconsFragment iconsFragment;
    private StatusBarFragment statusBarFragment;
    private MiscFragment miscFragment;

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

        if (!isPackageInstalled("projekt.substratum", context.getPackageManager())){

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

        if (prefUtils.getBool("gsBgPref") && !fileHelper.checkQsFile(prefUtils)) {
            prefUtils.putBool("qsBgPref", false);
        }
        String romName = prefUtils.getString("selectedRom", getString(R.string.chooseRom));
        if(romName.equals(getString(R.string.chooseRom))){
            shortToast(getString(R.string.selectRomToast));
            return;
        }
        if(romName.equals(betaString)) {
            // Check for new version
            boolean osVersion = !(System.getProperty("os.version")).equals(prefUtils.getString("osversion", ""));
            boolean buildUser = !(Build.USER).equals(prefUtils.getString("builduser", ""));
            boolean releaseVersion = !(Build.VERSION.RELEASE).equals(prefUtils.getString("buildversionrelease", ""));
            boolean newVersion = osVersion || buildUser || releaseVersion;
            if (newVersion){
                prefUtils.putString("osversion", System.getProperty("os.version"));
                prefUtils.putString("builduser", Build.USER);
                prefUtils.putString("buildversionrelease", Build.VERSION.RELEASE);
            }
            final String[] x = new File(rootFolder + "/userInput").list();
            List<String> xmls = new ArrayList<>();
            if (x != null){
                xmls = Arrays.asList(x);
            }
            String[] xmlNames = {"status_bar.xml", "keyguard_status_bar.xml", "system_icons.xml", "quick_status_bar_expanded_header.xml"};
            hasAll = xmls.containsAll(Arrays.asList(xmlNames));
            boolean hasSysUI = xmls.contains("SystemUI.apk");

            if (newVersion && (hasAll || hasSysUI)) {

                TextAlertDialogFragment fragment = new TextAlertDialogFragment();
                DialogClickListener clickListener = new DialogClickListener() {
                    @Override
                    public void onPositiveBtnClick() {
                        for (String f : x){
                            new File(rootFolder + "/userInput/" + f).delete();
                        }
                        hasAll = false;
                        buildingProcess();
                    }
                    @Override
                    public void onCancelBtnClick() {
                        buildingProcess();
                    }
                };
                fragment.Instantiate(getString(R.string.warning), getString(R.string.rom_files_updated),
                        getString(R.string.okay), getString(R.string.use_ones_have), clickListener);
                fragment.show(getSupportFragmentManager(), "");

            }
            else buildingProcess();

        }
        else if(!romName.equals("")) {
            buildingProcess();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.context = this;
        this.fileHelper = new FileHelper();
        this.prefUtils = new PrefUtils(context);

        super.onCreate(savedInstanceState);

        setTheme(prefUtils.getBool(PREF_BLACK_THEME) ? R.style.AppTheme_Dark : R.style.AppTheme);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup tabs
        // Create an adapter that knows which fragment should be shown on each page
        this.clockFragment = new ClockFragment();
        this.iconsFragment = new IconsFragment();
        this.statusBarFragment = new StatusBarFragment();
        this.miscFragment = new MiscFragment();


        tabAdapter = new SwipeTabAdapter(getSupportFragmentManager(), clockFragment, iconsFragment, statusBarFragment, miscFragment);

        // Set the adapter onto the view pager
        final ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(tabAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Setup Custom Crash Activity
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .trackActivities(true) //default: false
                .restartActivity(MainActivity.class) //default: null (your app's launch activity)
                .apply();



        LocalBroadcastManager.getInstance(this).registerReceiver(BReceiver, new IntentFilter("message"));

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


        // Check for updates
        Intent i = new Intent(context, CheckforUpdatesService.class);
        i.putExtra("action", 1);
        context.startService(i);

        fileHelper.newFolder(rootFolder);
        fileHelper.newFolder(rootFolder + "/userInput");

        welcomeScreen = new WelcomeHelper(this, MyWelcomeActivity.class);
        welcomeScreen.show(savedInstanceState);


        // Create the material drawer
        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.primary_dark)
                .withProfileImagesVisible(false)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(getString(R.string.app_name))
                                .withEmail(getString(R.string.version) + " " + BuildConfig.VERSION_NAME))
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
                                .withIdentifier(5));

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

        PrimaryDrawerItem aboutItem = new PrimaryDrawerItem().withIdentifier(10).withName(R.string.aboutItem).withIcon(R.drawable.about_24dp);

        if (fileHelper.newVersion(context)) builder.addDrawerItems(updateNotif);
        builder.addDrawerItems(telegramItem, settingsItem, faqsItem, linksItem, aboutItem, changelogItem);


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
                        case 7:
                            Intent rgb = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.rapidtables.com/web/color/RGB_Color.htm"));
                            startActivity(rgb);
                            break;
                        case 8:
                            Intent formatIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://forum.xda-developers.com/showthread.php?t=2713812"));
                            startActivity(formatIntent);
                            break;
                        case 9:
                            String changelist = prefUtils.getString(CHANGELOG_ARRAY, getString(R.string.no_changelog_available) + " ¯\\_(ツ)_/¯");
                            TextAlertDialogFragment dialogFragment = new TextAlertDialogFragment();
                            DialogClickListener dialogClickListener = new DialogClickListener() {
                                @Override
                                public void onPositiveBtnClick() {

                                }

                                @Override
                                public void onCancelBtnClick() {

                                }
                            };
                            dialogFragment.Instantiate(getString(R.string.changelog), changelist, getString(R.string.thanks),
                                    getString(R.string.okay), dialogClickListener);
                            dialogFragment.show(getSupportFragmentManager(), "klock");
                            break;
                        case 10:
                            StringBuilder tr = new StringBuilder(getString(R.string.translations_thanks) + "<br /> ");
                            String [] ta = getResources().getStringArray(R.array.translators);
                            if (ta.length != 0) {
                                tr.append(getString(R.string.translations_thanks));
                                tr.append("<br>");
                                for (String s: ta) {
                                    tr.append(s);
                                    tr.append("<br>");
                                }
                            }
                            new LibsBuilder()
                                    //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                                    .withActivityStyle(prefUtils.getBool(PREF_BLACK_THEME) ? Libs.ActivityStyle.DARK : Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                    .withAboutIconShown(true)
                                    .withAboutVersionShown(true)
                                    .withAutoDetect(true)
                                    .withFields(R.string.class.getFields())
                                    .withAboutDescription(tr.toString())
                                    //start the activity
                                    .withActivityTitle(getString(R.string.app_name))
                                    .start(getApplicationContext());
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
        builder.withSelectedItem(99);
        drawer = builder.build();


        if (isOtherRoms()) questionMark.setVisibility(View.VISIBLE);
        statusBarFragment.otherRomHide(isOtherRoms());
        orSettingsButton.setVisibility(isOtherRoms() ? View.VISIBLE : View.GONE);


        new CleanupFiles().execute();
        loadSpinner();

    }

    private boolean isOtherRoms(){
        String rom = prefUtils.getString(PREF_SELECTED_ROM, betaString);
        return rom.equals(context.getString(R.string.otherRomsBeta));
    }

    @OnClick (R.id.orSettings)
    public void orSettingsClick(){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.input_menu_dialog, null);
        builder.setView(view);

        TextView textView = view.findViewById(R.id.title);
        final EditText nameEdit = view.findViewById(R.id.name);
        final EditText valueEdit = view.findViewById(R.id.value);
        textView.setText(getString(R.string.rom_info));
        nameEdit.setHint(getString(R.string.rom_name_hint));
        valueEdit.setHint(getString(R.string.rom_version_hint));

        builder
                .setPositiveButton(getString(R.string.send), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        File zip = new File(rootFolder, nameEdit.getText() + " " + valueEdit.getText() + ".zip");
                        if (zip.exists()) zip.delete();
                        // Zip the xmls
                        String[] x = new File(rootFolder + "/userInput").list(fileHelper.XML);
                        List<String> xmls = Arrays.asList(x);
                        String[] xmlNames = {"status_bar.xml", "keyguard_status_bar.xml", "system_icons.xml", "quick_status_bar_expanded_header.xml"};
                        List<String> xmlNam = Arrays.asList(xmlNames);

                        if (!xmls.containsAll(xmlNam)){
                            shortToast(getString(R.string.files_not_found));
                            return;
                        }
                        List<ZipEntrySource> zipEntrySources = new ArrayList<>();
                        for (String f : xmlNam){
                            zipEntrySources.add(new FileSource("/"+f, new File(rootFolder + "/userInput/"+f)));

                        }
                        ZipEntrySource[] addedEntries = new ZipEntrySource[zipEntrySources.size()];
                        for (int i = 0; i< zipEntrySources.size(); i++){
                            addedEntries[i] = zipEntrySources.get(i);
                        }
                        ZipUtil.pack(addedEntries, zip);
                        // Send the zip file
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("message/rfc822");
                        String message = String.format("Hi, here are the rom files for %s %s.\nI'm %s making the clock dynamic and %s the stock clock.\nI would love it if you added them to K-Manager :)",
                                nameEdit.getText(), valueEdit.getText(),
                                prefUtils.getBool(DEV_MAKE_DYNAMIC) ? "" : "not",
                                prefUtils.getBool(DEV_HIDE_CLOCK) ? "I\'m hiding" : "not hiding");
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"przestrzelski.com@gmail.com"});
                        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Other Roms");
                        i.putExtra(Intent.EXTRA_TEXT, (message));
                        i.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", zip));
                        try {
                            context.startActivity(Intent.createChooser(i,
                                    "Send through..."));
                        } catch (ActivityNotFoundException ex) {
                            Toast.makeText(context, getString(R.string.error_sending_zip), Toast.LENGTH_LONG).show();
                        }

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }}

                );

        // Create the AlertDialog object and return it
        builder.create().show();


    }

    @OnClick (R.id.otherRomsQm)
    public void onClick(){
        Intent rom = new Intent(context, InformationWebViewActivity.class);
        rom.putExtra("value", 4);
        startActivity(rom);
    }
    private boolean alreadyRan = false;

    private void notifyOnUpdate(){

        alreadyRan = true;
        String changelist = prefUtils.getString(CHANGELOG_ARRAY, getString(R.string.no_changelog_available) + " ¯\\_(ツ)_/¯");
        TextAlertDialogFragment dialogFragment = new TextAlertDialogFragment();
        if (dialogFragment.isVisible()) return;
        DialogClickListener dialogClickListener = new DialogClickListener() {
            @Override
            public void onPositiveBtnClick() {
                if (!installed_from_playstore) {
                    Intent intent = new Intent(context, CheckforUpdatesService.class);
                    intent.putExtra("action", 2);
                    startService(intent);
                }
                // Open intent to playstore
                else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(isPro ? getString(R.string.k_manager_pro_link) : getString(R.string.k_manager_gp_link)));
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelBtnClick() {

            }
        };
        String name = prefUtils.getString(LATEST_GITHUB_VERSION_NAME, "");
        String versionName = " " + name.substring(10, name.lastIndexOf(".apk"));

        dialogFragment.Instantiate(getString(R.string.new_version_available) + versionName, getString(R.string.changelog) + "\n" + changelist,
                getString(R.string.download), getString(R.string.remind_me_later), dialogClickListener);
        dialogFragment.show(getSupportFragmentManager(), "klock");


    }

    private BroadcastReceiver  BReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            int key = intent.getIntExtra("key", 0);
            switch(key) {
                case 1:
                    boolean newVersion = fileHelper.newVersion(context);

                    if (newVersion) {
                        if (!alreadyRan) notifyOnUpdate();
                        drawer.addItemAtPosition(updateNotif, 1);
                    } else drawer.removeItem(99);
                    break;
                case 2:
                    loadSpinner();
                    break;
            }
        }
    };

    private void loadSpinner(){
        getArrayForRoms();
        // Initialize the spinner
        final SimpleListAdapter simpleListAdapter = new SimpleListAdapter(this, roms);
        searchableSpinner.setAdapter(simpleListAdapter);
        searchableSpinner.setSelectedItem(prefUtils.getString("selectedRom", getString(R.string.chooseRom)));
        searchableSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position, long id) {
                String selectedItem = simpleListAdapter.getItem(position).toString();
                prefUtils.putString("selectedRom", selectedItem);

                statusBarFragment.oosIndicators(fileHelper.getOos(selectedItem).equals("OxygenOS"));
                miscFragment.oosBg(fileHelper.getOos(selectedItem).equals("OxygenOS"));

                orSettingsButton.setVisibility(isOtherRoms() ? View.VISIBLE : View.GONE);
                questionMark.setVisibility(isOtherRoms() ? View.VISIBLE : View.GONE);

            } // to close the onItemSelected

            @Override
            public void onNothingSelected() {

            }
        });
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
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (touchEventInsideEditText(ev))
            searchableSpinner.hideEdit();
        return super.dispatchTouchEvent(ev);
    }

    public boolean touchEventInsideEditText(MotionEvent event){
        Rect editTextRect = new Rect();
        SpinnerLayout.getHitRect(editTextRect);

        return  (editTextRect.contains((int)event.getX(), (int)event.getY()));
    }


    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(BReceiver, new IntentFilter("message"));
        setTheme(prefUtils.getBool(PREF_BLACK_THEME) ? R.style.AppTheme_Dark : R.style.AppTheme);

    }

    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(BReceiver);
    }


    @BindView(R.id.listViewLayout) RelativeLayout bgLayout;
    @BindView(R.id.firstTextView) TextView oneTv;
    @BindView(R.id.secondTextView) TextView twoTv;
    @BindView(R.id.firstListView) ListView oneLv;
    @BindView(R.id.secondListView) ListView twoLv;
    @BindView(R.id.addButton) Button button;

    private void buildingProcess() {

        String[] check = new File(rootFolder).list(fileHelper.APK);
        int k = fileHelper.decreaseToLowest(check);
        final String apkVersion = "K-Klock_v" + k + ".apk";
        String romName = prefUtils.getString("selectedRom", getString(R.string.chooseRom));
        boolean dontdoit = false;
        try {
            if (!isOtherRoms() && prefUtils.getBool(PREF_QS_HEADER)) {
                File romZip = new File(Environment.getExternalStorageDirectory() + "/K-Manager/romSpecific/" + romName + ".zip");
                File tempZip = new File(Environment.getExternalStorageDirectory() + "/K-Manager/temp.zip");
                if (romZip.exists()) {
                    FileUtils.copyFile(romZip, tempZip);
                } else {
                    InputStream inputStream = context.getAssets().open("romSpecific/" + romName + ".zip");
                    FileUtils.copyInputStreamToFile(inputStream, tempZip);
                }
                if (tempZip.exists() && tempZip.isDirectory())
                    tempZip.delete();
                ZipUtil.explode(tempZip);
                File c = new File(tempZip, "quick_status_bar_expanded_header.xml");
                if (!c.exists()){
                    dontdoit = true;
                    DialogClickListener dialogClickListener = new DialogClickListener() {
                        @Override
                        public void onPositiveBtnClick() {
                            prefUtils.putString("selectedRom", getString(R.string.otherRomsBeta));
                            if (isOtherRoms()) questionMark.setVisibility(View.VISIBLE);
                            orSettingsButton.setVisibility(isOtherRoms() ? View.VISIBLE : View.GONE);
                            loadSpinner();
                            new ApkBuilder(context, loadingLayout, loadingTextView, defaultLayout, hasAll).execute(apkVersion, apkVersion, apkVersion);
                        }

                        @Override
                        public void onCancelBtnClick() {
                            new ApkBuilder(context, loadingLayout, loadingTextView, defaultLayout, hasAll).execute(apkVersion, apkVersion, apkVersion);

                        }
                    };
                    TextAlertDialogFragment alertDialogFragment = new TextAlertDialogFragment();
                    alertDialogFragment.Instantiate(
                            getString(R.string.important), getString(R.string.files_for_header_404), getString(R.string.okay),
                            getString(R.string.no), dialogClickListener
                    );
                    FileUtils.deleteDirectory(tempZip);
                    alertDialogFragment.show(getSupportFragmentManager(), "");
                }
            }
        }catch (IOException e){
            Log.e("klock", e.getMessage());
        }


        if (!dontdoit)new ApkBuilder(context, loadingLayout, loadingTextView, defaultLayout, hasAll).execute(apkVersion, apkVersion, apkVersion);

    }

    public void ShowIncluded(final View view){

        final ArrayList<String> titles = prefUtils.loadArray( ICON_TITLES);
        final ArrayList<String> values = prefUtils.loadArray(ICON_VALUES);

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
                    deleteItems(titles, values, name, value, ICON_TITLES, ICON_VALUES, "saveIcons");
                    ShowIncluded(view);
                }
            };

            ColorAdapter oneAdapter = new ColorAdapter(context, finalTitles, values, false, editListener, deleteListener);
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

        ColorAdapter twoAdapter = new ColorAdapter(context, one, two, true, null, null);
        twoLv.setAdapter(twoAdapter);
        twoTv.setText(getString(R.string.included_colors));
        oneTv.setText(getString(R.string.added_colors));
        showLayout(bgLayout);

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

    public void showIncludedColors(final View v){

        colorsTitles = prefUtils.loadArray( COLOR_TITLES);
        colorsValues = prefUtils.loadArray(COLOR_VALUES);

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
                    deleteItems(colorsTitles, colorsValues, name, value, COLOR_TITLES, COLOR_VALUES, "saveColors");
                    showIncludedColors(v);
                }
            };

            ColorAdapter oneAdapter = new ColorAdapter(context, finalColorsTitles, colorsValues, false, editListener, deleteListener);
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

        ColorAdapter twoAdapter = new ColorAdapter(context, one, two, true, null, null);
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


        formatsTitles = prefUtils.loadArray(FORMAT_TITLES);
        formatsValues = prefUtils.loadArray(FORMAT_VALUES);

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
                    deleteItems(formatsTitles, formatsValues, name, value, FORMAT_TITLES, FORMAT_VALUES, "saveFormats");
                    showIncludedFormats(v);
                }
            };

            FormatAdapter oneAdapter = new FormatAdapter(context, finalFormatsTitles,  false, editListener, deleteListener);
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

        FormatAdapter twoAdapter = new FormatAdapter(context, one, true, null, null);
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

        roms.add(betaString);
        try {
            String[] t = getAssets().list("romSpecific");
            List<String> temp = Arrays.asList(t);
            String[] notAssetsTemp = new File(Environment.getExternalStorageDirectory() + "/K-Manager/romSpecific").list(fileHelper.XML);
            if (notAssetsTemp != null) {
                for (String s : notAssetsTemp) {
                    if (!temp.contains(s)) {
                        temp.add(s);
                    }
                }
            }
            Collections.sort(temp);
            for(String s:temp){
                s = s.substring(0, s.lastIndexOf('.'));
                roms.add(s);
            };
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void colorPicked(MessageEvent event){

    }
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }



    @Override
    public void onColorSelected(int dialogId, int color) {
        EventBus.getDefault().post(new MessageEvent(dialogId, Integer.toHexString(color)));
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    private void promptTelegram(){
        if (isPackageInstalled("org.telegram.messenger", context.getPackageManager()) ||
                isPackageInstalled("org.telegram.plus", context.getPackageManager()) ||
                isPackageInstalled("org.thunderdog.challegram", context.getPackageManager())){

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

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String rootDir = Environment.getExternalStorageDirectory() + "/K-Klock/";
        String[] temps = new String[]{"temp", "temp2", "temp3", "tempF", "merger", "customInput"};
        for (String temp : temps) cleanDir(new File(rootDir + temp));
        File testKey = new File(rootDir + "test");
        if (testKey.exists()) testKey.delete();
        return null;
    }
}