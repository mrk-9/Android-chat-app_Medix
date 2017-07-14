package com.medx.android.utils.gcm;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.medx.android.App;
import com.medx.android.R;
import com.medx.android.activities.MXChatActivity;
import com.medx.android.activities.MXDirectoryProfileActivity;
import com.medx.android.activities.MXRootViewActivity;
import com.medx.android.classes.services.ChatService;
import com.medx.android.utils.app.AppUtils;

import java.util.List;

public class MXGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    public static final String ACTIONS_CHANGE_BASE = "com.medx.android.action.CHANGE_BASE";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Bundle notificationBundle = data.getBundle("notification");
        String type = data.getString("type");
        // check Chat Activity is running
        boolean isChatRunning = isRunning(this, MXChatActivity.class);
        boolean isMainRunning = isRunning(this, MXRootViewActivity.class);
        boolean isDoctorRunning = isRunning(this, MXDirectoryProfileActivity.class);

        System.out.println("MXGcmListenerService.onMessageReceived = " + bundle2string(notificationBundle));
        Log.d(TAG, "received");
        String title = notificationBundle.getString("title");

        if (AppUtils.isEmptyobject(type))
            return;

        // Remote Notification For New Message

        if (type.equals("N"))   {
            String senderId = data.getString("senderId");
            String dialogRecipientId = ChatService.dialogRecipientId;
            String dialogViewName = ChatService.dialogViewName;
            boolean bHasToShowCurtainNotification = false;
            boolean bHasToCheckNewMessageFromServer = true;

            Notification notification = null;
            if (dialogViewName.equals("INDEX")) {
                notification = getMainNotification(title);
            } else if (dialogViewName.equals("SETTINGS")){
                bHasToShowCurtainNotification = true;
                notification = getMainNotification(title);
            } else if (dialogViewName.equals("CHAT"))   {
                notification = getChatNotification(title);
                if (senderId.equals(dialogRecipientId))
                    bHasToCheckNewMessageFromServer = false;
                else
                    bHasToShowCurtainNotification = false;
            } else
                notification = getMainNotification(title);

            if ((bHasToShowCurtainNotification & bHasToCheckNewMessageFromServer) || isDoctorRunning) {
                Intent intent1 = new Intent("alert");
                intent1.putExtra("message", title);
                App.DefaultContext.sendBroadcast(intent1);
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(1, notification);
            }

            if (bHasToCheckNewMessageFromServer & !bHasToShowCurtainNotification & !isChatRunning) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(1, notification);
            }

            if (bHasToCheckNewMessageFromServer) {
                Intent intent = new Intent(this, ChatService.class);
                intent.putExtra("controller", "root");
                App.DefaultContext.startService(intent);
            }
        }
    }

    Notification getChatNotification(String title) {
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MXChatActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[]{50, 50, 50, 50, 50, 50, 50, 50, 50, 1000})
                .build();
        return notification;
    }

    Notification getMainNotification(String title) {
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MXRootViewActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .setVibrate(new long[]{50, 50, 50, 50, 50, 50, 50, 50, 50, 1000})
                .build();
        return notification;
    }

    public boolean isRunning(Context ctx, Class className) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (task.topActivity.getClassName().equals(className.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }

    public static String bundle2string(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        String string = "Bundle{";
        for (String key : bundle.keySet()) {
            string += " " + key + " => " + bundle.get(key) + ";";
        }
        string += " }Bundle";
        return string;
    }

}