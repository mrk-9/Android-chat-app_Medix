package com.medx.android.models.chat.db;

import android.content.Context;

import com.medx.android.models.chat.db.sqlite.MXMessageDBHelper;
import com.medx.android.models.chat.db.sqlite.MXRelationshipDBHelper;
import com.medx.android.models.chat.db.sqlite.MXUserDBHelper;

/**
 * Created by alexey on 9/12/16.
 */

public class DatabaseHelper {

    public static void newDBInstance(Context context)  {
        MXUserDBHelper.newInstance(context);
        MXRelationshipDBHelper.newInstance(context);
        MXMessageDBHelper.newInstance(context);
    }
}
