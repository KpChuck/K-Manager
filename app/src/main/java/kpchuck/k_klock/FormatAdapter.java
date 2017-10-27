package kpchuck.k_klock;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;

import kpchuck.k_klock.Interfaces.BtnClickListener;

/**
 * Created by Karol Przestrzelski on 27/08/2017.
 */

public class FormatAdapter extends ArrayAdapter{
    Context context;
    ArrayList<String> names;
    String prefFile = "prefFileName";
    TextView text;
    SharedPreferences myPref;
    SharedPreferences.Editor editor;
    boolean hide;
    private BtnClickListener mClickListener = null;
    private BtnClickListener kClickListener = null;


    public FormatAdapter(Context context, ArrayList<String> names,
                        boolean Hide, BtnClickListener listener, BtnClickListener kklistenr) {
        super(context, R.layout.format_list, R.id.formatListTextView, names);


        this.context=context;
        this.names=names;
        this.hide=Hide;

        SharedPreferences myPref = this.context.getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPref.edit();
        this.editor=editor;
        this.myPref=myPref;

        mClickListener = listener;
        kClickListener = kklistenr;

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.format_list, parent, false);

        final TextView text = (TextView) row.findViewById(R.id.formatListTextView);
        final Button edit = (Button) row.findViewById(R.id.feditOverlays);
        final Button delete = (Button) row.findViewById(R.id.fdeleteOverlays);

        this.text=text;

        SharedPreferences bleh = PreferenceManager.getDefaultSharedPreferences(context);

        text.setText(names.get(position));


        if(!bleh.getBoolean("saveFormats", true)) edit.setVisibility(View.GONE);

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
