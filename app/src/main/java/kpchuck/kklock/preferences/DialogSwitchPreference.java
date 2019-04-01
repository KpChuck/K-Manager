package kpchuck.kklock.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Method;

import kpchuck.kklock.R;

public class DialogSwitchPreference extends SwitchPreference{

    private String title;
    private String message;
    private String confirmButton;
    private String confirmButtonListener;
    private boolean onlyChecked;

    public DialogSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DialogSwitch);
        title = a.getString(R.styleable.DialogSwitch_title);
        message = a.getString(R.styleable.DialogSwitch_message);
        confirmButton = a.getString(R.styleable.DialogSwitch_confirmButton);
        confirmButtonListener = a.getString(R.styleable.DialogSwitch_confirmButtonClickListener);
        onlyChecked = a.getBoolean(R.styleable.DialogSwitch_only_checked, true);
        a.recycle();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        this.setOnPreferenceClickListener(this::onPreferenceClick);
    }

    private boolean onPreferenceClick(Preference preference) {
        if (onlyChecked && !((SwitchPreference) preference).isChecked())
            return true;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);

        LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
        View cust_view = inflater.inflate(R.layout.simple_text, null);
        builder.setView(cust_view);
        TextView textView = cust_view.findViewById(R.id.simple_text_dialog);
        textView.setText(message);
        textView.setTextColor(Color.WHITE);
        builder.setTitle(title);

        builder.setPositiveButton(confirmButton, (dialog, which) -> {
            try {
                if (onlyChecked)
                    ((SwitchPreference) preference).setChecked(true);

                String className = confirmButtonListener.substring(0, confirmButtonListener.lastIndexOf("."));
                String methodName = confirmButtonListener.substring(confirmButtonListener.lastIndexOf(".") + 1);

                Object obj = Class.forName(className).newInstance();
                Method method = obj.getClass().getMethod(methodName, Context.class, SwitchPreference.class);
                method.invoke(obj, getContext(), preference);

            } catch (Exception e) {
                Log.e("klock", e.getMessage());
            }

        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            if (onlyChecked)
                switchPreference.setChecked(false);

        })
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show();

        return true;
    }
}
