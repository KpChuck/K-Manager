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
        translate();
        utils.moveAttrsIfPresent(srcFolder.getAbsolutePath());
        modStatusBar();
        modKeyguardStatusBar();
        modSystemIcons();
        blackoutTopQs();

    }

    private void writeStatusBar(StatusBar statusBar, int clockStyle) throws Exception{
        statusBar.writeDocument(new File(String.format(formatXmlPath, utils.getType2(context, clockStyle), layout, "status_bar")));
    }

    private void modStatusBar() throws Exception{
        StatusBar statusBar = new StatusBar(utils, prefUtils, new File(srcFolder, "status_bar.xml"), context);
        Element customClock;

        // Now Left Clock
        // Left on lockscreen
        statusBar.createWorkCopy(XmlUtils.LEFT);
        customClock = statusBar.createClock(false, false);
        statusBar.insertLeft(customClock);
        writeStatusBar(statusBar, R.string.left_clock);
        // Left not on lockscreen
        utils.removeElement(customClock);
        customClock = statusBar.createClock(false, true);
        statusBar.insertLeft(customClock);
        writeStatusBar(statusBar, R.string.left_no_clock);
        //Dynamic Clock
        if (prefUtils.getBool(DEV_MAKE_DYNAMIC)){
            utils.changeAttribute(utils.getFirstChildElement(customClock), X_ID, "@*com.android.systemui:id/clock");
            writeStatusBar(statusBar, R.string.left_dynamic);
        }
        //Stock-Like
        utils.removeElement(customClock);
        statusBar.removeClock();
        customClock = statusBar.createClock(true, false);
        statusBar.insertLeft(customClock);
        writeStatusBar(statusBar, R.string.left_stock);

        //Insert Right Clocks First
        // Right on lockscreen
        statusBar.createWorkCopy(XmlUtils.RIGHT);
        customClock = statusBar.createClock(false, false);
        statusBar.insertAfterRight(customClock);

        Element sysiconarea = statusBar.createSystemAreaElement();
        sysiconarea.setAttribute(X_LAYOUT_WIDTH, X_FILL_PARENT);
        statusBar.insertAtRoot(sysiconarea);
        writeStatusBar(statusBar, R.string.right_clock);
        utils.removeElement(sysiconarea);

        // Right not on lockscreen
        utils.removeElement(customClock);
        customClock = statusBar.createClock(false, false);
        statusBar.insertRight(customClock);
        writeStatusBar(statusBar, R.string.right_no_clock);
        // Dynamic
        if (prefUtils.getBool(DEV_MAKE_DYNAMIC)) {
            utils.changeAttribute(customClock, X_ID, "@*com.android.systemui:id/clock");
            writeStatusBar(statusBar, R.string.right_dynamic);
        }

        // Stock-Like
        statusBar.removeClock();
        utils.removeElement(customClock);
        customClock = statusBar.createClock(true, false);
        statusBar.insertRight(customClock);
        writeStatusBar(statusBar, R.string.right_stock);

        // Center Clocks
        // On Lockscreen
        boolean oos = prefUtils.getBool(R.string.key_oos_is_bad);

        statusBar.createWorkCopy(XmlUtils.CENTER);
        customClock = statusBar.createClock(false, oos);
        Element temp = null;
        if (oos){
            temp = statusBar.createSystemAreaElement();
            statusBar.insertCenter(temp);
        }
        statusBar.insertCenter(customClock);
        statusBar.writeDocument(new File(String.format(formatXmlPath, "res", layout, "status_bar")));

        if (temp != null) utils.removeElement(temp);

        // Not on Lockscreen
        utils.removeElement(customClock);
        customClock = statusBar.createClock(false, true);
        statusBar.insertCenter(customClock);
        writeStatusBar(statusBar, R.string.center_no_clock);

        //Dynamic
        if (prefUtils.getBool(DEV_MAKE_DYNAMIC)){
            customClock = utils.changeAttribute(utils.getFirstChildElement(customClock), X_ID, "@*com.android.systemui:id/clock");
            writeStatusBar(statusBar, R.string.center_dynamic);
        }
        //STock-Like
        utils.removeElement(customClock);
        statusBar.removeClock();
        customClock = statusBar.createClock(true, oos);
        statusBar.insertCenter(customClock);
        writeStatusBar(statusBar, R.string.center_stock);

        // No Clock
        if (prefUtils.getBool(PREF_INCLUDE_NONE_OPT)) {
            statusBar.createWorkCopy(XmlUtils.LEFT);
            writeStatusBar(statusBar, R.string.no_clock);
        }

        utils.writeType2Desc(context.getString(R.string.sysui_type2_center),
                utils.baseFolders.getAbsolutePath() + "/type2");
    }

    private void modKeyguardStatusBar() throws Exception{

        KeyguardStatusBar keyguardStatusBar = new KeyguardStatusBar(utils, prefUtils, new File(srcFolder, "keyguard_status_bar.xml"), context);

        String[] modPlaces, unmodPlaces;
        if (prefUtils.getInt(R.string.key_move_network) != XmlUtils.RIGHT) {
            modPlaces = new String[]{"res"};
            unmodPlaces = new String[]{};
        } else {
            unmodPlaces = new String[]{"res"};
            // Left and Center - No clock on lockscreen and dynamic
            modPlaces = new String[]{
                    utils.getType2(context, R.string.center_no_clock), utils.getType2(context, R.string.center_dynamic),
                    utils.getType2(context, R.string.left_no_clock), utils.getType2(context, R.string.left_dynamic),
                    utils.getType2(context, R.string.right_clock)
            };
            if (prefUtils.getBool(R.string.key_oos_is_bad))
                modPlaces = new String[]{
                        utils.getType2(context, R.string.center_no_clock), utils.getType2(context, R.string.center_dynamic),
                        utils.getType2(context, R.string.left_no_clock), utils.getType2(context, R.string.left_dynamic),
                        utils.getType2(context, R.string.center_clock), utils.getType2(context, R.string.center_stock),
                        utils.getType2(context, R.string.right_clock)
                };

        }

        // Write unmodified keyguard
        keyguardStatusBar.writeDocuments(Arrays.asList(unmodPlaces));

        // Write modified keyguard
        keyguardStatusBar.hideStatusIcons();
        keyguardStatusBar.writeDocuments(Arrays.asList(modPlaces));
    }

    private void modSystemIcons() throws Exception{
        SystemIcons systemIcons = new SystemIcons(utils, prefUtils, new File(srcFolder, "/system_icons.xml"), context);
        systemIcons.writeDocument(new File(String.format(formatXmlPath, "res", layout, "system_icons")));
    }

    private void blackoutTopQs() throws Exception{
        if (prefUtils.getBool(context.getString(R.string.key_blackout_qs_top))){
            XmlBase qs = new XmlBase(utils, prefUtils, new File(srcFolder, "quick_status_bar_header_system_icons.xml"), context);
            utils.changeAttribute(qs.getDocumentElement(), "android:background", "#ff000000");
            qs.writeDocument(new File(String.format(formatXmlPath, "res", layout, "quick_status_bar_header_system_icons.xml")));
        }
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
