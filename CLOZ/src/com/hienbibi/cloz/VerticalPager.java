package com.hienbibi.cloz;

import java.util.HashMap;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
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

public class VerticalPager extends FrameLayout implements OnGestureListener {
	
	private PagerAdapter mAdapter;
	private int mPageIndex = 0;
	private int mPageCacheNum = 1;
	private HashMap<Integer, View> mPageCache;
	private float mPageTransOffset = 0;
	private GestureDetector mGestureDetector;
	private OverScroller mScroller;
	private Listener mListener = new Listener();
	
	public static class Listener {
		public void onPageChange(VerticalPager pager) {}
	}

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
		
		if (adapter == null || adapter.getCount() == 0)
			setVisibility(View.INVISIBLE);
		else
			setVisibility(View.VISIBLE);
		
		mPageIndex = pageIndex;
		mAdapter = adapter;
		
		iterateAdapter();
	}
	
	public void setListener(Listener listener) {
		mListener = listener;
	}
	
	public int getCurrentPageIndex() {
		return mPageIndex;
	}
	
	public View getCurrentPage() {
		
		if (mPageIndex < 0)
			return null;
		else
			return mPageCache.get(mPageIndex);
	}
	
	private void iterateAdapter() {
		
		if (mPageIndex < 0) {
			mListener.onPageChange(this);
			return;
		}
		
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
		
		mListener.onPageChange(this);
	}
	
	private void loadPage(int pageIndex) {
		
		if (mAdapter == null || pageIndex < 0 || pageIndex > mAdapter.getCount() - 1)
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
		
		if (mAdapter == null || pageIndex < 0 || pageIndex > mAdapter.getCount() - 1)
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
		
		if (mPageIndex < 0)
			return;
		
		int h = getMeasuredHeight();
		if (h == 0)
			return;
		
		for (Entry<Integer, View> entry : mPageCache.entrySet()) {
			int key = entry.getKey();
			entry.getValue().setTranslationY((key - mPageIndex) * h - mPageTransOffset); 
		}
	}
	
	private PointF mBegin = new PointF();
	private boolean mComplete = false;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		
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
					return false;
				
				float dx = event.getX() - mBegin.x;
				float dy = event.getY() - mBegin.y;
				float r = (float) Math.sqrt(dx*dx + dy*dy);
				if (r > touchSlop) {
					if (Math.abs(dy) > Math.abs(dx))
						return true;
					mComplete = true;
				}
			}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mComplete = false;
				break;
			}
		} catch (Exception e) {
		}

		return false;
	}
	
	private int mPrePageIndex = 0;
	private float mLastDistance = 0;
	private boolean mTouchComplete = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mAdapter == null)
			return false;
		
		if (mPageIndex < 0)
			return false;
		
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_UP:
			
			int newIndex = Math.round(mPageIndex + mPageTransOffset / getHeight());
			newIndex = Math.max(0, Math.min(mAdapter.getCount() - 1, newIndex));
			mPageTransOffset = mPageIndex + mPageTransOffset / getHeight() - newIndex;
			mPageTransOffset *= getHeight();
			mPrePageIndex = mPageIndex;
			mPageIndex = newIndex;
			if (mPageIndex != mPrePageIndex)
				iterateAdapter();
			mScroller.springBack(0, (int) mPageTransOffset, 0, 0, 0, 0);
			invalidate();
			mTouchComplete = true;
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (mTouchComplete) {
				mTouchComplete = false;
				MotionEvent newEvent = MotionEvent.obtain(event);
				newEvent.setAction(event.getActionIndex() | MotionEvent.ACTION_DOWN);
				newEvent.setLocation(mBegin.x, mBegin.y);
				mGestureDetector.onTouchEvent(newEvent);
			}
			break;
		}
		
		MotionEvent newEvent = MotionEvent.obtain(event);
		boolean result = mGestureDetector.onTouchEvent(event);
		
		return result;
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		return true;
	}

	@Override
	public boolean onFling (MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (mAdapter == null)
			return false;
		
		if (mPageIndex < 0)
			return false;
		
		if (velocityY > 0) {
			if (mPageIndex <= 0)
				return true;
			
			if (mPrePageIndex <= mPageIndex) {
				mPageIndex--;
				iterateAdapter();
				mPageTransOffset += getHeight();
				mScroller.abortAnimation();
				mScroller.springBack(0, (int) mPageTransOffset, 0, 0, 0, 0);
				invalidate();
			}
		} else {
			if (mPageIndex >= mAdapter.getCount() - 1)
				return true;
			
			if (mPrePageIndex >= mPageIndex) {
				mPageIndex++;
				iterateAdapter();
				mPageTransOffset -= getHeight();
				mScroller.abortAnimation();
				mScroller.springBack(0, (int) mPageTransOffset, 0, 0, 0, 0);
				invalidate();
			}
		}
		
		return true;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		mLastDistance = distanceY;
		Log.d("Message", "" + distanceY);
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
