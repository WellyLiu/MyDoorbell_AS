/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-1 AM9:31:01
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.iptnet.android.web.eac.EACContext;
import com.iptnet.android.web.eac.ForgotResult;
import com.iptnet.android.web.eac.ModifyResult;
import com.iptnet.android.web.eac.EACContext.EACException;
import com.gocontrol.doorbell.AppApplication;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.LocalUserInfo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Administrator
 *
 */
public class UserForgetPwdVerifyUI extends Activity implements View.OnClickListener{

	private final static String TAG = "UserForgetPwdVerifyUI";
	private String mDomain;//��Ϊ�̶�ֵ
	private EditText mVerifyCode;
	private EditText pwd, confirmPwd;
	
	private ResendRequestTask mResendRequestTask;
	private Button mActivate, mResendCode;
	private final static String DOMAIN = /*"bronci.iptnet.net"*/AppApplication.getInstance().getString(R.string.def_reg_domain);
	
	private String account, verifycode;
	private String mPwd, mConfirmPwd;
	private VerifyRequestTask mVerifyRequestTask;
	private class VerifyRequestTask extends AsyncTask<Void, Void, ModifyResult> implements OnCancelListener {

		private ProgressDialog mProgress;
		
		@Override
		protected void onPreExecute() {
			mProgress = new ProgressDialog(UserForgetPwdVerifyUI.this);
			mProgress.setTitle(null);
			mProgress.setMessage(getString(R.string.ntut_tip_1));
			mProgress.setOnCancelListener(this);
			mProgress.setCancelable(true);
			mProgress.show();
		}
		
