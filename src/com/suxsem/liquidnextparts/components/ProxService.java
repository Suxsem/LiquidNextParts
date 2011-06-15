package com.suxsem.liquidnextparts.components;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class ProxService extends Service {
	
	public static PowerManager powermanager;
	public static PowerManager.WakeLock wakelock = null;
	public static Service context = null;
	public void onCreate() {
		super.onCreate();
		context = this;
		powermanager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakelock = this.powermanager.newWakeLock(
		PowerManager.PARTIAL_WAKE_LOCK,
		"bring phone");
		wakelock.acquire();
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
}

		

