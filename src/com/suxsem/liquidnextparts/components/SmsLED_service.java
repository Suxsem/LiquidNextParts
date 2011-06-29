package com.suxsem.liquidnextparts.components;


import com.suxsem.liquidnextparts.LiquidSettings;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.telephony.SmsManager;

public class SmsLED_service extends Service {
	SmsManager sms;
	private final Handler mHandler = new Handler();
	private SharedPreferences prefs;

	private String[] strFields = { 
			android.provider.CallLog.Calls.NEW,
	};
	private String strOrder = android.provider.CallLog.Calls.DATE + " DESC";                      
	private Cursor mCallCursor;

	BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

			if(prefs.getBoolean("fixled", false)){                
				if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {            	

					if(prefs.getBoolean("fixsms", false)){
						Cursor cursor = getContentResolver().query(Uri.parse("content://sms/")
								, new String[]{"read",}
						, null //selection
						, null //selectionArgs
						, "read ASC"); //sortOrder

						if (cursor != null && cursor.moveToFirst()) {
							String read = cursor.getString(cursor.getColumnIndex("read"));
							if (read.equals("0")){
								LiquidSettings.runRootCommand("echo 1 > /sys/class/leds2/mail");
								if(prefs.getBoolean("bottomled", false))LiquidSettings.runRootCommand("echo 1 > /sys/class/leds2/bottom");
							}else{
								LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/mail");
								if(prefs.getBoolean("bottomled", false))LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/bottom");
							}
						}               		
						cursor.close();
					}else{
						if(prefs.getBoolean("bottomled", false))LiquidSettings.runRootCommand("tail /data/system/mail_led > /sys/class/leds2/bottom");
					}

					//CHIMATE
					if(prefs.getBoolean("fixcall", false)){
						mCallCursor = getContentResolver().query(
								android.provider.CallLog.Calls.CONTENT_URI,
								strFields,
								CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE,
								null,
								strOrder
						); 
						if(mCallCursor.moveToFirst()){
							if (mCallCursor.getString(mCallCursor.getColumnIndex(Calls.NEW)).equals("1")){
								LiquidSettings.runRootCommand("echo 1 > /sys/class/leds2/call");
							}else{
								LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/call");           		
							}
						}
					}
				}
				if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {            	
					if(prefs.getBoolean("bottomled", false))LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/bottom");
					if(prefs.getBoolean("fixcall", false))LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/call");
					if(prefs.getBoolean("fixsms", false))LiquidSettings.runRootCommand("echo 0 > /sys/class/leds2/mail");
				} 
				if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
					if(prefs.getBoolean("bottomled", false)) LiquidSettings.runRootCommand("echo 1 > /sys/class/leds2/bottom");
				}
			}else{
				terminaservizio();
				unregisterReceiver(this);
			}

		}
	};

	@Override
	public IBinder onBind(Intent intent) {


		// TODO Auto-generated method stub
		return null;
	}
	public void onCreate() {
		super.onCreate();
		LiquidSettings.runRootCommand("echo 0 > /data/system/mail_led && chmod 777 /data/system/mail_led"); 
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(!prefs.getBoolean("fixled", false)){terminaservizio();return;}
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		this.registerReceiver(mIntentReceiver, filter, null, mHandler);
	}
	public void terminaservizio(){
		LiquidSettings.runRootCommand("echo '0' > /sys/class/leds2/bottom");
		this.stopSelf();
	}
	public void onDesotry(){

		try {
			this.unregisterReceiver(mIntentReceiver);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}