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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils.TruncateAt;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
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
	@ViewById(id = R.id.fliper)		private ShitLayout mFlipper;
	
	@ViewById(id = R.id.txtDate1)	private TextView mTxtData1;
	@ViewById(id = R.id.txtDate2)	private TextView mTxtData2;
	@ViewById(id = R.id.txtDate3)	private TextView mTxtData3;
//	@ViewById(id = R.id.txtShare)	private TextView mTxtShare;
	
	@ViewById(id = R.id.layoutTag)	private InlineLayout mLayoutTag;
	
	@ViewById(id = R.id.txtDelete)	private TextView mTxtDelete;
	@ViewById(id = R.id.txtEdit)	private TextView mTxtEdit;
	@ViewById(id = R.id.txtShare)	private View mBtnShare;
	@ViewById(id = R.id.imgAddImage)private View mImgAddImage;
	@ViewById(id = R.id.imgAddTag)	private View mImgAddTag;
	@ViewById(id = R.id.imgDeleteImg)private View mImgDeleteImage;
	@ViewById(id = R.id.layoutDate)	private View mLayoutDate;
	@ViewById(id = R.id.layoutSeperate)	private View mLayoutSeperate;
	
	
//	@ViewById(id = R.id.imageView1)	private ImageView mImg1;
//	@ViewById(id = R.id.imageView2)	private ImageView mImg2;
	
	private ViewPager mPager1;
	private boolean mEditing = false;
	
