package com.suxsem.liquidnextparts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import android.content.Context;

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
        	reader.close();
        	input.close();
    	} catch (Exception e) {
    		return 0;
    	}
        if (Strings.onlyNumber(value)){
        	return Integer.parseInt(value);
        }
        return 0;
    }

	public static boolean setSDCache(int value, Context context){
			LiquidSettings.runRootCommand("echo " + Strings.getSdCacheSizeString(value) + " > "+context.getString(R.string.initscriptfolder)+"99sdcache");
			LiquidSettings.runRootCommand("chmod +x "+context.getString(R.string.initscriptfolder)+"99sdcache");
			LiquidSettings.runRootCommand("."+context.getString(R.string.initscriptfolder)+"99sdcache");
			return true;
	}
}
