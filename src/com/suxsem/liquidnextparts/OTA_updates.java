package com.suxsem.liquidnextparts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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

import com.suxsem.liquidnextparts.activities.Webview;
import com.suxsem.liquidnextparts.activities.settings;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class OTA_updates {

	private Context myactivity;
	private settings classactivity;

	public boolean ROOT = false;
	public boolean isFirstTime = false;
	public SharedPreferences prefs;

	public int SDCacheSize;
	EditTextPreference editNoise, editSensitivity, editSoftsens;
	public String otadownloadlocation = "";
	private ProgressDialog waitdialog;
	Boolean update = false;
	Boolean connection = false;

	private File localfile;
	private Editor prefeditor;
	
	public void checkupdates(Context myactivitytemp, settings classactivitytemp) {
		myactivity = myactivitytemp;
		classactivity = classactivitytemp;
		File file = new File(Environment.getExternalStorageDirectory(),
				"liquidnexttester");
		if (file.exists()) {
			return;
		}
		waitdialog = ProgressDialog.show(myactivity, "",
				"Checking for updates...", true);

		new Thread() {
			public void run() {

				try {
					ConnectivityManager connManager = (ConnectivityManager) myactivity
							.getSystemService(Context.CONNECTIVITY_SERVICE);
					android.net.NetworkInfo netInfo = connManager
							.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
					android.net.NetworkInfo wifiInfo = connManager
							.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					if (netInfo.getState() == android.net.NetworkInfo.State.CONNECTED
							|| wifiInfo.getState() == android.net.NetworkInfo.State.CONNECTED) {
						connection = true;
						
						HttpParams httpParameters = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(
								httpParameters, 10000);
						HttpConnectionParams
						.setSoTimeout(httpParameters, 10000);
						HttpClient httpClient = new DefaultHttpClient(
								httpParameters);
						HttpContext localContext = new BasicHttpContext();
						HttpGet httpGet = new HttpGet(
								myactivity.getString(R.string.lnpversioncheck));
						HttpResponse response = null;
						try {
							response = httpClient
									.execute(httpGet, localContext);
						} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
						} catch (IOException e) {
							// TODO Auto-generated catch block
						}
						if (response == null) {
							classactivity.runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(myactivity, "Server error",
											4000).show();
									return;
								}
							});
						}

						BufferedReader reader = null;
						try {
							reader = new BufferedReader(new InputStreamReader(
									response.getEntity().getContent()));
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
						} catch (IOException e) {
							// TODO Auto-generated catch block
						}

						String line = null;
						update = false;
						try {
							while ((line = reader.readLine()) != null) {
								if (!myactivity.getString(R.string.app_vname)
										.equals(line.substring(0, 5))) {
									update = true;
									Intent intent = new Intent(
											Intent.ACTION_VIEW);
									intent.setData(Uri
											.parse("market://details?id=com.suxsem.liquidnextparts"));
									myactivity.startActivity(intent);
									line = null;
								}
								if (line.substring(0, 5).equals("works")) {
									update = true;
									Toast.makeText(myactivity,
											"Updating server... Check later",
											4000).show();
									line = null;
								}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
						}
					} else {
						connection = false;
					}
					classactivity.runOnUiThread(new Runnable() {
						public void run() {
							if (update == false) {
								if (!updaterom()) {
									if (connection) {
										Toast.makeText(myactivity,
												"No new ROM updates", 4000)
												.show();
									} else {
										Toast.makeText(myactivity,
												"No Internet connection", 4000)
												.show();
									}
								}
							}
						}
					});
				} catch (Exception e) {
				}
				// dismiss the progressdialog
				waitdialog.dismiss();
			}
		}.start();
	}

	private boolean updaterom() {
		prefs = PreferenceManager.getDefaultSharedPreferences(myactivity);
		String romodversion = parsebuildprop.parseString("ro.modversion");
		String lastversion = myactivity.getString(R.string.lastversion);
		String[] romodversioncheckarray;
		try {
			romodversioncheckarray = romodversion.split("[\\s,]+")[1]
					.split("\\.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(myactivity, "ERROR in build.prop > ro.modversion",
					4000).show();
			return false;
		}
		String[] lastversioncheckarray = lastversion.split("[\\s,]+")[1]
				.split("\\.");
		Integer romodversioncheckarraynumber = 0;
		Integer lastversioncheckarraynumber = 0;
		for (int i = 0; i < romodversioncheckarray.length; i++) {
			romodversioncheckarraynumber = romodversioncheckarraynumber
					+ (int) Math.pow(10, 3 - i)
					* Integer.valueOf(romodversioncheckarray[i]);
		}
		for (int i = 0; i < lastversioncheckarray.length; i++) {
			lastversioncheckarraynumber = lastversioncheckarraynumber
					+ (int) Math.pow(10, 3 - i)
					* Integer.valueOf(lastversioncheckarray[i]);
		}
		if (lastversioncheckarraynumber > romodversioncheckarraynumber) {
			boolean possibleupdate = false;
			if (myactivity.getString(R.string.previousversion).equals("ALL")) {
				possibleupdate = true;
			} else {
				String[] previousversionarray = myactivity.getString(
						R.string.previousversion).split("/");
				for (int i = 0; i < previousversionarray.length; i++) {
					if (romodversion.split("[\\s,]+")[1]
							.equals(previousversionarray[i]))
						possibleupdate = true;
				}
			}

			if (possibleupdate) {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						myactivity);
				builder.setTitle("NEW ROM UPDATE");
				builder.setCancelable(true);
				builder.setMessage("There is a new update for your ROM!\n\nVersion: "
						+ myactivity.getString(R.string.lastversion)
						+ "\nSize: "
						+ myactivity.getString(R.string.size)
						+ " MB\nNotes: " + myactivity.getString(R.string.notes));
				builder.setPositiveButton("UPDATE",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {

						ConnectivityManager connManager = (ConnectivityManager) myactivity
								.getSystemService(Context.CONNECTIVITY_SERVICE);
						android.net.NetworkInfo netInfo = connManager
								.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
						android.net.NetworkInfo wifiInfo = connManager
								.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
						if (netInfo.getState() == android.net.NetworkInfo.State.CONNECTED
								|| wifiInfo.getState() == android.net.NetworkInfo.State.CONNECTED) {
							final String actualfilename = myactivity
									.getString(R.string.lastversion)
									.replaceAll(" ", "_");
							prefeditor = prefs.edit();
							localfile = new File(
									"/sdcard/"
											+ prefs.getString(
													"otadownloadlocation",
													""));
							final CharSequence[] items = {
									"Automatically flash after download",
									"Reboot in recovery after download",
							"Don't reboot in recovery after download" };

							AlertDialog.Builder builder = new AlertDialog.Builder(
									myactivity);
							builder.setTitle("Choose action after download");
							builder.setItems(
									items,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int item) {
											if (item == 0) {
												prefeditor
												.putString(
														"otadownloadrecovery",
														"r1");
												otadownloadlocation = "LiquidNext_autoflash.zip";
												startdownload(
														myactivity,
														true);

												return;
											} else if (item == 1) {
												prefeditor
												.putString(
														"otadownloadrecovery",
														"r2");
											} else if (item == 2) {
												prefeditor
												.putString(
														"otadownloadrecovery",
														"r3");
											}

											final CharSequence[] items = {
													"Save as /sdcard/LiquidNext_LastUpdate",
													"Save as /sdcard/"
															+ actualfilename,
											"Save as /sdcard/update" };
											AlertDialog.Builder builder = new AlertDialog.Builder(
													myactivity);
											builder.setTitle("Choose download location");
											builder.setItems(
													items,
													new DialogInterface.OnClickListener() {
														public void onClick(
																DialogInterface dialog,
																int item) {
															if (item == 0) {
																otadownloadlocation = "LiquidNext_LastUpdate.zip";
															} else if (item == 1) {
																otadownloadlocation = actualfilename
																		+ ".zip";
															} else if (item == 2) {
																otadownloadlocation = "update.zip";
															}
															prefeditor
															.putString(
																	"otadownloadlocation",
																	otadownloadlocation);
															
															if (localfile
																	.exists()) {
																final CharSequence[] items = {
																		"Use existing file",
																"Overwrite it" };
																AlertDialog.Builder builder = new AlertDialog.Builder(
																		myactivity);
																builder.setTitle("File already exists");
																builder.setItems(
																		items,
																		new DialogInterface.OnClickListener() {
																			public void onClick(
																					DialogInterface dialog,
																					int item) {
																				if (item == 0){
																					startdownload(myactivity, false);
																				}
																				if (item == 1){
																				startdownload(
																						myactivity,
																						true);
																				}
																			}
																		});
																builder.create()
																.show();
															} else {
																startdownload(
																		myactivity,
																		true);
															}
														}
													});
											builder.create().show();

										}
									});
							builder.create().show();
						} else {
							Toast.makeText(myactivity,
									"ERROR: NO CONNECTIONS!", 4000)
									.show();
						}
					}
				});
				builder.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
					}
				});
				builder.create().show();
			} else {
				final AlertDialog.Builder builder = new AlertDialog.Builder(
						myactivity);
				builder.setTitle("NEW ROM UPDATE");
				builder.setCancelable(true);
				builder.setMessage("There is a new update, but your ROM is too old to be automatically updated. Please visit Modaco website to get the last LiquidNext ROM available!");
				builder.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
					}
				});
				builder.create().show();
			}
			return true;

		}
		return false;
	}

	private void startdownload(Context myactivity, boolean downloadornot) {
		/*
		 * powermanager = (PowerManager)
		 * myactivity.getSystemService(Context.POWER_SERVICE); wifimanager =
		 * (WifiManager) myactivity.getSystemService(Context.WIFI_SERVICE);
		 * wakelockota = powermanager.newWakeLock(
		 * PowerManager.PARTIAL_WAKE_LOCK, "OTAWAKELOCK"); wifilockota =
		 * wifimanager.createWifiLock("OTAWIFILOCK");
		 * wakelockota.setReferenceCounted(false);
		 * wifilockota.setReferenceCounted(false); wakelockota.acquire();
		 * wifilockota.acquire();
		 * 
		 * DownloadTask.downloadtask = new
		 * DownloadTask(myactivity).execute(myactivity.getString(R.string.url),
		 * DownloadTaskInformations);
		 */

		if (downloadornot) {
			LiquidSettings
			.runRootCommand("rm -f "
					+ "/sdcard/"
					+ otadownloadlocation);
			Uri uri = Uri.parse(gethotlinkfrommultiupload(myactivity
					.getString(R.string.url)));
			DownloadManager dm = (DownloadManager) classactivity
					.getSystemService(Context.DOWNLOAD_SERVICE);
			try {
				dm.remove(prefs.getLong("otadownloadid", -1));
			} catch (Exception e) {
			}
			Editor edit = prefs.edit();
			edit.putBoolean("waitflash", false);
			edit.putBoolean("adsfinish", false);
			edit.commit();

			long Download = dm
					.enqueue(new DownloadManager.Request(uri)
					.setAllowedNetworkTypes(
							DownloadManager.Request.NETWORK_WIFI
							| DownloadManager.Request.NETWORK_MOBILE)
							.setTitle("LiquidNext")
							.setDescription("Downloading updates...")
							.setShowRunningNotification(true)
							.setVisibleInDownloadsUi(false)
							.setDestinationInExternalPublicDir("",
									otadownloadlocation));
			prefeditor.putLong("otadownloadid", Download);
			prefeditor.commit();

			 myactivity.startActivity(new Intent
			 (Intent.ACTION_VIEW).setClassName(myactivity,
			 Webview.class.getName()));
		} else {
			afterdownload(myactivity);
		}
	}

	private String gethotlinkfrommultiupload(String downloadpage) {
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
		if (response == null) {
			return "error";
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}

		String line = null;

		try {
			while ((line = reader.readLine()) != null) {
				int start = line
						.indexOf("<div id=\"downloadbutton_\" style=\"\"><a href=\"") + 44;
				if (start == -1) {
					return "error";
				}
				int end = line.indexOf("\"", start);

				return line.substring(start, end);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return "error";
	}
	
	public void afterdownload(Context context){
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(prefs.getBoolean("adsfinish", false)){
			flashrom(context);
		}else{
			Editor edit = prefs.edit();
			edit.putBoolean("waitflash", true);
			edit.commit();
		}
    	
	}
	
	private void flashrom(Context context){
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
    	if(prefs.getString("otadownloadrecovery","").equals("r1")){
    	
    		String tempcommand = "--update_package=SDCARD:LiquidNext_autoflash.zip";
    		LiquidSettings.runRootCommand("echo \""+tempcommand+"\" > /cache/recovery/command");
    		LiquidSettings.runRootCommand("echo reboot > /cache/recovery/lnpreboot");
    		LiquidSettings.runRootCommand("reboot recovery");
    	}
    	if(prefs.getString("otadownloadrecovery","").equals("r2")){
    		LiquidSettings.runRootCommand("reboot recovery");
    	}		
	}
}
