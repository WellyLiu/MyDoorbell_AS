/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-7 AM10:05:04
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import java.util.ArrayList;
import java.util.List;

import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author Administrator
 *
 */
public class UserAboutDevice extends PNPBaseActivity implements View.OnClickListener{

	private ListView odpList;
	private ArrayAdapter adapter;
	private Context mContext;
	private TextView smpVersion;
	private TextView mManufacture, mModel,mAndroidVersion, mBuildInEC;
	private String mManu, mMod, mAnVersion, mBuild;
	
	private List<String> list = new ArrayList<String>();
	public List<String> getListData(){  
        for(ODPInfo one : ODPManager.getInstance().getODPList())
        {
        	list.add(one.getOdpName());
        }
        return list;  
    }  
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		ImageView img = (ImageView) getActionBar().getCustomView().findViewById(R.id.btn_back);
		img.setOnClickListener(this);
		
		TextView txtTitle = (TextView) getActionBar().getCustomView().findViewById(android.R.id.title);
		txtTitle.setText(R.string.about_device_title);
		
		setContentView(R.layout.setting_about_device);
		getWindow().setBackgroundDrawable(null) ;
		
		smpVersion = (TextView)this.findViewById(R.id.smp_version);
		smpVersion.setText(this.getString(R.string.app_version));
		
		odpList = (ListView)this.findViewById(R.id.odp_list);
		
	    adapter=new ArrayAdapter<String>(this,R.layout.simple_txt_item, getListData());    
	    odpList.setAdapter(adapter);  
	    odpList.setOnItemClickListener(new OnItemClickListener()
	    {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				startActivity(new Intent(mContext, UserAboutDeviceDoor.class).putExtra("door_id", position));
				
			}
	    	
	    });
	    
	    getSMPInfo();
	    mManufacture = (TextView)this.findViewById(R.id.function_1_show);
	    mManufacture.setText(mManu);
	    mModel = (TextView)this.findViewById(R.id.function_2_show);
	    mModel.setText(mMod);
	    mAndroidVersion = (TextView)this.findViewById(R.id.function_3_show);
	    mAndroidVersion.setText(mAnVersion);
	    mBuildInEC = (TextView)this.findViewById(R.id.function_4_show);
	    mBuildInEC.setText(mBuild);
	}

	/**
	 * 
	 */
	private void getSMPInfo() {
		// TODO Auto-generated method stub
		mManu = getString(R.string.manufacture) + android.os.Build.MANUFACTURER;
		mMod =  getString(R.string.smp_model) + android.os.Build.MODEL ;
		mAnVersion = getString(R.string.android_version) + android.os.Build.VERSION.RELEASE;
		mBuild =  getString(R.string.aec) + (isDeviceSupport()== true? "Yes":"No");
	}
	
	public static boolean isDeviceSupport()
	{
	        return AcousticEchoCanceler.isAvailable();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		
		case R.id.btn_back:
			finish();
			break;
		default:
			break;
		}
	}

	
}