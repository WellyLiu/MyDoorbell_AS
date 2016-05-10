/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-7 上午9:23:36
 * Project: TecomDoor
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.iptnet.c2c.C2CHandle;
import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.Door;
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
import com.gocontrol.doorbell.utils.Utils;
import com.ypy.eventbus.EventBus;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Administrator
 *
 */
public class DoorPhonePasswordInput extends PNPBaseActivity implements View.OnClickListener{

	private enum STATE{
		INIT, //初始状态
		FOUND_ODP, //已经找到ODP，即广播收到ODP的回应
		HAVE_AUTH, //认证已通过
		HAVE_SEND_ACCOUNT_SELF, //已发送自己账号
		HAVE_ACCOUNT_CONFIG, //账号配对已成功
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

	
	
	private static final int SEND_SEARCH_ODP = 1000;
	private static final int SEND_AUTH_INFO = 2000;
	private static final int SEND_ACCOUNT_CONFIG = 3000;
	private static final int CLEAR_PROCESS_DIALOG = 4000; //账号配对成功
	private static final int CONFIG_TIME_OUT = 5000; //账号配对超时失败，一般是搜寻无回应
	private static final int CONFIG_FAILED = 6000; //ODP回报配对失败
	private Button mButton;
	private EditText mPassword;
	private String userInputPwd;
	private ProgressDialog proDialog;
	private Handler mHandler;
	private String smpAccount;
	private String smpAccountPwd;
	private String odpAccount;
	private String odpLocalAcc;
	private String odpLocalPwd;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setmState(STATE.INIT);
		
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		ImageView img = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_back);
		img.setOnClickListener(this);
		
		TextView txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(getString(R.string.door_setup_pwd));
		
		txtTitle.setText(spanBuilder);
		
		setContentView(R.layout.doorphone_add_password_input);
		getWindow().setBackgroundDrawable(null);
		
		mButton = (Button)this.findViewById(R.id.activie_password);
		mButton.setOnClickListener(this);
		mPassword = (EditText)this.findViewById(R.id.door_phone_pwd_input);
		
		smpAccount = LocalUserInfo.getInstance().getC2cAccount();
		smpAccountPwd = LocalUserInfo.getInstance().getC2cPassword();
		Log.d("Tecom", "DoorPhonePasswordInput.... user:" + smpAccount + "  pwd:" + smpAccountPwd);
		smpAccount = AppUtils.processWebLoginAccount(smpAccount, getString(R.string.def_reg_domain));
		
