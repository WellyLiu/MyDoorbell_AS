/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-8 PM2:20:28
 * Project: TecomDoor
 * PackageName: com.tecom.door.model
 */
package com.gocontrol.doorbell.model;

import java.util.ArrayList;

import android.util.Log;

import com.gocontrol.doorbell.bean.ODPTimer;

/**
 * @author Administrator
 *
 */
public class ODPTimerMannager {

	private static ODPTimerMannager mInstance;

	/**
	 * 
	 */
	private ODPTimerMannager() {
		super();
		// TODO Auto-generated constructor stub
		mTimerList = new ArrayList<ODPTimer>();
	}
	
	public static ODPTimerMannager getInstance()
	{
		if(mInstance ==  null)
			mInstance = new ODPTimerMannager();
		
		return mInstance;
	}
	
	private ArrayList<ODPTimer> mTimerList;
	
	public void addODPTimer(ODPTimer one)
	{
		mTimerList.add(one);
	}
	
	public void removeODPTimer(String odpID)
	{
		for(int i=0; i<mTimerList.size(); i++)
		{
			ODPTimer one = mTimerList.get(i);
			
			if(one != null && one.getOdpID().equals(odpID))
			{
				one.cancel();
				one.purge();
				//Log.d("tst", "==== remove timer:" + odpID );
				mTimerList.remove(i);
			}
		}
		//Log.d("tst", "==== timer number:" + mTimerList.size());
	}

	/**
	 * 
	 */
	public void removeAllODPTimer() {
		// TODO Auto-generated method stub
		for(int i=0; i<mTimerList.size(); i++)
		{
			ODPTimer one = mTimerList.get(i);
			
			if(one != null )
			{
				one.cancel();
				one.purge();
				mTimerList.remove(i);
			}
		}
	}
}
