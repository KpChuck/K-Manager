package kpchuck.kklock.xml;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.FileHelper;
import kpchuck.kklock.utils.PrefUtils;

import static kpchuck.kklock.constants.PrefConstants.*;
import static kpchuck.kklock.constants.XmlConstants.*;

public class XmlWork {

    private File srcFolder = new File(Environment.getExternalStorageDirectory() + "/K-Klock/userInput");

    private Context context;
    private PrefUtils prefUtils;
    private XmlUtils utils;

    public XmlWork(Context context) throws Exception{

        this.prefUtils = new PrefUtils(context);
        this.utils = new XmlUtils();
        this.context=context;

        // Start Modding
        makeFolders();
        translate();
        utils.moveAttrsIfPresent(srcFolder.getAbsolutePath());
        modStatusBar();
        modKeyguardStatusBar();
        modSystemIcons();

    }

    private void modStatusBar() throws Exception{
        StatusBar statusBar = new StatusBar(utils, prefUtils, new File(srcFolder, "status_bar.xml"));
        Element customClock;
        Element hideE;

        // Now Left Clock
        // Left on lockscreen
        statusBar.createWorkCopy();
        customClock = statusBar.createClock(false, "left|center", X_WRAP_CONTENT);
        statusBar.insertLeft(customClock);
        statusBar.writeDocument(new File(utils.baseFolders, utils.getType2(context, R.string.left_clock)+"/layout/status_bar.xml"));

        // Left not on lockscreen
        statusBar.createWorkCopy();
        hideE = statusBar.createHideyLayout(X_WRAP_CONTENT, "left");
        customClock = statusBar.createClock(false, "left|center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        statusBar.insertLeft(hideE);

        statusBar.writeDocument(new File(utils.baseFolders, utils.getType2(context, R.string.left_no_clock)+"/layout/status_bar.xml"));

        //Dynamic Clock
        if (prefUtils.getBool(DEV_MAKE_DYNAMIC)){
            utils.changeAttribute(customClock, X_ID, "@*com.android.systemui:id/clock");
            statusBar.writeDocument(new File(utils.baseFolders, utils.getType2(context, R.string.left_dynamic)+"/layout/status_bar.xml"));
        }
        //Stock-Like
        hideE.removeChild(customClock);
        customClock = statusBar.createClock(true, "left|center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        statusBar.writeDocument(new File(utils.baseFolders, utils.getType2(context, R.string.left_stock)+"/layout/status_bar.xml"));


        //Insert Right Clocks First
        // Right on lockscreen
        customClock = statusBar.createClock(false, "start|center", X_WRAP_CONTENT);
        statusBar.insertRight(customClock);
        statusBar.writeDocument(new File(utils.baseFolders, utils.getType2(context, R.string.right_clock)+"/layout/status_bar.xml"));

        // Right not on lockscreen
        statusBar.createWorkCopy();
        customClock = statusBar.createClock(false, "start|center", X_WRAP_CONTENT);
        hideE = statusBar.createHideyLayout(X_FILL_PARENT, "center");
        hideE.appendChild(customClock);
        statusBar.insertRight(hideE);

        statusBar.writeDocument(new File(utils.baseFolders,utils.getType2(context, R.string.right_no_clock)+"/layout/status_bar.xml"));
        // Dynamic
        if (prefUtils.getBool(DEV_MAKE_DYNAMIC)) {
            utils.changeAttribute(customClock, X_ID, "@*com.android.systemui:id/clock");
            statusBar.writeDocument(new File(utils.baseFolders, utils.getType2(context, R.string.right_dynamic)+"/layout/status_bar.xml"));
        }

        // Stock-Like
        hideE.removeChild(customClock);
        customClock = statusBar.createClock(true, "start|center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        statusBar.writeDocument(new File(utils.baseFolders, utils.getType2(context, R.string.right_stock)+"/layout/status_bar.xml"));



        // Center Clocks
        // On Lockscreen
        statusBar.createWorkCopy();
        customClock = statusBar.createClock(false, "center", X_WRAP_CONTENT);
        statusBar.insertCenter(customClock);
        statusBar.writeDocument(new File(utils.baseFolders, "res/layout/status_bar.xml"));

        // Not on Lockscreen
        statusBar.createWorkCopy();
        customClock = statusBar.createClock(false, "center", X_WRAP_CONTENT);
        hideE = statusBar.createHideyLayout(X_WRAP_CONTENT, "center");
        hideE.appendChild(customClock);
        statusBar.insertCenter(hideE);
        statusBar.writeDocument(new File(utils.baseFolders, utils.getType2(context, R.string.center_no_clock)+"/layout/status_bar.xml"));
        //Dynamic
        if (prefUtils.getBool(DEV_MAKE_DYNAMIC)){
            customClock = utils.changeAttribute(customClock, X_ID, "@*com.android.systemui:id/clock");
            statusBar.writeDocument(new File(utils.baseFolders, utils.getType2(context, R.string.center_dynamic)+"/layout/status_bar.xml"));
        }
        //STock-Like
        hideE.removeChild(customClock);
        customClock = statusBar.createClock(true, "center", X_WRAP_CONTENT);
        hideE.appendChild(customClock);
        statusBar.writeDocument( new File(utils.baseFolders, utils.getType2(context, R.string.center_stock)+"/layout/status_bar.xml"));

        // No Clock
        if (prefUtils.getBool(PREF_INCLUDE_NONE_OPT)) {
            statusBar.createWorkCopy();
            statusBar.writeDocument(new File(utils.baseFolders, utils.getType2(context, R.string.no_clock) + "/layout/status_bar.xml"));
        }

        utils.writeType2Desc(context.getString(R.string.sysui_type2_center),
                utils.baseFolders.getAbsolutePath() + "/type2");
    }

    private void modKeyguardStatusBar() throws Exception{

        KeyguardStatusBar keyguardStatusBar = new KeyguardStatusBar(utils, prefUtils, new File(srcFolder, "keyguard_status_bar.xml"));

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
        SystemIcons systemIcons = new SystemIcons(utils, prefUtils, new File(srcFolder, "/system_icons.xml"));
        systemIcons.writeDocument(new File(utils.baseFolders, "res/layout/system_icons.xml"));
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

        startFolder.forEach(k -> new File(String.format("%s/%s/layout", s, k)).mkdirs());
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
