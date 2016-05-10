/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-10-21 PM3:18:46
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui
 */
package com.gocontrol.doorbell.ui.v7;


import com.gocontrol.doorbell.DoorService;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.PNPSuccessEvent;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.model.SystemConfigManager;
import com.gocontrol.doorbell.presenter.Utils;
import com.gocontrol.doorbell.service.AppUtilsService;
import com.gocontrol.doorbell.utils.BuildConfig;
import com.ypy.eventbus.EventBus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * @author Administrator
 *
 */
public class SplashActivity extends PNPBaseActivity implements View.OnClickListener{

	private final String TAG = "SplashActivity";
	
	private Button login, toBuy;

	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		
		setContentView(R.layout.splash_activity_v7);	
		getWindow().setBackgroundDrawable(null);
		
		startService(new Intent(SplashActivity.this, AppUtilsService.class));
		
		controlUIProcess();
		
		initUI();
	}

	/**
	 * initial the UI compounds
	 */
	private void initUI() {
		// TODO Auto-generated method stub
		login = (Button)this.findViewById(R.id.log_in);
		toBuy = (Button)this.findViewById(R.id.buy);
		
		login.setOnClickListener(this);
		toBuy.setOnClickListener(this);
	}

	/**
	 * jump to the door phone list activity if user had logged in and odp number is more than 0.
	 * jump to the door phone add activity if user had logged in and odp number is less than or equal 0.
	 */
	private void controlUIProcess() {
		// TODO Auto-generated method stub
		int ret = SystemConfigManager.getInstance().isAppAutoLogin() ;
		Log.d(TAG, "App start mode:" + ret);
		if(ret == 1)
		{
			int odpNumber = ODPManager.getInstance().getODPNum();
			if(odpNumber > 0){
				startActivity(new Intent(this, DoorPhoneList.class));

				ODPManager.getInstance().registerAllODP(this);
				finish();
			}else
			{
				startActivity(new Intent(this, DoorPhoneAddType.class));
				finish();
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.buy:
			Uri uri = Uri.parse(BuildConfig.nortekUri);  
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
            startActivity(intent);  
			break;
		case R.id.log_in:
			Intent mIntent = new Intent(this, UserLoginUI.class);  
            startActivity(mIntent);  
            finish();
			break;
		}
	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {

			// show dialog
			new AlertDialog.Builder(this)
					.setMessage(R.string.sure_to_quit)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									//post evnent to PNP UI to finish themself.
									EventBus.getDefault().post(new PNPSuccessEvent());

									// stop the service
									stopService(new Intent(SplashActivity.this,
											DoorService.class));
									stopService(new Intent(SplashActivity.this,
											AppUtilsService.class));
									// close the activity
									finish();
								}
							}).setNegativeButton(R.string.no, null).show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
