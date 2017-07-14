package com.medx.android.models.user;

import android.media.Image;

import com.loopj.android.http.RequestParams;
import com.medx.android.App;
import com.medx.android.classes.backend.ApiURLs;
import com.medx.android.classes.backend.BackendBase;
import com.medx.android.classes.backend.MXWebServiceListener;
import com.medx.android.interfaces.CompletionListener;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.chat.db.sqlite.MXUserDBHelper;
import com.medx.android.utils.app.AppUtils;
import com.medx.android.utils.chat.MXMessageUtil;
import com.medx.android.utils.chat.MXUserUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by alexey on 9/6/16.
 */

public class MedXUser {

    /**
     * Properties field
     */

    public Image profileImage;
    public JSONObject info;
    public JSONObject userDialogs;

    public PublicKey publicKey;
    public PrivateKey privateKey;

    static MedXUser user;
    /**
     * Init methods
     */

    public static MedXUser CurrentUser()    {

        if (user == null)   {
            user = new MedXUser();
            user.userDialogs = new JSONObject();
        }

        return user;
    }

    public MedXUser()   {
        info = new JSONObject();
    }

    public void setUserInfo(JSONObject info)   {
        this.info = new JSONObject();

        if (AppUtils.isNotEmptyObject(AppUtils.getStringFromJSON(info, "access_token")))   {
            AppUtils.setJSONObjectWithObject(this.info, "access_token", AppUtils.getStringFromJSON(info, "access_token"));
        }

        if (AppUtils.isNotEmptyObject(AppUtils.getStringFromJSON(info, "offices"))) {
            AppUtils.setJSONObjectWithObject(this.info, "offices", AppUtils.getJSONArrayFromJSON(info, "offices"));
        }

        if (AppUtils.isNotEmptyObject(AppUtils.getStringFromJSON(info, "salutation")))  {
            AppUtils.setJSONObjectWithObject(this.info, "salutation", AppUtils.getStringFromJSON(info, "salutation"));
        }

        if (AppUtils.isNotEmptyObject(AppUtils.getStringFromJSON(info, "preferred_first_name")))   {
            AppUtils.setJSONObjectWithObject(this.info, "preferred_first_name", AppUtils.getStringFromJSON(info, "preferred_first_name"));
        }

        if (AppUtils.isNotEmptyObject(AppUtils.getStringFromJSON(info, "about")))  {
            AppUtils.setJSONObjectWithObject(this.info, "about", AppUtils.getStringFromJSON(info, "about"));
        }

        AppUtils.setJSONObjectWithObject(this.info, "user_id", AppUtils.getStringFromJSON(info, "user_id"));

        if (AppUtils.isNotEmptyObject(AppUtils.getArrayFromJSON(info, "blocked_user_ids")))    {
            AppUtils.setJSONObjectWithObject(this.info, "blocked_user_ids", AppUtils.getJSONArrayFromJSON(info, "blocked_user_ids"));
        }

        AppUtils.setJSONObjectWithObject(this.info, "user_id", AppUtils.getStringFromJSON(info, "user_id"));
        AppUtils.setJSONObjectWithObject(this.info, "first_name", AppUtils.getStringFromJSON(info, "first_name"));
        AppUtils.setJSONObjectWithObject(this.info, "last_name", AppUtils.getStringFromJSON(info, "last_name"));
        AppUtils.setJSONObjectWithObject(this.info, "specialty", AppUtils.getStringFromJSON(info, "specialty"));
        AppUtils.setJSONObjectWithObject(this.info, "public_key", AppUtils.getStringFromJSON(info, "public_key"));
        AppUtils.setJSONObjectWithObject(this.info, "status", AppUtils.getStringFromJSON(info, "status"));

        setupKeys();
    }

    public void setupKeys()    {
        KeyPair keyPair = MXUserUtil.getKeyPair();

        if (AppUtils.isNotEmptyObject(keyPair)) {
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        }
    }

    public void unset()    {
        info = null;
        publicKey = null;
        privateKey = null;
    }

    /**
     * Attributes Methods
     */
    public MXUser dbUser()    {
        // Get user by user id
        String userId = userId();
        return AppUtils.isNotEmptyString(userId) ? MXUserDBHelper.getInstance().getUser(MedXUser.CurrentUser().userId()) : null;
    }

    public String accessToken() {
        return AppUtils.getStringFromJSON(info, "access_token");
    }

    public String userId()  {
        return AppUtils.getStringFromJSON(info, "user_id");
    }

    public ArrayList<String> blockUserIds()  {
        ArrayList<String> blockUserIds = new ArrayList<>();
        if (AppUtils.isNotEmptyObject(AppUtils.getArrayFromJSON(info, "blocked_user_ids")))  {
            blockUserIds = AppUtils.getArrayFromJSON(info, "blocked_user_ids");
        }

        return blockUserIds;
    }

    public void addBlockedUserId(String user_id)    {
        ArrayList<String> blocked_ids = blockUserIds();

        blocked_ids.add(user_id);
        AppUtils.setJSONObjectWithObject(info, "blocked_user_ids", AppUtils.JSONFromArray(blocked_ids));
        // Save user info with last login date.
        MXUserUtil.updateUserDefaults(info, new Date());
    }

