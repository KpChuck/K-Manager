package kpchuck.kklock.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import kpchuck.kklock.R;
import kpchuck.kklock.adapters.SwitchListAdapter;
import kpchuck.kklock.interfaces.BtnClickListener;

public class ListDialogFragment  extends DialogFragment {

    public ListDialogFragment() {
        // Required empty public constructor
    }

    private String title;
    private List<String> names;
    private List<String> keys;
    private boolean oneValue;
    private boolean showButton;
    private BtnClickListener btnClickListener;

    public void Instantiate (String title, List<String> names,
                             List<String> keys,
                             boolean oneValue, boolean showButton, BtnClickListener btnClickListener){
        this.title=title;
        this.names=names;
        this.keys=keys;
        this.oneValue=oneValue;
        this.showButton = showButton;
        this.btnClickListener = btnClickListener;
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
        Button button = view.findViewById(R.id.list_button);
        button.setVisibility(showButton ? View.VISIBLE : View.GONE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnClickListener.onBtnClick(1);
            }
        });


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
