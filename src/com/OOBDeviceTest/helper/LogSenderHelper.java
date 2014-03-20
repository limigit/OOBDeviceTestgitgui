package com.OOBDeviceTest.helper;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.OOBDeviceTest.DeviceTest;

public class LogSenderHelper {
	private final static String TAG = "LogSenderHelper";
	
	private final String FTP_CONFIG_PATH = "ftp.config";

	FTPClient mFTPClient;
	
	private String mFTPHost;// = "172.16.9.86";
	private int mFTPPort;// = 21;
	private String mFTPUser;// = "test";
	private String mFTPPassword;// = "1";
	
	private Handler mHandler;
	
	public LogSenderHelper(Context context ,Handler handler) {
		mFTPClient = new FTPClient();
		mHandler = handler;
		getFtpConfig(context);
	}
	
	public void getFtpConfig(Context c) {
		String temp;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(c.getAssets().open(FTP_CONFIG_PATH)));

			while ((temp = br.readLine()) != null) {
				String[] data = temp.split(":");
				if (data[0].trim().equals("host")) {
					mFTPHost = data[1].trim();
				} else if (data[0].trim().equals("port")) {
					mFTPPort = Integer.valueOf(data[1].trim());
				} else if (data[0].trim().equals("user")) {
					mFTPUser = data[1].trim();
				} else if (data[0].trim().equals("pwd")) {
					mFTPPassword = data[1].trim();
				}
			}
			
			if (br != null) {
				br.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void connectFTP() {
		Thread thread = new Thread(new CmdConnect());
		thread.start();
	}
	
	public void sendLogToServer(String path) {
		if (mFTPClient != null && mFTPClient.isConnected()) {
			new CmdUpload().execute(path);
		}
	}

	public void disconnectFTP() {
		Thread thread = new Thread(new CmdDisConnect());
		thread.start();
	}
	
	/*
	 * Function ： 用于FTP 连接登陆
	 */
	
	public class CmdConnect extends FtpCmd {
		@Override
		public void run() {
			try {
				String[] welcome = mFTPClient.connect(mFTPHost, mFTPPort);
				if (welcome != null) {
					for (String value : welcome) {
						Log.e(TAG, "connect " + value);
					}
				}
				mFTPClient.login(mFTPUser, mFTPPassword);
				Log.e(TAG, "Login Sucess!");
				mHandler.sendEmptyMessage(DeviceTest.MSG_LOGIN_SUCESS);
			} catch (IllegalStateException illegalEx) {
				mHandler.sendEmptyMessage(DeviceTest.MSG_LOGIN_FAIL);
				illegalEx.printStackTrace();
			} catch (IOException ex) {
				mHandler.sendEmptyMessage(DeviceTest.MSG_LOGIN_FAIL);
				ex.printStackTrace();
			} catch (FTPIllegalReplyException e) {
				mHandler.sendEmptyMessage(DeviceTest.MSG_LOGIN_FAIL);
				e.printStackTrace();
			} catch (FTPException e) {
				mHandler.sendEmptyMessage(DeviceTest.MSG_LOGIN_FAIL);
				e.printStackTrace();
			}
		}
	}
	
	
	/*
	 * Function: 上传指定文件
	 */
	public class CmdUpload extends AsyncTask<String, Integer, Boolean> {
		String path;

		public CmdUpload() {

		}

		@Override
		protected Boolean doInBackground(String... params) {
			path = params[0];
			try {
				File file = new File(path);
				mFTPClient.upload(file, new DownloadFTPDataTransferListener(
						file.length()));
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}

			return true;
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Boolean result) {
			Log.e(TAG, "" + (result ? path + "上传成功" : "上传失败"));
			mHandler.sendEmptyMessage(result ? DeviceTest.MSG_SEND_LOG_SUCESS
					: DeviceTest.MSG_SEND_LOG_FAIL);
			//上传完毕， 断开链接
			disconnectFTP();
			
		}
	}

	/*
	 * Function: 断开连接
	 */
	public class CmdDisConnect extends FtpCmd {

		@Override
		public void run() {
			if (mFTPClient != null) {
				try {
					mFTPClient.disconnect(true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	private class DownloadFTPDataTransferListener implements
			FTPDataTransferListener {

		private int totolTransferred = 0;
		private long fileSize = -1;

		public DownloadFTPDataTransferListener(long fileSize) {
			if (fileSize <= 0) {
				throw new RuntimeException(
						"the size of file muset be larger than zero.");
			}
			this.fileSize = fileSize;
		}

		@Override
		public void aborted() {
		}

		@Override
		public void completed() {
		}

		@Override
		public void failed() {
		}

		@Override
		public void started() {
		}

		@Override
		public void transferred(int length) {
			// totolTransferred += length;
			// float percent = (float) totolTransferred / this.fileSize;
			// setLoadProgress((int) (percent * mPbLoad.getMax()));
		}
	}
	
	public abstract class FtpCmd implements Runnable {
		public abstract void run();

	}


}
