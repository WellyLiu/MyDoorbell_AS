/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-7 上午10:05:04
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.ReceivedC2CEvent;
import com.gocontrol.doorbell.message.MessageDataDefine;
import com.gocontrol.doorbell.message.ReceivedMessageType;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.utils.Utils;
import com.ypy.eventbus.EventBus;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Administrator
 *
 */
public class UserAboutDeviceDoor extends PNPBaseActivity implements View.OnClickListener{

	private int odpId;
	private ODPInfo doorInfo;

	private Handler mHandler;
	
	
	private final static int CHECK_RESULT_FAILED = 1000;
	private final static int CHECK_RESULT_SAME = 1001;
	private final static int CHECK_RESULT_HAS_NEW_VERSION = 1002;
	private final static int CHECK_VERSION_TIME_OUT = 2000;
	private final static int GET_VERSION_TIME_OUT = 2001;
	private final static int REQUEST_OK = 3000;
	
	private final static int ODP_UPDATE_COMMAND_OK = 4001;	
	private final static int ODP_UPDATE_BUSY = 4002;
	private final static int ODP_UPDATE_DOAWNLOAD_FAIL = 4003;
	private final static int ODP_UPDATE_START = 4004;
	private final static int ODP_UPDATE_FAIL = 4005;
	private final static int ODP_UPDATE_SUCCESS = 4006;
	private final static int UPDATE_VERSION_TIME_OUT = 4007;
	
	private final static int UPDATE_ODP_VERSION_TEXT = 5000;
	
	private final static  int REQUEST_SYS_LOG_OK = 6000;
	private final static int UPDATE_ODP_SYS_LOG_CK = 6001;
	
	private Button mCheckVersion;
	private TextView odpVersion, odpIPText, odpConSSID;
	private ProgressDialog proDialog;
	
