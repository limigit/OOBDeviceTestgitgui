package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import com.OOBDeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.IECService;
import android.os.Message;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.KeyEvent;


public class LIDTestActivity extends Activity {
	private final static String TAG = "LIDTestActivity";
	private final static int SEND_LID_SUCCESS=0;
	private final static int SEND_LID_FAIL=1;
	private final static int SEND_LID_TEST=2;
	int angle1=0;
	int angle2=0;
	 int i=0;

	private int mlid=-1;
	private TextView lidtext1;
	private TextView lidtext2;
	private static boolean isFirst=true;
	private  boolean StartTestLID=false;
	private  boolean sendLID=false;
	private  boolean EndTestLID=false;
	private  boolean LIDState=false;
	private SensorManager sensorManager;
	private SensorEventListener lsn = null;
	private IECService ecService;
	private int sensorAngle=-1;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lidtest);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
	  Log.v(TAG,"==onCreate===");
		isFirst=true;
		lidtext1=(TextView)findViewById(R.id.lidtext1);
		lidtext2=(TextView)findViewById(R.id.lidtext2);
		lidtext2.setText(R.string.lid_start_test);
		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		ControlButtonUtil.initControlButtonView(this); 
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
	/*	
		Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				lidtext2.setText(R.string.lid_start_test);
				 angle1=0;angle2=0;
				 i=0; mlid=-1;
				 sensorAngle=-1;
				 StartTestLID=false;sendLID=false;LIDState=false;
				onResume();
			}
		});
	*/	
	}
	
	 @Override
	  public void onResume(){
		  super.onResume();
		  sendLID=true;
		  getGsensorAngle();
		 // mHandler.sendEmptyMessageDelayed(SEND_LID_TEST, 1000);
	  }
	  
      @Override
	   public void onPause() {
		  super.onPause();
	  	sensorManager.unregisterListener(lsn);
	
	}
	 
	 
	 public void getlid(){
		 ecService = IECService.Stub.asInterface(ServiceManager.getService("ECServiceinfo"));
		  try { 
			  mlid=ecService.DeviceCtrl(22,0);	
			  Log.v(TAG, "mlid= "+mlid);
			    if(mlid==0){
			    	LIDState=true;
				    lidtext2.setText(R.string.lid_test_success);
			    }	 
	  	} catch (RemoteException e) {
	  			mlid=-1;
	  			e.printStackTrace();
	    }
	 }
	 
	 
	 private void getGsensorAngle(){
	 	
	 	ecService = IECService.Stub.asInterface(ServiceManager.getService("ECServiceinfo"));
		lsn = new SensorEventListener() {
				public void onAccuracyChanged(Sensor sensor, int accuracy) {

				}
				public void onSensorChanged(SensorEvent e) {
					if(sendLID){
					 try { 
						  sensorAngle=ecService.readGSensorAngle();	
						  i++;
						  Log.v(TAG, "sensorAngle= "+sensorAngle);
						  Log.v(TAG, "i= "+i);
						  if(i%2==0)angle1=sensorAngle;
						  else angle2=sensorAngle;
						  Log.v(TAG, "angle1= "+angle1+"angle2= "+angle2);
						  if(sensorAngle<0||sensorAngle>300){
							  Log.v(TAG, "sensorAngle read err ");
						  }		
						  else{ 
						  	    if(Math.abs(angle1-angle2)<10){
						           if(sensorAngle<30){
							            StartTestLID=true;
							            getlid();
						            }
						         
						           if(sensorAngle>60&&StartTestLID){
						  	          sendLID=false;
						  	          if(LIDState){
						  	        	 ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
						 				//((Button)findViewById(R.id.btn_Pass)).setClickable(false);
						 				((Button)findViewById(R.id.btn_Fail)).setClickable(false);
						  	        	  mHandler.sendEmptyMessageDelayed(SEND_LID_SUCCESS, 4000);
						  	          }
						     	        else{
						  	       	     sendLID=false;
						  		           lidtext2.setText(R.string.lid_test_fail);
						  		         ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
						 				 ((Button)findViewById(R.id.btn_Pass)).setClickable(false);
						 				//((Button)findViewById(R.id.btn_Fail)).setClickable(false);
						  		           mHandler.sendEmptyMessageDelayed(SEND_LID_FAIL, 4000);
						  		        }
						  	       }
						  	    }
						  	  } 
				   } catch (RemoteException e1) {
				  		sensorAngle=-1;
				  	  e1.printStackTrace();
				   }
				 }
				}

			};	
			Sensor sensors = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(lsn, sensors,SensorManager.SENSOR_DELAY_NORMAL);
	 	
	 	}

	private	Handler mHandler=new Handler(){
		@Override     
		public void handleMessage(Message msg) {   
				switch (msg.what) {    
				/*
				case SEND_LID_TEST: 
					getlid();
				if(sendLID)mHandler.sendEmptyMessageDelayed(SEND_LID_TEST, 1000);
					break;
					*/  
				case SEND_LID_SUCCESS: {
					if(isFirst){
						isFirst=false;
					   ((Button)findViewById(R.id.btn_Pass)).performClick();
					}
				}
					break; 
					case SEND_LID_FAIL: {
					if(isFirst){
						isFirst=false;
					   ((Button)findViewById(R.id.btn_Fail)).performClick();
					}
				}
					break; 	
			}
		}
	};
	
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

}
