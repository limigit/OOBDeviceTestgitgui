package com.OOBDeviceTest.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.os.Build;

public class LogFileHelper {
	public final static String TAG = "LogFileHelper";

	public final static String DEFAULT_LOG_FILE_PATH = "/data/data/com.OOBDeviceTest/";
	public final static String DEFAULT_LOG_FILE_NAME = "devicetest.oob1";

	public  static String adjustTimeFromServerString = null;
	public static FileWriter mFileWriter = null;
	public static String LogFile = null;


	public LogFileHelper(String logFilePath) {
		try {
			if (logFilePath != null && !logFilePath.trim().equals("")) {
				mFileWriter = new FileWriter(logFilePath, true);
			} else {
				mFileWriter = new FileWriter(DEFAULT_LOG_FILE_PATH
						+ DEFAULT_LOG_FILE_NAME, true);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	synchronized public static void wtireLog(String log) {
		try {
			if (LogFileHelper.LogFile == null) {
				LogFile = DEFAULT_LOG_FILE_PATH + DEFAULT_LOG_FILE_NAME;
			}
			FileWriter mFW = new FileWriter(LogFile, true);
			mFW.write(log);
			mFW.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	synchronized public static void writeLogWithoutClose(String log) {
		try {	
			if (LogFileHelper.LogFile == null) {
				LogFile = DEFAULT_LOG_FILE_PATH + DEFAULT_LOG_FILE_NAME;
			}
			mFileWriter = new FileWriter(LogFile, true);
			mFileWriter.write(log);
			mFileWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Close the File.
	 * 
	 * if you call writeLogWithoutClose ,you need call writeLogClose in pairs.
	 */
	
	synchronized public static void writeLogClose() {
		try {
			if (mFileWriter != null) 
				mFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Func: Generate Log header.
	 */
	public static String getLogHeader(Context c) {
		NativeManger nativeManger = new NativeManger();
		StringBuilder sb = new StringBuilder();
		sb.append("[Local]").append("\n");
		sb.append("Product_Name=").append(SystemInfoUtil.getProductName()).append("\n");
		sb.append("Lenovo_SN=").append(nativeManger.getSN()).append("\n");
		sb.append("EC=").append(SystemInfoUtil.getFormattedECVersion()).append("\n");
		sb.append("MAC=").append(SystemInfoUtil.getLocalMacAddress(c)).append("\n");
		//limi 8.9
		sb.append("UUID=").append(nativeManger.getUID()).append("\n");
		sb.append("BoardID=").append(nativeManger.getBoardId()).append("\n");
		//sb.append(adjustTimeFromServerString).append("\n");
		sb.append("Build_number=").append(SystemInfoUtil.getBuildNumber()).append("\n");
		sb.append("Line_NO=").append("\n");
		return sb.toString();
	}
	
	public static boolean rmLogFileIfExist() {
		File file = new File(LogFile);
		if (file != null && file.isFile() && file.exists()) {
			return file.delete();
		}
		return false;
	}
	
}
