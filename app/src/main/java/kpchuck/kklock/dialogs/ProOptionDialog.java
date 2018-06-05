package kpchuck.kklock.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import kpchuck.kklock.R;
import kpchuck.kklock.interfaces.DialogClickListener;

public class ProOptionDialog extends DialogFragment {


    private DialogClickListener dialogClickListener = new DialogClickListener() {
        @Override
        public void onPositiveBtnClick() {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.k_manager_pro_link)));
            startActivity(intent);
        }

        @Override
        public void onCancelBtnClick() {

        }
    };

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
        TextView textView = view.findViewById(R.id.simple_text_dialog);
        textView.setText(getString(R.string.pro_message));
        builder.setTitle(getString(R.string.pro_title));
        builder
                .setPositiveButton(getString(R.string.upgrade), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialogClickListener.onPositiveBtnClick();

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialogClickListener.onCancelBtnClick();
                            }
                        }
                );

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
