package com.gocontrol.doorbell.message;
/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-19 PM3:48:25
 * Project: Cloud_Phone_Demo
 * PackageName: java.tecom.door.message
 */

/**
 * @author Administrator
 *
 */
public class RequestMessageType extends MessageType{

	

	/**
	 * 
	 */
	public RequestMessageType() {
		super();
		// TODO Auto-generated constructor stub
	}

	//手动指定需要发向的ODP的IP Address
	private String mODPIPAddress;

	/**
	 * @return the mODPIPAddress
	 */
	public String getmODPIPAddress() {
		return mODPIPAddress;
	}

	/**
	 * @param mODPIPAddress the mODPIPAddress to set
	 */
	public void setmODPIPAddress(String mODPIPAddress) {
		this.mODPIPAddress = mODPIPAddress;
	}
	
	public void updateMessage(short messageType, String []args)
	{
		byte type[] = new byte[2];
		type[0] = (byte) ((messageType & 0xFF00) >> 8);
		type[1] = (byte) (messageType & 0x00FF);
		this.head.setEventType(type);
		
		int dstPos = 0;
		if(args != null)
		{
			int dataLength = 0;
			int num = args.length;
			byte tmp[][] = new byte[num][];
			for( int i=0; i<num; i++)
			{
				tmp[i] = DataConversion.StringToUTF8Byte(args[i]);
				dataLength = dataLength + tmp[i].length;
				dataLength = dataLength + 1; // stored the 0x0A.
			}
			byte data[]  = new byte[dataLength];
			
			for( int i=0; i<num; i++)
			{
				System.arraycopy(tmp[i], 0, data, dstPos, tmp[i].length);
				dstPos = dstPos + tmp[i].length;
				data[dstPos] = 0x0A;
				dstPos = dstPos + 1;
			}
			//set data.
			this.messageData = data;
		}else
		{
			System.out.println(this.toString() + "== updateMessage:" + " args null.");
		}
		byte payloadLength[] = new byte[4];
		payloadLength[0] = (byte) ((dstPos & 0xFF000000) >> 24);		
		payloadLength[1] = (byte) ((dstPos & 0x00FF0000) >> 16);		
		payloadLength[2] = (byte) ((dstPos & 0x0000FF00) >> 8);		
		payloadLength[3] = (byte) (dstPos & 0x000000FF);
		this.head.setPayloadLength(payloadLength);	
	}
	
	/*
	 * payload is only one byte.
	 */
	public void updateMessageData(short messageType, byte data)
	{
		byte type[] = new byte[2];
		type[0] = (byte) ((messageType & 0xFF00) >> 8);
		type[1] = (byte) (messageType & 0x00FF);
		this.head.setEventType(type);
		
		int dstPos = 1;
		byte payloadLength[] = new byte[4];
		payloadLength[0] = (byte) ((dstPos & 0xFF000000) >> 24);		
		payloadLength[1] = (byte) ((dstPos & 0x00FF0000) >> 16);		
		payloadLength[2] = (byte) ((dstPos & 0x0000FF00) >> 8);		
		payloadLength[3] = (byte) (dstPos & 0x000000FF);
		this.head.setPayloadLength(payloadLength);	

		this.messageData = new byte[]{data};
	}
	
	/*
	 * payload is byte array.
	 */
	public void updateMessageDatas(short messageType, byte data[])
	{
		byte type[] = new byte[2];
		type[0] = (byte) ((messageType & 0xFF00) >> 8);
		type[1] = (byte) (messageType & 0x00FF);
		this.head.setEventType(type);
		
		int dstPos = data.length + 1;
		byte payloadLength[] = new byte[4];
		payloadLength[0] = (byte) ((dstPos & 0xFF000000) >> 24);		
		payloadLength[1] = (byte) ((dstPos & 0x00FF0000) >> 16);		
		payloadLength[2] = (byte) ((dstPos & 0x0000FF00) >> 8);		
		payloadLength[3] = (byte) (dstPos & 0x000000FF);
		this.head.setPayloadLength(payloadLength);	
		
		byte []tmp = new byte[ dstPos ] ;
		tmp[0] = (byte) data.length;
		System.arraycopy(data, 0, tmp, 1, data.length);
		this.messageData = tmp;
	}
	
	public byte[] getByteArrayFromMessage()
	{
		byte len[] = this.head.getPayloadLength();
		if( len == null)
		{
			System.out.println("getByteArrayFromMessage. len null...");
			return null;
		}
		int payLen = DataConversion.bytesToInt2(len , 0);
		int dataLen = payLen + 38; // payload length + head length.
		
		byte data[] = new byte[dataLen];
		int offset = 0;
		
		DataConversion.printHexString("head version:" , this.head.getProtocolVersion());
		System.arraycopy(this.head.getProtocolVersion(), 0, data, offset, 2);
		offset = offset + 2;
		DataConversion.printHexString("head mac:" , this.head.getMacAddress());
		System.arraycopy(this.head.getMacAddress(), 0, data, offset, 6);
		offset = offset + 6;
		DataConversion.printHexString("head ip:" , this.head.getIPAddress());
		System.arraycopy(this.head.getIPAddress(), 0, data, offset, 4);
		offset = offset + 4;
		DataConversion.printHexString("head reserved data:" , this.head.getReservedData());
		System.arraycopy(this.head.getReservedData(), 0, data, offset, 20);
		offset = offset + 20;
		DataConversion.printHexString("head event type:" , this.head.getEventType());
		System.arraycopy(this.head.getEventType(), 0, data, offset, 2);
		offset = offset + 2;
		DataConversion.printHexString("head pay length:" , this.head.getPayloadLength());
		System.arraycopy(this.head.getPayloadLength(), 0, data, offset, 4);
		offset = offset + 4;
		
		DataConversion.printHexString("head pay data:" , this.getMessageData());
		System.arraycopy(this.getMessageData(), 0, data, offset, payLen);
		offset = offset + payLen;
		
		return data;
	}
	
	public byte[] getByteArrayFromMessageHead()
	{
		byte len[] = this.head.getPayloadLength();
		if( len == null)
		{
			System.out.println("getByteArrayFromMessage. len null...");
			return null;
		}
		
		int dataLen = 38; //  head length.
		
		byte data[] = new byte[dataLen];
		int offset = 0;
		
		DataConversion.printHexString("head version:" , this.head.getProtocolVersion());
		System.arraycopy(this.head.getProtocolVersion(), 0, data, offset, 2);
		offset = offset + 2;
		DataConversion.printHexString("head mac:" , this.head.getMacAddress());
		System.arraycopy(this.head.getMacAddress(), 0, data, offset, 6);
		offset = offset + 6;
		DataConversion.printHexString("head ip:" , this.head.getIPAddress());
		System.arraycopy(this.head.getIPAddress(), 0, data, offset, 4);
		offset = offset + 4;
		DataConversion.printHexString("head reserved data:" , this.head.getReservedData());
		System.arraycopy(this.head.getReservedData(), 0, data, offset, 20);
		offset = offset + 20;
		DataConversion.printHexString("head event type:" , this.head.getEventType());
		System.arraycopy(this.head.getEventType(), 0, data, offset, 2);
		offset = offset + 2;
		DataConversion.printHexString("head pay length:" , this.head.getPayloadLength());
		System.arraycopy(this.head.getPayloadLength(), 0, data, offset, 4);
		offset = offset + 4;
		
		return data;
	}
}
