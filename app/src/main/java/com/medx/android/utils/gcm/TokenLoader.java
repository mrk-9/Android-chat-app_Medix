package com.medx.android.utils.gcm;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.medx.android.R;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.chat.MXUserUtil;
import com.medx.android.utils.views.MXDialogManager;

import java.io.IOException;

public class TokenLoader extends AsyncTask<Void, Void, Object> {
    Context context;

    public TokenLoader(Context context) {
        this.context = context;
    }

    @Override
    protected Object doInBackground(Void... params) {
        Exception exception = null;
        String token = "";
        try {
            InstanceID instanceID = InstanceID.getInstance(context);
            token = instanceID.getToken(context.getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        } catch (IOException e) {
            e.printStackTrace();
            exception = e;
        }
        return exception == null ? token : exception;
    }

    @Override
    protected void onPostExecute(final Object object) {
        super.onPostExecute(object);
        if (object instanceof Exception) {
            MXDialogManager.showSimpleDialog(context, context.getString(R.string.error), String.valueOf(((Exception) object).getMessage()));
        } else {
            String token = (String) object;
            if (MedXUser.CurrentUser().checkUserLoggedIn())
                MedXUser.CurrentUser().registerDeviceToken(token, null);
            MXUserUtil.updateUserDefaultswithDeviceToken(token);
        }
    }
}
