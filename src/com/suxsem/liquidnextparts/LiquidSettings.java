package com.suxsem.liquidnextparts;

import java.io.DataOutputStream;

public class LiquidSettings {
	
	public static boolean isRoot(){
		return runRootCommand("echo $?");
	}
	
	public static boolean runRootCommand(String command) {
	      Process process = null;
	      DataOutputStream os = null;
	      try {
	                process = Runtime.getRuntime().exec("su");
	                os = new DataOutputStream(process.getOutputStream());
	                os.writeBytes(command+"\n");
	                os.writeBytes("exit\n");
	                os.flush();
	                process.waitFor();
	                } catch (Exception e) {
	                        return false;
	                }
	                finally {
	                        try {
	                             if (os != null) os.close();
	                             process.destroy();
	                        } catch (Exception e) {}
	                }
	                return true;
	}
	public static boolean runCommand(String command) {
	      Process process = null;
	      DataOutputStream os = null;
	      try {
	                process = Runtime.getRuntime().exec("");
	                os = new DataOutputStream(process.getOutputStream());
	                os.writeBytes(command+"\n");
	                os.writeBytes("exit\n");
	                os.flush();
	                process.waitFor();
	                } catch (Exception e) {
	                        return false;
	                }
	                finally {
	                        try {
	                             if (os != null) os.close();
	                             process.destroy();
	                        } catch (Exception e) {}
	                }
	                return true;
	}

}