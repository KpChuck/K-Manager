package kpchuck.kklock.xml;

import android.content.Context;
import android.os.Environment;

import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.FontListParser;
import kpchuck.kklock.utils.PrefUtils;
import kpchuck.kklock.utils.QsBgUtil;

import static kpchuck.kklock.constants.PrefConstants.DEV_MAKE_DYNAMIC;
import static kpchuck.kklock.constants.PrefConstants.PREF_INCLUDE_NONE_OPT;
import static kpchuck.kklock.constants.XmlConstants.X_FILL_PARENT;
import static kpchuck.kklock.constants.XmlConstants.X_ID;
import static kpchuck.kklock.constants.XmlConstants.X_LAYOUT_WIDTH;

public class XmlWork {

    private File srcFolder = new File(Environment.getExternalStorageDirectory() + "/K-Klock/inputFiles");

    private Context context;
    private PrefUtils prefUtils;
    private XmlUtils utils;
    public static final String layout = "layout-v22";
    private String formatXmlPath;

    private States states;

    public XmlWork(Context context) throws Exception{

        this.prefUtils = new PrefUtils(context);
        this.utils = new XmlUtils();
        this.context=context;
        this.formatXmlPath = utils.baseFolders.getAbsolutePath() + "/%s/%s/%s.xml";
        this.states = new States(prefUtils);

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
        for (int i=0; i<fvalues.length; i++)
            fvalues[i] = utils.wrapInFont(fvalues[i]);
        utils.writeResources(new File(values, "clockformats.xml"), "string", fnames, fvalues);

        if (prefUtils.getBool(R.string.key_statusbar_clock_size)){
            utils.writeResource(new File(values, "clocksize.xml"), "dimen",
                    "status_bar_clock_size", prefUtils.getIntString(R.string.key_clock_size, 14) + "sp");
        }
        if (prefUtils.getBool(R.string.key_am_everywhere)){
            utils.writeResource(new File(values, "ampm.xml"), "bool", "config_showAmpm", "true");
        }
    }

    private void modStatusBar() throws Exception{
        boolean stockClockOnLockscreen = !states.isStockClock() && states.isClockOnLockscreen();

        String statusbar_name = "status_bar";
        if (new File(srcFolder, "status_bar_contents_container.xml").exists())
            statusbar_name = "status_bar_contents_container";

        StatusBar statusBar = new StatusBar(utils, prefUtils, new File(srcFolder,  statusbar_name +".xml"), context);
        statusBar.createWorkCopy(states.getClockPosition());
        if (states.shouldModifyClock()){
            statusBar.writeDocument(new File(String.format(formatXmlPath, "res", layout, statusbar_name)));
            return;
        }

        Element customClock;

        /*
        Left ->
            stockClockOnLockscreen ->    false
            !stockClockOnLockscreen ->   true
            stock ->                false
         Right ->
            stockClockOnLockscreen ->    false    Needs SysIconArea Inserted at Root
            !stockClockOnLockscreen ->   false ;
            stock ->                false
         Center ->
            stockClockOnLockscreen ->    oos      If oos Needs SysIconArea inserted at center
            !stockClockOnLockscreen ->   true
            stock ->                oos
         Stock removes clock


         */
        if (!stockClockOnLockscreen){
            statusBar.removeClock();
        }

        if (states.isLeftClock()){
            customClock = statusBar.createClock(states.isStockClock(), !stockClockOnLockscreen, !stockClockOnLockscreen);
            statusBar.insertLeft(customClock);
        }
        else if (states.isRightClock()){
            customClock = statusBar.createClock(states.isStockClock(), false, !stockClockOnLockscreen);
            if (stockClockOnLockscreen){
                Element sysiconarea = statusBar.createSystemAreaElement();
                sysiconarea.setAttribute(X_LAYOUT_WIDTH, X_FILL_PARENT);
                statusBar.insertAtRoot(sysiconarea);
            }
            statusBar.insertRight(customClock);
        }
        else {
            customClock = statusBar.createClock(states.isStockClock(), !stockClockOnLockscreen ? states.isOos() : true, !stockClockOnLockscreen);
            if (states.isOos() && stockClockOnLockscreen){
                Element temp = statusBar.createSystemAreaElement();
                statusBar.insertCenter(temp);
            }
            statusBar.insertCenter(customClock);
        }
        statusBar.writeDocument(new File(String.format(formatXmlPath, "res", layout, statusbar_name)));

    }

