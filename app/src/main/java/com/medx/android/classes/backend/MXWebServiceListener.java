package com.medx.android.classes.backend;

import org.json.JSONObject;

/**
 * Created by alexey on 9/8/16.
 */

public interface MXWebServiceListener {
    public abstract void onSuccess(JSONObject result);
    public abstract void onFailed(JSONObject erroreResult);
}
