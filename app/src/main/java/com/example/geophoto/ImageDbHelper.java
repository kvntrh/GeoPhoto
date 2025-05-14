package com.example.geophoto;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class ImageDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ImageGallery.db";

    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + ImageContract.ImageEntry.TABLE_NAME + " (" +
            ImageContract.ImageEntry._ID + " INTEGER PRIMARY KEY," +
            ImageContract.ImageEntry.COLUMN_NAME_TITLE + " TEXT," +
            ImageContract.ImageEntry.COLUMN_NAME_IMAGE_DATA + " TEXT)";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ImageContract.ImageEntry.TABLE_NAME;

    public ImageDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}