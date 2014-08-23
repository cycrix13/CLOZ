package com.jorgebs.cloz;

import com.jorgebs.cloz.ZoomView.Listener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.ViewConfiguration;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;

public class NoImageView extends ImageView implements OnGestureListener {
	
	private static Listener sListener;
	
	private GestureDetector mDetector;
	private Listener mListener;

	public NoImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NoImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public NoImageView(Context context, Listener listener) {
		super(context);
		mDetector = new GestureDetector(this);
		mListener = listener;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mSwipeComplete = false;
			break;
		}
		
		return mDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent arg0) {

		return true;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		
	}

	private boolean mSwipeComplete;
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float distanceX, float distanceY) {
		
		float touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		if (Math.abs(distanceY) > touchSlop && !mSwipeComplete) {
			mSwipeComplete = true;
			
			if (distanceY < 0)
				mListener.onRequestDown();
			else
				mListener.onRequestUp();
		}
		
		return true;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {

		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {

		return false;
	}
}
