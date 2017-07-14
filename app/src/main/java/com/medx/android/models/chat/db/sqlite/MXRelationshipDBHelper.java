package com.medx.android.models.chat.db.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.medx.android.models.chat.MXRelationship;
import com.medx.android.utils.app.AppUtils;

import java.util.ArrayList;

/**
 * Created by alexey on 9/12/16.
 */

public class MXRelationshipDBHelper extends MXBaseDBHelper {

    /**
     * Properties field
     * @param context
     * @param name
     * @param factory
     * @param version
     */
    static MXRelationshipDBHelper instnace;

    private static final String TABLE_VALUE = "relationships";
    private static final String KEY_ID = "ID";
    private static final String KEY_LAST_MESSAGE_DATE = "last_message_date";
    private static final String KEY_PARTNER_ID = "partner_id";
    private static final String KEY_USER_ID = "user_id";

    private static final String[] COLUMNS = {KEY_ID, KEY_LAST_MESSAGE_DATE, KEY_PARTNER_ID, KEY_USER_ID};

    /**
     * Init methods
     */

    public static MXRelationshipDBHelper newInstance(Context context)   {
        if (instnace == null)   {
            instnace = new MXRelationshipDBHelper(context);
        }

        return instnace;
    }

    public static MXRelationshipDBHelper getInstnace()  {
        return instnace;
    }

    public MXRelationshipDBHelper(Context context)  {
        super(context);
    }

    /**
     * Manage db methods
     */
    public void addRelationShip(MXRelationship relationship)    {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_LAST_MESSAGE_DATE, AppUtils.StringFromDate(relationship.last_message_date));
        values.put(KEY_PARTNER_ID, relationship.partner_id);
        values.put(KEY_USER_ID, relationship.user_id);

        // 3. insert
        db.insert(TABLE_VALUE, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public MXRelationship getRelationship(String partner_id, String user_id) {
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        try {
            Cursor cursor =
                    db.query(TABLE_VALUE, // a. table
                            COLUMNS, // b. column names
                            " partner_id = ? AND user_id = ?", // c. selections
                            new String[]{partner_id, user_id}, // d. selections args
                            null, // e. group by
                            null, // f. having
                            null, // g. order by
                            null); // h. limit

            // 3. if we got results get the first one
            if (cursor != null)
                cursor.moveToFirst();

            // 4. build document object
            MXRelationship temp = new MXRelationship();
            try {
                temp.last_message_date = AppUtils.DateFromString(cursor.getString(1));
                temp.partner_id = cursor.getString(2);
                temp.user_id = cursor.getString(3);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            db.close();
            // 5. return document
            return temp;
        }   catch (SQLiteException se)  {
            se.printStackTrace();
            return null;
        }
    }

    public ArrayList<MXRelationship> getAllRelationship()  {
        ArrayList<MXRelationship> messages = new ArrayList<>();

        // 1. build the query
        String query = "SELECT  * FROM "+TABLE_VALUE;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build document and add it to list
        MXRelationship temp = null;
        if (cursor.moveToFirst()) {
            do {
                temp = new MXRelationship();
                temp.last_message_date = AppUtils.DateFromString(cursor.getString(1));
                temp.partner_id = cursor.getString(2);
                temp.user_id = cursor.getString(3);

                messages.add(temp);
            } while (cursor.moveToNext());
        }
        db.close();

        // return documents
        return messages;
    }

    public ArrayList<MXRelationship> getCollectionRelationships(String user_id)    {
        ArrayList<MXRelationship> relationships = new ArrayList<>();

        // 1. build the query
        String query = "SELECT  * FROM "+TABLE_VALUE + " WHERE user_id = ?";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{user_id});

        // 3. go over each row, build message and add it to list
        MXRelationship temp = null;
        if (cursor.moveToFirst()) {
            do {
                temp = new MXRelationship();
                temp.last_message_date = AppUtils.DateFromString(cursor.getString(1));
                temp.partner_id = cursor.getString(2);
                temp.user_id = cursor.getString(3);

                relationships.add(temp);
            } while (cursor.moveToNext());
        }
        db.close();

        // return messages
        return relationships;
    }

    public int updateRelationship(MXRelationship relationship) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        /* 2. create ContentValues to add key "column"/value */
        ContentValues values = new ContentValues();
        values.put(KEY_LAST_MESSAGE_DATE, AppUtils.StringFromDate(relationship.last_message_date));
        values.put(KEY_PARTNER_ID, relationship.partner_id);
        values.put(KEY_USER_ID, relationship.user_id);

        // 3. updating row
        int i = db.update(TABLE_VALUE, //table
                values, // column/value
                KEY_USER_ID+" = ? AND " + KEY_PARTNER_ID + " = ?",
                new String[] { String.valueOf(relationship.user_id), String.valueOf(relationship.partner_id) }); //selection args
        // 4. close
        db.close();

        return i;

    }

    // Deleting single document
    public void deleteMessage(String user_id, String partner_id) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. delete
        db.delete(TABLE_VALUE,
                KEY_USER_ID+" = ? AND " + KEY_PARTNER_ID + " = ?",
                new String[] { String.valueOf(user_id), String.valueOf(partner_id) });

        // 3. close
        db.close();
    }

    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. delete
        db.delete(TABLE_VALUE,null,null);
        // 3. close
        db.close();
    }
}
