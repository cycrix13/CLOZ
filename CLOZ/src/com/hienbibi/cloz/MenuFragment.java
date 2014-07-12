package com.hienbibi.cloz;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class MenuFragment extends Fragment {
	
	@ViewById(id = R.id.txtMenu)		private TextView mTxtMenu;
	@ViewById(id = R.id.imgMenu)		private ImageView mImgMenu;
	@ViewById(id = R.id.layoutMenu) 	private ViewGroup mLayoutMenu;
	
	@ViewById(id = R.id.layoutCamera)	private View mLayoutCamera;
	@ViewById(id = R.id.layoutMore1)	private View mLayoutMore1;
	@ViewById(id = R.id.layoutMore2)	private View mLayoutMore2;
	
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
	}
	
	@Click(id = R.id.txtMenu)
	void onMenuClick(View v) {
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
	
	@Click(id = R.id.imgNewLook)
	void onNewLookClick(View v) {
		
		mLayoutCamera.setVisibility(View.VISIBLE);
		mLayoutMore1.setVisibility(View.INVISIBLE);
		mLayoutMore2.setVisibility(View.INVISIBLE);
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(mLayoutMenu, "translationY", screenWidth / 3);
		animator1.setInterpolator(new AccelerateDecelerateInterpolator());
		animator1.start();
	}
	
	@Click(id = R.id.imgMore)
	void onMoreClick(View v) {
		mLayoutCamera.setVisibility(View.INVISIBLE);
		mLayoutMore1.setVisibility(View.VISIBLE);
		mLayoutMore2.setVisibility(View.VISIBLE);
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(mLayoutMenu, "translationY", 0);
		animator1.setInterpolator(new AccelerateDecelerateInterpolator());
		animator1.start();
	}
	
}
