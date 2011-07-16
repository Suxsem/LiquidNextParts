package com.suxsem.liquidnextparts.components;

import android.app.Activity;
import android.os.Bundle;

public class NotificationHelperStopProcess extends Activity{
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.stopdownload);
			NotificationHelper.closeclass();
			this.finish();
    }
}

