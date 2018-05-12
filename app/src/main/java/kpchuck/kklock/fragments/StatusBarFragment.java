package kpchuck.kklock.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import kpchuck.kklock.Checks;
import kpchuck.kklock.R;
import kpchuck.kklock.adapters.SwitchListAdapter;
import kpchuck.kklock.interfaces.DialogClickListener;
import kpchuck.kklock.services.HideIconsService;
import kpchuck.kklock.utils.FileHelper;
import kpchuck.kklock.utils.PrefUtils;

import static android.app.Activity.RESULT_OK;
import static kpchuck.kklock.constants.PrefConstants.PREF_AM;
import static kpchuck.kklock.constants.PrefConstants.PREF_CARRIER_CUSTOM_TEXT;
import static kpchuck.kklock.constants.PrefConstants.PREF_CARRIER_EVERYWHERE;
import static kpchuck.kklock.constants.PrefConstants.PREF_CARRIER_HIDE_NOTIFICATIONS;
import static kpchuck.kklock.constants.PrefConstants.PREF_CARRIER_TEXT;
import static kpchuck.kklock.constants.PrefConstants.PREF_CHANGE_STATBAR_COLOR;
import static kpchuck.kklock.constants.PrefConstants.PREF_CLOCK_HIDEABLE;
import static kpchuck.kklock.constants.PrefConstants.PREF_CUSTOM_ICON;
import static kpchuck.kklock.constants.PrefConstants.PREF_CUSTOM_ICON_FILE;
import static kpchuck.kklock.constants.PrefConstants.PREF_HIDE_ICONS_NOT_FULLY;
import static kpchuck.kklock.constants.PrefConstants.PREF_HIDE_ICONS_ON_LOCKSCREEN;
import static kpchuck.kklock.constants.PrefConstants.PREF_INDICATORS;
import static kpchuck.kklock.constants.PrefConstants.PREF_MOVE_LEFT;
import static kpchuck.kklock.constants.PrefConstants.PREF_MOVE_NOTIFICATIONS_RIGHT;
import static kpchuck.kklock.constants.PrefConstants.PREF_SELECTED_ROM;
import static kpchuck.kklock.constants.PrefConstants.PREF_STATBAR_COLOR;
import static kpchuck.kklock.constants.PrefConstants.PREF_STATUSBAR_CLOCK_SIZE;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatusBarFragment extends Fragment {


    public StatusBarFragment() {
        // Required empty public constructor
    }

    private PrefUtils prefUtils;
    private FragmentActivity myContext;
    private Unbinder unbinder;
    FileHelper fileHelper;

    // Bind Everything
    @BindView(R.id.ampm)
    Switch amSwitch;
    @BindView (R.id.moveNetworkLeft) Switch leftSwitch;
    @BindView (R.id.networkSignalIndicatorSwitch) Switch indicatorSwitch;
    @BindView (R.id.customText) Switch customText;
    @BindView (R.id.carrierCardView) CardView carrierView;
    @BindView (R.id.editCarrierText) EditText carrierEditText;
    @BindView (R.id.showLockscreen) CheckBox showLockscreen;
    @BindView(R.id.hideNotificationIcons) CheckBox hideNotifs;
    @BindView(R.id.clockSize) Switch clockSizeSwitch;
    @BindView(R.id.notifsRight) Switch notifsRightSwitch;
    @BindView(R.id.statBarColor) Switch statBarColorSwitch;
    @BindView(R.id.clockHideable) Switch clockHideableSwitch;

    private boolean isPro = false;



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
        this.isPro = new Checks().isPro(getContext());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_status_bar, container, false);

        unbinder = ButterKnife.bind(this, v);

        if (isPro){
            ButterKnife.apply(amSwitch, ENABLED, prefUtils.getBool(PREF_AM));

        }
        else {
            prefUtils.putBool(PREF_AM, false);
            ButterKnife.apply(amSwitch, ENABLED, false);
            amSwitch.setPaintFlags(amSwitch.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            amSwitch.setBackgroundColor(Color.GRAY);
            String text = amSwitch.getText().toString();
            amSwitch.setText(text + " [PRO]");
        }


        //Set position and visibility of switches
        ButterKnife.apply(clockSizeSwitch, ENABLED, prefUtils.getBool(PREF_STATUSBAR_CLOCK_SIZE));
        ButterKnife.apply(indicatorSwitch, ENABLED, prefUtils.getBool(PREF_INDICATORS));
        ButterKnife.apply(leftSwitch, ENABLED, prefUtils.getBool(PREF_MOVE_LEFT));
        ButterKnife.apply(showLockscreen, ENABLEDCheckBox, prefUtils.getBool(PREF_CARRIER_TEXT));
        ButterKnife.apply(customText, ENABLED, prefUtils.getBool(PREF_CARRIER_EVERYWHERE));
        ButterKnife.apply(hideNotifs, ENABLEDCheckBox, prefUtils.getBool(PREF_CARRIER_HIDE_NOTIFICATIONS));
        ButterKnife.apply(notifsRightSwitch, ENABLED, prefUtils.getBool(PREF_MOVE_NOTIFICATIONS_RIGHT));
        ButterKnife.apply(statBarColorSwitch, ENABLED, prefUtils.getBool(PREF_CHANGE_STATBAR_COLOR));
        ButterKnife.apply(clockHideableSwitch, ENABLED, prefUtils.getBool(PREF_CLOCK_HIDEABLE));


        if (prefUtils.getBool(PREF_CARRIER_EVERYWHERE)) ButterKnife.apply(carrierView, SetVisibility, View.VISIBLE);
        if (!fileHelper.getOos(prefUtils.getString(PREF_SELECTED_ROM, getString(R.string.chooseRom))).equals("OxygenOS"))
            ButterKnife.apply(indicatorSwitch, SetVisibility, View.GONE);

        ButterKnife.apply(carrierEditText, SetText, prefUtils.getString(PREF_CARRIER_CUSTOM_TEXT, ""));

        return v;
    }

    public void oosIndicators(boolean show){
        if (indicatorSwitch != null)ButterKnife.apply(indicatorSwitch, SetVisibility, show ? View.VISIBLE : View.GONE);

    }

    @OnTextChanged(R.id.editCarrierText)
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
    @OnClick(R.id.showLockscreen)
    public void checkClick(){
        prefUtils.setCheckboxPrefs(showLockscreen, PREF_CARRIER_TEXT);
    }

    @OnClick(R.id.clockHideable)
    public void launchers(){
        if(clockHideableSwitch.isChecked()){
            TextAlertDialogFragment fragment = new TextAlertDialogFragment();
            DialogClickListener listener = new DialogClickListener() {
                @Override
                public void onPositiveBtnClick() {
                    prefUtils.setSwitchPrefs(clockHideableSwitch, PREF_CLOCK_HIDEABLE);
                }

                @Override
                public void onCancelBtnClick() {

                }
            };
            fragment.Instantiate("Warning :)", "If your Rom already has statusbar customization like changing its position/stuff then don't use this." +
                "\nIt will cause a SystemUI crash...", "Sounds good, enable", "Cancel", listener);
            fragment.show(myContext.getSupportFragmentManager(), "");
        }
        else
            prefUtils.setSwitchPrefs(clockHideableSwitch, PREF_CLOCK_HIDEABLE);
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
        if (!isPro) {
            new ProOptionDialog().show(myContext.getSupportFragmentManager(), "");
            amSwitch.setChecked(false);
            return;
        }
        prefUtils.setSwitchPrefs(amSwitch, PREF_AM);

    }


    @OnClick(R.id.moveNetworkLeft)
    public void leftClock(){
        prefUtils.setSwitchPrefs(leftSwitch, PREF_MOVE_LEFT);

    }

    @OnClick(R.id.networkSignalIndicatorSwitch)
    public void indicatorClick(){
        prefUtils.setSwitchPrefs(indicatorSwitch, PREF_INDICATORS);

    }

    @OnClick(R.id.statBarColor)
    public void statbarClick(){
        prefUtils.setSwitchPrefs(statBarColorSwitch, PREF_CHANGE_STATBAR_COLOR);
        if (!statBarColorSwitch.isChecked()) return;
        AlertDialog builder = new AlertDialog.Builder(getContext()).create();
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.fragment_list, null);
        builder.setView(view);

        TextView textView = view.findViewById(R.id.title);
        ListView listView = view.findViewById(R.id.listView);
        final EditText editText = view.findViewById(R.id.editTextValue);
        editText.setVisibility(View.VISIBLE);
        textView.setText("Select StatusBar Background Color");

        List<String> names = new ArrayList<>(Arrays.asList("Black", "Black 25% Transparent", "White", "Other(Specify Below)"));
        List<String> values = new ArrayList<>(Arrays.asList("#ff000000", "#BF000000", "#ffffffff", "other"));
        List<String> keys = new ArrayList<>(Arrays.asList(PREF_STATBAR_COLOR, PREF_STATBAR_COLOR, PREF_STATBAR_COLOR, PREF_STATBAR_COLOR));

        SwitchListAdapter listAdapter = new SwitchListAdapter(getContext(), names, keys, values, true);
        listView.setAdapter(listAdapter);

        // Set up buttons
        builder.setButton(AlertDialog.BUTTON_POSITIVE, "Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (prefUtils.getString(PREF_STATBAR_COLOR, "").equals("other")){
                    prefUtils.putString(PREF_STATBAR_COLOR, editText.getText().toString());
                }
            }
        });

        builder.show();
    }

    @OnClick (R.id.customText)
    public void carrierSwitch(){
        prefUtils.setSwitchPrefs(customText, PREF_CARRIER_EVERYWHERE);
        if (customText.isChecked()) {
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




}
