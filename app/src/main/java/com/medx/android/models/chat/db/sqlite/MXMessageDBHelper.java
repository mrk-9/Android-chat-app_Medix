package com.medx.android.models.chat.db.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.medx.android.models.chat.MXMessage;
import com.medx.android.utils.app.AppUtils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by alexey on 9/11/16.
 */

public class MXMessageDBHelper extends MXBaseDBHelper {

    /**
     * Properties field
     * @param context
     * @param name
     * @param factory
     * @param version
     */

    static MXMessageDBHelper instance;

    private static final String TABLE_VALUE = "messages";
    private static final String KEY_ID = "ID";
    private static final String KEY_APP_MESSAGE_ID = "app_message_id";
    private static final String KEY_FILE_NAME = "filename";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_IS_ENCRYPTED = "is_encrypted";
    private static final String KEY_MESSAGE_ID = "message_id";
    private static final String KEY_RECIPIENT_ID = "recipient_id";
    private static final String KEY_SENDER_ID = "sender_id";
    private static final String KEY_SENT_AT = "sent_at";
    private static final String KEY_STATUS = "status";
    private static final String KEY_TEXT = "text";
    private static final String KEY_TYPE = "type";
    private static final String KEY_URL = "url";
    private static final String KEY_WIDTH = "width";

    private static final String[] COLUMNS = {KEY_ID, KEY_APP_MESSAGE_ID, KEY_FILE_NAME, KEY_HEIGHT, KEY_IS_ENCRYPTED, KEY_MESSAGE_ID, KEY_RECIPIENT_ID, KEY_SENDER_ID, KEY_SENT_AT, KEY_STATUS, KEY_TEXT, KEY_TYPE, KEY_URL, KEY_WIDTH};

    /**
     * Init methods
     * @param context
     */

    public static MXMessageDBHelper newInstance(Context context)    {
        if (instance == null)   {
            instance = new MXMessageDBHelper(context);
        }

        return instance;
    }

    public static MXMessageDBHelper getInstance()   {
        return instance;
    }

    public MXMessageDBHelper(Context context)   {
        super(context);
    }
    public MXMessageDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MXMessageDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    /**
     * Manage db methods
     */
    public void addMessage(MXMessage message)    {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_APP_MESSAGE_ID, message.app_message_id);
        values.put(KEY_FILE_NAME, message.fileName);
        values.put(KEY_HEIGHT, message.height);
        values.put(KEY_IS_ENCRYPTED, message.is_encrypted);
        values.put(KEY_MESSAGE_ID, message.message_id);
        values.put(KEY_RECIPIENT_ID, message.recipient_id);
        values.put(KEY_SENDER_ID, message.sender_id);
        values.put(KEY_SENT_AT, AppUtils.MillionsFromDate(message.sent_at));
        values.put(KEY_STATUS, message.status);
        values.put(KEY_TEXT, message.text);
        values.put(KEY_TYPE, message.type);
        values.put(KEY_URL, message.url);
        values.put(KEY_WIDTH, message.width);

