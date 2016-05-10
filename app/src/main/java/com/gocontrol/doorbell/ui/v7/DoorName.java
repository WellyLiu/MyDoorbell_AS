/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-3-23 ����2:12:34
 * Project: NortekDoorBell
 * PackageName: com.gocontrol.doorbell.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.ReceivedODPEvent;
import com.gocontrol.doorbell.message.DataConversion;
import com.gocontrol.doorbell.message.MessageDataDefine;
import com.gocontrol.doorbell.message.MessageQueueManager;
import com.gocontrol.doorbell.message.RequestMessageType;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author welly
 *
 */
public class DoorName extends PNPBaseActivity implements View.OnClickListener{

	private TextView front, back, office, custom;
	private String mODPInitPwd;
	private String type;
	private Context mContext;
	
	private Handler mHandler;
	private final int START_SENDING_NAME = 1000;
	private final int SENDING_NAME_OK = 1001;
	private final int SENDING_NAME_FAILED = 1002;
	private final int SENDING_TIME_OUT = 1003;
	private final int  TIME_OUT = 5*1000;
	private ProgressDialog proDialog;
	
	public static String ODPName;
	/* (non-Javadoc)
	 * @see com.gocontrol.doorbell.ui.v7.PNPBaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		
		Intent mIntent = getIntent();
		mODPInitPwd = "";
		if(mIntent != null)
		{
			type = mIntent.getStringExtra("ODP_PWD_TYPE");
			mODPInitPwd =  mIntent.getStringExtra("ODP_INIT_PWD");
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
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(getString(R.string.nortek_door_name));
		txtTitle.setText(spanBuilder);
		
		setContentView(R.layout.doorbell_name_set);
		getWindow().setBackgroundDrawable(null);
		getWindow().setBackgroundDrawable(null) ;
		
		front = (TextView)this.findViewById(R.id.front_door);
		front.setOnClickListener(this);
		back = (TextView)this.findViewById(R.id.back_door);
		back.setOnClickListener(this);
		office = (TextView)this.findViewById(R.id.office_door);
		office.setOnClickListener(this);
		custom = (TextView)this.findViewById(R.id.custom_name);
		custom.setOnClickListener(this);
		
		mHandler = new Handler(){

			/* (non-Javadoc)
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				switch( msg.what)
				{
				case START_SENDING_NAME:
					showProcessDialog();
					mHandler.sendEmptyMessageDelayed(SENDING_TIME_OUT, TIME_OUT);
					break;
				case SENDING_NAME_OK:
					
					if(proDialog != null){
						proDialog.dismiss();
					}
					
					Intent mIntent = new Intent(DoorName.this, DoorPhoneAPPasswordInput.class);
					mIntent.putExtra("ODP_PWD_TYPE", type);
					Log.d("Tecom", this.getClass().getSimpleName() + " mODPInitMacPwd:" + mODPInitPwd);
					mIntent.putExtra("ODP_INIT_PWD", mODPInitPwd);//��ֵ��Դ��ODP Mac��8λ��ȡСд��
					startActivity(mIntent);
					finish();
					
					break;
				case SENDING_NAME_FAILED:
					if(proDialog != null){
						proDialog.dismiss();
					}
					Toast.makeText(DoorName.this, getString(R.string.nortek_account_name_fail), Toast.LENGTH_SHORT).show();
					break;
					
				case SENDING_TIME_OUT:
					
					if(proDialog != null){
						proDialog.dismiss();
					}
					Toast.makeText(DoorName.this, getString(R.string.nortek_account_name_fail_2), Toast.LENGTH_SHORT).show();
					break;
					
					default:
						break;
				}
			}
			
		};
	}


	/* (non-Javadoc)
	 * @see com.gocontrol.doorbell.ui.v7.PNPBaseActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}


	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.front_door:
			sendODPSetName(getString(R.string.nortek_account_name_front));
			mHandler.sendEmptyMessage(START_SENDING_NAME);
			break;
		case R.id.back_door:
			sendODPSetName(getString(R.string.nortek_account_name_back));
			mHandler.sendEmptyMessage(START_SENDING_NAME);
			break;
		case R.id.office_door:
			sendODPSetName(getString(R.string.nortek_account_name_office));
			mHandler.sendEmptyMessage(START_SENDING_NAME);
			break;
		case R.id.custom_name:
			popupNameDialog();
			break;
		}
	}

	protected void showProcessDialog() {
		// TODO Auto-generated method stub
		proDialog = android.app.ProgressDialog.show(this,
				getString(R.string.nortek_operation_connecting),
				getString(R.string.nortek_operation_process));
		proDialog.show();
	}

	/**
	 * 
	 */
	private void popupNameDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(DoorName.this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(
				R.layout.dialog_one_eidit_name, null);

		TextView tv = (TextView) textEntryView.findViewById(R.id.edit_name);
		if (tv != null)
			tv.setText(R.string.nortek_account_name_custom_name_t);
		EditText password = (EditText) textEntryView.findViewById(R.id.edit);

		InputFilter[] filters = new InputFilter[2];
		filters[1] = new InputFilter.LengthFilter(16);
	    filters[0] = new InputFilter(){
	        @Override
	        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
	            if (end > start) {

	                char[] acceptedChars = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 
	                        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
	                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '@', '.', '_', '#', '$', '%', '&', '*', '-', '+', '(', ')', '!', '"', '\'', ':', 
	                        ';', '/', '?', ',', '~', '`', '|', '\\', '^', '<', '>', '{', '}', '[', ']', '=', '.','?','?'};

	                for (int index = start; index < end; index++) {                                         
	                    if (!new String(acceptedChars).contains(String.valueOf(source.charAt(index)))) { 
	                        return ""; 
	                    }               
	                }
	            }
	            return null;
	        }

	    };
	    password.setFilters(filters);
	    
	    
		builder.setTitle(R.string.nortek_account_name_custom_name);
		builder.setView(textEntryView);
		builder.setPositiveButton(this.getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				EditText password = (EditText) textEntryView
						.findViewById(R.id.edit);

				// ����˽��Э���ODP,����SSID��PWD��ODP
				String tmp = password.getEditableText().toString();
				tmp = tmp.trim();
				if( TextUtils.isEmpty(tmp))
				{
					Toast.makeText(mContext, R.string.nortek_name_not_null, Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(TextUtils.isEmpty(tmp))
					tmp = "";
				sendODPSetName(tmp);

				mHandler.sendEmptyMessage(START_SENDING_NAME);
			}
		});
		builder.setNegativeButton(this.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		builder.create().show();
	}


	public void onEvent(ReceivedODPEvent event){
		
		if(event.getMsg().getEventType() == MessageDataDefine.SMP_SET_ODP_NAME_ACK ){
			
			System.out.printf("DoorName... get SMP_SET_ODP_NAME_ACK.");

			mHandler.removeMessages(SENDING_TIME_OUT);
			byte ret = event.getMsg().getPayloadByte();
			
			if( ret == 0x01)
			{
				mHandler.sendEmptyMessage(SENDING_NAME_OK);
			}else if( ret == 0x02)
			{
				mHandler.sendEmptyMessage(SENDING_NAME_FAILED);
			}
		}
	}
	
	/**
	 * @param tmp
	 */
	protected void sendODPSetName(String name) {
		// TODO Auto-generated method stub
		
		ODPName = name;
		byte[] data = DataConversion.StringToUTF8Byte(name);
		
		RequestMessageType sendOneMsg = new RequestMessageType();
		sendOneMsg.setType(MessageDataDefine.P2P);
		sendOneMsg.updateMessageDatas((short) 0x0703, data);		
		
		MessageQueueManager.getInstance().addMessage(sendOneMsg);
	}

}
