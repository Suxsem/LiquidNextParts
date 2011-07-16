package com.suxsem.liquidnextparts.components;

import com.suxsem.liquidnextparts.parsebuildprop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	new StartSystem().startsystem(context);
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    		Editor editor = prefs.edit();
    		editor.putBoolean("noprox", parsebuildprop.parseInt("hw.acer.psensor_calib_min_base")==32717);
    		editor.commit();
        }
}
