package com.hienbibi.cloz;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.cycrix.util.CyUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Bitmap.Config;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class MoveTagActivity extends Activity {
	
	private static ArrayList<String> sTagList;
	private static Listener sListener;
	private static int[] sDate;
	private static String sPath;
	
	private ArrayList<String> mTagList; 
	private Listener mListener;
	private int[] mDate;
	private String mPath;
	
	private int dp24, dp48;
	
	
	@ViewById(id = R.id.layoutCanvas)		private ViewGroup mLayoutCanvas;
	@ViewById(id = R.id.img)				private ImageView mImg;
	
	public static class Listener {
		public void onComplete(Bitmap bm, int socialType, boolean saveImage) {}
	}
	
	public static void newInstance(Activity act, String imagePath, ArrayList<String> tagList, int[] date, Listener listener) {
		sTagList = tagList;
		sDate = date;
		sListener = listener;
		sPath = imagePath;
		Intent intent = new Intent(act, MoveTagActivity.class);
		act.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_move_tag);
		
		mTagList = sTagList;
		mListener = sListener;
		mDate = sDate;
		mPath = sPath;
		sTagList = null;
		sListener = null;
		sDate = null;
		sPath = null;
		
		dp24 = CyUtils.dpToPx(24, this);
		dp48 = CyUtils.dpToPx(48, this);
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		mImg.setImageBitmap(loadImageOptimize(mPath));
		
		for (String tag : mTagList) {
			TextView txt = new TextView(this);
			LayoutParams param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			txt.setLayoutParams(param);
			txt.setTextColor(0xFFFFFFFF);
			txt.setTextSize(18);
			txt.setText(tag);
			txt.setShadowLayer(2, 0, 2, 0xFF000000);
			mLayoutCanvas.addView(txt);
			txt.addOnLayoutChangeListener(new OnLayoutChangeListener() {
				
				@Override
				public void onLayoutChange(View v, int left, int top, int right,
						int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					Point p = randomPoint(right - left, bottom - top);
					v.setTranslationX(p.x);
					v.setTranslationY(p.y);
				}	
			});
			
			txt.setOnTouchListener(makeListener());
		}
		
		if (mDate != null) {
			View dateLayout = makeDateLayout();
			mLayoutCanvas.addView(dateLayout);
			dateLayout.addOnLayoutChangeListener(new OnLayoutChangeListener() {

				@Override
				public void onLayoutChange(View v, int left, int top,
						int right, int bottom, int oldLeft, int oldTop,
						int oldRight, int oldBottom) {
					Point p = randomPoint(right - left, bottom - top);
					v.setTranslationX(p.x);
					v.setTranslationY(p.y);
				}
			});
			dateLayout.setOnTouchListener(makeListener());
		}
	}
	
	private View makeDateLayout() {
		
		View v = getLayoutInflater().inflate(R.layout.date_layout, mLayoutCanvas, false);
		TextView txtDate1 = (TextView) v.findViewById(R.id.txtDate1);
		TextView txtDate2 = (TextView) v.findViewById(R.id.txtDate2);
		TextView txtDate3 = (TextView) v.findViewById(R.id.txtDate3);
		
		Date date = new Date(mDate[2], mDate[1] - 1, mDate[0]);
		txtDate1.setText("" + mDate[0]);
		txtDate2.setText(new SimpleDateFormat("LLL").format(date));
		txtDate3.setText("" + mDate[2]);
		
		return v;
	}
	
	private OnTouchListener makeListener() {
		return new OnTouchListener() {
			
			PointF start = new PointF();
			int sh = getResources().getDisplayMetrics().heightPixels;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					start.x = event.getX();
					start.y = event.getY();
					break;
					
				case MotionEvent.ACTION_MOVE:
					float tx = v.getTranslationX() + event.getX() - start.x;
					float ty = v.getTranslationY() + event.getY() - start.y;
					ty = Math.max(dp48, Math.min(sh - v.getHeight() - dp48, ty));
					v.setTranslationX(tx);
					v.setTranslationY(ty);
					break;
					
				case MotionEvent.ACTION_UP:
					break;
				}
				
				return true;
			}
		};
	}
	
	private Point randomPoint(int w, int h) {
		Point p = new Point();
		
		int sw = getResources().getDisplayMetrics().widthPixels;
		int sh = getResources().getDisplayMetrics().heightPixels;
		
		p.x = (int) (Math.random() * (sw - w));
		p.y = dp48 + (int) (Math.random() * (sh - h - dp48 * 2));
		
		return p;
	}
	
	@Click(id = R.id.btnBack)
	private void onBackClick(View v) {
		finish();
	}
	
	@Click(id = R.id.btnContinue)
	private void onContinueClick(View v) {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.share_dialog2);
		dialog.setTitle(R.string.movetag_text2);
		dialog.setCancelable(true);
		
		new OnClickListener() {
			
			private View[] btnArr = new View[4];
			private CheckBox chk;
			private View btnContinue;
			private int selecting = -1;
			
			public OnClickListener init() {
				
				btnArr[0] = dialog.findViewById(R.id.bttFacebook);
				btnArr[1] = dialog.findViewById(R.id.bttTwitter);
				btnArr[2] = dialog.findViewById(R.id.bttInstagram);
				btnArr[3] = dialog.findViewById(R.id.bttWhatApp);
				chk = (CheckBox) dialog.findViewById(R.id.chkBox);
				btnContinue = dialog.findViewById(R.id.bttClose);
				
				for (View v : btnArr)
					v.setOnClickListener(this);
				btnContinue.setOnClickListener(this);
				return this;
			}
			
			@Override
			public void onClick(View v) {
				
				if (v == btnContinue) {
					boolean check = chk.isChecked();
					
					if (!check && selecting == -1) {
						dialog.dismiss();
						return;
					}
					
					mListener.onComplete(drawViewToBitmap(), selecting, check);
					
					finish();
					return;
				}
				
				for (int i = 0; i < 4; i++)
					if (btnArr[i] == v) {
						if (selecting == i) {
							btnArr[i].setBackgroundColor(0);
							selecting = -1;
						} else {
							btnArr[i].setBackgroundColor(0xFF6AD201);
							selecting = i;
						}
					} else {
						btnArr[i].setBackgroundColor(0);
					}
			}
		}.init();
		
		dialog.show();
	}
	
	private Bitmap drawViewToBitmap() {
		View v = mLayoutCanvas;
		Bitmap result = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Config.ARGB_8888);
		
		Canvas canvas = new Canvas(result);
		
		v.draw(canvas);
		
		return result;
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
			scale = (int) Math.pow(2,Math.max(Math.min(photoW / targetW, photoH / targetH), 1));

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
}
