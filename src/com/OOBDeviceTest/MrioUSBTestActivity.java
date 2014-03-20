package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
//import android.hardware.Usb;
import android.util.Log;

public class MrioUSBTestActivity extends Activity {

	
	TextView mUsbPluginText1;
	TextView mUsbPluginText2;
	boolean pluginPass1 = false;
	boolean pluginPass2 = false;
	
	boolean stop = false;
	boolean testNum = false;
  private static final String TAG = "USBTestActivity";
  private static final int BACK_TIME = 1000;
  private static final int R_PASS = 1;
  private static final int R_FAIL = 2;
 private BroadcastReceiver mUsbStateReceiver;
 
  private String usb_path1 = null;   
  private String usb_path2 = null;
	   // private StringBuilder sBuilder1;
	   // private StringBuilder sBuilder2;
	 
	private File mFile ;
	private String SUCCESS;
  private String FAIL;
	   
	private boolean isfirstTest= false;
	private boolean issencondTest = false;
	private StorageManager mStorageManager = null;
	private String filename;
	private static boolean isFirst=true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFirst = true;
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.mriousbtest);
		if (mStorageManager == null) {
           mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		}
		
       mUsbPluginText1 = (TextView) findViewById(R.id.usbplugin1);
		   mUsbPluginText2= (TextView) findViewById(R.id.usbplugin2);

       ControlButtonUtil.initControlButtonView(this);
       findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
 //       findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
     	mUsbStateReceiver = new UsbConnectedBroadcastReceiver();
    	mUsbPluginText1.setText(getString(R.string.usbplugin1));
    	mUsbPluginText2.setText(getString(R.string.usbplugin2));
    	/*
    	Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onPause();
				pluginPass1 = false;pluginPass2 = false; 
				stop = false;testNum = false; usb_path1 = null;usb_path2 = null;
			    isfirstTest= false;issencondTest = false;
		    	mUsbPluginText1.setText(getString(R.string.usbplugin1));
		    	mUsbPluginText2.setText(getString(R.string.usbplugin2));
				onResume();
			}
		});
		*/
	}
	
	 @Override
	    protected void onResume() {
	        super.onResume();
	        IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
			intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
			intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
			intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
			intentFilter.addDataScheme("file");
			registerReceiver(mUsbStateReceiver, intentFilter);
	 
	       // mStorageManager.registerListener(mStorageListener);
	       // sBuilder1 = new StringBuilder();
	       // sBuilder2 = new StringBuilder();
	        selectTest();
	        	
	    }
	 
	 protected void selectTest(){
		   mStorageManager = StorageManager.from(this);
		    StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
		    Log.v(TAG, "storageVolumes = "+storageVolumes);
		   
		    for (StorageVolume volume : storageVolumes) {      
		    	if (!volume.isEmulated()) {	
		    		String USBstate = mStorageManager.getVolumeState(volume.getPath()); 
		    		if(Environment.MEDIA_MOUNTED.equals(USBstate)){
		    		if(volume.getPath().equals("/mnt/usb_storage/USB_DISK0")){
		    			usb_path1="/mnt/usb_storage/USB_DISK0"; isfirstTest=true;	
		    			testUsb(usb_path1);
		    		}  
		    		if(volume.getPath().equals("/mnt/usb_storage/USB_DISK1")){
		    			usb_path2="/mnt/usb_storage/USB_DISK1"; issencondTest=true;
		    			testUsb(usb_path2);
		    		} 
		    	  }
		    	}
		    }
		 
	 }

	    @Override
	    protected void onPause() {
	        super.onPause();
	       /// if (mStorageManager != null && mStorageListener != null) {
	       //     mStorageManager.unregisterListener(mStorageListener);
	       // }
	       
	    }
	    
	    public void  testUsb(String usbpath){
	    	filename=usbpath+"/udisk0"+"/usb.flg";
	    	FileOutputStream out;
	    	mFile =new File(filename);
			try {
				if(!mFile.exists())mFile.createNewFile();
				out = new FileOutputStream(mFile);
				out.write("test usb".getBytes());
				if(isfirstTest)mUsbPluginText1.setText(getString(R.string.usbplugin1)+getString(R.string.usb_write));
				if(issencondTest)mUsbPluginText2.setText(getString(R.string.usbplugin2)+getString(R.string.usb_write));
				Log.v(TAG, "write usb.flg file success");
				out.close();
			} catch (FileNotFoundException e) {
			 set_err();
			 e.printStackTrace();	
			 return;
		 } catch (IOException e) {
			 set_err();
			 e.printStackTrace(); 	
			 return;
		 }
			
			
			try {
	            BufferedReader br = new BufferedReader(new InputStreamReader
	                    (new FileInputStream(filename)));
	            String data = null;
	            StringBuilder temp = new StringBuilder();
	            if(isfirstTest) mUsbPluginText1.setText(getString(R.string.usbplugin1)+getString(R.string.usb_write)+";"+getString(R.string.usb_read));
				      if(issencondTest) mUsbPluginText2.setText(getString(R.string.usbplugin2)+getString(R.string.usb_write)+";"+getString(R.string.usb_read));
	           
	            while ((data = br.readLine()) != null) {
	                temp.append(data);
	            }
	            br.close();
	            mFile.delete();
	            Log.e(TAG, "Readfile " + temp.toString());
	        } catch (Exception e) {
	        	set_err();
	            e.printStackTrace();
	            return;
	           
	        }
			    
		     if(isfirstTest) {
		    	 mUsbPluginText1.setText(getString(R.string.usbplugin1)+getString(R.string.usb_write)+";"
		                 +getString(R.string.usb_read)+";"+getString(R.string.usb_test_success)); 
		    	 pluginPass1=true;isfirstTest=false;
		     }
		     if(issencondTest) {
		    	 mUsbPluginText2.setText(getString(R.string.usbplugin2)+getString(R.string.usb_write)+";"
		    		     +getString(R.string.usb_read)+";"+getString(R.string.usb_test_success)); 
		    	 pluginPass2=true; issencondTest=false;
		    	 }
		     if(pluginPass1&&pluginPass2) {
		    	 ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
					//((Button)findViewById(R.id.btn_Pass)).setClickable(false);
					((Button)findViewById(R.id.btn_Fail)).setClickable(false);
		    	 mHandler.sendEmptyMessageDelayed(R_PASS, 1000);
		     }
	    }

	    public void set_err() {
	    	if(isfirstTest)pluginPass1=false;
			if(issencondTest)pluginPass2=false;
        	if(isfirstTest)mUsbPluginText1.setText(getString(R.string.usbplugin1)+getString(R.string.usb_test_err));
			if(issencondTest)mUsbPluginText2.setText(getString(R.string.usbplugin1)+getString(R.string.usb_test_err));
            ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
		    ((Button)findViewById(R.id.btn_Pass)).setClickable(false);
		//	((Button)findViewById(R.id.btn_Fail)).setClickable(false);
			mHandler.sendEmptyMessageDelayed(R_FAIL, 2000);

		}
   
	    class UsbConnectedBroadcastReceiver extends BroadcastReceiver {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "intent.getAction()"+intent.getAction());
				 if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
					Log.v("limiUSB","198 UsbConnectedBroadcastReceiver");
					 selectTest();
					 
		            }else{
			            return;	
		            }
				
			}
		};
	   
	     /*
	    StorageEventListener mStorageListener = new StorageEventListener() {
	        @Override
	        public void onStorageStateChanged(String path, String oldState, String newState) {
	        	if (newState.equals(Intent.ACTION_MEDIA_MOUNTED)) {
	        			Log.v("limiUSB","213 mStorageListener");
	        		selectTest();
	        	}
	        }
	    };
	    */
	   // static boolean isFirst=true;
	    public void TestResult(int result) {
	    	
	        if (result == R_PASS) {
	        	 if(pluginPass1&&pluginPass2&&isFirst){
	        	 	isFirst=false;
	        	  ((Button)findViewById(R.id.btn_Pass)).performClick();
	        	}
	        } else if (result == R_FAIL&&isFirst) {
	        	 	isFirst=false;
	            ((Button) findViewById(R.id.btn_Fail)).performClick();
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
	            }
	        };
	    };
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
