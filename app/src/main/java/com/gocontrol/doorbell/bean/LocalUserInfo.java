/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-11-30 AM 11:57:51
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.bean
 */
package com.gocontrol.doorbell.bean;

/**
 * @author Administrator
 * C2C Login account, password.
 */
public class LocalUserInfo {

	private static LocalUserInfo mInstance;
	private String localAccount;
	private String localPwd; //default, "admin"
	private String localName; //default, "1234"
	private String c2cAccount;
	private String c2cPassword;
	private String c2cServer; //default "doorphone.tecom.com.tw"
	
	private LocalUserInfo()
	{
		
	}
	public static LocalUserInfo getInstance()
	{
		if(mInstance == null)
			mInstance = new LocalUserInfo();
		
		return mInstance;
	}
	
	/**
	 * @return the c2cServer
	 */
	public String getC2cServer() {
		return c2cServer;
	}
	/**
	 * @param c2cServer the c2cServer to set
	 */
	public void setC2cServer(String c2cServer) {
		this.c2cServer = c2cServer;
	}
	public void updateUserInfo(String c2cAcc, String c2cPwd, String localAcc, String localPwd, 
			String name, String server)
	{
		this.c2cAccount = c2cAcc;
		this.c2cPassword = c2cPwd;
		this.localAccount = localAcc;
		this.localPwd = localPwd;
		this.localName = name;
		this.c2cServer = server;
	}
	
	/**
	 * @return the c2cAccount
	 */
	public String getC2cAccount() {
		return c2cAccount;
	}
	/**
	 * @return the c2cPassword
	 */
	public String getC2cPassword() {
		return c2cPassword;
	}
	/**
	 * @param c2cAccount the c2cAccount to set
	 */
	public void setC2cAccount(String c2cAccount) {
		this.c2cAccount = c2cAccount;
	}
	/**
	 * @param c2cPassword the c2cPassword to set
	 */
	public void setC2cPassword(String c2cPassword) {
		this.c2cPassword = c2cPassword;
	}
	/**
	 * @return the localName
	 */
	public String getLocalName() {
		return localName;
	}
	/**
	 * @return the localAccount
	 */
	public String getLocalAccount() {
		return localAccount;
	}
	/**
	 * @return the localPwd
	 */
	public String getLocalPwd() {
		return localPwd;
	}
	/**
	 * @param localName the localName to set
	 */
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	/**
	 * @param localAccount the localAccount to set
	 */
	public void setLocalAccount(String localAccount) {
		this.localAccount = localAccount;
	}
	/**
	 * @param localPwd the localPwd to set
	 */
	public void setLocalPwd(String localPwd) {
		this.localPwd = localPwd;
	}
	
	
}
