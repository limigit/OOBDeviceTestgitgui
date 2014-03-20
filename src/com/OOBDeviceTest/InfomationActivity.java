package com.OOBDeviceTest;

import java.io.File;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.SystemInfoUtil;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.SystemProperties;
import android.os.storage.StorageEventListener;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;



public class InfomationActivity extends Activity {
	private final static String TAG = "InfomationActivity";
	
	private TextView mCPUInfoTV;
	private TextView mMemoryInfoTV;
	private TextView mNandFlashTotalTV;
	private TextView mNandFlashAvailTV;
	private TextView mInternalAvailTV;
	
	public  String flash_path = null;
	public  String sdcard_path = null;
	public  String usb_path = null;
	private int flash_pit = 0; 
	private int sdcard_pit = 1; 
	private int usb_pit = 2; 
	private StorageVolume[] storageVolumes = null;
    private StorageManager mStorageManager = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infomation);
		InitStorage();
		initRes();
		ControlButtonUtil.initControlButtonView(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateView();
	}
	
	private void initRes() {
		mCPUInfoTV = (TextView) findViewById(R.id.cpuinfo_tv);
		mMemoryInfoTV = (TextView) findViewById(R.id.meminfo_tv);
		mNandFlashTotalTV = (TextView) findViewById(R.id.nand_total_tv);
		mNandFlashAvailTV = (TextView) findViewById(R.id.nand_avail_tv);
		mInternalAvailTV  = (TextView) findViewById(R.id.internal_avail_tv);
	}
	
	private String getCpuInfoString() {
		StringBuilder cpuInfoSB = new StringBuilder(); 
		cpuInfoSB.append(SystemInfoUtil.getCpuName()).append("  ");
		cpuInfoSB.append(SystemInfoUtil.getNumCores()+"").append(" * ").append(""+SystemInfoUtil.getMaxCpuFreq()+" Hz");
		return cpuInfoSB.toString();
	}
	
	private void updateView() {
		mCPUInfoTV.setText(getCpuInfoString());
		mMemoryInfoTV.setText(SystemInfoUtil.GetMemInfo1(this));
		updateMemoryStatus(flash_path);
	}
	
	
	private void updateMemoryStatus(String path) {
        String status = SystemProperties.get("EXTERNAL_STORAGE_STATE","unmounted");
        if (path.equals(flash_path)) {
            status = mStorageManager.getVolumeState(path);
        }
        String readOnly = "";
        if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            status = Environment.MEDIA_MOUNTED;
            readOnly = getString(R.string.read_only);
        }
 
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            try {
                //File path = Environment.getExternalStorageDirectory();
                StatFs stat = new StatFs(path);
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availableBlocks = stat.getAvailableBlocks();

                if (path.equals(flash_path)){
                    mNandFlashTotalTV.setText(getString(R.string.total_space)+":"+formatSize(totalBlocks * blockSize));
                    mNandFlashAvailTV.setText(getString(R.string.available_space)+":"+formatSize(availableBlocks * blockSize) + readOnly);
                }
            } catch (IllegalArgumentException e) {
                // this can occur if the SD card is removed, but we haven't received the 
                // ACTION_MEDIA_REMOVED Intent yet.
                status = Environment.MEDIA_REMOVED;
            }
            
        } else {
            if (path.equals(flash_path)){
            	mNandFlashTotalTV.setText(getString(R.string.nand_unavailable));
            	mNandFlashAvailTV.setText(getString(R.string.nand_unavailable));
                if (status.equals(Environment.MEDIA_UNMOUNTED) ||
                    status.equals(Environment.MEDIA_NOFS) ||
                    status.equals(Environment.MEDIA_UNMOUNTABLE) ) {
                }
            }
        }

        File dataPath = Environment.getDataDirectory();
        StatFs stat = new StatFs(dataPath.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        mInternalAvailTV.setText(getString(R.string.available_space)+":"+formatSize(availableBlocks * blockSize));
    }
    
    private String formatSize(long size) {
        return Formatter.formatFileSize(this, size);
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }
    
	private void InitStorage(){
		if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            mStorageManager.registerListener(mStorageListener);
            storageVolumes = mStorageManager.getVolumeList();
            Log.e(TAG,"storageVolumes.length:"+storageVolumes.length);
            if(storageVolumes.length >= 3){
            	flash_path = storageVolumes[flash_pit].getPath();
            	sdcard_path = storageVolumes[sdcard_pit].getPath();
            	usb_path = storageVolumes[usb_pit].getPath();
            	Log.d(TAG, " _____ " + flash_path + "   " + sdcard_path + "   " + usb_path);
            }
        }
	}
	
    StorageEventListener mStorageListener = new StorageEventListener() {

        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Log.d(TAG, "Received storage state changed notification that " +
                    path + " changed state from " + oldState +
                    " to " + newState);
            if (path.equals(sdcard_path) && !newState.equals(Environment.MEDIA_MOUNTED)) {
            } else {
                updateMemoryStatus(flash_path);
            }
        }
    };
	
	
}
