package com.medx.android.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.ViewGroup;

import com.medx.android.R;
import com.medx.android.fragments.MXChatFragment;
import com.medx.android.fragments.MXSearchFragment;
import com.medx.android.fragments.MXSettingsFragment;

/**
 * Created by alexey on 9/21/16.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {

    Context context;

    Drawable icon;
    Drawable settings;
    Drawable chat;
    Drawable search;

    public MainPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        icon = context.getResources().getDrawable(R.mipmap.logout);
        settings = context.getResources().getDrawable(R.mipmap.settings);
        chat = context.getResources().getDrawable(R.mipmap.rchat);
        search = context.getResources().getDrawable(R.mipmap.search);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new MXSettingsFragment();
            case 1: return new MXChatFragment();
            case 2: return new MXSearchFragment();
            default:
                return new Fragment();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        SpannableStringBuilder sb = new SpannableStringBuilder(" ");
        Drawable drawable;
        switch (position) {
            case 0:
                drawable = settings;
                break;
            case 1:
                drawable = chat;
                break;
            case 2:
                drawable = search;
                break;
            default:
                drawable = icon;
                break;
        }
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
        sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return sb;
    }

    @Override
    public int getCount() {
        return 3;
    }

}