/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-1 AM9:56:23
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.bean.ReceivedODPEvent;
import com.gocontrol.doorbell.message.MessageDataDefine;
import com.gocontrol.doorbell.message.MessageQueueManager;
import com.gocontrol.doorbell.message.RequestMessageType;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Administrator
 *
 */
public class WiFiChoose extends Activity implements View.OnClickListener{

	private enum STATE{
		INIT, //̬
		FOUND_ODP, //
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
	
	
	protected static final int SEND_SEARCH_ODP = 1000;
	private static final int CONFIG_FAILED_1 = 2001;
	private static final int CONFIG_FAILED_2 = 2002;
	
	private static final int ODP_INIT_MODE = 3000;
	private static final int ODP_CONFIG_MODE = 3001;	
	
	private LinearLayout mChooseWiFI;
	private TextView mNext, mChooseWiFi;
	private TextView mNetworkState;
	
	private String smpAccount, smpAccountPwd;
	
	private String odpType;
	private Handler mHandler;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setmState(STATE.INIT);
		
		mODPInitMacPwd = "";
		
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		
		TextView txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(getString(R.string.door_setup));
		txtTitle.setText(spanBuilder);
		
		ImageView img = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_back);
		img.setOnClickListener(this);	
		
		setContentView(R.layout.wifi_choose);
		getWindow().setBackgroundDrawable(null);
		
		mChooseWiFI = (LinearLayout)this.findViewById(R.id.choose_wifi);
		mChooseWiFI.setOnClickListener(this);
		
		mNext = (TextView)this.findViewById(R.id.next);
		mNext.setOnClickListener(this);
		mChooseWiFi = (TextView)this.findViewById(R.id.choose_wifi_manual);
		mChooseWiFi.setOnClickListener(this);
		
		mNetworkState = (TextView)this.findViewById(R.id.textView3);
		
		EventBus.getDefault().register(this);
		
		//String[] params = AppUtils.readC2CLoginParams(this);
		//smpAccount = params[0];
		smpAccount = LocalUserInfo.getInstance().getC2cAccount();
		smpAccount = AppUtils.processWebLoginAccount(smpAccount, getString(R.string.def_reg_domain));
		//smpAccountPwd = params[1];
		smpAccountPwd = LocalUserInfo.getInstance().getC2cPassword();
		Log.d("Tecom", "WiFiChoose.... user:" + smpAccount + "  pwd:" + smpAccountPwd);
		
