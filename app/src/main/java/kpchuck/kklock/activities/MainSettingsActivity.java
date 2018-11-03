package kpchuck.kklock.activities;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.annotation.ColorInt;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import kpchuck.kklock.MainActivity;
import kpchuck.kklock.R;
import kpchuck.kklock.AppCompatPreferenceActivity;
import kpchuck.kklock.utils.PrefUtils;

import java.util.List;

import static kpchuck.kklock.constants.PrefConstants.PREF_BLACK_THEME;

public class MainSettingsActivity extends AppCompatPreferenceActivity {

    private PrefUtils prefUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        prefUtils = new PrefUtils(getApplicationContext());
        setTheme(prefUtils.getBool(PREF_BLACK_THEME) ? R.style.AppTheme_Dark : R.style.AppTheme);

        super.onCreate(savedInstanceState);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = this.getTheme();
        theme.resolveAttribute(R.attr.secondaryBackgroundColor, typedValue, true);
        @ColorInt int color = typedValue.data;
        getListView().setBackgroundColor(color);

        addPreferencesFromResource(R.xml.main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    }

    @Override
    public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen, final Preference preference) {
        // https://code.google.com/p/android/issues/detail?id=4611 comment #35
        if (preference instanceof PreferenceScreen && ((PreferenceScreen) preference).getDialog() != null)
            ((PreferenceScreen) preference).getDialog().getWindow().getDecorView().setBackgroundColor(Color.parseColor("#ffffff"));

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }



    public void shortToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }



}
