package com.OOBDeviceTest.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class PhaseServerFileHelper {
	public final static String TAG = "PhaseServerFileHelper";
	
    private DeviceInfo mInfo = new DeviceInfo();
    private int mDataAreaFlag = -1;
    
    public PhaseServerFileHelper() {
    	
    }
    
    /**
     * @param fileName 
     * @param skipline : skip the line if some file have unused header.
     * 
     */
    public DeviceInfo ReadIniFileAndPhase(String filepath ,int skipline) {
        File file = new File(filepath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            //skip log header
            while (skipline > 0) {
            	tempString = reader.readLine();
            	skipline--;
            	Log.e(TAG, "skipline:"+skipline+" :"+tempString);
            }
            
            while ((tempString = reader.readLine()) != null) {
            	if (tempString.trim().equals("")) continue;
            	if (tempString.charAt(0) == '[') {
            		int startPos = tempString.indexOf("[");
            		int endPos = tempString.indexOf("]");
            		String title = tempString.subSequence(startPos+1, endPos).toString();
            		phaseTitleLine(title);
            	} else {
            		phaseContentLine(tempString);
            	}
            	
            }
            reader.close();
            return mInfo;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
        
    }

   
    private void phaseTitleLine(String title) {
    	if (title.equals(DeviceInfo.TESTINFO)) {
    		mDataAreaFlag = DeviceInfo.TESTINFO_AREA;
    	} else {
    		Log.e(TAG, "Unknown title : "+title+"  mDataAreaFlag:"+mDataAreaFlag);
    	}
    }
    
    private void phaseContentLine(String line) {
    	switch (mDataAreaFlag) {
		case DeviceInfo.TESTINFO_AREA:
			phaseSysinfo(line);
			break;
		default:
			break;
		}
    }
    
  
    private void phaseSysinfo(String line) {
    	String[] content = line.split("=");
    	if (content[0].equals("EC_VER")) {
    		mInfo.EC_VER = content[1].trim();
    	} else if (content[0].equals("PRODUCT")) {
    		mInfo.PRODUCT = content[1].trim();
    	} else if (content[0].equals("HDD")) {
    		mInfo.HDD = content[1].trim();
    	} else if (content[0].equals("BID")) {
    		mInfo.BID = content[1].trim();
    	} else if (content[0].equals("DDR")) {
    		mInfo.DDR = content[1].trim();
    	} else if (content[0].equals("CPU")) {
    		mInfo.CPU = content[1].trim();
    	} else if (content[0].equals("CameraID")) {
    		mInfo.CameraID = content[1].trim();
    	} else if (content[0].equals("TouchPanel")) {
    		mInfo.TouchPanel = content[1].trim();
    	} else if (content[0].equals("WLAN_ID")) {
    		mInfo.WLAN_ID = content[1].trim();
    	} else if (content[0].equals("LCD")) {
    		mInfo.LCD = content[1].trim();
    	} else if (content[0].equals("BATTERY_CELL")) {
    		mInfo.BATTERY_CELL = content[1].trim();
    	} else if (content[0].equals("TOUCHPAD_ID")) {
    		mInfo.TOUCHPAD_ID = content[1].trim();
    	} else if (content[0].equals("COUNTRY_KEY")) {
    		mInfo.COUNTRY_KEY = content[1].trim();
    	}else if (content[0].equals("Runin_CY")) {
    		mInfo.Runin_CY = content[1].trim();
    	}else if (content[0].equals("Build_number")) {
    		mInfo.Build_number = content[1].trim();
    	}else if (content[0].equals("HDMI")) {
    		mInfo.HDMI = content[1].trim();
    	}else {
    		Log.e(TAG, "UnKnown Sysinfo:"+line);
    	}
    }
   
   
	
}