		@Override
		protected ModifyResult doInBackground(Void... params) {

			// get sign up data
			String mDomain = AppApplication.getInstance().getString(R.string.def_reg_domain);
			String mAccount = account;
			String mCode = verifycode;
			String newPwd = mPwd;
			
			try {

				String url = getString(R.string.web_eac_url);
				String apiKey = getString(R.string.web_eac_api_key);
				EACContext server = new EACContext(url, apiKey);
				server.showDebugMessage(true);
				return server.modifyPwd(mDomain, mAccount, mCode, newPwd);
				
			} catch (EACException e) {
				Log.w(TAG, e.getMessage());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(ModifyResult result) {

			mProgress.dismiss();
			
			// occur EACException
			if (result == null) {				
				Toast.makeText(UserForgetPwdVerifyUI.this, R.string.ntut_tip_2, Toast.LENGTH_SHORT).show();
			
			// get response
			} else {
				int state = result.getState();
				if (state == 200) {
					LocalUserInfo.getInstance().setC2cPassword(mPwd);
					Toast.makeText(UserForgetPwdVerifyUI.this, R.string.ntut_tip_3, Toast.LENGTH_SHORT).show();
					Intent mIntent = new Intent(UserForgetPwdVerifyUI.this, UserLoginUI.class);
					mIntent.putExtra("FROME_WHERE", "UserForgetPwdVerifyUI");
					mIntent.putExtra("USER_ACCOUNT", account);
					mIntent.putExtra("USER_PASSWORD", mPwd);
					
					startActivity(mIntent);
					finish();
					
				} else {
					String msg = getString(R.string.ntut_tip_5);
					msg += result.getDescription() + "(" + state + ")";
					Toast.makeText(UserForgetPwdVerifyUI.this, msg, Toast.LENGTH_SHORT).show();	
				}
			}
			
			mVerifyRequestTask = null;
		}
		
		@Override
		protected void onCancelled() {
			mProgress.dismiss();
			mVerifyRequestTask = null;
			Toast.makeText(UserForgetPwdVerifyUI.this, R.string.ntut_tip_6, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			cancel(true);
		}
	}
	
	
	private class ResendRequestTask extends AsyncTask<Void, Void, ForgotResult> implements OnCancelListener {

		private ProgressDialog mProgress;
		
		@Override
		protected void onPreExecute() {
			mProgress = new ProgressDialog(UserForgetPwdVerifyUI.this);
			mProgress.setTitle(null);
			mProgress.setMessage(getString(R.string.ntut_tip_11));
			mProgress.setOnCancelListener(this);
			mProgress.setCancelable(true);
			mProgress.show();
		}
		
		@Override
		protected ForgotResult doInBackground(Void... params) {

			// get sign up data
						
			try {

				String url = getString(R.string.web_eac_url);
				String apiKey = getString(R.string.web_eac_api_key);
				EACContext server = new EACContext(url, apiKey);
				return server.forgot(DOMAIN, account);
				
			} catch (EACException e) {
				Log.w("Tecom", e.getMessage());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(ForgotResult result) {

			mProgress.dismiss();
			
			// occur EACException
			if (result == null) {				
				Toast.makeText(UserForgetPwdVerifyUI.this, R.string.ntut_tip_12, Toast.LENGTH_SHORT).show();
			
			// get response
			} else {
				int state = result.getState();
				if (state == 200) {
					Toast.makeText(UserForgetPwdVerifyUI.this, R.string.ntut_tip_13, Toast.LENGTH_SHORT).show();
					
				} else {
					String msg = getString(R.string.ntut_tip_14);
					msg += result.getDescription() /*+ "(" + state + ")"*/;
					Toast.makeText(UserForgetPwdVerifyUI.this, msg, Toast.LENGTH_SHORT).show();	
				}
			}
			
			mResendRequestTask = null;
		}
		
		@Override
		protected void onCancelled() {
			mProgress.dismiss();
			mResendRequestTask = null;
			Toast.makeText(UserForgetPwdVerifyUI.this, R.string.ntut_tip_6, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			cancel(true);
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		account = getIntent().getStringExtra("USER_EMAIL");
		
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
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder("Forgot Password");

		//spanBuilder.setSpan(new TextAppearanceSpan(null, Typeface.BOLD, 0, redColors, null), 5, 10, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		txtTitle.setText(spanBuilder);
		
		setContentView(R.layout.user_forget_password_verify_ui);
		getWindow().setBackgroundDrawable(null);
		
		mVerifyCode = (EditText)this.findViewById(R.id.user_verify_code_input);
		
		mActivate = (Button)this.findViewById(R.id.activie_account);
		mResendCode = (Button)this.findViewById(R.id.reset_code);
		mActivate.setOnClickListener(this);
		mResendCode.setOnClickListener(this);
		pwd = (EditText)this.findViewById(R.id.pwd_input);
		confirmPwd = (EditText)this.findViewById(R.id.pwd_confirm_input);
	}

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		mDomain = getString(R.string.def_reg_domain);
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
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
		case R.id.btn_back:
			finish();
			break;
		case R.id.activie_account:

			verifycode = mVerifyCode.getText().toString();
			if (mVerifyCode.getText().toString().isEmpty()) {
				Toast.makeText(this,  getString(R.string.ntut_tip_19), Toast.LENGTH_SHORT).show();
				return ;
			}
			mPwd = pwd.getText().toString();
			if (mPwd.isEmpty()) {
				Toast.makeText(this,  getString(R.string.ntut_tip_21), Toast.LENGTH_SHORT).show();
				return ;
			}
			mConfirmPwd = confirmPwd.getText().toString();	
			if (mConfirmPwd.isEmpty()) {
				Toast.makeText(this,  getString(R.string.ntut_tip_22), Toast.LENGTH_SHORT).show();
				return ;
			}
			if(!mPwd.equals(mConfirmPwd))
			{
				Toast.makeText(this, getString(R.string.password_mis_match), Toast.LENGTH_SHORT).show();
				return;
			}
			mVerifyRequestTask = new VerifyRequestTask();
			mVerifyRequestTask.execute();
			
			break;
		case R.id.reset_code:
			
			mResendRequestTask = new ResendRequestTask();
			mResendRequestTask.execute();
			break;
		default:
			break;
		}
	}

	
}