		mHandler = new Handler()
		{

			/* (non-Javadoc)
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch(msg.what)
				{
				case SEND_SEARCH_ODP:
					if(mState == STATE.INIT){
						sendSearchODP();
						//每3s发一次，搜寻odp，一直到超时20s
						mHandler.sendEmptyMessageDelayed(SEND_SEARCH_ODP, 3000);
					}else
					{
						Log.d("Tecom", "mState != STATE.INIT, Can't send SEND_SEARCH_ODP...");
					}
					break;
				case SEND_AUTH_INFO :
					if(mState == STATE.FOUND_ODP){
						sendODPAuth(userInputPwd);
					}else
					{
						Log.d("Tecom", "mState != STATE.FOUND_ODP, Can't send SEND_AUTH_INFO...");
					}
					break;
				case SEND_ACCOUNT_CONFIG :
					if(mState == STATE.HAVE_AUTH)
					{
						sendAddSelfAccount();
						setmState(STATE.HAVE_SEND_ACCOUNT_SELF);
					}else
					{
						Log.d("Tecom", "mState != STATE.HAVE_AUTH, Can't send SEND_ACCOUNT_CONFIG...");
					}
					break;
				case CLEAR_PROCESS_DIALOG:
					EventBus.getDefault().post(new DoorPhoneListCloseDrawerEvent());
					if(proDialog != null){
						proDialog.dismiss();
					}
					//移除time out提醒
					mHandler.removeMessages(CONFIG_TIME_OUT);
					Toast.makeText(DoorPhonePasswordInput.this, DoorPhonePasswordInput.this.getString(R.string.pnp_successful), Toast.LENGTH_SHORT).show();
					System.out.printf("DoorPhonePasswordInput, 账号配对成功");
					Intent mIntent = new Intent(DoorPhonePasswordInput.this, DoorPhoneList.class);
					mIntent.putExtra("FROME_WHERE", "DoorPhonePasswordInput.class");
					startActivity(mIntent);
					//post evnent to PNP UI to finish themself.
					EventBus.getDefault().post(new PNPSuccessEvent());
					finish();
					break;
				case CONFIG_TIME_OUT:
					if(proDialog != null){
						proDialog.dismiss();
					}
					//移除重发的搜寻ODP
					mHandler.removeMessages(SEND_SEARCH_ODP);
					Toast.makeText(DoorPhonePasswordInput.this, DoorPhonePasswordInput.this.getString(R.string.send_wifi_ssid_fail), Toast.LENGTH_LONG).show();
					setmState(STATE.INIT);
					System.out.printf("DoorPhonePasswordInput, 账号配对失败,超时");
					break;
				case CONFIG_FAILED:
					if(proDialog != null){
						proDialog.dismiss();
					}
					Toast.makeText(DoorPhonePasswordInput.this, DoorPhonePasswordInput.this.getString(R.string.send_wifi_ssid_fail), Toast.LENGTH_LONG).show();
					setmState(STATE.INIT);
					System.out.printf("DoorPhonePasswordInput, 账号配对失败");
					break;
				default:
					break;
				}
			}
			
		};
	}



	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		setmState(STATE.INIT);
		mHandler.removeCallbacksAndMessages(null);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.activie_password:
			userInputPwd = mPassword.getEditableText().toString();
			Utils.ODP_System_Pwd = userInputPwd;
			showProcessDialog();			
			mHandler.sendEmptyMessage(SEND_SEARCH_ODP);			
			mHandler.sendEmptyMessageDelayed(CONFIG_TIME_OUT, 22*1000);
			break;
		case R.id.btn_back:
			finish();
			break;
		default:
			break;
		}
	}

	private void registerODP(String odpAccount, String odpLocalAcc,
			String odpLocalPwd) {
		// TODO Auto-generated method stub
		// get gcm token
		String token;
		if ((token = AppUtils.readGcmToken(this)).isEmpty()) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(DoorPhonePasswordInput.this, "Could not to get GCM token", Toast.LENGTH_SHORT).show();
			}});
			
			return ;
		}
		
		// setting notification
		Log.d("DoorPhonePasswordInput.class", "============1==================");
		Log.d("DoorPhonePasswordInput.class", token);
		Log.d("DoorPhonePasswordInput.class", odpAccount);
		int mLineId = C2CHandle.getInstance().setNotification(token, odpAccount, 8);
		Log.d("DoorPhonePasswordInput.class", "line id:" + mLineId);
		if (mLineId < 0) {
			Log.d("DoorPhonePasswordInput.class", "============2==================");
			//mHandler.sendEmptyMessageDelayed(REGISTER_NOTIFICATION_ODP, 2000);
			mLineId = Integer.MIN_VALUE;
			runOnUiThread(new Runnable() {
				public void run() {
					Log.d("DoorPhonePasswordInput.class", "============3==================");
					//Toast.makeText(WiFiSendSSID.this, "Setting notification (" + mLineId + ")", Toast.LENGTH_SHORT).show();
			}});
			
		}else{
			Log.d("DoorPhonePasswordInput.class", "register to GCM ok...");
		}
		
		Log.d("DoorPhonePasswordInput.class", "============4==================");
	}
	
	private String mODPIPAddress;
	public void onEvent(ReceivedODPEvent event){
				
		if( event.getMsg().getEventType() == MessageDataDefine.SMP_SEARCH_ODP_IP_ACK )
		{
			System.out.printf("DoorPhonePasswordInput... get SMP_SEARCH_ODP_IP_ACK.\n");
			//if the odp acc is in the smp account list, ignore this ACK.
			String tmp[] = event.getMsg().getPayloadStr();
			String mODPLoginAcc = Utils.getEqualString(tmp[3]);
			
			if(getmState() == STATE.INIT){
				setmState(STATE.FOUND_ODP);
				//记录当前搜寻到的ODP，后面只回应该ODP（应对多ODP）
				mODPIPAddress = event.getMsg().getServerIP();
			}
			else
			{
				Log.d("Tecom", this.getClass().getSimpleName() + " " + "The state is not STATE.INIT, so ignore this ACK...");
				return;
			}
			
			//remove the re-try search...
			mHandler.removeMessages(SEND_SEARCH_ODP);
			
			String str[] = event.getMsg().getPayloadStr();
			String mUnit_val = Utils.getEqualString(str[0]);
			String mWifi_mode = Utils.getEqualString(str[1]);			
			String mODP_system_password_mode = Utils.getEqualString(str[2]);
			
			if( mUnit_val.equals("2"))
			{
				setmState(STATE.INIT);
				mHandler.sendEmptyMessage(CONFIG_FAILED);
				Log.d("Tecom", "Private Protocal:" + "SMP_SEARCH_ODP_IP_ACK" + " 2 " + "the smp is full...");
				return;
			}
			if(mWifi_mode.equals("1"))
			{
				setmState(STATE.INIT);
				mHandler.sendEmptyMessage(CONFIG_FAILED);
				Log.d("Tecom", "Private Protocal:" + "SMP_SEARCH_ODP_IP_ACK" + " 1 " + "the odp AP mode...");
				return;
			}
			if(mODP_system_password_mode.equals("1"))
			{
				setmState(STATE.INIT);
				mHandler.sendEmptyMessage(CONFIG_FAILED);
				Log.d("Tecom", "Private Protocal:" + "SMP_SEARCH_ODP_IP_ACK" + " the odp password initial mode...");
				return;
			}else if(mODP_system_password_mode.equals("2"))
			{
				Log.d("Tecom", "Private Protocal:" + "SMP_SEARCH_ODP_IP_ACK" + " 1 " + "the odp password not initial mode...");
			}else
			{
				Log.d("Tecom", "Private Protocal:" + "SMP_SEARCH_ODP_IP_ACK" + " 1 " + "the odp password error mode...");
			}
			mHandler.sendEmptyMessage(SEND_AUTH_INFO);
			System.out.printf("DoorPhonePasswordInput... send ODP_AUTH msg.\n");
		}else if(event.getMsg().getEventType() == MessageDataDefine.SMP_TO_ODP_AUTH_ACK )
		{
			setmState(STATE.HAVE_AUTH);
			System.out.printf("DoorPhonePasswordInput... get SMP_TO_ODP_AUTH_ACK.\n");
			byte val = event.getMsg().getPayloadByte();
			if(val == 0x02)
			{
				setmState(STATE.INIT);
				mHandler.sendEmptyMessage(CONFIG_FAILED);
				Log.d("Tecom", "Private Protocal:" + "SMP_TO_ODP_AUTH_ACK" + " 0x02 " + "the auth failed...");
				return;
			}
			mHandler.sendEmptyMessage(SEND_ACCOUNT_CONFIG);				
		}else if(event.getMsg().getEventType() == MessageDataDefine.SMP_ADD_ACCOUNT_SELF_ACK )
		{
			setmState(STATE.HAVE_ACCOUNT_CONFIG);
			System.out.printf("DoorPhonePasswordInput... get SMP_ADD_ACCOUNT_SELF_ACK.\n");
			String str[] = event.getMsg().getPayloadStr();
			for(String s : str)
			{
				System.out.print(s);
			}
			
			odpAccount = Utils.getEqualString(str[1]);
			odpLocalAcc = Utils.getEqualString(str[2]);
			odpLocalPwd = Utils.getEqualString(str[3]);
			
			// 注册odp到gcm
			registerODP(odpAccount, odpLocalAcc, odpLocalPwd);

			if(ODPManager.getInstance().getOneODP(odpAccount) == null)
			{
				int index = Utils.getWhichDoorIndex(DoorPhonePasswordInput.this);
				Door.save(DoorPhonePasswordInput.this, new Door(index, odpAccount,
						odpLocalAcc, odpLocalPwd, ""));
				Utils.addODPToList(index, odpAccount, odpLocalAcc, odpLocalPwd, "");
			}
			else
			{
				Log.d("tecom", " pnp for client mode  OK!!! ==== but the odp has been in the ODP list ...");
			}
			mHandler.sendEmptyMessage(CLEAR_PROCESS_DIALOG);
			
			

		}
	}

	/**
	 * 
	 */
	private void sendSearchODP() {
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
	private void sendODPAuth(String pwd) {
		// TODO Auto-generated method stub
		RequestMessageType sendOneMsg = new RequestMessageType();
		
		
		sendOneMsg.setType(MessageDataDefine.P2P);
		sendOneMsg.updateMessage((short) 0x0102, new String[]
				{"ODP_local_account=" + com.gocontrol.doorbell.utils.BuildConfig.Account_ODP_Local_Default,
				"ODP_local_password=" + com.gocontrol.doorbell.utils.BuildConfig.Password_ODP_Local_Default,
				"ODP_system_password=" + pwd});
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
				{	
					"My_login_account=" + smpAccount,
					"My_login_password=" + smpAccountPwd,
					"My_local_account=" + com.gocontrol.doorbell.utils.BuildConfig.Account_Local_Default,
					"My_local_password=" +  com.gocontrol.doorbell.utils.BuildConfig.Password_Local_Default}); //smp终端本地acoount , admin, pwd, 1234
		
		MessageQueueManager.getInstance().addMessage(sendOneMsg);
		
	}
	
	
	protected void showProcessDialog() {
		// TODO Auto-generated method stub
		proDialog = android.app.ProgressDialog.show(this,
				getString(R.string.tecom_process_pnp_title),
				getString(R.string.tecom_precess_content));
		proDialog.show();
	}
}
