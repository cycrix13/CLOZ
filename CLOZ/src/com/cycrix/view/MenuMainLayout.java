package com.cycrix.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class MenuMainLayout extends FrameLayout {

	public MenuMainLayout(Context context) {
		super(context);
	}
	
	public MenuMainLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MenuMainLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		View v = getChildAt(0);
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		v.setTranslationY(screenWidth);
	}
	
	
}
