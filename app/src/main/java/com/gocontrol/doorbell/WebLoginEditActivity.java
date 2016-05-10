package com.gocontrol.doorbell;

import com.gocontrol.doorbell.R;
import com.iptnet.android.web.eac.EACContext;
import com.iptnet.android.web.eac.EACContext.EACException;
import com.iptnet.android.web.eac.LoginResult;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class WebLoginEditActivity extends Activity {

	private static final String TAG = WebLoginEditActivity.class.getSimpleName();
	private EditText mDomain, mWebAcc, mWebPwd;
	private RequestTask mRequestTask;
	
	private class RequestTask extends AsyncTask<Void, Void, LoginResult> implements OnCancelListener {

		private ProgressDialog mProgress;
		
		@Override
		protected void onPreExecute() {
			mProgress = new ProgressDialog(WebLoginEditActivity.this);
			mProgress.setTitle(null);
			mProgress.setMessage("Login ...");
			mProgress.setOnCancelListener(this);
			mProgress.setCancelable(true);
			mProgress.show();
		}
		
		@Override
		protected LoginResult doInBackground(Void... params) {

			// get sign up data
			String domain = mDomain.getText().toString();
			String account = mWebAcc.getText().toString();
			String password = mWebPwd.getText().toString();
			
			try {

				String url = getString(R.string.web_eac_url);
				String apiKey = getString(R.string.web_eac_api_key);
				EACContext server = new EACContext(url, apiKey);
				return server.login(domain, account, password);
				
			} catch (EACException e) {
				Log.w(TAG, e.getMessage());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(LoginResult result) {

			mProgress.dismiss();
			
			// occur EACException
			if (result == null) {				
				Toast.makeText(WebLoginEditActivity.this, "Login Error", Toast.LENGTH_SHORT).show();
			
			// get response
			} else {
				int state = result.getState();
				if (state == 200) {
					Toast.makeText(WebLoginEditActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
					String domain = mDomain.getText().toString();
					String account = mWebAcc.getText().toString();
					String password = mWebPwd.getText().toString();
					String token = result.getLoginToken();
					AppUtils.saveWebLoginParams(WebLoginEditActivity.this, domain, account, password, token);
					finish();
					
				} else {
					String msg = "Login Error\n";
					msg += result.getDescription() + "(" + state + ")";
					Toast.makeText(WebLoginEditActivity.this, msg, Toast.LENGTH_SHORT).show();	
				}
			}
			
			mRequestTask = null;
		}
		
		@Override
		protected void onCancelled() {
			mProgress.dismiss();
			mRequestTask = null;
			Toast.makeText(WebLoginEditActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			cancel(true);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_web);
		getActionBar().setHomeButtonEnabled(true);
		
		// initialize view
		mDomain = (EditText) findViewById(R.id.web_domain);
		mWebAcc = (EditText) findViewById(R.id.web_acc);
		mWebPwd = (EditText) findViewById(R.id.web_pwd);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// read configuration to view from shared preference
		String[] params = AppUtils.readWebLoginParams(this);
		mDomain.setText(params[0]);
		mWebAcc.setText(params[1]);
		mWebPwd.setText(params[2]);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// activity prepare not display, stop request task
		if (mRequestTask != null) {
			mRequestTask.cancel(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add("OK");
		item.setShowAsAction(
			MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// press OK menu button
		if (item.getTitle().equals("OK")) {
			
			// check parameter
			if (mDomain.getText().toString().isEmpty()) {
				Toast.makeText(this, "Domain is not allow empty", Toast.LENGTH_SHORT).show();
				return true;
			}
			if (mWebAcc.getText().toString().isEmpty()) {
				Toast.makeText(this, "Account is not allow empty", Toast.LENGTH_SHORT).show();
				return true;
			}
			if (mWebPwd.getText().toString().isEmpty()) {
				Toast.makeText(this, "Password is not allow empty", Toast.LENGTH_SHORT).show();
				return true;
			}
				
			mRequestTask = new RequestTask();
			mRequestTask.execute();
		}
		
		return super.onOptionsItemSelected(item);
	}
}
