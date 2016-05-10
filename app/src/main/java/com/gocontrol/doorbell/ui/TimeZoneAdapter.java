/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-4-27 PM2:44:01
 * Project: NortekDoorBell
 * PackageName: com.gocontrol.doorbell.ui
 */
package com.gocontrol.doorbell.ui;

import com.gocontrol.doorbell.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

/**
 * @author welly
 *
 */
public class TimeZoneAdapter extends BaseAdapter{

	private Context mContext;
	private String[] mSourceMsg;
	private String[] mSourceData;
	private int chooseOne;
	
	/**
	 * 
	 */
	public TimeZoneAdapter() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * @param mContext
	 * @param mSourceMsg
	 * @param mSourceData
	 * @param chooseOne 
	 */
	public TimeZoneAdapter(Context mContext, String[] mSourceMsg,
			String[] mSourceData, int chooseOne) {
		super();
		this.mContext = mContext;
		this.mSourceMsg = mSourceMsg;
		this.mSourceData = mSourceData;
		this.chooseOne = chooseOne;
	}
	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(mSourceMsg != null)
			return mSourceMsg.length;
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		View view = LayoutInflater.from(mContext).inflate(
				R.layout.timezone_list, null);

		CheckedTextView textData = (CheckedTextView) view
				.findViewById(R.id.text_data);
		TextView textMsg = (TextView) view.findViewById(R.id.text_msg);

		textData.setText(mSourceData[position]);
		textMsg.setText(mSourceMsg[position]);
		if (chooseOne == position)
			textData.setChecked(true);

		return view;
	}

}


