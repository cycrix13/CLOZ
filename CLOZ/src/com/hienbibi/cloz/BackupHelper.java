package com.hienbibi.cloz;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.cycrix.util.CyUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

public class BackupHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	
	private static final String TAG = "BackupHelper";
	
	public static final int REQUEST_CODE_RESOLUTION = 1;
	
	private static BackupHelper mInstance;
	
	private GoogleApiClient mGoogleApiClient;
	private boolean mConnected;
	private Activity mAct;
	private ProgressDialog progress;
	
	public static void init(Activity act) {
		if (mInstance != null)
			return;
		
		mInstance = new BackupHelper(act);
	}
	
	private BackupHelper(Activity act) {
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(act)
			.addApi(Drive.API)
			.addScope(Drive.SCOPE_FILE)
			.addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.build();
		}
		
		mAct = act;
		progress = ProgressDialog.show(act, null, "Connecting to Google Drive...", true, false);
		mGoogleApiClient.connect();
	}
 	
	public static BackupHelper instance() {
		return mInstance;
	}
	
	public static void notifyDataChange() {
		if (mInstance != null)
			mInstance.pnotifyDataChange();
	}
	
	private void pnotifyDataChange() {
		if (!mConnected)
			return;
		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK)
            mGoogleApiClient.connect();
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
		if (!result.hasResolution()) {
			progress.dismiss();
			progress = null;
			CyUtils.showToast("Connection failed", mAct);
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), mAct, 0).show();
			return;
		}
		
		try {
			result.startResolutionForResult(mAct, REQUEST_CODE_RESOLUTION);
		} catch (SendIntentException e) {
			Log.e(TAG, "Exception while starting resolution activity", e);
			progress.dismiss();
			progress = null;
			CyUtils.showToast("Connection failed", mAct);
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		mConnected = true;
		progress.dismiss();
		progress = null;
		
		CyUtils.showToast("Connected to Google Drive", mAct);
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		
	}
}
