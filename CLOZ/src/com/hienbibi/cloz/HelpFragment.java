package com.hienbibi.cloz;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.cycrix.util.FontsCollection;
import com.cycrix.util.PagerIndicator;

public class HelpFragment extends Fragment {

	@ViewById(id = R.id.pager)			private ViewPager mPager;
	@ViewById(id = R.id.pagerIndicator)	private PagerIndicator mIndicator;

	private Listener mListener = new Listener();

	public void setListener(Listener listener) {
		mListener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.help_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		try {
			AndroidAnnotationParser.parse(this, view);
		} catch (Exception e) {
			e.printStackTrace();
			getActivity().finish();
			return;
		}

		mPager.setAdapter(new HelpPageAdapter(mPager));

		mIndicator.setPager(mPager);

		mIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				mListener.onDrag(position + positionOffset);
			}

			public void onPageSelected(int arg0) {}
			public void onPageScrollStateChanged(int arg0) {}
		});
	}

	static class Listener {
		void onDrag(float offset) {}
		void onCloseClick() {}
	}

	@Click(id = R.id.imgClose)
	public void onCloseClick(View v) {
		mListener.onCloseClick();
	}

	private class HelpPageAdapter extends PagerAdapter {

		private ArrayList<ViewGroup> mViewList = new ArrayList<ViewGroup>();

		public HelpPageAdapter(ViewGroup container) {

			final int[] LAYOUT = new int[] {
					R.layout.help_page1_fragment,
					R.layout.help_page2_fragment,
					R.layout.help_page3_fragment
			};

			LayoutInflater inflater = getActivity().getLayoutInflater();
			for (int i = 0; i < 3; i++)
				mViewList.add((ViewGroup) inflater.inflate(LAYOUT[i], container, false));
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			ViewGroup view = mViewList.get(position);
			FontsCollection.setFont(view);
			container.addView(view);
			return view;
		}

		@Override
		public int getCount() {
			return mViewList.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return (view == object);
		}

		@Override
		public int getItemPosition(Object object) {
			int index = mViewList.indexOf(object);
			if (index < 0)
				return PagerAdapter.POSITION_NONE;
			else
				return index;
		}
	}
}

//class HelpPageFragment extends Fragment {
//	
//	private int mIndex;
//	private static final int[] LAYOUT = new int[] {
//		R.layout.help_page1_fragment,
//		R.layout.help_page2_fragment,
//		R.layout.help_page3_fragment
//	};
//	
//	public HelpPageFragment(int index) {
//		mIndex = index;
//	}
//	
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		return inflater.inflate(LAYOUT[mIndex], container, false);
//	}
//	
//	@Override
//	public void onViewCreated(View view, Bundle savedInstanceState) {
//		super.onViewCreated(view, savedInstanceState);
//		
//		FontsCollection.setFont(view);
//	}
//}
