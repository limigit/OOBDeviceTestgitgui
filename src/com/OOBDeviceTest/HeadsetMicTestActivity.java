package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.Recorder;

public class HeadsetMicTestActivity extends Activity {

	private static final String TAG = HeadsetMicTestActivity.class
			.getSimpleName();

	private final static String ERRMSG = "Record error";

	private static final int MSG_TEST_MIC_ING = 8738;
	private static final int MSG_TEST_MIC_OVER = 13107;
	private static final int MSG_TEST_MIC_START = 4369;
	boolean isSDcardTestOk = false;
	boolean isTestStart = false;
	AudioManager mAudioManager;
	private Handler mHandler;
	boolean mHeadSetOn = false;
	private BroadcastReceiver mHeadsetReceiver;
	boolean mIsTesting = false;
	int mOldVolume;
	Recorder mRecorder;
	TextView mResult;
	TextView mText;
	int mTimes = 0;
	TextView mTitle;

	public HeadsetMicTestActivity() {

		this.mHeadsetReceiver = new MBroadcastReceiver();

		this.mHandler = new MyHandler();
	}

	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.headsetmictest);

		this.mResult = (TextView) findViewById(R.id.headsetresultText);

		this.mResult.setVisibility(View.VISIBLE);
		this.mResult.setGravity(17);
		ControlButtonUtil.initControlButtonView(this);
		this.mRecorder = new Recorder();
	
		this.mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onPause();
				onResume();
			}
		});

	}

	
	protected void onResume() {

		super.onResume();
//		this.isSDcardTestOk = false;
//		if (!Environment.getExternalStorageState().equals(
//				Environment.MEDIA_MOUNTED)) {
//			this.mResult.setText("Please insert sdcard");
//			return;
//		}
//
//		if (!isSDcardHasSpace()) {
//
//			this.mResult.setText("sdcard has no space");
//			stopMediaPlayBack();
//			return;
//
//		}
		
		IntentFilter localIntentFilter = new IntentFilter(
				"android.intent.action.HEADSET_PLUG");
		registerReceiver(this.mHeadsetReceiver, localIntentFilter);
		
		stopMediaPlayBack();
		this.isSDcardTestOk = true;
		this.mOldVolume = this.mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVolume = this.mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				maxVolume, 0);
		this.mHeadSetOn = this.mAudioManager.isWiredHeadsetOn();
		if (!mHeadSetOn) {
			mResult.setText(getString(R.string.HeadsetTips));
			return;
		}

		mIsTesting = true;
		mHandler.sendEmptyMessage(MSG_TEST_MIC_START);

	}
	
	
	protected void onPause() {
		 super.onPause();
	

		if (!mHeadSetOn) {
			return;
			
		}
		
	   
	    if (this.isSDcardTestOk)
	    {
	     
	      switch (this.mRecorder.state()) {

			case Recorder.IDLE_STATE:
				this.mRecorder.delete();
				break;
			case Recorder.PLAYING_STATE:
				this.mRecorder.stop();
				this.mRecorder.delete();
				break;
			case Recorder.RECORDING_STATE:
				this.mRecorder.stop();
				this.mRecorder.clear();
				break;
			}
	      
	      unregisterReceiver(mHeadsetReceiver);
	      mAudioManager.setStreamVolume(3, mOldVolume, 0);
	    }
	    
	}

	public boolean isSDcardHasSpace() {
		File pathFile = android.os.Environment.getExternalStorageDirectory();

		StatFs statfs = new StatFs(pathFile.getPath());

		if (statfs.getAvailableBlocks() > 1) {

			return true;

		}

		return false;

	}

	public void stopMediaPlayBack() {
		Intent localIntent = new Intent("com.android.music.musicservicecommand");
		localIntent.putExtra("command", "pause");
		sendBroadcast(localIntent);
	}

	class MyHandler extends Handler {
		MyHandler() {

		}

		
		public void handleMessage(Message msg) {

			super.handleMessage(msg);

			switch (msg.what) {

			case MSG_TEST_MIC_START:
				isTestStart = true;
				removeMessages(MSG_TEST_MIC_START);
				mTimes = 5;
				mResult.setText(" " + mTimes + " ");
				mRecorder.startRecording(3, ".amr");
				sendEmptyMessageDelayed(MSG_TEST_MIC_ING, 1000L);
				break;
			case MSG_TEST_MIC_ING:
				removeMessages(MSG_TEST_MIC_ING);
				if (mTimes > 0) {

					mResult.setText(" " + mTimes + " ");
					mTimes--;

					Log.i(TAG, "mTimes=" + mTimes);

					sendEmptyMessageDelayed(MSG_TEST_MIC_ING, 1000L);
				} else {
					sendEmptyMessage(MSG_TEST_MIC_OVER);
				}
				break;
			case MSG_TEST_MIC_OVER:
				removeMessages(MSG_TEST_MIC_OVER);

				mRecorder.stopRecording();

				if (isTestStart) {
					isTestStart = false;
					if (mRecorder.sampleLength() > 0) {
						mResult.setText("record successs!start Play back");
						mRecorder.startPlayback();
					} else {
						mResult.setText(ERRMSG);
					}

				}

				break;
			}

		}

	}

	class MBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context paramContext, Intent paramIntent) {

			String action = paramIntent.getAction();
			Log.i(TAG, "action");
			if ("android.intent.action.HEADSET_PLUG".equals(action)) {
				if (paramIntent.getIntExtra("state", 0) != 1) {

					Log.i(TAG, "HEADSET has bean removed");
					
					if (mIsTesting)
						mHandler.sendEmptyMessage(MSG_TEST_MIC_OVER);
					mIsTesting = false;
					return;
				}

				if (!mIsTesting) {
					Log.i(TAG, "HEADSET has bean inserted");
					mIsTesting = true;
					mHandler.sendEmptyMessage(MSG_TEST_MIC_START);
				}
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
