/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-13 PM5:15:58
 * Project: TecomDoor
 * PackageName: com.tecom.door.model
 */
package com.gocontrol.doorbell.model;

import android.content.Context;

import com.gocontrol.doorbell.utils.Utils;

/**
 * @author Administrator
 *
 */
public class SystemConfigManager {

	private int logoutState;
	private int appLoginStatus;
	private int micVol; //1,2,3,4,5
	
	
	/**
	 * @return the appLoginStatus
	 */
	public int isAppAutoLogin() {
		return appLoginStatus;
	}

	/**
	 * @param appLoginStatus the appLoginStatus to set
	 */
	public void setAppAutoLogin(int appLoginStatus) {
		this.appLoginStatus = appLoginStatus;
	}
	
	public void saveAppAutoLogin(Context mContext)
	{
		Utils.saveAppAutoLogin(mContext, this.appLoginStatus);
	}
	/**
	 * @return the micVol
	 */
	public int getMicVol() {
		return micVol;
	}

	/**
	 * @param micVol the micVol to set
	 */
	public void setMicVol(int micVol) {
		this.micVol = micVol;
	}

	private static SystemConfigManager mInstance;
	private SystemConfigManager()
	{
		
	}
	public static SystemConfigManager getInstance()
	{
		if(mInstance  == null)
			mInstance = new SystemConfigManager();
		
		return mInstance;
	}

	/**
	 * @return the logoutState
	 */
	public int getLogoutState() {
		return logoutState;
	}

	/**
	 * @param logoutState the logoutState to set
	 */
	public void setLogoutState(int logoutState) {
		this.logoutState = logoutState;
	}
}
