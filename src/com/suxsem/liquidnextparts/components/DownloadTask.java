package com.suxsem.liquidnextparts.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.suxsem.liquidnextparts.activities.OTA_updates_status;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

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
	@SuppressWarnings("unchecked")
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

			conn = (HttpURLConnection) new URL(url[0]).openConnection();       
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
					NotificationHelper.cancelled();
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
				// update progress bar

				try {
					if (previousperc == (int) ((downloaded / fileSize) * 100)){
					}else{
						previousperc = (int) ((downloaded / fileSize) * 100);
						publishProgress();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
					e.printStackTrace();
					return null;
				}

				d = Drawable.createFromStream((InputStream) new ByteArrayInputStream(out.toByteArray()), "filename");
				return d;
				// end of if isOnline            
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
			e.printStackTrace();
		}
		mNotificationHelper.progressUpdate(previousperc);

	}

	@Override
	protected void onPreExecute()
	{
		main_service.startota();
		mNotificationHelper.createNotification();
	}

	@Override
	protected void onPostExecute(Drawable result)
	{
		main_service.stopota();
		try {
			conn.disconnect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean error;
		if(status == COMPLETE){    	
			error = false;
		}else{
			error = true; 		
		}
		mNotificationHelper.completed(error,gorecovery,filelocation,partialfilelocation);
		// do something
	}
}

