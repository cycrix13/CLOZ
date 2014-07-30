package com.hienbibi.cloz;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.flurry.android.FlurryAgent;

public class SearchActivity extends Activity {
	
	private static DatabaseHelper sHelper;
	private static Listener sListener;
	
	private DatabaseHelper mHelper; 
	private Listener mListener;
	
	private ArrayList<String> mTagList = new ArrayList<String>();
	private ArrayList<String> mContactList = new ArrayList<String>();
	private ArrayList<TItem> mItemList = new ArrayList<SearchActivity.TItem>();
	private ArrayList<TItem> mFilterItemList;
	private ItemAdapter mAdapter;
	
	@ViewById(id = R.id.lst)		private ListView mLst;
	@ViewById(id = R.id.edtSearch)	private EditText mEdtSearch;
	
	public static class TItem {
		public String text;
		public boolean check;
		public boolean isContact;
		public String toString() { return text; }
	}
	
	public static class Listener {
		public void onComplete(ArrayList<TItem> result) {}
	}
	
	public static void newInstance(Activity act, DatabaseHelper helper, Listener listener) {
		sHelper = helper;
		sListener = listener;
		Intent intent = new Intent(act, SearchActivity.class);
		act.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		mHelper = sHelper;
		sHelper = null;
		
		mListener = sListener;
		sListener = null;
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
			getTagContact();
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		mFilterItemList = new ArrayList<SearchActivity.TItem>(mItemList);
		mLst.setAdapter(mAdapter = new ItemAdapter(mFilterItemList));
		mLst.setOnItemClickListener(mAdapter);
		
		mEdtSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
				mFilterItemList.clear();
				String searchStr = s.toString().toLowerCase();
				for (TItem item : mItemList)
					if (item.text.toLowerCase().contains(searchStr))
						mFilterItemList.add(item);
					
				mAdapter.notifyDataSetChanged();
			}
			
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			public void afterTextChanged(Editable arg0) {}
		});
	}
	
	private void getTagContact() throws Exception {
		
		List<Looks> lookList = mHelper.getDao().queryForAll();
		for (Looks look : lookList) {
			JSONArray jTag = new JSONArray(look.tags);
			JSONArray jContact = new JSONArray(look.contacts);
			
			helperShit(mTagList, jTag);
			helperShit(mContactList, jContact);
		}
		
		for (String str : mTagList) {
			TItem item = new TItem();
			item.check = false;
			item.isContact = false;
			item.text = str;
			mItemList.add(item);
		}
		
		for (String str : mContactList) {
			TItem item = new TItem();
			item.check = false;
			item.isContact = true;
			item.text = str;
			mItemList.add(item);
		}
	}
	
	private void helperShit(List<String> container, JSONArray jArr) throws JSONException {
		for (int i = 0; i < jArr.length(); i++) {
			String str = jArr.getString(i);
			if (!container.contains(str)) {
				container.add(str);
			}
		}
	}
	
	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		finish();
	}
	
	@Click(id = R.id.btnContinue)
	private void onContinueClick(View v) {
		
		FlurryAgent.logEvent("PRESS_SEARCH_FIND_A_LOOK");
		
		ArrayList<TItem> result = new ArrayList<SearchActivity.TItem>();
		for (TItem item : mItemList)
			if (item.check)
				result.add(item);
		Settings.instance().numQuery += 1;
		Settings.instance().save();
		mListener.onComplete(result);
		finish();
	}
	
	private class ItemAdapter extends ArrayAdapter<TItem> implements OnItemClickListener {

		public ItemAdapter(List<TItem> objects) {
			super(SearchActivity.this, R.layout.search_item, R.id.txtName, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = super.getView(position, convertView, parent);
			
			TItem item = getItem(position);
			TextView txt = (TextView) convertView.findViewById(R.id.txtName);
			ImageView img = (ImageView) convertView.findViewById(R.id.imgCheck);
			
			txt.setText(item.text);
			txt.setBackgroundResource(item.isContact ? R.drawable.contact_border : R.drawable.tag_border);
			img.setImageResource(item.check ? R.drawable.check_box : R.drawable.uncheck);
			
			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
			
			TItem item = getItem(pos);
			item.check = !item.check;
			notifyDataSetChanged();
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
