/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-10-21 PM2:12:08
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.datasource
 */
package com.gocontrol.doorbell.datasource;

import java.util.HashSet;

import com.gocontrol.doorbell.AppApplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author Administrator
 *
 */
public class DataHelper {
	public static interface ChangeListener {
		public void dataChanged();
	}

    private static String DB_NAME = "doorphone.db";

    private static int DB_VERSION = 2;
    private SQLiteDatabase db;
    private SqliteHelper dbHelper;

    private static DataHelper mInstance;
    private final HashSet<ChangeListener> listeners=new HashSet<ChangeListener>();
    private DataHelper(Context context) {
          dbHelper = new SqliteHelper(context, DB_NAME, null, DB_VERSION );
          db = dbHelper.getWritableDatabase();
    }

    public static DataHelper getInstance()
    {
    	if(mInstance == null)
    		mInstance = new DataHelper(AppApplication.getInstance());
    	
    	return mInstance;
    }
    
    public static DataHelper getInstance(Context mContext)
    {
    	if(mInstance == null)
    		mInstance = new DataHelper(mContext);
    	
    	return mInstance;
    }
    public void Close() {
          db.close();
          dbHelper.close();
    }
    

    public Long AddUserInfo(CallLogInfo user) {
         ContentValues values = new ContentValues();
         values.put(CallLogInfo. CALLID, user.getCallId());
         values.put(CallLogInfo. DOORNAME, user.getDoorName());
         values.put(CallLogInfo. DOORPEERID, user.getDoorPeerId());
         values.put(CallLogInfo. CALLTIME, user.getCallTime());
         values.put(CallLogInfo. CALLTYPE, user.getCallType());
         Long cid = db.insert(SqliteHelper. TB_NAME, CallLogInfo.ID, values);
         Log. e("Vincent", "SaveUserInfo, " + cid + "");
         return cid;
    }
    

    public Long AddUserInfoData(String callID, String doorName,String doorPeerId, String callTime, int callType) {
         ContentValues values = new ContentValues();
         values.put(CallLogInfo. CALLID, callID);
         values.put(CallLogInfo. DOORNAME, doorName);
         values.put(CallLogInfo. DOORPEERID, doorPeerId);
         values.put(CallLogInfo. CALLTIME, callTime);
         values.put(CallLogInfo. CALLTYPE, callType);
         Long cid = db.insert(SqliteHelper. TB_NAME, CallLogInfo.ID, values);
         Log. e("Vincent", "SaveUserInfoData, " +  cid + "");
         for(ChangeListener listener:listeners) {
 			listener.dataChanged();
 		}
         return cid;
    }
    public void deleteCallLog(int deleteId) {
    	db.delete(SqliteHelper.TB_NAME,
    			 CallLogInfo.ID + " = " + deleteId, null);
		
		for(ChangeListener listener:listeners) {
			listener.dataChanged();
		}
	}
	/**
	 * Adds a listener to receive call log data changed events
	 * 
	 * @param listener
	 */
	public void addListener(ChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 Removes a listener to receive call log data changed events
	 *
	 * @param listener
	 */
	public void removeListener(ChangeListener listener) {
		listeners.remove(listener);
	}
    //�õ�call log��Ŀ
    public long getCount() {  
        Cursor cursor = db.rawQuery("select count(*) from calllogs", null);  
        cursor.moveToFirst();  
        return cursor.getLong(0);  
    }  
    
	// ɾ��call log��ļ�¼
	public int DelUserInfo() {
		int id = db.delete(SqliteHelper.TB_NAME, null, null);
		Log.e("Vincent", id + "");
		for(ChangeListener listener:listeners) {
 			listener.dataChanged();
 		}
		return id;
	}
	
	//�õ����е�����
	public Cursor getAllDataItems()
	{
		Cursor cursor = db.query(SqliteHelper.TB_NAME, null, null, null, null, null, CallLogInfo.ID + " DESC");    
        cursor.moveToFirst();  
        return cursor;
	}

	/**
	 * @param doorPeerId
	 *            �õ�����door peer id��call log
	 * @return
	 */
	public Cursor getAllDataItems(String doorPeerId) {
		// TODO Auto-generated method stub
		Log.d("tecom", "getAllDataItems ...... " + doorPeerId);

		Cursor cursor;
		if (TextUtils.isEmpty(doorPeerId)) {
			cursor = db.query(SqliteHelper.TB_NAME, null, null, null,
					null, null, CallLogInfo.ID + " DESC");
			cursor.moveToFirst();
		} else {
			String selection = "doorPeerId=?";
			String[] selectionArgs = new String[] { doorPeerId };

			cursor = db.query(SqliteHelper.TB_NAME, null, selection,
					selectionArgs, null, null, CallLogInfo.ID + " DESC");

			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * @param doorPeerId
	 *            ɾ������door peer id��call log
	 */
	public int DelUserInfo(String doorPeerId) {
		// TODO Auto-generated method stub
		Log.d("tecom", "DelUserInfo ...... " + doorPeerId);
		int id;
		if (TextUtils.isEmpty(doorPeerId)) {
			id = db.delete(SqliteHelper.TB_NAME, null, null);

			for (ChangeListener listener : listeners) {
				listener.dataChanged();
			}
		} else {
			String selection = "doorPeerId=?";
			String[] selectionArgs = new String[] { doorPeerId };

			id = db.delete(SqliteHelper.TB_NAME, selection, selectionArgs);

			for (ChangeListener listener : listeners) {
				listener.dataChanged();
			}
		}
		return id;
	}

	/**
	 * @param account, the ODP account (peer id)
	 * @param string, the ODP new name.
	 */
	public void updateCallLogDoorName(String account, String name) {
		// TODO Auto-generated method stub
		Log.d("tecom", "updateCallLogDoorName ...... " + account + "..." + name);
		if(TextUtils.isEmpty(account) || TextUtils.isEmpty(name))
			return;
		
		ContentValues values = new ContentValues();
		String selection = "doorPeerId=?";
		String[] selectionArgs = new String[] { account };
		values.put(CallLogInfo. DOORNAME, name);//keyΪ�ֶ�����valueΪֵ
		db.update(SqliteHelper.TB_NAME, values, selection, selectionArgs); 
		
	}

}
