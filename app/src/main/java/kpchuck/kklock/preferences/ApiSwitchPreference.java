package kpchuck.kklock.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import kpchuck.kklock.R;

public class ApiSwitchPreference extends SwitchPreference {

    private int minApi;
    private int maxApi;

    public ApiSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init (AttributeSet attrs){
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ApiSwitchPreference);
        minApi = a.getInt(R.styleable.ApiSwitchPreference_minApi, -1);
        maxApi = a.getInt(R.styleable.ApiSwitchPreference_maxApi, -1);
        a.recycle();

        int curr_api = Build.VERSION.SDK_INT;

        if ((minApi != -1 && curr_api <= minApi) || (maxApi != -1 && curr_api >= maxApi)){

            setLayoutResource(R.layout.list_view);
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

    }
}
