package com.example.zoomview;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.Scroller;

public class VerticalPager extends FrameLayout implements OnGestureListener {
	
	private PagerAdapter mAdapter;
	private int mPageIndex = 0;
	private int mPageCacheNum = 1;
	private HashMap<Integer, View> mPageCache;
	private float mPageTransOffset = 0;
	private GestureDetector mGestureDetector;
	private OverScroller mScroller;

	public VerticalPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public VerticalPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public VerticalPager(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		
		mPageCache = new HashMap<Integer, View>();
		mGestureDetector = new GestureDetector(getContext(), this);
		mScroller = new OverScroller(context);
		
		setBackgroundColor(0xFF000000);
	}
	
	public void setAdapter(PagerAdapter adapter, int pageIndex) {
		
		removeAllViews();
		mPageCache.clear();
		
		mPageIndex = pageIndex;
		mAdapter = adapter;
		
		iterateAdapter();
	}
	
	private void iterateAdapter() {
		
		for (Object entry : mPageCache.keySet().toArray()) {
			int key = (Integer) entry;
			if (key < mPageIndex - mPageCacheNum || key > mPageIndex + mPageCacheNum)
				purgePage(key);
		}
		
		loadPage(mPageIndex);
		for (int i = 1; i <= mPageCacheNum; i++) {
			loadPage(mPageIndex + i);
			loadPage(mPageIndex - i);
		}
	}
	
	private void loadPage(int pageIndex) {
		
		if (pageIndex < 0 || pageIndex > mAdapter.getCount() - 1)
			return;
		
		View viewPage = findPageByIndex(pageIndex);
		if (viewPage != null)
			return;
		
		viewPage = (View) mAdapter.instantiateItem(this, pageIndex);
		mPageCache.put(pageIndex, viewPage);
	}
	
	private View findPageByIndex(int pageIndex) {
		for (Entry<Integer, View> entry : mPageCache.entrySet())
			if (entry.getKey() == pageIndex)
				return entry.getValue();
		
		return null;
	}
	
	private void purgePage(int pageIndex) {
		
		if (pageIndex < 0 || pageIndex > mAdapter.getCount() - 1)
			return;
		
		View viewPage = findPageByIndex(pageIndex);
		if (viewPage == null)
			return;
		
		mAdapter.destroyItem(this, pageIndex, viewPage);
		mPageCache.remove(pageIndex);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		updatePageTranslation();
	}
	
	private void updatePageTranslation() {
		int h = getMeasuredHeight();
		if (h == 0)
			return;
		
		for (Entry<Integer, View> entry : mPageCache.entrySet()) {
			int key = entry.getKey();
			entry.getValue().setTranslationY((key - mPageIndex) * h - mPageTransOffset); 
		}
	}
	
	private int mPrePageIndex = 0;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		boolean result = mGestureDetector.onTouchEvent(event);
		
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_UP:
			int newIndex = Math.round(mPageIndex + mPageTransOffset / getHeight());
			newIndex = Math.max(0, Math.min(mAdapter.getCount() - 1, newIndex));
			mPageTransOffset = mPageIndex + mPageTransOffset / getHeight() - newIndex;
			mPageTransOffset *= getHeight();
			mPageIndex = newIndex;
			mScroller.springBack(0, (int) mPageTransOffset, 0, 0, 0, 0);
			invalidate();
			break;
		}
		
		return result;
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		return true;
	}

	@Override
	public boolean onFling (MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		
		if (velocityY > 0) {
			if (mPageIndex <= 0)
				return true;
			
			
		} else {
			if (mPageIndex >= mAdapter.getCount() - 1)
				return true;
		}
		
		return true;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		
		mPageTransOffset += distanceY;
		updatePageTranslation();
		
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		if (mScroller.computeScrollOffset()) {
			mPageTransOffset = mScroller.getCurrY();
			updatePageTranslation();
			invalidate();
		} else {
			iterateAdapter();
			updatePageTranslation();
		}
		
		super.onDraw(canvas);
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		return false;
	}
}
