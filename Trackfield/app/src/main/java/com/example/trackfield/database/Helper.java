package com.example.trackfield.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class Helper extends SQLiteOpenHelper {

    protected SQLiteDatabase db;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Trackfield.db";

    ////

    protected Helper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // extends SQLiteOpenHelper

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Contract.ExerciseEntry.CREATE_TABLE);
        db.execSQL(Contract.SubEntry.CREATE_TABLE);
        db.execSQL(Contract.RouteEntry.CREATE_TABLE);
        db.execSQL(Contract.DistanceEntry.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(Contract.ExerciseEntry.DELETE_TABLE);
        db.execSQL(Contract.SubEntry.DELETE_TABLE);
        db.execSQL(Contract.RouteEntry.DELETE_TABLE);
        db.execSQL(Contract.DistanceEntry.DELETE_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
