package com.OOBDeviceTest;

import com.OOBDeviceTest.StressTest.StressTestActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class TestSelectActivity extends Activity implements OnClickListener{

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testselect);
		Button deviceTestBtn = (Button) findViewById(R.id.devicetest_btn);
		deviceTestBtn.setOnClickListener(this);
		Button stressTestBtn = (Button) findViewById(R.id.stresstest_btn);
		stressTestBtn.setOnClickListener(this);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(0, menu.FIRST+1, Menu.NONE, "Exit");
		menu.add(0, menu.FIRST+2, Menu.NONE, "temp");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case Menu.FIRST + 1:
				finish();
				return true;
			case Menu.FIRST + 2:
			Intent intent = new Intent(TestSelectActivity.this,
					CompareServerInfoActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.devicetest_btn:
			Intent intent1 = new Intent(this, DeviceTest.class);
			startActivity(intent1);
			break;
		case R.id.stresstest_btn:
			Intent intent2 = new Intent(this, StressTestActivity.class);
			startActivity(intent2);
			
			break;
		default:
			break;
		}
	}
	
}
