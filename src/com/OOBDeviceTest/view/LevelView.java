package com.OOBDeviceTest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class LevelView extends View {

	private float x = 0;
	private float y = 0;
	private Paint paint = new Paint();
	private RectF rectF = new RectF();

	public LevelView(Context context) {
		this(context, null);
	}

	public LevelView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LevelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setBackgroundColor(Color.WHITE);
	}

	public void update(float values, float values2) {
		this.x = values;
		this.y = values2;
		postInvalidate();
	}

	protected void onDraw(Canvas canvas) {
		int height = getHeight();
		int width = getWidth();
		int size = (height > width ? width : height) * 3 / 8;
		int midX = width / 2;
		int midY = height / 2;
		int radius = size / 6;
		float scaler = (size - radius) / 10F;

		paint.reset();
		paint.setAlpha(150);

		paint.setColor(Color.RED);
		paint.setStyle(Style.FILL);

		canvas.drawCircle(midX - x * scaler, midY + y * scaler, radius, paint);
		canvas.drawCircle(midX - x * scaler, midY + size + radius, radius,
				paint);
		canvas.drawCircle(midX - size - radius, midY + y * scaler, radius,
				paint);

		paint.setTextSize(2 * radius);
		canvas.drawText("x=" + ((int) (-x * 100) / 100F) + ",y="
				+ ((int) (-y * 100) / 100F), 0, 2 * radius, paint);

		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setColor(Color.BLACK);

		canvas.drawCircle(midX, midY, size, paint);
		canvas.drawCircle(midX, midY, radius + 1, paint);
		rectF.set(midX - size - 2 * radius, midY - size, midX - size, midY
				+ size);
		canvas.drawRoundRect(rectF, radius, radius, paint);
		rectF.set(midX - size, midY + size, midX + size, midY + size + 2
				* radius);
		canvas.drawRoundRect(rectF, radius, radius, paint);

		canvas.drawLine(midX - radius, midY + size, midX - radius, midY + size
				+ 2 * radius, paint);
		canvas.drawLine(midX + radius, midY + size, midX + radius, midY + size
				+ 2 * radius, paint);

		canvas.drawLine(midX - size - 2 * radius, midY - radius, midX - size,
				midY - radius, paint);
		canvas.drawLine(midX - size - 2 * radius, midY + radius, midX - size,
				midY + radius, paint);

	}
}
