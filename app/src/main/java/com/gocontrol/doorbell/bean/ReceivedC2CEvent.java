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
 * @author welly
 *
 */
public class ReceivedC2CEvent {

	private byte type ; // 0x01 ACK from NTUT Server; 0x02 , data from other device.
	private ReceivedMessageType msg;
	/**
	 * @return the type
	 */
	public byte getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(byte type) {
		this.type = type;
	}

	private String peerId, acc, pwd;
	private byte [] data;
	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * @return the peerId
	 */
	public String getPeerId() {
		return peerId;
	}

	/**
	 * @return the acc
	 */
	public String getAcc() {
		return acc;
	}

	/**
	 * @return the pwd
	 */
	public String getPwd() {
		return pwd;
	}

	/**
	 * @param peerId the peerId to set
	 */
	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	/**
	 * @param acc the acc to set
	 */
	public void setAcc(String acc) {
		this.acc = acc;
	}

	/**
	 * @param pwd the pwd to set
	 */
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	/**
	 * 
	 */
	public ReceivedC2CEvent() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	
	/**
	 * @param peerId
	 * @param acc
	 * @param pwd
	 */
	public ReceivedC2CEvent(String peerId, String acc, String pwd) {
		super();
		this.peerId = peerId;
		this.acc = acc;
		this.pwd = pwd;
	}

	/**
	 * @param type
	 * @param peerId
	 * @param acc
	 * @param pwd
	 * @param data
	 */
	public ReceivedC2CEvent(byte type, String peerId, String acc, String pwd,
			byte[] data) {
		super();
		this.type = type;
		this.peerId = peerId;
		this.acc = acc;
		this.pwd = pwd;
		this.data = data;
	}

	/**
	 * @param peerId
	 * @param acc
	 * @param pwd
	 */
	public ReceivedC2CEvent(String peerId, String acc, String pwd, byte [] data) {
		super();
		this.peerId = peerId;
		this.acc = acc;
		this.pwd = pwd;
		this.data = data;
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
