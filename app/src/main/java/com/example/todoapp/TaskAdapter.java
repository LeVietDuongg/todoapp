package com.example.todoapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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
        holder.checkboxTask.setChecked(task.isSelected());
        
        // Apply alternating colors to card backgrounds
        if (position % 2 == 0) {
            holder.itemView.setBackgroundResource(R.color.task_bg_even);
        } else {
            holder.itemView.setBackgroundResource(R.color.task_bg_odd);
        }
        
        holder.checkboxTask.setOnClickListener(v -> {
            boolean isChecked = holder.checkboxTask.isChecked();
            task.setSelected(isChecked);
            
            // Change background color for selected tasks
            if (isChecked) {
                holder.itemView.setBackgroundResource(R.color.completed_task_bg);
            } else {
                // Restore original alternating color
                if (position % 2 == 0) {
                    holder.itemView.setBackgroundResource(R.color.task_bg_even);
                } else {
                    holder.itemView.setBackgroundResource(R.color.task_bg_odd);
                }
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
