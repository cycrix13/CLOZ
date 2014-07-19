package com.hienbibi.cloz;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class UseInfoActivity extends Activity {
	
	private static boolean sSingleMode;
	private static Listener sListener;
	
	private boolean mSingleMode;
	private Listener mListener;
	List<Looks> lookList;
	private DatabaseHelper mHelper;
	
	@ViewById(id = R.id.textViewLooksNum2)	private TextView mTextViewLook;
	@ViewById(id = R.id.textViewImageNum2)	private TextView mTextViewImage;
	@ViewById(id = R.id.textViewPeopleNum2)	private TextView mTextViewPeople;
	@ViewById(id = R.id.textViewQueryNum2)	private TextView mTextViewQuery;
	
	public static class Listener {
	}

	public static void newInstance(Activity act, Listener listener, boolean singleMode) {
		Intent intent = new Intent(act, UseInfoActivity.class);
		act.startActivity(intent);
		sListener = listener;
		sSingleMode = singleMode;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_useinfo);
		
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
		
		mHelper = new DatabaseHelper(this);
		try {
			lookList = mHelper.getDao().queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
			finish();
		}
		
		mTextViewLook.setText(Integer.toString(lookList.size()));
		
		ArrayList<String> listContact = new ArrayList<String>();
		int numPic = 0;
		int numContact = 0;
		for (int i = 0; i < lookList.size(); i++) {
			Looks look = lookList.get(i);
			
			JSONArray jArr = null;
			try {
				jArr = new JSONArray(look.fileName);
			} catch (JSONException e) {
			}
			
			if (jArr != null)
				numPic += jArr.length();
			
			JSONArray jContacts = null;
			String contactJson = lookList.get(i).contacts;
			try {
				jContacts = new JSONArray(contactJson);
				for (int j = 0; i < jContacts.length(); j++) {
					if (!listContact.contains(jContacts.get(j))) {
						listContact.add(jContacts.get(j).toString());
					}
				}
			}
			catch  (JSONException e) {};
		}
		numContact = listContact.size();
		
		mTextViewImage.setText(Integer.toString(numPic));
		mTextViewPeople.setText(Integer.toString(numContact));
		mTextViewQuery.setText(Integer.toString(Settings.instance().numQuery));
	}
	
	@Click(id = R.id.buttonClose)
	private void onBackClick(View v) {
		finish();
	}
}
