/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-7 上午10:04:43
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import java.util.ArrayList;
import java.util.List;

import com.gocontrol.doorbell.AppApplication;
import com.gocontrol.doorbell.R;
import com.gocontrol.doorbell.model.ODPInfo;
import com.gocontrol.doorbell.model.ODPManager;
import com.gocontrol.doorbell.model.SystemConfigManager;
import com.gocontrol.doorbell.ui.v7.UserSystemSettingsDoor.DIALOG_ITEM;
import com.gocontrol.doorbell.utils.Utils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Administrator
 *
 */
public class UserSystemSettings extends PNPBaseActivity implements View.OnClickListener{

	private static final String TAG = UserSystemSettings.class.getSimpleName();
	
	private LinearLayout mRingtone;
	private String mRingname;
	private TextView mSelectRing;
	private LinearLayout mMicoVol;
	private TextView mMicoVolShow;
	private CheckBox softwareAec;
	private int mSoftwareAec;
	
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private Context mContext;
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
		ColorStateList redColors = ColorStateList.valueOf(getResources().getColor(R.color.btn_hangup_bg_color));
		
		txtTitle.setText(R.string.system_setting_title);
		
		setContentView(R.layout.system_setting);
		getWindow().setBackgroundDrawable(null) ;
		
		mMicoVol = (LinearLayout)this.findViewById(R.id.mic_volume);
		mMicoVol.setOnClickListener(this);
		
		mMicoVolShow = (TextView)this.findViewById(R.id.mic_volume_show);
		mMicoVolShow.setText(DialogItems[1][mMicValue-1]);
		
		listView = (ListView) findViewById(R.id.odp_list);  
	 
