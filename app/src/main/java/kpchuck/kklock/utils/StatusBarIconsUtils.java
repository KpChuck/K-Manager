package kpchuck.kklock.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import java.util.List;

import static kpchuck.kklock.constants.PrefConstants.*;

/**
 * Created by karol on 11/01/18.
 */

public class StatusBarIconsUtils {

    String key = "icon_blacklist";
    SuUtils suUtils;

    public StatusBarIconsUtils(){
        suUtils = new SuUtils();

    }

    public boolean setPerms(){
        if (suUtils.hasRoot()) {
            suUtils.runSuCommand("pm grant kpchuck.k_klock.pro android.permission.WRITE_SECURE_SETTINGS");
        }else {
            return false;
        }

        return true;
    }

    public boolean hasPerms(Context context){
        String permission = "android.permission.WRITE_SECURE_SETTINGS";
        return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;


    }

    public void hideIcons(Context context){
        PrefUtils prefUtils = new PrefUtils(context);

        // All values taken from SystemUI tuner. Hide them all
        String value = "airplane,bluetooth,cdma_eri,data_connection,data_saver,dmb,ethernet" +
                ",hotspot,phone_evdo_signal,phone_signal," +
                "volte,ims_indicator,vowifi,vpn,wifi_calling" +
                ",headset,remote_call,speakerphone,tty,volume," +
                "alarm,alarm_clock,clock,do_not_disturb,zen," +
                "answering_memo,cast,felica_lock,ime," +
                "location,managed_profile,nfc,nfc_on,nfclock," +
                "otg_keyboard,oth_mouse,power_saver,rotate," +
                "secure,su,sync_active,sync_failing";
        if (!prefUtils.getBool(PREF_HIDE_ICONS_NOT_FULLY)){
            value += ",mobile,wifi,battery";
        }
        String before = "";
        try {
            before = Settings.Secure.getString(context.getContentResolver(), key);
            Settings.Secure.putString(context.getContentResolver(), key, value);
        } catch (Exception e) {

            String baseCommand = "settings put secure " + key + " " + value;
            List<String> result = suUtils.runSuCommand("settings get secure " + key);
            before =result.size() < 1 ? "" : result.get(0);
            if (suUtils.hasRoot()) {
                suUtils.runSuCommand(baseCommand);
            }
        }
        Log.d("klock", "Backup of icon_blacklist is " + before);
        if (prefUtils.getBool(ICON_BLACKLIST_SCREEN_UNLOCKED)) {
            prefUtils.putString(ICON_BLACKLIST, before);
        }
        prefUtils.putBool(ICON_BLACKLIST_SCREEN_UNLOCKED, false);
    }

    public void restoreIcons(Context context){
        String value = new PrefUtils(context).getString(ICON_BLACKLIST, "");
        new PrefUtils(context).putBool(ICON_BLACKLIST_SCREEN_UNLOCKED, true);
        try {
            Settings.Secure.putString(context.getContentResolver(), key, value);
        } catch (Exception e) {

            String baseCommand = "settings put secure " + key + " " + value;
            if (suUtils.hasRoot()) {
                suUtils.runSuCommand(baseCommand);
            }
        }
    }
}
