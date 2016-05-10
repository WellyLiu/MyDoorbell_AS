/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-9 AM11:37:59
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.service
 */
package com.gocontrol.doorbell.service;

import com.iptnet.c2c.ByteArrayCommand;
import com.iptnet.c2c.C2CChannel;
import com.iptnet.c2c.C2CCommand;
import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.iptnet.c2c.C2CListener;
import com.iptnet.c2c.ProtocolChannel;
import com.iptnet.c2c.StringCommand;
import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.DoorService;
import com.gocontrol.doorbell.GetGcmTokenService;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.bean.ReceivedC2CEvent;
import com.gocontrol.doorbell.message.DataConversion;
import com.gocontrol.doorbell.message.DataQueueManager;
import com.gocontrol.doorbell.message.MessageDataDefine;
import com.gocontrol.doorbell.message.MessageQueueManager;
import com.gocontrol.doorbell.message.MessageType;
import com.gocontrol.doorbell.message.ReceivedMessageType;
import com.gocontrol.doorbell.message.RequestMessageType;
import com.gocontrol.doorbell.model.IProxSensor;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.model.SystemConfigManager;
import com.gocontrol.doorbell.model.IProxSensor.IProxSensorListener;
import com.gocontrol.doorbell.utils.LogUtils;
import com.gocontrol.doorbell.utils.Utils;
import com.gocontrol.doorbell.utils.WebRtcAecManager;
import com.ypy.eventbus.EventBus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Administrator
 * 
 */
public class AppUtilsService extends Service {

	private final static String TAG = "AppUtilsService";
	private static boolean mC2CRegister = false;
	
	private final static int MOTION_LOG = 1000;
	private final static int PIR_LOG = 2000;
	private final static int LOG_OUT_BY_SERVER = 3000;
	
	private SensorManager sm;
	private Sensor mySensor;
	private SensorEventListener mySensorListener;
	private Context mContext;
	private Handler mHandler;
	// ntut process
	// /////////////////
	private String mRegAccount;
	private C2CListener mC2CListener = new C2CListener() {

		
		@Override
		protected synchronized void receiveCommand(C2CChannel channel, C2CCommand command) {
			// TODO Auto-generated method stub
			Log.w(TAG, "=========receive command===========");
			
			if (channel instanceof ProtocolChannel)
			{
				if(command instanceof ByteArrayCommand)
				{
					Log.d(TAG, " ByteArrayCommand");
				}
				if(command instanceof StringCommand)
				{
					Log.d(TAG, " StringCommand");
					ProtocolChannel ch = (ProtocolChannel)channel;
					String peerId = ch.getPeerId();
					String account = ch.getLoginAccount();
					String pwd = ch.getLoginPassword();
					StringCommand stringCommand = (StringCommand)command;					
					Log.d(TAG, stringCommand.getCommand());
					//之前只分ODP online or offline，目前加上“配对未上线”，故屏蔽
					//EventBus.getDefault().post(new ReceivedC2CEvent(peerId,account,pwd));
				}
				
			}
			if (channel instanceof ProtocolChannel && command instanceof ByteArrayCommand) {
				
				Log.w(TAG, "Command, ProtocolChannel, ByteArrayCommand");
				ProtocolChannel ch = (ProtocolChannel) channel;
				ByteArrayCommand bCommand = (ByteArrayCommand) command;

				int line = ch.getLine();
				int chan = ch.getChannel();
				String peerId = ch.getPeerId();
				String account = ch.getLoginAccount();
				String pwd = ch.getLoginPassword();
				Log.d("Tecom", "ProtocolChannel info:" + " line:" + line
						+ " chan: " + chan + " peerId: " + peerId
						+ " account: " + account + " pwd:" + pwd);
				int commandStructure = bCommand.getCommandStructure();
				int commandType = bCommand.getCommandType();
				int dataType = bCommand.getDataType();
				byte[] data = bCommand.getData();
				int length = bCommand.getLength();

				Log.d("Tecom", "commandStructure:" + commandStructure
						+ " commandType:" + commandType + " dataType:"
						+ dataType + " length:" + length + " data length:"
						+ data.length);
				if (data != null)
					DataConversion.printHexString("Data info", data);

				//construct message.
				if (data.length < 38) {
					System.out.println(this.toString()
							+ " == received data length error.");
					return;
				}
				byte[] len = new byte[] { data[34], data[35], data[36],
						data[37] };
				DataConversion.printHexString("received payload length:", len);
				int payloadLength = DataConversion.bytesToInt2(len, 0);
				System.out.println(payloadLength);
				System.out.println(data.length);
				if ((payloadLength + 38) != data.length) {
					System.out.println(this.toString()
							+ " == received data length error 2.");
					return;
				}
				// 构建ReceivedMessageType
				ReceivedMessageType one = new ReceivedMessageType(data);
				ReceivedC2CEvent c2cEvt = new ReceivedC2CEvent(peerId, account,
						pwd, data);
				c2cEvt.setMsg(one);
				EventBus.getDefault().post(c2cEvt);
				
				processMessageFromODPToSMP(c2cEvt);
			}
			
		}

		@Override
		protected void receiveMessage(C2CEvent event) {
			int lineId = event.getLine();
			Log.e(TAG, "service : c2c event = " + event + " lineId = " + lineId);
			
			// process logout by server
			if (C2CEvent.C2C_LOGOUT_BY_SVR == event) {
				Log.w(TAG, "occur logout!!, redo start register process");
				//提示用户
				mHandler.sendEmptyMessage(LOG_OUT_BY_SERVER);
			}else if (C2CEvent.C2C_REGISTER_DONE == event) {
					printMessage(mRegAccount, true);
					Log.e("Tecom", "home :reg done");
					if(ODPManager.getInstance().getRegisterGCMStatus() != 1)
					{
						Log.d("tecom", "try register GCM to odp.");
						ODPManager.getInstance().registerAllODP(mContext);
					}
			}else if (C2CEvent.C2C_REGISTER_FAIL == event) {
				printMessage(mRegAccount, false);
				Log.e("Tecom", "home :reg fail");
				if(event.getSubEvent() != null)
				{
					Log.e("Tecom", "reg fail reason:" + event.getSubEvent());
				}
			}else if(C2CEvent.C2C_SETUP_DONE == event)
			{
				Log.e("Tecom", "home :C2CEvent.C2C_SETUP_DONE");
				EventBus.getDefault().post(event);
				
			}else if(C2CEvent.C2C_SETUP_ERROR == event)
			{
				Log.e("Tecom", "home :C2C_SETUP_ERROR");
				EventBus.getDefault().post(event);
			}
		}
	};

