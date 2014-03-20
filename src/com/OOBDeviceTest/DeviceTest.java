package com.OOBDeviceTest;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager.WakeLock;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlSerializer;

import com.OOBDeviceTest.StressTest.RunInTest;
import com.OOBDeviceTest.helper.LogFileHelper;
import com.OOBDeviceTest.helper.LogSenderHelper;
import com.OOBDeviceTest.helper.LogSenderHelperByCISF;
import com.OOBDeviceTest.helper.NativeManger;
import com.OOBDeviceTest.helper.SystemInfoUtil;
import com.OOBDeviceTest.helper.SystemUtil;
import com.OOBDeviceTest.helper.TestCase;
import com.OOBDeviceTest.helper.XmlDeal;
import com.OOBDeviceTest.helper.TestCase.RESULT;
import com.OOBDeviceTest.view.MyGridView;
import com.OOBDeviceTest.view.MyItemView;

import com.android.internal.os.storage.ExternalStorageFormatter;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import android.view.View;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.os.IECService;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.widget.Toast;
import android.view.KeyEvent;

public class DeviceTest extends Activity {

	public static final int DEVICE_TEST_MAX_NUM = 1000;
	public static final int TEST_FAILED_DELAY = 5000;
	public static final String EXTRA_TEST_PROGRESS = "test progress";
	public static final String EXTRA_TEST_RESULT_INFO = "test result info";
	public static final String EXTRA_TEST_START_TIME = "test start";
	public static final String EXTRA_TEST_FINISH_TIME = "test finish";

	public static final String RESULT_INFO_HEAD = ";";
	public static final String RESULT_INFO_HEAD_JUST_INFO = "just info;";

	public static final String EXTRA_PATH = "/data/";
	private static final String CONFIG_FILE_NAME = "DeviceTestConfig.xml";
//	private static final String CONFIG_FILE_NAME = "sw_DeviceTestConfig.xml";
    private static final String EXTRA_CONFIG_FILE_NAME = EXTRA_PATH
			+ CONFIG_FILE_NAME;
	public static final String DATA_PATH = "/data/data/com.OOBDeviceTest/";
	private static final String SAVE_FILE_PATH = EXTRA_PATH
			+ "DeviceTestResult";
	private static final String TAG = "OOBDeviceTest";
	private static final String SAVE_DATA_PATH = DATA_PATH + "DeviceTest.tmp";
	public static final String TEMP_FILE_PATH = DeviceTest.DATA_PATH + "test";
	
	public static final int MSG_SEND_LOG_SUCESS = 0;
	public static final int MSG_SEND_LOG_FAIL = 1;
	public static final int MSG_LOGIN_SUCESS= 2;
	public static final int MSG_LOGIN_FAIL = 3;
	public static final int MSG_RECOVERY = 4;
	public static final int MSG_RUNIN = 5;
	public static final int OPEN_WIFI_SUCCESS = 8;
	public static final int OPEN_WIFI_TIME_OUT = 7;
	public static final int WAIT_FOR_OPEN_WIFI = 9;
	
	public static final int SEND_LOG_BY_FTP = 0;
	public static final int SEND_LOG_BY_CIFS = 1;
	private static int SEND_LOG_METHOD = SEND_LOG_BY_CIFS;
	private static int SEND_CHECK_ALLPASS = 6;
	
	private LogSenderHelper mSenderHelper;
	private LogSenderHelperByCISF mLogSenderHelperByCISF;
	
	private XmlDeal xmldoc = null;
	private Spinner mGroupTestSpinner;
	private Button mButtonCancel;
	private Button mTestChecked;
	private Button mSendLogButton;
	private Button mClearButton;
	private Button mUninstallButton;
	private Button mRecoveryButton;
	
	private ProgressDialog mProgressDialog;
	private ProgressDialog mwifiProgressDialog;
	private AlertDialog mNetworkCheckDialog;
	private AlertDialog mWIFIOpenFailDialog;
	private	boolean isNetworkConnect;
	
	MyGridView myGridView;

	private List<TestCase> mTestCases;
	private List<TestCase> mCurrentCaseGroup;
	Object[] mTestGroupNames;
	
	private NativeManger mNM;
	private WakeLock mWakeLock;
	private boolean mIsTestAll = false;
	private boolean mIsSendingLog = false;
	private int mTestPos = 0;
	
