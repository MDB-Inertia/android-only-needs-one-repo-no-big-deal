package com.inertia.phyzmo.datadisplay.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import com.inertia.phyzmo.datadisplay.DisplayDataActivity;
import com.inertia.phyzmo.datadisplay.models.PointModel;

public class DistanceSelectionView extends AppCompatImageView {

    public PointModel firstPoint;
    public PointModel secondPoint;

    private DisplayDataActivity activity;

    @SuppressLint("ClickableViewAccessibility")
    public DistanceSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        firstPoint = null;
        secondPoint = null;

        this.setOnTouchListener((v, event) -> {

            int[] viewCoords = new int[2];
            v.getLocationOnScreen(viewCoords);

            int touchX = (int) event.getRawX();
            int touchY = (int) event.getRawY();

            int imageX = touchX - viewCoords[0];
            int imageY = touchY - viewCoords[1];

            System.out.println("X: " + imageX + ", Y: " + imageY);

            if (firstPoint != null) {
                secondPoint = new PointModel(firstPoint.x, firstPoint.y);
            }
            firstPoint = new PointModel(imageX, imageY);

            invalidate();

            return false;
        });
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(android.R.color.holo_red_light));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

        if (firstPoint != null && secondPoint != null) {
            canvas.drawLine(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y, paint);
        }

        if (firstPoint != null) {
            Paint blue = new Paint(Paint.ANTI_ALIAS_FLAG);
            blue.setColor(getResources().getColor(android.R.color.holo_blue_dark));
            blue.setStyle(Paint.Style.FILL);
            canvas.drawCircle(firstPoint.x, firstPoint.y, 20, blue);
        }

        if (secondPoint != null) {
            Paint blue = new Paint(Paint.ANTI_ALIAS_FLAG);
            blue.setColor(getResources().getColor(android.R.color.holo_blue_dark));
            blue.setStyle(Paint.Style.FILL);
            canvas.drawCircle(secondPoint.x, secondPoint.y, 20, blue);
        }

        activity.getAdapter().updateSelectButton();
    }

    public void drawLine(float x1, float y1, float x2, float y2) {
        int wrapSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        this.measure(wrapSpec, wrapSpec);
        this.firstPoint = new PointModel(x1 * this.getMeasuredWidth(), y1 * this.getHeight());
        this.secondPoint = new PointModel(x2 * this.getMeasuredWidth(), y2 * this.getHeight());
        this.invalidate();
    }

    public boolean hasValidLine() {
        return firstPoint != null && secondPoint != null && !this.firstPoint.equals(secondPoint);
    }

    public void setActivity(DisplayDataActivity a) {
        this.activity = a;
    }

    public DisplayDataActivity getActivity() {
        return this.activity;
    }
}