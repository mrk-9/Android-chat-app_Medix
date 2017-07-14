package com.medx.android.utils.controller;

import android.content.Intent;
import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.medx.android.App;
import com.medx.android.aws.AWSConstants;
import com.medx.android.aws.AmazonUtil;
import com.medx.android.classes.services.ChatService;
import com.medx.android.interfaces.CompletionListener;
import com.medx.android.interfaces.MessageSaveCompletionListener;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.chat.MXMessageUtil;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by alexey on 10/14/16.
 */

public class DownloadController {

    String sourceFilePath;
    JSONObject message_info;

    public DownloadController(String sourceFilePath, JSONObject message_info)   {
        this.sourceFilePath = sourceFilePath;
        this.message_info = message_info;
    }

    public void downloadImage() {
//        new DownloadHelper(sourceFilePath).execute();

        TransferUtility transferUtility = AmazonUtil.getTransferUtility(App.DefaultContext);
        TransferObserver observer = transferUtility.download(
                AWSConstants.AWS_S3_BUCKET,     /* The bucket to download from */
                AppUtils.awsDownloadRequestKeyByFileUrl(AppUtils.getStringFromJSON(message_info, "url")),    /* The key for the object to download */
                new File(sourceFilePath)        /* The file to download the object to */
        );

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.equals(TransferState.COMPLETED))
                {
                    downloadAtFilePath(sourceFilePath);
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
                Intent intent = new Intent("download");
                intent.putExtra("file_size", 0);
                intent.putExtra("app_message_id", AppUtils.getStringFromJSON(message_info, "app_message_id"));
                App.DefaultContext.sendBroadcast(intent);
            }
        });

    }

    class DownloadHelper extends AsyncTask<Void, Void, Void> {

        String filePath;

        DownloadHelper(String path) {
            filePath = path;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream is = null;
                String imagePath = AppUtils.getStringFromJSON(message_info, "url");
                URL u = new URL(imagePath);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                int total = c.getContentLength();
                try {
                    is = c.getInputStream();
                    byte[] byteChunk = new byte[1024]; // Or whatever size you want to read in at a time.
                    int n;
                    int currentBytes = 0;
                    while ((n = is.read(byteChunk)) > 0) {
                        currentBytes += n;
                        int percentProgress = (int) (((float) currentBytes / (float) total) * 100.f);

                        Intent intent = new Intent("progress_upload");
                        intent.putExtra("percent", percentProgress);
                        intent.putExtra("app_message_id", AppUtils.getStringFromJSON(message_info, "app_message_id"));
                        App.DefaultContext.sendBroadcast(intent);

                        baos.write(byteChunk, 0, n);
                    }
                } catch (IOException e) {
                    System.out.println("ChatAdapterRx.subscriber get image IOE = " + e.getMessage());
                    e.printStackTrace();
                    // Perform any other exception handling that's appropriate.
                } catch (Exception e) {
                    System.out.println("ChatAdapterRx.subscriber get image e = " + e);
                    e.printStackTrace();
                } finally {
                    System.out.println("ChatAdapterRx.subscriber finally get image = " + baos);
                    if (is != null) {
                        is.close();
                    }
                    c.disconnect();
                }
                System.out.println("ChatAdapterRx.subscriber baos = " + baos);
                byte[] encrypted = baos.toByteArray();
                FileUtils.writeByteArrayToFile(new File(filePath), encrypted);
            } catch (IOException e) {
                System.out.println("ChatAdapterRx.subscriber after ioe = " + e);
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("ChatAdapterRx.subscriber after e = " + e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            int file_size = Integer.parseInt(String.valueOf(new File(filePath).length()/1024));
            if (file_size > 0)
                downloadAtFilePath(filePath);
            else {

            }
        }
    }

    private void downloadAtFilePath(String filePath)    {
        String status_read = "2";
        ArrayList<String> ids = new ArrayList<>();
        ids.add(AppUtils.getStringFromJSON(message_info, "app_message_id"));

        ChatService.instance.updateReceivedMessageStatus(status_read, ids, new CompletionListener() {
            @Override
            public void complete(boolean success, String errorStatus) {
                if (success)    {
                    String fileName = MXMessageUtil.imageFileNameByAppMessageId(AppUtils.getStringFromJSON(message_info, "app_message_id"));
                    // Moves the donwload file from temporary path to the destination path
                    String toPath = AppUtils.imagePathWithFilName(fileName);
                    try {
                        AppUtils.copy(new File(filePath), new File(toPath));
                        new File(filePath).delete();
                        //Prevents the file to be backend up.
                        AppUtils.addSkipBackupAttributeToItemAtPath(toPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    JSONObject info = new JSONObject();
                    AppUtils.setJSONObjectWithObject(info, "app_message_id", AppUtils.getStringFromJSON(message_info, "app_message_id"));
                    AppUtils.setJSONObjectWithObject(info, "type", 1);
                    AppUtils.setJSONObjectWithObject(info, "filename", fileName);
                    AppUtils.setJSONObjectWithObject(info, "url", "");
                    AppUtils.setJSONObjectWithObject(info, "status", 2);

                    MXMessageUtil.saveMessageByInfo(info, null, new MessageSaveCompletionListener() {
                        @Override
                        public void complete(String app_message_id, String sharedKeyString, Error error) {
                            if (error == null)  {
                                AppUtils.setJSONObjectWithObject(message_info, "filename", fileName);
                                AppUtils.setJSONObjectWithObject(message_info, "status", 2);
                                AppUtils.setJSONObjectWithObject(message_info, "url", "");
                                Intent intent = new Intent("download");
                                intent.putExtra("file_size", 100);
                                intent.putExtra("app_message_id", AppUtils.getStringFromJSON(message_info, "app_message_id"));
                                intent.putExtra("message", message_info.toString());
                                App.DefaultContext.sendBroadcast(intent);
                            }
                        }
                    });
                }
            }
        });
    }
}
