/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-7 AM12:11:20
 * Project: TecomDoor
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.gocontrol.doorbell.bean.PNPSuccessEvent;
import com.gocontrol.doorbell.bean.ReceivedODPEvent;
import com.ypy.eventbus.EventBus;

import android.app.Activity;
import android.os.Bundle;

public class PNPBaseActivity extends Activity{

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		EventBus.getDefault().register(this);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
	
	public void onEvent(PNPSuccessEvent event)
	{
		finish();
	}

}
