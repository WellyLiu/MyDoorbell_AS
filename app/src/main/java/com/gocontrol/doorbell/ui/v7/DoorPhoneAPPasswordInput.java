/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-7 AM9:23:36
 * Project: TecomDoor
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.Door;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.bean.ReceivedODPEvent;
import com.gocontrol.doorbell.message.MessageDataDefine;
import com.gocontrol.doorbell.message.MessageQueueManager;
import com.gocontrol.doorbell.message.RequestMessageType;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.utils.BuildConfig;
import com.gocontrol.doorbell.utils.Utils;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Administrator
 *
 */
public class DoorPhoneAPPasswordInput extends PNPBaseActivity implements View.OnClickListener{

	private enum STATE{
		INIT, //初始状态
		HAVE_AUTH, //认证已通过
		SMP_SEND_ACC, //发送SMP帐号信息
		ODP_SEND_ACC, //ODP回复帐号信息
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

	private final int SEND_TIME_OUT = 1000;
	private final int ODP_AUTH_OK = 2000;
	private final int CONFIG_FAILED_1 = 3000;
	private final int CONFIG_FAILED_2 = 3001;
	private final int CONFIG_FAILED_3 = 3002;
	private final int CONFIG_FAILED_4 = 3003;
	private final int ODP_UPDATE_PWD_OK = 4000;
	private final int SMP_SEND_ACC_TO_ODP = 5000;
	private final int ODP_SEND_SMP_ACC = 5001;
	private final int ACCOUNT_SWITCH_SUCCESS = 6000;
	
	private final long TIME_OUT = 10*1000;
	
	private Button mButton;
	private EditText mPassword, mOldPwd;
	private String userInputPwd;
	private ProgressDialog proDialog;
	private Handler mHandler;
	private String smpAccount;
	private String smpAccountPwd;
	private String odpAccount;
	private String odpLocalAcc;
	private String odpLocalPwd;
	
	private String type;
	
	private TextView mTextView;
	private String userOldPwd;
	private String mODPInitPwd;
	
	private LinearLayout pwdTips;
	private TextView pwdTip1, pwdTip2, pwdTip3;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setmState(STATE.INIT);
		
		Intent mIntent = getIntent();
		mODPInitPwd = "";
		if(mIntent != null)
		{
			type = mIntent.getStringExtra("ODP_PWD_TYPE");
			//nortek 密码为空
			//mODPInitPwd =  mIntent.getStringExtra("ODP_INIT_PWD");
		}
		
		
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
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(getString(R.string.nortek_door_pwd));
		txtTitle.setText(spanBuilder);
		
		setContentView(R.layout.doorphone_pnp_add_password_input);
		getWindow().setBackgroundDrawable(null);
		
		mTextView = (TextView)this.findViewById(R.id.textView1);
		mButton = (Button)this.findViewById(R.id.activie_password);
		mButton.setOnClickListener(this);
		mPassword = (EditText)this.findViewById(R.id.door_phone_pwd_input);
		pwdTips = (LinearLayout)this.findViewById(R.id.pwd_tips);
		pwdTip1 = (TextView)this.findViewById(R.id.pwd_tip_1);
		pwdTip2 = (TextView)this.findViewById(R.id.pwd_tip_2);
		pwdTip3 = (TextView)this.findViewById(R.id.pwd_tip_3);
		
