package com.suxsem.liquidnextparts;

public class Strings {
	public static String getnovibr(){
		return String.format("%s\n%s\n%s\n%s", 
	            "\"#!/system/bin/sh",
	            "#script created by liquid custom settings",
	            "#",
	            "echo 0 > /sys/module/avr/parameters/vibr\""
	        );
	}
	
	public static String getvibr(){
		return String.format("%s\n%s\n%s\n%s", 
	            "\"#!/system/bin/sh",
	            "#script created by liquid custom settings",
	            "#",
	            "echo 1 > /sys/module/avr/parameters/vibr\""
	        );	
		}
	
	public static String getSens(String sens, String noise, String softkeysens){
		return String.format("%s\n%s\n%s\n%s\n%s\n%s", 
	            "\"#!/system/bin/sh",
	            "#script created by Liquid Settings App",
	            "#",
	            "echo "+sens+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/sensitivity",
	            "echo "+noise+" > /sys/devices/platform/i2c-adapter/i2c-0/0-005c/noise",
	            "echo "+softkeysens+" > /sys/devices/platform/i2c-adapter/i2c-0/0-0066/threshold\""
	        );
	}
	
	public static String getCompcacheAutostart(){
		return String.format("%s\n%s\n%s\n%s","\"#!/system/bin/sh",
				"#script created by Liquid Settings App",
				"#",
				"compcache start\""
				);
	}
	
	public static String getSdCacheSizeString(int size){
		return String.format("\"%s\n%s\n\n%s\n%s\n%s\n\"",
				"#!/system/bin/sh",
				"#script created by Liquid Settings App",
				"if [ -e /sys/devices/virtual/bdi/179:0/read_ahead_kb ]; then ",
				"/system/xbin/echo \\\"" + size + "\\\" > /sys/devices/virtual/bdi/179:0/read_ahead_kb;",
				"fi;"
		);
	}
	
	public static boolean onlyNumber(String str){
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException excp){
			return false;
		}
	}

	public static String batteryleddisable(){
		return String.format("%s\n%s\n%s\n%s\n%s", 
	            "\"#!/system/bin/sh",
	            "#script created by liquid custom settings",
	            "#",
	            "echo 0 > /sys/class/leds2/power",
	            "chmod 000 /sys/class/leds2/power\""
	        );
	}	

}

