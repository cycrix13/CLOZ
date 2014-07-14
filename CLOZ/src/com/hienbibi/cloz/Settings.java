package com.hienbibi.cloz;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

	public static final String SHARED_PREFERENCE_NAME = "SETTINGS";

	private static Settings sInstance;
	private static Context mContext;

	public boolean firstTime;

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
//		editor.putString("tokenID", getAccessToken());
//		editor.putString("refreshID", getRefreshToken());
//		editor.putString("activateID", activateID);
//		editor.putString("userID", userID);
//		editor.putString("phoneNumber", phoneNumber);
//		editor.putString("avatar", avatar);
//		editor.putString("name", name);
//
//		editor.putString("fb_id", fb_id);
//		editor.putString("fb_access_token", fb_access_token);
//		editor.putString("email", email);
//		editor.putInt("gender", gender);
//		editor.putString("dob", dob);
//		editor.putString("job", job);
//
//		editor.putString("cityId", cityId);
//
//		editor.putString("cover", cover);
//		editor.putInt("socialType", socialType);
//		editor.putBoolean("firstTime", firstTime);
//		editor.putString("server", server);
//		
//		editor.putFloat("lat", lat);
//		editor.putFloat("lng", lng);
//		editor.putLong("time", time);

		editor.commit();
	}
	
	public void load() {
		SharedPreferences preference = 
				mContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);

		firstTime		= preference.getBoolean("firstTime", true);
//		setAccessToken(preference.getString("tokenID", "abc"), preference.getString("refreshID", ""));
//		activateID 		= preference.getString("activateID", "");
//		userID 			= preference.getString("userID", "");
//		phoneNumber 	= preference.getString("phoneNumber", "");
//		avatar 			= preference.getString("avatar", "");
//		name			= preference.getString("name", "");
//
//		fb_id 			= preference.getString("fb_id", "");
//		fb_access_token = preference.getString("fb_access_token", "");
//		email 			= preference.getString("email", "");
//		gender 			= preference.getInt("gender", -1);
//		dob 			= preference.getString("dob", "");
//		job 			= preference.getString("job", "");
//
//		cityId 			= preference.getString("cityId", "1");
//
//		cover 			= preference.getString("cover", "");
//		socialType		= preference.getInt("socialType", 0);
//		firstTime		= preference.getBoolean("firstTime", true);
//		
//		server 			= preference.getString("server", "https://api.infory.vn");
//		
//		lat				= preference.getFloat("lat", -1);
//		lng				= preference.getFloat("lng", -1);
//		lng				= preference.getFloat("lng", -1);
//		time			= preference.getLong("time", 0);
	}
}