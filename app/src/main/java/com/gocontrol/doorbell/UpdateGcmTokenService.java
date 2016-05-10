package com.gocontrol.doorbell;

import com.google.android.gms.iid.InstanceIDListenerService;

import android.content.Intent;

public class UpdateGcmTokenService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, GetGcmTokenService.class);
        startService(intent);
    }
}
