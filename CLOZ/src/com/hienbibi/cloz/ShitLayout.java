package com.hienbibi.cloz;

import com.cycrix.util.CyUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

public class ShitLayout extends ViewFlipper {
	
	public float startY, startX;
	private boolean bigY;
	private float range;
	private boolean complete = false;

	public ShitLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		range = CyUtils.dpToPx(16, context);
	}

	public ShitLayout(Context context) {
		super(context);
		range = CyUtils.dpToPx(16, context);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			complete = false;
			startX = event.getX();
			startY = event.getY();
			break;

		case MotionEvent.ACTION_MOVE:
			
			if (complete)
				return bigY;
			
			float dx = event.getX() - startX;
			float dy = event.getY() - startY;
			float distance = (float) Math.sqrt(dx*dx+dy*dy);
			
			if (!complete && distance > range) {
				bigY = Math.abs(dy) > Math.abs(dx);
				complete = true;
				return bigY;
			}
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_CANCEL:
			complete = false;
			break;
		}
		
		return false;
	}
}
