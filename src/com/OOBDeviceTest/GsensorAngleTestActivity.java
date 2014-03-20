package com.OOBDeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IECService;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;

import com.OOBDeviceTest.helper.ControlButtonUtil;

public class GsensorAngleTestActivity extends Activity {

	private final static String TAG = "GsensorAngleTestActivity";
	private SensorManager sensorManager;
	private SensorEventListener lsn = null;
	

	private final static int SEND_GsensorAngle_SUCCESS=0;
	private final static int SEND_GsensorAngle_FAIL=1;
	private final static int SEND_GsensorAngle_TEST=2;
	private IECService ecService;
	private int sensorAngle=-1;
	private int init_sensorAngle=-1;
	private TextView GsensorAngletext1;
	private TextView GsensorAngletext2;
	private static boolean isFirst=true;
  private  boolean isTest1=false;	
  private  boolean isTest2=false;	
  private  boolean isOnTest=false;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gsensorangletest);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
	
		isFirst=true;
		GsensorAngletext1=(TextView)findViewById(R.id.gsensorangletext1);
		GsensorAngletext2=(TextView)findViewById(R.id.gsensorangletext2);
		GsensorAngletext2.setText(R.string.gsensorangle_start_test1);
		
		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		ControlButtonUtil.initControlButtonView(this); 
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
		
	
		
	}
	
	 @Override
	  public void onResume(){
		  super.onResume();
      getinitGsensorAngle();
      
      if(init_sensorAngle>=0&&init_sensorAngle<180) {
      	GsensorAngletext2.setText(R.string.gsensorangle_start_test1);
      	isTest1=true;
      	isTest2=false;
      	mHandler.sendEmptyMessageDelayed(SEND_GsensorAngle_TEST, 3000);
      }
      if(init_sensorAngle<360&&init_sensorAngle>=180){
      	GsensorAngletext2.setText(R.string.gsensorangle_start_test2);
      	isTest1=false;
      	isTest2=true;	
      	mHandler.sendEmptyMessageDelayed(SEND_GsensorAngle_TEST, 3000);
     }
		  //mHandler.sendEmptyMessageDelayed(SEND_GsensorAngle_TEST, 100);
	  }
	  
	   @Override
	  protected void onPause() {
		  super.onPause();
		  isOnTest=false;
		 mHandler.removeMessages(SEND_GsensorAngle_TEST);
		 
	  	sensorManager.unregisterListener(lsn);
	   
	 }
	 
	 
	
	public void getGsensorAngle(){
		 ecService = IECService.Stub.asInterface(ServiceManager.getService("ECServiceinfo"));

			lsn = new SensorEventListener() {
				public void onAccuracyChanged(Sensor sensor, int accuracy) {

				}
				public void onSensorChanged(SensorEvent e) {
					 if(isOnTest){
					 try { 
						  sensorAngle=ecService.readGSensorAngle();	
						  Log.v(TAG, "sensorAngle= "+sensorAngle);
						  if(sensorAngle<0||sensorAngle>360){
							  GsensorAngletext2.setText(R.string.sensorAngle_read_err);
						  }		
						  else{
						  	 GsensorAngletext2.setText("当前角度为："+sensorAngle);
							   if(isTest1&&sensorAngle>185) {
							      GsensorAngletext2.setText("当前角度为："+sensorAngle+"测试成功！");
							      isTest1=false;isOnTest=false;
							      mHandler.sendEmptyMessageDelayed(SEND_GsensorAngle_SUCCESS, 3000);
							      ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
								//((Button)findViewById(R.id.btn_Pass)).setClickable(false);
								 ((Button)findViewById(R.id.btn_Fail)).setClickable(false);
                        }
                               if(isTest2&&sensorAngle<175) {
                	            isTest2=false;isOnTest=false;
							     GsensorAngletext2.setText("当前角度为 "+sensorAngle+"测试成功");						     mHandler.sendEmptyMessageDelayed(SEND_GsensorAngle_SUCCESS, 3000);   							     ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
								//((Button)findViewById(R.id.btn_Pass)).setClickable(false);
								 ((Button)findViewById(R.id.btn_Fail)).setClickable(false);
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
	 
	 	public void getinitGsensorAngle(){
		      ecService = IECService.Stub.asInterface(ServiceManager.getService("ECServiceinfo"));
					 try { 
						  init_sensorAngle=ecService.readGSensorAngle();	
						  Log.v(TAG, "init_sensorAngle= "+init_sensorAngle);
						  if(init_sensorAngle<0||init_sensorAngle>360){
							  GsensorAngletext2.setText(R.string.sensorAngle_read_err);
						  }	
				  } catch (RemoteException e1) {
				  		init_sensorAngle=-1;
				  	  e1.printStackTrace();
				  }
				}

		

	private	Handler mHandler=new Handler(){
		@Override     
		public void handleMessage(Message msg) {   
				switch (msg.what) {    
				
				case SEND_GsensorAngle_TEST: 
				   	isOnTest=true;
				   	getGsensorAngle();
					  break;  
			
				case SEND_GsensorAngle_SUCCESS: {
					if(isFirst){
					 isFirst=false;
					((Button)findViewById(R.id.btn_Pass)).performClick();
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
