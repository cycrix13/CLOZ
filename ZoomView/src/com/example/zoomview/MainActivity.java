package com.example.zoomview;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
	
	private ViewPager mPager;
	private ZoomView mViewZoom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mPager = (ViewPager) findViewById(R.id.pager);
		
		ArrayList<String> pathList = new ArrayList<String>();
		for (int i = 0 ; i < 10; i++)
			pathList.add("");
		
//		mPager.setAdapter(new TestAdapter(getSupportFragmentManager()));
		mPager.setAdapter(new LookAdapter(mPager, pathList));
		
//		mViewZoom = (ZoomView) findViewById(R.id.zoomView);
	}
	
//	private class TestAdapter extends FragmentPagerAdapter {
//
//		public TestAdapter(FragmentManager fm) {
//			super(fm);
//			// TODO Auto-generated constructor stub
//		}
//
//		@Override
//		public Fragment getItem(int arg0) {
//			
//			return new PageFrag();
//		}
//
//		@Override
//		public int getCount() {
//			// TODO Auto-generated method stub
//			return 10;
//		}
//
//		
//		
//	}
//	
//	private class PageFrag extends Fragment {
//		@Override
//		public View onCreateView(LayoutInflater inflater,
//				@Nullable ViewGroup container,
//				@Nullable Bundle savedInstanceState) {
//			View v = inflater.inflate(R.layout.page_item, container, false);
//			
//			return v;
//		}
//	}
	
	private class LookAdapter extends PagerAdapter {

		private ArrayList<String> mPathList;

		public LookAdapter(ViewGroup container, ArrayList<String> pathList) {
			
			mPathList = pathList;
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {

			ZoomView v = new ZoomView(MainActivity.this);
			LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			v.setLayoutParams(param);
			
			v.setImage("/sdcard/DCIM/Camera/20140727_012201.jpg");
//			v.setImage("/sdcard/DCIM/24850.jpg");
			container.addView(v);
			
			return v;
		}

		@Override
		public int getCount() {
			return Math.max(mPathList.size(), 1);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return (view == object);
		}

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_UNCHANGED;
		}
	}
}

// to hien thanh, big c, sang tao tre.