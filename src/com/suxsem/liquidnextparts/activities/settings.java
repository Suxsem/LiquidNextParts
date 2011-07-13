package com.suxsem.liquidnextparts.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.suxsem.liquidnextparts.components.DownloadTask;
import com.suxsem.liquidnextparts.components.StartSystem;
import com.suxsem.liquidnextparts.components.parsebuildprop;
import com.suxsem.liquidnextparts.BatteryLED;
import com.suxsem.liquidnextparts.DiskSpace;
import com.suxsem.liquidnextparts.LSystem;
import com.suxsem.liquidnextparts.LiquidSettings;
import com.suxsem.liquidnextparts.NetworkMode;
import com.suxsem.liquidnextparts.R;
import com.suxsem.liquidnextparts.SdCache;
import com.suxsem.liquidnextparts.Strings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class settings extends PreferenceActivity { 

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.inflatedmenu, menu);
		return true;
	}
	
	@Override
	public void onStop() {
		super.onStop();
		this.finish();
		return;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_help:
			showhelp();
			return true;
		case R.id.menu_close:
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public boolean ROOT = false;
	public boolean isFirstTime = false;
	public SharedPreferences prefs;
	public String noiseValue, sensitivityValue, softsensValue;
	public int SDCacheSize;
	private settings myactivity = this;
	EditTextPreference editNoise, editSensitivity, editSoftsens;
	public String DownloadTaskInformations = "";
	private ProgressDialog waitdialog;
	Boolean update = false;
	Boolean connection = false;

	@Override
	public void onCreate(Bundle savedInstanceState) { 

		super.onCreate(savedInstanceState);
				
		if (!LSystem.checkInitFolder()){
			Toast.makeText(this, "Can't make init.d folder, your system must be rooted", 4000).show();
			this.finish(); //Exit app
		}
		ROOT = LiquidSettings.isRoot();	
		new StartSystem().startsystem(true, myactivity);    
		addPreferencesFromResource(R.menu.menu);

		final Context context = getApplicationContext();
		final CheckBoxPreference hf = (CheckBoxPreference)findPreference("hf");
		final EditTextPreference sdcache = (EditTextPreference)findPreference("sdcache");
		final CheckBoxPreference powerled = (CheckBoxPreference)findPreference("powerled");
//		final CheckBoxPreference fixled = (CheckBoxPreference)findPreference("fixled");
		final CheckBoxPreference noprox = (CheckBoxPreference)findPreference("noprox");
		final Preference menu_info = findPreference("menu_info");
		final Preference diskspace = findPreference("diskspace");
		final Preference hotreboot = findPreference("hotreboot");
		final Preference forceupdate = findPreference("forceupdate");
		final Preference donateclick = findPreference("donateclick");
		final ListPreference networkmode = (ListPreference)findPreference("2g3gmode");
		
		noprox.setChecked((parsebuildprop.parseInt("hw.acer.psensor_calib_min_base")==32717));
		editNoise = (EditTextPreference)findPreference("noise");
		editSensitivity = (EditTextPreference)findPreference("sensitivity");
		editSoftsens = (EditTextPreference)findPreference("softsens");
		
		if (!LSystem.hapticAvailable())
			hf.setEnabled(false);
		else
			hf.setChecked(LSystem.vibrStatus());


		if (!SdCache.isCachePathAvailable())
			sdcache.setEnabled(false);
		if ((SDCacheSize=SdCache.getSdCacheSize()) >= 128){
			sdcache.setText(Integer.toString(SDCacheSize));
		}

		noiseValue = editNoise.getText();
		sensitivityValue = editSensitivity.getText();
		softsensValue = editSoftsens.getText();

		updateValues();

		hf.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				if(ROOT){
					if(LSystem.RemountRW()) {
						LiquidSettings.runRootCommand("echo " + ((hf.isChecked()) ? Strings.getvibr() : Strings.getnovibr()) + " > /system/etc/init.d/06vibrate");
						LiquidSettings.runRootCommand("echo " + ((hf.isChecked()==true) ? "1": "0") +" > /sys/module/avr/parameters/vibr");
						LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06vibrate");
						LSystem.RemountROnly();
						Toast.makeText(context, "Haptic set on " + Boolean.toString(hf.isChecked()), 4000).show();
					} else {
						Toast.makeText(context, "Error: unable to mount partition", 4000).show();
						hf.setChecked(false);
					}
				} else {
					Toast.makeText(context, "Sorry, you need ROOT permissions.", 4000).show();
					hf.setChecked(false);
				}
				return true;
			}

		});

		editNoise.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (!Strings.onlyNumber(newValue.toString())){
					Toast.makeText(context, "You must enter a numeric value!", 4000).show();
					return false;
				} //Check if the value is numeric, THEN assign it to noiseValue
				noiseValue = newValue.toString();
				int noiseValueInt = Integer.parseInt(noiseValue);
				if(noiseValueInt < 20) 
					noiseValue = "20";
				else if(noiseValueInt > 75) 
					noiseValue = "75";

				if(ROOT) {
					if(ROOT && LSystem.RemountRW()) {
						LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue, softsensValue)+" > /system/etc/init.d/06sensitivity");
						LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06sensitivity");
						LSystem.RemountROnly();
						if (LiquidSettings.runRootCommand("./system/etc/init.d/06sensitivity"))
							Toast.makeText(context, "Sensitivity set correctly", 4000).show();
						else 
							Toast.makeText(context, "Error, unable to set noise", 4000).show();
					}
					updateValues();
				} else {
					Toast.makeText(context, "Sorry, you need ROOT permissions.", 4000).show();
				}
				return true;
			}
		});


		editSensitivity.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (!Strings.onlyNumber(newValue.toString())){
					Toast.makeText(context, "You must enter a numeric value!", 4000).show();
					return false;
				} //Check if the value is numeric, THEN assign it to sensitivityValue
				sensitivityValue = newValue.toString();
				int sensitivityValueInt = Integer.parseInt(sensitivityValue);
				if(sensitivityValueInt < (20))
					sensitivityValue = ("20");
				else if (sensitivityValueInt>(75))
					sensitivityValue=("75");

				if(ROOT) {
					if(ROOT && LSystem.RemountRW()) {
						LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue, softsensValue)+" > /system/etc/init.d/06sensitivity");
						LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06sensitivity");
						LSystem.RemountROnly();
						if (LiquidSettings.runRootCommand("./system/etc/init.d/06sensitivity"))
							Toast.makeText(context, "Sensitivity set correctly", 4000).show();
						else 
							Toast.makeText(context, "Error, unable to set noise", 4000).show();
					}
					updateValues();
				} else {
					Toast.makeText(context, "Sorry, you need ROOT permissions.", 4000).show();
				}
				return true;
			}
		});

		editSoftsens.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (!Strings.onlyNumber(newValue.toString())){
					Toast.makeText(context, "You must enter a numeric value!", 4000).show();
					return false;
				} //Check if the value is numeric, THEN assign it to sensitivityValue
				softsensValue = newValue.toString();
				int softsensValueInt = Integer.parseInt(softsensValue);
				if(softsensValueInt < (15))
					softsensValue = ("15");
				else if (softsensValueInt>(30))
					softsensValue=("30");

				if(ROOT) {
					if(ROOT && LSystem.RemountRW()) {
						LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue, softsensValue)+" > /system/etc/init.d/06sensitivity");
						LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06sensitivity");
						LSystem.RemountROnly();
						if (LiquidSettings.runRootCommand("./system/etc/init.d/06sensitivity"))
							Toast.makeText(context, "Sensitivity set correctly", 4000).show();
						else 
							Toast.makeText(context, "Error, unable to set noise", 4000).show();
					}
					updateValues();
				} else {
					Toast.makeText(context, "Sorry, you need ROOT permissions.", 4000).show();
				}
				return true;
			}
		});
		
		menu_info.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				showhelp();
				return true;
			}
		});

		donateclick.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent myintent = new Intent (Intent.ACTION_VIEW);
				myintent.setClassName(context, Webview.class.getName());
				startActivity(myintent);
				return true;
			}
		});
		
		diskspace.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(myactivity);
				builder.setTitle("DiskSpace");
				builder.setCancelable(true);         
				builder.setMessage(DiskSpace.getdiskspace());
				builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {                    	
					}
				});
				builder.create().show();
				return true;
			}
		});

		networkmode.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				NetworkMode.switchnetworkmode(myactivity);
				return true;
			}
		});

		hotreboot.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				LiquidSettings.runRootCommand("killall system_server");
				return true;
			}
		});

		forceupdate.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				checkupdates();
				return true;
			}
		});

		sdcache.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (!Strings.onlyNumber(newValue.toString())){
					Toast.makeText(context, "You must enter a numeric value!", 4000).show();
					return false;
				} //Check if the value is numeric, THEN assign it to sensitivityValue
				String newValueString = newValue.toString();
				int newValueInt = Integer.parseInt(newValueString);
				if(newValueInt < 128)
					newValueInt = 128;
				else if (newValueInt > 4096)
					newValueInt = 4096;
				if (ROOT){
					if (SdCache.setSDCache(newValueInt)){
						Toast.makeText(context, "SD cache size set to " + newValueInt, 4000).show();
						return true;
					}else{
						Toast.makeText(context, "Error while setting SD Cache", 4000).show();
						return false;
					}
				} else 
					Toast.makeText(context, "Sorry you need root permissions", 4000).show();
				return false;
			}
		});

		powerled.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				if (ROOT){
					if (powerled.isChecked()) {						
						LiquidSettings.runRootCommand("echo '0' > /sys/class/leds2/power");
						LiquidSettings.runRootCommand("chmod 000 /sys/class/leds2/power");
					}else{
						LiquidSettings.runRootCommand("chmod 222 /sys/class/leds2/power");
					}
					if (BatteryLED.setdisable(powerled.isChecked())){						
						return true;
					} else{
						Toast.makeText(context, "Error while set Power LED disable", 4000).show();
						return false;
					}
				}else {
					Toast.makeText(context, "Sorry, you need ROOT permissions", 4000).show();
					return false;
				}	
			}
		});

