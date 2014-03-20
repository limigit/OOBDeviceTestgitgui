package com.OOBDeviceTest.helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import com.OOBDeviceTest.DeviceTest;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

public class LogSenderHelperByCISF {
	private static final String TAG = "LogSenderHelperByCISF";
	private static final String CIFS_CONFIG = "cifs.config";
//	private static final String CIFS_CONFIG = "sw_cifs.config";
    private SmbFile mSmbFile;
	private FileInputStream in;
	private SmbFileOutputStream out;
	private String mUser;
	private String mPwd;
	private String mHost;
	private String mLocalPath = "/data/data/com.OOBDeviceTest/";
	private String mServerPath;
	private String mLocalFilePath;
	private String mServerFilePath;
	private Handler mHandler;

	public LogSenderHelperByCISF(Context c, Handler handler) {
		mHandler = handler;
		getCIFSConfig(c);
		NativeManger nm = new NativeManger();
		mLocalFilePath = (mLocalPath + nm.getSN() + ".oob1");
		mServerFilePath = ("smb://" + mUser + ":" + mPwd + "@"
				+ mHost + mServerPath + nm.getSN() + ".oob1");
		Log.e("LogSenderHelperByCISF", mServerFilePath);
	}

	public void sendLog() {
		new Thread(new Runnable() {
			public void run() {
				try {
					mSmbFile = new SmbFile(mServerFilePath);
					mSmbFile.createNewFile();
					Log.v("LogSenderHelperByCISF", "connect");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (SmbException e) {
					e.printStackTrace();
				}

				File file = new File(mLocalFilePath);
				byte[] buffer = new byte[1024];
				try {
					out = new SmbFileOutputStream(mSmbFile);
					in = new FileInputStream(file);
					while (in.read(buffer) != -1) {
						out.write(buffer);
						buffer = new byte[1024];
					}
					in.close();
					out.close();
					mHandler.sendEmptyMessage(DeviceTest.MSG_SEND_LOG_SUCESS);
				} catch (SmbException e) {
					mHandler.sendEmptyMessage(DeviceTest.MSG_SEND_LOG_FAIL);
					e.printStackTrace();
				} catch (MalformedURLException e) {
					mHandler.sendEmptyMessage(DeviceTest.MSG_SEND_LOG_FAIL);
					e.printStackTrace();
				} catch (UnknownHostException e) {
					mHandler.sendEmptyMessage(DeviceTest.MSG_SEND_LOG_FAIL);
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					mHandler.sendEmptyMessage(DeviceTest.MSG_SEND_LOG_FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					mHandler.sendEmptyMessage(DeviceTest.MSG_SEND_LOG_FAIL);
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void getCIFSConfig(Context c) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(c.getAssets().open(
					CIFS_CONFIG)));
			String temp;
			while ((temp = br.readLine()) != null) {
				String[] data = temp.split(":");
				if (data[0].trim().equals("loghost"))
					mHost = data[1].trim();
				else if (data[0].trim().equals("user"))
					mUser = data[1].trim();
				else if(data[0].trim().equals("pwd"))
					mPwd = data[1].trim();
				else if (data[0].trim().equals("log_path_s")) 
					mServerPath = data[1].trim();
				else if (data[0].trim().equals("local_path")) 
					mLocalPath = data[1].trim();
			}

			if (br != null)
				br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
