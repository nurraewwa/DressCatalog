package com.example.dresscatalog.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedHashSet;
import java.util.Set;

public class FavoritesStore {

    private final FavoritesDbHelper helper;

    public FavoritesStore(Context context) {
        helper = new FavoritesDbHelper(context.getApplicationContext());
    }



    public boolean isFavorite(int dressId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM " + FavoritesDbHelper.TABLE +
                        " WHERE " + FavoritesDbHelper.COL_DRESS_ID + "=? LIMIT 1",
                new String[]{String.valueOf(dressId)}
        );
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    public void toggle(int dressId) {
        if (isFavorite(dressId)) remove(dressId);
        else add(dressId);
    }

    public void add(int dressId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FavoritesDbHelper.COL_DRESS_ID, dressId);

        db.insertWithOnConflict(
                FavoritesDbHelper.TABLE,
                null,
                cv,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    public void remove(int dressId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(
                FavoritesDbHelper.TABLE,
                FavoritesDbHelper.COL_DRESS_ID + "=?",
                new String[]{String.valueOf(dressId)}
        );
    }

    public Set<Integer> getAllFavoriteIds() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + FavoritesDbHelper.COL_DRESS_ID +
                        " FROM " + FavoritesDbHelper.TABLE +
                        " ORDER BY " + FavoritesDbHelper.COL_DRESS_ID + " ASC",
                null
        );

        Set<Integer> set = new LinkedHashSet<>();
        while (c.moveToNext()) {
            set.add(c.getInt(0));
        }
        c.close();
        return set;
    }



    public String getNote(int dressId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + FavoritesDbHelper.COL_NOTE +
                        " FROM " + FavoritesDbHelper.TABLE +
                        " WHERE " + FavoritesDbHelper.COL_DRESS_ID + "=? LIMIT 1",
                new String[]{String.valueOf(dressId)}
        );

        String note = null;
        if (c.moveToFirst()) {
            if (!c.isNull(0)) note = c.getString(0);
        }
        c.close();
        return note;
    }



    public void saveNote(int dressId, String note) {

        add(dressId);

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FavoritesDbHelper.COL_NOTE, note);

        db.update(
                FavoritesDbHelper.TABLE,
                cv,
                FavoritesDbHelper.COL_DRESS_ID + "=?",
                new String[]{String.valueOf(dressId)}
        );
    }


    public void clearNote(int dressId) {
        saveNote(dressId, null);
    }
}
