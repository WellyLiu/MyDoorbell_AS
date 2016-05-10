package com.gocontrol.doorbell;

import java.io.Serializable;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class Door implements Serializable {

	private static final long serialVersionUID = -274825762670463237L;
	private int index;
	private String id, account, password;
	//tecom odp name
	private String odpName;
	
	public static final int MAX_DOOR_NUM = 4;
	
	private static SharedPreferences getPreferences(Context context, int index) {
		return context.getSharedPreferences("door" + index, Context.MODE_PRIVATE);
	}
	
	public static Door read(Context context, int index) {
		if (index < 0 || index >= MAX_DOOR_NUM) return null;
		SharedPreferences prefs = getPreferences(context, index);
		return new Door(index,
						prefs.getString("door.id", ""),
						prefs.getString("door.acc", ""),
						prefs.getString("door.pwd", ""),
						prefs.getString("door.name", ""));
	}
	
	public static Door read(Context context, String id) {
		for (int i=0; i<MAX_DOOR_NUM; i++) {
			Door door;
			if ((door = read(context, i)).id.equalsIgnoreCase(id))
				return door;
		}
		return null;
	}
	
	public static boolean save(Context context, Door door) {
		if (door.index < 0 || door.index >= MAX_DOOR_NUM) return false;
		SharedPreferences prefs = getPreferences(context, door.index);
		
		return prefs.edit()
			.putString("door.id", door.id)
			.putString("door.acc", door.account)
			.putString("door.pwd", door.password)
			.putString("door.name", door.odpName)
			.commit();
	}
	
	public static boolean clear(Context context, int index) {
		if (index < 0 || index >= MAX_DOOR_NUM) return false;
		SharedPreferences prefs = getPreferences(context, index);
		return prefs.edit().clear().commit();
	}
	
	public static int getIndexById(Context context, String id) {
		for (int i=0; i<MAX_DOOR_NUM; i++) {
			if (read(context, i).id.equalsIgnoreCase(id))
				return i;
		}
		return Integer.MIN_VALUE;
	}
	
	public Door() {
		index = Integer.MIN_VALUE;
		id = "";
		account = "";
		password = "";
		odpName = "";
	}
	
	public Door(int index, String id, String account, String password, String odpName) {
		if (index < 0) index = Integer.MIN_VALUE;
		if (id == null) id = "";
		if (account == null) account = "";
		if (password == null) password = "";
		this.index = index;
		this.id = id;
		this.account = account;
		this.password = password;
		this.odpName = odpName;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		if (id == null) id = "";
		this.id = id;
	}
	
	public String getAccount() {
		return account;
	}
	
	public void setAccount(String account) {
		if (account == null) account = "";
		this.account = account;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		if (password == null) password = "";
		this.password = password;
	}
	
	public void setODPName(String name)
	{
		this.odpName = name;
	}
	public String getODPName()
	{
		return odpName;
	}
}