		if(!TextUtils.isEmpty(type))
		{
			if(type.equals("INIT"))
			{
				if(mTextView != null){
					mTextView.setText(getString(R.string.odp_password_update_tip_1)
							+ " "
							+ DoorName.ODPName
							+ " "
							+ getString(R.string.odp_password_update_tip_2));
					mOldPwd = (EditText)this.findViewById(R.id.door_phone_old_pwd_input);
				}
			}else
			{
				if(pwdTips != null)
					pwdTips.setVisibility(View.GONE);
				this.findViewById(R.id.old_pwd_lin).setVisibility(View.GONE);
				mPassword.setHint(R.string.pnp_tip_11);
				TextView title = (TextView)this.findViewById(R.id.textView1);
				title.setText(R.string.nortek_door_pwd_set_1);
			}
		}
		
		
		smpAccount = LocalUserInfo.getInstance().getC2cAccount();
		smpAccountPwd = LocalUserInfo.getInstance().getC2cPassword();
		Log.d("Tecom", "DoorPhoneAPPasswordInput.... user:" + smpAccount + "  pwd:" + smpAccountPwd);
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
				case SEND_TIME_OUT:
					if(proDialog != null){
						proDialog.dismiss();
					}
					mHandler.removeMessages(SMP_SEND_ACC_TO_ODP);
					mHandler.removeCallbacksAndMessages(null);
					Toast.makeText(DoorPhoneAPPasswordInput.this, getString(R.string.operation_time_out), Toast.LENGTH_SHORT).show();
				case ACCOUNT_SWITCH_SUCCESS:
					if(getmState() == STATE.ODP_SEND_ACC)
					{
						mHandler.removeMessages(SEND_TIME_OUT);
						Intent mIntent = new Intent(DoorPhoneAPPasswordInput.this, WiFiSendSSID.class);   
						mIntent.putExtra("ODP_ACC", odpAccount);
						mIntent.putExtra("ODP_LOCAL_ACC", odpLocalAcc);
						mIntent.putExtra("ODP_LOCAL_PWD", odpLocalPwd);
						startActivity(mIntent);
						
						finish();
					}
					break;
				case ODP_AUTH_OK:
					if(getmState() == STATE.HAVE_AUTH)
					{						
						mHandler.sendEmptyMessage(SMP_SEND_ACC_TO_ODP);
					}
					
					break;
				case SMP_SEND_ACC_TO_ODP:
					sendAddSelfAccount();
					setmState(STATE.SMP_SEND_ACC);
					mHandler.sendEmptyMessageDelayed(SMP_SEND_ACC_TO_ODP, 2*1000);
					break;
				case CONFIG_FAILED_1:
					if(proDialog != null){
						proDialog.dismiss();
					}
					mPassword.setText("");
					Toast.makeText(DoorPhoneAPPasswordInput.this, getString(R.string.password_error), Toast.LENGTH_SHORT).show();
					break;
				case CONFIG_FAILED_2:
					if(proDialog != null){
						proDialog.dismiss();
					}
					Toast.makeText(DoorPhoneAPPasswordInput.this, getString(R.string.odp_error), Toast.LENGTH_SHORT).show();
					break;
				case CONFIG_FAILED_3:
					if(proDialog != null){
						proDialog.dismiss();
					}
					Toast.makeText(DoorPhoneAPPasswordInput.this, getString(R.string.password_update_error), Toast.LENGTH_SHORT).show();
					break;
				case CONFIG_FAILED_4:
					if(proDialog != null){
						proDialog.dismiss();
					}
					Toast.makeText(DoorPhoneAPPasswordInput.this, getString(R.string.odp_error), Toast.LENGTH_SHORT).show();
					break;
				case ODP_UPDATE_PWD_OK:
					/*
					if(proDialog != null){
						proDialog.dismiss();
					}*/
					Log.d("Tecom", "ODP_UPDATE_PWD_OK...更新密码成功，开始认证...");
					sendODPAuth(userInputPwd);
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
		
		if(mHandler != null)
		{
			mHandler.removeCallbacksAndMessages(null);
		}
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
			
