package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.SystemUtil;
import com.OOBDeviceTest.helper.TestCase;
import com.OOBDeviceTest.helper.TestCase.RESULT;

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
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class GpsTestActivity extends Activity {
	private static final String TAG = GpsTestActivity.class.getSimpleName();
	LocationManager mLocatManager;

	private GpsStatus.Listener statusListener = new MystatusListener();
	HashMap<Integer, Integer> passSatellites = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> Satellites = new HashMap<Integer, Integer>();

	TextView ttffView, cnView, timerView, satellitesView;
	int ttff = 0;
	static final int CN_PASS = 38;
	static final int CN_PASS_NO = 4;
	static final int TTFF_PASS = 90;
	static final int TIMEOUT = 120 * 1000;
	TestCase.RESULT ttffResult = TestCase.RESULT.UNDEF;
	TestCase.RESULT cnResult = TestCase.RESULT.UNDEF;
	boolean ttffPass = false;
	boolean cnPass = false;
	boolean stop = false;
	Handler mHandler = new Handler();
	Runnable timerRunnable = new Runnable() {
		int time = 0;

		@Override
		public void run() {
			if (stop) {
				return;
			}
			time++;
			timerView.setText("Time:" + time);
			String ttffText = "Wait for TTFF";
			int count = time % 3;
			switch (count) {
			case 0:
				ttffText += ".";
				break;
			case 1:
				ttffText += "..";
				break;
			case 2:
				ttffText += "...";
				break;
			default:
				break;
			}
			if (!ttffPass) {
				ttffView.setText(ttffText);
			}
			if (!ttffPass || !cnPass)
				mHandler.postDelayed(this, 1000);
		}
	};

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.gpstest);

		ControlButtonUtil.initControlButtonView(this);

		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
		findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);

		this.mLocatManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		wifiManager.setWifiEnabled(false);
		BluetoothAdapter.getDefaultAdapter().disable();
		try {
			Settings.Secure.setLocationProviderEnabled(getContentResolver(),
					LocationManager.GPS_PROVIDER, true);
		} catch (Exception e) {
			// TODO: handle exception
		}

		mLocatManager.addGpsStatusListener(this.statusListener);
		mLocatManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 0, locationListener);

		ttffView = (TextView) findViewById(R.id.gpsTTFF);
		cnView = (TextView) findViewById(R.id.gpsCN);
		timerView = (TextView) findViewById(R.id.gpsTimer);
		satellitesView = (TextView) findViewById(R.id.gpsSatellite);
		satellitesView.setText("Satellites:\n");
		cnView.setText("Passed Satellites:0");

		stop = true;
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				SystemUtil.execRootCmd(DeviceTest.GPS_COLD_START_PATH);

				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (stop) {
							return;
						}
						Log.e("Jeffy",
								"Test Timeout, ttffPass:" + ttffPass
										+ ", cnPass:" + cnPass + ", time:"
										+ timerView.getText());

						cnPass = true;
						ttffPass = true;
						over();
					}
				}, TIMEOUT);
				mHandler.post(timerRunnable);
				stop = false;
			}
		}, 2000);

	}

	LocationListener locationListener = new LocationListener() {

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}

		public void onLocationChanged(Location location) {
		}

	};

	protected void onStop() {

		super.onStop();

		this.mLocatManager.removeGpsStatusListener(this.statusListener);
		mLocatManager.removeUpdates(locationListener);
		stop = true;
	}

	GpsStatus gpsStatus;

	class MystatusListener implements GpsStatus.Listener {

		public void onGpsStatusChanged(int event) {
			if (stop) {
				return;
			}
			gpsStatus = mLocatManager.getGpsStatus(null);

			switch (event) {
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Iterable<GpsSatellite> allSatellites = gpsStatus
						.getSatellites();
				Iterator<GpsSatellite> iterator = allSatellites.iterator();
				String msg = "Satellites:\n";

				while (iterator.hasNext()) {
					GpsSatellite gpsSatellite = iterator.next();
					int snr = (int) gpsSatellite.getSnr();
					int prn = gpsSatellite.getPrn();
					if (snr < 0) {
						continue;
					}
					msg += "" + prn + "(" + snr + ")\n";
					Integer oldSnr = Satellites.get(prn);
					if (oldSnr == null || snr > oldSnr) {
						Satellites.put(prn, snr);
						if (snr >= CN_PASS) {
							passSatellites.put(prn, snr);
						}
					}
				}
				satellitesView.setText(msg);

				int passCount = passSatellites.size();
				msg = "Passed Satellites:" + passCount;
				if (passCount >= CN_PASS_NO) {
					Log.e("Jeffy",
							"Get 4 CN > 38, ttffPass:" + ttffPass + ", cnPass:"
									+ cnPass + ", time:" + timerView.getText());
					msg += "(Pass)";
					cnPass = true;
					over();
				}
				cnView.setText(msg);
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				ttffPass = true;
				ttff = gpsStatus.getTimeToFirstFix() / 1000;
				msg = "TTFF:" + ttff;

				Log.e("Jeffy", "Get TTFF, ttffPass:" + ttffPass + ", cnPass:"
						+ cnPass + ", time:" + timerView.getText());

				if (ttff <= TTFF_PASS) {
					msg += "(Pass)";
				} else {
					msg += "(Failed)";
				}

				over();
				ttffView.setText(msg);
				break;
			default:
				break;
			}
		}
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

	private String getCNs() {
		if (Satellites.size() == 0) {
			return "";
		}
		String msg = "";
		for (int i = 0; i < 255; i++) {
			if (Satellites.get(i) != null) {
				msg += "" + i + "-" + Satellites.get(i) + ",";
			}
		}
		return msg.substring(0, msg.length() - 1);
	}

	public String getResult() {
		String result = DeviceTest.RESULT_INFO_HEAD_JUST_INFO;
		result += DeviceTest.formatResult("GPS C/No", cnResult,
				DeviceTest.RESULT_INFO_HEAD + getCNs()) + "\n";
		result += DeviceTest.formatResult("GPS Location", ttffResult,
				DeviceTest.RESULT_INFO_HEAD + ttff);

		return result;
	}

	public void over() {
		if (!cnPass || !ttffPass) {
			return;
		}
		stop = true;
		Log.e("Jeffy", "Test Over, ttffPass:" + ttffPass + ", cnPass:" + cnPass
				+ ", time:" + timerView.getText());

		if (passSatellites.size() >= CN_PASS_NO) {
			cnResult = TestCase.RESULT.OK;
		} else {
			cnResult = TestCase.RESULT.FAIL;
		}

		if (ttff <= TTFF_PASS) {
			ttffResult = TestCase.RESULT.OK;
		} else {
			ttffResult = TestCase.RESULT.FAIL;
		}
		ControlButtonUtil.setResult(getResult());
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (cnResult == ttffResult && cnResult == TestCase.RESULT.OK) {
					findViewById(R.id.btn_Pass).performClick();
				} else {
					findViewById(R.id.btn_Fail).performClick();
				}
			}
		}, 3000);
	}
}
