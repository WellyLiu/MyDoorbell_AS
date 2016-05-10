/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-7 PM2:51:09
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import java.util.ArrayList;

import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.AccountStatusBean;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.bean.ReceivedC2CEvent;
import com.gocontrol.doorbell.message.MessageDataDefine;
import com.gocontrol.doorbell.message.MessageQueueManager;
import com.gocontrol.doorbell.message.MessageType;
import com.gocontrol.doorbell.message.ReceivedMessageType;
import com.gocontrol.doorbell.message.RequestMessageType;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.presenter.AccountListAdapter;
import com.gocontrol.doorbell.utils.Utils;
import com.ypy.eventbus.EventBus;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
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
public class AllAccountShow extends Activity implements View.OnClickListener{

	private ListView mAccountList;
	private ArrayList<AccountStatusBean> mAccountListInfo;
	private ImageView addAccount;
	private ProgressDialog proDialog;
	private Handler mHandler;
	private static final int ADD_ACCOUNT_TIMEOUT = 25*1000;
	private static final int MESSAGE_ADD_ACCOUNT_TIMEOUT = 0x0000;
	
	private final static int TIME_OUT_GET_ACC = 1000;
	private final static int TIME_OUT_REMOVE_ACC = 1001;
	private final static int GET_SMP_ACC_OK = 2000;
	private final static int GET_SMP_ACC_FAIL = 2001;
	private final static int REMOVE_SMP_OK = 2002;
	private final static int REMOVE_SMP_FAIL = 2003;
	private final static int CAN_NOT_DELETE_SELF = 3000;
	private final static int USER_INPUT_ODP_PWD = 4000;
	private final static int MESSAGE_ADD_ACCOUNT_OTHER_OK = 5000;
	private final static int UPDATE_ADD_IMAGE = 6000;
	
	AccountListAdapter adapter;
	private int odpId;
	private ODPInfo doorInfo;
	private String doorLocalAcc;
	private String doorLocalPwd;
	private String doorAcc;

