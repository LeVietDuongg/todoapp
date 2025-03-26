package com.example.todoapp;

import java.util.Calendar;
import java.util.Random;

public class Task {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_WORKOUT = 1;
    public static final int TYPE_HABIT = 2;
    public static final int TYPE_GENERAL = 0;

    private int id;
    private String description;
    private int hour;
    private int minute;
    private boolean isSelected;
    private boolean isCompleted;
    private long createdDate;
    private int dayNumber; // 1-30 for 30-day challenge
    private int taskType; // 0=normal, 1=workout, 2=habit
    private String category; // Category for grouping tasks
    private String title;
    private String date;
    private int priority;
    private int type;

    // Constructor for creating a new task
    public Task(String title, String description, String date, int priority, boolean completed, int type) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.priority = priority;
        this.isCompleted = completed;
        this.type = type;
    }

    // Constructor for loading a task from database
    public Task(int id, String title, String description, String date, int priority, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.priority = priority;
        this.isCompleted = completed;
        this.type = TYPE_GENERAL; // Default type
    }

    // Constructor with type
    public Task(int id, String title, String description, String date, int priority, boolean completed, int type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.priority = priority;
        this.isCompleted = completed;
        this.type = type;
    }

    public Task(int id, String description, int hour, int minute) {
        this.id = id;
        this.description = description;
        this.hour = hour;
        this.minute = minute;
        this.isSelected = false;
        this.isCompleted = false;
        this.createdDate = System.currentTimeMillis();
        this.dayNumber = 0;
        this.taskType = TYPE_NORMAL;
        this.category = "General";
    }

    public Task(int id, String description, int hour, int minute, int dayNumber, int taskType, String category) {
        this.id = id;
        this.description = description;
        this.hour = hour;
        this.minute = minute;
        this.isSelected = false;
        this.isCompleted = false;
        this.createdDate = System.currentTimeMillis();
        this.dayNumber = dayNumber;
        this.taskType = taskType;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public static Task createRandomWorkoutTask(int dayNumber) {
        String[] workouts = {
                "Chạy bộ 30 phút", "Tập 20 hít đất", "Tập 30 gập bụng",
                "Plank 1 phút", "Tập Yoga 15 phút", "Tập 15 squat",
                "Nhảy dây 10 phút", "Đạp xe 20 phút", "Tập tay với tạ 15 phút",
                "Khởi động 5 phút + 20 burpees"
        };

        Random random = new Random();
        String taskDesc = workouts[random.nextInt(workouts.length)];
        int hour = 7 + random.nextInt(12); // Between 7:00 and 19:00
        int minute = random.nextInt(4) * 15; // 0, 15, 30, or 45 minutes

        Task task = new Task(0, taskDesc, hour, minute);
        task.setTaskType(TYPE_WORKOUT);
        task.setDayNumber(dayNumber);
        task.setCategory("Workout");

        return task;
    }

    public static Task createRandomHabitTask(int dayNumber) {
        String[] habits = {
                "Uống 2 lít nước", "Đi ngủ trước 23:00", "Không dùng điện thoại trong 1 giờ",
                "Ăn sáng lành mạnh", "Thiền 10 phút", "Viết nhật ký",
                "Khen ngợi bản thân", "Đọc sách 30 phút", "Dậy sớm 6:00 sáng",
                "Ăn ít đường và chất béo"
        };

        Random random = new Random();
        String taskDesc = habits[random.nextInt(habits.length)];
        int hour = 8 + random.nextInt(12); // Between 8:00 and 20:00
        int minute = random.nextInt(4) * 15; // 0, 15, 30, or 45 minutes

        Task task = new Task(0, taskDesc, hour, minute);
        task.setTaskType(TYPE_HABIT);
        task.setDayNumber(dayNumber);
        task.setCategory("Habit");

        return task;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
