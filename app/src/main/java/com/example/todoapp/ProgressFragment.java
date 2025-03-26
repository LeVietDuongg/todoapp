package com.example.todoapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class ProgressFragment extends Fragment {
    private BarChart chart;
    private TextView textOverallProgress;
    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        chart = view.findViewById(R.id.barChart);
        textOverallProgress = view.findViewById(R.id.textOverallProgress);

        db = new DatabaseHelper(getContext());
        setupChart();
        updateProgressData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProgressData();
    }

    private void setupChart() {
        // Đảm bảo chart được khởi tạo đúng cách
        if (chart == null) return;
        
        chart.getDescription().setEnabled(false);
        chart.setMaxVisibleValueCount(30);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setNoDataText("Không có dữ liệu tiến độ");

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7); // Giảm số lượng nhãn để tránh overlap
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "Ngày " + ((int) value);
            }
        });

        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        
        // Đảm bảo biểu đồ có đủ không gian
        chart.setMinimumHeight(500);
    }

    private void updateProgressData() {
        if (chart == null || db == null) return;
        
        int[] stats = db.getCompletionStats();

        // Cập nhật tổng tiến độ
        textOverallProgress.setText(stats[0] + "%");

        ArrayList<BarEntry> entries = new ArrayList<>();

        // Tạo dữ liệu mẫu nếu không có dữ liệu thực
        boolean hasData = false;
        for (int i = 1; i <= 30; i++) {
            if (stats[i] > 0) {
                hasData = true;
                break;
            }
        }
        
        // Thêm các giá trị vào entries
        for (int i = 1; i <= 30; i++) {
            entries.add(new BarEntry(i, stats[i]));
        }
        
        // Nếu không có dữ liệu thực, thêm dữ liệu mẫu
        if (!hasData) {
            for (int i = 1; i <= 5; i++) {
                entries.add(new BarEntry(i, 20 * i % 100));
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "Tiến độ");
        dataSet.setColors(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "%";
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);

        chart.setData(data);
        chart.setFitBars(true);
        chart.invalidate();
        
        // Debug: In ra log để kiểm tra dữ liệu
        System.out.println("Chart data updated with " + entries.size() + " entries");
    }
    
    // Phương thức public để làm mới dữ liệu từ bên ngoài
    public void refreshChartData() {
        if (isAdded() && getContext() != null) {
            updateProgressData();
        }
    }
}