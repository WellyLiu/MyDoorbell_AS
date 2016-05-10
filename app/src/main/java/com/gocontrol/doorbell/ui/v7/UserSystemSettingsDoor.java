/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-7 ����10:04:43
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.gocontrol.doorbell.AppApplication;
import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.Door;
import com.gocontrol.doorbell.QuietTimeHoursActivity;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.DoorPhoneListCloseDrawerEvent;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.bean.ODPFeature;
import com.gocontrol.doorbell.bean.ReceivedC2CEvent;
import com.gocontrol.doorbell.datasource.DataHelper;
import com.gocontrol.doorbell.message.DataConversion;
import com.gocontrol.doorbell.message.MessageDataDefine;
import com.gocontrol.doorbell.message.MessageQueueManager;
import com.gocontrol.doorbell.message.MessageType;
import com.gocontrol.doorbell.message.ReceivedMessageType;
import com.gocontrol.doorbell.message.RequestMessageType;
import com.gocontrol.doorbell.model.ODPFeatureManager;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.ui.TimeZoneAdapter;
import com.gocontrol.doorbell.utils.BuildConfig;
import com.gocontrol.doorbell.utils.Utils;
import com.ypy.eventbus.EventBus;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Administrator
 *
 */
public class UserSystemSettingsDoor extends PNPBaseActivity implements View.OnClickListener{

	enum DIALOG_ITEM{
		SPEAKER,
		MICRO,
		RESOLUTION,
		FRAME,
		PIR
	}
	
	private final static int TIME_OUT_GET_ODP_FEATURE = 1000;
	private final static int GET_ODP_FEATURE_OK = 1001;
	
	private final static int SET_ODP_FEATURE_OK = 2000;
	private final static int SET_ODP_FEATURE_FAIL = 2001;
	private final static int TIME_OUT_SET_ODP_FEATURE = 2002;
	private final static int UPDATE_UI_FROM_ODP_DATA = 3000;
	private final static int SEND_ODP_PWD_TIME_OUT = 4000;
	private final static int SEND_ODP_PWD_OK = 4001;
	private final static int SEND_ODP_PWD_FAILED = 4002;
	
	protected static final int ODP_UPDATE_NAME = 10000;
	private Context mContext;
	private LinearLayout mRemove, mRename, mPassword, mQuietHours, mSpeakerVol, mMicoVol,
	mResolution, mFrameRate, mColorMode, mTimeZone;
	private TextView mSpeakerVolShow, mMicoVolShow, mResolutionShow, mFrameRateShow, mPIR;
	private TextView mRenameShow;	
	private LinearLayout mMotionDetection, MPIR;
	
	private int odpId;
	private ODPInfo doorInfo;
	
	private Handler mHandler;
	private ProgressDialog proDialog;
	private ODPFeature feature;
	protected int index;
	
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
		
		feature = ODPFeatureManager.getInstance()
				.getODPFeature();
		
		
		Log.d("tecom", "UserSystemSettingsDoor... title:" + doorInfo.getOdpName() + "  odpIndex:" + odpId);
		
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
		ColorStateList redColors = ColorStateList.valueOf(getResources().getColor(R.color.btn_hangup_bg_color));
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(/*"Cloud 2 Door"*/doorInfo.getOdpName() + " Settings");
		txtTitle.setText(spanBuilder);
		
		setContentView(R.layout.system_setting_door);
		getWindow().setBackgroundDrawable(null) ;
		
		mRemove = ( LinearLayout )this.findViewById(R.id.door_remove);
		mRename = ( LinearLayout )this.findViewById(R.id.door_rename);
		mPassword = ( LinearLayout )this.findViewById(R.id.door_password);
		mQuietHours = ( LinearLayout )this.findViewById(R.id.quiet_hours);	
		mQuietHours.setOnClickListener(this);
		mRemove.setOnClickListener(this);
		mRename.setOnClickListener(this);
		mPassword.setOnClickListener(this);
		mTimeZone = (LinearLayout )this.findViewById(R.id.time_zone);
		mTimeZone.setOnClickListener(this);
		
