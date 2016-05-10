package com.gocontrol.doorbell;

import com.gocontrol.doorbell.R;
import com.iptnet.android.web.eac.EACContext;
import com.iptnet.android.web.eac.EACContext.EACException;
import com.iptnet.android.web.eac.ModifyResult;

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

public class WebModifyProfileActivity extends Activity {
	
	private static final String TAG = WebModifyProfileActivity.class.getSimpleName();
	private EditText mDomain, mWebAcc, mWebOldPwd, mWebNewPwd, mName, mToken;
	private RequestTask mRequestTask;
	
	private class RequestTask extends AsyncTask<Void, Void, ModifyResult> implements OnCancelListener {

		private ProgressDialog mProgress;
		
		@Override
		protected void onPreExecute() {
			mProgress = new ProgressDialog(WebModifyProfileActivity.this);
			mProgress.setTitle(null);
			mProgress.setMessage("Modifying ...");
			mProgress.setOnCancelListener(this);
			mProgress.setCancelable(true);
			mProgress.show();
		}
		
		@Override
		protected ModifyResult doInBackground(Void... params) {

			// get sign up data
			String domain = mDomain.getText().toString();
			String account = mWebAcc.getText().toString();
			String oldPwd = mWebOldPwd.getText().toString();
			String newPwd = mWebNewPwd.getText().toString();
			String name = mName.getText().toString();
			String token = mToken.getText().toString();
			
			try {

				String url = getString(R.string.web_eac_url);
				String apiKey = getString(R.string.web_eac_api_key);
				EACContext server = new EACContext(url, apiKey);
				server.showDebugMessage(true);
				return server.modifyProfile(domain, account, oldPwd, newPwd, name, token);
				
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
				Toast.makeText(WebModifyProfileActivity.this, "Modify Error", Toast.LENGTH_SHORT).show();
			
			// get response
			} else {
				int state = result.getState();
				if (state == 200) {
					Toast.makeText(WebModifyProfileActivity.this, "Modify Success", Toast.LENGTH_SHORT).show();
					finish();
					
				} else {
					String msg = "Modify Error\n";
					msg += result.getDescription() + "(" + state + ")";
					Toast.makeText(WebModifyProfileActivity.this, msg, Toast.LENGTH_SHORT).show();	
				}
			}
			
			mRequestTask = null;
		}
		
		@Override
		protected void onCancelled() {
			mProgress.dismiss();
			mRequestTask = null;
			Toast.makeText(WebModifyProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			cancel(true);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_profile);
		getActionBar().setHomeButtonEnabled(true);
		
		// initialize view
		mDomain = (EditText) findViewById(R.id.modify_profile_domain);
		mWebAcc = (EditText) findViewById(R.id.modify_profile_acc);
		mWebOldPwd = (EditText) findViewById(R.id.modify_profile_old_pwd);
		mWebNewPwd = (EditText) findViewById(R.id.modify_profile_new_pwd);
		mName = (EditText) findViewById(R.id.modify_profile_name);
		mToken = (EditText) findViewById(R.id.modify_profile_token);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// read configuration to view
		String[] params = AppUtils.readWebLoginParams(this);
		mDomain.setText(params[0]);
		mWebAcc.setText(params[1]);
		mWebOldPwd.setText(params[2]);
		mToken.setText(params[3]);
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
			if (mDomain.getText().toString().isEmpty()) {
				Toast.makeText(this, "Domain is not allow empty", Toast.LENGTH_SHORT).show();
				return true;
			}
			if (mWebAcc.getText().toString().isEmpty()) {
				Toast.makeText(this, "Account is not allow empty", Toast.LENGTH_SHORT).show();
				return true;
			}
			if (mWebOldPwd.getText().toString().isEmpty()) {
				Toast.makeText(this, "Old password is not allow empty", Toast.LENGTH_SHORT).show();
				return true;
			}
			if (mWebNewPwd.getText().toString().isEmpty()) {
				Toast.makeText(this, "New password is not allow empty", Toast.LENGTH_SHORT).show();
				return true;
			}
			if (mToken.getText().toString().isEmpty()) {
				Toast.makeText(this, "Login Token is not allow empty", Toast.LENGTH_SHORT).show();
				return true;
			}
			
			mRequestTask = new RequestTask();
			mRequestTask.execute();
		}
		
		return super.onOptionsItemSelected(item);
	}
}
