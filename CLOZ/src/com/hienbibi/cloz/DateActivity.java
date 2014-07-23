package com.hienbibi.cloz;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.flurry.android.FlurryAgent;

public class DateActivity extends Activity {
	
	private static boolean sSingleMode;
	private static Listener sListener;
	
	private boolean mSingleMode;
	private Listener mListener;
	
	@ViewById(id = R.id.datePicker)	private DatePicker mDatePicker;
	
	public static class Listener {
		public void onComplete(HashMap<String, Object> result) {}
	}

	public static void newInstance(Activity act, Listener listener, boolean singleMode) {
		Intent intent = new Intent(act, DateActivity.class);
		act.startActivity(intent);
		sListener = listener;
		sSingleMode = singleMode;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_date);
		
		mSingleMode = sSingleMode;
		mListener = sListener;
		sListener = null;
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
	}
	
	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		finish();
	}
	
	@Click(id = R.id.btnContinue)
	private void onContinueClick(View v) {
		FlurryAgent.logEvent("PRESS_CONTINUE");
		if (mSingleMode) {
			HashMap<String, Object> result = new HashMap<String, Object>();
			int[] date = new int[] {mDatePicker.getDayOfMonth(), mDatePicker.getMonth() + 1, mDatePicker.getYear()};
			result.put("date", date);
			mListener.onComplete(result);
			finish();
		} else {
			TagActivity.newInstance(this, new TagActivity.Listener() {
				@Override
				public void onComplete(HashMap<String, Object> result) {
					int[] date = new int[] {mDatePicker.getDayOfMonth(), mDatePicker.getMonth() + 1, mDatePicker.getYear()};
					result.put("date", date);
					mListener.onComplete(result);
					finish();
				}
			});
		}
		
		
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
