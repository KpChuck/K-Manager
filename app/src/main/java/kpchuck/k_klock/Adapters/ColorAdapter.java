package kpchuck.k_klock.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

import kpchuck.k_klock.Interfaces.BtnClickListener;
import kpchuck.k_klock.R;

/**
 * Created by Karol Przestrzelski on 23/08/2017.
 */

public class ColorAdapter extends ArrayAdapter {

    Context context;
    ArrayList<String> names;
    ArrayList<String> values;
    String prefFile = "prefFileName";
    ImageView image;
    TextView text;
    boolean hide;
    private BtnClickListener mClickListener = null;
    private BtnClickListener kClickListener = null;


    public ColorAdapter(Context context, ArrayList<String> names, ArrayList<String> values,
                        boolean Hide, BtnClickListener listener, BtnClickListener kklistenr) {
        super(context, R.layout.color_list, R.id.colorListTextView, names);


        this.context=context;
        this.names=names;
        this.values=values;
        this.hide=Hide;

        mClickListener = listener;
        kClickListener = kklistenr;

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.color_list, parent, false);

        final ImageView image = (ImageView) row.findViewById(R.id.colorImage);
        final TextView text = (TextView) row.findViewById(R.id.colorListTextView);
        final Button edit = (Button) row.findViewById(R.id.editOverlays);
        final Button delete = (Button) row.findViewById(R.id.deleteOverlays);


        this.image=image;
        this.text=text;

        text.setText(names.get(position));

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
                // TODO Auto-generated method stub
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
