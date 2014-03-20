package com.OOBDeviceTest.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class VoiceWaveView extends View {

	private Recorder mRecorder;
	
	
	public VoiceWaveView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public VoiceWaveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public VoiceWaveView(Context context) {
		super(context);
	}
	
	public void setRecorder(Recorder recorder) {
    	mRecorder = recorder;
    	invalidate();
    }
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		
	}
	
	

}
