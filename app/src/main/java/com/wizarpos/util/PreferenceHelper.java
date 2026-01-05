
package com.wizarpos.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceHelper {

    private static PreferenceHelper instance;

    public static PreferenceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceHelper(context);
        }
        return instance;
    }

    private SharedPreferences preferences;

    private Context context;

    private PreferenceHelper(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private String getString(int id) {
        return context.getResources().getString(id);
    }
    
    public void putBooleanValue(String key, boolean value){
        Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    
    public boolean getBooleanValue(String key){
        return preferences.getBoolean(key, false);
    }
    
    public String getStringValue(String key){
        return preferences.getString(key, "");
    }

    public int getIntValue(String key){
        return preferences.getInt(key, 0);
    }
	
	public void setValue(String key , boolean value){
		Editor edit = preferences.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}

    public void setStringValue(String key, String value) {
        Editor edit = preferences.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public void setIntValue(String key, int value) {
        Editor edit = preferences.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public void setBooleanValue(String key, boolean value) {
        Editor edit = preferences.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

}
