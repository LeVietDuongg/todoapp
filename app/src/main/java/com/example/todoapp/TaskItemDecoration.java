package com.example.todoapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class TaskItemDecoration extends RecyclerView.ItemDecoration {

    private final Paint oddPaint;
    private final Paint evenPaint;
    private final int dividerHeight;

    public TaskItemDecoration(Context context) {
        oddPaint = new Paint();
        oddPaint.setColor(ContextCompat.getColor(context, R.color.task_bg_odd));
        evenPaint = new Paint();
        evenPaint.setColor(ContextCompat.getColor(context, R.color.task_bg_even));
        dividerHeight = context.getResources().getDimensionPixelSize(R.dimen.divider_height);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, 
                              @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        
        int position = parent.getChildAdapterPosition(view);
        outRect.bottom = dividerHeight;
    }
}
