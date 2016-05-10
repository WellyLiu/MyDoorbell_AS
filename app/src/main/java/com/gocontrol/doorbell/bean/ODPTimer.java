/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-8 PM 2:17:34
 * Project: TecomDoor
 * PackageName: com.tecom.door.bean
 */
package com.gocontrol.doorbell.bean;

import java.util.Timer;

/**
 * @author Administrator
 *
 */
public class ODPTimer extends Timer{
	
	
	
	/**
	 * 
	 */
	public ODPTimer() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param isDaemon
	 */
	public ODPTimer(boolean isDaemon) {
		super(isDaemon);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 * @param isDaemon
	 */
	public ODPTimer(String name, boolean isDaemon) {
		super(name, isDaemon);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param odpID
	 */
	public ODPTimer(String odpID) {
		super();
		this.odpID = odpID;
	}

	private String odpID;

	/**
	 * @return the odpID
	 */
	public String getOdpID() {
		return odpID;
	}

	/**
	 * @param odpID the odpID to set
	 */
	public void setOdpID(String odpID) {
		this.odpID = odpID;
	}

}
