package com.cycrix.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class InlineLayout extends FrameLayout {

	public InlineLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public InlineLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public InlineLayout(Context context) {
		super(context);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int xOffset = 0;
		int yOffset = 0;
		int maxHeight = 0;
		int w = MeasureSpec.getSize(widthMeasureSpec);
		
		int padding = + CyUtils.dpToPx(4, getContext());
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			
			if (xOffset + child.getMeasuredWidth() < w) {
				child.layout(xOffset, yOffset, xOffset + child.getMeasuredWidth(), yOffset + child.getMeasuredHeight());
				maxHeight = Math.max(child.getMeasuredHeight(), maxHeight);
				xOffset += child.getMeasuredWidth() + padding;
			} else {
				xOffset = 0;
				yOffset += maxHeight + padding;
				maxHeight = child.getMeasuredHeight();
				child.layout(xOffset, yOffset, xOffset + child.getMeasuredWidth(), yOffset + child.getMeasuredHeight());
				xOffset += child.getMeasuredWidth() + padding;
			}
		}
		
		setMeasuredDimension(getMeasuredWidth(), yOffset + maxHeight);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		int xOffset = 0;
		int yOffset = 0;
		int maxHeight = 0;
		
		int padding = + CyUtils.dpToPx(4, getContext());
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			
			if (xOffset + child.getMeasuredWidth() < right - left) {
				child.layout(xOffset, yOffset, xOffset + child.getMeasuredWidth(), yOffset + child.getMeasuredHeight());
				maxHeight = Math.max(child.getMeasuredHeight(), maxHeight);
				xOffset += child.getMeasuredWidth() + padding;
			} else {
				xOffset = 0;
				yOffset += maxHeight + padding;
				maxHeight = child.getMeasuredHeight();
				child.layout(xOffset, yOffset, xOffset + child.getMeasuredWidth(), yOffset + child.getMeasuredHeight());
				xOffset += child.getMeasuredWidth() + padding;
			}
		}
	}
}
