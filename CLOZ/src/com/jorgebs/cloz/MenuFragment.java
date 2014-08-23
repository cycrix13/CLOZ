package com.jorgebs.cloz;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.flurry.android.FlurryAgent;

public class MenuFragment extends Fragment {
	
	@ViewById(id = R.id.txtMenu)		private TextView mTxtMenu;
	@ViewById(id = R.id.imgMenu)		private ImageView mImgMenu;
	@ViewById(id = R.id.layoutMenu) 	private ViewGroup mLayoutMenu;
	
	@ViewById(id = R.id.layoutCamera)	private View mLayoutCamera;
	@ViewById(id = R.id.layoutMore1)	private View mLayoutMore1;
	@ViewById(id = R.id.layoutMore2)	private View mLayoutMore2;
	
	private Listener mListener;
	
	public void setListerner(Listener listener) {
		mListener = listener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.menu_frag, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		try {
			AndroidAnnotationParser.parse(this, view);
		} catch (Exception e) {
			getActivity().finish();
			return;
		}
		
		view.findViewById(R.id.layoutRoot).setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				
				int screenWidth = getResources().getDisplayMetrics().widthPixels;
				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_UP:
					
					ObjectAnimator animator1 = ObjectAnimator.ofFloat(mLayoutMenu, "translationY", screenWidth);
					ObjectAnimator animator2 = ObjectAnimator.ofFloat(mTxtMenu, "alpha", 0, 1);
					ObjectAnimator animator3 = ObjectAnimator.ofFloat(mImgMenu, "alpha", 1, 0);

					AnimatorSet set = new AnimatorSet();
					set.playTogether(animator1, animator2, animator3);
					set.setInterpolator(new AccelerateDecelerateInterpolator());
					set.start();
					return true;

				default:
					if (mLayoutMenu.getTranslationY() != screenWidth)
						return true;
					break;
				}
				return false;
			}
		});
	}
	
	public void close() {
		
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		mLayoutMenu.setTranslationY(screenWidth);
		ObjectAnimator animator2 = ObjectAnimator.ofFloat(mTxtMenu, "alpha", 0, 1);
		ObjectAnimator animator3 = ObjectAnimator.ofFloat(mImgMenu, "alpha", 1, 0);
		AnimatorSet set = new AnimatorSet();
		set.playTogether(animator2, animator3);
		set.setDuration(0);
		set.start();
	}
	
	public void closeAnim() {
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(mLayoutMenu, "translationY", 
				screenWidth);
		ObjectAnimator animator2 = ObjectAnimator.ofFloat(mTxtMenu, "alpha", 0, 1);
		ObjectAnimator animator3 = ObjectAnimator.ofFloat(mImgMenu, "alpha", 1, 0);

		AnimatorSet set = new AnimatorSet();
		set.playTogether(animator1, animator2, animator3);
		set.setInterpolator(new AccelerateDecelerateInterpolator());
		set.start();
	}
	
	@Click(id = R.id.txtMenu)
	void onMenuClick(View v) {
		
		 FlurryAgent.logEvent("PRESS_MENU");
		
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		if (mLayoutMenu.getTranslationY() == screenWidth) {
			
			ObjectAnimator animator1 = ObjectAnimator.ofFloat(mLayoutMenu, "translationY", 
					screenWidth * 2 / 3);
			ObjectAnimator animator2 = ObjectAnimator.ofFloat(mTxtMenu, "alpha", 1, 0);
			ObjectAnimator animator3 = ObjectAnimator.ofFloat(mImgMenu, "alpha", 0, 1);

			AnimatorSet set = new AnimatorSet();
			set.playTogether(animator1, animator2, animator3);
			set.setInterpolator(new AccelerateDecelerateInterpolator());
			set.start();
		} else {
			
			ObjectAnimator animator1 = ObjectAnimator.ofFloat(mLayoutMenu, "translationY", 
					screenWidth);
			ObjectAnimator animator2 = ObjectAnimator.ofFloat(mTxtMenu, "alpha", 0, 1);
			ObjectAnimator animator3 = ObjectAnimator.ofFloat(mImgMenu, "alpha", 1, 0);

			AnimatorSet set = new AnimatorSet();
			set.playTogether(animator1, animator2, animator3);
			set.setInterpolator(new AccelerateDecelerateInterpolator());
			set.start();
		}
	}
	
	@Click(id = R.id.layoutNewLook)
	void onNewLookClick(View v) {
		
		FlurryAgent.logEvent("PRESS_NEWLOOK");
		
		mLayoutCamera.setVisibility(View.VISIBLE);
		mLayoutMore1.setVisibility(View.INVISIBLE);
		mLayoutMore2.setVisibility(View.INVISIBLE);
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(mLayoutMenu, "translationY", screenWidth / 3);
		animator1.setInterpolator(new AccelerateDecelerateInterpolator());
		animator1.start(); 
	}
	
	@Click(id = R.id.layoutMore)
	void onMoreClick(View v) {
		FlurryAgent.logEvent("PRESS_MORE");
		mLayoutCamera.setVisibility(View.INVISIBLE);
		mLayoutMore1.setVisibility(View.VISIBLE);
		mLayoutMore2.setVisibility(View.VISIBLE);
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(mLayoutMenu, "translationY", 0);
		animator1.setInterpolator(new AccelerateDecelerateInterpolator());
		animator1.start();
	}
	
	@Click(id = R.id.layoutPickCamera)
	void onCameraClick(View v) {
		FlurryAgent.logEvent("PRESS_CAMERA");
		
		if (mListener != null) mListener.onCameraClick();
		closeAnim();
	}
	
	@Click(id = R.id.layoutGallery)
	void onGalleryClick(View v) {
		FlurryAgent.logEvent("PRESS_GALLERY");
		if (mListener != null) mListener.onGalleryClick();
		closeAnim();
	}
	
	@Click(id = R.id.layoutHelp)
	void onHelpClick(View v) {
		FlurryAgent.logEvent("PRESS_HELP");
		if (mListener != null) mListener.onHelpClick();
		closeAnim();
	}
	
	@Click(id = R.id.layoutSearch)
	void onSearchClick(View v) {
		FlurryAgent.logEvent("PRESS_SEARCH");
		if (mListener != null) mListener.onSearchClick();
		closeAnim();
	}
	
	@Click(id = R.id.layoutSync)
	void onSyncClick(View v) {
		FlurryAgent.logEvent("PRESS_BACKUP");
		if (mListener != null) mListener.onSyncClick();
		closeAnim();
	}

	@Click(id = R.id.layoutUseInfo)
	void onUseInfoClick(View v) {
		FlurryAgent.logEvent("PRESS_USEINFO");
		if (mListener != null) mListener.onUseInfoClick();
		closeAnim();
	}
	
	@Click(id = R.id.layoutSuggest)
	void onSuggestClick(View v) {
		FlurryAgent.logEvent("PRESS_SUGGEST");
		if (mListener != null) mListener.onSuggestClick();
		closeAnim();
	}
	
	@Click(id = R.id.layoutRate)
	void onRateClick(View v) {
		FlurryAgent.logEvent("PRESS_RATE");
		if (mListener != null) mListener.onRateClick();
		closeAnim();
	}
	
	@Click(id = R.id.layoutInApp)
	void onInappClick(View v) {
		FlurryAgent.logEvent("PRESS_EXTRAS");
		if (mListener != null) mListener.onInAppClick();
		closeAnim();
	}

	public interface Listener {
		void onCameraClick();
		void onGalleryClick();
		void onHelpClick();
		void onSearchClick();
		void onSyncClick();
		void onUseInfoClick();
		void onSuggestClick();
		void onRateClick();
		void onInAppClick();
	}
}
