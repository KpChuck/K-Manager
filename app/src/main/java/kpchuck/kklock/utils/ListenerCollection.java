package kpchuck.kklock.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.v4.app.FragmentActivity;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import kpchuck.kklock.R;
import kpchuck.kklock.activities.CustomCrashActivity;
import kpchuck.kklock.dialogs.TextAlertDialogFragment;
import kpchuck.kklock.interfaces.DialogClickListener;
import kpchuck.kklock.services.CheckforUpdatesService;

import static kpchuck.kklock.constants.PrefConstants.PREF_BLACK_THEME;

public class ListenerCollection {

    public void doNothing(){
    }

    private void showAdbSteps(Context context){
        TextAlertDialogFragment dialogFragment = new TextAlertDialogFragment();
        FileHelper fileHelper = new FileHelper();
        DialogClickListener dialogClickListener = new DialogClickListener() {
            @Override
            public void onPositiveBtnClick() {
                fileHelper.copyToClipBoard(context,
                        "adb shell pm grant kpchuck.k_klock.pro android.permission.WRITE_SECURE_SETTINGS");
            }

            @Override
            public void onCancelBtnClick() {

            }
        };
        dialogFragment.Instantiate(context.getString(R.string.adb_required),
                context.getString(R.string.adb_how_to_run) +
                        "adb shell pm grant kpchuck.k_klock.pro android.permission.WRITE_SECURE_SETTINGS",
                context.getString(R.string.copy_to_clipboard),
                context.getString(R.string.cancel),
                dialogClickListener);
        dialogFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "klock");
    }

    public void restart_app(Context context, SwitchPreference preference){
        Intent i = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }

    public void check_updates(Context context, Preference preference){
        Intent i = new Intent(context, CheckforUpdatesService.class);
        i.putExtra("action", 1);
        context.startService(i);
    }

    public void about_libraries(Context context, Preference preference){
        StringBuilder tr = new StringBuilder();
        String [] ta = context.getResources().getStringArray(R.array.translators);
        if (ta.length != 0) {
            tr.append(context.getString(R.string.translations_thanks));
            tr.append("<br>");
            for (String s: ta) {
                tr.append(s);
                tr.append("<br>");
            }
        }
        new LibsBuilder()
                //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                .withActivityStyle(new PrefUtils(context).getBool(PREF_BLACK_THEME) ? Libs.ActivityStyle.DARK : Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withAutoDetect(true)
                .withFields(R.string.class.getFields())
                .withAboutDescription(tr.toString())
                //start the activity
                .withActivityTitle(context.getString(R.string.app_name))
                .start(context);
    }

    public void go_pro(Context context, Preference preference){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.k_manager_pro_link)));
        context.startActivity(intent);
    }

    public void report_bug(Context context, Preference preference){
        Intent rgb = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.github_issues)));
        context.startActivity(rgb);
    }

}
