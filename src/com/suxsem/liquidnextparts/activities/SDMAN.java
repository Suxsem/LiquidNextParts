package com.suxsem.liquidnextparts.activities;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.suxsem.liquidnextparts.LiquidSettings;
import com.suxsem.liquidnextparts.R;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

public class SDMAN extends PreferenceActivity {
	
	private CheckBoxPreference sdman_app;
	private CheckBoxPreference sdman_data;
	private CheckBoxPreference sdman_dalvik;
	private CheckBoxPreference sdman_download;
	private CheckBoxPreference sdman_swap;
	private Preference sdman_sdext_recovery;
	private Preference sdman_swap_recovery;
	private EditTextPreference sdman_swappyness;
	private boolean sdman_ext_exist_value;
	private boolean sdman_swap_exist_value;
	private boolean sdman_app_value;
	private boolean sdman_data_value;
	private boolean sdman_dalvik_value;
	private boolean sdman_download_value;
	private boolean sdman_swap_value;
	private Integer sdman_swappyness_value;
	private ProgressDialog waitdialog;
	private SDMAN myactivity;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		myactivity = this;
		addPreferencesFromResource(R.xml.sdman);
		sdman_swappyness = (EditTextPreference)findPreference("sdman_swappyness");
		sdman_sdext_recovery = findPreference("sdman_sdext_recovery");
		sdman_swap_recovery = findPreference("sdman_swap_recovery");
		sdman_app = (CheckBoxPreference)findPreference("sdman_app");
		sdman_data = (CheckBoxPreference)findPreference("sdman_data");
		sdman_dalvik = (CheckBoxPreference)findPreference("sdman_dalvik");
		sdman_download = (CheckBoxPreference)findPreference("sdman_download");
		sdman_swap = (CheckBoxPreference)findPreference("sdman_swap");
		
		checkstatus();

		sdman_sdext_recovery.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
					return sdman_set("recovery");
			}
		});
		sdman_swap_recovery.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
					return sdman_set("recovery");
			}
		});
		sdman_app.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(!sdman_app.isChecked()){
					return sdman_set("appon");
				}else{
					return sdman_set("appoff");
				}
			}
		});
		sdman_data.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(!sdman_data.isChecked()){
					return sdman_set("dataon");
				}else{
					return sdman_set("dataoff");
				}
			}
		});
		sdman_dalvik.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(!sdman_dalvik.isChecked()){
					return sdman_set("dalvikon");
				}else{
					return sdman_set("dalvikoff");
				}
			}
		});
		sdman_download.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(!sdman_download.isChecked()){
					return sdman_set("downloadon");
				}else{
					return sdman_set("downloadoff");
				}
			}
		});
		sdman_swap.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(!sdman_swap.isChecked()){
					return sdman_set("swapon");
				}else{
					return sdman_set("swapoff");
				}
			}
		});
		sdman_swappyness.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				LiquidSettings.runRootCommand("echo "+sdman_swappyness.getText()+" > /proc/sys/vm/swappiness");
				return true;
			}
		});
		
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
	
	private void checkstatus(){
		waitdialog = ProgressDialog.show(myactivity, "", 
				"Checking status...", true);
		new Thread()
		{
			public void run() 
			{
				String result = "";
				try {

					Process process = Runtime.getRuntime().exec("su");
					DataOutputStream os = new DataOutputStream(process
							.getOutputStream());
					DataInputStream osRes = new DataInputStream(process
							.getInputStream());

					os.writeBytes("sdman -Z\n");

					result = osRes.readLine();

					os.flush();
					os.writeBytes("exit\n");
					os.flush();
					process.waitFor();

				} catch (IOException e) {
				} catch (InterruptedException e) {
				}
				sdman_ext_exist_value = result.substring(0, 1).equals("e");
				sdman_swap_exist_value = result.substring(1, 2).equals("e");
				sdman_app_value = result.substring(2, 3).equals("e");
				sdman_data_value = result.substring(3, 4).equals("e");
				sdman_dalvik_value = result.substring(4, 5).equals("e");
				sdman_download_value = result.substring(5, 6).equals("e");
				sdman_swap_value = result.substring(6, 7).equals("e");
				
				result = "";
				try {

					Process process = Runtime.getRuntime().exec("su");
					DataOutputStream os = new DataOutputStream(process
							.getOutputStream());
					DataInputStream osRes = new DataInputStream(process
							.getInputStream());

					os.writeBytes("cat /proc/sys/vm/swappiness\n");

					result = osRes.readLine();					
					os.flush();
					os.writeBytes("exit\n");
					os.flush();
					process.waitFor();

				} catch (IOException e) {
				} catch (InterruptedException e) {
				}
				sdman_swappyness_value = Integer.valueOf(result);
				myactivity.runOnUiThread(new Runnable() {
					public void run() {
						updatemenu();
					}
				});
			}
		}.start();
	}
	
	private void updatemenu(){
		waitdialog.dismiss();
		if(sdman_ext_exist_value){
			sdman_sdext_recovery.setEnabled(false);
		}else{
			sdman_app.setEnabled(false);
			sdman_data.setEnabled(false);
			sdman_dalvik.setEnabled(false);
			sdman_download.setEnabled(false);
		}
		if(sdman_swap_exist_value){
			sdman_swap_recovery.setEnabled(false);
		}else{
			sdman_swap.setEnabled(false);
			sdman_swappyness.setEnabled(false);
		}
		sdman_swappyness.setText(String.valueOf(sdman_swappyness_value));
		sdman_swappyness.setSummary("Current value: "+String.valueOf(sdman_swappyness_value));
		sdman_app.setChecked(sdman_app_value);
		sdman_data.setChecked(sdman_data_value);
		sdman_dalvik.setChecked(sdman_dalvik_value);
		sdman_download.setChecked(sdman_download_value);
		sdman_swap.setChecked(sdman_swap_value);
	}
	
	private boolean sdman_set(String arg){
		waitdialog = ProgressDialog.show(myactivity, "", 
				"Applying changes... This can take a long time... PLEASE BE PATIENT", true);
		if(arg.equals("appon")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -e mvap");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("appoff")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -d mvap");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("dataon")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -e mvdt");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("dataoff")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -d mvdt");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
						}
					});
				}}.start();
			return true;
		}		
		if(arg.equals("dalvikon")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -e mvdc");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("dalvikoff")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -d mvdc");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
						}
					});
				}}.start();
			return true;
		}		
		if(arg.equals("downloadon")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -e mvdl");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("downloadoff")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -d mvdl");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							Toast.makeText(myactivity, "No need to reboot. Done", 4000).show();
						}
					});
				}}.start();
			return true;
		}		
		if(arg.equals("swapon")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -e swap");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("swapoff")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -d swap");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("recovery")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("reboot recovery");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
						}
					});
				}}.start();
			return true;
		}		
		return false;
		
	}
}