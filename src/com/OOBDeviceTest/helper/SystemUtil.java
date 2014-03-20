package com.OOBDeviceTest.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.rockchip.newton.UserModeManager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Picture;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;

public class SystemUtil {
	private static final String TAG = "SystemUtil";
	public static int execShellCmdForStatue(String command) {
		int status = -1;
		try {
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s = "";
			while((s = bufferedReader.readLine()) != null){
				Log.d(TAG, " >>>> " + s);
			}
			status = p.waitFor();
			Log.d(TAG, " ________________----------- command: " + command + "    status = " + status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;

	}

	public static String execShellCmd(String command) {
		String result = "";
		Log.i("execShellCmd", command);
		try {
			Process process = Runtime.getRuntime().exec(command + "\n");
			DataOutputStream stdin = new DataOutputStream(
					process.getOutputStream());
			DataInputStream stdout = new DataInputStream(
					process.getInputStream());
			DataInputStream stderr = new DataInputStream(
					process.getErrorStream());
			String line;
			while ((line = stdout.readLine()) != null) {
				result += line + "\n";
			}
			if (result.length() > 0) {
				result = result.substring(0, result.length() - 1);
			}
			while ((line = stderr.readLine()) != null) {
				Log.e("EXEC", line);
			}
			process.waitFor();
		} catch (Exception e) {
			e.getMessage();
		}
		return result;
	}

	public static String execRootCmd(String command) {
		int userMode = UserModeManager.getCurrentUserMode();
		UserModeManager.switchToUserMode(UserModeManager.SUPER_USER_MODE);

		String result = execShellCmd("su root " + command + "\n");

		UserModeManager.switchToUserMode(userMode);

		return result;
	}

	public static String execScriptCmd(String command, String path, boolean root) {
		int userMode = UserModeManager.getCurrentUserMode();
		UserModeManager.switchToUserMode(UserModeManager.SUPER_USER_MODE);
		File tempFile = null;
		String result = "";
		Log.i("execScriptCmd", command);
		try {
			tempFile = new File(path);
			tempFile.deleteOnExit();
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(tempFile)));
			br.write("#!/system/bin/sh\n");
			br.write(command);
			br.close();
			SystemUtil.execShellCmd("su root chmod 777 "
					+ tempFile.getAbsolutePath());
			result = SystemUtil.execShellCmd((root ? "su root " : "")
					+ tempFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (tempFile != null && tempFile.exists()) {
				tempFile.delete();
			}
		}
		UserModeManager.switchToUserMode(userMode);
		return result;
	}

	public static boolean killProcessByPath(String exePath) {
		File dir = new File("/proc/");
		String[] files = dir.list();
		int pid = -1;
		for (String path : files) {
			File file = new File("/proc/" + path + "/cmdline");
			if (file.exists()) {
				String cmdline = execShellCmd("cat " + file.getAbsolutePath());
				if (cmdline.startsWith(exePath)) {
					try {
						pid = Integer.parseInt(path);
						break;
					} catch (Exception e) {
						break;
					}
				}
			}
		}

		if (pid >= 0) {
			int userMode = UserModeManager.getCurrentUserMode();
			UserModeManager.switchToUserMode(UserModeManager.SUPER_USER_MODE);
			execShellCmd("su root kill " + pid);
			UserModeManager.switchToUserMode(userMode);
			return true;
		}
		return false;
	}
	
	/**
	 * @param dateAndTime
	 * format : 2012.12.11 21:00:00
	 * android:sharedUserId="android.uid.system"  and signed.
	 */
	public static void SetSystemDateAndTime(String dateAndTime) {
		if (dateAndTime == null && dateAndTime.trim().equals("")) {
			Log.e(TAG, "SetSystemDateAndTime(): dateAndTime "+dateAndTime+" illegal");
		}
		String[] data = dateAndTime.split(" ");
		String[] date = data[0].split("\\.");
		String[] time = data[1].split(":");
		Calendar c = Calendar.getInstance();

		c.set(Integer.valueOf(date[0]),   // year 
				Integer.valueOf(date[1])-1, // month is start from 0
				Integer.valueOf(date[2]), // day
				Integer.valueOf(time[0]), // hour
				Integer.valueOf(time[1]), // minute
				Integer.valueOf(time[2]));// second
		SystemClock.setCurrentTimeMillis(c.getTimeInMillis());
	}
	
	/**
	 * 
	 * @return Time in format yyyy-MM-dd kk-mm-ss
	 */
	public static String getSystemTime() {
		return DateFormat.format("yyyy-MM-dd kk-mm-ss",
				System.currentTimeMillis()).toString();
	}
	
	/**
	 * 鑾峰彇宸插畨瑁卆ppList 鏂规硶銆�
	 */
	public static ArrayList<String> getInstallAppList(Context context) {
		ArrayList<String> items = new ArrayList<String>();
		PackageManager pm = context.getPackageManager();  
		List<PackageInfo> packs = pm.getInstalledPackages(0);
		for(PackageInfo pi:packs){  
            items.add( pi.applicationInfo.loadLabel(pm).toString());  
		}
		return items;
	}
	
	/**
	 * 鑾峰彇璇ヨ矾寰勪笅鎵�湁鐨勬枃浠跺悕瀛�
	 * @param file : dir
	 * @param list 锛歴tore name in this list
	 */
	
	public static void listDir(File file, ArrayList<String> list) {
		if (file == null) return ;
		
		if (file.isFile()) {
			list.add(file.getName());
		} else if (file.isDirectory() && file.listFiles() != null) {
			for (File tfile: file.listFiles()) {
				if (file.isFile()) {
					list.add(file.getName());
				} else {
					listDir(tfile, list);
				}
			}
		}
	}
	
	
	public static boolean isInRecoveryTest() {
		final String RECOVERY_STATE_FILE = "/mnt/sdcard/Recovery_state";
		final String RECOVERY_STATE_FILE_TF = "/mnt/external_sd/Recovery_state";
		boolean result1 = readState(RECOVERY_STATE_FILE);
		boolean result2 = readState(RECOVERY_STATE_FILE_TF);
		
		return result1||result2;
	}
	
	public static boolean isInBoardidSwitchTest() {
		final String BOARD_ID_SWITCH_PATH = "/mnt/external_sd/boardid_test.state";
		boolean result1 = readState(BOARD_ID_SWITCH_PATH);
		return result1;
	}
	
	public static boolean readState(String fileName) {
		int enable = -1;
        File file = new File(fileName);
        if (file == null || !file.exists()) {
        	return false;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
            	String[] temp = tempString.split(":");
            	if (temp.length < 2) {
            		Log.e(TAG, "recovery test state file phase err.");
            		return false;
            	}
            	if (temp[0].equals("enable")) {
            		enable= Integer.valueOf(temp[1]);
            	} 
            }
            reader.close();
            if (enable == 1)
            	return true;
            else
            	return false;
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
		return false;
    }

	
	

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {
			int type = info.getType();
			//only check the ethernet
			if (type == ConnectivityManager.TYPE_ETHERNET) {
				return info.isAvailable();
			}
		}
		return false;
	}
	
}
