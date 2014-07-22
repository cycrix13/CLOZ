package com.hienbibi.cloz;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.cycrix.util.CyUtils;
import com.cycrix.util.FontsCollection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class BackupActivity extends Activity {
	
	@ViewById(id = R.id.txtBackupTitle)		private TextView mTxtBackupTitle;
	@ViewById(id = R.id.layoutOnOff)		private View mTxtOnOff;
	
	@ViewById(id = R.id.txtOn)				private TextView mTxtOn;
	@ViewById(id = R.id.txtOff)				private TextView mTxtOff;
	
	static private DatabaseHelper sDb;
	private DatabaseHelper mDb;
	
	static private MainActivity sAct;
	private MainActivity mAct;
	
	public static void newInstance(MainActivity act) {
		sDb = act.mHelper;
		sAct = act;
		Intent intent = new Intent(act, BackupActivity.class);
		act.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);
		
		mDb = sDb;
		sDb = null;
		
		mAct = sAct;
		sAct = null;
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
			FontsCollection.setFont(findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		setOnOff();
	}
	
	private void setOnOff() {
		boolean on = Settings.instance().autoBackup;
		
		mTxtOn.setTypeface(FontsCollection.getFont(FontsCollection.fontNameArr[on ? 2 : 0]),
				on ? Typeface.BOLD : Typeface.NORMAL);
		mTxtOff.setTypeface(FontsCollection.getFont(FontsCollection.fontNameArr[on ? 0 : 2]), 
				on ? Typeface.NORMAL : Typeface.BOLD);
	}
	
	@Click(id = R.id.txtClose)
	private void onCloseClick(View v) {
		finish();
	}
	
	@Click(id = R.id.txtBackupTitle)
	private void onBackupTitleClick(View v) {
		onOnOffClick(v);
	}
	
	@Click(id = R.id.layoutOnOff)
	private void onOnOffClick(View v) {
		
		if (!Settings.instance().autoBackup)
			BackupHelper.init(this, mDb);
		
		Settings.instance().autoBackup = !Settings.instance().autoBackup;
		Settings.instance().save();
		setOnOff();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		BackupHelper.instance().onActivityResult(requestCode, resultCode, data);
	}
	
	@Click(id = R.id.txtSyncFrom)
	private void onSyncFromClick(View v) {
		
		BackupHelper.init(this, mDb);
		BackupHelper.instance().downBackupAfterConnect(new BackupHelper.DownCallBack() {
			@Override
			void onFail(Exception e) {
				CyUtils.showToast("Sync from google failed", BackupActivity.this);
			}
			
			@Override
			void onSuccess() {
				CyUtils.showToast("Sync from google completed", BackupActivity.this);
				mAct.refreshDb();
			}
		});
		
	}
}
