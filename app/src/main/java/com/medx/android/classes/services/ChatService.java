package com.medx.android.classes.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.medx.android.classes.backend.ApiURLs;
import com.medx.android.classes.backend.BackendBase;
import com.medx.android.classes.backend.MXWebServiceListener;
import com.medx.android.interfaces.ChatServiceHandler;
import com.medx.android.interfaces.CompletionListener;
import com.medx.android.interfaces.DeliverCompletionListener;
import com.medx.android.interfaces.LocalStorageCompletionListener;
import com.medx.android.interfaces.MessageSaveCompletionListener;
import com.medx.android.models.chat.MXMessage;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.chat.db.sqlite.MXMessageDBHelper;
import com.medx.android.models.chat.db.sqlite.MXUserDBHelper;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.app.Utils;
import com.medx.android.utils.chat.EncryptionUtil;
import com.medx.android.utils.chat.MXMessageUtil;
import com.medx.android.utils.chat.MXUserUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by alexey on 9/26/16.
 */

public class ChatService extends Service {
    /**
     * Properties field
     */
    public static String dialogRecipientId;
    public static String dialogViewName;

    int callback_count;
    boolean isCheckDialogInProgress;
    public static ChatServiceHandler handler;

    public static ChatService instance;

    public static boolean isCallback;

    Handler h;

    /**
     * Init Methods
     */

