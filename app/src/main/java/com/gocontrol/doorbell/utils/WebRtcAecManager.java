package com.gocontrol.doorbell.utils;

import android.util.Log;

public class WebRtcAecManager{
	
	private final static String TAG = "webrtc_aec";
	private static WebRtcAecManager mAecManager = null;

	static {
		Log.i(TAG, "Loading WebRTC Aec JNI");
		System.loadLibrary("webrtc_aec");
	}
	
	public WebRtcAecManager() {
		super();
	}

	public static WebRtcAecManager getInstance() {
		if (mAecManager == null){
			mAecManager = new WebRtcAecManager();
		}
		return mAecManager;
	}

	public native int createWebRtcAec(int clockRate, int channelCount, 
			int samplesPerFrame, int tailMs, int options);
	
	public native int destroyWebRtcAec();
	
	public native int resetWebRtcAec();
	
	public native int startWebRtcAec(short[] inputArray, final short[] farEndArray);
}