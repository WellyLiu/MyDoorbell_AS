/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-1 AM9:31:01
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.iptnet.android.web.eac.EACContext;
import com.iptnet.android.web.eac.SignUpResult;
import com.iptnet.android.web.eac.VerifyResult;
import com.iptnet.android.web.eac.EACContext.EACException;
import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.LocalUserInfo;
import com.gocontrol.doorbell.utils.Utils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
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
public class UserRegisterVerifyUI extends Activity implements View.OnClickListener{

	private final static String TAG = "UserRegisterVerifyUI";
	private String mDomain;//
	private EditText mVerifyCode;
	
	private Button mActivate, mResetCode;
	
	private RequestTask mRequestTask;
	
	private RequestResendTask mResendTask;
	
	private String mAccount, mPwd, mName;
	
	private class RequestResendTask extends AsyncTask<Void, Void, SignUpResult> implements OnCancelListener {

		private ProgressDialog mProgress;
		
		@Override
		protected void onPreExecute() {
			mProgress = new ProgressDialog(UserRegisterVerifyUI.this);
			mProgress.setTitle(null);
			mProgress.setMessage(getString(R.string.ntut_tip_15));
			mProgress.setOnCancelListener(this);
			mProgress.setCancelable(true);
			mProgress.show();
		}
		
		@Override
		protected SignUpResult doInBackground(Void... params) {

			// get sign up data
			String domain = mDomain;
			String account = mAccount;
			String password = mPwd;
			String name = mName;
			
			try {

				String url = getString(R.string.web_eac_url);
				String apiKey = getString(R.string.web_eac_api_key);
				EACContext server = new EACContext(url, apiKey);
				return server.signUp(domain, account, password, name);
				
			} catch (EACException e) {
				Log.w("Tecom", e.getMessage());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(SignUpResult result) {

			mProgress.dismiss();
			
			// occur EACException
			if (result == null) {				
				Toast.makeText(UserRegisterVerifyUI.this, R.string.ntut_tip_16, Toast.LENGTH_SHORT).show();
			
			// get response
			} else {
				int state = result.getState();
				if (state == 200) {
					Toast.makeText(UserRegisterVerifyUI.this, R.string.ntut_tip_13, Toast.LENGTH_SHORT).show();
					String domain = mDomain;
					String account = mAccount;
					String password = mPwd;
					AppUtils.saveWebLoginParams(UserRegisterVerifyUI.this, domain, account, password, null);
					
				} else {
					String msg = getString(R.string.ntut_tip_18);
					msg += result.getDescription() /*+ "(" + state + ")"*/;
					Toast.makeText(UserRegisterVerifyUI.this, msg, Toast.LENGTH_SHORT).show();	
				}
			}
			
			mRequestTask = null;
		}
		
		@Override
		protected void onCancelled() {
			mProgress.dismiss();
			mRequestTask = null;
			Toast.makeText(UserRegisterVerifyUI.this, R.string.ntut_tip_6, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			cancel(true);
		}
	}
	
	private class RequestTask extends AsyncTask<Void, Void, VerifyResult> implements OnCancelListener {

		private ProgressDialog mProgress;
		
		@Override
		protected void onPreExecute() {
			mProgress = new ProgressDialog(UserRegisterVerifyUI.this);
			mProgress.setTitle(null);
			mProgress.setMessage(getString(R.string.ntut_tip_7));
			mProgress.setOnCancelListener(this);
			mProgress.setCancelable(true);
			mProgress.show();
		}
		
		@Override
		protected VerifyResult doInBackground(Void... params) {

			// get sign up data
			String param[] = AppUtils.readWebLoginParams(UserRegisterVerifyUI.this);
			String domain = param[0];
			String account = param[1];
			String password = param[2];
			String code = mVerifyCode.getText().toString();
			Log.d(TAG, domain + " " + account + " " + password + " " + code + " ");
			try {

				String url = getString(R.string.web_eac_url);
				String apiKey = getString(R.string.web_eac_api_key);
				EACContext server = new EACContext(url, apiKey);
				return server.verify(domain, account, password, code);
				
			} catch (EACException e) {
				Log.w(TAG, e.getMessage());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(VerifyResult result) {

			mProgress.dismiss();
			
			// occur EACException
			if (result == null) {				
				Toast.makeText(UserRegisterVerifyUI.this, R.string.ntut_tip_8, Toast.LENGTH_SHORT).show();
			
			// get response
			} else {
				int state = result.getState();
				
				if (state == 200) {
					//LocalUserInfo.getInstance().setLocalName(mName);
					Utils.saveC2CLoginName(UserRegisterVerifyUI.this, mName);
					Toast.makeText(UserRegisterVerifyUI.this, R.string.ntut_tip_9, Toast.LENGTH_SHORT).show();
					
					Intent mIntent = new Intent(UserRegisterVerifyUI.this, UserLoginUI.class);
					mIntent.putExtra("FROME_WHERE", "UserRegisterVerifyUI");
					mIntent.putExtra("USER_ACCOUNT", mAccount);
					mIntent.putExtra("USER_PASSWORD", mPwd);
					mIntent.putExtra("USER_NAME", mName);
					startActivity(mIntent);
					
					finish();
					
				} else {
					String msg = getString(R.string.ntut_tip_10);
					msg += result.getDescription() + "(" + state + ")";
					Toast.makeText(UserRegisterVerifyUI.this, msg, Toast.LENGTH_SHORT).show();	
				}
			}
			
			mRequestTask = null;
		}
		
		@Override
		protected void onCancelled() {
			mProgress.dismiss();
			mRequestTask = null;
			Toast.makeText(UserRegisterVerifyUI.this, R.string.ntut_tip_6, Toast.LENGTH_SHORT).show();
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
		
		Intent preIntent = getIntent();
		mAccount = preIntent.getStringExtra("USER_ACCOUNT");
		mPwd = preIntent.getStringExtra("USER_PASSWORD");
		mName = preIntent.getStringExtra("USER_NAME");;
		
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
		//ColorStateList redColors = ColorStateList.valueOf(getResources().getColor(R.color.btn_hangup_bg_color));
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(getString(R.string.register_activate));

		//spanBuilder.setSpan(new TextAppearanceSpan(null, Typeface.BOLD, 0, redColors, null), 5, 10, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		txtTitle.setText(spanBuilder);
		
		setContentView(R.layout.user_register_verify_ui);
		getWindow().setBackgroundDrawable(null);
		
		mVerifyCode = (EditText)this.findViewById(R.id.user_verify_code_input);
		
		mActivate = (Button)this.findViewById(R.id.activie_account);
		mResetCode = (Button)this.findViewById(R.id.reset_code);
		mActivate.setOnClickListener(this);
		mResetCode.setOnClickListener(this);
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

		// activity prepare not display, stop request task
		if (mRequestTask != null) {
			mRequestTask.cancel(true);
		}
		
		if (mResendTask != null) {
			mResendTask.cancel(true);
			mResendTask = null;
		}
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

			if (mVerifyCode.getText().toString().isEmpty()) {
				Toast.makeText(this, getString(R.string.ntut_tip_19), Toast.LENGTH_SHORT).show();
				return ;
			}
			
			mRequestTask = new RequestTask();
			mRequestTask.execute();
			
			break;
		case R.id.reset_code:
			
			mResendTask = new RequestResendTask();
			mResendTask.execute();
			
			break;
		default:
			break;
		}
	}

	
}
