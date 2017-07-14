package com.medx.android.utils.controller;

import android.content.Intent;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.medx.android.App;
import com.medx.android.aws.AWSConstants;
import com.medx.android.aws.AmazonUtil;
import com.medx.android.classes.services.ChatService;
import com.medx.android.interfaces.CompletionListener;
import com.medx.android.models.chat.MXMessage;
import com.medx.android.models.chat.db.sqlite.MXMessageDBHelper;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.chat.MXMessageUtil;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by alexey on 10/14/16.
 */

public class UploadController {

    String sourceFilePath;
    JSONObject message_info;
    TransferUtility uploadRequest;

    public UploadController(String filePath, JSONObject message_info)   {
        this.sourceFilePath = filePath;
        this.message_info = message_info;
    }

    public void uploadImage()   {
        File file = new File(sourceFilePath);
        String key = AWSConstants.AWS_S3_BUCKET_TEMP_FOLDER + File.separator + AppUtils.getStringFromJSON(message_info, "filename");
        uploadRequest = AmazonUtil.getTransferUtility(App.DefaultContext);
        TransferObserver observer = uploadRequest.upload(AWSConstants.AWS_S3_BUCKET, key,
                file, CannedAccessControlList.Private);

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.equals(TransferState.COMPLETED))
                {
                    uploadFileToS3AtPath();
                } else if (state.equals(TransferState.IN_PROGRESS)) {

                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentProgress = (int) (((float) bytesCurrent / (float) bytesTotal) * 100.f);
                Intent intent = new Intent("progress_upload");
                intent.putExtra("percent", percentProgress);
                intent.putExtra("app_message_id", AppUtils.getStringFromJSON(message_info, "app_message_id"));
                App.DefaultContext.sendBroadcast(intent);
            }

            @Override
            public void onError(int id, Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void uploadFileToS3AtPath()  {
        ChatService.instance.transferPhotoForMessage(message_info, new CompletionListener() {
            @Override
            public void complete(boolean success, String errorStatus) {
                if (success)    {
                    MXMessage message = MXMessageDBHelper.getInstance().getMessage(AppUtils.getStringFromJSON(message_info, "app_message_id"));
                    message_info = MXMessageUtil.dictionaryWithValuesFromMessage(message);
                    Intent intent = new Intent("upload");
                    intent.putExtra("app_message_id", AppUtils.getStringFromJSON(message_info, "app_message_id"));
                    intent.putExtra("message", message_info.toString());
                    App.DefaultContext.sendBroadcast(intent);
                }
            }
        });
    }
}
