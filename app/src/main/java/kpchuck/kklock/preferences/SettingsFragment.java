package kpchuck.kklock.preferences;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import kpchuck.kklock.Checks;
import kpchuck.kklock.R;
import kpchuck.kklock.dialogs.ProOptionDialog;
import kpchuck.kklock.utils.PrefUtils;

import static android.app.Activity.RESULT_OK;


public class SettingsFragment extends PreferenceFragment {

    private String title;
    private PrefUtils prefUtils;

    private Map<Integer, ImagePickerPreference> imagePickerPreferenceMap = new HashMap<>();
    private int requestId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int layout = getArguments().getInt("layout");
        title = getArguments().getString("title");

        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName("prefFileName");
        PreferenceManager.setDefaultValues(getContext(), layout, false);
        addPreferencesFromResource(layout);

        prefUtils = new PrefUtils(getContext());

        for (ImagePickerPreference imagePicker : findImagePicker()){
            imagePicker.setOnPreferenceClickListener(this::onPreferenceChange);
            imagePickerPreferenceMap.put(imagePicker.getHash(), imagePicker);
        }

        init();
    }

    public String getTitle() {
        return title;
    }

    public static SettingsFragment newInstance(String title, int layout) {

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("layout", layout);

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment getFragment(){
        return this;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        ImagePickerPreference imagePicker = imagePickerPreferenceMap.get(requestId);

        if(resultCode == RESULT_OK) {
            if(requestCode == Picker.PICK_IMAGE_DEVICE) {
                imagePicker.getImagePicker().submit(data);
            }
        }else {
            imagePicker.setChecked(false);
            prefUtils.remove(imagePicker.getImageFilePref());
        }

    }

    private boolean onPreferenceChange(Preference preference){
        if (!((SwitchPreference) preference).isChecked()) return false;

        ImagePickerPreference iPP = (ImagePickerPreference) preference;

        ImagePicker imagePicker = new ImagePicker(this);
        iPP.setImagePicker(imagePicker);
        this.requestId = iPP.getHash();

        iPP.getImagePicker().setImagePickerCallback(new ImagePickerCallback(){
            @Override
            public void onImagesChosen(List<ChosenImage> images) {

                String filePath = images.get(0).getOriginalPath();
                String extension = filePath.substring(filePath.lastIndexOf("."), filePath.length());

                if (!extension.equals(".png") && !extension.equals(".xml")){
                    shortToast(getContext().getString(R.string.not_png_error_message));
                    ((SwitchPreference) preference).setChecked(false);
                    new File(filePath).delete();
                    prefUtils.remove(iPP.getImageFilePref());
                }
                else{
                    try {
                        //Delete old files
                        File destDir = new File(iPP.getImageFilePath());
                        destDir.mkdirs();
                        File[] files = destDir.listFiles(file -> file.getName().startsWith(iPP.getImageName()));
                        for (File f : files) f.delete();

                        File destFile = new File(destDir, iPP.getImageName() + extension);
                        FileUtils.copyFile(new File(filePath), destFile);
                        prefUtils.putString(iPP.getImageFilePref(), destFile.getAbsolutePath());
                    }catch (IOException e){
                        ((SwitchPreference) preference).setChecked(false);
                        shortToast("Saving Image failed, try again later");
                    }
                }
                File dir = new File(new File(filePath).getParent());
                String[] files = dir.list();
                for (String f: files){
                    String check = dir.getAbsolutePath() + "/" + f;
                    if (!filePath.equals(check)){
                        new File(check).delete();
                    }
                }
            }

            @Override
            public void onError(String message) {
                // Do error handling
                ((SwitchPreference) preference).setChecked(false);
                prefUtils.remove(iPP.getImageFilePref());
            }}
        );
        iPP.getImagePicker().pickImage();

        return true;
    }

    private void shortToast(Object obj){
        Toast.makeText(getContext(), String.valueOf(obj), Toast.LENGTH_SHORT).show();
    }

    private List<ImagePickerPreference> findImagePicker(){
        List<ImagePickerPreference> imagePickerPreferenceList = new ArrayList<>();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int prefCount = prefScreen.getPreferenceCount();

        for(int i=0; i < prefCount; i++) {
            Preference pref = prefScreen.getPreference(i);
            // do something with the Preference
            if (pref.getClass().equals(PreferenceCategory.class)) {
                PreferenceCategory preferenceCategory = (PreferenceCategory) pref;
                for (int j=0; j< preferenceCategory.getPreferenceCount(); j++) {
                    Preference myPref = preferenceCategory.getPreference(j);
                    if (myPref.getClass().equals(ImagePickerPreference.class)){
                        imagePickerPreferenceList.add((ImagePickerPreference) myPref);

                    }
                }
            }
        }
        return imagePickerPreferenceList;
    }

    private void init(){
        ArrayList<String> stuff = new ArrayList<>(Arrays.asList(getString(R.string.pref_hide_icons_lockscreen), "hideStatusBarIconsOnLockscreenButNotFully",
                            getString(R.string.key_qs_header_switch), getString(R.string.key_am_everywhere)));
        String not_stuff = "goPro";
        boolean to_pro = new Checks().isPro(getContext());

        PreferenceScreen prefScreen = getPreferenceScreen();
        int prefCount = prefScreen.getPreferenceCount();

        for(int i=0; i < prefCount; i++) {
            Preference pref = prefScreen.getPreference(i);
            // do something with the Preference
            if (pref.getClass().equals(PreferenceCategory.class)) {
                PreferenceCategory preferenceCategory = (PreferenceCategory) pref;
                for (int j=0; j< preferenceCategory.getPreferenceCount(); j++) {
                    Preference key_pref = preferenceCategory.getPreference(j);
                    if (!to_pro && key_pref.hasKey() && stuff.contains(key_pref.getKey())){
                        if (key_pref.getClass().equals(SwitchPreference.class))
                            ((SwitchPreference) key_pref).setChecked(false);
                        String text = key_pref.getTitle().toString();
                        key_pref.setTitle(String.format("%s [%s]", text, getString(R.string.pro)));
                        key_pref.setOnPreferenceClickListener(preference -> {
                            if (key_pref.getClass().equals(SwitchPreference.class))
                                ((SwitchPreference) key_pref).setChecked(false);
                            showProDialog();
                            return true;
                        });
                    }
                    else if (to_pro && key_pref.hasKey() && key_pref.getKey().equals(not_stuff)){
                        key_pref.setLayoutResource(R.layout.list_view);
                    }
                }
            }
        }
    }

    private void showProDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.simple_text, null);
        builder.setView(view);
        TextView textView = view.findViewById(R.id.simple_text_dialog);
        textView.setText(getString(R.string.pro_message));
        builder.setTitle(getString(R.string.pro_title));
        builder
                .setPositiveButton(getString(R.string.upgrade), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.k_manager_pro_link)));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        }
                );
        builder.create().show();
    }

}
