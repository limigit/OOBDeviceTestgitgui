package com.OOBDeviceTest.helper;

import android.R.integer;


public class NativeManger {

	public NativeManger() {
		init();
	}
	
	static {
		try {
			System.loadLibrary("rkinfoOOB");
		} catch (Exception e) {

		}
	}
	
	public native void init();
	public native String getMAC();
	public native String getSN();
	public native String getBoardId();
	public native String getIMEI();
	public native String getUID();
	public native String getBT();
	
	public native String getLcdId();
	
	public native int gSensorStore(int x, int y, int z);
	public native int[] gSensorLoad();
	public native int gSensorCabiration(int[] array);
	
	
	
	
	
}
