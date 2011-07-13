package com.suxsem.liquidnextparts.components;

import java.io.File;

import com.suxsem.liquidnextparts.LiquidSettings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationHelper {
    private static Context mContext;
    private static int NOTIFICATION_ID = 1;
    private static Notification mNotification;
    private static NotificationManager mNotificationManager;
    private static PendingIntent mContentIntent;
    private static CharSequence mContentTitle;
    public static boolean adsfinish = false;
    public static boolean waitflash = false;
    private static String options_final = "";
    private static String updatefilelocation_final = "";
    public NotificationHelper(Context context)
    {
        mContext = context;
    }

    /**
     * Put the notification into the status bar
     */
    public void createNotification() {
    	waitflash = false;
    	adsfinish = false;
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
        
        if(!options.equals("error")){
        	options_final = options;
        	updatefilelocation_final = updatefilelocation;
        	if(adsfinish){        	
        	flashrom();
        	}else{
        		waitflash = true;
        		CharSequence tickerText = "Waiting ads..."; //Initial text that appears in the status bar
            	long when = System.currentTimeMillis();
            	mNotification = new Notification(android.R.drawable.stat_sys_download, tickerText, when);
            	mContentTitle = "Waiting ads..."; //Full title of the notification in the pull down
            	CharSequence contentText = "Waiting ads..."; //Text of the notification in the pull down        
            	Intent notificationIntent = new Intent();
            	mContentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
            	mNotification.setLatestEventInfo(mContext, mContentTitle, contentText, mContentIntent);
            	mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        	}
        }else{
            CharSequence tickerText = "Download ROM ERROR"; //Initial text that appears in the status bar
            long when = System.currentTimeMillis();
            mNotification = new Notification(android.R.drawable.stat_sys_download_done, tickerText, when);
            mContentTitle = "Download ROM ERROR"; //Full title of the notification in the pull down
            CharSequence contentText = "Download the update again"; //Text of the notification in the pull down        
            Intent notificationIntent = new Intent();
            mContentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
            mNotification.setLatestEventInfo(mContext, mContentTitle, contentText, mContentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
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
    public static void flashrom(){
    	CharSequence tickerText = "Download completed"; //Initial text that appears in the status bar
    	long when = System.currentTimeMillis();
    	mNotification = new Notification(android.R.drawable.stat_sys_download_done, tickerText, when);
    	mContentTitle = "Download ROM update"; //Full title of the notification in the pull down
    	CharSequence contentText = "Now flash zip from recovery"; //Text of the notification in the pull down        
    	Intent notificationIntent = new Intent();
    	mContentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
    	mNotification.setLatestEventInfo(mContext, mContentTitle, contentText, mContentIntent);
    	mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    	if(options_final.equals("r1")){
    		//String tempcommand = "--update_package=SDCARD:"+ updatefilelocation.substring(8,updatefilelocation.length());
    		String tempcommand = "--update_package=SDCARD:LiquidNext_autoflash.zip";
    		LiquidSettings.runRootCommand("echo \""+tempcommand+"\" > /cache/recovery/command");
    		LiquidSettings.runRootCommand("echo \""+updatefilelocation_final+"\" > /cache/recovery/lnpreboot");
    		LiquidSettings.runRootCommand("reboot recovery");
    		
    	}else if(options_final.equals("r2")){
    		LiquidSettings.runRootCommand("reboot recovery");
    	}else if(options_final.equals("r3")){  		
    	}
    }

}

