package com.example.mapsapppp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Map_markers.db";
    public static final String TABLE_MARKERS = "Markers";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "Address";
    public static final String COL_3 = "Latitude";
    public static final String COL_4 = "Longitude";
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_MARKERS +" (ID INTEGER PRIMARY KEY AUTOINCREMENT, Address TEXT, Latitude TEXT, Longitude TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists "+ TABLE_MARKERS);
        onCreate(db);
    }
    public boolean Add(String Address, double Lat,  double Longt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, Address);
        contentValues.put(COL_3, Lat);
        contentValues.put(COL_4, Longt);
        long result = db.insert(TABLE_MARKERS,null,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }
    public Cursor GetData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_MARKERS, null);
        return res;
    }
    public boolean Edit(String id, String Address, String Lat,  String Longt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, Address);
        contentValues.put(COL_3, Lat);
        contentValues.put(COL_4, Longt);
        long result = db.update(TABLE_MARKERS, contentValues, "ID = ?", new String[] {id});
        return true;
    }
    public Integer Delete(String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(TABLE_MARKERS, "ID = ?", new String[] {id});
    }
}
