package kpchuck.k_klock.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import kpchuck.k_klock.MainActivity;
import kpchuck.k_klock.R;
import kpchuck.k_klock.activities.InformationWebViewActivity;
import kpchuck.k_klock.interfaces.DialogClickListener;
import kpchuck.k_klock.services.HideIconsService;
import kpchuck.k_klock.utils.FileHelper;
import kpchuck.k_klock.utils.PrefUtils;
import kpchuck.k_klock.utils.StatusBarIconsUtils;

import static kpchuck.k_klock.constants.PrefConstants.*;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class StatusBar_Add_On_Fragment extends Fragment {

    private PrefUtils prefUtils;
    private FragmentActivity myContext;
    private Unbinder unbinder;
    FileHelper fileHelper;

    // Bind Everything
    @BindView(R.id.ampm) Switch amSwitch;
    @BindView(R.id.iconColorCardView) CardView iconView;
    @BindView (R.id.hideStatusbar) Switch lockSwitch;
    @BindView (R.id.moveNetworkLeft) Switch leftSwitch;
    @BindView (R.id.networkSignalIndicatorSwitch) Switch indicatorSwitch;
    @BindView (R.id.colorIcons) Switch iconSwitch;
    @BindView (R.id.includedIconsButton) Button showIncluded;
    @BindView (R.id.addIconButton) Button addColors;
    @BindView (R.id.carrierText) Switch carrierSwitch;
    @BindView (R.id.carrierCardView) CardView carrierView;
    @BindView (R.id.editCarrierText) EditText carrierEditText;
    @BindView (R.id.showEverywhereCarrier) CheckBox carrierEveryheckbox;
    @BindView(R.id.hideNotificationIcons) CheckBox hideNotifs;
    @BindView(R.id.clockSize) Switch clockSizeSwitch;
    @BindView(R.id.notifsRight) Switch notifsRightSwitch;
    @BindView(R.id.blackoutLockscreen) Switch blackoutSwitch;
    @BindView(R.id.hideStatusbarIcons) Switch hideIconsLockscreen;
    @BindView(R.id.hideStatusbarIconsNotFully) Switch hideIconsNotFully;

    public StatusBar_Add_On_Fragment() {
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
        View v = inflater.inflate(R.layout.fragment_add__on_, container, false);
        container.addView(v);

        unbinder = ButterKnife.bind(this, v);

        if (prefUtils.getBool(PREF_HIDE_ICONS_ON_LOCKSCREEN)){
            Intent i = new Intent(getContext(), HideIconsService.class);
            getContext().startService(i);
        }

        //Set position and visibility of switches
        ButterKnife.apply(clockSizeSwitch, ENABLED, prefUtils.getBool(PREF_STATUSBAR_CLOCK_SIZE));
        ButterKnife.apply(amSwitch, ENABLED, prefUtils.getBool(PREF_AM));
        ButterKnife.apply(iconSwitch, ENABLED, prefUtils.getBool(PREF_ICON));
        ButterKnife.apply(indicatorSwitch, ENABLED, prefUtils.getBool(PREF_INDICATORS));
        ButterKnife.apply(leftSwitch, ENABLED, prefUtils.getBool(PREF_MOVE_LEFT));
        ButterKnife.apply(lockSwitch, ENABLED, prefUtils.getBool(PREF_LOCKSCREEN_STATUSBAR_SIZE));
        ButterKnife.apply(carrierSwitch, ENABLED, prefUtils.getBool(PREF_CARRIER_TEXT));
        ButterKnife.apply(carrierEveryheckbox, ENABLEDCheckBox, prefUtils.getBool(PREF_CARRIER_EVERYWHERE));
        ButterKnife.apply(hideNotifs, ENABLEDCheckBox, prefUtils.getBool(PREF_CARRIER_HIDE_NOTIFICATIONS));
        ButterKnife.apply(notifsRightSwitch, ENABLED, prefUtils.getBool(PREF_MOVE_NOTIFICATIONS_RIGHT));
        ButterKnife.apply(blackoutSwitch, ENABLED, prefUtils.getBool(PREF_BLACKOUT_LOCKSCREEN));
        ButterKnife.apply(hideIconsLockscreen, ENABLED, prefUtils.getBool(PREF_HIDE_ICONS_ON_LOCKSCREEN));
        ButterKnife.apply(hideIconsNotFully, ENABLED, prefUtils.getBool(PREF_HIDE_ICONS_NOT_FULLY));


        if (prefUtils.getBool(PREF_CARRIER_TEXT)) ButterKnife.apply(carrierView, SetVisibility, View.VISIBLE);
        if (prefUtils.getBool(PREF_ICON)) ButterKnife.apply(iconView, SetVisibility, View.VISIBLE);
        if (!fileHelper.getOos(prefUtils.getString(PREF_SELECTED_ROM, getString(R.string.chooseRom))).equals("OxygenOS"))
            ButterKnife.apply(indicatorSwitch, SetVisibility, View.GONE);

        ButterKnife.apply(carrierEditText, SetText, prefUtils.getString(PREF_CARRIER_CUSTOM_TEXT, ""));

        return v;
    }

    @OnTextChanged (R.id.editCarrierText)
    public void saveEditText(CharSequence s){
        prefUtils.putString(PREF_CARRIER_CUSTOM_TEXT, s.toString());
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


    // Handle Clicks for Switches
        @OnClick (R.id.showEverywhereCarrier)
        public void checkClick(){
            prefUtils.setCheckboxPrefs(carrierEveryheckbox, PREF_CARRIER_EVERYWHERE);
        }

        @OnClick (R.id.hideStatusbarIconsNotFully)
        public void notFully(){
            prefUtils.setSwitchPrefs(hideIconsNotFully, PREF_HIDE_ICONS_NOT_FULLY);
        }
        @OnClick (R.id.blackoutLockscreen)
        public void onClick(){
            prefUtils.setSwitchPrefs(blackoutSwitch, PREF_BLACKOUT_LOCKSCREEN);
        }

        @OnClick (R.id.notifsRight)
        public void onClickTwo(){
            prefUtils.setSwitchPrefs(notifsRightSwitch, PREF_MOVE_NOTIFICATIONS_RIGHT);
        }

        @OnClick (R.id.hideNotificationIcons)
        public void hideChecked(){
            prefUtils.setCheckboxPrefs(hideNotifs, PREF_CARRIER_HIDE_NOTIFICATIONS);
        }

        @OnClick(R.id.clockSize)
        public void click(){
            prefUtils.setSwitchPrefs(clockSizeSwitch, PREF_STATUSBAR_CLOCK_SIZE);
        }

        @OnClick(R.id.ampm)
        public void amClick(){
            prefUtils.setSwitchPrefs(amSwitch, PREF_AM);

        }

        @OnClick(R.id.hideStatusbarIcons)
        public void hideLockscreen(){
            if (hideIconsLockscreen.isChecked()) {
                StatusBarIconsUtils utils = new StatusBarIconsUtils();
                if (utils.hasPerms(getContext())) {
                    prefUtils.setSwitchPrefs(hideIconsLockscreen, PREF_HIDE_ICONS_ON_LOCKSCREEN);
                    Intent i = new Intent(getContext(), HideIconsService.class);
                    getContext().startService(i);
                } else {
                    if (utils.setPerms()){
                        prefUtils.setSwitchPrefs(hideIconsLockscreen, PREF_HIDE_ICONS_ON_LOCKSCREEN);
                        Intent i = new Intent(getContext(), HideIconsService.class);
                        getContext().startService(i);
                    }
                    else {
                        showAdbSteps();
                        ButterKnife.apply(hideIconsLockscreen, ENABLED, false);
                        }
                }
            }
            else {
                prefUtils.setSwitchPrefs(hideIconsLockscreen, PREF_HIDE_ICONS_ON_LOCKSCREEN);
                Intent i = new Intent(getContext(), HideIconsService.class);
                getContext().stopService(i);
            }
        }

    private void showAdbSteps(){
        TextAlertDialogFragment dialogFragment = new TextAlertDialogFragment();
        DialogClickListener dialogClickListener = new DialogClickListener() {
            @Override
            public void onPositiveBtnClick() {
                Intent intent = new Intent(getContext(), InformationWebViewActivity.class);
                intent.putExtra("value", 5);
                startActivity(intent);
            }

            @Override
            public void onCancelBtnClick() {

            }
        };
        dialogFragment.Instantiate("Adb Permissions Required",
                "You need to grant K-Manager permissions through adb for this function to work",
                "Okay",
                "No way",
                dialogClickListener);
        dialogFragment.show(myContext.getSupportFragmentManager(), "klock");
    }

        @OnClick(R.id.hideStatusbar)
        public void lockClick(){
            prefUtils.setSwitchPrefs(lockSwitch, PREF_LOCKSCREEN_STATUSBAR_SIZE);

        }

        @OnClick(R.id.moveNetworkLeft)
        public void leftClock(){
            prefUtils.setSwitchPrefs(leftSwitch, PREF_MOVE_LEFT);

        }

        @OnClick(R.id.networkSignalIndicatorSwitch)
        public void indicatorClick(){
            prefUtils.setSwitchPrefs(indicatorSwitch, PREF_INDICATORS);

        }

        @OnClick (R.id.carrierText)
        public void carrierSwitch(){
            prefUtils.setSwitchPrefs(carrierSwitch, PREF_CARRIER_TEXT);
            if (carrierSwitch.isChecked()) {
                ButterKnife.apply(carrierView, SetVisibility, View.INVISIBLE);
                carrierView.animate()
                        .alpha(1.0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                ButterKnife.apply(carrierView, SetVisibility, View.VISIBLE);
                            }
                        });

            }
            else {
                carrierView.animate()
                        .alpha(0.0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                ButterKnife.apply(carrierView, SetVisibility, View.GONE);
                            }
                        });
            }
        }

        @OnClick(R.id.colorIcons)
        public void iconClick(){
            prefUtils.setSwitchPrefs(iconSwitch, PREF_ICON);
            if (iconSwitch.isChecked()) {
                ButterKnife.apply(iconView, SetVisibility, View.INVISIBLE);
                iconView.animate()
                        .alpha(1.0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                ButterKnife.apply(iconView, SetVisibility, View.VISIBLE);
                            }
                        });

            }
            else {
                iconView.animate()
                        .alpha(0.0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                ButterKnife.apply(iconView, SetVisibility, View.GONE);
                            }
                        });
            }
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
