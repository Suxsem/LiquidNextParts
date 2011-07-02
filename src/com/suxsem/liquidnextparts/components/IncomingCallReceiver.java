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
                	main_service.stopcall();
                }else if(state.equals("OFFHOOK")){
                	main_service.startcall();
                }
        }

}
