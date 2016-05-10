package com.gocontrol.doorbell;

import java.util.ArrayList;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.QuietTimeSet;
import com.gocontrol.doorbell.bean.ReceivedC2CEvent;
import com.gocontrol.doorbell.bean.QuietTimeSet.QuietTime;
import com.gocontrol.doorbell.message.MessageDataDefine;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.utils.Utils;
import com.ypy.eventbus.EventBus;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class QuietTimeHoursActivity extends Activity {

	private static final String TAG = QuietTimeHoursActivity.class.getSimpleName();
	
	protected static final int DIALOG_PROCESS_QUERY = 1000;
	protected static final int DIALOG_PROCESS_OK = 1001;
	private static final int DIALOG_TIME_OUT = 1002;
	
	//private ImageView addODP;
	private Context mContext;
	
	private TextView txtTitle;
	private ListView listView;
	private ODPInfo doorInfo;
	private CheckBox mCheckbox;
	private boolean chimeBoxStatus;
	
	private Handler mHandler;

	private ProgressDialog proDialog;
	
	public ArrayList<QuietTime> dataSet;
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG,"onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG,"onPause");
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		dataSet = new ArrayList<QuietTime>();
		
		mContext = this;
		int odpId = getIntent().getIntExtra("door_id", 0);
		doorInfo = ODPManager.getInstance().getOneODP(odpId);
		
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
		ActionBar.LayoutParams.MATCH_PARENT,
		ActionBar.LayoutParams.MATCH_PARENT,
		Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar_v7, null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		txtTitle.setText(getString(R.string.quiet_hours));
		ImageView btnBack = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(getFragmentManager().getBackStackEntryCount() > 0){
				getFragmentManager().popBackStack();
			}else{
				finish();
			}
		}
		});
		ImageView menu = (ImageView)getActionBar().getCustomView().findViewById(R.id.btn_menu);
		menu.setVisibility(4);
		
		setContentView(R.layout.quiet_time_hours);
		EventBus.getDefault().register(this);
		
		mCheckbox = (CheckBox)this.findViewById(R.id.chime_box_ck);
		if(mCheckbox != null)
		{
			mCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					// TODO Auto-generated method stub
					chimeBoxStatus = isChecked;
				}
				
			});
		}
		//ask quietime information from ODP
		Utils.sendQueryQuietimeInfo(doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd());
		
		
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
				case DIALOG_PROCESS_QUERY:
					popUpProcessDialg();
					break;
				case DIALOG_PROCESS_OK:
					dimissProcessDialog();
					updateCheckBox();
					break;
				case DIALOG_TIME_OUT:
					dimissProcessDialog();
					Toast.makeText(mContext, getString(R.string.request_time_out), Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}
		
		};
		
		mHandler.sendEmptyMessageDelayed(DIALOG_TIME_OUT, 12*1000);
		mHandler.sendEmptyMessage(DIALOG_PROCESS_QUERY);
	}    

	private void updateCheckBox() {
		// TODO Auto-generated method stub
		//just judge the first item for nortek.
		QuietTime one = dataSet.get(0);
		if(one != null)
		{
			if(one.isSelected == true)
			{
				mCheckbox.setChecked(true);
			}else
			{
				mCheckbox.setChecked(false);
			}
		}
	}
		
	
	/**
	 * 
	 */
	protected void dimissProcessDialog() {
		// TODO Auto-generated method stub
		if (proDialog != null)
			proDialog.dismiss();
	}

	/**
	 * 
	 */
	protected void popUpProcessDialg() {
		// TODO Auto-generated method stub
		if (proDialog == null)
			proDialog = android.app.ProgressDialog.show(this,
					getString(R.string.operation_process),
					getString(R.string.tecom_precess_content));
		else
			proDialog.show();
		
		proDialog.setCancelable(true);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		
		//remove all.
		mHandler.removeCallbacksAndMessages(null);
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		try{
			QuietTime one = dataSet.get(0);
			if(one != null){
				if(chimeBoxStatus)
				{
					one.isSelected = true;
					//one.startTimeHour = 0;
					//one.startTimeMin = 0;
					//one.endTimeHour = 24;
					//one.endTimeMin = 24;
				}else
				{
					one.isSelected = false;
					//one.startTimeHour = 0;
					//one.startTimeMin = 0;
					//one.endTimeHour = 0;
					//one.endTimeMin = 0;
				}
			}
			Utils.sendSetQuietTime(doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd() , dataSet);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	//public void onEvent(ReceivedODPEvent event){
	public void onEvent(ReceivedC2CEvent event){
		
		if( event.getMsg().getEventType() == MessageDataDefine.SMP_GET_ODP_QUIET_TIME_ACK )
		{
			mHandler.removeMessages(DIALOG_TIME_OUT);			
			System.out.printf("QuietTimeHoursActivity... get SMP_GET_ODP_QUIET_TIME_ACK.");
			dataSet.clear();
			String str[] = event.getMsg().getPayloadStr();
			for(String s: str)
				System.out.println(s);
			for(int i=0; i<str.length; )
			{
				String enableStatus = Utils.getEqualString(str[i]);
				String startHour = Utils.getEqualString(str[i+1]);
				String startMin = Utils.getEqualString(str[i+2]);
				String endHour = Utils.getEqualString(str[i+3]);
				String endMin = Utils.getEqualString(str[i+4]);
				i=i+5;
				QuietTime one = QuietTimeSet.getInstance().new QuietTime();;
				one.endTimeHour = Integer.valueOf(endHour);
				one.endTimeMin = Integer.valueOf(endMin);
				one.startTimeHour = Integer.valueOf(startHour);
				one.startTimeMin = Integer.valueOf(startMin);
				one.isSelected = Integer.valueOf(enableStatus)==0? false:true;
				dataSet.add(one);
			}
			mHandler.sendEmptyMessage(DIALOG_PROCESS_OK);
		}
		
	}
	}
