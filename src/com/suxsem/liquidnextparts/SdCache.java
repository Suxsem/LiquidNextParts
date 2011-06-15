package com.suxsem.liquidnextparts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import android.util.Log;

public class SdCache {
	
	public static boolean isCachePathAvailable(){
		return (new File("/sys/devices/virtual/bdi/179:0/read_ahead_kb").exists());
	}
	
	public static int getSdCacheSize() {
    	String value;
    	if (!isCachePathAvailable())
    		return 0;
    	try {
        	FileReader input = new FileReader("/sys/devices/virtual/bdi/179:0/read_ahead_kb");
        	BufferedReader reader = new BufferedReader(input);
        	value = reader.readLine();
        	Log.d("LS-APP","Read sd cache value " + value);
        	reader.close();
        	input.close();
    	} catch (Exception e) {
    		Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+e.getMessage());
    		return 0;
    	}
        if (Strings.onlyNumber(value)){
        	return Integer.parseInt(value);
        }
        Log.d("LS-APP", "Value read from SDCache is not numeric");
        return 0;
    }

	public static boolean setSDCache(int value){
		if (LSystem.RemountRW()){
			LiquidSettings.runRootCommand("echo " + Strings.getSdCacheSizeString(value) + " > /system/etc/init.d/99sdcache");
			LiquidSettings.runRootCommand("chmod +x /system/etc/init.d/99sdcache");
			LiquidSettings.runRootCommand("./system/etc/init.d/99sdcache");
			LSystem.RemountROnly();
			return true;
		}
		return false;
	}
}
