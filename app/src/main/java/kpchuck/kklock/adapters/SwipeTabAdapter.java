package kpchuck.kklock.adapters;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Environment;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Pair;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kpchuck.kklock.preferences.SettingsFragment;

/**
 * Created by karol on 16/01/18.
 */

public class SwipeTabAdapter extends FragmentPagerAdapter
{

    private List<SettingsFragment> fragments = new ArrayList<>();
    private List<String> titles = new ArrayList<>();

    public SwipeTabAdapter(FragmentManager fm, List<Pair<String, Integer>> fragments) {
        super(fm);
        for (Pair<String, Integer> pair : fragments){
            SettingsFragment settingsFragment = SettingsFragment.newInstance(pair.first, pair.second);
            this.fragments.add(settingsFragment);
            this.titles.add(pair.first);
        }
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position).getFragment();
    }



    // This determines the number of tabs
    @Override
    public int getCount() {
        return fragments.size();
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return titles.get(position);
    }


}

