package com.medx.android.models.chat.db.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.medx.android.models.chat.MXUser;

import java.util.ArrayList;

/**
 * Created by alexey on 9/12/16.
 */

public class MXUserDBHelper extends MXBaseDBHelper {

    /**
     * Properties field
     * @param context
     * @param name
     * @param factory
     * @param version
     */
    static MXUserDBHelper instance;

    private static final String TABLE_VALUE = "users";
    private static final String KEY_ID = "ID";
    private static final String KEY_ABOUT = "about";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_LOCATIONS = "locations";
    private static final String KEY_PREFERRED_FIRST_NAME = "preferred_first_name";
    private static final String KEY_PUBLIC_KEY = "public_key";
    private static final String KEY_SALUTATION = "salutation";
    private static final String KEY_SPECIALTY = "specialty";
    private static final String KEY_STATUS = "status";
    private static final String KEY_USER_ID = "user_id";

    private static final String[] COLUMNS = {KEY_ID, KEY_ABOUT, KEY_ADDRESS, KEY_FIRST_NAME, KEY_LAST_NAME, KEY_LOCATIONS, KEY_PREFERRED_FIRST_NAME, KEY_PUBLIC_KEY, KEY_SALUTATION, KEY_SPECIALTY, KEY_STATUS, KEY_USER_ID};

    /**
     * Init methods
     */
    public static MXUserDBHelper newInstance(Context context)   {
        if (instance == null)   {
            instance = new MXUserDBHelper(context);
        }

        return instance;
    }

    public static MXUserDBHelper  getInstance() {
        return instance;
    }

    public MXUserDBHelper(Context context)  {
        super(context);
    }

    public MXUserDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MXUserDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    /**
     * Manage db methods
     */
    public void addUser(MXUser user)    {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_ABOUT, user.about);
        values.put(KEY_ADDRESS, user.address);
        values.put(KEY_FIRST_NAME, user.first_name);
        values.put(KEY_LAST_NAME, user.last_name);
        values.put(KEY_LOCATIONS, user.locations);
        values.put(KEY_PREFERRED_FIRST_NAME, user.preferred_first_name);
        values.put(KEY_PUBLIC_KEY, user.public_key);
        values.put(KEY_SALUTATION, user.salutation);
        values.put(KEY_SPECIALTY, user.specialty);
        values.put(KEY_STATUS, user.status);
        values.put(KEY_USER_ID, user.user_id);

        // 3. insert
        db.insert(TABLE_VALUE, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public MXUser getUser(String user_id) {
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        try {
            Cursor cursor =
                    db.query(TABLE_VALUE, // a. table
                            COLUMNS, // b. column names
                            " user_id = ?", // c. selections
                            new String[]{user_id}, // d. selections args
                            null, // e. group by
                            null, // f. having
                            null, // g. order by
                            null); // h. limit

            // 3. if we got results get the first one
            if (cursor != null)
                cursor.moveToFirst();

            // 4. build document object
            MXUser temp = new MXUser();
            try {
                temp.about = cursor.getString(1);
                temp.address = cursor.getString(2);
                temp.first_name = cursor.getString(3);
                temp.last_name = cursor.getString(4);
                temp.locations = cursor.getString(5);
                temp.preferred_first_name = cursor.getString(6);
                temp.public_key = cursor.getString(7);
                temp.salutation = cursor.getString(8);
                temp.specialty = cursor.getString(9);
                temp.status = cursor.getInt(10);
                temp.user_id = cursor.getString(11);
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

    public ArrayList<MXUser> getAllUsers()  {
        ArrayList<MXUser> users = new ArrayList<>();

        // 1. build the query
        String query = "SELECT  * FROM "+TABLE_VALUE;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build document and add it to list
        MXUser temp = null;
        if (cursor.moveToFirst()) {
            do {
                temp = new MXUser();
                temp.about = cursor.getString(1);
                temp.address = cursor.getString(2);
                temp.first_name = cursor.getString(3);
                temp.last_name = cursor.getString(4);
                temp.locations = cursor.getString(5);
                temp.preferred_first_name = cursor.getString(6);
                temp.public_key = cursor.getString(7);
                temp.salutation = cursor.getString(8);
                temp.specialty = cursor.getString(9);
                temp.status = cursor.getInt(10);
                temp.user_id = cursor.getString(11);

                users.add(temp);
            } while (cursor.moveToNext());
        }
        db.close();

        // return documents
        return users;
    }

    public int updateUser(MXUser user) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        /* 2. create ContentValues to add key "column"/value */
        ContentValues values = new ContentValues();
        values.put(KEY_ABOUT, user.about);
        values.put(KEY_ADDRESS, user.address);
        values.put(KEY_FIRST_NAME, user.first_name);
        values.put(KEY_LAST_NAME, user.last_name);
        values.put(KEY_LOCATIONS, user.locations);
        values.put(KEY_PREFERRED_FIRST_NAME, user.preferred_first_name);
        values.put(KEY_PUBLIC_KEY, user.public_key);
        values.put(KEY_SALUTATION, user.salutation);
        values.put(KEY_SPECIALTY, user.specialty);
        values.put(KEY_STATUS, user.status);
        values.put(KEY_USER_ID, user.user_id);

        // 3. updating row
        int i = db.update(TABLE_VALUE, //table
                values, // column/value
                KEY_USER_ID+" = ?", // selections
                new String[] { user.user_id}); //selection args
        // 4. close
        db.close();

        return i;

    }

    // Deleting single user
    public void deleteUser(String user_id) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        // 2. delete
        db.delete(TABLE_VALUE,
                KEY_USER_ID+" = ?",
                new String[] { String.valueOf(user_id) });

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
