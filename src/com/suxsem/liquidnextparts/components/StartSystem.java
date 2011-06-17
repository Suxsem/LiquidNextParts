package com.suxsem.liquidnextparts.components;

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

public class StartSystem {
        public void startsystem(boolean forcenewflash, Context context) {
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);        	        	
        	if(prefs.getBoolean("firststart", true)){
        		Editor editor = prefs.edit();
        		editor.putBoolean("firststart", false);
        		editor.putBoolean("fixled", true);
        		editor.putBoolean("fixsms", false);
        		editor.putBoolean("fixcall", true);
        		editor.commit();
        		
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
        	if(prefs.getBoolean("fixled", false)){                
            	Intent fixledservice = new Intent(context, SmsLED_service.class);
            	context.startService(fixledservice);
        	}
        	
        	String firstflash = prefs.getString("firstflash", "0");
        	if(!firstflash.equals(context.getString(R.string.firstflashincremental))){
        		Editor editor = prefs.edit();
        		editor.putString("firstflash", context.getString(R.string.firstflashincremental));
        		editor.commit();
        		firstflash(context);
        	}

        }
        private void firstflash(Context context){
        	Settings.System.putInt(context.getContentResolver(), "light_sensor_custom", 1);
        	Settings.System.putInt(context.getContentResolver(), "light_decrease", 1);
        	Settings.System.putInt(context.getContentResolver(), "light_hysteresis", 0);
        	LiquidSettings.runRootCommand("sh /system/xbin/editxml.sh /data/data/com.android.phone/shared_prefs/com.android.phone_preferences.xml button_led_notify false");
        	
            int icon = android.R.drawable.stat_sys_warning;
            CharSequence tickerText = "System need a REBOOT"; //Initial text that appears in the status bar
            long when = System.currentTimeMillis();
            Notification mNotification = new Notification(icon, tickerText, when);
            String mContentTitle = "System need a REBOOT"; //Full title of the notification in the pull down
            CharSequence contentText = "because it has just been configured"; //Text of the notification in the pull down
            Intent notificationIntent = new Intent();
            PendingIntent mContentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
            mNotification.setLatestEventInfo(context, mContentTitle, contentText, mContentIntent);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(1, mNotification);        	
        	
        }
}
