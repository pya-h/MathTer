package com.thcplusplus.mathter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BodY on 11/9/2018.
 */

class DatabaseInterface {
    private DatabaseProvider sql;
    private SQLiteDatabase db;

    public static final String FILE_NAME = "rdata.db",TABLE_NAME = "Records",COLUMN_ID = "_id",COLUMN_NAME = "Name",COLUMN_RECORD = "Record",
            COLUMN_NUMBER_OF_OPERANDS_IN_START = "StartNumberOfOperands", COLUMN_TRIGGER = "OperandTrigger", COLUMN_OPERATORS = "Operators",COLUMN_PRIZES= "AllPrizes", COLUMN_GAME_DURATION = "GameDuration", COLUMN_ACCURACY="Accuracy";
    public static final int VERSION = 1;
    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME , COLUMN_RECORD , COLUMN_NUMBER_OF_OPERANDS_IN_START, COLUMN_TRIGGER, COLUMN_OPERATORS, COLUMN_PRIZES, COLUMN_GAME_DURATION, COLUMN_ACCURACY};

    public DatabaseInterface(Context c){
        sql = new DatabaseProvider(c);
    }

    public void open() throws SQLException{
        db = sql.getWritableDatabase();
    }

    public void close(){
        db.close();
    }

    public Record createRecord(String name, int value, byte numberOfOperandsInStart, byte trigger, byte operators , int allTimePrizes, int gameDuartion, float accuracy){
        ContentValues cv = new ContentValues();

        //**********optimize this piece with for loop
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_RECORD, value);
        cv.put(COLUMN_NUMBER_OF_OPERANDS_IN_START, numberOfOperandsInStart);
        cv.put(COLUMN_TRIGGER, trigger);
        cv.put(COLUMN_OPERATORS, operators);
        cv.put(COLUMN_PRIZES, allTimePrizes);
        cv.put(COLUMN_GAME_DURATION, gameDuartion);
        cv.put(COLUMN_ACCURACY, accuracy);

        long id = db.insert(TABLE_NAME, null, cv);
        Cursor cr = db.query(TABLE_NAME, ALL_COLUMNS , COLUMN_ID + " = " + id,null,null,null,null);
        cr.moveToFirst();
        return new Record(cr.getInt(0),cr.getString(1),cr.getInt(2),cr.getInt(3),cr.getInt(4),cr.getInt(5), cr.getInt(6), cr.getInt(7), cr.getFloat(8));
    }

    public List<Record> getAllRecords(Boolean sorted, Boolean asc){
        List<Record> result = new ArrayList<Record>();
        String orderBy = asc ? " ASC" : " DESC";
        Cursor cr = db.query(TABLE_NAME, ALL_COLUMNS,null,null,null,null,sorted ? ( COLUMN_RECORD + orderBy ): null);
        cr.moveToFirst();
        while(!cr.isAfterLast()){
            Record nextRecord = new Record(cr.getInt(0),cr.getString(1),cr.getInt(2),cr.getInt(3),cr.getInt(4),cr.getInt(5), cr.getInt(6), cr.getInt(7), cr.getFloat(8));
            result.add(nextRecord);
            cr.moveToNext();
        }
        cr.close();
        return result;
    }
}
