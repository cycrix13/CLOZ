package com.jorgebs.cloz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

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
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
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
	private DatabaseHelper mDb;
	private DownCallBack mDownBackup;
	
	public static void init(Activity act, DatabaseHelper db) {
		if (mInstance != null)
			return;
		
		mInstance = new BackupHelper(act, db);
	}
	
	private BackupHelper(Activity act, DatabaseHelper db) {
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
		mDb = db;
		progress = ProgressDialog.show(act, null, "Connecting to Google Drive...", true, false);
		mGoogleApiClient.connect();
	}
	
	public void downBackupAfterConnect(DownCallBack callback) {
		
		if (!mConnected)
			mDownBackup = callback;
		else
			mInstance.dowṇ̣(callback);
	}
 	
	public static BackupHelper instance() {
		return mInstance;
	}
	
	public static void notifyDataChange() {
		if (mInstance != null)
			mInstance.pnotifyDataChange();
	}
	
//	public static void downBackup(DownCallBack callback) {
//		
//		if (mInstance != null)
//			mInstance.dowṇ̣(callback);
//	}
	
	private void pnotifyDataChange() {
		
		if (!Settings.instance().autoBackup)
			return;
		
		if (!mConnected)
			return;
	
		sync();
	}
	
	private void sync() {
		new SyncTask().execute();
	}
	
	private void dowṇ̣(DownCallBack callback) {
		
		if (!mConnected)
			return;
		
		new DownTask(callback).execute();
	}
	
	public static class DownCallBack {
		void onSuccess() {}
		void onFail(Exception e) {}
	}
	
	private class SyncTask extends AsyncTask<Void, Void, Object> {

		@Override
		protected Object doInBackground(Void... params) {
			
			try {
				doSyncAction();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
		private void doSyncAction() throws IOException, SQLException, JSONException {
			
			DriveId appFolderId = Drive.DriveApi.getAppFolder(mGoogleApiClient).getDriveId();
			
			MetadataBufferResult result = Drive.DriveApi.getFolder(mGoogleApiClient, appFolderId)
					.queryChildren(mGoogleApiClient, null).await();
			
			if (!result.getStatus().isSuccess()) {
				throw new IOException("Create new drive content failed");
			}

			MetadataBuffer metadataBuffer = result.getMetadataBuffer();
			Metadata dbMeta = findMetadataByTitle(metadataBuffer, DatabaseHelper.DATABASE_NAME);
			
			// Sync db
			File dbFile = mAct.getDatabasePath(DatabaseHelper.DATABASE_NAME);
			if (dbMeta == null || modifyDataNeedUpdate(dbMeta, dbFile)) {
				
				if (dbMeta != null)
					uploadExistedFile(dbFile, dbMeta.getDriveId());
				else
					uploadNewFile(dbFile);
				
				sync();
				return;
			}
			
			// Sync image
			List<Looks> lookList = mDb.getDao().queryForAll();
			for (Looks lookItem : lookList) {
				JSONArray jFile = new JSONArray(lookItem.fileName);
				for (int i = 0; i < jFile.length(); i++) {
					
					String fileName = jFile.getString(i);
					File file = mAct.getFileStreamPath(fileName);
					
					if (!file.exists() || !file.isFile())
						continue;
					
					Metadata meta = findMetadataByTitle(metadataBuffer, file.getName());
					if (meta == null || modifyDataNeedUpdate(meta, file)) {
						if (meta != null)
							uploadExistedFile(file, meta.getDriveId());
						else
							uploadNewFile(file);
						
						sync();
						return;
					}
				}
			}
			
			CyUtils.showToast("Backup completed", mAct);
		}
		
		
		
		private boolean modifyDataNeedUpdate(Metadata meta, File local) {
			Date driveDate = meta.getModifiedDate();
			return local.lastModified() > driveDate.getTime();
		}
		
		private void uploadExistedFile(File file, DriveId driveId) throws IOException {
			DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, driveId);
			writeFileToDrive(driveFile, file);
		}
		
		private void uploadNewFile(final File file) throws IOException {
			ContentsResult result = Drive.DriveApi.newContents(mGoogleApiClient).await();
			if (!result.getStatus().isSuccess()) {
				throw new IOException("Create new drive content failed");
			}
			
			OutputStream outputStream = result.getContents().getOutputStream();
			try {
				FileInputStream fileInStream = new FileInputStream(file);
				byte[] byteBuffer = new byte[1024];
				int hasRead;
				while ((hasRead = fileInStream.read(byteBuffer)) > 0) {
					outputStream.write(byteBuffer, 0, hasRead);
				}
				fileInStream.close();
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
			.setTitle(file.getName())
			.setMimeType("application/octet-stream")
			.build();

			DriveFileResult fileResult = Drive.DriveApi.getAppFolder(mGoogleApiClient)
					.createFile(mGoogleApiClient, changeSet, result.getContents()).await();
			if (!fileResult.getStatus().isSuccess()) {
				throw new IOException("Save drive contents failed");
			}
			
			Log.d("File list", "Uploaded new file " + file.getName());
		}
	
		private void writeFileToDrive(DriveFile driveFile, File file) throws IOException {
			ContentsResult contentsResult = driveFile
					.openContents(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).await();
			
			if (!contentsResult.getStatus().isSuccess())
				throw new IOException("Open drive contents failed");
			
			OutputStream outputStream = contentsResult.getContents().getOutputStream();
			
			FileInputStream fileInStream = new FileInputStream(file);
			byte[] byteBuffer = new byte[1024];
			int hasRead;
			while ((hasRead = fileInStream.read(byteBuffer)) > 0)
				outputStream.write(byteBuffer, 0, hasRead);
			fileInStream.close();
			
			com.google.android.gms.common.api.Status status = driveFile.commitAndCloseContents(
                    mGoogleApiClient, contentsResult.getContents()).await();
			
			if (!status.getStatus().isSuccess())
				throw new IOException("Save drive contents failed");
			
			Log.d("File list", "Uploaded overwrite file " + file.getName());
		}
	}
	
	private class DownTask extends AsyncTask<Void, Void, Object> {
		
		private DownCallBack mCallBack;
		
		public DownTask(DownCallBack callback) {
			mCallBack = callback;
		}

		@Override
		protected Object doInBackground(Void... params) {
			
			Exception ex = null;
			try {
				doDownAction();
			} catch (Exception e) {
				e.printStackTrace();
				ex = e;
			}
			
			return ex;
		}
		
		private void doDownAction() throws IOException, SQLException, JSONException {

			DriveId appFolderId = Drive.DriveApi.getAppFolder(mGoogleApiClient).getDriveId();
			
			MetadataBufferResult result = Drive.DriveApi.getFolder(mGoogleApiClient, appFolderId)
					.queryChildren(mGoogleApiClient, null).await();
			
			if (!result.getStatus().isSuccess()) {
				throw new IOException("Get drive file list failed");
			}

			MetadataBuffer metadataBuffer = result.getMetadataBuffer();

			// Down db
			Metadata dbMeta = findMetadataByTitle(metadataBuffer, DatabaseHelper.DATABASE_NAME);
			
			if (dbMeta == null) {
				throw new IOException("Db not found on drive");
			}
				
			File dbFile = mAct.getDatabasePath(DatabaseHelper.DATABASE_NAME);
			DriveFile dbDriveFile = Drive.DriveApi.getFile(mGoogleApiClient, dbMeta.getDriveId());
			
			downFile(dbFile, dbDriveFile);
			Log.d("File list", "Downloaded db");
			
			// Down image file
			DatabaseHelper helper = new DatabaseHelper(mAct);
			List<Looks> lookList = helper.getDao().queryForAll();
			
			for (Looks lookItem : lookList) {
				JSONArray jFile = new JSONArray(lookItem.fileName);
				
				for (int i = 0; i < jFile.length(); i++) {
					String fileName = jFile.getString(i);
					
					Metadata meta = findMetadataByTitle(metadataBuffer, fileName);
					
					if (meta == null) {
						Log.d("File list", "Broken backup: file " + fileName + " found in db but not in drive");
						continue;
					}
					
					File file = mAct.getFileStreamPath(fileName);
					DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, meta.getDriveId());
					
					downFile(file, driveFile);
					Log.d("File list", "Downloaded " + fileName);
				}
			}
			
			metadataBuffer.close();
		}	
		
		private void downFile(File file, DriveFile driveFile) throws IOException {
			
			ContentsResult contentsResult = driveFile
					.openContents(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).await();
			
			if (!contentsResult.getStatus().isSuccess())
				throw new IOException("Open drive contents failed");
			
			InputStream driveStream = contentsResult.getContents().getInputStream();
			
			FileOutputStream fileStream = new FileOutputStream(file);
			
			byte[] byteBuffer = new byte[1024];
			int hasRead;
			while ((hasRead = driveStream.read(byteBuffer)) > 0)
				fileStream.write(byteBuffer, 0, hasRead);
			fileStream.close();
			driveStream.close();
			
			driveFile.discardContents(mGoogleApiClient, contentsResult.getContents()).await();
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			
			if (result == null)
				mCallBack.onSuccess();
			else
				mCallBack.onFail((Exception) result);
		}
	}
	
	private Metadata findMetadataByTitle(MetadataBuffer buffer, String title) {
		
		for (int i = 0; i < buffer.getCount(); i++) {
			Log.d("File list", buffer.get(i).getTitle());
		}
		
		for (int i = 0; i < buffer.getCount(); i++) {
			if (buffer.get(i).getTitle().equals(title))
				return buffer.get(i);
		}
		
		return null;
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
//		pnotifyDataChange();
		CyUtils.showToast("Connected to Google Drive", mAct);
		
		if (mDownBackup != null) {
			dowṇ̣(mDownBackup);
			mDownBackup = null;
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		
	}
}