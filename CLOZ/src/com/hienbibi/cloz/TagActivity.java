package com.hienbibi.cloz;

import java.util.HashMap;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.cycrix.util.FontsCollection;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.app.Activity;
import android.content.Intent;

public class TagActivity extends Activity {
	
	private static Listener sListener;
	
	private Listener mListener;
	
	@ViewById(id = R.id.edtTag)	private EditText mEdtTag;
	
	public static class Listener {
		public void onComplete(HashMap<String, Object> result) {}
	}
	
	public static void newInstance(Activity act, Listener listener) {
		Intent intent = new Intent(act, TagActivity.class);
		act.startActivity(intent);
		
		sListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag);
		
		mListener = sListener;
		sListener = null;
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		FontsCollection.setFont(findViewById(android.R.id.content));
	}

	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		finish();
	}
	
	@Click(id = R.id.btnContinue)
	private void onContinueClick(View v) {
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("tag", mEdtTag.getText().toString().trim());
		mListener.onComplete(result);
		finish();
	}
}
