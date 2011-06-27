package com.suxsem.liquidnextparts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class NetworkMode {
	
	public static void switchnetworkmode(Context context){
		
		 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		 Intent intent = new Intent("com.android.internal.telephony.MODIFY_NETWORK_MODE");
		 String choice = prefs.getString("2g3gmode", "nm3");
		 Log.d("=========",choice);
		 if(choice.equals("nm1")){
			 intent.putExtra("networkMode", 1);
		 }else if(choice.equals("nm2")){
			 intent.putExtra("networkMode", 2); 
		 }else if (choice .equals("nm3")){
			 intent.putExtra("networkMode", 0);
		 }
		 context.sendBroadcast(intent);
	}
}
