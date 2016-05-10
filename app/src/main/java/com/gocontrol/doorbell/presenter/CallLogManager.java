/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-10-21 PM2:39:34
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.presenter
 */
package com.gocontrol.doorbell.presenter;

import android.provider.CallLog;

import com.gocontrol.doorbell.AppApplication;
import com.gocontrol.doorbell.datasource.CallLogInfo;
import com.gocontrol.doorbell.datasource.DataHelper;

/**
 * @author Administrator
 *
 */
public class CallLogManager {

	private static int ERROR_PARAMETER_NULL = -1;
	
	private static CallLogManager mInstance;
	/**
	 * 
	 */
	private CallLogManager() {
		// TODO Auto-generated constructor stub
	}

	public static CallLogManager getInstance()
	{
		if(null == mInstance)
			mInstance = new CallLogManager();
		
		return mInstance;
	}
	
	public Long addCallLog(CallLogInfo user)
	{
		if(null != user)
			return DataHelper.getInstance(AppApplication.getInstance()).AddUserInfo(user);
		else
			return (long) ERROR_PARAMETER_NULL;
	}
	
	public Long addCallLog(String callID,String doorName, String callTime, int callType)
	{
		CallLogInfo callLog = new CallLogInfo();
		callLog.setCallId(callID);
		callLog.setCallTime(callTime);
		callLog.setCallType(callType);
		callLog.setDoorName(doorName);
		return DataHelper.getInstance(AppApplication.getInstance()).AddUserInfo(callLog);
	}
}
