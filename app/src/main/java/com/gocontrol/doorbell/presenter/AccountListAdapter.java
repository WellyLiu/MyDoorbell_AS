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
import com.gocontrol.doorbell.bean.AccountStatusBean;
import com.gocontrol.doorbell.bean.LocalUserInfo;

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
public class AccountListAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<AccountStatusBean> list;

	/**
	 * 
	 */
	public AccountListAdapter(Context mContext,
			ArrayList<AccountStatusBean> mlist) {
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
            rowView=inflater.inflate(R.layout.view_account_list,parent,false);
 
            ViewHolder viewHolder=new ViewHolder();
            viewHolder.name=(TextView)rowView.findViewById(R.id.name);
            viewHolder.eMail=(TextView)rowView.findViewById(R.id.email);
            viewHolder.status=(ImageView)rowView.findViewById(R.id.status);
            viewHolder.delete = (ImageView)rowView.findViewById(R.id.delete);
            rowView.setTag(viewHolder);
        }
 
        ViewHolder holder=(ViewHolder)rowView.getTag();
        
        AccountStatusBean one = list.get(position);
        holder.name.setText(one.getName());
        holder.eMail.setText(one.getShowEmail());
        if( one.getShowEmail().equalsIgnoreCase(LocalUserInfo.getInstance().getC2cAccount()))
        {
        	holder.delete.setVisibility(View.INVISIBLE);
        }else
        {
        	holder.delete.setVisibility(View.VISIBLE);
        }
        //1:online   2: offline 3
        int s = one.getStatus();
        if( 1 == s )
        	holder.status.setImageResource(R.drawable.all_account_status_online);
        else 
        	if( 2 == s )
        		holder.status.setImageResource(R.drawable.all_account_status_offline);
        	else if( 3 == s)
        		holder.status.setImageResource(R.drawable.alert);
        
        return rowView;
	}

	public static class ViewHolder {
		TextView name;
		TextView eMail;
		ImageView status;
		ImageView delete;
	}
}
