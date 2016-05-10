package com.gocontrol.doorbell.message;
/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-20 PM3:40:42
 * Project: Cloud_Phone_Demo
 * PackageName: java.tecom.door.message
 */


/**
 * @author Administrator
 *
 */
public class MessageDataDefine {

	public static final int BROADCAST = 1;
	public static final int P2P = 2;
	public static final int C2C = 3;
	public static final int ODP_RECEIVE_PORT = 9081;
	public static final int ODP_SEND_PORT = 9080;
	
	
	//////////////////////////////////////////////////////////
	public static final short PROTOCOL_VERSION = 1;
	
	public static final short SMP_SEARCH_ODP_IP = 0x0100;
	public static final short SMP_SEARCH_ODP_IP_ACK = 0x0101;
	public static final short SMP_TO_ODP_AUTH = 0x0102;
	public static final short SMP_TO_ODP_AUTH_ACK  = 0x0103;
	public static final short SMP_SET_ODP_SSID_PSWD = 0x0104;
	public static final short SMP_SET_ODP_SSID_PSWD_ACK = 0x0105;
	public static final short SMP_SET_ODP_WIFI_MODE = 0x0106;	
	public static final short SMP_SET_ODP_WIFI_MODE_ACK  = 0x0107;
	
	public static final short SMP_SET_ODP_WIFI_CLIENT_PARAMETER = 0x0108;
	public static final short SMP_SET_ODP_WIFI_CLIENT_PARAMETER_ACK = 0x0109;
	public static final short SMP_SET_ODP_WIFI_AP_PARAMETER = 0x010A;
	public static final short SMP_SET_ODP_WIFI_AP_PARAMETER_ACK = 0x010B;
	
	public static final short SMP_ADD_ACCOUNT_SELF = 0x0201;
	public static final short SMP_ADD_ACCOUNT_SELF_ACK = 0x0202;
	public static final short SMP_ADD_ACCOUNT_OTHER = 0x0203;
	public static final short SMP_ADD_ACCOUNT_OTHER_ACK = 0x0204;
	public static final short SMP_TO_SMP_ADD_ACCOUNT_ACK = 0x0205; 
	public static final short SMP_TO_SMP_ADD_ACCOUNT_ACK_ACK = 0x0206;
	public static final short SMP_TO_ODP_ADD_OTHER_ACCOUNT_ACK_ACK = 0x0207;
	
	
	public static final short SMP_SET_ODP_LOCAL_ACCOUNT = 0x0301;
	public static final short SMP_SET_ODP_LOCAL_ACCOUNT_ACK = 0x0302;
	public static final short SMP_SET_ODP_SYSTEM_PSWD = 0x0305;
	public static final short SMP_SET_ODP_SYSTEM_PSWD_ACK = 0x0306;
	
	public static final short SMP_GET_ODP_SMP_ACCOUNT = 0x0401;
	public static final short SMP_GET_ODP_SMP_ACCOUNT_ACK = 0x0402;
	public static final short SMP_REMOVE_ODP_SMP_ACCOUNT = 0x0403;
	public static final short SMP_REMOVE_ODP_SMP_ACCOUNT_ACK = 0x0404;
	
	
	public static final short SMP_GET_ODP_SYS_PARAMETER  = 0x0501;
	public static final short SMP_GET_ODP_SYS_PARAMETER_ACK  = 0x0502;
	public static final short SMP_SET_ODP_SYS_PARAMETER  = 0x0503;
	public static final short SMP_SET_ODP_SYS_PARAMETER_ACK  = 0x0504;
	public static final short SMP_GET_ODP_QUIET_TIME  = 0x0505;
	public static final short SMP_GET_ODP_QUIET_TIME_ACK  = 0x0506;
	public static final short SMP_SET_ODP_QUIET_TIME  = 0x0507;
	public static final short SMP_SET_ODP_QUIET_TIME_ACK  = 0x0508;
	
	public static final short SMP_GET_ODP_VERSION  = 0x0601;
	public static final short SMP_GET_ODP_VERSION_ACK  = 0x0602;
	public static final short SMP_TO_ODP_VERSION_CHECK  = 0x0603;
	public static final short SMP_TO_ODP_VERSION_CHECK_ACK  = 0x0604;
	public static final short SMP_TO_ODP_UPDATE_VERSION  = 0x0605;
	public static final short SMP_TO_ODP_UPDATE_VERSION_ACK  = 0x0606;
	
	public static final short SMP_SET_ODP_TIME   = 0x0701;
	public static final short SMP_SET_ODP_TIME_ACK  = 0x0702;
	
	public static final short SMP_SET_ODP_NAME = 0x0703;
	public static final short SMP_SET_ODP_NAME_ACK = 0x0704;
	public static final short SMP_SET_ODP_SYSLOG  = 0x0705;
	public static final short SMP_SET_ODP_SYSLOG_ACK = 0x706;
	public static final short SMP_GET_ODP_SYSLOG = 0x0707;
	public static final short SMP_GET_ODP_SYSLOG_ACK = 0x708;
	
	public static final short SMP_ASK_ODP_REG_STATUS = 0x0801;
	public static final short SMP_ASK_ODP_REG_STATUS_ACK = 0x0802;
	public static final short ODP_ASK_SMP_REG_STATUS = 0x0803;
	public static final short ODP_ASK_SMP_REG_STATUS_ACK = 0x0804;
	
	public static final short ODP_MOTION_DETECT_EVENT = 0x0901;
	public static final short ODP_MOTION_DETECT_EVENT_ACK = 0x0902;
	public static final short ODP_PIR_DETECT_EVENT  = 0x0903;
	public static final short ODP_PIR_DETECT_EVENT_ACK = 0x0904;
	public static final short SMP_QUERY_MOTION_DETECT_LOG = 0x0905;
	public static final short SMP_QUERY_MOTION_DETECT_LOG_ACK = 0x0906;
	public static final short SMP_QUERY_PIR_DETECT_LOG = 0x0907;
	public static final short SMP_QUERY_PIR_DETECT_LOG_ACK = 0x0908;
}
