package com.suxsem.liquidnextparts;

import android.content.Context;

public class UndervoltedKernel {

	public static boolean setenabled(boolean opt, Context context){
		try{
			if(opt == true)
				return (LiquidSettings.runRootCommand("echo " + Strings.UndervoltedKernel() + " > "+context.getString(R.string.initscriptfolder)+"12undervoltedkernel") && LiquidSettings.runRootCommand("chmod +x "+context.getString(R.string.initscriptfolder)+"12undervoltedkernel"));
			else
				return (LiquidSettings.runRootCommand("rm "+context.getString(R.string.initscriptfolder)+"12undervoltedkernel"));
		} catch (Exception e){}
		return false;
	}
	public static boolean checkenabled(Context context){
		java.io.File file = new java.io.File(""+context.getString(R.string.initscriptfolder)+"12undervoltedkernel");
		return file.exists();
	}
	
}