//	private ImageView[] mImgArr = new ImageView[2];
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
		
		mFlipper.setOnTouchListener(new OnTouchListener() {

//			private float startY;
			private float range = CyUtils.dpToPx(16, MainActivity.this);
			private boolean complete = false;

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				ShitLayout s = (ShitLayout) v;
				
				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
//					startY = event.getY();
					break;

				case MotionEvent.ACTION_MOVE:
					if (!complete && Math.abs(event.getY() - s.startY) > range) {
						prepareForLoadImage(event.getY() - s.startY);
						complete = true;
					}
					break;

				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
				case MotionEvent.ACTION_CANCEL:
					complete = false;
					break;
				}
				
				return true;
			}
		});
	}
	
	private void prepareForLoadImage(float deltaY) {
		
		if (lookList.size() == 0)
			return;
		
		int nextIndex;
		if (deltaY > 0) {
			nextIndex = Math.max(mSelecting - 1, 0);
		} else {
			nextIndex = Math.min(mSelecting + 1, lookList.size() - 1);
		}
		
		if (nextIndex == mSelecting)
			return;
		
		if (deltaY > 0) {
			mFlipper.setInAnimation(MainActivity.this, R.anim.slide_in_down);
			mFlipper.setOutAnimation(MainActivity.this, R.anim.slide_out_down);
		} else {
			mFlipper.setInAnimation(MainActivity.this, R.anim.slide_in_up);
			mFlipper.setOutAnimation(MainActivity.this, R.anim.slide_out_up);
		}
		
		mSelecting = nextIndex;
		loadImage();
	}
	
	private void loadImage() {
		
		int visibility = mSelecting >= 0 ? View.VISIBLE : View.INVISIBLE;
		
		mTxtEdit.setVisibility(visibility);
		mLayoutSeperate.setVisibility(visibility);
		if (mSelecting >= 0 && !mEditing)
			mBtnShare.setVisibility(View.VISIBLE);
		else
			mBtnShare.setVisibility(View.INVISIBLE);
		
		mFlipper.setVisibility(visibility);
		if (mSelecting >= 0) {
			String fileNames = lookList.get(mSelecting).fileName;

			int i = 1 - mFlipper.getDisplayedChild();
			
			ViewGroup group = (ViewGroup) mFlipper.getChildAt(i);
			group.removeAllViews();
			ViewPager pager = new ViewPager(this);
			ViewGroup.LayoutParams param = new LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			pager.setLayoutParams(param);
			pager.setAdapter(new LookAdapter(pager, fileNames));
			group.addView(pager);
			
//			ImageView img = (ImageView) mFlipper.getChildAt(i);
//			img.setImageBitmap(bm);
			mFlipper.setDisplayedChild(i);
		}
		
		// d
		// LLL
		// yyyy
		
		mLayoutDate.setVisibility(visibility);
//		mTxtData1.setVisibility(visibility);
//		mTxtData2.setVisibility(visibility);
//		mTxtData3.setVisibility(visibility);
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
			try {
				JSONArray jTag = new JSONArray(tagJson);
				for (int i = 0; i < jTag.length(); i++) {
					TextView txt = new TextView(this);
					LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					int padding = CyUtils.dpToPx(4, this);
					txt.setPadding(padding * 2, padding, padding * 2, padding);
					txt.setLayoutParams(params);
					txt.setBackgroundResource(R.drawable.tag_border);
					txt.setText(jTag.getString(i));
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
		
		String tagJson;
		{
			ArrayList<String> tagList = (ArrayList<String>) result.get("tag");
			JSONArray jArr = new JSONArray();
			for (String tag : tagList)
				jArr.put(tag);
			tagJson = jArr.toString();
		}
		
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

	private class LookAdapter extends PagerAdapter {

//		private ArrayList<ViewGroup> mViewList = new ArrayList<ViewGroup>();
		private ArrayList<String> mPathList = new ArrayList<String>();

		public LookAdapter(ViewGroup container, String jsonArrPath) {

			try {
				JSONArray jArr = new JSONArray(jsonArrPath);
				for (int i = 0; i < jArr.length(); i++) {
					String path = jArr.getString(i);
					mPathList.add(path);
				}
			} catch (JSONException e) {
			}
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			
			ImageView img = new ImageView(MainActivity.this);
			ViewGroup.LayoutParams param = new LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			img.setLayoutParams(param);
			String path = getFilesDir().getAbsolutePath() + "/" + mPathList.get(position);
			img.setImageBitmap(loadImageOptimize(path));
			
			container.addView(img);
			return img;
		}

		@Override
		public int getCount() {
			return mPathList.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View)object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return (view == object);
		}

		@Override
		public int getItemPosition(Object object) {
			
			return PagerAdapter.POSITION_UNCHANGED;
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
		bitmap = RotateBitmap(bitmap, orientation);
		return bitmap;
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
	
	@Click(id = R.id.txtEdit)
	private void onEditClick(View v) {
		mEditing = !mEditing;
		
		int visibleEdit = mEditing ? View.VISIBLE : View.INVISIBLE;
		int visibleNoEdit = mEditing ? View.INVISIBLE : View.VISIBLE;
		
		mTxtDelete.setVisibility(visibleEdit);
		mTxtEdit.setText(mEditing ? R.string.look_save : R.string.look_edit);
		mBtnShare.setVisibility(visibleNoEdit);
		mImgAddImage.setVisibility(visibleEdit);
		mImgAddTag.setVisibility(mEditing ? View.VISIBLE : View.GONE);
		mImgDeleteImage.setVisibility(visibleEdit);
		
		mLayoutDate.setBackgroundColor(mEditing ? 0x80000000 : 0x00000000);
	}
	
	@Click(id = R.id.txtDelete)
	private void onDeleteClick(View v) {
		
		if (lookList.size() == 0 || mSelecting == -1) {
			onEditClick(null);
			return;
		}
		
		new AlertDialog.Builder(this)
		.setMessage(R.string.look_msg_delete_look)
		.setPositiveButton(R.string.text_ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Looks item = lookList.get(mSelecting);
				
				try {
					mHelper.getDao().deleteById(item.id);
					lookList = mHelper.getDao().queryForAll();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				if (lookList.size() == 0) {
					mSelecting = -1;
					onEditClick(null);
				} else {
					mSelecting = Math.min(mSelecting, lookList.size() - 1);
					mFlipper.setInAnimation(MainActivity.this, R.anim.slide_in_up);
					mFlipper.setOutAnimation(MainActivity.this, R.anim.slide_out_up);
				}
				
				loadImage();
			}
		})
		.setNegativeButton(R.string.text_no, null)
		.create().show();
	}
}
