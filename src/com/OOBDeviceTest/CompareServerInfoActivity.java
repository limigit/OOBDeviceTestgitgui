package com.OOBDeviceTest;

import java.util.ArrayList;
import java.util.List;
import com.OOBDeviceTest.R;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


import com.OOBDeviceTest.helper.DeviceInfo;
import com.OOBDeviceTest.helper.GetDeviceInfoHelper;
import com.OOBDeviceTest.helper.GetServerIniHelper;
import com.OOBDeviceTest.helper.GetServerTimeHelper;
import com.OOBDeviceTest.helper.LogFileHelper;
import com.OOBDeviceTest.helper.PhaseServerFileHelper;
import com.OOBDeviceTest.helper.SystemUtil;

import com.android.internal.os.storage.ExternalStorageFormatter;

public class CompareServerInfoActivity extends Activity {
	public final static String TAG = "CompareServerInfoActivity";
	
	public final static int MSG_SUCCESS_AND_START = 0;
	public final static int MSG_SEND_SN = 1;
	public final static int MSG_SEND_SN_TIME_OUT = 2;
	public final static int MSG_GET_INI_START = 3;
	public final static int MSG_GET_INI_FINISH = 4;
	public final static int MSG_GET_INI_TIME_OUT = 5;
	public final static int MSG_NETWORK_ERR = 6;
	public final static int MSG_UNKNOWN_ERR = 7;
	public final static int MSG_GET_CLIENT_INFO_FINISH = 8;
	
	public final static int DIALOG_NETWORK_ERR = 0;
	public final static int DIALOG_UNKNOWN_ERR = 1;
	public final static int DIALOG_GET_INI_TIMEOUT = 2;
  public final static int DIALOG_COMPARE_FAIL=11111;
	
	public static String keyboardInfo=null;
	public static String runinVideoTime=null;
	public static String HasHDMI=null;
	private AlertDialog mShowCheckImageDialog;
	private AlertDialog mShowCheckImageerrDialog;
	
	public DeviceInfo mServerInfo;
	public DeviceInfo mClientInfo;
	
	private PhaseServerFileHelper mPSFH;
	private GetDeviceInfoHelper mGDIH;
	private GetServerIniHelper mGSIH;

	private TextView mClientView;
	private TextView mServerView;
	private TextView mResultView;

	private TextView mServerEC;
	private TextView mServerPRO;
	private TextView mServerHDD;
	private TextView mServerBID;
	private TextView mServerDDR;
	private TextView mServerCPU;
	private TextView mServerCAM;
	private TextView mServerPAN;
	private TextView mServerWLA;
	private TextView mServerLCD;
	private TextView mServerBAT;
	private TextView mServerPAD;
	private TextView mServerBUN;
	
	private TextView mClientEC;
	private TextView mClientPRO;
	private TextView mClientHDD;
	private TextView mClientBID;
	private TextView mClientDDR;
	private TextView mClientCPU;
	private TextView mClientCAM;
	private TextView mClientPAN;
	private TextView mClientWLA;
	private TextView mClientLCD;
	private TextView mClientBAT;
	private TextView mClientPAD;
	private TextView mClientBUN;
	
	private ProgressDialog mProgressDialog;
	private AlertDialog.Builder mAlertDialog;
	
