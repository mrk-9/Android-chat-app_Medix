package com.medx.android.models.chat.db.sqlite;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by alexey on 9/12/16.
 */

public class MXBaseDBHelper extends SQLiteOpenHelper {

    /**
     * Properties field
     * @param context
     * @param name
     * @param factory
     * @param version
     */
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "MXCoreDB";

    /**
     * Init methods
     */

    public MXBaseDBHelper(Context context)  {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public MXBaseDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MXBaseDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MESSAGE_TABLE = "CREATE TABLE "+"messages"+" ( "
                +"ID INTEGER PRIMARY KEY AUTOINCREMENT, " + "app_message_id TEXT, " + "filename TEXT, " + "height INTEGER DEFAULT 0, " + "is_encrypted TEXT, " + "message_id TEXT, " + "recipient_id TEXT, " + "sender_id TEXT, " + "sent_at LONG, " + "status INTEGER DEFAULT 0, " + "text TEXT, " + "type INTEGER DEFAULT 0, " + "url TEXT, " + "width INTEGER DEFAULT 0)";
        String CREATE_RELATIONSHIP_TABLE = "CREATE TABLE relationships ( " + "ID INTEGER PRIMARY KEY AUTOINCREMENT, " + "last_message_date TEXT, " + "partner_id TEXT, " + "user_id TEXT)";

        String CREATE_USER_TABLE = "CREATE TABLE users ( " + "ID INTEGER PRIMARY KEY AUTOINCREMENT, " + "about TEXT, " + "address TEXT, " + "first_name TEXT, " + "last_name TEXT, " + "locations TEXT, " + "preferred_first_name TEXT, " + "public_key TEXT, " + "salutation TEXT, " + "specialty TEXT, " + "status INTEGER DEFAULT 0, " + "user_id TEXT)";

        // create tables
        db.execSQL(CREATE_MESSAGE_TABLE);
        db.execSQL(CREATE_RELATIONSHIP_TABLE);
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
