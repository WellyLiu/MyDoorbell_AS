package com.gocontrol.doorbell.message;


/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-19 PM3:48:44
 * Project: Cloud_Phone_Demo
 * PackageName: java.tecom.door.message
 */


/**
 * @author Administrator
 *
 */
public class ReceivedMessageType extends MessageType{

	private String payloadStr[]; //maybe one unit，or many units. {"a"} or {"a", "b", "c"}
	private byte payloadByte;
	private byte[] payloadByteArray;
	/**
	 * @return the payloadByteArray
	 */
	public byte[] getPayloadByteArray() {
		return payloadByteArray;
	}

	/**
	 * @param payloadByteArray the payloadByteArray to set
	 */
	public void setPayloadByteArray(byte[] payloadByteArray) {
		this.payloadByteArray = payloadByteArray;
	}

	private String serverIP;
	private String serverMac;
	private short eventTypeStr;
	private int payloadLength;
	/**
	 * @return the payloadLength
	 */
	public int getPayloadLength() {
		return payloadLength;
	}

	/**
	 * @param payloadLength the payloadLength to set
	 */
	public void setPayloadLength(int payloadLength) {
		this.payloadLength = payloadLength;
	}

	/**
	 * @return the eventType
	 */
	public short getEventType() {
		return eventTypeStr;
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(short eventType) {
		this.eventTypeStr = eventType;
	}

	/**
	 * @return the payloadStr
	 */
	public String[] getPayloadStr() {
		return payloadStr;
	}

	/**
	 * @return the payloadByte
	 */
	public byte getPayloadByte() {
		return payloadByte;
	}

	/**
	 * @return the serverIP
	 */
	public String getServerIP() {
		return serverIP;
	}

	/**
	 * @return the serverMac
	 */
	public String getServerMac() {
		return serverMac;
	}

	/**
	 * @param payloadStr the payloadStr to set
	 */
	public void setPayloadStr(String payloadStr[]) {
		this.payloadStr = payloadStr;
	}

	/**
	 * @param payloadByte the payloadByte to set
	 */
	public void setPayloadByte(byte payloadByte) {
		this.payloadByte = payloadByte;
	}

	/**
	 * @param serverIP the serverIP to set
	 */
	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	/**
	 * @param serverMac the serverMac to set
	 */
	public void setServerMac(String serverMac) {
		this.serverMac = serverMac;
	}

	/**
	 * 
	 */
	public ReceivedMessageType() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ReceivedMessageType(byte recData[]) {
		super();
		// TODO Auto-generated constructor stub
		if(recData != null)
		{
			//fill data to the head
			updateDataHead(recData);
			//convert the data.
			transformData();
		}
	}

	/**
	 * 
	 */
	private void transformData() {
		// TODO Auto-generated method stub
		if(this.head.getEventType() != null)
		{
			short eventType = DataConversion.getShort2(this.head.getEventType(), 0);
			this.eventTypeStr = eventType;
			DataConversion.printHexString("received dta. message type:", this.head.getEventType());
		}
		if(this.head.getIPAddress() != null)
		{
			String str = DataConversion.binaryArray2Ipv4Address(this.head.getIPAddress());
			this.serverIP = str;
			System.out.printf("received data. server ip= %s\n", this.serverIP);			
		}
		if(this.head.getMacAddress() != null)
		{
			String str = DataConversion.binaryArray2mac(this.head.getMacAddress());
			this.serverMac = str;
			System.out.printf("received data. server mac= %s\n", this.serverMac);			
		}
		if(this.head.getMacAddress() != null)
		{
			//if you need server Mac address, do it here.
		}
		if(this.head.getPayloadLength() != null)
		{
			this.payloadLength = DataConversion.bytesToInt2(this.head.getPayloadLength(), 0);
			System.out.printf("received data. payload length= %d\n", this.payloadLength);
		}
		if(this.head.getReservedData() != null)
		{
			if(payloadLength > 0)
			{
				if(payloadLength == 1)
				{
					this.payloadByte = this.getMessageData()[0];
					System.out.printf("received data. payloadByte= %d\n", this.payloadByte);
				}else if(this.eventTypeStr == MessageDataDefine.SMP_ASK_ODP_REG_STATUS_ACK) //2进制protocol
				{
					if(payloadLength > 1)
					{
						System.out.println(this.toString() + " == " + "received message data data[] \n");
						
						this.payloadByteArray = this.getMessageData();
					}
				}/*else if(this.eventTypeStr == MessageDataDefine.ODP_MOTION_DETECT_EVENT  //对于这2个 私有协议,又是定义为一个字符串,没有=,也没有0a结尾. 
						|| this.eventTypeStr == MessageDataDefine.ODP_PIR_DETECT_EVENT)
				{
					byte [] t = this.getMessageData();
					String eventMsg = DataConversion.UTF8ByteToString(t, t.length);
					this.payloadStr = new String[]{eventMsg};
				}*/
				else //正常的有=号, 0a结尾的私有协议
				{
					//0A作为单个name的结束符，必须先找出有多少name
					byte []tmp = this.getMessageData();
					String str[] = DataConversion.spitAndTransfomBytes(tmp, (byte)0x0a);
					
					this.payloadStr = str;
					if(str == null)
					{						
						System.out.println(this.toString() + " == " + "received message data str[] null.\n");						
					}else{									
						for(String s : str)
						{
							System.out.printf("received data. data= %s\n", s);		
						}	
					}
				}
			}else
			{
				System.out.println(this.toString() + " == " + "transformData, length <= 0\n");
			}
		}
		
	}

	/**
	 * 
	 */
	private void updateDataHead(byte recData[]) {
		// TODO Auto-generated method stub
		byte []eventType = new byte[]{recData[32], recData[33]};
		this.head.setEventType(eventType);
		
		byte []iPAddress = new byte[]{recData[8], recData[9],recData[10], recData[11]};
		//System.out.println(recData[8]);
		//System.out.println(recData[9]);
		//System.out.println(recData[10]);
		//System.out.println(recData[11]);
		
		this.head.setIPAddress(iPAddress);
		
		byte []macAddress = new byte[]{recData[2], recData[3],recData[4], recData[5],
				recData[6], recData[7]};
		this.head.setMacAddress(macAddress);
		
		byte []payloadLength = new byte[]{recData[34], recData[35],recData[36], recData[37]};
		this.head.setPayloadLength(payloadLength);
		
		try{
			int payLen = DataConversion.bytesToInt2(payloadLength, 0);
			byte []payloadData = new byte[payLen];
			System.arraycopy(recData, 38, payloadData, 0, payLen);
			this.setMessageData(payloadData);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
}
