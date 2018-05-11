package kpchuck.kklock.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kpchuck.kklock.MainActivity;
import kpchuck.kklock.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class ClockFragment extends Fragment {

    private Unbinder unbinder;
    private FragmentActivity myContext;

    public ClockFragment() {
        // Required empty public constructor
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;

        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_clock, container, false);
        unbinder = ButterKnife.bind(this, v);
        return v;
    }

    // Handle Clicks for Icon View
    @OnClick(R.id.addColorButton)
    public void addColorListener(View view){
        InputAlertDialogFragment dialogFragment = new InputAlertDialogFragment();
        dialogFragment.Instantiate(getString(R.string.add_color_name_title), getString(R.string.add_color_name_hint),
                getString(R.string.add_color_value_hint), false, "colors",
                false, view);
        dialogFragment.show(myContext.getSupportFragmentManager(), "klock");
    }

    @OnClick(R.id.includedColorsButton)
    public void showColors(View view){
        ((MainActivity) getActivity()).showIncludedColors(view);

    }

    // Handle Clicks for Icon View
    @OnClick(R.id.addFormatButton)
    public void addFormatListener(View view){
        InputAlertDialogFragment dialogFragment = new InputAlertDialogFragment();
        dialogFragment.Instantiate(getString(R.string.add_format_name_title), getString(R.string.add_format_name_hint),
                getString(R.string.add_format_value_hint), false, "formats",
                false, view);
        dialogFragment.show(myContext.getSupportFragmentManager(), "klock");
    }

    @OnClick(R.id.includedFormatButton)
    public void showFormat(View view){
        ((MainActivity) getActivity()).showIncludedFormats(view);

    }

    public String getTitle(){
        return "Clock";
    }

}
