/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2016-1-13 AM12:24:01
 * Project: TecomDoor
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.bean.ODPFeature;
import com.gocontrol.doorbell.model.ODPFeatureManager;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.utils.Utils;

import android.app.ActionBar;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * @author Administrator
 *
 */
public class UserSystemSettingDoorColorMode extends PNPBaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{

	private TextView mBrightness, mContrast, mHue, mSaturation, mSharpess;
	private SeekBar mBri, mContra, mH, mSatura, mSharp;
	private int value_temp;
	private int value_brigh, value_con, value_hue, value_satu, vlaue_sharp;
	
	private ODPFeature feature;
	private int odpId;
	private ODPInfo doorInfo;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		odpId = getIntent().getIntExtra("door_id", 0);
		doorInfo = ODPManager.getInstance().getOneODP(odpId);
		
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
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(getString(R.string.color_mode));
		
		txtTitle.setText(spanBuilder);
		
		setContentView(R.layout.system_setting_door_color_mode);
		getWindow().setBackgroundDrawable(null) ;
		
		initUIWidget();
		
		feature = ODPFeatureManager.getInstance()
				.getODPFeature();
		
		updateUIFromeODPData(feature);
	}

	/**
	 * @param feature2
	 */
	private void updateUIFromeODPData(ODPFeature feature) {
		// TODO Auto-generated method stub
		mBri.setProgress(feature.getBrightness());
		mContra.setProgress(feature.getContrast());
		mH.setProgress(feature.getHue());
		mSatura.setProgress(feature.getSaturation());
		mSharp.setProgress(feature.getSharpness());
	}

	/**
	 * 
	 */
	private void initUIWidget() {
		// TODO Auto-generated method stub
		mBrightness = (TextView)this.findViewById(R.id.brigntness_value);
		mContrast = (TextView)this.findViewById(R.id.contrast_value);
		mHue = (TextView)this.findViewById(R.id.hue_value);
		mSaturation = (TextView)this.findViewById(R.id.saturation_value);
		mSharpess = (TextView)this.findViewById(R.id.sharpness_value);
		
		mBri = (SeekBar)this.findViewById(R.id.brigntness);
		mBri.setOnSeekBarChangeListener(this);
		mContra = (SeekBar)this.findViewById(R.id.contrast);
		mContra.setOnSeekBarChangeListener(this);
		mH = (SeekBar)this.findViewById(R.id.hue);
		mH.setOnSeekBarChangeListener(this);
		mSatura = (SeekBar)this.findViewById(R.id.saturation);
		mSatura.setOnSeekBarChangeListener(this);
		mSharp = (SeekBar)this.findViewById(R.id.sharpness);
		mSharp.setOnSeekBarChangeListener(this);		
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

		//Utils.sendODPSetFeature(doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd(), feature);
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

	/* (non-Javadoc)
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar, int, boolean)
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		 switch(seekBar.getId()){  
	        case R.id.brigntness:  
	            value_temp = progress;  
	            
	            mBrightness.setText(String.valueOf(value_temp));
	            break;  
	        case R.id.contrast:  
	            value_temp = progress;  
	            mContrast.setText(String.valueOf(value_temp));
	            break;  
	        case R.id.hue:  
	            value_temp = progress;  
	            mHue.setText(String.valueOf(value_temp));
	            break;  
	        case R.id.saturation:  
	            value_temp = progress;  
	            mSaturation.setText(String.valueOf(value_temp));
	            break; 
	        case R.id.sharpness:  
	            value_temp = progress;  
	            mSharpess.setText(String.valueOf(value_temp)); 
	            break; 
	       
	        default:
	        	break;
		 }
	}

	/* (non-Javadoc)
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onStartTrackingTouch(android.widget.SeekBar)
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onStopTrackingTouch(android.widget.SeekBar)
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		switch(seekBar.getId()){  
        case R.id.brigntness:  
        	value_brigh = value_temp;
            Log.d("Tecom", "value_temp = " + value_temp);
            feature.setBrightness(value_brigh);
            break;  
        case R.id.contrast:  
        	Log.d("Tecom", "value_temp = " + value_temp);
        	value_con  = value_temp;
        	feature.setContrast(value_con);
            break;  
        case R.id.hue:  
        	Log.d("Tecom", "value_temp = " + value_temp);
        	value_hue = value_temp;
        	feature.setHue(value_hue);
            break;  
        case R.id.saturation:  
        	value_satu = value_temp;
        	feature.setSaturation(value_satu);
        	Log.d("Tecom", "value_temp = " + value_temp);
            break; 
        case R.id.sharpness:  
        	vlaue_sharp = value_temp;
        	feature.setSharpness(vlaue_sharp);
        	Log.d("Tecom", "value_temp = " + value_temp);
            break; 
      
        default:
        	break;
        	
	 }
		Utils.sendODPSetFeature(doorInfo.getOdpAccount(), doorInfo.getOdpLocalAccount(), doorInfo.getOdpLocalPwd(), feature);
	}

	
}
