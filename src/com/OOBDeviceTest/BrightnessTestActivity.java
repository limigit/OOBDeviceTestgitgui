package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.*;

import java.util.Random;
import java.util.logging.Logger;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BrightnessTestActivity extends Activity implements OnClickListener{
	private static final String TAG = BrightnessTestActivity.class
			.getSimpleName();
	private static final int MAXIMUM_BRIGHTNESS = 255;
	private static final int MINIMUM_BRIGHTNESS = 0;
	private static final int ONE_STAGE = 5;
	
	private static final int MSG_TEST_BRIGHTNESS = 0;
	private static final int MSG_SHOW_BTN = 1;
	
	private MyHandler mHandler;
	private TextView mText;
	private TextView mTitle;
	private TextView progressText;
	private TextView mContentView;
	
	int mBrightness = 30;
	boolean increase = true;
	
	private Random mRandom;
	private int mRandomPassNum = -1;
	private int mPressBtnNum = 0;
	private View mNumberPadView;
	
	private static boolean isBrightnessFirstTest= true;

	public BrightnessTestActivity() {
		mHandler = new MyHandler();

	}

	ProgressBar progressBar;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.brightnesstest);

//		setTitle(getTitle() + "----("
//				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
//				+ ")");
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		isBrightnessFirstTest= true;

		progressBar = (ProgressBar) findViewById(R.id.brightnessBar);
		progressBar.setClickable(false);
		progressBar.setMax(MAXIMUM_BRIGHTNESS);
		progressText = (TextView) findViewById(R.id.progressText);
		
		mRandomPassNum = getRandomNum();
		mContentView = (TextView) findViewById(R.id.backLightMessage);
		mContentView.setText(getString(R.string.BrightnessPassCode)+""+mRandomPassNum);
		
		mNumberPadView = (View) findViewById(R.id.numberpad);
		mNumberPadView.setVisibility(View.INVISIBLE);
		initNumberPad();
		ControlButtonUtil.initControlButtonView(this);
		hideButton();
		
		
	}

	protected void onResume() {
		super.onResume();
		this.mHandler.sendEmptyMessage(MSG_TEST_BRIGHTNESS);
		mHandler.sendEmptyMessageDelayed(MSG_SHOW_BTN, 3000);
	}

	protected void onPause() {
		super.onPause();
		Log.d(TAG, " _____________________- onPause()");
		this.mHandler.removeMessages(MSG_TEST_BRIGHTNESS);
	}
	@Override
	protected void onDestroy() {
		Log.d(TAG, " _____________________- onDestroy()");
		super.onDestroy();
	}
	private void setBrightness(int paramInt) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		float brightness = (float) paramInt / MAXIMUM_BRIGHTNESS;
		Log.e("cghs","lp:"+lp.screenBrightness + " br:"+brightness);
		lp.screenBrightness = brightness;
		if (brightness == 0.0) return;
		getWindow().setAttributes(lp);
	}

	class MyHandler extends Handler {
		MyHandler() {
		}

		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			int delay = 25;
			switch (msg.what) {
			case MSG_TEST_BRIGHTNESS: {
				if (increase) {
					mBrightness += ONE_STAGE;
					if (mBrightness >= MAXIMUM_BRIGHTNESS) {
						mBrightness = MAXIMUM_BRIGHTNESS;
						increase = false;
						delay = 200;
					}
				} else {
					mBrightness -= ONE_STAGE;
					if (mBrightness <= MINIMUM_BRIGHTNESS) {
						mBrightness = MINIMUM_BRIGHTNESS;
						increase = true;
						delay = 200;
					}
				}
				progressBar.setProgress(mBrightness);
				progressText.setText(mBrightness + "/255");
				setBrightness(mBrightness);

				sendEmptyMessageDelayed(MSG_TEST_BRIGHTNESS, delay);

				break;
			}
			case MSG_SHOW_BTN:
				showBtnView();
				break;
			default:
				break;
			}
		}

	}
	
	private void showBtnView() {
		mContentView.setText(getString(R.string.BrightnessMessage));
		mContentView.setVisibility(View.VISIBLE);
		showButton();
		mNumberPadView.setVisibility(View.VISIBLE);
	}
	
	private void initNumberPad() {
		((Button) findViewById(R.id.one)).setOnClickListener(this);
		((Button) findViewById(R.id.two)).setOnClickListener(this);
		((Button) findViewById(R.id.three)).setOnClickListener(this);
		((Button) findViewById(R.id.four)).setOnClickListener(this);
		((Button) findViewById(R.id.five)).setOnClickListener(this);
		((Button) findViewById(R.id.six)).setOnClickListener(this);
		((Button) findViewById(R.id.seven)).setOnClickListener(this);
		((Button) findViewById(R.id.eight)).setOnClickListener(this);
		((Button) findViewById(R.id.nine)).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.one:
			mPressBtnNum = 1;
			compareBtnAndRandom();
			break;
		case R.id.two:
			mPressBtnNum = 2;
			compareBtnAndRandom();
			break;
		case R.id.three:
			mPressBtnNum = 3;
			compareBtnAndRandom();
			break;
		case R.id.four:
			mPressBtnNum = 4;
			compareBtnAndRandom();
			break;
		case R.id.five:
			mPressBtnNum = 5;
			compareBtnAndRandom();
			break;
		case R.id.six:
			mPressBtnNum = 6;
			compareBtnAndRandom();
			break;
		case R.id.seven:
			mPressBtnNum = 7;
			compareBtnAndRandom();
			break;
		case R.id.eight:
			mPressBtnNum = 8;
			compareBtnAndRandom();
			break;
		case R.id.nine:
			mPressBtnNum = 9;
			compareBtnAndRandom();
			break;
		default:
			break;
		}
		mNumberPadView.setVisibility(View.INVISIBLE);
	}
	
	private void compareBtnAndRandom() {
		if (mRandomPassNum == mPressBtnNum) {
			//pass
			if(isBrightnessFirstTest){
				isBrightnessFirstTest= false;
				((Button)findViewById(R.id.btn_Retest)).setClickable(false);
				//((Button)findViewById(R.id.btn_Pass)).setClickable(false);
				((Button)findViewById(R.id.btn_Fail)).setClickable(false);//limi 9.7 add
				((Button) findViewById(R.id.btn_Pass)).performClick();
			}
		} else {
			if(isBrightnessFirstTest){
				isBrightnessFirstTest= false;
				//fail
				//((Button)findViewById(R.id.btn_Retest)).setClickable(false);
				((Button)findViewById(R.id.btn_Pass)).setClickable(false);
				((Button)findViewById(R.id.btn_Fail)).setClickable(false);//limi 9.7 add
				((Button) findViewById(R.id.btn_Retest)).performClick();
			}
		
		}
	}
	
	private int getRandomNum() {
		if (mRandom == null)
			mRandom = new Random(System.currentTimeMillis());
		int num = Math.abs(mRandom.nextInt()) % 9 + 1;
		return num;
	}
	
	private void hideButton() {
		((Button) findViewById(R.id.btn_Pass)).setVisibility(View.INVISIBLE);
		((Button) findViewById(R.id.btn_Fail)).setVisibility(View.INVISIBLE);
		((Button) findViewById(R.id.btn_Retest)).setVisibility(View.INVISIBLE);
	}
	
	private void showButton() {
		((Button) findViewById(R.id.btn_Fail)).setVisibility(View.VISIBLE);
		((Button) findViewById(R.id.btn_Retest)).setVisibility(View.VISIBLE);
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

}
