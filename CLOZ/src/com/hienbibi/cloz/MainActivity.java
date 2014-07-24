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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthAdapter.Provider;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils.TruncateAt;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.cycrix.util.CyUtils;
import com.cycrix.util.FontsCollection;
import com.cycrix.util.InlineLayout;
import com.flurry.android.FlurryAgent;
import com.hienbibi.cloz.CameraActivity.ImageItem;
import com.hienbibi.cloz.SearchActivity.TItem;

public class MainActivity extends FragmentActivity implements
		MenuFragment.Listener, CameraActivity.Listener, OnScrollListener,
		OnClickListener {

	private MenuFragment mMenuFragment;
	private HelpFragment mHelpFragment;
	public DatabaseHelper mHelper;

	// @ViewById(id = R.id.lstLook) private ListView mLstLook;
	@ViewById(id = R.id.fliper)
	private ShitLayout mFlipper;

	@ViewById(id = R.id.txtDate1)
	private TextView mTxtData1;
	@ViewById(id = R.id.txtDate2)
	private TextView mTxtData2;
	@ViewById(id = R.id.txtDate3)
	private TextView mTxtData3;
	@ViewById(id = R.id.txtShare)
	private TextView mTxtShare;

	@ViewById(id = R.id.layoutTag)
	private InlineLayout mLayoutTag;

	@ViewById(id = R.id.txtDelete)
	private TextView mTxtDelete;
	@ViewById(id = R.id.txtEdit)
	private TextView mTxtEdit;
	@ViewById(id = R.id.txtShare)
	private View mBtnShare;
	@ViewById(id = R.id.imgAddImage)
	private View mImgAddImage;
	@ViewById(id = R.id.imgAddTag)
	private View mImgAddTag;
	@ViewById(id = R.id.imgDeleteImg)
	private View mImgDeleteImage;
	@ViewById(id = R.id.layoutDate)
	private View mLayoutDate;
	@ViewById(id = R.id.layoutSeperate)
	private View mLayoutSeperate;
	@ViewById(id = R.id.txtBackAll)
	private View mTxtBackAll;
	@ViewById(id = R.id.txtAll)
	private TextView mTxtAll;
	@ViewById(id = R.id.layoutSecondLookHolder)
	private ViewGroup mLayoutSecondLookHolder;
	@ViewById(id = R.id.layoutHolder)
	private ViewGroup mLayoutHolder;

	// @ViewById(id = R.id.imageView1) private ImageView mImg1;
	// @ViewById(id = R.id.imageView2) private ImageView mImg2;

	private ViewPager mPager1;
	private boolean mEditing = false;

	// private ImageView[] mImgArr = new ImageView[2];
	private int mSelecting = 0;
	List<Looks> lookList;
	private boolean mResultMode = false;

	private LookAdapter mAdapter;
	private ArrayList<String> mConditionTag;
	private ArrayList<String> mConditionContact;
	private boolean mZoomEnable;
	private Bitmap shareBitmap;

	public class CustomComparator implements Comparator<Looks> {
		@Override
		public int compare(Looks o1, Looks o2) {
			try {
				String dateStr1 = "";
				JSONArray dateArr1 = new JSONArray(o1.date);
				for (int i = 0; i < dateArr1.length(); i++) {
					dateStr1 += dateArr1.getString(i);
					if (i < dateArr1.length() - 1) {
						dateStr1 += "-";
					}
				}

				String dateStr2 = "";
				JSONArray dateArr2 = new JSONArray(o2.date);
				for (int i = 0; i < dateArr2.length(); i++) {
					dateStr2 += dateArr2.getString(i);
					if (i < dateArr2.length() - 1) {
						dateStr2 += "-";
					}
				}

				SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
				try {
					Date date1 = format.parse(dateStr1);
					Date date2 = format.parse(dateStr2);
					return date2.compareTo(date1);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return 0;
			} catch (Exception e) {
			}

			return 0;

		}
	}

	
	ScaleGestureDetector scaleDetector;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Locale locale = new Locale("es", "ES");
		// Locale.setDefault(locale);
		// Configuration config = new Configuration();
		// config.locale = locale;
		// getBaseContext().getResources().updateConfiguration(config,
		// getBaseContext().getResources().getDisplayMetrics());

		scaleDetector = new ScaleGestureDetector(MainActivity.this, new ScaleGestureDetector.OnScaleGestureListener() {
			
			@Override
			public void onScaleEnd(ScaleGestureDetector detector) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector) {
				// TODO Auto-generated method stub
				//showHelpScreen3();
				return true;
			}
			
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				// TODO Auto-generated method stub
				showHelpScreen3();
				return true;
			}
		});
		
		
		Settings.init(this);
		FontsCollection.init(this);
		mHelper = new DatabaseHelper(this);

		setContentView(R.layout.activity_main);

		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
			FontsCollection.setFont(findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}

		FlashActivity.newInstance(this);

		mMenuFragment = (MenuFragment) getSupportFragmentManager()
				.findFragmentById(R.id.fragMenu);

		if (Settings.instance().firstTime) {
			onHelpClick();

			Settings.instance().firstTime = false;
			Settings.instance().save();
		}

		mMenuFragment.setListerner(this);

		try {
			lookList = mHelper.getDao().queryForAll();
			Collections.sort(lookList, new CustomComparator());
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

			// private float startY;
			private float range = CyUtils.dpToPx(16, MainActivity.this);
			private boolean complete = false;

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				ShitLayout s = (ShitLayout) v;

				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					// startY = event.getY();
					break;

				case MotionEvent.ACTION_MOVE:
					if (!complete && Math.abs(event.getY() - s.startY) > range) {
						prepareForLoadImage(event.getY() - s.startY);
						complete = true;
					} else if (!complete
							&& Math.abs(event.getX() - s.startX) > range * 2) {
						complete = true;
						showHelpScreen3();
					} else if (event.getPointerCount() >= 2) {
						complete = true;
						showHelpScreen2();
					}
					break;

				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
				case MotionEvent.ACTION_CANCEL:
					complete = false;
					break;
				} 
				
				return false;
			}
		});

		if (Settings.instance().autoBackup) {
			BackupHelper.init(this, mHelper);
		}

		mZoomEnable = Settings.instance().unlockZoom;

		adapter = new SocialAuthAdapter(new ResponseListener());
	}

	
	SocialAuthAdapter adapter;
	private final class ResponseListener implements DialogListener {
		public void onComplete(Bundle values) {
			
			try {
				adapter.uploadImageAsync("Landscape Images", "icon.png", shareBitmap, 0,
						new UploadImageListener());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onBack() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onError(SocialAuthError arg0) {
			// TODO Auto-generated method stub
			
		}
	}

	// To get status of image upload after authentication
	private final class UploadImageListener implements
			SocialAuthListener<Integer> {

		@Override
		public void onError(SocialAuthError e) {
		}

		@Override
		public void onExecute(String arg0, Integer arg1) {
			// TODO Auto-generated method stub
			Toast.makeText(MainActivity.this, "Image Uploaded", Toast.LENGTH_SHORT)
					.show();
		}
	}

	protected void showHelpScreen2() {
		if (mLayoutSecondLookHolder.getChildCount() >0)
			return;
		ViewGroup view = (ViewGroup) getLayoutInflater().inflate(
				R.layout.help_screen2_fragment, mLayoutSecondLookHolder, true);
		view.findViewById(R.id.btnClose).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						mLayoutSecondLookHolder.removeAllViews();
					}
				});

		view.findViewById(R.id.btnActive).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						mLayoutSecondLookHolder.removeAllViews();
						InAppActivity.newInstance(MainActivity.this);
					}
				});
	}

	protected void showHelpScreen3() {
		if (mLayoutSecondLookHolder.getChildCount() >0)
			return;
		ViewGroup view = (ViewGroup) getLayoutInflater().inflate(
				R.layout.help_screen3_fragment, mLayoutSecondLookHolder, true);
		view.findViewById(R.id.btnClose).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						mLayoutSecondLookHolder.removeAllViews();
					}
				});

		view.findViewById(R.id.btnActive).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						mLayoutSecondLookHolder.removeAllViews();
						InAppActivity.newInstance(MainActivity.this);
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

	private void loadFlipper() {

		loadFlipperPage(mSelecting - 1);
		loadFlipperPage(mSelecting);
		loadFlipperPage(mSelecting + 1);

		for (int i = 0; i < mFlipper.getChildCount(); i++) {
			Object tag = mFlipper.getChildAt(i).getTag();
			if (tag != null && ((Integer) tag) == mSelecting) {
				mFlipper.setDisplayedChild(i);
				return;
			}
		}
	}

	private void loadFlipperPage(int pageIndex) {

		if (pageIndex < 0 || pageIndex > lookList.size() - 1)
			return;

		// Find. If found, return
		for (int i = 0; i < mFlipper.getChildCount(); i++) {
			Object tag = mFlipper.getChildAt(i).getTag();
			if (tag != null && ((Integer) tag) == pageIndex)
				return;
		}

		// If not found, find the idle one
		ViewGroup idleOne = null;
		for (int i = 0; i < mFlipper.getChildCount(); i++) {
			Object tag = mFlipper.getChildAt(i).getTag();
			if (tag == null || Math.abs(((Integer) tag) - mSelecting) > 1)
				idleOne = (ViewGroup) mFlipper.getChildAt(i);
		}

		if (idleOne == null)
			return; // This MUST NOT happen!

		// Load page into that page
		String fileNames = lookList.get(pageIndex).fileName;
		idleOne.removeAllViews();
		ViewPager pager = new ViewPager(this);
		ViewGroup.LayoutParams param = new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		pager.setLayoutParams(param);
		pager.setAdapter(new LookAdapter(pager, fileNames));
		idleOne.addView(pager);
		idleOne.setTag(pageIndex);
	}

	private void refreshCurrentPage() {

		if (mSelecting < 0)
			return;

		ViewGroup currentOne = null;
		for (int i = 0; i < mFlipper.getChildCount(); i++) {
			Object tag = mFlipper.getChildAt(i).getTag();
			if (tag == null || ((Integer) tag) == mSelecting)
				currentOne = (ViewGroup) mFlipper.getChildAt(i);
		}

		String fileNames = lookList.get(mSelecting).fileName;
		ViewPager pager = (ViewPager) currentOne.getChildAt(0);
		pager.setAdapter(new LookAdapter(pager, fileNames));
	}

	public void refreshDb() {
		try {
			lookList = mHelper.getDao().queryForAll();
			Collections.sort(lookList, new CustomComparator());
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
			loadFlipper();
		}

		// d
		// LLL
		// yyyy

		mLayoutDate.setVisibility(visibility);
		if (mSelecting >= 0) {
			String dateJson = lookList.get(mSelecting).date;
			try {
				JSONArray jDate = new JSONArray(dateJson);
				Date date = new Date(jDate.getInt(2), jDate.getInt(1) - 1,
						jDate.getInt(0));
				mTxtData1.setText("" + jDate.getInt(0));
				mTxtData2.setText(new SimpleDateFormat("LLL").format(date));
				mTxtData3.setText("" + jDate.getInt(2));
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
		}

		mLayoutTag.removeAllViews();
		mLayoutTag.setVisibility(visibility);

		if (mResultMode) {
			String tagJson = lookList.get(mSelecting).tags;
			try {
				JSONArray jTag = new JSONArray(tagJson);
				for (int i = 0; i < jTag.length(); i++) {

					if (!mConditionTag.contains(jTag.getString(i)))
						continue;

					TextView txt = new TextView(this);
					LayoutParams params = new LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					int padding = CyUtils.dpToPx(4, this);
					txt.setPadding(padding * 2, padding, padding * 2, padding);
					txt.setLayoutParams(params);
					txt.setBackgroundResource(R.drawable.search_result_border);
					txt.setText(jTag.getString(i));
					txt.setTextColor(0xFFFFFFFF);
					txt.setMaxWidth(CyUtils.dpToPx(100, this));
					txt.setSingleLine();
					txt.setEllipsize(TruncateAt.END);
					txt.setOnClickListener(this);
					txt.setTag("tag");
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

					if (!mConditionContact.contains(jContacts.getString(i)))
						continue;

					TextView txt = new TextView(this);
					LayoutParams params = new LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					int padding = CyUtils.dpToPx(4, this);
					txt.setPadding(padding * 2, padding, padding * 2, padding);
					txt.setLayoutParams(params);
					txt.setBackgroundResource(R.drawable.search_result_border);
					txt.setText(jContacts.getString(i));
					txt.setTextColor(0xFFFFFFFF);
					txt.setMaxWidth(CyUtils.dpToPx(100, this));
					txt.setSingleLine();
					txt.setEllipsize(TruncateAt.END);
					txt.setOnClickListener(this);
					txt.setTag("contact");
					mLayoutTag.addView(txt);
				}

			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
		}

		if (mSelecting >= 0) {

			String tagJson = lookList.get(mSelecting).tags;
			try {
				JSONArray jTag = new JSONArray(tagJson);
				for (int i = 0; i < jTag.length(); i++) {

					if (mResultMode
							&& mConditionTag.contains(jTag.getString(i)))
						continue;

					TextView txt = new TextView(this);
					LayoutParams params = new LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					int padding = CyUtils.dpToPx(4, this);
					txt.setPadding(padding * 2, padding, padding * 2, padding);
					txt.setLayoutParams(params);
					txt.setBackgroundResource(R.drawable.tag_border);
					txt.setText(jTag.getString(i));
					txt.setTextColor(0xFFFFFFFF);
					txt.setMaxWidth(CyUtils.dpToPx(100, this));
					txt.setSingleLine();
					txt.setEllipsize(TruncateAt.END);
					txt.setOnClickListener(this);
					txt.setTag("tag");
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

					if (mResultMode
							&& mConditionContact.contains(jContacts
									.getString(i)))
						continue;

					TextView txt = new TextView(this);
					LayoutParams params = new LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					int padding = CyUtils.dpToPx(4, this);
					txt.setPadding(padding * 2, padding, padding * 2, padding);
					txt.setLayoutParams(params);
					txt.setBackgroundResource(R.drawable.contact_border);
					txt.setText(jContacts.getString(i));
					txt.setTextColor(0xFFFFFFFF);
					txt.setMaxWidth(CyUtils.dpToPx(100, this));
					txt.setSingleLine();
					txt.setEllipsize(TruncateAt.END);
					txt.setOnClickListener(this);
					txt.setTag("contact");
					mLayoutTag.addView(txt);
				}

			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	@Override
	public void onClick(View v) {

		String type = (String) v.getTag();
		TextView txt = (TextView) v;

		if (mEditing) {
			deleteTag(txt.getText().toString(), type.equals("tag"));
		} else {
			new AlertDialog.Builder(this).setMessage(txt.getText().toString())
					.setPositiveButton(R.string.text_ok, null).create().show();
		}
	}

	private void deleteTag(final String text, final boolean isTag) {
		new AlertDialog.Builder(this)
				.setTitle(text)
				.setMessage(R.string.look_msg_delete_tag)
				.setPositiveButton(R.string.text_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Looks item = lookList.get(mSelecting);
								String json = null;
								if (isTag)
									json = item.tags;
								else
									json = item.contacts;

								String newJson = null;
								try {
									JSONArray jArr = new JSONArray(json);
									JSONArray jNewArr = new JSONArray();
									for (int i = 0; i < jArr.length(); i++) {
										String textItem = jArr.getString(i);
										if (!textItem.equals(text))
											jNewArr.put(textItem);
									}
									newJson = jNewArr.toString();
								} catch (JSONException e) {
									return;
								}

								if (isTag)
									item.tags = newJson;
								else
									item.contacts = newJson;

								try {
									mHelper.getDao().update(item);
									if (Settings.instance().autoBackup)
										BackupHelper.notifyDataChange();
								} catch (SQLException e) {
									return;
								}

								loadImage();
							}
						}).setNegativeButton(R.string.text_cancel, null)
				.create().show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mHelper.close();
		Settings.release();
	}
	
	private void removeHelp() {
		if (mHelpFragment != null) {
			getSupportFragmentManager().beginTransaction().remove(mHelpFragment).commit();
			mMenuFragment.getView().setVisibility(View.VISIBLE);
			mHelpFragment = null;
		}
	}

	@Override
	public void onCameraClick() {
		CameraActivity.newInstance(this, this, CameraActivity.MODE_CAMERA);
		removeHelp();
	}

	@Override
	public void onGalleryClick() {
		CameraActivity.newInstance(this, this, CameraActivity.MODE_FILE);
		removeHelp();
	}

	@Override
	public void onComplete(HashMap<String, Object> result) {

		String imageJson;
		{
			ArrayList<ImageItem> images = (ArrayList<ImageItem>) result
					.get("images");
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
			ArrayList<String> contacts = (ArrayList<String>) result
					.get("contacts");
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
		look.date = dateJson;
		look.tags = tagJson;

		try {
			mHelper.getDao().create(look);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

		try {
			lookList = mHelper.getDao().queryForAll();
			Collections.sort(lookList, new CustomComparator());
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

		if (!Settings.instance().unlockPub)
			CountDownActivity.newInstance(this, false);

		if (Settings.instance().autoBackup)
			BackupHelper.notifyDataChange();

		if (!Settings.instance().hasSecondLook && lookList.size() == 2) {
			Settings.instance().hasSecondLook = true;
			Settings.instance().save();

			View v = getLayoutInflater().inflate(
					R.layout.help_screen1_fragment, mLayoutSecondLookHolder,
					true);
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mLayoutSecondLookHolder.removeAllViews();
				}
			});
		}
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
		if (mHelpFragment != null) {
			return;
		}
		
		mMenuFragment.close();
		mHelpFragment = new HelpFragment();
		getSupportFragmentManager().beginTransaction()
				.add(R.id.layoutHolder, mHelpFragment).commit();
		mHelpFragment.setListener(new HelpFragment.Listener() {
			@Override
			void onDrag(float offset) {
				mMenuFragment.getView().setVisibility(
						offset > 1.5 ? View.VISIBLE : View.INVISIBLE);
			}

			@Override
			void onCloseClick() {
				getSupportFragmentManager().beginTransaction()
						.remove(mHelpFragment).commit();
				mMenuFragment.getView().setVisibility(View.VISIBLE);
				mHelpFragment = null;
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
		if (scrollState != AbsHListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
				&& preScrollState == AbsHListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {

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

		// private ArrayList<ViewGroup> mViewList = new ArrayList<ViewGroup>();
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

			// ImageView img = new ImageView(MainActivity.this);
			ViewGroup.LayoutParams params = new LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			// img.setLayoutParams(param);

			// GestureImageView img = new GestureImageView(MainActivity.this);
			SubsamplingScaleImageView img = new SubsamplingScaleImageView(
					MainActivity.this);
			img.setZoomEnabled(mZoomEnable);
			img.setOnTouchListener(new OnTouchListener() {
	            @Override
	            public boolean onTouch(View view, MotionEvent motionEvent) {
	                return scaleDetector.onTouchEvent(motionEvent);
	            }
	        });
			img.setLayoutParams(params);
			// img.
			// img.setImageResource(R.drawable.camera);
			if (mPathList.size() > 0) {
				String path = getFilesDir().getAbsolutePath() + "/"
						+ mPathList.get(position);
				img.setImageFile(path);
				// img.setImageBitmap(loadImageOptimize(path));
				// new LoadImageTask(path,
				// img).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				// img.setScaleType(ScaleType.FIT_XY);
				// img.setImageResource(R.drawable.nolook);
			}

			container.addView(img);
			return img;
		}

		@Override
		public int getCount() {
			return Math.max(mPathList.size(), 1);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
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

	private class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

		private String mPath;
		private ImageView mImg;

		public LoadImageTask(String path, ImageView img) {
			mPath = path;
			mImg = img;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			return loadImageOptimize(mPath);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);

			mImg.setImageBitmap(result);
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

		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_NORMAL);

		int photoW = bmOpt.outWidth;
		int photoH = bmOpt.outHeight;

		Display display = getWindowManager().getDefaultDisplay();
		int targetW = display.getWidth();
		int targetH = display.getHeight();

		int scale = 1;
		if ((targetW > 0) || (targetH > 0))
			scale = (int) Math.pow(2,
					Math.max(Math.min(photoW / targetW, photoH / targetH), 1));

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
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
				source.getHeight(), matrix, true);
	}

	@Click(id = R.id.txtEdit)
	private void onEditClick(View v) {
		FlurryAgent.logEvent("PRESS_EDIT");
		mEditing = !mEditing;

		int visibleEdit = mEditing ? View.VISIBLE : View.INVISIBLE;
		int visibleNoEdit = mEditing ? View.INVISIBLE : View.VISIBLE;

		updateLayout();
		mTxtEdit.setText(mEditing ? R.string.look_save : R.string.look_edit);
		mBtnShare.setVisibility(visibleNoEdit);
		mImgAddImage.setVisibility(visibleEdit);
		mImgAddTag.setVisibility(mEditing ? View.VISIBLE : View.GONE);
		mImgDeleteImage.setVisibility(visibleEdit);

		mLayoutDate.setBackgroundColor(mEditing ? 0x80000000 : 0x00000000);
		
		if (Settings.instance().firstEdit) {
			Settings.instance().firstEdit = false;
			Settings.instance().save();
			
			View view = getLayoutInflater().inflate(R.layout.help_screen4_fragment, mLayoutHolder, false);
			FontsCollection.setFont(view);
			view.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					mLayoutHolder.removeAllViews();
				}
			});
			mLayoutHolder.addView(view);
		}
	}

	@Click(id = R.id.txtDelete)
	private void onDeleteClick(View v) {
		FlurryAgent.logEvent("PRESS_DELETE");
		if (lookList.size() == 0 || mSelecting == -1) {
			onEditClick(null);
			return;
		}

		new AlertDialog.Builder(this)
				.setMessage(R.string.look_msg_delete_look)
				.setPositiveButton(R.string.text_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Looks item = lookList.get(mSelecting);

								try {
									mHelper.getDao().deleteById(item.id);
									lookList = mHelper.getDao().queryForAll();
									Collections.sort(lookList,
											new CustomComparator());
								} catch (SQLException e) {
									e.printStackTrace();
								}

								if (lookList.size() == 0) {
									mSelecting = -1;
									onEditClick(null);
								} else {
									mSelecting = Math.min(mSelecting,
											lookList.size() - 1);
									mFlipper.setInAnimation(MainActivity.this,
											R.anim.slide_in_up);
									mFlipper.setOutAnimation(MainActivity.this,
											R.anim.slide_out_up);
								}

								loadImage();
								if (Settings.instance().autoBackup)
									BackupHelper.notifyDataChange();
							}
						}).setNegativeButton(R.string.text_no, null).create()
				.show();
	}

	@Click(id = R.id.imgAddTag)
	private void onAddTagClick(View v) {
		FlurryAgent.logEvent("PRESS_ADDTAG");
		new AlertDialog.Builder(this)
				.setItems(R.array.look_msg_add_tag,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								switch (which) {
								case 0:
									ContactListActivity.newInstance(
											MainActivity.this,
											new ContactListActivity.Listener() {
												@Override
												public void onComplete(
														HashMap<String, Object> result) {
													Looks item = lookList
															.get(mSelecting);
													try {
														JSONArray jContacts = new JSONArray(
																item.contacts);
														ArrayList<String> newContactList = (ArrayList<String>) result
																.get("contacts");
														item.contacts = addStringNoDuplicate(
																jContacts,
																newContactList);
														mHelper.getDao()
																.update(item);
														loadImage();
														if (Settings.instance().autoBackup)
															BackupHelper
																	.notifyDataChange();
													} catch (Exception e) {
													}
												}
											}, true);

									break;

								case 1:
									TagActivity.newInstance(MainActivity.this,
											new TagActivity.Listener() {
												@Override
												public void onComplete(
														HashMap<String, Object> result) {
													Looks item = lookList
															.get(mSelecting);
													try {
														JSONArray jContacts = new JSONArray(
																item.tags);
														ArrayList<String> newContactList = (ArrayList<String>) result
																.get("tag");
														item.tags = addStringNoDuplicate(
																jContacts,
																newContactList);
														mHelper.getDao()
																.update(item);
														loadImage();
														if (Settings.instance().autoBackup)
															BackupHelper
																	.notifyDataChange();
													} catch (Exception e) {
													}
												}
											});
									break;
								}
							}
						}).setNegativeButton(R.string.text_cancel, null)
				.create().show();
	}

	private String addStringNoDuplicate(JSONArray jArr,
			ArrayList<String> newList) {

		JSONArray result = new JSONArray(newList);

		try {
			for (int i = 0; i < jArr.length(); i++) {
				String item = jArr.getString(i);
				if (!newList.contains(item))
					result.put(item);
			}

			return result.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Click(id = R.id.imgDeleteImg)
	private void onDeleteImageClick(View v) {

		new AlertDialog.Builder(this)
				.setMessage(R.string.look_msg_delete_image)
				.setPositiveButton(R.string.text_ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Looks item = lookList.get(mSelecting);
								try {
									JSONArray jArr = new JSONArray(
											item.fileName);
									ViewGroup g = (ViewGroup) mFlipper
											.getChildAt(mFlipper
													.getDisplayedChild());
									ViewPager pager = (ViewPager) g
											.getChildAt(0);
									int currentIndex = pager.getCurrentItem();

									JSONArray jNewArr = new JSONArray();
									for (int i = 0; i < jArr.length(); i++) {
										if (i != currentIndex)
											jNewArr.put(jArr.getString(i));
									}

									item.fileName = jNewArr.toString();
									mHelper.getDao().update(item);
									refreshCurrentPage();
									if (Settings.instance().autoBackup)
										BackupHelper.notifyDataChange();
								} catch (Exception e) {
									e.printStackTrace();
									return;
								}
							}
						}).setNegativeButton(R.string.text_no, null).create()
				.show();
	}

	@Click(id = R.id.imgAddImage)
	private void onAddImageClick(View v) {

		Looks item = lookList.get(mSelecting);

		try {
			JSONArray jArr = new JSONArray(item.fileName);
			int maxItem = Settings.instance().unlockZoom ? 4 : 1;
			if (jArr.length() >= maxItem) {
				if (maxItem == 1)
					showHelpScreen3();
				return;
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		new AlertDialog.Builder(this)
				.setItems(R.array.look_msg_add_image,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								CameraActivity.newInstance(
										MainActivity.this,
										new CameraActivity.Listener() {
											@Override
											public void onComplete(
													HashMap<String, Object> result) {
												Looks item = lookList
														.get(mSelecting);
												try {
													JSONArray jArr = new JSONArray(
															item.fileName);
													ArrayList<ImageItem> imageList = (ArrayList<ImageItem>) result
															.get("images");
													JSONArray jNewArr = new JSONArray();

													for (ImageItem imageItem : imageList) {
														File srcFile = new File(
																imageItem.path);
														String randomPath = generateRandomImageFileName(MainActivity.this);
														File dstFile = getFileStreamPath(randomPath);
														try {
															copy(srcFile,
																	dstFile);
														} catch (IOException e) {
															e.printStackTrace();
															return;
														}
														jNewArr.put(randomPath);
													}

													for (int i = 0; i < jArr
															.length(); i++)
														jNewArr.put(jArr
																.getString(i));
													item.fileName = jNewArr
															.toString();
													mHelper.getDao().update(
															item);
													refreshCurrentPage();
													if (Settings.instance().autoBackup)
														BackupHelper
																.notifyDataChange();
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										},
										which == 0 ? CameraActivity.MODE_PICK_CAMERA
												: CameraActivity.MODE_PICK_FILE);
							}
						}).setNegativeButton(R.string.text_cancel, null)
				.create().show();
	}

	@Click(id = R.id.layoutDate)
	private void onDateClick(View v) {

		if (!mEditing)
			return;

		DateActivity.newInstance(this, new DateActivity.Listener() {
			@Override
			public void onComplete(HashMap<String, Object> result) {
				Looks item = lookList.get(mSelecting);
				int[] date = (int[]) result.get("date");
				JSONArray jDate = new JSONArray();
				for (int i : date)
					jDate.put(i);
				item.date = jDate.toString();
				try {
					mHelper.getDao().update(item);
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				}
				loadImage();
				if (Settings.instance().autoBackup)
					BackupHelper.notifyDataChange();
			}
		}, true);
	}

	@Override
	public void onSearchClick() {
		removeHelp();
		SearchActivity.newInstance(this, mHelper, new SearchActivity.Listener() {
			@Override
			public void onComplete(ArrayList<TItem> result) {
				changeToResultMode(result);
			}
		});
	}

	private void changeToResultMode(ArrayList<TItem> condition) {

		// Change GUI
		mResultMode = true;
		updateLayout();

		// Query data
		ArrayList<String> tagList = new ArrayList<String>();
		ArrayList<String> contactList = new ArrayList<String>();
		for (TItem item : condition) {
			if (item.isContact) {
				if (!contactList.contains(item.text))
					contactList.add(item.text);
			} else {
				if (!tagList.contains(item.text))
					tagList.add(item.text);
			}
		}

		mConditionTag = tagList;
		mConditionContact = contactList;

		ArrayList<Looks> result = null;
		try {
			List<Looks> allLook = mHelper.getDao().queryForAll();
			Collections.sort(lookList, new CustomComparator());
			result = new ArrayList<Looks>();
			for (Looks lookItem : allLook) {
				if (changeToResultModeHelper(lookItem.contacts, contactList)
						|| changeToResultModeHelper(lookItem.tags, tagList)) {
					result.add(lookItem);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// Display data
		lookList = result;
		mSelecting = 0;
		loadImage();

		if (!Settings.instance().unlockPub)
			CountDownActivity.newInstance(this, true);
	}

	private boolean changeToResultModeHelper(String jsonStr,
			ArrayList<String> condition) throws Exception {

		JSONArray jStr = new JSONArray(jsonStr);
		for (int i = 0; i < jStr.length(); i++) {
			if (condition.contains(jStr.getString(i)))
				return true;
		}

		return false;
	}

	@Click(id = R.id.txtBackAll)
	private void onBackAllClick(View v) {

		// Change GUI
		mResultMode = false;
		updateLayout();

		// Query data
		try {
			lookList = mHelper.getDao().queryForAll();
			Collections.sort(lookList, new CustomComparator());
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

		// Display data
		mSelecting = lookList.size() != 0 ? 0 : -1;
		loadImage();
	}

	
	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix matrix = new Matrix();
	    // RESIZE THE BIT MAP
	    matrix.postScale(scaleWidth, scaleHeight);

	    // "RECREATE" THE NEW BITMAP
	    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	    return resizedBitmap;
	}
	
	public Bitmap drawTextToBitmap(Bitmap bitmap) {
		
		bitmap = getResizedBitmap(bitmap, 800 * bitmap.getHeight() / bitmap.getWidth(), 800);
		
		
		android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
		// set default bitmap config if none
		if (bitmapConfig == null) {
			bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
		}
		// resource bitmaps are imutable,
		// so we need to convert it to mutable one
		bitmap = bitmap.copy(bitmapConfig, true);

		Canvas canvas = new Canvas(bitmap);
		// new antialised Paint
		Paint paint = new Paint(Paint.DEV_KERN_TEXT_FLAG);
		// text color - #3D3D3D
		paint.setColor(Color.WHITE);
		// text size in pixels
		paint.setTextSize((int) (14));
		// text shadow
		paint.setShadowLayer(3f, -2f,- 2f, Color.BLACK);

		
		Looks look =lookList.get(mSelecting);
		try {
			JSONArray jDate = new JSONArray(look.date);
			Date date = new Date(jDate.getInt(2), jDate.getInt(1) - 1,
					jDate.getInt(0));
			String day = "" + jDate.getInt(0);
			String moth = new SimpleDateFormat("LLL").format(date);
			String year = "" + jDate.getInt(2);
			
			paint.setTextSize((int) (100));
			
			canvas.drawText(day, 80, 140, paint);
			canvas.drawText(moth, 50, 240, paint);
			canvas.drawText(year, 20, 340, paint);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		return bitmap;
	}

	public String saveBitmap(Bitmap bitmap) {
		String file_path = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/CLOZ";
		File dir = new File(file_path);
		if (!dir.exists())
			dir.mkdirs();
		File file = new File(dir, "a.png");
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
			fOut.flush();
			fOut.close();

			return file_path;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Click(id = R.id.txtShare)
	public void onShareClick(View v) {
		FlurryAgent.logEvent("PRESS_SHARE");
		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.setContentView(R.layout.share_dialog);
		dialog.setTitle("Share");

		Button dialogButton = (Button) dialog.findViewById(R.id.bttClose);

		ImageView bttFaceBook = (ImageView) dialog
				.findViewById(R.id.bttFacebook);
		bttFaceBook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FlurryAgent.logEvent("PRESS_FACEBOOK");
				
				shareBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.help1);
				shareBitmap = drawTextToBitmap(shareBitmap);
				
				adapter.authorize(MainActivity.this, Provider.FACEBOOK);
			}
		});

		ImageView bttTwitter = (ImageView) dialog.findViewById(R.id.bttTwitter);
		bttTwitter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FlurryAgent.logEvent("PRESS_TWITTER");
				// TODO Auto-generated method stub
				adapter.authorize(MainActivity.this, Provider.TWITTER);
				adapter.addCallBack(Provider.TWITTER, "http://www.croz.com");
			}
		});

		ImageView bttInstagram = (ImageView) dialog
				.findViewById(R.id.bttInstagram);
		bttInstagram.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FlurryAgent.logEvent("PRESS_INSTAGRAM");
				// TODO Auto-generated method stub
				File filePath = MainActivity.this
						.getFileStreamPath("help1.png");
				Bitmap bm = BitmapFactory.decodeResource(getResources(),
						R.drawable.help1);
				bm = drawTextToBitmap(bm);
				String path = saveBitmap(bm);
				// share("Instagram", filePath.toString(), "CLOZ App");
			}
		});

		ImageView bttWhatapp = (ImageView) dialog.findViewById(R.id.bttWhatApp);
		bttWhatapp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FlurryAgent.logEvent("PRESS_WHATSAPP");
				// TODO Auto-generated method stub
				File filePath = MainActivity.this.getFileStreamPath("add.png");
				// share("Whatsapp", filePath.toString(), "CLOZ App");
			}
		});

		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	void share(String nameApp, File imagePath, String message) {
		List<Intent> targetedShareIntents = new ArrayList<Intent>();
		Intent share = new Intent(android.content.Intent.ACTION_SEND);
		share.setType("image/jpeg");
		List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(
				share, 0);
		if (!resInfo.isEmpty()) {
			for (ResolveInfo info : resInfo) {

				if (info.activityInfo.packageName.toLowerCase().contains(
						nameApp.toLowerCase())
						|| info.activityInfo.name.toLowerCase().contains(
								nameApp.toLowerCase())) {
					Intent targetedShare = new Intent(
							android.content.Intent.ACTION_SEND);
					targetedShare.setType("image/jpeg");
					targetedShare.putExtra(Intent.EXTRA_SUBJECT, "Sharing");
					targetedShare.putExtra(Intent.EXTRA_TEXT, message);
					targetedShare.putExtra(Intent.EXTRA_STREAM,
							Uri.fromFile(imagePath));
					targetedShare.setPackage(info.activityInfo.packageName);
					targetedShareIntents.add(targetedShare);
				}
			}

			if (targetedShareIntents.size() > 0) {
				startActivity(targetedShareIntents.get(0));
				return;
			} else {
				this.showAlertBoxShare("You don't seem to have " + nameApp
						+ " installed on this device");
				return;
			}
		} else
			this.showAlertBoxShare("You don't seem to have " + nameApp
					+ " installed on this device");
	}

	public void showAlertBoxShare(String message) {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setTitle("Message");
		dlgAlert.setMessage(message);
		dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
			}
		});
		dlgAlert.setCancelable(true);
		dlgAlert.create().show();
	}

	@Override
	public void onSyncClick() {
		removeHelp();
		BackupActivity.newInstance(this);
	}

	private void updateLayout() {
		if (mEditing) {
			mTxtDelete.setVisibility(View.VISIBLE);
			mTxtBackAll.setVisibility(View.INVISIBLE);
			mTxtAll.setText(R.string.look_all);
		} else if (mResultMode) {
			mTxtDelete.setVisibility(View.INVISIBLE);
			mTxtBackAll.setVisibility(View.VISIBLE);
			mTxtAll.setText(R.string.look_result);
		} else {
			mTxtDelete.setVisibility(View.INVISIBLE);
			mTxtBackAll.setVisibility(View.INVISIBLE);
			mTxtAll.setText(R.string.look_all);
		}
	}

	@Override
	public void onUseInfoClick() {
		removeHelp();
		UseInfoActivity.newInstance(this, null, true);
	}

	@Override
	public void onSuggestClick() {
		removeHelp();
		SuggestActivity.newInstance(this, null, true);
	}

	@Override
	public void onRateClick() {
		removeHelp();
		 this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.hienbibi.cloz")));
	}

	@Override
	public void onInAppClick() {
		removeHelp();
		InAppActivity.newInstance(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, Settings.API_FLURRY_KEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
}
