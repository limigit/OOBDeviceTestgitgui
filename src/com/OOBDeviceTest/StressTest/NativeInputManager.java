package com.OOBDeviceTest.StressTest;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class NativeInputManager {

	public static void injectKeyEvent(KeyEvent paramKeyEvent,
			boolean paramBoolean) {
		long downTime = paramKeyEvent.getDownTime();
		long EventTime = paramKeyEvent.getEventTime();
		int action = paramKeyEvent.getAction();
		int keycode = paramKeyEvent.getKeyCode();
		int repeatCount = paramKeyEvent.getRepeatCount();
		int metaState = paramKeyEvent.getMetaState();
		int deviceId = paramKeyEvent.getDeviceId();
		int scanCode = paramKeyEvent.getScanCode();
		int source = paramKeyEvent.getSource();
		int flag = paramKeyEvent.getFlags();
		if (source == 0)
			source = 257;
		if (EventTime == 0L)
			EventTime = SystemClock.uptimeMillis();
		if (downTime == 0L)
			downTime = EventTime;
		KeyEvent localKeyEvent = new KeyEvent(downTime, EventTime, action,
				keycode, repeatCount, metaState, deviceId, scanCode, flag
						| KeyEvent.FLAG_FROM_SYSTEM, source);
		InputManager localInputManager = InputManager.getInstance();

		localInputManager.injectInputEvent(localKeyEvent, 1);
	}

	public static void injectPointerEvent(MotionEvent paramMotionEvent) {
		MotionEvent localMotionEvent = MotionEvent.obtain(paramMotionEvent);
		if ((0x2 & localMotionEvent.getSource()) == 0)
			localMotionEvent.setSource(4098);
		InputManager.getInstance().injectInputEvent(localMotionEvent, 0);
	}

	public static void sendKeyDownUpSync(final int paramInt) {
		new Thread(new Runnable() {
			public void run() {
				NativeInputManager.injectKeyEvent(new KeyEvent(0, paramInt),
						true);
				NativeInputManager.injectKeyEvent(new KeyEvent(1, paramInt),
						true);
			}
		}).start();
	}

	public static void sendTouchEventSync(final float paramFloat1,
			final float paramFloat2) {
		new Thread(new Runnable() {
			public void run() {
				try {
					NativeInputManager.injectPointerEvent(MotionEvent.obtain(
							SystemClock.uptimeMillis(),
							100L + SystemClock.uptimeMillis(), 0, paramFloat1,
							paramFloat2, 0));
				} catch (SecurityException localSecurityException1) {
					try {
						while (true) {
							NativeInputManager.injectPointerEvent(MotionEvent
									.obtain(SystemClock.uptimeMillis(),
											100L + SystemClock.uptimeMillis(),
											1, paramFloat1, paramFloat2, 0));
							return;
						}
					} catch (SecurityException localSecurityException2) {
					}
				}
			}
		}).start();
	}
}
