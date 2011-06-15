package com.suxsem.liquidnextparts.components;

import android.util.Log;

import com.suxsem.liquidnextparts.R;
import com.suxsem.liquidnextparts.components.DownloadTask;

public class NotificationHelperStopProcess {
	public NotificationHelperStopProcess myactivity = this;

    public void onCreate() {
    	Log.d("========LS=======","OK");
    	new DownloadTask(null).cancel(true);
    }
}

