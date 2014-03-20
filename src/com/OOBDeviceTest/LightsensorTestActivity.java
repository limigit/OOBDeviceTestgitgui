package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.util.List;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author LanBinYuan
 * @date 2011-06-11
 * 
 */

public class LightsensorTestActivity extends Activity {
	/** Called when the activity is first created. */
	private SensorManager sensorManager;
	private SensorEventListener lsn = null;

	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.lightsensortest);
		ControlButtonUtil.initControlButtonView(this);
		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		
		Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onResume();
			}
		});

	}

	
	protected void onResume() {
		super.onResume();

		lsn = new SensorEventListener() {
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub

			}

			public void onSensorChanged(SensorEvent e) {
				switch (e.sensor.getType()) {

				// Type Light
				case Sensor.TYPE_LIGHT: {
					TextView subTitle = (TextView) findViewById(R.id.Light);
					subTitle.setTextColor(android.graphics.Color.RED);

					TextView name = (TextView) findViewById(R.id.name_lig);
					name.setText(" 	name:" + String.valueOf(e.sensor.getName()));

					TextView vendor = (TextView) findViewById(R.id.vendor_lig);
					vendor.setText(" 	vendor:"
							+ String.valueOf(e.sensor.getVendor()));

					TextView version = (TextView) findViewById(R.id.version_lig);
					version.setText(" 	version:"
							+ String.valueOf(e.sensor.getVersion()));

					TextView maxRange = (TextView) findViewById(R.id.maxRange_lig);
					maxRange.setText(" 	maxRange:"
							+ String.valueOf(e.sensor.getMaximumRange()));

					TextView resolution = (TextView) findViewById(R.id.resolution_lig);
					resolution.setText(" 	resolution:"
							+ String.valueOf(e.sensor.getResolution()));

					TextView power = (TextView) findViewById(R.id.power_lig);
					power.setText(" 	power:"
							+ String.valueOf(e.sensor.getPower()));
					// --------------

					TextView x = (TextView) findViewById(R.id.light);
					x.setText(" 	Light:"
							+ String.valueOf(e.values[SensorManager.DATA_X]));
					x.setTextColor(android.graphics.Color.GREEN);
				}
				default:
					break;
				}

			}

		};

		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
		for (Sensor s : sensors) {
			sensorManager.registerListener(lsn, s,
					SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	//
	
	protected void onStop() {
		super.onStop();
		sensorManager.unregisterListener(lsn);
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
