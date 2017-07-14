package com.medx.android.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.medx.android.R;
import com.medx.android.adapters.WalkthroughAdapter;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.WalkthroughHelper;
import com.medx.android.utils.chat.MXUserUtil;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by alexey on 9/19/16.
 */

public class MXIntroActivity extends MXBaseActivity {

    /**
     * Properties field
     */

    @Bind(R.id.signIn)
    TextView signIn;
    @Bind(R.id.register)
    TextView register;
    @Bind(R.id.walkthrough)
    ViewPager walkthroughPager;
    @Bind(R.id.circles)
    CirclePageIndicator circlePageIndicator;
    private final int CLOSE_CODE = 100;

    /**
     * Init methods
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        if (Build.VERSION.SDK_INT > 9)  {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        initWalkthrough();
        // signIn.setTextColor(ContextCompat.getColor(this, R.color.white));
        register.setTextColor(ContextCompat.getColor(this, R.color.white));
        //throw new RuntimeException("test crash!!!");

        checkStatus();
    }

    private void initWalkthrough(){
        List<Fragment> fragmentList = WalkthroughHelper.getFragments(this);
        WalkthroughAdapter walkthroughAdapter = new WalkthroughAdapter(getSupportFragmentManager(), fragmentList);
        walkthroughPager.setAdapter(walkthroughAdapter);
        circlePageIndicator.setViewPager(walkthroughPager);
    }

    private void checkStatus()  {
        long timeStamp = System.currentTimeMillis() / 1000;

        MedXUser currentUser = MedXUser.CurrentUser();

        if (MXUserUtil.checkUserInfoExistsFromUserDefaults())   {
            long lastLogin = MXUserUtil.getLastLoginFromUserDefaults().getTime() / 1000;

            if (timeStamp - lastLogin < MXUserUtil.getLoginExpirePeriodInSeconds()) {
                currentUser.setUserInfo(MXUserUtil.getUserInfoFromUserDefaults());
                pushView(new Intent(MXIntroActivity.this, MXRootViewActivity.class));
            } else {
                MXUserUtil.removeUserParamsFromUserDefaults();
            }
        }
    }

    /**
     * Button events
     */

    @OnClick(R.id.signIn)
    void register(View view) {
        // finish();
        Intent intent = new Intent(this, MXLoginActivity.class);
        pushView(intent);
    }

    @OnClick(R.id.register)
    void signIn(View view) {
        //  finish();
        Intent intent = new Intent(this, MXVerifyRegCodeActivity.class);
        startActivity(intent);
//        startActivityForResult(intent, CLOSE_CODE);
    }
}
