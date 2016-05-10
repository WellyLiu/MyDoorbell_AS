package com.gocontrol.doorbell;

import com.gocontrol.doorbell.R;
import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.iptnet.c2c.C2CListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class DoorEditActivity extends Activity {

	private int index;
	private EditText mDoorId, mDoorAcc, mDoorPwd;
	private int mLineId = Integer.MIN_VALUE;
	private C2CEventListener mC2CEventListener = new C2CEventListener();
	private ProgressDialog mProgressDialog;
	private boolean mClear;
	
	private class C2CEventListener extends C2CListener {

		@Override
		protected void receiveMessage(C2CEvent event) {
			if (mLineId == event.getLine()) {
				if (C2CEvent.C2C_SETUP_DONE == event) {
					if (!mClear) {
					
						// read configuration from view and save configuration to shared preference
						Door.save(DoorEditActivity.this, new Door(index, mDoorId.getText().toString(),
							mDoorAcc.getText().toString(), mDoorPwd.getText().toString(),""));
						
					} else {
						
						// clear configuration
						Door.clear(DoorEditActivity.this, index);
					}
					
					// close activity
					mProgressDialog.dismiss();
					finish();
					
				} else if (C2CEvent.C2C_SETUP_ERROR == event) {
					
					// dismiss dialog, show toast
					mProgressDialog.dismiss();
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(DoorEditActivity.this, "Setting notification fail", Toast.LENGTH_SHORT).show();		
					}});
				}
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_door);
		getActionBar().setHomeButtonEnabled(true);
		
		// initialize view
		mDoorId = (EditText) findViewById(R.id.edit_door_id);
		mDoorAcc = (EditText) findViewById(R.id.edit_door_acc);
		mDoorPwd = (EditText) findViewById(R.id.edit_door_pwd);

		// get index from previous actvitiy
		index = getIntent().getIntExtra("index", 0);
		
		// register C2C listener
		C2CHandle.getInstance().addListener(mC2CEventListener);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// read configuration to view from shared preference
		Door door = Door.read(this, index);
		mDoorId.setText(door.getId());
		mDoorAcc.setText(door.getAccount());
		mDoorPwd.setText(door.getPassword());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// unregister C2C listener
		C2CHandle.getInstance().removeListener(mC2CEventListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_edit_ok: {
				
				// get gcm token
				String token;
				if ((token = AppUtils.readGcmToken(this)).isEmpty()) {
					Toast.makeText(this, "Could not to get GCM token", Toast.LENGTH_SHORT).show();
					return true;
				}

				// show progress dialog
				mProgressDialog = ProgressDialog.show(this, null, "Setting notification ...");
				
				// setting notification
				mClear = false;
				mLineId = C2CHandle.getInstance().setNotification(token, mDoorId.getText().toString(), 8);
				if (mLineId < 0) {
					mProgressDialog.dismiss();
					mLineId = Integer.MIN_VALUE;
					Toast.makeText(this, "Setting notification (" + mLineId + ")", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
			
			case R.id.menu_edit_clear:
				
				// show progress dialog
				mProgressDialog = ProgressDialog.show(this, null, "Clear notification ...");
				
				// setting notification
				mClear = true;
				mLineId = C2CHandle.getInstance().setNotification("", mDoorId.getText().toString(), 0);
				if (mLineId < 0) {
					mProgressDialog.dismiss();
					mLineId = Integer.MIN_VALUE;
					Toast.makeText(this, "Setting notification (" + mLineId + ")", Toast.LENGTH_SHORT).show();
				}
				return true;
			
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
