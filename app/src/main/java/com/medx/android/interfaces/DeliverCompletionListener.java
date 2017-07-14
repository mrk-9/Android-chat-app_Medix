package com.medx.android.interfaces;

/**
 * Created by alexey on 9/26/16.
 */

public interface DeliverCompletionListener {
    public abstract void complete(String app_message_id, int message_status);
}
