package kpchuck.kklock.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kpchuck.kklock.Checks;
import kpchuck.kklock.MainActivity;
import kpchuck.kklock.R;
import kpchuck.kklock.dialogs.ColorPicker;
import kpchuck.kklock.dialogs.ProOptionDialog;
import kpchuck.kklock.dialogs.TextAlertDialogFragment;
import kpchuck.kklock.interfaces.DialogClickListener;
import kpchuck.kklock.services.HideIconsService;
import kpchuck.kklock.utils.FileHelper;
import kpchuck.kklock.utils.PrefUtils;
import kpchuck.kklock.utils.StatusBarIconsUtils;

import static android.app.Activity.RESULT_OK;
import static kpchuck.kklock.constants.PrefConstants.PREF_BLACKOUT_LOCKSCREEN;
import static kpchuck.kklock.constants.PrefConstants.PREF_CUSTOM_ICON;
import static kpchuck.kklock.constants.PrefConstants.PREF_CUSTOM_ICON_FILE;
import static kpchuck.kklock.constants.PrefConstants.PREF_HIDE_ICONS_NOT_FULLY;
import static kpchuck.kklock.constants.PrefConstants.PREF_HIDE_ICONS_ON_LOCKSCREEN;
import static kpchuck.kklock.constants.PrefConstants.PREF_ICON;
import static kpchuck.kklock.constants.PrefConstants.PREF_LOCKSCREEN_STATUSBAR_SIZE;

/**
 * A simple {@link Fragment} subclass.
 */
public class IconsFragment extends Fragment {

    private PrefUtils prefUtils;
    private FragmentActivity myContext;
    private Unbinder unbinder;
    private FileHelper fileHelper;
    private boolean isPro = false;

    // Bind Everything
    @BindView (R.id.hideStatusbar) Switch lockSwitch;
    @BindView (R.id.colorIcons) Switch iconSwitch;
    @BindView (R.id.includedIconsButton) Button showIncluded;
    @BindView (R.id.addIconButton) Button addColors;
    @BindView(R.id.blackoutLockscreen) Switch blackoutSwitch;
    @BindView(R.id.hideStatusbarIcons) Switch hideIconsLockscreen;
    @BindView(R.id.hideStatusbarIconsNotFully) Switch hideIconsNotFully;
    @BindView(R.id.customIcon) Switch customIconSwitch;

    private ImagePicker imagePicker;


    public IconsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;

