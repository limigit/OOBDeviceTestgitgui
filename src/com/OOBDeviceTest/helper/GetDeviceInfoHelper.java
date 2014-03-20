package com.OOBDeviceTest.helper;

import java.io.File;
import java.util.ArrayList;

import com.OOBDeviceTest.CompareServerInfoActivity;

import android.content.Context;
import android.os.Build;
import android.os.Handler;


public class GetDeviceInfoHelper {
	public final static String TAG = "GetDeviceInfoHelper";
	
	public DeviceInfo mInfo = new DeviceInfo();
	private Context mContext;
	private NativeManger mNativeManger;
	private Handler mHandler;
	
	
	public GetDeviceInfoHelper(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		mNativeManger = new NativeManger();
		getSysInfoData();
		
	}
	
	public DeviceInfo getDeviceInfo() {
		return mInfo;
	}
	
	public void getSysInfoData() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				mInfo.SN = mNativeManger.getSN();
			    mInfo.BID = mNativeManger.getBoardId();
				mInfo.EC_VER = SystemInfoUtil.getFormattedECVersion();
				mInfo.PRODUCT = SystemInfoUtil.getProductName();
				mInfo.HDD = SystemInfoUtil.getHDDSize(mContext);
				mInfo.DDR = SystemInfoUtil.GetMemInfo1(mContext);
				mInfo.CPU = SystemInfoUtil.getCpuNameByProp();
				mInfo.LCD = mNativeManger.getLcdId();
				mInfo.CameraID = SystemInfoUtil.getCameraId();
				
				mInfo.TouchPanel = SystemInfoUtil.getTouchpanelId();
				mInfo.BATTERY_CELL = SystemInfoUtil.getBatteryCell();
				mInfo.TOUCHPAD_ID = SystemInfoUtil.getTouchpadId();
				mInfo.WLAN_ID = SystemInfoUtil.getWlanId(mContext);
				mInfo.Build_number=SystemInfoUtil.getBuildNumber();
				mHandler.sendEmptyMessageAtTime(CompareServerInfoActivity.MSG_GET_CLIENT_INFO_FINISH, 4000);
			}
		}).start();
	    
		/*
		mInfo.CPU = SystemInfoUtil.getCpuName() + ":"
				+ SystemInfoUtil.getNumCores() + "*"
				+ SystemInfoUtil.getMaxCpuFreq() + "Hz";
		*/	
	}
}


