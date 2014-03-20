package com.OOBDeviceTest.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class MyGridView extends FrameLayout implements View.OnClickListener {
	private String TAG = "MyGridView";
	private int mColumn = 1;
	private OnItemClickListener onItemClickListener;

	public MyGridView(Context context) {
		this(context, null, 0);
	}

	public MyGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MyGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setColumnCount(int column) {
		this.mColumn = column;
	}

	
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		final int childCount = getChildCount();
		if (0 == childCount) {
			super.onLayout(changed, left, top, right, bottom);
			return;
		}
		final int width = right - left;
		final int height = bottom - top;
		final int column = mColumn;
		final int row = (childCount - 1) / column + 1;
		final int childHeight = height / row;
		final int childWidth = width / column;

		int childLeft = left;
		int childTop = top;
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			child.layout(childLeft, childTop, childLeft + childWidth, childTop
					+ childHeight);
			childLeft += childWidth;
			if (((i + 1) % column) == 0) {
				childLeft = left;
				childTop += childHeight;
			}
		}
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}
	
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		child.setOnClickListener(this);
	}

	public interface OnItemClickListener {
		void onItemClick(ViewParent parent, View view, int position);
	}

	
	public void onClick(View v) {
		if (null == onItemClickListener) {
			return;
		}
		final int childCount = getChildCount();
		int istouchchildcount = 0;
		int touchid = -1;
		for (int i = 0; i < childCount; i++) {
			if (v == getChildAt(i)) {
				Log.d(TAG, "__________-----------onClick(),      i = " + i);
				touchid = i;
			}
			if(((MyItemView)getChildAt(i)).getIsTouch()){
				istouchchildcount ++;
			}
		}
		Log.d(TAG, "__________-----------onClick(),  touchid = " + touchid + "     istouchchildcount = " + istouchchildcount);
		if(touchid >= 0 && istouchchildcount == 0)
			onItemClickListener.onItemClick(this, v, touchid);
	}
}
