package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.IOException;
import java.util.Random;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HeadsetSpeakerTestActivityForLenovo extends Activity implements
		OnClickListener {
	public static final String TAG = "HeadsetSpeakerTestActivityForLenovo";
	public static final boolean DEBUG = true;

	private static final int MSG_START_LEFT = 0;
	private static final int MSG_SHOW_MESSAGE = 1;
	private static final int MSG_START_RIGHT = 2;
	private static final int MSG_PLAY_MUSIC = 3;

	private static final int LEFT_VOL = 0;
	private static final int RIGHT_VOL = 1;

	private TextView mTxtContent;
	private View mNumberPadView;

	private AudioManager mAudioManager;
	private MediaPlayer mPlayer;
	
	
	private int mOldVolume;
	private boolean mSpeakerOn;
	private boolean mHeadSetOn = false;

	private int mLeftCount = 0;
	private int mRightCount = 0;
	/** random max play count */
	private int mLeftRandomCount = 0;
	/** random max play count */
	private int mRightRandomCount = 0;
	private int mNowVolunm = 0;
	private int mPressBtnNum = 0;
	
	private boolean mIsTesting = false;
	
	
	private AssetFileDescriptor mFd;
	private Random mRandom;

	private BroadcastReceiver mHeadsetReceiver;
	
	
	private static boolean  isFirstLeftTest = false;
	private static boolean  isFirstRightTest = false;

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.headsetspeakertestforlenovo);
		
		mTxtContent = (TextView) findViewById(R.id.txtContent);
		mNumberPadView = (View) findViewById(R.id.numberpad);
		mNumberPadView.setVisibility(View.INVISIBLE);
		initNumberPad();

		mLeftRandomCount = getRandomNum();
		mRightRandomCount = getRandomNum();
		log("RANDOM LEFT:" + mLeftRandomCount + "  RANDOM RIGHT:"
				+ mRightRandomCount);

		mAudioManager = (AudioManager) getSystemService("audio");
		mPlayer = new MediaPlayer();

		ControlButtonUtil.initControlButtonView(this);
		hideButton();

		mHeadsetReceiver = new MBroadcastReceiver();

	}

	protected void onDestroy() {
		super.onDestroy();
		mPlayer.stop();
		if (this.mPlayer == null) {
			return;
		}
		this.mPlayer.release();
		this.mPlayer = null;
	}

	protected void onPause() {
		super.onPause();
		if (mHeadsetReceiver != null)
			unregisterReceiver(mHeadsetReceiver);
		this.mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, false);
		this.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				this.mOldVolume, 0);
		if (this.mSpeakerOn)
			return;
		this.mAudioManager.setSpeakerphoneOn(false);

	}

	protected void onResume() {
		super.onResume();

		IntentFilter localIntentFilter = new IntentFilter(
				"android.intent.action.HEADSET_PLUG");
		registerReceiver(this.mHeadsetReceiver, localIntentFilter);
/*
		initNumberPad();
		mLeftRandomCount = getRandomNum();
		mRightRandomCount = getRandomNum();
		log("RANDOM LEFT:" + mLeftRandomCount + "  RANDOM RIGHT:"
				+ mRightRandomCount);
		
		mLeftCount = 0;
		 mRightCount = 0;
		 */
		 isFirstLeftTest = true;
		 isFirstRightTest = false;
		
		stopMediaPlayBack();
		this.mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);
		int i = this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mOldVolume = i;
		int j = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, j, 0);
		this.mSpeakerOn = this.mAudioManager.isSpeakerphoneOn();
		if (!this.mSpeakerOn) {
			this.mAudioManager.setSpeakerphoneOn(true);
		}

		mHeadSetOn = this.mAudioManager.isWiredHeadsetOn();
		if (!mHeadSetOn) {
			mTxtContent.setText(getString(R.string.HeadsetTips));
			return;
		}

		mHandler.sendEmptyMessageDelayed(MSG_START_LEFT, 1500);

	}

	private void playMusic(int whichVol) {
		try {
			mFd = null;
			if (whichVol == LEFT_VOL) {
				log("playMusic: LEFT_VOL");
				mFd = getAssets().openFd("left.wav");
				mPlayer.setVolume(1.0f, 0.0f);
				mNowVolunm = LEFT_VOL;
				mLeftCount++;
			} else if (whichVol == RIGHT_VOL) {
				log("playMusic: RIGHT_VOL");
				mPlayer.reset();
				mFd = getAssets().openFd("right.wav");
				mPlayer.setVolume(0.0f, 1.0f);
				mNowVolunm = RIGHT_VOL;
				mRightCount++;
			}
			mPlayer.setDataSource(mFd.getFileDescriptor(),
					mFd.getStartOffset(), mFd.getDeclaredLength());
			mPlayer.prepare();
			mPlayer.setOnCompletionListener(mCompletionListener);
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void replayMusic() {
		log("replayMusic ---mPlayer:isPlaying STATE:" + mPlayer.isPlaying());
		mPlayer.start();
	}

	private void stopMediaPlayBack() {
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		sendBroadcast(i);

	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_START_LEFT:
				if (!mIsTesting) {
					playMusic(LEFT_VOL);
					mIsTesting = true;
				}
				break;
			case MSG_SHOW_MESSAGE:
				showMessage();
				break;
			case MSG_START_RIGHT:
				log("receive : MSG_START_RIGHT");
				mTxtContent.setVisibility(View.INVISIBLE);
				hideButton();
				playMusic(RIGHT_VOL);
				break;
			case MSG_PLAY_MUSIC:
				replayMusic();
				break;
			default:
				break;
			}
		};
	};

	private void showMessage() {
		if (mNowVolunm == LEFT_VOL) {
			log("showMessage -- LEFT_VOL ");
			mTxtContent.setText(getString(R.string.LeftMsg));
		} else if (mNowVolunm == RIGHT_VOL) {
			log("showMessage -- RIGHT_VOL ");
			isFirstRightTest=true;
			mTxtContent.setText(getString(R.string.RightMsg));
		}
		mTxtContent.setVisibility(View.VISIBLE);
		mNumberPadView.setVisibility(View.VISIBLE);
		showButton();
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

	private int getRandomNum() {
		if (mRandom == null)
			mRandom = new Random(System.currentTimeMillis());
		int num = Math.abs(mRandom.nextInt()) % 4 + 1;
		return num;
	}

	private synchronized void compareBtnAndRandom() {
		log("compareBtnAndRandom in ... ...");
		if (mNowVolunm == LEFT_VOL) {
			if (mPressBtnNum == mLeftRandomCount) {
				if(isFirstLeftTest){
					isFirstLeftTest=false;
					mHandler.sendEmptyMessageDelayed(MSG_START_RIGHT, 1000);
					log("compareBtnAndRandom - LEFT - FINISH	");
				}
				
			} else {
				log("compareBtnAndRandom - LEFT - btn_Retest	");
				if(isFirstLeftTest){
					isFirstLeftTest=false;
					//RetestFun();
					((Button) findViewById(R.id.btn_Retest)).performClick();
				}
			}
		} else if (mNowVolunm == RIGHT_VOL) {
			if (mPressBtnNum == mRightRandomCount) {
				log("compareBtnAndRandom - RIGHT - btn_Pass	");
				if(isFirstRightTest){
					isFirstRightTest=false;
					((Button) findViewById(R.id.btn_Pass)).performClick();
				}
			} else {
				log("compareBtnAndRandom - RIGHT - btn_Retest	");
				if(isFirstRightTest){
					isFirstRightTest=false;
					//RetestFun();
					((Button) findViewById(R.id.btn_Retest)).performClick();
				}
			}
		}
	}

	private OnCompletionListener mCompletionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			if (mNowVolunm == LEFT_VOL) {
				if (mLeftCount >= mLeftRandomCount) {
					// show msg
					mHandler.sendEmptyMessage(MSG_SHOW_MESSAGE);
				} else {
					// delay 1s to play the next
					mLeftCount++;
					mHandler.sendEmptyMessageDelayed(MSG_PLAY_MUSIC, 1000);
				}
			} else if (mNowVolunm == RIGHT_VOL) {
				if (mRightCount >= mRightRandomCount) {
					// show msg
					mHandler.sendEmptyMessage(MSG_SHOW_MESSAGE);
				} else {
					// delay 1s to play the next
					mRightCount++;
					mHandler.sendEmptyMessageDelayed(MSG_PLAY_MUSIC, 1000);
				}
			}
		}
	};

	private class MBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context paramContext, Intent paramIntent) {

			String action = paramIntent.getAction();
			Log.i(TAG, "action");
			if ("android.intent.action.HEADSET_PLUG".equals(action)) {
				if (paramIntent.getIntExtra("state", 0) != 1) {
					Log.i(TAG, "HEADSET has bean removed");
					return;
				}

				Log.i(TAG, "HEADSET has bean inserted");
				mTxtContent.setVisibility(View.INVISIBLE);
				mHandler.sendEmptyMessageDelayed(MSG_START_LEFT, 1500);
			}
		}
	}

	private void hideButton() {
		((Button) findViewById(R.id.btn_Pass)).setVisibility(View.INVISIBLE);
		((Button) findViewById(R.id.btn_Fail)).setVisibility(View.INVISIBLE);
		((Button) findViewById(R.id.btn_Retest)).setVisibility(View.INVISIBLE);
	}
	
	private void showButton() {
		((Button) findViewById(R.id.btn_Pass)).setVisibility(View.INVISIBLE);
		((Button) findViewById(R.id.btn_Fail)).setVisibility(View.VISIBLE);
		((Button) findViewById(R.id.btn_Retest)).setVisibility(View.VISIBLE);
	}
	
	private void log(String log) {
		if (DEBUG)
			Log.e(TAG, log);
	}
	
	
}
