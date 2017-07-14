package com.medx.android.models.chat;

import com.medx.android.utils.chat.MXUserUtil;

import java.util.Date;

/**
 * Created by alexey on 9/10/16.
 */

public class MXRelationship {

    /**
     * Properties field
     */
    public String partner_id;
    public String user_id;
    public Date last_message_date;
    public MXUser user;

    /**
     * Init Methods
     */
    public MXRelationship() {
        partner_id = "";
        user_id = "";
        last_message_date = new Date();
        user = null;
    }

    public void updateUser()    {
        user = MXUserUtil.findByUserId(user_id);
    }
}
