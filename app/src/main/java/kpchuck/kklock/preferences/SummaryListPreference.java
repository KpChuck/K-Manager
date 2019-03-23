package kpchuck.kklock.preferences;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;

public class SummaryListPreference extends ListPreference {

    public SummaryListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        setSummary(getValue());
        setOnPreferenceChangeListener((preference, o) -> {
            setSummary(o.toString());
            return true;
        });
    }
}
