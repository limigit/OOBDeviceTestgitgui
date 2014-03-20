package com.OOBDeviceTest.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.PrivateCredentialPermission;

import android.os.storage.IMountService;

import android.R.integer;
import android.R.string;
import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.IECService;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.text.format.Formatter;
import android.util.Log;

/*
 * TODO锛氳幏鍙栫郴缁熺浉鍏充俊鎭� * 
 * Data锛�013-04-24
 */

public class SystemInfoUtil {
	private static final String TAG = "SystemInfoUtil";

	// 鑾峰彇sdcard 鐨勫ぇ灏�	
	private static void getSdcardStorageSize() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			long blockSize = sf.getBlockSize();
			long blockCount = sf.getBlockCount();
			long availCount = sf.getAvailableBlocks();
			Log.d("cghs", "block澶у皬:" + blockSize + ",block鏁扮洰:" + blockCount
					+ ",鎬诲ぇ灏�" + blockSize * blockCount / 1024 + "KB");
			Log.d("cghs", "鍙敤鐨刡lock鏁扮洰锛�" + availCount + ",鍓╀綑绌洪棿:" + availCount
					* blockSize / 1024 + "KB");
		}
	}

	// 鑾峰彇绯荤粺鍒嗗尯鐨勫ぇ灏�	
	private static void getSystemStorageSize() {
		File root = Environment.getRootDirectory();
		StatFs sf = new StatFs(root.getPath());
		long blockSize = sf.getBlockSize();
		long blockCount = sf.getBlockCount();
		long availCount = sf.getAvailableBlocks();
		Log.d("cghs", "block澶у皬:" + blockSize + ",block鏁扮洰:" + blockCount
				+ ",鎬诲ぇ灏�" + blockSize * blockCount / 1024 + "KB");
		Log.d("cghs", "鍙敤鐨刡lock鏁扮洰锛�" + availCount + ",鍙敤澶у皬:" + availCount
				* blockSize / 1024 + "KB");
	}

	// 鑾峰緱鍐呭瓨淇℃伅 鏂规硶1
	public static String GetMemInfo1(Context mContext) {
		long MEM_UNUSED;
		long MEM_TOTAL;
		// 寰楀埌ActivityManager
		ActivityManager am = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		// 鍒涘缓ActivityManager.MemoryInfo瀵硅薄
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);

		// 鍙栧緱鍓╀綑鐨勫唴瀛樼┖闂�		
		MEM_UNUSED = mi.availMem / (1024*1024);
		MEM_TOTAL = mi.totalMem / (1024*1024);
		Log.d("cghs", "MEN_UNUSER: " + MEM_UNUSED + " MEM_TOTAL:" + MEM_TOTAL);
		String memStr = MEM_TOTAL + " MB";
		return memStr;

	}

	// 鑾峰緱鍐呭瓨淇℃伅 鏂规硶2
	public static void GetMemInfo2() {
		List<String> contents = new ArrayList<String>();
		long mTotal;
		// /proc/meminfo璇诲嚭鐨勫唴鏍镐俊鎭繘琛岃В閲�		
		String path = "/proc/meminfo";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path), 8);
			String line;
			if ((line = br.readLine()) != null) {
				contents.add(line);
			}

			if ((line = br.readLine()) != null) {
				contents.add(line);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		String total = contents.get(0);
		String rest = contents.get(1);
		// beginIndex
		int begin = total.indexOf(':');
		// endIndex
		int end = total.indexOf('k');
		// 鎴彇瀛楃涓蹭俊鎭�
		total = total.substring(begin + 1, end).trim();
		mTotal = Integer.parseInt(total);
		Log.d("cghs", "mTotal: " + mTotal);
		Log.d("cghs", "" + rest);
	}

	// -------------------------CPU INFO-------------------------

	// 鑾峰彇CPU鍚嶅瓧
	public static String getCpuName() {
		try {
			FileReader fr = new FileReader("/proc/cpuinfo");
			BufferedReader br = new BufferedReader(fr);
			String text = br.readLine();
			String[] array = text.split(":\\s+", 2);
			for (int i = 0; i < array.length; i++) {
			}
			return array[1];
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getCpuNameByProp() {
		String name = null;
		name = SystemProperties.get("ro.rk.cpu", "rk3188");
		return name;
	}

	// 鑾峰彇CPU鏈�ぇ棰戠巼锛堝崟浣岾HZ锛�	// "/system/bin/cat" 鍛戒护琛�	// "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" 瀛樺偍鏈�ぇ棰戠巼鐨勬枃浠剁殑璺緞
	public static String getMaxCpuFreq() {
		String result = "";
		ProcessBuilder cmd;
		try {
			String[] args = { "/system/bin/cat",
					"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
			cmd = new ProcessBuilder(args);
			Process process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[24];
			while (in.read(re) != -1) {
				result = result + new String(re);
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			result = "N/A";
		}
		return result.trim();
	}

	// 鑾峰彇CPU鏈�皬棰戠巼锛堝崟浣岾HZ锛�	
	public static String getMinCpuFreq() {
		String result = "";
		ProcessBuilder cmd;
		try {
			String[] args = { "/system/bin/cat",
					"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq" };
			cmd = new ProcessBuilder(args);
			Process process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[24];
			while (in.read(re) != -1) {
				result = result + new String(re);
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			result = "N/A";
		}
		return result.trim();
	}

	// 瀹炴椂鑾峰彇CPU褰撳墠棰戠巼锛堝崟浣岾HZ锛�	
	public static String getCurCpuFreq() {
		String result = "N/A";
		try {
			FileReader fr = new FileReader(
					"/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
			BufferedReader br = new BufferedReader(fr);
			String text = br.readLine();
			result = text.trim();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	// 鑾峰彇 cpu 鏍告暟
	public static int getNumCores() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			e.printStackTrace();
			// Default to return 1 core
			return 1;
		}
	}

	/**
	 * 鑾峰彇 HDD 澶у皬
	 * 
	 */
	public static String getHDDSize(Context context) {
		int trytime = 0;
		String flash_path = "/mnt/sdcard";
		while(!getSdCardState(flash_path).equals(Environment.MEDIA_MOUNTED)) {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				
			}
			trytime ++;
			if (trytime >= 8) {
				Log.e(TAG, "getHDDSize(): flash unmount");
				return "err!";
			}
		}
		
		
		StatFs stat = new StatFs(flash_path);
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		long availableBlocks = stat.getAvailableBlocks();
		float size = (blockSize * totalBlocks)/(1024*1024);
		float sizeInGB = size / 1024;
		if (sizeInGB > 11.00f && sizeInGB < 20.00f) {
			return "16.00 GB";
		} else if (sizeInGB > 20.00f) {
			return "32.00 GB";
		}
		return Formatter.formatFileSize(context, blockSize * totalBlocks);

	}
	
	/**
	 * 鑾峰彇褰撳墠璺緞涓嬬‖鐩樻槸鍚︽寕杞姐�
	 */
	public static String getSdCardState(String path) {
        try {
        	IMountService mMntSvc = null;
            if (mMntSvc == null) {
                mMntSvc = IMountService.Stub.asInterface(ServiceManager
                                                         .getService("mount"));
            }
            return mMntSvc.getVolumeState(path);
        } catch (Exception rex) {
            return Environment.MEDIA_REMOVED;
        }

    }

	/*
	 * 鑾峰彇wifi MAC need permission: <uses-permission
	 * android:name="android.permission.ACCESS_WIFI_STATE"/> <uses-permission
	 * android:name="android.permission.INTERNET"/>
	 */
	public static String getLocalMacAddress(Context context) {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		String mac ;
		int time=0;
		if(!wifi.isWifiEnabled()){
			wifi.setWifiEnabled(true);
		}
		WifiInfo info = wifi.getConnectionInfo();
		 mac =  info.getMacAddress();
		wifi.setWifiEnabled(false);
		return mac;

	}

	public static String getAndroidVersion() {
		return Build.VERSION.RELEASE;
	}

	/**
	 * 鑾峰彇kernel 鐗堟湰
	 */
	public static String getFormattedKernelVersion() {
		String procVersionStr;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"/proc/version"), 256);
			try {
				procVersionStr = reader.readLine();
			} finally {
				reader.close();
			}

			final String PROC_VERSION_REGEX = "\\w+\\s+" + /* ignore: Linux */
			"\\w+\\s+" + /* ignore: version */
			"([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
			"\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /*
														 * group 2:
														 * (xxxxxx@xxxxx
														 * .constant)
														 */
			"\\((?:[^(]*\\([^)]*\\))?[^)]*\\)\\s+" + /* ignore: (gcc ..) */
			"([^\\s]+)\\s+" + /* group 3: #26 */
			"(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
			"(.+)"; /* group 4: date */

			Pattern p = Pattern.compile(PROC_VERSION_REGEX);
			Matcher m = p.matcher(procVersionStr);

			if (!m.matches()) {

				return "Unavailable";
			} else if (m.groupCount() < 4) {

				return "Unavailable";
			} else {
				return (new StringBuilder(m.group(1)).append("\n")
						.append(m.group(2)).append(" ").append(m.group(3))
						.append("\n").append(m.group(4))).toString();
			}
		} catch (IOException e) {

			return "Unavailable";
		}
	}

	/**
	 * 鑾峰彇product 鍚嶅瓧
	 */
	public static String getProductName() {
		return Build.PRODUCT;
	}

	public static String getFormattedECVersion() {
		IECService ecService;
		byte[] vv = new byte[2];
		int ecVersion;
		try {
			ecService = IECService.Stub.asInterface(ServiceManager
					.getService("ECServiceinfo"));
			if (ecService == null) {
				Log.e(TAG, "Unable to create the ECService instance!");
				return "Unavailable";
			}

			ecVersion = ecService.getVersion();

			vv[0] = (byte) (ecVersion & 0x000000FF);
			vv[1] = (byte) ((ecVersion >> 8) & 0x000000FF);

			return String.format("%02x %02x", vv[1], vv[0]);

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			Log.e(TAG,
					"IO Exception when getting ec version for Device Info screen",
					e);
			return "Unavailable";
		}
	}

	public static String getCameraId() {
		String pidcmd = "/system/bin/cat /sys/bus/usb/devices/3-1/idProduct";
		String vidcmd = "/system/bin/cat /sys/bus/usb/devices/3-1/idVendor";
		String pid = runCmd(pidcmd);
		String vid = runCmd(vidcmd);
		return pid+"&"+vid;
	
	}

	public static String getWlanId(Context c) {
		int trytime = 0;
		WifiManager mWifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
		mWifiManager.setWifiEnabled(true);
		while (!mWifiManager.isWifiEnabled()) {
			try {
				Thread.sleep(500);
				trytime ++;
				if (trytime >= 8) {
					Log.e(TAG, "getWlanId(): open wifi timeout!");
					mWifiManager.setWifiEnabled(false);
					return "err!";
				}
			} catch (Exception e) {
			}
		}
		String id = runCmd("/system/bin/iwpriv wlan0 read 1,fc");
//		mWifiManager.setWifiEnabled(false);
		if (id == null) return "err!";
		String[] ids = id.split(":");
		if (ids.length > 1) {
			return ids[1];
		}
		return "err!";
		
	}

	  public static String getTouchpadId(){
	    	String touchpadId=null;	
	    	try {
	    	    IECService ecService;
		    	ecService=IECService.Stub.asInterface(ServiceManager.getService("ECServiceinfo"));
				if(ecService==null){
					Log.e(TAG,"Unable to create the ECService instance!");
					return "Unavailable";
				}
				int padid=ecService.DeviceCtrl(16,0);
		        if(padid==0)touchpadId="Elan";
		        else{
		        	if(padid==1)touchpadId="Synaptics";
		        	else touchpadId="Unknow";
		        }
	       }catch (RemoteException e) {
				// TODO Auto-generated catch block
	    	   Log.e(TAG,"IO Exception when getting touchpadId  ",e);  		
	    	   return "Unavailable";
			}
			return touchpadId;	
		}

	  public static String getBatteryCell(){
	    String batteryCell=null;
	    	try {
	    	    IECService ecService;
		    	ecService=IECService.Stub.asInterface(ServiceManager.getService("ECServiceinfo"));
				if(ecService==null){
					Log.e(TAG,"Unable to create the ECService instance!");
					return "Unavailable";
				}
				int batid=ecService.DeviceCtrl(17,0);
		        if(batid==0)batteryCell="Other";
		        else{
		        	if(batid==1)batteryCell="Simplo";
		        	else batteryCell="Unknow";
		        }
	       }catch (RemoteException e) {
				// TODO Auto-generated catch block
	    	   Log.e(TAG,"IO Exception when getting batteryCell  ",e);  		
	    	   return "Unavailable";
			}
			return batteryCell;
		}
	    
	   public static String getTouchpanelId(){
	        String touchpanelId=null;
	        try {
	     	    IECService ecService;
	 	    	ecService=IECService.Stub.asInterface(ServiceManager.getService("ECServiceinfo"));
	 			if(ecService==null){
	 				Log.e(TAG,"Unable to create the ECService instance!");
	 				return "Unavailable";
	 			}
	 			int padid=ecService.DeviceCtrl(18,0);
	 	        if(padid==0)touchpanelId="Ofilm";
	 	        else{
	 	        	if(padid==1)touchpanelId="Mutto";
	 	        	else touchpanelId="Unknow";
	 	        }
	        } catch (RemoteException e) {
	 			// TODO Auto-generated catch block
	     	   Log.e(TAG,"IO Exception when getting Touchpanel id ",e);  		
	     	   return "Unavailable";
	 		}
	 		return touchpanelId;	
	 	}
	
	public static String runCmd(String cmd) {
		Process process = null;
		InputStreamReader mISR;
        StringBuilder sBuilder = new StringBuilder();
        String temp;
		try {
			process = Runtime.getRuntime().exec(cmd);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            while ((temp = reader.readLine()) != null) {
//            	Log.e("cghs", "temp:"+temp);
//            }
			temp = reader.readLine();
            process.destroy();
            reader.close();
			return temp;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static String getBuildNumber() {
	
		String  mBuildNum=null;
		String  Build_number=null;
		mBuildNum=Build.DISPLAY;
		int key=mBuildNum.indexOf("A10_S");	
		Build_number=mBuildNum.substring(key, key+15);
		return Build_number;
	}

}
