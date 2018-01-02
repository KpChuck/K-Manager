package kpchuck.k_klock.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import android.view.View;
import android.widget.TextView;
import kpchuck.k_klock.interfaces.DialogClickListener;
import kpchuck.k_klock.R;


public class TextAlertDialogFragment extends DialogFragment {

    String title;
    String message;
    String positiveButton;
    String cancelButton;
    DialogClickListener dialogClickListener;

    public void Instantiate(String title, String message, String positiveButton, String cancelButton, DialogClickListener dialogClickListener){
        this.title = title;
        this.message = message;
        this.positiveButton = positiveButton;
        this.cancelButton = cancelButton;
        this.dialogClickListener = dialogClickListener;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.simple_text, null);
        builder.setView(view);
        TextView textView = (TextView) view.findViewById(R.id.simple_text_dialog);
        textView.setText(message);
        builder.setTitle(title);
        builder
            .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialogClickListener.onPositiveBtnClick();

                }
            })
            .setNegativeButton(cancelButton, new DialogInterface.OnClickListener() {
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
