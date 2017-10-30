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

import kpchuck.k_klock.Interfaces.DialogClickListener;
import kpchuck.k_klock.Interfaces.DialogInputClickListener;
import kpchuck.k_klock.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class InputAlertDialogFragment extends DialogFragment {

    String title;
    String nameHint;
    String valueHint;
    DialogInputClickListener dialogClickListener;
    boolean toEdit;


    public InputAlertDialogFragment() {
        // Required empty public constructor
    }

    public void Instantiate (String title, String nameHint, String valueHint,
                             DialogInputClickListener dialogClickListener, boolean toEdit){
        this.title=title;
        this.nameHint=nameHint;
        this.valueHint=valueHint;

        this.dialogClickListener=dialogClickListener;
        this.toEdit=toEdit;
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
                        String nameText = nameEdit.getText().toString();
                        String valueText = valueEdit.getText().toString();
                        dialogClickListener.onPositiveBtnClick(nameText, valueText);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dialogClickListener.onCancelBtnClick();

                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
