/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-10-21 PM1:53:21
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.datasource
 */
package com.gocontrol.doorbell.datasource;

import java.io.Serializable;

/**
 * @author Administrator
 * Data model class for database process.
 */
public class CallLogInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	public CallLogInfo() {
		// TODO Auto-generated constructor stub
	}

	public static final String ID = "_id";
    public static final String CALLID = "callId";
    public static final String DOORNAME = "doorName";
    public static final String CALLTIME = "callTime";
    public static final String CALLTYPE = "callType";
    public static final String DOORPEERID = "doorPeerId";
    
    private String id;
    private String callId; // call id
    private String doorName;
    private String callTime;
    private int callType;
    private String doorPeerId; // door account.
    
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @return the callId
	 */
	public String getCallId() {
		return callId;
	}
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
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @param callId the callId to set
	 */
	public void setCallId(String callId) {
		this.callId = callId;
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
	/**
	 * @return the doorPeerId
	 */
	public String getDoorPeerId() {
		return doorPeerId;
	}
	/**
	 * @param doorPeerId the doorPeerId to set
	 */
	public void setDoorPeerId(String doorPeerId) {
		this.doorPeerId = doorPeerId;
	}
    
    
}
