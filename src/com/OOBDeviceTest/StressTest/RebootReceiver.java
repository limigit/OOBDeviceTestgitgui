package com.OOBDeviceTest.StressTest;

import com.OOBDeviceTest.DeviceTest;
import com.OOBDeviceTest.helper.LogFileHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.content.SharedPreferences;

public class RebootReceiver extends BroadcastReceiver {
	private SharedPreferences mSharedPreferences;
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			mSharedPreferences = context.getSharedPreferences("state", 0);
			int rebootFlag = mSharedPreferences.getInt("reboot_flag", 0);
		//	int rebootFlag2 = mSharedPreferences.getInt("reboot_flag2", 0);
			int rebootCount= mSharedPreferences.getInt("reboot_count", 0);
			int maxtime= mSharedPreferences.getInt("reboot_max", 0);
			int autoFlag = mSharedPreferences.getInt("auto", 0);
			Log.e("cghs", "autoFlag:"+autoFlag+"  maxtime :"+maxtime+" rebootcount:"+ rebootCount);
			if (rebootFlag == 0) {
				//not 
			} else {
				if (autoFlag == 1 && rebootCount >= maxtime) {
					 SharedPreferences mSharedPreferences = context.getSharedPreferences("state", 0);
					SharedPreferences.Editor edit = mSharedPreferences.edit();
					edit.putInt("reboot_flag2", 0);				
					edit.putInt("second_start", 1);
				//	edit.putInt("finish", 1);
					edit.commit();
					
					Intent  DeviceIntent = new Intent(context, DeviceTest.class);
					DeviceIntent.putExtra("finish", 1);
					DeviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(DeviceIntent);
				} else {
					Intent pintent = new Intent(context, RebootTest.class);
					pintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(pintent);
				} 
				
			}
		}
	}

}
