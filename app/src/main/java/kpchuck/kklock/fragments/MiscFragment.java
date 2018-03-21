package kpchuck.kklock.fragments;

import static kpchuck.kklock.constants.PrefConstants.*;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kpchuck.kklock.interfaces.DialogClickListener;
import kpchuck.kklock.R;
import kpchuck.kklock.utils.FileHelper;
import kpchuck.kklock.utils.PrefUtils;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */


public class MiscFragment extends Fragment {


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
    @BindView(R.id.qsHeader) Switch qsHeaderSwitch;

    public MiscFragment() {
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
        ButterKnife.apply(qsSwitch, ENABLED, prefUtils.getBool(PREF_QS));
        ButterKnife.apply(recentsSwitch, ENABLED, prefUtils.getBool(PREF_RECENTS));
        ButterKnife.apply(qsBgSwitch, ENABLED, prefUtils.getBool(PREF_QS_BG));
        ButterKnife.apply(minitSwitch, ENABLED, prefUtils.getBool(PREF_MINIT));
        ButterKnife.apply(titleSwitch, ENABLED, prefUtils.getBool(PREF_QS_BG));
        ButterKnife.apply(qsSwitch, ENABLED, prefUtils.getBool(PREF_QS));
        ButterKnife.apply(qsHeaderSwitch, ENABLED, prefUtils.getBool(PREF_QS_HEADER));

        if (fileHelper.getOos(prefUtils.getString(PREF_SELECTED_ROM, getString(R.string.chooseRom))).equals("OxygenOS"))
            ButterKnife.apply(qsBgSwitch, SetVisibility, View.GONE);

        return v;
    }

    public void oosBg(boolean show){
        if (qsBgSwitch != null) ButterKnife.apply(qsBgSwitch, SetVisibility, !show ? View.VISIBLE : View.GONE);

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
        prefUtils.setSwitchPrefs(qsSwitch, PREF_QS);
    }

    @OnClick(R.id.qsTitle)
    public void myClick(){
        prefUtils.setSwitchPrefs(titleSwitch, PREF_QS_LABEL);

    }

    @OnClick(R.id.roundedRecents)
    public void recentsClick(){
        prefUtils.setSwitchPrefs(recentsSwitch, PREF_RECENTS);
    }
    @OnClick(R.id.minitMod)
    public void minitClick(){
        if(minitSwitch.isChecked()){
            TextAlertDialogFragment alertDialogFragment = new TextAlertDialogFragment();
            DialogClickListener listener = new DialogClickListener() {
                @Override
                public void onPositiveBtnClick() {
                    prefUtils.putBool(PREF_MINIT, true);
                }
                @Override
                public void onCancelBtnClick() {
                    prefUtils.putBool(PREF_MINIT, false);
                    ButterKnife.apply(minitSwitch, ENABLED, false);
                }
            };

            alertDialogFragment.Instantiate(getString(R.string.important), getString(R.string.minit_disclaimer),
                    getString(R.string.enable), getString(R.string.cancel), listener);
            alertDialogFragment.show(myContext.getSupportFragmentManager(), "klock");

        }else prefUtils.putBool(PREF_MINIT, false);
    }

    private ImagePicker imagePickerBg;
    private ImagePicker imagePickerHeader;
    private int requestId = -1;

    @OnClick(R.id.qsBg)
    public void qsBGClick(){
        if(qsBgSwitch.isChecked()) {

            imagePickerBg = pick(0, qsBgSwitch, PREF_QS_BG, PREF_QS_BG_FILE);

        }else{
            prefUtils.putBool(PREF_QS_BG, false);
            prefUtils.remove(PREF_QS_BG_FILE);
        }
    }

    @OnClick(R.id.qsHeader)
    public void qsHeadClick(){
        if(qsHeaderSwitch.isChecked()) {

            imagePickerHeader = pick(1, qsHeaderSwitch, PREF_QS_HEADER, PREF_QS_HEADER_FILE);

        }else{
            prefUtils.putBool(PREF_QS_HEADER, false);
            prefUtils.remove(PREF_QS_HEADER_FILE);
        }
    }

    private ImagePicker pick(final int requestId, final Switch mySwitch, final String switch_bool, final String file_pref) {
        ImagePicker imagePicker = new ImagePicker(this);
        this.requestId = requestId;

        imagePicker.setImagePickerCallback(new ImagePickerCallback(){
            @Override
            public void onImagesChosen(List<ChosenImage> images) {

                String filePath = images.get(0).getOriginalPath();

                if (!filePath.substring(filePath.lastIndexOf("."), filePath.length()).equals(".png")){
                    shortToast(getString(R.string.not_png_error_message));
                    prefUtils.putBool(switch_bool, false);
                    ButterKnife.apply(mySwitch, ENABLED, false);
                    prefUtils.remove(file_pref);
                    new File(filePath).delete();
                }
                else{
                    try {
                        File destFolder = fileHelper.newFolder(Environment.getExternalStorageDirectory() + "/K-Manager/qs_images");
                        File destFile = new File(destFolder, requestId == 0 ? "qs_bg.png" : "qs_header.png");
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


        ImagePicker imagePicker = requestId == 0 ? imagePickerBg : imagePickerHeader;

        if(resultCode == RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_DEVICE) {
                imagePicker.submit(data);

            }
        }else{
            Switch mySwitch = requestId == 0 ? qsBgSwitch : qsHeaderSwitch;
            prefUtils.putBool(requestId == 0 ? PREF_QS_BG: PREF_QS_HEADER, false);
            prefUtils.remove(requestId == 0 ? PREF_QS_BG_FILE : PREF_QS_HEADER_FILE);
            mySwitch.setChecked(false);

        }
    }




    private void shortToast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

}
