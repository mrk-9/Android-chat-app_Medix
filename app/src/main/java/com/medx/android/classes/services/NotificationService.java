package com.medx.android.classes.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.medx.android.R;
import com.medx.android.activities.MXRootViewActivity;

/**
 * Created by alexey on 10/21/16.
 */

public class NotificationService extends Service{

    private static final String TAG = "MyGcmListenerService";
    public static final String ACTIONS_CHANGE_BASE = "com.medx.android.action.CHANGE_BASE";

    Notification getMainNotification(String title) {
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MXRootViewActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[]{50, 50, 50, 50, 50, 50, 50, 50, 50, 1000})
                .build();
        return notification;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("status", "started");
        if (intent == null)
            return super.onStartCommand(intent, flags, startId);;

        String type = intent.getStringExtra("type") != null ? intent.getStringExtra("type") : "";
        String title = intent.getStringExtra("title") != null ? intent.getStringExtra("title") : "";
        int badge = intent.getIntExtra("badge", 0);
        // Remote Notification For New Message

        if (type.equals("N"))   {
            String dialogViewName = ChatService.dialogViewName;

            if (dialogViewName == null) {
                Notification notification = getMainNotification(title);
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(badge, notification);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
}
