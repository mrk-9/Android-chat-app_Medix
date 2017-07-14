package com.medx.android.aws;

public class ProgressFile {
    private String appMessageId;
    private int progress;

    public String getAppMessageId() {
        return appMessageId;
    }

    public void setAppMessageId(String appMessageId) {
        this.appMessageId = appMessageId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
