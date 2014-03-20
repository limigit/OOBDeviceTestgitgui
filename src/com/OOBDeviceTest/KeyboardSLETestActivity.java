package com.OOBDeviceTest;


import com.OOBDeviceTest.helper.ControlButtonUtil;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.Intent;  //luodh
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

class BoardKeySLE{
	  int boardkeyCode;
	  String boardkeyName;
}

public class KeyboardSLETestActivity extends Activity  {

	int []key=new int[]{4,3,231,176,91,25,24,223,224,220,226,222,228,64,120,112,
	   		68,8,9,10,11,12,13,14,15,16,7,76,70,67,
	   		61,45,51,33,46,48,54,49,37,43,44,71,72,73,
	   		115,29,47,32,34,35,36,38,39,40,74,75,66,
	   		59,81,53,52,31,50,30,42,41,55,56,69,60,
	   		113,84,227,57,62,58,82,114,92,19,93,21,20,22};
	 ImageButton[] imageButton=new ImageButton[84];
   String[] keyName=new String[]{"Back","Home" ,"Launcher","Settings","Volume Mute","Volume -","Volume +","Brightness -",
           "Brightness +","Wlan","Lock","Touch Pad","Screen Rotate","Browser","SysRq","Detele",
"..","1","2","3","4","5","6","7","8","9","0","?","+","Backspace",
"Tab","Q","W","E","R","T","Z","U","I","O","P","S`'","D-","Z`'",
"Caps Lock","A","S","D","F","G","H","J","K","L","C`'","C`","Enter",
"L_Shift",">","Y","X","C","V","B","N","M",",",".","-","R_Shift",
"L_Ctrl","Search","Recent task","L_Alt","Space","R_Alt","Menu","R_Ctrl","PgUp","Pad_Up","PgDn",
         "Pad_Left","Pad_Down","Pad_Right"};
	 
   BoardKeySLE[] bkey=new BoardKeySLE[84];
	  {
		  for(int i=0;i<84;i++)
		 {bkey[i]=new BoardKeySLE();
	      }
  	  for(int i=0;i<84;i++){
	      bkey[i].boardkeyCode=key[i];
	      bkey[i].boardkeyName=keyName[i];
	      }
	   }
	  
   private final static String TAG = "KeyboardUKTestActivity";  
   private TextView edi22;
   //private Button but2;
   private boolean[] test_states=new boolean[84];
   private final int B2=2000;
   private int mKeyCode = 0;
   private boolean down = false;
   private final int TEST_MODE=100;
   int mDockMode = Intent.EXTRA_DOCK_STATE_UNDOCKED;
   Button failButton;
  // Button skipButton;
   Button retestButton;
   Intent keyIntent;
	 
