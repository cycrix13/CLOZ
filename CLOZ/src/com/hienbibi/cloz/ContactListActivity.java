package com.hienbibi.cloz;

import java.util.ArrayList;
import java.util.List;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class ContactListActivity extends Activity {
	
	@ViewById(id = R.id.lstContact)	private ListView mLstContact;
	
	private ArrayList<String> mContactList;
	private boolean[] mCheckArr;
	private ContactListAdapter mAdapter;
	
	public static void newInstance(Activity act) {
		Intent intent = new Intent(act, ContactListActivity.class);
		act.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list);
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		mContactList = getContactList();
		mCheckArr = new boolean[mContactList.size()];
		mLstContact.setAdapter(mAdapter = new ContactListAdapter());
		mLstContact.setOnItemClickListener(mAdapter);
	}
	
	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		finish();
	}

	private ArrayList<String> getContactList() {
		ArrayList<String> result = new ArrayList<String>();
		
		ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                  String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                  String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                  result.add(name);
            }
        }
        
        return result;
	}
	
	private class ContactListAdapter extends ArrayAdapter<String> implements OnItemClickListener {

		public ContactListAdapter() {
			super(ContactListActivity.this, R.layout.contact_item, R.id.txtName, mContactList);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = super.getView(position, convertView, parent);
			
			View imgCheck = convertView.findViewById(R.id.imgCheck);
			imgCheck.setVisibility(mCheckArr[position] ? View.VISIBLE : View.INVISIBLE);
			
			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			mCheckArr[pos] = !mCheckArr[pos];
			notifyDataSetChanged();
		}
	}
}
