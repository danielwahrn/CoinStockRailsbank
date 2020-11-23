package com.brian.stocks.stock.adapter;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class StockDepositPageAdapter extends FragmentPagerAdapter {
    private String[] items={"Coin", "Bank"};
    private List<Fragment> fragments = new ArrayList<>();
    public StockDepositPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
//        Fragment fragment = null;
        Log.d("tabselect", i+"");

        Fragment fragment=fragments.get(i);
        return fragment;
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