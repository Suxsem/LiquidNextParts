package com.suxsem.liquidnextparts.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.suxsem.liquidnextparts.R;

public class Undervolt extends Activity { 

	Undervolt myactivity = null;
	
	@Override
	public void onBackPressed() {
		Intent myintent = new Intent (Intent.ACTION_VIEW);
		myintent.setClassName(this.getBaseContext(), settings.class.getName());
		startActivity(myintent);
		this.finish();
	return;
	}
	
	@Override
	public void onStop() {
		super.onStop();
		this.finish();
		return;
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.undervolt_layout);
		
		
		myactivity = this;
				
		}
}