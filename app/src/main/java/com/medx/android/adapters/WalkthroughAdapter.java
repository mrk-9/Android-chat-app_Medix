package com.medx.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * Created by alexey on 9/19/16.
 */

public class WalkthroughAdapter extends FragmentStatePagerAdapter {

    List<Fragment> mDataset;

    public WalkthroughAdapter(FragmentManager fm, List<Fragment> dataset) {
        super(fm);
        mDataset = dataset;
    }

    @Override
    public Fragment getItem(int position) {
        return mDataset.get(position);
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

}
