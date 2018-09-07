package kpchuck.kklock.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import kpchuck.kklock.interfaces.BtnClickListener;
import kpchuck.kklock.R;

/**
 * Created by Karol Przestrzelski on 23/08/2017.
 */

public class ColorAdapter extends ArrayAdapter {

    private boolean hide;
    private BtnClickListener mClickListener = null;
    private BtnClickListener kClickListener = null;
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> values = new ArrayList<>();


    public ColorAdapter(Context context, ArrayList<String> names, ArrayList<String> values,
                        boolean Hide, BtnClickListener listener, BtnClickListener kklistenr) {
        super(context, R.layout.color_list, R.id.colorListTextView, names);

        // Sort colors
        Map<String, String> sortedValues = new HashMap<>();
        for (int i=0; i<names.size(); i++){
            sortedValues.put(names.get(i), values.get(i));
        }
        Map<String, String> treeMap = new TreeMap<>(sortedValues);
        for (String key: treeMap.keySet()){
            this.names.add(key);
            this.values.add(treeMap.get(key));
        }

        this.hide=Hide;

        mClickListener = listener;
        kClickListener = kklistenr;

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.color_list, parent, false);

        final ImageView image = row.findViewById(R.id.colorImage);
        final TextView text = row.findViewById(R.id.colorListTextView);
        final Button edit = row.findViewById(R.id.editOverlays);
        final Button delete = row.findViewById(R.id.deleteOverlays);

        text.setText(names.get(position));

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text.setSelected(!text.isSelected());
            }
        });


        String color = values.get(position);
        if(color.startsWith("#") && (color.length() == 7 || color.length() == 9)) {
            image.setColorFilter(Color.parseColor(values.get(position)));
        }else image.setColorFilter(Color.parseColor("#ffffff"));

        if(hide){
            delete.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
        }

        edit.setTag(position);
        edit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(mClickListener != null)
                    mClickListener.onBtnClick((Integer) v.getTag());
            }
        });

        delete.setTag(position);
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(kClickListener != null)
                    kClickListener.onBtnClick((Integer) v.getTag());
            }
        });


        return row;

    }






}
