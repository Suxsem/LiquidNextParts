package com.suxsem.liquidnextparts;

public class BatteryLED {

	public static boolean setdisable(boolean opt){
		try{
			if(opt == true)
				return (LSystem.RemountRW() & LiquidSettings.runRootCommand("echo " + Strings.batteryleddisable() + " > /etc/init.d/10batteryled") && LiquidSettings.runRootCommand("chmod +x /etc/init.d/10batteryled") && LSystem.RemountROnly());
			else
				return (LSystem.RemountRW() && LiquidSettings.runRootCommand("rm /etc/init.d/10batteryled") && LSystem.RemountROnly());
		} catch (Exception e){}
		return false;
	}
	
}
