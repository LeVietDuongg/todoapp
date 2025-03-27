package com.example.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fab;
    private DatabaseHelper db;
    
    private static final String PREF_NAME = "SixPathPrefs";
    private static final String KEY_FIRST_RUN = "first_run";
    private static final int NUM_PAGES = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        setupViews();
        checkFirstRun();
    }
    
    private void setupViews() {
        // Set up ViewPager and adapter
        viewPager = findViewById(R.id.viewPager);
        ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        // Set up BottomNavigation
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_tasks) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.nav_workout) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.nav_progress) {
                viewPager.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.nav_settings) {
                viewPager.setCurrentItem(3);
                return true;
            }
            return false;
        });
        
        // ViewPager change listener to update bottom navigation
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.nav_tasks);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.nav_workout);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.nav_progress);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
                        break;
                }
            }
        });
        
        // Set up FAB
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            // Get current fragment
            int currentPage = viewPager.getCurrentItem();
            switch (currentPage) {
                case 0: // Tasks tab
                    showAddTaskDialog();
                    break;
                case 1: // Workout tab
                    addRandomWorkoutTask();
                    break;
                case 2: // Progress tab
                    // No action for progress tab
                    break;
                case 3: // Settings tab
                    // No action for settings tab
                    break;
            }
        });
    }
    
    private void checkFirstRun() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(KEY_FIRST_RUN, true);
        
        if (isFirstRun) {
            // Create initial workout schedule for 30 days
            createInitial30DayWorkout();
            
            // Mark as not first run
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_FIRST_RUN, false);
            editor.apply();
            
            // Show welcome message
            Toast.makeText(this, "Chào mừng đến với 30 Day Six Path Challenge!", Toast.LENGTH_LONG).show();
        }
    }
    
    private void createInitial30DayWorkout() {
        // Create workout tasks for 30 days
        for (int day = 1; day <= 30; day++) {
            // Each day has 1-2 workout tasks
            Task workoutTask = Task.createRandomWorkoutTask(day);
            
            // Đặt các thông tin bổ sung
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, day - 1);  // Đặt ngày dựa vào số ngày
            String dateStr = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), 
                cal.get(Calendar.MONTH) + 1, 
                cal.get(Calendar.DAY_OF_MONTH));
            
            workoutTask.setTitle("Workout ngày " + day);
            workoutTask.setDate(dateStr);
            workoutTask.setPriority(2);  // Ưu tiên cao hơn
            workoutTask.setType(Task.TYPE_WORKOUT);
            
            long id = db.addTask(workoutTask);
            if (id > 0) {
                workoutTask.setId((int) id);
                scheduleNotification(workoutTask);
            }
            
            // Each day has 1 habit task
            Task habitTask = Task.createRandomHabitTask(day);
            
            // Đặt các thông tin bổ sung
            habitTask.setTitle("Thói quen ngày " + day);
            habitTask.setDate(dateStr);
            habitTask.setPriority(1);
            habitTask.setType(Task.TYPE_HABIT);
            
            id = db.addTask(habitTask);
            if (id > 0) {
                habitTask.setId((int) id);
                scheduleNotification(habitTask);
            }
        }
    }
    
    private void addRandomWorkoutTask() {
        // Find highest day number
        int maxDay = db.getMaxDayNumber();
        
        // Add for next day
        int nextDay = Math.min(maxDay + 1, 30);
        Task workoutTask = Task.createRandomWorkoutTask(nextDay);
        
        // Đặt các thông tin bổ sung
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, nextDay - 1);  // Đặt ngày dựa vào số ngày
        String dateStr = String.format("%04d-%02d-%02d", 
            cal.get(Calendar.YEAR), 
            cal.get(Calendar.MONTH) + 1, 
            cal.get(Calendar.DAY_OF_MONTH));
        
        workoutTask.setTitle("Workout ngày " + nextDay);
        workoutTask.setDate(dateStr);
        workoutTask.setPriority(2);  // Ưu tiên cao hơn
        workoutTask.setType(Task.TYPE_WORKOUT);
        
        long id = db.addTask(workoutTask);
        if (id > 0) {
            workoutTask.setId((int) id);
            scheduleNotification(workoutTask);
            
            Toast.makeText(this, "Đã thêm bài tập cho Ngày " + nextDay, Toast.LENGTH_SHORT).show();
            
            // Refresh workout fragment
            if (getSupportFragmentManager().findFragmentByTag("f1") != null) {
                ((WorkoutFragment) getSupportFragmentManager().findFragmentByTag("f1")).refreshWorkouts();
            }
        }
    }
    
    // Adapter for viewpager
    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new TasksFragment();
                case 1:
                    return new WorkoutFragment();
                case 2:
                    return new ProgressFragment();
                case 3:
                    return new SettingsFragment();
                default:
                    return new TasksFragment();
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
    
    // Task notification methods
    public void scheduleNotification(Task task) {
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

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "Không thể đặt báo thức chính xác", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    // Add task dialog - simplified from original implementation
    private void showAddTaskDialog() {
        // Fragment will handle this
        if (getSupportFragmentManager().findFragmentByTag("f0") != null) {
            ((TasksFragment) getSupportFragmentManager().findFragmentByTag("f0")).showAddTaskDialog();
        }
    }

    // Add getter methods for fragments
    public TasksFragment getTasksFragment() {
        return (TasksFragment) getSupportFragmentManager().findFragmentByTag("f" + 0);
    }
    
    public WorkoutFragment getWorkoutFragment() {
        return (WorkoutFragment) getSupportFragmentManager().findFragmentByTag("f" + 1);
    }
    
    public ProgressFragment getProgressFragment() {
        return (ProgressFragment) getSupportFragmentManager().findFragmentByTag("f" + 2);
    }
}