//		fixled.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//			public boolean onPreferenceClick(Preference preference) {
//				return true;
//			}
//		});
		
		noprox.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				if (noprox.isChecked()) {
					parsebuildprop.editString("hw.acer.psensor_calib_min_base", "32717");		    	
				}else{
					parsebuildprop.editString("hw.acer.psensor_calib_min_base", "32716");
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(myactivity);
				builder.setTitle("Reboot required");
				builder.setCancelable(true);         
				builder.setMessage("This option requires a reboot to be applied. Do you want to reboot now?");
				builder.setPositiveButton("Reboot", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						LiquidSettings.runRootCommand("reboot");
					}
				});
				builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {                    	
					}
				});
				builder.create().show();    							
				return true;
			}
		});
		checkupdates();
	}
	
	private void showhelp(){
		Intent myintent = new Intent (Intent.ACTION_VIEW);
		myintent.setClassName(myactivity, InfoPreferenceActivity.class.getName());
		startActivity(myintent);	
	}



	private void updateValues() {
		editNoise.setSummary("Noise is set to " + noiseValue);
		editSensitivity.setSummary("Sensitivity is set to " + sensitivityValue);		
		editSoftsens.setSummary("Softkey sensitivity is set to "+ softsensValue);
	}

	private boolean updaterom(){
		String romodversion = parsebuildprop.parseString("ro.modversion");
		String lastversion = getString(R.string.lastversion);
		String[] romodversioncheckarray;
		try {
			romodversioncheckarray = romodversion.split("[\\s,]+")[1].split("\\.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(myactivity, "ERROR in build.prop > ro.modversion", 4000).show();
			return false;
		}
		String[] lastversioncheckarray = lastversion.split("[\\s,]+")[1].split("\\.");
		Integer romodversioncheckarraynumber = 0;
		Integer lastversioncheckarraynumber = 0;
		for (int i = 0; i < romodversioncheckarray.length; i++){
			romodversioncheckarraynumber = romodversioncheckarraynumber + (int) Math.pow(10, 3-i) * Integer.valueOf(romodversioncheckarray[i]);
		}
		for (int i = 0; i < lastversioncheckarray.length; i++){
			lastversioncheckarraynumber = lastversioncheckarraynumber + (int) Math.pow(10, 3-i) * Integer.valueOf(lastversioncheckarray[i]);
		}
		if(lastversioncheckarraynumber > romodversioncheckarraynumber){
			boolean possibleupdate = false;
			if(getString(R.string.previousversion).equals("ALL")){
				possibleupdate=true;
			}else{
				String[] previousversionarray = getString(R.string.previousversion).split("/");    		
				for (int i = 0; i < previousversionarray.length; i++){
					if(romodversion.split("[\\s,]+")[1].equals(previousversionarray[i]))possibleupdate=true;
				}
			}

			if(possibleupdate){


				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("NEW ROM UPDATE");
				builder.setCancelable(true);         
				builder.setMessage("There is a new update for your ROM!\n\nVersion: "+getString(R.string.lastversion)+"\nSize: "+getString(R.string.size)+" MB\nNotes: "+getString(R.string.notes));
				builder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						ConnectivityManager connManager =  (ConnectivityManager)myactivity.getSystemService(Context.CONNECTIVITY_SERVICE);
						android.net.NetworkInfo netInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
						android.net.NetworkInfo wifiInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
						if (netInfo.getState() == android.net.NetworkInfo.State.CONNECTED ||
								wifiInfo.getState() == android.net.NetworkInfo.State.CONNECTED  ) {
							final String actualfilename = getString(R.string.lastversion).replaceAll(" ", "_");
							
							
							final CharSequence[] items = {"Save as /sdcard/LiquidNext_LastUpdate", "Save as /sdcard/"+actualfilename,"Save as /sdcard/update"};

							AlertDialog.Builder builder = new AlertDialog.Builder(myactivity);
							builder.setTitle("Choose download location");
							builder.setItems(items, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int item) {
									if (item == 0){
										DownloadTaskInformations = DownloadTaskInformations + "/sdcard/LiquidNext_LastUpdate.zip";
									}else if (item == 1){
										DownloadTaskInformations = DownloadTaskInformations + "/sdcard/"+actualfilename+".zip";
									}else if (item == 2){
										DownloadTaskInformations = DownloadTaskInformations + "/sdcard/update.zip";
									}
									
									final CharSequence[] items = {"Automatically flash after download", "Reboot in recovery after download", "Don't reboot in recovery after download"};

									AlertDialog.Builder builder = new AlertDialog.Builder(myactivity);
									builder.setTitle("Choose action after download");
									builder.setItems(items, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int item) {
											if (item == 0){
												DownloadTaskInformations = DownloadTaskInformations + "#r1";
											}else if (item == 1){
												DownloadTaskInformations = DownloadTaskInformations + "#r2";
											}else if (item == 2){
												DownloadTaskInformations = DownloadTaskInformations + "#r3";
											}

											DownloadTask.downloadtask = new DownloadTask(myactivity).execute(myactivity.getString(R.string.url), DownloadTaskInformations);
											startActivity(new Intent (Intent.ACTION_VIEW).setClassName(myactivity, Webview.class.getName()));
										}
									});
									builder.create().show();
									
									
								}
							});
							builder.create().show();							
						}else{
							Toast.makeText(myactivity, "ERROR: NO CONNECTIONS!", 4000).show();
						}
					}
				});
				builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {                    	
					}
				});
				builder.create().show();    			
			}else{
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("NEW ROM UPDATE");
				builder.setCancelable(true);         
				builder.setMessage("There is a new update, but your ROM is too old to be automatically updated. Please visit Modaco website to get the last LiquidNext ROM available!");
				builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {                    	
					}
				});
				builder.create().show();
			}
			return true;


		}
		return false;    	    	
	}
	public void checkupdates(){

		waitdialog = ProgressDialog.show(myactivity, "", 
				"Checking for updates...", true);

		new Thread()
		{
			public void run() 
			{

				try
				{
					ConnectivityManager connManager =  (ConnectivityManager)myactivity.getSystemService(Context.CONNECTIVITY_SERVICE);
					android.net.NetworkInfo netInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
					android.net.NetworkInfo wifiInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					if (netInfo.getState() == android.net.NetworkInfo.State.CONNECTED ||
							wifiInfo.getState() == android.net.NetworkInfo.State.CONNECTED  ) {
						connection = true;
						HttpClient httpClient = new DefaultHttpClient();
						HttpContext localContext = new BasicHttpContext();
						HttpGet httpGet = new HttpGet("http://liquidnext.uphero.com/version");
						HttpResponse response = null;
						try {
							response = httpClient.execute(httpGet, localContext);
						} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						BufferedReader reader = null;
						try {
							reader = new BufferedReader(
									new InputStreamReader(
											response.getEntity().getContent()
									)
							);
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						String line = null;
						update = false;
						try {
							while ((line = reader.readLine()) != null){		     	  			  
									if(!getString(R.string.app_vname).equals(line.substring(0,5))){
										update = true;
										Intent intent = new Intent(Intent.ACTION_VIEW);
										intent.setData(Uri.parse("market://details?id=com.suxsem.liquidnextparts"));
										startActivity(intent);
										line = null;
									}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						connection = false;
					}
					runOnUiThread(new Runnable() {
						public void run() {
							if(update==false){	        			
								if(!updaterom()){
									if(connection){
										Toast.makeText(myactivity, "No new ROM updates", 4000).show();
									}else{
										Toast.makeText(myactivity, "No Internet connection", 4000).show();	                
									}
								}
							}		
						}
					});        	
				}
				catch (Exception e)
				{
				}
				// dismiss the progressdialog   
				waitdialog.dismiss();
			}
		}.start();		   
	}		               
}


