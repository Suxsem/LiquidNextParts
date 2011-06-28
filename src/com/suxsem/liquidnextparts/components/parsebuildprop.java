package com.suxsem.liquidnextparts.components;

import java.io.*;
import com.suxsem.liquidnextparts.LSystem;
import com.suxsem.liquidnextparts.LiquidSettings;

public class parsebuildprop {
        private static String build_prop = "/system/build.prop";
       
        private static String readProperty(String prop) {
                File propFile = new File(build_prop);
               
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                DataInputStream dis = null;
               
                try {
                        fis = new FileInputStream(propFile);
                        bis = new BufferedInputStream(fis);
                        dis = new DataInputStream(bis);
                        String buffer;
                       
                        while(dis.available() > 0) {
                                buffer = dis.readLine();
                                if(buffer.startsWith(prop))
                                        return buffer.substring(buffer.indexOf("=")+1);
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
               
                return "";
        }
       
        public static String parseString(String prop) {
                return readProperty(prop);
        }
       
        public static Integer parseInt(String prop) {
                Integer result;
               
                try {
                        result = Integer.parseInt(readProperty(prop));
                } catch (Exception e) {
                        // not an integer
                        result = -1;
                }
                return result;
        }
        public static void editString (String prop, String value){
        	File propFile = new File(build_prop);
            
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            DataInputStream dis = null;
            String FinalFile = "";
            try {
                    fis = new FileInputStream(propFile);
                    bis = new BufferedInputStream(fis);
                    dis = new DataInputStream(bis);
                    String buffer;                    
                    boolean found = false;
                    while(dis.available() > 0) {
                            buffer = dis.readLine();                            
                            if(buffer.startsWith(prop)){
                                    FinalFile = FinalFile + prop + "=" + value + "\n";
                                    found = true;
                    		}else{
                    			FinalFile = FinalFile + buffer +"\n";
                    		}
                    }
                    if(found == false){
                    	FinalFile = FinalFile + prop + "=" + value + "\n";
                    }
                    	
                    fis.close();
                    bis.close();
                    dis.close();                    
        			LSystem.RemountRW();
        			LiquidSettings.runRootCommand("chmod 646 /system/build.prop");
                    BufferedWriter out = new BufferedWriter(new FileWriter("/system/build.prop"));
                    out.write(FinalFile);
                    out.flush();
                    out.close();
                    LiquidSettings.runRootCommand("chmod 644 /system/build.prop");
                    LSystem.RemountROnly();
            } catch (Exception e) {
                    e.printStackTrace();
            }
        	return;
        }
}