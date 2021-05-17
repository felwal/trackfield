package com.example.trackfield.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_TARGET_VERSION = 1;
    public static final String DATABASE_NAME = "Trackfield.db";

    protected SQLiteDatabase db;

    //

    protected DbHelper(Context context) {
        // version: the target version. calls onUpgrade or onDowngrade if database is of different version
        super(context, DATABASE_NAME, null, DATABASE_TARGET_VERSION);
    }

    // extends SQLiteOpenHelper

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbContract.ExerciseEntry.CREATE_TABLE);
        db.execSQL(DbContract.SubEntry.CREATE_TABLE);
        db.execSQL(DbContract.RouteEntry.CREATE_TABLE);
        db.execSQL(DbContract.DistanceEntry.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            // oldVersion 0 is used to simply recreate
            db.execSQL(DbContract.ExerciseEntry.DELETE_TABLE);
            db.execSQL(DbContract.SubEntry.DELETE_TABLE);
            db.execSQL(DbContract.RouteEntry.DELETE_TABLE);
            db.execSQL(DbContract.DistanceEntry.DELETE_TABLE);
            onCreate(db);
        }
        //if (oldVersion < 2) {
        //    db.execSQL(Contract.ExerciseEntry.ALTER_TO_VER_2);
        //}
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DbContract.ExerciseEntry.DELETE_TABLE);
        db.execSQL(DbContract.SubEntry.DELETE_TABLE);
        db.execSQL(DbContract.RouteEntry.DELETE_TABLE);
        db.execSQL(DbContract.DistanceEntry.DELETE_TABLE);
        onCreate(db);
    }

}
