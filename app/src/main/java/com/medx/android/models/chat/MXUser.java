package com.medx.android.models.chat;

import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.chat.MXMessageUtil;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by alexey on 9/10/16.
 */

public class MXUser {
    /**
     * properties field
     */
    public String about;
    public String address;
    public String first_name;
    public String last_name;
    public String locations;
    public String preferred_first_name;
    public String public_key;
    public String salutation;
    public String specialty;
    public String user_id;
    public int status;
    public ArrayList<MXRelationship> partners;
    public ArrayList<MXMessage> receivedMessages;
    public ArrayList<MXMessage> sentMessages;
    public String distance;

    /**
     * Init methods
     */

    public MXUser() {
        about = "";
        address = "";
        first_name = "";
        last_name = "";
        locations = "";
        preferred_first_name = "";
        public_key = "";
        salutation = "";
        specialty = "";
        user_id = "";
        status = -1;
        partners = null;
        receivedMessages = null;
        sentMessages = null;
        distance = "";
    }

    public void updateUserWithInfo(JSONObject info)   {
        user_id = AppUtils.getStringFromJSON(info, "user_id").length() > 0 ? AppUtils.getStringFromJSON(info, "user_id") : user_id;
        first_name = AppUtils.getStringFromJSON(info, "first_name").length() > 0 ? AppUtils.getStringFromJSON(info, "first_name") : first_name;
        last_name = AppUtils.getStringFromJSON(info, "last_name").length() > 0 ? AppUtils.getStringFromJSON(info, "last_name") : last_name;
        specialty = AppUtils.getStringFromJSON(info, "specialty").length() > 0 ? AppUtils.getStringFromJSON(info, "specialty") : specialty;
        salutation = AppUtils.getStringFromJSON(info, "salutation").length() > 0 ? AppUtils.getStringFromJSON(info, "salutation") : salutation;
        status = AppUtils.getStringFromJSON(info, "status").length() > 0 ? Integer.parseInt(AppUtils.getStringFromJSON(info, "status")) : status;
        preferred_first_name = AppUtils.getStringFromJSON(info, "preferred_first_name").length() > 0 ? AppUtils.getStringFromJSON(info, "preferred_first_name") : preferred_first_name;
        address = AppUtils.getStringFromJSON(info, "address").length() > 0 ? AppUtils.getStringFromJSON(info, "address") : address;
        locations = AppUtils.getStringFromJSON(info, "locations").length() > 0 ? AppUtils.getStringFromJSON(info, "locations") : locations;
        about = AppUtils.getStringFromJSON(info, "about").length() > 0 ? AppUtils.getStringFromJSON(info, "about") : about;
        public_key = AppUtils.getStringFromJSON(info, "public_key").length() > 0 ? AppUtils.getStringFromJSON(info, "public_key") : public_key;
        distance = AppUtils.getStringFromJSON(info, "distance").length() > 0 ? AppUtils.getStringFromJSON(info, "distance") : distance;
    }

    public JSONObject dictionaryForUser()   {
        JSONObject info = new JSONObject();

        AppUtils.setJSONObjectWithObject(info, "user_id", user_id);
        AppUtils.setJSONObjectWithObject(info, "first_name", first_name);
        AppUtils.setJSONObjectWithObject(info, "last_name", last_name);
        AppUtils.setJSONObjectWithObject(info, "specialty", specialty);
        AppUtils.setJSONObjectWithObject(info, "salutation", salutation);
        AppUtils.setJSONObjectWithObject(info, "status", status);
        AppUtils.setJSONObjectWithObject(info, "preferred_first_name", preferred_first_name);
        AppUtils.setJSONObjectWithObject(info, "address", address);
        AppUtils.setJSONObjectWithObject(info, "locations", locations);
        AppUtils.setJSONObjectWithObject(info, "about", about);
        AppUtils.setJSONObjectWithObject(info, "public_key", public_key);
        AppUtils.setJSONObjectWithObject(info, "distance", distance);

        return info;
    }

    public void getRelationLists()  {
        receivedMessages = MXMessageUtil.findReceivedMessagesByUser(user_id);
        sentMessages = MXMessageUtil.findSentMessagesByUser(user_id);
    }

    /**
     * Attributes methods
     */

    public String fullName()    {
        return preferred_first_name + " " + last_name;
    }

    public String fullNameWithSalutation()  {
        return salutation + " " + preferred_first_name + " " + last_name;
    }

    public String shortNameWithSalutation() {
        return salutation + " " + last_name;
    }

    public String initials()    {
        StringBuilder builder = new StringBuilder();
        String name = fullName();
        if (name.length() > 0)  {
            String[] words = name.split(" ");
            for (String word : words)   {
                if (word.length() > 0)  {
                    String firstLetter = word.substring(1);
                    builder.append(firstLetter.toUpperCase());
                }
            }
        }

        if (builder.length() > 3) {
            builder.substring(0, 3);
        }

        return builder.toString();
    }

    public String contactIdentifier()   {
        return first_name + " " + last_name + " " + specialty;
    }

    public int avatarBGColorIndex() {
        return Integer.parseInt(user_id) * 29 * contactIdentifier().length() % 21;
    }

    /**
     * Flag methods
     */
    public boolean hasInstallApp()  {
        return AppUtils.isNotEmptyString(public_key);
    }

    public boolean isVerified() {
        return status == 1;
    }
}
