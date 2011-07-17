package com.suxsem.liquidnextparts.activities;

import com.suxsem.liquidnextparts.LiquidSettings;
import com.suxsem.liquidnextparts.R;
import com.suxsem.liquidnextparts.components.DownloadTask;
import com.suxsem.liquidnextparts.components.NotificationHelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class OTA_updates_status extends Activity{
	private OTA_updates_status myactivity;
	public static ProgressBar progressbar;
	public static TextView infoprogressbar;
	@Override
	public void onStop() {
		super.onStop();
		this.finish();
		return;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
			setContentView(R.layout.ota_updates_status_layout);
			myactivity = this;
			
			
			Button Cancel = (Button)findViewById(R.id.button1);
			Button CancelAndDelete = (Button)findViewById(R.id.button2);
			Button Close = (Button)findViewById(R.id.button3);
			progressbar = (ProgressBar)findViewById(R.id.progressBar1);
			progressbar.setMax(100);
			infoprogressbar = (TextView)findViewById(R.id.textView1);
			
			progressbar.setProgress(DownloadTask.previousperc);
			infoprogressbar.setText("Downloaded " + (int)(DownloadTask.downloaded/1000000) +" MB of " + (int)(DownloadTask.fileSize/1000000) + " MB ("+DownloadTask.previousperc+"%)");
			
			Cancel.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					NotificationHelper.closeclass();
					myactivity.finish();
				}
			});
			CancelAndDelete.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					NotificationHelper.closeclass();
					final String actualfilename = myactivity.getString(R.string.lastversion).replaceAll(" ", "_");
					LiquidSettings.runRootCommand("rm -f /sdcard/"+actualfilename+".PARTIALDOWNLOAD");					
					myactivity.finish();
				}
			});
			Close.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					myactivity.finish();
				}
			});
    }
}

