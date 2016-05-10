package com.gocontrol.doorbell;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RingEventProcess {

	private static final String TAG = RingEventProcess.class.getSimpleName();
	private static Context mContext;
	private static RingEventProcess mStatus;
	
	private RingEvent mRingEvent;
	
	public static void initialize(Context context) {
		if (mContext == null)
			mContext = context;
		
	}
	
	public static RingEventProcess getInstance() {
		if (mStatus == null) {
			if (mContext == null)
				throw new RuntimeException("must call 'initialize' first");
			mStatus = new RingEventProcess();
		}
		return mStatus;
	}
	
	private RingEventProcess() {
		
	}
	
	public boolean newConnection(RingEvent event) {
		
		// first connection
		if (mRingEvent == null) {
			mRingEvent = event;
		
			// start service
			Log.d(TAG, "new incoming connection (first)");
			mContext.startService(new Intent(mContext, DoorService.class)
				.putExtra("event", mRingEvent));	
			return true;
			
			
		// check peerId duplicated
		} else if (event.getPeerId().equals(mRingEvent.getPeerId())) {
			Log.w(TAG, "duplicated peerId (first)");
			return false;
			
		} else {
			Log.w(TAG, "new incoming connection not accepted");
			return false;
		}
	}
	
	public void connectionTerminated() {
		mRingEvent = null;
		Log.d(TAG, "connection terminated");
	}
}
