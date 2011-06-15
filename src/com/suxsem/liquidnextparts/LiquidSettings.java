package com.suxsem.liquidnextparts;

import java.io.DataOutputStream;
import android.util.Log;

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
	                        Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+e.getMessage());
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
	                        Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+e.getMessage());
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