package com.suxsem.liquidnextparts;

public class BottomLED {

	public static boolean setdisable(boolean opt){
		try{
			if(opt == true)
				return (LSystem.RemountRW() & LiquidSettings.runRootCommand("echo " + Strings.bottomleddisable() + " > /etc/init.d/11bottomled") && LiquidSettings.runRootCommand("chmod +x /etc/init.d/11bottomled") && LSystem.RemountROnly());
			else
				return (LSystem.RemountRW() && LiquidSettings.runRootCommand("rm /etc/init.d/11bottomled") && LSystem.RemountROnly());
		} catch (Exception e){}
		return false;
	}
	
}
