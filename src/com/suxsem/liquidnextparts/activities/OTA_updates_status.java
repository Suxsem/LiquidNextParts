package com.suxsem.liquidnextparts.activities;

import java.io.File;

import com.suxsem.liquidnextparts.LiquidSettings;
import com.suxsem.liquidnextparts.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class OTA_updates_status extends Activity{
	private OTA_updates_status myactivity;
	private SharedPreferences prefs;
	private DownloadManager dm;
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
			AlertDialog.Builder builder = new AlertDialog.Builder(myactivity);
			builder.setTitle("LiquidNext OTA");
			builder.setCancelable(false);
			
    		prefs = PreferenceManager.getDefaultSharedPreferences(myactivity);
    		dm = (DownloadManager) myactivity.getSystemService(Context.DOWNLOAD_SERVICE);
    		
    	    /*Cursor c=dm.query(new DownloadManager.Query().setFilterById(prefs.getLong("otadownloadid",-1)));
    	    Log.d("LNP", Long.toString(prefs.getLong("otadownloadid",-1)));
    	    if(c.getColumnIndex(DownloadManager.COLUMN_STATUS)==16){
			builder.setMessage("Failed to download updates...\nDo you want to remove broken downloaded file?");
			builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					File localfile = new File("/sdcard/"+prefs.getString("otadownloadlocation", ""));
	                if(localfile.exists()){
									LiquidSettings.runRootCommand("rm -f "+"/sdcard/"+prefs.getString("otadownloadlocation", ""));										
	                }
				}
			});
    	    }
    	    
    	    if(c.getColumnIndex(DownloadManager.COLUMN_STATUS)==16){
			builder.setMessage("Failed to download updates...\nDo you want to remove broken downloaded file?");
			builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					File localfile = new File("/sdcard/"+prefs.getString("otadownloadlocation", ""));
	                if(localfile.exists()){
									LiquidSettings.runRootCommand("rm -f "+"/sdcard/"+prefs.getString("otadownloadlocation", ""));										
	                }
				}
			});
    	    }
    	    
    	    if(c.getColumnIndex(DownloadManager.COLUMN_STATUS)==4){
			builder.setMessage("Trying to resume download...\nDo you want to stop download and remove broken downloaded file?");
			builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dm.remove(prefs.getLong("otadownloadid",-1));
					File localfile = new File("/sdcard/"+prefs.getString("otadownloadlocation", ""));
	                if(localfile.exists()){	                				
									LiquidSettings.runRootCommand("rm -f "+"/sdcard/"+prefs.getString("otadownloadlocation", ""));										
	                }
				}
			});
    	    }
			
    	    if(c.getColumnIndex(DownloadManager.COLUMN_STATUS)==1){
			builder.setMessage("Starting download...\nDo you want to stop download and remove broken downloaded file?");
			builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dm.remove(prefs.getLong("otadownloadid",-1));
					File localfile = new File("/sdcard/"+prefs.getString("otadownloadlocation", ""));
	                if(localfile.exists()){	                				
	                	LiquidSettings.runRootCommand("reboot recovery");										
	                }
				}
			});
    	    }
    	    
    	    if(c.getColumnIndex(DownloadManager.COLUMN_STATUS)==2){
			builder.setMessage("Download in progress...\nDo you want to stop download and remove broken downloaded file?");
			builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dm.remove(prefs.getLong("otadownloadid",-1));
					File localfile = new File("/sdcard/"+prefs.getString("otadownloadlocation", ""));
	                if(localfile.exists()){	                				
									LiquidSettings.runRootCommand("rm -f "+"/sdcard/"+prefs.getString("otadownloadlocation", ""));										
	                }
				}
			});
    	    }    	
    	    */    
    	    
			builder.setMessage("Download in progress...\nDo you want to stop download and remove broken downloaded file?");
			builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					try {
						dm.remove(prefs.getLong("otadownloadid",-1));
					} catch (Exception e) {
					}
					File localfile = new File("/sdcard/"+prefs.getString("otadownloadlocation", ""));
	                if(localfile.exists()){	                				
									LiquidSettings.runRootCommand("rm -f "+"/sdcard/"+prefs.getString("otadownloadlocation", ""));										
	                }
	                myactivity.finish();
				}
			});    	    
    	    
			builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					myactivity.finish();
				}
			});
			builder.create().show(); 
			
    }
}

