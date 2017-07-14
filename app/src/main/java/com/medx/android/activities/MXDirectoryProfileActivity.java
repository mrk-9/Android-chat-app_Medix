package com.medx.android.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.medx.android.R;
import com.medx.android.classes.notification.anim.NotificationOpenCloseAnimation;
import com.medx.android.classes.notification.timer.ActionTimer;
import com.medx.android.classes.notification.timer.ActionTimerListener;
import com.medx.android.interfaces.CompletionListener;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.chat.MXUserUtil;

import org.json.JSONArray;
import org.json.JSONException;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by alexey on 9/23/16.
 */

public class MXDirectoryProfileActivity extends MXAuthActivity {

    public static final String TAG = "MXDoctorActivity";
    public static final String USER_ID = "user_id";
    public static final String FROM_CHAT = "from_chat";

    boolean fromChat;

    @Bind(R.id.appNoteInstalled)
    View appNoteInstalled;
    @Bind(R.id.specialty)
    TextView specialty;
    @Bind(R.id.about)
    TextView about;
    @Bind(R.id.offices)
    TextView offices;

    @Bind(R.id.send)
    TextView send;

    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.block)
    TextView block;

    @Bind(R.id.notification_bar)
    RelativeLayout notificationView;

    @Bind(R.id.not_text)
    TextView notificationText;

    @Bind(R.id.not_title)
    TextView notificationTitleProf;

    private ActionTimer notificationTimer = new ActionTimer();

    MXUser user;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "alert":
                    String text = intent.getStringExtra("message");
                    postTimedNotification(text);
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);


        notificationTitleProf.setText("New Message");

        title.setTextColor(ContextCompat.getColor(this, R.color.white));
        send.setTextColor(ContextCompat.getColor(this, R.color.white));
        block.setTextColor(ContextCompat.getColor(this, R.color.white));

        String user_id = getIntent().getStringExtra(USER_ID);
        user = MXUserUtil.findByUserId(user_id);

        fromChat = getIntent().getBooleanExtra(FROM_CHAT, false);

        if (fromChat)
            send.setVisibility(View.GONE);

        setupUI();
    }

    @OnClick(R.id.close)
    void onClickClose(View view) {
        finish();
    }

    @OnClick(R.id.send)
    void onClickSend(View view) {
        Intent intent = new Intent(view.getContext(), MXChatActivity.class);
        intent.putExtra(MXChatActivity.DATA, user.user_id);
        pushView(intent);
    }

    @OnClick(R.id.block)
    void onClickBlock(View view) {
        if (!MedXUser.CurrentUser().isBlockingUserId(user.user_id)) {
            showConfirmDialog();
        } else {
            doBlock();
        }
    }

    private void setupUI()  {
        title.setText(user.fullNameWithSalutation());

        specialty.setText(user.specialty);
        about.setText(user.about);

        if (AppUtils.isEmptyString(user.about)) {
            about.setText(getString(R.string.no_info));
            about.setTextColor(ContextCompat.getColor(this, R.color.gray));
        }

        boolean isInstalledApp = user.hasInstallApp();
        boolean isVerified = user.isVerified();

        if (isInstalledApp || isVerified)  {
            appNoteInstalled.setVisibility(View.GONE);
        } else
            appNoteInstalled.setVisibility(View.VISIBLE);

        boolean isBlocked = MedXUser.CurrentUser().isBlockingUserId(user.user_id);
        setupBlockButtonTitleByState(isBlocked);

        if (AppUtils.isNotEmptyString(user.locations))  {
            try {
                JSONArray locationList = new JSONArray(user.locations);
                String location = "";
                for (int i = 0; i < locationList.length(); i ++)    {
                    String location1 = AppUtils.getStringFromJSONArray(locationList, i);
                    location1 = MXUserUtil.refinOfficePhoneNumberInLocation(location1);
                    location1 = location1 + "\n\n";
                    location += location1;
                }

                offices.setText(location);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            offices.setText(getString(R.string.no_info));
            offices.setTextColor(ContextCompat.getColor(this, R.color.gray));
        }
    }

    private void setupBlockButtonTitleByState(boolean isBlocked)    {
        if (!isBlocked) {
            block.setText(getString(R.string.block_user));
        } else {
            block.setText(getString(R.string.unblock_user));
        }
    }


    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("")
                .setMessage(R.string.confirm_block_user)
                .setPositiveButton(R.string.block, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doBlock();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Block methods
     */

    private void doBlock()  {
        boolean isBock = !MedXUser.CurrentUser().isBlockingUserId(user.user_id);

        showProgress("Updating...");
        MedXUser.CurrentUser().blockOrUnblockUserById(user.user_id, isBock, new CompletionListener() {
            @Override
            public void complete(boolean success, String errorStatus) {
                if (success)    {
                    setupBlockButtonTitleByState(isBock);
                } else {
                    showSimpleMessage("", getString(R.string.coult_not_be_done_try_later), "OK", null);
                }

                hideProgress();
            }
        });
    }

    /**
     * Notification methods
     */

    @Override
    protected void onResume()  {
        super.onResume();
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
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}
