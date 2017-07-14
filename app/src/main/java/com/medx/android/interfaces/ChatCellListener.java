package com.medx.android.interfaces;

import org.json.JSONObject;

/**
 * Created by alexey on 9/28/16.
 */

public interface ChatCellListener {
    public abstract void chatCellDidUpdateMessageInfo(JSONObject message_info);
}
