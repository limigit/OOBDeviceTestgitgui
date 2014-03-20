package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.*;

import java.io.File;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.view.PointerLocationView;
import com.OOBDeviceTest.view.PointerLocationView.OnPointCountChangeListener;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

public class TouchTestActivity extends Activity {
	TextView mText;
	TextView mTitle;
	PointerLocationView mPointerView;
	private Button passButton;

	
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.touchtest);

		mPointerView = (PointerLocationView) findViewById(R.id.pointerview);
		mPointerView.setBackgroundColor(Color.TRANSPARENT);

		mPointerView
				.setOnPointCountChangeListener(new OnPointCountChangeListener() {

					
					public void onPointCountChange(int newPointCount) {
						Log.i("Jeffy", "Count:" + newPointCount);
						if (newPointCount >= 20) {
//							passButton.setVisibility(View.VISIBLE);
							passButton.performClick();
						}
					}
				});

		ControlButtonUtil.initControlButtonView(this);
//		passButton = (Button) findViewById(R.id.btn_Pass);
//		passButton.setVisibility(View.INVISIBLE);
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

}