	private boolean mAC_IN = false;
	private boolean mSendLogFinish = false;
	private boolean mCanRecovery = false;
	private int mBatteryLevel = 0;
	public static boolean isOnTestAll = false;
	private SharedPreferences mSharedPreferences;
	private String  mSN=null;
	
	/**
	 * mAllfinish: when reboot test finish , it will start the actvity and 
	 * send log to server then recovery.
	 */
	private int mAllFinish = 0;

	/** Called when the activity is first created. */
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		isOnTestAll=true;
		if (!InitTestData()) {
			System.exit(-1);
		}
//		this.setTitle("DeviceTest Version:"
//				+ getResources().getString(R.string.Version) + "  (for android4.1)");
		this.setTitle("DeviceTest");
		mTestCases = xmldoc.mTestCases;
		try {
			loadData();
		} catch (Exception e) {
			Log.e(TAG, "load data error.");
			e.printStackTrace();
		}
	
//		NativeManger nativeManger = new NativeManger();
//		mSN=nativeManger.getSN();
//		if (mSN != null && !mSN.equals("")) {
//					LogFileHelper.LogFile = LogFileHelper.DEFAULT_LOG_FILE_PATH+"/"+mSN+".oob1";
//					LogFileHelper.rmLogFileIfExist();
//				}
//        WriteLocalLog();
		
		myGridView = (MyGridView) findViewById(R.id.myGridView);
		myGridView.setColumnCount(3);

		for (TestCase testCase : mTestCases) {
			MyItemView itemView = new MyItemView(this);
			itemView.setText(testCase.getTestName());
			itemView.setTag(testCase.getTestNo());
			itemView.setCheck(testCase.getneedtest());
			if (testCase.isShowResult()) {
				RESULT result = testCase.getResult();
				itemView.setResult(result);
			}
			myGridView.addView(itemView);
		}

		myGridView.setOnItemClickListener(new MyGridView.OnItemClickListener() {
			public void onItemClick(ViewParent parent, View view, int position) {
			if(isOnTestAll){
				if(((MyItemView)view).setCheckClick()){
					if(!((MyItemView)view).getischeck()){
						mTestCases.get(position).setneedtest(false);
					}else{
						mTestCases.get(position).setneedtest(true);
					}
					return;
				}
				if(enableitemclick)
					enableitemclick = false;
				else
					return;
				Intent intent = new Intent();
				try {
					if (mTestCases.get(position) != null) {
						String strClsPath = "com.OOBDeviceTest."
								+ mTestCases.get(position).getClassName();
						intent.setClass(DeviceTest.this,
								Class.forName(strClsPath).newInstance()
										.getClass());
						intent.putExtra(EXTRA_TEST_PROGRESS, "0/1");
						startActivityForResult(intent, position);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			 }
			}
		});

		mGroupTestSpinner = (Spinner) findViewById(R.id.GroupTestSpinner);

		mTestGroupNames = xmldoc.mCaseGroups.keySet().toArray();
		String[] testGroupTexts = new String[mTestGroupNames.length + 1];
		for (int i = 1; i < testGroupTexts.length; i++) {
			testGroupTexts[i] = "Group: " + mTestGroupNames[i - 1].toString();
		}
		testGroupTexts[0] = "CaseGroups";

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, testGroupTexts);
		mGroupTestSpinner.setAdapter(adapter);
		mGroupTestSpinner.setSelection(0, false);
		mGroupTestSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						if (position == 0) {
							return;
						}
						testGroup(mTestGroupNames[position - 1].toString());
						mGroupTestSpinner.setSelection(0, false);
					}

					public void onNothingSelected(AdapterView<?> parent) {

					}
				});

		mButtonCancel = (Button) findViewById(R.id.btn_cancel);
		mButtonCancel.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
