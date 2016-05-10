/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-10-21 AM11:56:25
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.model
 */
package com.gocontrol.doorbell.model;

/**
 * @author Administrator
 *
 */
public class Account {

	/**
	 * 
	 */
	public Account() {
		// TODO Auto-generated constructor stub
	}

	private String userAccount;
	private String userPassword;
	private String doorAccout;
	private String doorPassword;
	private String doorID;
	
	
	/**
	 * @return the userAccount
	 */
	public String getUserAccount() {
		return userAccount;
	}
	/**
	 * @return the userPassword
	 */
	public String getUserPassword() {
		return userPassword;
	}
	/**
	 * @return the doorAccout
	 */
	public String getDoorAccout() {
		return doorAccout;
	}
	/**
	 * @return the doorPassword
	 */
	public String getDoorPassword() {
		return doorPassword;
	}
	/**
	 * @return the doorID
	 */
	public String getDoorID() {
		return doorID;
	}
	/**
	 * @param userAccount the userAccount to set
	 */
	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
	/**
	 * @param userPassword the userPassword to set
	 */
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
	/**
	 * @param doorAccout the doorAccout to set
	 */
	public void setDoorAccout(String doorAccout) {
		this.doorAccout = doorAccout;
	}
	/**
	 * @param doorPassword the doorPassword to set
	 */
	public void setDoorPassword(String doorPassword) {
		this.doorPassword = doorPassword;
	}
	/**
	 * @param doorID the doorID to set
	 */
	public void setDoorID(String doorID) {
		this.doorID = doorID;
	}
	
}
