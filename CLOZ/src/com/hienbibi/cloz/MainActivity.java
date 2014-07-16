package com.hienbibi.cloz;

import it.sephiroth.android.library.widget.AbsHListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils.TruncateAt;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.ViewById;
import com.cycrix.util.CyUtils;
import com.cycrix.util.FontsCollection;
import com.cycrix.util.InlineLayout;
import com.hienbibi.cloz.CameraActivity.ImageItem;

public class MainActivity extends FragmentActivity implements MenuFragment.Listener, CameraActivity.Listener, OnScrollListener {

	private MenuFragment mMenuFragment;
	private HelpFragment mHelpFragment;
	private DatabaseHelper mHelper;
	
//	@ViewById(id = R.id.lstLook)	private ListView mLstLook;
	@ViewById(id = R.id.fliper)		private ViewFlipper mFlipper;
	
	@ViewById(id = R.id.txtDate1)	private TextView mTxtData1;
	@ViewById(id = R.id.txtDate2)	private TextView mTxtData2;
	@ViewById(id = R.id.txtDate3)	private TextView mTxtData3;
	@ViewById(id = R.id.txtShare)	private TextView mTxtShare;
	
	@ViewById(id = R.id.layoutTag)	private InlineLayout mLayoutTag;
	
	@ViewById(id = R.id.imageView1)	private ImageView mImg1;
	@ViewById(id = R.id.imageView2)	private ImageView mImg2;
	private ImageView[] mImgArr = new ImageView[2];
	private int mSelecting = 0;
	List<Looks> lookList;

	private LookAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//      Locale locale = new Locale("es", "ES");
//	    Locale.setDefault(locale);
//	    Configuration config = new Configuration();
//	    config.locale = locale;
//	    getBaseContext().getResources().updateConfiguration(config,
//	    getBaseContext().getResources().getDisplayMetrics());

		Settings.init(this);
		FontsCollection.init(this);
		mHelper = new DatabaseHelper(this);

		setContentView(R.layout.activity_main);
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		mImgArr[0] = mImg1;
		mImgArr[1] = mImg2;

		FlashActivity.newInstance(this);

		mMenuFragment = (MenuFragment) getSupportFragmentManager().findFragmentById(R.id.fragMenu);

		if (Settings.instance().firstTime) {
			onHelpClick();
			
			Settings.instance().firstTime = false;
			Settings.instance().save();
		}

		mMenuFragment.setListerner(this);
		
		try {
			lookList = mHelper.getDao().queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
			finish();
		}
		
		if (lookList.size() > 0) {
			mSelecting = 0;
			loadImage();
		} else {
			mSelecting = -1;
			loadImage();
		}
		
		
		final GestureDetector detector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
			
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}
			
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				
				if (lookList.size() == 0)
					return true;
				
				int nextIndex;
				if (velocityY > 0) {
					nextIndex = Math.max(mSelecting - 1, 0);
				} else {
					nextIndex = Math.min(mSelecting + 1, lookList.size() - 1);
				}
				
				if (nextIndex == mSelecting)
					return true;
				
				if (velocityY > 0) {
					mFlipper.setInAnimation(MainActivity.this, R.anim.slide_in_down);
					mFlipper.setOutAnimation(MainActivity.this, R.anim.slide_out_down);
				} else {
					mFlipper.setInAnimation(MainActivity.this, R.anim.slide_in_up);
					mFlipper.setOutAnimation(MainActivity.this, R.anim.slide_out_up);
				}
				
				mSelecting = nextIndex;
				loadImage();
				
				return true;
			}
		});
		
		mFlipper.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				return detector.onTouchEvent(arg1);
			}
		});
		
