package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.util.ArrayList;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.TestCase.RESULT;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BluetoothTestActivity extends Activity {
	private static final String TAG = BluetoothTestActivity.class
			.getSimpleName();
	private final static String ERRMSG = "FAIL!Can not find a bluetooth equipment!";

	private static final int MSG_OPEN = 0;
	private static final int MSG_FAILED = 1;

//	private boolean isTestFinish = false;
//	private boolean isUnRegOver = false;
	private BluetoothAdapter mAdapter;
	private BroadcastReceiver mBluetoothReceiver;

//	private ArrayList<String> mDeviceNames;
	private Handler mHandler;
	private TextView mResult;
	private int mTestCount;
	private int mTestOpen;
	private ProgressBar progressBar;
	boolean stop = false;
	private static boolean isBTFirstTest=true;

	public BluetoothTestActivity() {
//		this.mDeviceNames = new ArrayList<String>();
		this.mHandler = new MyHandler();
		this.mBluetoothReceiver = new MyBroadcastReceiver();

	}

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.bluetoothtest);
		isBTFirstTest=true;
		mResult = (TextView) findViewById(R.id.resultText);

		progressBar = (ProgressBar) findViewById(R.id.progress);

		ControlButtonUtil.initControlButtonView(this);
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);

		this.mAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mAdapter == null) {
//			isTestFinish = true;
			this.mResult.setText(getString(R.string.BluetoothAdapterFail));
			progressBar.setVisibility(View.GONE);
			failed();
		}

	}

	
	protected void onResume() {

		super.onResume();
		stop = false;
//		if ((this.mAdapter != null) && (!this.isTestFinish)) {

//			this.isUnRegOver = false;

			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			intentFilter
					.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
			intentFilter
					.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

			registerReceiver(this.mBluetoothReceiver, intentFilter);

//			this.mDeviceNames.clear();

			if (this.mAdapter.isEnabled()) {
				mAdapter.startDiscovery();
				mResult.setText(getString(R.string.BluetoothScan));
			} else {
				this.mHandler.sendEmptyMessage(MSG_OPEN);
				mResult.setText(getString(R.string.BluetoothInit));
			}
//		}
	}

	
	public void onPause() {
		super.onPause();
		stop = true;
//		if ((this.mAdapter == null) || (this.isTestFinish)) {
//			return;
//		}
//		this.isUnRegOver = true;
		if (this.mAdapter == null) {
			return;
		}
		unregisterReceiver(mBluetoothReceiver);
		mAdapter.cancelDiscovery();
		// this.mAdapter.disable();
		mHandler.removeMessages(MSG_FAILED);
	}
	
	

	class MyHandler extends Handler {

		public void handleMessage(Message msg) {
			if(stop) {
				return;
			}
			switch (msg.what) {
			case MSG_OPEN:

//				if ((isTestFinish) && (!isUnRegOver)) {
//					isUnRegOver = true;
//					removeMessages(MSG_OPEN);
//
//					unregisterReceiver(mBluetoothReceiver);
//					// mAdapter.disable();
//
//					break;
//				}
				Log.i(TAG, "142,mAdapter.isEnabled()" + mAdapter.isEnabled());
				if (mAdapter.isEnabled()) {

					Log.i(TAG, "145,bluetooth is open and then startDiscovery!");
					mAdapter.startDiscovery();

					removeMessages(MSG_OPEN);

				} else {

					if (mTestOpen < 15) {

						Log.i(TAG,
								"bluetooth is close and then open it! mTestOpen="
										+ mTestOpen);

						mTestOpen++;

						mAdapter.enable();
						sendEmptyMessageDelayed(MSG_OPEN, 1000L);

					} else {

//						isTestFinish = true;
						progressBar.setVisibility(View.GONE);
						mResult.setText(getString(R.string.BluetoothOpenF));
						failed();
					}

				}

				break;
			case MSG_FAILED:
				if(isBTFirstTest){
					isBTFirstTest=false;
					((Button)findViewById(R.id.btn_Retest)).setClickable(false);
					((Button)findViewById(R.id.btn_Pass)).setClickable(false);
					//((Button)findViewById(R.id.btn_Fail)).setClickable(false);
					//limi 9.7 add
					((Button)findViewById(R.id.btn_Fail)).performClick();
				}
				break;
			}
		}
	}

	private void failed() {
		mHandler.removeMessages(MSG_FAILED);
		mHandler.sendEmptyMessageAtTime(MSG_FAILED, DeviceTest.TEST_FAILED_DELAY);
	}
	
	class MyBroadcastReceiver extends BroadcastReceiver {

		
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(stop) {
				return;
			}
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(
						BluetoothAdapter.EXTRA_STATE,
						Integer.MIN_VALUE);
				if (state == BluetoothAdapter.STATE_ON) {
					Log.i(TAG, "onReceive -- STATE_ON");
					mAdapter.startDiscovery();
				} else if (state == BluetoothAdapter.STATE_OFF) {
					Log.i(TAG, "onReceive -- STATE_OFF");

				}

			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				Log.i(TAG, "onReceive -- ACTION_DISCOVERY_STARTED mTestCount="
						+ mTestCount);

                mResult.setText(getString(R.string.BluetoothScan));

			} else if (action
					.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				Log.i(TAG, "onReceive -- ACTION_DISCOVERY_FINISHED");

//				if (!mDeviceNames.isEmpty()) {
					/*
					 * progressBar.setVisibility(View.GONE); String findstr =
					 * "Find equipment-"; StringBuilder sb = new
					 * StringBuilder().append(findstr) .append("\n");
					 * 
					 * for (int i = 0; i < mDeviceNames.size(); i++) {
					 * 
					 * sb.append(" <"); String name = (String)
					 * mDeviceNames.get(i);
					 * sb.append(name).append(">").append("\n"); }
					 * 
					 * mResult.setText(sb.toString());
					 */
//				} else {

//					if ((isTestFinish) && (!isUnRegOver)) {
//						isUnRegOver = true;
//						mHandler.removeMessages(0);
//						unregisterReceiver(mBluetoothReceiver);
//						progressBar.setVisibility(View.GONE);
//						mResult.setText(ERRMSG);
//					}

					if (mTestCount < 15) {
						mTestCount++;
						mAdapter.startDiscovery();

					} else {

//						isTestFinish = true;
						progressBar.setVisibility(View.GONE);
						mResult.setText(getString(R.string.BluetoothFindF));

						failed();
					}

//				}

			} else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				// String name =
				// intent.getStringExtra("android.bluetooth.device.extra.NAME");
				if (device != null) {
					progressBar.setVisibility(View.GONE);
					mResult.setText(getString(R.string.BluetoothFindS));
					// mDeviceNames.add(device.getName() + "-" +
					// device.getAddress());
					
					if(isBTFirstTest){
						isBTFirstTest=false;
						((Button)findViewById(R.id.btn_Retest)).setClickable(false);
						//((Button)findViewById(R.id.btn_Pass)).setClickable(false);
						((Button)findViewById(R.id.btn_Fail)).setClickable(false);//limi 9.7 add
						((Button)findViewById(R.id.btn_Pass)).performClick();
					}
				}

				// String address = localBluetoothDevice.getAddress();
				// mDeviceNames.add(address);

			}

		}

	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
