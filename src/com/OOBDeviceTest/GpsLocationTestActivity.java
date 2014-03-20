package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.util.Iterator;

import com.OOBDeviceTest.GpsTestActivity.MystatusListener;
import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.SystemUtil;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class GpsLocationTestActivity extends Activity {
	private static final String TAG = GpsLocationTestActivity.class
			.getSimpleName();
	LocationManager mLocatManager;
	LocationListener mLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// mLocatManager.getGpsStatus(gpsStatus);
			// mResult.setText("Debug:get location data...");
			// Log.e("Jeffy", "!!!!!!!!!!!!!!!!Debug:get location data...");
			// if (gpsStatus != null && gpsStatus.getTimeToFirstFix() != 0) {
			// mResult.setText("Debug:TTFF: " + gpsStatus.getTimeToFirstFix());
			// Log.e("Jeffy", "!!!!!!!!!!!!!!!!Debug:TTFF:" +
			// gpsStatus.getTimeToFirstFix());
			// return;
			// }
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
	TextView mResult;
	TextView mText;
	TextView mTitle;

	private GpsStatus.Listener statusListener = new MystatusListener();
	private static final int step = 1000; // msecs
	private static final int MSG_RUN = 0;

	boolean stop = false;
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			removeMessages(MSG_RUN);
			switch (msg.what) {
			case MSG_RUN:
				((Runnable) msg.obj).run();
				break;
			default:
				break;
			}
		}
	};
	Runnable mFailedRunnable = new Runnable() {

		public void run() {
			if (stop) {
				return;
			}
			findViewById(R.id.btn_Fail).performClick();
		}
	};
	Runnable mSkipRunnable = new Runnable() {

		public void run() {
			if (stop) {
				return;
			}
			findViewById(R.id.btn_Skip).performClick();
		}
	};

	Runnable mPassRunnable = new Runnable() {

		public void run() {
			if (stop) {
				return;
			}
			findViewById(R.id.btn_Pass).performClick();
		}
	};

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.gpslocationtest);

		this.mResult = (TextView) findViewById(R.id.gpslocationresultText);
		this.mResult.setVisibility(View.VISIBLE);
		this.mResult.setGravity(Gravity.CENTER);
		ControlButtonUtil.initControlButtonView(this);
		this.mResult.setText("Wait for location data...");

		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);

		this.mLocatManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// mLocatManager.getGpsStatus(gpsStatus);
		// if(gpsStatus != null && gpsStatus.getTimeToFirstFix() != 0) {
		// mResult.setText("TTFF already been set, please turn off & turn on GPS..");
		//
		// mHandler.postDelayed(mSkipRunnable, 5000);
		// return;
		// }

		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		wifiManager.setWifiEnabled(false);
		BluetoothAdapter.getDefaultAdapter().disable();
		Settings.Secure.setLocationProviderEnabled(getContentResolver(),
				LocationManager.GPS_PROVIDER, true);

		if (!mLocatManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			this.mResult.setText("Gps location was not enabled!");
			mHandler.postDelayed(mSkipRunnable, 5000);
			return;
		}

		mLocatManager.addGpsStatusListener(this.statusListener);
		mLocatManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				step, 0, mLocationListener);
		stop = true;
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				SystemUtil.execRootCmd(DeviceTest.GPS_COLD_START_PATH);
				SystemUtil.execRootCmd(DeviceTest.GPS_COLD_START_PATH);
			}
		}, 2000);
		stop = false;
		mHandler.postDelayed(mFailedRunnable, 120 * 1000);
	}

	protected void onStop() {
		super.onStop();
		this.mLocatManager.removeGpsStatusListener(this.statusListener);
		this.mLocatManager.removeUpdates(this.mLocationListener);
		stop = true;
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

	GpsStatus gpsStatus;

	class MystatusListener implements GpsStatus.Listener {

		public void onGpsStatusChanged(int event) {
			gpsStatus = mLocatManager.getGpsStatus(null);
			if(stop) {
				return;
			}
			switch (event) {
			case GpsStatus.GPS_EVENT_FIRST_FIX:

				Log.e("Jeffy",
						"GPS_EVENT_FIRST_FIX:" + gpsStatus.getTimeToFirstFix());
				String ttff = ((int) (gpsStatus.getTimeToFirstFix() / 100)) / 10.0 + "s";
				mResult.setText("TTFF: "
						+ ttff);
				ControlButtonUtil.setResult(DeviceTest.RESULT_INFO_HEAD
						+ ttff);
				mHandler.removeMessages(MSG_RUN);
				if(gpsStatus.getTimeToFirstFix() > 90 * 1000 ) {
					mHandler.postDelayed(mFailedRunnable, 2 * 1000);
				} else {
					mHandler.postDelayed(mPassRunnable, 2 * 1000);
				}
				findViewById(R.id.btn_Pass).setVisibility(View.VISIBLE);
				findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
				break;
			default:
				break;
			}
		}

	}
}