			if(mOldPwd != null){
				userOldPwd = mOldPwd.getEditableText().toString();
				if(userOldPwd.length() < 8)
				{
					Toast.makeText(this, "At Least 8 Characters", Toast.LENGTH_SHORT).show();
					return;
				}else if(Utils.isDigit(userOldPwd))
				{
					Toast.makeText(this, "At Least One Letter", Toast.LENGTH_SHORT).show();
					return;
				}else if( !Utils.isContainOneUpper(userOldPwd))
				{
					Toast.makeText(this, "At Least One Upper Case Letter", Toast.LENGTH_SHORT).show();
					return;
				}else if( !Utils.isContainOneLower(userOldPwd))
				{
					Toast.makeText(this, "At Least One Lower Case Letter", Toast.LENGTH_SHORT).show();
					return;
				}else if(Utils.isCharacter(userOldPwd))
				{
					Toast.makeText(this, "At Least One Number", Toast.LENGTH_SHORT).show();
					return;
				}else
				if( !userOldPwd.equals(userInputPwd) ){
					Toast.makeText(this, R.string.password_mis_match, Toast.LENGTH_SHORT).show();
					return;
				}
			}
			
			showProcessDialog();
			if(type.equals("INIT"))
			{
				updateODPPwd(userInputPwd);
			}else
				sendODPAuth(userInputPwd);
			mHandler.sendEmptyMessageDelayed(SEND_TIME_OUT, TIME_OUT);
			
			Utils.ODP_System_Pwd = userInputPwd;
			break;
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
	private void sendAddSelfAccount() {
		// TODO Auto-generated method stub
		RequestMessageType sendOneMsg = new RequestMessageType();
		
		
		sendOneMsg.setType(MessageDataDefine.P2P);
		sendOneMsg.updateMessage((short) 0x0201, new String[]
				{	"My_login_account=" + smpAccount,
					"My_login_password=" + smpAccountPwd,
					"My_local_account=" + com.gocontrol.doorbell.utils.BuildConfig.Account_Local_Default,
					"My_local_password=" + com.gocontrol.doorbell.utils.BuildConfig.Password_Local_Default,
					"Time_Zone=" + Utils.getSMPCurrentTimeZone()
				}); //smp终端本地acoount , admin, pwd, 1234, timezone.
		
		MessageQueueManager.getInstance().addMessage(sendOneMsg);
		
	}
	/**
	 * 
	 */
	private void updateODPPwd(String pwd) {
		// TODO Auto-generated method stub
		Log.d("Tecom", "updateODPPwd..." + userOldPwd + "---" + pwd + "  mODPInitPwd:" + mODPInitPwd);
		RequestMessageType sendOneMsg = new RequestMessageType();
		sendOneMsg.setType(MessageDataDefine.P2P);
		sendOneMsg.updateMessage((short) 0x0305, new String[]
				{"ODP_OLD_system_password=" + mODPInitPwd,
				"ODP_NEW_system_password=" + pwd});
		
		MessageQueueManager.getInstance().addMessage(sendOneMsg);
	}

