package com.example.donxing.starnote.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.donxing.starnote.MyApplication;
import com.example.donxing.starnote.bean.Note;

import java.util.LinkedList;

public class NoteDbHelpBusiness {

    private static NoteDbHelpBusiness dbBus = null;
    private NotesDatabaseHelper mHelper;
    private SQLiteDatabase db;
    private  NoteDbHelpBusiness(Context context){
        mHelper = NotesDatabaseHelper.getInstance(context);
    }

    public synchronized static NoteDbHelpBusiness getInstance(Context context){
        if(dbBus == null){
            dbBus = new NoteDbHelpBusiness(context);
        }
        return dbBus;
    }
    public NotesDatabaseHelper getmHelper(){
        return mHelper;
    }

    //获得所有便签 用于展示 “所有便签” 这个虚拟分组
    public LinkedList<Note> getAll(){
        LinkedList<Note> notes = new LinkedList<Note>();
        db = mHelper.getReadableDatabase();
        Cursor cursor=db.query(NotesDatabaseHelper.TABLE.NOTE,null,null,null,null,null,null);
        while(cursor.moveToNext()){
            Note note = new Note();
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String subContent = cursor.getString(cursor.getColumnIndex("subContent"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            String groupName = cursor.getString(cursor.getColumnIndex("groupName"));
            String createTime = cursor.getString(cursor.getColumnIndex("createTime"));

            note.setId(id); note.setContent(content); note.setGroupName(groupName); note.setCreateTime(createTime);
            note.setTitle(title);note.setSubContent(subContent);

            notes.add(note);
        }
        cursor.close();
        return notes;
    }

    //获得一个组的所有便签
    public LinkedList<Note> getNotesByGroup(String GroupName){
        LinkedList<Note> notes = new LinkedList<Note>();
        db = mHelper.getReadableDatabase();
        Cursor cursor=db.query(NotesDatabaseHelper.TABLE.NOTE,null,"groupName = ?",new String[]{GroupName},null,null,null);
        while(cursor.moveToNext()){
            Note note = new Note();
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String subContent = cursor.getString(cursor.getColumnIndex("subContent"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            String groupName = cursor.getString(cursor.getColumnIndex("groupName"));
            String createTime = cursor.getString(cursor.getColumnIndex("createTime"));

            note.setId(id); note.setContent(content); note.setGroupName(groupName); note.setCreateTime(createTime);
            note.setTitle(title);note.setSubContent(subContent);

            notes.add(note);
        }
        cursor.close();
        return notes;
    }

    public void addNote(Note note){
        db = mHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title",note.getTitle());
        contentValues.put("subContent",note.getSubContent());
        contentValues.put("content",note.getContent());
        contentValues.put("createTime",note.getCreateTime());
        contentValues.put("groupName",note.getGroupName());

        db.insert(NotesDatabaseHelper.TABLE.NOTE,null,contentValues);
    }

    public void deleteNote(Note note){
        db = mHelper.getWritableDatabase();

        Integer id = note.getId();
        String id1 = id.toString();

        db.delete(NotesDatabaseHelper.TABLE.NOTE,"id = ?",new String[]{id1});
    }

    public String getNoteContentById(int id){
        String mContent = "";
        String id1 = Integer.toString(id);
        db = mHelper.getReadableDatabase();
        Cursor cursor=db.query(NotesDatabaseHelper.TABLE.NOTE,null,"id = ?",new String[]{id1},null,null,null);
        if(cursor != null){
            mContent = cursor.getString(cursor.getColumnIndex("content"));
        }else{
            Log.d("根据id查找便签内容","找不到");
        }
        return mContent;
    }

    public Note getNoteById(int id){
        Note note = null;
        String id1 = Integer.toString(id);
        db = mHelper.getReadableDatabase();
        Cursor cursor=db.query(NotesDatabaseHelper.TABLE.NOTE,null,"id = ?",new String[]{id1},null,null,null);
        if(cursor != null){
            note = new Note();
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String subContent = cursor.getString(cursor.getColumnIndex("subContent"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            String groupName = cursor.getString(cursor.getColumnIndex("groupName"));
            String createTime = cursor.getString(cursor.getColumnIndex("createTime"));

            note.setId(id); note.setContent(content); note.setGroupName(groupName); note.setCreateTime(createTime);
            note.setTitle(title);note.setSubContent(subContent);
        }else{
            Log.d("根据id查找便签","找不到");
        }
        return note;
    }
}