	private boolean mCompareFinish = false;
	private boolean mGetClientInfoFinish = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compare_server_info);
		Log.v(TAG," ===onCreate===");
		init_start();
	/*
		//get local info
		mGDIH = new GetDeviceInfoHelper(this, mHandler);
		
		//
		mGSIH = new GetServerIniHelper(this, mHandler);
		*/
		//set local time from server
		//adjustTimeFromServer(mGSIH.mCIFSData.getHost());
		//Log.v("limi adjustTimeFromServer===","getHost= "+mGSIH.mCIFSData.getHost());
		
		mServerView = (TextView) findViewById(R.id.server_content);
		mClientView = (TextView) findViewById(R.id.client_content);
         //		initClientInfoContent(mClientInfo);
		mResultView = (TextView) findViewById(R.id.result_tv);
		
		
		mServerEC=(TextView)findViewById(R.id.server_EC_VER);
		mServerPRO=(TextView)findViewById(R.id.server_PRODUCT);
		mServerHDD=(TextView)findViewById(R.id.server_HDD);
		mServerBID=(TextView)findViewById(R.id.server_BID);
		mServerDDR=(TextView)findViewById(R.id.server_DDR);
		mServerCPU=(TextView)findViewById(R.id.server_CPU);
		mServerCAM=(TextView)findViewById(R.id.server_CAMERA_ID);
		mServerPAN=(TextView)findViewById(R.id.server_TOUCHPANEL_ID);
		mServerWLA=(TextView)findViewById(R.id.server_WLAN_ID);
		mServerLCD=(TextView)findViewById(R.id.server_LCD_ID);
		mServerBAT=(TextView)findViewById(R.id.server_BATTERY_CELL);
		mServerPAD=(TextView)findViewById(R.id.server_TOUCHPAD_ID);
		mServerBUN=(TextView)findViewById(R.id.server_build_num);
		
		mClientEC=(TextView)findViewById(R.id.client_EC_VER);
		mClientPRO=(TextView)findViewById(R.id.client_PRODUCT);
		mClientHDD=(TextView)findViewById(R.id.client_HDD);
		mClientBID=(TextView)findViewById(R.id.client_BID);
		mClientDDR=(TextView)findViewById(R.id.client_DDR);
		mClientCPU=(TextView)findViewById(R.id.client_CPU);
		mClientCAM=(TextView)findViewById(R.id.client_CAMERA_ID);
		mClientPAN=(TextView)findViewById(R.id.client_TOUCHPANEL_ID);
		mClientWLA=(TextView)findViewById(R.id.client_WLAN_ID);
		mClientLCD=(TextView)findViewById(R.id.client_LCD_ID);
		mClientBAT=(TextView)findViewById(R.id.client_BATTERY_CELL);
		mClientPAD=(TextView)findViewById(R.id.client_TOUCHPAD_ID);
		mClientBUN=(TextView)findViewById(R.id.client_build_num);
		

		
		Button btnPass = (Button)findViewById(R.id.btn_Pass);
		btnPass.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CompareServerInfoActivity.this, DeviceTest.class);
				startActivity(intent);
			}
		});
	     btnPass.setVisibility(View.INVISIBLE);

		Button btnFail = (Button)findViewById(R.id.btn_Fail);
		btnFail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CompareServerInfoActivity.this.finish();
			}
		});
		
		Button btnRetest = (Button)findViewById(R.id.btn_Retest);
		btnRetest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				init_start();
			}
		});
		
	}
	 @Override
      public void onPause() {
        super.onPause();
        mHandler.removeMessages(MSG_SUCCESS_AND_START);
        mHandler.removeMessages(MSG_SEND_SN);
        mHandler.removeMessages(MSG_SEND_SN_TIME_OUT);
        
        mHandler.removeMessages(MSG_GET_INI_START);
        mHandler.removeMessages(MSG_GET_INI_FINISH);
        mHandler.removeMessages(MSG_GET_INI_TIME_OUT);

        mHandler.removeMessages(MSG_NETWORK_ERR);
        mHandler.removeMessages(MSG_UNKNOWN_ERR);
        mHandler.removeMessages(MSG_GET_CLIENT_INFO_FINISH);
        
        mHandler.removeMessages(DIALOG_NETWORK_ERR);
        mHandler.removeMessages(DIALOG_UNKNOWN_ERR);
        mHandler.removeMessages(DIALOG_GET_INI_TIMEOUT);
        mHandler.removeMessages(DIALOG_COMPARE_FAIL); 
       
        mProgressDialog.dismiss();
        mShowCheckImageerrDialog=null;
        mShowCheckImageDialog=null;
    }
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	
	protected void init_start() {
		initProgressDialog();
		mProgressDialog.show();
		Log.v(TAG," ===init_start()===");
		mGDIH = new GetDeviceInfoHelper(this, mHandler);
		mGSIH = new GetServerIniHelper(this, mHandler);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG," ===onResume()===");
          
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Recovery");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0) {
			Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
            intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
            startService(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	/**
	 * 閻㈢喐鍨氶弰鍓с仛閻ㄥ嫮閮寸紒鐔朵繆閹垬锟�	 * 
	 */
	
	private void initClientInfoContent(DeviceInfo info) {
		mClientEC.setText("EC_VER: "+info.EC_VER);
		mClientPRO.setText("PRODUCT: "+info.PRODUCT);
		mClientHDD.setText("HDD: "+info.HDD);
		mClientBID.setText("BID: "+info.BID);
		mClientDDR.setText("DDR: "+info.DDR);
		mClientCPU.setText("CPU: "+info.CPU);
		mClientCAM.setText("CameraID: "+info.CameraID);
		mClientPAN.setText("TouchPanel: "+info.TouchPanel);
		mClientWLA.setText("WLAN_ID: "+info.WLAN_ID);
		mClientLCD.setText("LCD: "+info.LCD);
		mClientBAT.setText("BATTERY_CELL: "+info.BATTERY_CELL);
		mClientPAD.setText("TOUCHPAD_ID: "+info.TOUCHPAD_ID);
		mClientBUN.setText("Build_number:"+info.Build_number);
	}
	
	private void initServerInfoContent(DeviceInfo info) {
		mServerEC.setText("EC_VER: "+info.EC_VER);
		mServerPRO.setText("PRODUCT: "+info.PRODUCT);
		mServerHDD.setText("HDD: "+info.HDD);
		mServerBID.setText("BID: "+info.BID);
		mServerDDR.setText("DDR: "+info.DDR);
		mServerCPU.setText("CPU: "+info.CPU);
		mServerCAM.setText("CameraID: "+info.CameraID);
		mServerPAN.setText("TouchPanel: "+info.TouchPanel);
		mServerWLA.setText("WLAN_ID: "+info.WLAN_ID);
		mServerLCD.setText("LCD: "+info.LCD);
		mServerBAT.setText("BATTERY_CELL: "+info.BATTERY_CELL);
		mServerPAD.setText("TOUCHPAD_ID: "+info.TOUCHPAD_ID);
		mServerBUN.setText("Build_number: "+info.Build_number);
		keyboardInfo=info.COUNTRY_KEY;
		runinVideoTime=info.Runin_CY;
		HasHDMI=info.HDMI;
		
		SharedPreferences mSharedPreferences = getSharedPreferences("rundata", 0);
	    SharedPreferences.Editor edit = mSharedPreferences.edit();
		edit.putString("runinVideoTime", runinVideoTime);
		edit.commit();
		
		Log.v("limi runin", "Runin_CY= "+info.Runin_CY);
	}
	
	
	public boolean compareServerAndClientInfo() {
		mCompareFinish = true;
		//return compareSysinfo(mServerInfo, mClientInfo);
			return true;  
			
	}
	public void setRED(TextView sTextView,TextView cTextView){
		sTextView.setBackgroundColor(Color.RED);
		cTextView.setBackgroundColor(Color.RED);
	}
	
	
	public boolean compareSysinfo(DeviceInfo s, DeviceInfo c) {
		boolean[]cpresult=new boolean[13];
		cpresult[0]=EQ(s.EC_VER, c.EC_VER);     if(!cpresult[0])setRED(mServerEC,mClientEC);
		cpresult[1]=EQ(s.PRODUCT, c.PRODUCT);   if(!cpresult[1])setRED(mServerPRO,mClientPRO);
		cpresult[2]=EQ(s.HDD, c.HDD);   		if(!cpresult[2])setRED(mServerHDD,mClientHDD);
		cpresult[3]=EQ(s.BID,c.BID);            if(!cpresult[3])setRED(mServerBID,mClientBID);
		cpresult[4]=EQ(s.DDR, c.DDR);           if(!cpresult[4])setRED(mServerDDR,mClientDDR);
		cpresult[5]=EQ(s.CPU, c.CPU);           if(!cpresult[5])setRED(mServerCPU,mClientCPU);
		cpresult[6]=EQ(s.CameraID, c.CameraID); if(!cpresult[6])setRED(mServerCAM,mClientCAM);
		cpresult[7]=EQ(s.TouchPanel, c.TouchPanel); if(!cpresult[7])setRED(mServerPAN,mClientPAN);
		cpresult[8]=EQ(s.WLAN_ID,c.WLAN_ID);              if(!cpresult[8])setRED(mServerWLA,mClientWLA);
		cpresult[9]=EQ(s.LCD, c.LCD);               if(!cpresult[9])setRED(mServerLCD,mClientLCD);
		cpresult[10]=EQ(s.BATTERY_CELL, c.BATTERY_CELL);  if(!cpresult[10])setRED(mServerBAT,mClientBAT);
		cpresult[11]=EQ(s.TOUCHPAD_ID,c.TOUCHPAD_ID);     if(!cpresult[11])setRED(mServerPAD,mClientPAD);
		cpresult[12]=EQ(s.Build_number,c.Build_number);     if(!cpresult[12])setRED(mServerBUN,mClientBUN);
		
		return  cpresult[0]&&cpresult[1]&&cpresult[2]&&cpresult[3]&&cpresult[4]&&cpresult[5]&&
				cpresult[6]&&cpresult[7]&&cpresult[8]&&cpresult[9]&&cpresult[10]&&cpresult[11]&&cpresult[12];
				
			
	}
	
	public boolean EQ(String server, String client) {
		if (server != null && client !=null) {
			if (server.equals(client)) {
				return true;
			}
		}
		LogFileHelper.wtireLog("compare falied! server:"+server+"  client:"+client+"\n");
		return false ;
	}
	
	public boolean HDDCampare(String server, String client) {
		if (server != null && client !=null) {
			float serverHDD=Float.valueOf(server.substring(0, 5)).floatValue(); 
			float clientHDD=Float.valueOf(client.substring(0, 5)).floatValue(); 
			if ((serverHDD<16&&serverHDD>0&&clientHDD>0&&clientHDD<16)
					||(serverHDD>16&&serverHDD<32&&clientHDD>16&&clientHDD<32)) {
				return true;
			}
		}
		LogFileHelper.wtireLog("compare falied! server:"+server+"  client:"+client+"\n");
		return false ;
	}
	
	
	
	
	private void initProgressDialog() {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setTitle(R.string.compare_pro_title);
		mProgressDialog.setMessage(getString(R.string.pro_msg_send_sn));
		mProgressDialog.setCancelable(false);
		mAlertDialog=new AlertDialog.Builder(this);
	}
	
	public void showResult(boolean result) {
		if (result) {
			mResultView.setText(R.string.btnPassText);
			mResultView.setBackgroundColor(Color.GREEN);
		} else {
			mResultView.setText(R.string.btnFailText);
			mResultView.setBackgroundColor(Color.RED);
		}
		mResultView.setVisibility(View.VISIBLE);
	}
	
	private void onGetIniFinish() {
		//phase server ini file 
		mPSFH = new PhaseServerFileHelper();
		mServerInfo = mPSFH.ReadIniFileAndPhase(
				mGSIH.mCIFSData.iniLocalPath + mClientInfo.SN + ".INI",
				0);
		initServerInfoContent(mServerInfo);
		mProgressDialog.cancel();
		adjustTimeFromServer(mGSIH.mCIFSData.getHost());//add adjust time limi 8.16
		
		if (compareServerAndClientInfo()) {
			showResult(true);
			mHandler.sendEmptyMessageDelayed(MSG_SUCCESS_AND_START, 3000);
		} else {
			showResult(false);
			showDialog(DIALOG_COMPARE_FAIL);
		}
		
	}
	
	private void onGetClientInfoFinish() {
		mClientInfo = mGDIH.getDeviceInfo();
		Log.e(TAG, "wlan id "+mClientInfo.WLAN_ID);
		mGetClientInfoFinish = true;
		initClientInfoContent(mClientInfo);
		
		//init logfile
		if (mClientInfo.SN != null && !mClientInfo.SN.equals("")) {
			LogFileHelper.LogFile = LogFileHelper.DEFAULT_LOG_FILE_PATH+"/"+mClientInfo.SN+".oob1";
			LogFileHelper.rmLogFileIfExist();
		}
		LogFileHelper.wtireLog(LogFileHelper.getLogHeader(CompareServerInfoActivity.this));
		
	}
	
	private Handler mHandler = new  Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_SUCCESS_AND_START:
				Intent intent = new Intent(CompareServerInfoActivity.this, DeviceTest.class);
				startActivity(intent);
				break;
			case MSG_SEND_SN:
				break;
			case MSG_SEND_SN_TIME_OUT:
				break;
			case MSG_GET_INI_START:
				mProgressDialog.setMessage(getString(R.string.pro_msg_get_ini));
				break;
			case MSG_GET_INI_FINISH:
				if (!mGetClientInfoFinish) {
					mHandler.sendEmptyMessageDelayed(MSG_GET_INI_FINISH, 1000);
				} else {
					onGetIniFinish();
				}
				break;
			case MSG_GET_INI_TIME_OUT:
				if (mProgressDialog.isShowing())
					mProgressDialog.cancel();
				showDialog(DIALOG_GET_INI_TIMEOUT);
				break;
			case MSG_NETWORK_ERR :
				if (mProgressDialog.isShowing())
					mProgressDialog.cancel();
				showDialog(DIALOG_NETWORK_ERR);
				break;
			case MSG_UNKNOWN_ERR:
				if (mProgressDialog.isShowing())
					mProgressDialog.cancel();
				showDialog(DIALOG_UNKNOWN_ERR);
				break;
			case MSG_GET_CLIENT_INFO_FINISH:
				onGetClientInfoFinish();
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_NETWORK_ERR:
			//return new AlertDialog.Builder(this)
			return mAlertDialog
					.setTitle("Error")
					.setMessage(R.string.dialog_msg_network_err)
					.setPositiveButton(R.string.dialog_btn_retry,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									initProgressDialog();
									mProgressDialog.show();
									mGSIH.restart();
								}

							}).create();
		case DIALOG_UNKNOWN_ERR:
			return mAlertDialog
					.setTitle("Error")
					.setMessage(R.string.dialog_msg_unknown_err)
					.setPositiveButton(R.string.dialog_cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									finish();
								}

							}).create();
		case DIALOG_GET_INI_TIMEOUT:
			return mAlertDialog
			.setTitle("Error")
			.setMessage(R.string.dialog_msg_ini_timeout)
			.setPositiveButton(R.string.dialog_btn_retry,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							mProgressDialog.show();
							mGSIH.getIniFromServer();
						}

					}).create();
		case DIALOG_COMPARE_FAIL:
			return mAlertDialog
			.setTitle("Error")
			.setMessage(R.string.dialog_msg_compare_fail)
			.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.cancel();
						}

					}).create();

		default:
			break;
		}
		
		return super.onCreateDialog(id);
	}

	//get time from ntp server
	private void adjustTimeFromServer(String host) {
		final GetServerTimeHelper GSTH = new GetServerTimeHelper(host);
		new Thread(new Runnable() {
			@Override
			public void run() {
				int trytime = 0;
				String temp = GSTH.getDateAndTime();
				while (temp == null) {
					try {
						Thread.sleep(500);
						if (trytime >= 4) {
							break;
						}
					} catch (Exception e) {
					}
					temp = GSTH.getDateAndTime();
					Log.v("====limi====", "temp ="+temp);
					trytime++;
				}
				
				if (temp == null) {
					LogFileHelper.writeLogWithoutClose("Get Server Time Failed! \n");
					//LogFileHelper.adjustTimeFromServerString="Get Server Time Failed! \n";
				} else {
					SystemUtil.SetSystemDateAndTime(temp);
					LogFileHelper.writeLogWithoutClose("Get Server Time and adjust time success! \n");
				//	LogFileHelper.adjustTimeFromServerString="Get Server Time and adjust time success! \n";
				}
				
			}
		}).start();
		
	}
	
	
}

