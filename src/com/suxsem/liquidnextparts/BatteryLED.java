package com.suxsem.liquidnextparts;

import android.content.Context;

public class BatteryLED {

	public static boolean setdisable(boolean opt, Context context){
		try{
			if(opt == true)
				return (LiquidSettings.runRootCommand("echo " + Strings.batteryleddisable() + " > "+context.getString(R.string.initscriptfolder)+"10batteryled") && LiquidSettings.runRootCommand("chmod +x "+context.getString(R.string.initscriptfolder)+"10batteryled"));
			else
				return (LiquidSettings.runRootCommand("rm "+context.getString(R.string.initscriptfolder)+"10batteryled"));
		} catch (Exception e){}
		return false;
	}
	
}
