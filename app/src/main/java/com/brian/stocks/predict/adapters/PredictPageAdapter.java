package com.brian.stocks.predict.adapters;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.brian.stocks.predict.IncomingPredictsFragment;
import com.brian.stocks.predict.MyPredictsFragment;
import com.brian.stocks.predict.NewPredictsFragment;

import org.json.JSONArray;

public class PredictPageAdapter extends FragmentStatePagerAdapter {
    private String[] items={"  Predict  ", "  Results  ", "  My posts  "};
    private List<Fragment> fragments = new ArrayList<>();
    private ArrayList all, incoming, my_post;
    public PredictPageAdapter(FragmentManager fm, ArrayList all, ArrayList incoming, ArrayList my_post) {
        super(fm);
        this.all = all;
        this.incoming = incoming;
        this.my_post = my_post;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new NewPredictsFragment(all);
            case 1:
                return new IncomingPredictsFragment(incoming);
            case 2:
                return new MyPredictsFragment(my_post);
            default:
                return null;
        }
//        Fragment sampleFragment=fragments.get(i);
//        return sampleFragment;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return items[position];
    }

    public void add(Fragment fragment) {
        fragments.add(fragment);
    }
}