/*				try {
					save(SAVE_FILE_PATH);
				} catch (Exception e) {
					Log.e(TAG, "Failed to save test result!");
					e.printStackTrace();
				}*/
				finish();
			}

		});
		mTestChecked = (Button) findViewById(R.id.btn_testall);
		mTestChecked.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				isOnTestAll=false;
				 mIsTestAll = true;
				testGroup(mTestGroupNames[0].toString());
			}
		});
		mClearButton = (Button) findViewById(R.id.btn_clear);
		mClearButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				showDialog(DIALOG_CLEAR_ID);
			}
		});

		mUninstallButton = (Button) findViewById(R.id.btn_uninstall);
		mUninstallButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				uninstallPackage("com.OOBDeviceTest");
				File delFile = new File("/dpfdata/test.mp4");
				if (delFile.exists()) {
					// delFile.delete();
				}
			}
		});
		
		mSendLogButton = (Button) findViewById(R.id.btn_send);
		mSendLogButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				sendLog();
			}
		});

		mRecoveryButton = (Button) findViewById(R.id.btn_reset);
		mRecoveryButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                showRecoveryDialog();
            }
        });
		mNM = new NativeManger();
		
		createAssetFile("memtester", MEMTESTER_PATH);
		createAssetFile("gps_coldstart", GPS_COLD_START_PATH);
		
		mAllFinish = getIntent().getIntExtra("finish", 0);
		
		registerReceiver(mNetworkReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
		
		mWakeLock = ((PowerManager)getSystemService("power")).newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "videotest");
		mWakeLock.acquire();
		
		//just for bitland project
			hideButton();
		//just for bitland project end
	}

       @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(0, menu.FIRST+1, Menu.NONE, "Exit");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == Menu.FIRST+1) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	private boolean enableitemclick = true;
	@Override
	protected void onResume() {
		enableitemclick = true;
		super.onResume();
		//if (mAllFinish == 1) {
		
		registerReceiver(mBatteryCheckReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
			 isNetworkConnect = SystemUtil.isNetworkConnected(this);
		//	if (isNetworkConnect) {
		//		sendLog();
		//	} else {
		//		showNetworkCheckDialog();
		//	}
		
		//}
		
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if (checkAllPass())
			unregisterReceiver(mBatteryCheckReceiver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWakeLock.release();
		unregisterReceiver(mNetworkReceiver);
	}
	
	
	private void WriteLocalLog(){
	   final WifiManager wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		 wifi.setWifiEnabled(true);
		 if(!wifi.isWifiEnabled()){
			 mHandler.sendEmptyMessage(WAIT_FOR_OPEN_WIFI);
			 new Thread(new Runnable(){
				@Override
				public void run() {
					int time=0;
					while (!wifi.isWifiEnabled()) {
						try {
							Thread.sleep(500);	
							time++;
							if(time>=20)mHandler.sendEmptyMessageDelayed(OPEN_WIFI_TIME_OUT, 100);
						} catch (Exception e) {
						}
					}
					//mwifiProgressDialog.dismiss();
					mHandler.sendEmptyMessageDelayed(OPEN_WIFI_SUCCESS, 100);
				}
			}).start();
		}else{
		 LogFileHelper.wtireLog(LogFileHelper.getLogHeader(DeviceTest.this));
		}
	}
	
	/**
	 *  send log to server.  please config the param{SEND_LOG_METHOD}.
	 */
	private void sendLog() {
		if (mIsSendingLog) {
			Log.e(TAG, "sendLog called , but is sending log, so return !");
			return ;
		}
			isOnTestAll = false;//LIMI 9.17	
		mTestChecked.setClickable(false);//LIMI 9.17	
			
			
		if (SEND_LOG_METHOD == SEND_LOG_BY_FTP) {
			if (mSenderHelper == null) {
				mSenderHelper = new LogSenderHelper(DeviceTest.this, mHandler);
			}
			mSenderHelper.connectFTP();
			mIsSendingLog = true;
		} else if (SEND_LOG_METHOD == SEND_LOG_BY_CIFS) {
			if (mLogSenderHelperByCISF == null)
				mLogSenderHelperByCISF = new LogSenderHelperByCISF(DeviceTest.this, mHandler);
			mLogSenderHelperByCISF.sendLog();
			mIsSendingLog = true;
		}
		
		showSendLogDialog();
	}
	
	private void createAssetFile(String name, String destPath) {

		InputStream is = null;
		OutputStream os = null;
		try {
			is = getAssets().open(name);
			os = new FileOutputStream(destPath);
			int data = 0;
			while (true) {
				data = is.read();
				if (data < 0) {
					break;
				}
				os.write(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
				}
			}
			SystemUtil.execRootCmd("chmod 777 " + destPath);
		}
	}

	public final static String MEMTESTER_PATH = DeviceTest.DATA_PATH
			+ "memtester";
	public final static String GPS_COLD_START_PATH = DeviceTest.DATA_PATH
	+ "gps_coldstart";
	static final int DIALOG_CLEAR_ID = 10;

	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Clear all test status?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								for (int i = 0; i < myGridView.getChildCount(); i++) {
									MyItemView myItemView = (MyItemView) myGridView
											.getChildAt(i);
									myItemView.setResult(RESULT.UNDEF);
									mTestCases.get(i).setShowResult(false);
								}
/*								try {
									save(SAVE_FILE_PATH);
								} catch (IOException e) {
									e.printStackTrace();
								}*/
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		return alert;
	}

	private void loadData() throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				SAVE_DATA_PATH));
		List<TestCase> savedData = (List<TestCase>) ois.readObject();
		for (TestCase savedCase : savedData) {
			for (TestCase testCase : mTestCases) {
				if (testCase.getClassName().equals(savedCase.getClassName())) {
					testCase.setResult(savedCase.getResult());
					testCase.setDetail(savedCase.getDetail());
					testCase.setShowResult(savedCase.isShowResult());
				}
			}
		}
		ois.close();
	}

	public static String formatResult(String testName, RESULT result,
			String detail) {
		if (detail == null) {
			return "[" + testName + "]\n" + result.name();
		}
		if (detail.startsWith(RESULT_INFO_HEAD_JUST_INFO)) {
			return detail.substring(RESULT_INFO_HEAD_JUST_INFO.length());
		}
		return "[" + testName + "]\n" + result.name() + detail;
	}
	
	//log add start time and finish time
	public String formatResultByTestcast(TestCase testcast) {
		StringBuilder resultStr = new StringBuilder();
		resultStr.append("[").append(testcast.startTime).append("] :")
				.append(testcast.getTestName()).append(" START.\n");
		if(testcast.getDetail()!=null && testcast.getDetail().startsWith(RESULT_INFO_HEAD_JUST_INFO)) {
			resultStr.append(testcast.getDetail().substring(RESULT_INFO_HEAD_JUST_INFO.length()));
		}
		resultStr.append("------").append(testcast.getResult().name()).append("------\n");
		resultStr.append("[").append(testcast.finishTime).append("] :")
		.append(testcast.getTestName()).append(" FINISH.\n\n");
		
		return resultStr.toString();
	}
	
	synchronized private void saveResult(TestCase testCase) {
		LogFileHelper.writeLogWithoutClose(formatResultByTestcast(testCase));
	}

	synchronized private void save(String saveFilePath) throws IOException {
		for (TestCase testCase : mTestCases) {
		/*	if (testCase.getClassName().equals(
					RuninTestActivity.class.getSimpleName())) {
				if (testCase.getDetail() == null) {
					testCase.setDetail(new RuninTestActivity().getResult());
				}
			} else*/ if(testCase.getClassName().equals(GpsTestActivity.class.getSimpleName())) {
				if (testCase.getDetail() == null) {
					testCase.setDetail(new GpsTestActivity().getResult());
				}
			}
//			fw.write(formatResult(testCase.getTestName(), testCase.getResult(),
//					testCase.getDetail()) + "\n");
			LogFileHelper.writeLogWithoutClose(formatResultByTestcast(testCase));
		}
		LogFileHelper.writeLogClose();

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				SAVE_DATA_PATH));
		oos.writeObject(mTestCases);
		oos.close();
	}

	protected void testGroup(String selectGroup) {
		mCurrentCaseGroup = xmldoc.mCaseGroups.get(selectGroup);
		int pos = 0;
		while(!mCurrentCaseGroup.get(pos).getneedtest()){
			pos ++;
			 if(pos >= mCurrentCaseGroup.size()){
                 return;
			 }
		}
		Intent intent = new Intent();
		if (mCurrentCaseGroup != null && mCurrentCaseGroup.get(pos) != null) {
			try {
				String strClsPath = "com.OOBDeviceTest."
						+ mCurrentCaseGroup.get(pos).getClassName();
				intent.setClass(DeviceTest.this, Class.forName(strClsPath)
						.newInstance().getClass());
				intent.putExtra(EXTRA_TEST_PROGRESS,
						"0/" + mCurrentCaseGroup.size());
				// we use nagtiv value to keep the sequence number when
				// do a all test.
				startActivityForResult(intent, pos+DEVICE_TEST_MAX_NUM);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
	}
	

	private void uninstallPackage(String packageName) {
		String cmd = "mount -o remount,rw /system /system\n"
				+ "rm -r /data/data/*OOBDeviceTest*\n"
				+ "rm /data/app/*OOBDeviceTest*\n"
				+ "rm /system/app/*OOBDeviceTest*\n";
		SystemUtil.execScriptCmd(cmd, TEMP_FILE_PATH, true);

		Uri uninstallUri = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, uninstallUri);
		startActivity(uninstallIntent);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent paramIntent) {
		super.onActivityResult(requestCode, resultCode, paramIntent);
		Log.i(TAG, " -------------------- onResult---request:" + requestCode + ",result:"
				+ resultCode);		

		if(mCurrentCaseGroup != null && (requestCode - DEVICE_TEST_MAX_NUM) >= mCurrentCaseGroup.size()) {
			return;
		}

		int pos = requestCode;
		boolean ignore = (resultCode == RESULT.UNDEF.ordinal());
		
		//for retest
		if (resultCode == RESULT.RETEST.ordinal()) {
			int p = 0;
			String strClsPath = null;
			
			if (pos >= DEVICE_TEST_MAX_NUM) {
				p = pos - DEVICE_TEST_MAX_NUM;
				strClsPath= "com.OOBDeviceTest."
						+ mCurrentCaseGroup.get(p).getClassName();
			} else {
				p = pos;
				strClsPath= "com.OOBDeviceTest."
						+ mTestCases.get(p).getClassName();
			}
			try {
				Intent retestIntent = new Intent();
				retestIntent.setClass(DeviceTest.this, Class
						.forName(strClsPath).newInstance().getClass());

//				retestIntent.putExtra(EXTRA_TEST_PROGRESS, p + "/"
//						+ mCurrentCaseGroup.size());
				startActivityForResult(retestIntent, pos);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		//for retest end.
		
		
		if (requestCode >= DEVICE_TEST_MAX_NUM) {
			if(mCurrentCaseGroup == null){
				Log.d(TAG, " _________________ mCurrentCaseGroup == null~~~~!!!!!");
			}
			// test auto judged.
			TestCase tmpTestCase = mCurrentCaseGroup.get(requestCode - DEVICE_TEST_MAX_NUM);
			if(tmpTestCase == null){
				Log.d(TAG, " _________________ tmpTestCase == null~~~~!!!!!");
			}
			pos = tmpTestCase.getTestNo();
		}
		
		if (!ignore && pos < mTestCases.size()) {
			MyItemView itemView = (MyItemView) myGridView.getChildAt(pos);
			RESULT result = RESULT.values()[resultCode];
			itemView.setResult(result);
			mTestCases.get(pos).setResult(result);
			mTestCases.get(pos).setShowResult(true);
			try {
				String detail = paramIntent
						.getStringExtra(EXTRA_TEST_RESULT_INFO);
				mTestCases.get(pos).setDetail(detail);
				mTestCases.get(pos).startTime = paramIntent
						.getStringExtra(EXTRA_TEST_START_TIME);
				mTestCases.get(pos).finishTime = paramIntent
						.getStringExtra(EXTRA_TEST_FINISH_TIME);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			saveResult(mTestCases.get(pos));
	/*		try {
				save(SAVE_FILE_PATH);
			} catch (IOException e1) {
				e1.printStackTrace();
			}*/
		}

		if (requestCode >= DEVICE_TEST_MAX_NUM) {
			// test next autojuaged.
			pos = requestCode - DEVICE_TEST_MAX_NUM;
			pos++;
			Intent intent = new Intent();
			Log.e(TAG, "pos:"+pos+"mCurrentCaseGroup.size():"+mCurrentCaseGroup.size());
			if (pos < mCurrentCaseGroup.size()) {
				while(!mCurrentCaseGroup.get(pos).getneedtest()){
					pos ++;
					 if(pos >= mCurrentCaseGroup.size()){
                         return;
					 }

				}
				try {
					String strClsPath = "com.OOBDeviceTest."
							+ mCurrentCaseGroup.get(pos).getClassName();
					intent.setClass(DeviceTest.this, Class.forName(strClsPath)
							.newInstance().getClass());

					intent.putExtra(EXTRA_TEST_PROGRESS, pos + "/"
							+ mCurrentCaseGroup.size());

					// we use nagtiv value to keep the sequence number when
					// do a all test.
					startActivityForResult(intent, pos + DEVICE_TEST_MAX_NUM);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				}
			} else if(pos >= mCurrentCaseGroup.size()) {
				//SharedPreferences mSharedPreferences = getSharedPreferences("state", 0);
				LogFileHelper.writeLogClose();
				
					
//				mSendLogButton.performClick();
				
			}
			
			/*else if(pos >= mCurrentCaseGroup.size()){
				//finish testing all selectd items then close the log file. 
				LogFileHelper.writeLogClose();
				mHandler.sendEmptyMessage(MSG_RUNIN);
				return;
			}*/
		} 
		
		if (checkAllPass()) {
			//registerReceiver(mBatteryCheckReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		  isNetworkConnect = SystemUtil.isNetworkConnected(this);
			if (isNetworkConnect) {
				sendLog();
			} else {
				showNetworkCheckDialog();
			}
			/*
			SharedPreferences mSharedPreferences = getSharedPreferences("state", 0);
		   SharedPreferences.Editor edit = mSharedPreferences.edit();
		   edit.putInt("first_finish", 1);
		   edit.commit();
		   */
          /*  	
  	   Intent shutdownIntent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
       shutdownIntent.putExtra("android.intent.extra.KEY_CONFIRM", false);
       shutdownIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       startActivity(shutdownIntent);
       */
		} else {
			Log.e(TAG, "NO all test case pass!");
		}
	}

	private boolean InitTestData(InputStream is) {
		if (is == null) {
			return false;
		}
		try {
			xmldoc = new XmlDeal(is);
		} catch (Exception e) {
			Log.e(TAG, "parse the xmlfile is fail");
			return false;
		}
		return true;

	}

	private boolean InitTestData() {
		InputStream is = null;
		try {
			File configFile = new File(EXTRA_CONFIG_FILE_NAME);
			if (configFile.exists()) {
				Log.i(TAG, "Use extra config file:"
						+ EXTRA_CONFIG_FILE_NAME);
				if (InitTestData(new FileInputStream(configFile))) {
					return true;
				}
			}

			// is = this.openFileInput(strXmlPath);
			is = getAssets().open(CONFIG_FILE_NAME);

			try {
				xmldoc = new XmlDeal(is);
			} catch (Exception e) {
				Log.e(TAG, "parse the xmlfile is fail");
				return false;
			}
		} catch (IOException e) {

			e.printStackTrace();
			Log.e(TAG, "read the xmlfile is fail" + e.getMessage());
			// ForwardErrorActive();
			return false;
		}

		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_SEND_LOG_SUCESS:
				mIsSendingLog = false;
				mSendLogFinish = true;
				if (mProgressDialog!=null && mProgressDialog.isShowing())
					mProgressDialog.dismiss();
				Toast.makeText(DeviceTest.this, R.string.send_sucess_and_recovery, Toast.LENGTH_LONG).show();
				/*
				if (mAllFinish == 1) {
					if (mBatteryLevel > 75 && !mAC_IN) {
						//LIMI 8.14
						int state_flag=-1;
					      IECService  ecService = IECService.Stub.asInterface(ServiceManager.getService("ECServiceinfo"));
					      try{
						       state_flag=ecService.writeVendorFlags(0x1010,1);
						     } catch (RemoteException e) {
			                   e.printStackTrace();
	                         }
						  if(state_flag==0){
							  mCanRecovery = true;
							  mHandler.sendEmptyMessageDelayed(MSG_RECOVERY, 5000);
						  }
					}
					else {
						 if(mAC_IN)showshipmodeACDialog();
				    	 if(mBatteryLevel <=75)showshipmodeBatteryDialog();
					}
				} 
				*/
				break;
			case MSG_SEND_LOG_FAIL:
				if (mProgressDialog!=null && mProgressDialog.isShowing())
					mProgressDialog.dismiss();
				mIsSendingLog = false;
				showSendLogFailDialog();
//				Toast.makeText(DeviceTest.this, R.string.send_fail, Toast.LENGTH_LONG).show();
				break;
			case MSG_LOGIN_SUCESS:
				if (mProgressDialog!=null && mProgressDialog.isShowing())
					mProgressDialog.dismiss();
				mSenderHelper.sendLogToServer(DATA_PATH + LogFileHelper.LogFile);
				break;
			case MSG_LOGIN_FAIL: 
				if (mProgressDialog!=null && mProgressDialog.isShowing())
					mProgressDialog.dismiss();
				Toast.makeText(DeviceTest.this, R.string.login_fail, Toast.LENGTH_LONG).show();
				break;
			case MSG_RECOVERY:
				Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
                intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                startService(intent);
				break;
			case MSG_RUNIN:
				//test all selectd items then test runInTest.
				if (mIsTestAll) {
					Intent RunInIntent = new Intent(DeviceTest.this, RunInTest.class);
					RunInIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(RunInIntent);
				}
				break;
			case OPEN_WIFI_SUCCESS:
				if (mwifiProgressDialog!=null && mwifiProgressDialog.isShowing())
					  mwifiProgressDialog.dismiss();
					  Log.v("limi===908","908");
				    LogFileHelper.wtireLog(LogFileHelper.getLogHeader(DeviceTest.this));
				break;
			case OPEN_WIFI_TIME_OUT:
				mwifiProgressDialog.dismiss();
				showOpenWifiTimeOut();
				break;
				/*
			case SEND_CHECK_ALLPASS:
			    showShutdownDialog(getString(R.string.Run_runin));
				break;
				*/
			case WAIT_FOR_OPEN_WIFI:
			   showWaitForOpenWifi();
				break;
				
			default:
				break;
			}
		};
	};
	
	private void showSendLogDialog() {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setMessage(getString(R.string.sending_log));
		mProgressDialog.show();
	}
	
	
	private void showWaitForOpenWifi() {
		if(mwifiProgressDialog!=null&&mwifiProgressDialog.isShowing())
		return;
		mwifiProgressDialog = new ProgressDialog(this);
		mwifiProgressDialog.setTitle(R.string.compare_pro_title);
		mwifiProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		//mwifiProgressDialog.setCanceledOnTouchOutside(false);
		mwifiProgressDialog.setCancelable(false);
		mwifiProgressDialog.setMessage(getString(R.string.wait_for_open_wifi));
		mwifiProgressDialog.show();
	}
	
	private void showOpenWifiTimeOut() {
	 if(mWIFIOpenFailDialog!=null&&mWIFIOpenFailDialog.isShowing())
		return;
		mWIFIOpenFailDialog=new AlertDialog.Builder(this)
	    .setMessage(getString(R.string.open_wifi_time_out))
        .setPositiveButton(R.string.dialog_btn_retry, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                 mwifiProgressDialog.dismiss();
                 mWIFIOpenFailDialog.dismiss();
                 WriteLocalLog();
            }
        })
       .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
               mWIFIOpenFailDialog.dismiss();
            	 DeviceTest.this.finish();
            }
        }).show();
	}
	
	
	private void showShutdownDialog(String shutmsg) {
		new AlertDialog.Builder(this)
	    .setTitle(R.string.Shutdown)
	    .setMessage(getString(R.string.Shutdown_msg)+ shutmsg)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                 Intent shutdownIntent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                 shutdownIntent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                 shutdownIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 startActivity(shutdownIntent);
            }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
	}
	
	private void showRecoveryDialog() {
	    final CheckBox cb = new CheckBox(this);
	    cb.setText(R.string.Erase_sd);
	    
	    new AlertDialog.Builder(this)
	    .setTitle(R.string.Recovery)
	    .setMessage(R.string.Recovery_msg)
	    .setView(cb)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (cb.isChecked()) {
                    Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
                    intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                    startService(intent);
                } else {
                    sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
                }
                
            }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
	}
	
	private void showSendLogFailDialog() {
		new AlertDialog.Builder(this)
	    .setMessage(getString(R.string.send_fail))
        .setPositiveButton(R.string.resend_log, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	mSendLogButton.performClick();
            	dialog.dismiss();
            }
        }).show();
