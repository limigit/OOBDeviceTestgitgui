package com.OOBDeviceTest.helper;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class DeviceInfo {
	public final static String TAG = "DeviceInfo";
	// TITLE
	public final static String TESTINFO = "Test Information";
	
	// AREA FLAG
	public final static int TESTINFO_AREA = 0;
	
	// [TESTINFO]
	
	public String SN;
	public String EC_VER;
	public String PRODUCT;
	public String HDD;
	public String BID;
	public String DDR;
	public String CPU;
	
	//[null]
	//8.7 LIMI
    public String CameraID;
    public String TouchPanel;
    public String LCD;
  
    public String WLAN_ID;
    public String BATTERY_CELL;
    public String TOUCHPAD_ID;
    public String FIRMWARE_VER;
    public String PRODUCT_VER;
    
    public String COUNTRY_KEY;
    public String Runin_CY;
    public String Build_number;
    public String HDMI;
   


	public DeviceInfo() {

	}

	public void printData() {
		
		Log.i(TAG, EC_VER + " " + PRODUCT + " " + HDD + " " + BID + " " + DDR+ " " + CPU + " " +CameraID + " "
				+ TouchPanel + " " + WLAN_ID+" "+LCD+" "+BATTERY_CELL+" "+TOUCHPAD_ID+" "+Build_number+"");
	
	}

}
