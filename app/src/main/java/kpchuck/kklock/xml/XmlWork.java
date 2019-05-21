package kpchuck.kklock.xml;

import android.content.Context;
import android.os.Environment;

import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.PrefUtils;
import kpchuck.kklock.utils.QsBgUtil;

import static kpchuck.kklock.constants.PrefConstants.DEV_MAKE_DYNAMIC;
import static kpchuck.kklock.constants.PrefConstants.PREF_INCLUDE_NONE_OPT;
import static kpchuck.kklock.constants.XmlConstants.X_FILL_PARENT;
import static kpchuck.kklock.constants.XmlConstants.X_ID;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_WIDTH;

public class XmlWork {

    private File srcFolder = new File(Environment.getExternalStorageDirectory() + "/K-Klock/userInput");

    private Context context;
    private PrefUtils prefUtils;
    private XmlUtils utils;
    public static final String layout = "layout-v22";
    private String formatXmlPath;

    public XmlWork(Context context) throws Exception{

        this.prefUtils = new PrefUtils(context);
        this.utils = new XmlUtils();
        this.context=context;
        this.formatXmlPath = utils.baseFolders.getAbsolutePath() + "/%s/%s/%s.xml";

        // Start Modding
        makeFolders();
        utils.moveAttrsIfPresent(srcFolder.getAbsolutePath());
        insertCustomXmls();
        modStatusBar();
        modKeyguardStatusBar();
        modSystemIcons();
        new QsBgUtil(context, utils);
    }

    private void addStatusBarValues(File values) throws Exception{
        String clockColor = "#" + Integer.toHexString(prefUtils.getInt(R.string.key_clock_color));
        utils.writeResource(new File(values, "clockcolor.xml"), "color", "status_bar_clock_color", clockColor);

        String clockFormat = prefUtils.getString(R.string.key_clock_format, "hh:mm");
        String[] fnames = new String[]{"keyguard_widget_12_hours_format", "keyguard_widget_24_hours_format"};
        String[] fvalues = new String[]{clockFormat, clockFormat};
        if (!prefUtils.getBool(R.string.key_use_included_format))
            fvalues[0] = clockFormat + " a";
        utils.writeResources(new File(values, "clockformats.xml"), "string", fnames, fvalues);

        String font = prefUtils.getString(R.string.key_clock_font, "roboto-regular").toLowerCase();
        utils.writeResources(new File(values, "clockfonts.xml"), "style",
                new String[]{"android:textSize", "android:textColor", "android:fontFamily"},
                new String[]{"@*com.android.systemui:dimen/status_bar_clock_size", "#ffffffff", font},
                "TextAppearance.StatusBar.Clock", "@*android:style/TextAppearance.StatusBar.Icon");

        if (prefUtils.getBool(R.string.key_statusbar_clock_size)){
            utils.writeResource(new File(values, "clocksize.xml"), "dimen",
                    "status_bar_clock_size", prefUtils.getString(R.string.key_clock_size, "") + "sp");
        }
        if (prefUtils.getBool(R.string.key_am_everywhere)){
            utils.writeResource(new File(values, "ampm.xml"), "bool", "config_showAmpm", "true");
        }
    }

    private void modStatusBar() throws Exception{
        int clockPosition = prefUtils.getInt(R.string.key_clock_position);
        boolean stockClock = prefUtils.getBool(R.string.key_stock_style);
        boolean clockOnLockscreen = stockClock ? false : prefUtils.getBool(R.string.key_sb_clock_on_lockscreen);
        boolean oos = prefUtils.getBool(R.string.key_oos_is_bad);
        boolean stockClockNotOnLockscreen = stockClock && !prefUtils.getBool(R.string.key_sb_clock_on_lockscreen);

        StatusBar statusBar = new StatusBar(utils, prefUtils, new File(srcFolder, "status_bar.xml"), context);
        statusBar.createWorkCopy(clockPosition);
        if (clockPosition == XmlUtils.NONE){
            statusBar.writeDocument(new File(String.format(formatXmlPath, "res", layout, "status_bar")));
            return;
        }

        Element customClock;

        /*
        Left ->
            clockOnLockscreen ->    false
            !clockOnLockscreen ->   true
            stock ->                false
         Right ->
            clockOnLockscreen ->    false    Needs SysIconArea Inserted at Root
            !clockOnLockscreen ->   false ;
            stock ->                false
         Center ->
            clockOnLockscreen ->    oos      If oos Needs SysIconArea inserted at center
            !clockOnLockscreen ->   true
            stock ->                oos
         Stock removes clock


         */
        if (stockClockNotOnLockscreen){
            statusBar.removeClock();
        }

        if (clockPosition == XmlUtils.LEFT){
            customClock = statusBar.createClock(stockClock, !clockOnLockscreen, stockClockNotOnLockscreen);
            statusBar.insertLeft(customClock);
        }
        else if (clockPosition == XmlUtils.RIGHT){
            customClock = statusBar.createClock(stockClock, false, stockClockNotOnLockscreen);
            if (clockOnLockscreen){
                Element sysiconarea = statusBar.createSystemAreaElement();
                sysiconarea.setAttribute(X_LAYOUT_WIDTH, X_FILL_PARENT);
                statusBar.insertAtRoot(sysiconarea);
            }
            statusBar.insertRight(customClock);
        }
        else {
            customClock = statusBar.createClock(stockClock, !clockOnLockscreen ? oos : true, stockClockNotOnLockscreen);
            if (oos && clockOnLockscreen){
                Element temp = statusBar.createSystemAreaElement();
                statusBar.insertCenter(temp);
            }
            statusBar.insertCenter(customClock);
        }
        statusBar.writeDocument(new File(String.format(formatXmlPath, "res", layout, "status_bar")));

    }

