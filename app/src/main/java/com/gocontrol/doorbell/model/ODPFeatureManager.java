/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-13 AM11:18:10
 * Project: TecomDoor
 * PackageName: com.tecom.door.model
 */
package com.gocontrol.doorbell.model;

import com.gocontrol.doorbell.bean.ODPFeature;


public class ODPFeatureManager {
	
	private ODPFeature odpFeature;
	
	private static ODPFeatureManager mInstance;
	
	private  ODPFeatureManager()
	{
		odpFeature = new ODPFeature();
		odpFeature.setBrightness(128);
		odpFeature.setContrast(128);
		odpFeature.setFlip(false);
		odpFeature.setHue(128);
		odpFeature.setmFrameRate(5);
		odpFeature.setMirror(false);
		odpFeature.setmMicroVol(5);
		odpFeature.setMotionDetec("000000000");
		odpFeature.setmResolution("2");
		odpFeature.setmSpeakerVol(5);
		odpFeature.setPIR(0);
		odpFeature.setSaturation(128);
		odpFeature.setSharpness(128);
		odpFeature.setTargetY(120);
	}

	public static ODPFeatureManager getInstance()
	{
		if(mInstance == null)
		{
			mInstance = new ODPFeatureManager();
		}
		
		return mInstance;
	}
	
	public ODPFeature getODPFeature()
	{
		return odpFeature;
	}
}
