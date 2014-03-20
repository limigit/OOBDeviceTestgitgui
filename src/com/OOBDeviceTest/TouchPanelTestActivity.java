 package com.OOBDeviceTest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.TextView;
import com.OOBDeviceTest.helper.ControlButtonUtil;
import com.OOBDeviceTest.helper.Drawl;
import com.OOBDeviceTest.R;
import android.view.KeyEvent;
import android.os.IECService;
import android.os.ServiceManager;
import android.os.RemoteException;


public class TouchPanelTestActivity extends Activity{
  
    TextView tttt;
    private Drawl bDrawl;
    IECService ecService;

    @SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_SHOW_FULLSCREEN);

        AbsoluteLayout.LayoutParams temp=new AbsoluteLayout.LayoutParams(1360,768,0, 0);
		setContentView(R.layout.touchpaneltest);
		tttt=(TextView)findViewById(R.id.tttt);
	    bDrawl=new Drawl(this, tttt);
		addContentView(bDrawl, temp);
		tttt.setText(getResources().getString(R.string.touchpanel_test_left));
		ControlButtonUtil.initControlButtonView(this);
		findViewById(R.id.btn_Pass).setVisibility(View.GONE);
		findViewById(R.id.btn_Fail).setVisibility(View.GONE);
		findViewById(R.id.btn_Skip).setVisibility(View.GONE);
			findViewById(R.id.btn_Retest).setVisibility(View.GONE);
			
			ecService = IECService.Stub.asInterface(ServiceManager.getService("ECServiceinfo"));
			  try { 
				
		     		ecService.DeviceCtrl(8,0);	
		  		} catch (RemoteException e) {
		  			e.printStackTrace();
		  		}
		
		IntentFilter TouchPanelTestActionFilter = new IntentFilter("com.DeviceTest.TouchPanelTestActivity.TouchPanelTestActionFilter");
	     registerReceiver(mTouchPanelTestReceiver,  TouchPanelTestActionFilter); 
	}
    
    protected void onStop() {
    	super.onStop();
    	ecService = IECService.Stub.asInterface(ServiceManager.getService("ECServiceinfo"));
		  try { 
			
	     		ecService.DeviceCtrl(8,1);	
	  		} catch (RemoteException e) {
	  			e.printStackTrace();
	  		}
    	unregisterReceiver(mTouchPanelTestReceiver); 
    };
  
    BroadcastReceiver mTouchPanelTestReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        	Bundle bundle = intent.getExtras();
        	if (bundle.getInt("test_result") == 0) {
        		findViewById(R.id.btn_Skip).performClick();
			}else if (bundle.getInt("test_result") == 1) {
				findViewById(R.id.btn_Fail).performClick();
			}else {
				findViewById(R.id.btn_Pass).performClick();
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

