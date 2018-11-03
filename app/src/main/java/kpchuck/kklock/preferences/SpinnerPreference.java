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

import kpchuck.kklock.R;

public class SpinnerPreference extends Preference {

    private String[] keys;
    private String[] names;

    public SpinnerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SpinnerPreference);
        keys = getContext().getResources().getStringArray(a.getResourceId(R.styleable.SpinnerPreference_keys, -1));
        names = getContext().getResources().getStringArray(a.getResourceId(R.styleable.SpinnerPreference_names, -1));
        a.recycle();
        setWidgetLayoutResource(R.layout.spinner_view);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Spinner dropdown = view.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
        dropdown.setAdapter(adapter);
        dropdown.setSelection(getPersistedInt(0));
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                persistInt(Integer.valueOf(keys[i]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
