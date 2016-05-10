/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-10-21 PM3:30:30
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.presenter
 */
package com.gocontrol.doorbell.presenter;


import com.gocontrol.doorbell.message.MessageDataDefine;
import com.gocontrol.doorbell.message.ReceivedMessageType;



/**
 * @author Administrator
 *
 */
public class Utils {

	private final static String TAG = "Utils.class";
	
	public static void printReceivedMsg(ReceivedMessageType one)
	{
		switch(one.getEventType())
		{
		case MessageDataDefine.SMP_SEARCH_ODP_IP_ACK:	
			System.out.println("Received msg: --- " + "SMP_SEARCH_ODP_IP_ACK \n");
			break;
		case MessageDataDefine.SMP_TO_ODP_AUTH_ACK:
			System.out.println("Received msg: --- " + "SMP_TO_ODP_AUTH_ACK \n");
			break;
		case MessageDataDefine.SMP_SET_ODP_SSID_PSWD_ACK:
			System.out.println("Received msg: --- " + "SMP_SET_ODP_SSID_PSWD_ACK \n");
			break;
		case MessageDataDefine.SMP_SET_ODP_WIFI_MODE_ACK:
			System.out.println("Received msg: --- " + "SMP_SET_ODP_WIFI_MODE_ACK \n");
			break;
		case MessageDataDefine.SMP_SET_ODP_WIFI_CLIENT_PARAMETER_ACK:
			System.out.println("Received msg: --- " + "SMP_SET_ODP_WIFI_CLIENT_PARAMETER_ACK \n");
			break;
		case MessageDataDefine.SMP_SET_ODP_WIFI_AP_PARAMETER_ACK:
			System.out.println("Received msg: --- " + "SMP_SET_ODP_WIFI_AP_PARAMETER_ACK \n");
			break;
		case MessageDataDefine.SMP_ADD_ACCOUNT_SELF_ACK:
			System.out.println("Received msg: --- " + "SMP_ADD_ACCOUNT_SELF_ACK \n");
			break;
		case MessageDataDefine.SMP_ADD_ACCOUNT_OTHER_ACK:
			System.out.println("Received msg: --- " + "SMP_ADD_ACCOUNT_OTHER_ACK \n");
			break;
		case MessageDataDefine.SMP_TO_ODP_ADD_OTHER_ACCOUNT_ACK_ACK:
			System.out.println("Received msg: --- " + "SMP_TO_ODP_ADD_OTHER_ACCOUNT_ACK_ACK \n");
			break;
		case MessageDataDefine.SMP_SET_ODP_LOCAL_ACCOUNT_ACK:
			System.out.println("Received msg: --- " + "SMP_SET_ODP_LOCAL_ACCOUNT_ACK \n");
			break;
		case MessageDataDefine.SMP_GET_ODP_SMP_ACCOUNT_ACK:
			System.out.println("Received msg: --- " + "SMP_GET_ODP_SMP_ACCOUNT_ACK \n");
			break;
		case MessageDataDefine.SMP_REMOVE_ODP_SMP_ACCOUNT_ACK:
			System.out.println("Received msg: --- " + "SMP_REMOVE_ODP_SMP_ACCOUNT_ACK \n");
			break;
		case MessageDataDefine.SMP_GET_ODP_SYS_PARAMETER_ACK:
			System.out.println("Received msg: --- " + "SMP_GET_ODP_SYS_PARAMETER_ACK \n");
			break;
		case MessageDataDefine.SMP_SET_ODP_SYS_PARAMETER_ACK:
			System.out.println("Received msg: --- " + "SMP_SET_ODP_SYS_PARAMETER_ACK \n");
			break;
		case MessageDataDefine.SMP_TO_ODP_VERSION_CHECK_ACK:
			System.out.println("Received msg: --- " + "SMP_TO_ODP_VERSION_CHECK_ACK \n");
			break;
		case MessageDataDefine.SMP_TO_ODP_UPDATE_VERSION_ACK:
			System.out.println("Received msg: --- " + "SMP_TO_ODP_UPDATE_VERSION_ACK \n");
			break;
		case MessageDataDefine.SMP_SET_ODP_TIME_ACK:
			System.out.println("Received msg: --- " + "SMP_SET_ODP_TIME_ACK \n");
			break;
		case MessageDataDefine.SMP_GET_ODP_VERSION_ACK:
			System.out.println("Received msg: --- " + "SMP_GET_ODP_VERSION_ACK \n");
			break;
		default:
			break;
		}
	}
}
