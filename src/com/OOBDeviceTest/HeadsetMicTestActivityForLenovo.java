package com.OOBDeviceTest;

import java.io.File;

import com.OOBDeviceTest.HeadsetMicTestActivity.MBroadcastReceiver;
import com.OOBDeviceTest.PhoneMicTestActivityForLenovo.MyHandler;
import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.Recorder;
import com.OOBDeviceTest.helper.VUMeter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;

public class HeadsetMicTestActivityForLenovo extends Activity {
	private static final String TAG = HeadsetMicTestActivityForLenovo.class
			.getSimpleName();

	private final static String ERRMSG = "Record error";
	private final static int PLAYBACK_TIME = 5;

	public final static int MSG_TEST_MIC_ING = 0;
	public final static int MSG_TEST_MIC_OVER = 1;
	public final static int MSG_TEST_MIC_START = 2;
	public final static int MSG_TEST_MIC_ABOVE_LEVEL = 3;
	public final static int MSG_TEST_PLAY_BACK = 4;
	public final static int MSG_TEST_PLAY_BACK_STOP = 5;
	public final static int MSG_SHOW_BUTTON = 6;

	private AudioManager mAudioManager;
	private Handler mHandler;
	private Recorder mRecorder;
	private BroadcastReceiver mHeadsetReceiver;

	private TextView mResult;
	private TextView mText;
	private TextView mTitle;
	private VUMeter mVUMeter;

	private int mOldVolume;
	private int mRecordTimesNow;
	private boolean mSpeakerOn = false;
	private boolean mIsTesting = false;
	private boolean mHeadSetOn = false;
	private boolean mIsFinish = false;

	/** test need the voice volunm above the Standard value. */
	public boolean mIsAbove = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().addFlags(1152);
		setContentView(R.layout.headsetmictest);

		mVUMeter = (VUMeter) findViewById(R.id.uvMeter);
		mResult = (TextView) findViewById(R.id.headsetresultText);
		mResult.setVisibility(View.VISIBLE);
		mResult.setGravity(17);

		mText = (TextView) findViewById(R.id.textSubTitle);
		mText.setText(getString(R.string.HeadsetMicSubTitleForLenove));

		mHandler = new MyHandler();
		mRecorder = new Recorder();
		mHeadsetReceiver = new MBroadcastReceiver();
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mVUMeter.setRecorder(mRecorder);
		mVUMeter.setHandler(mHandler);

