package com.kt.SmartStamp.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
	private final String PREFERENCE_NAME = "SMART_STAMP_PREFERENCE";

	private final String KEY_NAME_AUTH_KEY = "AUTH_KEY";
	private final String KEY_NAME_MEM_INDEX = "MEM_INDEX";
	private final String KEY_NAME_MEM_NAME = "MEM_NAME";
	private final String KEY_NAME_UT_NAME = "UT_NAME";
	private final String KEY_NAME_CONT_N_CNT = "CONT_N_CNT";
	private final String KEY_NAME_CONT_R_CNT = "CONT_R_CNT";
	private final String KEY_NAME_CONT_Y_CNT = "CONT_Y_CNT";

	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;

	public SessionManager(Context context) {
		sharedPreferences = context.getSharedPreferences( PREFERENCE_NAME, Activity.MODE_PRIVATE );
		editor = sharedPreferences.edit();
		editor.commit();
	}

	public void setAuthKey(String authKey) {
		editor.putString( KEY_NAME_AUTH_KEY, authKey );
		editor.commit();
	}

	public String getAuthKey() {
		return sharedPreferences.getString(KEY_NAME_AUTH_KEY, "");
	}

	public void setMemIdx(String memIdx) {
		editor.putString( KEY_NAME_MEM_INDEX, memIdx );
		editor.commit();
	}

	public String getMemIdx() {
		return sharedPreferences.getString(KEY_NAME_MEM_INDEX, "");
	}


	public void setMemName(String memName) {
		editor.putString( KEY_NAME_MEM_NAME, memName );
		editor.commit();
	}

	public String getMemName() {
		return sharedPreferences.getString(KEY_NAME_MEM_NAME, "");
	}

	public void setUtName(String utName) {
		editor.putString( KEY_NAME_UT_NAME, utName );
		editor.commit();
	}

	public String getUtName() {
		return sharedPreferences.getString(KEY_NAME_UT_NAME, "");
	}

}
