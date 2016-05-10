/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-10-21 PM1:57:21
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.datasource
 */
package com.gocontrol.doorbell.datasource;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Administrator
 *
 */
public class SqliteHelper extends SQLiteOpenHelper{

	 public static final String TB_NAME= "calllogs";
	 
	 public SqliteHelper(Context context, String name, CursorFactory factory, int version) {
	     super(context, name, factory, version);
	 }

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		 db.execSQL( "CREATE TABLE IF NOT EXISTS "+
	                TB_NAME+ "("+
	                CallLogInfo. ID + " integer primary key,"+
	                CallLogInfo. CALLID + " text,"+
	                CallLogInfo. DOORPEERID + " text,"+
	                CallLogInfo. DOORNAME + " text,"+
	                CallLogInfo. CALLTIME + " text,"+
	                CallLogInfo. CALLTYPE + " text"+
	                ")"
	                );
	        Log. e("Database" ,"onCreate" );
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		  db.execSQL( "DROP TABLE IF EXISTS " + TB_NAME );
	      onCreate(db);
	      Log. e("Database" ,"onUpgrade" );
	}
	    
}
