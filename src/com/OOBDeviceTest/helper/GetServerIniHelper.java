package com.OOBDeviceTest.helper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;

import com.OOBDeviceTest.CompareServerInfoActivity;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class GetServerIniHelper {
	public static final String TAG = "GetServerIniHelper";
	public static final String CIFS_CONFIG = "cifs.config";
	
	private Context mContext;
	private File mSNFile;
	private String mSNFilePath;
	
	private Handler mHandler;
	private NativeManger mNativeManger;
	private boolean mIsIniDown = false;

	public CIFSData mCIFSData;

	public GetServerIniHelper(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		
		mCIFSData = new CIFSData();
		mCIFSData.getCIFSConfig(context);
		mNativeManger = new NativeManger();

		sendSnToServer();
		getIniFromServer();

	}
	
	public void sendSnToServer() {
		WriteSnFlgFile();
		if (mSNFile.exists()) {
			Log.v(TAG, "SNfile exists");
			mHandler.sendEmptyMessage(CompareServerInfoActivity.MSG_GET_INI_START);
			putSN();
		} else {
			//need err
			Log.v(TAG, "SNfile no exists");
		}
	}
	
	public void getIniFromServer() {
//		TimerTask task = new TimerTask() {
//			public void run() {
//				if (!mIsIniDown)
//					getINI();
//			}
//		};
//		Timer timer = new Timer();
//		timer.schedule(task, 2000);
		getINI();
	}
	
	public void restart() {
		sendSnToServer();
		getIniFromServer();
	}

	public void putSN() {
		Thread thread = new Thread(new smbPut(mCIFSData.getSnServerStr(), mSNFilePath));
		thread.start();
	}

	public void getINI() {
		Thread thread = new Thread(new smbGet(mCIFSData.getIniServerStr(),
				mCIFSData.getLocalStr() + mNativeManger.getSN() + ".INI"));
		thread.start();
	}

	public void WriteSnFlgFile() {
		FileOutputStream out;

		mSNFilePath = mCIFSData.snLocalPath + mNativeManger.getSN() + ".flg";
		mSNFile = new File(mSNFilePath);
		try {
			mSNFile.createNewFile();
			out = new FileOutputStream(mSNFile);
			out.write("pretest pici request".getBytes());
			Log.v(TAG, "write SN.flg file success");
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class smbPut extends ConnectCmd {
		String remoteUrl;
		String localFilePath;

		public smbPut(String remoteUrl, String localFilePath) {
			this.remoteUrl = remoteUrl;
			this.localFilePath = localFilePath;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			InputStream in = null;
			OutputStream out = null;
			try {
				File localFile = new File(localFilePath);
				// String fileName = localFile.getName();
				SmbFile remoteFile = new SmbFile(remoteUrl);
				in = new BufferedInputStream(new FileInputStream(localFile));
				out = new SmbFileOutputStream(remoteFile);
				byte[] buffer = new byte[1024];
				while ((in.read(buffer)) != -1) {
					out.write(buffer);
					buffer = new byte[1024];
				}
				Log.v(TAG, "SNfile send success");
			} catch (SmbException smb) {
				mHandler.sendEmptyMessage(CompareServerInfoActivity.MSG_NETWORK_ERR);
				smb.printStackTrace();
			} catch (Exception e) {
				mHandler.sendEmptyMessage(CompareServerInfoActivity.MSG_UNKNOWN_ERR);
				e.printStackTrace();
			} finally {
				try {
					if (out != null)
						out.close();
					if (in != null)
						in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class smbGet extends ConnectCmd {
		String remoteUrl;
		String localDir;

		public smbGet(String remoteUrl, String localDir) {
			this.remoteUrl = remoteUrl;
			this.localDir = localDir;
		}

		@Override
		public void run() {
			InputStream in = null;
			FileOutputStream out = null;
			SmbFile smbFile = null;
			try {
				smbFile = new SmbFile(remoteUrl);
				int time = 0;
				while (!smbFile.exists()) {
					Thread.sleep(1000);
					time++;
					if (time >= 10) {
						mHandler.sendEmptyMessage(CompareServerInfoActivity.MSG_GET_INI_TIME_OUT);
						return;
					}
				}
				mIsIniDown = true;
			} catch (SmbException smb) {
				smb.printStackTrace();
				mHandler.sendEmptyMessage(CompareServerInfoActivity.MSG_NETWORK_ERR);
				return;
			} catch (MalformedURLException e1) {
				mHandler.sendEmptyMessage(CompareServerInfoActivity.MSG_UNKNOWN_ERR);
				e1.printStackTrace();
				return;
			} catch (Exception e) {
				mHandler.sendEmptyMessage(CompareServerInfoActivity.MSG_UNKNOWN_ERR);
				e.printStackTrace();
				return;
			}

			try {
				// String fileName = smbFile.getName();
				File localFile = new File(localDir);
				in = new BufferedInputStream(new SmbFileInputStream(smbFile));
				out = new FileOutputStream(localFile);
				byte[] buffer = new byte[1024];
				while ((in.read(buffer)) != -1) {
					out.write(buffer);
					buffer = new byte[1024];
				}
				mHandler.sendEmptyMessage(CompareServerInfoActivity.MSG_GET_INI_FINISH);
			} catch (Exception e) {
				mHandler.sendEmptyMessage(CompareServerInfoActivity.MSG_UNKNOWN_ERR);
				e.printStackTrace();
			} finally {
				try {
					if (out != null)
						out.close();
					if (in != null)
						in.close(); 
	               	  SmbFile delFile=new SmbFile(mCIFSData.getIniServerStrForDel());
		          Log.v(TAG, "========== delFile"+delFile);
	                  if(delFile.exists())delFile.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public abstract class ConnectCmd implements Runnable {
		public abstract void run();

	}
	
	
	public class CIFSData {
		public String host;
		public String user;
		public String pwd;
		
		public String snServerPath;
		public String snLocalPath;
		
		public String iniLocalPath;
		public String iniServerPath;
		
		public String logLocalPath;
		public String logServerPath;
		
		public String mLocal;
		public String mServer;
		
		public CIFSData() {
			
		}
		
		public void getCIFSConfig(Context c) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(c.getAssets().open(
						CIFS_CONFIG)));
				String temp;
				while ((temp = br.readLine()) != null) {
					String[] data = temp.split(":");
					if (data[0].trim().equals("host")) {
						host = data[1].trim();
					} else if (data[0].trim().equals("user")) {
						user = data[1].trim();
					} else if(data[0].trim().equals("pwd")) {
						pwd = data[1].trim();
					} else if (data[0].trim().equals("log_path_s")) { 
						logServerPath = data[1].trim();
					} else if (data[0].trim().equals("local_path")) {
						logLocalPath = data[1].trim();
						snLocalPath = data[1].trim();
						iniLocalPath = data[1].trim();
					} else if (data[0].trim().equals("sn_path_s")) { 
						snServerPath = data[1].trim();
					} else if (data[0].trim().equals("ini_path_s")) { 
						iniServerPath = data[1].trim();
					}
						
				}

				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public String getSnServerStr() {
			String temp = "smb://"+user+":"+pwd+"@"+host+snServerPath+mNativeManger.getSN()+".flg";
			return temp;
		}
		
		public String getIniServerStr() {
			String temp = "smb://"+user+":"+pwd+"@"+host+iniServerPath+mNativeManger.getSN()+".EBT/STANDARD.INI";
			return temp;
		}
		
		public String getIniServerStrForDel() {
			String temp = "smb://"+user+":"+pwd+"@"+host+iniServerPath+mNativeManger.getSN()+".EBT/";
			return temp;
		}
		public String getlogServerStr() {
			String temp = "smb://"+user+":"+pwd+"@"+host+logServerPath+mNativeManger.getSN()+".log";
			return temp;
		}
		
		public String getLocalStr() {
			return logLocalPath;
		}
		
		public String getHost() {
			return host;
		}
	}
}