    private void modKeyguardStatusBar() throws Exception{

        KeyguardStatusBar keyguardStatusBar = new KeyguardStatusBar(utils, prefUtils, new File(srcFolder, "keyguard_status_bar.xml"), context);
        int clockPosition = prefUtils.getInt(R.string.key_clock_position);
        boolean stockClock = prefUtils.getBool(R.string.key_stock_style);
        boolean clockOnLockscreen = prefUtils.getBool(R.string.key_sb_clock_on_lockscreen);

        boolean modClock = false;

        /*
        Only need to hide stock lockscreen icons if
        ->  Moving statusbar icons to not right - there'll be duplicateds
        ->  Using oos - it just complicates adding any clock that much
        ->  Read the rest of conditions
         */
        if (prefUtils.getInt(R.string.key_move_network) != XmlUtils.RIGHT
                || (prefUtils.getBool(R.string.key_oos_is_bad) && (!stockClock || clockPosition == XmlUtils.CENTER))
                || (!clockOnLockscreen && (clockPosition == XmlUtils.CENTER) || (clockPosition == XmlUtils.LEFT))
                || clockOnLockscreen && clockPosition == XmlUtils.RIGHT)
            modClock = true;

        if (modClock)
            keyguardStatusBar.hideStatusIcons();

        keyguardStatusBar.writeDocument(new File(String.format(formatXmlPath, "res", layout, "keyguard_status_bar.xml")));
    }

    private void modSystemIcons() throws Exception{
        SystemIcons systemIcons = new SystemIcons(utils, prefUtils, new File(srcFolder, "/system_icons.xml"), context);
        systemIcons.writeDocument(new File(String.format(formatXmlPath, "res", layout, "system_icons")));
    }

    private void makeFolders() {

        File s = new File(utils.romzip, "/assets/overlays/com.android.systemui/res");
        s.mkdirs();

        new File(s, "values").mkdir();
        new File(s, layout).mkdir();
    }

    private void insertCustomXmls() throws Exception{
        File values = new File(utils.baseFolders.getAbsolutePath() + "/res/values/");

        addStatusBarValues(values);
        addQsValues(values);

        if (prefUtils.getBool(R.string.key_theme_sb_icons)){
            String darkColor = "#" + Integer.toHexString(prefUtils.getInt(R.string.key_stock_dark_color));
            String clockRef = "@*com.android.systemui:color/status_bar_clock_color";
            XmlUtils utils = new XmlUtils();
            utils.writeResources(new File(values, "sbiconcolors.xml"), "color",
                    new String[]{"dark_mode_icon_color_single_tone", "dark_mode_icon_color_dual_tone_fill",
                    "light_mode_icon_color_single_tone", "light_mode_icon_color_dual_tone_fill"},
                    new String[]{darkColor, darkColor, clockRef, clockRef});
        }

    }

    private void addQsValues(File values) throws Exception{
        if (prefUtils.getBool("qsPref")){ // Num qs tiles
            for (File f: new File[]{values, new File(values.getAbsolutePath() + "-w550dp-land")}){
                if (!f.exists()) f.mkdirs();
                utils.writeResource(new File(f, "qs_tile_num.xml"), "integer", "quick_settings_num_columns",
                        prefUtils.getString(R.string.key_num_qs_tiles, "4"));
            }
        }
        if (prefUtils.getBool("qsTitlePref")) { // Qs title gone
            utils.writeResource(new File(values, "qstitlegone.xml"), "dimen", "qs_tile_text_size", "0.0sp");
        }
        if (prefUtils.getBool("prefHeadsUpTimeout")){
            utils.writeResource(new File(values, "headsuptimeout.xml"), "integer", "heads_up_notification_decay",
                    prefUtils.getString(R.string.key_heads_up_time, "5") + "000");
        }
        if (prefUtils.getBool("prefhidelockscreenclock")){
            utils.writeResources(new File(values, "hidelockclock1.xml"), "style",
                    new String[]{"android:textSize", "android:alpha"},
                    new String[]{"@*com.android.systemui:dimen/widget_big_font_size", "0.0"},
                    "widget_big_thin");

            utils.writeResources(new File(values, "hidelockclock2.xml"), "style",
                    new String[]{"android:textSize", "android:alpha"},
                    new String[]{"@*com.android.systemui:dimen/widget_label_font_size", "0.0"},
                    "widget_label");
        }
        int lockscreenHeight = prefUtils.getInt("hideStatusBarPref");
        if (lockscreenHeight == 1){
            utils.writeResource(new File(values, "lockscreen_height.xml"), "dimen", "status_bar_header_height_keyguard",
                    "@*android:dimen/status_bar_height");
        } else if (lockscreenHeight == 2){
            utils.writeResource(new File(values, "lockscreen_height.xml"), "dimen", "status_bar_header_height_keyguard",
                    "0.0dip");
        }
    }

}
