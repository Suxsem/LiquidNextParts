package com.suxsem.liquidnextparts;

public class UndervoltedKernel {

	public static boolean setenabled(boolean opt){
		try{
			if(opt == true)
				return (LSystem.RemountRW() & LiquidSettings.runRootCommand("echo " + Strings.UndervoltedKernel() + " > /etc/init.d/12undervoltedkernel") && LiquidSettings.runRootCommand("chmod +x /etc/init.d/12undervoltedkernel") && LSystem.RemountROnly());
			else
				return (LSystem.RemountRW() && LiquidSettings.runRootCommand("rm /etc/init.d/12undervoltedkernel") && LSystem.RemountROnly());
		} catch (Exception e){}
		return false;
	}
	public static boolean checkenabled(){
		java.io.File file = new java.io.File("/etc/init.d/12undervoltedkernel");
		return file.exists();
	}
	
}