    private void modKeyguardStatusBar() throws Exception{

        KeyguardStatusBar keyguardStatusBar = new KeyguardStatusBar(utils, prefUtils, new File(srcFolder, "keyguard_status_bar.xml"), context);

        boolean modClock = false;

        /*
        Only need to hide stock lockscreen icons if
        ->  Moving statusbar icons
        ->  Using oos - and custom clock or center clock
        ->  Clock not on lockscreen - and center or left clock
        ->  Clock on lockscreen and right clock

        Need stock clock center excluded
         */
        if (states.movingStatusbarIcons()
                || (states.isOos() && states.hasCustomOrCenterClock())
                || (!states.isClockOnLockscreen() && states.isCustomClock() && states.isCenterOrLeftClock())
                || states.isClockOnLockscreen() && states.isRightClock())
            modClock = true;


        if (modClock)
            keyguardStatusBar.hideStatusIcons();

        keyguardStatusBar.writeDocument(new File(String.format(formatXmlPath, "res", layout, "keyguard_status_bar")));
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

        if (prefUtils.getBool(R.string.key_statusbar_height_enable)){
            File androidOverlay = new File(utils.romzip, "/assets/overlays/android/res/values");
            androidOverlay.mkdirs();
            utils.writeResource(new File(androidOverlay, "status_bar_height.xml"), "dimen", "status_bar_height_portrait",
                    prefUtils.getIntString(R.string.key_statusbar_height, 24) + "dp");
        }
    }

    private void addQsValues(File values) throws Exception{
        if (prefUtils.getBool("qsPref")){ // Num qs tiles
            for (File f: new File[]{values, new File(values.getAbsolutePath() + "-w550dp-land")}){
                if (!f.exists()) f.mkdirs();
                utils.writeResource(new File(f, "qs_tile_num.xml"), "integer", "quick_settings_num_columns",
                        prefUtils.getIntString(R.string.key_num_qs_tiles, 4));
            }
        }
        if (prefUtils.getBool("qsTitlePref")) { // Qs title gone
            utils.writeResource(new File(values, "qstitlegone.xml"), "dimen", "qs_tile_text_size", "0.0sp");
        }
        if (prefUtils.getBool("prefHeadsUpTimeout")){
            utils.writeResource(new File(values, "headsuptimeout.xml"), "integer", "heads_up_notification_decay",
                    prefUtils.getIntString(R.string.key_heads_up_time, 5) + "000");
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
            utils.writeResources(new File(values, "hidelockclock3.xml"), "string",
                    new String[]{"clock_12hr_format", "clock_24hr_format", "system_ui_aod_date_pattern"},
                    new String[]{" "," "," "});
        }
        int lockscreenHeight = prefUtils.getInt("hideStatusBarPref");
        if (lockscreenHeight == 1){
            utils.writeResource(new File(values, "lockscreen_height.xml"), "dimen", "status_bar_header_height_keyguard",
                    "@*android:dimen/status_bar_height");
        } else if (lockscreenHeight == 2){
            utils.writeResource(new File(values, "lockscreen_height.xml"), "dimen", "status_bar_header_height_keyguard",
                    "0.0dip");
        }
        if (prefUtils.getBool(R.string.key_recents)){
            String radius = prefUtils.getIntString(R.string.key_recents_size, 6) + "dip";
            utils.writeResources(new File(values, "rounded_recents.xml"), "dimen",
                    new String[]{"recents_task_view_rounded_corners_radius", "recents_task_view_shadow_rounded_corners_radius"},
                    new String[]{radius, radius});
        }
    }

}
