package com.OOBDeviceTest.helper;

import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class VUMeter extends View {
    private static final float PIVOT_RADIUS = 3.5f;
    private static final float PIVOT_Y_OFFSET = 10f;
    private static final float SHADOW_OFFSET = 2.0f;
    private static final float DROPOFF_STEP = 0.18f;
    private static final float SURGE_STEP = 0.35f;
    private static final long  ANIMATION_INTERVAL = 70;
    
    private final static int MSG_TEST_MIC_ABOVE_LEVEL = 3;
    
    private Paint mPaint;
    private Paint mShadow;
    private float mCurrentAngle;
    
    private Recorder mRecorder;
    private Handler mHandler;
    
    private boolean mIsAbove = false; 
    
    public VUMeter(Context context) {
        super(context);
        init(context);
    }

    public VUMeter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    void init(Context context) {
//        Drawable background = getResources().getDrawable(R.drawable.vumeter);
//        setBackgroundDrawable(background);
        
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadow.setColor(Color.argb(60, 0, 0, 0));
        
        mRecorder = null;
        mIsAbove = false;
        
        mCurrentAngle = 0;
    }

    public void setRecorder(Recorder recorder) {
    	mRecorder = recorder;
    	invalidate();
    }
    
    public void setHandler(Handler handler) {
    	mHandler = handler;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float minAngle = (float)Math.PI/8;
        final float maxAngle = (float)Math.PI*7/8;
                
        float angle = minAngle;
        if (mRecorder != null) {
        	int amp = mRecorder.getMaxAmplitude();
        	angle += (float)(maxAngle - minAngle)*amp/32768;
        	if (mHandler != null && amp > 8000 && !mIsAbove ) {
        		mIsAbove = true;
        		mHandler.sendEmptyMessage(MSG_TEST_MIC_ABOVE_LEVEL);
        	}
        }
        if (angle > mCurrentAngle)
            mCurrentAngle = angle;
        else
            mCurrentAngle = Math.max(angle, mCurrentAngle - DROPOFF_STEP);

        mCurrentAngle = Math.min(maxAngle, mCurrentAngle);

        float w = getWidth();
        float h = getHeight();
        float pivotX = w/2;
        float pivotY = h - PIVOT_RADIUS - PIVOT_Y_OFFSET;
        float l = h*4/5;
        float sin = (float) Math.sin(mCurrentAngle);
        float cos = (float) Math.cos(mCurrentAngle);
        float x0 = pivotX - l*cos;
        float y0 = pivotY - l*sin;
        canvas.drawLine(x0 + SHADOW_OFFSET, y0 + SHADOW_OFFSET, pivotX + SHADOW_OFFSET, pivotY + SHADOW_OFFSET, mShadow);
        canvas.drawCircle(pivotX + SHADOW_OFFSET, pivotY + SHADOW_OFFSET, PIVOT_RADIUS, mShadow);
        canvas.drawLine(x0, y0, pivotX, pivotY, mPaint);
        canvas.drawCircle(pivotX, pivotY, PIVOT_RADIUS, mPaint);
        
        if (mRecorder != null && mRecorder.state() == Recorder.RECORDING_STATE)
        	postInvalidateDelayed(ANIMATION_INTERVAL);
    }
}
