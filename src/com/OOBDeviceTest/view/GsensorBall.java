package com.OOBDeviceTest.view;

import android.R.color;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class GsensorBall extends View {
	private final static String TAG = "GsensorBall";
	private Paint mPaint = new Paint();
	private Rect mRect = new Rect();
	
	private float center_point_x = 0;
	private float center_point_y = 0;
	private float draw_center_point_x = 0;
	private float draw_center_point_y = 0;
	private float ball_radius = 0;
	private final static float SCALE = 20.0f;
	private static float MOVESCALE = 20.0f;
	private int currentcolor = Color.RED;
	public GsensorBall(Context context) {
		this(context, null, 0);
	}

	public GsensorBall(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GsensorBall(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		center_point_x = this.getWidth()/2.0f;
		center_point_y = this.getHeight()/2.0f;
		MOVESCALE = this.getWidth() > this.getHeight() ? this.getHeight()/18.0f : this.getWidth()/18.0f;
		if(draw_center_point_x == 0){
			draw_center_point_x = center_point_x;
			draw_center_point_y = center_point_y;
		}
		ball_radius = this.getWidth() > this.getHeight() ? this.getHeight()/SCALE : this.getWidth()/SCALE;

		mPaint.setColor(Color.RED);
		mPaint.setStyle(Style.FILL);
	}
	
	public void setXYZ(float x, float y, float z){
		draw_center_point_x = center_point_x + y * MOVESCALE;
		draw_center_point_y = center_point_y + x * MOVESCALE;
		if(draw_center_point_x != 0 || draw_center_point_y != 0){
			currentcolor = Color.GREEN;
		}
//		Log.d(TAG, "__________________-------- setXYZ(),   draw_center_point_x = " + draw_center_point_x + 
//				"    draw_center_point_y = " + draw_center_point_y);
		this.invalidate();
	}
	
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		Log.d(TAG, "__________________-------- onDraw(),   draw_center_point_x = " + draw_center_point_x + 
//				"    draw_center_point_y = " + draw_center_point_y);
		mPaint.setColor(Color.RED);
		canvas.drawCircle(center_point_x, center_point_y, ball_radius / 2, mPaint);
		mPaint.setColor(currentcolor);
		canvas.drawCircle(draw_center_point_x, draw_center_point_y, ball_radius, mPaint);
	}
}
