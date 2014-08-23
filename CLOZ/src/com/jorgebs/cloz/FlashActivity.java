package com.jorgebs.cloz;

import com.flurry.android.FlurryAgent;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;

public class FlashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flash);
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		}, 1000);
	}
	
	public static void newInstance(Activity act) {
		Intent intent = new Intent(act, FlashActivity.class);
		act.startActivity(intent);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, Settings.API_FLURRY_KEY);
	}
	 
	@Override
	protected void onStop()
	{
		super.onStop();		
		FlurryAgent.onEndSession(this);
	}
}
