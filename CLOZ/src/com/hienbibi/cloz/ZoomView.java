package com.hienbibi.cloz;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Scroller;

public class ZoomView extends FrameLayout implements OnGestureListener, OnScaleGestureListener, OnDoubleTapListener {
	
	private ImageView mImg;
	private String mPath;
	private Bitmap mOverviewBitmap;
	private GestureDetector mDetector;
	private ScaleGestureDetector mZoom;
	private Scroller mScroller;
	private Scroller mZoomScroller;
	private Listener mListener = new Listener();
	
	private float mScale, mMinScale, mMaxScale;
	private int photoW, photoH;
	private int overviewW, overviewH;
	private boolean mEnableZoom = true;
	private boolean mEnableSwipeHorizontal = true;
	
	private int mTouchSlop;
	private float mStartX, mStartY;
	private float mScaleFactor;

	public ZoomView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ZoomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public ZoomView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		mImg = new ImageView(context);
		LayoutParams param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mImg.setLayoutParams(param);
		mImg.setScaleType(ScaleType.CENTER_INSIDE);
		addView(mImg);
		
		mDetector = new GestureDetector(context, this);
		mDetector.setOnDoubleTapListener(this);
		mZoom = new ScaleGestureDetector(context, this);
		mScroller = new Scroller(context);
		mZoomScroller = new Scroller(context);
		
		setBackgroundColor(0xFF000000);
		
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}
	
	public void setImage(String filePath) {
		mPath = filePath;
	}
	
	public void enableZoom(boolean enable) {
		mEnableZoom = enable;
	}
	
	public void enableSwipe(boolean enable) {
		mEnableSwipeHorizontal = enable;
	}
	
	public void setListener(Listener listener) {
		mListener = listener;
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

		super.onLayout(changed, left, top, right, bottom);
//		
//		mOverviewBitmap = loadImageOptimize(mPath, right - left, bottom - top);
//		mImg.setImageBitmap(mOverviewBitmap);
//		
		new DecodeTask().execute(new Point(right - left, bottom - top));
	}
	
	private class DecodeTask extends AsyncTask<Point, Void, Object> {

		@Override
		protected Object doInBackground(Point... params) {
			mOverviewBitmap = loadImageOptimize(mPath, params[0].x, params[0].y);
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			mImg.setImageBitmap(mOverviewBitmap);
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}
	
	private boolean mTouchComplete = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (mOverviewBitmap == null)
			return false;
		
		boolean result = mDetector.onTouchEvent(event) | mZoom.onTouchEvent(event);
		
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			requestDisallowInterceptTouchEvent(true);
			mStartX = event.getX();
			mStartY = event.getY();
			break;

		case MotionEvent.ACTION_MOVE: {
			
			if (mTouchComplete)
				break;
			
			float dx = event.getX() - mStartX;
			float dy = event.getY() - mStartY;
			
			float d = (float) Math.sqrt(dx*dx + dy*dy);
			
			if (d <= mTouchSlop || event.getPointerCount() > 1)
				break;
			
			PointF p = getImageSizeWithScaleInPx();
			
			if (dy > Math.abs(dx)) { 		// down
				if (onTopEdge(p)) {
					requestDisallowInterceptTouchEvent(false);
					mListener.onRequestDown();
				}
				mTouchComplete = true;
			} else if (dy < -Math.abs(dx)) {// up
				if (onBottomEdge(p)) {
					requestDisallowInterceptTouchEvent(false);
					mListener.onRequestUp();
				}
				mTouchComplete = true;
			} else if (dx > Math.abs(dy)) {	// right
				if (onLeftEdge(p) && mEnableSwipeHorizontal) {
					requestDisallowInterceptTouchEvent(false);
				} else if (!mEnableSwipeHorizontal) {
					mListener.onRequestSwipeHorizontal();
				}
				mTouchComplete = true;
			} else if (dx < -Math.abs(dy)) {// left
				if (onRightEdge(p) && mEnableSwipeHorizontal) {
					requestDisallowInterceptTouchEvent(false);
				} else if (!mEnableSwipeHorizontal) {
					mListener.onRequestSwipeHorizontal();
				}
				mTouchComplete = true;
			}
		}
			
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mTouchComplete = false;
			break;
		}
		
		return result;
	}
	
	private boolean onTopEdge(PointF imageSize) {
		
//		if (imageSize.y <= getHeight())
//			return true;
//		else
			return -imageSize.y / 2 + mImg.getTranslationY() > -getHeight() / 2 - 10;
	}
	
	private boolean onLeftEdge(PointF imageSize) {
		
//		if (imageSize.x <= getWidth())
//			return true;
//		else
			return -imageSize.x / 2 + mImg.getTranslationX() > -getWidth() / 2 - 10;
	}
	
	private boolean onRightEdge(PointF imageSize) {
		
//		if (imageSize.x <= getWidth())
//			return true;
//		else
			return imageSize.x / 2 + mImg.getTranslationX() < getWidth() / 2 + 10;
	}
	
	private boolean onBottomEdge(PointF imageSize) {
		
//		if (imageSize.y <= getHeight())
//			return true;
//		else
			return imageSize.y / 2 + mImg.getTranslationY() < getHeight() / 2 + 10;
	}
	
	private Bitmap loadImageOptimize(String fileName, int targetW, int targetH) {

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

		if (shouldRotate(orientation)) {
			photoW = bmOpt.outHeight;
			photoH = bmOpt.outWidth;
		} else {
			photoW = bmOpt.outWidth;
			photoH = bmOpt.outHeight;
		}
		
		int limit = 768*1280;
		int scale = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scale = (int) Math.max(Math.min(photoW / targetW, photoH / targetH), Math.sqrt((photoW*photoH) / limit));
		}

		bmOpt = new BitmapFactory.Options();
		bmOpt.inJustDecodeBounds = false;
		bmOpt.inSampleSize = scale;
		bmOpt.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(fileName, bmOpt);
		bitmap = RotateBitmap(bitmap, orientation);
		
		overviewW = bitmap.getWidth();
		overviewH = bitmap.getHeight();
		
		mScale = mMinScale = Math.min(1, Math.min((float)targetW / overviewW, (float)targetH / overviewH));
		mScaleFactor = mScale;