    public ChatService()    {
        isCheckDialogInProgress = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("controller")){
            // get uri of selected file
            String type = intent.getStringExtra("controller");

            if(type.equals("root"))
                checkAllDialogs();
            else if (type.equals("chat"))
                startRegularCheck();

        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Sending Methods
     */

    public void sendTextMessage(JSONObject message, LocalStorageCompletionListener localStorageCompletionListener, DeliverCompletionListener deliverCompletionListener) {
        JSONObject message_info = null;

        try {
            message_info = new JSONObject(message.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MedXUser currentUser = MedXUser.CurrentUser();

        // Encrypts the text by current user's public RSA key for local storage
        if (currentUser.publicKey != null)  {
            AppUtils.setJSONObjectWithObject(message_info, "text", EncryptionUtil.encryptText(AppUtils.getStringFromJSON(message, "text"), currentUser.publicKey));
            AppUtils.setJSONObjectWithObject(message_info, "is_encrypted", "1");
        } else {
            AppUtils.setJSONObjectWithObject(message_info, "is_encrypted", "0");
        }

        // Save chat history to Local
        MXMessageUtil.saveMessageByInfo(message_info, null, new MessageSaveCompletionListener() {
            @Override
            public void complete(String app_message_id, String sharedKeyString, Error error) {
                if (app_message_id != null) {
                    if (localStorageCompletionListener != null) {
                        localStorageCompletionListener.complete(app_message_id, false);
                    }

                    MXMessage savedMessage = MXMessageDBHelper.getInstance().getMessage(app_message_id);
                    // Encrypts the sending text by recipient's public RSA key
                    String is_encrypted = "0";
                    String sendingText = AppUtils.getStringFromJSON(message, "text");
                    savedMessage.updateUsersByMessage();
                    if (savedMessage.recipient != null && AppUtils.isNotEmptyString(savedMessage.recipient.public_key)) {
                        try {
                            PublicKey publicKey = AppUtils.convertStringToPublicKey(savedMessage.recipient.public_key);
                            sendingText = EncryptionUtil.encryptText(AppUtils.getStringFromJSON(message, "text"), publicKey);
                            is_encrypted = "1";
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                    }

                    JSONObject params = new JSONObject();
                    AppUtils.setJSONObjectWithObject(params, "token", currentUser.accessToken());
                    AppUtils.setJSONObjectWithObject(params, "sent_at", AppUtils.getUTCDate(savedMessage.sent_at));
                    AppUtils.setJSONObjectWithObject(params, "recipient_id", savedMessage.recipient_id);
                    AppUtils.setJSONObjectWithObject(params, "app_message_id", savedMessage.app_message_id);
                    AppUtils.setJSONObjectWithObject(params, "text", sendingText);
                    AppUtils.setJSONObjectWithObject(params, "is_encrypted", is_encrypted);
                    AppUtils.setJSONObjectWithObject(params, "type", AppUtils.getStringFromJSON(message, "type"));

                    BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_MESSAGES_SEND, params, new MXWebServiceListener() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            String response = AppUtils.getStringFromJSON(result, "response");
                            if (response.equals("fail"))    {
                                handleMessageDeliveredForAppMessageId(app_message_id, 100, deliverCompletionListener);
                            } else {
                                JSONObject info = new JSONObject();

                                AppUtils.setJSONObjectWithObject(info, "app_message_id", AppUtils.getStringFromJSON(message, "app_message_id"));
                                AppUtils.setJSONObjectWithObject(info, "message_id", AppUtils.getStringFromJSON(result, "message"));
                                AppUtils.setJSONObjectWithObject(info, "status", 0);

                                MXMessageUtil.saveMessageByInfo(info, null, null);
                            }

                            MXUserUtil.updateUserDefaults(null, null);
                        }

                        @Override
                        public void onFailed(JSONObject erroreResult) {
                            handleMessageDeliveredForAppMessageId(app_message_id, 100, deliverCompletionListener);
                        }
                    });
                }
            }
        });
    }

    public void handleMessageDeliveredForAppMessageId(String app_message_id, int message_status, DeliverCompletionListener deliverCompletionListener)   {
        JSONObject info = new JSONObject();

        AppUtils.setJSONObjectWithObject(info, "app_message_id", app_message_id);
        AppUtils.setJSONObjectWithObject(info, "status", message_status);

        MXMessageUtil.saveMessageByInfo(info, null, new MessageSaveCompletionListener() {
            @Override
            public void complete(String app_message_id, String sharedKeyString, Error error) {
                if (app_message_id != null) {
                    deliverCompletionListener.complete(app_message_id, message_status);
                }
            }
        });
    }

    public void sendPhotoMessage(MXMessage message, DeliverCompletionListener deliverCompletionListener)    {
        JSONObject params = new JSONObject();

        AppUtils.setJSONObjectWithObject(params, "token", MedXUser.CurrentUser().accessToken());
        AppUtils.setJSONObjectWithObject(params, "sent_at", AppUtils.getUTCDate(message.sent_at));
        AppUtils.setJSONObjectWithObject(params, "recipient_id", message.recipient_id);
        AppUtils.setJSONObjectWithObject(params, "app_message_id", message.app_message_id);
        AppUtils.setJSONObjectWithObject(params, "type", message.type);
        AppUtils.setJSONObjectWithObject(params, "width", message.width);
        AppUtils.setJSONObjectWithObject(params, "height", message.height);
        AppUtils.setJSONObjectWithObject(params, "is_encrypted", message.is_encrypted);
        message.updateUsersByMessage();
        MXUser recipient = MXUserDBHelper.getInstance().getUser(message.recipient.user_id);
        if (message.is_encrypted.equals("1") && AppUtils.isNotEmptyString(recipient.public_key))   {
            MedXUser currentUser = MedXUser.CurrentUser();

            // Decrypts the encrypted shared AES key by current user's private RSA key
            String sharedKeyString = null;
            try {
                sharedKeyString = EncryptionUtil.decrypt(message.text, currentUser.privateKey);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Encrypts the shared AES key by recipient's public RSA key
            try {
                PublicKey publicKey = AppUtils.convertStringToPublicKey(recipient.public_key);
                String encryptedSharedKeyString = EncryptionUtil.encryptText(sharedKeyString, publicKey);
                AppUtils.setJSONObjectWithObject(params, "text", encryptedSharedKeyString);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }

        BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_MESSAGES_SEND, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                String response = AppUtils.getStringFromJSON(result, "response");
                if (response.equals("fail"))    {
                    handleMessageDeliveredForAppMessageId(message.app_message_id, 100, deliverCompletionListener);
                } else {
                    JSONObject info = new JSONObject();
                    AppUtils.setJSONObjectWithObject(info, "app_message_id", message.app_message_id);
                    AppUtils.setJSONObjectWithObject(info, "message_id", AppUtils.getStringFromJSON(result, "message"));

                    MXMessageUtil.saveMessageByInfo(info, null, new MessageSaveCompletionListener() {
                        @Override
                        public void complete(String app_message_id, String sharedKeyString, Error error) {
                            if (deliverCompletionListener != null)  {
                                deliverCompletionListener.complete(app_message_id, 101);
                            }
                        }
                    });
                }

                MXUserUtil.updateUserDefaults(null, null);
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                handleMessageDeliveredForAppMessageId(message.app_message_id, 100, deliverCompletionListener);
            }
        });
    }

