/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-1 上午10:00:46
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iptnet.c2c.C2CHandle;
import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.Door;
import com.gocontrol.doorbell.GetGcmTokenService;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.DoorPhoneListCloseDrawerEvent;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.bean.PNPSuccessEvent;
import com.gocontrol.doorbell.bean.ReceivedODPEvent;
import com.gocontrol.doorbell.message.MessageDataDefine;
import com.gocontrol.doorbell.message.MessageQueueManager;
import com.gocontrol.doorbell.message.RequestMessageType;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.utils.BuildConfig;
import com.gocontrol.doorbell.utils.LogUtils;
import com.gocontrol.doorbell.utils.Utils;
import com.ypy.eventbus.EventBus;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Administrator
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WiFiSendSSID extends Activity implements View.OnClickListener {

	private enum STATE{
		INIT, //初始状态
		SEND_SSID_PWD, //发送sdid和pwd
		RECEIVE_SSID_ACK, //收到发送ssid ack
		START_CLIENT_MODE, //发送ODP启动Client
		START_CLIENT_MODE_SUC, //发送ODP启动Client成功
		//clinet
		SEARCH_ODP,
		FOUND_ODP, //已经找到ODP，即广播收到ODP的回应
		SEND_ODP_AUTH,
		ODP_AUTH_OK,
		SEND_SWITCH_ACCOUNT,
		RECEIVE_ODP_ACCOUNT,
	}
	private STATE mState;
	/**
	 * @return the mState
	 */
	public STATE getmState() {
		return mState;
	}

	/**
	 * @param mState the mState to set
	 */
	public void setmState(STATE mState) {
		this.mState = mState;
	}
	
	
	private ListView listView;
	private WifiListAdapter listViewAdapter;

	private Handler mHandler;
	ProgressDialog proDialog;
	private boolean mCanSendAccount = false;
	private String smpAccount;
	private String smpAccountPwd;

	private boolean setODPNotification = false;
	
	private final int SEND_WIFI_SSID_PWD = 1000;
	private final int SEND_WIFI_SSID_PWD_ACK = 1001;
	private final int SEND_WIFI_SSID_PWD_FAIL = 1003;	
	private final int SEND_WIFI_SSID_PWD_SUCCESS = 1004;
	private final int START_ODP_SEARCH = 1005;
	
	//private final int START_ACCOUNT_SWITCH = 3000;
	///private final int ACCOUNT_SWITCH_SUCCESS = 4000;
	//private final int ACCOUNT_SWITCH_FAIL = 5000;
	//private final int ACCOUNT_SWITCH_FAIL2 = 5001;
	
	private final int REGISTER_NOTIFICATION_ODP = 6000;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private List<Map<String, Object>> dataList;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		odpAccount = getIntent().getStringExtra("ODP_ACC");
		odpLocalAcc = getIntent().getStringExtra("ODP_LOCAL_ACC");
		odpLocalPwd = getIntent().getStringExtra("ODP_LOCAL_PWD");
		Log.d("WiFiSendSSID" , odpAccount + " " + odpLocalAcc + " " + odpLocalPwd);
		
		setContentView(R.layout.wifi_send_ssid);

		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		
		TextView txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(getString(R.string.nortek_pnp_network));
		txtTitle.setText(spanBuilder);
		
		ImageView img = (ImageView) getActionBar().getCustomView()
				.findViewById(R.id.btn_back);
		img.setOnClickListener(this);

		EventBus.getDefault().register(this);
		
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
		mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener()
		{

			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
				
				refreshContent();
				
			}
			
		});
		listView = (ListView) findViewById(R.id.wifi_list);
		dataList = getWifiListData((WifiManager) getSystemService(Context.WIFI_SERVICE));
		listViewAdapter = new WifiListAdapter(this, dataList);
		listView.setAdapter(listViewAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				TextView txt = (TextView) view.findViewById(R.id.wifi_ssid);
				String title = WiFiSendSSID.this.getString(R.string.nortek_pnp_network_title);
				if (position == (listViewAdapter.getCount() - 1)) {
					popUpDialog(1, title, txt.getText().toString());
				} else {
					popUpDialog(2, title, txt.getText().toString());
				}
			}

		});

		mHandler = new Handler() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case SEND_WIFI_SSID_PWD:
					showProcessDialog();
					mHandler.sendEmptyMessageDelayed(SEND_WIFI_SSID_PWD_FAIL, 5000); //5s内送ssid没有成功，则认为失败
					break;
					
				case SEND_WIFI_SSID_PWD_FAIL:
					if (proDialog != null) {
						proDialog.dismiss();
					}
					Toast.makeText(WiFiSendSSID.this, WiFiSendSSID.this.getString(R.string.send_wifi_ssid_fail), Toast.LENGTH_LONG).show();
					break;
					
				case SEND_WIFI_SSID_PWD_SUCCESS:
					//cancel send ssid dialog .
					mHandler.removeMessages(SEND_WIFI_SSID_PWD_FAIL);
					
					if (proDialog != null) {
						proDialog.dismiss();

						
						new AlertDialog.Builder(WiFiSendSSID.this).setTitle(R.string.nortek_connect_home_wifi_title)//设置对话框标题  
						  
					     .setMessage(R.string.nortek_connect_home_wifi)//设置显示的内容  
					  
					     .setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {//添加确定按钮  
					  
					          
					  
					         @Override  
					  
					         public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
					  
					             // TODO Auto-generated method stub  
					  
					        	// 通知ODP切换到Client模式后，自己需要切换wifi到大网。
								Intent wifiSettingsIntent = new Intent(
											"android.settings.WIFI_SETTINGS");
									startActivity(wifiSettingsIntent);
									//finish();
									mCanSendAccount = true;//配过大网，才允许进行账号匹配
					  
					         }  
					  
					     }).setCancelable(false).show();
					     
						
					}
					break;
				
				case REGISTER_NOTIFICATION_ODP:
					Log.d("Tecom", "re-try odp register...");
					registerODP(odpAccount, odpLocalAcc, odpLocalPwd);
					
					Intent mIntent = new Intent(WiFiSendSSID.this, DoorPhoneList.class);					
					startActivity(mIntent);
					//post evnent to PNP UI to finish themself.
					EventBus.getDefault().post(new PNPSuccessEvent());
					finish();
					break;
				default:
					break;
				}
			}

		};
		
		smpAccount = LocalUserInfo.getInstance().getC2cAccount();
		smpAccountPwd = LocalUserInfo.getInstance().getC2cPassword();
		Log.d("Tecom", "WiFiChoose.... user:" + smpAccount + "  pwd:" + smpAccountPwd);
		smpAccount = AppUtils.processWebLoginAccount(smpAccount, getString(R.string.def_reg_domain));
	
		setmState(STATE.INIT);
	}

	/**
	 * 
	 */
	protected void refreshContent() {
		// TODO Auto-generated method stub		
		
		dataList = getWifiListData((WifiManager) getSystemService(Context.WIFI_SERVICE));
		listViewAdapter = new WifiListAdapter(this, dataList);
		listView.setAdapter(listViewAdapter);
		
		mSwipeRefreshLayout.setRefreshing(false);		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		// get GCM token
		if (AppUtils.readGcmToken(this).isEmpty()) {
			startService(new Intent(this, GetGcmTokenService.class));
		}
		if (mCanSendAccount) {
			mHandler.sendEmptyMessage(REGISTER_NOTIFICATION_ODP);
		}
		//if the current state is STATE.RECEIVE_ODP_ACCOUNT that has received the account from the ODP.the PNP process has finished.
		if(getmState() == STATE.RECEIVE_ODP_ACCOUNT)
		{
			mHandler.sendEmptyMessage(REGISTER_NOTIFICATION_ODP);
		}
	}

	
	/**
	 * 
	 */
	protected void showProcessDialog() {
		// TODO Auto-generated method stub
		proDialog = android.app.ProgressDialog.show(this,
				getString(R.string.nortek_pnp_configure),
				getString(R.string.tecom_precess_content));
		proDialog.setCancelable(true);
	}

	/**
	 * @param i
	 */
	protected void popUpDialog(final int i, final String title , final String APName) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(WiFiSendSSID.this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.ssid_dialog, null);
		if (i == 2) {
			textEntryView.findViewById(R.id.dialogname)
					.setVisibility(View.GONE);
		}
		builder.setTitle(title);
		builder.setView(textEntryView);
		builder.setPositiveButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				EditText password = (EditText) textEntryView
						.findViewById(R.id.etPassWord);
				EditText ssid = (EditText)textEntryView.findViewById(R.id.etUserName);
				
				System.out.printf("WiFiSendSSID..."
						+ ssid.getEditableText().toString() + "..."
						+ password.getText().toString());
				// 发送私有协议给ODP,先送SSID和PWD给ODP
				if(i==2)
					sendODPSSID(APName, password.getText()
						.toString());
				else
					sendODPSSID(ssid.getEditableText().toString(), password.getText()
							.toString());
				mHandler.sendEmptyMessage(SEND_WIFI_SSID_PWD);
				setmState(STATE.SEND_SSID_PWD);
			}
		});
		builder.setNegativeButton(this.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		builder.create().show();

	}

	/**
	 * @return
	 */
	private List<Map<String, Object>> getWifiListData(WifiManager wifiManager) {
		// TODO Auto-generated method stub
		ArrayList<ScanResult> mItems = new ArrayList<ScanResult>();
		List<ScanResult> results = wifiManager.getScanResults();
		int size = results.size();
		HashMap<String, Integer> signalStrength = new HashMap<String, Integer>();
		// //////////
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<WifiConfiguration> wifiConfigList = wifiManager
				.getConfiguredNetworks();// 得到配置好的网络信息
		try {
			for (int i = 0; i < size; i++) {
				ScanResult result = results.get(i);
				if (!result.SSID.isEmpty()) {
					String key = result.SSID + " " + result.capabilities;
					LogUtils.LOGD("WifiScan", "ssid:" + result.SSID + "level:"
							+ result.level);
					LogUtils.LOGD("WifiScan", result.toString());
					if (!signalStrength.containsKey(key)) {
						signalStrength.put(key, i);
						mItems.add(result);
						LogUtils.LOGD("tst", result.toString());
						Map<String, Object> map = new HashMap<String, Object>();
						boolean enableAuth = getOneAPAuth(wifiConfigList,
								result.SSID);
						if (enableAuth)
							map.put("image", R.drawable.wifi_signal);
						else
							map.put("image", R.drawable.wifi_signal_three);
						map.put("title", result.SSID);
						list.add(map);
					} else {
						/*
						 * int position = signalStrength.get(key); ScanResult
						 * updateItem = mItems.get(position); if
						 * (calculateSignalStength(updateItem.level) >
						 * calculateSignalStength(result.level)) {
						 * mItems.set(position, updateItem);
						 * 
						 * }
						 */
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("image", null);
		map.put("title", "Other WiFi AP Router");
		list.add(map);
		return list;
	}

	/**
	 * @param wifiConfigList
	 * @param sSID
	 * @return
	 */
	private boolean getOneAPAuth(List<WifiConfiguration> wifiConfigList,
			String sSID) {
		// TODO Auto-generated method stub
		for (int i = 0; i < wifiConfigList.size(); i++) {

			WifiConfiguration one = wifiConfigList.get(i);
			LogUtils.LOGD("tst", one.toString());
			if (sSID.equalsIgnoreCase(one.SSID)) {
				if (one.preSharedKey != null)
					return true;
				if (one.wepKeys != null)
					return true;
				if (!one.allowedKeyManagement
						.get(WifiConfiguration.KeyMgmt.NONE))
					return true;
			}
		}
		return false;
	}

	public int calculateSignalStength(int level) {
		return WifiManager.calculateSignalLevel(level, 5) + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		EventBus.getDefault().unregister(this);
		
		//该UI销毁后，需重新走 配置SSID流程
		mCanSendAccount = false;
		
		// unregister C2C listener
		//C2CHandle.getInstance().removeListener(mC2CEventListener);
		
		//remove register notificaton odp
		mHandler.removeMessages(REGISTER_NOTIFICATION_ODP);
		
		//remove all.
		mHandler.removeCallbacksAndMessages(null);
		
		setmState(STATE.INIT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 */
	private void sendODPSSID(String account, String pwd) {
		// TODO Auto-generated method stub
		RequestMessageType sendOneMsg = new RequestMessageType();
		Log.d("Tecom", "sendODPSSID ... " + Utils.ODP_System_Pwd);
		
		sendOneMsg.setType(MessageDataDefine.P2P);
		sendOneMsg.updateMessage((short) 0x0104, new String[] {
				"ODP_local_account=" + com.gocontrol.doorbell.utils.BuildConfig.Account_ODP_Local_Default, 
				"ODP_local_password=" +  com.gocontrol.doorbell.utils.BuildConfig.Password_ODP_Local_Default,
				"ODP_system_password=" + Utils.ODP_System_Pwd, 
				"SSID=" + account,
				"SSID_PSWD=" + pwd });

		MessageQueueManager.getInstance().addMessage(sendOneMsg);

	}

	/**
	 * 
	 */
	private void sendSearch() {
		// TODO Auto-generated method stub
		RequestMessageType sendOneMsg = new RequestMessageType();
		
		
		sendOneMsg.setType(MessageDataDefine.BROADCAST);
		sendOneMsg.updateMessage((short) 0x0100, new String[]{"SMP_ID=" + smpAccount,
				"SMP_LOCAL_ACCOUNT=" + BuildConfig.Account_Local_Default});
		
		MessageQueueManager.getInstance().addMessage(sendOneMsg);
		
	}

	/**
	 * 
	 */
	private void sendODPAuth() {
		// TODO Auto-generated method stub
		RequestMessageType sendOneMsg = new RequestMessageType();
		
		Log.d("Tecom", "WiFiSendSSID... ODP pwd:" + Utils.ODP_System_Pwd);
		sendOneMsg.setType(MessageDataDefine.P2P);
		sendOneMsg.updateMessage((short) 0x0102, new String[]
				{	"ODP_local_account=" + com.gocontrol.doorbell.utils.BuildConfig.Account_ODP_Local_Default,
					"ODP_local_password=" +  com.gocontrol.doorbell.utils.BuildConfig.Password_ODP_Local_Default ,
					"ODP_system_password=" + Utils.ODP_System_Pwd});
		sendOneMsg.setmODPIPAddress(mODPIPAddress);
		MessageQueueManager.getInstance().addMessage(sendOneMsg);
		
	}
	
	/**
	 * 
	 */
	private void sendAddSelfAccount() {
		// TODO Auto-generated method stub
		RequestMessageType sendOneMsg = new RequestMessageType();
		
		
		sendOneMsg.setType(MessageDataDefine.P2P);
		sendOneMsg.updateMessage((short) 0x0201, new String[]
				{	/*"My_login_account=tecom013@bronci.iptnet.net",
					"My_login_password=bronci20150930",*/
					"My_login_account=" + smpAccount,
					"My_login_password=" + smpAccountPwd,
					"My_local_account=" + com.gocontrol.doorbell.utils.BuildConfig.Account_Local_Default,
					"My_local_password=" + com.gocontrol.doorbell.utils.BuildConfig.Password_Local_Default}); //smp终端本地acoount , admin, pwd, 1234
		
		MessageQueueManager.getInstance().addMessage(sendOneMsg);
		
	}
	
	private String mODPInitMacPwd;
	private String mODPIPAddress;
	public void onEvent(ReceivedODPEvent event){
		
		if( event.getMsg().getEventType() == MessageDataDefine.SMP_SET_ODP_SSID_PSWD_ACK )
		{
			System.out.printf("WiFiSendSSID... get SMP_SET_ODP_SSID_PSWD_ACK.");
			setmState(STATE.RECEIVE_SSID_ACK);
			
			mHandler.removeMessages(SEND_WIFI_SSID_PWD_FAIL);
			//固定切ODP wifi到client 模式
			RequestMessageType sendOneMsg = new RequestMessageType();
			sendOneMsg.updateMessageData(MessageDataDefine.SMP_SET_ODP_WIFI_MODE, (byte)0x02);
			MessageQueueManager.getInstance().addMessage(sendOneMsg);
			setmState(STATE.START_CLIENT_MODE);
		}else if( event.getMsg().getEventType() == MessageDataDefine.SMP_SET_ODP_WIFI_MODE_ACK )
		{
			System.out.printf("WiFiSendSSID... get SMP_SET_ODP_WIFI_MODE_ACK.");
			setmState(STATE.START_CLIENT_MODE_SUC);
			mODPInitMacPwd = event.getMsg().getServerMac();
			Log.d("Tecom", this.getClass().getSimpleName() + " " + "ODP Ack Mac:" + mODPInitMacPwd);
			//此时ODP已经开始切wifi到client模式，smp自己开始切换到大网wifi
			mHandler.sendEmptyMessage(SEND_WIFI_SSID_PWD_SUCCESS);
		}
		/*
		if( event.getMsg().getEventType() == MessageDataDefine.SMP_SEARCH_ODP_IP_ACK )
		{
			System.out.printf("WiFiSendSSID... get SMP_SEARCH_ODP_IP_ACK.");
			//以下部分处理Lan内存在多个ODP的情况，前面有设定过ODP的SSID的，才能进行配对，否则忽略。
			String odpMac = event.getMsg().getServerMac();
			if(!TextUtils.isEmpty(odpMac))
			{
				if(!odpMac.equalsIgnoreCase(mODPInitMacPwd))
				{
					Log.d("Tecom", this.getClass().getSimpleName() + " " + "ODP SMP_SEARCH_ODP_IP_ACK Mac:" + odpMac + "SMP_SET_ODP_WIFI_MODE_ACK, Mac:" + mODPInitMacPwd);
					
					return;
				}
			}
			mODPIPAddress = event.getMsg().getServerIP(); //记录正确ODP的ip（多个ODP的情况下，UDP部分记录的IP被另外一个ODP改写了）
			//remove the re-try search...
			mHandler.removeMessages(START_ODP_SEARCH);
			
			byte val = event.getMsg().getPayloadByte();
			if( val == 0x02)
			{
				mHandler.removeMessages(ACCOUNT_SWITCH_FAIL);
				mHandler.sendEmptyMessageDelayed(ACCOUNT_SWITCH_FAIL, 500);
				Log.d("Tecom", "Private Protocal:" + "SMP_SEARCH_ODP_IP_ACK" + " 0x02 " + "the smp is full...");
				return;
			}
			setmState(STATE.FOUND_ODP);
			sendODPAuth();
			setmState(STATE.SEND_ODP_AUTH);
			System.out.printf("WiFiSendSSID... send ODP_AUTH msg.");
		}else if(event.getMsg().getEventType() == MessageDataDefine.SMP_TO_ODP_AUTH_ACK )
		{
			if( !mCanSendAccount )
			{
				Log.d("tst", "the SMP_TO_ODP_AUTH_ACK is from the last activity process, so ignore it...");
				return;
			}
			System.out.printf("WiFiSendSSID... get SMP_TO_ODP_AUTH_ACK.");
			byte val = event.getMsg().getPayloadByte();
			if(val == 0x02)
			{
				mHandler.removeMessages(ACCOUNT_SWITCH_FAIL);
				mHandler.sendEmptyMessageDelayed(ACCOUNT_SWITCH_FAIL, 500);
				Log.d("Tecom", "Private Protocal:" + "SMP_TO_ODP_AUTH_ACK" + " 0x02 " + "the auth failed...");
				return;
			}
			setmState(STATE.ODP_AUTH_OK);
			sendAddSelfAccount();	
			setmState(STATE.SEND_SWITCH_ACCOUNT);
		}else if(event.getMsg().getEventType() == MessageDataDefine.SMP_ADD_ACCOUNT_SELF_ACK )
		{
			System.out.printf("WiFiSendSSID... get SMP_ADD_ACCOUNT_SELF_ACK.");
			String str[] = event.getMsg().getPayloadStr();
			for(String s : str)
			{
				System.out.print(s);
			}
			 odpAccount = getEqualString(str[1]);
			 odpLocalAcc = getEqualString(str[2]);
			 odpLocalPwd = getEqualString(str[3]);
			 setmState(STATE.RECEIVE_ODP_ACCOUNT);
			 //注册odp到gcm
			 registerODP(odpAccount, odpLocalAcc, odpLocalPwd);
			
			 mHandler.sendEmptyMessage(ACCOUNT_SWITCH_SUCCESS);	
			 if(ODPManager.getInstance().getOneODP(odpAccount) == null)
			 {
				 int index = getWhichDoorIndex();
				 String name = "";
				 if(DoorName.ODPName != null)
					 name = DoorName.ODPName;
				 Door.save(WiFiSendSSID.this, new Door(index, odpAccount,
							odpLocalAcc, odpLocalPwd, name));
				 Utils.addODPToList(index, odpAccount,
							odpLocalAcc, odpLocalPwd,name);
			 }
			 else
			 {
				 Log.d("tecom", " pnp for AP mode  OK!!! ==== but the odp has been in the ODP list ...");
			 }			 
		}*/
	}

	//////////////////////////////////////////////////////////////////
	String odpAccount /*= "80130@bronci.iptnet.net"*/;
	String odpLocalAcc /*= "root"*/;
	String odpLocalPwd /*= "admin"*/;
	private boolean mClear = false;
	private int mLineId = Integer.MIN_VALUE;
	
	private void registerODP(String odpAccount, String odpLocalAcc,
			String odpLocalPwd) {
		// TODO Auto-generated method stub
		// get gcm token
		String token;
		if ((token = AppUtils.readGcmToken(this)).isEmpty()) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(WiFiSendSSID.this, "Could not to get GCM token", Toast.LENGTH_SHORT).show();
			}});
			
			return ;
		}
		
		// setting notification
		mClear = false;
		Log.d("tecom", "============1==================");
		Log.d("tecom", token);
		Log.d("tecom", odpAccount);
		mLineId = C2CHandle.getInstance().setNotification(token, odpAccount, 8);
		Log.d("tecom", "line id:" + mLineId);
		if (mLineId < 0) {
			Log.d("tecom", "============2==================");
			//mHandler.sendEmptyMessageDelayed(REGISTER_NOTIFICATION_ODP, 2000);
			mLineId = Integer.MIN_VALUE;
			runOnUiThread(new Runnable() {
				public void run() {
					Log.d("tecom", "============3==================");
					//Toast.makeText(WiFiSendSSID.this, "Setting notification (" + mLineId + ")", Toast.LENGTH_SHORT).show();
			}});
			
		}else{
			Log.d("Tecom", "register to GCM ok...");
		}
		
		Log.d("tecom", "============4==================");
	}
	
	public int getWhichDoorIndex()
	{
		for(int i=0; i<BuildConfig.MAX_ODP_NUM; i++){
			Door door = Door.read(this, i);
		
			if (door.getId().isEmpty()){
				return i;
			}
		}
		//如果outdoor已满，则直接覆盖最后一个Outdoor的配对文件
		return BuildConfig.MAX_ODP_NUM - 1;
	}
}