//		mScale = mMinScale = 1;
		mMaxScale = mMinScale * 4;
		
		return bitmap;
	}
	
	private static boolean shouldRotate(int orientation) {

		switch (orientation) {

		case ExifInterface.ORIENTATION_ROTATE_90:
		case ExifInterface.ORIENTATION_ROTATE_270:
			return true;
		case ExifInterface.ORIENTATION_ROTATE_180:
		default:
			return false;
		}
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
	
	private PointF getImageSizeWithScaleInPx() {
		
		PointF p = new PointF();
//		float scale = Math.min(1, Math.min((float)getWidth() / overviewW, (float)getHeight() / overviewH));
//		scale *= mScale;
		p.x = overviewW * mScaleFactor * mScale;
		p.y = overviewH * mScaleFactor * mScale;
		
		return p;
	}
	
	private PointF clamp(float x, float y) {
		
		PointF p = new PointF();
		
		PointF overviewSize = getImageSizeWithScaleInPx();
		
		if (overviewSize.x <= getWidth())
			p.x = 0;
		else {
			float range = (overviewSize.x - getWidth()) / 2;
			p.x = Math.max(-range, Math.min(range, x));
		}
		
		if (overviewSize.y <= getHeight())
			p.y = 0;
		else {
			float range = (overviewSize.y - getHeight()) / 2;
			p.y = Math.max(-range, Math.min(range, y));
		}
		
		return p;
	}
	
	private Point rangeX(PointF overviewSize) {
		
		Point p = new Point();
		
		if (overviewSize.x <= getWidth())
			p.x = p.y = 0;
		else {
			float range = (overviewSize.x - getWidth()) / 2;
			p.x = (int) -range;
			p.y = (int) range;
		}
		
		return p;
	}
	
	private Point rangeY(PointF overviewSize) {
		
		Point p = new Point();
		
		if (overviewSize.y <= getHeight())
			p.x = p.y = 0;
		else {
			float range = (overviewSize.y - getHeight()) / 2;
			p.x = (int) -range;
			p.y = (int) range;
		}
		
		return p;
	}
	
	//////////////////////////////////////////////////
	// Gesture detector

	@Override
	public boolean onDown(MotionEvent e) {
//		Log.d("cycrix", "onDown");
		mScroller.abortAnimation();
//		mZoomScroller.abortAnimation();
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		
		PointF overviewSize = getImageSizeWithScaleInPx();
		Point rangeX = rangeX(overviewSize);
		Point rangeY = rangeY(overviewSize);
		
		mScroller.fling((int)mImg.getTranslationX(), (int)mImg.getTranslationY(), (int)velocityX, (int)velocityY, 
				rangeX.x, rangeX.y, rangeY.x, rangeY.y);
		
		invalidate();
		
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		
		
		int timepass = mZoomScroller.timePassed();
		if (!mZoomScroller.isFinished()) {
			mZoomScroller.computeScrollOffset();
			mScale = Math.max(mMinScale, Math.min(mMaxScale, (float) mZoomScroller.getCurrX() / 1000));
			mImg.setScaleX(mScale / mScaleFactor);
			mImg.setScaleY(mScale / mScaleFactor);
			
			PointF p = new PointF(mImg.getTranslationX(), mImg.getTranslationY());
			p = clamp(p.x, p.y);
			mImg.setTranslationX(p.x);
			mImg.setTranslationY(p.y);
			Log.d("cycrix", "" + mImg.getTranslationX() + " " + mImg.getTranslationY());
			invalidate();
		}
		
		if (mScroller.computeScrollOffset()) {
			mImg.setTranslationX(mScroller.getCurrX());
			mImg.setTranslationY(mScroller.getCurrY());
			Log.d("cycrix", "" + mImg.getTranslationX() + " " + mImg.getTranslationY());
			invalidate();
		}
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		
		PointF p = new PointF(mImg.getTranslationX() - distanceX, mImg.getTranslationY() - distanceY);
		p = clamp(p.x, p.y);
		mImg.setTranslationX(p.x);
		mImg.setTranslationY(p.y);
		Log.d("cycrix", "" + mImg.getTranslationX() + " " + mImg.getTranslationY());
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	
	//////////////////////////////////////////////////
	// Scale detector
	
	private float mPreScale;
	private boolean mDetectedRequestZoom = false;

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		
		if (mEnableZoom) {
			mScale = Math.max(mMinScale, Math.min(mMaxScale, mPreScale * detector.getScaleFactor()));
			mImg.setScaleX(mScale / mScaleFactor);
			mImg.setScaleY(mScale / mScaleFactor);
		} else {
			if (!mDetectedRequestZoom && detector.getScaleFactor() > 1.1) {
				mDetectedRequestZoom = true;
				mListener.onRequestZoom();
			}
		}
		
		return false;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		mPreScale = mScale;
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		mDetectedRequestZoom = false;
		requestDisallowInterceptTouchEvent(false);
	}
	
	//////////////////////////////////////////////////
	// Double tap detector

	@Override
	public boolean onDoubleTap(MotionEvent arg0) {
		
		float threshold = mMaxScale;
		
		if (mEnableZoom) {
			if (mScale < (threshold / 2) ) {
				mZoomScroller.startScroll((int)(mScale * 1000), 0, (int)((threshold - mScale) * 1000), 0);
			} else {
				mZoomScroller.startScroll((int)(mScale * 1000), 0, (int)((mMinScale - mScale) * 1000), 0);
			}
			invalidate();
		} else {
			mListener.onRequestZoom();
		}
		
//		while (!mZoomScroller.isFinished()) {
//		    Log.d("scroller", mZoomScroller.getCurrX() + " " + mZoomScroller.getCurrY());
//		    mZoomScroller.computeScrollOffset();
//		}
		
		
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent arg0) {
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent arg0) {
		return false;
	}
	
	public static class Listener {
		public void onRequestZoom() { Log.d("cycrix", "onRequestZoom");}
		public void onRequestUp() { Log.d("cycrix", "onRequestUp");}
		public void onRequestDown() { Log.d("cycrix", "onRequestDown");}
		public void onRequestSwipeHorizontal() { Log.d("cycrix", "onRequestSwipeHorizontal");}
	}
}
