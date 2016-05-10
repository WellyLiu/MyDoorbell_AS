/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-1 AM9:34:07
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.iptnet.android.web.eac.EACContext;
import com.iptnet.android.web.eac.ForgotResult;
import com.iptnet.android.web.eac.EACContext.EACException;
import com.gocontrol.doorbell.AppApplication;
import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.WebForgotActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
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
public class UserForgetPwdUI extends Activity implements View.OnClickListener{

	private String userAccount;
	private RequestTask mRequestTask;
	private EditText mWebAcc;
	private Button mForgetSend;
	private final static String DOMAIN = /*"bronci.iptnet.net"*/AppApplication.getInstance().getString(R.string.def_reg_domain);
	
	private class RequestTask extends AsyncTask<Void, Void, ForgotResult> implements OnCancelListener {

		private ProgressDialog mProgress;
		
		@Override
		protected void onPreExecute() {
			mProgress = new ProgressDialog(UserForgetPwdUI.this);
			mProgress.setTitle(null);
			mProgress.setMessage(getString(R.string.ntut_tip_11));
			mProgress.setOnCancelListener(this);
			mProgress.setCancelable(true);
			mProgress.show();
		}
		
		@Override
		protected ForgotResult doInBackground(Void... params) {

			// get sign up data
			
			String account = mWebAcc.getText().toString();
			account = account.toLowerCase();
			
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
				Toast.makeText(UserForgetPwdUI.this, R.string.ntut_tip_12, Toast.LENGTH_SHORT).show();
			
			// get response
			} else {
				int state = result.getState();
				if (state == 200) {
					Toast.makeText(UserForgetPwdUI.this, R.string.ntut_tip_13, Toast.LENGTH_SHORT).show();
					Intent mIntent = new Intent(UserForgetPwdUI.this, UserForgetPwdVerifyUI.class);
					mIntent.putExtra("USER_EMAIL", userAccount);
					startActivity(mIntent);
					finish();
					
				} else {
					String msg = getString(R.string.ntut_tip_14);
					msg += result.getDescription()/* + "(" + state + ")"*/;
					Toast.makeText(UserForgetPwdUI.this, msg, Toast.LENGTH_SHORT).show();	
				}
			}
			
			mRequestTask = null;
		}
		
		@Override
		protected void onCancelled() {
			mProgress.dismiss();
			mRequestTask = null;
			Toast.makeText(UserForgetPwdUI.this, R.string.ntut_tip_6, Toast.LENGTH_SHORT).show();
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
		
		setContentView(R.layout.user_forget_pwd_ui);
		getWindow().setBackgroundDrawable(null);
		
		mWebAcc = (EditText)this.findViewById(R.id.user_emal_input);
		mForgetSend = (Button)this.findViewById(R.id.forget_account);
		mForgetSend.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// read configuration to view
		String[] params = AppUtils.readWebLoginParams(this);
		
		mWebAcc.setText(params[1]);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// activity prepare not display, stop request task
		if (mRequestTask != null) {
			mRequestTask.cancel(true);
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
		case R.id.forget_account:
			if (mWebAcc.getText().toString().isEmpty()) {
				Toast.makeText(this,  getString(R.string.ntut_tip_20), Toast.LENGTH_SHORT).show();
				return;
			}
			userAccount = mWebAcc.getText().toString();
			mRequestTask = new RequestTask();
			mRequestTask.execute();
			break;
		default:
			break;
		}
	}

	
}
