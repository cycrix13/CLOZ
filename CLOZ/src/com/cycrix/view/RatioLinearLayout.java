package com.cycrix.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class RatioLinearLayout extends LinearLayout {

	public RatioLinearLayout(Context context) {
		super(context);
	}
	
	public RatioLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public RatioLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	private double getRatio() {
		String prefix = "ratio:";
		String dsc = getContentDescription().toString();
		if (!dsc.startsWith(prefix))
			return 1;
		
		String ratioStr = dsc.substring(prefix.length());
		try {
			return Double.parseDouble(ratioStr);
		} catch(Exception e) {
			return 1;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int mode = MeasureSpec.getMode(widthMeasureSpec);
		int size = MeasureSpec.getSize(widthMeasureSpec);
		int spec = MeasureSpec.makeMeasureSpec((int) (size / getRatio()), mode);
		super.onMeasure(widthMeasureSpec, spec);
	}
}
