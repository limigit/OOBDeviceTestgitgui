//  unused !








//package com.DeviceTest;
//
//import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
//import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Date;
//
//import android.R.integer;
//import android.app.Activity;
//import android.app.ActivityManager;
//import android.app.AlarmManager;
//import android.app.KeyguardManager;
//import android.app.PendingIntent;
//import android.app.KeyguardManager.KeyguardLock;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.pm.ActivityInfo;
//import android.content.res.Configuration;
//import android.database.DataSetObserver;
//import android.media.MediaPlayer;
//import android.media.MediaPlayer.OnCompletionListener;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.os.PowerManager;
//import android.os.PowerManager.WakeLock;
//import android.os.SystemClock;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.Surface;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemSelectedListener;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.MediaController;
//import android.widget.ProgressBar;
//import android.widget.Spinner;
//import android.widget.SpinnerAdapter;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.VideoView;
//
//import com.DeviceTest.helper.ControlButtonUtil;
//import com.DeviceTest.helper.SystemUtil;
//import com.DeviceTest.helper.TestCase.RESULT;
//
//public class RuninTestActivity extends Activity {
//
//	private static final String BATTERY_TEMP_PATH = "/sys/class/power_supply/*battery/temp";
//	private static final String VIDEO_PATH = DeviceTest.EXTRA_PATH
//			+ "Earth.mp4";
//
//	private static final int MSG_DO_TEST = 0;
//	private static final int MSG_TEST_FAILED = 1;
//	private static final int MSG_TEST_PASS = 2;
//	private static final int MSG_STOP = 3;
//	private static final int MSG_SLEEP_WAKE = 5;
//
//	private static final int SLEEP_WAKE_COUNT = 10;
//	private static final long VIDEO_TEST_TIME = 0;
//	private static final int MEM_TEST_SIZE = 200;
//
//	private static final int SLEEP_WAKE_DIST = 5 * 1000;
//
//	private String sleepWakeCountString;
//	private String videoTimeString;
//	private String memSizeString;
//
//	int sleepWakeCount = 0;
//	long videoTime = 0;
//
//	VideoView videoView;
//	long videoStart = 0;
//
//	private enum TEST_STEP {
//		SLEEP_WAKE_TEST, MEM_TEST, VIDEO_TEST, BATTERY_TEMP_TEST
//	}
//
//	String memTestResult;
//	private int mTestStep = 0;
//	float batteryTemp = 0;
//
//	private RESULT[] mTestResult;
//	TextView[] mTextViews;
//
//	Spinner sleepWakeCountSpinner, videoTimeSpinner, memSizeSpinner;
//	String[] sleepWakeCountVal = { "Def(10)", "0", "1", "2", "5", "10", "20", };
//	String[] memSizeVal = { "Def(200M)", "0", "1", "10", "100", "200", "250", };
//	String[] videoTimeVal = { "Def(--m)", "0", "1", "10", "30", "60", "90",
//			"120", "--" };
//
//	PowerManager powerManager;
//
//	ProgressBar progressBar;
//	KeyguardManager keyguardManager;
//	KeyguardLock keyguardLock;
//	Button stopVideo;
//
//	final String SLEEP_WAKE = "Sleep:";
//
//	boolean videoStoped = false;
//	protected WakeLock wakeLock;
//	final String ACTION = "sleep_wake_action";
//	AlarmManager alarmManager;
//	PendingIntent pendingIntent;
//	BroadcastReceiver receiver;
//	
//	public RuninTestActivity() {
//		mTestResult = new RESULT[] { RESULT.UNDEF, RESULT.UNDEF, RESULT.UNDEF,
//				RESULT.UNDEF, }; 
//		mTextViews = new TextView[mTestResult.length];
//	}
// 
//	protected void onCreate(Bundle paramBundle) {
//		super.onCreate(paramBundle);
//
//		setTitle(getTitle() + "----("
//				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
//				+ ")");
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
//
//		setContentView(R.layout.runintest);
//		mTextViews[0] = (TextView) findViewById(R.id.SleepWakeText);
//		mTextViews[0].setText(SLEEP_WAKE);
//		mTextViews[1] = (TextView) findViewById(R.id.MemText);
//		mTextViews[1].setText("Mem:");
//		mTextViews[2] = (TextView) findViewById(R.id.VideoText);
//		mTextViews[2].setText("Video:");
//		mTextViews[3] = (TextView) findViewById(R.id.BatteryTempText);
//		mTextViews[3].setText("Temp:");
//
//		progressBar = (ProgressBar) findViewById(R.id.progress);
//		stopVideo = (Button) findViewById(R.id.StopVideo);
//		stopVideo.setText("stop");
//
//		videoView = (VideoView) findViewById(R.id.VideoView);
//		videoView.setVideoPath(VIDEO_PATH);
//		// videoView.setMediaController(new MediaController(this));
//		videoView.setOnCompletionListener(new OnCompletionListener() {
//
//			public void onCompletion(MediaPlayer mp) {
//				videoView.setVideoPath(VIDEO_PATH);
//				videoView.start();
//			}
//		});
//
//		sleepWakeCountSpinner = (Spinner) findViewById(R.id.SleepWakeTimesSpinner);
//		memSizeSpinner = (Spinner) findViewById(R.id.MemSizeSpinner);
//		videoTimeSpinner = (Spinner) findViewById(R.id.VideoLengthSpinner);
//
//		sleepWakeCountSpinner.setAdapter(new ArrayAdapter<String>(this,
//				android.R.layout.simple_spinner_dropdown_item,
//				sleepWakeCountVal));
//		memSizeSpinner.setAdapter(new ArrayAdapter<String>(this,
//				android.R.layout.simple_spinner_dropdown_item, memSizeVal));
//		videoTimeSpinner.setAdapter(new ArrayAdapter<String>(this,
//				android.R.layout.simple_spinner_dropdown_item, videoTimeVal));
//
//		sleepWakeCountSpinner
//				.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//					public void onItemSelected(AdapterView<?> parent,
//							View view, int position, long id) {
//						sleepWakeCountString = parent.getItemAtPosition(
//								position).toString();
//					}
//
//					public void onNothingSelected(AdapterView<?> parent) {
//
//					}
//				});
//		videoTimeSpinner
//				.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//					public void onItemSelected(AdapterView<?> parent,
//							View view, int position, long id) {
//						videoTimeString = parent.getItemAtPosition(position)
//								.toString();
//					}
//
//					public void onNothingSelected(AdapterView<?> parent) {
//
//					}
//				});
//		memSizeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//			public void onItemSelected(AdapterView<?> parent, View view,
//					int position, long id) {
//				memSizeString = parent.getItemAtPosition(position).toString();
//			}
//
//			public void onNothingSelected(AdapterView<?> parent) {
//
//			}
//		});
//		final Button startButton = (Button) findViewById(R.id.TestStart);
//		startButton.setText("Start Test");
//		startButton.setOnClickListener(new View.OnClickListener() {
//
//			public void onClick(View v) {
//				if (startButton.getVisibility() != View.VISIBLE) {
//					return;
//				}
//				mHandler.sendEmptyMessage(MSG_DO_TEST);
//				startButton.setVisibility(View.GONE);
//				sleepWakeCountSpinner.setEnabled(false);
//				videoTimeSpinner.setEnabled(false);
//				memSizeSpinner.setEnabled(false);
//			}
//		});
//		
//		stopVideo.setOnClickListener(new View.OnClickListener() {
//
//			public void onClick(View v) {
//				if (stopVideo.getVisibility() == View.INVISIBLE) {
//					return;
//				}
//				stopVideo.setVisibility(View.INVISIBLE);
//				videoStoped = true;
//				videoTime = SystemClock.uptimeMillis() - videoStart;
//				mHandler.sendEmptyMessage(MSG_STOP);
//			}
//		});
//
//		ControlButtonUtil.initControlButtonView(this);
//		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
//		saveResult();
//
//		powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
//				| PowerManager.ACQUIRE_CAUSES_WAKEUP
//				| PowerManager.ON_AFTER_RELEASE, "jeffy_sleep_lock");
//
//		keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
//		keyguardLock = keyguardManager.newKeyguardLock("");
//		keyguardLock.disableKeyguard();
//
//		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//		pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION),
//				PendingIntent.FLAG_CANCEL_CURRENT);
//
//		receiver = new BroadcastReceiver() {
//			boolean sleep = false;
//			int count = 0;
//
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				if (!sleep) {
//					// goto Sleep
//
//					Log.i("Jeffy", "Sleep");
//					if (wakeLock.isHeld()) {
//						wakeLock.release();
//					}
//					powerManager.goToSleep(SystemClock.uptimeMillis());
//				} else {
//					// wake up
//
//					count++;
//					Log.i("Jeffy", "wake");
//					if (!wakeLock.isHeld()) {
//						wakeLock.acquire();
//					} else {
//						Log.e("Jeffy", "Run in, wake lock statue error");
//						mTextViews[TEST_STEP.SLEEP_WAKE_TEST.ordinal()]
//								.setText(SLEEP_WAKE);
//						mHandler.sendEmptyMessage(MSG_TEST_FAILED);
//					}
//					if (sleepWakeCount > count) {
//						mTextViews[TEST_STEP.SLEEP_WAKE_TEST.ordinal()]
//								.setText(SLEEP_WAKE + count);
//					} else {
//						mTextViews[TEST_STEP.SLEEP_WAKE_TEST.ordinal()]
//								.setText(SLEEP_WAKE);
//						mHandler.sendEmptyMessage(MSG_TEST_PASS);
//						return;
//					}
//				}
//				sleep = !sleep;
//
//				alarmManager.set(AlarmManager.RTC_WAKEUP,
//						System.currentTimeMillis() + SLEEP_WAKE_DIST,
//						pendingIntent);
//			}
//		};
//
//		registerReceiver(receiver, new IntentFilter(ACTION));
//	}
//
//	Handler mHandler = new Handler() {
//
//		public void handleMessage(Message msg) {
//			mHandler.removeMessages(msg.what);
//			boolean pass = false;
//
//			switch (msg.what) {
//			case MSG_DO_TEST:
//				saveResult();
//				if (mTestStep < TEST_STEP.values().length) {
//					doTest();
//				} else {
//					endAllTest();
//				}
//				break;
//			case MSG_STOP:
//				stopTest();
//				break;
//			case MSG_TEST_PASS:
//			case MSG_TEST_FAILED:
//				if (mTestStep >= TEST_STEP.values().length) {
//					return;
//				}
//				pass = msg.what == MSG_TEST_PASS;
//				mTestResult[mTestStep] = pass ? RESULT.OK : RESULT.FAIL;
//				String result = (pass ? "Pass" : "Failed");
//				if (msg.obj != null) {
//					result += "(" + msg.obj.toString() + ")";
//				}
//				mTextViews[mTestStep].setText(mTextViews[mTestStep].getText()
//						+ result);
//				if (!isFinishing()) {
//					mTestStep++;
//					sendEmptyMessage(MSG_DO_TEST);
//				}
//				break;
//
//			case MSG_SLEEP_WAKE:
//				Runnable runnable = (Runnable) msg.obj;
//				runnable.run();
//				break;
//			default:
//				break;
//			}
//		}
//	};
//
//	private void doTest() {
//		TEST_STEP step = TEST_STEP.values()[mTestStep];
//		switch (step) {
//		case SLEEP_WAKE_TEST:
//			doSleepWakeTest();
//			// mHandler.sendEmptyMessage(MSG_TEST_FAILED);
//			break;
//		case MEM_TEST:
//			doMemTest();
//			// mHandler.sendEmptyMessage(MSG_TEST_PASS);
//			break;
//		case VIDEO_TEST:
//			doVideoTest();
//			// mHandler.sendEmptyMessage(MSG_TEST_FAILED);
//			break;
//		case BATTERY_TEMP_TEST:
//			doBatteryTempTest();
//		default:
//			break;
//		}
//	}
//
//	private void doBatteryTempTest() {
//		String temp = SystemUtil.execScriptCmd("cat " + BATTERY_TEMP_PATH,
//				DeviceTest.TEMP_FILE_PATH, true);
//
//		batteryTemp = Integer.parseInt(temp);
//		batteryTemp /= 10;
//		mHandler.sendMessage(mHandler.obtainMessage(
//				batteryTemp < 40 ? MSG_TEST_PASS : MSG_TEST_FAILED, batteryTemp
//						+ "C"));
//	}
//
//	private void doSleepWakeTest() {
//		sleepWakeCount = SLEEP_WAKE_COUNT;
//		if (sleepWakeCountString != null) {
//			try {
//				sleepWakeCount = Integer.parseInt(sleepWakeCountString);
//			} catch (Exception e) {
//			}
//		}
//
//		if (sleepWakeCount == 0) {
//			mHandler.sendEmptyMessage(MSG_TEST_PASS);
//			return;
//		}
//		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
//				pendingIntent);
//	}
//
//	private void doVideoTest() {
//		videoTime = VIDEO_TEST_TIME;
//		if (this.videoTimeString != null) {
//			try {
//				videoTime = Integer.parseInt(this.videoTimeString) * 60 * 1000;
//				if (videoTime == 0) {
//					mHandler.sendEmptyMessage(MSG_TEST_PASS);
//					return;
//				}
//			} catch (Exception e) {
//			}
//		}
//
//		File file = new File(VIDEO_PATH);
//		if (!file.exists() || !file.canRead()) {
//			mHandler.sendEmptyMessage(MSG_TEST_FAILED);
//			return;
//		}
//
//		ControlButtonUtil.Hide();
//		stopVideo.setVisibility(View.VISIBLE);
//		videoView.setVisibility(View.VISIBLE);
//		videoView.bringToFront();
//		videoView.start();
//		videoStart = SystemClock.uptimeMillis();
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//
//		if (videoTimeString.indexOf("--") < 0) {
//			mHandler.sendEmptyMessageDelayed(MSG_STOP, videoTime);
//		}
//	}
//
//	protected void endAllTest() {
//		boolean pass = true;
//		for (RESULT result : mTestResult) {
//			if (result == RESULT.FAIL) {
//				pass = false;
//				break;
//			}
//		}
//		// saveResult();
//		if (pass) {
//			// findViewById(R.id.btn_Pass).setVisibility(View.VISIBLE);
//			findViewById(R.id.btn_Pass).performClick();
//			// } else {
//			// // findViewById(R.id.btn_Fail).performClick();
//			// } else {
//			// mHandler.postDelayed(new Runnable() {
//			//
//			// public void run() {
//			// if (!isFinishing()) {
//			// findViewById(R.id.btn_Fail).performClick();
//			// }
//			// }
//			// }, DeviceTest.TEST_FAILED_DELAY);
//		}
//	}
//
//	private void doMemTest() {
//		if (! new File(DeviceTest.MEMTESTER_PATH).exists()) {
//			mHandler.sendEmptyMessage(MSG_TEST_FAILED);
//			return;
//		}
//		progressBar.setVisibility(View.VISIBLE);
//		new Thread(new Runnable() {
//
//			public void run() {
//				String result = null;
//				int memSize = MEM_TEST_SIZE;
//
//				if (RuninTestActivity.this.memSizeString != null) {
//					try {
//						memSize = Integer
//								.parseInt(RuninTestActivity.this.memSizeString);
//					} catch (Exception e) {
//					}
//				}
//
//				if (memSize == 0) {
//					mHandler.post(new Runnable() {
//						public void run() {
//							progressBar.setVisibility(View.GONE);
//						}
//					});
//					mHandler.sendEmptyMessage(MSG_TEST_PASS);
//					return;
//				}
//				try {
//					result = SystemUtil.execRootCmd(DeviceTest.MEMTESTER_PATH + " "
//							+ memSize + "M 1");
//					mHandler.post(new Runnable() {
//
//						public void run() {
//							progressBar.setVisibility(View.GONE);
//						}
//					});
//					result = "---------------------------\n"
//							+ result.substring(result.indexOf("want"));
//
//					memTestResult = result;
//					boolean pass = memTestResult.endsWith("Pass.");
//					if (pass) {
//						mHandler.sendEmptyMessage(MSG_TEST_PASS);
//					} else {
//						mHandler.sendEmptyMessage(MSG_TEST_FAILED);
//					}
//				} catch (Exception e) {
//					mHandler.sendMessage(mHandler.obtainMessage(
//							MSG_TEST_FAILED, e.getMessage()));
//				}
//			}
//		}).start();
//	}
//
//	protected void stopTest() {
//		if (mTestStep >= TEST_STEP.values().length) {
//			return;
//		}
//		mTestResult[mTestStep] = RESULT.FAIL;
//		TEST_STEP step = TEST_STEP.values()[mTestStep];
//		switch (step) {
//		case SLEEP_WAKE_TEST:
//			stopSleepWakeTest();
//			break;
//		case MEM_TEST:
//			stopMemTest();
//			break;
//		case VIDEO_TEST:
//			stopVideoTest();
//			break;
//
//		default:
//			break;
//		}
//	}
//
//	private void stopSleepWakeTest() {
//		mHandler.removeMessages(MSG_SLEEP_WAKE);
//		mHandler.sendEmptyMessage(MSG_TEST_FAILED);
//
//		alarmManager.cancel(pendingIntent);
//	}
//
//	private void stopVideoTest() {
//		videoView.stopPlayback();
//		ControlButtonUtil.Show();
//		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
//		stopVideo.setVisibility(View.GONE);
//		videoView.setVisibility(View.GONE);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		boolean pass = (SystemClock.uptimeMillis() - videoStart) >= this.videoTime;
//
//		mHandler.sendEmptyMessage(pass ? MSG_TEST_PASS : MSG_TEST_FAILED);
//	}
//
//	//
//	// public void onConfigurationChanged(Configuration newConfig) {
//	// super.onConfigurationChanged(newConfig);
//	// }
//
//	private void stopMemTest() {
//		SystemUtil.killProcessByPath(DeviceTest.MEMTESTER_PATH);
//	}
//
//	protected void onStop() {
//		super.onStop();
//		stopTest();
//		keyguardLock.reenableKeyguard();
//		unregisterReceiver(receiver);
//	}
//
//	public String getResult() {
//		
//		String result = DeviceTest.RESULT_INFO_HEAD_JUST_INFO;
//		result += DeviceTest.formatResult("SleepWakeTest", mTestResult[0],
//				DeviceTest.RESULT_INFO_HEAD + sleepWakeCount) + "\n";
//		result += DeviceTest.formatResult("MemTest", mTestResult[1], null)
//				+ "\n";
//		result += DeviceTest.formatResult("VideoTest", mTestResult[2],
//				DeviceTest.RESULT_INFO_HEAD + (videoTime / 1000 / 60) + "Min")
//				+ "\n";
//		result += DeviceTest.formatResult("BatteryTemp", mTestResult[3],
//				DeviceTest.RESULT_INFO_HEAD + batteryTemp + "C") + "\n";
//		
//		return result;
//	}
//	
//	private void saveResult() {
//		ControlButtonUtil.setResult(getResult());
//	}
//
//	public boolean dispatchKeyEvent(KeyEvent event) {
//		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//			return false;
//		}
//		return super.dispatchKeyEvent(event);
//	}
//
//}
