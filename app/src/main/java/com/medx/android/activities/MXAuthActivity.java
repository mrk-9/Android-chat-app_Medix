package com.medx.android.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;

import com.medx.android.App;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.chat.MXUserUtil;

import java.security.KeyPair;
import java.util.Date;

/**
 * Created by alexey on 9/19/16.
 */

public class MXAuthActivity extends MXBaseActivity {

    /**
     * properties
     */

    public ProgressDialog dialog;

    /**
     * manage progress bar
     */

    public void showProgress(String message)  {
        dialog = ProgressDialog.show(this, "", message, true);
    }

    public void hideProgress()  {
        if (dialog != null)
            dialog.dismiss();
    }

    /**
     * Utility methods
     */

    public void showSimpleMessage(String title, String message, String okBtTitle, DialogInterface.OnClickListener listener)   {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, okBtTitle, listener != null ? listener : new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void showConfirmmessage(String message, String okBtTitle, DialogInterface.OnClickListener listener)  {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, okBtTitle, listener);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    /**
     * check network status
     */

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    /**
     * Authenticate
     */

    public void doAfterSignInWithKeyPair(KeyPair keyPair)   {
        MXUserUtil.updateUserDefaults(MedXUser.CurrentUser().info, new Date());

        if (keyPair != null)    {
            MXUserUtil.saveKeyPair(keyPair);
            MedXUser.CurrentUser().setupKeys();
        }
    }

    /**
     * Logout/Wipe methods
     */

    public void logout()    {
        App.getInstance().logout();
    }

    @SuppressWarnings("deprecation")
    private void Notify(String notificationTitle, String notificationMessage) {
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        @SuppressWarnings("deprecation")
//        Notification notification = new Notification(R.mipmap.ic_launcher,
//                "New Message", System.currentTimeMillis());
//
//        Intent notificationIntent = new Intent(this, MXRootViewActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//                notificationIntent, 0);
//
//        notification.setLatestEventInfo(MainActivity.this, notificationTitle,
//                notificationMessage, pendingIntent);
//        notificationManager.notify(9999, notification);
    }


}
