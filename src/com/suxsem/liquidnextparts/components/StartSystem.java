package com.suxsem.liquidnextparts.components;

import com.suxsem.liquidnextparts.LSystem;
import com.suxsem.liquidnextparts.LiquidSettings;
import com.suxsem.liquidnextparts.R;
import com.suxsem.liquidnextparts.Strings;

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
		Editor editor = prefs.edit();
		editor.putBoolean("firststart", prefs.getBoolean("firststart", true));
		editor.putBoolean("noprox", prefs.getBoolean("noprox", false));
		editor.putBoolean("updateonstart",prefs.getBoolean("updateonstart", true));
		editor.putString("2g3gmode", prefs.getString("2g3gmode", "nm3"));
		editor.commit();
		if(prefs.getBoolean("firststart", true)){
			editor.putBoolean("firststart", false);
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

		LiquidSettings.runRootCommand("echo 0 > /data/system/mail_led && chmod 777 /data/system/mail_led"); 

		String firstflash = prefs.getString("firstflash", "0");
		if(!firstflash.equals(context.getString(R.string.firstflashincremental))){			
			editor.putString("firstflash", context.getString(R.string.firstflashincremental));
			firstflash(context);
		}
		editor.commit();
	}
	private void firstflash(Context context){
		LSystem.RemountRW();
		LiquidSettings.runRootCommand("echo "+Strings.getSens("70", "70", "16","30")+" > /system/etc/init.d/06sensitivity");
		LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/06sensitivity");
		LSystem.RemountROnly();
		LiquidSettings.runRootCommand("./system/etc/init.d/06sensitivity");

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
		CharSequence contentText = "because it has just been configured"; //Text of the notification in the pull down
		Intent notificationIntent = new Intent();
		PendingIntent mContentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		mNotification.setLatestEventInfo(context, mContentTitle, contentText, mContentIntent);
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, mNotification);
	}
}
