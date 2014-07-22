package com.hienbibi.cloz;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class InAppActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_in_app);
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		
	}

	@Click(id = R.id.btnClose)
	private void onCloseClick(View v) {
		finish();
	}
}