   BroadcastReceiver mKeyboardHWTestReceiver = new BroadcastReceiver() {
       public void onReceive(Context context, Intent intent) {
           if ("com.DeviceTest.KeyboardHardwareTestActivity.KeyboardHWTestActionFilter".equals(intent.getAction())) {
               mKeyCode = intent.getIntExtra("keyCode",  Intent.EXTRA_DOCK_STATE_UNDOCKED);
               down = intent.getBooleanExtra("DownOrUp",  false );
               Log.d("KeyboardTest", "Aha we got *********************** " );
               Log.d("KeyboardTest", "Aha we got a KeyCode : " + mKeyCode + down);
               Log.d("KeyboardTest", "Aha we got *********************** " );
            	   if(down)button2Downaction(mKeyCode);
            	   else buttonUpaction(mKeyCode);
           }
       }
   };

  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
      setContentView(R.layout.keyboard_sle_test);
      getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);    
      imageButton[0]=(ImageButton)findViewById(R.id.l1n1); imageButton[1]=(ImageButton)findViewById(R.id.l1n2);imageButton[2]=(ImageButton)findViewById(R.id.l1n3); 
      imageButton[3]=(ImageButton)findViewById(R.id.l1n4);imageButton[4]=(ImageButton)findViewById(R.id.l1n5);imageButton[5]=(ImageButton)findViewById(R.id.l1n6);
      imageButton[6]=(ImageButton)findViewById(R.id.l1n7);imageButton[7]=(ImageButton)findViewById(R.id.l1n8);imageButton[8]=(ImageButton)findViewById(R.id.l1n9);
      imageButton[9]=(ImageButton)findViewById(R.id.l1n10);imageButton[10]=(ImageButton)findViewById(R.id.l1n11);imageButton[11]=(ImageButton) findViewById(R.id.l1n12);
      imageButton[12]=(ImageButton)findViewById(R.id.l1n13);imageButton[13]=(ImageButton)findViewById(R.id.l1n14);imageButton[14]=(ImageButton)findViewById(R.id.l1n15);
      imageButton[15]=(ImageButton)findViewById(R.id.l1n16); 
      imageButton[16]=(ImageButton)findViewById(R.id.l2n1);imageButton[17]=(ImageButton)findViewById(R.id.l2n2);imageButton[18]=(ImageButton)findViewById(R.id.l2n3); 
      imageButton[19]=(ImageButton)findViewById(R.id.l2n4);imageButton[20]=(ImageButton)findViewById(R.id.l2n5);imageButton[21]=(ImageButton)findViewById(R.id.l2n6);
      imageButton[22]=(ImageButton)findViewById(R.id.l2n7); imageButton[23]=(ImageButton)findViewById(R.id.l2n8);imageButton[24]=(ImageButton)findViewById(R.id.l2n9);
      imageButton[25]=(ImageButton)findViewById(R.id.l2n10);imageButton[26]=(ImageButton)findViewById(R.id.l2n11);imageButton[27]=(ImageButton)findViewById(R.id.l2n12);
      imageButton[28]=(ImageButton)findViewById(R.id.l2n13);imageButton[29]=(ImageButton)findViewById(R.id.l2n14); 
      imageButton[30]=(ImageButton)findViewById(R.id.l3n1);imageButton[31]=(ImageButton)findViewById(R.id.l3n2);imageButton[32]=(ImageButton)findViewById(R.id.l3n3); 
      imageButton[33]=(ImageButton)findViewById(R.id.l3n4);imageButton[34]=(ImageButton)findViewById(R.id.l3n5);imageButton[35]=(ImageButton)findViewById(R.id.l3n6);
      imageButton[36]=(ImageButton)findViewById(R.id.l3n7);imageButton[37]=(ImageButton)findViewById(R.id.l3n8);imageButton[38]=(ImageButton)findViewById(R.id.l3n9);
      imageButton[39]=(ImageButton)findViewById(R.id.l3n10);imageButton[40]=(ImageButton)findViewById(R.id.l3n11);imageButton[41]=(ImageButton)findViewById(R.id.l3n12);
      imageButton[42]=(ImageButton)findViewById(R.id.l3n13);imageButton[43]=(ImageButton)findViewById(R.id.l3n14);
      imageButton[44]=(ImageButton)findViewById(R.id.l4n1);imageButton[45]=(ImageButton)findViewById(R.id.l4n2);imageButton[46]=(ImageButton)findViewById(R.id.l4n3); 
      imageButton[47]=(ImageButton)findViewById(R.id.l4n4);imageButton[48]=(ImageButton)findViewById(R.id.l4n5);imageButton[49]=(ImageButton)findViewById(R.id.l4n6);
      imageButton[50]=(ImageButton)findViewById(R.id.l4n7);imageButton[51]=(ImageButton)findViewById(R.id.l4n8);imageButton[52]=(ImageButton)findViewById(R.id.l4n9);
      imageButton[53]=(ImageButton)findViewById(R.id.l4n10);imageButton[54]=(ImageButton)findViewById(R.id.l4n11);imageButton[55]=(ImageButton)findViewById(R.id.l4n12);
      imageButton[56]=(ImageButton)findViewById(R.id.l4n13);
      
      imageButton[57]=(ImageButton)findViewById(R.id.l5n1);imageButton[58]=(ImageButton)findViewById(R.id.l5n1111);
      imageButton[59]=(ImageButton)findViewById(R.id.l5n2); imageButton[60]=(ImageButton)findViewById(R.id.l5n3); 
      imageButton[61]=(ImageButton)findViewById(R.id.l5n4);imageButton[62]=(ImageButton)findViewById(R.id.l5n5);imageButton[63]=(ImageButton)findViewById(R.id.l5n6);
      imageButton[64]=(ImageButton)findViewById(R.id.l5n7);imageButton[65]=(ImageButton)findViewById(R.id.l5n8);imageButton[66]=(ImageButton)findViewById(R.id.l5n9);
      imageButton[67]=(ImageButton)findViewById(R.id.l5n10);imageButton[68]=(ImageButton)findViewById(R.id.l5n11);imageButton[69]=(ImageButton)findViewById(R.id.l5n12);
     
      
      
      imageButton[70]=(ImageButton)findViewById(R.id.l6n1);imageButton[71]=(ImageButton)findViewById(R.id.l6n2);imageButton[72]=(ImageButton)findViewById(R.id.l6n3); 
      imageButton[73]=(ImageButton)findViewById(R.id.l6n4);imageButton[74]=(ImageButton)findViewById(R.id.l6n5);imageButton[75]=(ImageButton)findViewById(R.id.l6n6);
      imageButton[76]=(ImageButton)findViewById(R.id.l6n7);imageButton[77]=(ImageButton)findViewById(R.id.l6n8);
      imageButton[78]=(ImageButton)findViewById(R.id.l6n9u);imageButton[79]=(ImageButton)findViewById(R.id.l6n10u);imageButton[80]=(ImageButton)findViewById(R.id.l6n11u);
      imageButton[81]=(ImageButton)findViewById(R.id.l6n9d);imageButton[82]=(ImageButton)findViewById(R.id.l6n10d); imageButton[83]=(ImageButton)findViewById(R.id.l6n11d);
      
      failButton = (Button)findViewById(R.id.btn_Fail);
    //  skipButton = (Button)findViewById(R.id.btn_Skip);
      retestButton=(Button)findViewById(R.id.btn_Retest);
       keyIntent=new Intent();
       
      failButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				keyIntent.putExtra("key_test_result", "Fail");
				setResult(1111,keyIntent);
				KeyboardSLETestActivity.this.finish();
			}
		});
		/*
      skipButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				keyIntent.putExtra("key_test_result", "Skip");
				setResult(1111,keyIntent);
				KeyboardUSTestActivity.this.finish();
			}
		});
		*/
		retestButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				initState(); 
			}
		});
      
      
       edi22=(TextView)findViewById(R.id.keytext22); 
  
     //  but2=(Button)findViewById(R.id.button2);
       
      // ControlButtonUtil.initControlButtonView(this);
      findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
       initState(); 
      // but2.setOnClickListener(this);
      // but2.setTag(B2);  
      	
 } 
  
  @Override
  public void onResume(){
	  super.onResume();
	  Log.d("KeyboardTest", "sendBroadcase to com.android.internal.policy.impl.KeyboardHWTestActionFilter for teset" );
	     Intent intent = new Intent("com.android.internal.policy.impl.KeyboardHWTestActionFilter");
	     intent.putExtra("StartTest", 86);
	     sendBroadcast(intent);
	       
	     IntentFilter KeyboardHWTestActionFilter = new IntentFilter("com.DeviceTest.KeyboardHardwareTestActivity.KeyboardHWTestActionFilter");
	     registerReceiver(mKeyboardHWTestReceiver,  KeyboardHWTestActionFilter); 
  }
  
  @Override
  public void onStop(){
	  super.onStop();
	  Log.d("KeyboardTest", "sendBroadcase to com.android.internal.policy.impl.KeyboardHWTestActionFilter for teset" );
	     Intent intent = new Intent("com.android.internal.policy.impl.KeyboardHWTestActionFilter");
	     intent.putExtra("StartTest", 93);
	     sendBroadcast(intent);
	       
	     IntentFilter KeyboardHWTestActionFilter = new IntentFilter("com.DeviceTest.KeyboardHardwareTestActivity.KeyboardHWTestActionFilter");
	     registerReceiver(mKeyboardHWTestReceiver,  KeyboardHWTestActionFilter); 
  }
  
  /*
   @Override
  public void onClick(View v) {  
	  initState(); 
    }
*/

 public void button2Downaction(int keyCode){ 	
 	for(int i=0;i<84;i++){
	       if(keyCode==bkey[i].boardkeyCode){   
	    	    setDownAction(imageButton[i]);
             }
          }  
       }

  public void buttonUpaction(int keyCode){
	     for(int i=0;i<84;i++){
	        if(keyCode==bkey[i].boardkeyCode){ 
	    	    edi22.setText("KeyCode:" + keyCode+ " keyName:" + bkey[i].boardkeyName); 
	    	    setUpAction(imageButton[i]);
	    	    test_states[i]=true;
	    	    mHandler.sendEmptyMessageDelayed(TEST_MODE, 100);
	    	    Log.v(TAG, "KeyCode:"+keyCode+" keyName:"+ bkey[i].boardkeyName);
	          }
	      }
    }
  
	private Handler mHandler = new Handler() {   
		@Override     
		public void handleMessage(Message msg) {   
				switch (msg.what) {    
				case TEST_MODE: {
					for(int i=0;i<84;i++){
				       if(!test_states[i])return;
					}
	//findViewById(R.id.btn_Pass).performClick();
					
					keyIntent.putExtra("key_test_result", "Pass");
					setResult(1111,keyIntent);
					KeyboardSLETestActivity.this.finish();
				}
					break;  
				} 
		}
	
	};

  public void initState() {
       for(int i=0;i<84;i++){
       imageButton[i].setBackgroundColor(Color.WHITE);
       test_states[i]=false;
       }
       edi22.setText("");
     
    }
  
  public void setUpAction(View v){
	  v.setBackgroundColor(Color.GREEN);
  }
   public void setDownAction(View v){	
    v.setBackgroundColor(Color.RED);
  }
  
  	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

}
