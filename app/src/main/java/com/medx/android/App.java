package com.medx.android;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.androidnetworking.AndroidNetworking;
import com.crashlytics.android.Crashlytics;
import com.medx.android.activities.MXChatActivity;
import com.medx.android.activities.MXRootViewActivity;
import com.medx.android.classes.backend.ApiURLs;
import com.medx.android.classes.backend.BackendBase;
import com.medx.android.classes.backend.MXWebServiceListener;
import com.medx.android.classes.services.ChatService;
import com.medx.android.models.chat.db.DatabaseHelper;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.app.WindowUtils;
import com.medx.android.utils.chat.MXUserUtil;

import org.json.JSONObject;

import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Created by alexey on 9/6/16.
 */

public class App extends Application {

    public static Context DefaultContext;
    static App app;

    public static App getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        Fabric.with(this, new Crashlytics());
        DefaultContext = getApplicationContext();
        DatabaseHelper.newDBInstance(getApplicationContext());
        WindowUtils.newInstance(getApplicationContext());
        AndroidNetworking.initialize(this);
        app = this;
    }

    public void logout()    {

        MXUserUtil.removeUserParamsFromUserDefaults();
        ChatService.dialogRecipientId = null;
        ChatService.dialogViewName = null;
        MedXUser.CurrentUser().unset();

        if (isRunning(this, MXRootViewActivity.class)) {
            MXRootViewActivity.getInstance().finish();
        } else
        if (isRunning(this, MXChatActivity.class)) {
            MXChatActivity.getInstance().finish();
        }
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

    public void wipe(String access_token)  {
        if (AppUtils.isEmptyString(access_token))
            return;
        JSONObject params = new JSONObject();
        AppUtils.setJSONObjectWithObject(params, "token", access_token);

        BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_USERS_UNSET_WIPE, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                String response = AppUtils.getStringFromJSON(result, "response");
                if (response.equals("success")) {
                    logout();
                }
            }

            @Override
            public void onFailed(JSONObject erroreResult) {

            }
        });
    }
}
