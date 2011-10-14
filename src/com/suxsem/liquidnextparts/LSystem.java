package com.suxsem.liquidnextparts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import android.widget.Toast;
import android.content.Context;

import android.util.Log;

public class LSystem {
	
	public static boolean RemountRW(){
		return LiquidSettings.runRootCommand("mount -o rw,remount -t yaffs2 /dev/block/mtdblock1 /system");
	}
	
	public static boolean RemountROnly(){
		return LiquidSettings.runRootCommand("mount -o ro,remount -t yaffs2 /dev/block/mtdblock1 /system");
	}
	
	public static String getModVersion() {
		String mod = "ro.modversion";
		String CMversion;
		BufferedReader input;
		try {
            Process get = Runtime.getRuntime().exec("getprop " + mod);
            input = new BufferedReader(new InputStreamReader(get.getInputStream()), 1024);
            CMversion = input.readLine();
            input.close();
		} catch (IOException ex) {
            Log.e("*** ERROR ***", "Unable to read mod version.");
            return null;
		}
		return CMversion;
	}
	
	public static String getProduct() {
		String mod = "ro.product.model";
		String product;
		BufferedReader input;
		try {
            Process get = Runtime.getRuntime().exec("getprop " + mod);
            input = new BufferedReader(new InputStreamReader(get.getInputStream()), 1024);
            product = input.readLine();
            input.close();
		} catch (IOException ex) {
            Log.e("*** ERROR ***", "Unable to read mod version.");
            return "";
		}
		Log.d("LS-APP","Find this product.model in build.prop: " + product);
		return product;
	}
	
	public static boolean isLiquidMetal(Context context){
		String product = getProduct();
		if (product.equalsIgnoreCase("Liquid MT") || product.equalsIgnoreCase("Liquid Metal")){
			Toast.makeText(context, "Liquid Metal detected", 1000).show();
			return true;
		}else
			return false;
	}
	
	public static boolean hapticAvailable(){
		if (new File("/sys/module/avr/parameters/vibr").exists()){
			Log.d("LS-APP","Haptic feedback path available");
			return true;
		} else {
			Log.d("LS-APP","Haptic feedback path unavailable, is it a stock rom?");
			return false;
		}
	}
	
	public static boolean vibrStatus() {
    	String value;
    	if (!hapticAvailable())
    		return false;
    	try {
        	FileReader input = new FileReader("/sys/module/avr/parameters/vibr");
        	BufferedReader reader = new BufferedReader(input);
        	value = reader.readLine();
        	reader.close();
        	input.close();
    	} catch (Exception e) {
    		Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+e.getMessage());
    		return false;
    	}
        if(value == null || value.equalsIgnoreCase("0")) 
        	return false;
        else if(value.equalsIgnoreCase("1")) 
        	return true;
        return false;
    }

}
