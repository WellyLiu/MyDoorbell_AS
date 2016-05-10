/**
 * 
 */
package com.gocontrol.doorbell.model;

import java.util.ArrayList;

import com.gocontrol.doorbell.AppUtils;
import com.gocontrol.doorbell.ui.v7.WiFiSendSSID;
import com.iptnet.c2c.C2CHandle;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


/**
 * @author Administrator
 *
 */
public class ODPManager {

	private static ODPManager mODPManager;
	private int registerGCMStatus;
	
	private ArrayList<ODPInfo> odpList;
	private ODPManager()
	{
		odpList = new ArrayList<ODPInfo>();
	}
	
	public static ODPManager getInstance()
	{
		if(mODPManager == null)
			mODPManager = new ODPManager();
		return mODPManager;
	}
	
	public ArrayList<ODPInfo> getODPList()
	{
		return odpList;
	}
	
	public void addOneODP(ODPInfo one)
	{
		if(one != null){
			odpList.add(one);
		}else
		{
			Log.d("tecom", "addOneODP... ODPBean null...");
		}
	}
	

	public ODPInfo getOneODP(String peerID)
	{
		for(ODPInfo one : odpList)
		{
			if(one.getOdpAccount().equals(peerID))
				return one;
		}
		return null;
	}
	
	public ODPInfo getOneODP(int id)
	{
		return odpList.get(id);
	}
	
	public int getODPNum()
	{
		int number = 0;
		if(odpList != null)
		{
			return odpList.size();
		}
		
		return number;
	}

	/*
	 * ��������ODP��״̬
	 */
	public void resetODPStatus()
	{
		int len = odpList.size();
		for(int i=0; i<len; i++)
		{
			ODPInfo odpInfo = odpList.get(i);
			if(odpInfo != null)
			{
				odpInfo.setOdpState(2);
			}
		}
	}
	/**
	 * ע��ODP list�е�ODP��GCM Server
	 */
	public int registerAllODP(Context mContext) {
		// TODO Auto-generated method stub
		int ret = 1;
		for(ODPInfo odp : odpList)
		{
			if( odp != null)
			{
				int tmp = registerODP(mContext,  odp.getOdpAccount()) ;
				if(tmp == -1)
					ret = tmp;
			}
		}
		setRegisterGCMStatus(ret);
		return ret;
	}
	/**
	 * ע��ODP list�е�ODP��GCM Server
	 */
	public void unRegisterAllODP(Context mContext) {
		// TODO Auto-generated method stub
		for(ODPInfo odp : odpList)
		{
			if( odp != null)
			{
				unRegisterODP(mContext,  odp.getOdpAccount()) ;
			}
		}
	}
	
	
	
	/**
	 * ע��ODP��GCM Server��ע��
	 * @param mContext
	 * @param odpAccount
	 */
	public int unRegisterODP(Context mContext, String odpAccount) {
		// TODO Auto-generated method stub
		int ret = -1;
		String token;
		if ((token = AppUtils.readGcmToken(mContext)).isEmpty()) {
			
			Log.d(this.getClass().getSimpleName(), "unRegisterODP ... " + odpAccount + "  could not get the tocke!");
			return -1;
		}
		
		
		int mLineId = C2CHandle.getInstance().setNotification(token, odpAccount, 0);
		ret = mLineId;
		Log.d(this.getClass().getSimpleName(), "line id:" + mLineId);
		if (mLineId < 0) {			
			Log.d(this.getClass().getSimpleName(), "unRegisterODP to GCM failed...");
		}else{
			Log.d(this.getClass().getSimpleName(), "unRegisterODP to GCM ok...");
		}
		return ret;
	}

	/*
	 * ע��ĳ��ODP��GCM Server
	 */	
	public int registerODP(Context mContext, String odpAccount) {
		// TODO Auto-generated method stub
		// get gcm token
		String token;
		if ((token = AppUtils.readGcmToken(mContext)).isEmpty()) {
			
			Log.d(this.getClass().getSimpleName(), "registerODP ... " + odpAccount + "  could not get the tocke!");
			return 0;
		}
		
		
		int mLineId = C2CHandle.getInstance().setNotification(token, odpAccount, 8);
		Log.d(this.getClass().getSimpleName(), "line id:" + mLineId);
		if (mLineId < 0) {			
			Log.d(this.getClass().getSimpleName(), "register to GCM failed...");
			return -1;
		}else{
			Log.d(this.getClass().getSimpleName(), "register to GCM ok...");
			return 1;
		}
		
		
	}

	/**
	 * @return the registerGCMStatus
	 */
	public int getRegisterGCMStatus() {
		return registerGCMStatus;
	}

	/**
	 * @param registerGCMStatus the registerGCMStatus to set
	 */
	public void setRegisterGCMStatus(int mRegisterGCMStatus) {
		this.registerGCMStatus = mRegisterGCMStatus;
	}
}
