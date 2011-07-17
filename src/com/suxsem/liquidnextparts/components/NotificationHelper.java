package com.suxsem.liquidnextparts.components;

import com.suxsem.liquidnextparts.LiquidSettings;
import com.suxsem.liquidnextparts.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.RemoteViews;

import com.suxsem.liquidnextparts.activities.OTA_updates_status;
import com.suxsem.liquidnextparts.components.DownloadTask;

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
    private static CountDownTimer reconnectiontimer;
    private String arg;
    private static RemoteViews contentView;
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
    	createnotification("Starting download...",
    			android.R.drawable.stat_sys_download,
    			"Download ROM update",
    			"Starting... - Click for options",
    			Notification.FLAG_ONGOING_EVENT);
    }

    /**
     * Receives progress updates from the background task and updates the status bar notification appropriately
     * @param percentageComplete
     */
    public void progressUpdate(int percentageComplete) {
        contentView.setTextViewText(R.id.notification_layout_text2, percentageComplete + "% complete - Click to cancel");
        mNotification.contentView = contentView;
        mNotification.contentIntent = mContentIntent;
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * called when the background task is complete, this removes the notification from the status bar.
     * We could also use this to add a new ‘task complete’ notification
     */
    public void completed(boolean error, String options, String updatefilelocation, String partialfilelocation)    {
        //remove the notification from the status bar
        mNotificationManager.cancel(NOTIFICATION_ID);
        arg = updatefilelocation + "#" + options + "#" + partialfilelocation;
        if(!error){
        	options_final = options;
        	updatefilelocation_final = updatefilelocation;
            LiquidSettings.runRootCommand("mv -f "+partialfilelocation+" "+updatefilelocation);
        	if(adsfinish){        	
        	flashrom();
        	}else{
        		waitflash = true;
                createnotification("Download ROM ERROR",
                		android.R.drawable.stat_sys_download_done,
                		"Download ROM ERROR",
                		"Reconnecting in 10 seconds...",
                		Notification.FLAG_ONGOING_EVENT); 
                
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
            createnotification("Download ROM ERROR",
            		android.R.drawable.stat_sys_download_done,
            		"Download ROM ERROR",
            		"Reconnecting in 10 seconds...",
            		Notification.FLAG_ONGOING_EVENT); 
            reconnectiontimer = new CountDownTimer(10000, 1000) {

				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					  DownloadTask.downloadtask = new DownloadTask(mContext).execute(mContext.getString(R.string.url), arg);
					  this.cancel();
				}

				@Override
				public void onTick(long millisUntilFinished) {
			        CharSequence contentText = "Reconnecting in "+(millisUntilFinished/1000)+" seconds...";
			        //publish it to the status bar
			        mNotification.setLatestEventInfo(mContext, mContentTitle, contentText, mContentIntent);
			        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
					// TODO Auto-generated method stub
				}
   			}.start();
    	}
    }
    public static void cancelled(){
    	try {
			reconnectiontimer.cancel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //remove the notification from the status bar
        mNotificationManager.cancel(NOTIFICATION_ID);
        createnotification("Download cancelled",
        		android.R.drawable.stat_sys_download_done,
        		"Download ROM update",
        		"Download cancelled by user",
        		Notification.FLAG_AUTO_CANCEL);       
    }
    public static void flashrom(){
    	createnotification("Download completed",
    			android.R.drawable.stat_sys_download_done,
    			"Download ROM update",
    			"Now flash zip from recovery",
    			Notification.FLAG_AUTO_CANCEL);
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
    public static void closeclass(){
    	try {
			cancelled();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try {
			DownloadTask.downloadtask.cancel(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
    }
    public static void createnotification(CharSequence tickerText,int icon, String text1, String text2, int flag){
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        long when = System.currentTimeMillis();
        mNotification = new Notification(icon, tickerText, when);                
        Intent intent = new Intent(mContext, OTA_updates_status.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        mContentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        contentView = new RemoteViews(mContext.getPackageName(), R.layout.notification_custom_layout);
        contentView.setImageViewResource(R.id.notification_layout_image, R.drawable.icon);
        contentView.setTextViewText(R.id.notification_layout_text1, text1);
        contentView.setTextViewText(R.id.notification_layout_text2, text2);
        mNotification.contentView = contentView;
        mNotification.contentIntent = mContentIntent;
        mNotification.flags = flag;
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

}

