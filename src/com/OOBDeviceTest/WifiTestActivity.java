package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpConnection;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.SystemUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
//import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;

public class WifiTestActivity extends Activity {

	private static final String TAG = WifiTestActivity.class.getSimpleName();
	private static final String HTTP_TEST_URL = "http://172.16.9.1/";
	private static final String PING_TEST_ADDR = "172.16.9.1";
	private static final int TEST_FAILED_DELAY = 5000;
	private static final int MSG_ERROR = 1;
	private static final int MSG_HTTP_TEST = 2;
	private static final int MSG_SCAN = 3;
	private static final int MSG_HTTP_TEST_PASS = 4;
	private static final int MSG_HTTP_TEST_FAILED = 5;
	private static final int MSG_FAIL = 6;
	private static final int MSG_PING_TEST_PASS = 7;
	private static final int MSG_PING_TEST_FAILED = 8;
	private static final int MSG_PING_TEST = 9;
	private static final int MSG_FINISH_TEST = 10;
	private Handler mHandler;
	private BroadcastReceiver mReceiver;
	TextView mResult;
	TextView mInfoText;
	boolean stop = false;
	private boolean mReadyToTest = false;
	private static boolean isWifiFirstTest = true;
	TextView mText;
	TextView mTitle;
	ProgressBar wifiProgressBar;

	private List<String> mWifiList;
	private WifiManager mWifiManager;

	private final static String ERRMSG = "Wifi test failed!";

	public WifiTestActivity() {

		this.mWifiList = new ArrayList<String>();

		this.mReceiver = new MyBroadcastReceiver();

		mHandler = new MyHandler();
	}

