package com.gocontrol.doorbell;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.iptnet.c2c.C2CHandle;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {

    private static final String TAG = GcmListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {

    	Log.d(TAG , "GET GCM MSG:" +  data.getString("msg", ""));
    	if (AppUtils.isServiceRunning(this, DoorService.class)) {
    		Log.w(TAG, "service is running, drop the GCM message");
    		return;
    	}else {
    		Log.d(TAG, "receive GCM data =" + from);
    	}

    	
        // get message from GCM
        String msg = data.getString("msg", "");
        String time = data.getString("time", "");
        String peerId = data.getString("camid", "");
        
        // clear the notification setting when door information is not exist
        if (Door.getIndexById(this, peerId) < 0) {
        	int ret = C2CHandle.getInstance().setNotification("", peerId, 0);
        	Log.d(TAG, "could not find the door information on device");
        	Log.d(TAG, ">> clear the GCM notification setting (" + ret + ")");
        	return;
        }

		// get session code
		//int sessionCode = Integer.MIN_VALUE;
		String[] split = msg.split("=");
		if (split != null && split.length > 1) {
			Log.d(TAG, "GCM message type:" + split[0]);/*

			if (split[0] != null && split[0].equalsIgnoreCase("RING")) {
				String[] split2 = split[1].split(";");
				if (split2 != null && split2.length > 0) {
					sessionCode = Integer.valueOf(split2[0]);
				}
				// get time by long type format
				long lTime = 0;
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy-MM-dd mm:dd:ss", Locale.getDefault());
				try {
					lTime = format.parse(time).getTime();
					// FIXME force time, because the received time from GCM is
					// abnormal.
					lTime = System.currentTimeMillis();

				} catch (ParseException e) {
					Log.e(TAG,
							"the time fomrat is not 'yyyy-MM-dd mm:dd:ss', drop the received message by GCM");
				}

				// new connection
				RingEventProcess.initialize(getApplicationContext());
				boolean addOk = RingEventProcess.getInstance().newConnection(
						new RingEvent(peerId, sessionCode, lTime));
				if (!addOk)
					Log.w(TAG, "new connection fail (GCM)");
			}
		*/}else {
			if(msg != null && msg.contains("Motion Detected"))
			{
				if( !TextUtils.isEmpty(msg))
				{
					
					 NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				     NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				     .setSmallIcon(R.drawable.ic_launcher)
				     .setContentTitle("MyDoorbell")
				     .setContentText(msg);
				     Notification  notification = mBuilder.build();
				     notification.defaults = Notification.DEFAULT_ALL;
				     notificationManager.notify(1, notification);
				}
			}
			else if(msg != null && msg.contains("PIR Detected"))
			{
				if( !TextUtils.isEmpty(msg))
				{
					
					 NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				     NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				     .setSmallIcon(R.drawable.ic_launcher)
				     .setContentTitle("MyDoorbell")
				     .setContentText(msg);
				     Notification  notification = mBuilder.build();
				     notification.defaults = Notification.DEFAULT_ALL;
				     notificationManager.notify(1, notification);
				}
			}else if(msg != null && msg.contains("Someone is at your"))
			{
				String tmp = data.getString("msg_i", "");
				if(TextUtils.isEmpty(tmp))
				{
					Log.d("Tecom", "msg_i is null..." );
					return;
				}
		        int sessionCode =Integer.valueOf(tmp);
		        // get time by long type format
		        long lTime = 0;
		        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd mm:dd:ss", Locale.getDefault());
		        try {
					lTime = format.parse(time).getTime();
					// FIXME force time, because the received time from GCM is abnormal.
					lTime = System.currentTimeMillis();

				} catch (ParseException e) {
					Log.e(TAG, "the time fomrat is not 'yyyy-MM-dd mm:dd:ss', drop the received message by GCM");
				}
		        
		        // new connection
		        RingEventProcess.initialize(getApplicationContext());
		        boolean addOk = RingEventProcess.getInstance().newConnection(new RingEvent(peerId, sessionCode, lTime));
		        if (!addOk) Log.w(TAG, "new connection fail (GCM)");

			}
			
		}
       
    }
}
