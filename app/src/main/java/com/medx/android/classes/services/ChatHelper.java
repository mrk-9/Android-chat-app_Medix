package com.medx.android.classes.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.medx.android.App;
import com.medx.android.classes.backend.ApiURLs;
import com.medx.android.classes.backend.BackendBase;
import com.medx.android.classes.backend.MXWebServiceListener;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.controller.ChatHistoryLoadController;
import com.medx.android.utils.controller.DownloadController;
import com.medx.android.utils.controller.UploadController;

import org.json.JSONObject;

/**
 * Created by alexey on 10/14/16.
 */

public class ChatHelper extends Service {
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
        if (intent != null && intent.hasExtra("type")){
            // get uri of selected file
            String type = intent.getStringExtra("type");

            String filePath = intent.getStringExtra("filePath");
            JSONObject message_info = AppUtils.getJSONFromString(intent.getStringExtra("message"));
            if(type.equals("upload"))
                new UploadController(filePath, message_info).uploadImage();
            else if (type.equals("download"))
                new DownloadController(filePath, message_info).downloadImage();
        } else if (intent != null && intent.hasExtra("mode") && intent.getStringExtra("mode").equals("alert"))  {
            Intent intent1 = new Intent("alert");
            String text = intent.getStringExtra("message");
            intent1.putExtra("message", text);
            App.DefaultContext.sendBroadcast(intent1);
        } else if (intent != null && intent.hasExtra("mode") && intent.getStringExtra("mode").equals("resetUser"))  {
            JSONObject params = new JSONObject();
            AppUtils.setJSONObjectWithObject(params, "token", MedXUser.CurrentUser().accessToken());
            AppUtils.setJSONObjectWithObject(params, "dialog_user_id", "0");

            BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_UPDATE_DIALOG, params, new MXWebServiceListener() {
                @Override
                public void onSuccess(JSONObject result) {

                }

                @Override
                public void onFailed(JSONObject erroreResult) {

                }
            });
        } else if (intent != null && intent.hasExtra("mode") && intent.getStringExtra("mode").equals("loadHistory")){
            String user_id = intent.getStringExtra("user_id");
            new ChatHistoryLoadController(user_id).loadData();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
