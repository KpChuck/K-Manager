package kpchuck.k_klock.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import kpchuck.k_klock.Adapters.ColorAdapter;
import kpchuck.k_klock.Interfaces.BtnClickListener;
import kpchuck.k_klock.MainActivity;
import kpchuck.k_klock.R;
import kpchuck.k_klock.Utils.PrefUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class StatusBar_Add_On_Fragment extends Fragment {

    // TODO: Rename and change types of parameters
    private PrefUtils prefUtils;
    CardView iconView;
    private FragmentActivity myContext;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add__on_, container, false);
        //Initialize the switches
        Switch amSwitch = (Switch) v.findViewById(R.id.ampm);
        Switch lockSwitch = (Switch) v.findViewById(R.id.hideStatusbar);
        Switch leftSwitch = (Switch) v.findViewById(R.id.moveNetworkLeft);
        Switch indicatorSwitch = (Switch) v.findViewById(R.id.networkSignalIndicatorSwitch);
        Switch iconSwitch = (Switch) v.findViewById(R.id.colorIcons);
        //Setup the add icons cardview
        Button showIncluded = v.findViewById(R.id.includedIconsButton);
        Button addColors = v.findViewById(R.id.addIconButton);
        addColors.setOnClickListener(addColorListener);
        this.iconView = v.findViewById(R.id.iconColorCardView);
        showIncluded.setOnClickListener(showColorListener);
        //Initialize the click listeners
        amSwitch.setOnClickListener(amClick);
        lockSwitch.setOnClickListener(lockClick);
        leftSwitch.setOnClickListener(leftClick);
        indicatorSwitch.setOnClickListener(indicatorClick);
        iconSwitch.setOnClickListener(iconClick);
        return v;
    }

    View.OnClickListener amClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch mswitch = getView().findViewById(R.id.ampm);
            prefUtils.setSwitchPrefs(mswitch, "amPref");
        }
    };
    View.OnClickListener lockClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch mswitch = getView().findViewById(R.id.hideStatusbar);
            prefUtils.setSwitchPrefs(mswitch, "hideStatusbarPref");
        }
    };
    View.OnClickListener leftClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch qsSwitch = getView().findViewById(R.id.moveNetworkLeft);
            prefUtils.setSwitchPrefs(qsSwitch, "moveLeftPref");
        }
    };
    View.OnClickListener indicatorClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch qsSwitch = getView().findViewById(R.id.networkSignalIndicatorSwitch);
            prefUtils.setSwitchPrefs(qsSwitch, "indicatorPref");
        }
    };
    View.OnClickListener iconClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch qsSwitch = getView().findViewById(R.id.colorIcons);
            prefUtils.setSwitchPrefs(qsSwitch, "iconPref");
            if (qsSwitch.isChecked()) {
                iconView.setVisibility(View.INVISIBLE);
                iconView.animate()
                        .alpha(1.0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                iconView.setVisibility(View.VISIBLE);
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
                                iconView.setVisibility(View.GONE);
                            }
                        });
            }
        }
    };

    View.OnClickListener addColorListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            InputAlertDialogFragment dialogFragment = new InputAlertDialogFragment();
            dialogFragment.Instantiate(getString(R.string.add_color_name_title), getString(R.string.add_color_name_hint),
                    getString(R.string.add_color_value_hint), false, "icons",
                    false, view);
            dialogFragment.show(myContext.getSupportFragmentManager(), "klock");
        }
    };

    View.OnClickListener showColorListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ((MainActivity) getActivity()).ShowIncluded(view);
        }

    };

}
