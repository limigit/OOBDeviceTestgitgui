/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Environment;
import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.StorageEventListener;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemProperties;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import java.io.File;
import java.util.List;

public class StorageActivity extends Activity implements OnCancelListener {
    private static final String TAG = "Memory";
    private static final boolean localLOGV = false;

	public  String flash_path = null;
	public  String sdcard_path = null;
	public  String usb_path = null;
	private int flash_pit = 0; 
	private int sdcard_pit = 1; 
	private int usb_pit = 2; 
	private StorageVolume[] storageVolumes = null;
    
    private Resources mRes;

    private TextView mNandSize;
    private TextView mNandAvail;
    private TextView mDataAvail;

    boolean mSdMountToggleAdded = true;
    boolean mNandMountToggleAdded = true;
    
    private StorageManager mStorageManager = null;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        InitStorage();
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.storageactivitytest);
        
        mRes = getResources();
        mNandSize = (TextView)findViewById(R.id.nand_total_space);
        mNandAvail = (TextView)findViewById(R.id.nand_available_space);
        mDataAvail = (TextView)findViewById(R.id.data_available_space);
        
        InitStorage();
        ControlButtonUtil.initControlButtonView(this);
        Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onPause();
				onResume();
			}
		});
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);
        updateMemoryStatus(flash_path);
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
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        if (mStorageManager != null && mStorageListener != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
        super.onDestroy();
    }
     
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateMemoryStatus(flash_path);
            //updateMemoryStatus(Environment.getExternalStorageDirectory().getPath());
        }
    };

    private void updateMemoryStatus(String path) {
        String status = SystemProperties.get("EXTERNAL_STORAGE_STATE","unmounted");
        if (path.equals(flash_path)) {
            status = mStorageManager.getVolumeState(path);
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

                if (path.equals(flash_path)){
                    mNandSize.setText(formatSize(totalBlocks * blockSize));
                    mNandAvail.setText(formatSize(availableBlocks * blockSize) + readOnly);
                }
            } catch (IllegalArgumentException e) {
                // this can occur if the SD card is removed, but we haven't received the 
                // ACTION_MEDIA_REMOVED Intent yet.
                status = Environment.MEDIA_REMOVED;
            }
            
        } else {
            if (path.equals(flash_path)){
                mNandSize.setText(mRes.getString(R.string.nand_unavailable));
                mNandAvail.setText(mRes.getString(R.string.nand_unavailable));
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
        mDataAvail.setText(formatSize(availableBlocks * blockSize));
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
    
    
}