        super.onAttach(activity);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.prefUtils = new PrefUtils(getContext());
        this.fileHelper=new FileHelper();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_icons, container, false);
        unbinder = ButterKnife.bind(this, v);

        isPro = new Checks().isPro(getContext());
        if (isPro) {

            if (prefUtils.getBool(PREF_HIDE_ICONS_ON_LOCKSCREEN)) {
                Intent i = new Intent(getContext(), HideIconsService.class);
                getContext().startService(i);
            }
            ButterKnife.apply(hideIconsLockscreen, ENABLED, prefUtils.getBool(PREF_HIDE_ICONS_ON_LOCKSCREEN));
            ButterKnife.apply(hideIconsNotFully, ENABLED, prefUtils.getBool(PREF_HIDE_ICONS_NOT_FULLY));
            hideIconsNotFully.setVisibility(prefUtils.getBool(PREF_HIDE_ICONS_ON_LOCKSCREEN) ? View.VISIBLE : View.GONE);
        }
        else {
            prefUtils.putBool(PREF_HIDE_ICONS_ON_LOCKSCREEN, false);
            prefUtils.putBool(PREF_HIDE_ICONS_NOT_FULLY, false);
            ButterKnife.apply(hideIconsLockscreen, ENABLED, false);
            ButterKnife.apply(hideIconsNotFully, ENABLED, false);
            hideIconsLockscreen.setPaintFlags(hideIconsLockscreen.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            hideIconsLockscreen.setBackgroundColor(Color.GRAY);
            String text = hideIconsLockscreen.getText().toString();
            hideIconsLockscreen.setText(String.format("%s [%s]", text, getString(R.string.pro)));
            hideIconsNotFully.setVisibility(View.GONE);
        }

        //Set position and visibility of switches
        ButterKnife.apply(iconSwitch, ENABLED, prefUtils.getBool(PREF_ICON));
        ButterKnife.apply(lockSwitch, ENABLED, prefUtils.getBool(PREF_LOCKSCREEN_STATUSBAR_SIZE));
        ButterKnife.apply(blackoutSwitch, ENABLED, prefUtils.getBool(PREF_BLACKOUT_LOCKSCREEN));
        ButterKnife.apply(customIconSwitch, ENABLED, prefUtils.getBool(PREF_CUSTOM_ICON));


        return v;
    }

    // Butterknife Apply Methods
    static final ButterKnife.Setter<EditText, String> SetText = new ButterKnife.Setter<EditText, String>() {
        @Override public void set(EditText view, String value, int index) {
            view.setText(value);
        }
    };

    static final ButterKnife.Setter<CheckBox, Boolean> ENABLEDCheckBox = new ButterKnife.Setter<CheckBox, Boolean>() {
        @Override public void set(CheckBox view, Boolean value, int index) {
            view.setChecked(value);
        }
    };
    static final ButterKnife.Setter<Switch, Boolean> ENABLED = new ButterKnife.Setter<Switch, Boolean>() {
        @Override public void set(Switch view, Boolean value, int index) {
            view.setChecked(value);
        }
    };

    static final ButterKnife.Setter<View, Integer> SetVisibility = new ButterKnife.Setter<View, Integer>() {
        @Override
        public void set(View view, Integer value, int index) {
            view.setVisibility(value);
        }
    };


    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick (R.id.hideStatusbarIconsNotFully)
    public void notFully(){
        prefUtils.setSwitchPrefs(hideIconsNotFully, PREF_HIDE_ICONS_NOT_FULLY);
    }
    @OnClick (R.id.blackoutLockscreen)
    public void onClick(){
        prefUtils.setSwitchPrefs(blackoutSwitch, PREF_BLACKOUT_LOCKSCREEN);
    }

    private void warnUser(){
        TextAlertDialogFragment dialogFragment = new TextAlertDialogFragment();
        DialogClickListener dialogClickListener = new DialogClickListener() {
            @Override
            public void onPositiveBtnClick() {
                Intent i = new Intent(getContext(), HideIconsService.class);
                if (Build.VERSION.SDK_INT > 25)getContext().startForegroundService(i);
                else getContext().startService(i);
            }

            @Override
            public void onCancelBtnClick() {
                ButterKnife.apply(hideIconsLockscreen, ENABLED, false);
                prefUtils.setSwitchPrefs(hideIconsLockscreen, PREF_HIDE_ICONS_ON_LOCKSCREEN);
                hideIconsNotFully.animate()
                        .alpha(0.0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                ButterKnife.apply(hideIconsNotFully, SetVisibility, View.GONE);
                            }
                        });
            }
        };
        dialogFragment.Instantiate(getString(R.string.warning),
                getString(R.string.hide_icons_warning),
                getString(R.string.okay), getString(R.string.cancel), dialogClickListener);
        dialogFragment.show(myContext.getSupportFragmentManager(), "");
    }

    @OnClick(R.id.hideStatusbarIcons)
    public void hideLockscreen(){
        if (!isPro){
            new ProOptionDialog().show(myContext.getSupportFragmentManager(), "");
            hideIconsLockscreen.setChecked(false);
            return;
        }
        if (hideIconsLockscreen.isChecked()) {
            StatusBarIconsUtils utils = new StatusBarIconsUtils();
            if (utils.hasPerms(getContext())) {
                prefUtils.setSwitchPrefs(hideIconsLockscreen, PREF_HIDE_ICONS_ON_LOCKSCREEN);
                warnUser();
            }
            else {
                if (utils.setPerms()){
                    prefUtils.setSwitchPrefs(hideIconsLockscreen, PREF_HIDE_ICONS_ON_LOCKSCREEN);
                    warnUser();
                }
                else {
                    showAdbSteps();
                    ButterKnife.apply(hideIconsLockscreen, ENABLED, false);
                    hideIconsNotFully.animate()
                            .alpha(0.0f)
                            .setDuration(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    ButterKnife.apply(hideIconsNotFully, SetVisibility, View.GONE);
                                }
                            });
                }
            }
        }
        else {
            prefUtils.setSwitchPrefs(hideIconsLockscreen, PREF_HIDE_ICONS_ON_LOCKSCREEN);
            Intent i = new Intent(getContext(), HideIconsService.class);
            getContext().stopService(i);
        }
        if (hideIconsLockscreen.isChecked()) {
            ButterKnife.apply(hideIconsNotFully, SetVisibility, View.INVISIBLE);
            hideIconsNotFully.animate()
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            ButterKnife.apply(hideIconsNotFully, SetVisibility, View.VISIBLE);
                        }
                    });

        }
        else {
            hideIconsNotFully.animate()
                    .alpha(0.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            ButterKnife.apply(hideIconsNotFully, SetVisibility, View.GONE);
                        }
                    });
        }
    }

    private void showAdbSteps(){
        TextAlertDialogFragment dialogFragment = new TextAlertDialogFragment();
        DialogClickListener dialogClickListener = new DialogClickListener() {
            @Override
            public void onPositiveBtnClick() {
                fileHelper.copyToClipBoard(getContext(),
                        "adb shell pm grant kpchuck.k_klock.pro android.permission.WRITE_SECURE_SETTINGS");
            }

            @Override
            public void onCancelBtnClick() {

            }
        };
        dialogFragment.Instantiate(getString(R.string.adb_required),
                getString(R.string.adb_how_to_run) +
                        "adb shell pm grant kpchuck.k_klock.pro android.permission.WRITE_SECURE_SETTINGS",
                getString(R.string.copy_to_clipboard),
                getString(R.string.cancel),
                dialogClickListener);
        dialogFragment.show(myContext.getSupportFragmentManager(), "klock");
    }

    @OnClick(R.id.hideStatusbar)
    public void lockClick(){
        prefUtils.setSwitchPrefs(lockSwitch, PREF_LOCKSCREEN_STATUSBAR_SIZE);

    }

    @OnClick(R.id.colorIcons)
    public void iconClick(){
        prefUtils.setSwitchPrefs(iconSwitch, PREF_ICON);
    }



    // Handle Clicks for Icon View
    @OnClick(R.id.addIconButton)
    public void addColorListener(View view){
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.init(getString(R.string.add_color_name_title), getString(R.string.add_color_name_hint),
                getString(R.string.add_color_value_hint), false, "icons",
                false, view);
        colorPicker.show(myContext.getSupportFragmentManager(), "");
    }

    @OnClick(R.id.includedIconsButton)
    public void showColors(View view){
        ((MainActivity) getActivity()).ShowIncluded(view);

    }

    @OnClick (R.id.customIcon)
    public void customIconMethod(){
        if(customIconSwitch.isChecked()) {

            imagePicker = pick(customIconSwitch, PREF_CUSTOM_ICON, PREF_CUSTOM_ICON_FILE);

        }else{
            prefUtils.putBool(PREF_CUSTOM_ICON, false);
            prefUtils.remove(PREF_CUSTOM_ICON_FILE);
        }
    }

    private ImagePicker pick(final Switch mySwitch, final String switch_bool, final String file_pref) {
        ImagePicker imagePicker = new ImagePicker(this);
        final String slash = "/";

        imagePicker.setImagePickerCallback(new ImagePickerCallback(){
            @Override
            public void onImagesChosen(List<ChosenImage> images) {

                String filePath = images.get(0).getOriginalPath();
                String extension = filePath.substring(filePath.lastIndexOf("."), filePath.length());

                if (!extension.equals(".png") && !extension.equals(".xml")){
                    Toast.makeText(getContext(), getString(R.string.not_png_error_message), Toast.LENGTH_SHORT).show();
                    prefUtils.putBool(switch_bool, false);
                    ButterKnife.apply(mySwitch, ENABLED, false);
                    prefUtils.remove(file_pref);
                    new File(filePath).delete();
                }
                else{
                    try {
                        File destFolder = fileHelper.newFolder(Environment.getExternalStorageDirectory() + "/K-Manager/qs_images");
                        File destFile = new File(destFolder, "custom_icon" + extension);
                        if (destFile.exists()) destFile.delete();
                        FileUtils.copyFile(new File(filePath), destFile);
                        prefUtils.putString(file_pref, destFile.getAbsolutePath());
                        prefUtils.putBool(switch_bool, true);
                    }catch (IOException e){}
                }
                File dir = new File(new File(filePath).getParent());
                String[] files = dir.list();
                for (String f: files){
                    String check = dir.getAbsolutePath() + slash + f;
                    if (!filePath.equals(check)){
                        new File(check).delete();
                    }
                }
            }

            @Override
            public void onError(String message) {
                // Do error handling
                prefUtils.putBool(switch_bool, false);
                prefUtils.remove(file_pref);
                ButterKnife.apply(mySwitch, ENABLED, false);
            }}
        );
        imagePicker.pickImage();

        return imagePicker;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_DEVICE) {
                imagePicker.submit(data);

            }
        }else{
            Switch mySwitch = customIconSwitch;
            prefUtils.putBool(PREF_CUSTOM_ICON, false);
            prefUtils.remove(PREF_CUSTOM_ICON_FILE);
            mySwitch.setChecked(false);

        }
    }

}
