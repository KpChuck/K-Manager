package kpchuck.kklock.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.Preference;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.PrefUtils;

public class ColorPickerPreference extends Preference{

    private String keyNames;
    private String keyValues;
    private String nameHint;
    private ListView listView;
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> values = new ArrayList<>();

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public void init(AttributeSet attrs){

        setLayoutResource(R.layout.recycler_view);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference);
        keyNames = a.getString(R.styleable.ColorPickerPreference_keyNames);
        keyValues = a.getString(R.styleable.ColorPickerPreference_keyValues);
        nameHint = a.getString(R.styleable.ColorPickerPreference_nameHint);
        a.recycle();

    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        //listView = view.findViewById(R.id.list_view);
        // Set adapter for contents.
        PrefUtils prefUtils = new PrefUtils(getContext());
        names = prefUtils.loadArray(keyNames);
        values = prefUtils.loadArray(keyValues);
        names.add(0, "Add a color");
        values.add(0, "#ffffff");

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        ColorAdapter colorAdapter = new ColorAdapter();
        recyclerView.setAdapter(colorAdapter);
    }

    private class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.MyViewHolder>{

        private PrefUtils prefUtils;
        private boolean editing = false;
        private View curr_view;


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            private EditText editText;
            private ImageView imageView;
            private Button deleteButton;
            private Button saveButton;
            private MyViewHolder(View view) {
                super(view);
                editText = view.findViewById(R.id.colorListEditTextView);
                imageView = view.findViewById(R.id.colorImage);
                deleteButton = view.findViewById(R.id.deleteOverlays);
                saveButton = view.findViewById(R.id.editOverlays);
            }
        }

        private ColorAdapter() {
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
        public ColorAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {

            View row = LayoutInflater.from(getContext())
                    .inflate(R.layout.color_preference, parent, false);
            MyViewHolder vh = new MyViewHolder(row);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            setColor(holder.imageView, position == 0 ? "#ffffff" : values.get(position));

            if (position == 0){
                holder.editText.setHint(nameHint);
                holder.imageView.setTag("#ffffff");
            }
            else {
                holder.editText.setText(names.get(position));
                holder.saveButton.setVisibility(View.INVISIBLE);
            }


            holder.editText.setOnFocusChangeListener((view, hasFocus) -> {
                if (hasFocus) {
                    if (editing && getPosition(view) != getPosition(curr_view))
                        savePrefs();
                    setEditing(view);
                }
            });

            holder.saveButton.setOnClickListener(view -> {
                if (holder.saveButton.getVisibility() == View.VISIBLE) {
                    savePrefs(view);
                    holder.saveButton.setVisibility(View.INVISIBLE);
                    holder.editText.setText("");
                    holder.editText.setHint(nameHint);
                    editing = false;
                }
            });

            holder.imageView.setOnClickListener(view -> {
                if(editing && getPosition(view) != getPosition(curr_view))
                    savePrefs();
                setEditing(view);
                pickColor();
            });

            holder.deleteButton.setOnClickListener(view -> {
                int pos = getPosition(view);
                if (pos == 0){
                    setColor(holder.imageView, "#ffffff");
                    holder.imageView.setTag("#ffffff");
                    holder.editText.setText("");
                    holder.editText.setHint(nameHint);
                    return;
                }
                names.remove(pos);
                values.remove(pos);
                saveArrays();
                notifyDataSetChanged();
            });


        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return names.size();
        }

        private void setColor(ImageView image, String color){
            if(color.startsWith("#") && (color.length() == 7 || color.length() == 9))
                image.setColorFilter(Color.parseColor(color));
            else
                image.setColorFilter(Color.parseColor("#ffffff"));
        }

        private void pickColor(){
            ColorPickerDialog dialog = ColorPickerDialog.newBuilder()
                    .setShowAlphaSlider(true)
                    .setDialogId(0)
                    .create();
            dialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
                @Override
                public void onColorSelected(int dialogId, int color) {
                    ViewGroup row = (ViewGroup) curr_view.getParent();
                    ImageView colorView = row.findViewById(R.id.colorImage);
                    colorView.setTag("#" + Integer.toHexString(color));
                    setColor(colorView, "#" + Integer.toHexString(color));
                }

                @Override
                public void onDialogDismissed(int dialogId) {

                }
            });
            dialog.show(((Activity) getContext()).getFragmentManager(), "color-picker");
        }

        private void setEditing(View view){
            this.curr_view=view;
            editing=true;
            Button edit = ((ViewGroup) view.getParent()).findViewById(R.id.editOverlays);
            edit.setVisibility(View.VISIBLE);
        }

        private void savePrefs(){
            savePrefs(curr_view);
        }

        private void savePrefs(View curr_view){

            int position = getPosition(curr_view);
            ViewGroup row = (ViewGroup) curr_view.getParent();
            EditText text = row.findViewById(R.id.colorListEditTextView);
            String name = nameUsed(text.getText().toString(), position);
            if (name.equals("")){
                Toast.makeText(getContext(), "You must enter a name!", Toast.LENGTH_SHORT).show();
                return;
            }

            ImageView colorImage = row.findViewById(R.id.colorImage);
            String color = (String) colorImage.getTag();

            names.remove(position);
            values.remove(position);
            names.add(name);
            values.add(color);

            if (position == 0){
                names.add(0, nameHint);
                values.add(0, "#ffffff");
            }
            saveArrays();

            notifyDataSetChanged();
        }

        private void saveArrays(){
            prefUtils.saveArray(getWithoutFirst(names), keyNames);
            prefUtils.saveArray(getWithoutFirst(values), keyValues);

            names.add(0, nameHint);
            values.add(0, "#ffffff");
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


