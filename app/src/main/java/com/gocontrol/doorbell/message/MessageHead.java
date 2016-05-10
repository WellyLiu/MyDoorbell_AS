package com.gocontrol.doorbell.message;
/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-24 PM1:11:17
 * Project: Cloud_Phone_Demo
 * PackageName: java.tecom.door.message
 */


/**
 * @author Administrator
 *
 */
public class MessageHead {

	private byte[] protocolVersion;
	private byte[] macAddress;
	private byte[] IPAddress;
	private byte[] reservedData;
	private byte[] eventType;
	private byte[] payloadLength;
	
	
	
	/**
	 * @param protocolVersion
	 * @param macAddress
	 * @param iPAddress
	 * @param reservedData
	 * @param eventType
	 * @param payloadLength
	 */
	public MessageHead(byte[] protocolVersion, byte[] macAddress,
			byte[] iPAddress, byte[] reservedData, byte[] eventType,
			byte[] payloadLength) {
		super();
		this.protocolVersion = protocolVersion;
		this.macAddress = macAddress;
		IPAddress = iPAddress;
		this.reservedData = reservedData;
		this.eventType = eventType;
		this.payloadLength = payloadLength;
	}
	
	
	/**
	 * 
	 */
	public MessageHead() {
		super();
		// TODO Auto-generated constructor stub
		this.protocolVersion = new byte[2];
		this.macAddress = new byte[6];
		IPAddress = new byte[4];
		this.reservedData = new byte[20];
		this.eventType = new byte[2];
		this.payloadLength = new byte[4];
		
		initDefaultData();
	}


	/**
	 * 
	 */
	private void initDefaultData() {
		// TODO Auto-generated method stub
		protocolVersion[0] = (byte) ((MessageDataDefine.PROTOCOL_VERSION & 0xFF00) >> 8);
		protocolVersion[1] = (byte) (MessageDataDefine.PROTOCOL_VERSION & 0xFF);
		byte mac[] = DataConversion.getMacAddressFromIp();
		//DataConversion.printHexString("Mac address:" , mac);
		if(mac != null)
			setMacAddress(mac);	
		byte ip[] = DataConversion.ipv4Address2BinaryArray(DataConversion.getLocalIpAddress());
		//DataConversion.printHexString("\nIP address:" , ip);
		if(ip != null)
			setIPAddress(ip);
		clearReservedData();
	}


	/**
	 * 
	 */
	private void clearReservedData() {
		// TODO Auto-generated method stub
		if(reservedData != null)
		{
			for(int i=0; i< 20; i++)
				reservedData[i]= 0x00;
		}
	}


	public static MessageHead constructOneMessage(byte[] messageType, byte[] payloadLength)
	{
		MessageHead one = new MessageHead();
		one.setEventType(messageType);
		one.setPayloadLength(payloadLength);
		return one;
	}
	
	/**
	 * @return the protocolVersion
	 */
	public byte[] getProtocolVersion() {
		return protocolVersion;
	}
	/**
	 * @return the macAddress
	 */
	public byte[] getMacAddress() {
		return macAddress;
	}
	/**
	 * @return the iPAddress
	 */
	public byte[] getIPAddress() {
		return IPAddress;
	}
	/**
	 * @return the reservedData
	 */
	public byte[] getReservedData() {
		return reservedData;
	}
	/**
	 * @return the eventType
	 */
	public byte[] getEventType() {
		return eventType;
	}
	/**
	 * @return the payloadLength
	 */
	public byte[] getPayloadLength() {
		return payloadLength;
	}
	/**
	 * @param protocolVersion the protocolVersion to set
	 */
	public void setProtocolVersion(byte[] protocolVersion) {
		this.protocolVersion = protocolVersion;
	}
	/**
	 * @param macAddress the macAddress to set
	 */
	public void setMacAddress(byte[] macAddress) {
		this.macAddress = macAddress;
	}
	/**
	 * @param iPAddress the iPAddress to set
	 */
	public void setIPAddress(byte[] iPAddress) {
		IPAddress = iPAddress;
	}
	/**
	 * @param reservedData the reservedData to set
	 */
	public void setReservedData(byte[] reservedData) {
		this.reservedData = reservedData;
	}
	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(byte[] eventType) {
		this.eventType = eventType;
	}
	/**
	 * @param payloadLength the payloadLength to set
	 */
	public void setPayloadLength(byte[] payloadLength) {
		this.payloadLength = payloadLength;
	}
	
	
}
