package kpchuck.k_klock;


import org.apache.commons.io.FileUtils;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

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
    String prefFile = "prefFileName";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        final SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(this);

        Preference deletePref = (Preference) findPreference("deleteSaved");
        deletePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences haPref = getSharedPreferences(prefFile, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = haPref.edit();
                deleteArrayFromPref("colorsTitles");
                deleteArrayFromPref("colorsValues");
                deleteArrayFromPref("formatsTitles");
                deleteArrayFromPref("formatsValues");
                editor.remove("colorsTitles");
                editor.remove("colorsValues");
                editor.remove("formatsValues");
                editor.remove("formatsValues");
                editor.apply();
                Toast.makeText(getApplicationContext(), getString(R.string.cleared_saved), Toast.LENGTH_SHORT).show();

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

    public void shortToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void deleteArrayFromPref(String arrayListKey)
    {
        SharedPreferences myPref = getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPref.edit();
        int key =  myPref.getInt(arrayListKey, 0);
        for(int i = 0; i<key; i++){
            editor.remove(arrayListKey + i);
        }

        editor.apply();
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
