package com.OOBDeviceTest.StressTest;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.OOBDeviceTest.R;
import com.OOBDeviceTest.helper.LogFileHelper;
import com.OOBDeviceTest.helper.SystemUtil;

public class SleepTest extends Activity  implements OnClickListener{
	private final String LOG_TAG = "SleepTestActivity";
	
	private final String AC_STATE_ACTION = "android.intent.action.AC_STATE";
	 private long mAwakeTime = 5000L;
	
	 private long mSleepTime = 40000L;
	
	private int mTestCount = 0;
	private int mLimitCount = 0;
	private boolean mIsRunning = false;
	private AlertDialog mDialog;
	private final static int MSG_SHOW_DIALOG = 0;
	
	private Button mStartBtn;
	private Button mStopBtn;
	private Button mExitBtn;
	private TextView mWakeTV;
	private Button mWakeBtn;
	private TextView mIntervalTV;
	private Button mIntervalBtn;
	private TextView mMaxTV;
	private Button mMaxBtn;
	
	private int mAutoTestFlag = 0;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sleep_test);
		initData();
		initRes();
		hideSettingView();
		
	    LogFileHelper.writeLogWithoutClose("["+SystemUtil.getSystemTime()+"]"+" :SLEEP TEST START.\n");
		registerReceiver(SleepTestReceiver, new IntentFilter(
				"com.rockchip.sleep.ACTION_TEST_CASE_SLEEP"));
		registerReceiver(ACDCDetectedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
	}
	
	protected void onResume() {
		super.onResume();
		if (mAutoTestFlag == 1) {
			mStartBtn.performClick();
		}
	}
	
	private void initRes() {
		mStartBtn = (Button) findViewById(R.id.start_btn);
		mStartBtn.setOnClickListener(this);
		mStartBtn.setVisibility(View.INVISIBLE);
		mStopBtn = (Button) findViewById(R.id.stop_btn);
		mStopBtn.setOnClickListener(this);
		mStopBtn.setVisibility(View.INVISIBLE);
		mExitBtn =(Button) findViewById(R.id.exit_btn);
		mExitBtn.setVisibility(View.INVISIBLE);
		mExitBtn.setOnClickListener(this);
		
		mWakeBtn = (Button) findViewById(R.id.waketime_btn);
		mWakeBtn.setOnClickListener(this);
		mIntervalBtn = (Button) findViewById(R.id.intervaltime_btn);
		mIntervalBtn.setOnClickListener(this);
		mMaxBtn = (Button) findViewById(R.id.max_count_btn);
		mMaxBtn.setOnClickListener(this);
		
		mWakeTV = (TextView) findViewById(R.id.waketime_tv);
		mIntervalTV = (TextView) findViewById(R.id.intervaltime_tv);
		mMaxTV = (TextView) findViewById(R.id.max_count_tv);
		updateView();
		
	}
	
	private void initData() {
		Intent intent = getIntent();
		if (intent != null) {
			mLimitCount = intent.getIntExtra("max", 0);
			mAutoTestFlag = intent.getIntExtra("auto", 0);
		}
	}
	
	private void updateView() {
		mWakeTV.setText(getString(R.string.wake_string)+mAwakeTime/1000);
		mIntervalTV.setText(getString(R.string.interval_string)+ mSleepTime/1000);
		mMaxTV.setText(getString(R.string.maxcount_string) + mLimitCount + "  "
				+ getString(R.string.nowcount_string) + mTestCount);
	}
	
	private void startTest(Context context) {
		stopAlarm(context);
		mIsRunning = true;
		try {
			Settings.System.putInt(context.getContentResolver(),
					"screen_off_timeout", 15000);
			setAlarm(context, mSleepTime, true);
			return;
		} catch (NumberFormatException localNumberFormatException) {
			while (true)
				Log.e(LOG_TAG, "could not persist screen timeout setting");
		}
	}
	
	private void stopTest(Context context) {
	    stopAlarm(context);
	    mIsRunning = false;
	}
	

	private void setAlarm(Context paramContext, long paramLong, boolean repeat) {
		AlarmManager localAlarmManager = (AlarmManager) paramContext
				.getSystemService("alarm");
		PendingIntent localPendingIntent = PendingIntent.getBroadcast(
				paramContext, 0, new Intent(
						"com.rockchip.sleep.ACTION_TEST_CASE_SLEEP"), 0);
		localAlarmManager.set(AlarmManager.RTC_WAKEUP, paramLong + System.currentTimeMillis(),
				localPendingIntent);
		if (repeat)
			localAlarmManager.setRepeating(0,
					paramLong + System.currentTimeMillis(), paramLong,
					localPendingIntent);
	}

	private void stopAlarm(Context paramContext) {
		PendingIntent localPendingIntent = PendingIntent.getBroadcast(
				paramContext, 0, new Intent(
						"com.rockchip.sleep.ACTION_TEST_CASE_SLEEP"), 0);
		((AlarmManager) paramContext.getSystemService("alarm"))
				.cancel(localPendingIntent);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
//		((KeyguardManager)getSystemService("keyguard")).newKeyguardLock("TestCaseSleep").reenableKeyguard();
	}
	
	protected void onDestroy() {
		super.onDestroy();
		stopTest(this);
		unregisterReceiver(SleepTestReceiver);
		unregisterReceiver(ACDCDetectedReceiver);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_btn:
			startTest(this);
			break;
		case R.id.stop_btn:
			stopTest(this);
			break;
		case R.id.waketime_btn:
			onSetClick(R.id.waketime_btn);
			break;
		case R.id.intervaltime_btn:
			onSetClick(R.id.intervaltime_btn);
			break;
		case R.id.max_count_btn:
			onSetClick(R.id.max_count_btn);
			break;
		case R.id.exit_btn:
			finish();
			break;
		default:
			break;
		}
	};
	
	private void onSetClick(final int id) {
		final EditText editText = new EditText(this);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
		new AlertDialog.Builder(this)
			.setTitle(R.string.dialog_title)
			.setView(editText)
			.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(!editText.getText().toString().trim().equals("")) {
						if (id == R.id.waketime_btn) {
							mAwakeTime = Integer.valueOf(editText.getText().toString()) * 1000L;
							updateView();
						} else if (id == R.id.intervaltime_btn) {
							mSleepTime = Integer.valueOf(editText.getText().toString()) * 1000L;
							updateView();
						} else if (id == R.id.max_count_btn) {
							mLimitCount = Integer.valueOf(editText.getText().toString());
							updateView();
						}
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
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_SHOW_DIALOG:
				ShowACDialog();
				break;

			default:
				break;
			}
		};
	};
	
	
	private void ShowACDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.AC_NO_IN_MSG)
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								
							}
						})
				
				;
		mDialog = builder.create();
		mDialog.show();
		
	}
	
	
	private BroadcastReceiver SleepTestReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			 mTestCount = mTestCount+1;
			 updateView();
			 if (mLimitCount != 0 && mTestCount >= mLimitCount) {
				 mIsRunning = false;
				 stopTest(context);
			     LogFileHelper.writeLogWithoutClose("["+SystemUtil.getSystemTime()+"]"+" :SLEEP TEST FINISH.\n");
				 ((PowerManager)context.getSystemService("power")).newWakeLock(268435482, "ScreenOnTimer").acquire(mAwakeTime);
				 ((KeyguardManager)context.getSystemService("keyguard")).newKeyguardLock("TestCaseSleep").disableKeyguard();
				 if (mAutoTestFlag == 1) {
					 finish();
				 }
					 
			 } else {
			     LogFileHelper.writeLogWithoutClose("["+SystemUtil.getSystemTime()+"]"+" :SLEEP TIME:"+mTestCount+".\n");
				 ((PowerManager)context.getSystemService("power")).newWakeLock(268435482, "ScreenOnTimer").acquire(mAwakeTime);
				 ((KeyguardManager)context.getSystemService("keyguard")).newKeyguardLock("TestCaseSleep").disableKeyguard();
			 }
		}
		
	};
	
	private void hideSettingView() {
		((View) findViewById(R.id.waketime_btn)).setVisibility(View.GONE);
		((View) findViewById(R.id.intervaltime_btn)).setVisibility(View.GONE);
		((View) findViewById(R.id.max_count_btn)).setVisibility(View.GONE);
	}
	
	private BroadcastReceiver ACDCDetectedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				int plugType = intent.getIntExtra("plugged", 0);
				if (plugType > 0) {
					if (mDialog != null && mDialog.isShowing()) {
						mDialog.dismiss();
					}
				} else {
					// AC NOT IN 
					//stopTest(context); //LIMI 8.15
					//finish();
					mHandler.sendEmptyMessage(MSG_SHOW_DIALOG);	
				}
			}
		}
	};
	
}

