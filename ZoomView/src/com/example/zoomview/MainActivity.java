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
		
		VerticalPager pager = (VerticalPager) findViewById(R.id.pager);
		pager.setAdapter(new Adapter(), 0);
	}
	
	class Adapter extends PagerAdapter {

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			
			TextView txt = new TextView(MainActivity.this);
			LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			txt.setLayoutParams(param);
			txt.setText("" + position);
			txt.setTextColor(-1);
			txt.setBackgroundColor(0xFF000000 | (0xFF << position));
			container.addView(txt);
			
			return txt;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}
}

// to hien thanh, big c, sang tao tre.