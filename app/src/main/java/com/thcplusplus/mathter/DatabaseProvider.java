package com.thcplusplus.mathter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by BodY on 11/9/2018.
 */

class DatabaseProvider extends SQLiteOpenHelper {


    public static final String CREATE_CMD = "CREATE TABLE " + DatabaseInterface.TABLE_NAME + " ( " + DatabaseInterface.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DatabaseInterface.COLUMN_NAME + " TEXT NOT NULL, " + DatabaseInterface.COLUMN_RECORD + " INTEGER, " + DatabaseInterface.COLUMN_NUMBER_OF_OPERANDS_IN_START + " INTEGER, " +
            DatabaseInterface.COLUMN_TRIGGER + " INTEGER, " + DatabaseInterface.COLUMN_OPERATORS + " INTEGER, " + DatabaseInterface.COLUMN_PRIZES + " INTEGER, " +
            DatabaseInterface.COLUMN_GAME_DURATION + " INTEGER, " + DatabaseInterface.COLUMN_ACCURACY + " INTEGER);";

    public static final String DROP_CMD = "DROP TABLE IF EXISTS " + DatabaseInterface.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_CMD); //******this drop command must be changed , all old data must be send to new database*******//
        onCreate(db);
    }

    public DatabaseProvider(Context c) {
        super(c, DatabaseInterface.FILE_NAME, null, DatabaseInterface.VERSION);
    }
}
