package com.cycrix.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class FullScreenLayout extends RelativeLayout {
	
	private Context mCt;

	public FullScreenLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mCt = context;
	}
	
	public FullScreenLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCt = context;
	}

	public FullScreenLayout(Context context) {
		super(context);
		mCt = context;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		if (isInEditMode()) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		} else {
			WindowManager wm = (WindowManager) mCt.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			widthMeasureSpec = MeasureSpec.makeMeasureSpec(display.getWidth(), MeasureSpec.EXACTLY);
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(display.getHeight(), MeasureSpec.EXACTLY);
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}

