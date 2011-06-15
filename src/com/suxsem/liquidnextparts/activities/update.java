package com.suxsem.liquidnextparts.activities;


import java.lang.reflect.Array;

import com.suxsem.liquidnextparts.components.SmsLED_service;
import com.suxsem.liquidnextparts.components.Eula;
import com.suxsem.liquidnextparts.components.parsebuildprop;
import com.suxsem.liquidnextparts.BatteryLED;
import com.suxsem.liquidnextparts.LSystem;
import com.suxsem.liquidnextparts.LiquidSettings;
import com.suxsem.liquidnextparts.R;
import com.suxsem.liquidnextparts.SdCache;
import com.suxsem.liquidnextparts.Strings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class update extends PreferenceActivity { 
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 

		super.onCreate(savedInstanceState);
			
		addPreferencesFromResource(R.menu.menu); 
		final Context context = getApplicationContext();
		final CheckBoxPreference hf = (CheckBoxPreference)findPreference("hf");
		final EditTextPreference sdcache = (EditTextPreference)findPreference("sdcache");
		final CheckBoxPreference powerled = (CheckBoxPreference)findPreference("powerled");
		final CheckBoxPreference fixled = (CheckBoxPreference)findPreference("fixled");
		final Preference menu_info = findPreference("menu_info");
				
		hf.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {

				return true;
			}
		
		});
	}
}
