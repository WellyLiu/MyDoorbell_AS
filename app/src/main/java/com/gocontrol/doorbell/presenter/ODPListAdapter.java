/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-7 PM3:03:56
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.presenter
 */
package com.gocontrol.doorbell.presenter;

import java.util.ArrayList;

import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.model.ODPInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Administrator
 * 
 */
public class ODPListAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<ODPInfo> list;

	/**
	 * 
	 */
	public ODPListAdapter(Context mContext,
			ArrayList<ODPInfo> mlist) {
		super();
		// TODO Auto-generated constructor stub
		list = mlist;
		this.mContext = mContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (list != null)
			return list.size();
		else
			return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (list != null)
			return list.get(position);
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		if (list != null)
			return position;
		else
			return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View rowView=convertView;  //reference to one of the previous Views in the list that we can reuse.
		 
        if(convertView==null){
 
            LayoutInflater inflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView=inflater.inflate(R.layout.view_odp_list,parent,false);
 
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView)rowView.findViewById(R.id.door_txt);
            viewHolder.odp = (ImageView)rowView.findViewById(R.id.door_img);
            viewHolder.missCallNum = (TextView)rowView.findViewById(R.id.door_log_num);
            rowView.setTag(viewHolder);
        }
 
        ViewHolder holder=(ViewHolder)rowView.getTag();
        
        ODPInfo one = list.get(position);
        holder.name.setText(one.getOdpName());
        
        int missCallNumber = one.getOdpMissedCallNum();
        if(missCallNumber <=0)
        {
        	holder.missCallNum.setVisibility(View.GONE);
        }else{
        	holder.missCallNum.setVisibility(View.VISIBLE);
        	holder.missCallNum.setText(mContext.getString(R.string.nortek_miss_call) + missCallNumber);
        }
        
        int s = one.getOdpState();
        if( 1 == s){
        	holder.odp.setImageResource(R.drawable.door_phone_active);
        	holder.name.setTextColor(mContext.getResources().getColor(R.color.text_color_blue));
        }
        else if( 2 == s){
        	//holder.name.setTextColor(mContext.getResources().getColor(R.color.gray));
        		holder.odp.setImageResource(R.drawable.door_phone_inactive);
        }
        else if( 3 == s){
        	//holder.name.setTextColor(mContext.getResources().getColor(R.color.text_color_blue));
        	holder.odp.setImageResource(R.drawable.door_phone_no_pnp);
        }else if( 0 == s){
        	//holder.name.setTextColor(mContext.getResources().getColor(R.color.gray));
    		holder.odp.setImageResource(R.drawable.door_phone_inactive);
        }
        
        return rowView;
	}

	public static class ViewHolder {
		TextView name;
		ImageView odp;
		TextView missCallNum;
	}
}
