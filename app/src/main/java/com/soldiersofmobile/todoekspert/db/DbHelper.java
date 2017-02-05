package com.soldiersofmobile.todoekspert.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

    public static final String TODOS_DB = "todos.db";
    public static final int VERSION = 1;

    public DbHelper(Context context) {
        super(context, TODOS_DB, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("CREATE TABLE %s " +
                        "(%s TEXT PRIMARY KEY NOT NULL," +
                        " %s TEXT," +
                        " %s INT," +
                        " %s INT," +
                        " %s INT," +
                        " %s TEXT)",
                TodoDao.TABLE_NAME,
                TodoDao.C_ID,
                TodoDao.C_CONTENT,
                TodoDao.C_DONE,
                TodoDao.C_CREATED_AT,
                TodoDao.C_UPDATED_AT,
                TodoDao.C_USER_ID);

        Log.d("TAG", sql);

        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
