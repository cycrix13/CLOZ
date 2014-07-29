package com.example.zoomview;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private ViewPager mPager;
	private ZoomView mViewZoom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mPager = (ViewPager) findViewById(R.id.pager);
		
		ArrayList<String> pathList = new ArrayList<String>();
		for (int i = 0 ; i < 4; i++)
			pathList.add("");
		
//		mPager.setAdapter(new TestAdapter(getSupportFragmentManager()));
		LookAdapter adapter;
		mPager.setAdapter(adapter = new LookAdapter(mPager, pathList));
		mPager.setOnPageChangeListener(adapter);
//		mPager.setOffscreenPageLimit(10);
		
//		mViewZoom = (ZoomView) findViewById(R.id.zoomView);
	}
	
	private class LookAdapter extends PagerAdapter implements OnPageChangeListener {

		private ArrayList<String> mPathList;

		public LookAdapter(ViewGroup container, ArrayList<String> pathList) {
			
			mPathList = pathList;
		}
		
		public ViewGroup fakeInstantiateItem(ViewGroup container, int position) {
			
			ViewGroup v = (ViewGroup) getLayoutInflater()
					.inflate(R.layout.page_item, container, false);
			
			return v;
//			return null;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			ViewGroup v = null;
			
			int fakePos = position;
			if (mPathList.size() > 1) {
				if (position == 0)
					fakePos = mPathList.size() - 1;
				else if (position == mPathList.size() + 1)
					fakePos = 0;
				else
					fakePos = position - 1;
			}
				
			v = fakeInstantiateItem(container, fakePos);
			
			return v;
		}
		
		@Override
		public int fakeGetCount() {
			
			int i = mPathList.size() + (mPathList.size() > 1 ? 2 : 0);
			return i
		}

		@Override
		public int getCount() {
			return fakeGetCount() + (fakeGetCount() > 1 ? 2 : 0);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			
			if (mPathList.size() > 1) {
				if (position == 0 || position == mPathList.size()) {
					
				} else if (position == 1 || position == mPathList.size() + 1) {
					
				} else {
					container.removeView((View) object);
				}
				
			} else {
				container.removeView((View) object);
			}
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return (view == object);
		}

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_UNCHANGED;
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			Log.d("cycrix", "onPageScrolled " + position + positionOffset);
			
			if (position + positionOffset == mPathList.size() + 1) {
				mPager.setCurrentItem(1, false);
			} else if (position + positionOffset == 0) {
				mPager.setCurrentItem(mPathList.size(), false);
			}
		}

		@Override
		public void onPageSelected(int position) {
			Log.d("cycrix", "onPageSelected " + position);
		}
	}
}

// to hien thanh, big c, sang tao tre.