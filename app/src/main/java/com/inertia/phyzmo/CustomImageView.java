package com.inertia.phyzmo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

public class CustomImageView extends AppCompatImageView {

    Point firstPoint;
    Point secondPoint;

    DisplayDataActivity activity;

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //In versions > 3.0 need to define layer Type
        if (android.os.Build.VERSION.SDK_INT >= 11)
        {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        firstPoint = null;
        secondPoint = null;

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int[] viewCoords = new int[2];
                v.getLocationOnScreen(viewCoords);

                int touchX = (int) event.getRawX();
                int touchY = (int) event.getRawY();

                int imageX = touchX - viewCoords[0]; // viewCoords[0] is the X coordinate
                int imageY = touchY - viewCoords[1];

                System.out.println("X: " + imageX + ", Y: " + imageY);

                if (firstPoint != null) {
                    secondPoint = new Point(firstPoint.x, firstPoint.y);
                }
                firstPoint = new Point(imageX, imageY);

                invalidate();

                return false;
            }
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

        activity.adapter.updateSelectButton();
    }

    public boolean hasValidLine() {
        return firstPoint != null && secondPoint != null;
    }

    public static class Point {
        float x;
        float y;
        Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}