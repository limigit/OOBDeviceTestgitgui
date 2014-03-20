package com.OOBDeviceTest;

import java.text.DecimalFormat;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.NativeManger;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.R.anim;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.FloatMath;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//鏍″噯锛侊紒锛�//This file is create by cai jianqing!!


public class GsensorActivity extends Activity {
	private static final String TAG = "SensorActivity";
	private static final boolean DBG = false;
	private static final float SIGMA_ACCURATE_VALUE = 0.07f;// 澶氭鍙栨牱鐨勭偣闂磗igma鍊肩殑鍏佽鍙樺寲鑼冨洿
	private static final float ACCURATE_VALUE = 1.0f; // X,Y,Z鏂瑰悜涓婇噰鏍峰�鐨勫厑璁歌宸寖鍥�	
	private static final float Z_GRAVITY_VALUE = 9.8f; // Z鏂瑰悜鐨勭悊璁哄�
	private static final int SAMPLE_DATA_COUNT = 50;
	private static final int DELAY_TIME = 3000;
	private float accurate_value = ACCURATE_VALUE; // X,Y,Z鏂瑰悜涓婇噰鏍峰�鐨勫厑璁歌宸寖鍥�	
	private int accelerometer_rotation_orign_value = 0;
	private Button btn_accurate;
	// private Button close_device;
	private Button btn_cabiration;
	private Button delay;
	private Button btn_examine;
	private Button btn_reset;

	private CheckBox CheckBox;
	public int fd = 0;
	int[] xyz = new int[3];
	public static boolean bIfDebug = false;

	int count = 0;
	private TextView myTextView01;
	// private TextView accelerometer;
	private TextView accelerometer_x;
	private TextView accelerometer_y;
	private TextView accelerometer_z;
	private TextView acc_offset_x;
	private TextView acc_offset_y;
	private TextView acc_offset_z;
	private TextView tv_accurate;
	private TextView acc_sigma_y;
	private TextView acc_sigma_z;
	private TextView tv_acc_info;
	private EditText tv_accurate_edit;
	private AlertDialog.Builder alert1;

	private SensorManager mSensorManager;
	DecimalFormat nf = new DecimalFormat("  #0.000;-#00.000  ");
	DecimalFormat of = new DecimalFormat("  #000;-#000");
	DecimalFormat tds = new DecimalFormat(" #,###,000");
	int delay_mode = 0;
	long TimeNewACC;
	long delayACC;
	static long TimeOldACC;

	float[] sum_acc_XYZ = new float[3];
	float[] sumAccSquare = new float[3];
	float[] sigma_acc = new float[3];
	float[] offset_acc_XYZ = new float[3];// will be save
	int[] save_XYZ = new int[3];
	int countS_acc = 0;
	private boolean isCalibration = false; // 鏄惁宸茬粡杩涜浜嗘牎鍑�	
	private boolean isOffsetHasValue = false;// 鏄惁鏈夊瓨鍌ㄤ簡鍋忓樊鍊�	
	private boolean isGsensorDataOk = false; // 鏄惁鑾峰緱瓒冲鐨勯噰鏍峰�

	private float[] mGravity = new float[3];
	// 閲嶅姏浼犳劅鍣ㄥ綊闆�	
	String str[] = { "Fastest", "Game  ", "UI     ", "Normal " };

