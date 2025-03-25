package com.example.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private DatabaseHelper db;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Set app title with blue theme
        getSupportActionBar().setTitle("Công việc của tôi");
        
        // Set the FAB to create multiple random tasks
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> createMultipleRandomTasks());

        loadTasks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_add) {
            showAddTaskDialog();
            return true;
        } else if (itemId == R.id.action_delete_selected) {
            deleteSelectedTasks();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Method to create multiple (5-10) random tasks with random times
    private void createMultipleRandomTasks() {
        Random random = new Random();
        int count = random.nextInt(6) + 5; // Random number between 5 and 10
        
        for (int i = 0; i < count; i++) {
            Task randomTask = Task.createRandomTask();
            long id = db.addTask(randomTask);
            if (id > 0) {
                Task newTask = new Task((int) id, randomTask.getDescription(), 
                        randomTask.getHour(), randomTask.getMinute());
                scheduleNotification(newTask);
            }
        }
        
        loadTasks();
        Toast.makeText(this, "Đã tạo " + count + " công việc ngẫu nhiên", Toast.LENGTH_SHORT).show();
    }

    // Method to manually add a task (not random)
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm công việc mới");
        
        // Set up the input
        final EditText input = new EditText(this);
        builder.setView(input);
        
        // Set up the buttons
        builder.setPositiveButton("Chọn giờ", (dialog, which) -> {
            String taskDescription = input.getText().toString().trim();
            if (taskDescription.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập mô tả công việc", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Show time picker for manual selection
            showTimePickerDialog(taskDescription, 
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE));
        });
        
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Existing methods remain unchanged
    private void showTimePickerDialog(String taskDescription, int defaultHour, int defaultMinute) {
        // ...existing code...
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                Task newTask = new Task(0, taskDescription, hourOfDay, minute);
                long id = db.addTask(newTask);
                if (id > 0) {
                    newTask = new Task((int) id, taskDescription, hourOfDay, minute);
                    scheduleNotification(newTask);
                    loadTasks();
                    Toast.makeText(this, "Đã tạo công việc mới", Toast.LENGTH_SHORT).show();
                }
            },
            defaultHour,
            defaultMinute,
            true
        );
        timePickerDialog.show();
    }

    private void deleteSelectedTasks() {
        // ...existing code...
        if (adapter == null) return;
        
        List<Integer> selectedIds = adapter.getSelectedTaskIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Chưa chọn công việc nào để xóa", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc muốn xóa " + selectedIds.size() + " công việc đã chọn?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                db.deleteTasks(selectedIds);
                loadTasks();
                Toast.makeText(MainActivity.this, "Đã xóa các công việc đã chọn", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void loadTasks() {
        // ...existing code...
        taskList = db.getAllTasks();
        
        // Sort tasks by time (nearest first)
        Collections.sort(taskList, Comparator.comparingLong(Task::getTimeInMillis));
        
        adapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(adapter);
        
        // Apply item decorations for visual separation
        recyclerView.addItemDecoration(new TaskItemDecoration(this));
        
        adapter.setOnItemClickListener(position -> {
            Task task = taskList.get(position);
            task.setSelected(!task.isSelected());
            adapter.notifyItemChanged(position);
        });
    }
    
    private void scheduleNotification(Task task) {
        // ...existing code...
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TaskAlarmReceiver.class);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_description", task.getDescription());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 
                task.getId(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        long alarmTimeMillis = task.getTimeInMillis();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
        }
    }
}
