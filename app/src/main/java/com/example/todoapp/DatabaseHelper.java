package com.example.todoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "todo_db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_TASK_HISTORY = "task_history";

    // Tasks Table Columns
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DATE = "date";
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_COMPLETED = "completed";
    private static final String KEY_TYPE = "type";
    private static final String KEY_HOUR = "hour";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_DAY_NUMBER = "day_number";
    private static final String KEY_TASK_TYPE = "task_type";
    private static final String KEY_CATEGORY = "category";

    // Task History Table Columns
    private static final String KEY_TASK_ID = "task_id";
    private static final String KEY_COMPLETION_DATE = "completion_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_PRIORITY + " INTEGER,"
                + KEY_COMPLETED + " INTEGER,"
                + KEY_TYPE + " INTEGER DEFAULT 0,"
                + KEY_HOUR + " INTEGER DEFAULT 0,"
                + KEY_MINUTE + " INTEGER DEFAULT 0,"
                + KEY_DAY_NUMBER + " INTEGER DEFAULT 0,"
                + KEY_TASK_TYPE + " INTEGER DEFAULT 0,"
                + KEY_CATEGORY + " TEXT" + ")";
        db.execSQL(CREATE_TASKS_TABLE);

        String CREATE_TASK_HISTORY_TABLE = "CREATE TABLE " + TABLE_TASK_HISTORY + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TASK_ID + " INTEGER,"
                + KEY_COMPLETION_DATE + " TEXT,"
                + "FOREIGN KEY(" + KEY_TASK_ID + ") REFERENCES " + TABLE_TASKS + "(" + KEY_ID + ")" + ")";
        db.execSQL(CREATE_TASK_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }
    

    public int[] getCompletionStats() {
        int[] stats = new int[31]; // 0: tổng tiến độ, 1-30: tiến độ theo ngày
        
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Tính tổng phần trăm hoàn thành
        String totalQuery = "SELECT COUNT(*) as total, SUM(" + KEY_COMPLETED + ") as completed FROM " + TABLE_TASKS;
        Cursor totalCursor = db.rawQuery(totalQuery, null);
        
        if (totalCursor.moveToFirst()) {
            int total = totalCursor.getInt(0);
            int completed = totalCursor.getInt(1);
            
            if (total > 0) {
                stats[0] = (completed * 100) / total;
            }
        }
        totalCursor.close();
        
        // Lấy ngày hiện tại và tính toán ngày 30 ngày trước
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        // Tính toán phần trăm hoàn thành cho mỗi ngày trong 30 ngày gần nhất
        for (int day = 1; day <= 30; day++) {
            calendar.add(Calendar.DAY_OF_MONTH, -1); // Lùi lại 1 ngày
            String date = dateFormat.format(calendar.getTime());
            
            // Đếm tổng công việc và công việc hoàn thành trong ngày này
            String dayQuery = "SELECT COUNT(*) as total, SUM(" + KEY_COMPLETED + ") as completed FROM " + TABLE_TASKS +
                    " WHERE " + KEY_DATE + " = '" + date + "'";
            Cursor dayCursor = db.rawQuery(dayQuery, null);
            
            if (dayCursor.moveToFirst()) {
                int total = dayCursor.getInt(0);
                int completed = dayCursor.getInt(1);
                
                if (total > 0) {
                    stats[31 - day] = (completed * 100) / total; // Điền từ cuối mảng
                }
            }
            dayCursor.close();
        }
        
        return stats;
    }
    
    // Các phương thức khác của DatabaseHelper (thêm, cập nhật, xóa, v.v.)
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        // Lưu các thông tin cơ bản
        values.put(KEY_TITLE, task.getTitle());
        values.put(KEY_DESCRIPTION, task.getDescription());
        values.put(KEY_DATE, task.getDate());
        values.put(KEY_PRIORITY, task.getPriority());
        values.put(KEY_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(KEY_TYPE, task.getType());
        
        // Lưu thêm thông tin về giờ và phút
        values.put(KEY_HOUR, task.getHour());
        values.put(KEY_MINUTE, task.getMinute());
        
        // Lưu thông tin thêm nếu là nhiệm vụ tập luyện/thói quen
        values.put(KEY_DAY_NUMBER, task.getDayNumber());
        values.put(KEY_TASK_TYPE, task.getTaskType());
        values.put(KEY_CATEGORY, task.getCategory());
        
        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    public void updateTaskCompletion(int taskId, boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_COMPLETED, completed ? 1 : 0);
        
        db.update(TABLE_TASKS, values, KEY_ID + " = ?", 
                new String[] { String.valueOf(taskId) });
        db.close();
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Sắp xếp theo ngày và giờ
        String sortOrder = KEY_DATE + " ASC, " + KEY_HOUR + " ASC, " + KEY_MINUTE + " ASC";
        Cursor cursor = db.query(TABLE_TASKS, null, null, null, null, null, sortOrder);
        
        if (cursor.moveToFirst()) {
            do {
                tasks.add(getTaskFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return tasks;
    }

    // Thêm phương thức để lấy ngày cao nhất trong danh sách công việc
    public int getMaxDayNumber() {
        int maxDay = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT MAX(" + KEY_DAY_NUMBER + ") FROM " + TABLE_TASKS;
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            maxDay = cursor.getInt(0);
        }
        
        cursor.close();
        db.close();
        return maxDay;
    }



    public List<Task> getTasksByType(int type) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = KEY_TYPE + " = ?";
        String[] selectionArgs = { String.valueOf(type) };
        String sortOrder = KEY_DATE + " ASC";
        
        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null, sortOrder);
        
        if (cursor.moveToFirst()) {
            do {
                tasks.add(getTaskFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return tasks;
    }
    
    private Task getTaskFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE));
        int priority = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PRIORITY));
        boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COMPLETED)) == 1;
        
        // Try to get type, defaulting to TYPE_GENERAL if column doesn't exist
        int type = Task.TYPE_GENERAL;
        try {
            type = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TYPE));
        } catch (IllegalArgumentException e) {
            // TYPE column might not exist in older database versions
        }
        
        // Đọc thông tin về giờ và phút
        int hour = 0;
        int minute = 0;
        int dayNumber = 0;
        int taskType = Task.TYPE_NORMAL;
        String category = "General";
        
        try {
            hour = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_HOUR));
            minute = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MINUTE));
            dayNumber = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DAY_NUMBER));
            taskType = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TASK_TYPE));
            category = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY));
        } catch (IllegalArgumentException e) {

        }
        
        // Tạo task với đầy đủ thông tin
        Task task = new Task(id, description, hour, minute, dayNumber, taskType, category);
        task.setTitle(title);
        task.setDate(date);
        task.setPriority(priority);
        task.setCompleted(completed);
        task.setType(type);
        
        return task;
    }


    
    public void deleteTasks(List<Integer> taskIds) {
        if (taskIds.isEmpty()) return;
        
        SQLiteDatabase db = this.getWritableDatabase();
        StringBuilder whereClause = new StringBuilder(KEY_ID + " IN (");
        
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
        
        db.delete(TABLE_TASKS, whereClause.toString(), whereArgs);
        db.close();
    }


    public int deleteAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Đầu tiên xóa các bản ghi lịch sử liên quan đến các task
        db.delete(TABLE_TASK_HISTORY, null, null);
        
        // Sau đó xóa tất cả các task
        int count = db.delete(TABLE_TASKS, null, null);
        
        db.close();
        return count;
    }

    // Phương thức để xóa tất cả các task đã hoàn thành

}
