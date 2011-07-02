package com.suxsem.liquidnextparts.components;


import com.suxsem.liquidnextparts.LiquidSettings;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
//import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
//import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.CallLog;
//import android.provider.Settings;
import android.provider.CallLog.Calls;
import android.telephony.SmsManager;

public class main_service extends Service {
	SmsManager sms;
	private final Handler mHandler = new Handler();
	private SharedPreferences prefs;

	private String[] strFields = { 
			android.provider.CallLog.Calls.NEW,
	};
	private String strOrder = android.provider.CallLog.Calls.DATE + " DESC";                      
	private Cursor mCallCursor;
	
	public static PowerManager powermanager;
	public static PowerManager.WakeLock wakelockcall = null;
	//public static PowerManager.WakeLock wakelockwifi = null;
	public static PowerManager.WakeLock wakelockota = null;
	public static WifiManager.WifiLock wifilockota = null;
	
	public static boolean call = false;
	/*private ConnectivityManager connManager;
	private android.net.NetworkInfo wifiInfo;
	private boolean wifiwakelock = false;
	private Intent pluggedintent;*/
	
	public WifiManager wifimanager;
	
	BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

			if(prefs.getBoolean("fixled", false)){                
				if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {            	

					if(call){
						LiquidSettings.runRootCommand("echo 50 > /sys/devices/platform/i2c-adapter/i2c-0/0-0066/threshold");
					}
					
					
					/*if (wifiInfo.getState() == android.net.NetworkInfo.State.CONNECTED  ) {
						int wifiSleepPolicy = Settings.System.getInt(getContentResolver(),
			                    Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
						if(wifiSleepPolicy == Settings.System.WIFI_SLEEP_POLICY_NEVER){
							wifiwakelock = true;
							wakelockwifi.acquire();
							Log.d("LS","AC:NEVER");
						}else if(wifiSleepPolicy == Settings.System.WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED){

							pluggedintent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
							Log.d("LS","AC:PLUGGED");
							if ((pluggedintent.getIntExtra("plugged", 0) == BatteryManager.BATTERY_PLUGGED_AC) || (pluggedintent.getIntExtra("plugged", 0) ==  BatteryManager.BATTERY_PLUGGED_USB) )
							{
								wifiwakelock = true;
								wakelockwifi.acquire();
							}
						}
					}*/
					
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

					/*if(wifiwakelock){
						wifiwakelock = false;
						wakelockwifi.release();
						Log.d("LS","RE");
					}*/
					if(call){
						LiquidSettings.runRootCommand("./system/etc/init.d/06sensitivity");
					}
					
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
	
	@SuppressWarnings("static-access")
	public void onCreate() {
		super.onCreate();
		powermanager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wakelockcall = this.powermanager.newWakeLock(
		PowerManager.PARTIAL_WAKE_LOCK,
		"bring phone call");
		/*wakelockwifi = this.powermanager.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK,
				"bring phone wifi");*/
		wakelockota = this.powermanager.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK,
				"bring phone ota");
		wifilockota = wifimanager.createWifiLock("wifilockota");
		/*connManager =  (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);		
		wifiInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);*/
		LiquidSettings.runRootCommand("echo 0 > /data/system/mail_led && chmod 777 /data/system/mail_led"); 
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(!prefs.getBoolean("fixled", false)){terminaservizio();return;}
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		this.registerReceiver(mIntentReceiver, filter, null, mHandler);
		LiquidSettings.runRootCommand("./system/etc/init.d/06sensitivity");
	}
	public void terminaservizio(){
		LiquidSettings.runRootCommand("echo '0' > /sys/class/leds2/bottom");
		this.stopSelf();
	}
	public void onDesotry(){

		try {
			this.unregisterReceiver(mIntentReceiver);
			try {
				wakelockcall.release();
				wifilockota.release();
				wakelockota.release();
				//wakelockwifi.release();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void startcall(){
			call = true;
			try {
				wakelockcall.acquire();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public static void stopcall(){
			call = false;
			LiquidSettings.runRootCommand("./system/etc/init.d/06sensitivity");
			try {
				wakelockcall.release();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public static void startota(){
		try {
			wakelockota.acquire();
			wifilockota.acquire();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void stopota(){
		try {
			wakelockota.release();
			wifilockota.acquire();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}	
}