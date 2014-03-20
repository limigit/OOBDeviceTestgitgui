package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.view.LcdTestView;
import android.os.UEventObserver;

public class HdmiTestActivity extends Activity {
    private final static String TAG = "HDMITEST";

    private final static int CHANGE_COLOR = 1;
    private final static int HDMI_SCAN = 2;
    private final static int TEST_SUCCESS_RESULT = 3;
    private final static int TEST_FAIL_RESULT = 4;
    private int[] TestColor = {Color.RED, Color.GREEN, Color.BLUE };
    private LcdTestView mTestView;
    private TextView mTitle;
    private TextView mResult;
    private TextView mShowTime;
    private int mTestNo;
    private boolean isStart = false;
    private boolean ServiceHDMI = false;
    private File HdmiFile = null;
    private File HdmiState = null;
    private AlertDialog mHDMIDialog;
    private boolean has_HDMI_service=false;
    private boolean has_HDMI_client=false;
    private static boolean isHDMIFirst=true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.hdmitest);

        mTestView = (LcdTestView) findViewById(R.id.lcdtestview);
        mResult = (TextView) findViewById(R.id.result);
        mShowTime = (TextView) findViewById(R.id.TimeShow);
        mTestNo = 0;
        isHDMIFirst=true;
        if(CompareServerInfoActivity.HasHDMI!=null){
          ServiceHDMI = true;
          if(CompareServerInfoActivity.HasHDMI.equals("Y")){
        	   has_HDMI_service=true;
        	  Log.v(TAG, "service has HDMI");
           }else if(CompareServerInfoActivity.HasHDMI.equals("N")){
           	has_HDMI_service=false;
          	Log.v(TAG, "service hasn't HDMI");
           }else mResult.setText(R.string.hdmi_service_catch_err);
       }else {    
         mResult.setText(R.string.hdmi_service_catch_err);
         mHandler.sendEmptyMessageDelayed(TEST_FAIL_RESULT, 4000);
      }
        ControlButtonUtil.initControlButtonView(this);
        ((Button) findViewById(R.id.btn_Pass)).setVisibility(View.INVISIBLE);
       /*
        Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onPause();
				 mTestNo = 0;isStart = false;
				  isStart = false; ServiceHDMI = false;
				   HdmiFile = null; HdmiState = null;
				   has_HDMI_service=false; has_HDMI_client=false;
				   if(CompareServerInfoActivity.HasHDMI!=null){
				          ServiceHDMI = true;
				          if(CompareServerInfoActivity.HasHDMI.equals("Y")){
				        	   has_HDMI_service=true;
				        	  Log.v(TAG, "service has HDMI");
				           }else if(CompareServerInfoActivity.HasHDMI.equals("N")){
				           	has_HDMI_service=false;
				          	Log.v(TAG, "service hasn't HDMI");
				           }else mResult.setText(R.string.hdmi_service_catch_err);
				       }else {    
				         mResult.setText(R.string.hdmi_service_catch_err);
				         mHandler.sendEmptyMessageDelayed(TEST_FAIL_RESULT, 4000);
				      }
				onResume();
			}
		});
		*/
    }

    @Override
    public void onResume() {
        super.onResume();
       if(ServiceHDMI){ 
        File mHdmiFile = new File("/sys/class/display/HDMI");
        if (!mHdmiFile.exists()) {
        	has_HDMI_client=false; 
        	Log.v(TAG, "client has no HDMI");
        }else {
        	has_HDMI_client=true; 
        	Log.v(TAG, "client has HDMI");
        }
        
        if(!has_HDMI_service&&!has_HDMI_client){
        	mResult.setText(R.string.hdmi_test_success);
        	((Button)findViewById(R.id.btn_Retest)).setClickable(false);
			//((Button)findViewById(R.id.btn_Pass)).setClickable(false);
			((Button)findViewById(R.id.btn_Fail)).setClickable(false);
        	mHandler.sendEmptyMessageDelayed(TEST_SUCCESS_RESULT, 4000);// all false
        }
        
        if((has_HDMI_service&&!has_HDMI_client)||(!has_HDMI_service&&has_HDMI_client)){
        	mResult.setText(R.string.hdmi_test_fail);
        	mHandler.sendEmptyMessageDelayed(TEST_FAIL_RESULT, 4000);// compare err
               ((Button)findViewById(R.id.btn_Retest)).setClickable(false); 
               ((Button)findViewById(R.id.btn_Pass)).setClickable(false);
               ((Button)findViewById(R.id.btn_Fail)).setClickable(false);
        }
     
        if(has_HDMI_service&&has_HDMI_client){
        	HdmiFile = new File("/sys/class/display/HDMI/enable");
            HdmiState = new File("/sys/class/display/HDMI/connect");
            mHandler.sendEmptyMessageDelayed(HDMI_SCAN, 500);
        }
       } 
  
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(HDMI_SCAN);
        mHandler.removeMessages(CHANGE_COLOR);
    }
    
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CHANGE_COLOR:
                    if (mTestNo > TestColor.length - 1) {
                        finishHdmiTest();
                        return;
                    }
                    ControlButtonUtil.Hide();
                    mShowTime.setVisibility(View.VISIBLE);
                    mTestView.setVisibility(View.VISIBLE);
                    mResult.setText(R.string.HdmiStart);
                    mTestView.setBackgroundColor(TestColor[mTestNo++]);
                    sendEmptyMessageDelayed(CHANGE_COLOR, 1500);
                    break;
                case HDMI_SCAN:
                	this.removeMessages(HDMI_SCAN);
                    if (startHdmiTest()) {
                        mResult.setText(R.string.HdmiPrepare);
                        setHdmiConfig(HdmiFile, true);
                        mTestNo = 0;
                        sendEmptyMessageDelayed(CHANGE_COLOR, 4000);
                    }else{
                        sendEmptyMessageDelayed(HDMI_SCAN, 500);
                    }
                    break;
                    
                case TEST_SUCCESS_RESULT:{
                      if(isHDMIFirst){
                          isHDMIFirst=false;
                       	((Button) findViewById(R.id.btn_Pass)).performClick();
                       }
                     }
                	break;
  
                case TEST_FAIL_RESULT:{
                       if(isHDMIFirst){   
                          isHDMIFirst=false;    
                        ((Button) findViewById(R.id.btn_Fail)).performClick();
                      }
                   }
                	break;
                default:
                    break;
            }
        }
    };
    
    public boolean startHdmiTest() {
        if (!isStart && isHdmiConnected(HdmiState)) {
              ((Button) findViewById(R.id.btn_Pass)).setVisibility(View.VISIBLE);
			mResult.setText(R.string.HdmiPrepare);
            setHdmiConfig(HdmiFile, true);
            mTestNo = 0;
            isStart = true;
            return true;
        }
        mResult.setText(R.string.HdmiNoInsert);
        Log.i(TAG, "Hdmi no insert");
        return false;
    }
    
    public void finishHdmiTest() {
        ((Button) findViewById(R.id.btn_Pass)).setVisibility(View.VISIBLE);
        ControlButtonUtil.Show();
        isStart = false;
        mShowTime.setVisibility(View.GONE);
        mTestView.setVisibility(View.GONE);
        mResult.setText(R.string.HdmiResult);
//        setHdmiConfig(HdmiFile, false);
    }

    protected boolean isHdmiConnected(File file) {
        boolean isConnected = false;
        if (file.exists()) {
            try {
                FileReader fread = new FileReader(file);
                BufferedReader buffer = new BufferedReader(fread);
                String str = null;

                while ((str = buffer.readLine()) != null) {
                    if (str.equals("1")) {
                        isConnected = true;
                        break;
                    } else {
                        isConnected = false;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "IO Exception");
            }
        } else {
            Log.e(TAG, file + "isHdmiConnected : file no exist");
        }
        return isConnected;
    }

    protected void setHdmiConfig(File file, boolean enable) {
        if (file.exists()) {
            try {
                SharedPreferences.Editor editor = getPreferences(0).edit();
                String strChecked = "1";
                String strUnChecked = "0";

                RandomAccessFile rdf = null;
                rdf = new RandomAccessFile(file, "rw");
                if (enable) {
                    rdf.writeBytes(strChecked);
                    editor.putInt("enable", 1);
                } else {
                    rdf.writeBytes(strUnChecked);
                    editor.putInt("enable", 0);
                }
                editor.commit();
            } catch (IOException re) {
                Log.e(TAG, "IO Exception");
            }
        } else {
            Log.i(TAG, "The File " + file + " is not exists");
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }
    
    public boolean onTouchEvent(MotionEvent paramMotionEvent) {
        if (has_HDMI_service&&has_HDMI_client&&paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN && !isStart) {
    		mHandler.sendEmptyMessageDelayed(HDMI_SCAN, 500);
        }
        return super.onTouchEvent(paramMotionEvent);
    }
    

}
