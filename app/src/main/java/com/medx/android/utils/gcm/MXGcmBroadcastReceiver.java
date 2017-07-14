package com.medx.android.utils.gcm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmReceiver;
import com.medx.android.App;
import com.medx.android.classes.services.ChatService;
import com.medx.android.classes.services.NotificationService;
import com.medx.android.utils.app.AppUtils;

import java.util.Iterator;
import java.util.Set;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by alexey on 10/21/16.
 */

public class MXGcmBroadcastReceiver extends GcmReceiver {

    private static final String TAG = "MyGcmListenerService";

    @Override
    public void onReceive(Context context, Intent intent1) {
        Bundle bundle = intent1.getExtras();
        bundle.keySet();
        Set<String> keySet = bundle.keySet();
        if(keySet != null && keySet.isEmpty() == false) {
            Iterator<String> it = keySet.iterator();
            int i = 0;
            while(it.hasNext()){
                String  key = it.next();
                String  desc = bundle.getString(key);
                Log.d("BroadCast Values",key +"  "+desc);
            }
        }
        Log.d("", "In Receive Method of Broadcast Receiver");

        if (bundle != null) {

            String type = bundle.getString("type");

            Log.d(TAG, "received");
            String title = bundle.getString("gcm.notification.title");

            if (AppUtils.isEmptyobject(type))
                return;

            // Remote Notification For New Message

            String dialogViewName = ChatService.dialogViewName;
            String dialogRecipientId = ChatService.dialogRecipientId;

            if (type.equals("N"))   {
                String badgeSt = bundle.getString("badge");
                int badge = AppUtils.isNotEmptyString(badgeSt) ? Integer.parseInt(bundle.getString("badge")) : 0;
                ShortcutBadger.applyCount(App.DefaultContext, badge);
                if (AppUtils.isEmptyobject(dialogRecipientId) && AppUtils.isEmptyobject(dialogViewName)) {
                    Log.d("status", "closed");
                    Intent serviceIntent = new Intent(context, NotificationService.class);
                    serviceIntent.putExtra("title", title);
                    serviceIntent.putExtra("badge", badge);
                    serviceIntent.putExtra("type", type);
                    context.startService(serviceIntent);
                } else {
                    super.onReceive(context, intent1);
                }
            } else {
                if (dialogViewName != null) {
                    Intent intent = new Intent(App.DefaultContext, ChatService.class);
                    intent.putExtra("controller", "root");
                    App.DefaultContext.startService(intent);
                }
            }
        }
    }
}
