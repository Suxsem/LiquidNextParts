package com.suxsem.liquidnextparts.activities;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.suxsem.liquidnextparts.R;

public class Webview extends Activity { 

	Context myactivity = null;
	Webview webviewclass = this;
	Timer timer;
	WebView mWebView;
	boolean firstloading = false;
	TextView waittextview;
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webviewlayout);
		waittextview = (TextView) findViewById(R.id.textView1);
		waittextview.setText("Loading ADS...");
		 final CountDownTimer timer2 = new CountDownTimer(6000, 6000) {

				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					  Toast.makeText(myactivity, "Thanks", 4000).show();
					  this.cancel();
					  webviewclass.finish();
				}

				@Override
				public void onTick(long millisUntilFinished) {
					// TODO Auto-generated method stub
				}
			 
		 };
			final CountDownTimer timer1 = new CountDownTimer(20000, 1000) {

				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					  waittextview.setText("Storing click...");
					  mWebView.loadUrl("javascript:skipButton()");
					  timer2.start();
					  this.cancel();
				}

				@Override
				public void onTick(long millisUntilFinished) {
					// TODO Auto-generated method stub				      
				      waittextview.setText(Long.toString(millisUntilFinished/1000)+" seconds...");
				}
   			 
   		 };
		
		myactivity = this.getBaseContext();
		mWebView = (WebView) findViewById(R.id.webView1);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
      	   public void onPageFinished(WebView view, String url) {
      		 //timer = new Timer();
      		 //timer.schedule(new UpdateTimeTask(), 0, 1000);
      		 runOnUiThread(new Runnable() {
					public void run() {
						if(!firstloading){
						timer1.start();
						firstloading = true;
						}
					}
				});        	
      	    }
      	});
        mWebView.loadUrl(getString(R.string.adsurl));
        //mWebView.loadUrl("http://www.google.it");

	}
}


