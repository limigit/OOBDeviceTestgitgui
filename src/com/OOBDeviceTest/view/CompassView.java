package com.OOBDeviceTest.view;

import com.OOBDeviceTest.R;
import com.OOBDeviceTest.R.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {

	private float yaw = 0;
	private Paint paint;
	private Drawable compass;

	public CompassView(Context context) {
		this(context, null);
	}

	public CompassView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		paint = new Paint();
		paint.setColor(Color.RED);
		// paint.setStyle(Style.FILL_AND_STROKE);
		paint.setStrokeWidth(2);

		compass = getResources().getDrawable(R.drawable.compass);

		this.setBackgroundColor(Color.WHITE);
	}

	public void update(float yaw) {
		if (Math.abs(this.yaw - yaw) > 1) {
			this.yaw = yaw;
			postInvalidate();
		}
	}

	protected void onDraw(Canvas canvas) {
		int height = getHeight();
		int width = getWidth();
		int midX = width / 2;
		int midY = height / 2;
		int size = (height > width ? width : height) * 3 / 8;
		compass.setBounds(midX - size, midY - size, midX + size, midY + size);
		canvas.save();
		canvas.rotate(-yaw, midX, midY);
		compass.draw(canvas);
		canvas.restore();
		paint.setTextSize(size / 3);
		canvas.drawText((int) yaw + "\260", 0, size / 3, paint);
	}
}
