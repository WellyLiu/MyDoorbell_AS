package com.gocontrol.doorbell.message;

import android.util.Log;

import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.bean.ReceivedODPEvent;
import com.gocontrol.doorbell.presenter.Utils;
import com.ypy.eventbus.EventBus;

/**
 * @author Administrator
 *
 */
public class ProcessDataQueueThread extends Thread{
	private boolean runFlag = true;
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.printf("== ProcessDataQueueThread start...\n");
		while(runFlag)
		{	
			
			MessageType one = DataQueueManager.getInstance().getOneData();
			if(one != null)
			{
				//call API to send data to the UI business logic.
				System.out.printf("== receive odp data from p2p. \n");
				processReceivedData((ReceivedMessageType)one);
			}else
			{
				//System.out.println(this.toString() +  " == run. get receive queue data null.");
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
	 * @param one
	 */
	private void processReceivedData(ReceivedMessageType one) {
		// TODO Auto-generated method stub
		Utils.printReceivedMsg(one);
		
		short messageType = one.getEventType();
		RequestMessageType sendOneMsg = new RequestMessageType();
		sendOneMsg.setType(MessageDataDefine.P2P);
		switch( messageType )
		{
		case MessageDataDefine.SMP_SEARCH_ODP_IP_ACK:			
			break;
		case MessageDataDefine.SMP_TO_ODP_AUTH_ACK:
			break;
		case MessageDataDefine.SMP_SET_ODP_SSID_PSWD_ACK:
			
			break;
		case MessageDataDefine.SMP_SET_ODP_WIFI_MODE_ACK:
			
			break;
		case MessageDataDefine.SMP_SET_ODP_WIFI_CLIENT_PARAMETER_ACK:
			break;
		case MessageDataDefine.SMP_SET_ODP_WIFI_AP_PARAMETER_ACK:
			break;
		case MessageDataDefine.SMP_ADD_ACCOUNT_SELF_ACK:
			break;
		case MessageDataDefine.SMP_ADD_ACCOUNT_OTHER_ACK:
			break;
		case MessageDataDefine.SMP_TO_ODP_ADD_OTHER_ACCOUNT_ACK_ACK:
			break;
		case MessageDataDefine.SMP_SET_ODP_LOCAL_ACCOUNT_ACK:
			break;
		case MessageDataDefine.SMP_GET_ODP_SMP_ACCOUNT_ACK:
			break;
		case MessageDataDefine.SMP_REMOVE_ODP_SMP_ACCOUNT_ACK:
			break;
		case MessageDataDefine.SMP_GET_ODP_SYS_PARAMETER_ACK:
			break;
		case MessageDataDefine.SMP_SET_ODP_SYS_PARAMETER_ACK:
			break;
		case MessageDataDefine.SMP_TO_ODP_VERSION_CHECK_ACK:
			break;
		case MessageDataDefine.SMP_TO_ODP_UPDATE_VERSION_ACK:
			break;
		case MessageDataDefine.SMP_SET_ODP_TIME_ACK:
			break;
		case MessageDataDefine.SMP_GET_ODP_VERSION_ACK:
			break;
		default:
			break;
		}
		//post event to the EventBus.
		EventBus.getDefault().post(new ReceivedODPEvent(one));
	}

	/**
	 * @param b
	 */
	public void setStopFlag(boolean b) {
		// TODO Auto-generated method stub
		runFlag = false;
	}

}
