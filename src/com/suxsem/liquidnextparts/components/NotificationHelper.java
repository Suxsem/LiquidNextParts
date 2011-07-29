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
    private static int NOTIFICATION_ID = 4;
    private static Notification mNotification;
    private static NotificationManager mNotificationManager;
    private static PendingIntent mContentIntent;
    public static boolean adsfinish = false;
    public static boolean waitflash = false;
    private static String options_final = "";
    private static String updatefilelocation_final = "";
    private static CountDownTimer reconnectiontimer;
    public static String arg;
    public static boolean ispaused = false;
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
    			Notification.FLAG_ONGOING_EVENT,
    			true);
    }

    /**
     * Receives progress updates from the background task and updates the status bar notification appropriately
     * @param percentageComplete
     */
    public void progressUpdate(int percentageComplete) {
        contentView.setTextViewText(R.id.notification_layout_text2, percentageComplete + "% complete - Click to options");
        mNotification.contentView = contentView;
        mNotification.contentIntent = mContentIntent;
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * called when the background task is complete, this removes the notification from the status bar.
     * We could also use this to add a new ‘task complete’ notification
     */
    public void completed(boolean error) {
        //remove the notification from the status bar
        mNotificationManager.cancel(NOTIFICATION_ID);
        if(!error){
        	options_final = arg.split("#")[1];
        	updatefilelocation_final = arg.split("#")[0];
            LiquidSettings.runRootCommand("mv -f "+arg.split("#")[2]+" "+arg.split("#")[0]);
        	if(adsfinish){        	
        	flashrom();
        	}else{
        		waitflash = true;
                createnotification("Waiting ads...",
                		android.R.drawable.stat_sys_download,
                		"Download ROM update",
                		"Waiting ads...",
                		Notification.FLAG_AUTO_CANCEL,
                		false);                
        	}
        }else{
            createnotification("Download ROM ERROR",
            		android.R.drawable.stat_sys_download_done,
            		"Download ROM update",
            		"ERROR - Reconnecting in 10 seconds...",
            		Notification.FLAG_ONGOING_EVENT,
            		true); 
            reconnectiontimer = new CountDownTimer(10000, 1000) {

				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					  DownloadTask.downloadtask = new DownloadTask(mContext).execute(mContext.getString(R.string.url), arg);
					  this.cancel();
				}

				@Override
				public void onTick(long millisUntilFinished) {
					contentView.setTextViewText(R.id.notification_layout_text2, "ERROR - Reconnecting in "+(millisUntilFinished/1000)+" seconds...");
			        mNotification.contentView = contentView;
			        mNotification.contentIntent = mContentIntent;
			        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
				}
   			}.start();
    	}
    }
    public static void cancelled(){
    	try {
			reconnectiontimer.cancel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
        //remove the notification from the status bar
        mNotificationManager.cancel(NOTIFICATION_ID);
        if(!ispaused){
        createnotification("Download cancelled",
        		android.R.drawable.stat_sys_download_done,
        		"Download ROM update",
        		"Download cancelled by user",
        		Notification.FLAG_AUTO_CANCEL,
        		false);
        }else{
        createnotification("Download paused",
            		android.R.drawable.stat_sys_download_done,
            		"Download ROM update",
            		"Download paused by user",
            		Notification.FLAG_ONGOING_EVENT,
            		true);
        }
    }
    
    public static void urlDecodeError(){
    	try {
			reconnectiontimer.cancel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
        //remove the notification from the status bar
        mNotificationManager.cancel(NOTIFICATION_ID);

        createnotification("Download ROM ERROR",
        		android.R.drawable.stat_sys_download_done,
        		"Download ROM update",
        		"Failed to decode download URL",
        		Notification.FLAG_AUTO_CANCEL,
        		false);        
    }
    
    public static void flashrom(){
    	createnotification("Download completed",
    			android.R.drawable.stat_sys_download_done,
    			"Download ROM update",
    			"Now flash zip from recovery",
    			Notification.FLAG_AUTO_CANCEL,
    			false);
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
		}
    	try {
			DownloadTask.downloadtask.cancel(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}		
    }
    public static void createnotification(CharSequence tickerText,int icon, String text1, String text2, int flag, boolean showintent){
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        long when = System.currentTimeMillis();
        mNotification = new Notification(icon, tickerText, when);
        Intent intent;
        if(showintent){
        intent = new Intent(mContext, OTA_updates_status.class);
        }else{
        intent = new Intent();
        }
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

