package com.medx.android.utils;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.medx.android.R;
import com.medx.android.fragments.WalkthroughFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexey on 9/19/16.
 */

public class WalkthroughHelper {

    private static final int COUNT = 5;

    public static List<Fragment> getFragments(Context context) {
        List<Fragment> fragmentList = new ArrayList<>();
        String[] headArray = context.getResources().getStringArray(R.array.walkthrough_heads);
        String[] bodyArray = context.getResources().getStringArray(R.array.walkthrough_bodies);

        for (int i = 0; i < COUNT; i++) {
            Fragment fragment = null;
            if (i == 0){
                fragment = WalkthroughFragment.newInstance(headArray[i], R.mipmap.logo_circle);
            } else{
                fragment = WalkthroughFragment.newInstance(headArray[i], bodyArray[i]);
            }
            fragmentList.add(fragment);
        }
        return fragmentList;
    }
}
