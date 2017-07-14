package com.medx.android.utils.app;

import com.medx.android.models.chat.MXUser;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alexey on 9/27/16.
 */

public class Utils {
    public final static int kNotificationChatDidReceiveNewMessage = 1;
    public final static int kKnotificationChatDidReceiveDialogMessages = kNotificationChatDidReceiveNewMessage + 1;
    public final static int kNotificationChatDidNotDeliverMessage = kKnotificationChatDidReceiveDialogMessages + 1;
    public final static int kNotificationChatDidFailInCheckAllDialogs = kNotificationChatDidNotDeliverMessage + 1;

    public static JSONObject notificationData;

    public static ArrayList<String> ids;

    public static boolean isDataLoaded;

    public static ArrayList<MXUser> partners;
}
