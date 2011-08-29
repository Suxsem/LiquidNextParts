package com.suxsem.liquidnextparts.components;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.suxsem.liquidnextparts.OTA_updates;
import com.suxsem.liquidnextparts.activities.OTA_updates_status;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadTask extends AsyncTask<String, Integer, Drawable>
{

	private Drawable d;
	private HttpURLConnection conn;
	private InputStream stream; //to read
	private ByteArrayOutputStream out; //to write

	public static double fileSize;
	private double localFileSize;
	public static double downloaded; // number of bytes downloaded
	private int status = DOWNLOADING; //status of current process

	private static final int MAX_BUFFER_SIZE = 3000000; //bytes
	private static final int DOWNLOADING = 0;
	private static final int COMPLETE = 1;
	private String filelocation = "";
	private String gorecovery = "";
	private String partialfilelocation = "";
	public static Integer previousperc = 0;
	static NotificationHelper mNotificationHelper;
	public static AsyncTask downloadtask;
	public DownloadTask(Context context){
		mNotificationHelper = new NotificationHelper(context);        
	}

	public void DownloadManager()
	{
		d          = null;
		conn       = null;
		fileSize   = 0;
		downloaded = 0;
		status     = DOWNLOADING;
	}


	@Override
	protected Drawable doInBackground(String... url)
	{
		try
		{
			String[] DownloadTaskInformations = url[1].split("#");
			filelocation = DownloadTaskInformations[0];
			gorecovery = DownloadTaskInformations[1];
			partialfilelocation = DownloadTaskInformations[2];

			if (gorecovery.equals("r1")){
				filelocation = "/sdcard/LiquidNext_autoflash.zip";
			}
			String hotlink = gethotlinkfrommultiupload(url[0]);
			if (hotlink.equals("error")){
				NotificationHelper.arg = filelocation + "#" + gorecovery + "#" + partialfilelocation;
				NotificationHelper.urlDecodeError();
				d = Drawable.createFromStream((InputStream) new ByteArrayInputStream(out.toByteArray()), "filename");
				return d;
			}		
			conn = (HttpURLConnection) new URL(hotlink).openConnection();       
			//conn.setRequestProperty("Connection", "close");                                
			File localfile = new File(partialfilelocation);
			if(localfile.exists()){
				localFileSize = downloaded = localfile.length();               
			}else{
				localFileSize = downloaded = 0;
			}
			if (localFileSize > 0) {
				conn.setRequestProperty("Range", "bytes=" +(int) localFileSize + "-");

				conn.setConnectTimeout(25000);
				conn.setReadTimeout(25000);    
				conn.setDoInput(true);
				conn.setDoOutput(true);


			} 
			conn.connect();
			/*int responseCode = conn.getResponseCode();
                switch (responseCode) { 
                case HttpURLConnection.HTTP_OK:
                	localFileSize = 0;
                    break;
                }*/

			fileSize = conn.getContentLength() + localFileSize;                

			FileOutputStream fos = new FileOutputStream(partialfilelocation, true);
			stream = conn.getInputStream();
			// loop with step
			while (status == DOWNLOADING)
			{
				if (isCancelled()){
					NotificationHelper.arg = filelocation + "#" + gorecovery + "#" + partialfilelocation;
					NotificationHelper.cancelled();
					OTA_updates.releaselocks();
					break;                        	
				}

				byte buffer[];
				if (fileSize - downloaded > MAX_BUFFER_SIZE)
				{
					buffer = new byte[MAX_BUFFER_SIZE];
					out = new ByteArrayOutputStream(MAX_BUFFER_SIZE);
				}
				else
				{
					buffer = new byte[(int) (fileSize - downloaded)];
					out = new ByteArrayOutputStream((int) (fileSize - downloaded));
				}

				try {
					int read = stream.read(buffer);

					if (read == -1)
					{
						publishProgress(100);
						break;
					}
					// writing to buffer
					out.write(buffer, 0, read);
					fos.write(out.toByteArray());
					downloaded += read;
				} catch (Exception e1) {
					// TODO Auto-generated catch block
				}

				try {
					if (previousperc == (int) ((downloaded / fileSize) * 100)){
					}else{
						previousperc = (int) ((downloaded / fileSize) * 100);
						publishProgress();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
			} // end of while

				if (status == DOWNLOADING)
				{
					status = COMPLETE;
				}
				try
				{                    
					fos.close();
				}
				catch ( IOException e )
				{
					return null;
				}

				d = Drawable.createFromStream((InputStream) new ByteArrayInputStream(out.toByteArray()), "filename");
				return d;
				// end of if isOnline            
		}
		catch (Exception e)
		{
			return null;
		}// end of catch
	} // end of class DownloadManager()

	@Override
	protected void onProgressUpdate(Integer... progress)
	{
		try {
			OTA_updates_status.progressbar.setProgress(previousperc);
			OTA_updates_status.infoprogressbar.setText("Downloaded " + (int)(downloaded/1000000) +" MB of " + (int)(fileSize/1000000) + " MB ("+previousperc+"%)");
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		mNotificationHelper.progressUpdate(previousperc);

	}

	@Override
	protected void onPreExecute()
	{	
		mNotificationHelper.createNotification();
	}

	@Override
	protected void onPostExecute(Drawable result)
	{
		OTA_updates.releaselocks();
		try {
			conn.disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		boolean error;
		if(status == COMPLETE){    	
			error = false;
		}else{
			error = true; 		
		}
		NotificationHelper.arg = filelocation + "#" + gorecovery + "#" + partialfilelocation;		
		mNotificationHelper.completed(error);
		// do something
	}
	
	private String gethotlinkfrommultiupload(String downloadpage){
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		HttpConnectionParams.setSoTimeout(httpParameters, 10000);
		HttpClient httpClient = new DefaultHttpClient(httpParameters);
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(downloadpage);					
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpGet, localContext);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		if(response==null){
					return "error";
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(
							response.getEntity().getContent()
					)
			);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}

		String line = null;

		try {
			while ((line = reader.readLine()) != null){		     	  			  
				int start = line.indexOf("<div id=\"downloadbutton_\" style=\"\"><a href=\"") + 44;
				if(start==-1){
					return "error";
				}
				int end = line.indexOf("\"",start);
				
				return line.substring(start, end);
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return "error";
	}
}

