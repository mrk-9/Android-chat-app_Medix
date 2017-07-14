package com.medx.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.medx.android.R;
import com.medx.android.adapters.MainPagerAdapter;
import com.medx.android.classes.notification.anim.NotificationOpenCloseAnimation;
import com.medx.android.classes.notification.timer.ActionTimer;
import com.medx.android.classes.notification.timer.ActionTimerListener;
import com.medx.android.classes.services.ChatService;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.app.Utils;
import com.medx.android.utils.app.WindowUtils;
import com.medx.android.utils.gcm.TokenLoader;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by alexey on 9/19/16.
 */

public class MXRootViewActivity extends MXAuthActivity implements ViewPager.OnPageChangeListener, View.OnClickListener{

    @Bind(R.id.pager)

    ViewPager pager;

    @Bind(R.id.logout)
    TextView logout;

    @Bind(R.id.pagerTitle)
    PagerTitleStrip titles;

    @Bind(R.id.notification_bar)
    RelativeLayout notificationView;

    @Bind(R.id.not_text)
    TextView notificationText;

    @Bind(R.id.not_title)
    TextView notificationTitle;

    boolean isEnableAlert;

    private ActionTimer notificationTimer = new ActionTimer();

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "alert":
                    String text = intent.getStringExtra("message");
                    if (isEnableAlert)
                        postTimedNotification(text);
                    break;
            }

        }
    };

    private final String TAG = "MainActivity";

    static MXRootViewActivity instance;
    public static MXRootViewActivity getInstance()  {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        instance = this;
        logout.setTextColor(ContextCompat.getColor(this, R.color.white));

        pager.setAdapter(new MainPagerAdapter(this, getSupportFragmentManager()));
        pager.setCurrentItem(1);
        ChatService.dialogViewName = "INDEX";
        pager.addOnPageChangeListener(this);
        addListeners(titles);
        new TokenLoader(getApplicationContext()).execute();

        notificationTitle.setText("New Message");

        AppUtils.createImagesDirectory();

        notificationTimer.addListener(new ActionTimerListener() {
            @Override
            public void actionTimerCompleted() {
                Handler mainHandler = new Handler(getMainLooper());
                Runnable post = new Runnable() {

                    @Override
                    public void run() {
                        if(notificationView.getVisibility() == View.VISIBLE)
                            hideNotification();
                    }
                };

                mainHandler.post(post);
            }
        });

        notificationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideNotification();
            }
        });
    }

    void addListeners(View view) {
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View child = ((ViewGroup) view).getChildAt(i);
                child.setOnClickListener(this);
                addListeners(child);
            }
        }
    }

    @OnClick(R.id.logout)
    void onClickLogout(View view) {
        logout();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position == 0 && positionOffset > 0.25)
            logout.setVisibility(View.GONE);
        else if (position == 0 && positionOffset < 0.28)
            logout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "Page selected: " + String.valueOf(position));
        if (position == 0 || position == 2)
            ChatService.dialogViewName = "SETTINGS";
        else if (position == 1)
            ChatService.dialogViewName = "INDEX";

        hideKeyboard();

        if (position == 1)
            Utils.isDataLoaded = false;

        if (position == 1)
            isEnableAlert = false;
        else
            isEnableAlert = true;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }


    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            int width = WindowUtils.getInstance().getWidth();
            int x = (int) v.getX();
            int center = width / 2;
            if (width - x > center) {
                if (x < Math.abs(center - x))
                    pager.setCurrentItem(pager.getCurrentItem() - 1, true);
            } else {
                if (x > Math.abs(width - x))
                    pager.setCurrentItem(pager.getCurrentItem() + 1, true);
            }
        }
    }


    @Override
    public void onResume()  {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("alert");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void hideNotification() {
        // check to be sure we have a notification view for this activity
        if (notificationView != null) {

            // initiate close animation if visible
            if(notificationView.getVisibility() == View.VISIBLE) {
                NotificationOpenCloseAnimation anim = new NotificationOpenCloseAnimation(
                        notificationView, 1000);
                notificationView.startAnimation(anim);
            }
        }
    }

    public void postNotification(String message, boolean closeable) {

        // check to be sure we have a notification view for this activity
        if (notificationView != null) {

            // change the notification message
            notificationText.setText(message);

            // initiate open animation if not visible
            if(notificationView.getVisibility() != View.VISIBLE) {
                NotificationOpenCloseAnimation anim = new NotificationOpenCloseAnimation(
                        notificationView, 10000);
                notificationView.startAnimation(anim);
            }
        }
    }

    public void postTimedNotification(String message) {
        if (notificationView != null) {

            // start delayed timer
            notificationTimer.startTimer();

            // show notification
            postNotification(message, false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        return;
    }
}
