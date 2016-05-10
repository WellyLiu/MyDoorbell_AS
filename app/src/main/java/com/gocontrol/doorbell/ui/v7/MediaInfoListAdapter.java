package com.gocontrol.doorbell.ui.v7;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.utils.Utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/** 
* @ClassName: MediaInfoListAdapter 
* @Description: TODO
* @author: Vincent Luo
* @email: wlluo@tecomtech.com
* @date: 2015-12-7 上午10:14:44 
*  
*/ 

public class MediaInfoListAdapter extends BaseAdapter {  
	  
	public static final int MEDIA_TYPE_CLIP = 1;
	public static final int MEDIA_TYPE_PICTURE = 0;
	public static final String KEY_THUMBNALL = "Thumbnail";
	public static final String KEY_TIME = "Time";
	public static final String KEY_PATH = "Path";
	public static final String KEY_DOOR_NAME = "DoorName";
	public static final String KEY_DURATION = "Duration";
	private static final String TAG = MediaInfoListAdapter.class.getSimpleName();
    private List<Map<String, Object>> data;  
    private LayoutInflater layoutInflater;  
    private Context context;  
    
    public MediaInfoListAdapter(Context context,List<Map<String, Object>> data){  
        this.context=context;  
        this.data=data;  
        this.layoutInflater=LayoutInflater.from(context);  
    }  
    public final class MediaItem{  
        public ImageView imageThumbnail;  
        public TextView time;
        public TextView doorName;
        //public TextView duration;//just for MEDIA_TYPE_CLIP
        public ImageView imageDel;  
    }  
    @Override  
    public int getCount() {  
        return data.size();  
    }  
    /** 
     * 获得某一位置的数据 
     */  
    @Override  
    public Object getItem(int position) {  
        return data.get(position);  
    }  
    /** 
     * 获得唯一标识 
     */  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
    	MediaItem item=null;  
        if(convertView==null){  
        	item=new MediaItem();  
            //获得组件，实例化组件  
            convertView=layoutInflater.inflate(R.layout.media_info_item, null);  
            item.imageThumbnail=(ImageView)convertView.findViewById(R.id.img);  
            item.time=(TextView)convertView.findViewById(R.id.txt_date); 
            item.doorName=(TextView)convertView.findViewById(R.id.txt_door_name);
            //item.duration=(TextView)convertView.findViewById(R.id.txt_duration);
            item.imageDel=(ImageView)convertView.findViewById(R.id.img_del);
            item.imageDel.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					/*
					String path = (String)v.getTag(R.id.btn_back);
					new File(path).delete();
					data.remove(((Integer)v.getTag(R.id.btn_menu)).intValue());
					notifyDataSetChanged();
					*/
					dialog(v);
				}
			});
            convertView.setTag(item);  
        }else{  
        	item=(MediaItem)convertView.getTag();  
        }  
        //绑定数据  
        Object obj = data.get(position).get(KEY_THUMBNALL);
        if(obj != null)
        	item.imageThumbnail.setImageBitmap((Bitmap)obj);  
        else
        	item.imageThumbnail.setImageResource(R.drawable.ic_launcher); 
        if(data.get(position).get(KEY_TIME) != null){
        	item.time.setText((String)data.get(position).get(KEY_TIME));  
        }
        if(data.get(position).get(KEY_DOOR_NAME) != null){
        	item.doorName.setText(Utils.getSubString((String)data.get(position).get(KEY_DOOR_NAME), 8));
        }
        /*
        if(data.get(position).get(KEY_DURATION) != null){
        	item.duration.setText((String)data.get(position).get(KEY_DURATION));
        }*/
        if(data.get(position).get(KEY_PATH) != null){
        	item.imageDel.setTag(R.id.btn_back,(String)data.get(position).get(KEY_PATH));
        	item.imageDel.setTag(R.id.btn_menu, position);
        }
        return convertView;  
    }  
  
	protected void dialog(final View v) {
    	AlertDialog.Builder builder = new Builder(context);
    	builder.setMessage(R.string.sure_to_delete);
    	builder.setPositiveButton(R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String path = (String)v.getTag(R.id.btn_back);
				new File(path).delete();
				data.remove(((Integer)v.getTag(R.id.btn_menu)).intValue());
				notifyDataSetChanged();
			}});
    	builder.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}});
    	builder.create().show();
	}
}  