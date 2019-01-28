package com.example.donxing.starnote.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.donxing.starnote.bean.Note;

import java.sql.ResultSet;
import java.util.LinkedList;

public class NotesDatabaseHelper extends SQLiteOpenHelper {

    private static  NotesDatabaseHelper mInstance;
    private static  final String DB_NAME = "note.db";
    private static final int DB_VERSION = 1;

    public interface TABLE {
        public static final String NOTE = "note";

    }

    public NotesDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_NOTE_TABLE =
                "CREATE TABLE note(id INTEGER PRIMARY KEY AUTOINCREMENT,title VARCHAR(30),subContent VARCHAR(30),content text, groupName VARCHAR(20), createTime VARCHAR(20))";
        sqLiteDatabase.execSQL(CREATE_NOTE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public NotesDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    static synchronized NotesDatabaseHelper getInstance(Context context){
        if(mInstance == null){
            mInstance = new NotesDatabaseHelper(context);
        }
        return mInstance;
    }
}
