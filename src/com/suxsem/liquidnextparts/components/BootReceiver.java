package com.suxsem.liquidnextparts.components;

import com.suxsem.liquidnextparts.parsebuildprop;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	new StartSystem().startsystem(context);
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    		if((parsebuildprop.parseInt("hw.acer.psensor_calib_min_base")==32717)!=(prefs.getBoolean("noprox", false))){
				if (prefs.getBoolean("noprox", false)) {
					parsebuildprop.editString("hw.acer.psensor_calib_min_base", "32717");		    	
				}else{
					parsebuildprop.editString("hw.acer.psensor_calib_min_base", "32716");
				}
    		}
            NotificationHelper.createnotification("System need a REBOOT",
            		android.R.drawable.stat_sys_warning,
            		"System need a REBOOT",
            		"due to changes in build.prop file",
            		Notification.FLAG_AUTO_CANCEL,
            		false);
    		
    		int icon = android.R.drawable.stat_sys_warning;
    		CharSequence tickerText = "System need a REBOOT"; //Initial text that appears in the status bar
    		long when = System.currentTimeMillis();
    		Notification mNotification = new Notification(icon, tickerText, when);
    		String mContentTitle = "System needs a REBOOT"; //Full title of the notification in the pull down
    		CharSequence contentText = "due to changes in build.prop file"; //Text of the notification in the pull down
    		Intent notificationIntent = new Intent();
    		PendingIntent mContentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
    		mNotification.setLatestEventInfo(context, mContentTitle, contentText, mContentIntent);
    		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    		mNotificationManager.notify(3, mNotification);
        }
}
