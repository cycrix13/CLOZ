package com.example.zoomview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ZoomPager extends ViewPager {

	public ZoomPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ZoomPager(Context context) {
		super(context);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		try {
			return super.onInterceptTouchEvent(arg0);
		} catch (Exception e) {
		}
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		
		try {
			return super.onTouchEvent(arg0);
		} catch (Exception e) {
		}
		return false;
	}
}
