package com.suxsem.liquidnextparts.components;

import java.io.File;

import com.suxsem.liquidnextparts.LiquidSettings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationHelper {
    private Context mContext;
    private int NOTIFICATION_ID = 1;
    private Notification mNotification;
    private NotificationManager mNotificationManager;
    private PendingIntent mContentIntent;
    private CharSequence mContentTitle;
    public NotificationHelper(Context context)
    {
        mContext = context;
    }

    /**
     * Put the notification into the status bar
     */
    public void createNotification() {
        
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        int icon = android.R.drawable.stat_sys_download;
        CharSequence tickerText = "Starting download..."; //Initial text that appears in the status bar
        long when = System.currentTimeMillis();
        mNotification = new Notification(icon, tickerText, when);
        mContentTitle = "Download ROM update"; //Full title of the notification in the pull down
        CharSequence contentText = "0% complete - Click to cancel"; //Text of the notification in the pull down
        //Intent notificationIntent = new Intent();
        //mContentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
        
        
        Intent intent = new Intent(mContext, NotificationHelperStopProcess.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        mContentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        mNotification.setLatestEventInfo(mContext, mContentTitle, contentText, mContentIntent);
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * Receives progress updates from the background task and updates the status bar notification appropriately
     * @param percentageComplete
     */
    public void progressUpdate(int percentageComplete) {
        //build up the new status message
        CharSequence contentText = percentageComplete + "% complete - Click to cancel";
        //publish it to the status bar
        mNotification.setLatestEventInfo(mContext, mContentTitle, contentText, mContentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * called when the background task is complete, this removes the notification from the status bar.
     * We could also use this to add a new ‘task complete’ notification
     */
    public void completed(String options, String updatefilelocation)    {
        //remove the notification from the status bar
        mNotificationManager.cancel(NOTIFICATION_ID);

        int icon = android.R.drawable.stat_sys_download_done;
        
        if(!options.equals("error")){
        	CharSequence tickerText = "Download completed"; //Initial text that appears in the status bar
        	long when = System.currentTimeMillis();
        	mNotification = new Notification(icon, tickerText, when);
        	mContentTitle = "Download ROM update"; //Full title of the notification in the pull down
        	CharSequence contentText = "Now flash zip from recovery"; //Text of the notification in the pull down        
        	Intent notificationIntent = new Intent();
        	mContentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
        	mNotification.setLatestEventInfo(mContext, mContentTitle, contentText, mContentIntent);
        	mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }else{
            CharSequence tickerText = "Download ROM ERROR"; //Initial text that appears in the status bar
            long when = System.currentTimeMillis();
            mNotification = new Notification(icon, tickerText, when);
            mContentTitle = "Download ROM ERROR"; //Full title of the notification in the pull down
            CharSequence contentText = "Download the update again"; //Text of the notification in the pull down        
            Intent notificationIntent = new Intent();
            mContentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
            mNotification.setLatestEventInfo(mContext, mContentTitle, contentText, mContentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);	
    	}
    	if(options.equals("r1")){
    		String tempcommand = "--update_package=SDCARD:"+ updatefilelocation.substring(8,updatefilelocation.length());
    		LiquidSettings.runRootCommand("echo \""+tempcommand+"\" > /cache/recovery/command");
    		LiquidSettings.runRootCommand("echo 0 > /cache/recovery/lnpreboot");
    		LiquidSettings.runRootCommand("reboot recovery");
    		
    	}else if(options.equals("r2")){
    		LiquidSettings.runRootCommand("reboot recovery");
    	}else if(options.equals("r3")){  		
    	}
    }
    public void cancelled(String filename){
        //remove the notification from the status bar
        mNotificationManager.cancel(NOTIFICATION_ID);

        int icon = android.R.drawable.stat_sys_download_done;
        CharSequence tickerText = "Download completed"; //Initial text that appears in the status bar
        long when = System.currentTimeMillis();
        mNotification = new Notification(icon, tickerText, when);
        mContentTitle = "Download ROM update"; //Full title of the notification in the pull down
        CharSequence contentText = "Download cancelled and file removed"; //Text of the notification in the pull down
        Intent notificationIntent = new Intent();
        mContentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
        mNotification.setLatestEventInfo(mContext, mContentTitle, contentText, mContentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        File file = new File(filename);
        file.delete();
    }

}

