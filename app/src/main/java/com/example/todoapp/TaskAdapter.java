package com.example.todoapp;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private OnItemClickListener listener;
    private OnTaskCompletionListener completionListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    
    public interface OnTaskCompletionListener {
        void onTaskCompletionChanged(Task task, boolean isCompleted);
    }

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnTaskCompletionListener(OnTaskCompletionListener listener) {
        this.completionListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.textTask.setText(task.getDescription());
        holder.textTime.setText(task.getTimeString());
        holder.checkboxTask.setChecked(task.isCompleted());
        
        // Style based on task type
        CardView cardView = (CardView) holder.itemView;
        
        switch (task.getTaskType()) {
            case Task.TYPE_WORKOUT:
                cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Light Green
                // Remove setStrokeColor - not available in CardView
                break;
            case Task.TYPE_HABIT:
                cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD")); // Light Blue
                // Remove setStrokeColor - not available in CardView
                break;
            default:
                // Normal tasks
                if (position % 2 == 0) {
                    cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5")); // Light Grey
                } else {
                    cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF")); // White
                }
                // Remove setStrokeColor - not available in CardView
                break;
        }
        
        // Add day number badge for challenge tasks
        if (task.getDayNumber() > 0) {
            holder.textTime.setText(String.format("Day %d • %s", task.getDayNumber(), task.getTimeString()));
        }
        
        // Strike through text if completed
        if (task.isCompleted()) {
            holder.textTask.setPaintFlags(holder.textTask.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            cardView.setAlpha(0.7f);
        } else {
            holder.textTask.setPaintFlags(holder.textTask.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            cardView.setAlpha(1.0f);
        }
        
        holder.checkboxTask.setOnClickListener(v -> {
            boolean isChecked = holder.checkboxTask.isChecked();
            task.setCompleted(isChecked);
            
            // Update text appearance
            if (isChecked) {
                holder.textTask.setPaintFlags(holder.textTask.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                cardView.setAlpha(0.7f);
            } else {
                holder.textTask.setPaintFlags(holder.textTask.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                cardView.setAlpha(1.0f);
            }
            
            // Notify completion listener
            if (completionListener != null) {
                completionListener.onTaskCompletionChanged(task, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public List<Task> getSelectedTasks() {
        List<Task> selectedTasks = new ArrayList<>();
        for (Task task : taskList) {
            if (task.isSelected()) {
                selectedTasks.add(task);
            }
        }
        return selectedTasks;
    }

    public List<Integer> getSelectedTaskIds() {
        List<Integer> selectedIds = new ArrayList<>();
        for (Task task : taskList) {
            if (task.isSelected()) {
                selectedIds.add(task.getId());
            }
        }
        return selectedIds;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textTask;
        TextView textTime;
        CheckBox checkboxTask;
        
        public TaskViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            textTask = itemView.findViewById(R.id.textTask);
            textTime = itemView.findViewById(R.id.textTime);
            checkboxTask = itemView.findViewById(R.id.checkboxTask);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}
