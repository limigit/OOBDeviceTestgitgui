package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.OOBDeviceTest.WifiTestActivity.MyBroadcastReceiver;
import com.OOBDeviceTest.helper.Recorder;
import com.OOBDeviceTest.helper.SystemUtil;
import com.OOBDeviceTest.helper.VUMeter;
import com.OOBDeviceTest.view.GsensorBall;
import com.OOBDeviceTest.view.KeyTestView;
import com.OOBDeviceTest.view.TestView;

import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.StorageEventListener;
import android.os.SystemProperties;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.os.ServiceManager;
import android.os.storage.IMountService;

public class FirstRun extends Activity implements SurfaceHolder.Callback{
	private static final String TAG = "FirstRun";
	
	public enum TEST_STATUS{
		WAITING,
		TESTING,
		FAILED,
		SUCCEED,
	}	
	private TEST_STATUS wifi_test_status = TEST_STATUS.WAITING;
	private TEST_STATUS sdcard_test_status = TEST_STATUS.WAITING;
	private TEST_STATUS otg_test_status = TEST_STATUS.WAITING;

	private View v = null;
	private WindowManager wm = null;
	KeyguardLock kl = null;
	
	private Resources mRes;
	private StorageManager mStorageManager = null;
	
	private int testszie = 21;
	private TextView productname = null;
	private TextView productversion = null;
	private TextView nandstorage = null;
	private TextView availablenandstorage = null;	

	private TestView mSDcardTestView = null;
	private TestView mUsbHostTestView = null;
	private TestView mWifiTestView = null;
    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiReceiver;
    private Handler mWifiHandler;
    private boolean mReadyToTest = false;
    private final static int WIFI_MSG_SCAN = 0;
    
    private SensorManager sensorManager;
	private SensorEventListener lsn = null;
	private GsensorBall mGsensorBall;
	private TextView gsensortext = null;
	
	private Camera mCameraDevice;
	private View nocamera;
	private boolean hasCamera = false;
	private ContentProviderClient mMediaProviderClient;
	private SurfaceView mSurfaceView;
	private Button mSwitchBut;
	private SurfaceHolder mSurfaceHolder = null;
	private int mNumberOfCameras = 0;
	private int mCurrentCameraId = 0;
	
	private final static int MAXIMUM_BRIGHTNESS = 255;
	private final static int MINIMUM_BRIGHTNESS = 0;
	private final static int ONE_STAGE = 5;
	
	private ProgressBar progressBar;
	private Button brightnessbut;
	private boolean isTestBrightness = false;
	private BrightnessHandler mBrightnessHandler = new BrightnessHandler();
	private static final int BRIGHTNESS_MSG = 0;
	
	private VUMeter mVUMeter;
	private Button recordtestbutton;
	private TextView recordtext;
	private Recorder mRecorder;
	private AudioManager mAudioManager;
	private int mOldVolume;
	private boolean mSpeakerOn = false;
	private RecordHandler mRecordHandler = new RecordHandler();
	private final static int RECORD_TIME = 5;	

	private static final String CURRENT_PATH = "/sys/class/power_supply/battery/capacity";
	PowerBroadcastReceiver mPowerBroadcastReceiver = new PowerBroadcastReceiver();
	private TextView mChargeStatus;
	private TextView mVoltage;
	private TextView mCurrent;
	private TextView mCapacity;
	private TextView mPlug;
	
	private KeyTestView mKeyTestView;
	private String mKeyNames[] = {	"VOLUME+", 
									"VOLUME-",
									/*"HOME", 
									"MENU", 
									"BACK",
									"SEARCH"*/};
	private int mKeyCodes[] = {		KeyEvent.KEYCODE_VOLUME_UP, 
									KeyEvent.KEYCODE_VOLUME_DOWN,		
									/*KeyEvent.KEYCODE_HOME,
									KeyEvent.KEYCODE_MENU,
									KeyEvent.KEYCODE_BACK,
									KeyEvent.KEYCODE_SEARCH*/};
	
	private Button mSingletestBut;
	private Button mClosefirstrunBut;
	
