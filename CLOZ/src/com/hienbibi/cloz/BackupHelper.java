package com.hienbibi.cloz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.cycrix.util.CyUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;

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
	
		sync();
	}
	
	private void sync() {
		new SyncTask().execute();
	}
	
	private class SyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			
			DriveId appFolderId = Drive.DriveApi.getAppFolder(mGoogleApiClient).getDriveId();
			
			MetadataBufferResult result = Drive.DriveApi.getFolder(mGoogleApiClient, appFolderId)
					.queryChildren(mGoogleApiClient, null).await();
			
			if (!result.getStatus().isSuccess()) {
				CyUtils.showToast("Problem backup", mAct);
				return null;
			}

			MetadataBuffer metadataBuffer = result.getMetadataBuffer();
			Metadata dbMeta = findDatabaseMetadata(metadataBuffer);
			File dbFile = mAct.getDatabasePath(DatabaseHelper.DATABASE_NAME);
			if (dbMeta == null || modifyDataNeedUpdate(dbMeta, dbFile)) {
				if (dbMeta != null)
					uploadExistedFile(dbFile, dbMeta.getDriveId());
				else
					uploadNewFile(dbFile);
			}

			return null;
		}
		
		private Metadata findDatabaseMetadata(MetadataBuffer buffer) {
			for (int i = 0; i < buffer.getCount(); i++) {
				if (buffer.get(i).getTitle().equals(DatabaseHelper.DATABASE_NAME))
					return buffer.get(i);
			}
			
			return null;
		}
		
		private boolean modifyDataNeedUpdate(Metadata meta, File local) {
			Date driveDate = meta.getModifiedDate();
			return local.lastModified() > driveDate.getTime();
		}
		
		private void uploadExistedFile(File file, DriveId driveId) {
			DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, driveId);
			writeFileToDrive(driveFile, file);
		}
		
		private void uploadNewFile(final File file) {
			ContentsResult result = Drive.DriveApi.newContents(mGoogleApiClient).await();
			if (!result.getStatus().isSuccess()) {
				CyUtils.showToast("Problem backup", mAct);
				return;
			}

			MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
			.setTitle(file.getName())
			.setMimeType("application/octet-stream")
			.build();

			DriveFileResult fileResult = Drive.DriveApi.getAppFolder(mGoogleApiClient)
					.createFile(mGoogleApiClient, changeSet, result.getContents()).await();
			if (!fileResult.getStatus().isSuccess()) {
				CyUtils.showToast("Problem backup", mAct);
				return;
			}

			DriveFile driveFile = fileResult.getDriveFile();
			writeFileToDrive(driveFile, file);
		}
	
		private void writeFileToDrive(DriveFile driveFile, File file) {
			ContentsResult contentsResult = driveFile
					.openContents(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).await();
			
			if (!contentsResult.getStatus().isSuccess()) {
				CyUtils.showToast("Problem backup", mAct);
                return;
            }
			
			OutputStream outputStream = contentsResult.getContents().getOutputStream();
			
			try {
				FileInputStream fileInStream = new FileInputStream(file);
				byte[] byteBuffer = new byte[1024];
				int hasRead;
				while ((hasRead = fileInStream.read(byteBuffer)) > 0) {
					outputStream.write(byteBuffer, 0, hasRead);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			com.google.android.gms.common.api.Status status = driveFile.commitAndCloseContents(
                    mGoogleApiClient, contentsResult.getContents()).await();
			
			if (!status.getStatus().isSuccess()) {
				CyUtils.showToast("Problem backup", mAct);
			} else {
				CyUtils.showToast("Backup file...", mAct);
			}
		}
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
		pnotifyDataChange();
		CyUtils.showToast("Connected to Google Drive", mAct);
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		
	}
}
