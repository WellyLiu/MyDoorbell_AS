package com.gocontrol.doorbell;

import com.gocontrol.doorbell.ui.v7.CallFunctionActivity;
import com.iptnet.c2c.C2CCallInfo;
import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.iptnet.c2c.C2CListener;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class DoorService extends Service {

	private static final String TAG = DoorService.class.getSimpleName();
	private C2CHandle mC2CHandle;
	private C2CEventListener mC2CListener;
	private WakeLock mWakeLock;
	
	private class C2CEventListener extends C2CListener {
		
		private int mIncomingProcessingLineId = Integer.MIN_VALUE;
		private boolean mIncomingProcessing;
		private RingEvent mRingEvent;
		
		@Override
		protected synchronized void receiveMessage(C2CEvent event) {
			int lineId = event.getLine();
			Log.e(TAG, "service : c2c event = " + event + " lineId = " + lineId);
			
			// process logout by server
			if (C2CEvent.C2C_LOGOUT_BY_SVR == event) {
				Log.w(TAG, "occur logout!!, redo start register process");
				
				// read registration parameters
				String[] params = AppUtils.readC2CLoginParams(getContext());
				String acc = params[0];
				String pwd = params[1];
				String srv = params[2];
				acc = AppUtils.processWebLoginAccount(acc, srv);
				int ret = C2CHandle.getInstance().startRegisterProcess(srv, acc, pwd);
				if (ret < 0) Log.w(TAG, "start register process fail (" + ret + ")");
			}
			
			boolean isManualCall = ((AppApplication) getApplication()).isManualCall();
			String isManualCallPeerId = ((AppApplication) getApplication()).getManualCallPeerId();
			
			if (isManualCall && C2CEvent.C2C_INCOMING_STATE == event &&
				event.hasCallInfo() && event.getCallInfo().getPeerId().equals(isManualCallPeerId)) {
				
				CallFunctionActivity.sFlag = true;
				mIncomingProcessing = false;
				mIncomingProcessingLineId = Integer.MIN_VALUE;
				((AppApplication) getApplication()).setManualCall(false, null);
				Log.e(TAG, "same peer call in !!");
				
			} else if (isManualCall) {
				Log.d(TAG, "alrady manual call, abort the event.");
				return;
			} else {
				mIncomingProcessing = false;
				mIncomingProcessingLineId = Integer.MIN_VALUE;
			}

			switch (event) {
				case C2C_INCOMING_STATE: {
			
					Log.d("tecom", "==============C2C_INCOMING_STATE 1 ================");
					// for visible view task to process the event
					if (((AppApplication) getApplication()).isViewerActivityShown()) {
						Log.d("tecom", "==============C2C_INCOMING_STATE 2 ================");
						break;
					}
					
					// incoming call is processing, reject current connection
					if (mIncomingProcessing || mIncomingProcessingLineId >= 0) {
						Log.w(TAG, "sorry, the incoming call is processing, reject the line (" + lineId + ")");
						int ret = C2CHandle.getInstance().rejectCurrentRequest(lineId);
						if (ret < 0) Log.w(TAG, ">> reject current request fail (" + ret + ")");
						break;
					}
					Log.d("tecom", "==============C2C_INCOMING_STATE 3 ================");
					
					// lock connection
					((AppApplication) getApplication()).lockConnection(true);
					

					// get incoming processing parameters
					mIncomingProcessing = true;
					mIncomingProcessingLineId = lineId;
					
					// doorbell is ringing, start activity to view stream
					if (event.hasCallInfo()) {
						Log.d("tecom", "==============C2C_INCOMING_STATE 4 ================");
						// check door configuration exist
						String id = event.getCallInfo().getPeerId();
						if (Door.read(getContext(), id) == null) {
							Log.w(TAG, "the incoming peer is not setting the APP, reject current request, lineId = " + lineId);
							int ret = C2CHandle.getInstance().rejectCurrentRequest(lineId);
							if (ret < 0) Log.w(TAG, ">> reject current request fail (" + ret + ")");
							break;
						}

						// get CallInfo data
						C2CCallInfo info = event.getCallInfo();
						String[] split = info.getCustomInfo().split("=");
						if (split != null && split.length > 1) {
							String[] split2 = split[1].split(";");
							if (split2 != null && split2.length > 0) {
								int sessionCode = Integer.valueOf(split2[0]);
								mRingEvent = new RingEvent(id, sessionCode, System.currentTimeMillis()).setLineId(lineId);
								Log.e(TAG, "[DoorService] C2C INCOMING STATE >> get session code ="+sessionCode);

							}
						}
					
					// not get CallInfo reject the incoming
					} else {
						Log.w(TAG, "could not get CallInfo, reject current request, lineId = " + lineId);
						int ret = C2CHandle.getInstance().rejectCurrentRequest(lineId);
						if (ret < 0) Log.w(TAG, ">> reject current request fail (" + ret + ")");
					}

				} break;
					
				case C2C_INCOMING_ERROR:
					Log.d("tecom", "C2C_INCOMING_ERROR==== 1");
					// not to process the incoming error event that not accepted line.
					if (!mIncomingProcessing && mIncomingProcessingLineId < 0) {
						Log.e(TAG, "not to process the incoming error event, lineId = " + lineId);
						break;
					}
					Log.d("tecom", "C2C_INCOMING_ERROR====2 ");
					// unlock connection
					((AppApplication) getApplication()).lockConnection(false);

					mRingEvent = null;
					
					// send notification to report the error
					//AppUtils.reportError(getContext(), AppUtils.ERR_C2C_INCOMING_ERROR, event.getSubEvent());
					
					// resume the parameter
					mIncomingProcessing = false;
					mIncomingProcessingLineId = Integer.MIN_VALUE;
					
					RingEventProcess.getInstance().connectionTerminated();
					break;
					
				case C2C_RELAY_MODE:
					Log.d("tecom", "==============C2C_RELAY_MODE 1 ================");
					if (((AppApplication) getApplication()).isViewerActivityShown()) {
						Log.d("tecom", "==============C2C_RELAY_MODE 2 ================");
						break;
					}
					// unlock connection
					((AppApplication) getApplication()).lockConnection(false);

					if (mRingEvent != null) {
						Log.d("tecom", "==============C2C_RELAY_MODE 3 ================");
						int curLineId;
						if ((curLineId = event.getLine()) == lineId) {
							Log.d("tecom", "==============C2C_RELAY_MODE 4 ================");
							// check door ID exist							
					        if (Door.read(getContext(), mRingEvent.getPeerId()) == null) {
					        
					        	// close connection
					        	Log.e(TAG, "the door is not setup, terminate the connection");
					        	C2CHandle.getInstance().terminateConnection(curLineId);
					        	break;
					        }
					        Log.d("tecom", "==============C2C_RELAY_MODE 5 ================");
					        boolean addOk = RingEventProcess.getInstance().newConnection(mRingEvent);
					        if (!addOk) {
					        	Log.w(TAG, "new connection fail (C2C), terminate the connection");
					        	C2CHandle.getInstance().terminateConnection(curLineId);
					        	break;
					        }
						}
					}
					
					// resume the parameter
					mIncomingProcessing = false;
					mIncomingProcessingLineId = Integer.MIN_VALUE;
					Log.d("tecom", "==============C2C_RELAY_MODE 6 ================");
					break;
					
				case C2C_CALL_TERMINATED:
					Log.d("tecom", "==============C2C_CALL_TERMINATED 1 ================");
					//NTUR need to do this.
					if (!((AppApplication) getApplication()).isViewerActivityShown())
					{
						// resume the parameter
						mIncomingProcessing = false;
						mIncomingProcessingLineId = Integer.MIN_VALUE;
						Log.d("tecom", "==============C2C_CALL_TERMINATED 2 ================");
					}
					
					break;
					
					
				case C2C_REQ_CANCELED:
					Log.d("tecom", "C2C_REQ_CANCELED====1 ");
					if (((AppApplication) getApplication()).isViewerActivityShown()) {
						break;
					}
					Log.d("tecom", "C2C_REQ_CANCELED====2 ");
					// unlock connection
					((AppApplication) getApplication()).lockConnection(false);
					
				default: // need not to implement other event


			}
		}
	}
	
	private Context getContext() {
		return this;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
		stopSelf();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// keep CPU always on
		PowerManager pwMgr = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pwMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoorSampe");
		mWakeLock.acquire();
		
		RingEventProcess.initialize(getApplicationContext());
		
		// register activity life cycle
		((AppApplication) getApplication()).registerActivityLifecycle();
		
		// read registration parameters
		String[] params = AppUtils.readC2CLoginParams(this);
		String acc = params[0];
		String pwd = params[1];
		String srv = params[2];
		
		// initialize C2C SDK and start register process
		mC2CHandle = C2CHandle.getInstance();
		mC2CHandle.initialize(srv, acc, pwd);
		mC2CHandle.addListener(mC2CListener = new C2CEventListener());
		acc = AppUtils.processWebLoginAccount(acc, srv);

		mC2CHandle.startRegisterProcess(srv, acc, pwd);
		mC2CHandle.showDebugMessage(false);

		mC2CHandle.setLocalAuthentication(com.gocontrol.doorbell.utils.BuildConfig.Account_Local_Default, 
				 com.gocontrol.doorbell.utils.BuildConfig.Password_Local_Default, 0) ;
		mC2CHandle.enableLocalAuthentication(true);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		((AppApplication) getApplication()).setManualCall(false, null);
		
		// release C2C SDK
		new Thread(new Runnable() {
			public void run() {
				mC2CHandle.deInitialize();
				mC2CHandle.removeListener(mC2CListener);
				mC2CHandle = null;
				Log.d(TAG, "C2C SDK released!");
		}}).start();

		
		// unregister activity life cycle
		((AppApplication) getApplication()).unregisterActivityLifecycle();
		
		// release always CPU on
		mWakeLock.release();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (intent != null) {

			RingEvent event = intent.getParcelableExtra("event");
			
			// from GCM
			if (event != null && event.getLineId() < 0) {
				Log.d(TAG, "from gcm ring event");

	        	// send command to door to tell door callback
	        	startService(new Intent(DoorService.this, ConnectDoorService.class)
	        		.putExtra("ring.event", event));
	
	        	// event processed
	        	RingEventProcess.getInstance().connectionTerminated();
			
			// from C2C INCOMING
			} else if (event != null && event.getLineId() >= 0) {
				Log.d(TAG, "start viewer to show door video");
				
				// start activity to show screen
		        startActivity(new Intent(DoorService.this, ViewerActivity.class)
		        		.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		        		.putExtra("event", event));
			}
		}		
		return super.onStartCommand(intent, flags, startId);

	}
}