		mSpeakerVol = (LinearLayout)this.findViewById(R.id.speaker_volume);
		mSpeakerVol.setOnClickListener(this);
		mMicoVol = (LinearLayout)this.findViewById(R.id.mic_volume);
		mMicoVol.setOnClickListener(this);
		mResolution = (LinearLayout)this.findViewById(R.id.function_1);
		mResolution.setOnClickListener(this);
		mFrameRate = (LinearLayout)this.findViewById(R.id.function_2);
		mFrameRate.setOnClickListener(this);
		mColorMode = (LinearLayout)this.findViewById(R.id.color_mode_show);
		mColorMode.setOnClickListener(this);
		mMotionDetection = (LinearLayout)this.findViewById(R.id.function_4);
		mMotionDetection.setOnClickListener(this);
		MPIR = (LinearLayout)this.findViewById(R.id.function_3);
		MPIR.setOnClickListener(this);
		
		mRenameShow = (TextView)this.findViewById(R.id.rename_1_show);
		mRenameShow.setText(doorInfo.getOdpName());
		
		mSpeakerVolShow = (TextView)this.findViewById(R.id.speaker_volume_show);
		mMicoVolShow = (TextView)this.findViewById(R.id.mic_volume_show);
		mResolutionShow = (TextView)this.findViewById(R.id.function_1_show);
		mFrameRateShow = (TextView)this.findViewById(R.id.function_2_show);
		mPIR = (TextView)this.findViewById(R.id.function_3_show);	
		
