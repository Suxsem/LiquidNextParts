/*
 * TODO:
 * 
 * 1) fixare autenticazione con google (per ora la richiesta restituisce:
 * Error=Unknown
 * D/LS      ( 6895): Url=https://www.google.com/accounts/ErrorMsg?service=liquidnextbugtracker&id=unknown&timeStmp=1311211260&secTok=.AG5fkS-z27e48BTzkT0YAjFDdSFQmGxOXA%3D%3D )
 * 
 * 2) aggiungere l'invio dei files di testo /cache/lnp/logcat e /cache/lnp/dmesg a pastebin e aggiungere i link di pastebin alla fine della variabile issuedescription
 * 
 * 3) aggiungere l'invio effettivo dell'xml
 * 
 * 
 * 
 * FUNZIONI:
 * onCreate: inizializza l'interfaccia utente
 * getlog: salva logcat e dmesg nella cache
 * sendissue: richiama e invia i dati inseriti al bugtracker
 * no 
 */

package com.suxsem.liquidnextparts.activities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.suxsem.liquidnextparts.LiquidSettings;
import com.suxsem.liquidnextparts.R;
import com.suxsem.liquidnextparts.UploadOnPastebin;
import com.suxsem.liquidnextparts.parsebuildprop;

public class ReportIssue extends Activity implements OnClickListener { 

	ReportIssue myactivity = null;
	ReportIssue webviewclass = this;
	Timer timer;
	WebView mWebView;
	boolean firstloading = false;
	TextView waittextview;
	ProgressDialog waitdialog;
	boolean waitinglogs = false;
	boolean gotlogcat = false;
	NotificationManager mNotificationManager;
	EditText accountuser;
	EditText accountpassword;
	EditText issuesummary;
	EditText issuedescription;
	Spinner issuecategory;
	Spinner issuepriority;
	SharedPreferences prefs;
	String toastmessage = "";
	boolean isnewissue = true;
	ArrayList<String> title = new ArrayList<String>();
	ArrayList<String> status = new ArrayList<String>();
	ArrayList<String> id = new ArrayList<String>();
	ViewFlipper mainview;
	Button viewback;
	Button viewnext;
	String updateissueurl = "";
	
