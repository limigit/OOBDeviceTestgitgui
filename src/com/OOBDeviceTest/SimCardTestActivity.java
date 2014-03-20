package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.File;
import java.math.BigInteger;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import android.telephony.TelephonyManager;
import android.util.Log;

public class SimCardTestActivity extends Activity {
	private static final String TAG = "SdCardTestActivity";

	TextView mResult;

	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);


		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.simcardtest);

		this.mResult = (TextView) findViewById(R.id.text);
		this.mResult.setVisibility(View.VISIBLE);
		this.mResult.setGravity(17);

		ControlButtonUtil.initControlButtonView(this);
	}

	
	protected void onResume() {
		super.onResume();
		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = mTelephonyMgr.getSubscriberId();

		if (imsi == null) {
			mResult.setText("Cann't get IMSI!");
		} else {
			mResult.setText("IMSI:" + imsi);
		}
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
