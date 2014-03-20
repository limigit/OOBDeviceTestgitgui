package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.FileDescriptor;
import java.io.IOException;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HeadsetSpeakerTestActivity extends Activity {
	private final static String TAG = "HeadsetSpeakerTestActivity";
	
	private static final int MSG_SHOW_LEFT_DIALOG = 0;
	private static final int MSG_SHOW_RIGHT_DIALOG = 1;
	private static final int MSG_START_TEST = 2;
	private AudioManager mAudioManager;
	private MediaPlayer mPlayer;
	private int mOldVolume;
	private boolean mSpeakerOn;
	private boolean mHeadSetOn = false;

	private Button leftButton;
	private Button rightButton;
	private TextView txtContent;
	private TextView txtTitle;
	
	private boolean leftEnable = true;
	private boolean rightEnable = true;
	
	private boolean isLeftOk = false;
	private boolean isRightOk = false;

	private BroadcastReceiver mHeadsetReceiver;
	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.headsetspeakertest);
		txtTitle = (TextView) findViewById(R.id.headsettxtTitle);
		txtContent = (TextView) findViewById(R.id.headsettxtContent);
		txtTitle.setText(R.string.headsetSpeakerTitle);
//		txtContent.setText(getString(R.string.headsetSpeakerTip));

		mAudioManager = (AudioManager) getSystemService("audio");
		mPlayer = new MediaPlayer();
		try {
			// mPlayer.setDataSource("/system/media/audio/ringtones/CrazyDream.ogg");
			AssetFileDescriptor fd = getAssets().openFd("test_music.mp3");
			mPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(),
					fd.getDeclaredLength());

			mPlayer.prepare();
			mPlayer.setLooping(true);
			mPlayer.setVolume(1.0f, 0.0f);
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
		leftButton = (Button) findViewById(R.id.headsetspk_btn_left);
		leftButton.setVisibility(View.INVISIBLE);
		leftButton.setOnClickListener(new OnClickListener() {

			
			public void onClick(View v) {
				leftEnable = !leftEnable;
				updateButtons();
			}
		});

		rightButton = (Button) findViewById(R.id.headsetspk_btn_right);
		rightButton.setVisibility(View.INVISIBLE);
		rightButton.setOnClickListener(new OnClickListener() {

			
			public void onClick(View v) {
				rightEnable = !rightEnable;
				updateButtons();
			}
		});
		updateButtons();
		
		mHeadsetReceiver = new MBroadcastReceiver();
		ControlButtonUtil.initControlButtonView(this);
		
		Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onPause();
				onResume();
			}
		});
		
	}

	protected void updateButtons() {
		leftButton.setText("left " + (leftEnable ? "enabled" : "disabled"));
		rightButton.setText("right " + (rightEnable ? "enabled" : "disabled"));

//		mPlayer.setVolume(leftEnable ? 1 : 0, rightEnable ? 1 : 0);
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
		mHandler.removeMessages(MSG_SHOW_LEFT_DIALOG);
		mHandler.removeMessages(MSG_SHOW_RIGHT_DIALOG);
		if (this.mSpeakerOn)
			return;
		this.mAudioManager.setSpeakerphoneOn(false);

	}

	protected void onResume() {
		super.onResume();
		
		IntentFilter localIntentFilter = new IntentFilter(
				"android.intent.action.HEADSET_PLUG");
		registerReceiver(this.mHeadsetReceiver, localIntentFilter);
		
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
			txtContent.setText(getString(R.string.HeadsetTips));
			return;
		}
		
		startTest();
	}
	
	private void startTest() {
		txtContent.setVisibility(View.GONE);
		mPlayer.start();
		mHandler.sendEmptyMessageDelayed(MSG_SHOW_LEFT_DIALOG, 3000);
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
	
	private void showLeftDialog() {
		new AlertDialog.Builder(this)
		.setMessage(R.string.LeftMsg)
		.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						isLeftOk = true;
						mPlayer.setVolume(0.0f, 1.0f);
						mHandler.sendEmptyMessageDelayed(MSG_SHOW_RIGHT_DIALOG, 3000);
					}

				})
		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				((Button) findViewById(R.id.btn_Fail)).performClick();
			}
			
		})
		.create().show();
	}
	
	private void showRightDialog() {
		new AlertDialog.Builder(this)
		.setMessage(R.string.RightMsg)
		.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						isRightOk = true;
						((Button) findViewById(R.id.btn_Pass)).performClick();
					}

				})
		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				((Button) findViewById(R.id.btn_Fail)).performClick();
			}
			
		})
		.create().show();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_START_TEST:
				startTest();
				break;
			case MSG_SHOW_LEFT_DIALOG:
				showLeftDialog();
				break;
			case MSG_SHOW_RIGHT_DIALOG:
				showRightDialog();
				break;
			default:
				break;
			}
		};
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
					mHandler.sendEmptyMessage(MSG_START_TEST);
			}

		}
	}
	
	
}
