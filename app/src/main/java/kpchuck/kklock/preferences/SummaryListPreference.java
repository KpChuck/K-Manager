package kpchuck.kklock.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import kpchuck.kklock.R;
import kpchuck.kklock.adapters.FormatAdapter;
import kpchuck.kklock.utils.PrefUtils;


public class SummaryListPreference extends ListPreference {

    private boolean showFont;


    public SummaryListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SummaryListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SummaryListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ListDialogPreference);
        showFont = a.getBoolean(R.styleable.ListDialogPreference_showFont, false);
        a.recycle();
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

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        if (showFont) {
            CharSequence[] entries = getEntries();
            CharSequence[] entryValues = getEntryValues();
            ArrayList<String> arrayEntries = new ArrayList<>();
            for (CharSequence i : Arrays.asList(entries)){
                arrayEntries.add(i.toString());
            }

            if (entries == null || entryValues == null || entries.length != entryValues.length) {
                throw new IllegalStateException("ListPreference requires an entries array and an entryValues array which are both the same length");
            }

            FormatAdapter adapter = new FormatAdapter(getContext(), arrayEntries, true, position -> {
                setValue(arrayEntries.get(position));
                getDialog().dismiss();
                setDescription(arrayEntries.get(position));
                Log.d("klock", arrayEntries.get(position));
            }, null, showFont);

            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("klock", "Here was presed");
                }
            });
        } else {
            super.onPrepareDialogBuilder(builder);
        }
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
