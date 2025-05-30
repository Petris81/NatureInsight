package com.example.natureinsight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

public class GradientValueView extends View {
    private Paint cursorPaint;
    private float value = 0.0f; // value betweeen 0 and 1
    private Drawable gradientLine;
    private int cursorWidth = 8;
    private int cursorHeight = 20;

    public GradientValueView(Context context) {
        super(context);
        init();
    }

    public GradientValueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cursorPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.black));
        gradientLine = ContextCompat.getDrawable(getContext(), R.drawable.gradient_line);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // gradient line
        gradientLine.setBounds(0, getHeight() / 2 - 4, getWidth(), getHeight() / 2 + 2);
        gradientLine.draw(canvas);

        // cursor
        float cursorX = value * getWidth();
        float cursorY = getHeight() / 2;
        canvas.drawRect(
                cursorX - cursorWidth / 2,
                cursorY - cursorHeight / 2,
                cursorX + cursorWidth / 2,
                cursorY + cursorHeight / 2,
                cursorPaint);
    }

    public void setValue(float value) {
        this.value = Math.max(0, Math.min(1, value));
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = 40;
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, height);
    }
}