/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-30 PM 12:28:21
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.bean
 */
package com.gocontrol.doorbell.bean;

import com.gocontrol.doorbell.message.ReceivedMessageType;

/**
 * @author Administrator
 *
 */
public class ReceivedODPEvent {

	private ReceivedMessageType msg;

	/**
	 * @param msg
	 */
	public ReceivedODPEvent(ReceivedMessageType msg) {
		super();
		this.msg = msg;
	}

	/**
	 * @return the msg
	 */
	public ReceivedMessageType getMsg() {
		return msg;
	}

	/**
	 * @param msg the msg to set
	 */
	public void setMsg(ReceivedMessageType msg) {
		this.msg = msg;
	}
	
	
}
