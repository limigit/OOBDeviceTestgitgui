package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.view.LevelView;

import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

public class LevelTestActivity extends Activity {

	private LevelView levelView;

	
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);



		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.leveltest);
		levelView = (LevelView) findViewById(R.id.leveltestview);

		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		sensorManager.registerListener(new SensorListener() {

			
			public void onSensorChanged(int sensor, float[] values) {
				levelView.update(values[0], values[1]);
			}

			
			public void onAccuracyChanged(int sensor, int accuracy) {
			}
		}, SensorManager.SENSOR_ACCELEROMETER);
		ControlButtonUtil.initControlButtonView(this);
		
		Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onPause();
				
				onResume();
			}
		});
	}

	
	protected void onPause() {
		super.onPause();
	}

	
	protected void onResume() {
		super.onResume();
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}