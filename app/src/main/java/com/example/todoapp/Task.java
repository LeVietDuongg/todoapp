package com.example.todoapp;

import java.util.Calendar;
import java.util.Random;

public class Task {
    private int id;
    private String description;
    private int hour;
    private int minute;
    private boolean isSelected;
    
    public Task(int id, String description, int hour, int minute) {
        this.id = id;
        this.description = description;
        this.hour = hour;
        this.minute = minute;
        this.isSelected = false;
    }
    
    public int getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getHour() {
        return hour;
    }
    
    public int getMinute() {
        return minute;
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    
    public String getTimeString() {
        return String.format("%02d:%02d", hour, minute);
    }
    
    public long getTimeInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        
        // If the time is in the past today, set it for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        return calendar.getTimeInMillis();
    }
    
    public static Task createRandomTask() {
        String[] randomTasks = {
            "Đọc sách", "Tập thể dục", "Học bài", "Mua sắm", 
            "Gọi điện cho gia đình", "Dọn dẹp nhà cửa", "Nấu ăn",
            "Làm việc nhà", "Gặp bạn bè", "Kiểm tra email"
        };
        
        Random random = new Random();
        String taskDesc = randomTasks[random.nextInt(randomTasks.length)];
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        
        return new Task(0, taskDesc, hour, minute);
    }
}
