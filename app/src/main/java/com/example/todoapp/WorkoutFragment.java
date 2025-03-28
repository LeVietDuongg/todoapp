package com.example.todoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class WorkoutFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> workoutList;
    private DatabaseHelper db;
    private TextView textDayCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerViewWorkout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        textDayCount = view.findViewById(R.id.textDayCount);
        
        db = new DatabaseHelper(getContext());
        
        refreshWorkouts();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshWorkouts();
    }
    
    public void refreshWorkouts() {
        workoutList = db.getTasksByType(Task.TYPE_WORKOUT);
        
        // Sắp xếp danh sách theo thời gian
        Collections.sort(workoutList, (task1, task2) -> {
            // Đầu tiên so sánh theo ngày
            int dayCompare = Integer.compare(task1.getDayNumber(), task2.getDayNumber());
            if (dayCompare != 0) return dayCompare;
            
            // Nếu cùng ngày, so sánh theo giờ
            int hourCompare = Integer.compare(task1.getHour(), task2.getHour());
            if (hourCompare != 0) return hourCompare;
            
            // Nếu cùng giờ, so sánh theo phút
            return Integer.compare(task1.getMinute(), task2.getMinute());
        });
        
        adapter = new TaskAdapter(workoutList);
        recyclerView.setAdapter(adapter);
        
        // Update day count
        int maxDay = 0;
        for (Task task : workoutList) {
            maxDay = Math.max(maxDay, task.getDayNumber());
        }
        textDayCount.setText(String.format("Ngày %d / 30", maxDay));
        
        adapter.setOnTaskCompletionListener((task, isCompleted) -> {
            db.updateTaskCompletion(task.getId(), isCompleted);
        });
    }
}
