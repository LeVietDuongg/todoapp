package com.example.todoapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RadioGroup themeRadioGroup = view.findViewById(R.id.theme_radio_group);
        Switch notificationsSwitch = view.findViewById(R.id.notifications_switch);
        Button clearAllButton = view.findViewById(R.id.btn_clear_all_tasks);

        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.theme_light) {
                applyTheme("light");
            } else if (checkedId == R.id.theme_dark) {
                applyTheme("dark");
            } else if (checkedId == R.id.theme_system) {
                applyTheme("system");
            }
        });

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleNotifications(isChecked);
        });

        clearAllButton.setOnClickListener(v -> {
            showConfirmClearDialog();
        });
    }

    private void applyTheme(String theme) {
        // Implement theme changing logic
        Toast.makeText(getContext(), "Theme changed to " + theme, Toast.LENGTH_SHORT).show();
    }

    private void toggleNotifications(boolean enabled) {
        // Implement notification toggle logic
        Toast.makeText(
                getContext(),
                enabled ? "Notifications enabled" : "Notifications disabled",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void showConfirmClearDialog() {
        // Show confirmation dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa tất cả bài tập")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả bài tập không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Get database and delete all tasks
                    DatabaseHelper db = new DatabaseHelper(getContext());
                    int count = db.deleteAllTasks();
                    
                    // Refresh các fragment khác nếu cần
                    if (getActivity() instanceof MainActivity) {
                        MainActivity activity = (MainActivity) getActivity();
                        
                        // Thông báo cho các fragment khác cập nhật dữ liệu
                        if (activity.getTasksFragment() != null) {
                            activity.getTasksFragment().loadTasks();
                        }
                        
                        if (activity.getWorkoutFragment() != null) {
                            activity.getWorkoutFragment().refreshWorkouts();
                        }
                    }
                    
                    Toast.makeText(getContext(), "Đã xóa " + count + " bài tập", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
