package com.medx.android.utils.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.medx.android.R;

/**
 * Created by alexey on 9/27/16.
 */

public class CircleProgressView extends View {
//------------------------------ Animation -----------------------------------------

    //animation types
    private final int OUTTER_CIRCLE_ANIMATION = 0;
    private final int INNER_SECTOR_ANIMATION = 1;
    private final int HIDE_ANIMATION = 2;

    //duration ms
    private final int OUTTER_CIRCLE_ANIMATION_DURATION = 2000;
    private final int INNER_SECTOR_ANIMATION_DURATION = 10000;
    private final int HIDE_SECTOR_ANIMATION_DURATION = 10000;

    private int FRAMES_PER_SECOND = 100;

    private int currentAnimation = -1;
    private long startTime;
    private long duration;

    //------------------------------ Paints --------------------------------------------

    private int bgColor;
    private int fgColor;
    private Paint bgPaint;
    private Paint fgPaint;
    private Paint crPaint;
    private RectF oval;
    float radius;
    private int nextPoint = 10;

    private float radiusOutCircle;

    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setProgress(int nextPoint){
        this.nextPoint = nextPoint;
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= 11)
        {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#cc000000"));

        fgPaint = new Paint();
        fgPaint.setColor(Color.TRANSPARENT);
        fgPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)); // A out B http://en.wikipedia.org/wiki/File:Alpha_compositing.svg

        crPaint = new Paint();
        crPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        crPaint.setStyle(Paint.Style.STROKE);
        crPaint.setStrokeWidth(getResources().getDimension(R.dimen.circle_stroke_width));
        crPaint.setColor(Color.TRANSPARENT);
        crPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

    }

    public boolean isAnimation(){
        return currentAnimation > 0;
    }

    public void startAnimation() {
        if (currentAnimation >= HIDE_ANIMATION){
            reset();
            return;
        }
        currentAnimation++;
        startTime = System.currentTimeMillis();
        switch (currentAnimation){
            case OUTTER_CIRCLE_ANIMATION:
                duration = OUTTER_CIRCLE_ANIMATION_DURATION;
                break;
            case HIDE_ANIMATION:
                duration = HIDE_SECTOR_ANIMATION_DURATION;
                break;
        }
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float xpad = (float)(getPaddingLeft()+getPaddingRight());
        float ypad = (float)(getPaddingBottom()+ getPaddingTop());

        float wwd = (float)w - xpad;
        float hhd = (float)h - ypad;

        float centerX = wwd / 2;
        float centerY = hhd / 2;

        radius = Math.min(centerX / 2, centerY / 2);
        float radiusFull = radius + getResources().getDimension(R.dimen.circle_stroke_width);

        oval = new RectF(getPaddingLeft() + (centerX - radiusFull),
                getPaddingTop() + (centerY - radiusFull),
                getPaddingLeft() + (centerX + radiusFull),
                getPaddingTop()+ (centerY + radiusFull));

        radiusOutCircle = radius / 3;
    }

    public CircleProgressView reset(){
        percent = 0;
        duration = 0;
        currentAnimation = -1;
        startTime = 0;
        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;

        radius = Math.min(centerX / 2, centerY / 2);
        float radiusFull = radius + getResources().getDimension(R.dimen.circle_stroke_width);

        oval = new RectF(getPaddingLeft() + (centerX - radiusFull),
                getPaddingTop() + (centerY - radiusFull),
                getPaddingLeft() + (centerX + radiusFull),
                getPaddingTop()+ (centerY + radiusFull));

        radiusOutCircle = radius / 3;
        invalidate();
        return this;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long elapsedTime = System.currentTimeMillis() - startTime;

        if (currentAnimation == -1){
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), fgPaint);
            return;
        }

        if (currentAnimation >= 0){
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), bgPaint);
        }


        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;
        switch(currentAnimation){
            case OUTTER_CIRCLE_ANIMATION:

                // canvas.drawArc(oval, startAngle, 100 * 3.6f, true, crPaint);
                radiusOutCircle+=20;
                if (radiusOutCircle >= radius + getResources().getDimension(R.dimen.circle_stroke_width)){
                    radiusOutCircle = radius + getResources().getDimension(R.dimen.circle_stroke_width);
                }
                RectF oval1 = new RectF(getPaddingLeft() + (centerX - radiusOutCircle),
                        getPaddingTop() + (centerY - radiusOutCircle),
                        getPaddingLeft() + (centerX + radiusOutCircle),
                        getPaddingTop() + (centerY + radiusOutCircle));
                canvas.drawArc(oval1, 0, 100 * 3.6f, true, crPaint);
                break;
            case INNER_SECTOR_ANIMATION:

                if (percent >= 100){

                    canvas.drawColor(Color.TRANSPARENT);
                    startAnimation();
                    break;
                }

                if (percent < nextPoint){
                    percent++;
                }


                radiusOutCircle = radius + getResources().getDimension(R.dimen.circle_stroke_width);
                RectF oval2 = new RectF(getPaddingLeft() + (centerX - radiusOutCircle),
                        getPaddingTop() + (centerY - radiusOutCircle),
                        getPaddingLeft() + (centerX + radiusOutCircle),
                        getPaddingTop() + (centerY + radiusOutCircle));
                canvas.drawArc(oval2, 0, 100 * 3.6f, true, crPaint);

                canvas.drawArc(oval, -90, percent * 3.6f, true, fgPaint);

                break;

            case HIDE_ANIMATION:
                Log.d("animation", "hide_animation");
                radiusOutCircle+=20;
                RectF oval3 = new RectF(getPaddingLeft() + (centerX - radiusOutCircle),
                        getPaddingTop() + (centerY - radiusOutCircle),
                        getPaddingLeft() + (centerX + radiusOutCircle),
                        getPaddingTop() + (centerY + radiusOutCircle));



                canvas.drawArc(oval3, 0, 100 * 3.6f, true, fgPaint);

                break;
        }
        if (currentAnimation >= 0 && currentAnimation <=2 && currentAnimation != 1){
            if (elapsedTime < duration) {
                this.postInvalidateDelayed(100);
            } else{
                startAnimation();
            }
        } else {
            this.postInvalidateDelayed(20);
        }

    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
        refreshTheLayout();
    }

    public int getFgColor() {
        return fgColor;
    }

    public void setFgColor(int fgColor) {
        this.fgColor = fgColor;
        refreshTheLayout();
    }


    private void refreshTheLayout() {
        invalidate();
        requestLayout();
    }

    private float percent;

    public float getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle + 270;
        invalidate();
        requestLayout();
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
        invalidate();
        requestLayout();
    }

    private float startAngle;
}