		ControlButtonUtil.initControlButtonView(this);
		hideButton();

	}

	@Override
	protected void onResume() {
		super.onResume();
    DeviceTest.isOnTestAll=true;
		IntentFilter localIntentFilter = new IntentFilter(
				"android.intent.action.HEADSET_PLUG");
		registerReceiver(mHeadsetReceiver, localIntentFilter);

		stopMediaPlayBack();

		mOldVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

		mHeadSetOn = mAudioManager.isWiredHeadsetOn();
		if (!mHeadSetOn) {
			mResult.setText(getString(R.string.HeadsetTips));
			return;
		}

		mIsTesting = true;
		mHandler.sendEmptyMessage(MSG_TEST_MIC_START);

	}

	@Override
	protected void onPause() {
     	mHandler.removeMessages(MSG_SHOW_BUTTON);
		mHandler.removeMessages(MSG_TEST_PLAY_BACK_STOP);
		mHandler.removeMessages(MSG_TEST_PLAY_BACK);
		mHandler.removeMessages(MSG_TEST_MIC_ABOVE_LEVEL);
		super.onPause();
		unregisterReceiver(mHeadsetReceiver);
    DeviceTest.isOnTestAll=true;
		switch (mRecorder.state()) {

		case Recorder.IDLE_STATE:
			mRecorder.delete();
			break;
		case Recorder.PLAYING_STATE:
			mRecorder.stop();
			mRecorder.delete();
			break;
		case Recorder.RECORDING_STATE:
			mRecorder.stop();
			mRecorder.clear();
			break;
		}
		
	  mAudioManager.setStreamVolume(3, mOldVolume, 0);
	  
		if (mSpeakerOn) {
			mAudioManager.setSpeakerphoneOn(false);

		}

	}

	public void stopMediaPlayBack() {
		Intent localIntent = new Intent("com.android.music.musicservicecommand");
		localIntent.putExtra("command", "pause");
		sendBroadcast(localIntent);
	}

	public boolean isSDcardHasSpace() {
		File pathFile = android.os.Environment.getExternalStorageDirectory();
		StatFs statfs = new StatFs(pathFile.getPath());
		if (statfs.getAvailableBlocks() > 1) {
			return true;
		}
		return false;
	}

	private void hideButton() {
		((Button) findViewById(R.id.btn_Pass)).setVisibility(View.INVISIBLE);
		//((Button) findViewById(R.id.btn_Fail)).setVisibility(View.INVISIBLE);
		//((Button) findViewById(R.id.btn_Retest)).setVisibility(View.INVISIBLE);
	}

	private void showButton() {
		((Button) findViewById(R.id.btn_Pass)).setVisibility(View.VISIBLE);
		((Button) findViewById(R.id.btn_Fail)).setVisibility(View.VISIBLE);
		((Button) findViewById(R.id.btn_Retest)).setVisibility(View.VISIBLE);
	}

	class MyHandler extends Handler {
		MyHandler() {
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_TEST_MIC_START:
				removeMessages(MSG_TEST_MIC_START);
				mResult.setText(getString(R.string.startRecord));
				mRecorder.startRecording(MediaRecorder.OutputFormat.AMR_NB, ".amr");
				sendEmptyMessageDelayed(MSG_TEST_MIC_ING, 1000L);
				break;

			case MSG_TEST_MIC_ING:
				mRecordTimesNow++;
				if (mRecordTimesNow > 5 && mIsAbove) {
					sendEmptyMessage(MSG_TEST_MIC_OVER);
				} else {
					sendEmptyMessageDelayed(MSG_TEST_MIC_ING, 1000L);
				}
				break;

			case MSG_TEST_MIC_OVER:
				mRecorder.stopRecording();
				removeMessages(MSG_TEST_MIC_OVER);
				
				if (!mIsAbove || mRecordTimesNow < 5) {
					mResult.setText(getString(R.string.HeadsetTips));
					return;
				}
				sendEmptyMessageDelayed(MSG_TEST_PLAY_BACK, 1000L);
				break;

			case MSG_TEST_MIC_ABOVE_LEVEL:
				mIsAbove = true;
				mResult.setText(R.string.PhoneMicEffective);
				break;

			case MSG_TEST_PLAY_BACK:
				if (mRecorder.sampleLength() > 0) {
					mResult.setText(R.string.HeadsetRecodrSuccess);
					mRecorder.startPlayback();
				} else {
					mResult.setText(R.string.RecordError);
				}
				mIsFinish = true;
				sendEmptyMessageDelayed(MSG_TEST_PLAY_BACK_STOP, 5000L);
				sendEmptyMessageDelayed(MSG_SHOW_BUTTON, 3000L);
				break;

			case MSG_TEST_PLAY_BACK_STOP:
				mRecorder.stopPlayback();
				break;

			case MSG_SHOW_BUTTON:
				showButton();
				break;
			default:
				break;
			}

			mVUMeter.invalidate();
		}

	}

	private class MBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context paramContext, Intent paramIntent) {

			String action = paramIntent.getAction();
			Log.i(TAG, "action");
			if ("android.intent.action.HEADSET_PLUG".equals(action)) {
				if (paramIntent.getIntExtra("state", 0) != 1) {

					Log.i(TAG, "HEADSET has bean removed");
					if (mIsTesting && !mIsFinish)
						mHandler.sendEmptyMessage(MSG_TEST_MIC_OVER);
					mIsTesting = false;
					return;
				}

				if (!mIsTesting && !mIsFinish) {
					Log.i(TAG, "HEADSET has bean inserted");
					mIsTesting = true;
					mHandler.sendEmptyMessage(MSG_TEST_MIC_START);
				}
			}

		}
	}
	
		@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

}
