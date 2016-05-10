package com.gocontrol.doorbell;

import com.gocontrol.doorbell.R;
import com.iptnet.c2c.C2CEvent;
import com.iptnet.c2c.C2CHandle;
import com.iptnet.c2c.C2CListener;
import com.iptnet.qrscanner.CodeScanner;
import com.iptnet.qrscanner.CodeScanner.ScannerListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

public class ScanActivity extends Activity implements ScannerListener {

	private int index;
	private CodeScanner scanner;
	private int mLineId = Integer.MIN_VALUE;
	private C2CEventListener mC2CEventListener = new C2CEventListener();
	private ProgressDialog mProgressDialog;
	private String mDoorId, mDoorAcc, mDoorPwd;
	
	private class C2CEventListener extends C2CListener {

		@Override
		protected void receiveMessage(C2CEvent event) {
			if (mLineId == event.getLine()) {
				if (C2CEvent.C2C_SETUP_DONE == event) {
				
					// read configuration from view and save configuration to shared preference
					Door.save(ScanActivity.this, new Door(index, mDoorId, mDoorAcc, mDoorPwd,""));
										
					// close activity
					mProgressDialog.dismiss();
					finish();
					
				} else if (C2CEvent.C2C_SETUP_ERROR == event) {
					
					// dismiss dialog, show toast
					mProgressDialog.dismiss();
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(ScanActivity.this, "Setting notification fail", Toast.LENGTH_SHORT).show();		
					}});
				}
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		getActionBar().setHomeButtonEnabled(true);
		
		// get index from Intent
		index = getIntent().getIntExtra("index", 0);
		
		// prepare scanner
		scanner = new CodeScanner(this,
			(ViewGroup) findViewById(R.id.scan_container));
		scanner.setScannerListener(this);
		scanner.setBeepResource(R.raw.beep);
		scanner.setVibrate(true);
		scanner.start();
		
		// register C2C listener
		C2CHandle.getInstance().addListener(mC2CEventListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// stop scanner
		scanner.stop();
		
		// unregister C2C listener
		C2CHandle.getInstance().removeListener(mC2CEventListener);
	}

	@Override
	public void onScanResult(String code) {
		
		scanner.stop();
		
		String[] split = code.split(",");
		
		// check code length
		if (split.length < 3) {
			Toast.makeText(this, "Incorrect format", Toast.LENGTH_SHORT).show();
			scanner.start();
			return;
		}

		// read configuration from code
		mDoorId = split[0];
		mDoorAcc = split[1];
		mDoorPwd = split[2];
		
		// get gcm token
		String token;
		if ((token = AppUtils.readGcmToken(this)).isEmpty()) {
			Toast.makeText(this, "Could not to get GCM token", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// show progress dialog
		mProgressDialog = ProgressDialog.show(this, null, "Setting notification ...");
		
		// setting notification
		mLineId = C2CHandle.getInstance().setNotification(token, mDoorId.toString(), 8);
		if (mLineId < 0) {
			mProgressDialog.dismiss();
			mLineId = Integer.MIN_VALUE;
			Toast.makeText(this, "Setting notification (" + mLineId + ")", Toast.LENGTH_SHORT).show();
		}
	}
}
