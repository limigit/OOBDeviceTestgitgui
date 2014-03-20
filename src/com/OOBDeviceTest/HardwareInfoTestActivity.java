package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.SystemUtil;
import com.OOBDeviceTest.helper.TestCase.RESULT;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HardwareInfoTestActivity extends Activity implements
		OnClickListener {
	private TextView currentView = null;
	private TextView cpuInfoTextView = null;
	private TextView memInfoTextView = null;
	private TextView dmiInfoTextView = null;
	boolean stop = false;

	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.hardwareinfotest);
		Button cpuInfoButton = (Button) findViewById(R.id.btn_cpu);
		Button memInfoButton = (Button) findViewById(R.id.btn_mem);

		cpuInfoTextView = (TextView) findViewById(R.id.cpu_info);
		memInfoTextView = (TextView) findViewById(R.id.mem_info);

		String cpuInfo = SystemUtil.execShellCmd("cat /proc/cpuinfo");

		cpuInfoTextView.setText("CPU INFO\n" + cpuInfo);
		memInfoTextView.setText("MEM INFO\n"
				+ SystemUtil.execShellCmd("cat /proc/meminfo"));

		cpuInfoButton.setTag(cpuInfoTextView);
		memInfoButton.setTag(memInfoTextView);

		cpuInfoButton.setOnClickListener(this);
		memInfoButton.setOnClickListener(this);

		currentView = cpuInfoTextView;
		cpuInfoTextView.setVisibility(View.VISIBLE);
		ControlButtonUtil.initControlButtonView(this);
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);

		boolean pass = true;
		String infoText = "";
		if (!cpuInfo.contains("Processor\t: ARMv7 Processor rev 2 (v7l)")) {
			infoText = "cpu - Processor;";
			pass = false;
		}
		if (!cpuInfo.contains("CPU architecture: 7")) {
			infoText += "cpu - CPU architecture;";
			pass = false;
		}
		if (!cpuInfo.contains("CPU revision\t: 2")) {
			infoText += "cpu - CPU revision;";
			pass = false;
		}

		if (!cpuInfo.contains("Hardware\t: RK29board")) {
			infoText += "cpu - Hardware;";
			pass = false;
		}

		if (pass) {
			findViewById(R.id.btn_Pass).performClick();
		} else {

			TextView infoView = (TextView) findViewById(R.id.hwinfoText);
			infoView.setText("Failed:\n" + infoText);
			mHandler.postDelayed(mFailedRunnable, DeviceTest.TEST_FAILED_DELAY);
		}
	}

	Handler mHandler = new Handler();
	Runnable mFailedRunnable = new Runnable() {

		
		public void run() {
			if(stop) {
				return;
			}
			mHandler.removeCallbacks(mFailedRunnable);
			findViewById(R.id.btn_Fail).performClick();
		}
	};

	
	public void onStop() {
		super.onStop();
		stop = true;
		mHandler.removeCallbacks(mFailedRunnable);
	}

	// if (device != null && progressBar.isShown()) {
	// progressBar.setVisibility(View.GONE);
	// mResult.setText("Find equipment....\n Pass!");

	
	public void onClick(View v) {
		TextView infoView = (TextView) v.getTag();
		if (currentView != null && currentView != infoView) {
			currentView.setVisibility(View.GONE);
		}
		infoView.setVisibility(View.VISIBLE);
		currentView = infoView;
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

}
