package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.util.Log;
import android.hardware.Camera;

public class CameraTestActivity extends Activity {
	private static final int mRequestCode = 1000;
        private static final String TAG="CameraTestActiviy";
        private Camera mCameraDevice;
        private static boolean isCameraFirstTest=true;
       
	@Override	
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.cameratest);
		isCameraFirstTest=true;
		ControlButtonUtil.initControlButtonView(this);
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
	}
	
	@Override	
	public void onRestart() {
		super.onRestart();

		findViewById(R.id.btn_Pass).setVisibility(View.VISIBLE);
	}

	
	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
        
            	if (paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN) {
	           try{ mCameraDevice=Camera.open(0);
                         mCameraDevice.release();
                         Intent localIntent = new Intent(
					"android.media.action.IMAGE_CAPTURE");
                 	startActivityIfNeeded(localIntent, 1000);
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.v(TAG,"====limi====3");
                       if(isCameraFirstTest){
                    	   isCameraFirstTest=false;
                    	   ((Button)findViewById(R.id.btn_Fail)).performClick();
                       }
                    }
	           }
                	return super.onTouchEvent(paramMotionEvent);      
        }

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
