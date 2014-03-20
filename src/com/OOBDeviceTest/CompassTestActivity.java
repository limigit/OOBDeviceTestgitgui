package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.SystemUtil;
import com.OOBDeviceTest.view.CompassView;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CompassTestActivity extends Activity {

	private CompassView compassView;
	private float firstYaw = 0;
	private int count;
	private float lastYaw = 0;
	private float STABLE_VALUE = 0.5F;
	private int STABLE_COUNT = 10;
	TextView compassText;
	String accuracyStr = "";

	int[] yawRange = new int[] { 285 - 30, 285 + 30 };
	boolean start = false;

	private static enum STATE {
		WAIT_FOR_FIRST_STABLE, WAIT_FOR_ROTATE, WAIT_FOR_SECOND_STABLE, UNDEF
	};

	Button startButton;
	STATE state;
	boolean stop = false;
	Handler mHandler = new Handler();
	Runnable mFailedRunnable = new Runnable() {

		public void run() {
			if (stop) {
				return;
			}
			mHandler.removeCallbacks(mFailedRunnable);
			findViewById(R.id.btn_Fail).performClick();
		}
	};
	protected String text = "";

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.compasstest);
		compassView = (CompassView) findViewById(R.id.compasstestview);
		state = STATE.WAIT_FOR_FIRST_STABLE;
		count = 0;
		compassText = (TextView) findViewById(R.id.compassText);

		startButton = (Button) findViewById(R.id.Start);
		startButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				start = true;
				count = 0;
				state = STATE.WAIT_FOR_FIRST_STABLE;
				startButton.setEnabled(false);
				text = "wait for stable data...";
			}
		});

		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		sensorManager.registerListener(new SensorListener() {
			public void onSensorChanged(int sensor, float[] values) {
				if (stop) {
					sensorManager.unregisterListener(this);
				}
				if ((int) values[0] == 0 || values[0] == 360) {
					return;
				}

				float yaw = values[0];
				compassView.update(yaw);
				Log.e("jeffy", "yaw:" + yaw);
				if (!start) {
					return;
				}
				boolean pass = Math.abs(lastYaw - yaw) < STABLE_VALUE;
				if (state == STATE.WAIT_FOR_ROTATE) {
					pass = !pass;
				}

				if (pass) {
					count++;
				} else {
					count = 0;
				}
				if (count >= STABLE_COUNT) {
					switch (state) {
					case WAIT_FOR_FIRST_STABLE:
						firstYaw = yaw;
						if (firstYaw < yawRange[0] || firstYaw > yawRange[1]) {
							text = "invalid orientation:" + (int) firstYaw;

							state = STATE.UNDEF;
							mHandler.postDelayed(mFailedRunnable, 2000);
							sensorManager.unregisterListener(this);
							break;
						}

						state = STATE.WAIT_FOR_ROTATE;
						count = 0;
						text = "valid orientation, first stable data:"
								+ (int) firstYaw + ", wait for rotate...";
						break;
					case WAIT_FOR_ROTATE:
						state = STATE.WAIT_FOR_SECOND_STABLE;
						count = 0;
						text = "first stable data:" + (int) firstYaw
								+ ", wait for stable data...";
						break;
					case WAIT_FOR_SECOND_STABLE:
						state = STATE.UNDEF;
						float deltaYaw = Math.abs(yaw - firstYaw);
						if (deltaYaw <= 5) {
							findViewById(R.id.btn_Pass).performClick();
						} else {
							findViewById(R.id.btn_Fail).performClick();
						}
						count = 0;
						sensorManager.unregisterListener(this);
						text = "first stable data:" + (int) firstYaw
								+ ", second stable data:" + (int) yaw;
						break;
					default:
						return;
					}
				}
				compassText.setText(accuracyStr + "\n" + text);
				lastYaw = yaw;
			}

			public void onAccuracyChanged(int sensor, int accuracy) {
				Log.e("Jeffy", "onAccuracyChanged" + accuracy);
				accuracyStr = "accuracy:" + accuracy;
				if (accuracy == 3) {
					accuracyStr += ",pass";
					if (!start) {
						accuracyStr += ",ready";
						startButton.setEnabled(true);
					}
				} else {
					accuracyStr += ",failed";
					accuracyStr += ",needs correction!";
					state = STATE.UNDEF;
					start = false;
					text = "";
					startButton.setEnabled(false);
				}

				compassText.setText(accuracyStr + "\n" + text);
			}
		}, SensorManager.SENSOR_ORIENTATION);
		ControlButtonUtil.initControlButtonView(this);
		findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
	}

	protected void onStop() {
		super.onStop();
		stop = true;
		mHandler.removeCallbacks(mFailedRunnable);
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}