
package com.OOBDeviceTest;

import java.util.Random;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.os.Handler;
import android.os.IECService;
import android.os.Message;
import android.os.ServiceManager;
import android.os.RemoteException;


public class LedTestActivity extends Activity {
	
	private final static String TAG = "LedTest";
	private Button org_led_button;
	private Button blu_led_button;
	private Button caps_button;
	private int[] randomNum2=new int[3]; 
    private int[] randomNum=new int[100];
	private final int SEND_OPEN=1000;
	private final int SEND_CLOSE=2000;
	private final int SEND_GOON_TEST=3000;
	
	private boolean send_states=false;
	private boolean test_org_led_pass=false;
	private boolean test_blu_led_pass=false;
	private boolean test_caps_pass=false;
	private static boolean isFirstLEDtest=true;
	private int caps_state=2;
	private int keyran=0;
	TextView ledtest;
	TextView capstest;
	TextView ledtestinfo;
	IECService ecService;
	int k=0;
	
	 @Override
	  public void onResume(){
		  super.onResume();
		  ecService = IECService.Stub.asInterface(ServiceManager.getService("ECServiceinfo"));
		  try { 
				caps_state=ecService.DeviceCtrl(20,1);
	     		Log.v("caps_state", "caps_state= "+caps_state);
	     		ecService.DeviceCtrl(2,1);	
	     		ecService.DeviceCtrl(19,0);	
	  		} catch (RemoteException e) {
	  			e.printStackTrace();
	  		}
		  create_randman();  
		 
	  }
	
