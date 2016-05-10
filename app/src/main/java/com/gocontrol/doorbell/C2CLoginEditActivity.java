package com.gocontrol.doorbell;

import com.gocontrol.doorbell.R;
import com.iptnet.c2c.C2CHandle;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class C2CLoginEditActivity extends Activity {

	private EditText mAgentAcc, mAgentPwd, mAgentSrv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_c2c);
		getActionBar().setHomeButtonEnabled(true);
		
		// initialize view
		mAgentAcc = (EditText) findViewById(R.id.agent_acc);
		mAgentPwd = (EditText) findViewById(R.id.agent_pwd);
		mAgentSrv = (EditText) findViewById(R.id.agent_srv);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// read configuration to view from shared preference
		String[] params = AppUtils.readC2CLoginParams(this);
		mAgentAcc.setText(params[0]);
		mAgentPwd.setText(params[1]);
		mAgentSrv.setText(params[2].equals("") ?
			getString(R.string.def_reg_srv):params[2]);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_agent, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_agent_ok:
				
				// read configuration from view
				String acc = mAgentAcc.getText().toString();
				String pwd = mAgentPwd.getText().toString();
				String srv = mAgentSrv.getText().toString();
				
				// save configuration to shared preference
				AppUtils.saveC2CLoginParams(this, acc, pwd, srv);

				// start register process
				acc = AppUtils.processWebLoginAccount(acc, srv);
				C2CHandle.getInstance().startRegisterProcess(srv, acc, pwd);
				
				// close activity
				finish();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
