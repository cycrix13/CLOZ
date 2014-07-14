package com.hienbibi.cloz;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;
import it.sephiroth.android.library.widget.HListView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class CameraActivity extends Activity {

	public static final int MODE_CAMERA = 1;
	public static final int MODE_FILE 	= 2;
	
	private static final int MEDIA_TYPE_IMAGE = 1;
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 123;
	private static final int SELECT_PICTURE = 124;

	private static Listener sListener;
	private static int 		sMode;

	private int 		mMaxItem = 4;
	private Listener 	mListener;
	private int 		mMode;
	private Uri 		fileUri;
	private int			mSelectingIndex = -1;
	private boolean		mFirst = true;
	
	private ArrayList<ImageItem> mItemList = new ArrayList<CameraActivity.ImageItem>();
	
	@ViewById(id = R.id.img)			private ImageView	mImg;
	@ViewById(id = R.id.txtGuide) 		private TextView mTxtGuide;
	@ViewById(id = R.id.imgClose)		private View mImgClose;
	@ViewById(id = R.id.lstThumbnail)	private HListView mLstThumbnail;
	private ThumbAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		mListener = sListener;
		mMode = sMode;
		sListener = null;
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		mLstThumbnail.setAdapter(mAdapter = new ThumbAdapter());
		mLstThumbnail.setOnItemClickListener(mAdapter);

		switch (mMode) {
		case MODE_CAMERA:
			takePhoto();
			break;

		case MODE_FILE:
			openGallary();
			break;
		}
	}

	/**
	 * 
	 * @param act
	 * @param mode CameraActivity.MODE_CAMERA or CameraActivity.MODE_FILE 
	 */
	public static void newInstance(Activity act, Listener listener, int mode) {

		sListener = listener;
		sMode = mode;
		Intent intent = new Intent(act, CameraActivity.class);
		act.startActivity(intent);
	}
	
	private void takePhoto() {
    	
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
	
	private void openGallary() {
		
		Intent intent = new Intent();
		intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, SELECT_PICTURE);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	switch (requestCode) {
    	
    	case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
    		if (resultCode == RESULT_OK) {
    			String path = fileUri.getPath();
    			setImage(path);
    			mFirst = false;
            } else if (mFirst) {
            	finish();
            }
    		break;
    	
    	case SELECT_PICTURE:
    		if (resultCode == RESULT_OK) {
	    		Uri selectedImageUri = data.getData();
	            String strPath = getPath(selectedImageUri);
	            setImage(strPath);
	            mFirst = false;
    		} else if (mFirst) {
            	finish();
            }
    		break;
    	}
	}
	
	private void setImage(String imgPath) {
		
		// calculate optimize scale
		BitmapFactory.Options bmOpt = new BitmapFactory.Options();
		bmOpt.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(imgPath, bmOpt);
		
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(imgPath);
		} catch (IOException e1) {
			return;
		}
		
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

		int photoW = bmOpt.outWidth;
		int photoH = bmOpt.outHeight;

		Display display = getWindowManager().getDefaultDisplay();
		int targetW = display.getWidth();
		int targetH = display.getHeight();
		
		int scale = 1;
		if ((targetW > 0) || (targetH > 0))
			scale = (int) Math.pow(2, Math.max(Math.min(photoW / targetW, photoH / targetH), 1));
		
		bmOpt = new BitmapFactory.Options();
		bmOpt.inJustDecodeBounds = false;
		bmOpt.inSampleSize = scale;
		bmOpt.inPurgeable = true;
		
		Bitmap bitmap = BitmapFactory.decodeFile(imgPath, bmOpt);
		bitmap = RotateBitmap(bitmap, orientation);
		
		mImg.setImageBitmap(bitmap);
		ImageItem item = new ImageItem();
		item.bitmap = bitmap;
		item.path = imgPath;
		mItemList.add(item);
		mSelectingIndex = mItemList.size() - 1;
		mImgClose.setVisibility(View.VISIBLE);
		mAdapter.notifyDataSetChanged();
	}
	
	@Click(id = R.id.imgClose)
	void onRemoveClick(View v) {
		if (mSelectingIndex != -1) {
			mItemList.remove(mSelectingIndex);
			
			if (mItemList.size() > 0) {
				mSelectingIndex = Math.min(mSelectingIndex, mItemList.size() - 1);
				mImgClose.setVisibility(View.VISIBLE);
				mImg.setImageBitmap(mItemList.get(mSelectingIndex).bitmap);
			} else {
				mSelectingIndex = -1;
				mImg.setImageBitmap(null);
				mImgClose.setVisibility(View.INVISIBLE);
			}
			
			mAdapter.notifyDataSetChanged();
		}
	}
	
	@Click(id = R.id.btnCancel)
	void onCancelClick(View v) {
		finish();
	}
	
	@Click(id = R.id.btnSave)
	void onSaveClick(View v) {
		ContactListActivity.newInstance(this);
	}
	
	private static Bitmap RotateBitmap(Bitmap source, int orientation) {
		Matrix matrix = new Matrix();
		
		switch (orientation) {
		
		case ExifInterface.ORIENTATION_ROTATE_90:
			matrix.postRotate(90);
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			matrix.postRotate(180);
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			matrix.postRotate(270);
			break;
		default:
			return source;
		}
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}
	
	private String getPath(Uri uri) {
		
		String[] projection = { MediaStore.Images.Media.DATA };
		
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if(cursor!=null)
		{
			//HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			//THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		else return null;
	}
	
	/** Create a file Uri for saving an image or video */
    public Uri getOutputMediaFileUri(int type){
          return Uri.fromFile(getOutputMediaFile(type));
    }
    
    /** Create a File for saving an image or video */
    public File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

    	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CLOZ");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()) {
                Log.d("CLOZ", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + 
            		"IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
    
    private static class ImageItem {
    	public String path;
    	public Bitmap bitmap;
    }
    
    private class ThumbAdapter extends BaseAdapter implements OnItemClickListener {

		@Override
		public int getCount() {
			return Math.min(mItemList.size() + 1, mMaxItem);
		}

		@Override
		public ImageItem getItem(int position) {
			return mItemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public int getItemViewType(int position) {
			return position < mItemList.size() ? 0 : 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				LayoutInflater inflater = getLayoutInflater();
				if (position < mItemList.size())
					convertView = inflater.inflate(R.layout.camera_thumbnail_item, parent, false);
				else {
					convertView = inflater.inflate(R.layout.camera_add_item, parent, false);
					ImageView img = (ImageView) convertView.findViewById(R.id.img);
					img.setImageResource(mMode == MODE_CAMERA ? R.drawable.add_cam : R.drawable.add_gallery);
				}
			}
			
			if (position < mItemList.size()) {
				ImageView img = (ImageView) convertView.findViewById(R.id.img);
				img.setImageBitmap(getItem(position).bitmap);
			}
			
			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			if (position < mItemList.size()) {
				mSelectingIndex = position;
				mImg.setImageBitmap(getItem(position).bitmap);
				mImgClose.setVisibility(View.VISIBLE);
			} else {
				switch (mMode) {
				case MODE_CAMERA:
					takePhoto();
					break;

				case MODE_FILE:
					openGallary();
					break;
				}
			}
		}
    }

	public interface Listener {

	}	
}