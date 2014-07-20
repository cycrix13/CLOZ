package com.hienbibi.cloz;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

public class BackupHelper implements
GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener {
	
	private static final String TAG = "BackupHelper";
	
	private static BackupHelper mInstance;
	
	private GoogleApiClient mGoogleApiClient;
	private boolean mConnected;
	
	public static void init(Activity ctx) {
		if (mInstance != null)
			return;
		
		mInstance = new BackupHelper(ctx);
	}
	
	private BackupHelper(Activity ctx) {
		if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
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

	@Override
	public void onConnectionFailed(ConnectionResult result) {
//		 Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
//	        if (!result.hasResolution()) {
//	            // show the localized error dialog.
//	            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
//	            return;
//	        }
//	        try {
//	            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
//	        } catch (SendIntentException e) {
//	            Log.e(TAG, "Exception while starting resolution activity", e);
//	        }
	}

	@Override
	public void onConnected(Bundle arg0) {
		mConnected = true;
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		
	}
}
