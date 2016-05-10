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
import com.iptnet.android.web.eac.EACContext.EACException;
import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.WebRegisterActivity;

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
public class UserRegisterUI extends Activity implements View.OnClickListener{

	private String mDomain;//��Ϊ�̶�ֵ
	private EditText mName;
	private EditText mAccount;
	private EditText mPassword;
	private EditText mConfirmPwd;
	private TextView mRegisterTips;
	
	private RequestTask mRequestTask;
	private Button mCreate;
	
	private class RequestTask extends AsyncTask<Void, Void, SignUpResult> implements OnCancelListener {

		private ProgressDialog mProgress;
		
		@Override
		protected void onPreExecute() {
			mProgress = new ProgressDialog(UserRegisterUI.this);
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
			String account = mAccount.getText().toString();
			account = account.toLowerCase();
			String password = mPassword.getText().toString();
			String name = mName.getText().toString();
			
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
				Toast.makeText(UserRegisterUI.this, R.string.ntut_tip_16, Toast.LENGTH_SHORT).show();
			
			// get response
			} else {
				int state = result.getState();
				
				if (state == 200) {
					Toast.makeText(UserRegisterUI.this, R.string.ntut_tip_13, Toast.LENGTH_SHORT).show();
					String domain = mDomain;
					String account = mAccount.getText().toString();
					String password = mPassword.getText().toString();
					AppUtils.saveWebLoginParams(UserRegisterUI.this, domain, account, password, null);
					
					Intent mIntent = new Intent(UserRegisterUI.this, UserRegisterVerifyUI.class);
					mIntent.putExtra("USER_ACCOUNT", account);
					mIntent.putExtra("USER_PASSWORD", password);
					mIntent.putExtra("USER_NAME", mName.getText().toString());
					startActivity(mIntent);
					finish();
					
				} else {
					String msg = getString(R.string.ntut_tip_18);
					msg += result.getDescription() + "(" + state + ")";
					//Toast.makeText(UserRegisterUI.this, msg, Toast.LENGTH_SHORT).show();	
					switch(state)
					{
					case 400:
						mRegisterTips.setText(R.string.register_tip_2);
						break;
					case 403:
						mRegisterTips.setText(R.string.register_tip_1);
						break;
					case 503:
						mRegisterTips.setText(R.string.register_tip_3);
						break;
					default:
						mRegisterTips.setText(R.string.register_tip_4);
						break;
					}				
				}
			}
			
			mRequestTask = null;
		}
		
		@Override
		protected void onCancelled() {
			mProgress.dismiss();
			mRequestTask = null;
			Toast.makeText(UserRegisterUI.this, R.string.ntut_tip_6, Toast.LENGTH_SHORT).show();
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
		//ColorStateList redColors = ColorStateList.valueOf(getResources().getColor(R.color.btn_hangup_bg_color));
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(getString(R.string.create_account));

		//spanBuilder.setSpan(new TextAppearanceSpan(null, Typeface.BOLD, 0, redColors, null), 5, 10, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		txtTitle.setText(spanBuilder);
		
		setContentView(R.layout.user_register_ui);
		getWindow().setBackgroundDrawable(null);
		
		mName = (EditText)this.findViewById(R.id.user_name_input);
		mAccount = (EditText)this.findViewById(R.id.user_email_input);
		mPassword = (EditText)this.findViewById(R.id.user_pwd_input);
		mConfirmPwd = (EditText)this.findViewById(R.id.user_confirm_pwd_input);
		
		mCreate = (Button)this.findViewById(R.id.create_account);
		mCreate.setOnClickListener(this);
		
		mRegisterTips = (TextView)this.findViewById(R.id.register_tip);
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
			mRequestTask = null;
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
		case R.id.create_account:

			if (mAccount.getText().toString().isEmpty()) {
				Toast.makeText(this, getString(R.string.ntut_tip_20), Toast.LENGTH_SHORT).show();
				return ;
			}
			if (mPassword.getText().toString().isEmpty()) {
				Toast.makeText(this,  getString(R.string.ntut_tip_21), Toast.LENGTH_SHORT).show();
				return ;
			}
			if (mConfirmPwd.getText().toString().isEmpty()) {
				Toast.makeText(this,  getString(R.string.ntut_tip_22), Toast.LENGTH_SHORT).show();
				return ;
			}
			
			if (!mConfirmPwd.getText().toString().equals(mPassword.getText().toString())) {
				Toast.makeText(this,  getString(R.string.ntut_tip_23), Toast.LENGTH_SHORT).show();
				return ;
			}
			
			//clear register tips.
			mRegisterTips.setText("");
			
			mRequestTask = new RequestTask();
			mRequestTask.execute();
			
			break;
		default:
			break;
		}
	}

	
}
