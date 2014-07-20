package com.hienbibi.cloz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
public class CountDownActivity extends Activity {
	
	private static boolean sIsSearch;
	
	private int mCount = 2;
	private TextView mTxtCount;
	
	public static void newInstance(Activity act, boolean isSearch) {
		sIsSearch = isSearch;
		Intent intent = new Intent(act, CountDownActivity.class);
		act.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (sIsSearch)
			setContentView(R.layout.activity_count_down_search);
		else
			setContentView(R.layout.activity_count_down_create);
		
		mTxtCount = (TextView) findViewById(R.id.txtCount);
		count();
	}
	
	private void count() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mCount--;
				if (mCount < 0) {
					finish();
				} else {
					mTxtCount.setText("" + mCount);
					count();
				}
			}
		}, 1000);
	}
}
