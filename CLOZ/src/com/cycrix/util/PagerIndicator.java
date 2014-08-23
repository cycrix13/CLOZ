package com.cycrix.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.ViewById;
import com.jorgebs.cloz.R;

public class PagerIndicator extends RelativeLayout implements OnPageChangeListener {
	
	private static final int MAX_ITEM 	= 5; 
	private static final int MAX_ITEM_2	= MAX_ITEM / 2;
	private static final int DOT_WIDTH	= 14;
	
	private static final int RID_LEFT 	= 1;
	private static final int RID_RIGHT 	= 2;
	private static final int RID_LOW 	= 3;
	private static final int RID_HIGH 	= 4;
	
	private ViewPager mPager;
	private int mCurrent = 0;
	private int mDotWidth;
	
	@ViewById(id = R.id.layoutDot)		private ViewGroup mLayoutDot;
	@ViewById(id = R.id.imgLeft)		private View mImgLeft;
	@ViewById(id = R.id.imgRight)		private View mImgRight;
	@ViewById(id = R.id.imgPointer)		private View mImgPointer;
	
	public int countOffset = 0;
	public int pageOffset = 0;
	
	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
		public void onPageSelected(int arg0) {}
		public void onPageScrolled(int arg0, float arg1, int arg2) {}
		public void onPageScrollStateChanged(int arg0) {}
	};
	
	public PagerIndicator(Context context) {
		super(context);
	}
	
	public PagerIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PagerIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setPager(ViewPager pager) {
		pager.setOnPageChangeListener(this);
		mPager = pager;
		mDotWidth = CyUtils.dpToPx(DOT_WIDTH, getContext());
		try {
			AndroidAnnotationParser.parse(this, this);
		} catch (Exception e) {}
		update(Math.max(0, mPager.getAdapter().getCount() + countOffset), 
				Math.max(0, pager.getCurrentItem() + pageOffset), 0);
	}
	
	private void update(int numItem, int currentItem, float offset) {
		// Update child count
		int numChild = numItem <= MAX_ITEM ? numItem : MAX_ITEM + 2;
		int delta = numChild - mLayoutDot.getChildCount();
		int step = Math.min(1, Math.max(-1, delta));
		
		for (int i = delta; i != 0; i -= step) {
			if (step > 0) {
				// add new view
				ImageView v = new ImageView(getContext());
				mLayoutDot.addView(v);
				android.view.ViewGroup.LayoutParams param = v.getLayoutParams();
				param.width = mDotWidth;
				param.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
				
				v.setImageResource(R.drawable.icon_dothidden);
				v.setLayoutParams(param);
			} else {
				// remove view
				mLayoutDot.removeViewAt(mLayoutDot.getChildCount() - 1);
			}
		}
		
		// Update image
		float a = numItem <= 1 ? 0 : 1;
		mImgLeft.setAlpha(a);
		mImgRight.setAlpha(a);
		mImgPointer.setAlpha(a);
		mLayoutDot.setAlpha(a);
		if (numItem <= 1)
			return;
		
		float dotOffset;
		if (numItem <= MAX_ITEM || currentItem < MAX_ITEM_2 || currentItem >= numItem - 1 - MAX_ITEM_2) {
			dotOffset = 0;
		} else {
			dotOffset = -mDotWidth * offset;
		}
		for (int i = 0; i < mLayoutDot.getChildCount(); i++)
			mLayoutDot.getChildAt(i).setTranslationX(dotOffset);

		float x = currentItem + offset;
		if (numItem > MAX_ITEM) {
			mLayoutDot.getChildAt(0).setAlpha(0);
			float dotAlpha = MAX_ITEM_2 + 1 - x;
			dotAlpha = Math.min(1, Math.max(0, dotAlpha));
			mLayoutDot.getChildAt(1).setAlpha(dotAlpha);
			if (x < MAX_ITEM_2 || currentItem >= numItem - 1 - MAX_ITEM_2) 
				dotAlpha = 1;
			else 
				dotAlpha = 1 - offset;
			mLayoutDot.getChildAt(2).setAlpha(dotAlpha);
			dotAlpha = x - 2;
			dotAlpha = Math.min(1, Math.max(0, dotAlpha));
			mImgLeft.setAlpha(dotAlpha);
			dotAlpha = numItem - MAX_ITEM_2 - 1 - x;
			dotAlpha = Math.min(1, Math.max(0, dotAlpha));
			mImgRight.setAlpha(dotAlpha);
			if (currentItem >= numItem - MAX_ITEM_2 - 1)
				dotAlpha = 0;
			else
				dotAlpha = Math.max(x - (numItem - MAX_ITEM_2 - 2), 0);
			mLayoutDot.getChildAt(6).setAlpha(dotAlpha);
			if (currentItem >= numItem - MAX_ITEM_2 - 1 )
				dotAlpha = 1;
			else if (currentItem < MAX_ITEM_2)
				dotAlpha = 0;
			else
				dotAlpha = offset;
			mLayoutDot.getChildAt(5).setAlpha(dotAlpha);
			
			float pointerPos = Math.min(x, MAX_ITEM_2) +
					Math.max(0, x - numItem + 1 + MAX_ITEM_2);
			mImgPointer.setTranslationX(pointerPos * mDotWidth);
		} else {
			mImgLeft.setAlpha(0);
			mImgRight.setAlpha(0);
			for (int i = 0; i < mLayoutDot.getChildCount(); i++)
				mLayoutDot.getChildAt(i).setAlpha(1);
			
			float pointerPos = x * mDotWidth - mDotWidth;
			mImgPointer.setTranslationX(pointerPos);
		}
	}
	
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mPageChangeListener = listener;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		mPageChangeListener.onPageScrollStateChanged(arg0);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		
		update(Math.max(0, mPager.getAdapter().getCount() + countOffset), 
				Math.max(0, position + pageOffset), positionOffset);
		
		mPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
	}

	@Override
	public void onPageSelected(int pos) {
		
		mPageChangeListener.onPageSelected(pos);
	}
}