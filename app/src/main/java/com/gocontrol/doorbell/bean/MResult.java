/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-18 PM 5:06:03
 * Project: TecomDoor
 * PackageName: com.tecom.door.bean
 */
package com.gocontrol.doorbell.bean;

/**
 * @author Administrator
 * 
 */
public class MResult {

	private int state;
	private String loginToken;

	private MResult() {
	}

	public MResult(int state, String loginToken) {
		this.state = state;
		this.loginToken = loginToken;
	}

	public int getState() {
		return state;
	}

	public String getLoginToken() {
		return loginToken;
	}

	public String getDescription() {
		switch (state) {
		case 200:
			return "Identify verification ok";
		case 400:
			return "Parameter format failed";
		case 403:
			return "Identify verification failed";
		case 404:
			return "No account in DB";
		case 503:
			return "System abnormalities";
		//1. modify state result.  1 + 404.
		case 1404:  return "No account or LoginToken in DB";
    
		}
		return "The state is not defined";
	}
}
