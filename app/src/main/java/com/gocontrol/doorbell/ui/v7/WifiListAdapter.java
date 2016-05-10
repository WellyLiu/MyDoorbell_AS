/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-3 ����11:17:55
 * Project: Cloud_Phone_UI
 * PackageName: com.tecom.cloud_phone_ui
 */
package com.gocontrol.doorbell.ui.v7;

import java.util.List;
import java.util.Map;

import com.gocontrol.doorbell.R;

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

public class WifiListAdapter extends BaseAdapter {  
	  
    private List<Map<String, Object>> data;  
    private LayoutInflater layoutInflater;  
    private Context context;  
    
    public WifiListAdapter(Context context,List<Map<String, Object>> data){  
        this.context=context;  
        this.data=data;  
        this.layoutInflater=LayoutInflater.from(context);  
    }  

    public final class wifiItem{  
        public ImageView image;  
        public TextView ssid;  
    }  
    @Override  
    public int getCount() {  
        return data.size();  
    }  

    @Override  
    public Object getItem(int position) {  
        return data.get(position);  
    }  

    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
    	wifiItem item=null;  
        if(convertView==null){  
        	item=new wifiItem();  
            //��������ʵ�������  
            convertView=layoutInflater.inflate(R.layout.wifi_list_item, null);  
            item.image=(ImageView)convertView.findViewById(R.id.wifi_img);  
            item.ssid=(TextView)convertView.findViewById(R.id.wifi_ssid);  
            
            convertView.setTag(item);  
        }else{  
        	item=(wifiItem)convertView.getTag();  
        }  
        //
        Object obj = data.get(position).get("image");
        if(obj != null)
        	item.image.setImageResource((Integer)obj);  
        else
        	item.image.setImageDrawable(null);  
        item.ssid.setText((String)data.get(position).get("title"));  
       
        return convertView;  
    }  
  
}  