	 @Override
	  public void onPause(){
		  super.onPause();
		  send_states=false;
		  close_ora();close_blu();close_caps();
		  try { 
		   if(caps_state==1) ecService.DeviceCtrl(19,1);
		   else ecService.DeviceCtrl(19,0);
  		   ecService.DeviceCtrl(2,0);
   		} catch (RemoteException e) {
   			e.printStackTrace();
   		}
	  }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ledtest);
		
		setTitle("LedTest");
		org_led_button=(Button)findViewById(R.id.org_led_light_id);
		blu_led_button=(Button)findViewById(R.id.blu_led_light_id);
		caps_button=(Button)findViewById(R.id.caps_light_id);
		ledtest=(TextView)findViewById(R.id.ledtesttext);
		capstest=(TextView)findViewById(R.id.capstesttext);
		
		ledtestinfo=(TextView)findViewById(R.id.ledtesttextinfo);
		ledtestinfo.setText(getString(R.string.led_text));
		
		isFirstLEDtest=true;
		ControlButtonUtil.initControlButtonView(this); 
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
		//findViewById(R.id.btn_Retest).setVisibility(View.INVISIBLE);
	/*
		Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
		      	close_ora();close_blu();close_caps();
		      	k=0;  
				test_org_led_pass=false;	
				test_blu_led_pass=false;
				test_caps_pass=false;
				org_led_button.setBackgroundColor(Color.GRAY);
				caps_button.setBackgroundColor(Color.GRAY);	
				blu_led_button.setBackgroundColor(Color.GRAY);		
				create_randman(); 	
			}
		});
	*/	
	}
	
	public void create_randman(){
	 /*
		int index,t;
        for(int i = 0 ;i < randomNum.length ;i++){
                randomNum[i]=i+1;
                index = new Random().nextInt(randomNum.length);
                t = randomNum[i];
                randomNum[i] = randomNum[index];
                randomNum[index] = t;
                }
            Log.v(TAG,"randomNum[0]= "+randomNum[0]);
      randomNum2[0]=randomNum[0]%3; Log.v(TAG,"randomNum2[0]= "+randomNum2[0]);
      randomNum2[1]=(randomNum[0]+1)%3;Log.v(TAG,"randomNum2[1]= "+randomNum2[1]);
      randomNum2[2]=(randomNum[0]+2)%3;Log.v(TAG,"randomNum2[1]= "+randomNum2[1]);
      if(randomNum2[0]==0&&randomNum2[1]==2||randomNum2[0]==2&&randomNum2[1]==0
    		  ||randomNum2[1]==0&&randomNum2[2]==2||randomNum2[2]==2&&randomNum2[1]==0
    		 ){
    	  create_randman();
      }else{
    	  
      */
    	  int rand= new Random().nextInt(2); 
    	 if(rand==0) {randomNum2[0]=2; randomNum2[1]=1; randomNum2[2]=0;}
    	 if(rand==1) {randomNum2[0]=0; randomNum2[1]=1; randomNum2[2]=2;}
    	  open_switch(randomNum2[k]);
     // }
	}
	
	public void open_switch(int testNum){
	          Log.v(TAG,"open_swith");	
                switch (testNum) {
		 case 0:{ 
               Log.v(TAG,"open orange led");
               keyran=1;
			 capstest.setText("");
			 ledtest.setText("");
			 ledtest.setText(R.string.led_light_text);
			 ledtest.setTextColor(Color.RED);
			 open_ora_light();
			 }
			break;
		 case 1: {
               Log.v(TAG,"open caps");
			 int capsran= new Random().nextInt(2); 
			 ledtest.setText("");capstest.setText("");
			 capstest.setText(R.string.caps_light_text);
			 capstest.setTextColor(Color.RED);                        
               open_caps_switch(capsran);
		     }
			break;
		 case 2:{ 
             Log.v(TAG,"open blue led");
             keyran=2;
			 capstest.setText(""); ledtest.setText("");
			 ledtest.setText(R.string.led_light_text);
			 ledtest.setTextColor(Color.RED);
			 open_blu_light();
			 }
			break;
		}
	}
 
	/*
	public void open_led_switch(int testledNum){
		 switch (testledNum) {
		 case 0:{ 
			 keyran=1;
			 open_ora_light();
			 }
			break;
		 case 1: {
			 keyran=2;
			 open_blu_light();
		     }
			break;
		}
	}
	*/
	
	public void open_caps_switch(int testcapsNum){
		 switch (testcapsNum) {
		 case 0: {
			 keyran=3;
			 open_caps_light();
		     }
			break;
		 case 1: {
			 keyran=4;
			 open_caps_flash();
		     }
			break;
		}
	}
	
	
	//open orange led
	public void open_ora_light(){
		try {
			ecService.DeviceCtrl(4,1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
        Log.v(TAG, "open orange light ");
	}
	//open blue led
	public void open_blu_light(){	
		try {
			ecService.DeviceCtrl(5,1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
        Log.v(TAG, "open blue light ");
	}
	//open  capsLock light
	public void open_caps_light(){
		try {
			ecService.DeviceCtrl(19,1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
        Log.v(TAG, "open capslock light ");
	}
	//open capsLock flash
	public void open_caps_flash(){
		 send_states=true;
		 open_caps_light();
		 Log.v(TAG, "open capslock flash ");
		 mHandler.sendEmptyMessageDelayed(SEND_CLOSE, 100);
	} 
	
	public void close_ora(){
		try {
			ecService.DeviceCtrl(4,0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		Log.v(TAG, "close orange light ");
	}
	
	public void close_blu(){
		try {
			ecService.DeviceCtrl(5,0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		Log.v(TAG, "close blue light ");
	}
	
	public void close_caps(){
		try {
			ecService.DeviceCtrl(19,0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		Log.v(TAG, "close capslock light ");
	}
	
	public void close_caps_flash(){
		close_caps();
		Log.v(TAG, "close capslock flash ");
		mHandler.sendEmptyMessageDelayed(SEND_OPEN, 100);
	}
	
	
	public void pass_swich(int passNum){
		switch (passNum) {
		case 0:{
			org_led_button.setBackgroundColor(Color.GREEN);
			 capstest.setText(""); ledtest.setText("");
			test_org_led_pass=true;
			close_ora();
		}
			break;
		case 1:{
			caps_button.setBackgroundColor(Color.GREEN);
			 capstest.setText(""); ledtest.setText("");
			test_caps_pass=true;
			close_caps();
		}
			break;
		case 2:{
			blu_led_button.setBackgroundColor(Color.GREEN);
			 capstest.setText(""); ledtest.setText("");
			test_blu_led_pass=true;
			close_blu();
		}
			break;
		}
		
		if(k<2){
		  open_switch(randomNum2[++k]);	
		}
		if(test_org_led_pass&&test_blu_led_pass&&test_caps_pass){
			if(isFirstLEDtest){
				isFirstLEDtest=false;
				 ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
				//((Button)findViewById(R.id.btn_Pass)).setClickable(false);
				((Button)findViewById(R.id.btn_Fail)).setClickable(false);
				findViewById(R.id.btn_Pass).performClick();
			}
			
		}
		
	}
	
	
	private Handler mHandler = new Handler() {   
		@Override     
		public void handleMessage(Message msg) {   
			if(send_states){
				switch (msg.what) {    
				case SEND_CLOSE: close_caps_flash();break;  
				case SEND_OPEN: open_caps_flash();break;  
				case SEND_GOON_TEST:open_switch(randomNum2[++k]);
				} 
			}
		}
	
	};

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if (event.getKeyCode() == keyran+7) {
			send_states=false;
			pass_swich(randomNum2[k]);
		}else{
			send_states=false;
			close_ora();close_blu();close_caps();
			errDialog();
		}
	
	   return super.onKeyDown(keyCode, event);
    }
	
	public void errDialog(){	
		Dialog alertDialog = new AlertDialog.Builder(this).		
				setMessage(R.string.led_err).
				setPositiveButton(R.string.led_err_reTest, new DialogInterface.OnClickListener() {   
					@Override
					public void onClick(DialogInterface dialog, int which) { 
						k=0;  
						test_org_led_pass=false;
						test_blu_led_pass=false;
						test_caps_pass=false;
						org_led_button.setBackgroundColor(Color.GRAY);
						caps_button.setBackgroundColor(Color.GRAY);
						blu_led_button.setBackgroundColor(Color.GRAY);
						create_randman(); 
					//   open_switch(randomNum2[k]); 	
						
					}   
				}).	   
				setNegativeButton(R.string.led_err_fail, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {  
						findViewById(R.id.btn_Fail).performClick();	
						Log.v(TAG, "test fail!");
					} 
				}).   
				create();
		alertDialog.setCancelable(false);   
		alertDialog.show();  
	  }	

   	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}