		mHandler = new Handler()
		{

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				switch(msg.what)
				{
				case ODP_UPDATE_NAME:
					
					updateTitle();
					break;
					
				case GET_ODP_FEATURE_OK:
					mHandler.removeMessages(TIME_OUT_GET_ODP_FEATURE);
					if (proDialog != null)
						proDialog.dismiss();
					Toast.makeText(mContext, getString(R.string.request_ok), Toast.LENGTH_SHORT).show();
					
					updateUIFeature();
					break;
				case SET_ODP_FEATURE_OK :
					mHandler.removeMessages(TIME_OUT_SET_ODP_FEATURE);
					if (proDialog != null)
						proDialog.dismiss();
					//Toast.makeText(mContext, getString(R.string.request_ok), Toast.LENGTH_SHORT).show();
					break;
				case SET_ODP_FEATURE_FAIL :
					mHandler.removeMessages(TIME_OUT_SET_ODP_FEATURE);
					if (proDialog != null)
						proDialog.dismiss();
					Toast.makeText(mContext, getString(R.string.request_fail), Toast.LENGTH_SHORT).show();
					break;
				
				case TIME_OUT_SET_ODP_FEATURE:
					if (proDialog != null)
						proDialog.dismiss();
					Toast.makeText(mContext, getString(R.string.request_time_out), Toast.LENGTH_SHORT).show();
					break;
					
				case TIME_OUT_GET_ODP_FEATURE:
					if (proDialog != null)
						proDialog.dismiss();
					Toast.makeText(mContext, getString(R.string.request_time_out), Toast.LENGTH_SHORT).show();
					break;
				
				case UPDATE_UI_FROM_ODP_DATA:
					
					updateUIFromODPData(feature);
					break;
				
				case SEND_ODP_PWD_TIME_OUT:
					if (proDialog != null)
						proDialog.dismiss();
					Toast.makeText(mContext, getString(R.string.request_time_out), Toast.LENGTH_SHORT).show();
					break;
				case SEND_ODP_PWD_OK:
					if (proDialog != null)
						proDialog.dismiss();
					mHandler.removeMessages(SEND_ODP_PWD_TIME_OUT);
					Toast.makeText(mContext, getString(R.string.password_update_ok), Toast.LENGTH_SHORT).show();
					break;
				case SEND_ODP_PWD_FAILED:
					if (proDialog != null)
						proDialog.dismiss();
					mHandler.removeMessages(SEND_ODP_PWD_TIME_OUT);
					Toast.makeText(mContext, getString(R.string.password_update_fail), Toast.LENGTH_SHORT).show();
					break;
					
				default:
						break;
				}
			}
			
		};
		
		Utils.sendODPRequestFeature(doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd());
		
		if (proDialog == null)
			proDialog = android.app.ProgressDialog.show(this,
					getString(R.string.nortek_operation_connecting),
					getString(R.string.nortek_operation_process));
		else
			proDialog.show();
			
		proDialog.setCancelable(true);
		
		mHandler.sendEmptyMessageDelayed(TIME_OUT_GET_ODP_FEATURE, 15 * 1000);
	}

	/**
	 * @param feature2
	 */
	protected void updateUIFromODPData(ODPFeature feature) {
		// TODO Auto-generated method stub
		if(feature == null)
			return;
		
		mSpeakerVolShow.setText(String.valueOf(feature.getmSpeakerVol()));
		mMicoVolShow.setText(String.valueOf(feature.getmMicroVol()));
		mResolutionShow.setText(getResolution(Integer.parseInt(feature.getmResolution())));
		mFrameRateShow.setText(getFrameRate(feature.getmFrameRate()));
			
		mPIR.setText(DialogItems[3][feature.getPIR()]);
		
	}

	/**
	 * @param getmFrameRate
	 * @return
	 */
	private CharSequence getFrameRate(int getmFrameRate) {
		// TODO Auto-generated method stub
		String []items = new String[]{"Low(1 FPS)", "Fair(5 FPS)", "Good(10 FPS)", "Better(15 FPS)", "Best(30 FPS)"};
		if( getmFrameRate > 7 || getmFrameRate < 0)
		{
			Log.i("Tecom", "getFrameRate index  error...");
			return items[ 0 ]; //default
		}else
		{
			
			if( getmFrameRate == 0)
			{
				return items[ 0 ];
			}else if( getmFrameRate == 1 || getmFrameRate == 2)
			{
				return items[ 1 ];
			}else if( getmFrameRate == 3)
			{
				return items[ 2 ];
			}else if( getmFrameRate == 4 || getmFrameRate == 5)
			{
				return items[ 3 ];
			}else if(  getmFrameRate == 6 || getmFrameRate == 7)
			{
				return items[ 4 ];
			}else 
				return items[ 0 ]; //default
			
		}
	}

	private String getResolution(int i)
	{
		
		if( i > 3 || i < 1)
		{
			Log.i("Tecom", "getResolution index  error...");
			return "";
		}else
		{
			String [] items = new String[]{"Good(360x240)", "Better(720x480)", "Best(1280x720)"};
			return items[ i - 1];
		}
	}
	
	
	/**
	 * 
	 */
	protected void updateUIFeature() {
		// TODO Auto-generated method stub
		//xxxxxxxxxxx
		
	}

	protected void updateTitle() {
		// TODO Auto-generated method stub
		TextView txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(doorInfo.getOdpName());
	
		txtTitle.setText(spanBuilder);
		if(mRenameShow != null)
		{
			mRenameShow.setText(spanBuilder);
		}
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
		if(proDialog != null)
			proDialog.dismiss();
		
		super.onDestroy();
		
	}

	private int value_temp, value_temp2;
	
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
		case R.id.time_zone:
			String tmp = feature.getTimeZone();
			int i = Utils.getWhichTimeZone(tmp);
			timeZoneDialog(i);
			break;
		case R.id.door_remove:
			dialog();
			//popupRemoveDialog(getString(R.string.door_remove) + doorInfo.getOdpName(), getString(R.string.door_remove_tips) );
			break;
		case R.id.door_rename:
			//startActivity(new Intent(this, DoorPhoneRename.class));
			popupRenameDialog("Rename " + doorInfo.getOdpName(), 
					getString(R.string.door_name) );
			break;
		case R.id.door_password:
			popupUpdatePwdDialog();
			break;
		case R.id.quiet_hours:
			startActivity(new Intent(this, QuietTimeHoursActivity.class).putExtra("door_id", odpId));
			break;
			
		case R.id.speaker_volume:
			// showChooseDialog(DIALOG_ITEM.SPEAKER);
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dialog_seekbar,
					(ViewGroup) findViewById(R.id.dialog_root));
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setView(layout);
			builder.setTitle(R.string.speaker_volume);
			builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					feature.setmSpeakerVol(value_temp);
					mSpeakerVolShow.setText(String.valueOf(value_temp));
					Utils.sendODPSetFeature(doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd(), feature);
				}
				}
				);
			builder.setNegativeButton(mContext.getString(R.string.cancel), null);
			
			AlertDialog alertDialog = builder.create();					
			SeekBar sb = (SeekBar) layout.findViewById(R.id.dialog_seekbar);
			sb.setProgress(feature.getmSpeakerVol());
			final TextView mVol = (TextView)layout.findViewById(R.id.val_value);
			mVol.setText(String.valueOf(feature.getmSpeakerVol()));
			sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {			
				

				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// Do something here with new value
					 if(progress == 0)
						 progress = 1;
					 value_temp = progress;  
			            
					 mVol.setText(String.valueOf(value_temp));
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
					
				}
			});
			value_temp = sb.getProgress();
			alertDialog.show();
			break;
		case R.id.mic_volume:
			//showChooseDialog(DIALOG_ITEM.MICRO);
			LayoutInflater inflater2 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout2 = inflater2.inflate(R.layout.dialog_seekbar,
					(ViewGroup) findViewById(R.id.dialog_root));
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this)
					.setView(layout2);
			builder2.setTitle(R.string.mic_volume);
			builder2.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					mMicoVolShow.setText(String.valueOf(value_temp2));
					feature.setmMicroVol(value_temp2);
					Utils.sendODPSetFeature(doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd(), feature);
				}
				}
				);
			builder2.setNegativeButton(mContext.getString(R.string.cancel), null);		
			
			AlertDialog alertDialog2 = builder2.create();					
			SeekBar sb2 = (SeekBar) layout2.findViewById(R.id.dialog_seekbar);
			sb2.setProgress(feature.getmMicroVol());
			final TextView mVol2 = (TextView)layout2.findViewById(R.id.val_value);
			mVol2.setText(String.valueOf(feature.getmMicroVol()));
			sb2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				
				private int value_temp;

				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// Do something here with new value
					 if(progress == 0)
						 progress = 1;
					 value_temp2 = progress;  
			            
					 mVol2.setText(String.valueOf(value_temp2));
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}
			});
			value_temp2 = sb2.getProgress();
			alertDialog2.show();
			
			break;
		case R.id.function_1:
			showChooseDialog(DIALOG_ITEM.RESOLUTION);
			break;
		case R.id.function_2:
			showChooseDialog(DIALOG_ITEM.FRAME);
			break;
		case R.id.color_mode_show:
			startActivity(new Intent(this, UserSystemSettingDoorColorMode.class).putExtra("door_id", odpId));
			break;
		case R.id.function_4:
			startActivity(new Intent(this, UserSystemSettingDoorMotionDetect.class).putExtra("door_id", odpId));
			break;
		case R.id.function_3:
			
			showChooseDialog(DIALOG_ITEM.PIR);
			break;
		default:
			break;
		}
	}
	
	
	private AlertDialog dialog;
	/**
	 * 
	 */
	private void timeZoneDialog(int chooseOne) {
		// TODO Auto-generated method stub
	

		
		ListAdapter timeZoneListAdapter;
		timeZoneListAdapter =  new TimeZoneAdapter(mContext, BuildConfig.timeZoneMsg, BuildConfig.timeZoneData, chooseOne);
		Builder builder = new AlertDialog.Builder(mContext);

		builder.setTitle("Time Zone Settings").setSingleChoiceItems(timeZoneListAdapter, chooseOne,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

						// TODO Auto-generated method stub
						doItemChoose(arg1);
					}
				});

		dialog = builder.create();
		dialog.show();
	}

	/**
	 * @param arg1
	 */
	protected void doItemChoose(int arg1) {
		// TODO Auto-generated method stub
		if(dialog != null)
		{
			dialog.dismiss();
			String timeZone = Utils.getTimeZoneStr(arg1);
			feature.setTimeZone(timeZone);
			Log.d("tst", "send time zone:" + timeZone);
			Utils.sendODPSetFeature(doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd(), feature);
		}
	}

	/**
	 * 
	 */
	private void updateODPPwd(String oldPwd , String newPwd) {
		// TODO Auto-generated method stub
		RequestMessageType sendOneMsg = new RequestMessageType();
		
		MessageType.C2CAccountInfo peerInfo = new MessageType.C2CAccountInfo();
		peerInfo.peerId = doorInfo.getOdpAccount();
		peerInfo.loginAccount = doorInfo.getOdpLocalAccount();
		peerInfo.loginPassword = doorInfo.getOdpLocalPwd();
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.setPeerAccountInfo(peerInfo);

		sendOneMsg.updateMessage((short) 0x0305, new String[]
				{"ODP_OLD_system_password=" + oldPwd,
				"ODP_NEW_system_password=" + newPwd});
		
		MessageQueueManager.getInstance().addMessage(sendOneMsg);
	}
	
	/**
	 * 
	 */
	private void popupUpdatePwdDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.dialog_update_pwd, null);

		TextView nameOne = (TextView) textEntryView
				.findViewById(R.id.name_one);
		nameOne.setText(R.string.odp_old_pwd);
		TextView nameTwo = (TextView) textEntryView
				.findViewById(R.id.name_two);
		nameTwo.setText(R.string.odp_new_pwd);
		TextView threeName = (TextView) textEntryView
				.findViewById(R.id.name_three);
		threeName.setText(R.string.door_confirm_pwd);
		
		EditText one = (EditText) textEntryView
				.findViewById(R.id.edit_one);
		one.setFilters(new InputFilter[]{new InputFilter.LengthFilter(63)});
		EditText two = (EditText) textEntryView
				.findViewById(R.id.edit_two);
		two.setFilters(new InputFilter[]{new InputFilter.LengthFilter(63)});
		EditText three = (EditText) textEntryView
				.findViewById(R.id.edit_three);
		three.setFilters(new InputFilter[]{new InputFilter.LengthFilter(63)});
		
		builder.setTitle(R.string.odp_pwd_update);
		builder.setView(textEntryView);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				
				EditText pwdOne = (EditText) textEntryView
						.findViewById(R.id.edit_one);
				EditText pwdTwo = (EditText) textEntryView
						.findViewById(R.id.edit_two);
				EditText pwd_three = (EditText) textEntryView
						.findViewById(R.id.edit_three);
				
				// check parameter
				String oldPwd = pwdOne.getText().toString();
				String newPwd = pwdTwo.getText().toString();
				String confirmPwd = pwd_three.getText().toString();
				
				if (oldPwd.isEmpty()) {
					Toast.makeText(mContext, mContext.getString(R.string.password_1), Toast.LENGTH_SHORT).show();
					return ;
				}
								
				if (newPwd.isEmpty()) {
					Toast.makeText(mContext, mContext.getString(R.string.password_3), Toast.LENGTH_SHORT).show();
					return ;
				}
				if (confirmPwd.isEmpty()) {
					Toast.makeText(mContext, mContext.getString(R.string.password_4), Toast.LENGTH_SHORT).show();
					return;
				}				
				if(newPwd.length() < 8)
				{
					Toast.makeText(mContext, "At Least 8 Characters", Toast.LENGTH_SHORT).show();
					return;
				}else if(Utils.isDigit(newPwd))
				{
					Toast.makeText(mContext, "At Least One Letter", Toast.LENGTH_SHORT).show();
					return;
				}else if( !Utils.isContainOneUpper(newPwd))
				{
					Toast.makeText(mContext, "At Least One Upper Case Letter", Toast.LENGTH_SHORT).show();
					return;
				}else if( !Utils.isContainOneLower(newPwd))
				{
					Toast.makeText(mContext, "At Least One Lower Case Letter", Toast.LENGTH_SHORT).show();
					return;
				}else if(Utils.isCharacter(newPwd))
				{
					Toast.makeText(mContext, "At Least One Number", Toast.LENGTH_SHORT).show();
					return;
				}
				if( !newPwd.equalsIgnoreCase(confirmPwd) )
				{
					Toast.makeText(mContext, mContext.getString(R.string.password_5) , Toast.LENGTH_SHORT).show();
					return;
				}
				
				//�����߼�
				updateODPPwd(pwdOne.getEditableText().toString(), pwdTwo.getEditableText().toString());
				mHandler.sendEmptyMessageDelayed(SEND_ODP_PWD_TIME_OUT, 15000);
				if (proDialog == null)
					proDialog = android.app.ProgressDialog.show(mContext,
							getString(R.string.operation_process),
							getString(R.string.tecom_precess_content));
				else
					proDialog.show();
					
				proDialog.setCancelable(true);
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
			}
		});
		builder.create().show();
	}

	private final static String DialogItems[][] = new String[][]{
		new String[]{"1", "2" , "3" , "4", "5", "6" , "7" , "8", "9"},
		new String[]{"Good(360x240)", "Better(720x480)", "Best(1280x720)"},		
		new String[]{"Low(1 FPS)", "Fair(5 FPS)", "Good(10 FPS)" , "Better(15 FPS)" , "Best(30 FPS)"},
		new String[]{AppApplication.getInstance().getString(R.string.pir_1),
				AppApplication.getInstance().getString(R.string.pir_2),
				AppApplication.getInstance().getString(R.string.pir_3),
				AppApplication.getInstance().getString(R.string.pir_4)}
	};
	
	/**
	 * @param speaker
	 */
	private void showChooseDialog(final DIALOG_ITEM whichOne) {
		// TODO Auto-generated method stub
		String[] items = null;
		String title = "";
		
		switch(whichOne)
		{
		case PIR:
			items = DialogItems[3];
			title = mContext.getString(R.string.pir_title);
			break;
		case SPEAKER:
			//items = new String[]{"1", "2" , "3" , "4", "5", "6" , "7" , "8", "9"};
			items = DialogItems[0];
			title = mContext.getString(R.string.speaker_volume);
			break;
		case MICRO:
			//items = new String[]{"1", "2" , "3" , "4", "5", "6" , "7" , "8", "9"};
			items = DialogItems[0];
			title = mContext.getString(R.string.mic_volume);
			break;
		case RESOLUTION:
			//items = new String[]{"Good(360x240)", "Better(720x480)", "Best(1280x720)"};
			items = DialogItems[1];
			title = mContext.getString(R.string.function_1);
			break;
		case FRAME:
			//items = new String[]{"Low", "Good" , "Better" , "Best""};
			items = DialogItems[2];
			title = mContext.getString(R.string.function_2);
			break;
		default:
			break;
		}
		new AlertDialog.Builder(this)
				.setTitle(title)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setSingleChoiceItems(items, index=getShowWhichIndex(whichOne),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								Log.d("tst", "======which:" + which);
								index = which;
							}
						}).setNegativeButton(mContext.getString(R.string.cancel), null)
						.setPositiveButton(mContext.getString(R.string.ok), new OnClickListener()
						{

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								switch(whichOne)
								{
								case PIR:
									///////////
									//����feature ���� PIR������
									///////////
									feature.setPIR(index);
									mPIR.setText(DialogItems[3][index]);
									break;
								case SPEAKER:
									//items = new String[]{"1", "2" , "3" , "4", "5", "6" , "7" , "8", "9"};
									feature.setmSpeakerVol(index+1);
									Log.d("tst", "====== 2 index:" + index);
									mSpeakerVolShow.setText(String.valueOf(index+1));
									break;
								case MICRO:
									//items = new String[]{"1", "2" , "3" , "4", "5", "6" , "7" , "8", "9"};
									feature.setmMicroVol(index + 1);
									mMicoVolShow.setText(String.valueOf(index+1));
									break;
								case RESOLUTION:
									//items = new String[]{"Good(360x240)", "Better(720x480)", "Best(1280x720)"};
									feature.setmResolution(String.valueOf(index +1));
									if(index == 0)
									{
										mResolutionShow.setText("Good(360x240)");
									}else if(index ==1)
									{
										mResolutionShow.setText("Better(720x480)");
									}else if(index ==2)
									{
										mResolutionShow.setText("Best(1280x720)");
									}
									break;
								case FRAME:
									//items = new String[]{"Low", "Good" , "Better" , "Best"};
									
									switch(index)
									{
									case 0:
										mFrameRateShow.setText("Low(1 FPS)");
										feature.setmFrameRate(0);
										break;
									case 1:
										mFrameRateShow.setText("Fair(5 FPS)");
										feature.setmFrameRate(2);
										break;
									case 2:
										mFrameRateShow.setText("Good(10 FPS)");
										feature.setmFrameRate(3);
										break;
									case 3:
										mFrameRateShow.setText("Better(15 FPS)");
										feature.setmFrameRate(5);
										break;
									case 4:
										mFrameRateShow.setText("Best(30 FPS)");
										feature.setmFrameRate(7);
										break;
									
									default:
										break;
									}
									
									break;
								
								}
								Utils.sendODPSetFeature(doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd(), feature);
								dialog.dismiss();
							}
							
						}).show();
	}


	/**
	 * @param speaker
	 * @return
	 */
	private int getShowWhichIndex(DIALOG_ITEM speaker) {
		// TODO Auto-generated method stub
		int ret = 0;
		switch(speaker)
		{
		case PIR:
			ret = feature.getPIR();
			break;
		case SPEAKER:
			//items = new String[]{"1", "2" , "3" , "4", "5", "6" , "7" , "8", "9"};
			ret = feature.getmSpeakerVol() -1;
			
			break;
		case MICRO:
			//items = new String[]{"1", "2" , "3" , "4", "5", "6" , "7" , "8", "9"};
			ret = feature.getmMicroVol() - 1;
			break;
		case RESOLUTION:
			//items = new String[]{"360x240", "720x480", "1280x720"};
			try{
			ret = Integer.parseInt(feature.getmResolution()) - 1;
			}catch(Exception e)
			{
				e.printStackTrace();
				ret = 0;
			}
			break;
		case FRAME:
			//items = new String[]{"1", "3", "5", "10", "12", "15", "20", "30"};
			try{
				ret = feature.getmFrameRate();
				switch(ret)
				{
				case 0:
					ret = 0;
					break;
				case 1:
					ret = 1;
					break;
				case 2:
					ret = 1;
					break;
				case 3:
					ret = 2;
					break;
				case 4:
					ret = 3;
					break;
				case 5:
					ret = 3;
					break;
				case 6:
					ret = 4;
					break;
				case 7:
					ret = 4;
					break;
				}
			}catch(Exception e)
			{
				e.printStackTrace();
				ret = 0;
			}
			break;
		default:
			break;
		}
		return ret;
	}

	/**
	 * @param string
	 * @param string2
	 */
	protected void popupRenameDialog(String title, String editName) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.dialog_one_eidit_name, null);

		TextView userName = (TextView) textEntryView
				.findViewById(R.id.edit_name);
		userName.setText(editName);
		EditText name = (EditText) textEntryView
				.findViewById(R.id.edit);
		
		InputFilter[] filters = new InputFilter[2];
		filters[1] = new InputFilter.LengthFilter(16);
	    filters[0] = new InputFilter(){
	        @Override
	        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
	            if (end > start) {

	                char[] acceptedChars = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 
	                        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
	                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '@', '.', '_', '#', '$', '%', '&', '*', '-', '+', '(', ')', '!', '"', '\'', ':', 
	                        ';', '/', '?', ',', '~', '`', '|', '\\', '^', '<', '>', '{', '}', '[', ']', '=', '.', '?','?'};

	                for (int index = start; index < end; index++) {                                         
	                    if (!new String(acceptedChars).contains(String.valueOf(source.charAt(index)))) { 
	                        return ""; 
	                    }               
	                }
	            }
	            return null;
	        }

	    };
	    name.setFilters(filters);
	    
		builder.setTitle(title);
		builder.setView(textEntryView);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				
				EditText name = (EditText) textEntryView
						.findViewById(R.id.edit);
				
				String tmp = name.getEditableText().toString();
				tmp = tmp.trim();
				if( TextUtils.isEmpty(tmp))
				{
					Toast.makeText(mContext, R.string.nortek_name_not_null, Toast.LENGTH_SHORT).show();
					return;
				}
				
				//�����߼�			
				Door one = Door.read(mContext, doorInfo.getOdpIndex());
				String preName = one.getODPName();
				one.setODPName(name.getText().toString());
				Log.d("tecom", "update name, preName:" + preName + "  name:" + name.getText().toString() );
				Door.save(mContext, one);
				
				//odp list ����
				doorInfo.setOdpName(name.getText().toString());
				//����CallLog���ݿ⣬��ʹCall Log��odp name����
				DataHelper.getInstance().updateCallLogDoorName(one.getId(), name.getText().toString());
				mHandler.sendEmptyMessage(ODP_UPDATE_NAME);
				
				//send to ODP to update
				sendODPSetName(name.getText().toString());
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
			}
		});
		builder.create().show();
	}

	/**
	 * @param tmp
	 */
	protected void sendODPSetName(String name) {
		// TODO Auto-generated method stub
		byte[] data = DataConversion.StringToUTF8Byte(name);
		
		MessageType.C2CAccountInfo peerInfo = new MessageType.C2CAccountInfo();
		peerInfo.peerId = doorInfo.getOdpAccount();
		peerInfo.loginAccount = doorInfo.getOdpLocalAccount();
		peerInfo.loginPassword = doorInfo.getOdpLocalPwd();
		RequestMessageType sendOneMsg = new RequestMessageType();
		sendOneMsg.setType(MessageDataDefine.C2C);
		sendOneMsg.setPeerAccountInfo(peerInfo);
		sendOneMsg.updateMessageDatas((short) 0x0703, data);		
		
		MessageQueueManager.getInstance().addMessage(sendOneMsg);

	}
	
	protected void dialog() {
    	AlertDialog.Builder builder = new Builder(mContext);
    	builder.setMessage(R.string.sure_to_delete);
    	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//�Ȼ�Ӧsetup done��ɹ�,����ʧ��
				int ret = ODPManager.getInstance().unRegisterODP(mContext, doorInfo.getOdpAccount());
				if(ret >=0 )
					sendPPToODPAndLocalRemove();
				else
					Toast.makeText(mContext, mContext.getString(R.string.ntut_tip_14), Toast.LENGTH_SHORT).show();
			}
    		
    	});
    	builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
    		
    	});
    	builder.create().show();
	}
	
	/**
	 * 
	 */
	protected void sendPPToODPAndLocalRemove() {
		// TODO Auto-generated method stub
		//����˽��Э��
		String acc = LocalUserInfo.getInstance().getC2cAccount();
		String srv = getString(R.string.def_reg_srv);
		acc = AppUtils.processWebLoginAccount(acc, srv);
		Log.d("Tecom", "remove self. acc:" + acc);
		Utils.sendRemoveSmpSelf(doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), 
				doorInfo.getOdpLocalPwd(), acc, "123");
		
		// ����ODP����ͨ������ɾ��ODP
		Log.d("Tecom", "remove odp index:" + doorInfo.getOdpIndex());
		boolean ret = Door.clear(mContext, doorInfo.getOdpIndex());
		//odp listɾ����һ��
		ODPManager.getInstance().getODPList().remove(odpId);
		//ɾ����odp��Ӧ��call log				
		DataHelper.getInstance().DelUserInfo(doorInfo.getOdpAccount());
		if(ret)
		{
			finish();
			//�������һ̨ODP��û�У�������Add ODP��ҳ��
			if(ODPManager.getInstance().getODPNum() == 0)
			{
				Intent mIntent = new Intent(mContext, DoorPhoneAddType.class);
				startActivity(mIntent);
				EventBus.getDefault().post(new DoorPhoneListCloseDrawerEvent());
			}
		}
	}

	
	public void onEvent(ReceivedC2CEvent event){
		
		ReceivedMessageType msg = event.getMsg();
		//SMP_GET_ODP_SYS_PARAMETER_ACK (0x0502)
		if(msg!= null &&  msg.getEventType() == MessageDataDefine.SMP_GET_ODP_SYS_PARAMETER_ACK)
		{
			mHandler.sendEmptyMessage(GET_ODP_FEATURE_OK);
			
			String str[] = event.getMsg().getPayloadStr();
			if( str == null)
			{
				Log.d("Tecom", "get odp feature null.... return");
				return ;
			}
			for(String s : str)
			{
				System.out.print(s);
			}
			Utils.updateODPFeature(str);
			mHandler.sendEmptyMessage(UPDATE_UI_FROM_ODP_DATA);
			
		}else
			//SMP_SET_ODP_SYS_PARAMETER_ACK (0x0504)
			if(msg!= null &&  msg.getEventType() == MessageDataDefine.SMP_SET_ODP_SYS_PARAMETER_ACK)
			{
				byte ret = event.getMsg().getPayloadByte();
				if(ret ==  0x01)//�ɹ�
				{
					mHandler.sendEmptyMessage(SET_ODP_FEATURE_OK);
				}else if(ret ==  0x02) //ʧ��
				{
					mHandler.sendEmptyMessage(SET_ODP_FEATURE_FAIL);
				}
			}
			else if(msg!= null && event.getMsg().getEventType() == MessageDataDefine.SMP_SET_ODP_SYSTEM_PSWD_ACK )
			{
				System.out.printf("UserSystemSettingDoor... get SMP_SET_ODP_SYSTEM_PSWD_ACK.");
				
				byte ret = event.getMsg().getPayloadByte();
				if(ret == 0x01)
				{
					System.out.printf("UserSystemSettingDoor... get SMP_SET_ODP_SYSTEM_PSWD_ACK. 0x01, autho ok");
					mHandler.sendEmptyMessage(SEND_ODP_PWD_OK);
				}else if(ret == 0x02)
				{
					mHandler.removeMessages(SEND_ODP_PWD_TIME_OUT);
					System.out.printf("UserSystemSettingDoor... get SMP_SET_ODP_SYSTEM_PSWD_ACK. 0x01, autho failed");
					
					mHandler.sendEmptyMessage(SEND_ODP_PWD_FAILED);	
					
					return;
				}else
				{
					mHandler.removeMessages(SEND_ODP_PWD_TIME_OUT);
					System.out.printf("DoorPhoneAPPasswordInput... get SMP_SET_ODP_SYSTEM_PSWD_ACK. 0x01, autho error code..");
					mHandler.sendEmptyMessage(SEND_ODP_PWD_FAILED);
					
				}
			}
	}

	
}
