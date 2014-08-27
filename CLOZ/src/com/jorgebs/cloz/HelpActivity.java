package com.jorgebs.cloz;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class HelpActivity extends Activity {
	
	@ViewById(id = R.id.switch1)		private Switch mSwitch;
	
	static private Listener sListener;
	private Listener mListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		mListener = sListener;
		sListener = null;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		mSwitch.setChecked(Settings.instance().showHelp);
		
		mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				if (isChecked)
					FlurryAgent.logEvent("PRESS_ON_HELP");
				else
					FlurryAgent.logEvent("PRESS_OFF_HELP");
				Settings.instance().showHelp = isChecked;
				Settings.instance().save();
			}
		});
	}

	public static void newInstance(Activity act, Listener listener) {
		sListener = listener;
		Intent intent = new Intent(act, HelpActivity.class);
		act.startActivity(intent);
	}
	
	@Click(id = R.id.btnClose)
	private void onCloseClick(View v) {
		finish();
	}
	
	@Click(id = R.id.btnSeeSlide)
	private void onSeeSlideClick(View v) {
		FlurryAgent.logEvent("PRESS_WELCOME_SLIDE");
		mListener.onSeeSlideClick();
		finish();
	}
	
	public static class Listener {
		public void onSeeSlideClick() {}
	}
}