	private CheckBox mCheckBox;
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
		ImageView img = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_back);
		img.setOnClickListener(this);
		
		TextView txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		
		if(doorInfo != null)
			txtTitle.setText(doorInfo.getOdpName());
		else
			txtTitle.setText(R.string.about_device_title);
		
		setContentView(R.layout.setting_about_device_door);
		getWindow().setBackgroundDrawable(null) ;
		
		odpVersion = (TextView)this.findViewById(R.id.odp_version);
		odpIPText = (TextView)this.findViewById(R.id.odp_ip);
		odpConSSID = (TextView)this.findViewById(R.id.odp_ssid);
		
		mCheckVersion = (Button)this.findViewById(R.id.button1);
		mCheckVersion.setOnClickListener(this);
		
		mCheckBox = (CheckBox)this.findViewById(R.id.sys_loge_ck);
		
		mCheckBox.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(mCheckBox.isChecked())
				{
					Utils.sendSetODPSysLog(doorInfo.getOdpAccount(),
							doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd(),
							sysLogServerIP, sysLogServerPort, "1");
				}else
				{
					Utils.sendSetODPSysLog(doorInfo.getOdpAccount(),
							doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd(),
							sysLogServerIP, sysLogServerPort, "0");
				}
			}
			
		});
		
		
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
				case ODP_UPDATE_SUCCESS:
					popupTipDialog();
					break;
					
				case CHECK_RESULT_FAILED:
					Toast.makeText(mContext, getString(R.string.request_fail), Toast.LENGTH_SHORT).show();
					break;

				case CHECK_RESULT_SAME:
					//Toast.makeText(mContext, getString(R.string.request_same_result), Toast.LENGTH_SHORT).show();
					popupTipDialog();
					break;

				case CHECK_RESULT_HAS_NEW_VERSION:
					mCheckVersion.setText(R.string.nortek_update_firmware);
					//pop up dialog
					popUpUpdateDialog();
					
					break;
				
				case CHECK_VERSION_TIME_OUT:
					if(proDialog != null)
						proDialog.dismiss();
					Toast.makeText(mContext, getString(R.string.request_time_out), Toast.LENGTH_SHORT).show();
					break;
				case GET_VERSION_TIME_OUT:
					if(proDialog != null)
						proDialog.dismiss();
					
					Toast.makeText(mContext, getString(R.string.request_time_out), Toast.LENGTH_SHORT).show();
					break;
				case REQUEST_OK:
					mHandler.removeMessages(CHECK_VERSION_TIME_OUT);
					mHandler.removeMessages(GET_VERSION_TIME_OUT);
					mHandler.removeMessages(UPDATE_VERSION_TIME_OUT);
					
					if(proDialog != null)
						proDialog.dismiss();
					break;
				
				case UPDATE_VERSION_TIME_OUT:
					if(proDialog != null)
						proDialog.dismiss();
					Toast.makeText(mContext, getString(R.string.operation_time_out), Toast.LENGTH_SHORT).show();
				case ODP_UPDATE_COMMAND_OK:
					Toast.makeText(mContext, getString(R.string.SEND_UPDATE_COMMAND_OK), Toast.LENGTH_LONG).show();
					break;
				case ODP_UPDATE_BUSY:
					Toast.makeText(mContext, getString(R.string.SEND_UPDATE_COMMAND_BUSY), Toast.LENGTH_LONG).show();
					break;
				case ODP_UPDATE_FAIL:
					Toast.makeText(mContext, getString(R.string.ODP_UPDATE_FAIL), Toast.LENGTH_SHORT).show();
					break;
				case UPDATE_ODP_VERSION_TEXT:
					odpVersion.setText(version);
					odpIPText.setText(odpIP);
					odpConSSID.setText(mSSID);
					if( !TextUtils.isEmpty(updateVersion) && updateVersion.equalsIgnoreCase("2"))
					{
						mCheckVersion.setText(R.string.nortek_update_firmware);
					}
					break;
				case UPDATE_ODP_SYS_LOG_CK:
					if(sysLogStatus.equalsIgnoreCase("1"))
					{
						mCheckBox.setChecked(true);
					}else
					{
						mCheckBox.setChecked(false);
					}
					break;
				default:
						break;
				}
			}
			
		};
		
		// SMP_GET_ODP_VERSION (0x0601)
		Utils.sendRequestODPVersion(doorInfo.getOdpAccount(),
				doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd());
		
		// SMP_GET_ODP_VERSION (0x0601)
		Utils.sendRequestODPSysLog(doorInfo.getOdpAccount(),
				doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd());
				
	
		if (proDialog == null)
			proDialog = android.app.ProgressDialog.show(this,
					getString(R.string.nortek_operation_connecting),
					getString(R.string.nortek_operation_process));
		else
			proDialog.show();
		proDialog.setCancelable(true);
		proDialog.setOnCancelListener(new OnCancelListener()
		{

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				if(mHandler != null)
				{
					mHandler.removeMessages(GET_VERSION_TIME_OUT);
				}
			}
			
		});
		mHandler.sendEmptyMessageDelayed(GET_VERSION_TIME_OUT, 15 * 1000);

	}

	/**
	 * 
	 */
	protected void popupTipDialog() {
		// TODO Auto-generated method stub
		
	
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);		
		
		builder.setTitle(mContext.getString(R.string.update_version_message_1));
		builder.setMessage(R.string.update_version_message_2);
		builder.setCancelable(true);
		builder.setPositiveButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
			
			}
		});
		
		builder.create().show();
	}

	/**
	 * update odp firmware.
	 */
	protected void popUpUpdateDialog() {
		// TODO Auto-generated method stub
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);		
		
		builder.setTitle(mContext.getString(R.string.check_new_firmware));
		builder.setMessage(R.string.update_version_message);
		builder.setCancelable(true);
		builder.setPositiveButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
				// 发送私有协议给ODP
				Utils.sendUpdateODP(
						doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd());
				if(proDialog == null)
					proDialog = android.app.ProgressDialog.show(mContext, getString(R.string.version_check_title),
						getString(R.string.tecom_precess_content));
				else
					proDialog.show();
				proDialog.setCancelable(true);
				//mHandler.sendEmptyMessageDelayed(UPDATE_VERSION_TIME_OUT, 15*1000);
				//Toast.makeText(mContext, mContext.getString(R.string.nortek_has_call_odp_update), Toast.LENGTH_SHORT).show();
			}
		});
		builder.setNegativeButton(this.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		builder.create().show();

	
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
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
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		mHandler.removeCallbacksAndMessages(null);
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
		case R.id.button1:
			Button tmp = (Button)v;
			String str = (String) tmp.getText();
			if(str != null && str.equalsIgnoreCase(getString(R.string.check_firmware_version)))
			{
				Utils.sendRequestCheckODPVersion(
						doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd());
				if(proDialog == null)
					proDialog = android.app.ProgressDialog.show(this, getString(R.string.version_check_title),
						getString(R.string.tecom_precess_content));
				else
					proDialog.show();
				proDialog.setCancelable(true);
				mHandler.sendEmptyMessageDelayed(CHECK_VERSION_TIME_OUT, 15 * 1000);
			}else
				if(str != null && str.equalsIgnoreCase(getString(R.string.nortek_update_firmware)))
				{
					popUpUpdateDialog();
				}
			
			break;
		default:
			break;
		}
	}

	private String version, mSSID;
	private String updateVersion;//0: check fail  1：the same   2：has new ODP software
	private String odpIP;
	private String sysLogStatus;
	private String sysLogServerIP;
	private String sysLogServerPort;
	public void onEvent(ReceivedC2CEvent event){
		
		ReceivedMessageType msg = event.getMsg();
		//SMP_GET_ODP_VERSION_ACK (0x0602)
		if(msg!= null &&  msg.getEventType() == MessageDataDefine.SMP_GET_ODP_VERSION_ACK)
		{
			mHandler.sendEmptyMessage(REQUEST_OK);
			
			odpIP = event.getMsg().getServerIP();
			Log.d("Tecom", "odpIP:" + odpIP);
			String str[] = event.getMsg().getPayloadStr();
			if( str == null)
			{
				Log.d("Tecom", "get odp version null.... return");
				return ;
			}
			for(String s : str)
			{
				System.out.print(s);
			}
			
			version = Utils.getEqualString(str[0]);
			updateVersion = Utils.getEqualString(str[1]);
			mSSID = Utils.getEqualString(str[2]);
			mHandler.sendEmptyMessage(UPDATE_ODP_VERSION_TEXT);
		}else 
			//SMP_TO_ODP_VERSION_CHECK_ACK (0x0604)
			if(msg!= null &&  msg.getEventType() == MessageDataDefine.SMP_TO_ODP_VERSION_CHECK_ACK)
			{
				mHandler.sendEmptyMessage(REQUEST_OK);
				
				byte ret = event.getMsg().getPayloadByte();
				
				switch(ret)
				{
				//fail
				case 0x00:
					mHandler.sendEmptyMessage(CHECK_RESULT_FAILED);
					break;
					//the same
				case 0x01:
					mHandler.sendEmptyMessage(CHECK_RESULT_SAME);
					break;
					//has new
				case 0x02:
					
					mHandler.sendEmptyMessage(CHECK_RESULT_HAS_NEW_VERSION);
					break;
				default:
					break;
				}
				
			}
			else//SMP_TO_ODP_UPDATE_VERSION_ACK (0x0606)
				if(msg!= null &&  msg.getEventType() == MessageDataDefine.SMP_TO_ODP_UPDATE_VERSION_ACK)
				{
					mHandler.sendEmptyMessage(REQUEST_OK);
					
					byte ret = event.getMsg().getPayloadByte();
					
					switch(ret)
					{	
						//odp update ok
					case 0x01:
						mHandler.sendEmptyMessage(ODP_UPDATE_COMMAND_OK);
						break;
						//odp busy
					case 0x02:
						mHandler.sendEmptyMessage(ODP_UPDATE_BUSY);
						break;
					case 0x03:
						mHandler.sendEmptyMessage(ODP_UPDATE_DOAWNLOAD_FAIL);
						break;
					case 0x04:
						mHandler.sendEmptyMessage(ODP_UPDATE_START);
						break;
					case 0x05:
						mHandler.sendEmptyMessage(ODP_UPDATE_FAIL);
						break;
					case 0x06:
						mHandler.sendEmptyMessage(ODP_UPDATE_SUCCESS);
						break;
					default:
						break;
					}
				}
				else if(msg != null && msg.getEventType() == MessageDataDefine.SMP_GET_ODP_SYSLOG_ACK) //SMP_GET_ODP_SYSLOG_ACK
				{
					mHandler.sendEmptyMessage(REQUEST_SYS_LOG_OK);
					
					odpIP = event.getMsg().getServerIP();
					Log.d("Tecom", "Sys log, odpIP:" + odpIP);
					String str[] = event.getMsg().getPayloadStr();
					if( str == null)
					{
						Log.d("Tecom", "get odp sys log null.... return");
						return ;
					}
					for(String s : str)
					{
						System.out.print(s);
					}
					
					sysLogServerPort = Utils.getEqualString(str[2]);
					sysLogServerIP = Utils.getEqualString(str[1]);
					sysLogStatus = Utils.getEqualString(str[0]);
					mHandler.sendEmptyMessage(UPDATE_ODP_SYS_LOG_CK);
				}
	}
	
}