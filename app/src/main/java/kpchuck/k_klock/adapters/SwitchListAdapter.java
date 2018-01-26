package kpchuck.k_klock.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kpchuck.k_klock.R;
import kpchuck.k_klock.interfaces.BtnClickListener;
import kpchuck.k_klock.utils.PrefUtils;

/**
 * Created by karol on 25/01/18.
 */

public class SwitchListAdapter extends ArrayAdapter {

    Context context;
    List<String> names;
    List<String> keys;
    PrefUtils prefUtils;
    private boolean oneValue = false;


    public SwitchListAdapter(Context context, List<String> names, List<String> keys, boolean oneValue) {
        super(context, R.layout.switch_layout, names);


        this.context=context;
        this.names= names;
        this.keys=keys;
        this.prefUtils = new PrefUtils(context);
        this.oneValue=oneValue;


    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.switch_layout, parent, false);

        final Switch s = row.findViewById(R.id.switch_id);

        s.setText(names.get(position));
        s.setChecked(prefUtils.getBool(keys.get(position)));

        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!oneValue) prefUtils.setSwitchPrefs(s, keys.get(position));
                else{
                    for (String key : keys) prefUtils.putBool(key, false);
                    prefUtils.setSwitchPrefs(s, keys.get(position));
                    notifyDataSetChanged();
                }
            }
        });
        return row;

    }




}