    public void removeBlockedUserId(String user_id) {
        ArrayList<String> blocked_ids = blockUserIds();

        if (blocked_ids.contains(user_id))
            blocked_ids.remove(user_id);

        AppUtils.setJSONObjectWithObject(info, "blocked_user_ids", AppUtils.JSONFromArray(blocked_ids));
        // Save user info with last login date.
        MXUserUtil.updateUserDefaults(info, new Date());
    }

    public void updateUserDialogsWithReadSentMessages(List<String> readAppMessageIds)   {
        JSONArray nameArray = userDialogs.names();
        if (nameArray == null)
            return;
        for (int i = 0; i < nameArray.length(); i ++)  {
            String user_id = AppUtils.getStringFromJSONArray(nameArray, i);
            JSONArray userMessages = AppUtils.getJSONArrayFromJSON(userDialogs, user_id);

            if (AppUtils.isNotEmptyObject(userMessages)) {
                for (int j = 0; j < userMessages.length(); j++) {
                    JSONObject message_info = AppUtils.getJSONFromJSONArray(userMessages, j);
                    if (readAppMessageIds.contains(AppUtils.getStringFromJSON(message_info, "app_message_id"))) {
                        AppUtils.setJSONObjectWithObject(message_info, "status", 2);
                    }
                }
            }
        }
    }

    /**
     * Check Methods
     */
    public boolean checkUserLoggedIn()  {
        return AppUtils.isNotEmptyObject(info) ? info.length() > 0 : false;
    }

    public boolean isBlockingUserId(String user_id) {
        ArrayList<String> blocked_ids = blockUserIds();
        return blocked_ids.contains(user_id);
    }

    /**
     * Badge Methods
     */

    public void resetApplicationBadge() {
        int totalUnreadMessages = AppUtils.isNotEmptyObject(dbUser()) ? MXMessageUtil.countOfUnreadMessageRecipient(dbUser()) : 0;
        ShortcutBadger.applyCount(App.DefaultContext, totalUnreadMessages); //for 1.1.4+
    }

    /**
     * API Methods
     */
    public void blockOrUnblockUserById(String user_id, boolean isBlock, CompletionListener completionListener) {
        JSONObject params = new JSONObject();
        AppUtils.setJSONObjectWithObject(params, "token", accessToken());
        AppUtils.setJSONObjectWithObject(params, "user_id", user_id);

        String route = isBlock ? ApiURLs.URL_USERS_BLOCK : ApiURLs.URL_USERS_UNBLOCK;

        BackendBase.newSharedConnection().accessAPIFastPOST(route, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                String response = AppUtils.getStringFromJSON(result, "response");
                if (response.equals("fail"))    {
                    MXUserUtil.updateUserDetaults(null);
                    if (completionListener != null) completionListener.complete(false, null);
                } else {
                    if (isBlock)
                        addBlockedUserId(user_id);
                    else
                        removeBlockedUserId(user_id);

                    if (completionListener != null) completionListener.complete(true, null);
                }
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                if (completionListener != null) completionListener.complete(false, null);
            }
        });
    }

    public void registerDeviceToken(String deviceToken, CompletionListener completionListener) {
        JSONObject params = new JSONObject();

        AppUtils.setJSONObjectWithObject(params, "token", accessToken());
        AppUtils.setJSONObjectWithObject(params, "device_token", deviceToken);
        AppUtils.setJSONObjectWithObject(params, "device_type", "android");

        BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_USERS_DEVICE, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                String response = AppUtils.getStringFromJSON(result, "response");
                if (response.equals("fail"))    {
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

    public void registerPublicKey(String publicKeyString, CompletionListener completionListener)    {

        RequestParams params1 = new RequestParams();
        params1.put("token", accessToken());
        params1.put("public_key", publicKeyString);

        BackendBase.newSharedConnection().accessAPIbyPostWithSync(App.DefaultContext, ApiURLs.URL_USERS_PUBLIC_KEY, params1, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                String response = AppUtils.getStringFromJSON(result, "response");
                if (response.equals("fail"))    {
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

    public void inviteUser(String user_id, String phoneNumber, CompletionListener completionListener)  {
        JSONObject params = new JSONObject();
        AppUtils.setJSONObjectWithObject(params, "token", accessToken());
        AppUtils.setJSONObjectWithObject(params, "invite", user_id);
        AppUtils.setJSONObjectWithObject(params, "phone", phoneNumber);

        BackendBase.newSharedConnection().accessAPIFastPOST(ApiURLs.URL_USERS_INVITE, params, new MXWebServiceListener() {
            @Override
            public void onSuccess(JSONObject result) {
                String respone = AppUtils.getStringFromJSON(result, "response");
                if (respone.equals("fail")) {
                    String status = AppUtils.getStringFromJSON(result, "status");
                    String errMessage  = "Could not invite the doctor. Please try again later";
                    if (status.equals("verified"))  {
                        errMessage = "The doctor has already been verified. Please pull to refresh the search";
                    } else  {
                        errMessage = "The phone has already been used by another doctor!";
                    }

                    if (completionListener != null) {
                        completionListener.complete(false, errMessage);
                    }
                } else {
                    if (completionListener != null) completionListener.complete(true, null);
                }
            }

            @Override
            public void onFailed(JSONObject erroreResult) {
                if (completionListener != null) completionListener.complete(false, "Communication with server failed.\\nPlease check your network connection.");
            }
        });
    }

}
