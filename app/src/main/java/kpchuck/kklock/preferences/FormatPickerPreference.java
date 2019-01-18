package kpchuck.kklock.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.support.annotation.ColorInt;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
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
                holder.deleteButton.setBackground(getContext().getDrawable(android.R.drawable.ic_dialog_info));
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
                if (pos == 0) {
                    showFormatInfo();
                } else {
                    names.remove(pos);
                    values.remove(pos);
                    saveArrays();
                    notifyDataSetChanged();
                }
            });
        }

        private void showFormatInfo(){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View view = inflater.inflate(R.layout.webview_layout, null);
            builder.setView(view);
            WebView webView = view.findViewById(R.id.web_view);
            String info = String.format(format_info, fetchColor(R.attr.secondaryBackgroundColor), fetchColor(R.attr.textColor));
            Toast.makeText(getContext(),
                    "Background Color: " + fetchColor(R.attr.secondaryBackgroundColor) + " . And textColor " + fetchColor(R.attr.textColor),
                    Toast.LENGTH_LONG).show();
            webView.loadDataWithBaseURL(null, info, "text/html", "utf-8", null);

            builder.setTitle("")
                    .setPositiveButton(R.string.okay, (dialogInterface, i) -> dialogInterface.dismiss());
            builder.show();
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

        private String fetchColor( int id ) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getContext().getTheme();
            theme.resolveAttribute(id, typedValue, true);
            @ColorInt int color = typedValue.data;
            return String.format("#%06X", (0xFFFFFF & color));
        }

        private String format_info = "<style> * {background-color: %s; color: %s; }</style>\n" +
                "            <b>Clock Format Codes</b>\n" +
                "            <ul>\n" +
                "                <li>d.............Day Of Month (single digit) 7</li>\n" +
                "                <li>dd.......... Day Of Month (double digit) Zero, 07</li>\n" +
                "                <li>EEEE......Day Of Week (Full) Monday</li>\n" +
                "                <li>EEE........Week Day (Short) Mon</li>\n" +
                "                <li>MMMM....Month (Full) AUGUST</li>\n" +
                "                <li>MMM.......Month (Short) AUG</li>\n" +
                "                <li>MM..........Month (double digit) 08</li>\n" +
                "                <li>M............Month (Single digit) 8</li>\n" +
                "                <li>yyyy........Year (Full) 2013</li>\n" +
                "                <li>yy............Year (Short) 13</li>\n" +
                "                <li>h..............Hour (12 hour, single digit) 8</li>\n" +
                "                <li>hh............Hour (12 hour, double digit) 08</li>\n" +
                "                <li>H.............Hour (24 hour, single digit) 8 20</li>\n" +
                "                <li>HH...........Hour (24 hour, double digit) 08 20 (Note: some roms use kk instead)</li>\n" +
                "                <li>M.............Minute (single digit) 9</li>\n" +
                "                <li>MM..........Minute (double digit) 09</li>\n" +
                "                <li>s..............Second (single digit) 9</li>\n" +
                "                <li>ss............Second (double digit) 09</li>\n" +
                "                <li>a..............Marker AM/PM</li>\n" +
                "            </ul>\n" +
                "            <br/>\n" +
                "            <b>These can then be styled with HTML</b>\n" +
                "            <ul>\n" +
                "                <li>&lt;b&gt;...&lt;/b&gt; ..................................makes the enclosed text bold</li>\n" +
                "                <li>&lt;i&gt;...&lt;/i&gt; ...................................makes the enclosed text italic</li>\n" +
                "                <li>&lt;font size =\"X\"&gt;...&lt;/font&gt; ............sets the font size of the enclosed text to X.0dip</li>\n" +
                "                <li>&lt;font fgcolor =\"#ffffffff\"&gt;...&lt;/&gt; ........sets the foreground colour of the enclosed text</li>\n" +
                "                <li>&lt;font bgcolor =\"#ff000000\"&gt;...&lt;/&gt; .sets the background colour of the enclosed text</li>\n" +
                "                <li>&lt;big&gt;...&lt;/big&gt;.....................increases the font size of the enclosed text</li>\n" +
                "                <li>&lt;small&gt;...&lt;/small&gt;......................decreases the font size of the enclosed text</li>\n" +
                "            </ul>\n" +
                "        </body>";
    }
}


