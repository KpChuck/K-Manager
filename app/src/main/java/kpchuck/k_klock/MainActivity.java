package kpchuck.k_klock;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
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

import java.util.ArrayList;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.text.InputType;
import android.content.res.AssetManager;
import java.io.IOException;
import java.io.File;
import android.net.Uri;
import android.content.Intent;
import android.widget.Toast;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gr.escsoft.michaelprimez.searchablespinner.SearchableSpinner;
import gr.escsoft.michaelprimez.searchablespinner.interfaces.OnItemSelectedListener;
import kpchuck.k_klock.Adapters.ColorAdapter;
import kpchuck.k_klock.Adapters.FormatAdapter;
import kpchuck.k_klock.Adapters.SimpleListAdapter;
import kpchuck.k_klock.Interfaces.BtnClickListener;
import kpchuck.k_klock.Interfaces.PositiveClickListener;
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


    java.io.FilenameFilter fileNameFilterAPK = new java.io.FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            if(name.lastIndexOf('.')>0) {

                // get last index for '.' char
                int lastIndex = name.lastIndexOf('.');

                // get extension
                String str = name.substring(lastIndex);


                // match path name extension
                if(str.equals(".apk")) {
                    return true;
                }
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        betaString = getResources().getString(R.string.otherRomsBeta);
        new FileHelper().newFolder(rootFolder + "/userInput");

        if(!hasPermissions(this, PERMISSIONS)){
            android.support.v4.app.ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


        if(Build.VERSION.SDK_INT == 26 && !getPackageManager().canRequestPackageInstalls()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Install Apps Permission Required");
            TextView tv = new TextView(this);
            tv.setText(R.string.request_install_perms);
            builder.setView(tv);
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent k = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    k.setData(uri);
                    startActivity(k);

                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener(){

                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    shortToast("You will have to install K-Klock manually from the K-Klock folder without this permission granted");
                }
            });
            builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();

        }

        final Switch indicatorSwitch = (Switch) findViewById(R.id.networkSignalIndicatorSwitch);
        this.prefUtils = new PrefUtils(getApplicationContext());

        Switch qsSwitch = (Switch) findViewById(R.id.noQsTilesTv);
        Switch recentsSwitch = (Switch) findViewById(R.id.roundedRecents);
        Switch moveLeftSwitch = (Switch) findViewById(R.id.moveNetworkLeft);
        Switch hideStatusbar = (Switch) findViewById(R.id.hideStatusbar);
        Switch iconColors = (Switch) findViewById(R.id.colorIcons);
        Switch qsBg = (Switch) findViewById(R.id.qsBg);
        Switch minit = (Switch) findViewById(R.id.minitMod);
        Switch qstitle = (Switch) findViewById(R.id.qsTitle);
        Switch ampm = (Switch) findViewById(R.id.ampm);

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

        if (!getOos(prefUtils.getString("selectedRom", getString(R.string.chooseRom))).equals("OxygenOS")) indicatorSwitch.setVisibility(View.GONE);

        if(prefUtils.getBoolTrue("joinTelegram")) promptTelegram();

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
                    indicatorSwitch.setVisibility(View.VISIBLE);
                }else indicatorSwitch.setVisibility(View.GONE);

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
        if (prefUtils.getBool("qsBgPref")) copyAssets("unviersal", "qsBgs.zip");
        if (prefUtils.getBool("qsTitlePref")) copyAssets("universal", "qsTitle.zip");
        if (prefUtils.getBool("amPref")) copyAssets("universal", "ampm.zip");
        if(prefUtils.getBool("indicatorPref") && getOos(romName).equals("OxygenOS Nougat")) copyAssets("universal", "indicatorsN.zip".trim());
        if(prefUtils.getBool("indicatorPref") && getOos(romName).equals("OxygenOS Oreo")) copyAssets("universal", "indicatorsO.zip".trim());


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

    public void iconPref(View v){
        Switch qsSwitch = (Switch) findViewById(R.id.colorIcons);
        prefUtils.setSwitchPrefs(qsSwitch, "iconPref");
    }

    private ImagePicker imagePicker;

    public void qsBgPref(View v){
        final Switch qsSwitch = (Switch) findViewById(R.id.qsBg);
        if(qsSwitch.isChecked()) {

            imagePicker = new ImagePicker(this);
            imagePicker.setImagePickerCallback(new ImagePickerCallback(){
                @Override
                public void onImagesChosen(List<ChosenImage> images) {

                    String filePath = images.get(0).getOriginalPath();

                    if (!filePath.substring(filePath.lastIndexOf("."), filePath.length()).equals(".png")){
                        shortToast("The image you have chosen is not a png. Convert it to a png and try again");
                        prefUtils.putBool("qsBgPref", false);
                        qsSwitch.setChecked(false);
                        prefUtils.remove("qsBgFilePath");
                        new File(filePath).delete();
                    }
                    else{
                        prefUtils.putString("qsBgFilePath", filePath);
                        prefUtils.putBool("qsBgPref", true);
                    }
                    File dir = new File(new File(filePath).getParent());
                    String[] files = dir.list();
                    for (String f: files){
                        String check = dir.getAbsolutePath() + slash + f;
                        if (!filePath.equals(check)){
                            new File(check).delete();
                        }
                    }
                }

                @Override
                public void onError(String message) {
                    // Do error handling
                    prefUtils.putBool("qsBgPref", false);
                    prefUtils.remove("qsBgFilePath");
                    qsSwitch.setChecked(false);
                }}
            );
            imagePicker.pickImage();

        }else{
            prefUtils.putBool("qsBgPref", false);
            prefUtils.remove("qsBgFilePath");
        }

    }

    public void amPref(View view){
        Switch mswitch = (Switch) findViewById(R.id.ampm);
        prefUtils.setSwitchPrefs(mswitch, "amPref");
    }

    public void qsPref(View v){
        Switch qsSwitch = (Switch) findViewById(R.id.noQsTilesTv);
        prefUtils.setSwitchPrefs(qsSwitch, "qsPref");
    }

    public void titlePref(View v){
        Switch mswitch = (Switch) findViewById(R.id.qsTitle);
        prefUtils.setSwitchPrefs(mswitch, "qsTitlePref");
    }

    public void hideStatusbarPref(View v){
        Switch mswitch = (Switch) findViewById(R.id.hideStatusbar);
        prefUtils.setSwitchPrefs(mswitch, "hideStatusbarPref");
    }

    public void moveLeftPref(View v){
        Switch qsSwitch = (Switch) findViewById(R.id.moveNetworkLeft);
        prefUtils.setSwitchPrefs(qsSwitch, "moveLeftPref");

    }

    public void recentsPref(View v){

        Switch qsSwitch = (Switch) findViewById(R.id.roundedRecents);
        prefUtils.setSwitchPrefs(qsSwitch, "recentsPref");
    }

    public void indicatorPref(View v){
        Switch qsSwitch = (Switch) findViewById(R.id.networkSignalIndicatorSwitch);
        prefUtils.setSwitchPrefs(qsSwitch, "indicatorPref");
        }


    public void minitPref(View v){
        final Switch mswitch = (Switch) findViewById(R.id.minitMod);
        if(mswitch.isChecked()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("IMPORTANT");
            TextView tv = new TextView(this);
            tv.setText(R.string.minit_disclaimer);
            builder.setView(tv);
            builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    prefUtils.putBool("minitPref", true);
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener(){

                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    prefUtils.putBool("minitPref", false);
                    mswitch.setChecked(false);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }else prefUtils.putBool("minitPref", false);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_DEVICE) {
                imagePicker.submit(data);
            }
        }else{
            Switch qsSwitch = (Switch) findViewById(R.id.qsBg);
            prefUtils.putBool("qsBgPref", false);
            prefUtils.remove("qsBgFilePath");
            qsSwitch.setChecked(false);

        }
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



    public int decreaseToLowest(String[] testStringArray){
        int kk;
        if(testStringArray.length != 0) {
            java.util.Arrays.sort(testStringArray);
            java.util.List<String> list = java.util.Arrays.asList(testStringArray);
            java.util.Collections.reverse(list);
            testStringArray = (String[]) list.toArray();

            ArrayList<Integer> listOfVersions = new ArrayList<>();

            for(String s : testStringArray){
                String toInt = s.substring(s.indexOf("v")+1, s.lastIndexOf("."));
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
        inputMenu(getString(R.string.add_color_value_title), getString(R.string.add_color_value_hint), colorsValues, getResources().getString(R.string.ok),
                false, "saveColors", "colorsValues",
                 false, v);

        inputMenu(getString(R.string.add_color_name_title), getString(R.string.add_color_name_hint), colorsTitles,
                getResources().getString(R.string.next), true, "saveColors", "colorsTitles",
                false, v);


    }

    public void addCustomFormats(View v){
        inputMenu(getResources().getString(R.string.add_format_value_title), getResources().getString(R.string.add_format_value_hint),
                formatsValues, getResources().getString(R.string.ok), false, "saveFormats", "formatsValues",
                false, v);
        inputMenu(getResources().getString(R.string.add_format_name_title), getResources().getString(R.string.add_format_name_hint),
                formatsTitles, getString(R.string.next), true, "saveFormats", "formatsTitles",
                false, v);


    }

    public void hideList(View v){
        RelativeLayout rv = (RelativeLayout) findViewById(R.id.listViewLayout);
        if(rv.getVisibility() == View.VISIBLE){
            RelativeLayout bgLayout = (RelativeLayout) findViewById(R.id.listViewLayout);
            bgLayout.setVisibility(View.GONE);
        }
    }

    public void showIncludedColors(final View v){
        RelativeLayout bgLayout = (RelativeLayout) findViewById(R.id.listViewLayout);
        TextView oneTv = (TextView) findViewById(R.id.firstTextView);
        TextView twoTv = (TextView) findViewById(R.id.secondTextView);
        ListView oneLv = (ListView) findViewById(R.id.firstListView);
        ListView twoLv = (ListView) findViewById(R.id.secondListView);
        Button button = (Button) findViewById(R.id.addButton);

        SharedPreferences bleh = PreferenceManager.getDefaultSharedPreferences(this);
        if(bleh.getBoolean("saveColors", true)){
           try {
               colorsTitles = prefUtils.loadArray(colorsTitles, "colorsTitles");
           }catch(Exception e){
               Log.d("klock", e.getMessage());
           }
            try {
                colorsValues = prefUtils.loadArray(colorsValues, "colorsValues");
            }catch(Exception e){
                Log.d("klock", e.getMessage());
            }

        }

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

                    inputEditMenu(name, value, colorsValues, getResources().getString(R.string.ok), false,
                            "saveColors", "colorsValues", name, "colorsTitles", finalColorsTitles, value, "COLOR", v);
                }
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
                inputMenu(getString(R.string.add_color_value_title), getString(R.string.add_color_value_hint), colorsValues, getResources().getString(R.string.ok),
                        false, "saveColors", "colorsValues",
                        true, v);

                inputMenu(getString(R.string.add_color_name_title), getString(R.string.add_color_name_hint), colorsTitles,
                        getResources().getString(R.string.next), true, "saveColors", "colorsTitles",
                        false, v);

            }
        });

        ArrayList<String> one = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.included_colors_title)));
        ArrayList<String> two = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.included_colors_value)));

        ColorAdapter twoAdapter = new ColorAdapter(getApplicationContext(), one, two, true, null, null);
        twoLv.setAdapter(twoAdapter);
        twoTv.setText(getString(R.string.included_colors));
        oneTv.setText(getString(R.string.added_colors));
        bgLayout.setVisibility(View.VISIBLE);

    }
    public void showIncludedFormats(final View v){
        RelativeLayout bgLayout = (RelativeLayout) findViewById(R.id.listViewLayout);
        TextView oneTv = (TextView) findViewById(R.id.firstTextView);
        TextView twoTv = (TextView) findViewById(R.id.secondTextView);
        ListView oneLv = (ListView) findViewById(R.id.firstListView);
        ListView twoLv = (ListView) findViewById(R.id.secondListView);
        Button button = (Button) findViewById(R.id.addButton);

        SharedPreferences bleh = PreferenceManager.getDefaultSharedPreferences(this);
        if(bleh.getBoolean("saveFormats", true)){
            formatsTitles.clear();
            try {
                formatsTitles = prefUtils.loadArray(formatsTitles, "formatsTitles");
            }catch(Exception e){
                Log.d("klock", e.getMessage());
            }
            try {
                formatsValues = prefUtils.loadArray(formatsValues, "formatsValues");
            }catch(Exception e){
                Log.d("klock", e.getMessage());
            }

        }


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

                    inputEditMenu(name, value, formatsValues, getResources().getString(R.string.ok), false,
                            "saveFormats", "formatsValues", name, "formatsTitles", finalFormatsTitles, value, "FORMAT", v);
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
                inputMenu(getResources().getString(R.string.add_format_value_title), getResources().getString(R.string.add_format_value_hint),
                        formatsValues, getResources().getString(R.string.ok), false, "saveFormats", "formatsValues",
                        true, v);
                inputMenu(getResources().getString(R.string.add_format_name_title), getResources().getString(R.string.add_format_name_hint),
                        formatsTitles, getString(R.string.next), true, "saveFormats", "formatsTitles",
                        false, v);
            }
        });

        ArrayList<String> one = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.included_formats_title)));

        FormatAdapter twoAdapter = new FormatAdapter(getApplicationContext(), one, true, null, null);
        twoLv.setAdapter(twoAdapter);
        twoTv.setText(getString(R.string.included_formats));
        oneTv.setText(getString(R.string.added_formats));
        bgLayout.setVisibility(View.VISIBLE);
    }


    public void deleteItems(ArrayList<String> titles, ArrayList<String> values, String title, String value, String titleArrayKey, String valueArrayKey, String toSaveBoolKey){
        SharedPreferences bleh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        title = title.replace(" ", "_")+".xml";
        FileHelper fileHelper = new FileHelper();

        if(bleh.getBoolean(toSaveBoolKey, true)){
            titles = prefUtils.loadArray(titles, titleArrayKey);
            values = prefUtils.loadArray(values, valueArrayKey);

            titles = fileHelper.deleteItemFromArray(title, titles);
            values = fileHelper.deleteItemFromArray(value, values);

            prefUtils.saveArray(titles, titleArrayKey);
            prefUtils.saveArray(values, valueArrayKey);
        }else {
            fileHelper.deleteItemFromArray(title, titles);
            fileHelper.deleteItemFromArray(value, values);
        }

    }

    public void inputMenu(String title, String hint, final ArrayList<String> arrayList, String positiveButtonText,
                          final boolean isXml, final String toSaveBoolKey, final String arrayListNameKey,
                          final boolean refresh, final View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(hint);
        builder.setView(input);
        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                if(isXml){
                    m_Text = m_Text.replace(' ','_') + ".xml";
                }

                SharedPreferences bleh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if(bleh.getBoolean(toSaveBoolKey, true)){
                   try{
                       ArrayList<String> templist = prefUtils.loadArray(arrayList, arrayListNameKey);
                       templist.add(m_Text);
                       prefUtils.saveArray(templist, arrayListNameKey);

                   }catch (Exception e){
                       Log.d("klock", e.getMessage());
                   }
                }else{
                    arrayList.add(m_Text);

                }
                if(refresh){
                    if(arrayListNameKey.equals("colorsValues")) showIncludedColors(view);
                    if (arrayListNameKey.equals("formatsValues")) showIncludedFormats(view);
                }

            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialogInterface) {

                ArrayList<String> one;
                ArrayList<String> two;
                if(arrayListNameKey.equals("colorsValues")){
                    one = prefUtils.loadArray(colorsTitles, "colorsTitles");
                    two = prefUtils.loadArray(colorsValues, "colorsValues");
                }else{
                    one = prefUtils.loadArray(formatsTitles, "formatsTitles");
                    two = prefUtils.loadArray(formatsValues, "formatsValues");
                }
                if(one.size() != two.size()){
                if(!isXml){
                    SharedPreferences bleh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String key;
                    ArrayList<String> tempArray;
                    if(bleh.getBoolean(toSaveBoolKey, true)){

                        if(arrayListNameKey.equals("colorsValues")){
                            key = "colorsTitles";
                            tempArray = colorsTitles;
                        }else if(arrayListNameKey.equals("formatsValues")){
                            key = "formatsTitles";
                            tempArray = formatsTitles;
                        }
                        else{
                            key = null;
                            tempArray = null;
                        }
                        tempArray = prefUtils.loadArray(tempArray, key);
                        if(tempArray.size() != 0) {
                            int k = tempArray.size() - 1;

                            tempArray.remove(k);
                            prefUtils.saveArray(tempArray, key);
                        }
                    }else{
                        if(arrayListNameKey.equals("colorsValues")){
                            tempArray = colorsTitles;
                        }else if(arrayListNameKey.equals("formatsValues")){
                            tempArray = formatsTitles;
                        }
                        else tempArray = null;
                        if(tempArray.size() != 0) {
                            int k = tempArray.size() - 1;
                            tempArray.remove(k);
                        }
                    }

                }
            }}
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        builder.show();
    }

    public void inputEditMenu(String title, String edit, final ArrayList<String> arrayList, String positiveButtonText,
                              final boolean isXml, final String toSaveBoolKey, final String valueArrayKey, final String name,
                              final String nameListArrayKey, final ArrayList<String> nameArray,
                              final String previousValue, final String colororformat, final View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(edit);
        builder.setView(input);
        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FileHelper fileHelper = new FileHelper();

                String m_Text = input.getText().toString();

                SharedPreferences bleh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if(bleh.getBoolean(toSaveBoolKey, true)){
                    ArrayList<String> namesArray = prefUtils.loadArray(nameArray, nameListArrayKey);
                    ArrayList<String> valuesArray = prefUtils.loadArray(arrayList, valueArrayKey);

                    String k = name.replace(' ','_') + ".xml";

                    fileHelper.deleteItemFromArray(k, namesArray);
                    fileHelper.deleteItemFromArray(previousValue, valuesArray);
                    //Add Value
                    valuesArray.add(m_Text);
                    prefUtils.saveArray(valuesArray, valueArrayKey);
                    //Add Name

                    namesArray.add(k);
                    ArrayList<String> templist = new ArrayList<>();
                    for(int f = 0; f<namesArray.size(); f++){
                        templist.add(namesArray.get(f));
                    }

                    prefUtils.saveArray(templist, nameListArrayKey);
                    templist.clear();

                }else {
                    arrayList.remove(previousValue);
                    nameArray.remove(name);

                    arrayList.add(m_Text);
                    nameArray.add(name);

                }
                if (colororformat.equals("COLOR")) showIncludedColors(view);
                if (colororformat.equals("FORMAT")) showIncludedFormats(view);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
            PositiveClickListener positiveClickListener = new PositiveClickListener() {
                @Override
                public void onBtnClick() {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/kklock"));
                    getApplicationContext().startActivity(browserIntent);
                }
            };
            simpleDialogText("K-Klock Telegram", getString(R.string.joinTelegram), positiveClickListener);
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

    public void simpleDialogText(String title, String message, final PositiveClickListener positiveClick){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        TextView tv = new TextView(this);
        tv.setText(message);
        builder.setView(tv);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                positiveClick.onBtnClick();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener(){

            @Override
            public void onCancel(DialogInterface dialogInterface) {

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

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
        SharedPreferences bleh = PreferenceManager.getDefaultSharedPreferences(this);
        if(bleh.getBoolean("saveColors", true)){

            try{
                colorsTitles = prefUtils.loadArray(colorsTitles, "colorsTitles");
            }catch (Exception e){
                Log.d("klock", e.getMessage());
            }
        }
        if(bleh.getBoolean("saveColors", true)){

            try{
                colorsValues = prefUtils.loadArray(colorsValues, "colorsValues");
            }catch (Exception e){
                Log.d("klock", e.getMessage());
            }
        }
        if(bleh.getBoolean("saveFormats", true)){
            try{
                formatsTitles = prefUtils.loadArray(formatsTitles, "formatsTitles");
            }catch (Exception e){
                Log.d("klock", e.getMessage());
            }
        }
        if(bleh.getBoolean("saveFormats", true)) {

            try{
                formatsValues = prefUtils.loadArray(formatsValues, "formatsValues");
            }catch (Exception e){
                Log.d("klock", e.getMessage());
            }
            }

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
