package com.example.darkknight.doit2;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

    public class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "DoItDb";
        private static final int DB_VER = 1;
        private static final String DB_TABLE = "Tasks";
        public static final String DB_COLUMN1 = "task";
        public static final String DB_COLUMN2 = "dueDate";
        public static final String DB_COLUMN3 = "dueTime";


        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VER);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String query = String.format("CREATE TABLE %s (ID INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT, %s TEXT);", DB_TABLE, DB_COLUMN1, DB_COLUMN2, DB_COLUMN3);
            db.execSQL(query);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String query = String.format("DELETE TABLE IF EXISTS %s", DB_TABLE);
            db.execSQL(query);
            onCreate(db);
        }

        public void insertNewTask(String task, String dueDate, String dueTime) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DB_COLUMN1, task);
            values.put(DB_COLUMN2, dueDate);
            values.put(DB_COLUMN3, dueTime);
            db.insertWithOnConflict(DB_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.close();
        }

        public void deleteTask(String task) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(DB_TABLE, DB_COLUMN1 + " = ?", new String[]{task});
            db.close();
        }

        public ArrayList<String> getTaskList() {
            ArrayList<String> taskList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(DB_TABLE, new String[]{DB_COLUMN1}, null, null, null, null, null);
            while (cursor.moveToNext()) {
                int index = cursor.getColumnIndex(DB_COLUMN1);
                taskList.add(cursor.getString(index));
            }
            cursor.close();
            db.close();
            return taskList;
        }

        public void updateTask(String task, String oldTask){
            SQLiteDatabase db= this.getWritableDatabase();
            ContentValues values= new ContentValues();
            values.put(DB_COLUMN1, task);
            db.update(DB_TABLE, values, " task = \"" + oldTask + "\"", null);
        }

       /* public void insertDueDate(String dueDate){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DB_COLUMN2, dueDate);
            db.insertWithOnConflict(DB_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.close();
        }

        public void insertDueTime(String dueTime){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DB_COLUMN3, dueTime);
            db.insertWithOnConflict(DB_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.close();
        }*/

    }




