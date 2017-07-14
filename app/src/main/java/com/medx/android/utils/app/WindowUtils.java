package com.medx.android.utils.app;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by alexey on 9/29/16.
 */

public class WindowUtils {

    static WindowUtils insctance;
    Context context;
    DisplayMetrics displayMetrics;

    public WindowUtils(Context context) {
        this.context = context;
        displayMetrics = context.getResources().getDisplayMetrics();
    }

    public static WindowUtils getInstance() {
        return insctance;
    }

    public static WindowUtils newInstance(Context context) {
        insctance = new WindowUtils(context);
        return insctance;
    }

    public int dpToPx(int dp) {
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public int spToPx(float sp) {
        int px = Math.round(sp * displayMetrics.scaledDensity);
        return px;
    }

    public int getWidth() {
        return displayMetrics.widthPixels;
    }

    public int getHeight() {
        return displayMetrics.heightPixels;
    }

}