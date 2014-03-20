package com.OOBDeviceTest;

import com.OOBDeviceTest.helper.ControlButtonUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PowerTestActivity extends Activity {
	 
	private TextView mStatus;
    private TextView mLevel;
    private TextView mAcstate;

    private IntentFilter mIntentFilter; 
    private static final int EVENT_TICK = 1;
    private String statusString="";
    private String AcstatusString="";
    private int level;
    private boolean pass=true;
    private int plugType ;
 
    private Handler mHandler = new Handler() {
        @Override
          public void handleMessage(Message msg) {
              switch (msg.what) {
                 case EVENT_TICK:
               	  updateBatteryStats();
                  sendEmptyMessageDelayed(EVENT_TICK, 1000); 
                  break;
             }
       }
    };
    
     private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
   	  @Override
          public void onReceive(Context context, Intent intent) {
              String action = intent.getAction();
              if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                   plugType = intent.getIntExtra("plugged", 0);
                  level=intent.getIntExtra("level", 0);
                 // mLevel.setText("" + intent.getIntExtra("level", 0));
                  int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
                  if (plugType > 0)  AcstatusString ="AC";
               	   else AcstatusString ="no AC"; 
              if (status == BatteryManager.BATTERY_STATUS_CHARGING) {	   
           	   statusString = "charging";  
               } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                 statusString = "discharging"; 
              } else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                 statusString = "not_charging"; 
               } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                 statusString = "full"; 
               } else statusString = "unknow"; 
            
           }
        }
    };

   @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.powertest);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        ControlButtonUtil.initControlButtonView(this);
        findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
	    testpowerlevel();  
	    Button retestButton = (Button) findViewById(R.id.btn_Retest);
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onPause();
				 testpowerlevel();  
				onResume();
			}
		});
	    
   }

     @Override
    public void onResume() {
       super.onResume();
       mStatus = (TextView)findViewById(R.id.status);
       mLevel = (TextView)findViewById(R.id.level);
       mAcstate=(TextView)findViewById(R.id.acstate);
       
       
       mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
       registerReceiver(mIntentReceiver, mIntentFilter);  
     }

   @Override
    public void onPause() {
         super.onPause();
         mHandler.removeMessages(EVENT_TICK);
         unregisterReceiver(mIntentReceiver);
     }
   private void updateBatteryStats() {
   	     mStatus.setText(statusString);
         mAcstate.setText(AcstatusString);
         mLevel.setText(level+"%");
           }
   
  
   
	  public void testpowerlevel(){
			Dialog alertDialog = new AlertDialog.Builder(this).
		    	    //setMessage("Battery Level is right?").		
		    	    setMessage(getString(R.string.battery_message)).
		    	    setPositiveButton("YES", new DialogInterface.OnClickListener() {   
		    	     @Override
		    	     public void onClick(DialogInterface dialog, int which) { 
		    	    	 Log.v("DCACTest", "The power level  is right!");	
		    	    	 powerstate2();
		    	     }
		    	    }).
		    	    setNegativeButton("NO", new DialogInterface.OnClickListener() {
		    	     @Override
		    	     public void onClick(DialogInterface dialog, int which) {  
		    	    	 Log.e("DCACTest", "The power level is wrong!");
		    	    	 pass=false;
		    	    	 powerstate2();
		    	     }
		    	    }).
		    	    create();
		    alertDialog.setCancelable(false);
		    alertDialog.show();  
	  }
	  
	  public void powerstate1(){  
		   Dialog alertDialog = new AlertDialog.Builder(this).
		    	    setMessage(getString(R.string.ac_message)).
		    	    //setMessage("Is the battery  charged when AC  pluged in?(Pluging in AC,please)").
		    	    setPositiveButton("YES", new DialogInterface.OnClickListener() {   
		    	     @Override
		    	     public void onClick(DialogInterface dialog, int which) {
		    	    	Log.v("DCACTest", "The power state is right when AC is pluged in!"); 
		    	    	if(plugType<=0)Toast.makeText(getApplicationContext(), getString(R.string.dcac_ac), 9000).show();
		    	    	if (plugType>0&&pass) {
		    					findViewById(R.id.btn_Pass).performClick();
		    				}
		    	     }
		    	    }).
		    	    setNegativeButton("NO", new DialogInterface.OnClickListener() {
		    	     @Override
		    	     public void onClick(DialogInterface dialog, int which) {
		    	    	 pass=false;
		    	    	 Log.e("DCACTest", "The power state is wrong when AC is pluged in  !");
		    	     }
		    	    }).
		    	    create();
		   alertDialog.setCancelable(false);
		   alertDialog.show();
		  
	  }
	  
	  public void powerstate2(){
		  Dialog alertDialog = new AlertDialog.Builder(this).
				      setMessage(getString(R.string.no_ac_message)).
				//  setMessage("Is the battery  dis_charged when AC  pluged out?(Pluging out AC,please)").
				  setPositiveButton("YES", new DialogInterface.OnClickListener() {   
		    	     @Override
		    	     public void onClick(DialogInterface dialog, int which) {	
		    	    	 Log.v("DCACTest","The power state is right when AC is unplugged!");
		    	    	if(plugType>0) Toast.makeText(getApplicationContext(), getString(R.string.dcac_no_ac), 9000).show();
		    	    	else powerstate1(); 
		    	      }
		    	    	
		    	    }).
		    	    setNegativeButton("NO", new DialogInterface.OnClickListener() {
		    	     @Override
		    	     public void onClick(DialogInterface dialog, int which) {
		    	    	 Log.e("DCACTest","The power state is wrong when AC is unplugged!");	
		    	    	 pass=false;
		    	    	 powerstate1();
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







