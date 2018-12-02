package kpchuck.kklock.preferences;


import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kpchuck.kklock.R;

public class SpinnerPreference extends Preference {

    private String[] names;
    private ArrayAdapter<String> adapter;

    public SpinnerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SpinnerPreference);
        names = getContext().getResources().getStringArray(a.getResourceId(R.styleable.SpinnerPreference_names, -1));
        a.recycle();
        setWidgetLayoutResource(R.layout.spinner_view);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Spinner dropdown = view.findViewById(R.id.spinner);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
        dropdown.setAdapter(adapter);
        dropdown.setSelection(getPersistedInt(0) >= names.length ? 0 : getPersistedInt(0));
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                persistInt(i);
                Toast.makeText(getContext(), String.valueOf(i), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void removeIndex(int index){
        String[] new_names = new String[names.length-1];
        int dec = 0;
        for (int i=0; i<new_names.length; i++){
            if (i == index)
                dec = 1;

            new_names[i] = names[i+dec];
        }
        names = new_names;
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        persistInt(restore ? getPersistedInt(-22) : (Integer) defaultValue);
    }
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, -22);
    }
}
