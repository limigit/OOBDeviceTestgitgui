package com.OOBDeviceTest;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.DeviceInfo;
import com.OOBDeviceTest.helper.GetDeviceInfoHelper;
import com.OOBDeviceTest.helper.GetServerIniHelper;
import com.OOBDeviceTest.helper.LogFileHelper;
import com.OOBDeviceTest.helper.NativeManger;
import com.OOBDeviceTest.helper.PhaseServerFileHelper;
import com.OOBDeviceTest.helper.TestCase;
import com.OOBDeviceTest.helper.TestCase.RESULT;
import com.OOBDeviceTest.view.MyItemView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;



public class KeyboardHardwareTestActivity extends Activity {

	private PhaseServerFileHelper mPSFH;
	private GetDeviceInfoHelper mGDIH;
	private GetServerIniHelper mGSIH;
	public DeviceInfo mServerInfo;
	private NativeManger mNativeManger;
	private String Sn;
	TextView keyboard;
	public final static String TAG = "KeyboardHardwareTestActivity";
	
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
      setContentView(R.layout.keyboard_hardware_test);  
     // mGSIH = new GetServerIniHelper(this, mHandler);
     keyboard=(TextView)findViewById(R.id.keyboardid);
      mNativeManger = new NativeManger();
      ControlButtonUtil.initControlButtonView(this);
      findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
     /* 
      Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startKeyboardTest("US"); 
			}
		});
*/
   
      if(CompareServerInfoActivity.keyboardInfo!=null)startKeyboardTest(CompareServerInfoActivity.keyboardInfo);
       else	{
       	Log.v(TAG,"keyboardInfo=NULL");
       	keyboard.setText("keyboardInfo=null");
       	//findViewById(R.id.btn_Fail).performClick();
       	}
       	

     // startKeyboardTest("US"); 
 } 

  
  public void startKeyboardTest(String keyID){
	 Intent intent =new Intent();
	  if(keyID.equals("US"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardUSTestActivity.class);
	  else if(keyID.equals("UK"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardUKTestActivity.class);
	  else if(keyID.equals("BEL"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardBELTestActivity.class);
	  else if(keyID.equals("DUT"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardDUTTestActivity.class);
	  else if(keyID.equals("FRA"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardFRATestActivity.class);
	  else if(keyID.equals("ITA"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardITATestActivity.class);
	  else if(keyID.equals("SPA"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardSPATestActivity.class);
	  else if(keyID.equals("SWI"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardSWITestActivity.class);
	  else if(keyID.equals("GER"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardGERTestActivity.class);
	  else if(keyID.equals("BRA"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardBRATestActivity.class);
	  else if(keyID.equals("HUN"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardHUNTestActivity.class); 
	  else if(keyID.equals("ICE"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardICETestActivity.class);
	  else if(keyID.equals("LAT"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardLATTestActivity.class);
	  else if(keyID.equals("NOD"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardNODTestActivity.class);
	  else if(keyID.equals("POR"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardPORTestActivity.class);
	  else if(keyID.equals("SLE"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardSLETestActivity.class);
	  else if(keyID.equals("TUR"))
		  intent.setClass(KeyboardHardwareTestActivity.this,KeyboardTURTestActivity.class);
	  else{
		    intent=null;
		    keyboard.setText("keyboardInfo=unknow");
		 }
		 
	  if(intent!=null)startActivityForResult(intent, 10);
  }

  protected void onActivityResult(int requestCode, int resultCode,
			Intent keyIntent) {
		super.onActivityResult(requestCode, resultCode, keyIntent);
		if(resultCode == 1111){
			String keyresult=keyIntent.getExtras().getString("key_test_result");
			if(keyresult.equals("Pass")){
				findViewById(R.id.btn_Pass).performClick();
				 ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
				//((Button)findViewById(R.id.btn_Pass)).setClickable(false);
				 ((Button)findViewById(R.id.btn_Fail)).setClickable(false);
			}
			else if(keyresult.equals("Fail")){
				 ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
					((Button)findViewById(R.id.btn_Pass)).setClickable(false);
					// ((Button)findViewById(R.id.btn_Fail)).setClickable(false);
				findViewById(R.id.btn_Fail).performClick();
			}
			else if(keyresult.equals("Skip")){
				 ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
				((Button)findViewById(R.id.btn_Pass)).setClickable(false);
				((Button)findViewById(R.id.btn_Fail)).setClickable(false);
				findViewById(R.id.btn_Skip).performClick();
			}
		}
	
	}

  	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

}
  

