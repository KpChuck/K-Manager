package kpchuck.k_klock.activities;

import org.apache.commons.io.FileUtils;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.annotation.ColorInt;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import kpchuck.k_klock.AppCompatPreferenceActivity;
import kpchuck.k_klock.MainActivity;
import kpchuck.k_klock.R;
import kpchuck.k_klock.utils.PrefUtils;

import static kpchuck.k_klock.constants.PrefConstants.*;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    PrefUtils prefUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        prefUtils = new PrefUtils(getApplicationContext());
        setTheme(prefUtils.getBool(PREF_BLACK_THEME) ? R.style.AppTheme_Dark : R.style.AppTheme);

        setupActionBar();

        getListView().setBackgroundColor(Color.TRANSPARENT);

        getListView().setCacheColorHint(Color.TRANSPARENT);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = this.getTheme();
        theme.resolveAttribute(R.attr.secondaryBackgroundColor, typedValue, true);
        @ColorInt int color = typedValue.data;
        getListView().setBackgroundColor(color);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        final SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(this);

        final SwitchPreference changeTheme = (SwitchPreference) findPreference(PREF_BLACK_THEME);

        changeTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                prefUtils.putBool(PREF_BLACK_THEME, !changeTheme.isChecked());
                finish();
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            }
        });

        Preference deletePref = (Preference) findPreference("deleteSaved");
        deletePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                String[] list = {"colorsTitles", "colorsValues", "formatsTitles", "formatsValues"};
                prefUtils.deleteArrayLists(list);
                shortToast(getString(R.string.cleared_saved));
                return true;
            }
        });

        final SwitchPreference enableDevOptions = (SwitchPreference) findPreference(DEV_ENABLED);
        enableDevOptions.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {

                SharedPreferences.Editor editor = myPref.edit();
                editor.remove(DEV_HIDE_CLOCK);
                editor.remove(DEV_MAKE_DYNAMIC);
                editor.apply();
                SwitchPreference devDynamic = (SwitchPreference) findPreference(DEV_MAKE_DYNAMIC);
                SwitchPreference devClock = (SwitchPreference) findPreference(DEV_HIDE_CLOCK);

                if (enableDevOptions.isChecked()) {

                    devDynamic.setChecked(false);
                    devClock.setChecked(false);
                }

                return true;
            }
        });

        Preference deleteApks = (Preference) findPreference("deleteApks");
        deleteApks.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference) {
                File folder = new File(Environment.getExternalStorageDirectory() + "/K-Klock");
                if(folder.exists()){
                    String[] apks = folder.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String s) {
                            if(s.lastIndexOf('.')>0) {
                                // get last index for '.' char
                                int lastIndex = s.lastIndexOf('.');
                                // get extension
                                String str = s.substring(lastIndex);
                                // match path name extension
                                if(str.equals(".apk")) {
                                    return true;
                                }
                            }return false;}
                    });
                    for(String s: apks){
                        File apk = new File(folder, s);
                        try{
                            FileUtils.forceDelete(apk);
                        }catch (IOException e){
                            Log.e("klock", "Error deleting apks: " + e.getMessage());
                        }
                    }
                    Toast.makeText(getApplicationContext(), "All Apk's Cleared", Toast.LENGTH_SHORT).show();

                }
                return true;
            }
        });

    }


    private void setupActionBar() {
        ViewGroup rootView = (ViewGroup)findViewById(R.id.action_bar_root); //id from appcompat

        if (rootView != null) {
            View view = getLayoutInflater().inflate(R.layout.app_bar_layout, rootView, false);
            rootView.addView(view, 0);

            Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void shortToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
