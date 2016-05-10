/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-10-21 AM11:58:55
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.model
 */
package com.gocontrol.doorbell.model;
/**
 * @author Administrator
 * data model class for business process. for example ,you want pick up one call log.
 */
public class CallLog{

	/**
	 * 
	 */
	public CallLog() {
		// TODO Auto-generated constructor stub
	}

	private String doorName;
	private String callTime;
	private int callType;
	/**
	 * @return the doorName
	 */
	public String getDoorName() {
		return doorName;
	}
	/**
	 * @return the callTime
	 */
	public String getCallTime() {
		return callTime;
	}
	/**
	 * @return the callType
	 */
	public int getCallType() {
		return callType;
	}
	/**
	 * @param doorName the doorName to set
	 */
	public void setDoorName(String doorName) {
		this.doorName = doorName;
	}
	/**
	 * @param callTime the callTime to set
	 */
	public void setCallTime(String callTime) {
		this.callTime = callTime;
	}
	/**
	 * @param callType the callType to set
	 */
	public void setCallType(int callType) {
		this.callType = callType;
	}
	
}
