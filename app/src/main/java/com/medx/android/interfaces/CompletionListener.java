package com.medx.android.interfaces;

/**
 * Created by alexey on 9/21/16.
 */

public interface CompletionListener {
    public abstract void complete(boolean success, String errorStatus);
}
