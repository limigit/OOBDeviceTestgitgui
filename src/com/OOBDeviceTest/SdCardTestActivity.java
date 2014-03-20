package com.OOBDeviceTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ServiceManager;
import android.os.StatFs;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.os.SystemProperties;
import android.text.format.Formatter;
import android.util.Log;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.StorageEventListener;

public class SdCardTestActivity extends Activity {
    private static final String TAG = "SdCardTestActivity";
    private static final String TEST_STRING = "Rockchip UsbHostTest File";
    private final static String SDCARD_PATH = "/mnt/external_sd";
    private static final int BACK_TIME = 2000;
    private static final int SEND_REND_WRITE_SD = 3;
    private static final int R_PASS = 1;
    private static final int R_FAIL = 2;
    private String sdcard_path = null;
    private StringBuilder sBuilder;
   // private SdcardReceiver sdcardReceiver = null;
    public String SUCCESS;
    public String FAIL;
    private boolean isFindSd = false;
    private StorageManager mStorageManager = null;
    TextView mResult;
    private static boolean isSDFirstTest =true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().addFlags(1152);
        setContentView(R.layout.sdcardtest);
        isSDFirstTest =true;
		if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		}
        this.mResult = (TextView) findViewById(R.id.sdresultText);
        this.mResult.setVisibility(View.VISIBLE);
        this.mResult.setGravity(17);

        ControlButtonUtil.initControlButtonView(this);
        findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
       // findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
        SUCCESS = getString(R.string.success);
        FAIL = getString(R.string.fail);
       
        Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onPause();
				isFindSd = false;
				onResume();
			}
		});
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStorageManager.registerListener(mStorageListener);
        StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
        if(storageVolumes.length >= 3){
        	sdcard_path = storageVolumes[1].getPath();
        	Log.d(TAG, " _____ " + sdcard_path + "   ");
        }
        sBuilder = new StringBuilder();
        String sdState = getSdCardState();
		   if (sdState.equals(Environment.MEDIA_MOUNTED)) {
			  	isFindSd=true;
			  	mResult.setText(getString(R.string.resume_findSD));
			  	mHandler.sendEmptyMessageDelayed(SEND_REND_WRITE_SD, 100);
	    	}
        
      
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
    }
    

    public void testSdcard() {
        try {
            String externalVolumeState = mStorageManager.getVolumeState(sdcard_path);
            if (!externalVolumeState.equals(Environment.MEDIA_MOUNTED)) {
                sBuilder.append(getString(R.string.SdCardFail)).append("\n");
                mResult.setText(sBuilder.toString());
                mHandler.sendEmptyMessageDelayed(R_FAIL, 3000);
                isFindSd = false;
                return;
            }
        } catch (Exception rex) {
            rex.printStackTrace();
            isFindSd = false;
            mHandler.sendEmptyMessageDelayed(R_FAIL, 3000);
            return;
        }

        File pathFile = new File(sdcard_path);

        Log.d(TAG, "pathFile = " + pathFile.toString());

        StatFs stat = new StatFs(pathFile.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        String totalSize = Formatter.formatFileSize(this, totalBlocks
                * blockSize);

        String prix = getString(R.string.SdCardFind);
        sBuilder.append(prix + totalSize).append("\n");
        try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        testReadAndWrite();
       
    }

    public void testReadAndWrite() {
        if (isFindSd && dotestReadAndWrite()) {
            sBuilder.append(getString(R.string.SdCardTitle) + SUCCESS);
            ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
			//((Button)findViewById(R.id.btn_Pass)).setClickable(false);
			((Button)findViewById(R.id.btn_Fail)).setClickable(false);
            mHandler.sendEmptyMessageDelayed(R_PASS, BACK_TIME);
        } else {
            sBuilder.append(getString(R.string.SdCardTitle) + FAIL);
            ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
			((Button)findViewById(R.id.btn_Pass)).setClickable(false);
			//((Button)findViewById(R.id.btn_Fail)).setClickable(false);
            mHandler.sendEmptyMessageDelayed(R_FAIL, BACK_TIME);
        }

        mResult.setText(sBuilder.toString());
    }

    private boolean dotestReadAndWrite() {
       // String directoryName = Environment.getExternalStorageDirectory().toString()+ "/test";
        String directoryName = SDCARD_PATH+ "/test";
        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                sBuilder.append(getString(R.string.MakeDir) + FAIL).append("\n");
                return false;
            } else {
                sBuilder.append(getString(R.string.MakeDir) + SUCCESS).append(
                        "\n");
            }
        }
        File f = new File(directoryName, "SDCard.txt");
        try {
            // Remove stale file if any
            if (f.exists()) {
                f.delete();
            }
            if (!f.createNewFile()) {
                sBuilder.append(getString(R.string.CreateFile) + FAIL).append(
                        "\n");
                return false;
            } else {
                sBuilder.append(getString(R.string.CreateFile) + SUCCESS).append(
                        "\n");

                doWriteFile(f.getAbsoluteFile().toString());

                if (doReadFile(f.getAbsoluteFile().toString()).equals(
                        TEST_STRING)) {
                    sBuilder.append(getString(R.string.Compare)).append(SUCCESS).append(
                            "\n");
                } else {
                    sBuilder.append(getString(R.string.Compare)).append(FAIL).append(
                            "\n");
                    return false;
                }
            }

            sBuilder.append(getString(R.string.FileDel)).append(
                    (f.delete() ? SUCCESS : FAIL)).append("\n");
            sBuilder.append(getString(R.string.DirDel)).append(
                    (directory.delete() ? SUCCESS : FAIL)).append("\n");
            return true;
        } catch (IOException ex) {
            Log.e(TAG, "isWritable : false (IOException)!");
            return false;
        }
    }

    public void doWriteFile(String filename) {
        try {
            sBuilder.append(getString(R.string.WriteData)).append("\n");
            OutputStreamWriter osw = new OutputStreamWriter(
                                                            new FileOutputStream(
                                                                                 filename));
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
            sBuilder.append(getString(R.string.ReadData)).append("\n");
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

    /*
    public class SdcardReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReveive ..... " + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
            	 Log.e(TAG, "239 ..... " + intent.getAction()); 
            	testSdcard();
                testReadAndWrite();
            }
        }
    }
   */
    StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
        	if (path.equals(sdcard_path) && newState.equals(Environment.MEDIA_MOUNTED)) {
        	   	isFindSd=true;
        	   	mResult.setText(getString(R.string.resume_findSD));
        	  	mHandler.sendEmptyMessageDelayed(SEND_REND_WRITE_SD, 100);
              // testReadAndWrite();
        	}
        }
    };
    
    
    public void TestResult(int result) { 
        if (result == R_PASS) {
           if(isSDFirstTest){
        	   isSDFirstTest=false;
        	   ((Button) findViewById(R.id.btn_Pass)).performClick();
           }
        	
        } else if (result == R_FAIL) {
        	if(isSDFirstTest){
        		isSDFirstTest=false;
        		((Button) findViewById(R.id.btn_Fail)).performClick();
        	}
        }
    }

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case R_PASS:
                	TestResult(R_PASS);
                    break;
                case R_FAIL:
                    TestResult(R_FAIL);
                    break;
              
                case SEND_REND_WRITE_SD:
                   testSdcard();
                    break;
                    
            }
        };
    };
    
    public static String getSdCardState() {
        try {
        	IMountService mMntSvc = null;
            if (mMntSvc == null) {
                mMntSvc = IMountService.Stub.asInterface(ServiceManager
                                                         .getService("mount"));
            }
            return mMntSvc.getVolumeState(SDCARD_PATH);
        } catch (Exception rex) {
            return Environment.MEDIA_REMOVED;
        }

    }
}
