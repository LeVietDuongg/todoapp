package com.example.todoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "todo.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TASK = "task";
    private static final String COLUMN_HOUR = "hour";
    private static final String COLUMN_MINUTE = "minute";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TASK + " TEXT, " +
                COLUMN_HOUR + " INTEGER, " +
                COLUMN_MINUTE + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Migration from version 1 to version 2
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_HOUR + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_MINUTE + " INTEGER DEFAULT 0");
        }
    }

    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK, task.getDescription());
        values.put(COLUMN_HOUR, task.getHour());
        values.put(COLUMN_MINUTE, task.getMinute());
        
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String sortOrder = COLUMN_HOUR + ", " + COLUMN_MINUTE + " ASC";
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, sortOrder);
        
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK));
                int hour = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOUR));
                int minute = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINUTE));
                
                tasks.add(new Task(id, description, hour, minute));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return tasks;
    }

    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[] { String.valueOf(taskId) });
        db.close();
    }

    public void deleteAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }
    
    public void deleteTasks(List<Integer> taskIds) {
        if (taskIds.isEmpty()) return;
        
        SQLiteDatabase db = this.getWritableDatabase();
        StringBuilder whereClause = new StringBuilder(COLUMN_ID + " IN (");
        
        for (int i = 0; i < taskIds.size(); i++) {
            whereClause.append("?");
            if (i < taskIds.size() - 1) {
                whereClause.append(",");
            }
        }
        whereClause.append(")");
        
        String[] whereArgs = new String[taskIds.size()];
        for (int i = 0; i < taskIds.size(); i++) {
            whereArgs[i] = String.valueOf(taskIds.get(i));
        }
        
        db.delete(TABLE_NAME, whereClause.toString(), whereArgs);
        db.close();
    }
}
