package com.OOBDeviceTest.helper;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.OOBDeviceTest.R;

public class Drawl extends View{

	private int mov_x;
	private int mov_y;
	private Paint paint;
	private Paint paint2;
	private Paint paint3;
	private Paint clearPaint;
	private Canvas canvas;
	private Bitmap bitmap;
	boolean isPassed = false;
	Context mContext;
	TextView navigationText;
	private AlertDialog.Builder alert1;
	
	int currentIndex = 0;
	ArrayList<Point> listPointsTop;
	ArrayList<Point> listPointsBottom;
	ArrayList<Point> listPointsLeft;
	ArrayList<Point> listPointsRight;
	ArrayList<Point> listPointsLeftTop2RightBottom;
	ArrayList<Point> listPointsRightTop2LeftBottom;
	int distance = 30;
	public Drawl(Context context, TextView text) {
		super(context);
		mContext = context;
		navigationText = text;
		initPoint();
		initRetestDialog();
		paint=new Paint(Paint.DITHER_FLAG);
		paint2=new Paint(Paint.DITHER_FLAG);
		paint3=new Paint();
		clearPaint = new Paint(Paint.DITHER_FLAG);
		bitmap = Bitmap.createBitmap(1360, 768, Bitmap.Config.ARGB_8888);
		canvas=new Canvas();
		canvas.setBitmap(bitmap);
		
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(5);
		paint.setColor(Color.WHITE);
		// paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		paint.setAntiAlias(true);

		paint2.setAntiAlias(true);
		paint2.setColor(Color.RED);
		paint2.setStrokeWidth(10);    
		paint2.setStyle(Style.STROKE);
		
		paint3.setAntiAlias(true);
		paint3.setColor(Color.BLUE);
		paint3.setStrokeWidth(5);    
		PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);
		paint3.setPathEffect(effects);
		canvas.drawLine(25, 25, 25, 743, paint3);
	}

	private void initPoint(){
		listPointsTop = new ArrayList<Point>();
		for (int i = 0; i < 3; i++) {
			Point point = new Point();
			listPointsTop.add(point);
		}
		listPointsTop.get(0).x = 25;
		listPointsTop.get(0).y = 25;
		listPointsTop.get(1).x = 683;
		listPointsTop.get(1).y = 25;
		listPointsTop.get(2).x = 1335;
		listPointsTop.get(2).y = 25;
		
		listPointsBottom = new ArrayList<Point>();
		for (int i = 0; i < 3; i++) {
			Point point = new Point();
			listPointsBottom.add(point);
		}
		listPointsBottom.get(0).x = 25;
		listPointsBottom.get(0).y = 743;
		listPointsBottom.get(1).x = 683;
		listPointsBottom.get(1).y = 743;
		listPointsBottom.get(2).x = 1335;
		listPointsBottom.get(2).y = 743;
		
		listPointsLeft = new ArrayList<Point>();
		for (int i = 0; i < 3; i++) {
			Point point = new Point();
			listPointsLeft.add(point);
		}
		listPointsLeft.get(0).x = 25;
		listPointsLeft.get(0).y = 25;
		listPointsLeft.get(1).x = 25;
		listPointsLeft.get(1).y = 384;
		listPointsLeft.get(2).x = 25;
		listPointsLeft.get(2).y = 743;
		
		listPointsRight = new ArrayList<Point>();
		for (int i = 0; i < 3; i++) {
			Point point = new Point();
			listPointsRight.add(point);
		}
		listPointsRight.get(0).x = 1335;
		listPointsRight.get(0).y = 25;
		listPointsRight.get(1).x = 1335;
		listPointsRight.get(1).y = 384;
		listPointsRight.get(2).x = 1335;
		listPointsRight.get(2).y = 743;
		
		listPointsLeftTop2RightBottom = new ArrayList<Point>();
		for (int i = 0; i < 3; i++) {
			Point point = new Point();
			listPointsLeftTop2RightBottom.add(point);
		}
		listPointsLeftTop2RightBottom.get(0).x = 25;
		listPointsLeftTop2RightBottom.get(0).y = 25;
		listPointsLeftTop2RightBottom.get(1).x = 683;
		listPointsLeftTop2RightBottom.get(1).y = 384;
		listPointsLeftTop2RightBottom.get(2).x = 1335;
		listPointsLeftTop2RightBottom.get(2).y = 743;
		
		
		listPointsRightTop2LeftBottom = new ArrayList<Point>();
		for (int i = 0; i < 3; i++) {
			Point point = new Point();
			listPointsRightTop2LeftBottom.add(point);
		}
		listPointsRightTop2LeftBottom.get(0).x = 1335;
		listPointsRightTop2LeftBottom.get(0).y = 25;
		listPointsRightTop2LeftBottom.get(1).x = 683;
		listPointsRightTop2LeftBottom.get(1).y = 384;
		listPointsRightTop2LeftBottom.get(2).x = 25;
		listPointsRightTop2LeftBottom.get(2).y = 743;
	}
	
	private void showRetestDialog(){
			/*
		new AlertDialog.Builder(mContext)
		.setTitle(mContext.getResources().getString(R.string.touchpanel_dailog_title))
		.setMessage(mContext.getResources().getString(R.string.touchpanel_dailog_msg))
		.setNegativeButton(mContext.getResources().getString(R.string.touchpanel_fail), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent();
				intent.setAction("com.DeviceTest.TouchPanelTestActivity.TouchPanelTestActionFilter");
				Bundle bundle = new Bundle();
				bundle.putInt("test_result", 1);
				intent.putExtras(bundle);
				mContext.sendBroadcast(intent);
			}
		}).setNeutralButton(mContext.getResources().getString(R.string.touchpanel_retest), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				redrawPoints();
			}
		})
	
		.setPositiveButton(mContext.getResources().getString(R.string.touchpanel_skip), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent();
				intent.setAction("com.DeviceTest.TouchPanelTestActivity.TouchPanelTestActionFilter");
				Bundle bundle = new Bundle();
				bundle.putInt("test_result", 0);
				intent.putExtras(bundle);
				mContext.sendBroadcast(intent);
			}
			
		})
		.setCancelable(false)
		*/
		if(alert1!=null)alert1.show();
	}
	
	private void initRetestDialog(){
	  alert1= new AlertDialog.Builder(mContext);
		alert1.setTitle(mContext.getResources().getString(R.string.touchpanel_dailog_title));
		alert1.setMessage(mContext.getResources().getString(R.string.touchpanel_dailog_msg));
		alert1.setNegativeButton(mContext.getResources().getString(R.string.touchpanel_fail), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				alert1=null;
				Intent intent = new Intent();
				intent.setAction("com.DeviceTest.TouchPanelTestActivity.TouchPanelTestActionFilter");
				Bundle bundle = new Bundle();
				bundle.putInt("test_result", 1);
				intent.putExtras(bundle);
				mContext.sendBroadcast(intent);
			}
		});
		alert1.setNeutralButton(mContext.getResources().getString(R.string.touchpanel_retest), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				redrawPoints();
			}
		});
		alert1.setCancelable(false);
	
	}
	
	private void redrawPoints(){
		currentIndex = 0;
		initPoint();
		navigationText.setText(mContext.getResources().getString(R.string.touchpanel_test_left));
		
		
		clearPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		canvas.drawPaint(clearPaint);
		clearPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
		
		canvas.drawLine(25, 25, 25, 743, paint3);
	}
	
	private void drawPoints(Point point, Canvas canvas, Paint paint){
			canvas.drawCircle(point.x, point.y, 25, paint);
	}
	
	private void checkPoint(int x, int y, int lineIndex){
		
		switch (lineIndex) {
		case 0:{
			for (Point point : listPointsLeft){
				if ((point.x - x)*(point.x - x) + (point.y - y)*(point.y - y) < distance * distance) {
					listPointsLeft.remove(point);
					drawPoints(point, canvas, paint2);
					break;
				}
			}
			break;
		}
		case 1:{
			for (Point point : listPointsTop){
				if ((point.x - x)*(point.x - x) + (point.y - y)*(point.y - y) < distance * distance) {
					listPointsTop.remove(point);
					drawPoints(point, canvas, paint2);
					break;
				}
			}
			break;
		}
		case 2:{
			for (Point point : listPointsRight){
				if ((point.x - x)*(point.x - x) + (point.y - y)*(point.y - y) < distance * distance) {
					listPointsRight.remove(point);
					drawPoints(point, canvas, paint2);
					break;
				}
			}
			break;
		}
		case 3:{
			for (Point point : listPointsBottom){
				if ((point.x - x)*(point.x - x) + (point.y - y)*(point.y - y) < distance * distance) {
					listPointsBottom.remove(point);
					drawPoints(point, canvas, paint2);
					break;
				}
			}
			break;
		}
		case 4:{
			for (Point point : listPointsLeftTop2RightBottom){
				if ((point.x - x)*(point.x - x) + (point.y - y)*(point.y - y) < distance * distance) {
					listPointsLeftTop2RightBottom.remove(point);
					drawPoints(point, canvas, paint2);
					break;
				}
			}
			break;
		}
		case 5:{
			for (Point point : listPointsRightTop2LeftBottom){
				if ((point.x - x)*(point.x - x) + (point.y - y)*(point.y - y) < distance * distance) {
					listPointsRightTop2LeftBottom.remove(point);
					drawPoints(point, canvas, paint2);
					break;
				}
			}
			break;
		}

		default:
			break;
		}
	}
	
	private boolean isPass(){
		switch (currentIndex) {
		case 0:
			if (listPointsLeft.isEmpty()) {
				navigationText.setText(mContext.getResources().getString(R.string.touchpanel_test_top));
				canvas.drawLine(25, 25, 1335, 25, paint3);
				currentIndex ++ ;
			}else {
				showRetestDialog();
			}
			break;
		case 1:
			if (listPointsTop.isEmpty()) {
				navigationText.setText(mContext.getResources().getString(R.string.touchpanel_test_right));
				canvas.drawLine(1335, 25, 1335, 743, paint3);
				currentIndex ++ ;
			}else {
				showRetestDialog();
			}
			break;
		case 2:
			if (listPointsRight.isEmpty()) {
				navigationText.setText(mContext.getResources().getString(R.string.touchpanel_test_bottom));
				canvas.drawLine(25, 743, 1335, 743, paint3);
				currentIndex ++ ;
			}else {
				showRetestDialog();
			}
			break;
		case 3:
			if (listPointsBottom.isEmpty()) {
				navigationText.setText(mContext.getResources().getString(R.string.touchpanel_test_lt2rb));
				canvas.drawLine(25, 25, 1335, 743, paint3);
				currentIndex ++ ;
			}else {
				showRetestDialog();
			}
			break;
		
		case 4:
			if (listPointsLeftTop2RightBottom.isEmpty()) {
				navigationText.setText(mContext.getResources().getString(R.string.touchpanel_test_rt2lb));
				canvas.drawLine(25, 743, 1335, 25, paint3);
				currentIndex ++ ;
			}else {
				showRetestDialog();
			}
			break;
		case 5:
			if (listPointsRightTop2LeftBottom.isEmpty()) {
				return true;
			}else {
				showRetestDialog();
			}
			break;
		default:
			break;
		}
		return false;
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		//  super.onDraw(canvas);
		canvas.drawBitmap(bitmap,0,0,null);
		canvas.drawLine(120, 50, 1310, 688, paint);
		canvas.drawLine(50, 80, 1240, 718, paint);
		canvas.drawLine(50, 688, 1240, 50, paint);
		canvas.drawLine(120, 718, 1310, 80, paint);
		canvas.drawLine(120, 50, 1240, 50, paint);
		canvas.drawLine(120, 718, 1240, 718, paint);
		canvas.drawLine(50, 80, 50, 688, paint);
		canvas.drawLine(1310, 80, 1310, 688, paint);
	}
	public boolean onTouchEvent(MotionEvent event){
		int action =event.getAction();
		
		switch (action) {		
		case MotionEvent.ACTION_MOVE:{
			canvas.drawLine(mov_x, mov_y, event.getX(), event.getY(), paint2);
			
			checkPoint(mov_x, mov_y, currentIndex);
			invalidate();
		}		
		break;
		case MotionEvent.ACTION_DOWN:{
			mov_x=(int) event.getX();
			mov_y=(int) event.getY();

			canvas.drawPoint(mov_x, mov_y, paint2);
			invalidate();  
		}
		break;
		case MotionEvent.ACTION_UP:{
			if (isPass()) {
				Intent intent = new Intent();
				intent.setAction("com.DeviceTest.TouchPanelTestActivity.TouchPanelTestActionFilter");
				Bundle bundle = new Bundle();
				bundle.putInt("test_result", 2);
				intent.putExtras(bundle);
				mContext.sendBroadcast(intent);
			}
			break;
		}
		}		
		mov_x=(int) event.getX();
		mov_y=(int) event.getY();
		return true;
	}
}
