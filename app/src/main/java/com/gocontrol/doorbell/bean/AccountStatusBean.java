/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-7 AM3:10:28
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.bean
 */
package com.gocontrol.doorbell.bean;

/**
 * @author Administrator
 *
 */
public class AccountStatusBean {

	private int status;
	private String name;
	private String eMail; //
	private String showEmail; //
	
	/**
	 * @return the showEmail
	 */
	public String getShowEmail() {
		return showEmail;
	}
	/**
	 * @param showEmail the showEmail to set
	 */
	public void setShowEmail(String showEmail) {
		this.showEmail = showEmail;
	}
	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the eMail
	 */
	public String geteMail() {
		return eMail;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @param eMail the eMail to set
	 */
	public void seteMail(String eMail) {
		this.eMail = eMail;
	}
	
}
