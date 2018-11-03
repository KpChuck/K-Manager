package kpchuck.kklock.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kpchuck.kklock.R;
import kpchuck.kklock.adapters.ColorAdapter;
import kpchuck.kklock.adapters.FormatAdapter;

public class ListDialogPreference extends DialogPreference {

    private ArrayList<String> titles, colors;
    private boolean showColors;

    public ListDialogPreference(Context context, AttributeSet attr){
        super(context, attr);
        TypedArray a = getContext().obtainStyledAttributes(attr, R.styleable.ListDialogPreference);
        titles = getStringArray(a.getResourceId(R.styleable.ListDialogPreference_textArray, -1));
        colors = getStringArray(a.getResourceId(R.styleable.ListDialogPreference_colorArray, -1));
        showColors = a.getBoolean(R.styleable.ListDialogPreference_showColors, false);
        a.recycle();
        setDialogLayoutResource(R.layout.list_view);
    }

    private ArrayList<String> getStringArray(int id){
        Resources res = getContext().getResources();
        String[] array = res.getStringArray(id);
        return new ArrayList<>(Arrays.asList(array));
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ListView listView = view.findViewById(R.id.list_view);
        if (showColors) {
            ColorAdapter adapter = new ColorAdapter(
                    getContext(), titles, colors, true, null, null);
            listView.setAdapter(adapter);
        }
        else {
            FormatAdapter adapter = new FormatAdapter(
                    getContext(), titles, true, null, null);
            listView.setAdapter(adapter);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
    }
}