	private NativeManger mNativeManger;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor);

    initInfoDlg();
		mNativeManger = new NativeManger();
		tv_accurate = (TextView) findViewById(R.id.tv_accurate_value);
		myTextView01 = (TextView) findViewById(R.id.horizontal);
		// accelerometer = (TextView) findViewById(R.id.accelerometer);
		accelerometer_x = (TextView) findViewById(R.id.accelerometer_x);
		accelerometer_y = (TextView) findViewById(R.id.accelerometer_y);
		accelerometer_z = (TextView) findViewById(R.id.accelerometer_z);
		acc_offset_x = (TextView) findViewById(R.id.acc_offset_x);
		acc_offset_y = (TextView) findViewById(R.id.acc_offset_y);
		acc_offset_z = (TextView) findViewById(R.id.acc_offset_z);
		// acc_sigma_x = (TextView) findViewById(R.id.acc_sigma_x);
		// acc_sigma_y = (TextView) findViewById(R.id.acc_sigma_y);
		tv_acc_info = (TextView) findViewById(R.id.acc_info);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		btn_accurate = (Button) findViewById(R.id.accurate);
		// close_device = (Button) findViewById(R.id.close);
		delay = (Button) findViewById(R.id.delay);
		btn_cabiration = (Button) findViewById(R.id.cab_1);
		// btn_cabiration.setTextColor(R.drawable.olivedrab);
		btn_examine = (Button) findViewById(R.id.examine);
		// btn_examine.setTextColor(R.drawable.olivedrab);
		// CheckBox = (CheckBox) findViewById(R.id.CheckBox);
		// CheckBox.setChecked(false);
		btn_cabiration.setEnabled(false);
		/* 浣跨敤setOnClickListener渚嗙洠鑱戒簨浠�*/
		btn_accurate.setOnClickListener(set_accurate_listener);
		btn_reset = (Button) findViewById(R.id.reset);
		btn_reset.setOnClickListener(reset_listener);

		// close_device.setOnClickListener(close_listener);
		delay.setOnClickListener(delay_listener);
		btn_cabiration.setOnClickListener(cabirationListener);
		btn_examine.setOnClickListener(examineListener);
		ControlButtonUtil.initControlButtonView(this);
		hideButton();
		/*
		 * CheckBox.setOnClickListener(new CheckBox.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { // TODO Auto-generated method
		 * stub if (CheckBox.isChecked()) { btn_cabiration.setEnabled(true);
		 * myTextView01.setText(R.string.str_horizontal); } else {
		 * btn_cabiration.setEnabled(false); //
		 * myTextView01.setText(R.string.text1); 锟絙TextView2锟斤拷锟斤拷堀X"锟叫つ匡拷讦P锟絅"
		 * myTextView01.setText(R.string.str_horizontal); } } });
		 */
		// btn_examine.setEnabled(false);
	}

	final SensorEventListener mSensorListener = new SensorEventListener() {
		// private float[] mGravity = new float[3];
		// private boolean mFailed;
		// private float mAzimuth;
		private long TimeNewACC;

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			DecimalFormat tds = new DecimalFormat(" #,###,000");
			switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				System.arraycopy(event.values, 0, mGravity, 0, 3);
				accelerometer_x.setText("  X : " + nf.format(mGravity[0]));
				accelerometer_y.setText("  Y : " + nf.format(mGravity[1]));
				accelerometer_z.setText("  Z : " + nf.format(mGravity[2]));
				TimeNewACC = event.timestamp;
				delayACC = (long) ((TimeNewACC - TimeOldACC) / 1000000);//
				// Log.d("@@@Sensor.TYPE_ACCELEROMETER@@@", delayACC + " ms");
				// delay.setText("Delay mode=" + str[delay_mode]
				// + tds.format(delayACC) + " ms");
				TimeOldACC = TimeNewACC;

				sum_acc_XYZ[0] += mGravity[0];
				sum_acc_XYZ[1] += mGravity[1];
				sum_acc_XYZ[2] += mGravity[2];
				sumAccSquare[0] += mGravity[0] * mGravity[0];
				sumAccSquare[1] += mGravity[1] * mGravity[1];
				sumAccSquare[2] += mGravity[2] * mGravity[2];
				countS_acc++;
				if (countS_acc == SAMPLE_DATA_COUNT) {
					sum_acc_XYZ[0] /= SAMPLE_DATA_COUNT;
					sum_acc_XYZ[1] /= SAMPLE_DATA_COUNT;
					sum_acc_XYZ[2] /= SAMPLE_DATA_COUNT;
					sigma_acc[0] = FloatMath.sqrt(sumAccSquare[0]
							/ SAMPLE_DATA_COUNT - sum_acc_XYZ[0]
							* sum_acc_XYZ[0]);//
					sigma_acc[1] = FloatMath.sqrt(sumAccSquare[1]
							/ SAMPLE_DATA_COUNT - sum_acc_XYZ[1]
							* sum_acc_XYZ[1]);
					sigma_acc[2] = FloatMath.sqrt(sumAccSquare[2]
							/ SAMPLE_DATA_COUNT - sum_acc_XYZ[2]
							* sum_acc_XYZ[2]);
					offset_acc_XYZ[0] = sum_acc_XYZ[0];
					offset_acc_XYZ[1] = sum_acc_XYZ[1];
					offset_acc_XYZ[2] = sum_acc_XYZ[2];
					sum_acc_XYZ[0] = 0;
					sum_acc_XYZ[1] = 0;
					sum_acc_XYZ[2] = 0;
					sumAccSquare[0] = 0;
					sumAccSquare[1] = 0;
					sumAccSquare[2] = 0;
					countS_acc = 0;
					if (!isGsensorDataOk)// 浣块噰鏍峰�鍒ゆ柇鏈夋晥
					{
						isGsensorDataOk = true;
						// jniUtil.cabiration(save_XYZ);//閫氱煡gsensor閲嶆柊鍔犺浇鍋忓樊鍊�
					}
				}
				break;

			default:
				return;
			}
		}
	};
	private void resetCalibrate()
	{
		
		tv_acc_info.setText(R.string.gsensor_calibrate_process);
		mNativeManger.gSensorStore(0, 0, 0);
		//jniUtil.cabiration(save_XYZ);
		refreshGSensor(true);
		
		countS_acc=0;
		sum_acc_XYZ[0] = 0;
		sum_acc_XYZ[1] = 0;
		sum_acc_XYZ[2] = 0;
		sumAccSquare[0] = 0;
		sumAccSquare[1] = 0;
		sumAccSquare[2] = 0;
		isOffsetHasValue=false;
		btn_reset.setEnabled(false);
	}
	private void autoCalibrate()
	{
		if (DBG)
				Log.d(TAG, "sx=" + sigma_acc[0] + " sy=" + sigma_acc[1]
						+ " sz=" + sigma_acc[2]);
			if (Math.abs(offset_acc_XYZ[0]) < accurate_value
					&& Math.abs(offset_acc_XYZ[1]) < accurate_value
					&& Math.abs(offset_acc_XYZ[2] - Z_GRAVITY_VALUE) < accurate_value) {
				if (sigma_acc[0] < SIGMA_ACCURATE_VALUE
						&& sigma_acc[1] < SIGMA_ACCURATE_VALUE
						&& sigma_acc[2] < SIGMA_ACCURATE_VALUE) {
					acc_offset_x.setText("  X : "
							+ nf.format(offset_acc_XYZ[0]) + "  ");
					acc_offset_y.setText("  Y : "
							+ nf.format(offset_acc_XYZ[1]) + "  ");
					acc_offset_z.setText("  Z : "
							+ nf.format(offset_acc_XYZ[2] - Z_GRAVITY_VALUE)
							+ "  ");
					float fx = offset_acc_XYZ[0] * 101.97162f;// 101.97162f=1000/9.80665f
					float fy = offset_acc_XYZ[1] * 101.97162f;
					float fz = (offset_acc_XYZ[2] - Z_GRAVITY_VALUE) * 101.97162f;
					Log.d(TAG, "fx=" + fx + " fy=" + fy + " fz=" + fz);

					if (fx > 0)
						fx = fx + 0.5f;
					else
						fx = fx - 0.5f;

					if (fy > 0)
						fy = fy + 0.5f;
					else
						fy = fy - 0.5f;

					if (fz > 0)
						fz = fz + 0.5f;
					else
						fz = fz - 0.5f;

					int gx = (int) fx;
					int gy = (int) fy;
					int gz = (int) fz;
					save_XYZ[0] = gx;
					save_XYZ[1] = gy;
					save_XYZ[2] = gz;
					mNativeManger.gSensorStore(gx, gy, gz);
					isCalibration=true;
					isOffsetHasValue=true;
					btn_cabiration.setEnabled(false);
					
					refreshGSensor(true);
					//jniUtil.cabiration(save_XYZ);//閫氱煡gsensor閲嶆柊鍔犺浇鍋忓樊鍊�					//Toast.makeText(SensorActivity.this,
					//			R.string.gsensor_calibrate_success,
					//			Toast.LENGTH_LONG).show();
					//btn_examine.setEnabled(true);
					tv_acc_info.setText(R.string.gsensor_calibrate_success);
					((Button)findViewById(R.id.btn_Pass)).performClick();
					//btn_examine.setTextColor(R.drawable.blue);
				} else {
					//Toast.makeText(SensorActivity.this,
					//		R.string.calculate_error, Toast.LENGTH_SHORT)
					//		.show();
					showInfoDlg(R.string.calculate_error);
					showButton();
					tv_acc_info.setText("");
					acc_offset_x.setText("  X : ");
					acc_offset_y.setText("  Y : ");
					acc_offset_z.setText("  Z : ");
				}
			} else {
				//Toast.makeText(SensorActivity.this,
				//		R.string.gsensor_position_unfit, Toast.LENGTH_LONG)
				//		.show();
				tv_acc_info.setText("");
				showInfoDlg(R.string.gsensor_position_unfit);
				showButton();
				acc_offset_x.setText("  X : ");
				acc_offset_y.setText("  Y : ");
				acc_offset_z.setText("  Z : ");
			}
		btn_reset.setEnabled(true);
		}

	private void showInfoDlg(int info) {
		
		if(alert1!=null){
		alert1.setTitle(info);
		alert1.show();
		}
		/*
		alert = new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setTitle(info)
		
		.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				})
		.show();
		*/
	}
	
	private void initInfoDlg(){
	   alert1= new AlertDialog.Builder(this);
	   alert1.setIcon(android.R.drawable.ic_dialog_info);
	   alert1.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
	
	}

	private Button.OnClickListener cabirationListener = new Button.OnClickListener() {
		public void onClick(View v) {

			if (DBG)
				Log.d(TAG, "sx=" + sigma_acc[0] + " sy=" + sigma_acc[1]
						+ " sz=" + sigma_acc[2]);
			if (Math.abs(offset_acc_XYZ[0]) < accurate_value
					&& Math.abs(offset_acc_XYZ[1]) < accurate_value
					&& Math.abs(offset_acc_XYZ[2] - Z_GRAVITY_VALUE) < accurate_value) {
				if (sigma_acc[0] < SIGMA_ACCURATE_VALUE
						&& sigma_acc[1] < SIGMA_ACCURATE_VALUE
						&& sigma_acc[2] < SIGMA_ACCURATE_VALUE) {
					acc_offset_x.setText("  X : "
							+ nf.format(offset_acc_XYZ[0]) + "  ");
					acc_offset_y.setText("  Y : "
							+ nf.format(offset_acc_XYZ[1]) + "  ");
					acc_offset_z.setText("  Z : "
							+ nf.format(offset_acc_XYZ[2] - Z_GRAVITY_VALUE)
							+ "  ");
					float fx = offset_acc_XYZ[0] * 101.97162f;// 101.97162f=1000/9.80665f
					float fy = offset_acc_XYZ[1] * 101.97162f;
					float fz = (offset_acc_XYZ[2] - Z_GRAVITY_VALUE) * 101.97162f;
					Log.d(TAG, "fx=" + fx + " fy=" + fy + " fz=" + fz);

					if (fx > 0)
						fx = fx + 0.5f;
					else
						fx = fx - 0.5f;

					if (fy > 0)
						fy = fy + 0.5f;
					else
						fy = fy - 0.5f;

					if (fz > 0)
						fz = fz + 0.5f;
					else
						fz = fz - 0.5f;

					int gx = (int) fx;
					int gy = (int) fy;
					int gz = (int) fz;
					save_XYZ[0] = gx;
					save_XYZ[1] = gy;
					save_XYZ[2] = gz;
					mNativeManger.gSensorStore(gx, gy, gz);
					isCalibration = true;
					isOffsetHasValue = true;
					btn_cabiration.setEnabled(false);
					refreshGSensor(true);
					
//					mNativeManger.gSensorCabiration(save_XYZ);//閫氱煡gsensor閲嶆柊鍔犺浇鍋忓樊鍊�			
					Toast.makeText(GsensorActivity.this,
							R.string.gsensor_calibrate_success,
							Toast.LENGTH_LONG).show();
					// btn_examine.setEnabled(true);

					// btn_examine.setTextColor(R.drawable.blue);
				} else {
					// Toast.makeText(SensorActivity.this,
					// R.string.calculate_error, Toast.LENGTH_SHORT)
					// .show();
					showInfoDlg(R.string.calculate_error);
					acc_offset_x.setText("  X : ");
					acc_offset_y.setText("  Y : ");
					acc_offset_z.setText("  Z : ");
				}
			} else {
				// Toast.makeText(SensorActivity.this,
				// R.string.gsensor_position_unfit, Toast.LENGTH_LONG)
				// .show();
				showInfoDlg(R.string.gsensor_position_unfit);
				acc_offset_x.setText("  X : ");
				acc_offset_y.setText("  Y : ");
				acc_offset_z.setText("  Z : ");
			}

		}
	};
	private Button.OnClickListener examineListener = new Button.OnClickListener() {
		public void onClick(View v) {
			if (isCalibration) {
				// mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				// mNativeManger.gSensorCabiration(save_XYZ);
				refreshGSensor(true);
			}

			else
				showInfoDlg(R.string.no_calibration);
			// Toast.makeText(SensorActivity.this, R.string.no_calibration,
			// Toast.LENGTH_SHORT).show();

			// 0涓哄叧闂�1涓哄紑鍚�
			// Settings.System.
			int flag0 = android.provider.Settings.System.getInt(
					getContentResolver(),
					android.provider.Settings.System.ACCELEROMETER_ROTATION, 0);
			Log.d(TAG, "examineListener *******************flag origin="
					+ flag0);
			android.provider.Settings.System.putInt(getContentResolver(),
					android.provider.Settings.System.ACCELEROMETER_ROTATION, 0);
			int flag1 = android.provider.Settings.System.getInt(
					getContentResolver(),
					android.provider.Settings.System.ACCELEROMETER_ROTATION, 0);
			Log.d(TAG, "examineListener *******************flag set to="
					+ flag1);
		}
	};
	private Button.OnClickListener reset_listener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			/*mNativeManger.gSensorStore(0, 0, 0);
			// mNativeManger.gSensorCabiration(save_XYZ);
			refreshGSensor(true);
			isOffsetHasValue = false;*/
			
			resetCalibrate();
			new Handler().postDelayed(new Runnable() {
				public void run() {
					btn_cabiration.setEnabled(true);
					//btn_reset.setEnabled(true);
					autoCalibrate();
					//tv_acc_info.setText(R.string.gsensor_calibrate_success);

				}
			}, DELAY_TIME);

			// btn_cabiration.setEnabled(true);
			// refreshGSensor(true);
			// if()

		}

	};

	// 寰楀埌鍋忕Щ鍊�
	private Button.OnClickListener set_accurate_listener = new Button.OnClickListener() {
		public void onClick(View v) {

			showSetAccurateDlg();

			/*
			 * new AlertDialog.Builder(SensorActivity.this)
			 * .setTitle(R.string.str_dlg_info)
			 * .setIcon(android.R.drawable.ic_dialog_info) .setView(new
			 * EditText(SensorActivity.this))
			 * .setPositiveButton(R.string.str_ok, new
			 * DialogInterface.OnClickListener() { public void
			 * onClick(DialogInterface dialog, int whichButton) {
			 * accurate_value=0.6f;
			 * tv_accurate.setText(String.valueOf(accurate_value)); } })
			 * .setNegativeButton(R.string.str_cancel, null) .show();
			 */

		}
	};

	private Button.OnClickListener delay_listener = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			delay_mode++;
			delay_mode %= 4;

			mSensorManager.registerListener(mSensorListener,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					delay_mode);
			delay.setText("Delay mode=" + str[delay_mode]);
		}

	};

	public void exitActivity(int exitMethod) {
		// throw new RuntimeException("Exit!");
		try {
			switch (exitMethod) {
			case 0:
				System.exit(0);
				break;
			case 1:
				android.os.Process.killProcess(android.os.Process.myPid());
				break;
			case 2:
				finish();
				break;
			}
		} catch (Exception e) {
			finish();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		android.provider.Settings.System.putInt(getContentResolver(),
				android.provider.Settings.System.ACCELEROMETER_ROTATION,
				accelerometer_rotation_orign_value);
		int flag = android.provider.Settings.System.getInt(
				getContentResolver(),
				android.provider.Settings.System.ACCELEROMETER_ROTATION, 1);

		Log.w(TAG, "onPause -*******************get ACCELEROMETER_ROTATION="
				+ flag);
		mSensorManager.unregisterListener(mSensorListener);
		// exitActivity(1);
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				delay_mode);
		// delay.setText("Delay mode=" + str[delay_mode]);
		tv_accurate.setText(String.valueOf(ACCURATE_VALUE));
		tv_acc_info.setText(R.string.gsensor_calibrate_process);
		resetCalibrate();


			
		new Handler().postDelayed(new Runnable() {
			public void run() {
				btn_cabiration.setEnabled(true);
				autoCalibrate();
				//tv_acc_info.setText(R.string.gsensor_calibrate_success);
			}
		}, DELAY_TIME);
		accelerometer_rotation_orign_value = android.provider.Settings.System
				.getInt(getContentResolver(),
						android.provider.Settings.System.ACCELEROMETER_ROTATION,
						0);
		;
		android.provider.Settings.System.putInt(getContentResolver(),
				android.provider.Settings.System.ACCELEROMETER_ROTATION, 0);

		int flag = android.provider.Settings.System.getInt(
				getContentResolver(),
				android.provider.Settings.System.ACCELEROMETER_ROTATION, 0);
		Log.w(TAG,
				"onResume +*******************ACCELEROMETER_ROTATION:origin="
						+ accelerometer_rotation_orign_value + " set to: "
						+ flag);

		/*
		 * // 濡傛灉宸茬粡鍦ㄥ亸宸�锛屽垯鏃犳硶杩涜鏍″噯 int loadXYZ[] = jniUtil.load(); if (loadXYZ[0] !=
		 * 0 && loadXYZ[1] != 0 && loadXYZ[2] != 0) { isOffsetHasValue = true;
		 * btn_cabiration.setEnabled(false); } else { isOffsetHasValue = false;
		 * btn_cabiration.setEnabled(true); }
		 */
	}

	private void showSetAccurateDlg() {
		// final
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.accurate_dlg, null);
		final AlertDialog.Builder alert = new AlertDialog.Builder(
				GsensorActivity.this);
		tv_accurate_edit = (EditText) textEntryView
				.findViewById(R.id.accurate_edit);
		/*
		 * tv_accurate_edit.addTextChangedListener(new TextWatcher(){
		 * 
		 * @Override public void afterTextChanged(Editable s) { // TODO
		 * Auto-generated method stub
		 * 
		 * float value_get=Float.valueOf(s.toString());
		 * Log.d(TAG,"value_get="+value_get); if(value_get>1 || value_get < 0) {
		 * Toast.makeText(SensorActivity.this, R.string.accurate_value_error,
		 * Toast.LENGTH_SHORT) .show(); return; }
		 * 
		 * }
		 * 
		 * @Override public void beforeTextChanged(CharSequence s, int start,
		 * int count, int after) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void onTextChanged(CharSequence s, int start, int
		 * before, int count) { // TODO Auto-generated method stub
		 * 
		 * }});
		 */

		alert.setTitle(R.string.str_dlg_info);
		alert.setView(textEntryView);
		alert.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						String strAccurate = tv_accurate_edit.getText()
								.toString();
						if (strAccurate.length() != 0) {
							// setTitle(asdu.getText().toString());
							float accurate_get = Float.valueOf(strAccurate);
							if (accurate_get > 1 || accurate_get < 0) {
								Toast.makeText(GsensorActivity.this,
										R.string.accurate_value_error,
										Toast.LENGTH_SHORT).show();
								return;
							}
							accurate_value = accurate_get;

							tv_accurate.setText(strAccurate);

							if (DBG)
								Log.d(TAG, "showSetAccurateDlg:accurate_value="
										+ accurate_value);
						} else {
							Toast.makeText(GsensorActivity.this,
									R.string.accurate_value_error,
									Toast.LENGTH_SHORT).show();
						}
					}
				});
		alert.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
		alert.show();

	}

	private void refreshGSensor(boolean isEnable) {
		// RotationPolicy.setRotationLockForAccessibility(getActivity(),
		// !mToggleLockScreenRotationPreference.isChecked());
		Log.d(TAG, "refreshGSensor======================================");
		mSensorManager.unregisterListener(mSensorListener);
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				delay_mode);
		// jniUtil.cabiration(save_XYZ);
		// SensorActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// SensorActivity.SetRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}
	
	
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
	
	private void hideButton() {
		((Button) findViewById(R.id.btn_Pass)).setVisibility(View.INVISIBLE);
		((Button) findViewById(R.id.btn_Fail)).setVisibility(View.INVISIBLE);
		((Button) findViewById(R.id.btn_Retest)).setVisibility(View.GONE);
	}
	
	private void showButton() {
//		((Button) findViewById(R.id.btn_Pass)).setVisibility(View.VISIBLE);
		((Button) findViewById(R.id.btn_Fail)).setVisibility(View.VISIBLE);
	}
	
}
