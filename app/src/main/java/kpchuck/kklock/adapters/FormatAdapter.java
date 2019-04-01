package kpchuck.kklock.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

import kpchuck.kklock.interfaces.BtnClickListener;
import kpchuck.kklock.R;

/**
 * Created by Karol Przestrzelski on 27/08/2017.
 */

public class FormatAdapter extends ArrayAdapter{
    Context context;
    ArrayList<String> names;
    TextView text;
    boolean hide;
    private BtnClickListener mClickListener = null;
    private BtnClickListener kClickListener = null;


    public FormatAdapter(Context context, ArrayList<String> names,
                        boolean Hide, BtnClickListener listener, BtnClickListener kklistenr) {
        super(context, R.layout.format_list, R.id.formatListTextView, names);


        this.context=context;
        this.names = names;
        Collections.sort(this.names);
        this.hide=Hide;

        mClickListener = listener;
        kClickListener = kklistenr;

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.format_list, parent, false);

        final TextView text = row.findViewById(R.id.formatListTextView);
        final Button edit = row.findViewById(R.id.feditOverlays);
        final Button delete = row.findViewById(R.id.fdeleteOverlays);

        this.text=text;

        text.setText(names.get(position));

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text.setSelected(!text.isSelected());
            }
        });

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
