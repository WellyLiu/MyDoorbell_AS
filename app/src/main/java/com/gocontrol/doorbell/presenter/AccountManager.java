/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-10-21 ����12:03:08
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.presenter
 */
package com.gocontrol.doorbell.presenter;

import com.gocontrol.doorbell.model.Account;

/**
 * @author Administrator
 *
 */
public class AccountManager {

	private static AccountManager mAccountManager; //
	private static Account mCurAccount; //
	/**
	 * 
	 */
	private AccountManager() {
		// TODO Auto-generated constructor stub
	}

	public static AccountManager getAccountManager()
	{
		if(null == mAccountManager)
			mAccountManager = new AccountManager();
		return mAccountManager;
	}
	
	public static Account getCurrentAccount()
	{
		if(mCurAccount == null)
		{
			mCurAccount = new Account();
		}
		return mCurAccount;
	}
	
	public void setAccount(String user, String userPwd, String doorID, String doorAcc, String doorPwd)
	{
		Account mAccount = getCurrentAccount();
		mAccount.setDoorAccout(doorAcc);
		mAccount.setDoorID(doorID);
		mAccount.setDoorPassword(doorPwd);
		mAccount.setUserAccount(user);
		mAccount.setUserPassword(userPwd);
	}
}
