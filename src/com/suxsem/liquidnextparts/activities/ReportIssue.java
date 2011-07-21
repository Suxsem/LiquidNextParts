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
 * 
 */

package com.suxsem.liquidnextparts.activities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.suxsem.liquidnextparts.LiquidSettings;
import com.suxsem.liquidnextparts.R;
import com.suxsem.liquidnextparts.parsebuildprop;
import com.suxsem.liquidnextparts.components.NotificationHelper;
import com.suxsem.liquidnextparts.components.StartSystem;

public class ReportIssue extends Activity { 


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
		LiquidSettings.runRootCommand("logcat -d > /cache/lnp/logcat");
		LiquidSettings.runRootCommand("dmesg > /cache/lnp/dmesg");
		Toast.makeText(myactivity, "Logs saved!", 4000).show();
		gotlogcat = true;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reportissue_layout);
		
		myactivity = this;
		final ViewFlipper mainview = (ViewFlipper)findViewById(R.id.viewFlipper1);		
		final Button viewback = (Button)findViewById(R.id.button1);
			viewback.setEnabled(false);
		final Button viewnext = (Button)findViewById(R.id.button2);
		final Button close = (Button)findViewById(R.id.button6);
		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				myactivity.finish();
			}
		});
		viewback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mainview.showPrevious();
				viewnext.setEnabled(true);
				if (mainview.getCurrentView() == findViewById(R.id.linearLayout1)){
					viewback.setEnabled(false);					
				}
			}
		});
		viewnext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mainview.showNext();
				viewback.setEnabled(true);
				if (mainview.getCurrentView() == findViewById(R.id.linearLayout5)){
					viewnext.setEnabled(false);					
				}
			}
		});
				
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
				LiquidSettings.runRootCommand("echo \"No logcat\" > /cache/lnp/logcat");
				LiquidSettings.runRootCommand("echo \"No dmesg\" > /cache/lnp/dmesg");
				Toast.makeText(myactivity, "Choice saved!", 4000).show();		
			}
		});
		
		accountuser = (EditText)findViewById(R.id.accountuser);
		accountpassword = (EditText)findViewById(R.id.accountpassword);
		issuesummary = (EditText)findViewById(R.id.issuesummary);
		issuedescription = (EditText)findViewById(R.id.issuedescription);
		
		accountuser.setText("@gmail.com");
		accountpassword.setText("");
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
	if(/*accountuser.equals("gmail.com") ||*/ accountuser.equals("")){
		Toast.makeText(myactivity, "Invalid goocle account mail!", 4000).show();
	}else if(accountpassword.equals("")){
		Toast.makeText(myactivity, "Invalid google account password!", 4000).show();
	/*}else if(issuesummary.equals("")){
		Toast.makeText(myactivity, "Invalid issue summary!", 4000).show();
	}else if( (int)issuecategory.getSelectedItemId()== 0){
		Toast.makeText(myactivity, "Invalid issue category!", 4000).show();
	}else if ((int)issuepriority.getSelectedItemId() == 0){
		Toast.makeText(myactivity, "Invalid issue priority!", 4000).show();
	}else if (!gotlogcat){
		Toast.makeText(myactivity, "You have to take logs!", 4000).show();*/
	}else{
	

	HttpClient client = new DefaultHttpClient();
	HttpPost post = new HttpPost("https://www.google.com/accounts/ClientLogin");
	//post.setHeader("Content-Type", "application/x-www-form-urlencoded");
	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
	pairs.add(new BasicNameValuePair("accountType", "GOOGLE"));
	pairs.add(new BasicNameValuePair("Email", accountuser.getText().toString()));
	pairs.add(new BasicNameValuePair("Passwd", accountpassword.getText().toString()));
	pairs.add(new BasicNameValuePair("service", "liquidnextbugtracker"));
	pairs.add(new BasicNameValuePair("source", "Suxsem-LiquidNextParts"));
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
				                        StringBuffer sb = new StringBuffer("");
				                        String line = "";
				                        String NL = System.getProperty("line.separator");
				                        while((line = in.readLine())!= null)
				                                sb.append(line + NL);
				                        in.close();

				                        String result = sb.toString(); 
		Log.d("LS",result);
	} catch (ClientProtocolException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	String posturl = "https://code.google.com/feeds/issues/p/liquidnextbugtracker/issues/full";
	String finalxml = "";
	finalxml += "<?xml version='1.0' encoding='UTF-8'?>";
	finalxml += "<entry xmlns='http://www.w3.org/2005/Atom' xmlns:issues='http://schemas.google.com/projecthosting/issues/2009'>";
	finalxml += "<title>" + issuesummary.getText().toString() + "</title>";
	finalxml += "<content type='text/plain'>" + issuedescription.getText().toString() + "</content>";
	finalxml += "<author><name>" + accountuser.getText().toString() + "</author></name>";
	finalxml += "<issues:status>New</issues:status>";
	finalxml += "<issues:owner><issues:username>" + accountuser.getText().toString() + "</issues:username></issues:owner>";
	finalxml += "<issues:label>"+myactivity.getResources().getStringArray(R.array.issuecategory)[(int)issuecategory.getSelectedItemId()].toString()+"</issues:label>";
	finalxml += "<issues:label>"+myactivity.getResources().getStringArray(R.array.issuepriority)[(int)issuepriority.getSelectedItemId()].toString()+"</issues:label>";
	finalxml += "</entry>";
	}
	waitdialog.dismiss();	
	}
}


