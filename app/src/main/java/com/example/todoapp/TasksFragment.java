package com.example.todoapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TasksFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private DatabaseHelper db;
    private ImageButton btnTaskSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnTaskSettings = view.findViewById(R.id.btnTaskSettings);
        btnTaskSettings.setOnClickListener(v -> showDeleteCompletedTasksDialog());

        db = new DatabaseHelper(getContext());

        loadTasks();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }

    public void loadTasks() {
        taskList = db.getAllTasks();

        adapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(adapter);

        adapter.setOnTaskCompletionListener((task, isCompleted) -> {
            db.updateTaskCompletion(task.getId(), isCompleted);
        });

        adapter.setOnItemClickListener(position -> {
            Task task = taskList.get(position);
            task.setSelected(!task.isSelected());
            adapter.notifyItemChanged(position);
        });
    }

    private void showDeleteCompletedTasksDialog() {
        // Tìm các nhiệm vụ đã hoàn thành
        List<Integer> completedTaskIds = new ArrayList<>();

        for (Task task : taskList) {
            if (task.isCompleted()) {
                completedTaskIds.add(task.getId());
            }
        }

        if (completedTaskIds.isEmpty()) {
            Toast.makeText(getContext(), "Không có nhiệm vụ đã hoàn thành để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị hộp thoại xác nhận
        new AlertDialog.Builder(getContext())
            .setTitle("Xóa nhiệm vụ đã hoàn thành")
            .setMessage("Bạn có muốn xóa " + completedTaskIds.size() + " nhiệm vụ đã hoàn thành?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                // Xóa các nhiệm vụ đã hoàn thành
                db.deleteTasks(completedTaskIds);

                // Làm mới danh sách
                loadTasks();

                // Cập nhật biểu đồ tiến độ nếu cần
                if (getActivity() instanceof MainActivity) {
                    ProgressFragment progressFragment = ((MainActivity) getActivity()).getProgressFragment();
                    if (progressFragment != null) {
                        progressFragment.refreshChartData();
                    }
                }

                Toast.makeText(getContext(), "Đã xóa " + completedTaskIds.size() + " nhiệm vụ", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    public void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm công việc mới");

        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("Chọn giờ", (dialog, which) -> {
            String taskDescription = input.getText().toString().trim();
            if (taskDescription.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập mô tả công việc", Toast.LENGTH_SHORT).show();
                return;
            }

            showTimePickerDialog(taskDescription,
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE));
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showTimePickerDialog(String taskDescription, int defaultHour, int defaultMinute) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    Task newTask = new Task(0, taskDescription, hourOfDay, minute);
                    long id = db.addTask(newTask);
                    if (id > 0) {
                        newTask = new Task((int) id, taskDescription, hourOfDay, minute);
                        ((MainActivity) getActivity()).scheduleNotification(newTask);
                        loadTasks();
                        Toast.makeText(getContext(), "Đã tạo công việc mới", Toast.LENGTH_SHORT).show();
                    }
                },
                defaultHour,
                defaultMinute,
                true
        );
        timePickerDialog.show();
    }
}
