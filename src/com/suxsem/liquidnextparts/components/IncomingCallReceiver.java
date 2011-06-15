package com.suxsem.liquidnextparts.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

public class IncomingCallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                
                if(null == bundle)
                        return;
                String state = bundle.getString(TelephonyManager.EXTRA_STATE);                
                if (state.equals("IDLE")){        
                	ProxService.terminaservizio();
                }else if(state.equals("OFFHOOK")){
                	Intent servizio = new Intent(context, ProxService.class);
              		context.startService(servizio);
                }
        }

}
