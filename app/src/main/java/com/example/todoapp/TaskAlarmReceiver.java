package com.example.todoapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

public class TaskAlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "task_notification_channel";
    private static final String CHANNEL_NAME = "Task Notifications";
    private static final long[] VIBRATION_PATTERN = {0, 500, 200, 500};

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskDescription = intent.getStringExtra("task_description");
        int taskId = intent.getIntExtra("task_id", 0);

        // Create notification channel for Android 8.0+
        createNotificationChannel(context);
        
        // Show notification
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Nhắc nhở công việc")
                .setContentText(taskDescription)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(VIBRATION_PATTERN)
                .setAutoCancel(true);

        notificationManager.notify(taskId, builder.build());
        
        // Vibrate the device
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, -1));
        } else {
            vibrator.vibrate(VIBRATION_PATTERN, -1);
        }
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            channel.setVibrationPattern(VIBRATION_PATTERN);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
