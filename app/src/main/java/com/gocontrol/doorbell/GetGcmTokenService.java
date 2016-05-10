package com.gocontrol.doorbell;

import com.gocontrol.doorbell.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class GetGcmTokenService extends IntentService {

    private static final String TAG = GetGcmTokenService.class.getSimpleName();

    public GetGcmTokenService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	
    	// get sender ID
    	String id = getString(R.string.gcm_sender_id);
    	if (id == null || id.isEmpty()) {
    		Log.e(TAG, "could not to get gcm sender ID");
    		return;
    	}
    	
    	// get gcm token
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(id, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            boolean saveOk = AppUtils.saveGcmToken(this, token);
            if (saveOk)	Log.d(TAG, "save gcm token ok, token = " + token);
            else		Log.w(TAG, "save gcm token fail, token = " + token);
           
        } catch (Exception e) {
        	e.printStackTrace();
        	Log.e(TAG, "could not to get gcm token");
        }
    }
}
