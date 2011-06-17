package com.suxsem.liquidnextparts;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.content.Context;

public class DiskSpace {
	private static String result;
	private static Process process;
	private static String output;
	
	public static String getdiskspace(){
		output = "";
		// system
		try {

			process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process
					.getOutputStream());
			DataInputStream osRes = new DataInputStream(process
					.getInputStream());

			os.writeBytes("df -h /system | awk '{print $6 \" \" $3 \"/\" $2}'| tail -n 1\n");

			result = osRes.readLine();
			output = output + result + "\n";

			os.flush();

			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// data
		try {

			process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process
					.getOutputStream());
			DataInputStream osRes = new DataInputStream(process
					.getInputStream());

			os.writeBytes("df -h /data | awk '{print $6 \" \" $3 \"/\" $2}'| tail -n 1\n");

			result = osRes.readLine();
			output = output + result + "\n";

			os.flush();

			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// sdcard
		try {

			process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process
					.getOutputStream());
			DataInputStream osRes = new DataInputStream(process
					.getInputStream());

			os.writeBytes("df -h /sdcard | awk '{print $5 \" \" $2 \"/\" $1}'| tail -n 1\n");

			result = osRes.readLine();
			output = output + result + "\n";

			os.flush();

			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// system/sd
		try {

			process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process
					.getOutputStream());
			DataInputStream osRes = new DataInputStream(process
					.getInputStream());

			os.writeBytes("df -h /sd-ext | awk '{print $6 \" \" $3 \"/\" $2}'| tail -n 1\n");

			result = osRes.readLine();
			output = output + result + "\n";

			os.flush();

			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return	output;
	}
	
}
