package com.gocontrol.doorbell.message;

import android.text.TextUtils;
import android.util.Log;

import com.gocontrol.doorbell.network.UdpClientODP;
import com.gocontrol.doorbell.presenter.Utils;
import com.iptnet.c2c.C2CHandle;

/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-19 PM4:36:10
 * Project: Cloud_Phone_Demo
 * PackageName: java.tecom.door.message
 */


/**
 * @author Administrator
 *
 */
public class ProcessMessageQueueThread extends Thread {

	private boolean runFlag = true;
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("ProcessMessageQueueThread start... \n");
		byte []data;
		while(runFlag)
		{
			//Log.d("tst", "runFlag:" + runFlag);
			RequestMessageType one = (RequestMessageType)MessageQueueManager.getInstance().getOneMessage();
			if(one != null)
			{
				System.out.printf("==get message \n" );
				//call socket API to send data.				
				if(one.getType() == MessageDataDefine.BROADCAST)
				{
					System.out.println("== get broadcast message ==");
					data = one.getByteArrayFromMessage();
					if(data != null)
					{
						UdpClientODP.getInstance().sendData(data, "255.255.255.255");
					}
				}else if(one.getType() == MessageDataDefine.P2P)
				{
					System.out.println("== get p2p message ==");
					data = one.getByteArrayFromMessage();
					if(data != null)	
					{
						String odpIP = one.getmODPIPAddress();
						if(TextUtils.isEmpty(odpIP))
							UdpClientODP.getInstance().sendData(data, UdpClientODP.getInstance().getServerAddress());
						else
							UdpClientODP.getInstance().sendData(data, odpIP);
					}						
				}else if(one.getType() == MessageDataDefine.C2C)
				{
					System.out.println("== get C2C message ==");
					
					data = one.getByteArrayFromMessage();
					
						//Log.d("Tecom",one.getPeerAccountInfo().peerId + one.getPeerAccountInfo().loginAccount +  one.getPeerAccountInfo().loginPassword);
						//DataConversion.printHexString("data:", data);
					
					C2CHandle.getInstance().sendCommandByProtocol(one.getPeerAccountInfo().peerId, one.getPeerAccountInfo().loginAccount, one.getPeerAccountInfo().loginPassword, 0x14, data , data.length);
				}else{
					System.out.println("You not pass a message type to me !!! ,I think it must == get p2p message ==");
					data = one.getByteArrayFromMessage();
					if(data != null)	
					{
						UdpClientODP.getInstance().sendData(data, UdpClientODP.getInstance().getServerAddress());
					}						
				}
				
			}else
			{
				//System.out.println(this.toString() +  "==run. get send queue message null.");
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		super.run();
	}

	/**
	 * @param b
	 */
	public void setStopFlag(boolean b) {
		// TODO Auto-generated method stub
		runFlag = false;
	}

	
}