        // 3. insert
        db.insert(TABLE_VALUE, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public MXMessage getMessage(String app_message_id) {
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        try {
            Cursor cursor =
                    db.query(TABLE_VALUE, // a. table
                            COLUMNS, // b. column names
                            " app_message_id = ?", // c. selections
                            new String[]{String.valueOf(app_message_id)}, // d. selections args
                            null, // e. group by
                            null, // f. having
                            null, // g. order by
                            null); // h. limit

            // 3. if we got results get the first one
            if (cursor != null)
                cursor.moveToFirst();

            // 4. build message object
            MXMessage temp = new MXMessage();
            try {
                temp.app_message_id = cursor.getString(1);
                temp.fileName = cursor.getString(2);
                temp.height = cursor.getInt(3);
                temp.is_encrypted = cursor.getString(4);
                temp.message_id = cursor.getString(5);
                temp.recipient_id = cursor.getString(6);
                temp.sender_id = cursor.getString(7);
                temp.sent_at = AppUtils.DateFromMillions(cursor.getLong(8));
                temp.status = cursor.getInt(9);
                temp.text = cursor.getString(10);
                temp.type = cursor.getInt(11);
                temp.url = cursor.getString(12);
                temp.width = cursor.getInt(13);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            db.close();
            // 5. return message
            return temp;
        }   catch (SQLiteException se)  {
            se.printStackTrace();
            return null;
        }
    }

    public MXMessage getMessage(String  user_id1, String user_id2)  {
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        try {
            Cursor cursor =
                    db.query(TABLE_VALUE, // a. table
                            COLUMNS, // b. column names
                            " ((sender_id = ? AND recipient_id = ?) OR (sender_id = ? AND recipient_id = ?))", // c. selections
                            new String[]{user_id1, user_id2, user_id2, user_id1}, // d. selections args
                            null, // e. group by
                            null, // f. having
                            null, // g. order by
                            null); // h. limit

            // 3. if we got results get the first one
            if (cursor != null)
                cursor.moveToFirst();

            // 4. build message object
            MXMessage temp = new MXMessage();
            try {
                temp.app_message_id = cursor.getString(1);
                temp.fileName = cursor.getString(2);
                temp.height = cursor.getInt(3);
                temp.is_encrypted = cursor.getString(4);
                temp.message_id = cursor.getString(5);
                temp.recipient_id = cursor.getString(6);
                temp.sender_id = cursor.getString(7);
                temp.sent_at = AppUtils.DateFromMillions(cursor.getLong(8));
                temp.status = cursor.getInt(9);
                temp.text = cursor.getString(10);
                temp.type = cursor.getInt(11);
                temp.url = cursor.getString(12);
                temp.width = cursor.getInt(13);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            db.close();
            // 5. return message
            return temp;
        }   catch (SQLiteException se)  {
            se.printStackTrace();
            return null;
        }
    }

    public ArrayList<MXMessage> getAllMessages()  {
        ArrayList<MXMessage> messages = new ArrayList<>();

        // 1. build the query
        String query = "SELECT  * FROM "+TABLE_VALUE;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build document and add it to list
        MXMessage temp = null;
        if (cursor.moveToFirst()) {
            do {
                temp = new MXMessage();
                temp.app_message_id = cursor.getString(1);
                temp.fileName = cursor.getString(2);
                temp.height = cursor.getInt(3);
                temp.is_encrypted = cursor.getString(4);
                temp.message_id = cursor.getString(5);
                temp.recipient_id = cursor.getString(6);
                temp.sender_id = cursor.getString(7);
                temp.sent_at = AppUtils.DateFromMillions(cursor.getLong(8));
                temp.status = cursor.getInt(9);
                temp.text = cursor.getString(10);
                temp.type = cursor.getInt(11);
                temp.url = cursor.getString(12);
                temp.width = cursor.getInt(13);
                messages.add(temp);
            } while (cursor.moveToNext());
        }
        db.close();

        // return messages
        return messages;
    }

    public ArrayList<MXMessage> getCollectionMessages(String query, String sender_id, String recipient_id)    {
        ArrayList<MXMessage> messages = new ArrayList<>();

        query = "SELECT  * FROM "+ TABLE_VALUE + query;
        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{sender_id, recipient_id, recipient_id, sender_id});

        // 3. go over each row, build message and add it to list
        MXMessage temp = null;
        if (cursor.moveToFirst()) {
            do {
                temp = new MXMessage();
                temp.app_message_id = cursor.getString(1);
                temp.fileName = cursor.getString(2);
                temp.height = cursor.getInt(3);
                temp.is_encrypted = cursor.getString(4);
                temp.message_id = cursor.getString(5);
                temp.recipient_id = cursor.getString(6);
                temp.sender_id = cursor.getString(7);
                temp.sent_at = AppUtils.DateFromMillions(cursor.getLong(8));
                temp.status = cursor.getInt(9);
                temp.text = cursor.getString(10);
                temp.type = cursor.getInt(11);
                temp.url = cursor.getString(12);
                temp.width = cursor.getInt(13);

                messages.add(temp);
            } while (cursor.moveToNext());
        }
        db.close();

        // return messages
        return messages;
    }

    public ArrayList<MXMessage> getCollectionMessages(String query, String user_id)    {
        ArrayList<MXMessage> messages = new ArrayList<>();

        query = "SELECT  * FROM "+TABLE_VALUE + query;
        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{user_id});

        // 3. go over each row, build message and add it to list
        MXMessage temp = null;
        if (cursor.moveToFirst()) {
            do {
                temp = new MXMessage();
                temp.app_message_id = cursor.getString(1);
                temp.fileName = cursor.getString(2);
                temp.height = cursor.getInt(3);
                temp.is_encrypted = cursor.getString(4);
                temp.message_id = cursor.getString(5);
                temp.recipient_id = cursor.getString(6);
                temp.sender_id = cursor.getString(7);
                temp.sent_at = AppUtils.DateFromMillions(cursor.getLong(8));
                temp.status = cursor.getInt(9);
                temp.text = cursor.getString(10);
                temp.type = cursor.getInt(11);
                temp.url = cursor.getString(12);
                temp.width = cursor.getInt(13);

                messages.add(temp);
            } while (cursor.moveToNext());
        }
        db.close();

        // return messages
        return messages;
    }

    public ArrayList<MXMessage> getCollectionMessages(String query, String sender_id, String recipient_id, Date sent_date)    {
        ArrayList<MXMessage> messages = new ArrayList<>();

        query = "SELECT  * FROM "+TABLE_VALUE + query;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (sent_date == null)
            cursor = db.rawQuery(query, new String[]{sender_id, recipient_id, recipient_id, sender_id});
        else
            cursor = db.rawQuery(query, new String[]{sender_id, recipient_id, recipient_id, sender_id, String.valueOf(AppUtils.MillionsFromDate(sent_date))});


        // 3. go over each row, build message and add it to list
        MXMessage temp = null;
        if (cursor.moveToFirst()) {
            do {
                temp = new MXMessage();
                temp.app_message_id = cursor.getString(1);
                temp.fileName = cursor.getString(2);
                temp.height = cursor.getInt(3);
                temp.is_encrypted = cursor.getString(4);
                temp.message_id = cursor.getString(5);
                temp.recipient_id = cursor.getString(6);
                temp.sender_id = cursor.getString(7);
                temp.sent_at = AppUtils.DateFromMillions(cursor.getLong(8));
                temp.status = cursor.getInt(9);
                temp.text = cursor.getString(10);
                temp.type = cursor.getInt(11);
                temp.url = cursor.getString(12);
                temp.width = cursor.getInt(13);

                messages.add(temp);
            } while (cursor.moveToNext());
        }
        db.close();

        // return messages
        return messages;
    }

    public ArrayList<MXMessage> getCollectionMessages(String query, String recipient_id, String sender_id, int status)    {
        ArrayList<MXMessage> messages = new ArrayList<>();

        query = "SELECT  * FROM "+TABLE_VALUE + query;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{recipient_id, sender_id, String.valueOf(status)});

        // 3. go over each row, build message and add it to list
        MXMessage temp = null;
        if (cursor.moveToFirst()) {
            do {
                temp = new MXMessage();
                temp.app_message_id = cursor.getString(1);
                temp.fileName = cursor.getString(2);
                temp.height = cursor.getInt(3);
                temp.is_encrypted = cursor.getString(4);
                temp.message_id = cursor.getString(5);
                temp.recipient_id = cursor.getString(6);
                temp.sender_id = cursor.getString(7);
                temp.sent_at = AppUtils.DateFromMillions(cursor.getLong(8));
                temp.status = cursor.getInt(9);
                temp.text = cursor.getString(10);
                temp.type = cursor.getInt(11);
                temp.url = cursor.getString(12);
                temp.width = cursor.getInt(13);
                messages.add(temp);
            } while (cursor.moveToNext());
        }
        db.close();

        // return messages
        return messages;
    }