	@Override
	public void onBackPressed() {
		Intent myintent = new Intent (Intent.ACTION_VIEW);
		myintent.setClassName(this.getBaseContext(), settings.class.getName());
		startActivity(myintent);
		this.finish();
	return;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(waitinglogs){
			waitinglogs = false;
			mNotificationManager.cancel(6);
			getlog();
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if(!waitinglogs){
			try {
				mNotificationManager.cancel(6);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		this.finish();
		return;
		}
	}
	
	private void getlog(){
		LiquidSettings.runRootCommand("mkdir /cache/lnp");
		LiquidSettings.runRootCommand("logcat -d > /cache/lnp/logcat");
		LiquidSettings.runRootCommand("dmesg > /cache/lnp/dmesg");
		Toast.makeText(myactivity, "Logs saved!", 4000).show();
		gotlogcat = true;
	}
	
	private void changestep(boolean forward){
		if(forward){
			mainview.showNext();
		}else{
			mainview.showPrevious();
		}
		viewback.setEnabled(true);
		viewnext.setEnabled(true);
		if (mainview.getCurrentView() == findViewById(R.id.linearLayout1)){
			viewback.setEnabled(false);
		}
		if (mainview.getCurrentView() == findViewById(R.id.linearLayout6)){
			viewnext.setEnabled(false);					
		}
		if (mainview.getCurrentView() == findViewById(R.id.linearLayout2)){
			viewnext.setEnabled(false);
			viewnext.setEnabled(false);
		}
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId()!=0){
			isnewissue = false;
			issuesummary.setText(title.get(v.getId() -1 ));
			issuesummary.setEnabled(false);
			updateissueurl = id.get(v.getId() -1 );
		}
		changestep(true);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reportissue_layout);
		
		
		myactivity = this;
				
		prefs = PreferenceManager.getDefaultSharedPreferences(myactivity);
		
		mainview = (ViewFlipper)findViewById(R.id.viewFlipper1);		
		viewback = (Button)findViewById(R.id.button1);
			viewback.setEnabled(false);
		viewnext = (Button)findViewById(R.id.button2);
		final Button close = (Button)findViewById(R.id.button6);
		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent myintent = new Intent (Intent.ACTION_VIEW);
				myintent.setClassName(myactivity, settings.class.getName());
				startActivity(myintent);
				myactivity.finish();
			}
		});
		viewback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				changestep(false);

			}
		});
		viewnext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				changestep(true);
			}
		});
				
		LinearLayout rootview = (LinearLayout) findViewById(R.id.linearLayout_innserll2);
		TextView textView;
	    
		//GET ISSUES
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		HttpConnectionParams.setSoTimeout(httpParameters, 10000);
		HttpClient httpClient = new DefaultHttpClient(httpParameters);
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet("https://code.google.com/feeds/issues/p/liquidnextbugtracker/issues/full?can=open");					
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpGet, localContext);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(response==null){
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
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String line = null;
		String fullresponse = "";
		ArrayList<Integer> titlestart = new ArrayList<Integer>();
		ArrayList<Integer> titleend = new ArrayList<Integer>();
		ArrayList<Integer> statusstart = new ArrayList<Integer>();
		ArrayList<Integer> statusend = new ArrayList<Integer>();
		ArrayList<Integer> idstart = new ArrayList<Integer>();
		ArrayList<Integer> idend = new ArrayList<Integer>();
		
		int tempindexof;
		try {
			while ((line = reader.readLine()) != null){		     	  			  
				fullresponse += line;				
			}
			tempindexof = fullresponse.indexOf("<title>");
			if (tempindexof != -1){
			tempindexof = 0;
			while(tempindexof >= 0) {
			     tempindexof = fullresponse.indexOf("<title>", tempindexof+1);
			     titlestart.add(tempindexof);
			}
			}
			tempindexof = fullresponse.indexOf("</title>");
			if (tempindexof != -1){
			tempindexof = 0;
			while(tempindexof >= 0) {
			     tempindexof = fullresponse.indexOf("</title>", tempindexof+1);
			     titleend.add(tempindexof);
			}
			}
			tempindexof = fullresponse.indexOf("<issues:status>");
			if (tempindexof != -1){
			tempindexof = 0;				
			while(tempindexof >= 0) {
			     tempindexof = fullresponse.indexOf("<issues:status>", tempindexof+1);
			     statusstart.add(tempindexof);
			}
			}
			tempindexof = fullresponse.indexOf("</issues:status>");
			if (tempindexof != -1){
			tempindexof = 0;
			while(tempindexof >= 0) {
			     tempindexof = fullresponse.indexOf("</issues:status>", tempindexof+1);
			     statusend.add(tempindexof);
			}			
			}
			tempindexof = fullresponse.indexOf("<link rel='replies' type='application/atom+xml' href='");
			if (tempindexof != -1){
			tempindexof = 0;				
			while(tempindexof >= 0) {
			     tempindexof = fullresponse.indexOf("<link rel='replies' type='application/atom+xml' href='", tempindexof+1);
			     idstart.add(tempindexof);
			}
			}
			tempindexof = fullresponse.indexOf("'/><link rel='alternate' type='text/html' href='");
			if (tempindexof != -1){
			tempindexof = 0;
			while(tempindexof >= 0) {
			     tempindexof = fullresponse.indexOf("'/><link rel='alternate' type='text/html' href='", tempindexof+1);
			     idend.add(tempindexof);
			}			
			}
			for (int titlecount = 0; titlecount < titlestart.size() -1; titlecount++){
				String titletemp = fullresponse.substring(titlestart.get(titlecount)+"<title>".length(),titleend.get(titlecount));
				if(!titletemp.equals("Issues - liquidnextbugtracker"))
				title.add(titletemp);
			}
			for (int idcount = 0; idcount < idstart.size() -1; idcount++){
				id.add(fullresponse.substring(idstart.get(idcount)+"<link rel='replies' type='application/atom+xml' href='".length(),idend.get(idcount)));
			}
		    textView = new TextView(this);		 
		    textView.setText("\n#) CREATE A NEW ISSUE\n");
		    textView.setOnClickListener(myactivity);
		    textView.setId(0);
		    rootview.addView(textView);			
			for (int statuscount = 0; statuscount < statusstart.size() -1; statuscount++){
				status.add(fullresponse.substring(statusstart.get(statuscount)+"<issues:status>".length(),statusend.get(statuscount)));
			    textView = new TextView(this);		 
			    textView.setText("#) "+title.get(statuscount)+"\nStatus: "+status.get(statuscount)+"\n");
			    textView.setOnClickListener(myactivity);
			    textView.setId(statuscount +1);
			    rootview.addView(textView);				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

		/*final Button openbugtracker = (Button)findViewById(R.id.button8);
		openbugtracker.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "http://code.google.com/p/liquidnextbugtracker/issues/list";  
				Intent i = new Intent(Intent.ACTION_VIEW);  
				i.setData(Uri.parse(url));  
				startActivity(i);
			}
		});*/
		
		final Button getlocat = (Button)findViewById(R.id.button3);
		final Button getlocatnow = (Button)findViewById(R.id.button4);
		final Button getlocatnever = (Button)findViewById(R.id.button5);
		getlocat.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				waitinglogs = true;
				String tickerText = "Now reproduce the bug...";
		        mNotificationManager = (NotificationManager) myactivity.getSystemService(Context.NOTIFICATION_SERVICE);
		        Notification mNotification = new Notification(android.R.drawable.stat_sys_warning, tickerText, System.currentTimeMillis());
		        Intent intent;
		        intent = new Intent(myactivity, ReportIssue.class);
		        PendingIntent mContentIntent = PendingIntent.getActivity(myactivity, 0, intent, 0);
				RemoteViews contentView = new RemoteViews(myactivity.getPackageName(), R.layout.notification_custom_layout);
		        contentView.setImageViewResource(R.id.notification_layout_image, R.drawable.icon);
		        contentView.setTextViewText(R.id.notification_layout_text1, "LNP - Report an issue");
		        contentView.setTextViewText(R.id.notification_layout_text2, "Issue reproduced?");
		        mNotification.contentView = contentView;
		        mNotification.contentIntent = mContentIntent;
		        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
		        mNotificationManager.notify(6, mNotification);
		        Intent i = new Intent(); 
		        i.setAction(Intent.ACTION_MAIN); 
		        i.addCategory(Intent.CATEGORY_HOME); 
		        myactivity.startActivity(i);
			}
		});
		getlocatnow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getlog();					
			}
		});
		getlocatnever.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gotlogcat = true;				
				LiquidSettings.runRootCommand("mkdir /cache/lnp");
				LiquidSettings.runRootCommand("echo \"No logcat\" > /cache/lnp/logcat");
				LiquidSettings.runRootCommand("echo \"No dmesg\" > /cache/lnp/dmesg");
				Toast.makeText(myactivity, "Choice saved!", 4000).show();		
			}
		});
		
		accountuser = (EditText)findViewById(R.id.accountuser);
		accountpassword = (EditText)findViewById(R.id.accountpassword);
		issuesummary = (EditText)findViewById(R.id.issuesummary);
		issuedescription = (EditText)findViewById(R.id.issuedescription);
		
		
		accountuser.setText(prefs.getString("googleaccountmail", "@gmail.com"));
		accountpassword.setText(prefs.getString("googleaccountpassword", ""));
		issuesummary.setText("");
		String issuedescripttext = "";
		issuedescripttext += "=============" + "\n";
		issuedescripttext += "Some information about your configuration..." + "\n-------------\n";
		issuedescripttext += "ROM version: " + parsebuildprop.parseString("ro.modversion") + "\n-------------\n";
		issuedescripttext += "Full wipe or update (and from which version)?:" + "\n-------------\n";
		issuedescripttext += "LNP version: " + this.getString(R.string.app_vname)+ "\n-------------\n";
		try {

			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process
					.getOutputStream());
			DataInputStream osRes = new DataInputStream(process
					.getInputStream());

			os.writeBytes("cat /proc/version\n");

			issuedescripttext += "Kernel version and build number: " + osRes.readLine() + "\n-------------\n";

			os.flush();

			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		issuedescripttext += "PC operative system (if the issue is PC related):" + "\n-------------\n";
		issuedescripttext += "\n";
		issuedescripttext += "=============" + "\n";
		issuedescripttext += "What steps will reproduce the problem (PLEASE BE ACCURATE - IF WE CAN'T REPRODUCE THE ISSUE WE CAN'T FIX IT)?\n";
		issuedescripttext += "1.\n";
		issuedescripttext += "2.\n";
		issuedescripttext += "3.\n";
		issuedescripttext += "...\n";
		issuedescripttext += "\n";
		issuedescripttext += "=============" + "\n";
		issuedescripttext += "What is the expected output? What do you see instead?\n";
		issuedescripttext += "\n";
		issuedescripttext += "\n";
		issuedescripttext += "=============" + "\n";
		issuedescripttext += "Have you edited the ROM in someway? How?\n";
		issuedescripttext += "\n";
		issuedescripttext += "\n";
		issuedescripttext += "=============" + "\n";
		issuedescripttext += "Have you tried to make a full wipe, flash again the ROM and test again the issue?\n";
		issuedescripttext += "\n";
		issuedescripttext += "\n";
		issuedescripttext += "=============" + "\n";
		issuedescripttext += "Please provide any additional information below.\n";
		issuedescripttext += "\n";
		issuedescripttext += "\n";
		issuedescripttext += "\n";
		issuedescripttext += "\n";
		issuedescription.setText(issuedescripttext);
		
		issuecategory = (Spinner)findViewById(R.id.spinner1);
		issuepriority = (Spinner)findViewById(R.id.spinner2);
		final Button sendissue = (Button)findViewById(R.id.button7);
		sendissue.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendissue();	
			}
		});
	}
	
	private void sendissue(){
	waitdialog = ProgressDialog.show(myactivity, "Report an issue", 
				"Sending issue...", true);
	
	new Thread()
	{
		public void run() 
		{

			ConnectivityManager connManager =  (ConnectivityManager)myactivity.getSystemService(Context.CONNECTIVITY_SERVICE);
			android.net.NetworkInfo netInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			android.net.NetworkInfo wifiInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			 	
			if (netInfo.getState() != android.net.NetworkInfo.State.CONNECTED &&
					wifiInfo.getState() != android.net.NetworkInfo.State.CONNECTED  ) {
				toastmessage = "No Internet connection!";
			}else if(/*accountuser.equals("gmail.com") ||*/ accountuser.equals("")){
				toastmessage = "Invalid goocle account mail!";
			}else if(accountpassword.equals("")){
				toastmessage = "Invalid google account password!";
			}else if(issuesummary.equals("")){
				toastmessage = "Invalid issue summary!";
			}else if(isnewissue && (int)issuecategory.getSelectedItemId()== 0){
				toastmessage = "Invalid issue category!";
			}else if(isnewissue && (int)issuepriority.getSelectedItemId() == 0){
				toastmessage = "Invalid issue priority!";
			}else if(!gotlogcat){
				toastmessage = "You have to take logs!";
			}else
			{

				Editor editor = prefs.edit();
				editor.putString("googleaccountmail", accountuser.getText().toString());
				editor.putString("googleaccountpassword", accountpassword.getText().toString());
				editor.commit();

				String authtoken = "";

				//AUTHENTICATION
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost("https://www.google.com/accounts/ClientLogin");
				post.setHeader("Content-Type", "application/x-www-form-urlencoded");
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("accountType", "GOOGLE"));
				pairs.add(new BasicNameValuePair("Email", accountuser.getText().toString()));
				pairs.add(new BasicNameValuePair("Passwd", accountpassword.getText().toString()));
				pairs.add(new BasicNameValuePair("service", "code"));
				pairs.add(new BasicNameValuePair("source", "LiquidNextParts"));
				try {
					post.setEntity(new UrlEncodedFormEntity(pairs));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					HttpResponse response = client.execute(post);
					BufferedReader in = new BufferedReader(new
							InputStreamReader(response.getEntity().getContent()));
					String line = "";
					while((line = in.readLine())!= null)
						if(line.substring(0,4).equals("Auth")){
							authtoken = line.substring(5,line.length());
							break;
						}
					in.close();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	

				String pastebin_logcat_url = "";
				String pastebin_dmesg_url = "";
				String pastebin_buildprop_url = "";
				try {
					pastebin_logcat_url = UploadOnPastebin.sub_paste(new File("/cache/lnp/logcat"));
					pastebin_dmesg_url = UploadOnPastebin.sub_paste(new File("/cache/lnp/dmesg"));
					pastebin_buildprop_url = UploadOnPastebin.sub_paste(new File("/system/build.prop"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String addpastebinlinks = "\n\nLOGCAT: " + pastebin_logcat_url + "\nDMESG: " + pastebin_dmesg_url + "\nBUILD.PROP: " + pastebin_buildprop_url + "\n\nPOWERED BY LIQUIDNEXTPARTS APP";
				String finalxml = "";
												
				if(isnewissue){
				finalxml += "<?xml version='1.0' encoding='UTF-8'?>";
				finalxml += "<entry xmlns='http://www.w3.org/2005/Atom' xmlns:issues='http://schemas.google.com/projecthosting/issues/2009'>";
				finalxml += "<title>" + issuesummary.getText().toString() + "</title>";
				finalxml += "<content type='html'>" + issuedescription.getText().toString() + addpastebinlinks + "</content>";
				finalxml += "<author><name>" + accountuser.getText().toString() + "</name></author>";
				finalxml += "<issues:status>New</issues:status>";
				finalxml += "<issues:owner><issues:username>" + accountuser.getText().toString() + "</issues:username></issues:owner>";
				finalxml += "<issues:label>"+myactivity.getResources().getStringArray(R.array.issuecategory)[(int)issuecategory.getSelectedItemId()].toString()+"</issues:label>";
				finalxml += "<issues:label>"+myactivity.getResources().getStringArray(R.array.issuepriority)[(int)issuepriority.getSelectedItemId()].toString()+"</issues:label>";
				finalxml += "</entry>";
				}else{
					finalxml += "<?xml version='1.0' encoding='UTF-8'?>";
					finalxml += "<entry xmlns='http://www.w3.org/2005/Atom' xmlns:issues='http://schemas.google.com/projecthosting/issues/2009'>";
					finalxml += "<content type='html'>" + issuedescription.getText().toString() + addpastebinlinks + "</content>";
					finalxml += "<author><name>" + accountuser.getText().toString() + "</name></author>";
					finalxml += "<issues:updates></issues:updates></entry>";					
				}
				//SEND ISSUE
				Log.d("LS",authtoken);
				Log.d("LS",finalxml);
				// Make connection

				URL url = null;
				HttpURLConnection urlConnection;
				try {
					if(isnewissue){
					url = new URL("https://code.google.com/feeds/issues/p/liquidnextbugtracker/issues/full");
					}else{
						url = new URL(updateissueurl);
					}
					urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setRequestMethod("POST");
					urlConnection.setRequestProperty("Connection", "close"); 
					urlConnection.setRequestProperty("Content-Type", "application/atom+xml");
					urlConnection.setRequestProperty("Authorization", "GoogleLogin auth="+authtoken+"");
					urlConnection.setRequestProperty("Content-Length", Integer.toString(finalxml.length()));		

					urlConnection.setDoOutput(true);		
					OutputStreamWriter out = new OutputStreamWriter(
							urlConnection.getOutputStream());

					// Write query string to request body

					out.write(finalxml);
					out.flush();

					if (urlConnection.getHeaderField(0).equals("HTTP/1.1 201 Created")){
						toastmessage = "New issue has been sent!";
					}else{
						toastmessage = "ERROR: " + urlConnection.getHeaderField(0);			
					}
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			myactivity.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(myactivity, toastmessage, 4000).show();
				}});
			waitdialog.dismiss();
		}
	}.start();		
	}
}