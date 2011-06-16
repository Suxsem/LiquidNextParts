package com.suxsem.liquidnextparts.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import com.suxsem.liquidnextparts.R;

public class StartSystem {
		private String incrementalflash = "3";
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
				mNotificationManager.notify(1, mNotification);
        	}
        	if(prefs.getBoolean("fixled", false)){                
            	Intent fixledservice = new Intent(context, SmsLED_service.class);
            	context.startService(fixledservice);
        	}
        	
        	String firstflash = prefs.getString("firstflash", "0");
        	if(!firstflash.equals(incrementalflash)){
        		Editor editor = prefs.edit();
        		editor.putString("firstflash", incrementalflash);
        		editor.commit();
        		firstflash(context);
        	}

        }
        private void firstflash(Context context){
        	Settings.System.putInt(context.getContentResolver(), "light_sensor_custom", 1);
        	Settings.System.putInt(context.getContentResolver(), "light_decrease", 1);
        	Settings.System.putInt(context.getContentResolver(), "light_hysteresis", 0);        	
        }
}
