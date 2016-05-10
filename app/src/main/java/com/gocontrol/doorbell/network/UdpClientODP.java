package com.gocontrol.doorbell.network;
/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-20 PM4:01:42
 * Project: Cloud_Phone_Demo
 * PackageName: java.tecom.door.network
 */


import java.io.IOException;

import com.gocontrol.doorbell.message.DataQueueManager;
import com.gocontrol.doorbell.message.MessageDataDefine;

/**
 * @author Administrator
 *
 */
public class UdpClientODP {

	private static UdpClientODP mInstance;
	
	private String serverAddress;
	
	/**
	 * @return the serverAddress
	 */
	public String getServerAddress() {
		return serverAddress;
	}
	/**
	 * @param serverAddress the serverAddress to set
	 */
	public void setServerAddress(String serverAddress) {
		if(serverAddress != null && 
				!serverAddress.equalsIgnoreCase(this.serverAddress))
			this.serverAddress = serverAddress;
	}
	
	
	private UdpClientODP()
	{
		
	}
	public static UdpClientODP getInstance()
	{
		if(mInstance  == null)
			mInstance = new UdpClientODP();
		return mInstance;
	}
	
	private UdpClientSocket udpServerSocket;
	private boolean flag = false;
	public void stopSearch()
	{
		System.out.printf("stopSearch() .. \n");
		flag = false;
		if(udpServerSocket != null)
		{
			udpServerSocket.close();
			udpServerSocket = null;
		}
	}
	private void initUDPSocket()
	{
		stopSearch();
		//String localHost = "127.0.0.1";
        int localPort = MessageDataDefine.ODP_SEND_PORT;
        try {
			//udpServerSocket = new UdpClientSocket(localHost, localPort);
        	udpServerSocket = new UdpClientSocket(localPort);
        	flag = true;
        	new Thread(){

				/* (non-Javadoc)
				 * @see java.lang.Thread#run()
				 */
				@Override
				public void run() {
					// TODO Auto-generated method stub
					super.run();
					
					while(flag){
						byte[] rec;						
						try {							
							rec = udpServerSocket.receive();							
							if(rec != null)
							{
								DataConversion.printHexString("received:", rec);
								DataQueueManager.getInstance().receiveData(rec);
							}else
							{
								System.out.println(this.toString() + " ==  receive data null" );
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}
        		
        	}.start();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(udpServerSocket != null)
				udpServerSocket.close();
			udpServerSocket = null;
		}
	}

	public void sendData(byte cmd[], String ip) 
	{
		if(udpServerSocket == null)
			initUDPSocket();
		
		try {
			if( ip != null && !ip.equalsIgnoreCase("") ){
				if(ip.equalsIgnoreCase("255.255.255.255"))
					udpServerSocket.sendData(cmd, "255.255.255.255", MessageDataDefine.ODP_RECEIVE_PORT);
				else
					udpServerSocket.sendData(cmd, ip , MessageDataDefine.ODP_RECEIVE_PORT);
			}else
			{
				System.out.println("sendData . ip error...");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void searchCommandODP(byte cmd[]) {
		initUDPSocket();
		try {
			udpServerSocket.sendData(cmd, "255.255.255.255", MessageDataDefine.ODP_RECEIVE_PORT);
			byte[] data = udpServerSocket.receive();
			/*
			 * 
			 * process data and covent it to the message.
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally
		{
			stopSearch();
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		//UdpClientODP.getInstance().searchCommandODP(new  byte[]{0x01, 0x02});
	}
}
