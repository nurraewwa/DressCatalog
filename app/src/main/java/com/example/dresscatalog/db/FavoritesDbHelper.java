package com.example.dresscatalog.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoritesDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "dress_catalog.db";
    public static final int DB_VERSION = 2; // было 1 → стало 2

    public static final String TABLE = "favorites";
    public static final String COL_DRESS_ID = "dress_id";
    public static final String COL_NOTE = "note";

    public FavoritesDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                        COL_DRESS_ID + " INTEGER PRIMARY KEY, " +
                        COL_NOTE + " TEXT" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Миграция v1 -> v2: добавляем колонку note
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE + " ADD COLUMN " + COL_NOTE + " TEXT");
        }
    }
}
