package com.gocontrol.doorbell;
import java.util.Random;

import com.gocontrol.doorbell.ConnectTask.Result;
import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.iptnet.c2c.C2CListener;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {

	private final int MAX_DOOR_COUNT = 4;
	private TextView[] mDoorIds = new TextView[MAX_DOOR_COUNT];
	private TextView mMainInfo;
	private String mRegAccount;
	
	public static boolean sFlag = false;
	
	private C2CListener mC2CListener = new C2CListener() {

		@Override
		protected void receiveMessage(C2CEvent event) {
			if (C2CEvent.C2C_REGISTER_DONE == event) {
				printMessage(mRegAccount, true);
				
				Log.e("Hikari", "home :reg done");
				
			} else if (C2CEvent.C2C_REGISTER_FAIL == event) {
				printMessage(mRegAccount, false);
				
				Log.e("Hikari", "home :reg fail");
			}
		}
	};
	
	private class StartConnect extends AsyncTask<Void, Void, Result> {

		private Door door;
		private ProgressDialog mProgressDialog;
		private int sessionCode;
		
		public StartConnect(Door door) {
			this.door = door;
		}

		private void showFailMsg(String msg) {
			
			// show dialog to show connection fail
			new AlertDialog.Builder(HomeActivity.this)
				.setMessage(msg)
				.setCancelable(false)
				.setPositiveButton("OK", null)
				.show();
		}
		
		private void resumeState() {
			((AppApplication) getApplication()).setManualCall(false, null);
		}
		
		@Override
		protected void onPreExecute() {
			
			// show progress dialog
			mProgressDialog = ProgressDialog.show(HomeActivity.this, null, "Connect ...");
			
			((AppApplication) getApplication()).setManualCall(true, door.getId());
		}
		
		@Override
		protected Result doInBackground(Void... params) {

			// start connection with session code
			sessionCode = new Random().nextInt(1000);
			String tag = "i=" + String.valueOf(sessionCode) + ";";
			return new ConnectTask().startConnection(door.getId(), door.getAccount(), door.getPassword(), tag);
		}

		@Override
		protected void onPostExecute(Result result) {
			
			// dismiss progress dialog
			mProgressDialog.dismiss();		
			
			if (sFlag) {
				sFlag = false;
				return;
			}
			
			// process connection result
			switch (result) {
			
				// success case
				case CONNECTED_P2P:
				case CONNECTED_RELAY:
					startActivity(new Intent(HomeActivity.this, ViewerActivity.class)
						.putExtra("door", door)
						.putExtra("line.id", result.getLineId())
						.putExtra("session.code", sessionCode));
					break;
					
				// fail case
				case FAIL_CALLING:		showFailMsg("Connect Fail");		resumeState(); break;
				case FAIL_NETWORK:		showFailMsg("Connect Fail ( " + result.getLineId() + ")");	resumeState(); break;
				case FAIL_TIMEOUT:		showFailMsg("Connect timeout");		resumeState(); break;
				case FAIL_UNAUTHORIZED:	showFailMsg("Unauthorized");		resumeState(); break;
				case FAIL_SRV_NO_RSP:	showFailMsg("Server no response");	resumeState(); break;
				case FAIL_PEER_OFFLINE:	showFailMsg("Peer offline");		resumeState(); break;
				case FAIL_PEER_NO_RSP:	showFailMsg("Peer no response");	resumeState(); break;
				case FAIL_PEER_BUSY:	showFailMsg("Peer is busy");		resumeState(); break;
			}
		}	
	}
	
	private class ReleaseSdkTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog mProgressDialog;
		
		@Override
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(HomeActivity.this, null, "Release ...");
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
			stopService(new Intent(HomeActivity.this, DoorService.class));
			
			// finish activity
			finish();
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mProgressDialog.dismiss();
		}
	}

	
	private void printMessage(final String agentId, final boolean isRegDone) {
		
		// update text information
		mMainInfo.post(new Runnable() {
			public void run() {
				StringBuilder builder = new StringBuilder();
				String sdkVer = C2CHandle.getInstance().getSDKVersion();
				builder.append("Register Server: " + AppUtils.readC2CLoginParams(HomeActivity.this)[2]);
				builder.append("\nRegister Account: " + (agentId == null ? "[EMPTY]" : agentId));
				builder.append("\nC2C Module Version: " + C2CHandle.VERSION + " (SDK:" + sdkVer + ")");
				if (isRegDone)	builder.append("\nOnline (C2C Register Done)");
				else			builder.append("\nOffline (C2C Register Fail)");
				mMainInfo.setText(builder);
		}});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);	
		
		// initialize view
		mDoorIds[0] = (TextView) findViewById(R.id.text_door_id0);
		mDoorIds[1] = (TextView) findViewById(R.id.text_door_id1);
		mDoorIds[2] = (TextView) findViewById(R.id.text_door_id2);
		mDoorIds[3] = (TextView) findViewById(R.id.text_door_id3);
		mMainInfo = (TextView) findViewById(R.id.main_information);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// get agent id to view from shared preference
		for (int i=0; i<Door.MAX_DOOR_NUM; i++) {
			String doorId = Door.read(this, i).getId();
			if (doorId.isEmpty()) doorId = "[EMPTY]";
			mDoorIds[i].setText(doorId);
		}
		
		// register listener
		C2CHandle.getInstance().addListener(mC2CListener);
		
		// get registration account
		String[] params = AppUtils.readC2CLoginParams(this);
		String acc = params[0];
		String pwd = params[1];
		String srv = params[2];
		
		// show account
		boolean isRegDone = C2CHandle.getInstance().isRegistrationDone();
		printMessage(acc, isRegDone);
		mRegAccount = acc;
		
		// start service to registration
		boolean running = AppUtils.isServiceRunning(this, DoorService.class);
		if (!running && !acc.isEmpty() && !pwd.isEmpty()) {
			startService(new Intent(this, DoorService.class)
				.putExtra("server", srv)
				.putExtra("account", acc)
				.putExtra("password",pwd));
		}

		// get GCM token
		if (AppUtils.readGcmToken(this).isEmpty()) {
	        startService(new Intent(this, GetGcmTokenService.class));
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// unregister listener
		C2CHandle.getInstance().removeListener(mC2CListener);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
		
			// show dialog
			new AlertDialog.Builder(this)
				.setMessage("Sure to quit?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						
						// [2016/04/25] some time to wait service stop 
						new ReleaseSdkTask().execute();

				}})
				.setNegativeButton("No", null)
				.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add("GCM");
		item.setShowAsAction(
			MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// press GCM menu button
		if (item.getTitle().equals("GCM")) {
			String gcmToken = AppUtils.readGcmToken(this);
			new AlertDialog.Builder(this)
				.setMessage(gcmToken)
				.show();
		}
		
		return super.onOptionsItemSelected(item);
	}

	public void onButtonByConnect(View view) {
		
		if (((AppApplication) getApplication()).isLockConnection()) {
			Toast.makeText(this, "Busy, a session is calling", Toast.LENGTH_SHORT).show();
			return;
		}
		
		// get press button index
		int index = Integer.valueOf((String) view.getTag());

		// start connect to door
		Door door = Door.read(this, index);
		if (!door.getId().isEmpty()) {
			new StartConnect(door).execute();
		}
	}
	
	public void onButtonByEdit(View view) {
		
		// get press button index
		final int index = Integer.valueOf((String) view.getTag());
		
		// show dialog
		new AlertDialog.Builder(this)
			.setItems(new String[] {"Manual", "Scan"}, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) {

						// manual
						startActivity(new Intent(HomeActivity.this, DoorEditActivity.class)
							.putExtra("index", index));
						
					} else if (which == 1) {

						// scan
						startActivity(new Intent(HomeActivity.this, ScanActivity.class)
							.putExtra("index", index));
					}
			}})
			.show();
	}
	
	public void onButtonByMedia(View view) {
		
		// get press button index
		final int index = Integer.valueOf((String) view.getTag());
		
		// show dialog
		new AlertDialog.Builder(this)
			.setItems(new String[] {"Pictures", "Clips"}, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) {
						
						// pictures
						startActivity(new Intent(HomeActivity.this, MediaActivity.class)
							.putExtra("index", index)
							.putExtra("type", 0));
						
					} else if (which == 1) {

						// clips
						startActivity(new Intent(HomeActivity.this, MediaActivity.class)
							.putExtra("index", index)
							.putExtra("type", 1));
					}
			}})
			.show();
	}

	public void onButtonByWebLogin(View view) {
		startActivity(new Intent(this, WebLoginEditActivity.class));
	}
	
	public void onButtonByC2CLogin(View view) {
		startActivity(new Intent(this, C2CLoginEditActivity.class));
	}
	
	public void onButtonByRegister(View view) {
		startActivity(new Intent(this, WebRegisterActivity.class));
	}
	
	public void onButtonByVerify(View view) {
		startActivity(new Intent(this, WebVerifyActivity.class));
	}

	public void onButtonByForgot(View view) {
		startActivity(new Intent(this, WebForgotActivity.class));
	}
	
	public void onButtonByModify(View view) {
		
		new AlertDialog.Builder(this).setItems(new String[] {"Modify Password", "Modify Profile"},
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0)
						startActivity(new Intent(HomeActivity.this, WebModifyPasswordActivity.class));				
					else if (which == 1)
						startActivity(new Intent(HomeActivity.this, WebModifyProfileActivity.class));
		}}).show();
	}
}
