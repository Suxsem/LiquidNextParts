package com.suxsem.liquidnextparts.activities;

import com.suxsem.liquidnextparts.components.StartSystem;
import com.suxsem.liquidnextparts.BatteryLED;
import com.suxsem.liquidnextparts.DiskSpace;
import com.suxsem.liquidnextparts.LSystem;
import com.suxsem.liquidnextparts.LiquidSettings;
import com.suxsem.liquidnextparts.NetworkMode;
import com.suxsem.liquidnextparts.OTA_updates;
import com.suxsem.liquidnextparts.R;
import com.suxsem.liquidnextparts.SdCache;
import com.suxsem.liquidnextparts.Strings;
import com.suxsem.liquidnextparts.parsebuildprop;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
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
	public String noiseValue, sensitivityValue, softsensValue, hftimeValue;
	public int SDCacheSize;
	private settings myactivity = this;
	EditTextPreference editNoise, editSensitivity, editSoftsens, editHftime;
	public String DownloadTaskInformations = "";
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
		prefs = PreferenceManager.getDefaultSharedPreferences(myactivity);
		new StartSystem().startsystem(myactivity);
		addPreferencesFromResource(R.xml.menu);

		final Context context = getApplicationContext();
		final CheckBoxPreference hf = (CheckBoxPreference)findPreference("hf");
		final EditTextPreference sdcache = (EditTextPreference)findPreference("sdcache");
		final CheckBoxPreference powerled = (CheckBoxPreference)findPreference("powerled");
//		final CheckBoxPreference fixled = (CheckBoxPreference)findPreference("fixled");
		final CheckBoxPreference noprox = (CheckBoxPreference)findPreference("noprox");
		final CheckBoxPreference updateonstart = (CheckBoxPreference)findPreference("updateonstart");
		final Preference menu_info = findPreference("menu_info");
		final Preference diskspace = findPreference("diskspace");
		final Preference hotreboot = findPreference("hotreboot");
		final Preference forceupdate = findPreference("forceupdate");
		final Preference donateclick = findPreference("donateclick");
		final Preference v6scripttweaker = findPreference("v6scripttweaker");
		final Preference reportissue = findPreference("reportissue");
		final ListPreference networkmode = (ListPreference)findPreference("2g3gmode");
		final Preference resetall = findPreference("resetall");
		
		//noprox.setChecked((parsebuildprop.parseInt("hw.acer.psensor_calib_min_base")==32717));
		editNoise = (EditTextPreference)findPreference("noise");
		editSensitivity = (EditTextPreference)findPreference("sensitivity");
		editSoftsens = (EditTextPreference)findPreference("softsens");
		editHftime = (EditTextPreference)findPreference("hftime");
		
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
		hftimeValue = editHftime.getText();

		updateValues();

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
						LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue, softsensValue, hftimeValue)+" > /system/etc/init.d/06sensitivity");
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
						LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue, softsensValue, hftimeValue)+" > /system/etc/init.d/06sensitivity");
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
						LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue, softsensValue, hftimeValue)+" > /system/etc/init.d/06sensitivity");
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
		
		editHftime.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (!Strings.onlyNumber(newValue.toString())){
					Toast.makeText(context, "You must enter a numeric value!", 4000).show();
					return false;
				} //Check if the value is numeric, THEN assign it to sensitivityValue
				hftimeValue = newValue.toString();
				int hftimeValueInt = Integer.parseInt(hftimeValue);
				if(hftimeValueInt < (10))
					hftimeValue = ("10");
				else if (hftimeValueInt>(2000))
					hftimeValue=("2000");

				if(ROOT) {
					if(ROOT && LSystem.RemountRW()) {
						LiquidSettings.runRootCommand("echo "+Strings.getSens(sensitivityValue, noiseValue, softsensValue, hftimeValue)+" > /system/etc/init.d/06sensitivity");
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
		
		networkmode.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				NetworkMode.switchnetworkmode(myactivity);
				return true;
			}
		});
		
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

		v6scripttweaker.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
					try {
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.setComponent(new ComponentName("jackpal.androidterm2", "jackpal.androidterm2.Term"));
						intent.putExtra("jackpal.androidterm.iInitialCommand", "su \r sh /system/xbin/V6SuperChargerLN.sh");
						startActivity(intent);
					} catch (Exception e) {
						Toast.makeText(myactivity, "No terminal emulator app found", 4000).show();
					}					
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

		hotreboot.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				ProgressDialog.show(myactivity, "", 
						"Rebooting...", true);
				LiquidSettings.runRootCommand("killall system_server");
				return true;
			}
		});
		
		donateclick.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				ConnectivityManager connManager =  (ConnectivityManager)myactivity.getSystemService(Context.CONNECTIVITY_SERVICE);
				android.net.NetworkInfo netInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				android.net.NetworkInfo wifiInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (netInfo.getState() == android.net.NetworkInfo.State.CONNECTED ||
						wifiInfo.getState() == android.net.NetworkInfo.State.CONNECTED  ) {			
				Intent myintent = new Intent (Intent.ACTION_VIEW);
				myintent.setClassName(context, Webview.class.getName());
				startActivity(myintent);
				}else{
					Toast.makeText(myactivity, "ERROR: NO CONNECTIONS!", 4000).show();
				}
				return true;
			}
		});

		forceupdate.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				//checkupdates();
				new OTA_updates().checkupdates(myactivity, myactivity);
				return true;
			}
		});
		
		updateonstart.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				if (!updateonstart.isChecked()) {
					parsebuildprop.editString("hw.acer.psensor_calib_min_base", "32717");
					AlertDialog.Builder builder = new AlertDialog.Builder(myactivity);
					builder.setTitle("Are you sure?");
					builder.setCancelable(true);         
					builder.setMessage("It's very important to have the latest available updates! Please check this option only if you have problems launching app.");
					builder.setPositiveButton("Undo", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Editor editor = prefs.edit();
							editor.putBoolean("updateonstart",true);
							updateonstart.setChecked(true);
							editor.commit();
						}
					});
					builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {                    	
						}
					});
					builder.create().show();
				}									
				return true;
			}
		});

		
		reportissue.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {				
				ConnectivityManager connManager =  (ConnectivityManager)myactivity.getSystemService(Context.CONNECTIVITY_SERVICE);
				android.net.NetworkInfo netInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				android.net.NetworkInfo wifiInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (netInfo.getState() == android.net.NetworkInfo.State.CONNECTED ||
						wifiInfo.getState() == android.net.NetworkInfo.State.CONNECTED  ) {
					ProgressDialog.show(myactivity, "Report an issue", 
							"Loading issues list...", true);
					Intent myintent = new Intent (Intent.ACTION_VIEW);
					myintent.setClassName(myactivity, ReportIssue.class.getName());
					startActivity(myintent);
				}else{
					Toast.makeText(myactivity, "ERROR: NO CONNECTIONS!", 4000).show();
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
		resetall.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Editor editor = prefs.edit();
				editor.putBoolean("firststart", true);
				editor.commit();
				new StartSystem().startsystem(myactivity);
				return true;
			}
		});

		
//		fixled.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//			public boolean onPreferenceClick(Preference preference) {
//				return true;
//			}
//		});
		
		if(prefs.getBoolean("updateonstart", true)){
			//checkupdates();
			new OTA_updates().checkupdates(myactivity,myactivity);
		}		
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
		editHftime.setSummary("Softkey vibration time is set to "+ hftimeValue +"ms");
	}
	   		               
}


