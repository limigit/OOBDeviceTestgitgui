package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.util.List;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.graphics.Color;
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

public class MsensorTestActivity extends Activity {
	/** Called when the activity is first created. */
	private SensorManager sensorManager;
	private SensorEventListener lsn = null;

	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.msensortest);
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
				TextView subTitle = (TextView) findViewById(R.id.Magnetic);
				subTitle.setTextColor(Color.rgb(255, 0, 0));

				String info = " 	name:" + String.valueOf(e.sensor.getName());
				info += "\n";

				info += " 	vendor:" + String.valueOf(e.sensor.getVendor());
				info += "\n";
				info += " 	version:" + String.valueOf(e.sensor.getVersion());
				info += "\n";
				info += " 	maxRange:"
						+ String.valueOf(e.sensor.getMaximumRange());
				info += "\n";
				info += " 	resolution:"
						+ String.valueOf(e.sensor.getResolution());
				info += "\n";
				info += " 	power:" + String.valueOf(e.sensor.getPower());

				TextView infoView = (TextView) findViewById(R.id.magnetic_info);
				infoView.setText(info);
				// --------------

				TextView x = (TextView) findViewById(R.id.magnetic_x);
				x.setText(" 	x:"
						+ String.valueOf(e.values[SensorManager.DATA_X]));
				x.setTextColor(android.graphics.Color.GREEN);

				TextView y = (TextView) findViewById(R.id.magnetic_y);
				y.setText(" 	y:"
						+ String.valueOf(e.values[SensorManager.DATA_Y]));
				y.setTextColor(android.graphics.Color.GREEN);

				TextView z = (TextView) findViewById(R.id.magnetic_z);
				z.setText(" 	z:"
						+ String.valueOf(e.values[SensorManager.DATA_Z]));
				z.setTextColor(android.graphics.Color.GREEN);

			}

		};

		Sensor sensors = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		sensorManager.registerListener(lsn, sensors,
				SensorManager.SENSOR_DELAY_NORMAL);
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
