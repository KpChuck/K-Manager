package kpchuck.k_klock.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import kpchuck.k_klock.MainActivity;
import kpchuck.k_klock.R;
import kpchuck.k_klock.Utils.FileHelper;
import kpchuck.k_klock.Utils.PrefUtils;
import static kpchuck.k_klock.Constants.PrefConstants.*;

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

        unbinder = ButterKnife.bind(this, v);
        //Set position and visibility of switches
        ButterKnife.apply(amSwitch, ENABLED, prefUtils.getBool("amPref"));
        ButterKnife.apply(iconSwitch, ENABLED, prefUtils.getBool("iconPref"));
        ButterKnife.apply(indicatorSwitch, ENABLED, prefUtils.getBool("indicatorPref"));
        ButterKnife.apply(leftSwitch, ENABLED, prefUtils.getBool("moveLeftPref"));
        ButterKnife.apply(lockSwitch, ENABLED, prefUtils.getBool("hideStatusBarPref"));
        ButterKnife.apply(carrierSwitch, ENABLED, prefUtils.getBool(PREF_CARRIER_TEXT));
        ButterKnife.apply(carrierEveryheckbox, ENABLEDCheckBox, prefUtils.getBool(PREF_CARRIER_EVERYWHERE));
        ButterKnife.apply(hideNotifs, ENABLEDCheckBox, prefUtils.getBool(PREF_CARRIER_HIDE_NOTIFICATIONS));


        if (prefUtils.getBool(PREF_CARRIER_TEXT)) ButterKnife.apply(carrierView, SetVisibility, View.VISIBLE);
        if (prefUtils.getBool("iconPref")) ButterKnife.apply(iconView, SetVisibility, View.VISIBLE);
        if (!fileHelper.getOos(prefUtils.getString("selectedRom", getString(R.string.chooseRom))).equals("OxygenOS"))
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

    @OnClick (R.id.hideNotificationIcons)
    public void hideChecked(){
        prefUtils.setCheckboxPrefs(hideNotifs, PREF_CARRIER_HIDE_NOTIFICATIONS);
    }

    @OnClick(R.id.ampm)
    public void amClick(){
        prefUtils.setSwitchPrefs(amSwitch, "amPref");

    }

    @OnClick(R.id.hideStatusbar)
    public void lockClick(){
        prefUtils.setSwitchPrefs(lockSwitch, "hideStatusbarPref");

    }

    @OnClick(R.id.moveNetworkLeft)
    public void leftClock(){
        prefUtils.setSwitchPrefs(leftSwitch, "moveLeftPref");

    }

    @OnClick(R.id.networkSignalIndicatorSwitch)
    public void indicatorClick(){
        prefUtils.setSwitchPrefs(indicatorSwitch, "indicatorPref");

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
        prefUtils.setSwitchPrefs(iconSwitch, "iconPref");
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
