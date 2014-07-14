package com.hienbibi.cloz;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class ContactListActivity extends Activity {
	
	private static class ContactItem {
		public String name;
		public boolean check;
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public static class Listener {
		public void onComplete(HashMap<String, Object> result) {}
	}
	
	@ViewById(id = R.id.lstContact)	private ListView mLstContact;
	@ViewById(id = R.id.edtSearch)	private EditText mEdtSearch;
	
	private static boolean sSingleMode;
	private static Listener sListener;
	
	private boolean mSingleMode;
	private Listener mListener;
	
	private ArrayList<ContactItem> mContactList;
	private ArrayList<ContactItem> mFilterContactList;
	private ContactListAdapter mAdapter;
	
	
	public static void newInstance(Activity act, Listener listener, boolean singleMode) {
		Intent intent = new Intent(act, ContactListActivity.class);
		act.startActivity(intent);
		sListener = listener;
		sSingleMode = singleMode;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list);
		
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
		
		mContactList = getContactList();
		mFilterContactList = (ArrayList<ContactItem>) mContactList.clone();
		mLstContact.setAdapter(mAdapter = new ContactListAdapter());
		mLstContact.setOnItemClickListener(mAdapter);
		
		mEdtSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence searchStr, int arg1, int arg2, int arg3) {
				mFilterContactList.clear();
				for (ContactItem contact : mContactList)
					if (contact.name.contains(searchStr))
						mFilterContactList.add(contact);
					
				mAdapter.notifyDataSetChanged();
			}
			
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			public void afterTextChanged(Editable arg0) {}
		});
	}
	
	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		finish();
	}
	
	@Click(id = R.id.btnContinue)
	private void onContinueClick(View v) {
		
		if (getCheckContacts().size() == 0) {
			new AlertDialog.Builder(this).setMessage(R.string.contact_noselect)
					.setPositiveButton(R.string.text_ok, null).create().show();
			return;
		}
		
		if (mSingleMode) {
			HashMap<String, Object> result = new HashMap<String, Object>();
			result.put("contacts", getCheckContacts());
			mListener.onComplete(result);
			finish();
		} else {
			DateActivity.newInstance(this, new DateActivity.Listener() {
				@Override
				public void onComplete(HashMap<String, Object> result) {
					result.put("contacts", getCheckContacts());
					mListener.onComplete(result);
					finish();
				}
			}, false);
		}
	}
	
	private ArrayList<String> getCheckContacts() {
		ArrayList<String> result = new ArrayList<String>();
		
		for (ContactItem contact : mContactList)
			if (contact.check)
				result.add(contact.name);
		
		return result;
	}

	private ArrayList<ContactItem> getContactList() {
		ArrayList<ContactItem> result = new ArrayList<ContactItem>();
		
		ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
//                  String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                  String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                  ContactItem item = new ContactItem();
                  item.name = name;
                  item.check = false;
                  result.add(item);
            }
        }
        
        return result;
	}
	
	private class ContactListAdapter extends ArrayAdapter<ContactItem> implements OnItemClickListener {

		public ContactListAdapter() {
			super(ContactListActivity.this, R.layout.contact_item, R.id.txtName, mFilterContactList);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = super.getView(position, convertView, parent);
			
			View imgCheck = convertView.findViewById(R.id.imgCheck);
			imgCheck.setVisibility(getItem(position).check ? View.VISIBLE : View.INVISIBLE);
			
			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			ContactItem item = getItem(pos);
			item.check = !item.check;
			notifyDataSetChanged();
		}
	}
}
