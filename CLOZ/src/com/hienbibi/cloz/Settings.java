package com.hienbibi.cloz;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

	public static final String SHARED_PREFERENCE_NAME = "SETTINGS";

	private static Settings sInstance;
	private static Context mContext;

	public boolean firstTime;
	public boolean autoBackup;
	public int numQuery;
	public boolean unlockZoom;
	public boolean unlockPub;
	
	public boolean hasSecondLook;

	public static void init(Context context) {
		if (sInstance == null) {
			mContext = context;
			sInstance = new Settings();
			sInstance.load();
		}
	}

	public static void release() {
		mContext = null;
		sInstance = null;
	}

	public static Settings instance() {	
		return sInstance;
	}

	public void save() {
		SharedPreferences.Editor editor = 
				mContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE).edit();

		editor.putBoolean("firstTime", firstTime);
		editor.putBoolean("autoBackup", autoBackup);
		editor.putInt("numQuery", numQuery);
		editor.putBoolean("unlockZoom", unlockZoom);
		editor.putBoolean("unlockPub", unlockPub);
		editor.putBoolean("hasSecondLook", hasSecondLook);

		editor.commit();
	}
	
	public void load() {
		SharedPreferences preference = 
				mContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);

		firstTime		= preference.getBoolean("firstTime", true);
		autoBackup		= preference.getBoolean("autoBackup", false);
		numQuery		= preference.getInt("numQuery", 0);
		unlockZoom		= preference.getBoolean("unlockZoom", false);
		unlockPub		= preference.getBoolean("unlockPub", false);
		hasSecondLook	= preference.getBoolean("hasSecondLook", false);
	}
}