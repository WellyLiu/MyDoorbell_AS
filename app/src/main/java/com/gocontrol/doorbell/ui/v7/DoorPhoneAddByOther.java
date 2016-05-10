/**
 * Author: welly
 * Email: wliu@TAGtech.com
 * Data: 2016-1-7 AM9:24:15
 * Project: TecomDoor
 * PackageName: com.TAG.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.iptnet.c2c.C2CHandle;
import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.Door;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.PNPSuccessEvent;
import com.gocontrol.doorbell.bean.ReceivedC2CEvent;
import com.gocontrol.doorbell.message.MessageDataDefine;
import com.gocontrol.doorbell.message.MessageQueueManager;
import com.gocontrol.doorbell.message.MessageType;
import com.gocontrol.doorbell.message.ReceivedMessageType;
import com.gocontrol.doorbell.message.RequestMessageType;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.utils.BuildConfig;
import com.gocontrol.doorbell.utils.Utils;
import com.ypy.eventbus.EventBus;

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Administrator
 * 
 */
public class DoorPhoneAddByOther extends PNPBaseActivity implements
		View.OnClickListener {

	private final static String TAG = "DoorPhoneAddByOther.class";
	
	protected static final int OTHER_PNP_SUC = 1000;
	private Handler mHandler;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		ImageView img = (ImageView) getActionBar().getCustomView()
				.findViewById(R.id.btn_back);
		img.setOnClickListener(this);

		TextView txtTitle = (TextView) getActionBar().getCustomView()
				.findViewById(android.R.id.title);		
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(
				getString(R.string.door_setup));
	
		txtTitle.setText(spanBuilder);

		setContentView(R.layout.doorphone_add_by_other);
		getWindow().setBackgroundDrawable(null);
		
		mHandler = new Handler(){

			/* (non-Javadoc)
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				switch (msg.what)
				{
				case OTHER_PNP_SUC:
					Toast.makeText(DoorPhoneAddByOther.this, DoorPhoneAddByOther.this.getString(R.string.pnp_successful), Toast.LENGTH_SHORT).show();
					
					System.out.printf("被第三方账号配对成功");
					Intent mIntent = new Intent(DoorPhoneAddByOther.this, DoorPhoneList.class);
					
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

	public void onEvent(ReceivedC2CEvent event) {

		ReceivedMessageType msg = event.getMsg();
		if (msg != null
				&& msg.getEventType() == MessageDataDefine.SMP_TO_SMP_ADD_ACCOUNT_ACK) {

			String str[] = msg.getPayloadStr();
			for (String s : str) {
				System.out.print(s);
			}
			String hostSMPAccount = Utils.getEqualString(str[3]);
			String hostSMPLocalAccount = Utils.getEqualString(str[4]);
			String hostSMPLocalPwd = Utils.getEqualString(str[5]);
			RequestMessageType sendOneMsg = new RequestMessageType();

			MessageType.C2CAccountInfo peerInfo = new MessageType.C2CAccountInfo();
			peerInfo.peerId = hostSMPAccount;
			peerInfo.loginAccount = com.gocontrol.doorbell.utils.BuildConfig.Account_Local_Default;
			peerInfo.loginPassword = com.gocontrol.doorbell.utils.BuildConfig.Password_Local_Default;
			peerInfo.peerId = hostSMPAccount;
			
			Log.d("TAG", "other smp acc:"  + peerInfo.peerId);
			
			sendOneMsg.setType(MessageDataDefine.C2C);
			sendOneMsg.setPeerAccountInfo(peerInfo);
			
			sendOneMsg.updateMessage(
					MessageDataDefine.SMP_TO_SMP_ADD_ACCOUNT_ACK_ACK,
					new String[] { "ADD_other_result=" + 1,
							"ODP_login_account=" + Utils.getEqualString(str[0])});

			MessageQueueManager.getInstance().addMessage(sendOneMsg);
			
			String odpAccount = Utils.getEqualString(str[0]);
			String odpLocalAcc = Utils.getEqualString(str[1]);
			String odpLocalPwd = Utils.getEqualString(str[2]);
			
			 //注册odp到gcm
			registerODP(odpAccount, odpLocalAcc, odpLocalPwd);
			 
			if(ODPManager.getInstance().getOneODP(odpAccount) == null)
			{
				Log.d("tecom", " added by the third party  OK!!!");
				 int index = getWhichDoorIndex();
				 Door.save(DoorPhoneAddByOther.this, new Door(index, odpAccount,
							odpLocalAcc, odpLocalPwd,""));
				 Utils.addODPToList(index, odpAccount,
							odpLocalAcc, odpLocalPwd,"");
			}
			else
			{
				Log.d("tecom", " added by the third party  OK!!! ==== but the odp has been in the ODP list ...");
			}
			mHandler.sendEmptyMessage(OTHER_PNP_SUC);
		}
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
	
	private void registerODP(String odpAccount, String odpLocalAcc,
			String odpLocalPwd) {
		// TODO Auto-generated method stub
		// get gcm token
		String token;
		if ((token = AppUtils.readGcmToken(this)).isEmpty()) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(DoorPhoneAddByOther.this, "Could not to get GCM token", Toast.LENGTH_SHORT).show();
			}});
			
			return ;
		}
		
		// setting notification
		Log.d(TAG, "============1==================");
		Log.d(TAG, token);
		Log.d(TAG, odpAccount);
		int mLineId = C2CHandle.getInstance().setNotification(token, odpAccount, 8);
		Log.d(TAG, "line id:" + mLineId);
		if (mLineId < 0) {
			Log.d("TAG", "============2==================");
			mLineId = Integer.MIN_VALUE;
			runOnUiThread(new Runnable() {
				public void run() {
					Log.d(TAG, "============3==================");
					//Toast.makeText(WiFiSendSSID.this, "Setting notification (" + mLineId + ")", Toast.LENGTH_SHORT).show();
			}});
			
		}
		else{
			Log.d(TAG, "odp register GCM successfully...");
		}
		Log.d(TAG, "============4==================");
	}

}
