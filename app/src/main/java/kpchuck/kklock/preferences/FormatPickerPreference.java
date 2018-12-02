package kpchuck.kklock.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.PrefUtils;

public class FormatPickerPreference extends Preference{

    private String keyNames;
    private String keyValues;
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> values = new ArrayList<>();

    public FormatPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public void init(AttributeSet attrs){

        setLayoutResource(R.layout.recycler_view);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference);
        keyNames = a.getString(R.styleable.ColorPickerPreference_keyNames);
        keyValues = a.getString(R.styleable.ColorPickerPreference_keyValues);
        a.recycle();

    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        // Set adapter for contents.
        PrefUtils prefUtils = new PrefUtils(getContext());
        names = prefUtils.loadArray(keyNames);
        values = prefUtils.loadArray(keyValues);
        names.add(0, getContext().getString(R.string.add_format_name_hint));
        values.add(0, getContext().getString(R.string.add_format_value_hint));

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        FormatAdapter formatAdapter = new FormatAdapter();
        recyclerView.setAdapter(formatAdapter);
    }

    private class FormatAdapter extends RecyclerView.Adapter<FormatAdapter.MyViewHolder>{

        private PrefUtils prefUtils;
        private View curr_view;


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            private Button editFormat;
            private Button deleteButton;
            private MyViewHolder(View view) {
                super(view);
                editFormat = view.findViewById(R.id.formatTextViewButton);
                deleteButton = view.findViewById(R.id.deleteOverlays);
            }
        }

        private FormatAdapter() {
            prefUtils = new PrefUtils(getContext());
            // Sort colors
            Map<String, String> sortedValues = new HashMap<>();
            for (int i=0; i<names.size(); i++){
                sortedValues.put(names.get(i), values.get(i));
            }
            names.clear();
            values.clear();

            Map<String, String> treeMap = new TreeMap<>(sortedValues);
            for (String key: treeMap.keySet()){
                names.add(key);
                values.add(treeMap.get(key));
            }
        }

        // Create new views (invoked by the layout manager)
        @Override
        public FormatAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {

            View row = LayoutInflater.from(getContext())
                    .inflate(R.layout.format_preference, parent, false);
            MyViewHolder vh = new MyViewHolder(row);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            if (position == 0){
                holder.editFormat.setText(getContext().getString(R.string.add_format_name_hint));
                holder.deleteButton.setVisibility(View.INVISIBLE);
            }
            else {
                holder.editFormat.setText(names.get(position));
            }

            holder.editFormat.setOnClickListener(view -> {
                this.curr_view = view;
                showFormatDialog();
            });

            holder.deleteButton.setOnClickListener(view -> {
                int pos = getPosition(view);
                if (pos == 0) return;
                names.remove(pos);
                values.remove(pos);
                saveArrays();
                notifyDataSetChanged();
            });
        }

        private void showFormatDialog(){

            int position = getPosition(curr_view);
            boolean toEdit = position != 0;
            String nameHint = names.get(position);
            String valueHint = values.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );

            View view = inflater.inflate(R.layout.input_menu_dialog, null);
            builder.setView(view);

            TextView textView = view.findViewById(R.id.title);
            EditText nameEdit = view.findViewById(R.id.name);
            EditText valueEdit = view.findViewById(R.id.value);
            textView.setText(getContext().getString(R.string.add_format));


            if (toEdit){
                nameEdit.setText(nameHint);
                valueEdit.setText(valueHint);
            }
            else {
                nameEdit.setHint(nameHint);
                valueEdit.setHint(valueHint);
            }
            builder.setPositiveButton(getContext().getString(R.string.save), (dialogInterface, i) -> {
                String name = nameEdit.getText().toString();
                String value = valueEdit.getText().toString();
                savePrefs(name, value);
            });
            builder.setNegativeButton(getContext().getString(R.string.cancel), (dialogInterface, i) -> {
                dialogInterface.cancel();
            });

            AlertDialog alertDialog = builder.create();
            // Show it
            alertDialog.show();

        }

        private void savePrefs(String name, String value){

            int position = getPosition(curr_view);
            if (name.equals("") || value.equals("")){
                Toast.makeText(getContext(), "You must enter a name and format!", Toast.LENGTH_SHORT).show();
                return;
            }
            name = nameUsed(name, position);

            names.remove(position);
            values.remove(position);
            names.add(name);
            values.add(value);

            if (position == 0){
                names.add(0, getContext().getString(R.string.add_format_name_hint));
                values.add(0, getContext().getString(R.string.add_format_value_hint));
            }
            saveArrays();
            notifyDataSetChanged();
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return names.size();
        }

        private void saveArrays(){
            prefUtils.saveArray(getWithoutFirst(names), keyNames);
            prefUtils.saveArray(getWithoutFirst(values), keyValues);

            names.add(0, getContext().getString(R.string.add_format_name_hint));
            values.add(0, getContext().getString(R.string.add_format_value_hint));
        }

        private ArrayList<String> getWithoutFirst(ArrayList<String> list){
            ArrayList<String> arb = list;
            arb.remove(0);
            return arb;
        }

        private int getPosition(View v){
            View parentRow = (View) v.getParent();
            RecyclerView recyclerView = (RecyclerView) parentRow.getParent();
            if (recyclerView == null) return 0;
            return recyclerView.getChildAdapterPosition(parentRow);
        }

        private String nameUsed(String name, int position){
            if (names.contains(name) && names.indexOf(name) != position)
                return nameUsed(name + "0", position);
            return name;
        }
    }
}