//		mLstLook.setAdapter(mAdapter = new LookAdapter(lookList));
//		mLstLook.setOnScrollListener(this);
	}
	
	private void loadImage() {
		
		int visibility = mSelecting >= 0 ? View.VISIBLE : View.INVISIBLE;
		mFlipper.setVisibility(visibility);
		if (mSelecting >= 0) {
			String fileNames = lookList.get(mSelecting).fileName;

			Bitmap bm = null;
			try {
				JSONArray jArr = new JSONArray(fileNames);
				String path = getFilesDir().getAbsolutePath() + "/" + jArr.getString(0);
				bm = loadImageOptimize(path);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			int i = 1 - mFlipper.getDisplayedChild();
			ImageView img = (ImageView) mFlipper.getChildAt(i);
			img.setImageBitmap(bm);
			mFlipper.setDisplayedChild(i);

		}
		
		// d
		// LLL
		// yyyy
		
		
		mTxtData1.setVisibility(visibility);
		mTxtData2.setVisibility(visibility);
		mTxtData3.setVisibility(visibility);
		mTxtShare.setVisibility(visibility);
		if (mSelecting >= 0) {
			String dateJson = lookList.get(mSelecting).date;
			try {
				JSONArray jDate = new JSONArray(dateJson);
				Date date = new Date(jDate.getInt(2), jDate.getInt(1) - 1, jDate.getInt(0));
				mTxtData1.setText("" + jDate.getInt(0));
				mTxtData2.setText(new SimpleDateFormat("LLL").format(date));
				mTxtData3.setText("" + jDate.getInt(2));
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
		}
		
		mLayoutTag.removeAllViewsInLayout();
		mLayoutTag.setVisibility(visibility);
		if (mSelecting >= 0) {
			
			String tagJson = lookList.get(mSelecting).tags;
			if (tagJson.length() > 0) {
				TextView txt = new TextView(this);
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				int padding = CyUtils.dpToPx(4, this);
				txt.setPadding(padding * 2, padding, padding * 2, padding);
				txt.setLayoutParams(params);
				txt.setBackgroundResource(R.drawable.tag_border);
				txt.setText(tagJson);
				txt.setTextColor(0xFFFFFFFF);
				txt.setMaxWidth(CyUtils.dpToPx(100, this));
				txt.setSingleLine();
				txt.setEllipsize(TruncateAt.END);
				mLayoutTag.addView(txt);
			}
			
			String contactJson = lookList.get(mSelecting).contacts;
			try {
				JSONArray jContacts = new JSONArray(contactJson);
				for (int i = 0; i < jContacts.length(); i++) {
					TextView txt = new TextView(this);
					LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					int padding = CyUtils.dpToPx(4, this);
					txt.setPadding(padding * 2, padding, padding * 2, padding);
					txt.setLayoutParams(params);
					txt.setBackgroundResource(R.drawable.contact_border);
					txt.setText(jContacts.getString(i));
					txt.setTextColor(0xFFFFFFFF);
					txt.setMaxWidth(CyUtils.dpToPx(100, this));
					txt.setSingleLine();
					txt.setEllipsize(TruncateAt.END);
					mLayoutTag.addView(txt);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mHelper.close();
		Settings.release();
	}

	@Override
	public void onCameraClick() {
		CameraActivity.newInstance(this, this, CameraActivity.MODE_CAMERA);
	}

	@Override
	public void onGalleryClick() {
		CameraActivity.newInstance(this, this, CameraActivity.MODE_FILE);
	}
	
	@Override
	public void onComplete(HashMap<String, Object> result) {
		
		String imageJson;
		{
			ArrayList<ImageItem> images = (ArrayList<ImageItem>) result.get("images");
			JSONArray jArr = new JSONArray();
			for (ImageItem item : images) {
				File srcFile = new File(item.path);
				String randomPath = generateRandomImageFileName(this);
				File dstFile = getFileStreamPath(randomPath);
				try {
					copy(srcFile, dstFile);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				jArr.put(randomPath);
			}
			imageJson = jArr.toString();
		}
		
		
		String contactJson;
		{
			ArrayList<String> contacts = (ArrayList<String>) result.get("contacts");
			JSONArray jArr = new JSONArray();
			for (String contact : contacts)
				jArr.put(contact);
			contactJson = jArr.toString();
		}
		
		String dateJson;
		{
			int[] date = (int[]) result.get("date");
			JSONArray jArr = new JSONArray();
			for (int i : date)
				jArr.put(i);
			dateJson = jArr.toString();
		}
		
		String tagJson = (String) result.get("tag");
		
		Looks look = new Looks();
		look.fileName = imageJson;
		look.contacts = contactJson;
		look.date	  = dateJson;
		look.tags	  = tagJson;
		
		try {
			mHelper.getDao().create(look);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			lookList = mHelper.getDao().queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
			finish();
		}
		
		if (lookList.size() > 0) {
			mSelecting = lookList.size() - 1;
		} else {
			mSelecting = -1;
		}
		loadImage();
//		mLstLook.setAdapter(mAdapter = new LookAdapter(lookList));
	}
	
	public void copy(File src, File dst) throws IOException {
	    FileInputStream inStream = new FileInputStream(src);
	    FileOutputStream outStream = new FileOutputStream(dst);
	    FileChannel inChannel = inStream.getChannel();
	    FileChannel outChannel = outStream.getChannel();
	    inChannel.transferTo(0, inChannel.size(), outChannel);
	    inStream.close();
	    outStream.close();
	}
	
	public static String generateRandomImageFileName(Activity act) {
		
		Random random = new Random(System.currentTimeMillis());
		String name = null;
		File f = null;
		do {
			// generate file name
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < 10; i++) {
				builder.append((char) ('a' + random.nextInt('z' - 'a')));
			}
			
			// check existence
			name = builder.toString();
			f = act.getFileStreamPath(name);
		} while (f.isFile());
		
		return name;
	}

	@Override
	public void onHelpClick() {
		mMenuFragment.close();
		mHelpFragment = new HelpFragment();
		getSupportFragmentManager().beginTransaction().add(R.id.layoutHolder, mHelpFragment).commit();
		mHelpFragment.setListener(new HelpFragment.Listener() {
			@Override
			void onDrag(float offset) {
				mMenuFragment.getView().setVisibility(offset > 1.5 ? View.VISIBLE : View.INVISIBLE);
			}

			@Override
			void onCloseClick() {
				getSupportFragmentManager().beginTransaction().remove(mHelpFragment).commit();
				mMenuFragment.getView().setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	private int preScrollState = AbsHListView.OnScrollListener.SCROLL_STATE_IDLE;
	@Override
	public void onScrollStateChanged(final AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		if (scrollState != AbsHListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL && 
				preScrollState == AbsHListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {

			View firstView = view.getChildAt(0);
			if (firstView == null)
				return;

			final int posToScrolll;
			if (-firstView.getTop() < view.getHeight() / 2)
				posToScrolll = view.getFirstVisiblePosition();
			else
				posToScrolll = view.getFirstVisiblePosition() + 1;

			new Handler().post(new Runnable() {
				@Override
				public void run() {
					view.smoothScrollToPositionFromTop(posToScrolll, 0);
				}
			});
		}

		preScrollState = scrollState;
	}

	private class LookAdapter extends ArrayAdapter<Looks> {

		public LookAdapter(List<Looks> objects) {
			super(MainActivity.this, R.layout.look_item, R.id.txtDate, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = super.getView(position, convertView, parent);
			
			Looks item = getItem(position);
			ImageView img = (ImageView) convertView.findViewById(R.id.img);
			
			JSONArray jArr = null;
			try {
				jArr = new JSONArray(item.fileName);
				String path = getFilesDir().getAbsolutePath() + "/" + jArr.getString(0);
				img.setImageBitmap(loadImageOptimize(path));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return convertView;
		}
		
		
	}
	
	private Bitmap loadImageOptimize(String fileName) {
		
		// calculate optimize scale
		BitmapFactory.Options bmOpt = new BitmapFactory.Options();
		bmOpt.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(fileName, bmOpt);
		
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(fileName);
		} catch (IOException e1) {
			return null;
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
		
		Bitmap bitmap = BitmapFactory.decodeFile(fileName, bmOpt);
		return bitmap;
	}
}
