package com.medx.android.utils.chat;

import com.medx.android.interfaces.CompletionListener;
import com.medx.android.interfaces.MessageSaveCompletionListener;
import com.medx.android.models.chat.MXMessage;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.chat.db.sqlite.MXMessageDBHelper;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.ImageHelper;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.crypto5.AESImage;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by alexey on 9/13/16.
 */

public class MXMessageUtil {

    /**
     * Dump Methods
     */

    public static void dumpAllMessages()    {
        // Delete all messages from DB.
        MXMessageDBHelper.getInstance().deleteAll();
        AppUtils.dumpFileInDirectoryPath(AppUtils.imagesPath());
    }

    /**
     * CRUD Methods
     */

    public static MXMessage createMessageByInfo(JSONObject info)    {
        boolean isNew = false;
        MXMessage message = MXMessageDBHelper.getInstance().getMessage(AppUtils.getStringFromJSON(info, "app_message_id"));

        if (AppUtils.isEmptyobject(message))    {
            message = new MXMessage();
            isNew = true;
        }

        message.updateMessageWithInfo(info);

        if (isNew)  {
            MXMessageDBHelper.getInstance().addMessage(message);
        } else {
            MXMessageDBHelper.getInstance().updateMessage(message);
        }

        return message;
    }

    public static void saveMessageByInfo(JSONObject info, String source, MessageSaveCompletionListener completionListener)   {
        JSONObject message_info = null;
        try {
            message_info = new JSONObject(info.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MedXUser currentUser = MedXUser.CurrentUser();

        String sharedKeyString = null;
        if (info.has("type")) {
            int type = AppUtils.getStringFromJSON(info, "type").length() > 0 ? Integer.parseInt(AppUtils.getStringFromJSON(info, "type")) : 0;
            if (type == 1 && AppUtils.isNotEmptyString(source)) {

                boolean isResult = false;
                // Encrypts Image data
                if (AppUtils.isNotEmptyObject(currentUser.publicKey)) {
                    try {
                        // Generates a shared AES key
                        String filePath = AppUtils.imagePathWithFilName(AppUtils.getStringFromJSON(info, "filename"));

                        // Encrypts the attachment data by shared AES key
                        sharedKeyString = new String(ImageHelper.encodeImage(source, filePath));

                        // Encrypts the shared AES key by current user's public RSA key
                        String encryptedSharedKeyString = EncryptionUtil.encryptText(sharedKeyString, currentUser.publicKey);

                        // Stores the encrypted shared AES key into text field
                        message_info.put("text", encryptedSharedKeyString);
                        message_info.put("is_encrypted", "1");

                        isResult = true;

                    } catch (JSONException e) {
                        isResult = false;
                        e.printStackTrace();
                    }
                } else {
                    try {
                        message_info.put("encrypted", "0");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (isResult)
                    AppUtils.addSkipBackupAttributeToItemAtPath(AppUtils.imagePathWithFilName(AppUtils.getStringFromJSON(info, "filename")));
                else
                    try {
                        message_info.put("encrypted", "0");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }
        }

        MXMessage message = createMessageByInfo(message_info);

        if (completionListener != null) {
            if (AppUtils.isNotEmptyObject(message))
                completionListener.complete(AppUtils.getStringFromJSON(info, "app_message_id"), sharedKeyString, null);
            else
                completionListener.complete(AppUtils.getStringFromJSON(info, "app_message_id"), sharedKeyString, null);
        }
    }

    public static void saveIncomingMessages(JSONArray incomingMessages, JSONArray readAppMessageIds, CompletionListener completionListener)   {
        for (int i = 0; i < incomingMessages.length(); i ++)    {
            JSONObject message_info = AppUtils.getJSONFromJSONArray(incomingMessages, i);

            JSONObject info = null;
            try {
                info = new JSONObject(message_info.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            AppUtils.setJSONObjectWithObject(info, "status", "1");
            MXMessage message = createMessageByInfo(info);
        }

        for (int i = 0; i < readAppMessageIds.length(); i ++)   {
            String  app_message_id = AppUtils.getStringFromJSONArray(readAppMessageIds, i);
            JSONObject info = new JSONObject();
            AppUtils.setJSONObjectWithObject(info, "app_message_id", app_message_id);
            AppUtils.setJSONObjectWithObject(info, "status", "2");

            createMessageByInfo(info);
        }

        if (completionListener != null)
            completionListener.complete(true, null);
    }

    /**
     * Marks array of app_message_ids to a status
     */

    public static void updateMessagesInAppMessagesIds(JSONArray appMessagesIds, int status, boolean isOnlyTextMessage, CompletionListener completionListener)  {
        if (appMessagesIds.length() == 0)   {
            if (completionListener != null)
                completionListener.complete(true, null);
            return;
        }

        // Save in local storage

        ArrayList<MXMessage> messages = new ArrayList<>();

        for (int i = 0; i < appMessagesIds.length(); i ++)  {
            String app_message_id = AppUtils.getStringFromJSONArray(appMessagesIds, i);
            if (app_message_id.length() > 0) {
                MXMessage message = MXMessageDBHelper.getInstance().getMessage(app_message_id);
                if (AppUtils.isNotEmptyObject(message))
                    messages.add(message);
            }
        }

        for (MXMessage m : messages)    {
            if (m != null) {
                if (isOnlyTextMessage) {
                    if (m.type == 0) {
                        m.status = status;
                    }
                } else {
                    m.status = status;
                }
            }
        }

        for (MXMessage m : messages)
            MXMessageDBHelper.getInstance().updateMessage(m);

        if (completionListener != null)
            completionListener.complete(true, null);
    }

    /**
     * Find Methods
     */

    public static MXMessage findLastMessagesBetweenUsers(String user_id1, String user_id2)  {
        ArrayList<MXMessage> messages = findMessagesBetweenUsers(user_id1, user_id2);
        Collections.sort(messages, new VisitComparator1());
        return messages.size() > 0 ? messages.get(0) : null;
    }

    public static ArrayList<MXMessage> findReceivedMessagesByUser(String user_id)   {
        String query = " WHERE recipient_id = ?";
        return MXMessageDBHelper.getInstance().getCollectionMessages(query, user_id);
    }

    public static ArrayList<MXMessage> findSentMessagesByUser(String user_id)   {
        String query = " WHERE sender_id = ?";
        return MXMessageDBHelper.getInstance().getCollectionMessages(query, user_id);
    }

    public static ArrayList<MXMessage> findMessagesBetweenUsers(String user_id1, String user_id2)   {
        String query = " WHERE (sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?)";
        return MXMessageDBHelper.getInstance().getCollectionMessages(query, user_id1, user_id2);
    }

    public static ArrayList<MXMessage> findMessagesBetweenUsers(String user_id1, String user_id2, Date lastSentAt, int pageSize)    {
        String query = "";
        if (lastSentAt == null) {
            query = " WHERE (sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?)";
        } else {
            query = " WHERE ((sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?)) AND sent_at < ?";
        }

        ArrayList<MXMessage> messages = MXMessageDBHelper.getInstance().getCollectionMessages(query, user_id1, user_id2, lastSentAt);
        Collections.sort(messages, new VisitComparator1());

        if (messages.size() > pageSize)   {
            ArrayList<MXMessage> messages1 = new ArrayList<>();
            for (int i = 0; i < pageSize; i ++)   {
                messages1.add(messages.get(i));
            }
            return messages1;
        } else
            return messages;
    }

    public static ArrayList<MXMessage> findMessagesBetweenUsers(String user_id1, String user_id2, Date lastSentAt)  {
        String query = " WHERE ((sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?)) AND sent_at > ?";
        ArrayList<MXMessage> messages = MXMessageDBHelper.getInstance().getCollectionMessages(query, user_id1, user_id2, lastSentAt);
        Collections.sort(messages, new VisitComparator());
        return messages;
    }

    /**
     * Attribute Methods
     */

    public static String appMessageIdByUserId(String user_id, Date date)    {
        return user_id + "." + date.getTime();
    }

    public static String imageFileNameByAppMessageId(String app_message_id) {
        return app_message_id + ".jpg";
    }

    public static String temporaryDownloadPathByAppMessageId(String app_message_id) {
        String tempPath = FileUtils.getTempDirectoryPath();
        return tempPath + "/" + app_message_id;
    }

    public static String temporaryPathByFileName(String fileName)   {
        return FileUtils.getTempDirectoryPath() + "/" + fileName;
    }

    public static boolean checkLocalImageExistsForMessage(JSONObject message_info)  {
        String filePath = AppUtils.imagePathWithFilName(imageFileNameByAppMessageId(AppUtils.getStringFromJSON(message_info, "app_message_id")));
        File file = new File(filePath);

        if (file.exists())  {
            int file_size = Integer.parseInt(String.valueOf(file.length()/1024));
            if (file_size > 0)  {
                return true;
            }
        }

        return false;
    }

    public static String imageOfMessages(JSONObject message_info)    {
        String filePath = AppUtils.imagePathWithFilName(imageFileNameByAppMessageId(AppUtils.getStringFromJSON(message_info, "app_message_id")));
        File file = new File(filePath);

        boolean fileExists = file.exists();

        if (fileExists) {
            if (AppUtils.getStringFromJSON(message_info, "is_encrypted").equals("1"))   {
                String decryptedFilepath = FileUtils.getTempDirectoryPath() + File.separator + AppUtils.getStringFromJSON(message_info, "filename");
                File file1 = new File(decryptedFilepath);
                if (file1.exists())
                    return decryptedFilepath;
                try {
                    AESImage.decrypt2(filePath, decryptedFilepath, AppUtils.getStringFromJSON(message_info, "text"));
                    return FileUtils.getTempDirectoryPath() + File.separator + AppUtils.getStringFromJSON(message_info, "filename");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static JSONObject dictionaryWithValuesFromMessage(MXMessage message) {
        JSONObject message_info = new JSONObject();
        message_info = message.fetchMessageToJSON();

        if (message.type == 0)  {
            if (message.is_encrypted.equals("1"))   {
                try {
                    AppUtils.setJSONObjectWithObject(message_info, "text", EncryptionUtil.decrypt(message.text, MedXUser.CurrentUser().privateKey));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // For Photo type message
            if (AppUtils.isNotEmptyString(message.text))    {
                MedXUser currentUser = MedXUser.CurrentUser();

                // Decrypts the encrypted shared AES key by current user's private RSA key
                String decryptedSharedKeySTring = null;
                try {
                    decryptedSharedKeySTring = EncryptionUtil.decrypt(message.text, currentUser.privateKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                AppUtils.setJSONObjectWithObject(message_info, "text", decryptedSharedKeySTring);
            }
        }

        return message_info;
    }

    /**
     * Count Methods
     */

    public static int countOfUnreadMessageRecipientBySender(MXUser recipient, MXUser sender)    {
        String query = " WHERE recipient_id = ? AND sender_id = ? AND status < ?";
        return MXMessageDBHelper.getInstance().getCollectionMessages(query, recipient.user_id, sender.user_id, 2).size();
    }

    public static int countOfUnreadMessageRecipient(MXUser recipient)    {
        String query = " WHERE recipient_id = ? AND status < ?";
        ArrayList<MXMessage> messages = MXMessageDBHelper.getInstance().getCollectionMessages(query, recipient.user_id, 2);
        return messages == null ? 0 : messages.size();
    }

    /**
     * Delete Methods
     */

    public static void deleteOldMessagesForUserID(String user_id, CompletionListener completionListener)   {
        ArrayList<String > deletingMessageIds = new ArrayList<>();
        long timestamp = System.currentTimeMillis();

        // Collects olf messages info
        String query = " WHERE sender_id = ? OR recipient_id = ?";ArrayList<MXMessage> messages = MXMessageDBHelper.getInstance().getCollectionMessagesByUserID(query, user_id);
        for (MXMessage msg : messages)  {
            long sentAtInterval  = msg.sent_at.getTime();
            if (timestamp - sentAtInterval >= 604800000)   {
                deletingMessageIds.add(msg.app_message_id);

                if (msg.type == 1)  {
                    String filepath = AppUtils.imagePathWithFilName(msg.fileName);
                    File deletingFile = new File(filepath);
                    deletingFile.delete();
                }
            }
        }

        // Deletes old messages in local store
        if (deletingMessageIds.size() > 0)  {
            for (String id : deletingMessageIds)    {
                MXMessageDBHelper.getInstance().deleteMessage(id);
            }
        }

        if (completionListener != null)
            completionListener.complete(true, null);
    }

    public static void deleteConversationBetweenUsers(String user_id1, String user_id2) {
        ArrayList<String> deletingMessageIds = new ArrayList<>();
        String query = " WHERE (sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?)";
        ArrayList<MXMessage> messages = MXMessageDBHelper.getInstance().getCollectionMessages(query, user_id1, user_id2);

        for (MXMessage msg : messages)  {
            deletingMessageIds.add(msg.app_message_id);

            if (msg.type == 1)  {
                String filePath = AppUtils.imagePathWithFilName(msg.fileName);
                File deletingFile = new File(filePath);
                deletingFile.delete();
            }
        }

        // Deletes messages in local stsore
        if (deletingMessageIds.size() > 0)  {
            for (String id : deletingMessageIds)    {
                MXMessageDBHelper.getInstance().deleteMessage(id);
            }
        }
    }

    /**
     * compare methods
     */
    public static class VisitComparator implements Comparator<MXMessage> {
        @Override
        public int compare(MXMessage o1, MXMessage o2) {
            return o1.sent_at.compareTo(o2.sent_at);
        }
    }

    public static class VisitComparator1 implements Comparator<MXMessage>   {
        @Override
        public int compare(MXMessage o1, MXMessage o2) {
            return o2.sent_at.compareTo(o1.sent_at);
        }
    }
}
