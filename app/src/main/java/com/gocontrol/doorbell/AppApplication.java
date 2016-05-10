package com.gocontrol.doorbell;

import com.gocontrol.doorbell.utils.Utils;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;

public class AppApplication extends Application implements ActivityLifecycleCallbacks {

	private static final String TAG = AppApplication.class.getSimpleName();
		
	private static AppApplication mInstance;
	
	private boolean isViewerActivityShown;
	
	private boolean mManualCall;
	private String mManualCallPeerId;
	private boolean mLockConnection;

	public boolean isManualCall() {
		return mManualCall;
	}
	public String getManualCallPeerId() {
		if (mManualCallPeerId == null) mManualCallPeerId = ""; 
		return mManualCallPeerId;
	}

	public void setManualCall(boolean enable, String peerId) {
		mManualCall = enable;
		if (enable)	mManualCallPeerId = peerId;
		else		mManualCallPeerId = null;
	}
	
	public void lockConnection(boolean lock) {
		mLockConnection = lock;
	}
	
	public boolean isLockConnection() {
		return mLockConnection;
	}

	
	public boolean isViewerActivityShown() {
		return isViewerActivityShown;
	}
	
	public void registerActivityLifecycle() {
		registerActivityLifecycleCallbacks(this);
	}
	
	public void unregisterActivityLifecycle() {
		unregisterActivityLifecycleCallbacks(this);
	}
	
	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		Log.e(TAG, "onActivityCreated = " + activity.getLocalClassName());
		
		if (activity instanceof ViewerActivity) {
			isViewerActivityShown = true;			
		}
	}

	@Override
	public void onActivityDestroyed(Activity activity) {
		Log.e(TAG, "onActivityDestroyed = " + activity.getLocalClassName());
		if (activity instanceof ViewerActivity) {
			isViewerActivityShown = false;			
			mManualCall = false;
		}
	}

	@Override
	public void onActivityPaused(Activity activity) {
		
	}

	@Override
	public void onActivityResumed(Activity activity) {
		
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		
	}

	@Override
	public void onActivityStarted(Activity activity) {
		
	}

	@Override
	public void onActivityStopped(Activity activity) {
		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();	
		
		Utils.initiaUserAccount(this);
		Utils.initiaODPInfo(this);
		Utils.initiaAppSystemConfig(this);
		
		Log.d("tst", "AppApplication onCreate.");
		
	}

	/**
	 * 
	 */
	public AppApplication() {
		super();
		// TODO Auto-generated constructor stub
		mInstance = this;
		
	}	
	
	public static AppApplication getInstance()
	{		
		return mInstance;
	}
	
}
