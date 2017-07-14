package com.medx.android.utils.controller;

import android.content.Intent;

import com.medx.android.App;
import com.medx.android.interfaces.CompletionListener;
import com.medx.android.models.chat.MXUser;
import com.medx.android.models.user.MedXUser;
import com.medx.android.utils.app.Utils;
import com.medx.android.utils.chat.MXMessageUtil;
import com.medx.android.utils.chat.MXRelationshipUtil;

import java.util.ArrayList;

/**
 * Created by alexey on 10/23/16.
 */

public class ChatHistoryLoadController {

    String userId;

    public ChatHistoryLoadController(String userId) {
        this.userId = userId;
    }

    public void loadData()  {
        MXMessageUtil.deleteOldMessagesForUserID(userId, new CompletionListener() {
            @Override
            public void complete(boolean success, String errorStatus) {
                if (success)    {

                    Utils.partners = new ArrayList<>();
                    ArrayList<MXUser> partners = MXRelationshipUtil.findPartnersByUserId(userId);

                    MedXUser.CurrentUser().resetApplicationBadge();

                    Utils.partners = partners;

                    Intent intent = new Intent("loadHistory");
                    App.DefaultContext.sendBroadcast(intent);
                }
            }
        });
    }
}