	public static String flash_path = null;
	public static String sdcard_path = null;
	public static String usb_path = null;
	private int flash_pit = 0; 
	private int sdcard_pit = 1; 
	private int usb_pit = 2; 
	private StorageVolume[] storageVolumes = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.firstrun_layout);
		initView();
		GetDevieInformation();
		InitStorage();
	}
	private void initView(){
		v = new View(FirstRun.this);
		wm = (WindowManager)FirstRun.this.getSystemService(WINDOW_SERVICE);        
        KeyguardManager km= (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("unLock");
		
		mRes = getResources();
		productname = (TextView)findViewById(R.id.product_name);
		productversion = (TextView)findViewById(R.id.product_version);
		nandstorage = (TextView)findViewById(R.id.nand_storage);
		availablenandstorage = (TextView)findViewById(R.id.available_nand_storage);
		productname.setTextSize(testszie + 2);
		productversion.setTextSize(testszie);
		nandstorage.setTextSize(testszie);
		availablenandstorage.setTextSize(testszie);
		
		mWifiTestView = (TestView)findViewById(R.id.wifitestview);
		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		mWifiReceiver = new MyBroadcastReceiver();		
		mWifiHandler = new WifiHandler();
		
		mSDcardTestView = (TestView)findViewById(R.id.sdcardtestview);
		mUsbHostTestView = (TestView)findViewById(R.id.usbhosttestview);

		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		mGsensorBall = (GsensorBall)findViewById(R.id.gsensorball);
		gsensortext = (TextView)findViewById(R.id.gsensortext);
		lsn = new SensorEventListener() {
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
			public void onSensorChanged(SensorEvent e) {
				float x = e.values[0];
				float y = e.values[1];
				float z = e.values[2];
				mGsensorBall.setXYZ(x, y, z);
				gsensortext.setText(mRes.getString(R.string.GsensorTest) + "\nX: " + x + "\nY: " + y + "\nZ: " + z);
			}
		};
		
		nocamera = findViewById(R.id.nocamera);
		mSurfaceView = (SurfaceView)findViewById(R.id.camera_preview);
		mSwitchBut = (Button)findViewById(R.id.camera_switch_btu);
		mSwitchBut.setOnClickListener(mOnClickListener);
		mNumberOfCameras = Camera.getNumberOfCameras();
		if(mNumberOfCameras <= 1)
			mSwitchBut.setVisibility(View.GONE);
		
		progressBar = (ProgressBar) findViewById(R.id.brightnessBar);
		progressBar.setClickable(false);
		progressBar.setMax(MAXIMUM_BRIGHTNESS);
		brightnessbut = (Button)findViewById(R.id.brightnesstestbut);
		brightnessbut.setOnClickListener(mOnClickListener);
		
		mVUMeter = (VUMeter) findViewById(R.id.uvMeter);
		mRecorder = new Recorder();
		mVUMeter.setRecorder(mRecorder);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mOldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		recordtestbutton = (Button)findViewById(R.id.recordtestbutton);
		recordtestbutton.setOnClickListener(mOnClickListener);
		recordtext = (TextView)findViewById(R.id.recordtext);		

		mChargeStatus = (TextView) findViewById(R.id.chargeStatusText);
		mVoltage = (TextView) findViewById(R.id.voltageText);
		mCurrent = (TextView) findViewById(R.id.currentText);
		mCapacity = (TextView) findViewById(R.id.capacityText);
		mPlug = (TextView) findViewById(R.id.plugText);
		
		mKeyTestView = (KeyTestView)findViewById(R.id.keytestview);
		if(mKeyNames.length == mKeyCodes.length){
			for(int i = 0; i < mKeyNames.length; i ++){
				mKeyTestView.addKey(mKeyNames[i], mKeyCodes[i]);
			}
		}
		mSingletestBut = (Button)findViewById(R.id.singletest);
		mSingletestBut.setOnClickListener(mOnClickListener);
		mClosefirstrunBut = (Button)findViewById(R.id.closefirstrun);
		mClosefirstrunBut.setOnClickListener(mOnClickListener);
	}
	@Override
	protected void onResume() {
		super.onResume();	
        updateMemoryStatus(flash_path);
        kl.disableKeyguard(); 
        addWindow();
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		localIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		localIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		registerReceiver(mWifiReceiver, localIntentFilter);
		Log.i("Jeffy-------", "try to enable wifi");
		mWifiTestView.setStatus(TEST_STATUS.TESTING);
		mWifiManager.setWifiEnabled(true);
		
		mSDcardTestView.setStatus(TEST_STATUS.TESTING);
		if(testSdcard()){
			mSDcardTestView.setStatus(TEST_STATUS.SUCCEED);
		}else{
			mSDcardTestView.setStatus(TEST_STATUS.FAILED);
		}

		mUsbHostTestView.setStatus(TEST_STATUS.TESTING);
		if(testUSBHost()){
			mUsbHostTestView.setStatus(TEST_STATUS.SUCCEED);
		}else{
			mUsbHostTestView.setStatus(TEST_STATUS.FAILED);
		}		

		Sensor sensors = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(lsn, sensors, SensorManager.SENSOR_DELAY_NORMAL);
		
		SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		registerReceiver(mPowerBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
	@Override
	protected void onPause() {
		super.onPause();
		removeWindow();
        kl.reenableKeyguard();
		unregisterReceiver(mWifiReceiver);
		sensorManager.unregisterListener(lsn);
		mWifiHandler.removeMessages(WIFI_MSG_SCAN);
		Log.d(TAG, " __________________---------- oldBrightness = " + oldBrightness);
		
		switch (mRecorder.state()) {
			case Recorder.IDLE_STATE:
				mRecorder.delete();
				break;
			case Recorder.PLAYING_STATE:
				mRecorder.stop();
				mRecorder.delete();
				break;
			case Recorder.RECORDING_STATE:
				mRecorder.stop();
				mRecorder.clear();
				break;
		}
	    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOldVolume, 0);
		if (mSpeakerOn) {
			mAudioManager.setSpeakerphoneOn(false);
		}
		unregisterReceiver(mPowerBroadcastReceiver);
	}
	
	@Override
	protected void onDestroy() {
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
        if(mCameraDevice != null)
        	mCameraDevice.release();
		super.onDestroy();
	}
	
	/******************************************************************
	 * about DeviceInfo
	 */
	private void GetDevieInformation(){
		productname.setText(Build.DEVICE);
		productversion.setText(mRes.getString(R.string.Firmwareversion) + Build.DISPLAY);
	}

	/******************************************************************
	 * about DeviceStorage()
	 */
	private void InitStorage(){
		if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            mStorageManager.registerListener(mStorageListener);
            storageVolumes = mStorageManager.getVolumeList();
            if(storageVolumes.length >= 3){
            	flash_path = storageVolumes[flash_pit].getPath();
            	sdcard_path = storageVolumes[sdcard_pit].getPath();
            	usb_path = storageVolumes[usb_pit].getPath();
            	Log.d(TAG, " _____ " + flash_path + "   " + sdcard_path + "   " + usb_path);
            }
        }
	}	
	private void updateMemoryStatus(String path) {
        String status = mStorageManager.getVolumeState(path);
        if (path.equals(flash_path)) {
            status = mStorageManager.getVolumeState(flash_path);
        }
        String readOnly = "";
        if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            status = Environment.MEDIA_MOUNTED;
            readOnly = mRes.getString(R.string.read_only);
        }
 
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            try {
                //File path = Environment.getExternalStorageDirectory();
                StatFs stat = new StatFs(path);
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availableBlocks = stat.getAvailableBlocks();

                if (path.equals(flash_path)) {
                	nandstorage.setText(mRes.getString(R.string.total_space) + " : " + formatSize(totalBlocks * blockSize));
                	availablenandstorage.setText(mRes.getString(R.string.available_space) + " : " + formatSize(availableBlocks * blockSize) + readOnly);
                }
            } catch (IllegalArgumentException e) {
                status = Environment.MEDIA_REMOVED;
            }            
        } else {
            if (path.equals(flash_path)) {
            	nandstorage.setText(mRes.getString(R.string.nand_unavailable));
            	availablenandstorage.setText(mRes.getString(R.string.nand_unavailable));
            }
        }
    }
	private String formatSize(long size) {
        return Formatter.formatFileSize(this, size);
    }
	StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.d(TAG, "--------------->>>>> Received storage state changed, path = " +
                    path + ",      changed state from " + oldState +
                    " to " + newState);
            if (path.equals(flash_path) 
                             && !newState.equals(Environment.MEDIA_MOUNTED)) {
            } else {
                updateMemoryStatus(flash_path);
            }
            if(path.equals(sdcard_path)){
        		if(testSdcard()){
        			mSDcardTestView.setStatus(TEST_STATUS.SUCCEED);
        		}else{
        			mSDcardTestView.setStatus(TEST_STATUS.FAILED);
        		}
            }else if(path.equals(usb_path)){
        		if(testUSBHost()){
        			mUsbHostTestView.setStatus(TEST_STATUS.SUCCEED);
        		}else{
        			mUsbHostTestView.setStatus(TEST_STATUS.FAILED);
        		}
            }
        }
    };

    
	/******************************************************************
	 * about WiFitest
	 */    
    class WifiHandler extends Handler {
		public void handleMessage(Message msg) {
			switch(msg.what){
			case WIFI_MSG_SCAN:
				removeMessages(WIFI_MSG_SCAN);
				mWifiManager.startScan();
				break;
			}
		}
    }
    class MyBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i("Jeffy", "action:" + action);
			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
						WifiManager.WIFI_STATE_UNKNOWN);
				if (state == WifiManager.WIFI_STATE_ENABLED) {
					mWifiHandler.sendEmptyMessage(WIFI_MSG_SCAN);
				}
			}

			if (WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)) {
				boolean connected = intent.getBooleanExtra(
						WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
				if (connected && mReadyToTest) {
					Log.d("Jeffy===", "already connect to:" + mWifiManager.getConnectionInfo().getSSID());
//					mHandler.sendEmptyMessageDelayed(MSG_FINISH_TEST, 1000);
					mWifiTestView.setStatus(TEST_STATUS.SUCCEED);
				}
			}
			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
				List<ScanResult> resultList = mWifiManager.getScanResults();
				if ((resultList != null) && (!resultList.isEmpty())) {
					mReadyToTest = true;
					Log.d("Jeffy===", " ____________-------- resultList.size() = " + resultList.size());
					mWifiTestView.setStatus(TEST_STATUS.SUCCEED);
				}
			}

		}
	}    

	/******************************************************************
	 * about SDcard
	 */
    private static final String TEST_STRING = "Rockchip UsbHostTest File";
    public boolean testSdcard() {
        try {
            String externalVolumeState = mStorageManager.getVolumeState(sdcard_path);

            Log.d(TAG, " __________----------- testSdcard(),    externalVolumeState = " + externalVolumeState);
            if (!externalVolumeState.equals(Environment.MEDIA_MOUNTED)) {
                return false;
            }
        } catch (Exception rex) {
            rex.printStackTrace();
        	//test sdcard fail
            return false;
        }
        Log.d(TAG, " __________----------- testSdcard() __ begin test read and write");
        return testReadAndWrite(sdcard_path + "/test");
    }
    public boolean testReadAndWrite(String directoryName) {
       return dotestReadAndWrite(directoryName);
    }
    private boolean dotestReadAndWrite(String directoryName) {
        File directory = new File(directoryName);
        Log.d(TAG, " _______-------- dotestReadAndWrite()0, directoryName = " + directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                Log.d(TAG, " _______-------- dotestReadAndWrite()0 1, directoryName = " + directoryName);
                return false;
            }
        }
        File f = new File(directoryName, "storagetest.txt");
        try {
            if (f.exists()) {
                f.delete();
            }
            if (!f.createNewFile()) {
                return false;
            } else {
                doWriteFile(f.getAbsoluteFile().toString());
                if (!doReadFile(f.getAbsoluteFile().toString()).equals(TEST_STRING)) {
                    return false;
                }
            }
            return true;
        } catch (IOException ex) {
            Log.e(TAG, "isWritable : false (IOException)!");
            return false;
        }
    }

    public void doWriteFile(String filename) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename));
            osw.write(TEST_STRING, 0, TEST_STRING.length());
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String doReadFile(String filename) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader
                    (new FileInputStream(filename)));
            String data = null;
            StringBuilder temp = new StringBuilder();
            while ((data = br.readLine()) != null) {
                temp.append(data);
            }
            br.close();
            Log.e(TAG, "Readfile " + temp.toString());
            return temp.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

	/******************************************************************
	 * about USBHost
	 */
    public boolean testUSBHost() {
        try {
            String externalVolumeState = mStorageManager.getVolumeState(usb_path).toString();

            Log.d(TAG, " __________----------- testUSBHost(),    externalVolumeState = " + externalVolumeState);
            if (!externalVolumeState.equals(Environment.MEDIA_MOUNTED)) {
                return false;
            }
        } catch (Exception rex) {
            rex.printStackTrace();
            return false;
        }
        Log.d(TAG, " __________----------- testUSBHost() __ begin test read and write");
        return testReadAndWrite(usb_path + "/test");
    }
    
	/******************************************************************
	 * about Camera
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {	
        Log.d(TAG, "---->>>>>>>>>> surfaceChanged()");
		if (holder.getSurface() == null) {
            Log.d(TAG, "---- surfaceChanged(),  holder.getSurface() == null");
            return;
        }
		mSurfaceHolder = holder;
		try{
			hasCamera = true;
			nocamera.setVisibility(View.GONE);
			mCameraDevice = Camera.open(mCurrentCameraId);
		}catch(Exception e){
			hasCamera = false;
			nocamera.setVisibility(View.VISIBLE);
			mSwitchBut.setVisibility(View.GONE);
			Log.e(TAG, " ____________- camera error");
			return;
		}
		try{
			mCameraDevice.setPreviewDisplay(mSurfaceHolder);
		}catch(IOException e){
			e.printStackTrace();
		}
		mCameraDevice.startPreview();
	}
	public void surfaceCreated(SurfaceHolder holder) {
	}
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(mCameraDevice != null){
			mCameraDevice.stopPreview();
	    	mCameraDevice.release();
		}
		mSurfaceHolder = null;
	}
	OnClickListener mOnClickListener = new OnClickListener(){
		public void onClick(View arg0) {
			switch(arg0.getId()){
			case R.id.camera_switch_btu:
				mCameraDevice.stopPreview();
				mCameraDevice.release();
				mCameraDevice = null;
				mCurrentCameraId = (mCurrentCameraId + 1) % mNumberOfCameras;
				mCameraDevice = Camera.open(mCurrentCameraId);
				mSurfaceHolder = null;
				mSurfaceView.setVisibility(View.GONE);
				mSurfaceView.setVisibility(View.VISIBLE);
				break;
			case R.id.brightnesstestbut:
				if(isTestBrightness){
					mBrightnessHandler.removeMessages(0);
					setBrightness(oldBrightness);
					brightnessbut.setText(R.string.BrightnessTitle);
				}else{
					mBrightnessHandler.sendEmptyMessage(0);
					brightnessbut.setText(R.string.BrightnessTitleStop);
				}
				isTestBrightness = !isTestBrightness;
				break;
			case R.id.recordtestbutton:
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
						mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
				mSpeakerOn = mAudioManager.isSpeakerphoneOn();
				if (!mSpeakerOn) {
					mAudioManager.setSpeakerphoneOn(true);
				}
				mRecordHandler.sendEmptyMessage(MSG_TEST_MIC_START);
				recordtestbutton.setEnabled(false);
				break;
			case R.id.singletest:
				Intent tmp = new Intent(FirstRun.this, DeviceTest.class);
				FirstRun.this.startActivity(tmp);
				break;
			case R.id.closefirstrun:
				FirstRun.this.finish();
				break;
			default:
				break;
			}
		}		
	};	

	/******************************************************************
	 * about brightness
	 */
	int mBrightness = 30;
	int oldBrightness = 200;
	boolean increase = true;
	private void setBrightness(int paramInt) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		float brightness = (float) paramInt / MAXIMUM_BRIGHTNESS;
		lp.screenBrightness = brightness;
		if(brightness == 0.0) return;
		getWindow().setAttributes(lp);
	}	
	class BrightnessHandler extends Handler {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int delay = 25;
			if (increase) {
				mBrightness += ONE_STAGE;
				if (mBrightness >= MAXIMUM_BRIGHTNESS) {
					mBrightness = MAXIMUM_BRIGHTNESS;
					increase = false;
					delay = 200;
				}
			} else {
				mBrightness -= ONE_STAGE;
				if (mBrightness <= MINIMUM_BRIGHTNESS) {
					mBrightness = MINIMUM_BRIGHTNESS;
					increase = true;
					delay = 200;
				}
			}
			progressBar.setProgress(mBrightness);
			setBrightness(mBrightness);			
			sendEmptyMessageDelayed(0, delay);
		}
	}
	
	/******************************************************************
	 * about mic
	 */
	int mTimes;
	private static final int MSG_TEST_MIC_ING = 0;
	private static final int MSG_TEST_MIC_OVER = 1;
	private static final int MSG_TEST_MIC_START = 2;
	class RecordHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			default:
			case MSG_TEST_MIC_START:
				removeMessages(MSG_TEST_MIC_START);
				mTimes = RECORD_TIME;
				recordtext.setText("  "+mTimes+" ");
				mRecorder.startRecording(3, ".amr");
				sendEmptyMessageDelayed(MSG_TEST_MIC_ING, 1000L);
				break;
			case MSG_TEST_MIC_ING:
				if (mTimes > 0) {
					recordtext.setText("  "+mTimes+" ");
					mTimes--;
					Log.i(TAG, "mTimes=" + mTimes);
					sendEmptyMessageDelayed(MSG_TEST_MIC_ING, 1000L);
				} else {
					removeMessages(MSG_TEST_MIC_ING);
					sendEmptyMessage(MSG_TEST_MIC_OVER);

				}
				break;
			case MSG_TEST_MIC_OVER:
				removeMessages(MSG_TEST_MIC_OVER);
				mRecorder.stopRecording();
				if (mRecorder.sampleLength() > 0) {
					recordtext.setText(R.string.HeadsetRecodrSuccess);
					mRecorder.startPlayback();
				} else {
					recordtext.setText(R.string.RecordError);
				}
				recordtestbutton.setEnabled(true);
				break;
			}			
			mVUMeter.invalidate();
		}
	}

	/******************************************************************
	 * about Power
	 */
	class PowerBroadcastReceiver extends BroadcastReceiver {
		public void onReceive(Context paramContext, Intent intent) {
			String action = intent.getAction();
			if (!Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				return;
			}
			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
			Log.e("Jeffy", "plugged:" + plugged);
			int current = -1;
			try {
				String currentStr = SystemUtil.execScriptCmd("cat "
						+ CURRENT_PATH, DeviceTest.TEMP_FILE_PATH, true);
				if (currentStr.length() > 0) {
					current = Integer.parseInt(currentStr);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			String statusString = "";
			switch (status) {
			case BatteryManager.BATTERY_STATUS_UNKNOWN:
				statusString = "Unknown";
				break;
			case BatteryManager.BATTERY_STATUS_CHARGING:
				statusString = "Charging";
				break;
			case BatteryManager.BATTERY_STATUS_DISCHARGING:
				statusString = "Discharging";
				break;
			case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
				statusString = "Not Charging";
				break;
			case BatteryManager.BATTERY_STATUS_FULL:
				statusString = "Full";
				break;
			}
			mChargeStatus.setText(getString(R.string.ChargeState) + statusString);
			mVoltage.setText(getString(R.string.Voltage) + voltage + "mV");
			if (current != -1) {
				mCurrent.setText("Current:" + (current / 1000) + "mA");
			} else {
				mCurrent.setVisibility(View.GONE);
			}
			mCapacity.setText(getString(R.string.Capacity) + (level * 100 / scale) + "%");
			boolean acPlugin = false;
			String pluggedStr = "";
			switch (plugged) {
			case BatteryManager.BATTERY_PLUGGED_AC:
				acPlugin = true;
				pluggedStr = "AC";
				break;
			case BatteryManager.BATTERY_PLUGGED_USB:
				pluggedStr = "USB";
				break;
			default:
				pluggedStr = "Unplugged";
				break;
			}
			mPlug.setText(getString(R.string.Plug) + pluggedStr);
		}
	}

	/******************************************************************
	 * about Key
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.d(TAG, " _____________---- dispatchKeyEvent(),   " + event.getKeyCode());
		switch(event.getAction()){
		case KeyEvent.ACTION_DOWN:
			mKeyTestView.setKeyDown(event.getKeyCode());
			break;
		case KeyEvent.ACTION_UP:
			mKeyTestView.setKeyUp(event.getKeyCode());
			break;
		}
		return true;
	}
	@Override
	public void onAttachedToWindow() {
	    Log.d(TAG, "onAttachedToWindow");
		Log.d(TAG, "____________________________ ____________ onAttachedToWindow type: "  + getWindow().getAttributes().type);
//		getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
////		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
//		getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
		super.onAttachedToWindow();
	}
	
	private void addWindow(){
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		 params.type = WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;
//		 params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
//		 params.flags = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
//                 |  WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
		params.width = 1;//WindowManager.LayoutParams.FILL_PARENT;
		params.height = 1;//WindowManager.LayoutParams.FILL_PARENT;
		params.format=PixelFormat.TRANSLUCENT;				
		params.gravity=Gravity.LEFT|Gravity.TOP;
	   //����Ļ���Ͻ�Ϊԭ�㣬����x��y��ʼֵ
		params.x = 0;
		params.y = 0;
		wm.addView(v, params);
		v.requestFocus();
		v.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Log.d(TAG, " _____________---- onKey(),   " + event.getKeyCode());
				switch(event.getAction()){
				case KeyEvent.ACTION_DOWN:
					mKeyTestView.setKeyDown(event.getKeyCode());
					break;
				case KeyEvent.ACTION_UP:
					mKeyTestView.setKeyUp(event.getKeyCode());
					break;
				}
				return false;
			}
		});		
	}	
	private void removeWindow(){
		wm.removeView(v);
	}
}