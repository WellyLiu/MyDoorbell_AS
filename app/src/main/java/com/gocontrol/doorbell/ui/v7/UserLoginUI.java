/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-1 上午9:28:07
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import java.lang.ref.WeakReference;

import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.iptnet.c2c.C2CListener;
import com.iptnet.c2c.C2CSubEvent;
import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.Door;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.bean.PNPSuccessEvent;
import com.gocontrol.doorbell.datasource.DataHelper;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.model.SystemConfigManager;
import com.gocontrol.doorbell.service.AppUtilsService;
import com.gocontrol.doorbell.utils.BuildConfig;
import com.gocontrol.doorbell.utils.Utils;
import com.ypy.eventbus.EventBus;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Administrator
 * 
 */
public class UserLoginUI extends PNPBaseActivity implements View.OnClickListener {

	private final static int C2C_REG_OK = 1000;
	private final static int C2C_REG_FAILED = 1001;
	private final static int C2C_GO_DOORLIST = 2000;
	
	private  boolean showDialog = false;
	
	public static String acc, acpwd;
	
	private TextView mForgotPwd;
	private Button mLogin, mCreateAccount;

	private ProcessHandler mHandler;
	ProgressDialog proDialog;
	AlertDialog okDialog;

	private EditText mWebAcc, mWebPwd;	
	String mDomain;
	
	private Context mContext;
	private int RegFailedTime;
	
	private C2CListener mC2CListener = new C2CListener() {

		@Override
		protected void receiveMessage(C2CEvent event) {
			if (C2CEvent.C2C_REGISTER_DONE == event) {

				mHandler.sendEmptyMessage(C2C_REG_OK);			
				
				Log.e("Hikari", "home :reg done");
				
			} else if (C2CEvent.C2C_REGISTER_FAIL == event) {
				
				RegFailedTime ++ ;
				if( RegFailedTime == 1)
				{
					Log.e("Tecom", "ignore the first reg fail");
					return;
				}				
				//只有在以下两种情况才提示失败
				if(event.getSubEvent() == C2CSubEvent.C2C_INVALID_ACCOUNT
						|| event.getSubEvent() == C2CSubEvent.C2C_UNAUTHORIZED
						|| event.getSubEvent() == C2CSubEvent.C2C_SRV_NO_RESP)
				{					
					if(okDialog == null || !okDialog.isShowing())						
						if(showDialog){							
							Message msg = mHandler.obtainMessage();
							msg.what = C2C_REG_FAILED;
							msg.arg1 = event.getSubEvent().ordinal();
							mHandler.sendMessage(msg);	
							showDialog = false;							
						}					
				}				
				Log.e("Hikari", "home :reg fail");
			}
		}
	};
	
