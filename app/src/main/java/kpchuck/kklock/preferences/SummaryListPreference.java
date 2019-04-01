package kpchuck.kklock.preferences;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import kpchuck.kklock.utils.PrefUtils;


public class SummaryListPreference extends ListPreference {

    public SummaryListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SummaryListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SummaryListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SummaryListPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View v = super.onCreateView(parent);
        setCurrentValue();
        setDescription(getValue());
        return v;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        setOnPreferenceChangeListener((preference, o) -> {
            setDescription(o.toString());
            return true;
        });
    }

    private void setCurrentValue(){
        setValue(new PrefUtils(getContext()).getString(getKey(), ""));
    }

    private void setDescription(String value){
        CharSequence[] entryValues = getEntryValues();
        CharSequence[] entries = getEntries();
        for (int i=0; i<entries.length; i++){
            if (entryValues[i].toString().equals(value)){
                setSummary(entries[i]);
                break;
            }
        }
    }
}
