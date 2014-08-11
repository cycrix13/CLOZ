package com.hienbibi.cloz;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class ZoomPager extends ViewPager {

	public ZoomPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ZoomPager(Context context) {
		super(context);
		setOffscreenPageLimit(10);
	}
	
	private PointF mBegin = new PointF();
	private boolean mComplete = false;
	private boolean mIsHorizon = false;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		
		super.onInterceptTouchEvent(event);
		ViewConfiguration config = new ViewConfiguration();
		
		float touchSlop = config.getScaledTouchSlop();
		
		try {
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				mBegin.x = event.getX();
				mBegin.y = event.getY();
				break;
			case MotionEvent.ACTION_MOVE: {
				
				if (mComplete)
					return mIsHorizon;
				
				float dx = event.getX() - mBegin.x;
				float dy = event.getY() - mBegin.y;
				float r = (float) Math.sqrt(dx*dx + dy*dy);
				if (r > touchSlop) {
					mIsHorizon = Math.abs(dx) > Math.abs(dy);
					mComplete = true;
					return mIsHorizon;
				}
			}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mComplete = false;
				mIsHorizon = false;
				break;
			}
		} catch (Exception e) {
		}

		return false;
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
