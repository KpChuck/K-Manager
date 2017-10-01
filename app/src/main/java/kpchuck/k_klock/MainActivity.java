package kpchuck.k_klock;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
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
import android.widget.Spinner;
import android.widget.ArrayAdapter;

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

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import kpchuck.k_klock.Utils.FileHelper;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ArrayList<String> roms = new ArrayList<String>();
    ArrayList<String> colorsTitles = new ArrayList<String>();
    ArrayList<String> formatsTitles = new ArrayList<String>();
    ArrayList<String> colorsValues = new ArrayList<String>();
    ArrayList<String> formatsValues = new ArrayList<String>();
    String prefFile = "prefFileName";
    String slash = "/";
    SharedPreferences myPref;
    SharedPreferences.Editor editor;

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

        if(!hasPermissions(this, PERMISSIONS)){
            android.support.v4.app.ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        Switch qsSwitch = (Switch) findViewById(R.id.noQsTilesTv);
        Switch recentsSwitch = (Switch) findViewById(R.id.roundedRecents);
        Switch moveLeftSwitch = (Switch) findViewById(R.id.moveNetworkLeft);
        Switch hideStatusbar = (Switch) findViewById(R.id.hideStatusbar);
        Switch iconColors = (Switch) findViewById(R.id.colorIcons);
        Switch qsBg = (Switch) findViewById(R.id.qsBg);
        Switch minit = (Switch) findViewById(R.id.minitMod);
        Switch title = (Switch) findViewById(R.id.qsTitle);

        final Switch indicatorSwitch = (Switch) findViewById(R.id.networkSignalIndicatorSwitch);
        final SharedPreferences pref = getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        final SharedPreferences.Editor meditor = pref.edit();
        SharedPreferences.Editor editor = pref.edit();

        final SharedPreferences myPref = getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        this.editor=editor;
        this.myPref=myPref;
        qsSwitch.setChecked(pref.getBoolean("qsPref", false));
        iconColors.setChecked(pref.getBoolean("iconPref", false));
        recentsSwitch.setChecked(pref.getBoolean("recentsPref", false));
        indicatorSwitch.setChecked(pref.getBoolean("indicatorPref", false));
        moveLeftSwitch.setChecked(pref.getBoolean("moveLeftPref", false));
        hideStatusbar.setChecked(pref.getBoolean("hideStatusbarPref", false));
        qsBg.setChecked(pref.getBoolean("qsBgPref", false));
        minit.setChecked(pref.getBoolean("minitPref", false));
        title.setChecked(pref.getBoolean("qsTitlePref", false));


        File rootfile = new File(rootFolder);
        if(!rootfile.exists()){
            rootfile.mkdirs();
        }if(!rootfile.isDirectory()) rootfile.mkdirs();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (myPref.getBoolean("gsBgPref", false) && !checkQsFile()) return;

                new ErrorHandle().resetAsyncError(getApplicationContext());

                String romName = pref.getString("selectedRom", getResources().getString(R.string.chooseRom));
                if(romName.equals(getResources().getString(R.string.chooseRom))){
                    shortToast(getResources().getString(R.string.selectRomToast));
                }
                else if(romName.equals(betaString)) {
                    OtherRomsHandler handler = new OtherRomsHandler(getApplicationContext());
                    if (handler.checkForXmls()) {

                        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.loadingId);
                        TextView textView = (TextView) findViewById(R.id.loadingTextView);
                        ScrollView frameLayout = (ScrollView) findViewById(R.id.defaultLayout);

                        //      XmlExtractor xmlExtractor = new XmlExtractor(getApplicationContext(), relativeLayout, textView);
                        //     xmlExtractor.throwInTheDex();
                       handler.execute();

                        xmlBuilder();
                        copyAssets("universal", "universalFiles.zip".trim());

                        if (pref.getBoolean("qsPref", false))
                            copyAssets("universal", "qsTiles.zip".trim());
                        if (pref.getBoolean("recentsPref", false))
                            copyAssets("universal", "recents.zip".trim());
                        if (pref.getBoolean("hideStatusbarPref", false)) copyAssets("universal", "hideStatusbar.zip".trim());
                        if(pref.getBoolean("iconPref", false)) copyAssets("universal", "colorIcons.zip".trim());

                        if (pref.getBoolean("qsBgPref", false)) copyAssets("unviersal", "qsBgs.zip");
                        if (pref.getBoolean("qsTitlePref", false)) copyAssets("universal", "qsTitle.zip");


                        handler.execute();


                        String[] check = new File(rootFolder).list(fileNameFilterAPK);

                        int k = decreaseToLowest(check);
                        String apkVersion = "K-Klock v" + k + ".apk";

                        new apkBuilder(getApplication(), relativeLayout, textView, frameLayout).execute(apkVersion, apkVersion, apkVersion);
                    }
                    else if (!handler.checkForXmls()){
                        Intent i = new Intent(getApplicationContext(), OtherRomsInfoActivity.class);
                        startActivity(i);
                    }
                }

                else if(!romName.equals("") || !romName.equals(betaString)) {
                    copyAssets("romSpecific", romName+".zip".trim());

                    xmlBuilder();
                    copyAssets("universal", "universalFiles.zip".trim());

                    if(pref.getBoolean("qsPref", false)) copyAssets("universal", "qsTiles.zip".trim());
                    if(pref.getBoolean("iconPref", false)) copyAssets("universal", "colorIcons.zip".trim());
                    if(pref.getBoolean("recentsPref", false)) copyAssets("universal", "recents.zip".trim());
                    if (pref.getBoolean("hideStatusbarPref", false)) copyAssets("universal", "hideStatusbar.zip".trim());
                    if(pref.getBoolean("indicatorPref", false) && getOos(romName).equals("OxygenOS")) copyAssets("universal", "indicators.zip".trim());
                    if (pref.getBoolean("qsBgPref", false)) copyAssets("unviersal", "qsBgs.zip");
                    if (pref.getBoolean("qsTitlePref", false)) copyAssets("universal", "qsTitle.zip");


                    ScrollView frameLayout = (ScrollView) findViewById(R.id.defaultLayout);


                    RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.loadingId);
                    TextView textView = (TextView) findViewById(R.id.loadingTextView);

                    String[] check = new File(rootFolder).list(fileNameFilterAPK);

                    int k = decreaseToLowest(check);
                    String apkVersion = "K-Klock v" + k + ".apk";

                    new apkBuilder(getApplication(), relativeLayout, textView, frameLayout).execute(apkVersion,apkVersion,apkVersion);


            }}


        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

       Spinner spinner = (Spinner) findViewById(kpchuck.k_klock.R.id.romSelectionSpinner);
        //Make array generation automatic
        getArrayForRoms();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roms);
        spinner.setAdapter(adapter);
        String romName = pref.getString("selectedRom", "");
        if(!romName.equals(getResources().getString(R.string.chooseRom))){
            spinner.setSelection(adapter.getPosition(romName));
        }
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = parent.getItemAtPosition(position).toString();
                meditor.putString("selectedRom", selectedItem);
                meditor.apply();
                if (getOos(selectedItem).equals("OxygenOS")){
                    indicatorSwitch.setVisibility(View.VISIBLE);
                }else indicatorSwitch.setVisibility(View.GONE);

            } // to close the onItemSelected
            public void onNothingSelected(android.widget.AdapterView<?> parent)
            {

            }
        });
    }



    public String getOos(String oos){
        if (oos.length() <8) return "thisIsNotOxygenOS";
        oos = oos.substring(0, 8);
        return oos;
    }

    public void iconPref(View v){
        Switch qsSwitch = (Switch) findViewById(R.id.colorIcons);
        setSwitchPrefs(qsSwitch, "iconPref");
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
                        editor.putBoolean("qsBgPref", false);
                        qsSwitch.setChecked(false);
                        editor.remove("qsBgFilePath");
                        new File(filePath).delete();
                    }
                    else{
                        editor.putString("qsBgFilePath", filePath);
                        editor.putBoolean("qsBgPref", true);
                    }
                    editor.apply();
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
                    editor.putBoolean("qsBgPref", false);
                    editor.remove("qsBgFilePath");
                    editor.apply();
                    qsSwitch.setChecked(false);
                }}
            );
            imagePicker.pickImage();

        }else{
            editor.putBoolean("qsBgPref", false);
            editor.remove("qsBgFilePath");
            editor.apply();
        }

    }

    public void qsPref(View v){
        Switch qsSwitch = (Switch) findViewById(R.id.noQsTilesTv);
        setSwitchPrefs(qsSwitch, "qsPref");
    }

    public void titlePref(View v){
        Switch mswitch = (Switch) findViewById(R.id.qsTitle);
        setSwitchPrefs(mswitch, "qsTitlePref");
    }

    public void hideStatusbarPref(View v){
        Switch mswitch = (Switch) findViewById(R.id.hideStatusbar);
        setSwitchPrefs(mswitch, "hideStatusbarPref");
    }

    public void moveLeftPref(View v){
        Switch qsSwitch = (Switch) findViewById(R.id.moveNetworkLeft);
        setSwitchPrefs(qsSwitch, "moveLeftPref");

    }

    public void recentsPref(View v){

        Switch qsSwitch = (Switch) findViewById(R.id.roundedRecents);
        setSwitchPrefs(qsSwitch, "recentsPref");
    }

    public void indicatorPref(View v){
        Switch qsSwitch = (Switch) findViewById(R.id.networkSignalIndicatorSwitch);
        setSwitchPrefs(qsSwitch, "indicatorPref");
        }

    public void setSwitchPrefs(Switch mswitch, String key){
         if(mswitch.isChecked()) {
            editor.putBoolean(key, true);
            editor.apply();
        }else{
            editor.putBoolean(key, false);
            editor.apply();
        }
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
                    editor.putBoolean("minitPref", true);
                    editor.apply();
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener(){

                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    editor.putBoolean("minitPref", false);
                    editor.apply();
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
        }else {
             editor.putBoolean("minitPref", false);
                    editor.apply();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_DEVICE) {
                imagePicker.submit(data);
            }
        }else{
            Switch qsSwitch = (Switch) findViewById(R.id.qsBg);
            editor.putBoolean("qsBgPref", false);
            editor.remove("qsBgFilePath");
            editor.apply();
            qsSwitch.setChecked(false);

        }
    }

    public boolean checkQsFile(){
        String path = myPref.getString("qsBgFilePath", "null");
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
               colorsTitles = loadArray(colorsTitles, "colorsTitles");
           }catch(Exception e){
               Log.d("klock", e.getMessage());
           }
            try {
                colorsValues = loadArray(colorsValues, "colorsValues");
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
                formatsTitles = loadArray(formatsTitles, "formatsTitles");
            }catch(Exception e){
                Log.d("klock", e.getMessage());
            }
            try {
                formatsValues = loadArray(formatsValues, "formatsValues");
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
            titles = loadArray(titles, titleArrayKey);
            values = loadArray(values, valueArrayKey);

            titles = fileHelper.deleteItemFromArray(title, titles);
            values = fileHelper.deleteItemFromArray(value, values);

            saveArray(titles, titleArrayKey);
            saveArray(values, valueArrayKey);
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
                       ArrayList<String> templist = loadArray(arrayList, arrayListNameKey);
                       templist.add(m_Text);
                       saveArray(templist, arrayListNameKey);

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
                    one = loadArray(colorsTitles, "colorsTitles");
                    two = loadArray(colorsValues, "colorsValues");
                }else{
                    one = loadArray(formatsTitles, "formatsTitles");
                    two = loadArray(formatsValues, "formatsValues");
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
                        tempArray = loadArray(tempArray, key);
                        if(tempArray.size() != 0) {
                            int k = tempArray.size() - 1;

                            tempArray.remove(k);
                            saveArray(tempArray, key);
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
                    ArrayList<String> namesArray = loadArray(nameArray, nameListArrayKey);
                    ArrayList<String> valuesArray = loadArray(arrayList, valueArrayKey);

                    String k = name.replace(' ','_') + ".xml";

                    fileHelper.deleteItemFromArray(k, namesArray);
                    fileHelper.deleteItemFromArray(previousValue, valuesArray);
                    //Add Value
                    valuesArray.add(m_Text);
                    saveArray(valuesArray, valueArrayKey);
                    //Add Name

                    namesArray.add(k);
                    ArrayList<String> templist = new ArrayList<>();
                    for(int f = 0; f<namesArray.size(); f++){
                        templist.add(namesArray.get(f));
                    }

                    saveArray(templist, nameListArrayKey);
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

    public void saveArray(ArrayList<String> arrayList, String arrayListKey)
    {
        //Removing old
       int key =  myPref.getInt(arrayListKey, 0);
        for(int i = 0; i<key; i++){
            editor.remove(arrayListKey + i);
        }
        editor.remove(arrayListKey);
        editor.commit();

        //Adding new
        editor.putInt(arrayListKey, arrayList.size());

        for(int m=0;m<arrayList.size();m++)
        {
            editor.remove(arrayListKey + m);
            editor.putString(arrayListKey + m, arrayList.get(m));
        }

        editor.apply();
    }

    public ArrayList<String> loadArray(ArrayList<String> arrayList, String arrayListKey)
    {
        arrayList.clear();
        int size = myPref.getInt(arrayListKey, 0);

        for(int i=0;i<size;i++)
        {
            arrayList.add(myPref.getString(arrayListKey + i, null));
        }
        return arrayList;

    }


    public void getArrayForRoms(){
        try {
            String[] temp = getAssets().list("romSpecific");
            Arrays.sort(temp);
            roms.add(getResources().getString(R.string.chooseRom));
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
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
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

        //noinspection SimplifiableIfStatement
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
        }else if (id == R.id.otherroms_help){
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
                colorsTitles = loadArray(colorsTitles, "colorsTitles");
            }catch (Exception e){
                Log.d("klock", e.getMessage());
            }
        }
        if(bleh.getBoolean("saveColors", true)){

            try{
                colorsValues = loadArray(colorsValues, "colorsValues");
            }catch (Exception e){
                Log.d("klock", e.getMessage());
            }
        }
        if(bleh.getBoolean("saveFormats", true)){
            try{
                formatsTitles = loadArray(formatsTitles, "formatsTitles");
            }catch (Exception e){
                Log.d("klock", e.getMessage());
            }
        }
        if(bleh.getBoolean("saveFormats", true)) {

            try{
                formatsValues = loadArray(formatsValues, "formatsValues");
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
