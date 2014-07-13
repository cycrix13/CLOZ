package com.hienbibi.cloz;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.cycrix.util.FontsCollection;

public class MainActivity extends FragmentActivity implements MenuFragment.Listener {

	private MenuFragment mMenuFragment;
	private HelpFragment mHelpFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//      Locale locale = new Locale("es", "ES");
//	    Locale.setDefault(locale);
//	    Configuration config = new Configuration();
//	    config.locale = locale;
//	    getBaseContext().getResources().updateConfiguration(config,
//	    getBaseContext().getResources().getDisplayMetrics());

		FontsCollection.init(this);

		setContentView(R.layout.activity_main);

		FlashActivity.newInstance(this);

		mMenuFragment = (MenuFragment) getSupportFragmentManager().findFragmentById(R.id.fragMenu);
		mHelpFragment = new HelpFragment();
		getSupportFragmentManager().beginTransaction().add(R.id.layoutHolder, mHelpFragment).commit();

		mHelpFragment.setListener(new HelpFragment.Listener() {
			@Override
			void onDrag(float offset) {
				mMenuFragment.getView().setVisibility(offset > 1.5 ? View.VISIBLE : View.INVISIBLE);
			}

			@Override
			void onCloseClick() {
				getSupportFragmentManager().beginTransaction().remove(mHelpFragment).commit();
				mMenuFragment.getView().setVisibility(View.VISIBLE);
			}
		});
		
		mMenuFragment.setListerner(this);
	}

	@Override
	public void onCameraClick() {
		CameraActivity.newInstance(this, new CameraActivity.Listener() {
		}, CameraActivity.MODE_CAMERA);
	}

	@Override
	public void onGalleryClick() {
		CameraActivity.newInstance(this, new CameraActivity.Listener() {
		}, CameraActivity.MODE_FILE);
	}
}
