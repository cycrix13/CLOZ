package com.hienbibi.cloz;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.cycrix.util.FontsCollection;
import com.flurry.android.FlurryAgent;

public class TagActivity extends Activity {
	
	private static Listener sListener;
	
	private Listener mListener;
	
	@ViewById(id = R.id.edtTag)		private EditText mEdtTag;
	@ViewById(id = R.id.lstTag)		private ListView mLstTag;
	
	
	private ArrayAdapter<String> mAdapter;
	
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
		
		mEdtTag.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().contains(" ")) {
					String[] tagArr = s.toString().split(" ");
					mAdapter.addAll(tagArr);
					s.clear();
				}
			}
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		});
		
		mLstTag.setAdapter(mAdapter = new ArrayAdapter<String>(this, R.layout.tag_item, R.id.txtTag, new ArrayList<String>()));
		mLstTag.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				mAdapter.remove(mAdapter.getItem(pos));
			}
		});
	}

	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		finish();
	}
	
	@Click(id = R.id.btnContinue)
	private void onContinueClick(View v) {
		
		FlurryAgent.logEvent("PRESS_TAG_CONTINUE");
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		ArrayList<String> tagList = new ArrayList<String>();
		for (int i = 0; i < mAdapter.getCount(); i++) {
			tagList.add(mAdapter.getItem(i));
		}
		{
			String t = mEdtTag.getText().toString().trim();
			if (t.length() > 0)
				tagList.add(t);
		}
		result.put("tag", tagList);
		mListener.onComplete(result);
		finish();
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
