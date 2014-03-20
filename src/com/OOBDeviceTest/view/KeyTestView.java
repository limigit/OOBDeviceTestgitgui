package com.OOBDeviceTest.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class KeyTestView extends View{
	private final static String TAG = "KeyTestView";
	
	private int COLOR_NOR = Color.GRAY;
	private int COLOR_DOWN = Color.YELLOW;
	private int COLOR_PASS = Color.GREEN;
	private Paint mPaint = null;
	private Paint mWordPaint = null;
	private final static int ROW = 4;
	private final static int LINE = 3;
	private final static int PER_GAP = 3;
	private int per_width = 0;
	private int per_height = 0;
	ArrayList<KeyStruct> mKeyArray = new ArrayList<KeyStruct>();
	public KeyTestView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
		mPaint.setColor(COLOR_NOR);
		mPaint.setStyle(Style.FILL);
		mWordPaint = new Paint();
		mWordPaint.setColor(Color.WHITE);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		per_width = (this.getWidth() - PER_GAP * (ROW + 1)) / ROW;
		per_height = (this.getHeight() - PER_GAP * (LINE + 1))/ LINE;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int current_x = PER_GAP;
		int current_y = PER_GAP;
		int i = 0;
		while(i < mKeyArray.size()){
			int color = COLOR_NOR;
			if(mKeyArray.get(i).getIsPass()){
				color = COLOR_PASS;
			}
			if(mKeyArray.get(i).getIsDown()){
				color = COLOR_DOWN;
			}
			mPaint.setColor(color);
			canvas.drawRect(current_x, current_y, current_x + per_width, current_y + per_height, mPaint);
			canvas.drawText(mKeyArray.get(i).getKeyName(), current_x, current_y + per_height / 2, mWordPaint);
			i ++;
			if(i % ROW == 0){
				current_x = PER_GAP;
				current_y += (per_height + PER_GAP);
			}else{
				current_x += (per_width + PER_GAP);
			}
		}
	}
	
	public void addKey(String name, int code){
		KeyStruct mkey = new KeyStruct(name, code);
		mKeyArray.add(mkey);
	}	
	public void setKeyDown(int code){
		for(KeyStruct key : mKeyArray){
			if(key.getKeyCode() == code){
				key.setIsDown(true);
				break;
			}
		}
		invalidate();
	}
	public void setKeyUp(int code){
		for(KeyStruct key : mKeyArray){
			if(key.getKeyCode() == code){
				key.setIsDown(false);
				break;
			}
		}
		invalidate();
	}
	
	private class KeyStruct{
		private String keyname;
		private int keycode;
		private boolean isdown;
		private boolean pass;
		public KeyStruct(String name, int code){
			this.keyname = name;
			this.keycode = code;
			this.isdown = false;
			this.pass = false;
		}
		public void setIsDown(boolean tmp){
			this.isdown = tmp;
			if(tmp)
				this.pass = true;
		}
		public boolean getIsDown(){
			return this.isdown;
		}
		public boolean getIsPass(){
			return this.pass;
		}
		public String getKeyName(){
			return this.keyname;
		}
		public int getKeyCode(){
			return this.keycode;
		}
	}
}