package com.gocontrol.doorbell.ui.v7;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.gocontrol.doorbell.AppApplication;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.datasource.DataHelper;
import com.gocontrol.doorbell.datasource.DataHelper.ChangeListener;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.model.SystemConfigManager;
import com.gocontrol.doorbell.ui.CallLogAdapter;
import com.gocontrol.doorbell.utils.BuildConfig;
import com.gocontrol.doorbell.utils.LogUtils;
import com.ypy.eventbus.EventBus;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CallLogActivity extends Activity implements OnItemClickListener,ChangeListener {

	private static final String TAG = CallLogActivity.class.getSimpleName();
	private CursorAdapter callLogAdapter;
	private TextView txtTitle;
	private List<Map<String, Object>> list;
	private ListView listView;
	private Button delAll;
	private TextView status,log;
	private DrawerLayout mDrawerLayout = null;
	private ListView mLvRightMenu;
	Cursor callLogCursor;
	
	private Context mContext;
	private String doorPeerId;
	
	private static Handler mHandler;
	private ProgressDialog proDialog;
	private final static int START_LOG_OUT = 1000;
	private final static int LOG_OUT_DONE = 1001;
	private final static int LOG_OUT_ERROR = 1002;
	private final static int POP_UP_SURE_LOG_OUT = 1003;
	private final static int LOG_OUT_TIME_OUT = 1004;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		if( !EventBus.getDefault().isRegistered(this))
			EventBus.getDefault().register(this);
		
		// get index from previous actvitiy
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
		ActionBar.LayoutParams.MATCH_PARENT,
		ActionBar.LayoutParams.MATCH_PARENT,
		Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar_v7, null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		ImageView btnBack = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
		});
		ImageView btnMenu = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_menu);
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
		setContentView(R.layout.calllog);
		getWindow().setBackgroundDrawable(null) ;
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mLvRightMenu = (ListView) findViewById(R.id.right_drawer);   
	
		doorPeerId = getIntent().getStringExtra("door_id");
		Log.d("Tecom", "CallLogActivity......" + doorPeerId);
		//置ODP miss call number为0
		ODPInfo one = ODPManager.getInstance().getOneODP(doorPeerId);
		if(one != null)
			one.setOdpMissedCallNum(0);
		 
		setUpDrawer();
		DataHelper.getInstance().addListener(this);
		listView = (ListView)findViewById(R.id.list);
		callLogCursor = DataHelper.getInstance().getAllDataItems(doorPeerId);
		//Log.d("tst", "cursor size is:" + callLogCursor.getCount());
		callLogAdapter = new CallLogAdapter(this, callLogCursor);
		listView.setAdapter(callLogAdapter);
		listView.setOnItemClickListener(this);
		
		status = (TextView)findViewById(R.id.txt_status);
		log = (TextView)findViewById(R.id.txt_log);
		
		delAll = (Button)findViewById(R.id.btn_del_all);
		delAll.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
						// TODO Auto-generated method stub
				
				//DataHelper.getInstance(CallLogActivity.this).DelUserInfo(doorPeerId);
				dialog();
				
			}
		});
		txtTitle.setText(getString(R.string.select_log));
		
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
				case LOG_OUT_TIME_OUT :
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
	public void onEvent(Object event){
		
		if( event instanceof C2CEvent)
		{
			Log.d("Tecom", ((C2CEvent) event).getInfo());
			if(C2CEvent.C2C_SETUP_ERROR == event){
				mHandler.removeMessages(LOG_OUT_TIME_OUT);
				mHandler.sendEmptyMessage(LOG_OUT_ERROR);
			}else if(C2CEvent.C2C_SETUP_DONE == event)
			{
				if(SystemConfigManager.getInstance().getLogoutState() == 1){
					mHandler.removeMessages(LOG_OUT_TIME_OUT);
					mHandler.sendEmptyMessage(LOG_OUT_DONE);
				}
			}
		}
		
	}
	

	private void setUpDrawer()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mLvRightMenu.addHeaderView(inflater.inflate(R.layout.header_just_username, mLvRightMenu, false));
        mLvRightMenu.setAdapter(new MenuItemAdapter(this));
        
        TextView userAcc = (TextView) mLvRightMenu.findViewById(R.id.id_username);
        TextView showAcc = (TextView) mLvRightMenu.findViewById(R.id.id_showname);
        if(userAcc != null)
        	userAcc.setText(LocalUserInfo.getInstance().getC2cAccount());
        if(showAcc != null)
        	showAcc.setText(LocalUserInfo.getInstance().getLocalName());
    }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		DataHelper.getInstance().removeListener(this);
		EventBus.getDefault().unregister(this);
	}


	/**
	* ɾ��ĳ���ļ����µ������ļ��к��ļ�
	* @param delpath
	* @return
	*/
	public  boolean deleteFile(String delpath) {
		File file = new File(delpath);
		if (!file.isDirectory()) {
			Log.d(TAG,"delete is file");
			file.delete();
		}else if (file.isDirectory()) {
			Log.d(TAG,"delete is directory");
			File[] fileList = file.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				File delfile = fileList[i];
				if (!delfile.isDirectory()) {
					delfile.delete();
				} else if (delfile.isDirectory()) {
					deleteFile(fileList[i].getPath());
				}
			}
			file.delete();
		}
		return true;
	}
	@Override
	protected void onResume() {
		super.onResume();
		
		// update list
		//mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
	}


	private static class LvMenuItem
    {
        public LvMenuItem(int icon, String name)
        {
            this.icon = icon;
            this.name = name;

            if (icon == NO_ICON && TextUtils.isEmpty(name))
            {
                type = TYPE_SEPARATOR;
            } else if (icon == NO_ICON)
            {
                type = TYPE_NO_ICON;
            } else
            {
                type = TYPE_NORMAL;
            }

            if (type != TYPE_SEPARATOR && TextUtils.isEmpty(name))
            {
                throw new IllegalArgumentException("you need set a name for a non-SEPARATOR item");
            }

            LogUtils.LOGD(this, type + "");


        }

        public LvMenuItem(String name)
        {
            this(NO_ICON, name);
        }

        public LvMenuItem()
        {
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

	 private static class MenuItemAdapter extends BaseAdapter
	    {
	        private final int mIconSize;
	        private LayoutInflater mInflater;
	        private Context mContext;

	        public MenuItemAdapter(Context context)
	        {
	            mInflater = LayoutInflater.from(context);
	            mContext = context;

	            mIconSize = 65;
	        }

	        private List<LvMenuItem> mItems = new ArrayList<LvMenuItem>(
	                Arrays.asList(
	                        new LvMenuItem(R.drawable.menu_system_settings, AppApplication.getInstance().getString(R.string.system_settings)),
	                        new LvMenuItem(R.drawable.menu_account_management, AppApplication.getInstance().getString(R.string.account_management)),
	                        new LvMenuItem(R.drawable.menu_about_device, AppApplication.getInstance().getString(R.string.about_devices)),
	                        new LvMenuItem(R.drawable.where_to_buy, AppApplication.getInstance().getString(R.string.nortek_where_to_buy)),
	                        new LvMenuItem(R.drawable.feedback, AppApplication.getInstance().getString(R.string.nortek_feedback)),
	                        new LvMenuItem(R.drawable.menu_system_logout, AppApplication.getInstance().getString(R.string.system_logout)) 
	                        //new LvMenuItem(),
	                        //new LvMenuItem("Sub Items"),
	                        //new LvMenuItem(R.drawable.ic_dashboard, "Sub Item 1"),
	                        //new LvMenuItem(R.drawable.ic_forum, "Sub Item 2")
	                ));


	        @Override
	        public int getCount()
	        {
	            return mItems.size();
	        }


	        @Override
	        public Object getItem(int position)
	        {
	            return mItems.get(position);
	        }


	        @Override
	        public long getItemId(int position)
	        {
	            return position;
	        }

	        @Override
	        public int getViewTypeCount()
	        {
	            return 3;
	        }

	        @Override
	        public int getItemViewType(int position)
	        {
	            return mItems.get(position).type;
	        }

	        @Override
	        public View getView(int position, View convertView, ViewGroup parent)
	        {
	            LvMenuItem item = mItems.get(position);
	            switch (item.type)
	            {
	                case LvMenuItem.TYPE_NORMAL:
	                    if (convertView == null)
	                    {
	                        convertView = mInflater.inflate(R.layout.design_drawer_item, parent,
	                                false);
	                    }
	                    TextView itemView = (TextView) convertView;
	                    itemView.setText(item.name);
	                   
	                    Drawable icon = mContext.getResources().getDrawable(item.icon);
	                    setIconColor(icon);
	                    if (icon != null)
	                    {
	                        icon.setBounds(20, 0, mIconSize + 20, mIconSize );
	                        TextViewCompat.setCompoundDrawablesRelative(itemView, icon, null, null, null);
	                    }

	                    break;
	                case LvMenuItem.TYPE_NO_ICON:
	                    if (convertView == null)
	                    {
	                        convertView = mInflater.inflate(R.layout.design_drawer_item_subheader,
	                                parent, false);
	                    }
	                    TextView subHeader = (TextView) convertView;
	                    subHeader.setText(item.name);
	                    break;
	                case LvMenuItem.TYPE_SEPARATOR:
	                    if (convertView == null)
	                    {
	                        convertView = mInflater.inflate(R.layout.design_drawer_item_separator,
	                                parent, false);
	                    }
	                    break;
	            }
	            
	            final int itemIndex = position;
	            convertView.setOnClickListener(new OnClickListener()
	            {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
						//just test code. you must control your own business process.
						//((Activity) mContext).finish();
						switch(itemIndex)
						{
						case 0:
							mContext.startActivity(new Intent(mContext, UserSystemSettings.class));
							break;
						case 1:
							mContext.startActivity(new Intent(mContext, UserAccountManager.class));
							break;
						
						case 2:
							mContext.startActivity(new Intent(mContext, UserAboutDevice.class));
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

	        public void setIconColor(Drawable icon)
	        {
	            //int textColorSecondary = android.R.attr.textColorSecondary;
	        	int textColorSecondary = R.color.gray;
	            TypedValue value = new TypedValue();
	            if (!mContext.getTheme().resolveAttribute(textColorSecondary, value, true))
	            {
	                return;
	            }
	            int baseColor = mContext.getResources().getColor(value.resourceId);
	            icon.setColorFilter(baseColor, PorterDuff.Mode.MULTIPLY);
	        }
	    }

	@Override
	public void dataChanged() {
		// TODO Auto-generated method stub
		callLogCursor.requery();
	}

	protected void dialog() {
    	AlertDialog.Builder builder = new Builder(mContext);
    	builder.setMessage(R.string.sure_to_delete);
    	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				DataHelper.getInstance().DelUserInfo(doorPeerId);
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
}
