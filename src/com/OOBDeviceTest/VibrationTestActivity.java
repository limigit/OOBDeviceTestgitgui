package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.widget.TextView;

public class VibrationTestActivity extends Activity {

	private Vibrator mVibrator;

	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);


		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.vibrationtest);

		TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
		TextView txtContent = (TextView) findViewById(R.id.txtContent);
		txtTitle.setText(R.string.VibrationTitle);
		txtContent.setText(R.string.VibrationContent);
		this.mVibrator = (Vibrator) getSystemService("vibrator");
		ControlButtonUtil.initControlButtonView(this);

	}

	protected void onPause() {
		super.onPause();
		this.mVibrator.cancel();
	}

	protected void onResume() {
		super.onResume();
		long pattern[] = { 500L, 500L };
		this.mVibrator.vibrate(pattern, 0);

	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
