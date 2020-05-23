package kpchuck.kklock.preferences;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.pavelsikun.seekbarpreference.SeekBarPreference;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.FontListParser;
import kpchuck.kklock.utils.PrefUtils;
import kpchuck.kklock.xml.XmlUtils;

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

        if (new XmlUtils().hasStatusIconContainer(getContext())){
            removeOption();
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

    private void fixSeekerDependencies(){
        List<SeekBarPreference> l = getPreferencesByClass(SeekBarPreference.class);
        for (SeekBarPreference preference: l){
            if (preference.getDependency() != null && preference.getDependency().length() > 0){
                String dependency = preference.getDependency();
                Preference depPref = findPreference(dependency);
                preference.setEnabled(depPref.isEnabled());
                depPref.setOnPreferenceChangeListener((preference1, o) -> {
                    preference.setEnabled((boolean) o);
                    return true;
                });
            }
        }
    }

    private void removeOption(){
        SpinnerPreference preference = (SpinnerPreference) findPreference(getString(R.string.key_move_network));
        if (preference != null)
            preference.removeIndex(2);
    }

    private <T> List<T> getPreferencesByClass(Class className){
        List<T> list = new ArrayList<>();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int prefCount = prefScreen.getPreferenceCount();

        for(int i=0; i < prefCount; i++) {
            Preference pref = prefScreen.getPreference(i);
            // do something with the Preference
            if (pref.getClass().equals(PreferenceCategory.class)) {
                PreferenceCategory preferenceCategory = (PreferenceCategory) pref;
                for (int j=0; j< preferenceCategory.getPreferenceCount(); j++) {
                    Preference myPref = preferenceCategory.getPreference(j);
                    if (myPref.getClass().equals(className)){
                        list.add((T) myPref);
                    }
                }
            } else if (pref.getClass().equals(className)){
                list.add((T)pref);
            }
        }
        return list;
    }

    private List<ImagePickerPreference> findImagePicker(){
        return getPreferencesByClass(ImagePickerPreference.class);
    }

    private void init(){
        fixSeekerDependencies();
        Preference stockStyle = findPreference(getString(R.string.key_stock_style));
        if (stockStyle != null) {
            stockStyle.setOnPreferenceChangeListener((preference, o) -> {
                PreferenceCategory pc = (PreferenceCategory) findPreference("formatOptionsPreference");
                pc.setEnabled(!Boolean.valueOf(o.toString()));
                return true;
            });
        }
        Preference fo = findPreference(getString(R.string.key_clock_font));
        if (fo != null){
            SummaryListPreference fontOptions = (SummaryListPreference) fo;
            final List<FontListParser.SystemFont> fonts = FontListParser.safelyGetSystemFonts();
            CharSequence[] items = new CharSequence[fonts.size()*FontListParser.TYPES.size()];
            int j = 0;
            for (int i = 0; i < items.length;) {
                String input = fonts.get(j++).name;
                String name = input.substring(0, 1).toUpperCase() + input.substring(1);
                for (String key: FontListParser.TYPES.keySet()) {
                    items[i] = name + " " + key;
                    i++;
                }
            }
            fontOptions.setEntries(items);
            fontOptions.setEntryValues(items);
        }
    }

}
