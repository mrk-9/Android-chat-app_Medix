package com.medx.android.models.chat;

import com.medx.android.models.chat.db.sqlite.MXRelationshipDBHelper;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.chat.MXRelationshipUtil;
import com.medx.android.utils.chat.MXUserUtil;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by alexey on 9/10/16.
 */

public class MXMessage {

    /**
     * Properties field
     */
    public String app_message_id;
    public String fileName;
    public int height;
    public String  message_id;
    public String recipient_id;
    public String sender_id;
    public Date sent_at;
    public int status;
    public String text;
    public int type;
    public String url;
    public int width;
    public String is_encrypted;
    public MXUser recipient;
    public MXUser sender;

    /**
     * Init Methods
     */
    public MXMessage()  {
        app_message_id = "";
        fileName = "";
        height = 0;
        message_id = "";
        recipient_id = "";
        sender_id = "";
        sent_at = new Date();
        status = -1;
        text = "";
        type = -1;
        url = "";
        width = 0;
        is_encrypted = "";
        recipient = null;
        sender = null;
    }

    public void updateMessageWithInfo(JSONObject info)  {
        // get sent_at

        type = AppUtils.getStringFromJSON(info, "type").length() > 0? Integer.parseInt(AppUtils.getStringFromJSON(info, "type")) : type;
        status = AppUtils.getStringFromJSON(info, "status").length() > 0 ? Integer.parseInt(AppUtils.getStringFromJSON(info, "status")) : status;
        width = AppUtils.getStringFromJSON(info, "width").length() > 0 ? Integer.parseInt(AppUtils.getStringFromJSON(info, "width")) : width;
        height = AppUtils.getStringFromJSON(info, "height").length() > 0 ? Integer.parseInt(AppUtils.getStringFromJSON(info, "height")) : height;

        sender_id = AppUtils.getStringFromJSON(info, "sender_id").length() > 0 ? AppUtils.getStringFromJSON(info, "sender_id") : sender_id;
        sender = MXUserUtil.findByUserId(sender_id);

        recipient_id = AppUtils.getStringFromJSON(info, "recipient_id").length() > 0 ? AppUtils.getStringFromJSON(info, "recipient_id") : recipient_id;
        recipient = MXUserUtil.findByUserId(recipient_id);

        is_encrypted = AppUtils.getStringFromJSON(info, "is_encrypted").length() > 0 ? AppUtils.getStringFromJSON(info, "is_encrypted") : is_encrypted;
        app_message_id = AppUtils.getStringFromJSON(info, "app_message_id").length() > 0 ? AppUtils.getStringFromJSON(info, "app_message_id") : app_message_id;
        fileName = AppUtils.getStringFromJSON(info, "filename").length() > 0 ? AppUtils.getStringFromJSON(info, "filename") : fileName;
        sent_at = AppUtils.getStringFromJSON(info, "sent_at").length() > 0 ? AppUtils.DateFromString(AppUtils.getStringFromJSON(info, "sent_at")) : sent_at;
        url = AppUtils.getStringFromJSON(info, "url").length() > 0 ? AppUtils.getStringFromJSON(info, "url") : url;
        message_id = AppUtils.getStringFromJSON(info, "message_id").length() > 0 ? AppUtils.getStringFromJSON(info, "message_id") : message_id;
        text = AppUtils.getStringFromJSON(info, "text").length() > 0 ? AppUtils.getStringFromJSON(info, "text") : text;

        if (info.has("sent_at") && AppUtils.isNotEmptyObject(sent_at))  {
            if (info.has("sender_id") && info.has("recipient_id"))  {
                MedXUser currentUser = MedXUser.CurrentUser();
                String primaryUserId = currentUser.userId();
                String partner_id = AppUtils.getStringFromJSON(info, "recipient_id");
                if (partner_id.equals(primaryUserId))
                    partner_id = AppUtils.getStringFromJSON(info, "sender_id");

                MXRelationship rel = MXRelationshipUtil.findRelationshipByUserId(primaryUserId, partner_id);
                if (rel != null && AppUtils.isNotEmptyObject(rel) && rel.last_message_date.getTime() < sent_at.getTime())  {
                    rel.last_message_date = sent_at;
                    MXRelationshipDBHelper.getInstnace().updateRelationship(rel);
                }
            }
        }
    }

    public void updateUsersByMessage() {
        sender = MXUserUtil.findByUserId(sender_id);
        recipient = MXUserUtil.findByUserId(recipient_id);
    }

    public JSONObject fetchMessageToJSON()  {
        JSONObject message_info = new JSONObject();
        AppUtils.setJSONObjectWithObject(message_info, "type", type);
        AppUtils.setJSONObjectWithObject(message_info, "status", status);
        AppUtils.setJSONObjectWithObject(message_info, "width", width);
        AppUtils.setJSONObjectWithObject(message_info, "height", height);
        AppUtils.setJSONObjectWithObject(message_info, "sender_id", sender_id);
        AppUtils.setJSONObjectWithObject(message_info, "recipient_id", recipient_id);
        AppUtils.setJSONObjectWithObject(message_info, "sent_at", AppUtils.StringFromDate(sent_at));
        AppUtils.setJSONObjectWithObject(message_info, "is_encrypted", is_encrypted);
        AppUtils.setJSONObjectWithObject(message_info, "text", text);
        AppUtils.setJSONObjectWithObject(message_info, "url", url);
        AppUtils.setJSONObjectWithObject(message_info, "app_message_id", app_message_id);
        AppUtils.setJSONObjectWithObject(message_info, "filename", fileName);
        AppUtils.setJSONObjectWithObject(message_info, "message_id", message_id);

        return message_info;
    }
}
