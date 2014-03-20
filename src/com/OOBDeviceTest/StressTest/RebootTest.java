package com.OOBDeviceTest.StressTest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.OOBDeviceTest.R;
import com.OOBDeviceTest.helper.LogFileHelper;
import com.OOBDeviceTest.helper.NativeManger;
import com.OOBDeviceTest.helper.SystemUtil;

/*
 * Author	: cghs
 * Date  	: 2013-05-06
 * Function	: Reboot Test
 */

public class RebootTest extends Activity implements OnClickListener {
	private final static String LOG_TAG = "RebootTest";

	private final static int MSG_REBOOT = 0;
	private final static int MSG_REBOOT_COUNTDOWN = 1;
	private final static int MSG_REBOOT_STARTCOUNT = 2;
	private final int DELAY_TIME = 5000;// ms
	private final int REBOOT_OFF = 0;
	private final int REBOOT_ON = 1;
	private String  mSN=null;
	private SharedPreferences mSharedPreferences;

	private TextView mCountTV;
	private TextView mCountdownTV;
	private TextView mMaxTV;
	private Button mStartButton;
	private Button mStopButton;
	private Button mExitButton;
	private Button mSettingButton;
	private Button mClearButton;

	private int mState;
	private int mCount;
	private int mCountDownTime;
	private int mMaxTimes; // max times to reboot
	private int mAutoTestFlag = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reboot_test);
		mSharedPreferences = getSharedPreferences("state", 0);
		
		NativeManger nativeManger = new NativeManger();
		mSN=nativeManger.getSN();
		if (mSN != null && !mSN.equals("")) {
			LogFileHelper.LogFile = LogFileHelper.DEFAULT_LOG_FILE_PATH+"/"+mSN+".log";					
		}	
		//init data from intent
		initData();
		
		// get the reboot flag and count.
		mState = mSharedPreferences.getInt("reboot_flag", 0);
		mCount = mSharedPreferences.getInt("reboot_count", 0);
		if (mMaxTimes == 0)
			mMaxTimes = mSharedPreferences.getInt("reboot_max", 0);
		if (mAutoTestFlag == 0)
			mAutoTestFlag = mSharedPreferences.getInt("auto", 0);

		   SharedPreferences mSharedPreferences = this.getSharedPreferences("state", 0);
			SharedPreferences.Editor edit = mSharedPreferences.edit();
			edit.putInt("reboot_flag2", 1);
			edit.commit();
			
		// init resource
		initRes();
		
		//hide setting view
		hideSettingView();
		
		
		LogFileHelper.writeLogWithoutClose("["+SystemUtil.getSystemTime()+"]"+" :Reboot TEST START.\n");
		if (mState == REBOOT_ON) {
			if (mMaxTimes != 0 && mMaxTimes <= mCount) {
				mState = REBOOT_OFF;
				saveSharedPreferences(mState, 0);
				saveMaxTimes(0);
				updateBtnState();
				LogFileHelper.writeLogWithoutClose("["+SystemUtil.getSystemTime()+"]"+" :Reboot TEST FINISH.\n");
				mCountTV.setText(mCountTV.getText()+" TEST FINISH!");
				
			} else {
				mCountDownTime = DELAY_TIME / 1000;
				mHandler.sendEmptyMessage(MSG_REBOOT_STARTCOUNT);
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mAutoTestFlag == 1 && mState != REBOOT_ON) {
			mState = REBOOT_ON;
			mHandler.sendEmptyMessageDelayed(MSG_REBOOT_STARTCOUNT, 2000);
		}
	}

	private void initRes() {
		mCountTV = (TextView) findViewById(R.id.count_tv);
		mCountTV.setText(getString(R.string.reboot_time) + mCount);
		mMaxTV = (TextView) findViewById(R.id.maxtime_tv);
		if (mMaxTimes == 0) {
			mMaxTV.setText(getString(R.string.reboot_maxtime)
					+ getString(R.string.not_setting));
		} else {
			mMaxTV.setText(getString(R.string.reboot_maxtime) + mMaxTimes);
		}

		mStartButton = (Button) findViewById(R.id.start_btn);
		mStartButton.setVisibility(View.INVISIBLE);
		mStartButton.setOnClickListener(this);

		mStopButton = (Button) findViewById(R.id.stop_btn);
		mStopButton.setVisibility(View.INVISIBLE);
		mStopButton.setOnClickListener(this);
		
		mExitButton = (Button) findViewById(R.id.exit_btn);
		mExitButton.setVisibility(View.INVISIBLE);
		mExitButton.setOnClickListener(this);
		
		
		mSettingButton = (Button) findViewById(R.id.setting_btn);
		mSettingButton.setOnClickListener(this);

		mClearButton = (Button) findViewById(R.id.clear_btn);
		mClearButton.setOnClickListener(this);

		updateBtnState();

		mCountdownTV = (TextView) findViewById(R.id.countdown_tv);
	}
	
	private void initData() {
		Intent intent = getIntent();
		mAutoTestFlag = intent.getIntExtra("auto", 0);
		mMaxTimes = intent.getIntExtra("max", 0);
		
		SharedPreferences.Editor edit = mSharedPreferences.edit();
		if(mAutoTestFlag != 0)
			edit.putInt("auto", mAutoTestFlag);
		if (mMaxTimes != 0)
			edit.putInt("reboot_max", mMaxTimes);
		edit.commit();
	}

	private void reboot() {
		// save state
		saveSharedPreferences(mState, mCount + 1);
		LogFileHelper.writeLogWithoutClose("["+SystemUtil.getSystemTime()+"]"+" :Reboot TIME "+(mCount+1)+".\n");
		LogFileHelper.writeLogClose();
		// 重启
		/*
		 * String str = "重启"; try { str = runCmd("reboot", "/system/bin"); }
		 * catch (IOException e) { e.printStackTrace(); }
		 */
		/*
		 * Intent reboot = new Intent(Intent.ACTION_REBOOT);
		 * reboot.putExtra("nowait", 1); reboot.putExtra("interval", 1);
		 * reboot.putExtra("window", 0); sendBroadcast(reboot);
		 */
		PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		pManager.reboot("重启");
		System.out.println("execute cmd--> reboot\n" + "重启");
	}

	private void saveSharedPreferences(int flag, int count) {
		SharedPreferences.Editor edit = mSharedPreferences.edit();
		edit.putInt("reboot_flag", flag);
		edit.putInt("reboot_count", count);
		edit.commit();
	}
	
	private void saveMaxTimes(int max) {
		SharedPreferences.Editor edit = mSharedPreferences.edit();
		edit.putInt("reboot_max", max);
		edit.commit();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REBOOT:
				if (mState == 1)
					reboot();
				break;

			case MSG_REBOOT_COUNTDOWN:
				if (mState == 0)
					return;
				if (mCountDownTime != 0) {
					mCountdownTV.setText(getString(R.string.reboot_countdown)
							+ mCountDownTime);
					mCountdownTV.setVisibility(View.VISIBLE);
					mCountDownTime--;
					sendEmptyMessageDelayed(MSG_REBOOT_COUNTDOWN, 1000);
				} else {
					mCountdownTV.setText(getString(R.string.reboot_countdown)
							+ mCountDownTime);
					mCountdownTV.setVisibility(View.VISIBLE);
					sendEmptyMessage(MSG_REBOOT);
				}

				break;
			case MSG_REBOOT_STARTCOUNT:
				sendEmptyMessage(MSG_REBOOT_COUNTDOWN);
				break;

			default:
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_btn:
			onStartClick();
			break;
		case R.id.stop_btn:
			onStopClick();
			break;
		case R.id.setting_btn:
			onSettingClick();
			break;
		case R.id.clear_btn:
			onClearSetting();
			break;
		case R.id.exit_btn:
			finish();
			break;
		default:
			break;
		}

	}

	private void onStartClick() {
		new AlertDialog.Builder(RebootTest.this)
				.setTitle(R.string.reboot_dialog_title)
				.setMessage(R.string.reboot_dialog_msg)
				.setPositiveButton(R.string.dialog_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mState = REBOOT_ON;
								mCountDownTime = DELAY_TIME / 1000; // ms->s
								updateBtnState();
								mHandler.sendEmptyMessage(MSG_REBOOT_STARTCOUNT);
							}
						})
				.setNegativeButton(R.string.dialog_cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						}).show();
	}

	private void onStopClick() {
		mHandler.removeMessages(MSG_REBOOT);
		mCountdownTV.setVisibility(View.INVISIBLE);
		mState = REBOOT_OFF;
		updateBtnState();
		saveSharedPreferences(mState, 0);

	}
	
	private void onSettingClick() {
		final EditText editText = new EditText(this);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
		new AlertDialog.Builder(this)
			.setTitle(R.string.btn_setting)
			.setView(editText)
			.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(!editText.getText().toString().trim().equals("")) {
						mMaxTimes = Integer.valueOf(editText.getText().toString());
						saveMaxTimes(mMaxTimes);
						mMaxTV.setText(getString(R.string.reboot_maxtime)+mMaxTimes);
					}
				}
			})
			.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
				
			}).show();
		}

	private void onClearSetting() {
		mMaxTimes = 0;
		saveMaxTimes(mMaxTimes);
		mMaxTV.setText(getString(R.string.reboot_maxtime)
				+ getString(R.string.not_setting));
	}
	
	private void updateBtnState() {
		mStartButton.setEnabled(mState == REBOOT_OFF);
		mClearButton.setEnabled(mState == REBOOT_OFF);
		mSettingButton.setEnabled(mState == REBOOT_OFF);
		mStopButton.setEnabled(mState == REBOOT_ON);
	}
	
	private void hideSettingView() {
		mSettingButton.setVisibility(View.GONE);
		mClearButton.setVisibility(View.GONE);
	}

}
