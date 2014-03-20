package com.OOBDeviceTest;

import java.io.File;

import com.OOBDeviceTest.PhoneMicTestActivity.MyHandler;
import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.Recorder;
import com.OOBDeviceTest.helper.VUMeter;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
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

public class PhoneMicTestActivityForLenovo extends Activity {
	private static final String TAG = PhoneMicTestActivityForLenovo.class
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

	private TextView mResult;
	private TextView mText;
	private TextView mTitle;
	private VUMeter mVUMeter;

	private int mOldVolume;
	private int mRecordTimesNow;
	private boolean mSpeakerOn = false;
	
	/** test need the voice volunm above the Standard value. */ 
	public boolean mIsAbove = false;
	
	public boolean mIsTesting = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().addFlags(1152);
		setContentView(R.layout.phonemictest);
		
		mVUMeter = (VUMeter) findViewById(R.id.uvMeter);
		mResult = (TextView) findViewById(R.id.phoneresultText);
		mResult.setVisibility(View.VISIBLE);
		mResult.setGravity(17);
		
		mText = (TextView) findViewById(R.id.textSubTitle);
		mText.setText(getString(R.string.PhoneMicSubTitleForLenove));
		
		ControlButtonUtil.initControlButtonView(this);
		
		mHandler = new MyHandler();
		mRecorder = new Recorder();
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mVUMeter.setRecorder(mRecorder);
		mVUMeter.setHandler(mHandler);
		hideButton();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		stopMediaPlayBack();

		mOldVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				maxVolume, 0);

		mSpeakerOn = mAudioManager.isSpeakerphoneOn();

		if (!mSpeakerOn) {
			mAudioManager.setSpeakerphoneOn(true);
		}
		mHandler.sendEmptyMessage(MSG_TEST_MIC_START);

	}

	@Override
	protected void onPause() {

		super.onPause();
		mHandler.removeMessages(MSG_SHOW_BUTTON);
		mHandler.removeMessages(MSG_TEST_PLAY_BACK_STOP);
		mHandler.removeMessages(MSG_TEST_PLAY_BACK);
		mHandler.removeMessages(MSG_TEST_MIC_ABOVE_LEVEL);
		
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

	private void hideButton() {
		((Button) findViewById(R.id.btn_Pass)).setVisibility(View.INVISIBLE);
	//	((Button) findViewById(R.id.btn_Fail)).setVisibility(View.INVISIBLE);
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
				if (!mIsTesting) {
					mResult.setText(getString(R.string.startRecord));
					mRecorder.startRecording(3, ".amr");
					sendEmptyMessageDelayed(MSG_TEST_MIC_ING, 1000L);
					mIsTesting = true;
				}
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
				removeMessages(MSG_TEST_MIC_OVER);
				mRecorder.stopRecording();
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
	
	  @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
