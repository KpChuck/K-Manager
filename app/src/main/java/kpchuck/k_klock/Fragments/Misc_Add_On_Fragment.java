package kpchuck.k_klock.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kpchuck.k_klock.Interfaces.DialogClickListener;
import kpchuck.k_klock.R;
import kpchuck.k_klock.Utils.FileHelper;
import kpchuck.k_klock.Utils.PrefUtils;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */


public class Misc_Add_On_Fragment extends Fragment {


    private Unbinder unbinder;
    PrefUtils prefUtils;
    private String slash = "/";
    private FragmentActivity myContext;
    private FileHelper fileHelper;


    // Bind Everything
    @BindView (R.id.noQsTilesTv) Switch qsSwitch;
    @BindView (R.id.qsTitle) Switch titleSwitch;
    @BindView (R.id.roundedRecents) Switch recentsSwitch;
    @BindView (R.id.minitMod) Switch minitSwitch;
    @BindView (R.id.qsBg) Switch qsBgSwitch;

    public Misc_Add_On_Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.prefUtils = new PrefUtils(getContext());
        this.fileHelper= new FileHelper();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_misc__add__on_, container, false);

        unbinder = ButterKnife.bind(this, v);
        //Set position and visibility of switches
        ButterKnife.apply(qsSwitch, ENABLED, prefUtils.getBool("qsPref"));
        ButterKnife.apply(recentsSwitch, ENABLED, prefUtils.getBool("recentsPref"));
        ButterKnife.apply(qsBgSwitch, ENABLED, prefUtils.getBool("qsBgPref"));
        ButterKnife.apply(minitSwitch, ENABLED, prefUtils.getBool("minitPref"));
        ButterKnife.apply(titleSwitch, ENABLED, prefUtils.getBool("qsTitlePref"));
        ButterKnife.apply(qsSwitch, ENABLED, prefUtils.getBool("qsPref"));

        if (fileHelper.getOos(prefUtils.getString("selectedRom", getString(R.string.chooseRom))).equals("OxygenOS"))
            ButterKnife.apply(qsBgSwitch, SetVisibility, View.GONE);

        return v;
    }

    // ButterKnife Methods

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

    // Handle On Click Methods
    @OnClick(R.id.noQsTilesTv)
    public void onClick (){
        prefUtils.setSwitchPrefs(qsSwitch, "qsPref");
    }

    @OnClick(R.id.qsTitle)
    public void myClick(){
        prefUtils.setSwitchPrefs(titleSwitch, "qsTitlePref");

    }

    @OnClick(R.id.roundedRecents)
    public void recentsClick(){
        prefUtils.setSwitchPrefs(recentsSwitch, "recentsPref");
    }
    @OnClick(R.id.minitMod)
    public void minitClick(){
        if(minitSwitch.isChecked()){
            TextAlertDialogFragment alertDialogFragment = new TextAlertDialogFragment();
            DialogClickListener listener = new DialogClickListener() {
                @Override
                public void onPositiveBtnClick() {
                    prefUtils.putBool("minitPref", true);
                }
                @Override
                public void onCancelBtnClick() {
                    prefUtils.putBool("minitPref", false);
                    ButterKnife.apply(minitSwitch, ENABLED, false);
                }
            };

            alertDialogFragment.Instantiate("IMPORTANT", getString(R.string.minit_disclaimer), "Enable", "Cancel", listener);
            alertDialogFragment.show(myContext.getSupportFragmentManager(), "klock");

        }else prefUtils.putBool("minitPref", false);
    }

    @OnClick(R.id.qsBg)
    public void qsBGClick(){
        if(qsBgSwitch.isChecked()) {

            imagePicker = new ImagePicker(getFragmentManager().findFragmentById(R.id.miscAddOnFragment));
            imagePicker.setImagePickerCallback(new ImagePickerCallback(){
                @Override
                public void onImagesChosen(List<ChosenImage> images) {

                    String filePath = images.get(0).getOriginalPath();

                    if (!filePath.substring(filePath.lastIndexOf("."), filePath.length()).equals(".png")){
                        shortToast("The image you have chosen is not a png. Convert it to a png and try again");
                        prefUtils.putBool("qsBgPref", false);
                        ButterKnife.apply(qsBgSwitch, ENABLED, false);
                        prefUtils.remove("qsBgFilePath");
                        new File(filePath).delete();
                    }
                    else{
                        prefUtils.putString("qsBgFilePath", filePath);
                        prefUtils.putBool("qsBgPref", true);
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
                    prefUtils.putBool("qsBgPref", false);
                    prefUtils.remove("qsBgFilePath");
                    ButterKnife.apply(qsBgSwitch, ENABLED, false);
                }}
            );
            imagePicker.pickImage();

        }else{
            prefUtils.putBool("qsBgPref", false);
            prefUtils.remove("qsBgFilePath");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_DEVICE) {
                imagePicker.submit(data);
            }
        }else{
            Switch qsSwitch = (Switch) getView().findViewById(R.id.qsBg);
            prefUtils.putBool("qsBgPref", false);
            prefUtils.remove("qsBgFilePath");
            qsSwitch.setChecked(false);

        }
    }


    private ImagePicker imagePicker;

    private void shortToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

}
