package com.gocontrol.doorbell.message;


/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-19 PM3:55:10
 * Project: Cloud_Phone_Demo
 * PackageName: java.tecom.door.message
 */


/**
 * @author Administrator
 *
 */
public class MessageType {

	
	protected MessageHead head;
	protected C2CAccountInfo peerAccount;
	/**
	 * @return the head
	 */
	public MessageHead getHead() {
		return head;
	}

	/**
	 * @param head the head to set
	 */
	public void setHead(MessageHead head) {
		this.head = head;
	}

	protected int type; //broadcast or normal (peer to peer).
	
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	protected byte[] messageData;
	/**
	 * @return the messageData
	 */
	public byte[] getMessageData() {
		return messageData;
	}

	/**
	 * @param messageData the messageData to set
	 */
	public void setMessageData(byte[] messageData) {
		this.messageData = messageData;
	}
	public void setPeerAccountInfo(C2CAccountInfo peer) {
		this.peerAccount = peer;
	}
	public C2CAccountInfo getPeerAccountInfo() {
		return this.peerAccount;
	}
	/**
	 * 
	 */
	public MessageType() {
		super();
		// TODO Auto-generated constructor stub
		head = new MessageHead();
		peerAccount = new C2CAccountInfo();
	}
	public static class C2CAccountInfo{
		public String peerId;
		public String loginAccount;
		public String loginPassword;
	}
}
