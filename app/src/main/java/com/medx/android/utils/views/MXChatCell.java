package com.medx.android.utils.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.bumptech.glide.Glide;
import com.medx.android.R;
import com.medx.android.activities.FullSizeImageActivity;
import com.medx.android.activities.MXChatActivity;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.chat.EncryptionUtil;
import com.medx.android.utils.chat.MXMessageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

/**
 * Created by alexey on 9/28/16.
 */

public class MXChatCell extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView message;
    TextView date;
    ImageView image;
    View placeholder;
    View container;
    View readIndicator;
    CircleProgressView progressView;
    ImageView balloon;
    Activity activity;

    private View.OnClickListener mSpaceClickListener;
    JSONObject message_info;

    TransferUtility uploadRequest;
    int transferID;

    boolean sentMessage;
    boolean textMessage;

    MedXUser currentUser;

    String imagePath;

    public boolean hasInstance;

    public MXChatCell(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        image = (ImageView) itemView.findViewById(R.id.image);
        message = (TextView) itemView.findViewById(R.id.message);
        date = (TextView) itemView.findViewById(R.id.date);
        placeholder = itemView.findViewById(R.id.loadPlaceholder);
        container = itemView.findViewById(R.id.container);
        progressView = (CircleProgressView) itemView.findViewById(R.id.progressCircle);
        readIndicator = itemView.findViewById(R.id.read);
        balloon = (ImageView) itemView.findViewById(R.id.balloon);

    }

    public void initWithMessage(Activity mActivity, JSONObject message_info, View.OnClickListener mSpaceClickListener)   {

        try {
            this.message_info = new JSONObject(message_info.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.mSpaceClickListener = mSpaceClickListener;
        activity = mActivity;

        sentMessage = AppUtils.getStringFromJSON(message_info, "sender_id").equals(MedXUser.CurrentUser().userId());
        textMessage = Integer.parseInt(AppUtils.getStringFromJSON(message_info, "type")) == 0;
        currentUser = MedXUser.CurrentUser();

        if (sentMessage)
            balloon.setColorFilter(setBubble());

        if (Integer.parseInt(AppUtils.getStringFromJSON(message_info, "type")) == 1)    {
            container.setVisibility(View.VISIBLE);

            image.setVisibility(View.VISIBLE);
            message.setVisibility(View.GONE);

            if (MXMessageUtil.checkLocalImageExistsForMessage(message_info))    {
                showImage(MXMessageUtil.imageOfMessages(message_info));
            } else {
                image.setImageResource(0);
            }

            if (sentMessage && AppUtils.isEmptyString(AppUtils.getStringFromJSON(message_info, "status")))  {
                uploadAttachment();
            } else if (!sentMessage && AppUtils.isEmptyString(AppUtils.getStringFromJSON(message_info, "filename")) && AppUtils.isNotEmptyString(AppUtils.getStringFromJSON(message_info, "url"))) {
                image.setImageResource(0);
                downloadAttachmentFromServer();
            } else {
                progressView.setVisibility(View.GONE);
                progressView.reset();
            }

        } else {
            image.setVisibility(View.GONE);
            container.setVisibility(View.GONE);
            message.setVisibility(View.VISIBLE);
            String message_str = AppUtils.getStringFromJSON(message_info, "text");
            if (AppUtils.isEmptyString(message_str) || message_str.length() > 500) {
                message_str = "Message sent to difference device";
            }

            message.setText(message_str);
            message.setTextColor(textMessageColor(message_info));
        }

        date.setText(AppUtils.printDate(AppUtils.getStringFromJSON(message_info, "sent_at")));

        date.setTextColor(timeLabelColor(message_info));
        setReadStatus();

        hasInstance = true;

        setupCell(message_info);
    }
    public void setupCell(JSONObject message_info)  {

        int oldStatus = AppUtils.getStringFromJSON(message_info, "status").length() > 0 ? Integer.parseInt(AppUtils.getStringFromJSON(message_info, "status")) : 0;
        this.message_info = message_info;
        setReadStatus();

        if (AppUtils.isNotEmptyString(AppUtils.getStringFromJSON(message_info, "status")))  {
            if (Integer.parseInt(AppUtils.getStringFromJSON(message_info, "status")) == 100) {
                // Resets Bubble Image & Text Colors
                if (sentMessage)
                  balloon.setColorFilter(setBubble());
                message.setTextColor(textMessageColor(message_info));
                date.setTextColor(timeLabelColor(message_info));

                // Cancels image uploading
                if(uploadRequest != null)   {
                    uploadRequest.cancel(transferID);
                    uploadRequest = null;
                }
            } else {
                if (oldStatus == 100)   {
                    // Resets Bubble Image & Text Colors
                    if (sentMessage)
                        balloon.setColorFilter(setBubble());
                    message.setTextColor(textMessageColor(message_info));
                    date.setTextColor(timeLabelColor(message_info));
                }
            }
        }
    }

    private void setReadStatus()    {
        if(sentMessage)
        {
            if (AppUtils.isNotEmptyString(AppUtils.getStringFromJSON(message_info, "status")) && Integer.parseInt(AppUtils.getStringFromJSON(message_info, "status")) == 2) {
                readIndicator.setVisibility(View.VISIBLE);
            } else {
                readIndicator.setVisibility(View.GONE);
            }
        }
    }

    private int setBubble()   {
        if (AppUtils.isNotEmptyString(AppUtils.getStringFromJSON(message_info, "status")) && Integer.parseInt(AppUtils.getStringFromJSON(message_info, "status")) == 100)   {
            return Color.RED;
        } else
            return ContextCompat.getColor(date.getContext(), R.color.colorPrimary);
    }

    private int textMessageColor(JSONObject message_info)   {
        return sentMessage ? Color.WHITE : Color.BLACK;
    }

    private int timeLabelColor(JSONObject message_info)    {
        return sentMessage ? Color.WHITE : Color.GRAY;
    }

    /**
     * Image download
     */

    private void downloadAttachmentFromServer() {
        if (!progressView.isAnimation()) {
            progressView.reset().startAnimation();
        }
        String filePath = MXMessageUtil.temporaryDownloadPathByAppMessageId(AppUtils.getStringFromJSON(message_info, "app_message_id"));
        File file = new File(filePath);
        if (!file.exists())
            file.mkdirs();

        filePath = filePath + File.separator + System.currentTimeMillis() + ".jpg";

        progressView.reset().startAnimation();
        progressView.setVisibility(View.VISIBLE);
        container.setVisibility(View.VISIBLE);
        image.setVisibility(View.VISIBLE);

        ((MXChatActivity)activity).callDownloadService(filePath, message_info);
    }

    private void showImage(String filePath)    {
        imagePath = filePath;
        image.setVisibility(View.VISIBLE);
        container.setVisibility(View.VISIBLE);

        if (imagePath == null || imagePath.isEmpty())
            return;
        int[] size = getContainerSize(new File(imagePath), image.getContext());
        if (size != null){
            container.getLayoutParams().width = size[0];
            container.getLayoutParams().height = size[1];
            Glide.with(image.getContext()).load(new File(filePath)).into(image);
        }
    }

    /**
     * Image compression
     */

    private int[] getContainerSize(File file, Context context) {
        float maxSideSize = (int) context.getResources().getDimension(R.dimen.max_size_image);
        int[] size = new int[2];

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
        if (options.outWidth != -1 && options.outHeight != -1) {
            float ratio = ((float)options.outWidth / (float)options.outHeight);
            if (ratio > 1) {
                size[0] = (int)maxSideSize;
                size[1] = (int)(maxSideSize / (float)ratio);
            } else {
                size[1] = (int)maxSideSize;
                size[0] = (int)(maxSideSize * (float)ratio);
            }
        }
        return size;

    }

    /**
     * Image Upload
     */

    public void uploadAttachment()  {
        progressView.setVisibility(View.VISIBLE);
        progressView.reset().startAnimation();

        String sourceFilePath = AppUtils.imagePathWithFilName(MXMessageUtil.imageFileNameByAppMessageId(AppUtils.getStringFromJSON(message_info, "app_message_id")));
        if (!new File(sourceFilePath).exists())   {
            try {
                String in_path = AppUtils.writeImageToFile(image, AppUtils.getStringFromJSON(message_info, "filename"));
                if (in_path != null) {
                    EncryptionUtil.encryptAttachmentWithMessage(in_path, sourceFilePath, message_info);
                    new File(in_path).delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
        }

        ((MXChatActivity)activity).callUploadService(sourceFilePath, message_info);
    }

    @Override
    public void onClick(View v) {
        mSpaceClickListener.onClick(v);
        if (!MXMessageUtil.checkLocalImageExistsForMessage(message_info))
            return;

        if (Integer.parseInt(AppUtils.getStringFromJSON(message_info, "type")) == 1)    {
            Context context = v.getContext();
            Intent intent = new Intent(v.getContext(), FullSizeImageActivity.class);
            // get temp directory v
            intent.putExtra("image", imagePath);
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        }
    }
}
