package com.suxsem.liquidnextparts.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*Intent startServiceIntent = new Intent(context, MyService.class);
            context.startService(startServiceIntent);*/
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        	if(prefs.getBoolean("bottomled", false)){                
            	Intent bottomledservice = new Intent(context, SmsLED_service.class);
            	context.startService(bottomledservice);
        	}
        }


}
