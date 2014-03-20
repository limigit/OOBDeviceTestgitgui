package com.OOBDeviceTest.StressTest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;

public class CheckDCACService extends Service {

	private IntentFilter mIntentFilter;
	private String AcstatusString = "";
	private boolean mIsAcIn = false;;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(mIntentReceiver, mIntentFilter);
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mIntentReceiver);
	}

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				int plugType = intent.getIntExtra("plugged", 0);
				Intent dischargeIntent = new Intent("android.intent.action.AC_STATE");
				if (plugType > 0) {
					mIsAcIn = true;
				} else {
					mIsAcIn = false;
				}
				dischargeIntent.putExtra("state", mIsAcIn);
				context.sendBroadcast(dischargeIntent);
			}
		}
	};

}
