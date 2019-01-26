package kpchuck.kklock.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Method;

import kpchuck.kklock.R;

public class ActionPreference extends Preference {

    private String clickListener;

    public ActionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionSwitch);
        clickListener = a.getString(R.styleable.ActionSwitch_switchListener);
        a.recycle();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        this.setOnPreferenceClickListener(this::onPreferenceClick);
    }

    private boolean onPreferenceClick(Preference preference) {

        try {
            String className = clickListener.substring(0, clickListener.lastIndexOf("."));
            String methodName = clickListener.substring(clickListener.lastIndexOf(".") + 1);

            Object obj = Class.forName(className).newInstance();
            Method method = obj.getClass().getMethod(methodName, Context.class, Preference.class);
            method.invoke(obj, getContext(), preference);

        } catch (Exception e) {
            Log.e("klock", e.getMessage());
        }


        return true;
    }
}
