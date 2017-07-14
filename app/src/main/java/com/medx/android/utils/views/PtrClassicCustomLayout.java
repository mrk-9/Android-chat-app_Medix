package com.medx.android.utils.views;

import android.content.Context;
import android.util.AttributeSet;

import com.chanven.lib.cptr.PtrFrameLayout;

/**
 * Created by alexey on 9/23/16.
 */

public class PtrClassicCustomLayout extends PtrFrameLayout {

    private ClassicHeader mPtrClassicHeader;

    public PtrClassicCustomLayout(Context context) {
        super(context);
        initViews();
    }

    public PtrClassicCustomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public PtrClassicCustomLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    private void initViews() {
        mPtrClassicHeader = new ClassicHeader(getContext());
        setHeaderView(mPtrClassicHeader);
        addPtrUIHandler(mPtrClassicHeader);
    }

    public ClassicHeader getHeader() {
        return mPtrClassicHeader;
    }

    /**
     * Specify the last update time by this key string
     *
     * @param key
     */
    public void setLastUpdateTimeKey(String key) {
        if (mPtrClassicHeader != null) {
            mPtrClassicHeader.setLastUpdateTimeKey(key);
        }
    }

    /**
     * Using an object to specify the last update time.
     *
     * @param object
     */
    public void setLastUpdateTimeRelateObject(Object object) {
        if (mPtrClassicHeader != null) {
            mPtrClassicHeader.setLastUpdateTimeRelateObject(object);
        }
    }
}