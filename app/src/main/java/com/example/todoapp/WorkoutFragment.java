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