		mHandler = new Handler()
		{

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				switch(msg.what)
				{
				case SEND_SEARCH_ODP:
					if(getmState() == STATE.INIT)
						sendSearch();
					break;
				case CONFIG_FAILED_1 :
					Toast.makeText(WiFiChoose.this, getString(R.string.odp_smp_full), Toast.LENGTH_SHORT).show();
					//odp smp ful.
					break;
				case CONFIG_FAILED_2 :

					//Toast.makeText(WiFiChoose.this, getString(R.string.odp_has_configured), Toast.LENGTH_SHORT).show();
					break;
				
				case ODP_CONFIG_MODE:
					odpType = "CONFIG";
					mNext.setBackgroundResource(R.drawable.button_shape_style_pnp_1);
					mNext.setEnabled(true);
					mNext.setClickable(true);
					if(mNetworkState != null)
						mNetworkState.setText(R.string.tecom_odp_connected);
					break;
				case ODP_INIT_MODE:
					odpType = "INIT";
					mNext.setBackgroundResource(R.drawable.button_shape_style_pnp_1);
					mNext.setEnabled(true);
					mNext.setClickable(true);
					if(mNetworkState != null)
						mNetworkState.setText(R.string.tecom_odp_connected);
					break;
				
				default:
						break;
				}
			}
			
		};
	}

	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		mHandler.sendEmptyMessage(SEND_SEARCH_ODP);
		//re-try
		mHandler.sendEmptyMessageDelayed(SEND_SEARCH_ODP, 3000);
		//sendSearch();
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



	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		setmState(STATE.INIT);
		
		EventBus.getDefault().unregister(this);
		
		mHandler.removeMessages(SEND_SEARCH_ODP);
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.choose_wifi:
			Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");   
			startActivity(wifiSettingsIntent);   
			break;
		case R.id.choose_wifi_manual:
			Intent mWifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");   
			startActivity(mWifiSettingsIntent);   
			break;
		case R.id.next:
			if(odpType.equalsIgnoreCase("INIT"))
			{
				Intent intent = new Intent(WiFiChoose.this, DoorName.class);
				intent.putExtra("ODP_PWD_TYPE", odpType);
				Log.d("Tecom", this.getClass().getSimpleName() + " mODPInitMacPwd:" + mODPInitMacPwd);
				intent.putExtra("ODP_INIT_PWD", mODPInitMacPwd);//
				startActivity(intent);
			}else{
				Intent mIntent = new Intent(WiFiChoose.this, DoorPhoneAPPasswordInput.class);
				mIntent.putExtra("ODP_PWD_TYPE", odpType);
				Log.d("Tecom", this.getClass().getSimpleName() + " mODPInitMacPwd:" + mODPInitMacPwd);
				mIntent.putExtra("ODP_INIT_PWD", mODPInitMacPwd);//
				startActivity(mIntent);
			}
			mHandler.removeMessages(SEND_SEARCH_ODP);
			finish();
			break;
		case R.id.btn_back:
			mHandler.removeMessages(SEND_SEARCH_ODP);
			finish();
			break;
		default:
			break;
		}
	}

	private String mODPInitMacPwd;
	public void onEvent(ReceivedODPEvent event){
		
		if( event.getMsg().getEventType() == MessageDataDefine.SMP_SEARCH_ODP_IP_ACK )
		{
			System.out.printf("WiFiChoose... get SMP_SEARCH_ODP_IP_ACK.");
			//if the odp acc is in the smp account list, ignore this ACK.
			String tmp[] = event.getMsg().getPayloadStr();
			String mODPLoginAcc = Utils.getEqualString(tmp[3]);
			if( Utils.isInODPAccountList(mODPLoginAcc))
				/*
			{
				Log.d(this.getClass().getSimpleName(), "The ODP Acc from ACK is " + mODPLoginAcc + "... is in ODP list, so ignore it...");
				return;
			}*/
			
			setmState(STATE.FOUND_ODP);
			mHandler.removeMessages(SEND_SEARCH_ODP);
			
			mODPInitMacPwd = event.getMsg().getServerMac();
			Log.d("tst", " mODPInitMacPwd :" + mODPInitMacPwd);
			mODPInitMacPwd = Utils.getInitPwdFromMac(mODPInitMacPwd);
			Log.d("tst", " mODPInitMacPwd :" + mODPInitMacPwd);
			String str[] = event.getMsg().getPayloadStr();
			String mUnit_val = Utils.getEqualString(str[0]);
			String mWifi_mode = Utils.getEqualString(str[1]);			
			String mODP_system_password_mode = Utils.getEqualString(str[2]);
			Log.d("Tecom", "mUnit_val:" + mUnit_val);
			Log.d("Tecom", "mWifi_mode:" + mWifi_mode);
			Log.d("Tecom", "mODP_system_password_mode:" + mODP_system_password_mode);
			if( mUnit_val.equals("2"))
			{
				setmState(STATE.INIT);
				mHandler.sendEmptyMessage(CONFIG_FAILED_1);
				Log.d("Tecom", "WiFiChoose Private Protocal:" + "SMP_SEARCH_ODP_IP_ACK" + " 2 " + "the smp is full...");
				return;
			}
			if(mWifi_mode.equals("2"))
			{
				setmState(STATE.INIT);
				mHandler.sendEmptyMessage(CONFIG_FAILED_2);
				Log.d("Tecom", "WiFiChoose Private Protocal:" + "SMP_SEARCH_ODP_IP_ACK" + " 1 " + "the odp Client mode...");
				return;
			}
			if(mODP_system_password_mode.equals("1"))
			{
				mHandler.sendEmptyMessage(ODP_INIT_MODE);
				Log.d("Tecom", "WiFiChoose Private Protocal:" + "SMP_SEARCH_ODP_IP_ACK" + " the odp password initial mode...");
				
			}else if(mODP_system_password_mode.equals("2"))
			{
				mHandler.sendEmptyMessage(ODP_CONFIG_MODE);
				Log.d("Tecom", "WiFiChoose Private Protocal:" + "SMP_SEARCH_ODP_IP_ACK" + " 1 " + "the odp password not initial mode...");
			}else
			{
				Log.d("Tecom", "WiFiChoose Private Protocal:" + "SMP_SEARCH_ODP_IP_ACK" + " 1 " + "the odp password error mode...");
				setmState(STATE.INIT);
				return;
			}
			
		}
	}
	
}
