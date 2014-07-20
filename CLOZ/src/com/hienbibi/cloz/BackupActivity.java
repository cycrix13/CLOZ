package com.hienbibi.cloz;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.util.FontsCollection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class BackupActivity extends Activity {
	
	public static void newInstance(Activity act) {
		Intent intent = new Intent(act, BackupActivity.class);
		act.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
			FontsCollection.setFont(findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
	}
	
	@Click(id = R.id.txtClose)
	private void onCloseClick(View v) {
		finish();
	}
}