	private void printMessage(final String agentId, final boolean isRegDone) {

		// update text information

		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		String sdkVer = C2CHandle.getInstance().getSDKVersion();
		builder.append("Register Server: "
				+/* AppUtils.readC2CLoginParams(AppApplication.getInstance())[2])*/ LocalUserInfo.getInstance().getC2cAccount());
		builder.append("\nRegister Account: "
				+ (agentId == null ? "[EMPTY]" : agentId));
		builder.append("\nC2C Module Version: " + C2CHandle.VERSION + " (SDK:"
				+ sdkVer + ")");
		if (isRegDone)
			builder.append("\nOnline (C2C Register Done)");
		else
			builder.append("\nOffline (C2C Register Fail)");

		System.out.print("Tecom==" + builder.toString() + "\n");
	}

	/**
	 * @param c2cEvt
	 * 以下message由ODP主动发送，故在此处理；
	 * 其他SMP请求，ODP回应的Message不应该在此处理。
	 *
	 */
	protected void processMessageFromODPToSMP(ReceivedC2CEvent c2cEvt) {
		// TODO Auto-generated method stub
		switch(c2cEvt.getMsg().getEventType())
		{
			//如是是ODP_ASK_SMP_REG_STATUS (0x0803)，则在这里直接回应
		case MessageDataDefine.ODP_ASK_SMP_REG_STATUS: 
			String mPeerId = c2cEvt.getPeerId();
			Log.d("Tecom", "Got  ODP_ASK_SMP_REG_STATUS... peerId:" + mPeerId);
			if(!TextUtils.isEmpty(mPeerId))
			{	
				RequestMessageType sendOneMsg = new RequestMessageType();
				MessageType.C2CAccountInfo peerInfo = new MessageType.C2CAccountInfo();
				peerInfo.peerId = mPeerId;
				peerInfo.loginAccount = com.gocontrol.doorbell.utils.BuildConfig.Account_ODP_Local_Default;
				peerInfo.loginPassword = com.gocontrol.doorbell.utils.BuildConfig.Password_ODP_Local_Default;
				sendOneMsg.setType(MessageDataDefine.C2C);
				sendOneMsg.setPeerAccountInfo(peerInfo);
				//Data format: 1 byte,  1: online  2��offline  3��SMPû�д�ODP�˻������Ϣ
				if(ODPManager.getInstance().getOneODP(mPeerId) != null)
				{
					sendOneMsg.updateMessageData(MessageDataDefine.ODP_ASK_SMP_REG_STATUS_ACK, (byte)0x01);
						
				}else
				{
					Log.d("Tecom", "Send ODP_ASK_SMP_REG_STATUS_ACK... ODP account is not in the smp lost...");
					sendOneMsg.updateMessageData(MessageDataDefine.ODP_ASK_SMP_REG_STATUS_ACK, (byte)0x03);
				}
				MessageQueueManager.getInstance().addMessage(sendOneMsg);
			}
			break;
		case MessageDataDefine.ODP_MOTION_DETECT_EVENT:
			ReceivedMessageType msg = c2cEvt.getMsg();
			if(msg.getPayloadStr() == null)
				break;
			for(String s : msg.getPayloadStr())
			{
				System.out.println(s);
			}
			if(TextUtils.isEmpty(msg.getPayloadStr()[0]))
				return;
			try{
				String log[] = new String[]{Utils.getEqualString(msg.getPayloadStr()[0]),
						Utils.getEqualString(msg.getPayloadStr()[1])};
				//send notification.
				Message ont = mHandler.obtainMessage();
				ont.what = MOTION_LOG;
				ont.obj = log;
				mHandler.sendMessage(ont);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			////
			break;
		case MessageDataDefine.ODP_PIR_DETECT_EVENT:
			ReceivedMessageType pir = c2cEvt.getMsg();
			if( pir.getPayloadStr() == null)
				break;
			for(String s : pir.getPayloadStr())
			{
				System.out.println(s);
			}
			try{
			String pirLog[] = new String[]{Utils.getEqualString(pir.getPayloadStr()[0]),
					Utils.getEqualString(pir.getPayloadStr()[1])};
			//send notification.
			Message on = mHandler.obtainMessage();
			on.what = PIR_LOG;
			on.obj = pirLog;
			mHandler.sendMessage(on);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			break;
		default:
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		Log.d("tecom", "AppUtilService onCreate...");
		Utils.initiaODPStatus(this); //退出,重启,默认设置离线;(不走Application)
		LogUtils.StartLogCatToFile(this);
		
		mContext = this;
		//init NTUT Process.
		ntupProcess();
		
		// Prox sensor
		initSensors();
		
		sm.registerListener(mySensorListener, mySensor,
				SensorManager.SENSOR_DELAY_NORMAL);

		// 启动私有协议
		MessageQueueManager.getInstance().startProcessMessage();
		DataQueueManager.getInstance().startDataProcessMessage();
		
		mHandler = new Handler(){

			/* (non-Javadoc)
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch(msg.what)
				{
				case MOTION_LOG:
					String data[] = (String [])msg.obj;
					String title = data[0];
					String message = data[1];
					NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				     NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
				     .setSmallIcon(R.drawable.ic_launcher)	
				      .setContentTitle(title)
				     .setContentText(message);
				     Notification  notification = mBuilder.build();
				     notification.defaults = Notification.DEFAULT_ALL;
				     notificationManager.notify(1, notification);
					
					/*
					String str2[] = TextUtils.split((String) msg.obj, "=");
					if(str2.length < 2)
						break;
					
					if(TextUtils.isEmpty(str2[0]) || TextUtils.isEmpty(str2[1]))
						break;
					
					NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				     NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
				     .setSmallIcon(R.drawable.ic_launcher)
				     .setContentTitle("Event")
				     .setContentText(str2[0] + " " + str2[1]);
				     Notification  notification = mBuilder.build();
				     notification.defaults = Notification.DEFAULT_ALL;
				     notificationManager.notify(1, notification);
				     
					//Toast.makeText(mContext, mContext.getString(R.string.motion_detect_tip) + msg.obj, Toast.LENGTH_SHORT).show();
					 
					 */
					break;
				case PIR_LOG:
					String data1[] = (String [])msg.obj;
					String title1 = data1[0];
					String message1 = data1[1];
					NotificationManager notificationManager2= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				     NotificationCompat.Builder mBuilder2 = new NotificationCompat.Builder(mContext)
				     .setSmallIcon(R.drawable.ic_launcher)		
				      .setContentTitle(title1)
				     .setContentText(message1);
				     Notification  notification2 = mBuilder2.build();
				     notification2.defaults = Notification.DEFAULT_ALL;
				     notificationManager2.notify(1, notification2);
				     
				     
					/*
					String str[] = TextUtils.split((String) msg.obj, "=");
					if(str.length < 2)
						break;
					
					if(TextUtils.isEmpty(str[0]) || TextUtils.isEmpty(str[1]))
						break;
					NotificationManager notificationManager2= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				     NotificationCompat.Builder mBuilder2 = new NotificationCompat.Builder(mContext)
				     .setSmallIcon(R.drawable.ic_launcher)
				     .setContentTitle("Event")
				     .setContentText(str[0] + " " + str[1]);
				     Notification  notification2 = mBuilder2.build();
				     notification2.defaults = Notification.DEFAULT_ALL;
				     notificationManager2.notify(1, notification2);
				     
					//Toast.makeText(mContext, mContext.getString(R.string.pir_tip) + msg.obj, Toast.LENGTH_SHORT).show();
					  */
					 
					break;
				case LOG_OUT_BY_SERVER:
					Toast.makeText(mContext, getString(R.string.account_exception), Toast.LENGTH_SHORT).show();
					break;
				}
			}
			
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		
		if(intent != null)
		{
			
				String command = (String)intent.getStringExtra("COMMAND_TYPE");
				if(!TextUtils.isEmpty(command))
				{
					if(command.equalsIgnoreCase("UPDATE_NTUT_PROCESS"))
					{
						Log.d("Tecom", " start update C2C process ntut ....");
						ntupUpdateC2CProcess();
					}
				}
			
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// unregister listener
		C2CHandle.getInstance().removeListener(mC2CListener);
		
		mHandler.removeCallbacksAndMessages(null);
		sm.unregisterListener(mySensorListener);

		//delete log file
		LogUtils.DeleteLogFile();
		//注销ODP到GCM的注册
		//ODPManager.getInstance().unRegisterAllODP(this);

		WebRtcAecManager.getInstance().destroyWebRtcAec();

		//store the local settins.
		Utils.saveAppAutoLogin(this, SystemConfigManager.getInstance().isAppAutoLogin());

		// 停止收发私有协议数据
		MessageQueueManager.getInstance().stopProcessMessageQueueThread();
		DataQueueManager.getInstance().stopDataMessageQueueThread();
		
	}

	private void initSensors() {
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mySensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		mySensorListener = new SensorEventListener() {
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}

			@Override
			public void onSensorChanged(SensorEvent event) {
				// if (event.timestamp == 0) return; //just ignoring for nexus 1
				boolean sLastProximitySensorValueNearby = Utils
						.isProximitySensorNearby(event);
				IProxSensor.proxSensorNear = sLastProximitySensorValueNearby;
				for (IProxSensorListener l : IProxSensor
						.getProxSensorListener())
					l.onProximitySensorChanged(sLastProximitySensorValueNearby);

			}
		};
	}

	private void ntupUpdateC2CProcess() {
		ntupProcess();
	}
	
	private void ntupProcess() {
		// TODO Auto-generated method stub
		// register listener
		C2CHandle.getInstance().addListener(mC2CListener);

		// get registration account
		//String[] params = AppUtils.readC2CLoginParams(this);
		String acc = LocalUserInfo.getInstance().getC2cAccount();
		String pwd = LocalUserInfo.getInstance().getC2cPassword();
		String srv = LocalUserInfo.getInstance().getC2cServer();

		// show account
		boolean isRegDone = C2CHandle.getInstance().isRegistrationDone();
		printMessage(acc, isRegDone);
		mRegAccount = acc;

		// start service to registration
		boolean running = AppUtils.isServiceRunning(this, DoorService.class);
		if (!running && !TextUtils.isEmpty(acc) && !TextUtils.isEmpty(pwd)) {
			startService(new Intent(this, DoorService.class)
					.putExtra("server", srv).putExtra("account", acc)
					.putExtra("password", pwd));
		}

		// get GCM token
		if (AppUtils.readGcmToken(this).isEmpty()) {
			startService(new Intent(this, GetGcmTokenService.class));
		}
	}

}
