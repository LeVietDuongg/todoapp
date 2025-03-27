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

        // Tạo view cho dialog từ layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task, null);
        final EditText input = dialogView.findViewById(R.id.editTextTaskDescription);
        builder.setView(dialogView);

        // Thêm các nút
        builder.setPositiveButton("Chọn giờ", (dialog, which) -> {
            String taskDescription = input.getText().toString().trim();
            if (taskDescription.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập mô tả công việc", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiển thị hộp thoại chọn giờ với giờ hiện tại
            Calendar cal = Calendar.getInstance();
            showTimePickerDialog(taskDescription, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        });

        builder.setNeutralButton("Tạo ngẫu nhiên", (dialog, which) -> {
            // Lấy ngày cao nhất hiện tại
            int maxDay = db.getMaxDayNumber();
            int nextDay = maxDay + 1;
            
            // Tạo công việc ngẫu nhiên
            Task randomTask = Task.createRandomTask();
            
            // Đặt các thông tin bổ sung
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, nextDay - 1);  // Ngày tăng dần
            String dateStr = String.format("%04d-%02d-%02d", 
                cal.get(Calendar.YEAR), 
                cal.get(Calendar.MONTH) + 1, 
                cal.get(Calendar.DAY_OF_MONTH));
            
            randomTask.setTitle(randomTask.getDescription());  // Đặt tiêu đề giống với mô tả
            randomTask.setDate(dateStr);  // Đặt ngày tăng dần
            randomTask.setPriority(1);    // Mức ưu tiên mặc định
            randomTask.setType(Task.TYPE_NORMAL);
            randomTask.setDayNumber(nextDay);  // Đặt số ngày tăng dần
            
            long id = db.addTask(randomTask);
            if (id > 0) {
                randomTask.setId((int) id);
                ((MainActivity) getActivity()).scheduleNotification(randomTask);
                loadTasks();
                Toast.makeText(getContext(), 
                    "Đã tạo công việc ngày " + nextDay + ": " + randomTask.getDescription() + 
                    " lúc " + randomTask.getTimeString(), 
                    Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showTimePickerDialog(String taskDescription, int defaultHour, int defaultMinute) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    // Tạo task mới với thông tin đầy đủ
                    Calendar cal = Calendar.getInstance();
                    String dateStr = String.format("%04d-%02d-%02d", 
                        cal.get(Calendar.YEAR), 
                        cal.get(Calendar.MONTH) + 1, 
                        cal.get(Calendar.DAY_OF_MONTH));
                    
                    Task newTask = new Task(0, taskDescription, hourOfDay, minute);
                    newTask.setTitle(taskDescription);  // Đặt tiêu đề giống với mô tả
                    newTask.setDate(dateStr);  // Đặt ngày hiện tại
                    newTask.setPriority(1);    // Mức ưu tiên mặc định
                    newTask.setType(Task.TYPE_NORMAL);
                    
                    long id = db.addTask(newTask);
                    if (id > 0) {
                        newTask.setId((int) id);
                        ((MainActivity) getActivity()).scheduleNotification(newTask);
                        loadTasks();
                        Toast.makeText(getContext(), 
                            "Đã tạo công việc '" + taskDescription + "' lúc " + 
                            String.format("%02d:%02d", hourOfDay, minute), 
                            Toast.LENGTH_SHORT).show();
                    }
                },
                defaultHour,
                defaultMinute,
                true
        );
        timePickerDialog.show();
    }
}
