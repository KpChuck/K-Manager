package kpchuck.k_klock.Fragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kpchuck.k_klock.Interfaces.DialogClickListener;
import kpchuck.k_klock.Interfaces.DialogInputClickListener;
import kpchuck.k_klock.MainActivity;
import kpchuck.k_klock.R;
import kpchuck.k_klock.Utils.PrefUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class InputAlertDialogFragment extends DialogFragment {

    String title;
    String nameHint;
    String valueHint;
    DialogInputClickListener dialogClickListener;
    boolean toEdit;
    String colorsORformats;
    ArrayList<String> titles;
    ArrayList<String> values;
    boolean refresh;
    View v;
    PrefUtils prefUtils;

    public InputAlertDialogFragment() {
        // Required empty public constructor
    }

    public void Instantiate (String title, String nameHint, String valueHint,
                             boolean toEdit,
                             String colorsORformats,boolean refresh,
                             View v){
        this.title=title;
        this.nameHint=nameHint;
        this.valueHint=valueHint;
        this.toEdit=toEdit;
        this.refresh=refresh;
        this.v=v;
        this.colorsORformats=colorsORformats;
        this.titles = new ArrayList<>();
        this.values = new ArrayList<>();
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
        View view = inflater.inflate(R.layout.input_menu_dialog, null);
        builder.setView(view);
        this.prefUtils= new PrefUtils(getContext());

        TextView textView = view.findViewById(R.id.title);
        final EditText nameEdit = view.findViewById(R.id.name);
        final EditText valueEdit = view.findViewById(R.id.value);
        textView.setText(title);

        if (toEdit){
            nameEdit.setText(nameHint);
            valueEdit.setText(valueHint);
        }
        else {
            nameEdit.setHint(nameHint);
            valueEdit.setHint(valueHint);
        }
        builder
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String name = nameEdit.getText().toString();
                        String value = valueEdit.getText().toString();
                        if (!name.equals("") && !value.equals("")) {
                            name = name.replace(' ', '_') + ".xml";
                            String arrayListNameKey = colorsORformats + "Titles";
                            String arrayListValueKey = colorsORformats + "Values";

                            titles = prefUtils.loadArray(arrayListNameKey);
                            values = prefUtils.loadArray(arrayListValueKey);

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
                                if (arrayListValueKey.equals("formatsValues")) ((MainActivity) getActivity()).showIncludedFormats(v);
                                if (arrayListNameKey.equals("iconsTitles")) ((MainActivity) getActivity()).ShowIncluded(v);
                            }

                        }
                        else shortToast("Nothing was saved, you must have two values inputted");


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }}

                );

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void shortToast (String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
