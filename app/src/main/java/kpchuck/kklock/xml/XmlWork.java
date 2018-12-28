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

import static kpchuck.kklock.constants.PrefConstants.DEV_MAKE_DYNAMIC;
import static kpchuck.kklock.constants.PrefConstants.PREF_INCLUDE_NONE_OPT;
import static kpchuck.kklock.constants.PrefConstants.PREF_MOVE_LEFT;
import static kpchuck.kklock.constants.XmlConstants.X_FILL_PARENT;
import static kpchuck.kklock.constants.XmlConstants.X_ID;
import static kpchuck.kklock.constants.XmlConstants.X_WRAP_CONTENT;

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
        translate();
        utils.moveAttrsIfPresent(srcFolder.getAbsolutePath());
        modStatusBar();
        modKeyguardStatusBar();
        modSystemIcons();

    }

    private void writeStatusBar(StatusBar statusBar, int clockStyle) throws Exception{
        statusBar.writeDocument(new File(String.format(formatXmlPath, utils.getType2(context, clockStyle), layout, "status_bar")));
    }

    private void modStatusBar() throws Exception{
        StatusBar statusBar = new StatusBar(utils, prefUtils, new File(srcFolder, "status_bar.xml"), context);
        Element customClock;
        Element hideE;

        // Now Left Clock
        // Left on lockscreen
        statusBar.createWorkCopy();
        customClock = statusBar.createClock(false, "left|center", X_WRAP_CONTENT);
        statusBar.insertLeft(customClock);
        writeStatusBar(statusBar, R.string.left_clock);
        // Left not on lockscreen
        statusBar.createWorkCopy();
        hideE = statusBar.createHideyLayout(X_WRAP_CONTENT, "left");
        customClock = statusBar.createClock(false, "left|center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        statusBar.insertLeft(hideE);

        writeStatusBar(statusBar, R.string.left_no_clock);
        //Dynamic Clock
        if (prefUtils.getBool(DEV_MAKE_DYNAMIC)){
            utils.changeAttribute(customClock, X_ID, "@*com.android.systemui:id/clock");
            writeStatusBar(statusBar, R.string.left_dynamic);
        }
        //Stock-Like
        hideE.removeChild(customClock);
        customClock = statusBar.createClock(true, "left|center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        writeStatusBar(statusBar, R.string.left_stock);

        //Insert Right Clocks First
        // Right on lockscreen
        customClock = statusBar.createClock(false, "start|center", X_WRAP_CONTENT);
        statusBar.insertRight(customClock);
        writeStatusBar(statusBar, R.string.right_clock);

        // Right not on lockscreen
        statusBar.createWorkCopy();
        customClock = statusBar.createClock(false, "start|center", X_WRAP_CONTENT);
        hideE = statusBar.createHideyLayout(X_FILL_PARENT, "center");
        hideE.appendChild(customClock);
        statusBar.insertRight(hideE);
        writeStatusBar(statusBar, R.string.right_no_clock);

        // Dynamic
        if (prefUtils.getBool(DEV_MAKE_DYNAMIC)) {
            utils.changeAttribute(customClock, X_ID, "@*com.android.systemui:id/clock");
            writeStatusBar(statusBar, R.string.right_dynamic);
        }

        // Stock-Like
        hideE.removeChild(customClock);
        customClock = statusBar.createClock(true, "start|center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        writeStatusBar(statusBar, R.string.right_stock);


        // Center Clocks
        // On Lockscreen
        statusBar.createWorkCopy();
        customClock = statusBar.createClock(false, "center", X_WRAP_CONTENT);
        statusBar.insertCenter(customClock);
        statusBar.writeDocument(new File(String.format(formatXmlPath, "res", layout, "status_bar")));

        // Not on Lockscreen
        statusBar.createWorkCopy();
        customClock = statusBar.createClock(false, "center", X_WRAP_CONTENT);
        hideE = statusBar.createHideyLayout(X_WRAP_CONTENT, "center");
        hideE.appendChild(customClock);
        statusBar.insertCenter(hideE);
        writeStatusBar(statusBar, R.string.center_no_clock);

        //Dynamic
        if (prefUtils.getBool(DEV_MAKE_DYNAMIC)){
            customClock = utils.changeAttribute(customClock, X_ID, "@*com.android.systemui:id/clock");
            writeStatusBar(statusBar, R.string.center_dynamic);
        }
        //STock-Like
        hideE.removeChild(customClock);
        customClock = statusBar.createClock(true, "center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        writeStatusBar(statusBar, R.string.center_stock);

        // No Clock
        if (prefUtils.getBool(PREF_INCLUDE_NONE_OPT)) {
            statusBar.createWorkCopy();
            writeStatusBar(statusBar, R.string.no_clock);
        }

        utils.writeType2Desc(context.getString(R.string.sysui_type2_center),
                utils.baseFolders.getAbsolutePath() + "/type2");
    }

    private void modKeyguardStatusBar() throws Exception{

        KeyguardStatusBar keyguardStatusBar = new KeyguardStatusBar(utils, prefUtils, new File(srcFolder, "keyguard_status_bar.xml"), context);

        String[] unmodPlaces = {utils.getType2(context, R.string.right_no_clock), utils.getType2(context, R.string.right_dynamic),
                utils.getType2(context, R.string.right_stock), context.getString(R.string.right_clock),
                utils.getType2(context, R.string.left_clock), utils.getType2(context, R.string.center_clock), "res"};

        // Write unmodified keyguard
        if (prefUtils.getInt(PREF_MOVE_LEFT) == 2) {
            keyguardStatusBar.writeDocuments(Arrays.asList(unmodPlaces));
        }

        // Write modified keyguard
        keyguardStatusBar.hideStatusIcons();

        List<String> modPlaces = new ArrayList<>(Arrays.asList(utils.getType2(context, R.string.center_no_clock), utils.getType2(context, R.string.center_dynamic),
                utils.getType2(context, R.string.center_stock), utils.getType2(context, R.string.left_no_clock),
                utils.getType2(context, R.string.left_stock), utils.getType2(context, R.string.left_dynamic)));

        if (prefUtils.getInt(PREF_MOVE_LEFT) != 2)
            modPlaces.addAll(Arrays.asList(unmodPlaces));

        keyguardStatusBar.writeDocuments(modPlaces);
    }

    private void modSystemIcons() throws Exception{
        SystemIcons systemIcons = new SystemIcons(utils, prefUtils, new File(srcFolder, "/system_icons.xml"), context);
        systemIcons.writeDocument(new File(String.format(formatXmlPath, "res", layout, "system_icons")));
    }


    private void makeFolders() {

        File s = new File(utils.romzip, "/assets/overlays/com.android.systemui");
        s.mkdirs();

        List<String> startFolder = new ArrayList<>(Arrays.asList(
                utils.getType2(context, R.string.right_no_clock),
                utils.getType2(context, R.string.right_stock),
                utils.getType2(context, R.string.right_clock),
                utils.getType2(context, R.string.left_clock),
                "res",
                utils.getType2(context, R.string.center_no_clock),
                utils.getType2(context, R.string.center_stock),
                utils.getType2(context, R.string.left_no_clock),
                utils.getType2(context, R.string.left_stock)));

        if (prefUtils.getBool(DEV_MAKE_DYNAMIC)) {
            startFolder.add(utils.getType2(context, R.string.right_dynamic));
            startFolder.add(utils.getType2(context, R.string.left_dynamic));
            startFolder.add(utils.getType2(context, R.string.center_dynamic));
        }
        if (prefUtils.getBool(PREF_INCLUDE_NONE_OPT))
            startFolder.add(utils.getType2(context, R.string.no_clock));

        startFolder.forEach(k -> new File(String.format("%s/%s/%s", s, k, layout)).mkdirs());
    }

    private void translate(){
        ArrayList<String> filenames = utils.substratize(utils.getEngArray(context, R.array.included_colors_title), "type1a", ".xml");
        filenames.addAll(utils.substratize(utils.getEngArray(context, R.array.included_formats_title), "type1b", ".xml"));
        filenames.addAll(utils.substratize(utils.getEngArray(context, R.array.font_names), "type1c", ".xml"));
        ArrayList<String> translated_filenames = utils.substratize(utils.getArray(context, R.array.included_colors_title), "type1a", ".xml");
        translated_filenames.addAll(utils.substratize(utils.getArray(context, R.array.included_formats_title), "type1b", ".xml"));
        translated_filenames.addAll(utils.substratize(utils.getArray(context, R.array.font_names), "type1c", ".xml"));

        utils.translate(context, utils.baseFolders, filenames, translated_filenames, R.string.sysui_type1a, R.string.sysui_type1b, R.string.sysui_type1c, 0);
    }


}
