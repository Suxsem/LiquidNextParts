package com.suxsem.liquidnextparts;

import com.suxsem.liquidnextparts.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class StartSystem {
	public void startsystem(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(prefs.getBoolean("firststart", true)){
			int icon = android.R.drawable.stat_sys_warning;
			CharSequence tickerText = "Welcome to LiquidNext!"; //Initial text that appears in the status bar
			long when = System.currentTimeMillis();
			Notification mNotification = new Notification(icon, tickerText, when);
			String mContentTitle = "Welcome to LiquidNext!"; //Full title of the notification in the pull down
			CharSequence contentText = "by thepasto and Suxsem"; //Text of the notification in the pull down
			Intent notificationIntent = new Intent();
			PendingIntent mContentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			mNotification.setLatestEventInfo(context, mContentTitle, contentText, mContentIntent);
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(2, mNotification);
		}

		LiquidSettings.runRootCommand("mkdir "+context.getString(R.string.initscriptfolder));
		LiquidSettings.runRootCommand("echo 0 > /data/system/mail_led && chmod 777 /data/system/mail_led"); 

		String firstflash = prefs.getString("firstflash", "0");
		if(!firstflash.equals(context.getString(R.string.firstflashincremental))){
			if(!prefs.getBoolean("firststart", true)){
				int icon = android.R.drawable.stat_sys_warning;
				CharSequence tickerText = "LNP preferences cleared"; //Initial text that appears in the status bar
				long when = System.currentTimeMillis();
				Notification mNotification = new Notification(icon, tickerText, when);
				String mContentTitle = "LNP preferences cleared"; //Full title of the notification in the pull down
				CharSequence contentText = "You have to set them again"; //Text of the notification in the pull down
				Intent notificationIntent = new Intent();
				PendingIntent mContentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				mNotification.setLatestEventInfo(context, mContentTitle, contentText, mContentIntent);
				NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.notify(1, mNotification);				
			}
			firstflash(context);
		}
		
	}
	
	public void firstflash(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor;
		editor = prefs.edit();
		editor.clear();
		editor.commit();
		editor = prefs.edit();
		editor.putBoolean("firststart", false);
		editor.putString("firstflash", context.getString(R.string.firstflashincremental));
		editor.commit();
		LiquidSettings.runRootCommand("rm -f "+context.getString(R.string.initscriptfolder)+"*");

		LiquidSettings.runRootCommand("chmod 222 /sys/class/leds2/power");
		LiquidSettings.runRootCommand("echo "+Strings.getSens("70", "70", "16","30")+" > "+context.getString(R.string.initscriptfolder)+"06sensitivity");
		LiquidSettings.runRootCommand("chmod +x "+context.getString(R.string.initscriptfolder)+"06sensitivity");
		LiquidSettings.runRootCommand("."+context.getString(R.string.initscriptfolder)+"06sensitivity");

		Log.d("liquidnext",Integer.toString(1));
		Settings.System.putInt(context.getContentResolver(), "light_sensor_custom", 1);
		Settings.System.putInt(context.getContentResolver(), "light_decrease", 1);
		Settings.System.putInt(context.getContentResolver(), "light_hysteresis", 0);
		
		java.io.File file = new java.io.File("/data/data/com.android.phone/shared_prefs/com.android.phone_preferences.xml");
		if (file.exists()) {		
			LiquidSettings.runRootCommand("sh /system/xbin/editxml.sh /data/data/com.android.phone/shared_prefs/com.android.phone_preferences.xml button_led_notify false");
			LiquidSettings.runRootCommand("sh /system/xbin/editxml.sh /data/data/com.android.phone/shared_prefs/com.android.phone_preferences.xml button_always_proximity true");
		}else{
			LiquidSettings.runRootCommand("echo '<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?><map><boolean name=\"button_always_proximity\" value=\"true\" /><boolean name=\"button_led_notify\" value=\"false\" /></map>' > /data/data/com.android.phone/shared_prefs/com.android.phone_preferences.xml");
		}
		int icon = android.R.drawable.stat_sys_warning;
		CharSequence tickerText = "System need a REBOOT"; //Initial text that appears in the status bar
		long when = System.currentTimeMillis();
		Notification mNotification = new Notification(icon, tickerText, when);
		String mContentTitle = "System needs a REBOOT"; //Full title of the notification in the pull down
		CharSequence contentText = "Because it has just been configured"; //Text of the notification in the pull down
		Intent notificationIntent = new Intent();
		PendingIntent mContentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		mNotification.setLatestEventInfo(context, mContentTitle, contentText, mContentIntent);
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, mNotification);
	}
}