	    listView.setOnItemClickListener(new OnItemClickListener()
	    {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				//以odp list中的数据做处理源头。door id指在list中位置
				startActivity(new Intent(mContext, UserSystemSettingsDoor.class).putExtra("door_id", position));
				
			}
	    	
	    });
		
		mRingtone = ( LinearLayout )this.findViewById(R.id.user_ringtone);
		
		mRingtone.setOnClickListener(this);

		mSelectRing=(TextView) findViewById(R.id.select_ringtone);
		int chooseIndex = getSharedPreferences("ring", 0).getInt("current_ring_index", 0);
		String ringtoneName = "";
		if (chooseIndex == 0)
			ringtoneName = getString(R.string.nortek_ringtone_1);
		if (chooseIndex == 1)
			ringtoneName = getString(R.string.nortek_ringtone_2);
		if (chooseIndex == 2)
			ringtoneName = getString(R.string.nortek_ringtone_3);
		mSelectRing.setText(ringtoneName);
		
		getSFData();
		
		softwareAec = (CheckBox)this.findViewById(R.id.enable_software_aec);

		if(mSoftwareAec == 1){
			softwareAec.setChecked(true);
		}else{
			softwareAec.setChecked(false);
		}
		
		softwareAec.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					mSoftwareAec = 1;
					Toast.makeText(UserSystemSettings.this, getString(R.string.webrtc_aec_notice_toast), Toast.LENGTH_LONG).show();
				}
				else{
					mSoftwareAec = 0;
				}
			}
		});
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		storeSFData();
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
		list.clear();
		adapter=new ArrayAdapter<String>(this,R.layout.simple_txt_item, getListData());    
		listView.setAdapter(adapter);  
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		//store the audio parameter.
		Utils.saveAudioConfig(mContext, mMicValue);
		
		super.onDestroy();
	}

	int value_m;
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
			
		case R.id.user_ringtone:
			//startActivity(new Intent(this, DoorPhoneRingTone.class));
			startActivityForResult(new Intent(this, DoorPhoneRingTone.class), 1);
			break;		
			
		case R.id.mic_volume:
			//showChooseDialog(DIALOG_ITEM.MICRO);
			
			value_m = SystemConfigManager.getInstance().getMicVol() -1 ;
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dialog_seekbar_2,
					(ViewGroup) findViewById(R.id.dialog_root));
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setView(layout);
			builder.setTitle(R.string.mic_volume);
			builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					
					mMicoVolShow.setText( DialogItems[1][value_m]);
					SystemConfigManager.getInstance().setMicVol(value_m+1);
					
				}
				}
				);
			builder.setNegativeButton(mContext.getString(R.string.cancel), null);
			
			AlertDialog alertDialog = builder.create();					
			SeekBar sb = (SeekBar) layout.findViewById(R.id.dialog_seekbar);
			sb.setProgress(SystemConfigManager.getInstance().getMicVol() - 1);
			sb.setMax(4);
			final TextView mVol = (TextView)layout.findViewById(R.id.val_value);
			mVol.setText(DialogItems[1][SystemConfigManager.getInstance().getMicVol() - 1]);
			sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {			
				

				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// Do something here with new value
					
					value_m = progress;  
					mVol.setText( DialogItems[1][progress]);    
					//mMicoVolShow.setText( DialogItems[1][progress]);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
					
				}
			});
			
			alertDialog.show();
			
			break;
			
		default:
			break;
		}
	}
	

	/**
	 * @param speaker
	 * @return
	 */
	private int getShowWhichIndex(DIALOG_ITEM speaker) {
		// TODO Auto-generated method stub
		int ret = 0;
		switch(speaker)
		{
		
		case MICRO:	
			int mVol = SystemConfigManager.getInstance().getMicVol();
			ret = mVol -1;
			break;
		default:
			break;
		}
		return ret;
	}
	
	private int mMicValue  = SystemConfigManager.getInstance().getMicVol();
	private int index;
	private String [][] DialogItems = new String[][]{
			new String[]{AppApplication.getInstance().getString(R.string.speaker_1),
					AppApplication.getInstance().getString(R.string.speaker_2),
					AppApplication.getInstance().getString(R.string.speaker_3),
					AppApplication.getInstance().getString(R.string.speaker_4),
					AppApplication.getInstance().getString(R.string.speaker_5)},
			new String[]{AppApplication.getInstance().getString(R.string.mic_1),
					AppApplication.getInstance().getString(R.string.mic_2),
					AppApplication.getInstance().getString(R.string.mic_3),
					AppApplication.getInstance().getString(R.string.mic_4),
					AppApplication.getInstance().getString(R.string.mic_5)}}
			;
	/**
	 * @param speaker
	 */
	private void showChooseDialog(final DIALOG_ITEM speaker) {
		// TODO Auto-generated method stub
		String[] items = null;
		String title = "";
		
		switch(speaker)
		{
		
		case MICRO:
			//items = new String[]{"1", "2" , "3" , "4", "5"};
			items = DialogItems[1];
			title = mContext.getString(R.string.mic_volume);
			break;
		
		default:
			break;
		}
		new AlertDialog.Builder(this)
				.setTitle(title)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setSingleChoiceItems(items, getShowWhichIndex(speaker),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {								
								index = which;
							}
						}).setNegativeButton(mContext.getString(R.string.cancel), null)
						.setPositiveButton(mContext.getString(R.string.ok), new OnClickListener()
						{

							

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								switch(speaker)
								{
								
								case MICRO:
									mMicValue = index +1;
									SystemConfigManager.getInstance().setMicVol(mMicValue);
									mMicoVolShow.setText(DialogItems[1][index]);
									break;
									
								default:
									break;
								}
								}							
							
						}).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			if(data!=null){
				mRingname=data.getStringExtra("ringname");
				mSelectRing.setText(mRingname);
			}
			break;

		default:
			break;
		}
	}
	
	private void storeSFData(){
		SharedPreferences.Editor editor = getSharedPreferences("cloud2door_aec", MODE_PRIVATE).edit();
		editor.putInt("software_aec", mSoftwareAec);
		editor.commit();
	}
	
	private void getSFData(){
		SharedPreferences pref = getSharedPreferences("cloud2door_aec", MODE_PRIVATE);
		mSoftwareAec = pref.getInt("software_aec", 0);
	}
	
}
