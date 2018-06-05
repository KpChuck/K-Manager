package kpchuck.kklock.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.widget.Button;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import kpchuck.kklock.MainActivity;
import kpchuck.kklock.R;
import kpchuck.kklock.utils.MessageEvent;
import kpchuck.kklock.utils.PrefUtils;

public class ColorPicker extends DialogFragment{

    String title;
    String nameHint;
    String valueHint;
    boolean toEdit;
    String start_pref;
    boolean refresh;
    View v;
    PrefUtils prefUtils;
    String color_value = "";
    private Button valueEdit;

    public ColorPicker() {
        // Required empty public constructor
    }

    public void init(String title, String nameHint, String valueHint,
                     boolean toEdit,
                     String start_pref,boolean refresh,
                     View v){
        this.title=title;
        this.nameHint=nameHint;
        this.valueHint=valueHint;
        this.toEdit=toEdit;
        this.refresh=refresh;
        this.v=v;
        this.start_pref=start_pref;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.color_picker_dialog, null);
        builder.setView(view);
        this.prefUtils = new PrefUtils(getContext());

        TextView textView = view.findViewById(R.id.title);
        final EditText nameEdit = view.findViewById(R.id.name);
        valueEdit = view.findViewById(R.id.value);
        textView.setText(title);

        valueEdit.setText(valueHint);
        if (!valueHint.startsWith("#")) valueHint = "#ff0000";
        valueEdit.setOnClickListener(click_listener);

        if (toEdit){
            nameEdit.setText(nameHint);
        }
        else {
            nameEdit.setHint(nameHint);
        }
        builder
                .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String name = nameEdit.getText().toString();
                        String value = color_value;
                        if (!name.equals("") && !value.equals("")) {
                            name = name.replace(' ', '_') + ".xml";
                            String arrayListNameKey = start_pref + "Titles";
                            String arrayListValueKey = start_pref + "Values";

                            ArrayList<String> titles = prefUtils.loadArray(arrayListNameKey);
                            ArrayList<String> values = prefUtils.loadArray(arrayListValueKey);

                            String k = nameHint.replace(' ','_') + ".xml";
                            if (toEdit) {
                                for (int i=0; i<titles.size(); i++){
                                    if (titles.get(i).equals(k)){
                                        titles.remove(i);
                                        values.remove(i);
                                    }
                                }
                            }

                            titles.add(name);
                            values.add(value);

                            prefUtils.saveArray(titles, arrayListNameKey);
                            prefUtils.saveArray(values, arrayListValueKey);

                            if(refresh){
                                if(arrayListValueKey.equals("colorsValues")) ((MainActivity) getActivity()).showIncludedColors(v);
                                if (arrayListNameKey.equals("iconsTitles")) ((MainActivity) getActivity()).ShowIncluded(v);
                            }

                        }
                        else Toast.makeText(getContext(), getString(R.string.error_adding_new_value), Toast.LENGTH_SHORT).show();


                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }}

                );

        // Create the AlertDialog object and return it
        return builder.create();

    }

    View.OnClickListener click_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ColorPickerDialog
                    .newBuilder()
                    .setShowAlphaSlider(true)
                    .setDialogId(0)
                    .show(getActivity());
        }
    };

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void colorPicked(MessageEvent event){
        if (event.dialogId == 0){
            color_value = "#" + event.color;
            valueEdit.setText(color_value);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}
