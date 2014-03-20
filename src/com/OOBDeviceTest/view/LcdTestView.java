package com.OOBDeviceTest.view;

import android.R.color;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class LcdTestView extends View {

	private boolean grayScale = false;
	private boolean paneBorder = false;
	private Paint mPaint = new Paint();
	private Rect mRect = new Rect();

	public LcdTestView(Context context) {
		this(context, null, 0);
	}

	public LcdTestView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LcdTestView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void grayScale(boolean enable) {
		grayScale = enable;
	}

	public void paneBorder(boolean enable) {
		paneBorder = enable;
	}

	
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (grayScale) {
			mPaint.setStyle(Style.FILL);
			getDrawingRect(mRect);
			final int width = getWidth();
			final int scale = 16;
			final int scaleWidth = width / scale;
			final int left = 0;

			for (int i = 0; i < scale; i++) {
				int gray = i * 16;
				gray = gray > 255 ? 255 : gray;
				mPaint.setColor(Color.rgb(gray, gray, gray));
				mRect.left = left + i * scaleWidth;
				mRect.right = left + (i + 1) * scaleWidth;
				canvas.drawRect(mRect, mPaint);
			}

			canvas.drawRect(mRect, mPaint);
		}
		if (paneBorder) {
			canvas.drawColor(Color.BLACK);
			mPaint.setColor(Color.WHITE);
			mPaint.setStyle(Style.STROKE);
			mPaint.setStrokeWidth(5);
			getDrawingRect(mRect);
			canvas.drawRect(mRect, mPaint);
		}

	}
}
