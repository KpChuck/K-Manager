package kpchuck.k_klock.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import kpchuck.k_klock.R;
import kpchuck.k_klock.Utils.PrefUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class StatusBar_Add_On_Fragment extends Fragment {

    // TODO: Rename and change types of parameters
    private PrefUtils prefUtils;

    public StatusBar_Add_On_Fragment() {
        // Required empty public constructor
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
            Switch mswitch = (Switch) getView().findViewById(R.id.ampm);
            prefUtils.setSwitchPrefs(mswitch, "amPref");
        }
    };
    View.OnClickListener lockClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch mswitch = (Switch) getView().findViewById(R.id.hideStatusbar);
            prefUtils.setSwitchPrefs(mswitch, "hideStatusbarPref");
        }
    };
    View.OnClickListener leftClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch qsSwitch = (Switch) getView().findViewById(R.id.moveNetworkLeft);
            prefUtils.setSwitchPrefs(qsSwitch, "moveLeftPref");
        }
    };
    View.OnClickListener indicatorClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch qsSwitch = (Switch) getView().findViewById(R.id.networkSignalIndicatorSwitch);
            prefUtils.setSwitchPrefs(qsSwitch, "indicatorPref");
        }
    };
    View.OnClickListener iconClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch qsSwitch = (Switch) getView().findViewById(R.id.colorIcons);
            prefUtils.setSwitchPrefs(qsSwitch, "iconPref");
        }
    };

}
