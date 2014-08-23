package com.jorgebs.cloz;

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
import android.widget.Toast;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.cycrix.util.FontsCollection;
import com.flurry.android.FlurryAgent;

public class SuggestActivity extends Activity {
	
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
		Intent intent = new Intent(act, SuggestActivity.class);
		act.startActivity(intent);
		sListener = listener;
		sSingleMode = singleMode;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suggest);
		
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
		
		FontsCollection.setFont(findViewById(android.R.id.content));
	}
	
	@Click(id = R.id.btcSend)
	private void onSendClick(View v) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"clozappandroid@gmail.com"});
		try {
		    startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	@Click(id = R.id.buttonClose)
	private void onBackClick(View v) {
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
