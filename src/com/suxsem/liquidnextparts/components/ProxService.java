package com.suxsem.liquidnextparts.components;

import com.suxsem.liquidnextparts.LiquidSettings;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.CallLog;
import android.provider.CallLog.Calls;

public class ProxService extends Service {
	
	public static PowerManager powermanager;
	public static PowerManager.WakeLock wakelock = null;
	public static Service context = null;
	private final Handler mHandler = new Handler();
	@SuppressWarnings("static-access")
	public void onCreate() {
		super.onCreate();
		context = this;
		powermanager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakelock = this.powermanager.newWakeLock(
		PowerManager.PARTIAL_WAKE_LOCK,
		"bring phone");
		wakelock.acquire();
		IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        this.registerReceiver(mIntentReceiver, filter, null, mHandler);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public static void terminaservizio(){
		try {
			wakelock.release();
			context.stopSelf();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {            	
            	LiquidSettings.runRootCommand("echo 50 > /sys/devices/platform/i2c-adapter/i2c-0/0-0066/threshold");
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {            	
            	LiquidSettings.runRootCommand("./system/etc/init.d/06sensitivity");
            }
        }
    };
}

		

