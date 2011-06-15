package com.suxsem.liquidnextparts.components;

import com.suxsem.liquidnextparts.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;

public class Eula {
	static interface OnEulaAgreedTo {
        void onEulaAgreedTo();
    }

	private static String version;
	
    public static boolean show(final Activity activity) {
    	version = activity.getString(R.string.app_vname);
        final SharedPreferences preferences = activity.getSharedPreferences("LS-EULA",Activity.MODE_PRIVATE);
        if (!preferences.getBoolean("EULA-accepted-v"+version, false)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Liquid Settings v" + activity.getString(R.string.app_vname)+" EULA");
            builder.setCancelable(true);
            builder.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    accept(preferences);
                    if (activity instanceof OnEulaAgreedTo) {
                        ((OnEulaAgreedTo) activity).onEulaAgreedTo();
                    }
                }
            });
            builder.setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    refuse(activity);
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    refuse(activity);
                }
            });
            builder.setMessage(readEula(activity));
            builder.create().show();
            return false;
        }
        return true;
    }

    private static void accept(SharedPreferences preferences) {
        preferences.edit().putBoolean("EULA-accepted-v"+version, true).commit();
    }

    private static void refuse(Activity activity) {
        activity.finish();
    }

    private static CharSequence readEula(Activity activity) {
    	StringBuilder builder=new StringBuilder();
    	builder.append(activity.getString(R.string.EULA_ASSETS));
    	return builder;
    }
   
}