	private Context mContext;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		
		odpId = getIntent().getIntExtra("door_id", 0);
		doorInfo = ODPManager.getInstance().getOneODP(odpId);
		
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		TextView txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		//if(doorInfo != null)
			//txtTitle.setText(doorInfo.getOdpName());
		//else
			txtTitle.setText(getString(R.string.all_account_status));
		ImageView img = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_back);
		img.setOnClickListener(this);
		
		mHandler = new Handler() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				System.out.println("Handler... process");
				switch (msg.what) {
				case MESSAGE_ADD_ACCOUNT_TIMEOUT:
					if(proDialog != null){
						proDialog.dismiss();
					}
					Toast.makeText(AllAccountShow.this, getString(R.string.add_account_failed), Toast.LENGTH_SHORT).show();
					break;
				
				case TIME_OUT_GET_ACC:
					if(proDialog != null){
						proDialog.dismiss();
					}
					Toast.makeText(AllAccountShow.this, getString(R.string.request_time_out), Toast.LENGTH_SHORT).show();
					break;
				case TIME_OUT_REMOVE_ACC:
					if(proDialog != null){
						proDialog.dismiss();
					}
					Toast.makeText(AllAccountShow.this, getString(R.string.operation_time_out), Toast.LENGTH_SHORT).show();
					break;
				case GET_SMP_ACC_OK:
					if(proDialog != null){
						proDialog.dismiss();
					}
					mHandler.removeMessages(TIME_OUT_GET_ACC);
					Toast.makeText(AllAccountShow.this, getString(R.string.request_ok), Toast.LENGTH_SHORT).show();
					break;
				case GET_SMP_ACC_FAIL:
					if(proDialog != null){
						proDialog.dismiss();
					}
					mHandler.removeMessages(TIME_OUT_GET_ACC);
					Toast.makeText(AllAccountShow.this, getString(R.string.request_fail), Toast.LENGTH_SHORT).show();
					break;
				case REMOVE_SMP_OK:
					if(proDialog != null){
						proDialog.dismiss();
					}
					mHandler.removeMessages(TIME_OUT_REMOVE_ACC);
					Toast.makeText(AllAccountShow.this, getString(R.string.remove_ok), Toast.LENGTH_SHORT).show();
					break;
				case REMOVE_SMP_FAIL:
					if(proDialog != null){
						proDialog.dismiss();
					}
					mHandler.removeMessages(TIME_OUT_REMOVE_ACC);
					Toast.makeText(AllAccountShow.this, getString(R.string.request_remove_fail), Toast.LENGTH_SHORT).show();
					break;
				
				case CAN_NOT_DELETE_SELF:
					Toast.makeText(AllAccountShow.this, getString(R.string.can_not_delete_self), Toast.LENGTH_SHORT).show();
					break;
				
				case USER_INPUT_ODP_PWD:
					popupInputODPPwd();
					break;
				case MESSAGE_ADD_ACCOUNT_OTHER_OK:
					mHandler.removeMessages(MESSAGE_ADD_ACCOUNT_TIMEOUT);
					Toast.makeText(AllAccountShow.this, getString(R.string.add_account_ok), Toast.LENGTH_SHORT).show();
				case UPDATE_ADD_IMAGE:
					if( (adapter != null && adapter.getCount() >= 4)
							||(mAccountListInfo != null && mAccountListInfo.size() >= 4))
					{
						if(addAccount != null)
							addAccount.setVisibility(View.INVISIBLE);
					}else
					{
						if(addAccount != null)
							addAccount.setVisibility(View.VISIBLE);
					}
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}

		};
		EventBus.getDefault().register(this);
		setContentView(R.layout.all_account_show);
		getWindow().setBackgroundDrawable(null) ;
		
		mAccountListInfo = new ArrayList<AccountStatusBean>();
		
		mAccountList = (ListView)this.findViewById(R.id.account_list);
		adapter = new AccountListAdapter(AllAccountShow.this, mAccountListInfo);
		mAccountList.setAdapter(adapter);
		
		mAccountList.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				////如果删除的是自己的账号，则不允许删除
				if(mAccountListInfo.get(position).getShowEmail().equalsIgnoreCase(LocalUserInfo.getInstance().getC2cAccount()))
				{
					//无需提示
					//mHandler.sendEmptyMessage(CAN_NOT_DELETE_SELF);
					return;
				}
				popupRemoveDialog(position, AllAccountShow.this.getString(R.string.account_remove), AllAccountShow.this.getString(R.string.door_phone_pwd));
			}
			
		});
		
		addAccount = (ImageView)this.findViewById(R.id.account_add);
		addAccount.setOnClickListener(this);
		
		doorAcc = doorInfo.getOdpAccount();
		doorLocalAcc = doorInfo.getOdpLocalAccount();
		doorLocalPwd = doorInfo.getOdpLocalPwd();
		
		///此为弹出密码校验，再查询账号列表
		//mHandler.sendEmptyMessage(USER_INPUT_ODP_PWD);
		//此为直接查询，不需输入密码
		Utils.sendODPRequestSMPAccountStatus(doorAcc, doorLocalAcc, doorLocalPwd );
		if (proDialog == null)
			proDialog = android.app.ProgressDialog.show(this,
					getString(R.string.nortek_operation_connecting),
					getString(R.string.nortek_operation_process));
		else
			proDialog.show();
		proDialog.setCancelable(true);
		mHandler.sendEmptyMessageDelayed(TIME_OUT_GET_ACC, 15 * 1000);
	}

	protected void popupInputODPPwd()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.dialog_one_eidit, null);
		TextView userName = (TextView) textEntryView
				.findViewById(R.id.edit_name);
		userName.setVisibility(View.GONE);
		builder.setTitle(R.string.odp_input_pwd );
		builder.setView(textEntryView);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			
				////////////////////
				EditText password = (EditText) textEntryView
						.findViewById(R.id.edit);
				String pwd = password.getEditableText().toString();
				Utils.ODP_System_Pwd = pwd;
				Log.d("tecom", "ODP system pwd. user input pwd:" + pwd);
				//处理逻辑
				Utils.sendODPRequestSMPAccountStatus(doorAcc, doorLocalAcc, doorLocalPwd );
				
				if (proDialog == null)
					proDialog = android.app.ProgressDialog.show(mContext,
							getString(R.string.operation_process),
							getString(R.string.tecom_precess_content));
				else
					proDialog.show();
				proDialog.setCancelable(true);
				mHandler.sendEmptyMessageDelayed(TIME_OUT_GET_ACC, 15 * 1000);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
			}
		});
		builder.create().show();
	}
	
	/**
	 * 
	 */
	protected void popupRemoveDialog(final int position, String title, final String editName) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				LayoutInflater factory = LayoutInflater.from(this);
				final View textEntryView = factory.inflate(R.layout.dialog_one_eidit, null);
				TextView userName = (TextView) textEntryView
						.findViewById(R.id.edit_name);
				userName.setText(editName);
				builder.setTitle(title);
				builder.setView(textEntryView);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						
						////////////////////
						EditText password = (EditText) textEntryView
								.findViewById(R.id.edit);
						String pwd = password.getEditableText().toString();
						Log.d("tecom", "remove one smp account. user input pwd:" + pwd);
						//�����߼�
						Utils.sendODPRemoveOneSMPAccount(doorAcc, doorLocalAcc, doorLocalPwd, mAccountListInfo.get(position).geteMail(), pwd );			
						if (proDialog == null)
							proDialog = android.app.ProgressDialog.show(mContext,
									getString(R.string.operation_process),
									getString(R.string.tecom_precess_content));
						else
							proDialog.show();
						proDialog.setCancelable(true);
						mHandler.sendEmptyMessageDelayed(TIME_OUT_GET_ACC, 15 * 1000);
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						
					}
				});
				builder.create().show();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_back:
			finish();
			break;
		case R.id.account_add:
			popupAddDialog("Add New Account", "New Account Email", getString(R.string.pnp_tip_11));
			break;
		default:
			break;
		}
	}

	
	private String otherSMPAccount = "";
	/**
	 * 
	 */
	private void popupAddDialog(String title, final String oneNameStr,
			final String towNameStr) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.dialog_three_edit, null);
		textEntryView.findViewById(R.id.lin_three).setVisibility(View.GONE);
		TextView emailName = (TextView) textEntryView
				.findViewById(R.id.name_one);
		
		EditText t = (EditText) (textEntryView.findViewById(R.id.edit_one));
		t.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT);
		
		emailName.setText( oneNameStr );
		TextView pwdName = (TextView) textEntryView
				.findViewById(R.id.name_two);
		pwdName.setText(towNameStr);
		
		builder.setTitle(title);
		builder.setView(textEntryView);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
		
				EditText email_edit = (EditText) textEntryView
						.findViewById(R.id.edit_one);
				
				EditText pwd_edit = (EditText) textEntryView
						.findViewById(R.id.edit_two);
				
				
				String account = email_edit.getText().toString();
				account = account.toLowerCase();
				String password = pwd_edit.getText().toString();
				
				//store the other smp account.
				otherSMPAccount = account;
				
				if(!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)){
				
					RequestMessageType sendOneMsg = new RequestMessageType();
					
					String doorAcc = doorInfo.getOdpAccount();
					String doorLocalAcc = doorInfo.getOdpLocalAccount();
					String doorLocalPwd = doorInfo.getOdpLocalPwd();
					if(password.equalsIgnoreCase("doorLocalPwd"))
					{
						Toast.makeText(mContext, R.string.password_mis_match, Toast.LENGTH_LONG).show();
						return;
					}
					account = AppUtils.processWebLoginAccount(account, getString(R.string.def_reg_domain));
					MessageType.C2CAccountInfo peerInfo = new MessageType.C2CAccountInfo();
					peerInfo.peerId = doorAcc;
					peerInfo.loginAccount = doorLocalAcc;
					peerInfo.loginPassword = doorLocalPwd;
					sendOneMsg.setType(MessageDataDefine.C2C);
					sendOneMsg.setPeerAccountInfo(peerInfo);
					sendOneMsg.updateMessage(MessageDataDefine.SMP_ADD_ACCOUNT_OTHER, new String[]
							{"Other_login_account=" + account,
							"Other_login_password=1234", //���贫smp login�����ODP
							"Other_local_account=" + com.gocontrol.doorbell.utils.BuildConfig.Account_Local_Default,
							"Other_local_password=" + com.gocontrol.doorbell.utils.BuildConfig.Password_Local_Default,
							"ODP_system_password=" + password });
					
					///////////////////////////////////////////////////////////////////////////////////////

					MessageQueueManager.getInstance().addMessage(sendOneMsg);
					proDialog = android.app.ProgressDialog.show(AllAccountShow.this, getString(R.string.tecom_process_add_title),
							getString(R.string.tecom_precess_content));
					proDialog.setCancelable(true);
					mHandler.sendEmptyMessageDelayed(MESSAGE_ADD_ACCOUNT_TIMEOUT, ADD_ACCOUNT_TIMEOUT);
				}else{
					Toast.makeText(AllAccountShow.this, getString(R.string.add_account_failed) + ":" +  getString(R.string.add_account_failed_reason_input_error) , Toast.LENGTH_SHORT).show();
				}
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
			}
		});
		builder.create().show();
	}
	
	
	public void onEvent(ReceivedC2CEvent event){
		ReceivedMessageType msg = event.getMsg();
		
		if(msg!= null &&  msg.getEventType() == MessageDataDefine.SMP_ADD_ACCOUNT_OTHER_ACK)
		{
			
			String str[] = msg.getPayloadStr();
			for(String s : str)
			{
				System.out.print(s);
			}
			 String result = getEqualString(str[0]);
			 if(result.equals("1")){//success
				 String odpAccount = getEqualString(str[1]);
				 String odpLocalAcc = getEqualString(str[2]);
				 String odpLocalPwd = getEqualString(str[3]);
				 String otherLoginAccount = getEqualString(str[4]);
				 
				 RequestMessageType sendOneMsg = new RequestMessageType();
				/*	
				if(!otherSMPAccount.equals(otherLoginAccount))	
				{
					Log.d("Tecom", "other smp account mismatch. " + otherSMPAccount + " -- " + otherLoginAccount);
					return;
				}*/
				
				String hostSMPAccount = LocalUserInfo.getInstance().getC2cAccount();
				String srv = getString(R.string.def_reg_srv);
				hostSMPAccount =  AppUtils.processWebLoginAccount(hostSMPAccount , srv);
				String hostSMPLocalAccount = LocalUserInfo.getInstance().getLocalAccount();
				String hostSMPLocalPwd = LocalUserInfo.getInstance().getLocalPwd();
				
				//���͸�������SMP
				MessageType.C2CAccountInfo peerInfo = new MessageType.C2CAccountInfo();
				peerInfo.peerId = AppUtils.processWebLoginAccount(otherSMPAccount, srv);
				Log.d("Tecom", "sendd 0x0205, other peer id:" + peerInfo.peerId);
				peerInfo.loginAccount = com.gocontrol.doorbell.utils.BuildConfig.Account_Local_Default;
				peerInfo.loginPassword =  com.gocontrol.doorbell.utils.BuildConfig.Password_Local_Default;
				sendOneMsg.setType(MessageDataDefine.C2C);
				sendOneMsg.setPeerAccountInfo(peerInfo);
				sendOneMsg.updateMessage(MessageDataDefine.SMP_TO_SMP_ADD_ACCOUNT_ACK, new String[]
						{"ODP_login_account=" + odpAccount,
						"ODP_local_account=" + odpLocalAcc,
						"ODP_local_password=" + odpLocalPwd,
						"MIDSMP_login_account=" + hostSMPAccount,
						"MIDSMP_local_account=" + hostSMPLocalAccount,
						"MIDSMP_local_password=" + hostSMPLocalPwd});
				
				MessageQueueManager.getInstance().addMessage(sendOneMsg);
			 }else{
				 if(proDialog != null){
					 proDialog.dismiss();
				 }
				 mHandler.removeMessages(MESSAGE_ADD_ACCOUNT_TIMEOUT);
				 mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(AllAccountShow.this, getString(R.string.add_account_failed), Toast.LENGTH_SHORT).show();
					}
				});
			 }
		}else if(msg!= null &&  msg.getEventType() == MessageDataDefine.SMP_TO_SMP_ADD_ACCOUNT_ACK_ACK)
		{
			
			String str[] = msg.getPayloadStr();
			for(String s : str)
			{
				System.out.print(s);
			}
			/*
			RequestMessageType sendOneMsg = new RequestMessageType();
		
			String doorAcc = doorInfo.getOdpAccount();
			String doorLocalAcc = doorInfo.getOdpLocalAccount();
			String doorLocalPwd = doorInfo.getOdpLocalPwd();
			
			MessageType.C2CAccountInfo peerInfo = new MessageType.C2CAccountInfo();
			peerInfo.peerId = doorAcc;
			peerInfo.loginAccount = doorLocalAcc;
			peerInfo.loginPassword = doorLocalPwd;
			sendOneMsg.setType(MessageDataDefine.C2C);
			sendOneMsg.setPeerAccountInfo(peerInfo);
			sendOneMsg.updateMessage(MessageDataDefine.SMP_TO_ODP_ADD_OTHER_ACCOUNT_ACK_ACK, new String[]
					{"ADD_other_result=" + str[0],"Other_login_account=" + str[1]});
			
			MessageQueueManager.getInstance().addMessage(sendOneMsg);
			
			*/
			
			 if(proDialog != null){
				 proDialog.dismiss();
			 }
			 mHandler.removeMessages(MESSAGE_ADD_ACCOUNT_TIMEOUT);
			 mHandler.sendEmptyMessage(MESSAGE_ADD_ACCOUNT_OTHER_OK);
			 
			 AccountStatusBean one = new  AccountStatusBean();
			 	String srv = getString(R.string.def_reg_srv);
			 	one.setShowEmail(otherSMPAccount);
				String showAcc = AppUtils.processWebLoginAccount(otherSMPAccount, srv);
				one.seteMail(showAcc);
				one.setStatus(1);
				
				mAccountListInfo.add(one);	 
				mAccountList.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						adapter.notifyDataSetChanged();
					}
				});
				
				//update "Add account" image.
				mHandler.sendEmptyMessage(UPDATE_ADD_IMAGE);
		}else
			 //SMP_GET_ODP_SMP_ACCOUNT_ACK (0x0402)
			if(msg!= null &&  msg.getEventType() == MessageDataDefine.SMP_GET_ODP_SMP_ACCOUNT_ACK)
			{
				String str[] = event.getMsg().getPayloadStr();
				for(String s : str)
				{
					System.out.print(s);
					System.out.println();
				}
				String ret = getEqualString(str[0]);
				if(!ret.equalsIgnoreCase("1"))
				{
					mHandler.sendEmptyMessage(GET_SMP_ACC_FAIL);
					Log.d("Tecom", "Request all smp account failed......");
					return;
				}
				int smpNum = (str.length - 1)/2;
				mHandler.sendEmptyMessage(GET_SMP_ACC_OK);
				if(smpNum < 0)
				{
					Log.d("Tecom", "Request all smp account number < 0......");
					return;
				}
				processAllSmpAccount(smpNum, str);
				//update "Add account" image.
				mHandler.sendEmptyMessage(UPDATE_ADD_IMAGE);
			}
			else
				//SMP_REMOVE_ODP_SMP_ACCOUNT_ACK (0404)
				if(msg!= null &&  msg.getEventType() == MessageDataDefine.SMP_REMOVE_ODP_SMP_ACCOUNT_ACK)
				{
					String str[] = event.getMsg().getPayloadStr();
					for(String s : str)
					{
						System.out.print(s);
						System.out.println();
					}
					
					String ret = getEqualString(str[0]);
					if(!ret.equalsIgnoreCase("1"))
					{
						mHandler.sendEmptyMessage(REMOVE_SMP_FAIL);
						Log.d("Tecom", "Remove one smp account failed......" + ret);
						return;
					}
					int smpNum = (str.length - 1)/2;
					mHandler.sendEmptyMessage(REMOVE_SMP_OK);
					if(smpNum < 0)
					{
						Log.d("Tecom", "Remove one smp account,now all smp account number < 0......");
						return;
					}
					processAllSmpAccount(smpNum, str);
					//update "Add account" image.
					mHandler.sendEmptyMessage(UPDATE_ADD_IMAGE);
				}
	}
	/**
	 * 
	 */
	private void processAllSmpAccount(int smpNum, String str[]) {
		// TODO Auto-generated method stub
		mAccountListInfo.clear();
		for( int i=0 ; i<smpNum; i++)
		{
			String acc = getEqualString(str[1 + i*2]);
			String status = getEqualString(str[2 + i*2]);
			AccountStatusBean one = new  AccountStatusBean();
			one.seteMail(acc);
			one.setShowEmail(Utils.processOrignalLoginAccount(acc));
			
			one.setStatus(Integer.parseInt(status));			
			
			mAccountListInfo.add(one);
		}
		mAccountList.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				adapter.notifyDataSetChanged();
			}
		});
	}

	private String getEqualString(String string) {
		// TODO Auto-generated method stub
		String[] strs = string.split("=");
		return strs[1];
	}
}
