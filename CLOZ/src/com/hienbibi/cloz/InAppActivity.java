package com.hienbibi.cloz;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.hienbibi.cloz.util.IabHelper;
import com.hienbibi.cloz.util.IabResult;
import com.hienbibi.cloz.util.Purchase;
import com.hienbibi.cloz.util.Inventory;
import com.hienbibi.cloz.util.IabHelper.QueryInventoryFinishedListener;
import com.hienbibi.cloz.util.IabHelper.OnIabPurchaseFinishedListener;
import com.hienbibi.cloz.util.IabHelper.OnConsumeMultiFinishedListener;
import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class InAppActivity extends Activity {
	
	private static final String  TAG = "InAppActivity";
	
	private static MainActivity sAct;
	private MainActivity mAct;
	
//	public static final String SKU_4IMAGES 		= "zoom_4images";
//	public static final String SKU_DELETE_PUB 	= "delete_pub";
	public static final String SKU_4IMAGES 		= "android.test.purchased";
	public static final String SKU_DELETE_PUB 	= "android.test.purchased";
//	android.test.purchased
	public static final int REQUEST_CODE_PURCHASE = 1001;
	
	public IabHelper mHelper;
	
	public static void newInstance(MainActivity act) {
		sAct = act;
		Intent intent = new Intent(act, InAppActivity.class);
		act.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_in_app);
		
		mAct = sAct;
		sAct = null;
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCg" +
				"KCAQEApIVX2Ty92iWg5todSD05AkOJUQxkMrCXyTWB2quV7F5peTYd38Ig10if" +
				"rb2mT1/EhUGI37g4LplIB0lFSg0Rv5xvArMV0W99NIwSaD0a+tPltAbMM4tyqd" +
				"ObVU7QRwxmX4lX0BZ7pJmfJfExxIXS5aS61m4XldKW7dhVppGIlGXmDo5pxsH9" +
				"qiq5ERIynVQwUjRPhnvjeN3I+6AMsz3U2sfgy6uobxxgECvrPvFrmquyBepyXa" +
				"RzYJxwQxl2PmEl6xAAs2wohpymdmZRz26TSHDP6Qq0B/Q2ONa24J1p46jxBKYG" +
				"H8NaxGoUyBiDy03J8I6arbm56j3r/Ii70trYUQIDAQAB";
		
		mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new 
        		IabHelper.OnIabSetupFinishedListener() {
        	public void onIabSetupFinished(IabResult result) 
        	{
        		if (!result.isSuccess()) {
        			Log.d(TAG, "In-app Billing setup failed: " + result);
        			mHelper = null;
        		} else {             
        			Log.d(TAG, "In-app Billing is set up OK");
        		}
        	}
        });
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mHelper != null)
    		mHelper.dispose();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (mHelper != null)
    		mHelper.handleActivityResult(requestCode, resultCode, data);
	}

	@Click(id = R.id.btnClose)
	private void onCloseClick(View v) {
		finish();
	}
	
	@Override
	public void finish() {
		super.finish();
		
		if (Settings.instance().unlockZoom) {
			mAct.unlookZoom();
		}
	}
	
	@Click(id = R.id.btn4Image)
	private void on4ImageClick(View v) {
		
		FlurryAgent.logEvent("PRESS_ZOOM_4_IMAGES");
		
		try {
    		if (Settings.instance().unlockZoom) {
    			AlertDialog.Builder builder = new AlertDialog.Builder(InAppActivity.this);
				builder.setMessage("You have already owned this feature!");
				builder.setPositiveButton("OK", null);
				builder.setCancelable(false);
				builder.create().show();
    			return;
    		}
    		
			mHelper.queryInventoryAsync(new QueryInventoryFinishedListener() {
				
				@Override
				public void onQueryInventoryFinished(IabResult result, Inventory inv) {
					
//					Purchase p = inv.getPurchase(SKU_4IMAGES);
					Purchase p;
					if (inv != null && (p = inv.getPurchase(SKU_4IMAGES)) != null) {
						AlertDialog.Builder builder = new AlertDialog.Builder(InAppActivity.this);
						builder.setMessage("You have already owned this feature. " +
								"Would you like to restore purchase?");
						
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								unlock4Image();
								
								AlertDialog.Builder builder = new AlertDialog.Builder(InAppActivity.this);
								builder.setMessage("Zoom + up to 4 images/Look unlocked!");
								builder.setPositiveButton("OK", null);
								builder.setCancelable(false);
								builder.create().show();
							}
						});
						
						builder.setNegativeButton("No", null);
						builder.create().show();
					} else {
						mHelper.launchPurchaseFlow(InAppActivity.this, SKU_4IMAGES,
								REQUEST_CODE_PURCHASE, new OnIabPurchaseFinishedListener() {

							@Override
							public void onIabPurchaseFinished(IabResult result, Purchase info) {
								onPurchase4ImageFinished(result, info);
							}
						});
					}
				}
			});
			
		} catch (Exception e) {
			showError("Purchase failed");
		}
	}
	
	private void onPurchase4ImageFinished(IabResult result, Purchase info) {
		try {
			if (result.isFailure())
				throw new Exception();

			unlock4Image();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Zoom + up to 4 images/Look unlocked!");
			builder.setPositiveButton("OK", null);
			builder.setCancelable(false);
			builder.create().show();
			
		} catch (Exception e) {
			showError("Purchase failed");
		}
	}
	
	private void unlock4Image() {
		Settings.instance().unlockZoom = true;
		Settings.instance().save();
	}
	
	@Click(id = R.id.btnDeletePub)
	private void onDeletePubClick(View v) {
		
		FlurryAgent.logEvent("PRESS_ZOOM_4_IMAGES");
		
		try {
    		if (Settings.instance().unlockPub) {
    			AlertDialog.Builder builder = new AlertDialog.Builder(InAppActivity.this);
				builder.setMessage("You have already owned this feature!");
				builder.setPositiveButton("OK", null);
				builder.setCancelable(false);
				builder.create().show();
    			return;
    		}
    		
			mHelper.queryInventoryAsync(new QueryInventoryFinishedListener() {
				
				@Override
				public void onQueryInventoryFinished(IabResult result, Inventory inv) {
//					Purchase p = inv.getPurchase(SKU_DELETE_PUB);
					Purchase p;
					if (inv != null && (p = inv.getPurchase(SKU_DELETE_PUB)) != null) {
						AlertDialog.Builder builder = new AlertDialog.Builder(InAppActivity.this);
						builder.setMessage("You have already owned this feature. " +
								"Would you like to restore purchase?");
						
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								unlockDeletePub();
								
								AlertDialog.Builder builder = new AlertDialog.Builder(InAppActivity.this);
								builder.setMessage("Delete pub unlocked!");
								builder.setPositiveButton("OK", null);
								builder.setCancelable(false);
								builder.create().show();
							}
						});
						
						builder.setNegativeButton("No", null);
						builder.create().show();
					} else {
						mHelper.launchPurchaseFlow(InAppActivity.this, SKU_DELETE_PUB,
								REQUEST_CODE_PURCHASE, new OnIabPurchaseFinishedListener() {

							@Override
							public void onIabPurchaseFinished(IabResult result, Purchase info) {
								onPurchaseDeletePubFinished(result, info);
							}
						});
					}
				}
			});
			
		} catch (Exception e) {
			showError("Purchase failed");
		}
	}
	
	private void onPurchaseDeletePubFinished(IabResult result, Purchase info) {
		try {
			if (result.isFailure())
				throw new Exception();

			unlockDeletePub();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Delete pub unlocked!");
			builder.setPositiveButton("OK", null);
			builder.setCancelable(false);
			builder.create().show();
			
		} catch (Exception e) {
			showError("Purchase failed");
		}
	}
	
	private void unlockDeletePub() {
		Settings.instance().unlockPub = true;
		Settings.instance().save();
	}
	
	@Click(id = R.id.btnRestore)
	private void onRestoreClick(View v) {
		mHelper.queryInventoryAsync(new QueryInventoryFinishedListener() {

			@Override
			public void onQueryInventoryFinished(IabResult result, Inventory inv) {
				Purchase p1 = inv.getPurchase(SKU_4IMAGES);
				Purchase p2 = inv.getPurchase(SKU_DELETE_PUB);
				
				if (p1 == null && p2 == null) {
					showError("You have not purchased anything!");
					return;
				} else {
					if (p1 != null) {
						unlock4Image();
						showError("Restored Zoom + up to 4 images/Look");
					}
					
					if (p2 != null) {
						unlockDeletePub();
						showError("Restored Delete pub");
					}
				}
				
//				mHelper.consumeAsync(Arrays.asList(p1, p2), new OnConsumeMultiFinishedListener() {
//					@Override
//					public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {
//						showError("Remove purchase");
//					}
//				});
			}
		});
	}

	private void showError(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.setCancelable(false);
		builder.create().show();
    }

	@Override
	protected void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, Settings.API_FLURRY_KEY);
	}
	 
	@Override
	protected void onStop()
	{
		super.onStop();		
		FlurryAgent.onEndSession(this);
	}
}
