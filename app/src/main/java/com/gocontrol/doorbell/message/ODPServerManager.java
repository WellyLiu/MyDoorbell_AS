package com.gocontrol.doorbell.message;
/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-24 AM6:08:49
 * Project: Cloud_Phone_Demo
 * PackageName: java.tecom.door.message
 */


import java.util.ArrayList;

/**
 * @author Administrator
 *
 */
public class ODPServerManager {

	private static ODPServerManager mInstance;
	private ArrayList<ODPServerInfo> odpList;
	
	private ODPServerManager()
	{
		odpList = new ArrayList<ODPServerInfo>();
	}
	public ODPServerManager getInstance()
	{
		if(mInstance == null)
			mInstance = new ODPServerManager();
		
		return null;
	}
	
	public void addOneODP(ODPServerInfo one)
	{
		odpList.add(one);
	}
}
