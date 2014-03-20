package com.OOBDeviceTest.StressTest;

import com.OOBDeviceTest.R;
import com.OOBDeviceTest.helper.LogFileHelper;
import com.OOBDeviceTest.helper.SystemUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoPlayActivity extends Activity{
	private VideoView mVideoView;
	private String mPath = null;
	private int mTime; //hours
	private int mAutoTestFlag = 0;
	private WakeLock mWakeLock;
	private AlertDialog mDialog;
	private final static int MSG_SHOW_DIALOG = 0;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		mWakeLock = ((PowerManager)getSystemService("power")).newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "videotest");
		mWakeLock.acquire();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
	                WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		setContentView(R.layout.activity_video_play);
		
		init();
		LogFileHelper.writeLogWithoutClose("["+SystemUtil.getSystemTime()+"]"+" :VIDEO TEST START.\n");
	
		playVideo();
		
		registerReceiver(ACDCDetectedReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(ACDCDetectedReceiver);
	}
	
	private void init() {
		mPath = getIntent().getStringExtra("path");
		mAutoTestFlag = getIntent().getIntExtra("auto", 0);
		if (mAutoTestFlag != 0) {
			mTime = getIntent().getIntExtra("time", 0);
			mHandler.sendEmptyMessageDelayed(1, mTime*60*60*1000);
		}
		
		mVideoView = (VideoView) this.findViewById(R.id.video_view);
		Button stopBtn = (Button) findViewById(R.id.stop_btn);
		stopBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				stopVideo();
			}
		});
		
	}
	
	private void playVideo() {
		if (mPath == null) {
			Toast.makeText(this, R.string.error_video, Toast.LENGTH_LONG).show();
			return;
		}
		LogFileHelper.writeLogWithoutClose("["+SystemUtil.getSystemTime()+"]"+" :PLAY VIDEO ="+mPath+".\n");
        MediaController mc = new MediaController(this);
        mVideoView.setMediaController(mc);
        //videoView.setVideoURI(Uri.parse(""));
        mVideoView.setVideoPath(mPath);
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				mVideoView.start();
			}
		});
        mVideoView.requestFocus();
        mVideoView.start();
	}
	
	private void stopVideo() {
		mVideoView.stopPlayback();
		mWakeLock.release();
		LogFileHelper.writeLogWithoutClose("["+SystemUtil.getSystemTime()+"]"+" :VIDEO TEST FINISH.\n");
		finish();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_SHOW_DIALOG:
				ShowACDialog();
				break;
			case 1:
				if(mAutoTestFlag == 1)
					stopVideo();
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
					//AC NOT IN
					//topVideo(); //LIMI 8.15
					mHandler.sendEmptyMessage(MSG_SHOW_DIALOG);	
				}
			}
		}
	};
}
