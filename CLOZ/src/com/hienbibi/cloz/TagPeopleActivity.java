package com.hienbibi.cloz;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.flurry.android.FlurryAgent;

public class TagPeopleActivity extends Activity {

	private static DatabaseHelper sHelper;
	private static Listener sListener;
	private static ArrayList<String> sTagList;
	
	private DatabaseHelper mHelper; 
	private Listener mListener;
	private ArrayList<String> mTagList;
	
	private ArrayList<TItem> mItemList = new ArrayList<TItem>();
	private ArrayList<TItem> mFilterItemList;
	private boolean mCheckDate;
	private ItemAdapter mAdapter;
	
	
	@ViewById(id = R.id.lst)		private ListView mLst;
	
	public static class TItem {
		public String text;
		public boolean check;
		public String toString() { return text; }
	}
	
	public static class Listener {
		public void onComplete(ArrayList<String> result, boolean haveDate) {}
	}
	
	public static void newInstance(Activity act, DatabaseHelper helper, ArrayList<String> tagList, Listener listener) {
		sHelper = helper;
		sListener = listener;
		sTagList = tagList;
		Intent intent = new Intent(act, TagPeopleActivity.class);
		act.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag_people);
		
		mHelper = sHelper;
		sHelper = null;
		mTagList = sTagList;
		
		mListener = sListener;
		sListener = null;
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
			mItemList = getItemList();
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		mFilterItemList = new ArrayList<TItem>(mItemList);
		mLst.setAdapter(mAdapter = new ItemAdapter());
		mLst.setOnItemClickListener(mAdapter);
	}

	private ArrayList<TItem> getItemList() {
		
		ArrayList<TItem> result = new ArrayList<TItem>();
		
		for (String tag : mTagList) {
			TItem item = new TItem();
			item.check = false;
			item.text = tag;
			result.add(item);
		}
		
		return result;
	}
	
//	private void getContact() throws Exception {
//		
//		for (Looks look : lookList) {
//			JSONArray jContact = new JSONArray(look.contacts);
//			
//			helperShit(mContactList, jContact);
//		}
//		
//		for (String str : mContactList) {
//			TItem item = new TItem();
//			item.check = false;
//			item.text = str;
//			mItemList.add(item);
//		}
//	}
//	
//	private void helperShit(List<String> container, JSONArray jArr) throws JSONException {
//		for (int i = 0; i < jArr.length(); i++) {
//			String str = jArr.getString(i);
//			if (!container.contains(str)) {
//				container.add(str);
//			}
//		}
//	}
	
	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		finish();
	}
	
	@Click(id = R.id.btnContinue)
	private void onContinueClick(View v) {
		
		ArrayList<String> result = new ArrayList<String>();
		for (TItem item : mItemList)
			if (item.check)
				result.add(item.text);
		mListener.onComplete(result, mCheckDate);
		finish();
	}
	
	private class ItemAdapter extends BaseAdapter implements OnItemClickListener {
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				if (getItemViewType(position) == 0)
					convertView = getLayoutInflater().inflate(R.layout.tag_people_item, parent, false);
				else
					convertView = getLayoutInflater().inflate(R.layout.tag_people_date, parent, false);
			}
			
			TextView txt = (TextView) convertView.findViewById(R.id.txtName);
			ImageView img = (ImageView) convertView.findViewById(R.id.imgCheck);
			if (getItemViewType(position) == 0) {
				TItem item = getItem(position);

				txt.setText(item.text);
				if (item.check)
					img.setImageResource(R.drawable.tagpeople_check);
				else
					img.setImageBitmap(null);
			} else {
				txt.setText(R.string.tagpeople_text3);
				if (mCheckDate)
					img.setImageResource(R.drawable.tagpeople_check);
				else
					img.setImageBitmap(null);
			}
			
			return convertView;
		}
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public int getItemViewType(int position) {
			if (position < mFilterItemList.size())
				return 0;
			return 1;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
			if (getItemViewType(pos) == 0) {
				TItem item = getItem(pos);
				item.check = !item.check;
			} else {
				mCheckDate = !mCheckDate;
			}
			
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mFilterItemList.size() + 1;
		}

		@Override
		public TItem getItem(int pos) {
			return mFilterItemList.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}
	}
	
	private ArrayList<TItem> getContactList() {
		ArrayList<TItem> result = new ArrayList<TItem>();
		
		ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
            	String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                TItem item = new TItem();
                item.check = false;
                item.text = name;
                result.add(item);
            }
        }
        
        return result;
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
