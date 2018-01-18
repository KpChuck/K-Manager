package kpchuck.k_klock.adapters;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


import java.util.List;

import kpchuck.k_klock.fragments.ClockFragment;
import kpchuck.k_klock.fragments.IconsFragment;
import kpchuck.k_klock.fragments.MiscFragment;
import kpchuck.k_klock.fragments.StatusBarFragment;

/**
 * Created by karol on 16/01/18.
 */

public class SwipeTabAdapter extends FragmentPagerAdapter
{

    ClockFragment clockFragment;
    IconsFragment iconsFragment;
    StatusBarFragment statusBarFragment;
    MiscFragment miscFragment;

    public SwipeTabAdapter(FragmentManager fm, ClockFragment clockFragment, IconsFragment iconsFragment, StatusBarFragment statusBarFragment,
                           MiscFragment miscFragment) {
        super(fm);
        this.clockFragment = clockFragment;
        this.iconsFragment=iconsFragment;
        this.statusBarFragment=statusBarFragment;
        this.miscFragment=miscFragment;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return clockFragment;
            case 1: return iconsFragment;
            case 2: return statusBarFragment;
            case 3: return miscFragment;
        }
        return null;
    }



    // This determines the number of tabs
    @Override
    public int getCount() {
        return 4;
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position){
            case 0: return "Clock";
            case 1: return "Icons";
            case 2: return "StatusBar";
            case 3: return "Misc";
        }
        return "Undefined";
    }
}