	private String registerAcc;
	private String registerPwd;
	private String registerName;
	private String fromeWhichIntent;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		
		Intent fromIntent = this.getIntent();
		if(fromIntent != null)
		{
			fromeWhichIntent = fromIntent.getStringExtra("FROME_WHERE");
			registerAcc = fromIntent.getStringExtra("USER_ACCOUNT");
			registerPwd = fromIntent.getStringExtra("USER_PASSWORD");
			registerName = fromIntent.getStringExtra("USER_NAME");
		}
		
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);

		getActionBar().getCustomView().findViewById(R.id.btn_back).setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(mContext , SplashActivity.class));
				finish();
			}
			
		});
				

		TextView txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		//ColorStateList redColors = ColorStateList.valueOf(getResources().getColor(R.color.btn_hangup_bg_color));
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder("Log In");
		//style 为0 即是正常的，还有Typeface.BOLD(粗体) Typeface.ITALIC(斜体)等
		//size  为0 即采用原始的正常的 size大小 
		//spanBuilder.setSpan(new TextAppearanceSpan(null, Typeface.BOLD, 0, redColors, null), 5, 10, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		txtTitle.setText(spanBuilder);
		
		setContentView(R.layout.user_login_ui);
		getWindow().setBackgroundDrawable(null);
		
		mWebAcc = (EditText) findViewById(R.id.user_account_input);
		mWebPwd = (EditText) findViewById(R.id.user_pwd_input);
		//mName = (EditText) findViewById(R.id.reg_name);
		mDomain = getString(R.string.def_reg_domain);
		
		mForgotPwd = (TextView) this.findViewById(R.id.forget_pwd_txt);
		mForgotPwd.setOnClickListener(this);
		mCreateAccount = (Button) this.findViewById(R.id.create_account_txt);
		mCreateAccount.setOnClickListener(this);

		mForgotPwd.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
		mForgotPwd.getPaint().setAntiAlias(true);// 抗锯齿

		mLogin = (Button) this.findViewById(R.id.login_ok);
		mLogin.setOnClickListener(this);
		
		// register listener
		C2CHandle.getInstance().addListener(mC2CListener);
		
		mHandler = new ProcessHandler(this);
	}

	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(okDialog != null)
			okDialog.dismiss();
		
		mWebAcc.setText(LocalUserInfo.getInstance().getC2cAccount());
		mWebPwd.setText(LocalUserInfo.getInstance().getC2cPassword());
		
		if(!TextUtils.isEmpty(fromeWhichIntent) && 
				(fromeWhichIntent.equalsIgnoreCase("UserRegisterVerifyUI") ||fromeWhichIntent.equalsIgnoreCase("UserForgetPwdVerifyUI") ))
		{
			mWebAcc.setText(registerAcc);
			mWebPwd.setText(registerPwd);
		}
		
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mHandler.removeCallbacksAndMessages(null);
		
		if(okDialog != null)
		{
			okDialog.dismiss();
		}
		if(proDialog != null)
		{
			proDialog.dismiss();
		}
		
		super.onDestroy();
		
		// unregister listener
		C2CHandle.getInstance().removeListener(mC2CListener);
		
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
		case R.id.forget_pwd_txt:
			startActivity(new Intent(this, UserForgetPwdUI.class));
			break;
		case R.id.create_account_txt:
			startActivity(new Intent(this, UserRegisterUI.class));
			break;
		case R.id.login_ok:
			
			//String c2cAccount = AppUtils.readC2CLoginParams(this)[0];
			//String c2cPwd = AppUtils.readC2CLoginParams(this)[1];
			String c2cAccount = LocalUserInfo.getInstance().getC2cAccount();
			String c2cPwd =	LocalUserInfo.getInstance().getC2cPassword();
			
			// 获得Email，Password，发送C2C Command给北科大云去Login，并等待Login结果。
			if (mWebAcc.getText().toString().isEmpty()) {
				Toast.makeText(this,  getString(R.string.ntut_tip_20), Toast.LENGTH_SHORT).show();
				break;
			}
			if (mWebPwd.getText().toString().isEmpty()) {
				Toast.makeText(this,  getString(R.string.ntut_tip_21), Toast.LENGTH_SHORT).show();
				break;
			}
			
			showDialog = true;			
			RegFailedTime = 0;
			
			String acc = mWebAcc.getText().toString();
			acc = acc.toLowerCase();
			String pwd = mWebPwd.getText().toString();
			String srv = getString(R.string.def_reg_srv);
			
			if(!acc.equalsIgnoreCase(c2cAccount) && SystemConfigManager.getInstance().isAppAutoLogin() == 2)
			{
				popupTipDialog(acc, pwd, srv);
				break;
			}
			
			startAccRegister(acc, pwd, srv);
			
			Intent mIntent = new Intent(UserLoginUI.this, AppUtilsService.class);
			mIntent.putExtra("COMMAND_TYPE", "UPDATE_NTUT_PROCESS");
			startService(mIntent);
			
			
			break;
		default:
			break;
		}
	}

	/**
	 * @param acc2
	 * @param pwd
	 * @param srv
	 */
	private void startAccRegister(String acc, String pwd, String srv) {
		// TODO Auto-generated method stub
		// save configuration to shared preference
		AppUtils.saveC2CLoginParams(this, acc, pwd, srv);
		
		if( !TextUtils.isEmpty(registerName) )
			LocalUserInfo.getInstance().setLocalName(registerName);
		// save configuration the Tecom preference.
		String localName = LocalUserInfo.getInstance().getLocalName();
		if (TextUtils.isEmpty(localName))
			localName = BuildConfig.Name_Local_Default;
		Utils.saveC2CLoginParams(this, acc, pwd, srv, localName);

		Log.d("tst", acc + " " + pwd + "  " + srv + " " + localName);
		// store to the object.
		LocalUserInfo.getInstance().setC2cAccount(acc);
		LocalUserInfo.getInstance().setC2cPassword(pwd);
		LocalUserInfo.getInstance().setC2cServer(srv);
		
		// start register process
		acc = AppUtils.processWebLoginAccount(acc, srv);
		C2CHandle.getInstance().startRegisterProcess(srv, acc, pwd);
		Log.d("tst", "=========srv:" + srv + " ==acc:" + acc + "===pwd:" + pwd);
		// mHandler.sendEmptyMessageDelayed(C2C_REG_FAILED, 10000);

		proDialog = android.app.ProgressDialog.show(this,
				getString(R.string.tecom_process_title),
				getString(R.string.tecom_precess_content));
		proDialog.setCancelable(true);
	}


	protected void popupTipDialog(final String acc2, final String pwd, final String srv) {
    	AlertDialog.Builder builder = new Builder(mContext);
    	builder.setTitle(R.string.sure_to_delete_title);
    	builder.setMessage(R.string.sure_to_delete_user_info);
    	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//删除所有配对信息
				clearAllODPAndUserInfo();
				startAccRegister(acc2, pwd, srv);
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
	protected void clearAllODPAndUserInfo() {
		// TODO Auto-generated method stub
		for(ODPInfo doorInfo : ODPManager.getInstance().getODPList())
		{
			if(doorInfo != null)
			{
				boolean ret = Door.clear(mContext, doorInfo.getOdpIndex());					
			}
		}
		ODPManager.getInstance().getODPList().clear();		
		DataHelper.getInstance().DelUserInfo();
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			
			startActivity(new Intent(mContext , SplashActivity.class));
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void processC2CRegOK()
	{
		mHandler.removeMessages(C2C_REG_FAILED);
		if (proDialog != null) {
			proDialog.dismiss();// 万万不可少这句，否则会程序会卡死。

			if(okDialog != null)
				okDialog.dismiss();
			
			mHandler.sendEmptyMessageDelayed(
					C2C_GO_DOORLIST, 100);
		}
	}
	private void processC2CRegFail(Message msg)
	{
		try{
			if (proDialog != null) {
				proDialog.dismiss();// 万万不可少这句，否则会程序会卡死。

				if(okDialog != null)
					okDialog.dismiss();
				Log.d("tst", "======msg.arg1:" + msg.arg1);
				String content = UserLoginUI.this.getString(R.string.reg_c2c_result_fail);
				if(msg.arg1 == 5){
					content = UserLoginUI.this.getString(R.string.reg_c2c_result_fail_no_resp);
				}
				else
				{
					if(msg.arg1 == 2)
						content = UserLoginUI.this.getString(R.string.reg_c2c_result_fail_un_authorized);
				}
				okDialog = new AlertDialog.Builder(UserLoginUI.this)
						.setMessage(content)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(
											DialogInterface dialog,
											int which) {
										// TODO Auto-generated method
										// stub
										okDialog.dismiss();
										/*c2c login failed, don't go to door phone list page.
										 * 
										mHandler.sendEmptyMessageDelayed(
												C2C_GO_DOORLIST, 100);
												*/
									}

								}).show();
			}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
	}
	
	/**
	 * 
	 */
	private void processGoDoorList() {
		// TODO Auto-generated method stub
		SystemConfigManager.getInstance().setAppAutoLogin(1);
		SystemConfigManager.getInstance().saveAppAutoLogin(this);
		if(proDialog != null)
			proDialog.dismiss();
		if(okDialog != null)
			okDialog.dismiss();
		
		int odpNumber = ODPManager.getInstance().getODPNum();
		if(odpNumber > 0){
			startActivity(new Intent(UserLoginUI.this,
					DoorPhoneList.class));
			//post evnent to PNP UI to finish themself.
			EventBus.getDefault().post(new PNPSuccessEvent());
			//去注册所有的已配对ODP到GCM上
			ODPManager.getInstance().registerAllODP(mContext);
		}
		else{
			startActivity(new Intent(UserLoginUI.this,
					DoorPhoneAddType.class));
			finish();
		}
	}
	
	static class ProcessHandler extends Handler{

		private WeakReference<UserLoginUI> mHostActivity;
		/**
		 * 
		 */
		public ProcessHandler(UserLoginUI mActivity) {
			super();
			// TODO Auto-generated constructor stub
			mHostActivity = new WeakReference<UserLoginUI>(mActivity);
		}

		/* (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			UserLoginUI mActivity = mHostActivity.get();

			// TODO Auto-generated method stub
			System.out.println("Handler... process");
			switch (msg.what) {
			case C2C_REG_OK:				
				mActivity.processC2CRegOK();
				break;
				
			case C2C_REG_FAILED:
				mActivity.processC2CRegFail(msg);
				break;
				
			case C2C_GO_DOORLIST:				
				mActivity.processGoDoorList();				
				break;
				
			default:
				break;
			}
			super.handleMessage(msg);
		
		}
		
	}

	
}
