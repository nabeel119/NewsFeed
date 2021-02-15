package com.example.newsfeed.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;

import org.w3c.dom.Text;

import java.util.List;

public class SQLiteDatabaseHandler extends SQLiteOpenHelper {

    List<Article> sqlList;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "TranslateDB";
    private static final String TABLE_NAME = "DescriptionTb";
    private static final String KEY_DESCRIPTION = "Description";
    private static final String[] COLUMNS = { KEY_DESCRIPTION};



    public SQLiteDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // you can implement here migration process
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }
    public void addDescription(String article) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DESCRIPTION, String.valueOf(article));
        Log.d("Des value",String.valueOf(KEY_DESCRIPTION));
        // insert
        db.insert(TABLE_NAME,null, values);
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATION_TABLE = "CREATE TABLE Description ("+"Description)";
        sqLiteDatabase.execSQL(CREATION_TABLE);
    }
}