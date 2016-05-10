/**
 * Author: welly
 * Email: wliu@tecomtech.com
 * Data: 2015-12-7 PM4:56:37
 * Project: Cloud_Phone_Demo
 * PackageName: com.tecom.door.ui.v7
 */
package com.gocontrol.doorbell.ui.v7;

import com.gocontrol.doorbell.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Administrator
 * 
 */
public class DoorPhoneRingTone extends Activity implements View.OnClickListener {

	protected static final String TAG = null;
	private String ringtoneName;

	SharedPreferences sp;
	SharedPreferences.Editor spe;

	private MediaPlayer mMediaPlayer;

	private int chooseItem;
	private TextView ringtoneTxtOne, ringtoneTxtTwo, ringtoneTxtThree;
	private CheckBox ringtoneChOne, ringtoneChTwo, ringtoneChThree;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		View viewTitleBar = getLayoutInflater().inflate(R.layout.actionbar,
				null);
		getActionBar().setCustomView(viewTitleBar, lp);
		getActionBar().setDisplayShowCustomEnabled(true);
		ImageView img = (ImageView) getActionBar().getCustomView()
				.findViewById(R.id.btn_back);
		img.setOnClickListener(this);

		TextView txtTitle = (TextView) getActionBar().getCustomView()
				.findViewById(android.R.id.title);
		SpannableStringBuilder spanBuilder = new SpannableStringBuilder(
				"Ringtone");
		txtTitle.setText(spanBuilder);

		setContentView(R.layout.door_phone_ring_tone);
		getWindow().setBackgroundDrawable(null) ;
		
		sp = getSharedPreferences("ring", 0);
		int chooseIndex = sp.getInt("current_ring_index", 0);
		chooseItem = chooseIndex;
		spe = sp.edit();

		ringtoneTxtOne = (TextView) this.findViewById(R.id.ringtone_txt_one);
		ringtoneTxtOne.setOnClickListener(this);
		ringtoneTxtTwo = (TextView) this.findViewById(R.id.ringtone_txt_two);
		ringtoneTxtTwo.setOnClickListener(this);
		ringtoneTxtThree = (TextView) this
				.findViewById(R.id.ringtone_txt_three);
		ringtoneTxtThree.setOnClickListener(this);

		ringtoneChOne = (CheckBox) this.findViewById(R.id.ringtone_check_one);
		ringtoneChTwo = (CheckBox) this.findViewById(R.id.ringtone_check_two);
		ringtoneChThree = (CheckBox) this
				.findViewById(R.id.ringtone_check_three);
		if (chooseIndex == 0)
			ringtoneChOne.setChecked(true);
		if (chooseIndex == 1)
			ringtoneChTwo.setChecked(true);
		if (chooseIndex == 2)
			ringtoneChThree.setChecked(true);
		ringtoneChOne.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					chooseItem = 0;

					ringtoneChTwo.setChecked(false);
					ringtoneChThree.setChecked(false);
					
					startPlayRing(0);
				}else
				{
					stopPlayRing();
				}
			}

		});
		ringtoneChTwo.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					chooseItem = 1;
					ringtoneChOne.setChecked(false);
					ringtoneChThree.setChecked(false);
					
					startPlayRing(1);
				}else
				{
					stopPlayRing();
				}
			}

		});
		ringtoneChThree
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked) {
							chooseItem = 2;
							ringtoneChTwo.setChecked(false);
							ringtoneChOne.setChecked(false);
							
							startPlayRing(2);
						}else
						{
							stopPlayRing();
						}
					}

				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mMediaPlayer != null)
			mMediaPlayer.release();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_back:
			spe.putInt("current_ring_index", chooseItem).commit();

			if (chooseItem == 0)
				ringtoneName = getString(R.string.nortek_ringtone_1);
			if (chooseItem == 1)
				ringtoneName = getString(R.string.nortek_ringtone_2);
			if (chooseItem == 2)
				ringtoneName = getString(R.string.nortek_ringtone_3);

			Intent intent = new Intent();
			intent.putExtra("ringname", ringtoneName);
			setResult(RESULT_OK, intent);

			finish();
			break;
		case R.id.ringtone_txt_one:
			//startPlayRing(0);
			break;
		case R.id.ringtone_txt_two:
			//startPlayRing(1);
			break;
		case R.id.ringtone_txt_three:
			//startPlayRing(2);
			break;
		default:
			break;
		}
	}

	public void stopPlayRing()
	{
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
	public void startPlayRing(int current_ring_index) {

		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}

		if (mMediaPlayer == null) {
			if (current_ring_index == 0)
				mMediaPlayer = MediaPlayer.create(this,
						R.raw.ringtone_60seconds_1);
			else if (current_ring_index == 1)
				mMediaPlayer = MediaPlayer.create(this,
						R.raw.ringtone_60seconds_2);
			else if (current_ring_index == 2)
				mMediaPlayer = MediaPlayer.create(this,
						R.raw.ringtone_60seconds_3);
		}

		if( mMediaPlayer != null)
		try {
			mMediaPlayer.start();
			mMediaPlayer.setLooping(false);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			spe.putInt("current_ring_index", chooseItem).commit();

			if (chooseItem == 0)
				ringtoneName = getString(R.string.nortek_ringtone_1);
			if (chooseItem == 1)
				ringtoneName = getString(R.string.nortek_ringtone_2);
			if (chooseItem == 2)
				ringtoneName = getString(R.string.nortek_ringtone_3);

			Intent intent = new Intent();
			intent.putExtra("ringname", ringtoneName);
			setResult(RESULT_OK, intent);

			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