	//
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.wifitest);
		isWifiFirstTest = true;
		this.mResult = (TextView) findViewById(R.id.wifiresultText);
		this.mResult.setVisibility(View.VISIBLE);
		// this.mResult.setGravity(17);
		mInfoText = (TextView) findViewById(R.id.wifiInfoText);

		wifiProgressBar = (ProgressBar) findViewById(R.id.wifiprogress);
		wifiProgressBar.setVisibility(View.VISIBLE);
		ControlButtonUtil.initControlButtonView(this);

		this.mWifiManager = (WifiManager) getSystemService("wifi");
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
		/*
		Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onPause();
				stop = false;mReadyToTest = false;
				onResume();
			}
		});
		*/
	}

	protected void onResume() {
		super.onResume();
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		localIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		localIntentFilter
				.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		registerReceiver(mReceiver, localIntentFilter);
		this.mWifiList.clear();

		stop = false;
		Log.i(TAG, "try to enable wifi");
		mWifiManager.setWifiEnabled(true);
//		mHandler.sendEmptyMessage(MSG_SCAN);

		Log.i(TAG, "start test");
	}

	public void onPause() {
		stop = true;
		super.onPause();
		if (this.mWifiManager == null) {
			return;
		}

		Log.i(TAG, "end test");
		this.mHandler.removeMessages(MSG_SCAN);
		this.mHandler.removeMessages(MSG_ERROR);

		unregisterReceiver(mReceiver);
		mWifiManager.setWifiEnabled(false);
		mHandler.removeMessages(MSG_FAIL);
	}

	class MyHandler extends Handler {

		public void handleMessage(Message msg) {
			if (stop) {
				return;
			}
			switch (msg.what) {

			case MSG_SCAN:
				Log.i(TAG, "try to scan");
				removeMessages(MSG_SCAN);
				mWifiManager.startScan();
				break;

			case MSG_HTTP_TEST:
				removeMessages(MSG_HTTP_TEST);
				if (mReadyToTest) {
					Log.i(TAG, "do http test:" + HTTP_TEST_URL);
					mInfoText.setText(mInfoText.getText() + "\ntesting "
							+ HTTP_TEST_URL);
					new Thread(new Runnable() {

						public void run() {

							mHandler.sendEmptyMessage(httpClientTest() ? MSG_HTTP_TEST_PASS
									: MSG_HTTP_TEST_FAILED);
						}
					}).start();
				}
				break;

			case MSG_PING_TEST:
				removeMessages(MSG_PING_TEST);
				if (mReadyToTest) {
					Log.i(TAG, "do ping test:" + PING_TEST_ADDR);
					mInfoText.setText(mInfoText.getText() + "\ntesting "
							+ PING_TEST_ADDR);
					new Thread(new Runnable() {

						public void run() {

							mHandler.sendEmptyMessage(pingTest() ? MSG_PING_TEST_PASS
									: MSG_PING_TEST_FAILED);
						}
					}).start();
				}
				break;
			case MSG_PING_TEST_PASS: {
				mInfoText.setText(mInfoText.getText() + "\n" + getString(R.string.WifiPing)+getString(R.string.btnPassText));
				wifiProgressBar.setVisibility(View.GONE);
				 ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
				 //((Button)findViewById(R.id.btn_Pass)).setClickable(false);
				 ((Button)findViewById(R.id.btn_Fail)).setClickable(false);
				mHandler.sendEmptyMessageDelayed(MSG_FINISH_TEST, 2000);
				
				//findViewById(R.id.btn_Pass).performClick();
				break;
			}
			case MSG_PING_TEST_FAILED: {
				mInfoText.setText(mInfoText.getText() + "\n" + getString(R.string.WifiPing)+getString(R.string.btnFailText));
				wifiProgressBar.setVisibility(View.GONE);
				 ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
				 ((Button)findViewById(R.id.btn_Pass)).setClickable(false);
				 //((Button)findViewById(R.id.btn_Fail)).setClickable(false);
				mHandler.sendEmptyMessageDelayed(MSG_FAIL,
						TEST_FAILED_DELAY);
				break;
			}
			case MSG_HTTP_TEST_PASS: {
				mInfoText.setText(mInfoText.getText() + "\n" + getString(R.string.WifiHttp)+getString(R.string.btnPassText));
				wifiProgressBar.setVisibility(View.GONE);
				//findViewById(R.id.btn_Pass).performClick();
				break;
			}

			case MSG_HTTP_TEST_FAILED: {
				mInfoText.setText(getString(R.string.WifiHttp)+getString(R.string.btnFailText));
				wifiProgressBar.setVisibility(View.GONE);
				((Button)findViewById(R.id.btn_Retest)).setClickable(false);
				 ((Button)findViewById(R.id.btn_Pass)).setClickable(false);
				 //((Button)findViewById(R.id.btn_Fail)).setClickable(false);
				mHandler.sendEmptyMessageDelayed(MSG_FAIL,
						TEST_FAILED_DELAY);
				break;
			}

			case MSG_ERROR:

				removeMessages(MSG_ERROR);
				wifiProgressBar.setVisibility(View.GONE);
				mResult.setText(getString(R.string.WifiError));
				((Button)findViewById(R.id.btn_Retest)).setClickable(false);
				 ((Button)findViewById(R.id.btn_Pass)).setClickable(false);
				 //((Button)findViewById(R.id.btn_Fail)).setClickable(false);
				mHandler.sendEmptyMessageDelayed(MSG_FAIL,
						TEST_FAILED_DELAY);
				break;
			case MSG_FAIL:
				removeMessages(MSG_FAIL);
				if(isWifiFirstTest){
					isWifiFirstTest = false;
					findViewById(R.id.btn_Fail).performClick();
				}
				break;
			case MSG_FINISH_TEST:
				if(isWifiFirstTest){
					isWifiFirstTest = false;
					findViewById(R.id.btn_Pass).performClick();
				}
				break;
			}
		}
	}

	class MyBroadcastReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			if (stop) {
				return;
			}
			String action = intent.getAction();

			Log.i(TAG, "action:" + action);

			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
						WifiManager.WIFI_STATE_UNKNOWN);
				if (state == WifiManager.WIFI_STATE_ENABLED) {
					mHandler.sendEmptyMessage(MSG_SCAN);
				}
			}

			if (WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)) {
				boolean connected = intent.getBooleanExtra(
						WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
				if (connected && mReadyToTest) {
					Log.i(TAG, "already connect to:"
							+ mWifiManager.getConnectionInfo().getSSID());
					mInfoText.setText("connect to "
							+ mWifiManager.getConnectionInfo().getSSID());
//					mHandler.sendEmptyMessage(MSG_PING_TEST);	//change no pingtest
					mHandler.sendEmptyMessageDelayed(MSG_FINISH_TEST, 1000);
				}
			}

			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
				List<ScanResult> resultList = mWifiManager.getScanResults();
				Collections.sort(resultList, new Comparator<ScanResult>() {

					public int compare(ScanResult s1, ScanResult s2) {
						return s2.level - s1.level;
					}
				});

				if ((resultList != null) && (!resultList.isEmpty())) {

					String str3 = getString(R.string.wififindtest);
					StringBuilder sb = new StringBuilder().append(str3).append(
							"\n");
					ScanResult selectAp = null;
					for (ScanResult scanResult : resultList) {
						sb.append(
								scanResult.SSID
										+ "\t- "
										+ scanResult.capabilities
										+ "\t- level:"
										+ WifiManager.calculateSignalLevel(
												scanResult.level, 4)).append(
								"\n");
//						if (scanResult.capabilities.length() < 1) {
						if (!isNeedPwd(scanResult)) {
							if (null == selectAp
									|| selectAp.level < scanResult.level) {
								selectAp = scanResult;
							}
						}
					}
					mResult.setText(sb.toString());

					mReadyToTest = true;

					if (mWifiManager.getConnectionInfo().getIpAddress() != 0) {
						Log.i(TAG, "--already connect to:"
								+ mWifiManager.getConnectionInfo().getSSID());
						mInfoText.setText(getString(R.string.WifiConnect)
								+ mWifiManager.getConnectionInfo().getSSID());
//						mHandler.sendEmptyMessage(MSG_PING_TEST);	//change no pingtest
						mHandler.sendEmptyMessageDelayed(MSG_FINISH_TEST, 1000);
						return;
					}
					Log.i(TAG, "--selected ap:" + selectAp);
					if (null == selectAp) {
						mInfoText.setText(getString(R.string.WifiConnectErr));
						wifiProgressBar.setVisibility(View.GONE);
						mHandler.sendEmptyMessageDelayed(MSG_FAIL,
								 TEST_FAILED_DELAY);
						return;
					}

					int networkId = getNetworkId(selectAp.BSSID, selectAp.SSID);
					mInfoText.setText(getString(R.string.WifiTry) + selectAp.SSID);
					Log.i(TAG, "--try connect to ap:" + selectAp.SSID);
					boolean connectResult = mWifiManager.enableNetwork(networkId, true);
					if (connectResult) {
						mHandler.sendEmptyMessageDelayed(MSG_FINISH_TEST, 1000);
					} else {
						mHandler.sendEmptyMessageDelayed(MSG_FAIL, 1000);
					}

					//
					// wifiProgressBar.setVisibility(View.GONE);
					// isTestFinish = true;
					// mResult.setText("Find wifi network...\n Pass!");

				}
			}

		}
	}

	private int getNetworkId(String BSSID, String SSID) {
		for (WifiConfiguration wifiConfiguration : mWifiManager
				.getConfiguredNetworks()) {
			if (BSSID.equals(wifiConfiguration.BSSID)) {
				Log.i(TAG, "--get existed config:" + wifiConfiguration.SSID);
				return wifiConfiguration.networkId;
			}
		}

		WifiConfiguration wc = new WifiConfiguration();
		wc.BSSID = BSSID;
		wc.SSID = "\"" + SSID + "\"";
		wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		wc.status = WifiConfiguration.Status.ENABLED;
		wc.networkId = mWifiManager.addNetwork(wc);
		Log.i(TAG, "--new config:" + wc.SSID);
		return wc.networkId;
	}

	private boolean httpClientTest() {
		HttpClient client = new DefaultHttpClient();
		try {
			// TODO: Hardcoded for now, make it UI configurable
			HttpGet request = new HttpGet(HTTP_TEST_URL);
			HttpResponse response = client.execute(request);
			Log.i(TAG, "http result code:"
					+ response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() == 401) {
				request.abort();
				return true;
			} else {
				request.abort();
				return false;
			}
		} catch (IOException e) {
			Log.i(TAG, "http exception");
			e.printStackTrace();
			return false;
		} finally {
		}
	}

	private boolean pingTest() {
//		int status = SystemUtil.execShellCmdForStatue("ping -c 1 -w 5 "
//				+ PING_TEST_ADDR);
//		return (status == 0);
		boolean ret = false;
		URL serverURL;
		try {
			serverURL = new URL("http://www.baidu.com");
	        Log.d(TAG, " __________________-------- serverURL = " + serverURL.toString());
	        // connect to server
	        URLConnection uc2 = serverURL.openConnection();
	        HttpURLConnection conn = (HttpURLConnection) uc2;
	        Log.d(TAG, " __________________--------00 serverURL = " + serverURL.toString());
	        uc2.setAllowUserInteraction(true);
	        uc2.setConnectTimeout(55000);
	        uc2.setDoInput(true);
	        uc2.setDoOutput(true);
//	        conn.setConnectTimeout(1000);
	        conn.setReadTimeout(1000);
	        int numBytesRead = 0;
	        int allBytesRead = 0;
	        Log.d(TAG, " __________________--------11 conn.getContentLength() = " + conn.getContentLength());
	        InputStream in = conn.getInputStream();
	        byte[] buffer = new byte[4096];
	        do {
		        numBytesRead = in.read(buffer);
		        allBytesRead = allBytesRead + numBytesRead;
	        } while (numBytesRead > 0);
	        Log.d(TAG, " __________________-------- allBytesRead = " + allBytesRead + "   " + conn.getContentLength());
	        if(allBytesRead > 10)
	        	ret = true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
	
	private boolean isNeedPwd(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
        	return true;
        } else if (result.capabilities.contains("PSK")) {
        	return true;
        } else if (result.capabilities.contains("EAP")) {
        	return true;
        }
        return false;
    }
	
}
