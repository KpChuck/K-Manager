package kpchuck.kklock.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kpchuck.kklock.Checks;
import kpchuck.kklock.MainActivity;
import kpchuck.kklock.R;
import kpchuck.kklock.interfaces.DialogClickListener;
import kpchuck.kklock.services.HideIconsService;
import kpchuck.kklock.utils.FileHelper;
import kpchuck.kklock.utils.PrefUtils;
import kpchuck.kklock.utils.StatusBarIconsUtils;

import static kpchuck.kklock.constants.PrefConstants.PREF_BLACKOUT_LOCKSCREEN;
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
            hideIconsLockscreen.setText(text + " [PRO]");
            hideIconsNotFully.setVisibility(View.GONE);
        }

        //Set position and visibility of switches
        ButterKnife.apply(iconSwitch, ENABLED, prefUtils.getBool(PREF_ICON));
        ButterKnife.apply(lockSwitch, ENABLED, prefUtils.getBool(PREF_LOCKSCREEN_STATUSBAR_SIZE));
        ButterKnife.apply(blackoutSwitch, ENABLED, prefUtils.getBool(PREF_BLACKOUT_LOCKSCREEN));

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
        if(!isPro){
            new ProOptionDialog().show(myContext.getSupportFragmentManager(), "");
            hideIconsNotFully.setChecked(false);
            return;
        }
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
        dialogFragment.Instantiate("Warning :)",
                "Hiding icons on lockscreen result in more icons appearing or sometimes all of them disappearing." +
                        "You can use the SystemUI Tuner app from the playstore to backup/restore your icon configuration.\n" +
                        "You have been and will continue being warned, so I take no responsibility for any icons I may cause",
                "Okay, I understand", "Cancel", dialogClickListener);
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
                        "adb shell pm grant kpchuck.k_klock android.permission.WRITE_SECURE_SETTINGS");
            }

            @Override
            public void onCancelBtnClick() {

            }
        };
        dialogFragment.Instantiate("Adb Permissions Required",
                "You need to grant K-Manager permissions through adb for this function to work" +
                        "\nRun this command through ADB on your PC\n\n" +
                        "adb shell pm grant kpchuck.k_klock android.permission.WRITE_SECURE_SETTINGS",
                "Copy to Clipboard",
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
        InputAlertDialogFragment dialogFragment = new InputAlertDialogFragment();
        dialogFragment.Instantiate(getString(R.string.add_color_name_title), getString(R.string.add_color_name_hint),
                getString(R.string.add_color_value_hint), false, "icons",
                false, view);
        dialogFragment.show(myContext.getSupportFragmentManager(), "klock");
    }

    @OnClick(R.id.includedIconsButton)
    public void showColors(View view){
        ((MainActivity) getActivity()).ShowIncluded(view);

    }

}
