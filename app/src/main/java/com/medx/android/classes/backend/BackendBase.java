package com.medx.android.classes.backend;

import android.content.Context;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.medx.android.App;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by alexey on 9/8/16.
 */

public class BackendBase {
    /** properties field */
    private String url;
    private JSONObject params;
    private MXWebServiceListener listener;

    static BackendBase sharedConnection;
    static BackendBase sharedConnection1;
    /** Init methods */

    public static BackendBase newSharedConnection1()    {
        if (sharedConnection == null)
            sharedConnection = new BackendBase();
        return new BackendBase();
    }
    public static BackendBase newSharedConnection() {
        if (sharedConnection == null)
            sharedConnection = new BackendBase();
        return new BackendBase();
    }

    public static BackendBase newInstance() {
        if (sharedConnection == null)
            sharedConnection = new BackendBase();
        return new BackendBase();
    }

    public void accessAPIbyPostWithSync(Context context, String apiKey, RequestParams object, MXWebServiceListener webServiceListener)    {
        this.url = apiKey;
        this.listener = webServiceListener;

        SyncHttpClient client  = new SyncHttpClient();
        client.post(context, ApiURLs.BASE_URL + url, object, new JsonHttpResponseHandler()   {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject result) {
                if (isRemoteWipeSetWithResponse(result))
                    doRemoteWipeWithResponse(result);
                else if (isSessionExpiredWithResponse(result))
                    App.getInstance().logout();
                else
                    listener.onSuccess(result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                listener.onFailed(errorResponse);
            }
        });
    }

    public void accessPublicAPIbyPost(Context context, String apiKey, JSONObject params, MXWebServiceListener webServiceListener)   {

        listener = webServiceListener;
        AndroidNetworking.post(ApiURLs.PUBLIC_URL + apiKey).addJSONObjectBody(params).setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject result) {
                if (isRemoteWipeSetWithResponse(result))
                    doRemoteWipeWithResponse(result);
                else if (isSessionExpiredWithResponse(result))
                    App.getInstance().logout();
                else
                    listener.onSuccess(result);
            }

            @Override
            public void onError(ANError anError) {
                JSONObject errorResponse = new JSONObject();
                listener.onFailed(errorResponse);
            }
        });
    }

    public void accessAPIFastGET(String apiPath, JSONObject mParams, MXWebServiceListener webServiceListener)   {
        listener = webServiceListener;
        ANRequest.GetRequestBuilder builder = AndroidNetworking.get(ApiURLs.BASE_URL + apiPath).setPriority(Priority.MEDIUM);
        JSONArray names = mParams.names();

        for (int i = 0; i < names.length(); i ++)   {
            String name = AppUtils.getStringFromJSONArray(names, i);
            builder.addQueryParameter(name, AppUtils.getStringFromJSON(mParams, name));
        }

        builder.build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject result) {
                if (isRemoteWipeSetWithResponse(result))
                    doRemoteWipeWithResponse(result);
                else if (isSessionExpiredWithResponse(result))
                    App.getInstance().logout();
                else
                    listener.onSuccess(result);
            }

            @Override
            public void onError(ANError anError) {
                JSONObject errorResponse = new JSONObject();
                listener.onFailed(errorResponse);
            }
        });
    }

    public void accessAPIFastPOST(String apiPath, JSONObject mParams, MXWebServiceListener webServiceListener)   {
        listener = webServiceListener;
        AndroidNetworking.post(ApiURLs.BASE_URL + apiPath).addJSONObjectBody(mParams).setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject result) {
                if (isRemoteWipeSetWithResponse(result))
                    doRemoteWipeWithResponse(result);
                else if (isSessionExpiredWithResponse(result))
                    App.getInstance().logout();
                else
                    listener.onSuccess(result);
            }

            @Override
            public void onError(ANError anError) {
                JSONObject errorResponse = new JSONObject();
                listener.onFailed(errorResponse);
            }
        });
    }

    /**
     * Check remote wipe & invalid token status
     */

    private boolean isRemoteWipeSetWithResponse(JSONObject response)   {
        if (AppUtils.isNotEmptyString(AppUtils.getStringFromJSON(response, "wipe"))) {
            boolean isRemoteWipeSet = Integer.parseInt(AppUtils.getStringFromJSON(response, "wipe")) == 1;
            return isRemoteWipeSet;
        } else
            return false;
    }

    private void doRemoteWipeWithResponse(JSONObject response)  {
        String token = null;
        if (MedXUser.CurrentUser() != null && MedXUser.CurrentUser().accessToken() != null)    {
            token = MedXUser.CurrentUser().accessToken();
        } else if (AppUtils.getJSONFromJSON(response, "user") != null && AppUtils.isNotEmptyString(AppUtils.getStringFromJSON(AppUtils.getJSONFromJSON(response, "user"), "access_token"))){
            token = AppUtils.getStringFromJSON(AppUtils.getJSONFromJSON(response, "user"), "access_token");
        }

        App.getInstance().wipe(token);
    }

    private boolean isSessionExpiredWithResponse(JSONObject response)   {
        boolean isExpired = AppUtils.getStringFromJSON(response, "response").equals("fail") && AppUtils.getStringFromJSON(response, "status").equals("invalid_token");
        return isExpired;
    }
}
