package com.OOBDeviceTest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnHoverListener;
import android.view.Window;
import android.view.View.OnGenericMotionListener;
import android.widget.Button;
import android.widget.LinearLayout;


import com.OOBDeviceTest.helper.ControlButtonUtil;



public class TouchPadTestActivity extends Activity {
    Button left;
    Button right;
    Button pad1;
    Button pad2;
    Button pad3;
    Button pad4;
    int action2;
    LinearLayout linear1;
    LinearLayout linear2;
    boolean isleftButton=false;
    Boolean[] padBooleans=new Boolean[6];
    {for(int i=0;i<6;i++){
    	padBooleans[i]=false;  	
    }
    }
   private final int PASS_STATE=1;
   private static boolean isTouchPadFirstTest=true;
   Boolean last_pass_state=false;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_SHOW_FULLSCREEN);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.touchpadtest);
		isTouchPadFirstTest=true;
		left=(Button)findViewById(R.id.leftbutton);
		left.setBackgroundColor(Color.GRAY);
		
		right=(Button)findViewById(R.id.rightbutton);
		right.setBackgroundColor(Color.GRAY);
		pad1=(Button)findViewById(R.id.textview1);
		pad2=(Button)findViewById(R.id.textview2);
		pad3=(Button)findViewById(R.id.textview3);
		pad4=(Button)findViewById(R.id.textview4);
		//linear1=(LinearLayout)findViewById(R.id.linear1);
		//linear1.setOnGenericMotionListener(ogml1);
		linear2=(LinearLayout)findViewById(R.id.linear2);
		linear2.setOnGenericMotionListener(ogml2);
		ControlButtonUtil.initControlButtonView(this);
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
		findViewById(R.id.btn_Skip).setVisibility(View.INVISIBLE);
	//=============limi================= touchpad need add	
	/*
		Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onPause();
				for(int i=0;i<6;i++){
			    	padBooleans[i]=false;  	
			    }
				last_pass_state=false;
				left.setBackgroundColor(Color.GRAY);
				right.setBackgroundColor(Color.GRAY);// need test limi =========9.6
				pad1.setBackgroundColor(Color.GRAY);
				pad2.setBackgroundColor(Color.GRAY);	
				pad3.setBackgroundColor(Color.GRAY);	
				pad4.setBackgroundColor(Color.GRAY);	
				onResume();
			}
		});
*/
		left.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_UP 
						&& event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) {
					if (event.getEventTime() - event.getDownTime() > 2000) {
						if(isleftButton){
							left.setBackgroundColor(Color.GREEN);
							padBooleans[5]=true;
							mHandler.sendEmptyMessageDelayed(PASS_STATE, 100);
						}
					}
					isleftButton=false;
					
				}
				if (event.getAction() == MotionEvent.ACTION_DOWN 
						&& event.getButtonState() == MotionEvent.BUTTON_PRIMARY
						&& event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE
						&&event.getPointerCount()==1) {
					  isleftButton=true;
				}
				return false;
			}
		});
	
		pad1.setOnHoverListener(new OnHoverListener() {
			@Override		  
			public boolean onHover(View v, MotionEvent event) {	   
				int what = event.getAction();  
				switch(what){  
				case MotionEvent.ACTION_HOVER_ENTER: { 
					pad1.setBackgroundColor(Color.GREEN);	
					padBooleans[0]=true;
					mHandler.sendEmptyMessageDelayed(PASS_STATE, 100);
					}
					break;
				}
				return false;	
			}	
		});
		pad2.setOnHoverListener(new OnHoverListener() {
			@Override		  
			public boolean onHover(View v, MotionEvent event) {	   
				int what = event.getAction();  
				switch(what){  
				case MotionEvent.ACTION_HOVER_ENTER: { 
					pad2.setBackgroundColor(Color.GREEN);
					padBooleans[1]=true;
					mHandler.sendEmptyMessageDelayed(PASS_STATE, 100);
				   }
					break;
				}
				return false;	
			}	
		});
		
		pad3.setOnHoverListener(new OnHoverListener() {
			@Override		  
			public boolean onHover(View v, MotionEvent event) {	   
				int what = event.getAction();  
				switch(what){  
				case MotionEvent.ACTION_HOVER_ENTER:{ 
					pad3.setBackgroundColor(Color.GREEN);	
					padBooleans[2]=true;
					mHandler.sendEmptyMessageDelayed(PASS_STATE, 100);
				}break;
				}
				return false;	
			}	
		});
		
		pad4.setOnHoverListener(new OnHoverListener() {
			@Override		  
			public boolean onHover(View v, MotionEvent event) {	   
				int what = event.getAction();  
				switch(what){  
				case MotionEvent.ACTION_HOVER_ENTER:{  
					pad4.setBackgroundColor(Color.GREEN);
					padBooleans[3]=true;
					mHandler.sendEmptyMessageDelayed(PASS_STATE, 100);
				}
					break;
				}
				return false;	
			}	
		});
		
	}
   /*
	 OnGenericMotionListener ogml1 = new OnGenericMotionListener () {
			@SuppressLint("NewApi")
			@Override
			public boolean onGenericMotion(View v, MotionEvent event) {
			// TODO Auto-generated method stub
				action2=event.getAction();
				int action1=event.getButtonState();
				//int action3=event.get
		    Log.v("limi touchpad 135","action1= "+action1);
			Log.v("limi touchpad 157","action2= "+action2);
			int what = event.getButtonState();
			if(action2==7){
			switch (what) {		
			case MotionEvent.BUTTON_PRIMARY:{
				left.setBackgroundColor(Color.GREEN);
				padBooleans[5]=true;
				mHandler.sendEmptyMessageDelayed(PASS_STATE, 100);
			}
			break;
			default:
				break;
			}
			
			 }
			return false;
			}

	 };
	 */
	
	
	
	  OnGenericMotionListener ogml2 = new OnGenericMotionListener () {
			@SuppressLint("NewApi")
			@Override
			public boolean onGenericMotion(View v, MotionEvent event) {
				// TODO Auto-generated method stub
			
				int what = event.getButtonState();
		
				switch (what) {	
				case MotionEvent.BUTTON_SECONDARY: {
					
					right.setBackgroundColor(Color.GREEN);
					padBooleans[4]=true;
					mHandler.sendEmptyMessageDelayed(PASS_STATE, 100);
			     	}
					break;	
				default:
					break;
			}
				return false;
			}

	 };
	 
	 
	 private Handler mHandler = new Handler() {   
			@Override     
			public void handleMessage(Message msg) {   
				switch (msg.what) {    
				case PASS_STATE: {
					if(padBooleans[0]&&padBooleans[1]&&padBooleans[2]
							&&padBooleans[3]&&padBooleans[4]&&padBooleans[5]){
						if(isTouchPadFirstTest){
							isTouchPadFirstTest=false;
							 ((Button)findViewById(R.id.btn_Retest)).setClickable(false);
								//((Button)findViewById(R.id.btn_Pass)).setClickable(false);
								((Button)findViewById(R.id.btn_Fail)).setClickable(false);
							findViewById(R.id.btn_Pass).performClick();
						}
					}	
					}
					break;  
				}  
			}
		};

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				return false;
			}
			return super.dispatchKeyEvent(event);
		}
	

}
