package com.suxsem.liquidnextparts.components;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.suxsem.liquidnextparts.R;
import com.suxsem.liquidnextparts.components.DownloadTask;

public class NotificationHelperStopProcess extends Activity{
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.stopdownload);
			DownloadTask.downloadtask.cancel(true);
			

			this.finish();
    }
}
