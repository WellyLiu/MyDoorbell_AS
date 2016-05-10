/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-10-23 下午4:51:06
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui
 */
package com.gocontrol.doorbell.ui;
import java.io.File;

import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.datasource.DataHelper;
import com.gocontrol.doorbell.utils.Utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;



/**
 * @author Administrator
 *
 */
public class CallLogAdapter extends CursorAdapter {

	  private LayoutInflater mInflater;  
      private Context mContext;  
	
	/**
	 * @param context
	 * @param c
	 * @param flags
	 */
	public CallLogAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		// TODO Auto-generated constructor stub
		 mContext = context;  
         mInflater = LayoutInflater.from(context);
	}

	/**
	 * @param context
	 * @param c
	 * @deprecated
	 */
	public CallLogAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
		mContext = context;  
        mInflater = LayoutInflater.from(context);
	}

	/* (non-Javadoc)
	 * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		TextView noteTime = (TextView) view.findViewById(R.id.txt_date);  
        TextView noteName = (TextView) view.findViewById(R.id.txt_door_name); 
        ImageView typeImg = (ImageView)view.findViewById(R.id.img);
        ImageView del = (ImageView)view.findViewById(R.id.img_del);
        del.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//int id = ((Integer)v.getTag()).intValue();
				//DataHelper.getInstance(mContext).deleteCallLog(id);
				
				dialog(v);
			}
		});
        del.setTag(cursor.getInt(0));
        //Log.d("xxxx", "cur="+cursor.getCount()+",c_count="+cursor.getColumnCount());  
        //Log.d("tst", cursor.getString(1) + " " + cursor.getString(2) + " " + cursor.getString(3) + "  " + cursor.getString(4));
        noteTime.setText(" " + cursor.getString(4));         
        
        String type = cursor.getString(5);
        //noteType.getPaint().setFakeBoldText(true);
        String tmp = Utils.getSubString(cursor.getString(3), 8);
        if(type.equalsIgnoreCase("1")){
        	typeImg.setImageResource(R.drawable.call_normal);        	
        	noteName.setText(" " + tmp + " " + mContext.getString(R.string.nortek_call_answered));
        }else{ 
        	typeImg.setImageResource(R.drawable.call_miss);
        	noteName.setText(" " + tmp + " " + mContext.getString(R.string.nortek_call_missed));
        }
	}

	/* (non-Javadoc)
	 * @see android.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
	 */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		  return mInflater.inflate(R.layout.calllog_info_item, parent, false);  
	}

	protected void dialog(final View v) {
    	AlertDialog.Builder builder = new Builder(mContext);
    	builder.setMessage(R.string.sure_to_delete);
    	builder.setPositiveButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				int id = ((Integer)v.getTag()).intValue();
				DataHelper.getInstance().deleteCallLog(id);
			}});
    	builder.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}});
    	builder.create().show();
	}
}
