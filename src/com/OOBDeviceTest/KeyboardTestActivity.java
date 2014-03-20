package com.OOBDeviceTest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import com.OOBDeviceTest.helper.ControlButtonUtil;

public class KeyboardTestActivity extends Activity {
	 ImageButton imageButton;
	
	
	 
	 

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
	        setContentView(R.layout.keyboadtest);
	       
	        imageButton=(ImageButton)findViewById(R.id.l4n13);
	        ControlButtonUtil.initControlButtonView(this);
	        
	    }
	
	    @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) { 
	    	if(keyCode==66){   	    	
			      setDownAction(imageButton);
	            } 
	      return true;//super.onKeyDown(keyCode, event);
	    }
	    
	    @Override
	    public boolean onKeyUp(int keyCode, KeyEvent event) {
	    	if(keyCode==66){   	    	
	    		setUpAction(imageButton);
	            } 
	       return true;
	    }   
	    
	    public void setUpAction(View v){
	    	 v.setBackgroundColor(Color.GREEN);
	      }
	    public void setDownAction(View v){	
	         v.setBackgroundColor(Color.RED);
	      }
	
	     public void onPass(View v){
	    	 v.setBackgroundColor(Color.GREEN);
	       }
	       
	     @Override
		public boolean dispatchKeyEvent(KeyEvent event) {
	     	if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
		    	return false;
		     }
		   return super.dispatchKeyEvent(event);
	    }
	     
}
