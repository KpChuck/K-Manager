package kpchuck.kklock.preferences;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.SuUtils;

public class RootDependentSwitchPreference extends SwitchPreference {

    public RootDependentSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!new SuUtils().hasRoot())
            setLayoutResource(R.layout.list_view);
    }

}
