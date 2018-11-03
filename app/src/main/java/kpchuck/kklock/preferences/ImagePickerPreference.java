package kpchuck.kklock.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Environment;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.PrefUtils;

public class ImagePickerPreference extends SwitchPreference{

    private String imageName;
    private String imageFilePath = Environment.getExternalStorageDirectory() + "/K-Manager/images";
    private ImagePicker imagePicker;
    private String imageFilePref;

    public ImagePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ImagePickerPreference);
        imageName = a.getString(R.styleable.ImagePickerPreference_imageFilePath);
        imageFilePref = a.getString(R.styleable.ImagePickerPreference_imageFilePref);
        a.recycle();
        imagePicker = new ImagePicker((Activity) getContext());
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        File[] files = new File(imageFilePath).listFiles(file -> file.getName().startsWith(imageName));
        if (files == null || files.length == 0){
            this.setChecked(false);
        }
    }

    public void setImagePicker(ImagePicker imagePicker){
        this.imagePicker=imagePicker;
    }

    public int getHash(){
        return imageName.hashCode();
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public String getImageName() {
        return imageName;
    }

    public ImagePicker getImagePicker(){
        return imagePicker;
    }

    public String getImageFilePref() {
        return imageFilePref;
    }
}
