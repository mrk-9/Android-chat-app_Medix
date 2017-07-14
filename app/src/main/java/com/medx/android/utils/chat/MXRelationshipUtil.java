package com.medx.android.utils.chat;

import com.medx.android.models.chat.MXRelationship;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.chat.db.sqlite.MXRelationshipDBHelper;
import com.medx.android.utils.app.AppUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by alexey on 9/12/16.
 */

public class MXRelationshipUtil {

    /**
     * CRUD Methods
     */

    public static MXRelationship createRelationshipByInfo(JSONObject info, String userId)   {
        MXRelationship relationship = MXRelationshipDBHelper.getInstnace().getRelationship(AppUtils.getStringFromJSON(info, "user_id"), userId);
        boolean isNew = false;

        if (AppUtils.isEmptyobject(relationship))   {
            relationship = new MXRelationship();
            isNew = true;
        }

        relationship.partner_id = AppUtils.getStringFromJSON(info, "user_id");
        relationship.user_id = userId;

        if (isNew)  {
            MXRelationshipDBHelper.getInstnace().addRelationShip(relationship);
        } else {
            MXRelationshipDBHelper.getInstnace().updateRelationship(relationship);
        }

        return relationship;
    }

    public static void saveRelationShipByInfo(JSONObject info, String userId)   {
        createRelationshipByInfo(info, userId);
    }

    /**
     * Find Methods
     * @param userId
     * @param partnerId
     * @return
     */

    public static MXRelationship findRelationshipByUserId(String userId, String partnerId)  {
        MXRelationship relationship = MXRelationshipDBHelper.getInstnace().getRelationship(partnerId, userId);
        return relationship;
    }

    public static ArrayList<MXUser> findPartnersByUserId(String user_id)    {
        ArrayList<MXRelationship> partners = MXRelationshipDBHelper.getInstnace().getCollectionRelationships(user_id);
        Collections.sort(partners, new VisitComparator());

        ArrayList<MXUser> partnerUsers = new ArrayList<>();

        for (MXRelationship rel : partners) {
            MXUser u = MXUserUtil.findByUserId(rel.partner_id);
            if (u != null)
                u.getRelationLists();
            if (AppUtils.isNotEmptyObject(u) && (u.sentMessages.size() > 0 || u.receivedMessages.size() > 0))
                partnerUsers.add(u);
        }

        return partnerUsers;
    }

    /**
     * compare methods
     */
    public static class VisitComparator implements Comparator<MXRelationship> {
        @Override
        public int compare(MXRelationship o1, MXRelationship o2) {
            return o2.last_message_date.compareTo(o1.last_message_date);
        }
    }
}