//        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        }).show();
	}
	
	private void showNetworkCheckDialog() {
		if (mNetworkCheckDialog != null && mNetworkCheckDialog.isShowing())
			return ;
		
		mNetworkCheckDialog = new AlertDialog.Builder(this)
				.setMessage(getString(R.string.Network_CHECK_MSG))
			/*
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								if(isNetworkConnect)dialog.dismiss();
							}
						})
				*/
						.setCancelable(false)
						.show();
	}
	
	
	
	private void hideButton() {
		mButtonCancel.setVisibility(View.GONE);
		mClearButton.setVisibility(View.GONE);
		mRecoveryButton.setVisibility(View.GONE);
		mSendLogButton.setVisibility(View.GONE);
		mUninstallButton.setVisibility(View.GONE);
	}
	
	private boolean checkAllPass() {
		for (TestCase testCase : mTestCases) {
			if (testCase.getResult() != RESULT.OK) {
				return false;
			}
		}
		return true;
	}
	
	//use for listen the network state change.
	private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {  
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
	                NetworkInfo networkInfo =
	                        (NetworkInfo)extras.get(ConnectivityManager.EXTRA_NETWORK_INFO);
	                
	                if (networkInfo == null) return;
	                
	                State state = networkInfo.getState();
	                int networkType = networkInfo.getType();
	                //also can use TYPE_WIFI
	            	if (networkType == ConnectivityManager.TYPE_ETHERNET) {
		                if (state == State.CONNECTED) {
		                	Log.e(TAG, "ETHERNET ... CONNECTED");
		                	if (mNetworkCheckDialog != null && mNetworkCheckDialog.isShowing())
		                		mNetworkCheckDialog.cancel();
		                	if (checkAllPass()) {  
		                		sendLog();     
		                	}
		                } else if (state == State.DISCONNECTED) {
		                	Log.e(TAG, "ETHERNET ... DISCONNECTED");
		                }
	            	}
				}
			}
		}
	};
	
	
	private BroadcastReceiver mBatteryCheckReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				int plugType = intent.getIntExtra("plugged", 0);
				mBatteryLevel = intent.getIntExtra("level", 0);
				  if (plugType > 0) {
					  //AC IN
					  mAC_IN = true;
				  } else {
					    //AC NOT IN
					    mAC_IN = false;
				     	//
			
			     }
		   }
		   //if (mAllFinish == 1 && mSendLogFinish) { 
		    if(checkAllPass() && mSendLogFinish) {
		    	if (mBatteryLevel > 75 && !mAC_IN) {
					 int state_flag=-1;
					 int read_flag=-1;  
					 int[] w_array=new int[]{-1,-1,-1,-1,-1};
					 int[] r_array=new int[]{-1,-1,-1,-1,-1};
					 IECService  ecService = IECService.Stub.asInterface(ServiceManager.getService("ECServiceinfo"));	
					 try{  
						//do 5 times for check shipping mode
						 for(int i=0;i<5;i++){
							 w_array[i]=ecService.writeVendorFlags(0x1010,1); 
							 Thread.sleep(500);
							 r_array[i]=ecService.readVendorFlags(); 
							 Thread.sleep(500);
							 }
						 if(w_array[0]==0&&w_array[1]==0&&w_array[2]==0&&w_array[3]==0&&w_array[4]==0)state_flag=0;
						 if(r_array[0]==1&&r_array[1]==1&&r_array[2]==1&&r_array[3]==1&&r_array[4]==1)read_flag=1;
					 } catch (RemoteException e) { 
						 e.printStackTrace();
	                        } catch (Exception e1) {
                                        e1.printStackTrace();
                                }
					 if(state_flag==0&&read_flag==1) mHandler.sendEmptyMessageDelayed(MSG_RECOVERY, 1000); 
					
		    	}
		      else // Toast.makeText(getApplicationContext(), getString(R.string.shippingMode), 3000).show();
		      {
		    	  if(mAC_IN)showshipmodeACDialog(); 
		    	  if(mBatteryLevel <=75)showshipmodeBatteryDialog();
		      }
	      }
	  }
	};
	
	private void showshipmodeACDialog() {
		AlertDialog	mshipmodeACDialog = new AlertDialog.Builder(this)
				.setMessage(getString(R.string.shipmode_AC_Dialog))
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).show();
	}
	
	private void showshipmodeBatteryDialog() {
		AlertDialog	mshipmodeBatteryDialog = new AlertDialog.Builder(this)
				.setMessage(getString(R.string.shipmode_Battery_Dialog))
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).show();
	}
	
	
	
	

}