    public void transferPhotoForMessage(JSONObject message_info, CompletionListener completionListener) {
        JSONObject params = new JSONObject();
        MXMessage message = MXMessageDBHelper.getInstance().getMessage(AppUtils.getStringFromJSON(message_info, "app_message_id"));
        if (AppUtils.isEmptyString(message.message_id)) {
            completionListener.complete(false, "message_id is not present");
            return;
        }

        AppUtils.setJSONObjectWithObject(params, "token", MedXUser.CurrentUser().accessToken());
        AppUtils.setJSONObjectWithObject(params, "message_id", message.message_id);

        BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_MESSAGES_TRANSFER_PHOTO, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                String response = AppUtils.getStringFromJSON(result, "response");
                if (response.equals("fail"))    {
                    if (completionListener != null) {
                        completionListener.complete(false, AppUtils.getStringFromJSON(result, "status"));
                    }
                } else {
                    JSONObject info = new JSONObject();

                    AppUtils.setJSONObjectWithObject(info, "app_message_id", message.app_message_id);
                    AppUtils.setJSONObjectWithObject(info, "status", 0);

                    MXMessageUtil.saveMessageByInfo(info, null, new MessageSaveCompletionListener() {
                        @Override
                        public void complete(String app_message_id, String sharedKeyString, Error error) {
                            if (completionListener != null)
                                completionListener.complete(true, null);
                        }
                    });
                }

                MXUserUtil.updateUserDefaults(null, null);
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                completionListener.complete(false, null);
            }
        });
    }

    /**
     * Dropbox Methods
     */

    public void deleteReadMessageFromServer(JSONArray readAppMessageIds, CompletionListener completionListener)    {
        String ids = AppUtils.StringWithComponentsJoinedByStringFromJSON(readAppMessageIds, ",");

        JSONObject params = new JSONObject();

        AppUtils.setJSONObjectWithObject(params, "token", MedXUser.CurrentUser().accessToken());
        AppUtils.setJSONObjectWithObject(params, "app_message_ids", ids);

        BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_MESSAGES_DELETE, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                String reponse = AppUtils.getStringFromJSON(result, "response");
                if (reponse.equals("fail"))   {
                    if (completionListener != null)
                        completionListener.complete(false, null);
                } else {
                    if (completionListener != null)
                        completionListener.complete(true, null);
                }
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                if (completionListener != null)
                    completionListener.complete(false, null);
            }
        });
    }

    public void updateReceivedMessageStatus(String updatingStatus, ArrayList<String> app_message_ids, CompletionListener completionListener)    {
        String ids = AppUtils.StringWithComponentsJoinedByString(app_message_ids, ",");

        JSONObject params = new JSONObject();
        AppUtils.setJSONObjectWithObject(params, "token", MedXUser.CurrentUser().accessToken());
        AppUtils.setJSONObjectWithObject(params, "app_message_ids", ids);
        AppUtils.setJSONObjectWithObject(params, "updating_status", updatingStatus);

        BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_MESSAGES_UPDATE_STATUS, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                String response_st = AppUtils.getStringFromJSON(result, "response");
                if (response_st.equals("fail"))    {
                    if (completionListener != null) completionListener.complete(false, null);
                } else {
                    if (completionListener != null) completionListener.complete(true, null);
                }
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                if (completionListener != null) completionListener.complete(false, null);
            }
        });
    }

    /**
     * Timer Methods
     */

    public void startRegularCheck() {
        h = new Handler();
        isCallback = true;
        final Runnable r = new Runnable() {
            public void run() {
                checkAllDialogs();
                if (isCallback) {
                    h.postDelayed(this, 1500);
                }
            }
        };
        h.postDelayed(r, 2000);
    }

    public void stopRegularCheck()  {
        ChatService.dialogRecipientId = null;
        isCallback = false;
        if (h != null) {
            h.removeCallbacksAndMessages(null);
            h = null;
        }
    }

    /**
     * Check and Handles new incoming & sent messages
     */

    public void checkAllDialogs()  {
        if (MedXUser.CurrentUser().accessToken() == null)
            return;

        JSONObject params = new JSONObject();
        AppUtils.setJSONObjectWithObject(params, "token", MedXUser.CurrentUser().accessToken());
        if (AppUtils.isNotEmptyString(dialogRecipientId))
            AppUtils.setJSONObjectWithObject(params, "dialog_user_id", dialogRecipientId);
        BackendBase.newSharedConnection1().accessAPIFastGET(ApiURLs.URL_ALL_DIALOGS, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                String response_st = AppUtils.getStringFromJSON(result, "response");
                if (response_st.equals("fail"))    {
                    handleFailureInCheckAllDialogs();
                } else {
                    handleIncomingMessages(AppUtils.getJSONArrayFromJSON(result, "incomings"), AppUtils.getJSONArrayFromJSON(result, "reads"), AppUtils.getJSONArrayFromJSON(result, "senders"));
                }
                MXUserUtil.updateUserDefaults(null, null);
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                handleFailureInCheckAllDialogs();
            }
        });
    }

    private void handleFailureInCheckAllDialogs()   {
        //
        if (handler != null)
            handler.receiveNotification(Utils.kNotificationChatDidFailInCheckAllDialogs);
    }

    private void handleIncomingMessages(JSONArray incomingMessages, JSONArray readAppMessageIds, JSONArray senders) {
        if (isCheckDialogInProgress)
            return;

        isCheckDialogInProgress = true;
        callback_count = 0;

        if (incomingMessages.length() > 0 || readAppMessageIds.length() > 0 || senders.length() > 0)    {
            MXUserUtil.saveUsers(senders, MedXUser.CurrentUser().userId(), new CompletionListener() {
                @Override
                public void complete(boolean success, String errorStatus) {
                    if (errorStatus == null)    {
                        for (int i = 0; i < incomingMessages.length(); i ++)    {
                            JSONObject info = AppUtils.getJSONFromJSONArray(incomingMessages, i);
                            AppUtils.setJSONObjectWithObject(info, "sent_at", AppUtils.getLocalDate(AppUtils.getStringFromJSON(info, "sent_at")));
                            try {
                                incomingMessages.put(i, info);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        MXMessageUtil.saveIncomingMessages(incomingMessages, readAppMessageIds, new CompletionListener() {
                            @Override
                            public void complete(boolean success, String errorStatus) {
                                if (errorStatus == null)    {
                                    ArrayList<String> incomingAppMessageIds = new ArrayList<>();

                                    // Mark new messages to sent status in server
                                    for (int i = 0; i < incomingMessages.length(); i ++)    {
                                        incomingAppMessageIds.add(AppUtils.getStringFromJSON(AppUtils.getJSONFromJSONArray(incomingMessages, i), "app_message_id"));
                                    }

                                    if (incomingAppMessageIds.size() > 0)   {
                                        String status_sent = "1";
                                        updateReceivedMessageStatus(status_sent, incomingAppMessageIds, new CompletionListener() {
                                            @Override
                                            public void complete(boolean success, String errorStatus) {

                                            }
                                        });
                                        callback_count ++;
                                        if (callback_count > 1) {
                                            postNotificationsAfterHandlingIcomingMessages(incomingMessages, readAppMessageIds);
                                        }
                                    } else
                                        callback_count ++;

                                    // Delete read messages from server
                                    if (readAppMessageIds.length() > 0) {
                                        deleteReadMessageFromServer(readAppMessageIds, new CompletionListener() {
                                            @Override
                                            public void complete(boolean success, String errorStatus) {
                                                callback_count ++;

                                                if (callback_count > 1) {
                                                    postNotificationsAfterHandlingIcomingMessages(incomingMessages, readAppMessageIds);
                                                }
                                            }
                                        });
                                    } else
                                        callback_count ++;

                                    if (callback_count > 1) {
                                        postNotificationsAfterHandlingIcomingMessages(incomingMessages, readAppMessageIds);
                                    }
                                } else
                                    postNotificationsAfterHandlingIcomingMessages(incomingMessages, readAppMessageIds);
                            }
                        });
                    } else
                        postNotificationsAfterHandlingIcomingMessages(incomingMessages, readAppMessageIds);
                }
            });
        } else
            postNotificationsAfterHandlingIcomingMessages(incomingMessages, readAppMessageIds);
    }

    private void postNotificationsAfterHandlingIcomingMessages(JSONArray incomingMessages, JSONArray readAppMessageIds) {
        isCheckDialogInProgress = false;

        if (dialogRecipientId != null)  {
            JSONArray newDialogAppMessageIds = new JSONArray();

            for (int i = 0; i < incomingMessages.length(); i ++)    {
                JSONObject info = AppUtils.getJSONFromJSONArray(incomingMessages, i);
                if (AppUtils.getStringFromJSON(info, "sender_id").equals(dialogRecipientId))
                    newDialogAppMessageIds.put(AppUtils.getStringFromJSON(info, "app_message_id"));
            }

            if (newDialogAppMessageIds.length() > 0 || readAppMessageIds.length() > 0)    {
                JSONObject userInfo = new JSONObject();
                AppUtils.setJSONObjectWithObject(userInfo, "incomings", newDialogAppMessageIds);
                AppUtils.setJSONObjectWithObject(userInfo, "reads", readAppMessageIds);

                Utils.notificationData = userInfo;
                Intent intent = new Intent("InComing");
                intent.putExtra("type", Utils.kKnotificationChatDidReceiveDialogMessages);
                sendBroadcast(intent);
            }
        } else {
            MedXUser.CurrentUser().updateUserDialogsWithReadSentMessages(AppUtils.ArrayFromJSON(readAppMessageIds));
            JSONObject userInfo = new JSONObject();
            AppUtils.setJSONObjectWithObject(userInfo, "incomings", incomingMessages);
            AppUtils.setJSONObjectWithObject(userInfo, "reads", readAppMessageIds);
            Utils.notificationData = userInfo;

            Intent intent = new Intent("InComing");
            intent.putExtra("type", Utils.kNotificationChatDidReceiveNewMessage);
            sendBroadcast(intent);
        }
    }
}
