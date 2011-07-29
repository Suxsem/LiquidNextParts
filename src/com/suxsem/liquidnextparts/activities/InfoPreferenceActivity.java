package com.suxsem.liquidnextparts.activities;



import com.suxsem.liquidnextparts.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class InfoPreferenceActivity extends PreferenceActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.menu_info);
	}
	@Override
	public void onStop() {
		super.onStop();
		this.finish();
		return;
	}
	@Override
	public void onBackPressed() {
		Intent myintent = new Intent (Intent.ACTION_VIEW);
		myintent.setClassName(this.getBaseContext(), settings.class.getName());
		startActivity(myintent);
	this.finish();
	return;
	}
	
}
