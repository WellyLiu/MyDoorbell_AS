package com.gocontrol.doorbell.ui.v7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.gocontrol.doorbell.AppApplication;
import com.gocontrol.doorbell.ConnectTask;
import com.gocontrol.doorbell.Door;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.ViewerActivity;
import com.gocontrol.doorbell.ConnectTask.Result;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.model.SystemConfigManager;
import com.gocontrol.doorbell.utils.BuildConfig;
import com.gocontrol.doorbell.utils.LogUtils;
import com.ypy.eventbus.EventBus;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.TextViewCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class CallFunctionActivity extends ListActivity {

	private static final String TAG = CallFunctionActivity.class
			.getSimpleName();
	public static final String KEY_DOOR_ID = "door id";
	public static final String KEY_ODP_ID = "odp id";//在odp list中的位置
	public static final String KEY_ODP_ACC = "odp acc"; //odp对应的acc账号
	public static final String KEY_ODP_NAME ="odp name"; // odp name.
	private Door door;
	private TextView txtTitle;
	private DrawerLayout mDrawerLayout = null;
	private ListView mLvRightMenu;

	private String odpAcc; //odp 的acc账号
	private int odpID;// 在odp list中的位置
	private String doorName;
	
	private Context mContext;
	private ProgressDialog proDialog;
	private static Handler mHandler;
	private final static int START_LOG_OUT = 1000;
	private final static int LOG_OUT_DONE = 1001;
	private final static int LOG_OUT_ERROR = 1002;
	private final static int POP_UP_SURE_LOG_OUT = 1003;
	private final static int LOG_OUT_TIME_OUT = 1004;
	
	private ODPInfo odpInfo;
	
	public static boolean sFlag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		if( !EventBus.getDefault().isRegistered(this))
			EventBus.getDefault().register(this);
		
		odpAcc = getIntent().getStringExtra(KEY_ODP_ACC);
		odpID = getIntent().getIntExtra(KEY_ODP_ID, 0);
		doorName = getIntent().getStringExtra(KEY_ODP_NAME);
		odpInfo = ODPManager.getInstance().getOneODP(odpAcc);
				
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.actionbar_v7);
		txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(doorName);
		txtTitle.setText(spanBuilder);
		
		ImageView btnBack = (ImageView) getActionBar().getCustomView()
				.findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		ImageView btnMenu = (ImageView) getActionBar().getCustomView()
				.findViewById(R.id.btn_menu);
		btnMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mDrawerLayout != null)
				{
					if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
						mDrawerLayout.closeDrawers();
					else
						mDrawerLayout.openDrawer(Gravity.RIGHT);
				}
			}
		});
		btnMenu.setVisibility(View.VISIBLE);
		setContentView(R.layout.call_function);
		getWindow().setBackgroundDrawable(null) ;
		
		door = (Door) getIntent().getSerializableExtra(KEY_DOOR_ID);
		if (door == null) {
			System.out
					.println("====== CallFunctionAcitivity==== door is null... ");
		} else {
			System.out
			.println("====== CallFunctionAcitivity==== door is ... " + door);
		}

		getListView().setBackgroundColor(
				getResources().getColor(R.color.bg_color));
		getListView().setDivider(
				getResources().getDrawable(R.color.btn_bg_color));
		getListView().setDividerHeight(dip2px(1));
		
		SimpleAdapter adapter = new SimpleAdapter(this, getData(),
				R.layout.func_item, new String[] { "img", "info" }, new int[] {
						R.id.func_img, R.id.func_info });
		this.setListAdapter(adapter);
		
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mLvRightMenu = (ListView) findViewById(R.id.right_drawer);

		setUpDrawer();

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
				case START_LOG_OUT:
					SystemConfigManager.getInstance().setLogoutState(1);
					int ret = C2CHandle.getInstance().resetAllNotification();
					//Log.d("tst", "logout==============ret:" + ret);
					if(ret < 0)
					{
						SystemConfigManager.getInstance().setLogoutState(0);
						mHandler.sendEmptyMessage(LOG_OUT_ERROR);
					}else{
						proDialog = android.app.ProgressDialog.show(mContext, mContext.getString(R.string.ntut_tip_11),
								getString(R.string.tecom_precess_content));
						proDialog.setCancelable(true);
						mHandler.sendEmptyMessageDelayed(LOG_OUT_TIME_OUT, 10*1000);
					}
					break;
				case LOG_OUT_DONE:
					if(proDialog != null)
						proDialog.dismiss();
					
					SystemConfigManager.getInstance().setAppAutoLogin(2);
					SystemConfigManager.getInstance().saveAppAutoLogin(mContext);
					//Toast.makeText(mContext, mContext.getString(R.string.log_out_suc), Toast.LENGTH_SHORT).show();
					//startActivity(new Intent(mContext, UserLoginUI.class));
					
					finish();
					break;
				case LOG_OUT_ERROR:
					if(proDialog != null)
						proDialog.dismiss();
					Toast.makeText(mContext, mContext.getString(R.string.log_out_error), Toast.LENGTH_SHORT).show();
					break;
				case POP_UP_SURE_LOG_OUT:
					popupTipDialog();
					break;	
				case LOG_OUT_TIME_OUT:
					if(proDialog != null)
						proDialog.dismiss();
					Toast.makeText(mContext, mContext.getString(R.string.ntut_tip_12), Toast.LENGTH_SHORT).show();
					break;
				}
			}
			
		};
	}

	protected void popupTipDialog() {
    	AlertDialog.Builder builder = new Builder(mContext);
    	builder.setTitle(R.string.system_logout);
    	builder.setMessage(R.string.sure_to_delete_logout);
    	builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mHandler.sendEmptyMessage(START_LOG_OUT);
			}
    		
    	});
    	builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
    		
    	});
    	builder.create().show();
	}
	
	private void setUpDrawer() {
		LayoutInflater inflater = LayoutInflater.from(this);
		mLvRightMenu.addHeaderView(inflater.inflate(
				R.layout.header_just_username, mLvRightMenu, false));
		mLvRightMenu.setAdapter(new MenuItemAdapter(this));
		
		TextView userAcc = (TextView) mLvRightMenu.findViewById(R.id.id_username);
        TextView showAcc = (TextView) mLvRightMenu.findViewById(R.id.id_showname);
        if(userAcc != null)
        	userAcc.setText(LocalUserInfo.getInstance().getC2cAccount());
        if(showAcc != null)
        	showAcc.setText(LocalUserInfo.getInstance().getLocalName());
	}

	private int dip2px(float dpValue) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("img", R.drawable.function_monitor);
        map.put("info", getString(R.string.select_monitor));
        
        list.add(map);
 
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.function_clips);
        map.put("info", getString(R.string.dlg_select_clips));
        
        list.add(map);
 
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.function_pictures);
        map.put("info", getString(R.string.dlg_select_pictures));
        
        list.add(map);
        
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.function_calllog);
        map.put("info", getString(R.string.select_log));
        
        list.add(map);
        
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.function_event);
        map.put("info", getString(R.string.detect_log));
        
        list.add(map);
        
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.function_pir);
        map.put("info", getString(R.string.PIR_log));
        
        list.add(map);
         
        return list;
    }

	private class StartConnect extends AsyncTask<Void, Void, Result> {

		private Door door;
		private ProgressDialog mProgressDialog;
		private int sessionCode;
		
		public StartConnect(Door door) {
			this.door = door;
		}

		private void showFailMsg(String msg) {
			
			// show dialog to show connection fail
			new AlertDialog.Builder(CallFunctionActivity.this)
				.setMessage(msg)
				.setCancelable(true)
				.setPositiveButton("OK", null)
				.show();
		}
		
		private void resumeState() {
			((AppApplication) getApplication()).setManualCall(false, null);
		}
		
		@Override
		protected void onPreExecute() {
			
			// show progress dialog
			mProgressDialog = ProgressDialog.show(CallFunctionActivity.this, null, "Connect ...");
			
			((AppApplication) getApplication()).setManualCall(true, door.getId());

		}
		
		@Override
		protected Result doInBackground(Void... params) {

			// start connection with session code
			sessionCode = new Random().nextInt(1000);
			String tag = "i=" + String.valueOf(sessionCode) + ";";
			return new ConnectTask().startConnection(door.getId(), door.getAccount(), door.getPassword(), tag);
		}

		@Override
		protected void onPostExecute(Result result) {
			
			// dismiss progress dialog
			mProgressDialog.dismiss();
			
			if (sFlag) {
				sFlag = false;
				return;
			}

			// process connection result
			switch (result) {
			
				// success case
				case CONNECTED_P2P:
				case CONNECTED_RELAY:
					startActivity(new Intent(CallFunctionActivity.this, ViewerActivity.class)
						.putExtra("door", door)
						.putExtra("line.id", result.getLineId())
						.putExtra("session.code", sessionCode));
					break;
					
				// fail case
				case FAIL_CALLING:		showFailMsg(getString(R.string.failed_reason_calling_fail));		resumeState(); break;
				case FAIL_NETWORK:		showFailMsg(getString(R.string.failed_reason_calling_fail) + "( " + result.getLineId() + ")");	resumeState(); break;
				case FAIL_TIMEOUT:		showFailMsg(getString(R.string.failed_reason_timeout));		resumeState(); break;
				case FAIL_UNAUTHORIZED:	showFailMsg(getString(R.string.failed_reason_unauthorized));		resumeState(); break;
				case FAIL_SRV_NO_RSP:					
					showFailMsg(getString(R.string.failed_reason_server_no_response));	
					resumeState(); 					
				break;
				case FAIL_PEER_OFFLINE:
					Log.d("tecom", "FAIL_PEER_OFFLINE");
					updateODPStatus(door.getAccount());
					showFailMsg(getString(R.string.failed_reason_peer_offline));		
					resumeState();
					break;
				case FAIL_PEER_NO_RSP:	showFailMsg(getString(R.string.failed_reason_peer_not_response));	resumeState(); break;
				case FAIL_PEER_BUSY:	showFailMsg(getString(R.string.failed_reason_peer_busy));		resumeState(); break;
			}
		}	
	}


	public void onButtonByConnect() {

		if (((AppApplication) getApplication()).isLockConnection()) {
			Toast.makeText(this, "Busy, a session is calling", Toast.LENGTH_SHORT).show();
			return;
		}

		// get press button index

		if (door == null) {
			System.out
					.println("====== CallFunctionAcitivity==== door is null 2...");
		}

		if (!door.getId().isEmpty()) {
			try{
				new StartConnect(door).execute();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param account
	 */
	public void updateODPStatus(String account) {
		// TODO Auto-generated method stub
		
		ODPInfo odp = ODPManager.getInstance().getOneODP(odpAcc);
		
		if(odp  != null)
		{
			
			odp.setOnLine(false);
			odp.setOdpState(2);
		}
		
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		switch (position) {
		case 0:// monitor
			if(odpInfo != null && (odpInfo.getOdpState() == 2 || odpInfo.getOdpState() == 0))
			{
				Toast.makeText(this, getString(R.string.nortek_odp_offline), Toast.LENGTH_SHORT).show();
			}else
				onButtonByConnect();
			break;
		case 1:// clips
			startActivity(new Intent(CallFunctionActivity.this,
					MediaActivity.class).putExtra("index", door.getId()).putExtra("type",
					MediaInfoListAdapter.MEDIA_TYPE_CLIP).putExtra("door_name", doorName));
			break;
		case 2:// pictures
			startActivity(new Intent(CallFunctionActivity.this,
					MediaActivity.class).putExtra("index", door.getId()).putExtra("type",
					MediaInfoListAdapter.MEDIA_TYPE_PICTURE).putExtra("door_name", doorName));
			break;
		case 3:// log
			startActivity(new Intent(CallFunctionActivity.this,
					CallLogActivity.class).putExtra("door_id", odpAcc));
			break;
		case 4:// Motion detect log
			if(odpInfo != null && odpInfo.getOdpState() == 2)
			{
				Toast.makeText(this, getString(R.string.nortek_odp_offline), Toast.LENGTH_SHORT).show();
			}else
			startActivity(new Intent(CallFunctionActivity.this,
					CallMotionPIRLogActivity.class).putExtra("door_id", odpAcc)
													  .putExtra("item_type", "motion_detect"));
			break;
		case 5:// PIR detect log
			if(odpInfo != null && odpInfo.getOdpState() == 2)
			{
				Toast.makeText(this, getString(R.string.nortek_odp_offline), Toast.LENGTH_SHORT).show();
			}else
			startActivity(new Intent(CallFunctionActivity.this,
					CallMotionPIRLogActivity.class).putExtra("door_id", odpAcc)
													  .putExtra("item_type", "pir_detect"));
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	public void onEvent(Object event){
		
		if( event instanceof C2CEvent)
		{
			Log.d("Tecom", ((C2CEvent) event).getInfo());
			if(C2CEvent.C2C_SETUP_ERROR == event){
				mHandler.removeMessages(LOG_OUT_TIME_OUT);
				mHandler.sendEmptyMessage(LOG_OUT_ERROR);
			}else if(C2CEvent.C2C_SETUP_DONE == event)
			{
				if(SystemConfigManager.getInstance().getLogoutState() == 1)
				{
					mHandler.removeMessages(LOG_OUT_TIME_OUT);
					mHandler.sendEmptyMessage(LOG_OUT_DONE);			
				}
				
			}
		}
		
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
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	private static class LvMenuItem {
		public LvMenuItem(int icon, String name) {
			this.icon = icon;
			this.name = name;

			if (icon == NO_ICON && TextUtils.isEmpty(name)) {
				type = TYPE_SEPARATOR;
			} else if (icon == NO_ICON) {
				type = TYPE_NO_ICON;
			} else {
				type = TYPE_NORMAL;
			}

			if (type != TYPE_SEPARATOR && TextUtils.isEmpty(name)) {
				throw new IllegalArgumentException(
						"you need set a name for a non-SEPARATOR item");
			}

			LogUtils.LOGD(this, type + "");

		}

		public LvMenuItem(String name) {
			this(NO_ICON, name);
		}

		public LvMenuItem() {
			this(null);
		}

		private static final int NO_ICON = 0;
		public static final int TYPE_NORMAL = 0;
		public static final int TYPE_NO_ICON = 1;
		public static final int TYPE_SEPARATOR = 2;

		int type;
		String name;
		int icon;

	}

	private static class MenuItemAdapter extends BaseAdapter {
		private final int mIconSize;
		private LayoutInflater mInflater;
		private Context mContext;

		public MenuItemAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			mContext = context;

			mIconSize = 65;
		}

		private List<LvMenuItem> mItems = new ArrayList<LvMenuItem>(
				Arrays.asList(
						new LvMenuItem(R.drawable.menu_system_settings,AppApplication.getInstance().getString(R.string.system_settings)),
						new LvMenuItem(	R.drawable.menu_account_management,	AppApplication.getInstance().getString(R.string.account_management)), 
						new LvMenuItem(R.drawable.menu_about_device, AppApplication.getInstance().getString(R.string.about_devices)),
                        new LvMenuItem(R.drawable.where_to_buy, AppApplication.getInstance().getString(R.string.nortek_where_to_buy)),
                        new LvMenuItem(R.drawable.feedback, AppApplication.getInstance().getString(R.string.nortek_feedback)),
                        new LvMenuItem(R.drawable.menu_system_logout, AppApplication.getInstance().getString(R.string.system_logout)) 
				// new LvMenuItem(),
				// new LvMenuItem("Sub Items"),
				// new LvMenuItem(R.drawable.ic_dashboard, "Sub Item 1"),
				// new LvMenuItem(R.drawable.ic_forum, "Sub Item 2")
				));

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getViewTypeCount() {
			return 3;
		}

		@Override
		public int getItemViewType(int position) {
			return mItems.get(position).type;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LvMenuItem item = mItems.get(position);
			switch (item.type) {
			case LvMenuItem.TYPE_NORMAL:
				if (convertView == null) {
					convertView = mInflater.inflate(
							R.layout.design_drawer_item, parent, false);
				}
				TextView itemView = (TextView) convertView;
				itemView.setText(item.name);

				Drawable icon = mContext.getResources().getDrawable(item.icon);
				setIconColor(icon);
				if (icon != null) {
					icon.setBounds(20, 0, mIconSize + 20, mIconSize);
					TextViewCompat.setCompoundDrawablesRelative(itemView, icon,
							null, null, null);
				}

				break;
			case LvMenuItem.TYPE_NO_ICON:
				if (convertView == null) {
					convertView = mInflater.inflate(
							R.layout.design_drawer_item_subheader, parent,
							false);
				}
				TextView subHeader = (TextView) convertView;
				subHeader.setText(item.name);
				break;
			case LvMenuItem.TYPE_SEPARATOR:
				if (convertView == null) {
					convertView = mInflater.inflate(
							R.layout.design_drawer_item_separator, parent,
							false);
				}
				break;
			}

			final int itemIndex = position;
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
				
					switch (itemIndex) {
					case 0:
						mContext.startActivity(new Intent(mContext,
								UserSystemSettings.class));
						break;
					case 1:
						mContext.startActivity(new Intent(mContext,
								UserAccountManager.class));
						break;
					
					case 2:
						mContext.startActivity(new Intent(mContext,
								UserAboutDevice.class));
						break;
					
					case 3: //logout			
						Uri uri = Uri.parse(BuildConfig.nortekUri);  
			            Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
			            mContext.startActivity(intent);  
						break;
						
					case 4: //feedback					
						LogUtils.SendLogFileToEmail(mContext);
						///xxx;
						break;
					case 5: //logout					
						
						mHandler.sendEmptyMessage(POP_UP_SURE_LOG_OUT);
						break;
						
					default:
						break;
					}

				}
			});
			return convertView;
		}

		public void setIconColor(Drawable icon) {
			//int textColorSecondary = android.R.attr.textColorSecondary;
			int textColorSecondary = R.color.gray;
			TypedValue value = new TypedValue();
			if (!mContext.getTheme().resolveAttribute(textColorSecondary,
					value, true)) {
				return;
			}
			int baseColor = mContext.getResources().getColor(value.resourceId);
			icon.setColorFilter(baseColor, PorterDuff.Mode.MULTIPLY);
		}
	}

}
