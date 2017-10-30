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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.File;
import java.util.List;

import kpchuck.k_klock.Interfaces.DialogClickListener;
import kpchuck.k_klock.MainActivity;
import kpchuck.k_klock.R;
import kpchuck.k_klock.Utils.PrefUtils;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class Misc_Add_On_Fragment extends Fragment {

    PrefUtils prefUtils;
    private String slash = "/";
    private FragmentActivity myContext;

    public Misc_Add_On_Fragment() {
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
        View v = inflater.inflate(R.layout.fragment_misc__add__on_, container, false);
        //Initialize Switches
        Switch qsSwitch = v.findViewById(R.id.noQsTilesTv);
        Switch titleSwitch = v.findViewById(R.id.qsTitle);
        Switch recentsSwitch = v.findViewById(R.id.roundedRecents);
        Switch minitSwitch = v.findViewById(R.id.minitMod);
        Switch qsBgSwitch = v.findViewById(R.id.qsBg);
        //Initialize the click listeners
        qsSwitch.setOnClickListener(qsClick);
        titleSwitch.setOnClickListener(titleClick);
        recentsSwitch.setOnClickListener(recentsClick);
        minitSwitch.setOnClickListener(minitClick);
        qsBgSwitch.setOnClickListener(qsBgClick);
        return v;
    }

    View.OnClickListener qsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch qsSwitch = (Switch) getView().findViewById(R.id.noQsTilesTv);
            prefUtils.setSwitchPrefs(qsSwitch, "qsPref");
        }
    };
    View.OnClickListener titleClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch mswitch = (Switch) getView().findViewById(R.id.qsTitle);
            prefUtils.setSwitchPrefs(mswitch, "qsTitlePref");
        }
    };
    View.OnClickListener recentsClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Switch qsSwitch = (Switch) getView().findViewById(R.id.roundedRecents);
            prefUtils.setSwitchPrefs(qsSwitch, "recentsPref");
        }
    };
    View.OnClickListener minitClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Switch mswitch = getView().findViewById(R.id.minitMod);
            if(mswitch.isChecked()){
                TextAlertDialogFragment alertDialogFragment = new TextAlertDialogFragment();
                DialogClickListener listener = new DialogClickListener() {
                    @Override
                    public void onPositiveBtnClick() {
                        prefUtils.putBool("minitPref", true);
                    }
                    @Override
                    public void onCancelBtnClick() {
                        prefUtils.putBool("minitPref", false);
                        mswitch.setChecked(false);
                    }
                };

                alertDialogFragment.Instantiate("IMPORTANT", getString(R.string.minit_disclaimer), "Enable", "Cancel", listener);
                alertDialogFragment.show(myContext.getSupportFragmentManager(), "klock");

            }else prefUtils.putBool("minitPref", false);
        }
    };
    View.OnClickListener qsBgClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Switch qsSwitch = getView().findViewById(R.id.qsBg);
            if(qsSwitch.isChecked()) {

                imagePicker = new ImagePicker(getFragmentManager().findFragmentById(R.id.miscAddOnFragment));
                imagePicker.setImagePickerCallback(new ImagePickerCallback(){
                    @Override
                    public void onImagesChosen(List<ChosenImage> images) {

                        String filePath = images.get(0).getOriginalPath();

                        if (!filePath.substring(filePath.lastIndexOf("."), filePath.length()).equals(".png")){
                            shortToast("The image you have chosen is not a png. Convert it to a png and try again");
                            prefUtils.putBool("qsBgPref", false);
                            qsSwitch.setChecked(false);
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
                        qsSwitch.setChecked(false);
                    }}
                );
                imagePicker.pickImage();

            }else{
                prefUtils.putBool("qsBgPref", false);
                prefUtils.remove("qsBgFilePath");
            }

        }
    };


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