    public ArrayList<MXMessage> getCollectionMessages(String query, String recipient_id, int status)    {
        ArrayList<MXMessage> messages = new ArrayList<>();

        query = "SELECT  * FROM "+TABLE_VALUE + query;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{recipient_id, String.valueOf(status)});

        // 3. go over each row, build message and add it to list
        MXMessage temp = null;
        if (cursor.moveToFirst()) {
            do {
                temp = new MXMessage();
                temp.app_message_id = cursor.getString(1);
                temp.fileName = cursor.getString(2);
                temp.height = cursor.getInt(3);
                temp.is_encrypted = cursor.getString(4);
                temp.message_id = cursor.getString(5);
                temp.recipient_id = cursor.getString(6);
                temp.sender_id = cursor.getString(7);
                temp.sent_at = AppUtils.DateFromMillions(cursor.getLong(8));
                temp.status = cursor.getInt(9);
                temp.text = cursor.getString(10);
                temp.type = cursor.getInt(11);
                temp.url = cursor.getString(12);
                temp.width = cursor.getInt(13);

                messages.add(temp);
            } while (cursor.moveToNext());
        }
        db.close();

        // return messages
        return messages;
    }

    public ArrayList<MXMessage> getCollectionMessagesByUserID(String query, String user_id)    {
        ArrayList<MXMessage> messages = new ArrayList<>();

        query = "SELECT  * FROM "+TABLE_VALUE + query;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{user_id, user_id});

        // 3. go over each row, build message and add it to list
        MXMessage temp = null;
        if (cursor.moveToFirst()) {
            do {
                temp = new MXMessage();
                temp.app_message_id = cursor.getString(1);
                temp.fileName = cursor.getString(2);
                temp.height = cursor.getInt(3);
                temp.is_encrypted = cursor.getString(4);
                temp.message_id = cursor.getString(5);
                temp.recipient_id = cursor.getString(6);
                temp.sender_id = cursor.getString(7);
                temp.sent_at = AppUtils.DateFromMillions(cursor.getLong(8));
                temp.status = cursor.getInt(9);
                temp.text = cursor.getString(10);
                temp.type = cursor.getInt(11);
                temp.url = cursor.getString(12);
                temp.width = cursor.getInt(13);
                messages.add(temp);
            } while (cursor.moveToNext());
        }
        db.close();

        // return messages
        return messages;
    }

    public int updateMessage(MXMessage message) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        /* 2. create ContentValues to add key "column"/value */
        ContentValues values = new ContentValues();
        values.put(KEY_APP_MESSAGE_ID, message.app_message_id);
        values.put(KEY_FILE_NAME, message.fileName);
        values.put(KEY_HEIGHT, message.height);
        values.put(KEY_IS_ENCRYPTED, message.is_encrypted);
        values.put(KEY_MESSAGE_ID, message.message_id);
        values.put(KEY_RECIPIENT_ID, message.recipient_id);
        values.put(KEY_SENDER_ID, message.sender_id);
        values.put(KEY_SENT_AT, AppUtils.MillionsFromDate(message.sent_at));
        values.put(KEY_STATUS, message.status);
        values.put(KEY_TEXT, message.text);
        values.put(KEY_TYPE, message.type);
        values.put(KEY_URL, message.url);
        values.put(KEY_WIDTH, message.width);

        // 3. updating row
        int i = db.update(TABLE_VALUE, //table
                values, // column/value
                KEY_APP_MESSAGE_ID+" = ?", // selections
                new String[] { message.app_message_id}); //selection args
        // 4. close
        db.close();

        return i;

    }

    // Deleting single document
    public void deleteMessage(String app_message_id) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. delete
        db.delete(TABLE_VALUE,
                KEY_APP_MESSAGE_ID+" = ?",
                new String[] { String.valueOf(app_message_id) });

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
