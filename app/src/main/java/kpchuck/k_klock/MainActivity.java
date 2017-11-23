package kpchuck.k_klock;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.widget.CardView;
import android.util.Log;
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
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.FilenameFilter;
import java.util.ArrayList;

import android.content.res.AssetManager;
import java.io.IOException;
import java.io.File;
import android.net.Uri;
import android.content.Intent;
import android.widget.Toast;

import com.stephentuso.welcome.WelcomeHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gr.escsoft.michaelprimez.searchablespinner.SearchableSpinner;
import gr.escsoft.michaelprimez.searchablespinner.interfaces.OnItemSelectedListener;
import kpchuck.k_klock.Activities.MyWelcomeActivity;
import kpchuck.k_klock.Adapters.ColorAdapter;
import kpchuck.k_klock.Adapters.FormatAdapter;
import kpchuck.k_klock.Adapters.SimpleListAdapter;
import kpchuck.k_klock.Fragments.InputAlertDialogFragment;
import kpchuck.k_klock.Fragments.TextAlertDialogFragment;
import kpchuck.k_klock.Interfaces.BtnClickListener;
import kpchuck.k_klock.Interfaces.DialogClickListener;
import kpchuck.k_klock.Utils.FileHelper;
import kpchuck.k_klock.Utils.PrefUtils;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ArrayList<String> roms = new ArrayList<String>();
    ArrayList<String> colorsTitles = new ArrayList<String>();
    ArrayList<String> formatsTitles = new ArrayList<String>();
    ArrayList<String> colorsValues = new ArrayList<String>();
    ArrayList<String> formatsValues = new ArrayList<String>();
    String slash = "/";
    PrefUtils prefUtils;

    String rootFolder = android.os.Environment.getExternalStorageDirectory() + slash + "K-Klock";
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
    String betaString;
    WelcomeHelper welcomeScreen;


    FilenameFilter fileNameFilterAPK = new java.io.FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            if(name.lastIndexOf('.')>0) {
                int lastIndex = name.lastIndexOf('.');
                String str = name.substring(lastIndex);
                if(str.equals(".apk")) {
                    return true;
                }}return false;};

    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        welcomeScreen.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostResume() {
        if(prefUtils.getBoolTrue("joinTelegram")) promptTelegram();

        if(Build.VERSION.SDK_INT == 26 && !getPackageManager().canRequestPackageInstalls()){
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
        super.onPostResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        betaString = getResources().getString(R.string.otherRomsBeta);
        new FileHelper().newFolder(rootFolder + "/userInput");

        welcomeScreen = new WelcomeHelper(this, MyWelcomeActivity.class);
        welcomeScreen.show(savedInstanceState);

        if(!hasPermissions(this, PERMISSIONS)){
            android.support.v4.app.ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


        final Switch indicatorSwitch = findViewById(R.id.networkSignalIndicatorSwitch);
        this.prefUtils = new PrefUtils(getApplicationContext());

        Switch qsSwitch = findViewById(R.id.noQsTilesTv);
        Switch recentsSwitch = findViewById(R.id.roundedRecents);
        Switch moveLeftSwitch = findViewById(R.id.moveNetworkLeft);
        Switch hideStatusbar = findViewById(R.id.hideStatusbar);
        Switch iconColors = findViewById(R.id.colorIcons);
        final Switch qsBg = findViewById(R.id.qsBg);
        Switch minit = findViewById(R.id.minitMod);
        Switch qstitle = findViewById(R.id.qsTitle);
        Switch ampm = findViewById(R.id.ampm);
        CardView iconView = findViewById(R.id.iconColorCardView);

        ampm.setChecked(prefUtils.getBool("amPref"));
        qsSwitch.setChecked(prefUtils.getBool("qsPref"));
        iconColors.setChecked(prefUtils.getBool("iconPref"));
        recentsSwitch.setChecked(prefUtils.getBool("recentsPref"));
        indicatorSwitch.setChecked(prefUtils.getBool("indicatorPref"));
        moveLeftSwitch.setChecked(prefUtils.getBool("moveLeftPref"));
        hideStatusbar.setChecked(prefUtils.getBool("hideStatusbarPref"));
        qsBg.setChecked(prefUtils.getBool("qsBgPref"));
        minit.setChecked(prefUtils.getBool("minitPref"));
        qstitle.setChecked(prefUtils.getBool("qsTitlePref"));
        if (prefUtils.getBool("iconPref")) iconView.setVisibility(View.VISIBLE);

        if (!getOos(prefUtils.getString("selectedRom", getString(R.string.chooseRom))).equals("OxygenOS")) indicatorSwitch.setVisibility(View.GONE);
        if (getOos(prefUtils.getString("selectedRom", getString(R.string.chooseRom))).equals("OxygenOS")) qsBg.setVisibility(View.GONE);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.loadingId);
        TextView textView = (TextView) findViewById(R.id.loadingTextView);
        new CleanupFiles(relativeLayout, textView).execute();


        File rootfile = new File(rootFolder);
        if(!rootfile.exists()){
            rootfile.mkdirs();
        }if(!rootfile.isDirectory()) rootfile.mkdirs();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (prefUtils.getBool("gsBgPref") && !checkQsFile()) return;

                String romName = prefUtils.getString("selectedRom", getString(R.string.chooseRom));
                if(romName.equals(getResources().getString(R.string.chooseRom))){
                    shortToast(getResources().getString(R.string.selectRomToast));

                }
                else if(romName.equals(betaString)) {
                    OtherRomsHandler handler = new OtherRomsHandler(getApplicationContext());
                    if (handler.checkForXmls()) {
                       handler.execute();
                       buildingProcess(romName);
                    }
                    else if (!handler.checkForXmls()){
                        Intent i = new Intent(getApplicationContext(), InformationWebViewActivity.class);
                        i.putExtra("value", 4);
                        startActivity(i);
                    }
                }

                else if(!romName.equals("") || !romName.equals(betaString)) {
                    copyAssets("romSpecific", romName+".zip".trim());
                    buildingProcess(romName);
            }}


        });




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getArrayForRoms();

        final SearchableSpinner searchableSpinner = (SearchableSpinner) findViewById(R.id.romSelectionSpinner);
        final SimpleListAdapter simpleListAdapter = new SimpleListAdapter(this, roms);
        searchableSpinner.setAdapter(simpleListAdapter);
        searchableSpinner.setSelectedItem(prefUtils.getString("selectedRom", getString(R.string.chooseRom)));
        searchableSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(View view, int position, long id) {
                String selectedItem = simpleListAdapter.getItem(position).toString();
                prefUtils.putString("selectedRom", selectedItem);
                if (getOos(selectedItem).equals("OxygenOS")){
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

    private void buildingProcess(String romName) {
        xmlBuilder();
        copyAssets("universal", "universalFiles.zip".trim());

        if(prefUtils.getBool("qsPref")) copyAssets("universal", "qsTiles.zip".trim());
        if(prefUtils.getBool("iconPref")) copyAssets("universal", "colorIcons.zip".trim());
        if(prefUtils.getBool("recentsPref")) copyAssets("universal", "recents.zip".trim());
        if (prefUtils.getBool("hideStatusbarPref")) copyAssets("universal", "hideStatusbar.zip".trim());
        if (prefUtils.getBool("qsBgPref") && !getOos(romName).equals("OxygenOS")) copyAssets("unviersal", "qsBgs.zip");
        if (prefUtils.getBool("qsTitlePref")) copyAssets("universal", "qsTitle.zip");
        if (prefUtils.getBool("amPref")) copyAssets("universal", "ampm.zip");
        if(prefUtils.getBool("indicatorPref") && romName.equals("OxygenOS Nougat")) copyAssets("universal", "indicatorsN.zip".trim());
        if(prefUtils.getBool("indicatorPref") && romName.equals("OxygenOS Oreo")) copyAssets("universal", "indicatorsO.zip".trim());


        ScrollView frameLayout = (ScrollView) findViewById(R.id.defaultLayout);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.loadingId);
        TextView textView = (TextView) findViewById(R.id.loadingTextView);

        String[] check = new File(rootFolder).list(fileNameFilterAPK);
        int k = decreaseToLowest(check);
        String apkVersion = "K-Klock v" + k + ".apk";

        new apkBuilder(getApplication(), relativeLayout, textView, frameLayout).execute(apkVersion,apkVersion,apkVersion);
    }

    private String getOos(String oos){
        if (oos.length() <8) return "thisIsNotOxygenOS";
        oos = oos.substring(0, 8);
        return oos;
    }

    public boolean checkQsFile(){
        String path = prefUtils.getString("qsBgFilePath", "null");
        if (path=="null") return  false;
        File file = new File(path);
        if (!file.exists()) return false;
        String ext = FilenameUtils.getExtension(file.getName());
        shortToast(ext);
        return !ext.equals("png");
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

    public int decreaseToLowest(String[] testStringArray){
        int kk;

        Arrays.sort(testStringArray);
        List<String> list = Arrays.asList(testStringArray);
        Collections.reverse(list);

        ArrayList<String> klockArray = new ArrayList<>();
        for (String s: list) if (s.substring(0, 7).equals("K-Klock")) klockArray.add(s);

        if(klockArray.size() != 0) {
            testStringArray = (String[]) klockArray.toArray();

            ArrayList<Integer> listOfVersions = new ArrayList<>();

            for(String s : testStringArray){
                    String toInt = s.substring(s.indexOf("v") + 1, s.lastIndexOf("."));
                    int bleh = Integer.parseInt(toInt);
                    listOfVersions.add(bleh);

            }
            Integer[] intArray = listOfVersions.toArray(new Integer[listOfVersions.size()]);
            Arrays.sort(intArray);
            List<Integer> li = Arrays.asList(intArray);
            Collections.reverse(li);
            intArray = (Integer[]) li.toArray();
            kk = intArray[0]+1;
        }else{ kk = 1; }
        return kk;
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
            //roms.add(getResources().getString(R.string.chooseRom));
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        RelativeLayout rv = findViewById(R.id.listViewLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else if (rv.getVisibility() == View.VISIBLE){
            hideLayout(rv);
        } else {
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }else if (id == R.id.telegramJoin){
             Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/kklock"));
            startActivity(browserIntent);
        }
        else if (id == R.id.otherroms_help){
             Intent i = new Intent(this, InformationWebViewActivity.class);
            i.putExtra("value", 4);
            startActivity(i);
        }else if(id == R.id.faq){
            Intent i = new Intent(this, InformationWebViewActivity.class);
            i.putExtra("value", 1);
            startActivity(i);
        }else if(id == R.id.qsbghelp){
            Intent i = new Intent(this, InformationWebViewActivity.class);
            i.putExtra("value", 2);
            startActivity(i);
        }else if(id == R.id.networkhelp){
            Intent i = new Intent(this, InformationWebViewActivity.class);
            i.putExtra("value", 3);
            startActivity(i);

        }else if(id == R.id.whatIsRgb){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.rapidtables.com/web/color/RGB_Color.htm"));
            startActivity(browserIntent);
        }else if(id == R.id.clockFormatsHelp){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://forum.xda-developers.com/showthread.php?t=2713812"));
            startActivity(browserIntent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    private void copyAssets(String assetDir, String whichString) {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list(assetDir);
        } catch (IOException e) {
            android.util.Log.e("tag", "Failed to get asset file list.", e);

        }
        if (files != null) for (String filename : files) {
            if(filename.equals(whichString)){
            java.io.InputStream in = null;
            java.io.OutputStream out = null;
            try {
                in = assetManager.open(assetDir + slash + filename);
                File tempFile = new File(rootFolder + slash + "temp");
                tempFile.mkdirs();
                File outFile = new File(rootFolder + slash + "temp" + slash + filename);
                out = new java.io.FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                android.util.Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }}
            }
        }
    }

    private void copyFile(java.io.InputStream in, java.io.OutputStream out) throws java.io.IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public void xmlBuilder(){

            colorsTitles = prefUtils.loadArray("colorsTitles");

            colorsValues = prefUtils.loadArray("colorsValues");

            formatsTitles = prefUtils.loadArray("formatsTitles");

            formatsValues = prefUtils.loadArray("formatsValues");

            int colorLimit = colorsTitles.size();
            for (int i = 0; i < colorLimit; i++) {
                String colorTitle = colorsTitles.get(i);
                String colorValue = colorsValues.get(i);
                xmlCreation xmlCreator = new xmlCreation();
                xmlCreator.putContext(getApplicationContext());
                xmlCreator.createTypeA(colorTitle, colorValue);
            }

            int formatLimit = formatsTitles.size();
            for (int i = 0; i < formatLimit; i++) {
                String formatTitle = formatsTitles.get(i);
                String formatValue = formatsValues.get(i);
                xmlCreation xmlCreator = new xmlCreation();
                xmlCreator.putContext(getApplicationContext());
                xmlCreator.createTypeB(formatTitle, formatValue);
            }

            if (prefUtils.getBool("iconPref")){
                ArrayList<String> titles = prefUtils.loadArray("iconsTitles");
                ArrayList<String> values = prefUtils.loadArray("iconsValues");
                for (int i = 0; i < titles.size(); i ++){
                    String title = titles.get(i);
                    String value = values.get(i);
                    xmlCreation xmlcreation = new xmlCreation();
                    xmlcreation.putContext(getApplicationContext());
                    xmlcreation.createIcons(title, value);
                }
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

    RelativeLayout relativeLayout;
    TextView tv;

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
        String[] temps = new String[]{"temp", "temp2", "temp3"};
        for (String temp : temps) cleanDir(new File(rootDir + temp));
        File testKey = new File(rootDir + "test");
        if (testKey.exists()) testKey.delete();
        return null;
    }
}