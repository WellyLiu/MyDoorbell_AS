/**
 * 
 */
package com.gocontrol.doorbell.model;

/**
 * @author Administrator
 *
 */
public class ODPInfo {
	
	private String odpAccount;	
	private String odpLocalAccount;
	private String odpLocalPwd;
	private int odpMissedCallNum; //
	//properties for UI
	private String odpName;
	

	private int odpIndex;
	
	private boolean onLine;
	private int odpState;
	
	/**
	 * @return the onLine
	 */
	public boolean isOnLine() {
		return onLine;
	}

	/**
	 * @param onLine the onLine to set
	 */
	public void setOnLine(boolean onLine) {
		this.onLine = onLine;
	}

	public String getOdpAccount() {
		return odpAccount;
	}

	public void setOdpAccount(String odpAccount) {
		this.odpAccount = odpAccount;
	}

	public String getOdpLocalAccount() {
		return odpLocalAccount;
	}

	public void setOdpLocalAccount(String odpLocalAccount) {
		this.odpLocalAccount = odpLocalAccount;
	}

	public String getOdpLocalPwd() {
		return odpLocalPwd;
	}

	public void setOdpLocalPwd(String odpLocalPwd) {
		this.odpLocalPwd = odpLocalPwd;
	}

	public int getOdpIndex() {
		return odpIndex;
	}

	public void setOdpIndex(int odpIndex) {
		this.odpIndex = odpIndex;
	}

	public String getOdpName() {
		return odpName;
	}

	public void setOdpName(String odpName) {
		this.odpName = odpName;
	}

	/**
	 * @return the odpState
	 */
	public int getOdpState() {
		return odpState;
	}

	/**
	 * @param odpState the odpState to set
	 */
	public void setOdpState(int odpState) {
		this.odpState = odpState;
	}

	/**
	 * @return the odpMissedCallNum
	 */
	public int getOdpMissedCallNum() {
		return odpMissedCallNum;
	}

	/**
	 * @param odpMissedCallNum the odpMissedCallNum to set
	 */
	public void setOdpMissedCallNum(int odpMissedCallNum) {
		this.odpMissedCallNum = odpMissedCallNum;
	}
	
}
