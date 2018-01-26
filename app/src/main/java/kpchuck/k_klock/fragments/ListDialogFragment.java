package kpchuck.k_klock.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kpchuck.k_klock.MainActivity;
import kpchuck.k_klock.R;
import kpchuck.k_klock.adapters.SwitchListAdapter;
import kpchuck.k_klock.interfaces.DialogInputClickListener;
import kpchuck.k_klock.utils.PrefUtils;

public class ListDialogFragment  extends DialogFragment {

    public ListDialogFragment() {
        // Required empty public constructor
    }

    private String title;
    private List<String> names;
    private List<String> keys;
    private boolean oneValue;

    public void Instantiate (String title, List<String> names,
                             List<String> keys,
                             boolean oneValue){
        this.title=title;
        this.names=names;
        this.keys=keys;
        this.oneValue=oneValue;
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
        View view = inflater.inflate(R.layout.fragment_list, null);
        builder.setView(view);

        TextView textView = view.findViewById(R.id.title);
        ListView listView = view.findViewById(R.id.listView);
        textView.setText(title);

        SwitchListAdapter listAdapter = new SwitchListAdapter(getContext(), names, keys, null,  oneValue);
        listView.setAdapter(listAdapter);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void shortToast (String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