	public void onEvent(ReceivedODPEvent event){
				
		if(event.getMsg().getEventType() == MessageDataDefine.SMP_TO_ODP_AUTH_ACK )
		{
			System.out.printf("DoorPhoneAPPasswordInput... get SMP_TO_ODP_AUTH_ACK.");
			setmState(STATE.HAVE_AUTH);
			
			byte ret = event.getMsg().getPayloadByte();
			if(ret == 0x01)
			{
				System.out.printf("DoorPhoneAPPasswordInput... get SMP_TO_ODP_AUTH_ACK. 0x01, autho ok");
				mHandler.sendEmptyMessage(ODP_AUTH_OK);
			}else if(ret == 0x02)
			{
				System.out.printf("DoorPhoneAPPasswordInput... get SMP_TO_ODP_AUTH_ACK. 0x01, autho failed");
				setmState(STATE.INIT);
				mHandler.sendEmptyMessage(CONFIG_FAILED_1);				
				return;
			}else
			{
				System.out.printf("DoorPhoneAPPasswordInput... get SMP_TO_ODP_AUTH_ACK. 0x01, autho error code..");
				mHandler.sendEmptyMessage(CONFIG_FAILED_2);
				setmState(STATE.INIT);
			}		
		}else if(event.getMsg().getEventType() == MessageDataDefine.SMP_SET_ODP_SYSTEM_PSWD_ACK )
		{
			System.out.printf("DoorPhoneAPPasswordInput... get SMP_SET_ODP_SYSTEM_PSWD_ACK.");
			
			byte ret = event.getMsg().getPayloadByte();
			if(ret == 0x01)
			{
				System.out.printf("DoorPhoneAPPasswordInput... get SMP_SET_ODP_SYSTEM_PSWD_ACK. 0x01, autho ok");
				mHandler.sendEmptyMessage(ODP_UPDATE_PWD_OK);
			}else if(ret == 0x02)
			{
				mHandler.removeMessages(SEND_TIME_OUT);
				System.out.printf("DoorPhoneAPPasswordInput... get SMP_SET_ODP_SYSTEM_PSWD_ACK. 0x01, autho failed");
				setmState(STATE.INIT);
				mHandler.sendEmptyMessage(CONFIG_FAILED_3);	
				
				return;
			}else
			{
				mHandler.removeMessages(SEND_TIME_OUT);
				System.out.printf("DoorPhoneAPPasswordInput... get SMP_SET_ODP_SYSTEM_PSWD_ACK. 0x01, autho error code..");
				mHandler.sendEmptyMessage(CONFIG_FAILED_4);
				setmState(STATE.INIT);
			}
		}else if(event.getMsg().getEventType() == MessageDataDefine.SMP_ADD_ACCOUNT_SELF_ACK )
		{
			System.out.printf("DoorPhoneAPPasswordInput... get SMP_ADD_ACCOUNT_SELF_ACK.");
			String str[] = event.getMsg().getPayloadStr();
			for(String s : str)
			{
				System.out.print(s);
			}
			 odpAccount = Utils.getEqualString(str[1]);
			 odpLocalAcc = Utils.getEqualString(str[2]);
			 odpLocalPwd = Utils.getEqualString(str[3]);
			 setmState(STATE.ODP_SEND_ACC);
			 mHandler.removeMessages(SMP_SEND_ACC_TO_ODP);
			 mHandler.removeMessages(SEND_TIME_OUT);
			 mHandler.sendEmptyMessage(ACCOUNT_SWITCH_SUCCESS);	
			 if(ODPManager.getInstance().getOneODP(odpAccount) == null)
			 {
				 int index = getWhichDoorIndex();
				 String name = "";
				 if(DoorName.ODPName != null)
					 name = DoorName.ODPName;
				 Door.save(DoorPhoneAPPasswordInput.this, new Door(index, odpAccount,
							odpLocalAcc, odpLocalPwd, name));
				 Utils.addODPToList(index, odpAccount,
							odpLocalAcc, odpLocalPwd,name);
			 }
			 else
			 {
				 Log.d("tecom", "DoorPhoneAPPasswordInput, pnp for AP mode  OK!!! ==== but the odp has been in the ODP list ...");
			 }			 
		}
		
	}

	/**
	 * 
	 */
	private void sendODPAuth(String mODPSystemPwd) {
		// TODO Auto-generated method stub
		RequestMessageType sendOneMsg = new RequestMessageType();
		
		
		sendOneMsg.setType(MessageDataDefine.P2P);
		sendOneMsg.updateMessage((short) 0x0102, new String[]
				{"ODP_local_account=" + com.gocontrol.doorbell.utils.BuildConfig.Account_ODP_Local_Default,
				"ODP_local_password=" + com.gocontrol.doorbell.utils.BuildConfig.Password_ODP_Local_Default,
				"ODP_system_password=" + mODPSystemPwd});
		
		MessageQueueManager.getInstance().addMessage(sendOneMsg);
		
	}

	
	protected void showProcessDialog() {
		// TODO Auto-generated method stub
		proDialog = android.app.ProgressDialog.show(this,
				getString(R.string.tecom_process_pnp_title),
				getString(R.string.tecom_precess_content));
		proDialog.show();
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
