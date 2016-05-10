/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-7 上午9:22:18
 * Project: TecomDoor
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gocontrol.doorbell.AppApplication;
import com.gocontrol.doorbell.DoorService;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.DoorPhoneListCloseDrawerEvent;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.bean.PNPSuccessEvent;
import com.gocontrol.doorbell.model.SystemConfigManager;
import com.gocontrol.doorbell.service.AppUtilsService;
import com.gocontrol.doorbell.utils.BuildConfig;
import com.gocontrol.doorbell.utils.LogUtils;
import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.ypy.eventbus.EventBus;

import android.app.ActionBar;
import android.app.AlertDialog;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Administrator
 *
 */
public class DoorPhoneAddType extends PNPBaseActivity implements View.OnClickListener{

	private final static int START_LOG_OUT = 1000;
	private final static int LOG_OUT_DONE = 1001;
	private final static int LOG_OUT_ERROR = 1002;
	private final static int POP_UP_SURE_LOG_OUT = 1003;
	private final static int LOG_OUT_TIME_OUT = 1004;
	private static Handler mHandler;
	private Context mContext;
	private ProgressDialog proDialog;
	
	private DrawerLayout mDrawerLayout = null;
	private ListView mLvRightMenu;
	
	private String fromWhere ;
	private Button mAddTypeOne, mAddTypeTwo;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		
		fromWhere = this.getIntent().getStringExtra("where");
		
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar_menu,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		
		TextView txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);		
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(getString(R.string.door_setup));		
		txtTitle.setText(spanBuilder);
		
		ImageView menu = (ImageView)getActionBar().getCustomView().findViewById(R.id.btn_menu);
		menu.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mDrawerLayout != null)
				{
					if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT))
						mDrawerLayout.closeDrawers();
					else
						mDrawerLayout.openDrawer(Gravity.RIGHT);
				}
			}
			
		});		
		
		setContentView(R.layout.doorphone_add_type);
		getWindow().setBackgroundDrawable(null);
		
		mAddTypeOne = (Button)this.findViewById(R.id.door_phone_add_configured);
		mAddTypeTwo = (Button)this.findViewById(R.id.door_phone_add_new);
		mAddTypeOne.setOnClickListener(this);
		mAddTypeTwo.setOnClickListener(this);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mLvRightMenu = (ListView) findViewById(R.id.right_drawer);   
	
		setUpDrawer();
		
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
					
				case POP_UP_SURE_LOG_OUT:
					popupTipDialog();
					break;
				case LOG_OUT_ERROR:
					if(proDialog != null)
						proDialog.dismiss();
					Toast.makeText(mContext, mContext.getString(R.string.log_out_error), Toast.LENGTH_SHORT).show();
					break;
				case LOG_OUT_DONE:
					if(proDialog != null){
						proDialog.dismiss();						
					}
					
					SystemConfigManager.getInstance().setAppAutoLogin(2);
					SystemConfigManager.getInstance().saveAppAutoLogin(mContext);
					if( !TextUtils.isEmpty(fromWhere) && fromWhere.equalsIgnoreCase("DoorPhoneList"))
					{
						///////////////
					}else
					{
						Toast.makeText(mContext, mContext.getString(R.string.log_out_suc), Toast.LENGTH_SHORT).show();
						C2CHandle.getInstance().startRegisterProcess(getString(R.string.def_reg_srv), "", "");;
						startActivity(new Intent(mContext, UserLoginUI.class));
					}
					
					finish();
					break;
				}
			}
			
		};
	}

public void onEvent(Object event){
		
		if(event instanceof DoorPhoneListCloseDrawerEvent)
		{
			Log.d("DoorPhoneAddType", "onEvent...... DoorPhoneListCloseDrawerEvent");
			if(mDrawerLayout != null)
			{
					mDrawerLayout.closeDrawers();
					finish();
			}
		}else if( event instanceof C2CEvent)
		{
			Log.d("DoorPhoneAddType", ((C2CEvent) event).getInfo());
			if(C2CEvent.C2C_SETUP_ERROR == event){
				mHandler.removeMessages(LOG_OUT_TIME_OUT);
				if(SystemConfigManager.getInstance().getLogoutState() == 1)
					mHandler.sendEmptyMessage(LOG_OUT_ERROR);
				SystemConfigManager.getInstance().setLogoutState(2);
			}else if(C2CEvent.C2C_SETUP_DONE == event)
			{
				
				if(SystemConfigManager.getInstance().getLogoutState() == 1)
				{
					mHandler.removeMessages(LOG_OUT_TIME_OUT);
					mHandler.sendEmptyMessage(LOG_OUT_DONE);
					SystemConfigManager.getInstance().setLogoutState(2);
				}
					
			}
		}
		
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
	
	private TextView showAcc;
	private void setUpDrawer()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mLvRightMenu.addHeaderView(inflater.inflate(R.layout.header_just_username, mLvRightMenu, false));
        TextView userAcc = (TextView) mLvRightMenu.findViewById(R.id.id_username);
        showAcc = (TextView) mLvRightMenu.findViewById(R.id.id_showname);
        if(userAcc != null)
        	userAcc.setText(LocalUserInfo.getInstance().getC2cAccount());
        if(showAcc != null)
        	showAcc.setText(LocalUserInfo.getInstance().getLocalName());
        mLvRightMenu.setAdapter(new MenuItemAdapter(this));
    }
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(proDialog != null){
			proDialog.dismiss();						
		}
		
		
		mHandler.removeCallbacksAndMessages(null);
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
		case R.id.btn_back:
			finish();
			break;
		case R.id.door_phone_add_configured:
			startActivity(new Intent(this,DoorPhoneAddNetworkType.class));
			break;
		case R.id.door_phone_add_new:
			startActivity(new Intent(this, WiFiChoose.class));
			break;
		}
	}
	

	private class ReleaseSdkTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog mProgressDialog;
		
		@Override
		protected void onPreExecute() {
			try{
				mProgressDialog = ProgressDialog.show(DoorPhoneAddType.this, null, "");
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
						
			// sleep 3 seconds to released
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// stop the service
			stopService(new Intent(DoorPhoneAddType.this, DoorService.class));
			
			// finish activity
			finish();
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try{
				mProgressDialog.dismiss();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			
			if(mDrawerLayout != null)
			{
				if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT)){
					mDrawerLayout.closeDrawers();
					return true;
				}
				
			}

			if( !TextUtils.isEmpty(fromWhere) && fromWhere.equalsIgnoreCase("DoorPhoneList"))
			{
				return super.onKeyDown(keyCode, event);
			}else
			// show dialog
			new AlertDialog.Builder(this)
					.setMessage(R.string.sure_to_quit)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									//post evnent to PNP UI to finish themself.
									//EventBus.getDefault().post(new PNPSuccessEvent());
									stopService(new Intent(DoorPhoneAddType.this, AppUtilsService.class));

									//stopService(new Intent(DoorPhoneAddType.this, DoorService.class));
									new ReleaseSdkTask().execute();
									
								}
							}).setNegativeButton(R.string.no, null).show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
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

}
