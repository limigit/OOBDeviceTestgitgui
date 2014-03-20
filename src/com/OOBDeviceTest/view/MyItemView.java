package com.OOBDeviceTest.view;

import com.OOBDeviceTest.helper.TestCase.RESULT;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import com.OOBDeviceTest.R;
public class MyItemView extends TextView {
	private static final String TAG = "MyItemView";

	public static final int PASS_COLOR = Color.rgb(0, 255, 0);
	public static final int FAILED_COLOR = Color.rgb(255, 0, 0);
	public static final int SKIP_COLOR = Color.rgb(0, 0, 255);
	public static final int CLICK_COLOR = Color.rgb(0, 255, 255);

	private RESULT mResult = RESULT.UNDEF;
	private int current_color = 0x00000000;
	private Paint mPaint = new Paint();
	private Rect mRect = new Rect();
	private Bitmap checkedbitmap = null;
	private Bitmap uncheckedbitmap = null;
	private int checkicon_left = 0;
	private int checkicon_top = 0;
	private final static int PADDINGLEN = 5;
	private boolean ischeck = true;
	public MyItemView(Context context) {
		this(context, null, 0);
	}

	public MyItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MyItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(2);
		mPaint.setStyle(Style.STROKE);
//		Drawable tmp = context.getResources().getDrawable(R.drawable.devicetest_icon);
		checkedbitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.btn_check_on);
		uncheckedbitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.btn_check_on_disable);
		setTextColor(Color.WHITE);
		setResult(RESULT.UNDEF);
		setBackgroundResource(R.drawable.itemclickbk);
	}

	
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		setTextSize((bottom - top) * 2 / 4);
		checkicon_left = this.getWidth() - checkedbitmap.getWidth() - PADDINGLEN * 2;
		checkicon_top = (this.getHeight() - checkedbitmap.getHeight()) / 2;
	}

	public void setResult(RESULT result) {
		int color;
		switch (result) {
		case OK:
			color = PASS_COLOR;
			break;
		case FAIL:
			color = FAILED_COLOR;
			break;
		case SKIP:
			color = SKIP_COLOR;
			break;
		case UNDEF:
			color = 0xff101010;
			break;
		default:
			return;
		}
		
		mResult = result;
		current_color = color;
		this.setBackgroundColor(color);
	}

	public RESULT getResult() {
		return mResult;
	}
	public void setCheck(boolean check){
		this.ischeck = check;
	}
	public boolean setCheckClick(){
		if(tmpcheckclick){
			if(ischeck)
				ischeck = false;
			else
				ischeck = true;
		}
		this.invalidate();
		return tmpcheckclick;
	}
	public boolean getischeck(){
		return ischeck;
	}
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		getDrawingRect(mRect);
		canvas.drawRect(mRect, mPaint);
		if(ischeck)
			canvas.drawBitmap(checkedbitmap, checkicon_left, checkicon_top, null);
		else
			canvas.drawBitmap(uncheckedbitmap, checkicon_left, checkicon_top, null);
	}
	
	private boolean tmpcheckclick = false;
	private boolean istouch = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		Log.d(TAG, " ________ action = " + event.getAction() + "   " + event.getX() + ", " + event.getY() + "   w = " + this.getWidth());
//		Log.d(TAG, "_____________________ onTouchEvent(),  " + event.getPointerCount());
		boolean ret = false;
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			tmpcheckclick = false;
			this.setBackgroundColor(CLICK_COLOR);
			if(event.getX() > checkicon_left){
				tmpcheckclick = true;
			}
			istouch = true;
			break;
		case MotionEvent.ACTION_MOVE:
			if(event.getX()>0 && event.getX() < this.getWidth() && event.getY() > 0 && event.getY() < this.getHeight()){
				this.setBackgroundColor(CLICK_COLOR);
				if(tmpcheckclick && event.getX() < checkicon_left){
					tmpcheckclick = false;
				}
			}else{
				tmpcheckclick = false;
				this.setBackgroundColor(current_color);
			}
			break;
		case MotionEvent.ACTION_UP:
			this.setBackgroundColor(current_color);
			istouch = false;
			break;
		}
		return super.onTouchEvent(event);
	}
	public boolean getIsTouch(){
		return istouch;
	}
}
